package kled.pagesaver;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;


/*
A thread that reads in book entries from the database
 */
public class ReadInEntriesAsyncTask extends AsyncTask<Void, BookEntry, Void> {
    public final static int PAST_MODE = 1;
    public final static int CURRENT_MODE = 0;
    public final static int ALL_MODE = 2;

    private int mode;
    private DBEntryAdapter adapter;
    private BookEntryDbHelper dbHelper;

    public ReadInEntriesAsyncTask(Context context, int MODE, DBEntryAdapter adapter) {
        mode = MODE;
        this.adapter = adapter;

        dbHelper = new BookEntryDbHelper(context);

    }

    @Override
    protected void onPreExecute() {


    }

    @Override
    protected Void doInBackground (Void... params) {
        Cursor cursor;
        if(mode == ALL_MODE)
            cursor = dbHelper.cursorToAllEntries();
        else
            cursor = dbHelper.cursorToEntriesWithMode(mode);

        BookEntry entry = dbHelper.getNextEntryFromCursor(cursor);
        while(entry != null)
        {
            if(isCancelled())
            {
                cursor.close();
                dbHelper.closeDB();
                return null;
            }
            publishProgress(entry);
            entry = dbHelper.getNextEntryFromCursor(cursor);

        }

        return null;

    }

    @Override
    protected void onProgressUpdate (BookEntry... param) {
        // Add to adapter
        BookEntry entry = param[0];
        if(entry != null)
            adapter.addToAdapter(entry);
    }

    @Override
    protected void onPostExecute(Void result) {}
}
