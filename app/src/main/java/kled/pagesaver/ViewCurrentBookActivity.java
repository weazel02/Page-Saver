package kled.pagesaver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;

/*
This activity is used after a user clicks on a current book. It allows them to edit or
update their entry
 */
public class ViewCurrentBookActivity extends AppCompatActivity implements View.OnClickListener{
    public final static String ID_BUNDLE_KEY = "_idbundle key";
    private long mEntryId;
    private BookEntry entry;
    private EntryDatastoreHelper datastoreHelper;
    private BookEntryDbHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_current_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_edit);
        fab.setOnClickListener(this);

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab_mark_complete);
        fab2.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        mEntryId = bundle.getLong(ID_BUNDLE_KEY);

        entry = new BookEntryDbHelper(this).fetchEntryByIndex(mEntryId);

        setUpUI();

    }

    public void setUpUI() {
        ((TextView)findViewById(R.id.current_book_view_title)).setText(entry.getTitle());
        ((TextView)findViewById(R.id.current_book_view_author)).setText(entry.getAuthor());
        ((TextView)findViewById(R.id.current_book_view_genre)).setText(Search.getAllGenres()
                .get(entry.getGenre()));
        ((TextView)findViewById(R.id.progress_view_current)).setText(entry.getProgressString());

        if(entry.getLocationList().size() == 0) {
            ((Button)findViewById(R.id.view_locations_button)).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.current_book_view_menu, menu);
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
                datastoreHelper = new EntryDatastoreHelper(this);
                toDelete delete = new toDelete();
                delete.execute(mEntryId);

                //Delete from local db
                database = new BookEntryDbHelper(this);
                DeleteFromDatabase databaseDelete = new DeleteFromDatabase();
                databaseDelete.execute(mEntryId);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
AsyncTask to add an exercise entry to the database
*/
    private class toDelete extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... params) {
            datastoreHelper.deleteEntry(""+params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void params){
            Toast.makeText(getApplicationContext(), "Entry Deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private class DeleteFromDatabase extends AsyncTask<Long, Void, Void> {
        @Override
        protected Void doInBackground(Long... params) {
            database.removeEntry(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void params){
        }
    }


    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.fab_edit:
                Intent intent = new Intent(this, EditBookActivity.class);
                Bundle extras = new Bundle();
                extras.putLong(EditBookActivity.ID_BUNDLE_KEY, mEntryId);
                intent.putExtras(extras);
                startActivity(intent);
                finish();
                break;
            case R.id.fab_mark_complete:
                intent = new Intent(this, MarkCompleteActivity.class);
                extras = new Bundle();
                extras.putLong(MarkCompleteActivity.ID_BUNDLE_KEY, mEntryId);
                intent.putExtras(extras);
                startActivity(intent);
                finish();
                break;
        }
    }

    public void onViewLocationClick(View view) {
        //TODO CHECK
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

}
