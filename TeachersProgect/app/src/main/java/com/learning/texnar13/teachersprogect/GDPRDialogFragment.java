package com.learning.texnar13.teachersprogect;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;

/**
 * Весной 2018 года в силу вступил общий регламент по защите данных
 * (General Data Protection Regulation, сокращенно GDPR).
 * Регламент регулирует сбор и обработку информации о физических лицах —
 * гражданах Европейской экономической зоны и Швейцарии.
 * Он призван усилить защиту конфиденциальных данных и сделать прозрачными все элементы сбора,
 * хранения и обработки информации в интернете.
 * <p>
 * GDPR имеет экстерриториальное действие и применяется ко всем компаниям,
 * которые обрабатывают персональные данные граждан Европейской экономической зоны и Швейцарии,
 * независимо от местонахождения такой компании.
 * <p>
 * Начиная с версии 2.80, Yandex Mobile Ads SDK позволит ограничить сбор данных пользователей,
 * расположенных в Европейской экономической зоне и Швейцарии, при отсутствии их согласия на это.
 */
public class GDPRDialogFragment extends DialogFragment {

    // Код демонстрирует создание диалога.
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // начинаем строить диалог
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        View rootView = getLayoutInflater().inflate(R.layout.base_dialog_with_head_body_text_and_two_buttons, null);
        builder.setView(rootView);

        // кнопка отмены
        rootView.findViewById(R.id.close_button).setOnClickListener(v ->
                onButtonClicked(requireActivity(), false));

        // заголовок
        ((TextView) rootView.findViewById(R.id.title))
                .setText(R.string.start_screen_activity_dialog_GDPR_title);

        // текст
        ((TextView) rootView.findViewById(R.id.body_text))
                .setText(R.string.start_screen_activity_dialog_GDPR_text);

        // кнопка согласия
        TextView acceptButton = rootView.findViewById(R.id.accept_text_button);
        acceptButton.setText(R.string.start_screen_activity_dialog_GDPR_accept);
        acceptButton.setOnClickListener(v ->
                onButtonClicked(requireActivity(), true));

        // кнопка несогласия
        TextView discardButton = rootView.findViewById(R.id.accept_text_button_2);
        discardButton.setText(R.string.start_screen_activity_dialog_GDPR_decline);
        discardButton.setOnClickListener(v ->
                onButtonClicked(requireActivity(), false));


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        return dialog;
    }

    private void onButtonClicked(final Context context, final boolean userConsent) {
        dismiss();
        ((GDPRDialogBackInterface) context).dialogResultGDPR(userConsent);
    }


// возможно это не понадобится
//    private void openPrivacyPolicy() {
//        final String url = "https://docs.google.com/document/d/1Goa-tW9tp6A9hciCn89nIWxfjwxk4buKJcvZMnHD2TA";
//        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//        startActivity(intent);
//    }


    public interface GDPRDialogBackInterface {
        void dialogResultGDPR(boolean accepted);
    }

}