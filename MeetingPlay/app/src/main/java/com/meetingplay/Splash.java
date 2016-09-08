package com.meetingplay;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

/**
 * Created by KSB1 on 3/13/2016.
 */
public class Splash extends Activity {
    final Context context = this;
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    private BluetoothAdapter mBluetoothAdapter;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Create an Intent that will start the Menu-Activity.
                Intent mainIntent = new Intent(Splash.this, Login.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
