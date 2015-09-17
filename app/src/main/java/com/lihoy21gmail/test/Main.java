package com.lihoy21gmail.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class Main extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StartFragment startFragment = new StartFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, startFragment).commit();

        Notification_settings notification_settings = new Notification_settings();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, notification_settings).commit();
    }


}