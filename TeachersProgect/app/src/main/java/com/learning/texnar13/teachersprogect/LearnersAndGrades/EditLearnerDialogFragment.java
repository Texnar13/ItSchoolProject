package com.learning.texnar13.teachersprogect.LearnersAndGrades;

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

public class EditLearnerDialogFragment extends DialogFragment {//входные данные предыдущее имя, фамилия, id

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Редактирование ученика");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.edit_learner_dialog_fragment_layout, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_learner_dialog_fragment_linear_layout);

        //текстовое поле имени
        final EditText editName = new EditText(getActivity());
        editName.setTextColor(Color.BLACK);
        editName.setHint("ИМЯ");
        editName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editName.setHintTextColor(Color.GRAY);
        try {//входные данные предыдущее имя
            editName.setText(getArguments().getString("name"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp","you must give bundle argument \"name\"");
        }
        //добавляем текстовое поле
        linearLayout.addView(editName);

        //текстовое поле фамилии
        final EditText editLastName = new EditText(getActivity());
        editLastName.setTextColor(Color.BLACK);
        editLastName.setHint("ФАМИЛИЯ");
        editLastName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editLastName.setHintTextColor(Color.GRAY);
        try {//входные данные предыдущая фамилия
            editLastName.setText(getArguments().getString("lastName"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp","you must give bundle argument \"lastName\"");
        }
        //добавляем текстовое поле
        linearLayout.addView(editLastName);

        //кнопка сохранения изменений
        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //вызываем в активности метод по созданию ученика и передаем ей имя и фамилию
                    ((EditLearnerDialogInterface) getActivity()).editLearner(
                            editLastName.getText().toString(),
                            editName.getText().toString(),
                            getArguments().getLong("learnerId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс CreateLearnerInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnerDialogFragment: you must implements CreateLearnerInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id ученика( Bungle putLong("learnerId",learnerId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnerDialogFragment: you must give learnerId( Bungle putLong(\"learnerId\",learnerId) )"
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
        //удаление ученика
        builder.setNeutralButton("удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //вызываем в активности метод по далению ученика и передаем id
                    ((EditLearnerDialogInterface) getActivity()).removeLearner(
                            getArguments().getLong("learnerId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс CreateLearnerInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnerDialogFragment: you must implements CreateLearnerInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id ученика( Bungle putLong("learnerId",learnerId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnerDialogFragment: you must give learnerId( Bungle putLong(\"learnerId\",learnerId) )"
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

interface EditLearnerDialogInterface {
    void editLearner(String lastName, String name, long learnerId);
    void removeLearner(long learnerId);
}

