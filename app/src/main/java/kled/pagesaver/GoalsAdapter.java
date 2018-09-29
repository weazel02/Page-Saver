package kled.pagesaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kelley on 2/26/2017.
 * An adapter view to see a list of all reading goals created
 */

public class GoalsAdapter extends ArrayAdapter<GoalEntry> {

    private Context mContext;
    private ArrayList<GoalEntry> goalsList;
    private static LayoutInflater inflater = null;

    public GoalsAdapter(Context c, ArrayList<GoalEntry> viewedList) {
        super(c, R.layout.goal_row, viewedList);
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
            v = inflater.inflate(R.layout.goal_row, null);
        }

        TextView title = (TextView) v.findViewById(R.id.goal_title);
        TextView text = (TextView) v.findViewById(R.id.goal_description);

        title.setText(goalsList.get(position).getBookTitle());
        text.setText(goalsList.get(position).getDescription());

        return v;
    }

}
