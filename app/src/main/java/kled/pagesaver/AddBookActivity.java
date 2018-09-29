package kled.pagesaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

/*
AddBookActivity creates a new BookEntry in the data store based on user-inputted information
 */

public class AddBookActivity extends AppCompatActivity implements View.OnClickListener{
    private LatLng chosenLatLng = null;
    private final static int MAP_REQUEST_CODE = 5789;

    private EntryDatastoreHelper datastoreHelper;
    private BookEntryDbHelper database;
    private EditText mTitleView;
    private EditText mAuthorView;
    private EditText mProgressSoFarView;
    private EditText mTotalPagesView;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private EditText mDurationHour;
    private EditText mDurationMinute;
    private EditText mISBNView;
    Spinner genreSpinner;

    long id;


    //bundle keys
    private final static String TITLE_KEY = "title";
    private final static String AUTHOR_KEY = "author";
    private final static String GENRE_KEY = "genre";
    private final static String PROGRESS_SO_FAR_KEY = "psf";
    private final static String TOTAL_PAGES_KEY = "totalpages";
    private final static String TIME_KEY = "time";
    private final static String DHOUR_KEY = "dhour";
    private final static String DMINUTE_KEY = "dminute";
    private final static String LAT_BUNDLE_KEY = "latitude";
    private final static String LNG_BUNDLE_KEY = "longitude";
    private final static String ISBN_KEY = "isbn";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton)
                findViewById(R.id.fab_add_add_book_view);
        fab.setOnClickListener(this);

        setUpUIConnections();
    }


    //REGION button call backs
    public void onAddLocationClick(View view) {
        Intent intent = new Intent(this, PSMapActivity.class);
        Bundle extras = new Bundle();
        extras.putString(PSMapActivity.MAP_MODE, PSMapActivity.PLACE_MARKER_MODE);
        intent.putExtras(extras);
        startActivityForResult(intent, MAP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MAP_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Bundle bundle = data.getExtras();
                double lat = bundle.getDouble(PSMapActivity.LAT_KEY, -1);
                double lng = bundle.getDouble(PSMapActivity.LNG_KEY, -1);
                if(lat != -1 && lng != -1)
                    chosenLatLng = new LatLng(lat, lng);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Don't do anything
            }
        }
    }

    public void onClick(View view) {
        int id = view.getId();

        if(id == R.id.fab_add_add_book_view) {
            String errorString = isReadyToAdd();
            if(errorString.equals("")) {
                addEntry();
                finish();
            } else {
                //SHOW DIALOG with error
                makeTextDialog(errorString);
            }
        }
    }

    private String isReadyToAdd() {
        String errorString = "Must enter the following fields to add entry:\n";
        boolean canAddFlag = true;

        if(!fieldNotEmpty(mTitleView)) {
            canAddFlag = false;
            errorString = errorString + "Title\n";
        }

        if(!fieldNotEmpty(mAuthorView)) {
            canAddFlag = false;
            errorString = errorString + "Author\n";
        }

        if(!fieldNotEmpty(mProgressSoFarView)) {
            canAddFlag = false;
            errorString = errorString + "Pages Read\n";
        } else {
            int progressSoFar = Integer.parseInt(mProgressSoFarView.getText().toString());
            if (progressSoFar <= 0) {
                canAddFlag = false;
                errorString = errorString + "Non-Zero Pages Read\n";
            }
        }

        if(!fieldNotEmpty(mTotalPagesView)) {
            canAddFlag = false;
            errorString = errorString + "Total Number of Pages\n";
        } else {
            int totalPages = Integer.parseInt(mTotalPagesView.getText().toString());
            if (totalPages <= 0) {
                canAddFlag = false;
                errorString = errorString + "Non-Zero Total Number of Pages\n";
            }
        }

        if(!fieldNotEmpty(mDurationHour)) {
            canAddFlag = false;
            errorString = errorString + "Hours Spent Reading\n";
        }

        if(!fieldNotEmpty(mDurationMinute)) {
            canAddFlag = false;
            errorString = errorString + "Minutes Spent Reading\n";
        }

        if (fieldNotEmpty(mDurationHour) && fieldNotEmpty(mDurationMinute)) {
            int durationHours = Integer.parseInt(mDurationHour.getText().toString());
            int durationMinutes = Integer.parseInt(mDurationMinute.getText().toString());

            if (durationHours == 0 && durationMinutes == 0) {
                canAddFlag = false;
                errorString = errorString + "Non-Zero Duration\n";
            }
        }

        //TODO UNCOMMENT THIS WHEN WESLEY FIXES

//        if(chosenLatLng == null) {
//            errorString = errorString + "Choose a Location\n";
//            canAddFlag = false;
//        }

        if(canAddFlag) {
            return "";
        } else {
            return errorString;
        }


    }

    private boolean fieldNotEmpty(EditText field) {
        if("".equals(field.getText().toString()))
            return false;

        return true;
    }

    private void addEntry() {
        //turn UI info into an entry
        BookEntry entry = retrieveUIInfo();

        //Add to database!
        database = new BookEntryDbHelper(this);

        //Add to datastore
        datastoreHelper = new EntryDatastoreHelper(this);
        toAdd add= new toAdd();
        add.execute(entry);

    }

    /*
AsyncTask to add an exercise entry to the database
*/
    private class toAdd extends AsyncTask<BookEntry, Void, Void> {

        @Override
        protected Void doInBackground(BookEntry... params) {
            //SET ID
            id = database.insertEntry(params[0]);
            params[0].setRowId(id);
            datastoreHelper.addEntry(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void params){
            Toast.makeText(getApplicationContext(), "Entry saved", Toast.LENGTH_SHORT).show();
        }
    }




/*
Returns a BookEntry with user-inputted information from the UI
 */
    private BookEntry retrieveUIInfo() {
        BookEntry entry = new BookEntry();

        entry.setStatus(BookEntry.STATUS_CURRENT);

        entry.setTitle(mTitleView.getText().toString());
        entry.setAuthor(mAuthorView.getText().toString());
        entry.setGenre(genreSpinner.getSelectedItemPosition());
        entry.setISBN(mISBNView.getText().toString());


        //Deal with progress entries
        int progressSoFar = Integer.parseInt(mProgressSoFarView.getText().toString());
        if(progressSoFar > 0)
            entry.addPageRange(0, progressSoFar);

        int totalPages = Integer.parseInt(mTotalPagesView.getText().toString());
        if (totalPages >= 0)
            entry.setTotalPages(totalPages);


        long startTime = getChosenTime();

        //Get end time
        long minutes = Long.parseLong(mDurationMinute.getText().toString());
        long hours = Long.parseLong(mDurationHour.getText().toString());
        long endTime = startTime + 3600000 * hours + 60000 * minutes;

        if(endTime >= startTime)
            entry.addStartEndTime(startTime, endTime);


        //Add Location if any
        if(chosenLatLng != null)
            entry.addLatLng(chosenLatLng);

        return entry;

    }

    /*
    Retrieves user-chosen time from mDatePickers
     */
    private long getChosenTime() {
        Calendar cal = Calendar.getInstance();
        int year = mDatePicker.getYear();
        int month = mDatePicker.getMonth();
        int day = mDatePicker.getDayOfMonth();
        int hour = mTimePicker.getHour();
        int minute = mTimePicker.getMinute();
        cal.set(year, month, day, hour, minute);
        return cal.getTimeInMillis();
    }

    private void setUpUIConnections() {
        mTitleView = (EditText)findViewById(R.id.add_book_title);
        mAuthorView = (EditText)findViewById(R.id.add_book_author);
        genreSpinner = (Spinner) findViewById(R.id.add_book_genre);

        ArrayAdapter<CharSequence> adapterGenre = ArrayAdapter
                .createFromResource(this,
                        R.array.genre_array, android.R.layout.simple_spinner_item);

        adapterGenre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        genreSpinner.setAdapter(adapterGenre);
        mProgressSoFarView = (EditText)findViewById(R.id.add_book_progress_so_far);
        mTotalPagesView = (EditText)findViewById(R.id.add_book_total_pages);
        mDatePicker = (DatePicker)findViewById(R.id.date_picker_add_book);
        mTimePicker = (TimePicker)findViewById(R.id.time_picker_add_book);
        mDurationHour = (EditText)findViewById(R.id.add_book_duration_hour);
        mDurationMinute = (EditText)findViewById(R.id.add_book_duration_minute);
        mISBNView = (EditText) findViewById(R.id.add_book_isbn);

    }

    private void makeTextDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        saveInstanceState.putString(TITLE_KEY, mTitleView.getText().toString());
        saveInstanceState.putString(AUTHOR_KEY, mAuthorView.getText().toString());
        saveInstanceState.putInt(GENRE_KEY, genreSpinner.getSelectedItemPosition());
        saveInstanceState.putString(PROGRESS_SO_FAR_KEY,
                mProgressSoFarView.getText().toString());
        saveInstanceState.putString(TOTAL_PAGES_KEY,
                mTotalPagesView.getText().toString());
        saveInstanceState.putString(ISBN_KEY,mISBNView.getText().toString());

        saveInstanceState.putLong(TIME_KEY, getChosenTime());
        saveInstanceState.putString(DHOUR_KEY,
                mDurationHour.getText().toString());
        saveInstanceState.putString(DMINUTE_KEY,
                mDurationMinute.getText().toString());

        if(chosenLatLng != null) {
            saveInstanceState.putDouble(LAT_BUNDLE_KEY, chosenLatLng.latitude);
            saveInstanceState.putDouble(LNG_BUNDLE_KEY, chosenLatLng.longitude);
        }
    }

    /*
    Saves UI information with old information fromsavedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mTitleView.setText(savedInstanceState.getString(TITLE_KEY, ""));
        mAuthorView.setText(savedInstanceState.getString(AUTHOR_KEY, ""));
        genreSpinner.setSelection(savedInstanceState.getInt(GENRE_KEY, 0));
        mProgressSoFarView.setText(savedInstanceState.getString(PROGRESS_SO_FAR_KEY, ""));
        mTotalPagesView.setText(savedInstanceState.getString(TOTAL_PAGES_KEY, ""));
        mDurationHour.setText(savedInstanceState.getString(DHOUR_KEY, ""));
        mDurationMinute.setText(savedInstanceState.getString(DMINUTE_KEY, ""));
        mISBNView.setText(savedInstanceState.getString(ISBN_KEY,""));


        long time = savedInstanceState.getLong(TIME_KEY);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        mDatePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        mTimePicker.setHour(cal.get(Calendar.HOUR));
        mTimePicker.setMinute(cal.get(Calendar.MINUTE));

        double lat = savedInstanceState.getDouble(LAT_BUNDLE_KEY, -1);
        double lng = savedInstanceState.getDouble(LNG_BUNDLE_KEY, -1);

        if(lat != -1 && lng != -1)
            chosenLatLng = new LatLng(lat, lng);
        else
            chosenLatLng = null;



    }


}
