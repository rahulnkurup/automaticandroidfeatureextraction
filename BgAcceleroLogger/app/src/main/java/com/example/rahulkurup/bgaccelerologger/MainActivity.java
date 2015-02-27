package com.example.rahulkurup.bgaccelerologger;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    ToggleButton button;

    static Button exitButton;

    TextView textview;

    static boolean appFound = false;

    final GameCheckTask gameChecker = new GameCheckTask();

    static String backgroundRunningGame= "com.game.accballlite"; //insert any game package that runs in background

    //com.game.accballlite

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = (TextView) findViewById(R.id.textView);

        button = (ToggleButton) findViewById(R.id.toggleButton);


        exitButton = (Button) findViewById(R.id.button);

        gameChecker.execute();
        Log.i("Game Checker ", "Launched");


        exitButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Log.i("MESSAGE:", "Activity Stopped");
                Intent altStopIntent = new Intent(MainActivity.this, AccelerometerService.class);
                altStopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                stopService(new Intent(getApplicationContext(), AccelerometerService.class));
                gameChecker.cancel(true);
                Log.i("Game Checker ", "Stopped");
                finish();


            }
        });


    }

    public void onToggleClicked(View view) {





        boolean on = ((ToggleButton) view).isChecked();

        if (on) {

            textview.setText(getResources().getString(R.string.Stop));


            Intent accelerometerIntent = new Intent(getApplicationContext(), AccelerometerService.class);
            accelerometerIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            startService(accelerometerIntent);

        } else {
            Intent stopIntent = new Intent(MainActivity.this, AccelerometerService.class);
            stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            startService(stopIntent);
            textview.setText(getResources().getString(R.string.start_accel_logging));
            Log.i("App Status", "Stopped");

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
        return;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            this.moveTaskToBack(true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME && event.getRepeatCount() == 0) {
            this.moveTaskToBack(true);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private class GameCheckTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);

            List<ActivityManager.RunningAppProcessInfo> processInfo = am.getRunningAppProcesses();


            Iterator i = processInfo.iterator();
            while(i.hasNext()) {
                ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
                try {
                   if(info.processName.equalsIgnoreCase(backgroundRunningGame))
                   {
                       Log.i(backgroundRunningGame, "found");
                       textview.append(backgroundRunningGame+" found");
                   }
                    else
                   {
                       Log.i("App",info.processName);
                   }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }


            return null;
        }
    }


}
