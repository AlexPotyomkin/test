package com.lihoy21gmail.test;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class NotificationService extends Service {
    public NotificationService() {
    }
   public static final int NOTIFY_ID = 101;
    NotificationManager nm;
    private Timer myTimer = new Timer();

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int arPeriod[] = intent.getIntArrayExtra("period");
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendNotification(intent);
            }
        }, 0L, arPeriod[0] * 3600 * 1000    // количество часов переведенных в милисекунды
             + arPeriod[1] * 60 * 1000      // количество минут переведенных в милисекунды
             +arPeriod[2] * 1000);           // количество секунд переведенных в милисекунды
        return super.onStartCommand(intent, flags, startId);
    }

    void sendNotification(Intent intent) {
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, Main.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.putExtra("Open_Notification_settings", 2);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Resources res = context.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if(intent.getByteArrayExtra("image") != null) {
            byte[] byteArray = intent.getByteArrayExtra("image");
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            builder.setLargeIcon(bmp);
        }
        else
            builder.setLargeIcon(BitmapFactory.decodeResource(res,R.drawable.img1));

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(res.getString(R.string.warning))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(res.getString(R.string.notification_title))
                .setContentText(intent.getStringExtra("Notification_text"));
        if(intent.getData()!= null)
            builder.setSound(intent.getData());
        else
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        myTimer.cancel();
        myTimer = null;
        super.onDestroy();
    }
}