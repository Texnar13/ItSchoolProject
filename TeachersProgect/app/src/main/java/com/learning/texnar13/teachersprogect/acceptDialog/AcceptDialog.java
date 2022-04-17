package com.learning.texnar13.teachersprogect.acceptDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class AcceptDialog extends DialogFragment {

    public static final String ARG_ACCEPT_MESSAGE = "acceptMessage";
    public static final String ARG_ACCEPT_BUTTON_TEXT = "acceptButton";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.accept_dialog, null);
        builder.setView(rootView);

        // кнопка закрытия диалога
        rootView.findViewById(R.id.accept_dialog_button_close).setOnClickListener(v -> dismiss());

        // получаем параметры
        String title = getArguments().getString(ARG_ACCEPT_MESSAGE);
        String buttonText = getArguments().getString(ARG_ACCEPT_BUTTON_TEXT);
        if (title == null || buttonText == null) throw new NullPointerException();

        // заголовок
        ((TextView) rootView.findViewById(R.id.accept_dialog_title)).setText(title);

        // кнопка
        TextView button = rootView.findViewById(R.id.accept_dialog_text_button_accept);
        button.setText(buttonText);
        button.setOnClickListener(v -> {
            ((AcceptDialogInterface) getActivity()).accept();
            dismiss();
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    public interface AcceptDialogInterface {
        void accept();
    }
}