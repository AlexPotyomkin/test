package com.lihoy21gmail.test;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

public class Main extends FragmentActivity {
    static final int PICK_IMAGE_REQUEST = 1;
    static final int PICK_RINGTONE_REQUEST = 2;
    private StartFragment startFragment;
    private Notification_settings notification_settings;
    private FragmentTransaction ft;
    private Intent ServiceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startFragment = new StartFragment();
        notification_settings = new Notification_settings();
        ServiceIntent = new Intent(this, NotificationService.class);

        ft = getSupportFragmentManager().beginTransaction();
        if (getIntent().getIntExtra("Open_Notification_settings", 0) == 2)
            ft.add(R.id.fragment, notification_settings);
        else
            ft.add(R.id.fragment, startFragment);
        ft.commit();

    }

    public void change_fragment() {
        ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment, notification_settings)
                .addToBackStack(null)
                .commit();
    }

    public void change_image() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void change_sound() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                getResources().getString(R.string.pick_ringtone));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        startActivityForResult(intent, PICK_RINGTONE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bitmap galleryPic = null;
                    try {
                        galleryPic = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ImageView ivNotificationIco = (ImageView) findViewById(R.id.ivNotificationIco);
                    ivNotificationIco.setImageBitmap(galleryPic);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    assert galleryPic != null;
                    galleryPic.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    ServiceIntent.putExtra("image", byteArray);
                }
                break;
            case PICK_RINGTONE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri ChosenSoundUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (ChosenSoundUri == null)
                        break;
                    Cursor returnCursor =
                            getContentResolver().query(ChosenSoundUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    TextView tvChosenSound = (TextView) findViewById(R.id.tvChosenSound);
                    tvChosenSound.setText(returnCursor.getString(nameIndex));
                    ServiceIntent.setData(ChosenSoundUri);

                }
        }
    }

    public void myStartService() {
        EditText etNotification = (EditText) findViewById(R.id.etNotification);
        if (etNotification.getText().length() == 0)
            etNotification.setText("Notification text");
        ServiceIntent.putExtra("Notification_text", etNotification.getText().toString());

        EditText etPeriod = (EditText) findViewById(R.id.etPeriod);
        String Text_period = etPeriod.getText().toString();
        StringTokenizer stk = new StringTokenizer(Text_period, ":");
        int[] arPeriod = {0, 0, 30};
        if (stk.countTokens() == 3) {
            for (int i = 0; i < arPeriod.length; i++)
                arPeriod[i] = Integer.parseInt(stk.nextToken());
        } else
            etPeriod.setText("00:00:30");
        if ((arPeriod[0] + arPeriod[1] + arPeriod[2]) == 0) {
            arPeriod[0] = 0;
            arPeriod[1] = 0;
            arPeriod[2] = 30;
            etPeriod.setText("00:00:30");
        }
        ServiceIntent.putExtra("period", arPeriod);
        startService(ServiceIntent);
    }

    public void myStopService() {
        stopService(ServiceIntent);
    }
}