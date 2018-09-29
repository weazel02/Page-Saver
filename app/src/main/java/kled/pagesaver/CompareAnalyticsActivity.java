package kled.pagesaver;

import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

/*
This activity shows users data compared with that of the rest of the app's users
 */
public class CompareAnalyticsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<BookEntry>> {

    private EntryDatastoreHelper datastoreHelper;

    // If using scaled analytics
    private boolean scaled;

    public String message;

    public ArrayList<BookEntry> entries;

    private int[] universeHoursArray;
    private int[] universeMonthsArray;
    private ArrayList<Integer> universePagesArray;
    private ArrayList<Integer> universeDurationArray;
    private int[] universePagesPerMonthsArray;

    private int[] personalHoursArray;
    private int[] personalMonthsArray;
    private ArrayList<Integer> personalPagesArray;
    private ArrayList<Integer> personalDurationArray;
    private int[] personalPagesPerMonthsArray;

    boolean isBound = false;

    GcmIntentService messageService;
    public UpdateMessageHandler updateHandler;
    LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_analytics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        datastoreHelper = new EntryDatastoreHelper(getApplicationContext());
        updateHandler = new UpdateMessageHandler();

        // Bind to service to get information from datastore
        Intent messageIntent = new Intent(this, GcmIntentService.class);
        bindService(messageIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        scaled = true;

        // Get personal points
        loaderManager = this.getLoaderManager();
        loaderManager.initLoader(1, null, this);

        // Send message to get universe information
        UpdateUniverseWorker updateUniverseWorker = new UpdateUniverseWorker();
        updateUniverseWorker.execute();
    }

    public void updateView() {
        getUniversePoints();
        getPersonalPoints();
        buildTimesGraph();
        buildMonthsGraph();
        buildDurationGraph();
        buildPagesGraph();
        buildMonthlyPagesGraph();
    }

    /**
     * Get personal data
     */
    public void getPersonalPoints() {
        if (entries == null) return;

        // Initialize data structures

        personalHoursArray = new int[24];
        for (int i = 0; i < personalHoursArray.length; i++) {
            personalHoursArray[i] = 0;
        }
        personalMonthsArray = new int[12];
        for (int i = 0; i < personalMonthsArray.length; i++) {
            personalMonthsArray[i] = 0;
        }
        personalPagesArray = new ArrayList<>();
        personalDurationArray = new ArrayList<>();
        personalPagesPerMonthsArray = new int[12];
        for (int i = 0; i < personalPagesPerMonthsArray.length; i++) {
            personalPagesPerMonthsArray[i] = 0;
        }

        for (int i = 0; i < entries.size(); i++) {

            ArrayList<BookEntry.StartEndTimes> individualTime = entries.get(i).getTimeList();
            ArrayList<BookEntry.StartEndPages> individualPage = entries.get(i).getPageList();

            // Get times and durations
            if (individualTime.size() == individualPage.size()) {
                for (int j = 0; j < individualTime.size(); j++) {

                    Long startTime = individualTime.get(j).startTime;
                    Long endTime = individualTime.get(j).endTime;
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(startTime);
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int month = cal.get(Calendar.MONTH);

                    personalHoursArray[hour]++;
                    personalMonthsArray[month]++;

                    // Difference in milliseconds - converted to hours
                    Long timeDiff = endTime - startTime;
                    int minutesRead = (int) (timeDiff / 1000 / 60 / 60);

                    personalDurationArray.add(minutesRead);

                    int diff = individualPage.get(j).endPage - individualPage.get(j).startPage;
                    personalPagesArray.add(diff);

                    personalPagesPerMonthsArray[month] =
                            personalPagesPerMonthsArray[month] + diff;
                }
            }
        }

        if (scaled) scalePersonalValues();
    }

