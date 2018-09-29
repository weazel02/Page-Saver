package kled.pagesaver;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by kelle on 2/27/2017.
 */

public class LoadBookEntries extends AsyncTaskLoader<ArrayList<BookEntry>> {

    Context c;

    public LoadBookEntries(Context context) {
        super(context);
        c = context;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * Load entries in background
     * @return
     */
    @Override
    public ArrayList<BookEntry> loadInBackground() {
        BookEntryDbHelper booksDatabase = new BookEntryDbHelper(c);
        return booksDatabase.fetchEntries();
    }
}