package com.pherux.skyquest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.gc.materialdesign.views.ButtonRectangle;
import com.pherux.skyquest.R;
import com.pherux.skyquest.managers.Persistence;
import com.pherux.skyquest.utils.Tracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Fernando Valdez on 8/18/15
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PictureCallback {

    private static final String TAG = CameraActivity.class.getName();
    final Activity me = this;
    Camera cam = null;
    SurfaceView surface = null;
    SurfaceHolder holder = null;
    PowerManager.WakeLock wakeLock = null;
//    boolean highRes = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "CameraActivity onCreate");

        //// TODO: 29/9/2016 clean comments
//        Bundle b = getIntent().getExtras();
//        if(b != null)
//            highRes = b.getBoolean("highRes");
//        Log.d("RESOLUTION", ": " + highRes);

        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.d(TAG, "CameraActivity onUncaughtException");
                Tracker.logException(ex);
                Tracker.pingError();
                me.finish();
            }
        });

        setContentView(R.layout.camera_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ButtonRectangle cancel = (ButtonRectangle) findViewById(R.id.photo_cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                me.finish();
            }
        });

        surface = (SurfaceView) findViewById(R.id.photo_surface);
        holder = surface.getHolder();
        holder.addCallback(this);
        cam = Camera.open();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.FULL_WAKE_LOCK, "SkyQuestTrackerCameraActivityLock");
        wakeLock.acquire();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        cam.stopPreview();
    }

    @Override
    protected void onStop() {
        Log.d("SkyQuest", "CameraActivity onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("SkyQuest", "CameraActivity onDestroy");
        wakeLock.release();
        if (cam != null) {
            cam.release();
        }
        super.onDestroy();
    }

    //scaleDown method to scale down resolution of image to meet requirements
    //http://stackoverflow.com/questions/19264834/resize-image-to-fit-screen-size-and-resolution-in-android
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d("SkyQuest", "PhotoActivity onPictureTaken");
        File pictureFile = getOutputMediaFile(true);
        File lowResFile = getOutputMediaFile(false);

        if (pictureFile == null) {
            Log.d("SkyQuest", "Error creating media file, check storage permissions: ");
            return;
        }

        try {
            //1. convert original image byte array into BM
            Bitmap origBM = BitmapFactory.decodeByteArray(data, 0, data.length);

            //2. Scale down BM to required res
            Bitmap scaledBM = scaleDown(origBM, 352, false);

            //3. Convert scaled down BM back to byte array
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaledBM.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] scaledData = stream.toByteArray();

            //4. Save both images in their respective folders
            FileOutputStream out = new FileOutputStream(pictureFile);
            out.write(data);
            out.flush();
            out.close();

            FileOutputStream outLowRes = new FileOutputStream(lowResFile);
            outLowRes.write(scaledData);
            outLowRes.flush();
            outLowRes.close();
        } catch (FileNotFoundException e) {
            Log.d("SkyQuest", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("SkyQuest", "Error accessing file: " + e.getMessage());
        }
        //cam.stopPreview();
        //cam.release();

        // Let the android gallery know that it can show this file.
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(pictureFile)));
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(lowResFile)));

        Tracker.pingSuccess();

        Integer iteration = Persistence.getIntVal(Tracker.photoCountKey, 0);
        String photoStatus = "Photo " + new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()) + " Number: " + iteration.toString();
        Persistence.putStringVal(Tracker.photoStatusKey, photoStatus);

        if (getParent() == null) {
            setResult(me.RESULT_OK);
        }
        else {
            getParent().setResult(me.RESULT_OK);
        }

        pleaseWait(5);
        me.finish();
    }


    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("SkyQuest", "PhotoActivity surfaceCreated");
        //// TODO: 29/9/2016 clean comments
//        boolean lowRes = false;
        try {
            setUpCameraParameter();
            cam.setPreviewDisplay(holder);
            cam.startPreview();
            takePicture();

//            lowRes = true;
//            setUpCameraParameter(lowRes);
//            takePicture();
        } catch (IOException e) {
            Log.d("SkyQuest", "Error setting camera preview: " + e.getMessage());
            me.finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void setUpCameraParameter() {

        try {
            Camera.Parameters params = cam.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            List<String> sceneModes = params.getSupportedSceneModes();
            if (sceneModes != null) {
                for (String sceneMode : sceneModes) {
                    if (sceneMode.equals(Camera.Parameters.SCENE_MODE_ACTION)) {
                        params.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
                    } else if (sceneMode.equals(Camera.Parameters.SCENE_MODE_SPORTS)) {
                        params.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
                    }
                }
            }
            params.setJpegQuality(100);
            params.setPictureFormat(ImageFormat.JPEG);
            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            params.setColorEffect(Camera.Parameters.EFFECT_NONE);
            params.setAutoExposureLock(false);
            params.setAutoWhiteBalanceLock(false);

            Camera.Size newSize = params.getPictureSize();
            //// TODO: 29/9/2016 clean comments
//            if(highRes){
                for (Camera.Size size : params.getSupportedPictureSizes()) {
//                    Log.d("size", "w: " + size.width + "h: " + size.height);
                    if ((size.width * size.height) > (newSize.width * newSize.height)) {
                        newSize = size;
                    }
                }
//            }else{
//                newSize.width = 352;
//                newSize.height = 288;
//            }

            params.setPictureSize(newSize.width, newSize.height);
            cam.setParameters(params);

            try {
                params = cam.getParameters();
                Location location = Tracker.getLocation();
                Log.d("SkyQuest", "EXIF GPS Altitude = " + Double.toString(location.getAltitude()));
                Log.d("SkyQuest", "EXIF GPS Latitude = " + Double.toString(location.getLatitude()));
                Log.d("SkyQuest", "EXIF GPS Longitude = " + Double.toString(location.getLongitude()));
                Log.d("SkyQuest", "EXIF GPS Time = " + Double.toString(location.getTime()));
                params.setGpsAltitude(location.getAltitude());
                params.setGpsLatitude(location.getLatitude());
                params.setGpsLongitude(location.getLongitude());
                params.setGpsTimestamp(location.getTime());
                cam.setParameters(params);
                Log.d("SkyQuest", "EXIF location saved");
            } catch (Throwable ex) {
                Log.d("SkyQuest", "Error saving EXIF location");
            }

        } catch (Throwable ex) {
            Log.d("SkyQuest", "Error setting camera parameters");
        }
    }

    private void takePicture() {
        pleaseWait(2);
        cam.takePicture(null, null, this);
    }

    private void pleaseWait(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private File getOutputMediaFile(boolean highRes) {
        File mediaStorageDir;

        if(highRes){
            File temp = new File(Tracker.getStorageRoot(), "Photos");
            mediaStorageDir = temp;
        }else{
            File temp = new File(Tracker.getStorageRoot(), "LowResPhotos");
            mediaStorageDir = temp;
        }


        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("SkyQuest", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        Integer iteration = Tracker.incrementIntVal(Tracker.photoCountKey);
        String fileName = Persistence.getStringVal(Tracker.photoPrefixKey, "SkyQuest_") + iteration.toString() + ".jpg";

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
        Log.d("SkyQuest", "Saved file to " + mediaFile);

        return mediaFile;
    }

}
