package com.lihoy21gmail.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class Main extends FragmentActivity  {
    private static final String TAG = "myLogs";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int intentMSG = getIntent().getIntExtra("Open_Notification_settings", 0);
        Log.d(TAG, "onCreate: " + intentMSG);
        myFragments fragment = myFragments.getFragment(intentMSG);
        //fragment =
        //fragment.getFragment(intentMSG);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment,fragment);
        /*
        Notification_settings notification_settings_fragment =
                Notification_settings.newInstance(1);
        StartFragment startFragment = StartFragment.newInstance(1);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (getIntent().getIntExtra("Open_Notification_settings", 0) == 2) {
            ft.add(R.id.fragment, notification_settings_fragment);
        } else
            ft.add(R.id.fragment, startFragment);
        */
        ft.commit();
    }

    // Смена фрагмента StartFragment -> NotificationFragment
    public void change_fragment() {
        myFragments fragment = myFragments.getFragment(1);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
}