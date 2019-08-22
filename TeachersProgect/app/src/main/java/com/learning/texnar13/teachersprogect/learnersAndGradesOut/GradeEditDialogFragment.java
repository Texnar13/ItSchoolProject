package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;

public class GradeEditDialogFragment extends DialogFragment {//входные данные оценка, id оценки

    //передаваемые данные
    public static final String GRADES = "grades";

    // максимальная оценка из базы данных
    int maxGrade;
    // массив с выбранными оценками
    int[] grades;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i("TeachersApp", "LessonListEditDialogFragment - onCreateDialog");
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // ---layout диалога ---
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundResource(R.color.colorBackGround);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
        linearLayout.setLayoutParams(linearLayoutParams);
        builder.setView(linearLayout);

//--заголовок--
        TextView title = new TextView(getActivity());
        title.setText(R.string.learners_and_grades_out_activity_dialog_title_edit_grade);
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        title.setAllCaps(true);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

        linearLayout.addView(title, titleParams);

// -------- получаем оценки в массиве --------
        grades = getArguments().getIntArray(GRADES);// todo для шлифовки кода можно убрать эту строчку(или отправить null) и исправить ошибки если они вдруг появятся
        if (grades == null) {
            grades = new int[]{};
            Log.i("TeachersApp", "you must give bundle argument \"" + GRADES + "\"");
        }

// -------- spinner-ы с выбором оценки --------
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
        maxGrade = db.getSettingsMaxGrade(1);
        // массив со спиннерами
        final Spinner[] spinners = new Spinner[grades.length];

// -------- инициализируем значения в спиннерах --------
        for (int i = 0; i < spinners.length; i++) {
            // создаем массив с текстами
            String[] gradesText;

            // если вдруг оценка в поле больше максимальной
            if (grades[i] > maxGrade) {
                // создаем дополнительное поле
                gradesText = new String[maxGrade + 3];
                // и интициализируем его дополнительной оценкой
                gradesText[maxGrade + 2] = "" + grades[i];
            } else
                gradesText = new String[maxGrade + 2];

            // инициализируем первые два поля
            gradesText[0] = getString(R.string.learners_and_grades_out_activity_title_grade_n);
            gradesText[1] = getString(R.string.learners_and_grades_out_activity_title_grade_no_answers);
            // инициализируем все оценки кроме дополнительной
            for (int j = 2; j < maxGrade + 2; j++) {
                gradesText[j] = "" + (j - 1);
            }

            // нициализируем спинер
            spinners[i] = new Spinner(getActivity().getApplicationContext());
            // ставим адаптер
            spinners[i].setAdapter(
                    new ArrayAdapter<>(
                            getActivity().getApplicationContext(),
                            R.layout.spinner_dropdown_element_learners_and_grades_answers,
                            gradesText
                    )
            );
            // выводим спиннер
            linearLayout.addView(
                    spinners[i],
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            // выбираем нужный элемент спиннера
            switch (grades[i]) {
                case 0:
                    spinners[i].setSelection(1, false);
                    break;
                case -2:
                    spinners[i].setSelection(0, false);
                    break;
                default:
                    if (grades[i] > maxGrade) {
                        spinners[i].setSelection(maxGrade + 2, false);
                    } else
                        spinners[i].setSelection(grades[i] + 1, false);
                    break;
            }
        }

//--кнопки согласия/отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.learners_classes_out_activity_dialog_button_cancel);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.my_buttons_height_size)
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка согласия
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        positiveButton.setText(R.string.learners_classes_out_activity_dialog_button_save);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.my_buttons_height_size)
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
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    for (int j = 0; j < spinners.length; j++) {
                        switch (spinners[j].getSelectedItemPosition()) {
                            case 0://нулевая позиция - н
                                finalGrades[j] = -2;
                                break;
                            default://остальные оценки
                                if (maxGrade + 2 == spinners[j].getSelectedItemPosition()) {
                                    finalGrades[j] = grades[j];
                                } else
                                    finalGrades[j] = spinners[j].getSelectedItemPosition() - 1;
                                break;
                        }
                    }

                    //вызываем в активности метод по изменению оценки и передаем выбранную оценку
                    ((EditGradeDialogInterface) getActivity()).editGrade(
                            finalGrades
                    );

//                    // убираем с текста краску
//                    ((EditGradeDialogInterface) getActivity()).returnSimpleColorForCellBackground(getArguments().getIntArray(INDEXES));
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditGradeDialogInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "GradeEditDialogFragment: you must implements EditGradeDialogInterface in your activity"
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
                try {
                    //вызываем в активности метод разрешения изменения оценок
                    ((AllowUserEditGradesInterface) getActivity()).allowUserEditGrades();
//                    // убираем с текста краску
//                    ((EditGradeDialogInterface) getActivity()).returnSimpleColorForCellBackground(getArguments().getIntArray(INDEXES));
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс AllowUserEditGradesInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "GradeEditDialogFragment: you must implements AllowUserEditGradesInterface in your activity"
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
        try {
            //вызываем в активности метод разрешения изменения оценок
            ((AllowUserEditGradesInterface) getActivity()).allowUserEditGrades();
//            // убираем с текста краску
//            ((EditGradeDialogInterface) getActivity()).returnSimpleColorForCellBackground(getArguments().getIntArray(INDEXES));
        } catch (java.lang.ClassCastException e) {
            //в вызвающей активности должен быть имплементирован класс AllowUserEditGradesInterface
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "GradeEditDialogFragment: you must implements AllowUserEditGradesInterface in your activity"
            );
        }
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface EditGradeDialogInterface {
    void editGrade(int[] grades);
}

interface AllowUserEditGradesInterface {
    void allowUserEditGrades();
}
