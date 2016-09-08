package com.meetingplay;

import android.app.Application;

/**
 * Created by Douglas on 12/12/15.
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.PUSH_MESSAGES) {

//            Parse.initialize(this, getString(R.string.parse_1), getString(R.string.parse_2));
//            ParseInstallation.getCurrentInstallation().saveInBackground();
        }
    }
}
