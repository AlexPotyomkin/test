package com.lihoy21gmail.test;

import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Notification_settings extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.notification_settings, null);
        Button btChooseImage = (Button) v.findViewById(R.id.btChooseImage);
        btChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Main) getActivity()).change_image();
            }
        });

        Button btChooseSound = (Button) v.findViewById(R.id.btChooseSound);
        btChooseSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Main) getActivity()).change_sound();
            }
        });

        if (Build.VERSION.SDK_INT >= 14) {
            Switch swtch = (Switch) v.findViewById(R.id.swtch);
            if(((Main) getActivity()).isMyServiceRunning(NotificationService.class))
                swtch.setChecked(true);
            swtch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        ((Main) getActivity()).myStartService();
                    else
                        ((Main) getActivity()).myStopService();
                }
            });
        } else {
            ToggleButton TB = (ToggleButton) v.findViewById(R.id.toggleButton);
            if(((Main) getActivity()).isMyServiceRunning(NotificationService.class))
                TB.setChecked(true);
            TB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        ((Main) getActivity()).myStartService();
                    else
                        ((Main) getActivity()).myStopService();
                }
            });
        }
        TextView tvChosenSound = (TextView) v.findViewById(R.id.tvChosenSound);
        Uri ChosenSoundUri = RingtoneManager.getActualDefaultRingtoneUri(
                getActivity().getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        Cursor returnCursor =
                getActivity().getContentResolver().query(ChosenSoundUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        tvChosenSound.setText(returnCursor.getString(nameIndex));

        return v;
    }

}