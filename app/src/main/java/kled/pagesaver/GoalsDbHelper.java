package kled.pagesaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kelle on 2/26/2017.
 * A table to store goal entries
 */

public class GoalsDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "GoalsDB";

    public static final String TABLE_NAME_ENTRIES = "Goals";
    public static final int DATABASE_VERSION = 1;

    public static final String KEY_ID = "_id";
    public static final String KEY_TITLE= "_title";
    public static final String KEY_DESCRIPTION = "_description";
    public static final String KEY_GOALS_PAGES = "_total_pages";
    public static final String KEY_PAGES_INCREMENT = "_daily_pages";
    public static final String KEY_PROGRESS = "_progress";
    public static final String KEY_GOAL_START = "_goal_start_today";
    public static final String KEY_GOAL_END = "_goal_end_today";
    public static final String KEY_END_TIME = "_end_time";

    private static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME_ENTRIES
            + " ("
            + KEY_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_TITLE
            + " TEXT, "
            + KEY_DESCRIPTION
            + " TEXT, "
            + KEY_GOALS_PAGES
            + " INTEGER NOT NULL, "
            + KEY_PAGES_INCREMENT
            + " INTEGER NOT NULL, "
            + KEY_PROGRESS
            + " INTEGER NOT NULL, "
            + KEY_GOAL_START
            + " INTEGER NOT NULL, "
            + KEY_GOAL_END
            + " INTEGER NOT NULL, "
            + KEY_END_TIME
            + " LONG "
            + "); ";

    public static final String[] columns = new String[]{KEY_ID, KEY_TITLE,
            KEY_DESCRIPTION, KEY_GOALS_PAGES, KEY_PAGES_INCREMENT, KEY_PAGES_INCREMENT,
            KEY_PROGRESS, KEY_GOAL_START, KEY_GOAL_END, KEY_END_TIME };


    public GoalsDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_ENTRIES);
        onCreate(db);
    }

    public long insertGoalEntry(GoalEntry entry){
        ContentValues value = new ContentValues();

        value.put(KEY_ID, entry.getId());
        value.put(KEY_TITLE, entry.getBookTitle());
        value.put(KEY_DESCRIPTION, entry.getDescription());
        value.put(KEY_GOALS_PAGES, entry.getPagesToComplete());
        value.put(KEY_PAGES_INCREMENT, entry.getDailyPages());
        value.put(KEY_PROGRESS, entry.getReadPages());
        value.put(KEY_GOAL_START, entry.getGoalStartPage());
        value.put(KEY_GOAL_END, entry.getGoalEndPage());
        value.put(KEY_END_TIME, entry.getEndTime());

        SQLiteDatabase database = getWritableDatabase();
        long id = database.insert(TABLE_NAME_ENTRIES, null, value);
        database.close();
        return id;
    }

    public long updateGoalEntry(long id, int newProgress, int newGoal) {
        GoalEntry entry = fetchEntryByIndex(id);

        /* if book has not been completed, set current progress to be the user input number;
         * otherwise if book has been completed, set current progress to be total page
         */
        if (newProgress > 0) {
            if (newProgress >= entry.getPagesToComplete()) {
                entry.setReadPages(entry.getPagesToComplete());
            } else {
                entry.setReadPages(newProgress);
            }
        }
        else {
            entry.setReadPages(0);
        }

        // set new goal
        entry.setDailyPages(newGoal);

        /* If goal starting page changes, update goal ending page accordingly */
        int newGoalEndPage = entry.getGoalStartPage() + entry.getDailyPages() - 1;

        if (newGoalEndPage >= entry.getPagesToComplete()) {
            entry.setGoalEndPage(entry.getPagesToComplete());
        } else {
            entry.setGoalEndPage(newGoalEndPage);
        }

        ContentValues value = new ContentValues();

        value.put(KEY_ID, entry.getId());
        value.put(KEY_TITLE, entry.getBookTitle());
        value.put(KEY_DESCRIPTION, entry.getDescription());
        value.put(KEY_GOALS_PAGES, entry.getPagesToComplete());
        value.put(KEY_PAGES_INCREMENT, entry.getDailyPages());
        value.put(KEY_PROGRESS, entry.getReadPages());
        value.put(KEY_GOAL_START, entry.getGoalStartPage());
        value.put(KEY_GOAL_END, entry.getGoalEndPage());
        value.put(KEY_END_TIME, entry.getEndTime());

        SQLiteDatabase database = getWritableDatabase();
        database.update(TABLE_NAME_ENTRIES, value, KEY_ID + "=" + id, null);
        database.close();

        return id;
    }

    public void removeEntry(long index){
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_NAME_ENTRIES, KEY_ID + "=" + index, null);
        database.close();
    }

    public GoalEntry fetchEntryByIndex(long id) throws SQLException {
        SQLiteDatabase database = getReadableDatabase();
        GoalEntry entry = null;

        Cursor cursor = database.query(true, TABLE_NAME_ENTRIES, columns, KEY_ID + "="
                + id, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            entry = cursorToEntry(cursor);
        }

        cursor.close();
        database.close();
        return entry;
    }

    public ArrayList<GoalEntry> fetchEntries() {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<GoalEntry> entryList = new ArrayList<GoalEntry>();

        Cursor cursor = database.query(TABLE_NAME_ENTRIES, columns,
                null, null, null, null, null);

        while(cursor.moveToNext()) {
            GoalEntry entry = cursorToEntry(cursor);
            entryList.add(entry);
        }

        cursor.close();
        database.close();

        return entryList;
    }

    private GoalEntry cursorToEntry(Cursor cursor) {
        GoalEntry entry = new GoalEntry();
        entry.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
        entry.setBookTitle(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
        entry.setDescription(cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)));
        entry.setPagesToComplete(cursor.getInt(cursor.getColumnIndex(KEY_GOALS_PAGES)));
        entry.setDailyPages(cursor.getInt(cursor.getColumnIndex(KEY_PAGES_INCREMENT)));
        entry.setReadPages(cursor.getInt(cursor.getColumnIndex(KEY_PROGRESS)));
        entry.setGoalStartPage(cursor.getInt(cursor.getColumnIndex(KEY_GOAL_START)));
        entry.setGoalEndPage(cursor.getInt(cursor.getColumnIndex(KEY_GOAL_END)));
        entry.setEndTime(cursor.getLong(cursor.getColumnIndex(KEY_END_TIME)));

        return entry;
    }

    // update goal range for next day at midnight
    public void updateNextDayGoal() {
        ArrayList<GoalEntry> entryList = new ArrayList<GoalEntry>();
        entryList = fetchEntries();
        for(int i=0;i<entryList.size();i++){
            int totalPage = entryList.get(i).getPagesToComplete();

            // update start page
            int oldStartPage = entryList.get(i).getGoalStartPage();
            int newStartPage = oldStartPage + entryList.get(i).getDailyPages();
            if (newStartPage < totalPage) {
                entryList.get(i).setGoalStartPage(newStartPage);
            } else {
                entryList.get(i).setGoalStartPage(totalPage);
            }

            // update end page
            int newEndPage = newStartPage + entryList.get(i).getDailyPages() - 1;
            if (newEndPage < totalPage) {
                entryList.get(i).setGoalEndPage(newEndPage);
            } else {
                entryList.get(i).setGoalEndPage(totalPage);
            }

            ContentValues value = new ContentValues();

            value.put(KEY_ID, entryList.get(i).getId());
            value.put(KEY_TITLE, entryList.get(i).getBookTitle());
            value.put(KEY_DESCRIPTION, entryList.get(i).getDescription());
            value.put(KEY_GOALS_PAGES, entryList.get(i).getPagesToComplete());
            value.put(KEY_PAGES_INCREMENT, entryList.get(i).getDailyPages());
            value.put(KEY_PROGRESS, entryList.get(i).getReadPages());
            value.put(KEY_GOAL_START, entryList.get(i).getGoalStartPage());
            value.put(KEY_GOAL_END, entryList.get(i).getGoalEndPage());
            value.put(KEY_END_TIME, entryList.get(i).getEndTime());

            SQLiteDatabase database = getWritableDatabase();
            database.update(TABLE_NAME_ENTRIES, value, KEY_ID + "=" +
                    entryList.get(i).getId(), null);
            database.close();
        }
    }
}
