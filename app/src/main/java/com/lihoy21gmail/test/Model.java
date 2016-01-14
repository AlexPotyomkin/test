package com.lihoy21gmail.test;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Observable;

public class Model extends Observable implements Serializable{
    private static Model instance;
    private String Notification_text;
    private String Notification_period;
    private String ChosenSoundUri = null;
    private String BitmapInString;
    private transient Bitmap InnerBitmap = null;

    private Model(){}

    public static Model getInstance(){
        if(instance == null){
            instance = new Model();
        }
        return instance;
    }

    public String getChosenSoundUri() {
        return ChosenSoundUri;
    }

    public Bitmap getInnerBitmap() {
        return InnerBitmap;
    }

    public String getNotification_text(){
        return Notification_text;
    }

    public String getNotification_period() {
        return Notification_period;
    }

    public String getBitmapInString() {
        return BitmapInString;
    }

    public void setNotification_text(String notification_text) {
        Notification_text = notification_text;
    }

    public void setNotification_period(String notification_period) {
        Notification_period = notification_period;
    }

    public void setInnerBitmap(Bitmap innerBitmap) {
        InnerBitmap = innerBitmap;
        setChanged();
        notifyObservers();
    }

    public void setChosenSoundUri(String chosenSoundUri) {
        ChosenSoundUri = chosenSoundUri;
        setChanged();
        notifyObservers();
    }

    public void setBitmapInString(String bitmapInString) {
        BitmapInString = bitmapInString;
    }
}
