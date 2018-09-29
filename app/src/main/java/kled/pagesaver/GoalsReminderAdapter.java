package kled.pagesaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Yu Liu on 3/6/2017.
 */

public class GoalsReminderAdapter extends ArrayAdapter<GoalEntry> {
    private Context mContext;
    private ArrayList<GoalEntry> goalsList;
    private static LayoutInflater inflater = null;

    public GoalsReminderAdapter(Context c, ArrayList<GoalEntry> viewedList) {
        super(c, R.layout.goal_reminder_row, viewedList);
        mContext = c;
        goalsList = viewedList;

        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return goalsList.size();
    }

    @Override
    public GoalEntry getItem(int position) {
        return goalsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Refresh list
     */
    public void addToAdapter(GoalEntry entry) {
        goalsList.add(entry);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            v = inflater.inflate(R.layout.goal_reminder_row, null);
        }

        TextView title = (TextView) v.findViewById(R.id.goal_reminder_title);
        TextView dailyGoal = (TextView) v.findViewById(R.id.goal_reminder_daily);
        TextView progress = (TextView) v.findViewById(R.id.goal_reminder_progress);
        TextView status = (TextView) v.findViewById(R.id.goal_reminder_status);

        String myTitle = goalsList.get(position).getBookTitle();
        int myGoalStart = goalsList.get(position).getGoalStartPage();
        int myGoalEnd = goalsList.get(position).getGoalEndPage() ;
        int myProgress = goalsList.get(position).getReadPages();

        title.setText("Book title: " + myTitle);

        dailyGoal.setText("Today's goal is page "+ myGoalStart + " to page "
                + myGoalEnd + ".");

        progress.setText("You have read " + myProgress + " pages so far.");

        // Compare current progress with goal for today
        if (myProgress < myGoalEnd) {
            status.setText("Goal not yet completed.");
        } else {
            status.setText("Goal completed!");
        }

        return v;
    }
}
