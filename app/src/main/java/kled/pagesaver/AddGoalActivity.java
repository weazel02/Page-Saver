package kled.pagesaver;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Calendar;

/**
 * AddGoalActivity creates a new GoalEntry in the database
 */
public class AddGoalActivity extends AppCompatActivity {

    private GoalsDbHelper entrySource;
    private GoalEntry entry;

    private int dailyPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        entrySource = new GoalsDbHelper(getApplicationContext());
    }

    /**
     * Save entry information
     */
    protected void saveEntry() {
        // Data Storage
        entry = new GoalEntry();

        // Get and save input from different fields
        EditText titleText = (EditText) findViewById(R.id.add_goals_title_text);

        entry.setBookTitle(titleText.getText().toString());

        EditText descriptionText = (EditText) findViewById(R.id.add_goals_description_text);
        entry.setDescription(descriptionText.getText().toString());

        EditText incrementText = (EditText) findViewById(R.id.add_goals_increment_text);
        try {
            dailyPages = Integer.parseInt(incrementText.getText().toString());
            entry.setDailyPages(dailyPages);
        } catch (Exception e) {
            entry.setDailyPages(0);
        }

        EditText totalGoalText = (EditText) findViewById(R.id.add_goals_total_goal_text);
        try {
            int totalPages = Integer.parseInt(totalGoalText.getText().toString());
            entry.setPagesToComplete(totalPages);
        } catch (Exception e) {
            entry.setPagesToComplete(0);
        }

        // Only accept date if set after current date
        DatePicker datePicker = (DatePicker) findViewById(R.id.add_goals_date_picker);

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        Calendar current = Calendar.getInstance();

        if(calendar.getTimeInMillis() > current.getTimeInMillis())
            entry.setEndTime(calendar.getTimeInMillis());
        else
            entry.setEndTime(new Long(-1));

        // Get goal pages if an end date and total pages are specified
        if (entry.getDailyPages() == 0 && entry.getEndTime() != -1
                && entry.getPagesToComplete() != 0) {
            Long timeDifference = calendar.getTimeInMillis() - current.getTimeInMillis();
            int numberOfDays = (int) (timeDifference / 1000 / 60 / 60 / 24);
            dailyPages = (int) Math.ceil(
                            ((entry.getPagesToComplete() - entry.getReadPages()) / numberOfDays));

            entry.setDailyPages(dailyPages);
        }

        // Current progress
        EditText progressText = (EditText) findViewById(R.id.add_goals_progress_text);
        try {
            int priorProgress = Integer.parseInt(progressText.getText().toString());
            entry.setReadPages(priorProgress);
            // starting page for tomorrow's goal
            entry.setGoalStartPage(priorProgress + 1);
            // ending page for tomorrow's goal
            entry.setGoalEndPage(priorProgress + dailyPages);
        } catch (Exception e) {
            entry.setReadPages(0);
        }
    }

    // Make sure delete option is in toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_goal_menu, menu);
        return true;
    }

    // Save if necessary
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_goal:
                saveEntry();
                EntryAddWorker addWorker = new EntryAddWorker();
                addWorker.execute(entry);
                finish();
                return true;
            case R.id.cancel_goal:
                Toast.makeText(getApplicationContext(),	"Goal cancelled.",
                        Toast.LENGTH_SHORT).show();
                finish();
                return true;
            default:
                return false;
        }
    }

    /**
     * Worker thread to create new entry in the database in the background
     * without interfering with interface
     */
    class EntryAddWorker extends AsyncTask<GoalEntry, Void, String> {
        @Override
        protected String doInBackground(GoalEntry... params) {
            long id = entrySource.insertGoalEntry(params[0]);
            return String.valueOf(id);
        }

        @Override
        protected void onPostExecute(String newId) {
            Toast.makeText(getApplicationContext(),	"New goal" + newId + " was added.",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
