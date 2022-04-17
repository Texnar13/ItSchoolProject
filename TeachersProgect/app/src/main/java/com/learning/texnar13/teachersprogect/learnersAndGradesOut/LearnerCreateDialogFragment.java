package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class LearnerCreateDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i("TeachersApp", "LearnerCreateDialogFragment - onCreateDialog");
        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.learners_and_grades_dialog_learner_create, null);
        builder.setView(dialogView);

        // отмена
        dialogView.findViewById(R.id.learners_and_grades_dialog_learner_create_button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Log.i("TeachersApp", "CreateLearnerDialogFragment - onCancel");
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
        final EditText editSurname = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_create_edit_second_name);
        // текстовое поле имени
        final EditText editName = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_create_edit_name);
        // текстовое поле комментария
        final EditText editComment = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_create_edit_comment);

        //при нажатии...
        //согласие
        dialogView.findViewById(R.id.learners_and_grades_dialog_learner_create_button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editSurname.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), R.string.learners_and_grades_out_activity_dialog_toast_no_last_name, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        //вызываем в активности метод по созданию ученика и передаем ей имя и фамилию
                        ((CreateLearnerInterface) getActivity()).createLearner(
                                editSurname.getText().toString().trim(),
                                editName.getText().toString().trim(),
                                editComment.getText().toString().trim()
                        );
                    } catch (java.lang.ClassCastException e) {
                        //в вызвающей активности должен быть имплементирован класс CreateLearnerInterface
                        e.printStackTrace();
                        Log.i(
                                "TeachersApp",
                                "LearnerCreateDialogFragment: you must implements CreateLearnerInterface in your activity"
                        );
                    }
                    dismiss();
                }
            }
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.i("TeachersApp", "LearnerCreateDialogFragment - onDismiss");
    }


    //закрытие диалога при помощи кнопки назад или нажатия вне окна диалога
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.i("TeachersApp", "CreateLearnerDialogFragment - onCancel");
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

interface CreateLearnerInterface {
    void createLearner(String lastName, String name, String comment);
}


