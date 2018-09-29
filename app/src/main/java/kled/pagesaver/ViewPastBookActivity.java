package kled.pagesaver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.util.ArrayList;

/*
This class is used after a user clicks on a past book, it allows them to see previous
information that they stored
 */
public class ViewPastBookActivity extends AppCompatActivity {
    public final static String ID_BUNDLE_KEY = "_idbundle key";
    private long mEntryId;
    private BookEntry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_book_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        mEntryId = bundle.getLong(ID_BUNDLE_KEY);

        entry = new BookEntryDbHelper(this).fetchEntryByIndex(mEntryId); //fetch an entry based on ID

        setUpUI();

    }

    /*
    Set UI information based on entry information from database.
     */
    public void setUpUI() {
        ((TextView)findViewById(R.id.past_book_view_title)).setText(entry.getTitle());
        ((TextView)findViewById(R.id.past_book_view_author)).setText(entry.getAuthor());
        ((TextView)findViewById(R.id.past_book_view_genre)).setText(Search.getAllGenres()
                .get(entry.getGenre()));
        ((RatingBar)findViewById(R.id.past_book_view_rating_bar)).setRating(entry.getRating());
        ((TextView)findViewById(R.id.past_book_view_comments)).setText(entry.getComment());

        if(entry.getLocationList().size() == 0) {
            ((Button)findViewById(R.id.show_map_view_button)).setVisibility(View.GONE);
        }

    }


    public void onShowMapViewClick(View view) {
        Intent intent = new Intent(this, PSMapActivity.class);
        Bundle extras = new Bundle();
        ArrayList<BookEntry.StartEndPages> pages =  entry.getPageList();
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> mapText = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        for(BookEntry.StartEndPages pg: pages){
            titles.add(String.valueOf(((double)pg.endPage/(double)entry.getTotalPages())*100)
                    + "% Complete");
            // values.add(((double)pg.endPage/(double)entry.getTotalPages())*100);
            values.add(String.valueOf(((double)pg.endPage/(double)entry.getTotalPages())*100));
        }
        mapText.add(entry.getTitle());
        mapText.add(entry.getAuthor());
        mapText.add(String.valueOf(entry.getGenre()));
        mapText.add(entry.getProgressString());

        extras.putString("isbn",entry.getISBN());
        extras.putStringArrayList("mapText",mapText);
        extras.putStringArrayList("values", values);
        extras.putStringArrayList(PSMapActivity.BOOKS_LIST,titles);
        extras.putString(PSMapActivity.MAP_MODE, PSMapActivity.VIEW_SINGLE_ENTRY);
        extras.putByteArray(PSMapActivity.LOCATIONS_LIST, entry.getLocationByteArray());
        intent.putExtras(extras);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.past_book_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.delete_menu_item:
                //Delete from datastore
                EntryDatastoreHelper datastoreHelper = new EntryDatastoreHelper(this);
                datastoreHelper.deleteEntry(""+mEntryId);

                //Delete from local db
                new BookEntryDbHelper(this).removeEntry(mEntryId);
                finish();
                break;

            case R.id.share_menu_item:
                //TODO SHARE FUNCTIONALITY
                CallbackManager callbackManager = CallbackManager.Factory.create();
                ShareDialog shareDialog = new ShareDialog(this);

                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentTitle("PageSaved")
                            .setContentDescription(
                                    getDescription())
                            .setContentUrl(Uri.parse(GcmRegistrationAsyncTask.SERVER_ADDR + "/query.do"))
                            .build();

                    shareDialog.show(linkContent);
                }

                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getDescription() {
        return "Hello Facebook Friends!\nI recommend you read " + entry.getTitle() +
                " by " + entry.getAuthor() + ". I gave it " + entry.getRating() + " stars out of 5.";
    }



}
