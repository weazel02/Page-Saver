package kled.pagesaver;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Telephony;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PSMapActivity extends FragmentActivity
        implements OnMapReadyCallback,OnProgressBarListener {

    private GoogleMap mMap;
    private URL url;
    public static final String PLACE_MARKER_MODE = "marker mode";
    public static final String MAP_MODE = "map mode";
    public static final String LOCATIONS_LIST = "locations list";
    public static final String BOOKS_LIST = "books list";
    public static final String VIEW_ALL_ENTRIES = "view all entries";
    public static final String VIEW_SINGLE_ENTRY = "view single entry";
    public static final String LAT_KEY = "lat";
    public static final String LNG_KEY = "long";


    private NumberProgressBar bnp;
    MyTrackingService myTrackingService;
    private Intent mServiceIntent;
    private boolean isPlaceMarkerMode;
    private boolean viewAllEntries;
    String bookISBN;
    AlertDialog alert;
    Marker currMarker;
    int animationIndex = 0;
    ImageView bookImage;

    EditText search;
    Button searchButton;

    private ArrayList<LatLng> traceLocations;
    private ArrayList<String> mapText;
    private ArrayList<String> progressValues;

    LatLng startLocation;
    LatLng firstLocation;
    LatLng endLocation;
    double curMaxProgress;



    private CardView cView;
    private boolean isBound;
    ArrayList<LatLng> savedLocations;
    ArrayList<String> booksAtLocation;
    private double curLat;
    private double curLong;
    ArrayList<int[]> list;
    Marker mark;

    //Callback for multiple animations
    GoogleMap.CancelableCallback animationCancelableCallback =
            new GoogleMap.CancelableCallback() {

                @Override
                public void onCancel() {
                }

                @Override
                public void onFinish() {


                    if (animationIndex < savedLocations.size()) {
                        Log.d("OnFinish", "Animation Index: " + animationIndex);

                        LatLng curLatLng = savedLocations.get(animationIndex);
                        LatLng targetLatLng = mMap.getCameraPosition().target;
                        LatLng nextLatLng;
                        if (animationIndex + 1 > savedLocations.size() - 1) {
                            nextLatLng = savedLocations.get(animationIndex);
                        } else {
                            nextLatLng = savedLocations.get(animationIndex + 1);
                        }
                        traceLocations.add(savedLocations.get(animationIndex));

                        startLocation = curLatLng;
                        endLocation = nextLatLng;

                        CameraPosition cameraPosition =
                                new CameraPosition.Builder()
                                        .target(savedLocations.get(animationIndex))
                                        .tilt(animationIndex < savedLocations.size() - 1 ? 90 : 0)
                                        .bearing(bearingBetweenLatLngs(targetLatLng, curLatLng))
                                        .zoom(mMap.getCameraPosition().zoom)
                                        .build();


                        mark = mMap.addMarker(new MarkerOptions()
                                .position(savedLocations.get(animationIndex))
                                .title(booksAtLocation.get(animationIndex))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_book)));


                        animateBar();
                        animationIndex++;
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                3000,
                                animationCancelableCallback);
                    }
                }
            };


    @Override
    public void onProgressChange(int current, int max) {
        if (current == max) {

        }
    }

    //Function that begins the map animation between different locations
    public void animateMap() {
        animationIndex = 0;
        traceLocations = new ArrayList<LatLng>();
        traceLocations.add(savedLocations.get(animationIndex));
        firstLocation = savedLocations.get(animationIndex);

        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(savedLocations.get(animationIndex))
                        .tilt(animationIndex < savedLocations.size() - 1 ? 90 : 0)
                        //.bearing((float)heading)
                        .zoom(18)
                        .build();

        mMap.addMarker(new MarkerOptions().position(savedLocations.get(animationIndex))
                .title(booksAtLocation.get(animationIndex))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_book)));


        bnp = (NumberProgressBar) findViewById(R.id.progress_bar);
        bnp.setOnProgressBarListener(this);

        animateBar();
        animationIndex++;

        mMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(cameraPosition),
                3000,
                animationCancelableCallback);


    }

    //Function that animates the progress bar
    public void animateBar() {
        curMaxProgress = Double.valueOf(progressValues.get(animationIndex));

        Log.d("animateBar", String.valueOf(curMaxProgress));

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((double) bnp.getProgress() < curMaxProgress) {
                            bnp.incrementProgressBy(1);

                        } else {
                            bnp.setProgress((int) curMaxProgress);
                            timer.cancel();
                            timer.purge();
                        }
                    }
                });
            }
        }, 500, 50);

    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            MyTrackingService.TrackingServiceBinder binder =
                    (MyTrackingService.TrackingServiceBinder) service;
            myTrackingService = binder.getService();

        }


        public void onServiceDisconnected(ComponentName name) {
            myTrackingService = null;
        }
    };

    private LocationUpdateReceiver myLocationReceiver;

    public class LocationUpdateReceiver extends BroadcastReceiver {
        //Receive broadcast that new location data has been addded
        @Override
        public void onReceive(Context ctx, Intent intent) {
            Log.d("PSMapsActivity", "OnReceive Called");
            //Get the intent from the Tracking Service and set the current Longitude/Latitude
            if (intent != null) {
                if (intent.getExtras() != null) {
                    Bundle extras = intent.getExtras();
                    curLat = (double) extras.get("lat");
                    curLong = (double) extras.get("long");

                }
            }

            if(isPlaceMarkerMode) {

                redrawUI();
            }
        }

    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        animationIndex = 0;

        setContentView(R.layout.activity_psmap);


        //Initialize arrays for storage of locations and the corresponding books
        if (savedLocations == null) {
            savedLocations = new ArrayList<LatLng>();
        }
        if (booksAtLocation == null) {
            booksAtLocation = new ArrayList<String>();
        }

        //Async setup of Map
        setUpMapIfNeeded();

        //Get the intent in order to determine what map mode we are in
        Intent mapIntent = getIntent();
        Bundle extras = mapIntent.getExtras();

        //Connect the receiver for collecting location updates
        myLocationReceiver = new LocationUpdateReceiver();

        if (extras != null) {

            //If in Place Marker Mode set the isPlaceMarkerMode boolean as true
            if (extras.get(MAP_MODE).equals(PLACE_MARKER_MODE)) {
                isPlaceMarkerMode = true;
                bnp = (NumberProgressBar) findViewById(R.id.progress_bar);
                bnp.setVisibility(View.INVISIBLE);
                bookImage = (ImageView) findViewById(R.id.imageview_bitmap);
                bookImage.setVisibility(View.INVISIBLE);
                cView = (CardView) findViewById(R.id.card_view);
                cView.setVisibility(View.INVISIBLE);
                viewAllEntries = false;

            }
            //If in view all entries mode, get the saved locations and their corresponding books for viewing
            if (extras.get(MAP_MODE).equals(VIEW_ALL_ENTRIES)) {
                isPlaceMarkerMode = false;
                viewAllEntries = true;
                bnp = (NumberProgressBar) findViewById(R.id.progress_bar);
                bnp.setVisibility(View.INVISIBLE);
                search = (EditText) findViewById(R.id.edittext_search);
                search.setVisibility(View.INVISIBLE);
                searchButton = (Button) findViewById(R.id.button_search);
                searchButton.setVisibility(View.INVISIBLE);
                bookImage = (ImageView) findViewById(R.id.imageview_bitmap);
                bookImage.setVisibility(View.INVISIBLE);
                cView = (CardView) findViewById(R.id.card_view);
                cView.setVisibility(View.INVISIBLE);
                byte[] byteArray = extras.getByteArray(LOCATIONS_LIST);

                if (byteArray != null)
                    setLocationListFromByteArray(byteArray, savedLocations);
                booksAtLocation = extras.getStringArrayList(BOOKS_LIST);

                //Log.d("HERE", helper.imageURL);
            }

            //If in single entry mode, get the location list of a single book with the corresponding information about the location
            //i.e. How long you read, how many pages etc...
            if (extras.get(MAP_MODE).equals(VIEW_SINGLE_ENTRY)) {
                progressValues = new ArrayList<String>();
                mapText = new ArrayList<String>();
                isPlaceMarkerMode = false;
                viewAllEntries = false;
                search = (EditText) findViewById(R.id.edittext_search);
                search.setVisibility(View.INVISIBLE);
                searchButton = (Button) findViewById(R.id.button_search);
                searchButton.setVisibility(View.INVISIBLE);


                byte[] byteArray = extras.getByteArray(LOCATIONS_LIST);
                if (byteArray != null)
                    setLocationListFromByteArray(extras.getByteArray(LOCATIONS_LIST),
                            savedLocations);
                booksAtLocation = extras.getStringArrayList(BOOKS_LIST);
                progressValues = extras.getStringArrayList("values");
                mapText = extras.getStringArrayList("mapText");
                bookISBN = extras.getString("isbn");
                //bookISBN = "9780465026562";
                if (bookISBN != null && bookISBN.length() > 0) {
                    BookAPIHelper bookHelper = new BookAPIHelper(bookISBN);
                    bookHelper.execute();
                } else {
                    bookImage = (ImageView) findViewById(R.id.imageview_bitmap);
                    bookImage.setImageDrawable(getResources().getDrawable(R.drawable.icon_book));

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup
                            .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(70, 80, 0, 0);
                    params.height = 150;
                    params.width = 150;
                    bookImage.setLayoutParams(params);


                }

            }
        }
        Log.d("MAP", "Map Mode: " + extras.get(MAP_MODE));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //If in Place Marker Mode, set up to map click listener and dialog box
        if (isPlaceMarkerMode) {
            startTrackingService();

            redrawUI();
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                    savedLocations.add(latLng);
                    currMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_book)));

                    curLat = latLng.latitude;
                    curLong = latLng.longitude;
                    drawDialogBox();
                }
            });
        }
        //Else we are in one of the view entry modes therefore just draw the stored locations
        else {

            startTrackingService();
            redrawUI();

        }


    }

    //Function that starts the tracking service
    private void startTrackingService() {
        Log.d("Map", "Start tracking called");
        isBound = true;
        mServiceIntent = new Intent(this, MyTrackingService.class);
        //mServiceIntent.putExtra(MainActivity.SAVED_ACTIVITY, activityType);
        startService(mServiceIntent);
        bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    //Unregister the location receiver if the app is paused
    //Unbind the service if the app is paused
    @Override
    protected void onPause() {
        animationIndex = 0;


        unregisterReceiver(myLocationReceiver);
        if (isBound) {
            //unbind service
            unbindService(mServiceConnection);
            isBound = false;
        }

        super.onPause();
    }

    //Function that stops that unbinds the tracking service
    private void stopTrackingService() {
        if (myTrackingService != null) {
            if (isBound) {
                //unbind service
                unbindService(mServiceConnection);
                isBound = false;
            }
            stopService(mServiceIntent);
        }
    }

    public void redrawUI() {
        //If in Place Marker Mode, zoom to current location for ease of placing a marker near you
        if (mMap != null) {
            if (isPlaceMarkerMode) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(curLat, curLong))
                        .zoom(17)
                        .bearing(0)
                        .tilt(45)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                Log.d("MapActivity", "Lat: " + String.valueOf(curLat) + " Long: "
                        + String.valueOf(curLong));
            }
            //Else we are in one of the view entry modes therefore set markers for locations on the maps
            else {
                if (savedLocations.size() == 0 || savedLocations == null) {
                    if(viewAllEntries == false) {
                        Log.d("MAP", "no saved locations");
                        Toast.makeText(getApplicationContext(), "No Saved Locations", Toast.LENGTH_SHORT);

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(curLat, curLong))
                                .zoom(17)
                                .bearing(0)
                                .tilt(45)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    }

                } else {

                    int count = 0;
                    if (viewAllEntries) {
                        if (savedLocations.size() > 0) {
                            if (booksAtLocation.size() > 0) {

                                Log.d("map", "saved locations.size: " +
                                        String.valueOf(savedLocations.size()));
                                for (LatLng l : savedLocations) {
                                    mMap.addMarker(new MarkerOptions().position(l)
                                            .title(booksAtLocation.get(count))
                                            .icon(BitmapDescriptorFactory.
                                                    fromResource(R.drawable.icon_book)));
                                    count++;
                                }
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(new LatLng(savedLocations
                                                .get(savedLocations.size() - 1).latitude,
                                                savedLocations.get(savedLocations.size() - 1).longitude))
                                        .zoom(17)
                                        .bearing(0)
                                        .tilt(45)
                                        .build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            }
                        }
                    } else {
                        drawMapStats();
                        animateMap();


                    }
                }
            }
        }
    }


    //Function that draws the stats corresponding to the current book
    public void drawMapStats() {
        TextView textTitle = (TextView) findViewById(R.id.textview_title);
        TextView textAuthor = (TextView) findViewById(R.id.textview_author);
        TextView textGenre = (TextView) findViewById(R.id.textview_genre);
        String[] genres = getResources().getStringArray(R.array.genre_array);


        String title = "Title: " + mapText.get(0);
        String author = "Author: " + mapText.get(1);
        String genre = "Genre: " + genres[Integer.parseInt(mapText.get(2))];

        textTitle.setText(title);
        textAuthor.setText(author);
        textGenre.setText(genre);


    }


    //When the application returns if we are in Place Marker Mode, register the receiver
    protected void onResume() {
        animationIndex = 0;
        super.onResume();
        setUpMapIfNeeded();


        // register the receiver for receiving the location update broadcast
        //if (isPlaceMarkerMode) {
        IntentFilter intentFilter = new IntentFilter(LocationUpdateReceiver.class.getName());
        registerReceiver(myLocationReceiver, intentFilter);
    }


    public void drawDialogBox() {
        alert = new AlertDialog.Builder(PSMapActivity.this)
                .setTitle("Latitude: " + curLat + " Longitude: " + curLong)
                .setMessage("Would you like to save this location?")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent data = new Intent();
                        data.putExtra("lat", curLat);
                        data.putExtra("long", curLong);
                        data.setClass(getApplicationContext(), MainActivity.class);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        currMarker.remove();
                        alert.dismiss();

                    }
                }).show();
    }


    // Function for converting latlng into location objects
    private Location convertLatLngToLocation(LatLng latLng) {
        Location location = new Location("someLoc");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }
    //Function that calculates bearing between objects
    private float bearingBetweenLatLngs(LatLng beginLatLng, LatLng endLatLng) {
        Location beginLocation = convertLatLngToLocation(beginLatLng);
        Location endLocation = convertLatLngToLocation(endLatLng);
        return beginLocation.bearingTo(endLocation);
    }

    //Stop tracking if we leave the application from a back press
    @Override
    public void onBackPressed() {
        stopTrackingService();
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);

        //Restore the image when the activity returns from the background
    }

    //Initialize the map if it is currently null
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    //Function that sets location list from a byte array
    public void setLocationListFromByteArray(byte[] bytePointArray, ArrayList<LatLng> list) {

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytePointArray);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();

        int[] intArray = new int[bytePointArray.length / Integer.SIZE];
        intBuffer.get(intArray);

        int locationNum = intArray.length / 2;

        for (int i = 0; i < locationNum; i++) {
            LatLng latLng = new LatLng((double) intArray[i * 2] / 1E6F,
                    (double) intArray[i * 2 + 1] / 1E6F);
            list.add(latLng);
        }
    }

    //Function that starts the geocoding search call
    public void onSearchClick(View v) {
        search = (EditText) findViewById(R.id.edittext_search);


        String searchText = search.getText().toString();

        GeoCoding gc = new GeoCoding(searchText);
        gc.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTrackingService();
    }
    //Class to get human readable address
    public class GeoCoding extends AsyncTask<Void, Void, Void> {
        private String address;
        private final String TAG = GeoCoding.class.getSimpleName();
        JSONObject jsonObj;
        String URL;
        private String Address1 = "", Address2 = "", City = "", State = "", Country = "",
                County = "", PIN = "", Area = "";
        private double latitude, longitude;
        HttpURLConnection connection;
        BufferedReader br;
        StringBuilder sb;

        public GeoCoding(String address) {
            this.address = address;
        }


        public void getAddress() {
            Address1 = "";
            Address2 = "";
            City = "";
            State = "";
            Country = "";
            County = "";
            PIN = "";
            Area = "";

            try {

                String Status = jsonObj.getString("status");
                if (Status.equalsIgnoreCase("OK")) {
                    JSONArray Results = jsonObj.getJSONArray("results");
                    JSONObject zero = Results.getJSONObject(0);

                    JSONArray address_components = zero.getJSONArray("address_components");

                    for (int i = 0; i < address_components.length(); i++) {
                        JSONObject zero2 = address_components.getJSONObject(i);
                        String long_name = zero2.getString("long_name");
                        JSONArray mtypes = zero2.getJSONArray("types");
                        String Type = mtypes.getString(0);

                        if (!TextUtils.isEmpty(long_name) || !long_name.equals(null) ||
                                long_name.length() > 0 || !long_name.equals("")) {
                            if (Type.equalsIgnoreCase("street_number")) {
                                Address1 = long_name + " ";
                            } else if (Type.equalsIgnoreCase("route")) {
                                Address1 = Address1 + long_name;
                            } else if (Type.equalsIgnoreCase("sublocality")) {
                                Address2 = long_name;
                            } else if (Type.equalsIgnoreCase("locality")) {
                                City = long_name;
                            } else if (Type.equalsIgnoreCase("administrative_area_level_2")) {
                                County = long_name;
                            } else if (Type.equalsIgnoreCase("administrative_area_level_1")) {
                                State = long_name;
                            } else if (Type.equalsIgnoreCase("country")) {
                                Country = long_name;
                            } else if (Type.equalsIgnoreCase("postal_code")) {
                                PIN = long_name;
                            } else if (Type.equalsIgnoreCase("neighborhood")) {
                                Area = long_name;
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        public void getGeoPoint() {

            try {
                longitude = ((JSONArray) jsonObj.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");
                latitude = ((JSONArray) jsonObj.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }




        @Override
        protected Void doInBackground(Void... params) {
            try {
                StringBuilder urlStringBuilder =
                        new StringBuilder("http://maps.google.com/maps/api/geocode/json");
                urlStringBuilder.append("?address=" + URLEncoder.encode(address, "utf8"));
                urlStringBuilder.append("&sensor=false");
                URL = urlStringBuilder.toString();
                Log.d(TAG, "URL: " + URL);

                URL url = new URL(URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb = sb.append(line + "\n");
                }
                Log.d("Map", sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                try {
                    //Log.d(TAG, "response code: " + connection.getResponseCode());
                    jsonObj = new JSONObject(sb.toString());
                } catch (Exception e) {
                    Log.d("Map", "Error onPost 1");
                    e.printStackTrace();
                    jsonObj = new JSONObject(sb.toString());

                }
                //Log.d(TAG, "JSON obj: " + jsonObj);
                getAddress();
                //Log.d(TAG, "area is: " + getArea());
                getGeoPoint();
                //Log.d("latitude", "" + latitude);
                //Log.d("longitude", "" + longitude);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Map", "Error onPost 2");
            }
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(17)
                    .bearing(0)
                    .tilt(45)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            super.onPostExecute(aVoid);
        }
    }



    //Function that fetches a bitmap from the web
    public class BitmapURLHelper extends AsyncTask<Void, Void, Void> {
        String imgURL;
        Bitmap bm;

        public BitmapURLHelper(String url) {
            imgURL = url;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(imgURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bm = BitmapFactory.decodeStream(input);
                Log.d("bm", bm.toString());

            } catch (IOException e) {
                // Log exception

            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {

            try {
                try {

                } catch (Exception e) {
                    Log.d("Map", "Error onPost 1");
                    e.printStackTrace();

                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Map", "Error onPost 2");
            }
            super.onPostExecute(aVoid);
        }
    }



    public class BookAPIHelper extends AsyncTask<Void, Void, Void> {

        private String isbn;
        private final String TAG = BookAPIHelper.class.getSimpleName();
        JSONObject jsonObj;
        String URL;
        HttpURLConnection connection;
        BufferedReader br;
        StringBuilder sb;
        String imageURL;
        Bitmap bitmap;
        BitmapURLHelper bmHelper;


        public BookAPIHelper(String i) {
            isbn = i;
        }
        public void getImageURL() {

            try {
                JSONArray items = jsonObj.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject bookRecord = items.getJSONObject(i);
                    JSONObject bookVolumeInfo = bookRecord.getJSONObject("volumeInfo");

                    JSONObject bookImageLinks = null;
                    try {
                        bookImageLinks = bookVolumeInfo.getJSONObject("imageLinks");
                    } catch (JSONException ignored) {
                    }

                    String bookSmallThumbnail = "";
                    if (bookImageLinks == null) {
                        bookSmallThumbnail = "null";
                    } else {
                        bookSmallThumbnail = bookImageLinks.getString("smallThumbnail");
                    }
                    imageURL = bookSmallThumbnail;
                }


                   // Log.d("Image Url", imageURL);

                }catch(Exception e){
                    e.printStackTrace();
                    Log.d("getIMAGEURL ERROR", "ERROR");
                }

            }



        @Override
        protected Void doInBackground(Void... params) {
            try {
                StringBuilder urlStringBuilder =
                        new StringBuilder("https://www.googleapis.com/books/v1/volumes?q=isbn:");
                urlStringBuilder.append(isbn);
                URL = urlStringBuilder.toString();
               // Log.d(TAG, "URL: " + URL);

                URL url = new URL(URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb = sb.append(line + "\n");
                }
               // Log.d("Book JSON", sb.toString());
                jsonObj = new JSONObject(sb.toString());
                getImageURL();
                bmHelper = new BitmapURLHelper(imageURL);
                bmHelper.doInBackground();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                try {

                    bookImage = (ImageView) findViewById(R.id.imageview_bitmap);
                    bookImage.setImageBitmap(bmHelper.bm);
                    //Log.d(TAG, "response code: " + connection.getResponseCode());
                   // jsonObj = new JSONObject(sb.toString());
                    //getImageURL();

                } catch (Exception e) {
                    Log.d("Map", "Error onPost 1");
                    e.printStackTrace();
                    jsonObj = new JSONObject(sb.toString());

                }
               // Log.d(TAG, "JSON obj: " + jsonObj);

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Map", "Error onPost 2");
            }
            super.onPostExecute(aVoid);
        }
    }

}



