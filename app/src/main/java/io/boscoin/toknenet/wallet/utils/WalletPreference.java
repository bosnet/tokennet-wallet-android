package io.boscoin.toknenet.wallet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;


public class WalletPreference {
    private static final String TAG = WalletPreference.class.getSimpleName();

    private static final String WALLET_IS_CHANGE_ORDER = "wallet.change.order";
    private static final String SKIP_CAUTIONS = "wallet.skip.caution";
    private static final String SETTING_LANG = "wallet.setting.language";


    public static void defaultPreference(Context context) {

        setBoolean(context, WALLET_IS_CHANGE_ORDER, false);
        setBoolean(context, SKIP_CAUTIONS, false);

    }

    public static boolean getWalletIsChangeOrder(Context context) {
        return getBoolean(context,WALLET_IS_CHANGE_ORDER, false);
    }

    public static void setWalletIsChangeOrder(Context context, boolean set) {
        setBoolean(context, WALLET_IS_CHANGE_ORDER, set);
    }

    public static boolean getSkipCaution(Context context) {
        return getBoolean(context,SKIP_CAUTIONS, false);
    }

    public static void setSkipCaution(Context context, boolean set) {
        setBoolean(context, SKIP_CAUTIONS, set);
    }


    public static void setWalletLanguage(Context context, String lang) {
        setString(context, SETTING_LANG, lang);
    }

    public static String getWalletLanguage(Context context) {
        Locale systemLocale = context.getResources().getConfiguration().locale;
        String strLanguage = systemLocale.getLanguage();

        return getString(context,SETTING_LANG, strLanguage);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean default_value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(key, default_value);
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(Context context, String key, int default_value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(key, default_value);
    }

    public static void setLong(Context context, String key, long value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(Context context, String key, long default_value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(key, default_value);
    }

    public static void setFloat(Context context, String key, float value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static float getFloat(Context context, String key, float default_value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getFloat(key, default_value);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key, String default_value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(key, default_value);
    }


}
