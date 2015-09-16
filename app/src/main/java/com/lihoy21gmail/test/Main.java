package com.lihoy21gmail.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class Main extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainFragment myFrag = new MainFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.cont, myFrag).commit();
    }


}