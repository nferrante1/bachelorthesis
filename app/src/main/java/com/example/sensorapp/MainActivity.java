package com.example.sensorapp;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    final int REQUEST_CODE_ASK_PERMISSIONS = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();

    }


    private boolean isMyServiceRunning(Class sensorService) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (sensorService.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    public void startButton(View v)
    {
        if (!isMyServiceRunning(SensorService.class)){
            startService(new Intent(this,SensorService.class));
        } else {
            Toast.makeText(getBaseContext(), "Registrazione già in corso", Toast.LENGTH_LONG).show();
        }

    }

    public void stopButton(View v) {
        if (isMyServiceRunning(SensorService.class)){
            SensorService.keepRunning = false;
            stopService(new Intent(this,SensorService.class));
        } else {
            Toast.makeText(getBaseContext(), "Registrazione già conclusa", Toast.LENGTH_LONG).show();
        }
    }

    private void checkPermissions() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        Toast.makeText(getBaseContext(), "Permission is already granted", Toast.LENGTH_LONG).show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Toast.makeText(getBaseContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "WRITE_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
