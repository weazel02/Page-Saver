package kled.pagesaver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ViewGoalActivity extends AppCompatActivity
        implements CustomRangeDialogFragment.CustomRangeDialogListener,
        GoalUpdateDialogFragment.GoalUpdateDialogListener{

    private GoalsDbHelper goalsDataBase;
    Long entryId;
    public int goal;
    public int progress;

    DialogFragment newFragment;

    @Override
    /* Update progress after user inputs custom range */
    public void onFinishEditDialog(int inputRange){
        progress = inputRange;
        UpdateParams update_custom = new UpdateParams(entryId, progress, goal);
        EntryUpdateWorker updateWorkerCustom = new EntryUpdateWorker();
        updateWorkerCustom.execute(update_custom);
        finish();
    }

    @Override
    /* Update goal after user inputs new goal for each day */
    public void onFinishEditGoalUpdateDialog(int inputGoalUpdate){
        goal = inputGoalUpdate;
        UpdateParams update_goal = new UpdateParams(entryId, progress, goal);
        EntryUpdateWorker updateWorkerGoal = new EntryUpdateWorker();
        updateWorkerGoal.execute(update_goal);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_goal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        goalsDataBase = new GoalsDbHelper(this);

        Intent intent = getIntent();

        // Used if user needs to delete entry
        entryId = intent.getLongExtra(GoalsDbHelper.KEY_ID, -1);

        // Find associated EditText blocks for display
        EditText titleText = (EditText)findViewById(R.id.goals_title_text);
        EditText descriptionText = (EditText)findViewById(R.id.goals_description_text);
        EditText incrementText = (EditText)findViewById(R.id.goals_increment_text);
        EditText progressText = (EditText)findViewById(R.id.goals_progress_text);
        EditText totalText = (EditText)findViewById(R.id.goals_total_goal_text);
        EditText endTimeText = (EditText)findViewById(R.id.goals_end_time_text);

        // Set text according to entry
        titleText.setText(intent.getStringExtra(GoalsDbHelper.KEY_TITLE));
        descriptionText.setText(intent.getStringExtra(GoalsDbHelper.KEY_DESCRIPTION));
        goal = Integer.parseInt(intent.getStringExtra(GoalsDbHelper.KEY_PAGES_INCREMENT));
        incrementText.setText(intent.getStringExtra(GoalsDbHelper.KEY_PAGES_INCREMENT));
        progress = Integer.parseInt(intent.getStringExtra(GoalsDbHelper.KEY_PROGRESS));
        progressText.setText(intent.getStringExtra(GoalsDbHelper.KEY_PROGRESS));
        totalText.setText(intent.getStringExtra(GoalsDbHelper.KEY_GOALS_PAGES));


        Long endTimeMillis = intent.getLongExtra(GoalsDbHelper.KEY_END_TIME, -1);

        if(endTimeMillis == -1) {
            endTimeText.setText("No end date specified");
        } else {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy");
            String dateString = format.format(endTimeMillis);
            endTimeText.setText(dateString);
        }

    }

    // Make sure delete option is in toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.goal_menu, menu);
        return true;
    }

    // Update Goal
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.goal_custom_progress:
                newFragment = new CustomRangeDialogFragment();
                newFragment.show(getSupportFragmentManager(), "customRange");
                return true;
            case R.id.goal_update:
                newFragment = new GoalUpdateDialogFragment();
                newFragment.show(getSupportFragmentManager(), "updateGoal");
                return true;
            case R.id.goal_complete:
                EntryDeleteWorker deleteWorker = new EntryDeleteWorker();
                deleteWorker.execute(entryId);
                finish();
                return true;
            default:
                return false;
        }
    }

    /**
     * Worker thread to delete entry in the database in the background
     * without interfering with interface
     */
    class EntryDeleteWorker extends AsyncTask<Long, Void, String> {
        @Override
        protected String doInBackground(Long... params) {
            goalsDataBase.removeEntry(params[0]);
            return String.valueOf(params[0]);
        }

        // Confirm entry deleted
        @Override
        protected void onPostExecute(String id) {
            Toast.makeText(getApplicationContext(),	"Goal " + id + " was completed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private static class UpdateParams {
        Long id;
        int pagesRead;
        int currentGoal;

        UpdateParams(Long _id, int _pagesRead, int _currentGoal) {
            this.id = _id;
            this.pagesRead = _pagesRead;
            this.currentGoal = _currentGoal;
        }
    }

    /**
     * Worker thread to delete entry in the database in the background
     * without interfering with interface
     */
    class EntryUpdateWorker extends AsyncTask<UpdateParams, Void, String> {
        @Override
        protected String doInBackground(UpdateParams... params) {
            goalsDataBase.updateGoalEntry(params[0].id, params[0].pagesRead, params[0].currentGoal);
            return String.valueOf(params[0].id);
        }

        // Confirm entry deleted
        @Override
        protected void onPostExecute(String id) {
            Toast.makeText(getApplicationContext(),	"Goal " + id + " was updated.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
