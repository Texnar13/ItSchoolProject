package com.learning.texnar13.teachersprogect.lesson.lessonList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.learning.texnar13.teachersprogect.R;

public class LessonListEditDialogFragment extends DialogFragment {//входные данные позиция в столбце, позиция в строке

    //передаваемые данные
    public static final String LEARNER_NUMBER = "learnerNumber";
    public static final String GRADE_NUMBER = "gradeNumber";
    public static final String GRADE = "grade";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i("TeachersApp", "LessonListEditDialogFragment - onCreateDialog");
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Редактирование оценки");

//---layout диалога---
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_learner, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_learner_dialog_fragment_linear_layout);

//---spinner с выбором оценки---
        //массив с текстами оценок
        String gradesText[] = {"Н", "нет оценки", "1", "2", "3", "4", "5"};
        //адаптер
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(),
                R.layout.spiner_dropdown_element_learners_and_grades_subjects,
                gradesText
        );
        //спинер
        final Spinner spinner = new Spinner(getActivity().getApplicationContext());
        //ставим адаптер
        spinner.setAdapter(arrayAdapter);
        //выводим спиннер
        linearLayout.addView(
                spinner,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        try {//входные данные выбранная оценка
            switch (getArguments().getInt(GRADE)) {
                case -2:
                    spinner.setSelection(0, false);
                    break;
                case 0:
                    spinner.setSelection(1, false);
                    break;
                case 1:
                    spinner.setSelection(2, false);
                    break;
                case 2:
                    spinner.setSelection(3, false);
                    break;
                case 3:
                    spinner.setSelection(4, false);
                    break;
                case 4:
                    spinner.setSelection(5, false);
                    break;
                case 5:
                    spinner.setSelection(6, false);
                    break;
                default:
                    Log.i("TeachersApp", "yourGradeIsDefault");
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"" + GRADE + "\"");
        }


//----кнопка сохранения изменений----
        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    int tempI;
                    switch (spinner.getSelectedItemPosition()) {
                        case 0:
                            tempI = -2;
                            break;
                        case 1:
                            tempI = 0;
                            break;
                        case 2:
                            tempI = 1;
                            break;
                        case 3:
                            tempI = 2;
                            break;
                        case 4:
                            tempI = 3;
                            break;
                        case 5:
                            tempI = 4;
                            break;
                        case 6:
                            tempI = 5;
                            break;
                        default:
                            tempI = 0;
                            break;
                    }

                    //вызываем в активности метод по изменению оценки и передаем выбранную оценку
                    ((com.learning.texnar13.teachersprogect.lesson.lessonList.EditGradeDialogInterface) getActivity()).editGrade(
                            getArguments().getInt(LEARNER_NUMBER),
                            getArguments().getInt(GRADE_NUMBER),
                            tempI);
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditGradeDialogInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "LessonListEditDialogFragment: you must implements EditGradeDialogInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать оценку и позиции( Bungle putLong("grade",grade) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "LessonListEditDialogFragment: you must give grade or position( Bungle putLong(\""+GRADE+"\",grade) )"
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
//        builder.setNeutralButton("удалить", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
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
//            }
//        });

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

interface EditGradeDialogInterface {
    void editGrade(int learnerNumber, int gradeNumber, int grade);
}