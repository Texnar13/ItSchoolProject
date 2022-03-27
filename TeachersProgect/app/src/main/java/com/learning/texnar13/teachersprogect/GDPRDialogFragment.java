package com.learning.texnar13.teachersprogect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.DialogFragment;

/**
    Весной 2018 года в силу вступил общий регламент по защите данных
 (General Data Protection Regulation, сокращенно GDPR).
 Регламент регулирует сбор и обработку информации о физических лицах —
 гражданах Европейской экономической зоны и Швейцарии.
 Он призван усилить защиту конфиденциальных данных и сделать прозрачными все элементы сбора,
 хранения и обработки информации в интернете.

    GDPR имеет экстерриториальное действие и применяется ко всем компаниям,
 которые обрабатывают персональные данные граждан Европейской экономической зоны и Швейцарии,
 независимо от местонахождения такой компании.

    Начиная с версии 2.80, Yandex Mobile Ads SDK позволит ограничить сбор данных пользователей,
 расположенных в Европейской экономической зоне и Швейцарии, при отсутствии их согласия на это.
 */
public class GDPRDialogFragment extends DialogFragment {

    // Код демонстрирует создание диалога.
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Заголовок политики")
                .setMessage("Текст политики...персонализированная реклама")
                .setPositiveButton("/Канеш", (dialog, id) -> onButtonClicked(context, true))
                .setNeutralButton("О чем речь?", (dialog, which) -> openPrivacyPolicy())
                .setNegativeButton("Не хочу", (dialog, id) -> onButtonClicked(context, false));
        return builder.create();
    }

    private void openPrivacyPolicy() {
        final String url = "https://docs.google.com/document/d/1Goa-tW9tp6A9hciCn89nIWxfjwxk4buKJcvZMnHD2TA";
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void onButtonClicked(final Context context, final boolean userConsent) {
        //todo Это все одно большое todo
//        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        preferences.edit()
//                .putBoolean(SettingsFragment.USER_CONSENT_KEY, userConsent)
//                .putBoolean(SettingsFragment.DIALOG_SHOWN_KEY, true)
//                .apply();
// сохраняем инфу, о том, хочет пользователь персонализированную рекламу или нет
//
        /*
         * Устанавливает значение, которое определяет, разрешил ли пользователь из GDPR-региона
         * сбор персональных данных, используемых для аналитики и таргетирования рекламы.
         * Пользовательские данные не будут собираться до тех пор, пока сбор данных не будет разрешен.
         * Если пользователь однажды разрешил или запретил сбор данных, требуется передавать это значение
         * при каждом запуске приложения.
         *
         * */
//        mNoticeDialogListener.onDialogClick();
    }
}