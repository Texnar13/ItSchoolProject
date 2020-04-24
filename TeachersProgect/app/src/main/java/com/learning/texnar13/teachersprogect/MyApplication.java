package com.learning.texnar13.teachersprogect;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.Locale;

public class MyApplication extends Application {
    private Locale locale;


    @Override
    public void onCreate() {

        // обновляем значение локали
        MyApplication.updateLangForContext(getApplicationContext());

        //создаем приложение
        super.onCreate();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        locale = new Locale(lang);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getApplicationContext().getResources().updateConfiguration(config, null);
    }


    public static void updateLangForContext(Context context) {
        //здесь из базы данных достаем предыдущее значение локали и ставим его
        DataBaseOpenHelper db = new DataBaseOpenHelper(context);
        String lang = db.getSettingsLocale(1);
        if (lang.equals(SchoolContract.TableSettingsData.COLUMN_LOCALE_DEFAULT_CODE)) {
            lang = context.getResources().getConfiguration().locale.getLanguage();
        }
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
    }
}
