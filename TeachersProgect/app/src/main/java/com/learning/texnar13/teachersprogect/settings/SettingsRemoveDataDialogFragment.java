package com.learning.texnar13.teachersprogect.settings;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

import java.util.Random;

public class SettingsRemoveDataDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // начинаем строить диалог
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        View root = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_remove_data, null);
        builder.setView(root);

        // при нажатии на кнопку закрыть
        root.findViewById(R.id.settings_dialog_remove_data_close).setOnClickListener(v -> dismiss());

        // в качестве защиты выводим случайное число
        Random random = new Random();
        final int n = random.nextInt(40) + 10;
        ((TextView) root.findViewById(R.id.settings_dialog_remove_data_confirm_output)).setText(
                getResources().getString(R.string.settings_activity_dialog_delete_text_confirm_delete, n));

        // нажатие на кнопку удалить все данные
        root.findViewById(R.id.settings_dialog_remove_data_button).setOnClickListener(v -> {
            EditText input = root.findViewById(R.id.settings_dialog_remove_data_input);
            //если числа совпали вызываем в активности метод по удалению
            if (input.getText().toString().trim().equals(Integer.toString(n))) {
                ((SettingsRemoveInterface)
                        getActivity()).settingsRemove();
            } else {
                //пишем пользователю о его ошибке
                Toast toast = Toast.makeText(getActivity().getApplicationContext()
                        , getResources().getString(R.string.settings_activity_toast_data_delete_falture), Toast.LENGTH_SHORT);
                toast.show();
            }
            dismiss();
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        return dialog;
    }
}

interface SettingsRemoveInterface {
    void settingsRemove();
}