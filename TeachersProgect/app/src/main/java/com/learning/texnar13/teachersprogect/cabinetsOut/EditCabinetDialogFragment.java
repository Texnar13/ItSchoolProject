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

public class EditCabinetDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Редактирование кабинета");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_cabinet, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_cabinet_dialog_fragment_linear_layout);

//текстовое поле имени
        final EditText editName = new EditText(getActivity());
        editName.setTextColor(Color.BLACK);
        editName.setHint("ИМЯ");
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setHintTextColor(Color.GRAY);
        try {//входные данные предыдущее имя
            editName.setText(getArguments().getString("name"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"name\"");
        }
        //добавляем текстовое поле
        linearLayout.addView(editName);

//кнопка сохранения изменений
        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //вызываем в активности метод по созданию кабинета и передаем ей имя
                    ((EditCabinetDialogInterface) getActivity()).editCabinet(
                            editName.getText().toString(),
                            getArguments().getLong("cabinetId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must implements EditCabinetInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id кабинета( Bungle putLong("cabinetId",cabinetId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must give cabinetId( Bungle putLong(\"cabinetId\",cabinetId) )"
                    );
                }
            }
        });
//просто выход из диалога
        builder.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
//удаление кабинета
        builder.setNeutralButton("удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //вызываем в активности метод по далению кабинета и передаем id
                    ((EditCabinetDialogInterface) getActivity()).removeCabinet(
                            getArguments().getLong("cabinetId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must implements EditCabinetInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id кабинета( Bungle putLong("cabinetId",cabinetId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must give cabinetId( Bungle putLong(\"cabinetId\",cabinetId) )"
                    );
                }
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

interface EditCabinetDialogInterface {
    void editCabinet(String name, long cabinetId);
    void removeCabinet(long cabinetId);
}