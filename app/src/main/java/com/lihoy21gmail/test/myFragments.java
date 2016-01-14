package com.lihoy21gmail.test;

/**
 * Created by Потёмкин on 14.01.2016.
 */
public abstract class myFragments extends android.support.v4.app.Fragment {
    private static myFragments fragment = null;

    public static myFragments getFragment(int TypeFragment){
        if(TypeFragment == 1)
            fragment = new Notification_settings();
        else if(TypeFragment == 0)
            fragment = new StartFragment();
        return fragment;
    }
}
