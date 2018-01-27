package com.learning.texnar13.teachersprogect.lesson.lessonList;

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

public class LessonListEditDialogFragment extends DialogFragment {//входные данные позиция в столбце, позиция в строке


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Редактирование ученика");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_learner, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_learner_dialog_fragment_linear_layout);



        //кнопка сохранения изменений
        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                try {
//                    //вызываем в активности метод по созданию ученика и передаем ей имя и фамилию
//                    ((com.learning.texnar13.teachersprogect.LearnersAndGradesOut.EditLearnerDialogInterface) getActivity()).editLearner(
//                            editLastName.getText().toString(),
//                            editName.getText().toString(),
//                            getArguments().getLong("learnerId")
//                    );
//                } catch (java.lang.ClassCastException e) {
//                    //в вызвающей активности должен быть имплементирован класс EditLearnerInterface
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditLearnerDialogFragment: you must implements EditLearnerInterface in your activity"
//                    );
//                } catch (java.lang.NullPointerException e) {
//                    //в диалог необходимо передать id ученика( Bungle putLong("learnerId",learnerId) )
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditLearnerDialogFragment: you must give learnerId( Bungle putLong(\"learnerId\",learnerId) )"
//                    );
//                }
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
//                try {
//                    //вызываем в активности метод по далению ученика и передаем id
//                    ((com.learning.texnar13.teachersprogect.LearnersAndGradesOut.EditLearnerDialogInterface) getActivity()).removeLearner(
//                            getArguments().getLong("learnerId")
//                    );
//                } catch (java.lang.ClassCastException e) {
//                    //в вызвающей активности должен быть имплементирован класс EditLearnerInterface
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditLearnerDialogFragment: you must implements EditLearnerInterface in your activity"
//                    );
//                } catch (java.lang.NullPointerException e) {
//                    //в диалог необходимо передать id ученика( Bungle putLong("learnerId",learnerId) )
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditLearnerDialogFragment: you must give learnerId( Bungle putLong(\"learnerId\",learnerId) )"
//                    );
//                }
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