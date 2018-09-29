package kled.pagesaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
/*
This class allows the user to edit past book entries
 */
public class EditBookActivity extends AppCompatActivity implements View.OnClickListener{
    public final static String ID_BUNDLE_KEY = "_idbundle key";
    private long mEntryId;
    private BookEntry entry;

    private LatLng chosenLatLng = null;
    private final static int MAP_REQUEST_CODE = 5789;

    private EditText mProgressSoFarView;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private EditText mDurationHour;
    private EditText mDurationMinute;


    //bundle keys
    private final static String PROGRESS_SO_FAR_KEY = "psf";
    private final static String TIME_KEY = "time";
    private final static String DHOUR_KEY = "dhour";
    private final static String DMINUTE_KEY = "dminute";
    private final static String LAT_BUNDLE_KEY = "latitude";
    private final static String LNG_BUNDLE_KEY = "longitude";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_mark_complete);
        fab.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        mEntryId = bundle.getLong(ID_BUNDLE_KEY);

        entry = new BookEntryDbHelper(this).fetchEntryByIndex(mEntryId);

        Log.d("EDIT BOOK", "Entry is null with id " + mEntryId);

        setUpUIConnections();
    }

    private void setUpUIConnections() {
        mProgressSoFarView = (EditText)findViewById(R.id.edit_book_new_progress);
        mDatePicker = (DatePicker)findViewById(R.id.date_picker_edit_book);
        mTimePicker = (TimePicker)findViewById(R.id.time_picker_edit_book);
        mDurationHour = (EditText)findViewById(R.id.edit_hours_duration);
        mDurationMinute = (EditText)findViewById(R.id.edit_hours_minutes);
    }


    //REGION button call backs
    public void onAddLocationClick(View view) {
        Intent intent = new Intent(this, PSMapActivity.class);
        Bundle extras = new Bundle();
        extras.putString(PSMapActivity.MAP_MODE, PSMapActivity.PLACE_MARKER_MODE);
        intent.putExtras(extras);
        startActivityForResult(intent, MAP_REQUEST_CODE);
    }

    /*
    Called after mapview finishes
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MAP_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
                Bundle bundle = data.getExtras();
                double lat = bundle.getDouble(PSMapActivity.LAT_KEY, -1);
                double lng = bundle.getDouble(PSMapActivity.LNG_KEY, -1);
                if(lat != -1 && lng != -1)
                    chosenLatLng = new LatLng(lat, lng);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Don't do anything
            }
        }
    }

    /*
    Saves updated book entry information into the datastore.
     */
    public void onSaveButtonClicked(View view) {

        String errorString = isReadyToAdd();
        if(errorString.equals("")) {
            retrieveUIInfo();
            new BookEntryDbHelper(this).updateEntry(entry);
            //update datastore
            new EntryDatastoreHelper(this).updateEntry(entry);
            finish();
        } else {
            //SHOW DIALOG with error
            makeTextDialog(errorString);
        }

    }

    public void onCancelButtonClicked(View view) {
        finish();
    }

    public void onClick(View view) {
        int id = view.getId();

        if(id == R.id.fab_mark_complete) {
            Intent intent = new Intent(this, MarkCompleteActivity.class);
            Bundle extras = new Bundle();
            extras.putLong(MarkCompleteActivity.ID_BUNDLE_KEY, mEntryId);
            intent.putExtras(extras);
            startActivity(intent);
            finish();
        }
    }

    /*
    Checks to makes sure information was completely filled in
     */
    private String isReadyToAdd() {
        String errorString = "Must enter the following fields to add entry:\n";
        boolean canAddFlag = true;

        int lastPage = entry.getFurthestPageRead();
        if(!fieldNotEmpty(mProgressSoFarView)) {
            canAddFlag = false;
            errorString = errorString + "Page number >= " + lastPage + " and < "
                    + entry.getTotalPages() + "\n";
        } else {
            int progress = Integer.parseInt(mProgressSoFarView.getText().toString());
            if(progress >= entry.getTotalPages() || progress < lastPage) {
                canAddFlag = false;
                errorString = errorString + "Page number >= " + lastPage + " and < "
                        + entry.getTotalPages() + "\n";
            }
        }

        if(!fieldNotEmpty(mDurationHour)) {
            canAddFlag = false;
            errorString = errorString + "Hours Spent Reading\n";
        }

        if(!fieldNotEmpty(mDurationMinute)) {
            canAddFlag = false;
            errorString = errorString + "Minutes Spent Reading\n";
        }

        if (fieldNotEmpty(mDurationHour) && fieldNotEmpty(mDurationMinute)) {
            int durationHours = Integer.parseInt(mDurationHour.getText().toString());
            int durationMinutes = Integer.parseInt(mDurationMinute.getText().toString());

            if (durationHours == 0 && durationMinutes == 0) {
                canAddFlag = false;
                errorString = errorString + "Non-Zero Duration\n";
            }
        }


        //TODO UNCOMMENT THIS WHEN WESLEY FIXES

//        if(chosenLatLng == null) {
//            errorString = errorString + "Choose a Location\n";
//            canAddFlag = false;
//        }

        if(canAddFlag) {
            return "";
        } else {
            return errorString;
        }


    }

    private boolean fieldNotEmpty(EditText field) {
        if("".equals(field.getText().toString()))
            return false;

        return true;
    }

    private BookEntry retrieveUIInfo() {

        //Deal with progress entries
        int progressSoFar = Integer.parseInt(mProgressSoFarView.getText().toString());
        if(progressSoFar >= entry.getFurthestPageRead())
            entry.addPageRange(entry.getFurthestPageRead(), progressSoFar);


        long startTime = getChosenTime();

        //Get end time
        long minutes = Long.parseLong(mDurationMinute.getText().toString());
        long hours = Long.parseLong(mDurationHour.getText().toString());
        long endTime = startTime + 3600000 * hours + 60000 * minutes;

        if(endTime >= startTime)
            entry.addStartEndTime(startTime, endTime);


        //Add Location if any
        if(chosenLatLng != null)
            entry.addLatLng(chosenLatLng);

        return entry;

    }

    private long getChosenTime() {
        Calendar cal = Calendar.getInstance();
        int year = mDatePicker.getYear();
        int month = mDatePicker.getMonth();
        int day = mDatePicker.getDayOfMonth();
        int hour = mTimePicker.getHour();
        int minute = mTimePicker.getMinute();
        cal.set(year, month, day, hour, minute);
        return cal.getTimeInMillis();
    }

    private void makeTextDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    @Override
    protected void onSaveInstanceState(Bundle saveInstanceState) {
        saveInstanceState.putString(PROGRESS_SO_FAR_KEY,
                mProgressSoFarView.getText().toString());

        saveInstanceState.putLong(TIME_KEY, getChosenTime());
        saveInstanceState.putString(DHOUR_KEY,
                mDurationHour.getText().toString());
        saveInstanceState.putString(DMINUTE_KEY,
                mDurationMinute.getText().toString());

        if(chosenLatLng != null) {
            saveInstanceState.putDouble(LAT_BUNDLE_KEY, chosenLatLng.latitude);
            saveInstanceState.putDouble(LNG_BUNDLE_KEY, chosenLatLng.longitude);
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mProgressSoFarView.setText(savedInstanceState.getString(PROGRESS_SO_FAR_KEY, ""));
        mDurationHour.setText(savedInstanceState.getString(DHOUR_KEY, ""));
        mDurationMinute.setText(savedInstanceState.getString(DMINUTE_KEY, ""));

        long time = savedInstanceState.getLong(TIME_KEY);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        mDatePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        mTimePicker.setHour(cal.get(Calendar.HOUR));
        mTimePicker.setMinute(cal.get(Calendar.MINUTE));

        double lat = savedInstanceState.getDouble(LAT_BUNDLE_KEY, -1);
        double lng = savedInstanceState.getDouble(LNG_BUNDLE_KEY, -1);

        if(lat != -1 && lng != -1)
            chosenLatLng = new LatLng(lat, lng);
        else
            chosenLatLng = null;



    }


}
