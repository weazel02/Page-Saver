package kled.pagesaver;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

/**
 * Created by Danielle on 2/24/17.
 * This is the fragment that allows the user to search their past entries by typing in words
 */

public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener{
    private View view;
    private ListView listView;
    private SearchView searchView;
    private TextView searchErrorView;
    private DBEntryAdapter adapter;
    private ReadInEntriesAsyncTask task;

    private List<BookEntry> oldEntries;
    private boolean oldEntriesAreSaved = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set up the adapter and the listView, but don't add database items.
        //They will be added in onResume.
        adapter = new DBEntryAdapter(getActivity(), DBEntryAdapter.ALL_MODE);

        view = inflater.inflate(R.layout.search_frag, container, false);

        searchView = (SearchView)view.findViewById(R.id.search_bar);
        searchErrorView = (TextView)view.findViewById(R.id.search_error_textbox);

        searchView.setOnQueryTextListener(this);

        listView = (ListView)view.findViewById(R.id.search_lv);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if(!oldEntriesAreSaved) {
            Log.d("SEARCHFRAG", "SAVING LIST");
            oldEntries = adapter.getList();
            oldEntriesAreSaved = true;
        }

        if(newText.equals("")) {
            Log.d("SEARCHFRAG", "IN EMPTY TEXT");
            adapter.setList(oldEntries);
            Log.d("SEARCHFRAG", "SIZE_LIST IS " + oldEntries.size());
            searchErrorView.setVisibility(View.GONE);
            return true;
        }

        Search search = new Search(newText, oldEntries);
        Set<BookEntry> set = search.narrowEntries();

        adapter.setList(set);

        if(set.size() == 0) {
            //Set up the error
            searchErrorView.setVisibility(View.VISIBLE);
        } else {
            searchErrorView.setVisibility(View.GONE);
        }

       return true;
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        //Now modify it!
        Search search = new Search(newText, oldEntries);
        Set<BookEntry> set = search.narrowEntries();

        adapter.setList(set);

        if(set.size() == 0) {
            //Set up the error
            searchErrorView.setVisibility(View.VISIBLE);
        } else {
            searchErrorView.setVisibility(View.GONE);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Reset the adapter to reflect any changes made to the database;
        adapter.clearAdapter();

        task = new ReadInEntriesAsyncTask(getActivity(), ReadInEntriesAsyncTask.ALL_MODE, adapter);
        task.execute();

        searchErrorView.setVisibility(View.GONE);

        oldEntriesAreSaved = false;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        Bundle extras = new Bundle();
        String key;

        if(((BookEntry)adapter.getItem(position)).isComplete()) {
            intent = new Intent(getActivity(), ViewPastBookActivity.class);
            key = ViewPastBookActivity.ID_BUNDLE_KEY;
        } else {
            intent = new Intent(getActivity(), ViewCurrentBookActivity.class);
            key = ViewCurrentBookActivity.ID_BUNDLE_KEY;
        }

        extras.putLong(key, id);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(task != null)
            task.cancel(true);

    }
}
