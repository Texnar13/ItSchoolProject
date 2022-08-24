package com.learning.texnar13.teachersprogect;

import android.content.Context;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;
import com.yandex.mobile.ads.common.MobileAds;

import java.util.Locale;

public class MyApplication extends MultiDexApplication {//MultiDexApplication/Application // todo multiDex


    @Override
    public void onCreate() {

        // меняем тему если она жестко задана
        switch (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt(SharedPrefsContract.PREFS_DAY_NIGHT_MODE, 0)) {
//            case 0:
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
//                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }

        // настраиваем программный вывод векторных изображений
        //  https://stackoverflow.com/questions/43004886/resourcescompat-getdrawable-vs-appcompatresources-getdrawable
        //AppCompatResources.getDrawable(this, R.drawable.base_button_close_background_round));
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // обновляем значение локали
        MyApplication.updateLangForContext(getApplicationContext());
        // создаем приложение
        super.onCreate();


        /*
         * GDPR
         * Устанавливает значение, которое определяет, разрешил ли пользователь из GDPR-региона
         * сбор персональных данных, используемых для аналитики и таргетирования рекламы.
         * Пользовательские данные не будут собираться до тех пор, пока сбор данных не будет разрешен.
         * Если пользователь однажды разрешил или запретил сбор данных, требуется передавать это значение
         * при каждом запуске приложения.
         *
         * */

        // инициализируем бибилотеку рекламы
        MobileAds.initialize(this, () -> Log.d("YANDEX_MOBILE_ADS_TAG", "SDK initialized"));

        int GDPRState = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt(SharedPrefsContract.PREFS_INT_WHATS_NEW,
                        SharedPrefsContract.PREFS_INT_GDPR_STATE_NONE);
        MobileAds.setUserConsent(GDPRState == SharedPrefsContract.PREFS_INT_GDPR_STATE_ACCEPT);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    public static void updateLangForContext(Context context) {
        // здесь из базы данных достаем предыдущее значение локали
        DataBaseOpenHelper db = new DataBaseOpenHelper(context);
        String lang = db.getSettingsLocale(1);
        if (lang.equals(SchoolContract.TableSettingsData.COLUMN_LOCALE_DEFAULT_CODE)) {
            lang = context.getResources().getConfiguration().locale.getLanguage();
        }

        // создаем нужный обьект локали
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);


        // получаем текущую конфигурацию в контексте
        Configuration config = context.getResources().getConfiguration();

        // добавляем конфигурации новые параметры
        config.locale = locale;
        config.fontScale = 1.0f;
        // config.fontScale todo почитать про динамическое изменение шрифта

        // ставим обратно получившуюся конфигурацию
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}