    /**
     * Scale personal values
     */
    public void scalePersonalValues() {
        // get totals and divide
        double personalHoursTotal = 0;
        double personalMonthsTotal = 0;
        double personalPagesPerMonthTotal = 0;

        for (int entry : personalHoursArray) {
            personalHoursTotal += entry;
        }

        for (int i = 0; i < personalHoursArray.length; i++) {
            personalHoursArray[i] = (int)(((double)personalHoursArray[i]
                    / personalHoursTotal) * 100);
        }

        for (int entry : personalMonthsArray) {
            personalMonthsTotal += entry;
        }

        for (int i = 0; i < personalMonthsArray.length; i++) {
            personalMonthsArray[i] = (int)(((double)personalMonthsArray[i]
                    / personalMonthsTotal) * 100);
        }

        for (int entry : personalPagesPerMonthsArray) {
            if (entry >= 0)
                personalPagesPerMonthTotal += entry;
        }

        // No negative values
        for (int i = 0; i < personalPagesPerMonthsArray.length; i++) {
            if (personalPagesPerMonthsArray[i] >= 0) {
                personalPagesPerMonthsArray[i] = (int)(((double)personalPagesPerMonthsArray[i]
                        / personalPagesPerMonthTotal) * 100);
            } else {
                personalPagesPerMonthsArray[i] = 0;
            }
        }
    }

    /**
     * Get universe data
     */
    public void getUniversePoints() {

        if (message == null) return;

        // Initialize data structures

        universeHoursArray = new int[24];
        for (int i = 0; i < universeHoursArray.length; i++) {
            universeHoursArray[i] = 0;
        }
        universeMonthsArray = new int[12];
        for (int i = 0; i < universeMonthsArray.length; i++) {
            universeMonthsArray[i] = 0;
        }
        universePagesArray = new ArrayList<>();
        universeDurationArray = new ArrayList<>();
        universePagesPerMonthsArray = new int[12];
        for (int i = 0; i < universePagesPerMonthsArray.length; i++) {
            universePagesPerMonthsArray[i] = 0;
        }

        String[] messages = message.split(" ");

        for (String point : messages) {
            String[] parts = point.split(",");

            // Make sure the info has all the necessary components
            if (parts.length == 4) {

                int hours = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int duration = Integer.parseInt(parts[2]);
                int numPages = Integer.parseInt(parts[3]);

                universeHoursArray[hours]++;
                universeMonthsArray[month]++;
                universeDurationArray.add(duration);
                universePagesArray.add(numPages);
                universePagesPerMonthsArray[month] =
                        universePagesPerMonthsArray[month] + numPages;
            }
        }

        if(scaled) scaleUniverseValues();
    }

    /**
     * Scale universe values for averages
     */
    public void scaleUniverseValues() {

        // get totals and divide
        double universeHoursTotal = 0;
        double universeMonthsTotal = 0;
        double universePagesPerMonthTotal = 0;

        for (int entry : universeHoursArray) {
            universeHoursTotal += entry;
        }

        for (int i = 0; i < universeHoursArray.length; i++) {
            universeHoursArray[i] = (int)(((double)universeHoursArray[i]
                    / universeHoursTotal) * 100);
        }

        for (int entry : universeMonthsArray) {
            universeMonthsTotal += entry;
        }

        for (int i = 0; i < universeMonthsArray.length; i++) {
            universeMonthsArray[i] = (int)(((double)universeMonthsArray[i]
                    / universeMonthsTotal) * 100);
        }

        for (int entry : universePagesPerMonthsArray) {
            if (entry >= 0)
                universePagesPerMonthTotal += entry;
        }

        for (int i = 0; i < universePagesPerMonthsArray.length; i++) {
            if (universePagesPerMonthsArray[i] >= 0) {
                universePagesPerMonthsArray[i] = (int)(((double)universePagesPerMonthsArray[i]
                        / universePagesPerMonthTotal) * 100);
            } else {
                universePagesPerMonthsArray[i] = 0;
            }
        }
    }

