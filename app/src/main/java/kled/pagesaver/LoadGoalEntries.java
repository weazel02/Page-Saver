package kled.pagesaver;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by kelle on 2/27/2017.
 */

public class LoadGoalEntries extends AsyncTaskLoader<ArrayList<GoalEntry>> {

    Context c;

    public LoadGoalEntries(Context context) {
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
    public ArrayList<GoalEntry> loadInBackground() {
        GoalsDbHelper goalsDatabase = new GoalsDbHelper(c);
        return goalsDatabase.fetchEntries();
    }
}
