package com.learning.texnar13.teachersprogect;

import android.app.Application;
import android.content.res.Configuration;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.Locale;

public class MyApplication extends Application {
    private Locale locale;
    private String lang;


    @Override
    public void onCreate() {
        //здесь из базы данных достаем предыдущее значение локали и ставим его
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        lang = db.getSettingsLocale(1);
        if (lang.equals(SchoolContract.TableSettingsData.COLUMN_LOCALE_DEFAULT_CODE)) {
            lang = getResources().getConfiguration().locale.getLanguage();
        }
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
        //создаем приложение
        super.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }
}
