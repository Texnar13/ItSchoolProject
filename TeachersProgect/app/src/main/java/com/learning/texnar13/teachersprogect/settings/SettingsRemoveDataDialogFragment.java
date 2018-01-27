package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;

import java.util.Random;

public class SettingsRemoveDataDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Вы точно хотите удалить все данные приложения?");

//----layout диалога----

        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_settings_remove, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.settings_remove_dialog_fragment_linear_layout);

//----защита от случайных удалений----

        Random random = new Random();
        final int n = random.nextInt(50);

        //текстовое поле с числом
        final TextView numberText = new TextView(getActivity());
        numberText.setGravity(Gravity.CENTER);
        numberText.setTextColor(Color.BLACK);
        numberText.setText("Для подтверждения введите число  " + n);
        numberText.setTextSize(20);
        //добавляем текстовое поле
        linearLayout.addView(numberText);

        //текстовое поле для числа
        final EditText editNumber = new EditText(getActivity());
        editNumber.setTextColor(Color.BLACK);
        editNumber.setHint("число");
        editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        editNumber.setHintTextColor(Color.GRAY);
        //добавляем текстовое поле
        linearLayout.addView(editNumber);

//----кнопки согласия/отмены----

        builder.setPositiveButton("удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //если числа совпали вызываем в активности метод по удалению
                    if(editNumber.getText().toString().equals(Integer.toString(n))) {
                        ((com.learning.texnar13.teachersprogect.settings.SettingsRemoveInterface)
                                getActivity()).settingsRemove();
                    }else{
                        //пишем пользователю о его ошибке
                        Toast toast = Toast.makeText(getActivity().getApplicationContext()
                                ,"вы ввели неправильное число, данные не удалены",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс CreateCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "SettingsRemoveDataDialogFragment: you must implements SettingsRemoveInterface in your activity"
                    );
                }
            }
        });
        builder.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }
}

interface SettingsRemoveInterface {
    void settingsRemove();
}