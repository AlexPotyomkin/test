package com.lihoy21gmail.test;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

public class Main extends FragmentActivity {
    static final int PICK_IMAGE_REQUEST = 1;
    static final int PICK_RINGTONE_REQUEST = 2;
    private static StartFragment startFragment;
    private static Notification_settings notification_settings;
    private FragmentTransaction ft;
    private Intent ServiceIntent;
    SharedPreferences sPref;
    private Uri ChosenSoundUri = null;
    private Bitmap InnerBitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startFragment = new StartFragment();
        notification_settings = new Notification_settings();
        ServiceIntent = new Intent(this, NotificationService.class);
        ft = getSupportFragmentManager().beginTransaction();
        if (getIntent().getIntExtra("Open_Notification_settings", 0) == 2) {
            ft.add(R.id.fragment, notification_settings);
        } else
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
        Intent intent = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void change_sound() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                getResources().getString(R.string.pick_ringtone));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        startActivityForResult(intent, PICK_RINGTONE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (getContentResolver().getType(data.getData()) != null) {
                        Bitmap galleryPic = null;
                        try {
                            galleryPic = MediaStore.Images.Media.getBitmap(getContentResolver(),
                                    data.getData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ImageView ivNotificationIco = (ImageView) findViewById(R.id.ivNotificationIco);
                        galleryPic = scaleDownBitmap(galleryPic, 64, this);
                        ivNotificationIco.setImageBitmap(galleryPic);
                        InnerBitmap = galleryPic;
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.error_type), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case PICK_RINGTONE_REQUEST:
                if (resultCode == RESULT_OK) {
                    ChosenSoundUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (ChosenSoundUri == null)
                        break;
                    Cursor returnCursor =
                            getContentResolver().query(ChosenSoundUri, null, null, null, null);
                    assert returnCursor != null;
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    TextView tvChosenSound = (TextView) findViewById(R.id.tvChosenSound);
                    try {
                        tvChosenSound.setText(returnCursor.getString(nameIndex));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    returnCursor.close();
                }
        }
    }

    public void StartService() {
        EditText etNotification = (EditText) findViewById(R.id.etNotification);
        if (etNotification.getText().length() == 0)
            etNotification.setText(getResources().getString(R.string.default_notification_text));
        ServiceIntent.putExtra("Notification_text", etNotification.getText().toString());

        EditText etPeriod = (EditText) findViewById(R.id.etPeriod);
        String Text_period = etPeriod.getText().toString();
        StringTokenizer stk = new StringTokenizer(Text_period, ":");
        int[] arPeriod = {0, 0, 5};
        if (stk.countTokens() == 3) {
            for (int i = 0; i < arPeriod.length; i++)
                arPeriod[i] = Integer.parseInt(stk.nextToken());
        } else
            etPeriod.setText("00:00:05");
        if ((arPeriod[0] + arPeriod[1] + arPeriod[2]) == 0) {
            arPeriod[2] = 5;
            etPeriod.setText("00:00:05");
        }

        ServiceIntent.setData(ChosenSoundUri);

        if (InnerBitmap != null) {
            Bitmap galleryPic = InnerBitmap;
            ImageView ivNotificationIco = (ImageView) findViewById(R.id.ivNotificationIco);
            galleryPic = scaleDownBitmap(galleryPic, 64, this);
            ivNotificationIco.setImageBitmap(galleryPic);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            assert galleryPic != null;
            galleryPic.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            //encodedImage = "some text";
            ServiceIntent.putExtra("image", encodedImage);
        }
        ServiceIntent.putExtra("period", arPeriod);
        startService(ServiceIntent);
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void StopService() {
        stopService(ServiceIntent);
    }

    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;
        int h, w;
        if (photo.getHeight() >= photo.getWidth()) {
            h = (int) (newHeight * densityMultiplier);
            w = (int) (h * photo.getWidth() / ((double) photo.getHeight()));
            photo = Bitmap.createScaledBitmap(photo, w, h, true);
        } else {
            w = (int) (newHeight * densityMultiplier);
            h = (int) (w * photo.getHeight() / ((double) photo.getWidth()));
            photo = Bitmap.createScaledBitmap(photo, w, h, true);
            Matrix m = new Matrix();
            m.preRotate(90, photo.getWidth() / 2, photo.getHeight() / 2);
            photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), m, true);
        }
        return photo;
    }

    public void recover_start_fragment() {
        if (InnerBitmap != null) {
            Bitmap galleryPic = InnerBitmap;
            ImageView ivStartIco = (ImageView) findViewById(R.id.ivStartIco);
            galleryPic = scaleDownBitmap(galleryPic, 96, this);
            ivStartIco.setImageBitmap(galleryPic);
        }
    }

    public void recover_notification_fragment() {
        Bitmap galleryPic = InnerBitmap;
        if (galleryPic != null) {
            ImageView ivStartIco = (ImageView) findViewById(R.id.ivNotificationIco);
            galleryPic = scaleDownBitmap(galleryPic, 64, this);
            ivStartIco.setImageBitmap(galleryPic);
        }
        if (ChosenSoundUri != null) {
            Cursor returnCursor =
                    getContentResolver().query(ChosenSoundUri, null, null, null, null);
            assert returnCursor != null;
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            TextView tvChosenSound = (TextView) findViewById(R.id.tvChosenSound);
            try {
                tvChosenSound.setText(returnCursor.getString(nameIndex));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void LoadState() {
        sPref = getPreferences(MODE_PRIVATE);

        // Востановление периода
        String Text_period = sPref.getString("Period_text", "");
        EditText etPeriod = (EditText) findViewById(R.id.etPeriod);
        etPeriod.setText(Text_period);

        // Востановление текста уведомления
        String Text_Notification = sPref.getString("Notification_Text", "");
        EditText etNotification = (EditText) findViewById(R.id.etNotification);
        etNotification.setText(Text_Notification);

        // Востановление изображения
        String previouslyEncodedImage = sPref.getString("image_data", "");
        if (!previouslyEncodedImage.equalsIgnoreCase("")) {
            byte[] b = Base64.decode(previouslyEncodedImage, Base64.DEFAULT);
            InnerBitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        }

        // Востановление рингтона
        String strSoundUri = sPref.getString("ChosenSoundUri", "");
        if (strSoundUri.length() != 0)
            ChosenSoundUri = Uri.parse(strSoundUri);
    }

    public void SaveState() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();

        // Сохранения периода
        EditText etPeriod = (EditText) findViewById(R.id.etPeriod);
        String Text_period = etPeriod.getText().toString();
        ed.putString("Period_text", Text_period);

        // Сохранение текста уведомления
        EditText etNotification = (EditText) findViewById(R.id.etNotification);
        String Text_Notification = etNotification.getText().toString();
        ed.putString("Notification_Text", Text_Notification);

        // Сохранение изображения
        if (InnerBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InnerBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            ed.putString("image_data", encodedImage);
        }

        // Сохранение рингтона
        if (ChosenSoundUri != null)
            ed.putString("ChosenSoundUri", ChosenSoundUri.toString());
        ed.apply();
    }
}