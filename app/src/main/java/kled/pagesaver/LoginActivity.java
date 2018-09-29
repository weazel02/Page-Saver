package kled.pagesaver;


import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.view.View;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


/**
 * A login screen that offers user option ot login with facebook
 */
public class LoginActivity extends AppCompatActivity {

    public final static String LOGIN_PREF_KEY = "kled_main_pref_key";
    public final static String FB_USER_ID_PREF_KEY = "facebook_user_id";
    public final static String LOGIN_SKIPPED = "login skipped";

    private CallbackManager callbackManager;
    private LoginButton loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!shouldPromptFbLogin()) {
            //If already logged in, then just send to MainActivity
            startMainActivity();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setPublishPermissions("publish_actions");


        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String id = loginResult.getAccessToken().getUserId();

                //store the user id
                SharedPreferences sharedPreferences =
                        getSharedPreferences(LOGIN_PREF_KEY, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Log.d("USER FACEBOOK ID", id);
                editor.clear();
                editor.putString(FB_USER_ID_PREF_KEY, id);
                editor.commit();

                Log.d("FACEBOOK", "SUCCESSFUL LOG WITH USER ID " + id);

                startMainActivity();
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK", "UNSUCCESSFUL LOG IN DUE TO CANCEL ");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("FACEBOOK", "UNSUCCESSFUL LOG IN DUE TO ERROR " + exception.toString());
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        finish();
    }


    //handle when user clicks skip
    public void onSkipButtonClick(View view) {
        //store the user id
        SharedPreferences sharedPreferences =
                getSharedPreferences(LOGIN_PREF_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putString(FB_USER_ID_PREF_KEY, LOGIN_SKIPPED);
        editor.commit();

        Log.d("FACEBOOK", "USER SKIPPED LOGIN ");

        finish();
    }

    //helper function that decides if we should show the login in page
    private boolean shouldPromptFbLogin() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(LoginActivity.LOGIN_PREF_KEY, MODE_PRIVATE);

        String id_string = sharedPreferences.getString(LoginActivity.FB_USER_ID_PREF_KEY, null);

        if(id_string == null || id_string.equals(LoginActivity.LOGIN_SKIPPED))
            return true;

        //only login if you don't have the user id
        return false;
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

