package com.pherux.skyquest.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gc.materialdesign.views.ButtonRectangle;
import com.pherux.skyquest.R;
import com.pherux.skyquest.utils.Tracker;

/**
 * Created by Fernando Valdez on 8/23/15
 */
public class TestingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        ButtonRectangle photoButton = (ButtonRectangle) findViewById(R.id.activity_testing_take_photo);
        ButtonRectangle sendLocationWebButton = (ButtonRectangle) findViewById(R.id.activity_testing_location_web);
        ButtonRectangle sendLocationSMSButton = (ButtonRectangle) findViewById(R.id.activity_testing_location_sms);
        ButtonRectangle rebootButton = (ButtonRectangle) findViewById(R.id.activity_testing_reboot);

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        sendLocationWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.sendTrackerPing();
            }
        });

        sendLocationSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.sendLocationSMS();
            }
        });

        rebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.reboot();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void launchCamera() {
        Intent name = new Intent(this, CameraActivity.class);
        startActivity(name);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
