package com.example.rahulkurup.bgaccelerologger;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class AccelerometerService extends IntentService implements SensorEventListener {

    private static final String DEBUG_TAG = "AccelerometerSensorService";
    private static final String TAG = "AccelService";
    private static float[] history = new float[3];
    private static float[] gravity = new float[3];
    private static float[] linear_acceleration = new float[3];
    private static String[] direction = {"NONE", "NONE", "NONE"};
    private static String accelTimeStamp;
    private static float accX, accY, accZ;
    String accStr;

    File backgroundAccDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/CapturedBackgroundData/");
    File accText = new File(backgroundAccDir, "Background Accelerometer Reading.txt");

    BufferedWriter accTout;

    private SensorManager sensorManager = null;
    private Sensor sensor = null;
    int i = 0;

    @Override
    protected void onHandleIntent(Intent arg0) {
        //

    }

    public AccelerometerService() {
        super("test-service");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Start Foreground Intent ");
            Intent notificationIntent = new Intent(this, AccelerometerService.class);
            notificationIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
            Intent playIntent = new Intent(this, AccelerometerService.class);
            playIntent.setAction(Constants.ACTION.PLAY_ACTION);
            PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.abc_popup_background_mtrl_mult);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("BackGround Accelerometer Logger")
                    .setTicker("This service logs accelerometer data")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_play, "Start",
                            pplayIntent)
                    .build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
            new SensorEventLoggerTask().execute();
            Log.i(TAG, "Service running");
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            stopForeground(true);



            stopSelf();
            Log.i(TAG, "Stopped");
        }


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        //mention stackoverflowsource

        float[] values = event.values;
        accX = values[0];
        accY = values[1];
        accZ = values[2];

        final float alpha = (float) 0.8;

       // ACCOUNT FOR GRAVITY?



        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];


        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];


        float xChange = history[0] - linear_acceleration[0];
        float yChange = history[1] - linear_acceleration[1];
        float zChange = history[2] - linear_acceleration[2];

        history[0] = linear_acceleration[0];
        history[1] = linear_acceleration[1];
        history[2] = linear_acceleration[2];

        if (xChange > 2) {
            direction[0] = "LEFT";
        } else if (xChange < -2) {
            direction[0] = "RIGHT";
        }

        if (yChange > 2) {
            direction[1] = "DOWN";
        } else if (yChange < -2) {
            direction[1] = "UP";
        }

        if (zChange > 2) {
            direction[2] = "RISING";
        } else if (zChange < -2) {
            direction[2] = "DESCENDING";
        }

        new SensorEventLoggerTask().execute(event);

        Log.i(TAG, "Sensed");


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class SensorEventLoggerTask extends
            AsyncTask<SensorEvent, Void, Void> {

        @Override
        protected Void doInBackground(SensorEvent... params) {
            {
                Log.i(TAG, "Writing");
                //File backgroundAccDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/CapturedBackgroundData/");
                Log.i("File Operation", "STARTED");
                if (!backgroundAccDir.exists()) {
                    backgroundAccDir.mkdirs();
                }
                File accText = new File(backgroundAccDir, "Background Accelerometer Reading.txt");
                if (!accText.exists()) {

                    try {
                        accText.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                        accTout = new BufferedWriter(new FileWriter(accText, true));

                        String accStr = "Accelerometer Direction X:" + direction[0] + " Y:" + direction[1] + " Movement:"+ direction[2] + "\nAcceleration :" + (accX * accX + accY * accY + accZ * accZ)
                                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH) + "units \n";
                        Log.i(TAG, "Appended: " + accStr);
                        accTout.append(accStr);


                        accTout.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
                return null;
            }
        }



    @Override
    public void onDestroy() {

        Log.i(TAG, "Stopped");
        try {
            sensorManager.unregisterListener(this);
       /* stopService(new Intent(this, AccelerometerService.class));
        super.onDestroy();
       */
        }catch(Exception e)
        {  e.printStackTrace();}

    }
}





