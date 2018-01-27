package com.learning.texnar13.teachersprogect.cabinetsOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.learning.texnar13.teachersprogect.R;

public class CreateCabinetDialogFragment extends DialogFragment{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Добавление кабинета");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_create_cabinet, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.create_cabinet_dialog_fragment_linear_layout);

        //текстовое поле имени
        final EditText editName = new EditText(getActivity());
        editName.setTextColor(Color.BLACK);
        editName.setHint("ИМЯ");
        //editName.setSingleLine(true);
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setHintTextColor(Color.GRAY);
        //добавляем текстовое поле
        linearLayout.addView(editName);

        //кнопки согласия/отмены
        builder.setPositiveButton("добавление", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //вызываем в активности метод по созданию кабинета и передаем ей имя
                    ((com.learning.texnar13.teachersprogect.cabinetsOut.CreateCabinetInterface) getActivity()).createCabinet(
                            editName.getText().toString()
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс CreateCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateCabinetDialogFragment: you must implements CreateCabinetInterface in your activity"
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

interface CreateCabinetInterface {
    void createCabinet(String name);
}
