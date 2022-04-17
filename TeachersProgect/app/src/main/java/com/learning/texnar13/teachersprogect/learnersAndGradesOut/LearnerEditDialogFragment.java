package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class LearnerEditDialogFragment extends DialogFragment {//входные данные предыдущее имя, фамилия, id

    // константы по которым в диалог передаются аргументы
    public static final String ARGS_LEARNER_NAME = "name";
    public static final String ARGS_LEARNER_LAST_NAME = "lastName";
    public static final String ARGS_LEARNER_COMMENT = "comment";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.learners_and_grades_dialog_learner_edit, null);
        builder.setView(dialogView);

        // кнопка назад
        dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                try {
                    //вызываем в активности метод по обновлению таблицы
                    ((UpdateTableInterface) getActivity()).allowUserEditLearners();
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс UpdateTableInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnerDialogFragment: you must implements UpdateTableInterface in your activity"
                    );
                }
            }
        });

        // текстовое поле фамилии
        final EditText editSurname = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_edit_second_name);
        try {//входные данные предыдущая фамилия
            editSurname.setText(getArguments().getString(ARGS_LEARNER_LAST_NAME));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"" + ARGS_LEARNER_LAST_NAME + "\"");
        }

        // текстовое поле имени
        final EditText editName = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_edit_name);
        try {//входные данные предыдущее имя
            editName.setText(getArguments().getString(ARGS_LEARNER_NAME));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"" + ARGS_LEARNER_NAME + "\"");
        }

        // текстовое поле комментария
        final EditText editComment = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_edit_comment);
        try {//входные данные предыдущее имя
            editComment.setText(getArguments().getString(ARGS_LEARNER_COMMENT));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"" + ARGS_LEARNER_COMMENT + "\"");
        }

        // кнопка сохранения
        dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editSurname.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), R.string.learners_and_grades_out_activity_dialog_toast_no_last_name, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        //вызываем в активности метод по созданию ученика и передаем ей имя и фамилию
                        ((EditLearnerDialogInterface) getActivity()).editLearner(
                                editSurname.getText().toString().trim(),
                                editName.getText().toString().trim(),
                                editComment.getText().toString().trim()
                        );
                    } catch (java.lang.ClassCastException e) {
                        //в вызвающей активности должен быть имплементирован класс EditLearnerInterface
                        e.printStackTrace();
                        Log.i(
                                "TeachersApp",
                                "LearnerEditDialogFragment: you must implements EditLearnerInterface in your activity"
                        );
                    }
                    dismiss();
                }
            }
        });

        // кнопка удаления
        dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_button_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по далению ученика и передаем id
                    ((EditLearnerDialogInterface) getActivity()).removeLearner();
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditLearnerInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "LearnerEditDialogFragment: you must implements EditLearnerInterface in your activity"
                    );
                }
                dismiss();
            }
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        Log.i("TeachersApp", "LearnerEditDialogFragment - onCancel");
        try {
            //вызываем в активности метод по обновлению таблицы
            ((UpdateTableInterface) getActivity()).allowUserEditLearners();
        } catch (java.lang.ClassCastException e) {
            //в вызвающей активности должен быть имплементирован класс UpdateTableInterface
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "CreateLearnerDialogFragment: you must implements UpdateTableInterface in your activity"
            );
        }
    }
}

interface EditLearnerDialogInterface {
    void editLearner(String lastName, String name,String comment);

    void removeLearner();
}

