package kled.pagesaver;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Danielle on 2/24/17.
 */

/*
An adapter to display list of book entires
 */
public class DBEntryAdapter extends BaseAdapter {
    private Activity context;
    private List<BookEntry> mList;


    enum MODE {
        CURRENT, PAST, ALL
    }

    private MODE mode;

    public static final int CURRENT_MODE = 0;
    public static final int PAST_MODE = 1;
    public static final int ALL_MODE = 2;


    public DBEntryAdapter(Activity context, int mode) {
        this.context = context;
        mList = new ArrayList<BookEntry>();

        switch (mode) {
            case CURRENT_MODE:
                this.mode = MODE.CURRENT;
                break;
            case PAST_MODE:
                this.mode = MODE.PAST;
                break;
            default:
                this.mode = MODE.ALL;
                break;
        }
    }

    /* Clear adapter for when you want to reload fragment*/
    public void clearAdapter() {
        mList.clear();
        notifyDataSetChanged();
    }

    public List<BookEntry> getList() {
        List<BookEntry> ret = new ArrayList<>();
        for(BookEntry entry : mList) {
            ret.add(entry);
        }
        return ret;
    }

    public void setList(List<BookEntry> entries) {
        mList = entries;
        notifyDataSetChanged();
    }

    public void setList(Set<BookEntry> entries) {
        mList.clear();
        for(BookEntry entry : entries) {
            mList.add(entry);
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return mList.size();
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return mList.get(position).getRowId();
    }


    /** Update and refresh list*/
    public void addToAdapter(BookEntry entry) {
        mList.add(entry);
        notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        BookEntry entry = mList.get(position);
        switch (mode) {
            case PAST:
                if(convertView == null) {
                    rowView = context.getLayoutInflater()
                            .inflate(R.layout.book_entry_row_past, null);
                } else {
                    rowView = convertView;
                }
                rowView = setupPastView(entry, rowView);
                break;

            case CURRENT:
                if(convertView == null) {
                    rowView = context.getLayoutInflater()
                            .inflate(R.layout.book_entry_row_current, null);
                } else {
                    rowView = convertView;
                }
                rowView = setupCurrentView(entry, rowView);
                break;

            default:
                if(entry.isCompleted()) {
                    rowView = context.getLayoutInflater()
                            .inflate(R.layout.book_entry_row_past, null);
                    rowView = setupPastView(entry, rowView);
                } else {
                    rowView = context.getLayoutInflater()
                            .inflate(R.layout.book_entry_row_current, null);
                    rowView = setupCurrentView(entry, rowView);
                }
                break;
        }

        return rowView;
    }

    private View setupPastView(BookEntry entry, View rowView) {
        TextView headerTV = (TextView)rowView.findViewById(R.id.first_tv);
        RatingBar ratingBar = (RatingBar)rowView.findViewById(R.id.rating_bar_lv);

        String headerString = entry.getTitle() + ": " + entry.getAuthor();
        headerTV.setText(headerString);

        ratingBar.setRating(entry.getRating());
        ratingBar.setClickable(false);


        return rowView;
    }
    public void getBitMap(BookEntry e){


    }
    private View setupCurrentView(BookEntry entry, View rowView) {
        TextView textView = (TextView)rowView.findViewById(R.id.first_tv_current);
        //TextView progressView = (TextView) rowView.findViewById(R.id.progress_view_row);
        ImageView bookImage = (ImageView) rowView.findViewById(R.id.imageView_current_books);
        NumberProgressBar bar = (NumberProgressBar) rowView.findViewById(R.id.progress_bar2);
        TextView preadView = (TextView) rowView.findViewById(R.id.pages_read);

        if(entry.getISBN()!= null && entry.getISBN().length()== 13){
            BookAPIHelper helper = new BookAPIHelper(entry.getISBN(),bookImage);
            helper.execute();
        }else{
            bookImage.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_book));
        }

        String headerString = entry.getTitle() + ": " + entry.getAuthor();
        textView.setText(headerString);

        ArrayList<BookEntry.StartEndPages> pages = entry.getPageList();
        int totalPages = entry.getTotalPages();

        preadView.setText("Pages Read: " + String.valueOf(pages.get(pages.size() - 1).endPage) + "/" + String.valueOf(totalPages));


        double percentValue = ((double)pages.get(pages.size() - 1).endPage)/((double)totalPages) *100;
        bar.setProgress((int) percentValue);



        //progressView.setText(entry.getProgressString());

        return rowView;
    }

    public class BitmapURLHelper extends AsyncTask<Void, Void, Void> {
        String imgURL;
        Bitmap bm;
        ImageView view;

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
        private final String TAG = PSMapActivity.BookAPIHelper.class.getSimpleName();
        JSONObject jsonObj;
        String URL;
        HttpURLConnection connection;
        BufferedReader br;
        StringBuilder sb;
        String imageURL;
        Bitmap bitmap;
        BitmapURLHelper bmHelper;
        ImageView imgV;



        public BookAPIHelper(String i,ImageView img){
            isbn = i;
            imgV = img;


        }
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


                Log.d("Image Url", imageURL);

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
                Log.d("Book JSON", sb.toString());
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
                    imgV.setImageBitmap(bmHelper.bm);
                    //imgV.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_book));

                    //Log.d(TAG, "response code: " + connection.getResponseCode());
                    // jsonObj = new JSONObject(sb.toString());
                    //getImageURL();

                } catch (Exception e) {
                    Log.d("Map", "Error onPost 1");
                    e.printStackTrace();
                    jsonObj = new JSONObject(sb.toString());

                }
                Log.d(TAG, "JSON obj: " + jsonObj);

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Map", "Error onPost 2");
            }
            super.onPostExecute(aVoid);
        }
    }

}
