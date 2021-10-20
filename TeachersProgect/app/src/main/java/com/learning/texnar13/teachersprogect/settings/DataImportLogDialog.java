package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class DataImportLogDialog extends DialogFragment {

    // константа для передачи текста сообщения
    public static final String PARAM_LOG_MESSAGE = "log_message";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View scrollLayout = getActivity().getLayoutInflater().inflate(
                R.layout.settings_dialog_data_import_log, null);
        builder.setView(scrollLayout);

        // кнопка закрыть диалог
        scrollLayout.findViewById(R.id.settings_dialog_data_import_log_cancel_button).setOnClickListener(v -> dismiss());
        // кнопка подтверждения изменений
        scrollLayout.findViewById(R.id.settings_dialog_data_import_log_button_accept).setOnClickListener(v -> {
            //todo
            //  после нажатия кнопки подтвердить отправляем данные как результат
        });

        // вывод текста с информацией
        if (getArguments() == null){ dismiss(); return builder.create();}
        ((TextView) scrollLayout.findViewById(R.id.settings_dialog_data_import_log_out_text_field))
                .setText(getArguments().getString(PARAM_LOG_MESSAGE));

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    interface DataImportAccept {
        void acceptDataImport();
    }
}
