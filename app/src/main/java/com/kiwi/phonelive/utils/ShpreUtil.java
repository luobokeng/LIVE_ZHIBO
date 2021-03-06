package com.kiwi.phonelive.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class ShpreUtil {

    private final String SP_NAME = "language_setting";
    private final String TAG_LANGUAGE = "language_select";
    private final String TAG_SYSTEM_LANGUAGE = "system_language";
    private static volatile ShpreUtil instance;

    private final SharedPreferences mSharedPreferences;

    private Locale systemCurrentLocal = Locale.ENGLISH;


    public ShpreUtil(Context context) {
        mSharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }


    public void saveLanguage(int select) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(TAG_LANGUAGE, select);
        edit.commit();
    }

    public int getSelectLanguage() {
        return mSharedPreferences.getInt(TAG_LANGUAGE, 0);
    }


    public Locale getSystemCurrentLocal() {
        return systemCurrentLocal;
    }

    public void setSystemCurrentLocal(Locale local) {
        systemCurrentLocal = local;
    }

    public static ShpreUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (ShpreUtil.class) {
                if (instance == null) {
                    instance = new ShpreUtil(context);
                }
            }
        }
        return instance;
    }
}
