package kled.pagesaver;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Danielle on 2/24/17.
 * This fragment displays a list of the current books
 * (entries before the user has completed them)
 */

public class CurrentBooksFragment extends Fragment implements AdapterView.OnItemClickListener {
    private View view;
    private ListView listView;
    private DBEntryAdapter adapter;
    private ReadInEntriesAsyncTask task;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set up the adapter and the listView, but don't add database items.
        //They will be added in onResume.
        adapter = new DBEntryAdapter(getActivity(), DBEntryAdapter.CURRENT_MODE);
        view = inflater.inflate(R.layout.book_list_view, container, false);
        listView = (ListView)view.findViewById(R.id.book_lv);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Reset the adapter to reflect any changes made to the database;
        adapter.clearAdapter();

        task = new ReadInEntriesAsyncTask(getActivity(), ReadInEntriesAsyncTask.CURRENT_MODE, adapter);
        task.execute();
    }

    /*
    Called when user clicks on a current book entry
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), ViewCurrentBookActivity.class);
        Bundle extras = new Bundle();
        extras.putLong(ViewCurrentBookActivity.ID_BUNDLE_KEY, id);
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
