package com.learning.texnar13.teachersprogect.data;

import com.learning.texnar13.teachersprogect.BuildConfig;

public class SharedPrefsContract {// менять значение констант запрещено!!!

    // активность подписок и иже с ней
    // статус подписки
    public static final String PREFS_BOOLEAN_PREMIUM_STATE = "premiumState";


    // при старте приожения на главной активности
    // счетчик заходов в приложение
    public static final String PREFS_INT_ENTERS_COUNT = "entersCount";
    // оценено?
    public static final String PREFS_BOOLEAN_IS_RATE = "isRate";
    // дляы новых версий
    public static final String PREFS_INT_WHATS_NEW = "whatsNew";
    public static final int PREFS_INT_NOW_VERSION = BuildConfig.VERSION_CODE;

    // ctrl + shift + F  SharedPreferences
}


// получаем сохраненные данные
//        SharedPreferences preferences =
//                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                { // имя
//                      String errorString = getResources().getString(R.string.no_name_text);
//                      String name = preferences.getString(StudentSettingsActivity.PREF_NAME, errorString);
//                      if (!name.equals(errorString)) {
//                          nameEditText.setText(name);
//                      }
//                }
//
//
//
//
//                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                // имя
//                String name = preferences.getString(StudentSettingsActivity.PREF_NAME,
//                getResources().getString(R.string.no_name_text));
//
//    // сохраняем параметр в SharedPreferences
//    SharedPreferences.Editor editor =
//            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
//                                editor.putInt(SharedPrefsContract.PREFS_PREMIUM_STATE,
//    SharedPrefsContract.VALUE_PREMIUM_ACTIVE);
//                                editor.apply();