package kled.pagesaver;

import android.app.Activity;
import android.app.IntentService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by eloisedietz on 2/19/17.
 * This intent service allows the backend to communicate with the app
 */

public class GcmIntentService extends IntentService {
    int id;
    private Handler updateHandler;
    private final MessageBinder messageBinder = new MessageBinder();
    public static final int MSG_INT_VALUE = 0;
    private String messageToSend;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());

               //SHow message from server
                String mess = extras.getString("message");
                Log.d("RECEIVED MESSAGE ", mess);

                String[] messageParts = mess.split(" ");

                if(messageParts[0].equals("timePages")) {
                    //showToast("Comparing now");

                    messageToSend = mess;

                    // Send message to CompareAnalyticsActivity to retrieve info string
                    if (updateHandler != null) {
                        Message message = updateHandler.obtainMessage();
                        message.what = MSG_INT_VALUE;
                        updateHandler.sendMessage(message);
                        Log.d("hello", mess);
                    }

                } else {
                    showToast(mess);
                }


            }
        }
        GcmReceiver.completeWakefulIntent(intent);
    }

    protected void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Retrieve message
     * @return
     */
    public String getMessage() {
        return messageToSend;
    }

    /**
     * Allows access to service from other activities
     */
    public class MessageBinder extends Binder {

        public void getUIMsgHandler(Handler msgHandler) {
            updateHandler = msgHandler;
            Log.d("hello", updateHandler.toString());
        }

        GcmIntentService getService() {
            return GcmIntentService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        updateHandler = null;
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messageBinder;
    }
}


