package kled.pagesaver;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.iid.InstanceID;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Danielle on 2/26/17.
 * A helper class to interact with the datastore
 */

public class EntryDatastoreHelper {
    private Context mContext;
    private String phone_id = null;

    public EntryDatastoreHelper(Context context) {
        mContext = context;
    }

    public String getInstanceId() {
        if(phone_id == null)
            phone_id = InstanceID.getInstance(mContext).getId();

        return phone_id;
    }

    /*
    Returns the registration ID
     */
    public String getRegID() {
        SharedPreferences sharedPreferences =
                mContext.getSharedPreferences(GcmRegistrationAsyncTask.GCM_PREF_KEY, MODE_PRIVATE);

        return sharedPreferences.getString(GcmRegistrationAsyncTask.REG_ID_PREF_KEY, null);
    }

    /*
    Deletes an entry from the datastore
     */
    public void deleteEntry(String id) {
        Map<String, String> params = new HashMap<>();
        params.put(BookEntry.ID, id);
        params.put(BookEntry.REG_ID, getRegID());
        params.put(BookEntry.PHONE_ID, getInstanceId());
        String endpoint = GcmRegistrationAsyncTask.SERVER_ADDR + "/delete.do";
        new GCMThread(endpoint, params).start();

    }

    /*
    Adds an entry to the datastore
     */
    public void addEntry(BookEntry entry) {
        Map<String, String> params = new HashMap<>();
        params.put(BookEntry.REG_ID, getRegID());
        params.put(BookEntry.PHONE_ID, getInstanceId());

        entry.entryToMap(params);

        String endpoint = GcmRegistrationAsyncTask.SERVER_ADDR + "/add.do";
        new GCMThread(endpoint, params).start();
    }

    /*
    Updates an entry in the datastore
     */
    public void updateEntry(BookEntry entry) {
        Map<String, String> params = new HashMap<>();
        params.put(BookEntry.REG_ID, getRegID());
        params.put(BookEntry.PHONE_ID, getInstanceId());

        entry.entryToMap(params);

        String endpoint = GcmRegistrationAsyncTask.SERVER_ADDR + "/update.do";

        new GCMThread(endpoint, params).start();
    }

    /*
        Returns information for analytics
     */
    public void retrieveDatastore() {
        Map<String, String> params = new HashMap<>();
        params.put(BookEntry.REG_ID, getRegID());
        params.put(BookEntry.PHONE_ID, getInstanceId());

        String endpoint = GcmRegistrationAsyncTask.SERVER_ADDR + "/retrieve.do";

        new GCMThread(endpoint, params).start();
    }

    /*
    clearDatastore (can be helpful for debugging)
     */
    private void clearDatastore() {
        Map<String, String> params = new HashMap<>();
        params.put(BookEntry.REG_ID, getRegID());
        params.put(BookEntry.PHONE_ID, getInstanceId());

        String endpoint = GcmRegistrationAsyncTask.SERVER_ADDR + "/clear.do";

        new GCMThread(endpoint, params).start();
    }

    private class GCMThread extends Thread {
        private String endPoint;
        private Map<String, String> params;

        public GCMThread(String endPoint, Map<String, String> params) {
            this.endPoint = endPoint;
            this.params = params;

        }

        @Override
        public void run() {
            try {
                ServerUtilities.post(endPoint, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
