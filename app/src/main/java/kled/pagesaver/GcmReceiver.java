package kled.pagesaver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by eloisedietz on 2/19/17.
 */

public class GcmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("GCM RECEIVER", "received message");

        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        //GcmIntentService will handle the received message
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}