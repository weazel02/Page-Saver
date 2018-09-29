package kled.pagesaver;

import android.*;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
MainActivity starts after user logs in with Facebook
Shows fragment view and checks permissions
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TabLayout mTabBar;
    private ViewPager mViewPager;
    private ArrayList<Fragment> mFragmentList;
    private PSFragmentPagerAdapter mViewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        //register with the server
        new GcmRegistrationAsyncTask(this).execute();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open
                , R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //set up the tabbar and viewPager!
        mTabBar = (TabLayout)findViewById(R.id.tab_bar);
        mViewPager = (ViewPager)findViewById(R.id.view_pager);

        mFragmentList = new ArrayList<Fragment>(3);
        mFragmentList.add(new CurrentBooksFragment());
        mFragmentList.add(new PreviousBooksFragment());
        mFragmentList.add(new SearchFragment());
        //Add the start, history, and settings fragments

        //Bind the tab bar and the view pager for seamless transitions between tabs and fragments
        mViewPagerAdapter = new PSFragmentPagerAdapter(getFragmentManager()
                , mFragmentList);
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabBar.setupWithViewPager(mViewPager);
        mTabBar.setTabMode(TabLayout.MODE_FIXED);

    }

    @Override
    protected void onResume() {
        //start to get the entries from the server!
        super.onResume();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    /*
Code rewritten from onReuestPermissionsResult in IAmHere
*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1]
                == PackageManager.PERMISSION_GRANTED
                || grantResults[2] == PackageManager.PERMISSION_GRANTED) {

        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)
                        || shouldShowRequestPermissionRationale
                        (android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || shouldShowRequestPermissionRationale
                        (android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]
                                        {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                android.Manifest.permission.CAMERA,
                                                android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                            }

                        }
                    });
                    requestPermissions(new String[]{android.Manifest.permission
                            .WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }else{
                }
            }
        }
    }

    private void checkPermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA, android.Manifest
                    .permission.ACCESS_FINE_LOCATION}, 0);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.add_menu_item:
                Intent intent = new Intent(this, AddBookActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        Intent intent;
        int id = item.getItemId();

        switch(id) {
            case R.id.nav_map:
                intent = new Intent(this, PSMapActivity.class);
                intent.putExtra(PSMapActivity.MAP_MODE, PSMapActivity.VIEW_ALL_ENTRIES);
                //TODO send locations and names to mapview
                ArrayList<BookEntry> allEntries = new BookEntryDbHelper(this).fetchEntries();
                Map<LatLng, Set<String>> locsWithBooks =
                        getSetOfLocationsWithBookTitles(allEntries);

                List<LatLng> locations = new ArrayList<>();
                ArrayList<String> titleStrings = new ArrayList<>();

                for(LatLng latLng : locsWithBooks.keySet()) {
                    locations.add(latLng);
                    Set<String>  titles = locsWithBooks.get(latLng);
                    String titleList = "";

                    int count = 0;
                    for(String title : titles) {
                        titleList += title;
                        if(count < titles.size() - 1) {
                            titleList += ", ";
                        }
                        count++;
                    }

                    titleStrings.add(titleList);
                }

                Bundle extras = new Bundle();
                extras.putStringArrayList(PSMapActivity.BOOKS_LIST, titleStrings);
                byte[] bytes = BookEntry.getLocationByteArray(locations);
                extras.putByteArray(PSMapActivity.LOCATIONS_LIST, bytes);
                intent.putExtras(extras);
                startActivity(intent);
                break;
            case R.id.nav_analytics:
                intent = new Intent(this, AnalyticsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_goal_tracker:
                intent = new Intent(this, GoalsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
    Returns a map mapping a LatLng with a string of Book Titles
     */
    private Map<LatLng, Set<String>> getSetOfLocationsWithBookTitles(List<BookEntry> entries) {
        Map<LatLng, Set<String>> map = new HashMap<>();

        for(BookEntry entry : entries) {
            for(LatLng latLng : entry.getLocationList()) {
                if(map.containsKey(latLng)) {
                    Set<String> oldSet = map.get(latLng);
                    oldSet.add(entry.getTitle());
                    map.put(latLng, oldSet);
                } else {
                    Set<String> newSet = new HashSet<>();
                    newSet.add(entry.getTitle());
                    map.put(latLng, newSet);
                }
            }
        }

        return map;
    }




}
