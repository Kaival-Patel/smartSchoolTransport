package com.example.kaival.smartschool;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Myservice extends Service {
    public Myservice() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onTaskRemoved(intent);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartservice=new Intent(getApplicationContext(),this.getClass());
        restartservice.setPackage(getPackageName());

        startService(restartservice);
        super.onTaskRemoved(rootIntent);

    }
}
