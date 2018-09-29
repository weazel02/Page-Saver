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
 * Created by Yu Liu on 3/1/2017.
 */

public class CustomRangeDialogFragment extends DialogFragment {

    public interface CustomRangeDialogListener {
        public void onFinishEditDialog(int inputRange);
    }

    CustomRangeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (CustomRangeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customRangeView = inflater.inflate(R.layout.dialog_custom_range, null);

        final TextView customRangeTv = (EditText) customRangeView.findViewById(R.id.custom_range);

        // Allow the user to input a custom page range
        builder.setView(customRangeView)
                // Save custom range
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Pass custom page increment back to ViewGoalActivity
                        mListener.onFinishEditDialog(Integer.parseInt
                                (customRangeTv.getText().toString()));
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CustomRangeDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
