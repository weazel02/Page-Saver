package kled.pagesaver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Yu Liu on 3/7/2017.
 * A receiver to create a goal's alarm to help user keep up with reading goals
 */

public class GoalAlarmReceiver extends BroadcastReceiver{

    private GoalsDbHelper goalsDatabase;

    @Override
    public void onReceive(final Context context, Intent intent) {

        // update goal starting page for next day
        goalsDatabase = new GoalsDbHelper(context);
        goalsDatabase.updateNextDayGoal();
    }

}
