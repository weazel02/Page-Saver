package kled.pagesaver;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
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
Analytics activity builds graphs based on the user's reading habits
 */
public class AnalyticsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<ArrayList<BookEntry>> {

    private ArrayList<Integer> hoursArray;
    private ArrayList<Integer> monthsArray;
    private ArrayList<Integer> pagesArray;
    private ArrayList<Integer> durationArray;
    private int[] pagesPerMonthsArray;

    LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loaderManager = this.getLoaderManager();
        loaderManager.initLoader(1, null, this);
    }

    // Toolbar to add
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compare_analytics_menu, menu);
        return true;
    }

    // Add Entry Page
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.compare_analytics:
                Intent intent = new Intent(this, CompareAnalyticsActivity.class);
                startActivity(intent);
                return true;
            default:
                return true;
        }
    }

    /**
     * Get durations, start times, and pages read per reading session
     */
    public void getPoints(ArrayList<BookEntry> entries) {
        if(entries==null) return;

        hoursArray = new ArrayList<>();
        monthsArray = new ArrayList<>();
        pagesArray = new ArrayList<>();
        durationArray = new ArrayList<>();
        pagesPerMonthsArray = new int[12];
        for (int i = 0; i < pagesPerMonthsArray.length; i++) {
            pagesPerMonthsArray[i] = 0;
        }

        for (int i = 0; i < entries.size(); i++) {

            ArrayList<BookEntry.StartEndTimes> individualTime = entries.get(i).getTimeList();
            ArrayList<BookEntry.StartEndPages> individualPage = entries.get(i).getPageList();

            // Get times and durations
            for (int j = 0; j < individualTime.size(); j++ ) {
                Long startTime = individualTime.get(j).startTime;
                Long endTime = individualTime.get(j).endTime;
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(startTime);
                hoursArray.add(cal.get(Calendar.HOUR_OF_DAY));
                monthsArray.add(cal.get(Calendar.MONTH));

                // Difference in milliseconds - converted to hours
                Long timeDiff = endTime - startTime;
                int minutesRead = (int) (timeDiff / 1000 / 60 / 60);

                durationArray.add(minutesRead);
            }

            // Get pages
            for (int j = 0; j < individualPage.size(); j++ ) {
                int diff = individualPage.get(j).endPage - individualPage.get(j).startPage;
                pagesArray.add(diff);
            }

            // Make sure the pages match up with the times
            if (individualPage.size() == individualTime.size()) {
                for (int j = 0; j < individualPage.size(); j++ ) {
                    Long startTime = individualTime.get(j).startTime;
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(startTime);
                    int month = cal.get(Calendar.MONTH);

                    int diff = individualPage.get(j).endPage - individualPage.get(j).startPage;

                    int currentPages = pagesPerMonthsArray[month];

                    // Update how many pages per month read
                    pagesPerMonthsArray[month] = currentPages + diff;
                }
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

        // Create columns from entries
        int[] hours = new int[24];

        for (int i = 0; i < hoursArray.size(); i++) {
            int hourObtained = hoursArray.get(i);
            hours[hourObtained]++;
        }

        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < hours.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
            SubcolumnValue value = new SubcolumnValue(hours[i], ChartUtils.COLOR_VIOLET);
            values.add(value);

            Column column = new Column(values);
            columns.add(column);

        }

        hoursData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> hoursLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < hours.length; i++) {

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
        axisY.setName("Number of Times");

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

        // Create columns from entries
        int[] months = new int[12];

        for (int i = 0; i < monthsArray.size(); i++) {
            int monthObtained = monthsArray.get(i);
            months[monthObtained]++;
        }

        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < months.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
            SubcolumnValue value = new SubcolumnValue(months[i], ChartUtils.COLOR_VIOLET);
            values.add(value);

            Column column = new Column(values);
            columns.add(column);

        }

        monthsData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> monthLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < months.length; i++) {

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
        axisY.setName("Number of Times");

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
        int[] hoursRead = new int[6];

        for (int i = 0; i < durationArray.size(); i++) {
            int hours = durationArray.get(i);
            if (hours > 5 ) hours = 5;
            hoursRead[hours]++;
        }

        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < hoursRead.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
            SubcolumnValue value = new SubcolumnValue(hoursRead[i], ChartUtils.COLOR_VIOLET);
            values.add(value);

            Column column = new Column(values);
            columns.add(column);

        }

        durationData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> durationLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < hoursRead.length; i++) {

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
        axisY.setName("Number of Times");

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
        int[] pagesRead = new int[5];

        for (int i = 0; i < pagesArray.size(); i++) {
            int pages = pagesArray.get(i);

            // Group in chunks of 10 pages read
            int pagesGroup = pages / 25;
            if (pagesGroup > 4 ) pagesGroup = 4;
            pagesRead[pagesGroup]++;
        }

        List<Column> columns = new ArrayList<Column>();

        for (int i = 0; i < pagesRead.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
            SubcolumnValue value = new SubcolumnValue(pagesRead[i], ChartUtils.COLOR_VIOLET);
            values.add(value);

            Column column = new Column(values);
            columns.add(column);

        }

        pagesData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> pagesLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < pagesRead.length; i++) {

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
        axisY.setName("Number of Times");

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

        for (int i = 0; i < pagesPerMonthsArray.length; ++i) {

            List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
            SubcolumnValue value = new SubcolumnValue(pagesPerMonthsArray[i],
                    ChartUtils.COLOR_VIOLET);
            values.add(value);

            Column column = new Column(values);
            columns.add(column);

        }

        monthlyPagesData = new ColumnChartData(columns);

        // Format axes
        List<AxisValue> monthLabels = new ArrayList<AxisValue>();

        for (int i = 0; i < pagesPerMonthsArray.length; i++) {

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
        axisY.setName("Number of Pages");

        monthlyPagesData.setAxisXBottom(axisX);
        monthlyPagesData.setAxisYLeft(axisY);

        // Set data
        monthlyPagesChart.setColumnChartData(monthlyPagesData);
    }

    @Override
    public Loader<ArrayList<BookEntry>> onCreateLoader(int id, Bundle args) {
        LoadBookEntries loadBookEntries = new LoadBookEntries(this);
        return loadBookEntries;
    }

    /*
    Initializes building graphs on Analytics page
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<BookEntry>> loader, ArrayList<BookEntry> data) {
        getPoints(data);
        buildTimesGraph();
        buildMonthsGraph();
        buildDurationGraph();
        buildPagesGraph();
        buildMonthlyPagesGraph();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<BookEntry>> loader) {

    }
}
