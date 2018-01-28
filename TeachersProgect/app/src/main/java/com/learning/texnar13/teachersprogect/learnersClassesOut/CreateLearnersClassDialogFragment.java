package com.learning.texnar13.teachersprogect.learnersClassesOut;

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

public class CreateLearnersClassDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Добавление класса");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_create_learners_class, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.create_learners_class_dialog_fragment_linear_layout);

        //текстовое поле имени
        final EditText editName = new EditText(getActivity());
        editName.setTextColor(Color.BLACK);
        editName.setHint("ИМЯ");
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setHintTextColor(Color.GRAY);
        //добавляем текстовое поле
        linearLayout.addView(editName);

        //кнопки согласия/отмены
        builder.setPositiveButton("добавление", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //вызываем в активности метод по созданию класса и передаем ей имя
                    ((com.learning.texnar13.teachersprogect.learnersClassesOut.CreateLearnersClassDialogInterface) getActivity()).createLearnersClass(
                            editName.getText().toString()
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс CreateLearnersClassInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnersClassDialogFragment: you must implements CreateLearnersClassInterface in your activity"
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

interface CreateLearnersClassDialogInterface {
    void createLearnersClass(String name);
}
