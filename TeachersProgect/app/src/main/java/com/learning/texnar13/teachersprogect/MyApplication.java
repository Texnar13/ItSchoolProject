package com.learning.texnar13.teachersprogect;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.yandex.mobile.ads.common.MobileAds;

import java.util.Locale;

public class MyApplication extends MultiDexApplication {//MultiDexApplication/Application // todo multiDex
    private Locale locale;


    @Override
    public void onCreate() {

        // обновляем значение локали
        MyApplication.updateLangForContext(getApplicationContext());
        // создаем приложение
        super.onCreate();

        // инициализируем бибилотеку рекламы
        MobileAds.initialize(this, () -> Log.d("YANDEX_MOBILE_ADS_TAG", "SDK initialized"));

        /*
        * Устанавливает значение, которое определяет, разрешил ли пользователь из GDPR-региона
        * сбор персональных данных, используемых для аналитики и таргетирования рекламы.
        * Пользовательские данные не будут собираться до тех пор, пока сбор данных не будет разрешен.
        * Если пользователь однажды разрешил или запретил сбор данных, требуется передавать это значение
        * при каждом запуске приложения.
        *
        * */
        MobileAds.setUserConsent(false);// Есть диалог на главном экране GDPR Вот из него и надо брать информацию разрешил пользователь или нет
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
        // и ставим его
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        // config.fontScale todo почитать про динамическое изменение шрифта
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
    }
}
