package kled.pagesaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Database to store all BookEntries for a specific user
 */

public class BookEntryDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "PageSavedDB";

    public static final String TABLE_NAME_ENTRIES = "ENTRIES";
    public static final int DATABASE_VERSION = 1;

    public static final String KEY_ROW_ID = "_row_id";
    public static final String KEY_TITLE= "_title";
    public static final String KEY_AUTHOR = "_author";
    public static final String KEY_GENRE = "_genre";
    public static final String KEY_RATING = "_rating";
    public static final String KEY_COMMENT = "_comment";
    public static final String KEY_STATUS = "_status";
    public static final String KEY_LOCATIONS = "_locations";
    public static final String KEY_START_END_TIMES = "_start_end_times";
    public static final String KEY_START_END_PAGES = "start_end_pages";
    public static final String KEY_TOTAL_PAGES = "_total_pages";
    public static final String KEY_ISBN = "_isbn";
    private SQLiteDatabase database;

    private static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME_ENTRIES
            + " ("
            + KEY_ROW_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_TITLE
            + " TEXT, "
            + KEY_AUTHOR
            + " TEXT, "
            + KEY_GENRE
            + " INTEGER, "
            + KEY_RATING
            + " INTEGER, "
            + KEY_COMMENT
            +" TEXT, "
            + KEY_STATUS
            + " INTEGER NOT NULL, "
            + KEY_LOCATIONS
            + " BLOB, "
            + KEY_START_END_TIMES
            + " BLOB, "
            + KEY_START_END_PAGES
            + " BLOB, "
            + KEY_ISBN
            + " TEXT, "
            + KEY_TOTAL_PAGES
            + " INTEGER NOT NULL "
            + ");";


    public static final String[] columns = new String[]{KEY_ROW_ID,
            KEY_TITLE, KEY_AUTHOR, KEY_GENRE, KEY_RATING,
            KEY_COMMENT, KEY_STATUS, KEY_LOCATIONS,
            KEY_START_END_TIMES, KEY_START_END_PAGES,KEY_ISBN, KEY_TOTAL_PAGES};


    public BookEntryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*
    Adds a bookEntry to database
     */
    public long insertEntry(BookEntry entry){
       ContentValues value = entryToContentValues(entry);
        database = getWritableDatabase();
        long id = database.insert(TABLE_NAME_ENTRIES, null, value);
        database.close();
        return id;

    }

    /*
    Helper method for insertEntry
     */
    public ContentValues entryToContentValues(BookEntry entry) {
        ContentValues value = new ContentValues();
        value.put(KEY_TITLE, entry.getTitle());
        value.put(KEY_AUTHOR, entry.getAuthor());
        value.put(KEY_ISBN,entry.getISBN());
        value.put(KEY_GENRE, entry.getGenre());
        value.put(KEY_RATING, entry.getRating());
        value.put(KEY_COMMENT, entry.getComment());
        value.put(KEY_STATUS, entry.getStatus());

        value.put(KEY_TOTAL_PAGES, entry.getTotalPages());

        byte[] byteLocations = entry.getLocationByteArray();
        if (byteLocations.length > 0) {
            value.put(KEY_LOCATIONS, byteLocations);
        }

        byte[] byteTimes = entry.getTimeByteArray();
        if (byteTimes.length > 0) {
            value.put(KEY_START_END_TIMES, byteTimes);
        }

        byte[] bytePages = entry.getPageByteArray();
        if (bytePages.length > 0) {
            value.put(KEY_START_END_PAGES, bytePages);
        }

        return value;
    }

    /*
    Removes a bookEntry from the database
     */
    public void removeEntry(long index){
        database = getWritableDatabase();
        database.delete(TABLE_NAME_ENTRIES, KEY_ROW_ID + "=" + index, null);
        database.close();
    }

    /*
    Returns a specific BookEntry based on the index
     */
    public BookEntry fetchEntryByIndex(long id) throws SQLException {
        database = getReadableDatabase();
        BookEntry entry = null;

        Cursor cursor = database.query(true, TABLE_NAME_ENTRIES, columns, KEY_ROW_ID + "="
                + id, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            entry = cursorToEntry(cursor);
        }

        cursor.close();
        database.close();
        return entry;
    }

    public Cursor cursorToAllEntries() {
        database = getReadableDatabase();

        Cursor cursor = database.query(TABLE_NAME_ENTRIES, columns,
                null, null, null, null, null);

        cursor.moveToFirst();
        return cursor;
    }

    public Cursor cursorToEntriesWithMode(int mode) {
        database = getReadableDatabase();

        Cursor cursor = database.query(TABLE_NAME_ENTRIES, columns, KEY_STATUS + "=" + mode, null, null, null, null);

        cursor.moveToFirst();
        return cursor;
    }

    public BookEntry getNextEntryFromCursor(Cursor cursor) {
        if(cursor.isAfterLast()) {
            cursor.close();
            closeDB();
            return null;
        }

        BookEntry entry = cursorToEntry(cursor);

        cursor.moveToNext();

        return entry;

    }

    /*
    Fetches all book entries from the database,
    returns an ArrayList
     */
    public ArrayList<BookEntry> fetchEntries() {
        database = getReadableDatabase();
        ArrayList<BookEntry> entryList = new ArrayList<BookEntry>();

        Cursor cursor = database.query(TABLE_NAME_ENTRIES, columns, 
                null, null, null, null, null);

        while(cursor.moveToNext()) {
            BookEntry entry = cursorToEntry(cursor);
            entryList.add(entry);
        }

        cursor.close();
        database.close();

        return entryList;
    }

    /*
    Takes information from a cursor and converts it back to a BookEntry
     */
    private BookEntry cursorToEntry(Cursor cursor) {
        BookEntry entry = new BookEntry();
        entry.setRowId(cursor.getLong(cursor.getColumnIndex(KEY_ROW_ID)));
        entry.setTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
        entry.setAuthor(cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)));
        entry.setGenre(cursor.getInt(cursor.getColumnIndex(KEY_GENRE)));
        entry.setRating(cursor.getInt(cursor.getColumnIndex(KEY_RATING)));
        entry.setComment(cursor.getString(cursor.getColumnIndex(KEY_COMMENT)));
        entry.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_STATUS)));
        entry.setISBN(cursor.getString(cursor.getColumnIndex(KEY_ISBN)));
        entry.setTotalPages(cursor.getInt(cursor.getColumnIndex(KEY_TOTAL_PAGES)));

        byte[] byteTrackLocations = cursor.getBlob(cursor
                .getColumnIndex(KEY_LOCATIONS));
        if(byteTrackLocations != null)
            entry.setLocationListFromByteArray(byteTrackLocations);

        byte[] byteTrackTimes = cursor.getBlob(cursor
                .getColumnIndex(KEY_START_END_TIMES));
        if(byteTrackTimes != null)
            entry.setTimeListFromByteArray(byteTrackTimes);

        byte[] byteTrackPages = cursor.getBlob(cursor
                .getColumnIndex(KEY_START_END_PAGES));
        if(byteTrackPages != null)
            entry.setPageListFromByteArray(byteTrackPages);

        return entry;

    }

    /*
    This updates old entries about a book with the new information
     */
    public void updateEntry(BookEntry entry) {
        database = getWritableDatabase();
        ContentValues data= entryToContentValues(entry);
        database.update(TABLE_NAME_ENTRIES, data, KEY_ROW_ID + "=" + entry.getRowId(), null);
        database.close();
    }

    public void closeDB() {
        database.close();
    }

}
