package kled.pagesaver;

import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static kled.pagesaver.GoalsActivity.GOAL_NOTIFICATION_ID;

/**
 * Created by Yu Liu on 3/6/2017.
 * Reminds user about reading goals to help them stay on track
 */

public class GoalsReminderActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<GoalEntry>>{

    public ArrayList<GoalEntry> entries;
    private GoalsReminderAdapter goalsReminderAdapter;
    private GoalsDbHelper goalsDatabase;

    LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals_reminder);

        goalsDatabase = new GoalsDbHelper(this);
        entries = new ArrayList<GoalEntry>();

        // Load entries
        loaderManager = this.getLoaderManager();
        loaderManager.initLoader(1, null, this);

        // Set up list view
        ListView entryView = (ListView) findViewById(R.id.goals_reminder_list);
        goalsReminderAdapter = new GoalsReminderAdapter(this, entries);
        entryView.setAdapter(goalsReminderAdapter);

        entryView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ViewGoalActivity.class);
                GoalEntry viewedEntry = entries.get(position);

                intent.putExtra(GoalsDbHelper.KEY_ID, viewedEntry.getId());
                intent.putExtra(GoalsDbHelper.KEY_TITLE, viewedEntry.getBookTitle());
                intent.putExtra(GoalsDbHelper.KEY_DESCRIPTION, viewedEntry.getDescription());
                intent.putExtra(GoalsDbHelper.KEY_GOALS_PAGES,
                        Integer.toString(viewedEntry.getPagesToComplete()));
                intent.putExtra(GoalsDbHelper.KEY_PAGES_INCREMENT,
                        Integer.toString(viewedEntry.getDailyPages()));
                intent.putExtra(GoalsDbHelper.KEY_PROGRESS,
                        Integer.toString(viewedEntry.getReadPages()));
                intent.putExtra(GoalsDbHelper.KEY_GOAL_START,
                        Integer.toString(viewedEntry.getGoalStartPage()));
                intent.putExtra(GoalsDbHelper.KEY_GOAL_END,
                        Integer.toString(viewedEntry.getGoalEndPage()));

                if (viewedEntry.getEndTime() != null) {
                    intent.putExtra(GoalsDbHelper.KEY_END_TIME, viewedEntry.getEndTime());
                }

                startActivity(intent);
            }
        });
    }

    /**
     * Update entries after a change
     */
    @Override
    public void onResume() {
        super.onResume();
        updateEntries();
    }

    // Make sure delete option is in toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.goal_reminder_menu, menu);
        return true;
    }

    // Update Goal
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop_reminder:
                // cancel goal reminder notification
                NotificationManager nManager = (NotificationManager)
                        getSystemService(NOTIFICATION_SERVICE);
                nManager.cancel(GOAL_NOTIFICATION_ID);
                finish();
                return true;
            default:
                return false;
        }
    }

    /**
     * Update entries after there has been a change
     */
    public void updateEntries() {
        loaderManager.initLoader(1, null, this).forceLoad();
    }

    @Override
    public Loader<ArrayList<GoalEntry>> onCreateLoader(int id, Bundle args) {
        LoadGoalEntries loadGoalEntries = new LoadGoalEntries(this);
        return loadGoalEntries;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<GoalEntry>> loader, ArrayList<GoalEntry> data) {
        // Clear and reload list to be displayed
        entries.clear();
        entries.addAll(data);
        goalsReminderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<GoalEntry>> loader) {
        // Clear all data
        entries.clear();
        goalsReminderAdapter.notifyDataSetChanged();
    }
}

