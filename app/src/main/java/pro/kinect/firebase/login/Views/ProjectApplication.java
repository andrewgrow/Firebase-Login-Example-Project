package pro.kinect.firebase.login.Views;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by http://kinect.pro on 22.09.16.
 * Developer Andrew.Gahov@gmail.com
 */

public class ProjectApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the FacebookSDK before executing any other operations.
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}
