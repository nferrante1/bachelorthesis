package com.example.sensorapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class SensorService extends Service implements SensorEventListener {
    public static Boolean keepRunning = true;
    Date startTime;
    List<Sensor> deviceSensors;
    ThreadPoolExecutor stpe;
    private SensorManager sensorManager;
    NotificationCompat.Builder mBuilder;
    private final static int notificationId = 33;

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SensorService";
            String description = "SensorService";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(description, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void createNotification() {
        mBuilder = new NotificationCompat.Builder(this, "SensorService")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("SensorService")
                .setContentText("SensorService is running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }



    private class Recorder implements Runnable{
        float[] values;
        Sensor sensor;

        public Recorder (float[] fl, Sensor s){
            values = fl;
            sensor = s;
        }


        public void run (){
            registerData();
        }

        public void registerData() {
            String entry = sensor.getName() + ",";
            Date d = new Date();
            entry += d.toString()+ ",";
            for(float f:values){
                entry += Float.toString(f) + ",";
            }
            entry+="\n";

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/SensorApp");
        Boolean dirsMade = dir.mkdir();
        Log.d("dir", dirsMade.toString());
        File file = new File(dir, sensor.getStringType()+".csv");
        Log.d("path", file.getPath());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
                out.write(entry.getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Log.i("reg", entry);
            //System.out.println(entry);
        }
    }

    @Override
    public void onCreate() {
        startTime = new Date();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        stpe = (ThreadPoolExecutor) Executors.newScheduledThreadPool(10);
        createNotification();
        createNotificationChannel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Exit, onDestroy");
        Toast.makeText(getBaseContext(), "End recording", Toast.LENGTH_LONG).show();
        for (Sensor s: deviceSensors) {
            sensorManager.unregisterListener(this, s);

        }
     //   sensorManager.unregisterListener(this, accelerometer);
       // sensorManager.unregisterListener(this, gyroscope);
        if (keepRunning) {
                System.out.println("EXIT! restarting!");
                Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
                sendBroadcast(broadcastIntent);

            }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        Toast.makeText(getBaseContext(), "Start recording...", Toast.LENGTH_LONG).show();
        for (Sensor s: deviceSensors) {
            sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }
    startForeground(notificationId, mBuilder.build());
        return START_STICKY;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        final float[] values = event.values;
        int id = event.sensor.getType();
        Recorder r = new Recorder(values, event.sensor);
        stpe.execute(r);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



