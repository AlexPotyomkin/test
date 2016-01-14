package com.lihoy21gmail.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class Main extends FragmentActivity  {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Notification_settings notification_settings_fragment =
                Notification_settings.newInstance(1);
        StartFragment startFragment = StartFragment.newInstance(1);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (getIntent().getIntExtra("Open_Notification_settings", 0) == 2) {
            ft.add(R.id.fragment, notification_settings_fragment);
        } else
            ft.add(R.id.fragment, startFragment);
        ft.commit();
    }

    // Смена фрагмента StartFragment -> NotificationFragment
    public void change_fragment() {
        Notification_settings notification_settings_fragment =
                Notification_settings.newInstance(1);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment, notification_settings_fragment)
                .addToBackStack(null)
                .commit();
    }
}