    /**
     * Draw chart of time of day
     */
    public void buildTimesGraph() {

        ColumnChartView hoursChart;
        ColumnChartData hoursData;

        hoursChart = (ColumnChartView)findViewById(R.id.time_chart);

        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < universeHoursArray.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();

            // Universe
            SubcolumnValue valueUniverse = new SubcolumnValue(universeHoursArray[i],
                    ChartUtils.COLOR_BLUE);
            values.add(valueUniverse);

            // Personal
            SubcolumnValue valuePersonal = new SubcolumnValue(personalHoursArray[i],
                    ChartUtils.COLOR_VIOLET);
            values.add(valuePersonal);

            Column column = new Column(values);
            columns.add(column);

        }

        hoursData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> hoursLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < universeHoursArray.length; i++) {

            AxisValue value = new AxisValue(i);

            if (i < 12) {
                if (i == 0) value.setLabel("12 AM");
                else value.setLabel(i + "AM");
            } else {
                if (i == 12) value.setLabel("12 PM");
                else value.setLabel((i - 12) + "PM");
            }

            hoursLabels.add(value);
        }

        Axis axisX = new Axis(hoursLabels);
        axisX.setName("Hour of Day You Start Reading");

        Axis axisY = new Axis().setHasLines(true);
        axisY.setName("% of Entries");

        hoursData.setAxisXBottom(axisX);
        hoursData.setAxisYLeft(axisY);

        // Set data
        hoursChart.setColumnChartData(hoursData);
    }

    /**
     * Draw chart of months of reading
     */
    public void buildMonthsGraph() {

        ColumnChartView monthsChart;
        ColumnChartData monthsData;

        monthsChart = (ColumnChartView) findViewById(R.id.month_chart);

        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < universeMonthsArray.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();

            // Universe
            SubcolumnValue valueUniverse = new SubcolumnValue(universeMonthsArray[i],
                    ChartUtils.COLOR_BLUE);
            values.add(valueUniverse);

            // Personal
            SubcolumnValue valuePersonal = new SubcolumnValue(personalMonthsArray[i],
                    ChartUtils.COLOR_VIOLET);
            values.add(valuePersonal);

            Column column = new Column(values);
            columns.add(column);
        }

        monthsData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> monthLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < universeMonthsArray.length; i++) {

            AxisValue value = new AxisValue(i);

            if (i == 0) value.setLabel("Jan");
            else if (i == 1) value.setLabel("Feb");
            else if (i == 2) value.setLabel("Mar");
            else if (i == 3) value.setLabel("Apr");
            else if (i == 4) value.setLabel("May");
            else if (i == 5) value.setLabel("Jun");
            else if (i == 6) value.setLabel("Jul");
            else if (i == 7) value.setLabel("Aug");
            else if (i == 8) value.setLabel("Sep");
            else if (i == 9) value.setLabel("Oct");
            else if (i == 10) value.setLabel("Nov");
            else if (i == 11) value.setLabel("Dec");

            monthLabels.add(value);
        }

        Axis axisX = new Axis(monthLabels);
        axisX.setName("Month");

        Axis axisY = new Axis().setHasLines(true);
        axisY.setName("% of Entries");

        monthsData.setAxisXBottom(axisX);
        monthsData.setAxisYLeft(axisY);

        // Set data
        monthsChart.setColumnChartData(monthsData);
    }

    /**
     * Draw chart of durations
     */
    public void buildDurationGraph() {

        ColumnChartView durationChart;
        ColumnChartData durationData;

        durationChart = (ColumnChartView) findViewById(R.id.duration_chart);

        // Create columns from entries

        // Universe
        int[] universeHoursRead = new int[6];

        for (int i = 0; i < universeDurationArray.size(); i++) {
            int universeHours = universeDurationArray.get(i);
            if (universeHours > 5 ) universeHours = 5;
            universeHoursRead[universeHours]++;
        }

        // Personal
        int[] personalHoursRead = new int[6];

        for (int i = 0; i < personalDurationArray.size(); i++) {
            int personalHours = personalDurationArray.get(i);
            if (personalHours > 5 ) personalHours = 5;
            personalHoursRead[personalHours]++;
        }

        if (scaled) {
            int universeDurationTotal = 0;

            for (int entry : universeHoursRead) {
                universeDurationTotal += entry;
            }

            for (int i = 0; i < universeHoursRead.length; i++) {
                universeHoursRead[i] = (int)(((double)universeHoursRead[i]
                        / universeDurationTotal) * 100);
            }

            int personalDurationTotal = 0;

            for (int entry : personalHoursRead) {
                personalDurationTotal += entry;
            }

            for (int i = 0; i < personalHoursRead.length; i++) {
               personalHoursRead[i] = (int)(((double)personalHoursRead[i]
                        / personalDurationTotal) * 100);
            }
        }

        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < universeHoursRead.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();

            // Universe
            SubcolumnValue valueUniverse = new SubcolumnValue(universeHoursRead[i],
                    ChartUtils.COLOR_BLUE);
            values.add(valueUniverse);

            // Personal
            SubcolumnValue valuePersonal = new SubcolumnValue(personalHoursRead[i],
                    ChartUtils.COLOR_VIOLET);
            values.add(valuePersonal);

            Column column = new Column(values);
            columns.add(column);

        }

        durationData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> durationLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < universeHoursRead.length; i++) {

            AxisValue value = new AxisValue(i);

            if (i == 0) value.setLabel("< 1 hr");
            else if (i == 1) value.setLabel("1-2 hrs");
            else if (i == 2) value.setLabel("2-3 hrs");
            else if (i == 3) value.setLabel("3-4 hrs");
            else if (i == 4) value.setLabel("4-5 hrs");
            else if (i == 5) value.setLabel("5+ hrs");

            durationLabels.add(value);
        }

        Axis axisX = new Axis(durationLabels);
        axisX.setName("Hours Read");

        Axis axisY = new Axis().setHasLines(true);
        axisY.setName("% of Entries");

        durationData.setAxisXBottom(axisX);
        durationData.setAxisYLeft(axisY);

        // Set data
        durationChart.setColumnChartData(durationData);
    }

    /**
     * Draw chart of pages read per session
     */
    public void buildPagesGraph() {

        ColumnChartView pagesChart;
        ColumnChartData pagesData;

        pagesChart = (ColumnChartView) findViewById(R.id.pages_chart);

        // Create columns from entries
        int[] universePagesRead = new int[5];

        for (int i = 0; i < universePagesArray.size(); i++) {
            int universePages = universePagesArray.get(i);

            // Group in chunks of 10 pages read
            int universePagesGroup = universePages / 25;
            if (universePagesGroup > 4 ) universePagesGroup = 4;
            universePagesRead[universePagesGroup]++;
        }

        int[] personalPagesRead = new int[5];

        for (int i = 0; i < personalPagesArray.size(); i++) {
            int personalPages = personalPagesArray.get(i);

            // Group in chunks of 10 pages read
            int personalPagesGroup = personalPages / 25;
            if (personalPagesGroup > 4 ) personalPagesGroup = 4;
            personalPagesRead[personalPagesGroup]++;
        }

        if (scaled) {
            int universePagesTotal = 0;

            for (int entry : universePagesRead) {
                universePagesTotal += entry;
            }

            for (int i = 0; i < universePagesRead.length; i++) {
                universePagesRead[i] = (int)(((double)universePagesRead[i]
                        / universePagesTotal) * 100);
            }

            int personalPagesTotal = 0;

            for (int entry : personalPagesRead) {
                personalPagesTotal += entry;
            }

            for (int i = 0; i < personalPagesRead.length; i++) {
                personalPagesRead[i] = (int)(((double)personalPagesRead[i]
                        / personalPagesTotal) * 100);
            }
        }

        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < universePagesRead.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();

            // Universe
            SubcolumnValue valueUniverse = new SubcolumnValue(universePagesRead[i],
                    ChartUtils.COLOR_BLUE);
            values.add(valueUniverse);

            // Personal
            SubcolumnValue valuePersonal = new SubcolumnValue(personalPagesRead[i],
                    ChartUtils.COLOR_VIOLET);
            values.add(valuePersonal);

            Column column = new Column(values);
            columns.add(column);

        }

        pagesData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> pagesLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < universePagesRead.length; i++) {

            AxisValue value = new AxisValue(i);

            if (i == 0) value.setLabel("< 25 pg");
            else if (i == 1) value.setLabel("25-50 pgs");
            else if (i == 2) value.setLabel("50-75 pgs");
            else if (i == 3) value.setLabel("75-100 pgs");
            else if (i == 4) value.setLabel("100+ pgs");

            pagesLabels.add(value);
        }

        Axis axisX = new Axis(pagesLabels);
        axisX.setName("Pages Read");

        Axis axisY = new Axis().setHasLines(true);
        axisY.setName("% of Entries");

        pagesData.setAxisXBottom(axisX);
        pagesData.setAxisYLeft(axisY);

        // Set data
        pagesChart.setColumnChartData(pagesData);
    }

    /**
     * Draw chart for number of pages read per month
     */
    public void buildMonthlyPagesGraph() {
        ColumnChartView monthlyPagesChart;
        ColumnChartData monthlyPagesData;

        monthlyPagesChart = (ColumnChartView) findViewById(R.id.monthly_pages_chart);

        // Construct column values
        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < universePagesPerMonthsArray.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();

            // Universe
            SubcolumnValue valueUniverse = new SubcolumnValue(universePagesPerMonthsArray[i],
                    ChartUtils.COLOR_BLUE);
            values.add(valueUniverse);

            SubcolumnValue valuePersonal = new SubcolumnValue(personalPagesPerMonthsArray[i],
                    ChartUtils.COLOR_VIOLET);
            values.add(valuePersonal);

            Column column = new Column(values);
            columns.add(column);

        }

        monthlyPagesData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> monthLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < universePagesPerMonthsArray.length; i++) {

            AxisValue value = new AxisValue(i);

            if (i == 0) value.setLabel("Jan");
            else if (i == 1) value.setLabel("Feb");
            else if (i == 2) value.setLabel("Mar");
            else if (i == 3) value.setLabel("Apr");
            else if (i == 4) value.setLabel("May");
            else if (i == 5) value.setLabel("Jun");
            else if (i == 6) value.setLabel("Jul");
            else if (i == 7) value.setLabel("Aug");
            else if (i == 8) value.setLabel("Sep");
            else if (i == 9) value.setLabel("Oct");
            else if (i == 10) value.setLabel("Nov");
            else if (i == 11) value.setLabel("Dec");

            monthLabels.add(value);
        }

        Axis axisX = new Axis(monthLabels);
        axisX.setName("Month");

        Axis axisY = new Axis().setHasLines(true);
        axisY.setName("% of Total Pages Read");

        monthlyPagesData.setAxisXBottom(axisX);
        monthlyPagesData.setAxisYLeft(axisY);

        // Set data
        monthlyPagesChart.setColumnChartData(monthlyPagesData);
    }

    /**
     * Connect to GcmIntentService
     */
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GcmIntentService.MessageBinder binder = (GcmIntentService.MessageBinder) service;
            messageService = binder.getService();

            binder.getUIMsgHandler(updateHandler);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            messageService = null;
        }

    };

    /**
     * Receive message from service to update view
     */
    public class UpdateMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            if(msg.what == GcmIntentService.MSG_INT_VALUE) {
                message = messageService.getMessage();
                updateView();
            }
        }
    }

    /**
     * Update universe information
     */
    private class UpdateUniverseWorker extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            message = "";
            datastoreHelper.retrieveDatastore();
            return null;
        }

        @Override
        protected void onPostExecute(Void params){

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Temporarily unbind
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Rebind the service if unbounded previous and if you're still tracking
        if(!isBound) {
            Intent messageIntent = new Intent(this, GcmIntentService.class);
            // Rebind the service
            bindService(messageIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public Loader<ArrayList<BookEntry>> onCreateLoader(int id, Bundle args) {
        LoadBookEntries loadBookEntries = new LoadBookEntries(this);
        return loadBookEntries;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<BookEntry>> loader, ArrayList<BookEntry> data) {
        entries = data;
        updateView();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<BookEntry>> loader) {

    }
}
