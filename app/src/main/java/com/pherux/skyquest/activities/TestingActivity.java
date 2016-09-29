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
    static final int TAKE_PICTURE_REQUEST = 1;

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
// TODO: 29/9/2016 clean comments 
//        photoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                launchCamera(false);
//            }
//        });

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
        // TODO: 29/9/2016 clean comments
        Intent name = new Intent(this, CameraActivity.class);
//        Bundle b = new Bundle();
//        b.putBoolean("highRes", true);
//        name.putExtras(b);
//        startActivityForResult(name, TAKE_PICTURE_REQUEST);
        startActivity(name);
    }

    // TODO: 29/9/2016 clean comments 
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == TAKE_PICTURE_REQUEST){
//            Log.d("TESTACTIVITY", "REQ CODE");
//            if(resultCode == RESULT_OK){
//                Log.d("TESTACTIVITY", "RES OK");
//                Intent name = new Intent(this, CameraActivity.class);
//                Bundle b = new Bundle();
//                b.putBoolean("highRes", false);
//                name.putExtras(b);
//                startActivity(name);
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
