package com.dentist.dokterdent.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preference {

    private static final String KEY_CHAT_ID  ="chat_id";
    private static final String KEY_KONSELOR_ID = "konselor_id";
    private static final String KEY_CHAT_NAME  ="chat_name";
    private static final String KEY_CHAT_PHOTO ="chat_photo";
    private static final String KEY_CHAT_STATUS = "chat_status";

    private static SharedPreferences getSharedPreference(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setKeyChatId(Context context, String id){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_CHAT_ID, id);
        editor.apply();
    }

    public static String getKeyChatId(Context context){
        return getSharedPreference(context).getString(KEY_CHAT_ID,"");
    }

    public static void setKeyKonselorId(Context context, String id){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_KONSELOR_ID, id);
        editor.apply();
    }

    public static String getKeyKonselorId(Context context){
        return getSharedPreference(context).getString(KEY_KONSELOR_ID,"");
    }


    public static void setKeyChatName(Context context, String nama){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_CHAT_NAME, nama);
        editor.apply();
    }

    public static String getKeyChatName(Context context){
        return getSharedPreference(context).getString(KEY_CHAT_NAME,"");
    }

    public static void setKeyChatPhoto(Context context, String photo){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_CHAT_PHOTO, photo);
        editor.apply();
    }

    public static String getKeyChatPhoto(Context context){
        return getSharedPreference(context).getString(KEY_CHAT_PHOTO,"");
    }

    public static void setKeyChatStatus(Context context, String status){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString(KEY_CHAT_STATUS, status);
        editor.apply();
    }

    public static String getKeyChatStatus(Context context){
        return getSharedPreference(context).getString(KEY_CHAT_STATUS,"");
    }

    public static void removeChatData(Context context){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.remove(KEY_CHAT_ID);
        editor.remove(KEY_KONSELOR_ID);
        editor.remove(KEY_CHAT_NAME);
        editor.remove(KEY_CHAT_PHOTO);
        editor.remove(KEY_CHAT_STATUS);
        editor.apply();
    }
}
