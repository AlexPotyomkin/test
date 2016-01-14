package com.lihoy21gmail.test;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

public class Notification_settings extends myFragments implements Observer {
    private static final String TAG = "myLogs";
    static final int PICK_IMAGE_REQUEST = 1;
    static final int PICK_RINGTONE_REQUEST = 2;
    private Model mModel;
    private Intent ServiceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceIntent = new Intent(getContext(), NotificationService.class);
    }

    @Override
    public void onAttach(Context context) {
        mModel = Model.getInstance();
        Log.d(TAG, "onAttach: addObserver");
        mModel.addObserver(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.notification_settings, null);
        Button btChooseImage = (Button) v.findViewById(R.id.btChooseImage);
        Button btChooseSound = (Button) v.findViewById(R.id.btChooseSound);

        // Обработчик кнопки выбора иконки уведомления
        btChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        // Обработчик кнопки выбора мелодии уведомления
        btChooseSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                        getResources().getString(R.string.pick_ringtone));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                        RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                startActivityForResult(intent, PICK_RINGTONE_REQUEST);
            }
        });

        if (Build.VERSION.SDK_INT >= 14) {
            Switch swtch = (Switch) v.findViewById(R.id.swtch);
            if (isMyServiceRunning(NotificationService.class))
                swtch.setChecked(true);
            swtch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        StartService();
                    else
                        StopService();
                }
            });
        } else {
            ToggleButton TB = (ToggleButton) v.findViewById(R.id.toggleButton);
            if (isMyServiceRunning(NotificationService.class))
                TB.setChecked(true);
            TB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        StartService();
                    else
                        StopService();
                }
            });
        }

        // Получение названия рингтона установленного системой по умолчанию
        TextView tvChosenSound = (TextView) v.findViewById(R.id.tvChosenSound);
        Uri ChosenSoundUri = RingtoneManager.getActualDefaultRingtoneUri(
                getActivity().getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        Cursor returnCursor = getActivity().getContentResolver().
                        query(ChosenSoundUri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        try {
            tvChosenSound.setText(returnCursor.getString(nameIndex));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        returnCursor.close();
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_REQUEST:
                    if (getActivity().getContentResolver().getType(data.getData()) != null) {
                        Bitmap galleryPic;
                        try {
                            galleryPic = MediaStore.Images.Media.getBitmap( getActivity().
                                    getContentResolver(), data.getData());
                            mModel.setInnerBitmap(galleryPic);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(getContext().getApplicationContext(),
                                getResources().getString(R.string.error_type),
                                Toast.LENGTH_SHORT).show();
                    break;
                case PICK_RINGTONE_REQUEST:
                    Uri ChosenSoundUri = data.getParcelableExtra
                            (RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (ChosenSoundUri != null)
                        mModel.setChosenSoundUri(ChosenSoundUri.toString());
                    break;
            }
        }
        else
            Toast.makeText(getContext().getApplicationContext(),
                    getResources().
                    getString(R.string.error_type), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: Deserialize");
        FileInputStream fis;
        ObjectInputStream in;
        try
        {
            fis = new FileInputStream(getActivity().getFilesDir() + "obj.ser");
            in = new ObjectInputStream(fis);
            mModel = (Model)in.readObject();
            in.close();
            Log.d(TAG, "onActivityCreated: Deserialize SUCCESS");
        }
        catch(IOException | ClassNotFoundException ex)
        {
            Log.d(TAG, "onActivityCreated: Deserialize FAILURE");
        ex.printStackTrace();
        }
        mModel.addObserver(this);
        if(mModel.getBitmapInString()!=null) {
            byte[] b = Base64.decode(mModel.getBitmapInString(), Base64.DEFAULT);
            mModel.setInnerBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void update(Observable observable, Object data) {
        // Обновление картинки
        if(mModel.getInnerBitmap()!=null) {
            ImageView ivNotificationIco = (ImageView) getActivity().
                    findViewById(R.id.ivNotificationIco);
            Bitmap galleryPic = mModel.getInnerBitmap();
            galleryPic = scaleDownBitmap(galleryPic, 64, getContext());
            ivNotificationIco.setImageBitmap(galleryPic);
        }

        // Обновление названия мелодии
        if(mModel.getChosenSoundUri()!=null) {
            Uri ChosenSoundUri = Uri.parse(mModel.getChosenSoundUri());
            Cursor returnCursor = getActivity().
                    getContentResolver().query(ChosenSoundUri, null, null, null, null);
            assert returnCursor != null;
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            TextView tvChosenSound = (TextView) getActivity().
                    findViewById(R.id.tvChosenSound);
            try {
                tvChosenSound.setText(returnCursor.getString(nameIndex));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            returnCursor.close();
        }

        // Обновление переода появления уведомления
        if(mModel.getNotification_period()!=null) {
            EditText etPeriod = (EditText) getActivity().
                    findViewById(R.id.etPeriod);
            etPeriod.setText(mModel.getNotification_period());
        }

        // Обновление текста уведомления
        if(mModel.getNotification_text()!=null) {
            EditText etNotification = (EditText) getActivity().
                    findViewById(R.id.etNotification);
            etNotification.setText(mModel.getNotification_text());
        }
        Log.d(TAG, "UPDATE");
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "serialization");
        EditText etPeriod = (EditText) getActivity().findViewById(R.id.etPeriod);
        String Text_period = etPeriod.getText().toString();
        if(Text_period.length()!=0) {
            mModel.setNotification_period(Text_period);
        }
        EditText etNotification = (EditText) getActivity().findViewById(R.id.etNotification);
        String Text_Notification = etNotification.getText().toString();
        if(Text_Notification.length()!=0) {
            mModel.setNotification_text(Text_Notification);
        }
        if(mModel.getInnerBitmap()!=null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            mModel.getInnerBitmap().compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            mModel.setBitmapInString(encodedImage);
        }

        // Сериализация модели
        FileOutputStream fos;
        ObjectOutputStream out;
        try {
            fos = new FileOutputStream(getActivity().getFilesDir() + "obj.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(mModel);
            out.close();
            Log.d(TAG, "serialization SUCCESS");

        } catch (IOException ex) {
            Log.d(TAG, "serialization Failure");
            ex.printStackTrace();
        }

        Log.d(TAG, "delete observer");
        mModel.deleteObservers();
        super.onDestroyView();
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().
                getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

    public void StartService() {
        EditText etNotification = (EditText) getActivity().
                findViewById(R.id.etNotification);
        if (etNotification.getText().length() == 0)
            etNotification.setText(getResources().getString(
                    R.string.default_notification_text));
        ServiceIntent.putExtra("Notification_text",
                etNotification.getText().toString());
        EditText etPeriod = (EditText) getActivity().
                findViewById(R.id.etPeriod);
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

        ServiceIntent.setData(Uri.parse(mModel.getChosenSoundUri()));

        if (mModel.getInnerBitmap() != null) {
            Bitmap galleryPic = mModel.getInnerBitmap();
            galleryPic = scaleDownBitmap(galleryPic, 64, getContext());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            galleryPic.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            ServiceIntent.putExtra("image", encodedImage);
        }
        ServiceIntent.putExtra("period", arPeriod);
        getActivity().startService(ServiceIntent);
    }

    public void StopService() {
        getActivity().stopService(ServiceIntent);
    }

}