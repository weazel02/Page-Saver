package kled.pagesaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Yu Liu on 3/7/2017.
 * This class allows users to update goals
 */

public class GoalUpdateDialogFragment extends DialogFragment {

    public interface GoalUpdateDialogListener {
        public void onFinishEditGoalUpdateDialog(int inputNewGoal);
    }

    GoalUpdateDialogFragment.GoalUpdateDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (GoalUpdateDialogFragment.GoalUpdateDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View goalUpdateView = inflater.inflate(R.layout.dialog_goal_update, null);

        final TextView goalUpdateTv = (EditText) goalUpdateView.findViewById(R.id.goal_update);

        // Allow the user to input a new goal
        builder.setView(goalUpdateView)
                // Save new goal
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Pass new goal back to ViewGoalActivity
                        mListener.onFinishEditGoalUpdateDialog(Integer.parseInt
                                (goalUpdateTv.getText().toString()));
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        GoalUpdateDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
