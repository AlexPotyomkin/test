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
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    public static final int NOTIFY_ID = 1;
    NotificationManager nm;
    private Timer myTimer = new Timer();
    private static int numMessages = 0;
    private int arPeriod[];

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        arPeriod = new int[3];
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {
        arPeriod = intent.getIntArrayExtra("period");
        if (arPeriod != null) {
            arPeriod = intent.getIntArrayExtra("period");
            myTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendNotification(intent);
                }
            }, 0L, arPeriod[0] * 3600 * 1000    // количество часов переведенных в милисекунды
                    + arPeriod[1] * 60 * 1000   // количество минут переведенных в милисекунды
                    + arPeriod[2] * 1000);      // количество секунд переведенных в милисекунды
        }
        return START_REDELIVER_INTENT;
    }

    void sendNotification(Intent intent) {
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, Main.class);
        notificationIntent.putExtra("Open_Notification_settings", 2);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Resources res = context.getResources();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        long[] vibrate = new long[]{10, 250, 300, 100, 500, 100, 500, 250, 300, 100, 500, 100};

        if (Build.VERSION.SDK_INT >= 11) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            if (intent.getStringExtra("image") != null) {
                String previouslyEncodedImage = intent.getStringExtra("image");
                byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                builder.setLargeIcon(bmp);
            } else {
                builder.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.img1));
            }
            builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker(res.getString(R.string.warning))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(res.getString(R.string.notification_title))
                    .setContentText(intent.getStringExtra("Notification_text"))
                    .setContentIntent(PendingIntent.getActivity(context,
                            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            if (intent.getData() != null)
                builder.setSound(intent.getData());
            else
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            builder.setVibrate(vibrate);
            notificationManager.notify(NOTIFY_ID + numMessages++, builder.build());
        } else {
            int icon = R.mipmap.ic_launcher;
            long when = System.currentTimeMillis();
            DateFormat df = DateFormat.getTimeInstance();
            String strTime = df.format(when);
            Notification notification = new Notification(icon, getString(R.string.warning), when);
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
            if (intent.getStringExtra("image") != null) {
                String previouslyEncodedImage = intent.getStringExtra("image");
                byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                contentView.setImageViewBitmap(R.id.notification_image, bmp);
            } else {
                contentView.setImageViewResource(R.id.notification_image, R.drawable.img1);
            }
            contentView.setTextViewText(R.id.notification_title, getString(R.string.notification_title));
            contentView.setTextViewText(R.id.notification_text, intent.getStringExtra("Notification_text"));
            contentView.setTextViewText(R.id.notification_time, strTime);
            notification.contentView = contentView;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (intent.getData() != null)
                notification.sound = intent.getData();
            else
                notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notification.vibrate = vibrate;
            notificationManager.notify(NOTIFY_ID + numMessages++, notification);
        }
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