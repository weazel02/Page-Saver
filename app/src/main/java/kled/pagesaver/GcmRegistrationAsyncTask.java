package kled.pagesaver;

/**
 * Created by eloisedietz on 2/24/17.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.eloisedietz.myapplication.backend.registration.Registration;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static android.content.Context.MODE_PRIVATE;

/*
A class to communicate with Datasstore
 */
class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
    private static Registration regService = null;
    private GoogleCloudMessaging gcm;
    private Context context;
    public final static String SERVER_ADDR = "https://neural-tangent-160718.appspot.com/";
    //public static String SERVER_ADDR = "http://10.31.186.147:8080";
    // Google Developers Console project number
    private static final String SENDER_ID = "989220098413";

    public final static String GCM_PREF_KEY = "gcm_pref_key";
    public final static String REG_ID_PREF_KEY = "registration_id";

    public GcmRegistrationAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        //This is used for debugging on a local datastore
        /*if (regService == null) {

            Registration.Builder builder = new Registration
                    .Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)

                    .setRootUrl(SERVER_ADDR+"/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
        */

        if (regService == null) {
            Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                    // otherwise they can be skipped
                    .setRootUrl(SERVER_ADDR + "/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            /*Registration.Builder builder = new Registration
            .Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl(SERVER_ADDR+"/_ah/api/");*/
            // end of optional local run code

            regService = builder.build();
        }

        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            String regId = gcm.register(SENDER_ID);
            msg = "Device registered, registration ID=" + regId;

            setRegID(regId);

            regService.register(regId).execute();

        } catch (IOException ex) {
            ex.printStackTrace();
            msg = "Error: " + ex.getMessage();
        }
        return msg;
    }

    @Override
    protected void onPostExecute(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        Logger.getLogger("REGISTRATION").log(Level.INFO, msg);
    }

    private void setRegID(String regID) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(GCM_PREF_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("REGISTRATION ID", regID);
        editor.clear();
        editor.putString(REG_ID_PREF_KEY, regID);
        editor.commit();
    }

    public String getRegID() {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(GCM_PREF_KEY, MODE_PRIVATE);

        return sharedPreferences.getString(GCM_PREF_KEY, null);
    }
}
