package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;

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
        dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_button_close).setOnClickListener(view -> {
            dismiss();

            //вызываем в активности метод по обновлению таблицы
            ((UpdateTableInterface) getActivity()).allowUserEditLearners();
        });

        // текстовое поле фамилии
        final EditText editSurname = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_edit_second_name);
        // входные данные предыдущая фамилия
        editSurname.setText(getArguments().getString(ARGS_LEARNER_LAST_NAME));


        // текстовое поле имени
        final EditText editName = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_edit_name);
        // входные данные предыдущее имя
        editName.setText(getArguments().getString(ARGS_LEARNER_NAME));


        // текстовое поле комментария
        final EditText editComment = dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_edit_comment);
        // входные данные предыдущий комментарий
        editComment.setText(getArguments().getString(ARGS_LEARNER_COMMENT));
        editComment.setFilters(new InputFilter[]{new InputFilter.LengthFilter(
                SharedPrefsContract.PREMIUM_PARAM_LEARNER_MAX_COMMENT_LENGTH)});


        // кнопка сохранения
        dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editSurname.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), R.string.learners_and_grades_out_activity_dialog_toast_no_last_name, Toast.LENGTH_SHORT).show();
                } else {

                    //вызываем в активности метод по созданию ученика и передаем ей имя и фамилию
                    ((EditLearnerDialogInterface) getActivity()).editLearner(
                            editSurname.getText().toString().trim(),
                            editName.getText().toString().trim(),
                            editComment.getText().toString().trim()
                    );
                    dismiss();
                }
            }
        });

        // кнопка удаления
        dialogView.findViewById(R.id.learners_and_grades_dialog_learner_edit_button_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //вызываем в активности метод по далению ученика и передаем id
                ((EditLearnerDialogInterface) getActivity()).removeLearner();

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

        //вызываем в активности метод по обновлению таблицы
        ((UpdateTableInterface) getActivity()).allowUserEditLearners();
    }
}

interface EditLearnerDialogInterface {
    void editLearner(String lastName, String name, String comment);

    void removeLearner();
}

