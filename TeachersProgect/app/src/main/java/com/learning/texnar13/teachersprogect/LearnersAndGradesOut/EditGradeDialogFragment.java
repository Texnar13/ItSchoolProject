package com.learning.texnar13.teachersprogect.LearnersAndGradesOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class EditGradeDialogFragment extends DialogFragment {//входные данные оценка, id оценки

    //передаваемые данные
    public static final String GRADES_ID = "gradesId";
    public static final String LEARNER_ID= "learnerId";
    public static final String GRADES = "grades";
    public static final String SUBJECT_ID = "subjectId";
    public static final String DATE = "date";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i("TeachersApp", "LessonListEditDialogFragment - onCreateDialog");
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        //заголовок
//        builder.setTitle("Редактирование оценки");

//---layout диалога---
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_learner, null);
        builder.setView(dialogLayout);



        //--LinearLayout в layout файле--
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_learner_dialog_fragment_linear_layout);
        linearLayout.setBackgroundResource(R.color.colorBackGround);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
        linearLayout.setLayoutParams(linearLayoutParams);

//--заголовок--
        TextView title = new TextView(getActivity());
        title.setText(R.string.learners_and_grades_out_activity_dialog_title_edit_grade);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        title.setAllCaps(true);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

        linearLayout.addView(title, titleParams);

//---получаем входные данные с массивами---
        int[] grades = {};
        long[] gradesId = {};
        try {
            grades = getArguments().getIntArray(GRADES);
            gradesId = getArguments().getLongArray(GRADES_ID);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"" + GRADES + "\"");
        }

//---spinner-ы с выбором оценки---
        //массив с текстами оценок
        String gradesText[] = {"Н", "нет оценки", "1", "2", "3", "4", "5"};
        //адаптер
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(),
                R.layout.spiner_dropdown_element_learners_and_grades_subjects,
                gradesText
        );
        //массив со спиннерами
        final Spinner[] spinners = new Spinner[grades.length];

        //инициализируем
        for (int i = 0; i < spinners.length; i++) {
            //спинер
            spinners[i] = new Spinner(getActivity().getApplicationContext());
            //ставим адаптер
            spinners[i].setAdapter(arrayAdapter);
            //выводим спиннер
            linearLayout.addView(
                    spinners[i],
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            // ставим в спиннеры выбранные оценки

            switch (grades[i]) {
                case 0:
                    spinners[i].setSelection(1, false);
                    break;
                case -2:
                    spinners[i].setSelection(0, false);
                    break;
                default:
                    spinners[i].setSelection(grades[i] + 1, false);
                    break;
            }

        }

//--кнопки согласия/отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.learners_classes_out_activity_dialog_button_cancel);
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка согласия
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        positiveButton.setText(R.string.learners_classes_out_activity_dialog_button_save);
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        positiveButtonParams.weight = 1;
        positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));


        //кнопки в контейнер
        container.addView(neutralButton, neutralButtonParams);
        container.addView(positiveButton, positiveButtonParams);

        //контейнер в диалог
        linearLayout.addView(container);


        //при нажатии...
        //согласие

        //передаваемые массивы
        final int[] finalGrades = grades;
        final long[] finalGradesId = gradesId;
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    for (int j = 0; j < spinners.length; j++) {

                        switch (spinners[j].getSelectedItemPosition()) {
                            case 0:
                                finalGrades[j] = -2;
                                break;
                            default:
                                finalGrades[j] = spinners[j].getSelectedItemPosition() - 1;
                                break;
                        }
                    }

                    Log.e(""+getArguments().getStringArray(DATE)[0],"---");
                    //вызываем в активности метод по изменению оценки и передаем выбранную оценку
                    ((EditGradeDialogInterface) getActivity()).editGrade(
                            finalGradesId,
                            getArguments().getLong(LEARNER_ID),
                            finalGrades,
                            getArguments().getLong(SUBJECT_ID),
                            getArguments().getStringArray(DATE)
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditGradeDialogInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditGradeDialogFragment: you must implements EditGradeDialogInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать оценку и позиции( Bungle putLong("grade",grade) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditGradeDialogFragment: you must give grade or id( Bungle putLong(\"" + GRADES + "\",grades[]) )"
                    );
                }
                dismiss();
            }
        });

        //отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });





//        //LinearLayout в layout файле
//        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_learner_dialog_fragment_linear_layout);

//---получаем входные данные с массивами---
//        int[] grades = {};
//        long[] gradesId = {};
//        try {
//            grades = getArguments().getIntArray(GRADES);
//            gradesId = getArguments().getLongArray(GRADES_ID);
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//            Log.i("TeachersApp", "you must give bundle argument \"" + GRADES + "\"");
//        }
//
//
////---spinner-ы с выбором оценки---
//        //массив с текстами оценок
//        String gradesText[] = {"Н", "нет оценки", "1", "2", "3", "4", "5"};
//        //адаптер
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
//                getActivity().getApplicationContext(),
//                R.layout.spiner_dropdown_element_learners_and_grades_subjects,
//                gradesText
//        );
//        //массив со спиннерами
//        final Spinner[] spinners = new Spinner[grades.length];
//
//        //инициализируем
//        for (int i = 0; i < spinners.length; i++) {
//            //спинер
//            spinners[i] = new Spinner(getActivity().getApplicationContext());
//            //ставим адаптер
//            spinners[i].setAdapter(arrayAdapter);
//            //выводим спиннер
//            linearLayout.addView(
//                    spinners[i],
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            );
//
//            // ставим в спиннеры выбранные оценки
//
//            switch (grades[i]) {
//                case 0:
//                    spinners[i].setSelection(1, false);
//                    break;
//                case -2:
//                    spinners[i].setSelection(0, false);
//                    break;
//                default:
//                    spinners[i].setSelection(grades[i] + 1, false);
//                    break;
//            }
//
//        }
//
//
////----кнопка сохранения изменений----
//        //передаваемые массивы
//        final int[] finalGrades = grades;
//        final long[] finalGradesId = gradesId;
//        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                try {
//                    for (int j = 0; j < spinners.length; j++) {
//
//                        switch (spinners[j].getSelectedItemPosition()) {
//                            case 0:
//                                finalGrades[j] = -2;
//                                break;
//                            default:
//                                finalGrades[j] = spinners[j].getSelectedItemPosition() - 1;
//                                break;
//                        }
//                    }
//
//                    Log.e(""+getArguments().getStringArray(DATE)[0],"---");
//                    //вызываем в активности метод по изменению оценки и передаем выбранную оценку
//                    ((EditGradeDialogInterface) getActivity()).editGrade(
//                            finalGradesId,
//                            getArguments().getLong(LEARNER_ID),
//                            finalGrades,
//                            getArguments().getLong(SUBJECT_ID),
//                            getArguments().getStringArray(DATE)
//                    );
//                } catch (java.lang.ClassCastException e) {
//                    //в вызвающей активности должен быть имплементирован класс EditGradeDialogInterface
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditGradeDialogFragment: you must implements EditGradeDialogInterface in your activity"
//                    );
//                } catch (java.lang.NullPointerException e) {
//                    //в диалог необходимо передать оценку и позиции( Bungle putLong("grade",grade) )
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditGradeDialogFragment: you must give grade or id( Bungle putLong(\"" + GRADES + "\",grades[]) )"
//                    );
//                }
//            }
//        });
//        //просто выход из диалога
//        builder.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
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

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface EditGradeDialogInterface {
    void editGrade(long[] gradesId,long learnerId, int[] grades, long subjectId, String [] dates);
}
