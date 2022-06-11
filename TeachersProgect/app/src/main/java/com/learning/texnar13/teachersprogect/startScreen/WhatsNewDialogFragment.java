package com.learning.texnar13.teachersprogect.startScreen;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class WhatsNewDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // начинаем строить диалог
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        View rootView = getLayoutInflater().inflate(R.layout.base_dialog_with_head_and_body_text, null);
        builder.setView(rootView);

        // кнопка выхода
        rootView.findViewById(R.id.close_button).setOnClickListener(v -> dismiss());

        // заголовок
        ((TextView) rootView.findViewById(R.id.title)).setText(
                R.string.start_screen_activity_dialog_whats_new_title);

        // текст
        ((TextView) rootView.findViewById(R.id.body_text)).setText(
                R.string.start_screen_activity_dialog_whats_new_text);

        // кнопка согласия
        TextView acceptButton = rootView.findViewById(R.id.accept_text_button);
        acceptButton.setText(R.string.start_screen_activity_dialog_whats_new_button_ok);
        acceptButton.setOnClickListener(v -> dismiss());

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        return dialog;
    }
}
