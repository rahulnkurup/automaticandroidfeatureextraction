package com.example.rahulkurup.monkeyrunner;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice


                device = MonkeyRunner.waitForConnection()

        # Installs the Android package. Notice that this method returns a boolean, so you can test
        # to see if the installation worked.
                device.installPackage('myproject/bin/MyApplication.apk')

        # sets a variable with the package's internal name
        package = 'com.example.android.myapplication'

        # sets a variable with the name of an Activity in the package
        activity = 'com.example.android.myapplication.MainActivity'

        # sets the name of the component to start
        runComponent = package + '/' + activity

        # Runs the component
        device.startActivity(component=runComponent)

        # Presses the Menu button
        device.press('KEYCODE_MENU', MonkeyDevice.DOWN_AND_UP)

        # Takes a screenshot
                result = device.takeSnapshot()

        # Writes the screenshot to a file
        result.writeToFile('myproject/shot1.png','png')
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
