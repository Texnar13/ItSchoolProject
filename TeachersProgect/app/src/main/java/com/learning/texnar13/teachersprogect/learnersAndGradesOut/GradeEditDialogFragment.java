package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class GradeEditDialogFragment extends DialogFragment {//входные данные оценка, id оценки

    // переменные константы позволяющие получить данные через intent
    public static final String ARGS_LEARNER_NAME = "name";
    public static final String ARGS_STRING_GRADES_TYPES_ARRAY = "gradesTypes";
    public static final String ARGS_INT_GRADES_ARRAY = "grades";
    public static final String ARGS_INT_MAX_GRADE = "maxGrade";
    public static final String ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY = "chosenTypes";
    public static final String ARGS_STRING_CURRENT_DATE = "currentDate";
    public static final String ARGS_INT_LESSON_NUMBER = "lessonNumber";


    // массив позиций выбранных оценок
    int[] grades;
    // массив позиций выбранных типов
    int[] chosenTypes;

    // номер выбранного урока
    int lessonPosition;

    // массив спиннеров с оценками
    Spinner[] gradesSpinners;
    // массив спиннеров с типами оценок
    Spinner[] typesSpinners;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i("TeachersApp", "LessonListEditDialogFragment - onCreateDialog");

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        //layout диалога
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) getResources().getDimension(R.dimen.simple_margin), (int) getResources().getDimension(R.dimen.half_more_margin), (int) getResources().getDimension(R.dimen.simple_margin), (int) getResources().getDimension(R.dimen.half_more_margin));
        linearLayout.setLayoutParams(linearLayoutParams);
        builder.setView(linearLayout);


        // получаем данные из intent

        // имя редактируемого ученика
        String learnerName = getArguments().getString(ARGS_LEARNER_NAME);

        // выбранные номера типов оценок
        chosenTypes = getArguments().getIntArray(ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY);

        // оценки
        grades = getArguments().getIntArray(ARGS_INT_GRADES_ARRAY);
        // если есть Н ставим их везде
        for (int gradeI = 0; gradeI < grades.length; gradeI++) {
            if (grades[gradeI] == -2) {
                for (int gradeI2 = 0; gradeI2 < grades.length; gradeI2++) {
                    grades[gradeI2] = -2;
                    chosenTypes[gradeI] = -2;
                }
                break;
            }
        }


        // создаем массив с оценками в текстовом виде
        final int maxGrade = getArguments().getInt(ARGS_INT_MAX_GRADE, 0);
        final String[][] gradesString = new String[grades.length][];
        // массив для хранения дополнительных оценок
        final int[] dopGrades = new int[grades.length];
        for (int gradeNumberI = 0; gradeNumberI < gradesString.length; gradeNumberI++) {
            // если вдруг оценка в поле больше максимальной
            if (grades[gradeNumberI] > maxGrade) {
                // создаем дополнительное поле
                gradesString[gradeNumberI] = new String[maxGrade + 2];
                // и интициализируем его дополнительной оценкой
                gradesString[gradeNumberI][maxGrade + 1] = grades[gradeNumberI]+" ";

                dopGrades[gradeNumberI] = grades[gradeNumberI];
            } else
                gradesString[gradeNumberI] = new String[maxGrade + 1];

            // инициализируем оценки от прочерка до максимальной
            gradesString[gradeNumberI][0] = "- ";
            for (int gradeI = 1; gradeI < maxGrade + 1; gradeI++) {
                gradesString[gradeNumberI][gradeI] = "" + gradeI+" ";
            }
        }


        // названия типов оценок
        final String[] gradesTypesNames = getArguments().getStringArray(ARGS_STRING_GRADES_TYPES_ARRAY);

        // контейнер текущей даты и номера урока
        RelativeLayout currentContainer = new RelativeLayout(getActivity());
        currentContainer.setBackgroundResource(R.drawable._dialog_head_background_dark);
        LinearLayout.LayoutParams currentContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayout.addView(currentContainer, currentContainerParams);

        // текст текущей даты
        TextView dateText = new TextView(getActivity());
        dateText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        dateText.setTextColor(Color.BLACK);
        dateText.setAllCaps(true);
        dateText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        dateText.setText(getArguments().getString(ARGS_STRING_CURRENT_DATE));
        RelativeLayout.LayoutParams dateTextParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dateTextParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        dateTextParams.addRule(RelativeLayout.CENTER_VERTICAL);
        dateTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        currentContainer.addView(dateText, dateTextParams);

        // спиннер номера урока
        Spinner lessonNumberSpinner = new Spinner(getActivity());
        RelativeLayout.LayoutParams lessonNumberSpinnerParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lessonNumberSpinnerParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        lessonNumberSpinnerParams.addRule(RelativeLayout.CENTER_VERTICAL);
        lessonNumberSpinnerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        currentContainer.addView(lessonNumberSpinner, lessonNumberSpinnerParams);
        // адаптер спиннера
        String[] lessonsTexts = new String[9];
        for (int lessonsI = 0; lessonsI < lessonsTexts.length; lessonsI++) {
            lessonsTexts[lessonsI] = (lessonsI + 1) + " " + getResources().getString(R.string.learners_and_grades_out_activity_dialog_title_lesson);
        }
        lessonNumberSpinner.setAdapter(new ArrayAdapter<>(
                getActivity(),
                R.layout.spinner_dropdown_element_subtitle,
                lessonsTexts
        ));
        // получаем выбранный номер урока
        lessonPosition = getArguments().getInt(ARGS_INT_LESSON_NUMBER);
        // выставляем его в спиннер
        lessonNumberSpinner.setSelection(lessonPosition, false);
        // при выборе номера урока
        lessonNumberSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                lessonPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // контейнер для имени и поля с отсутствием
        LinearLayout titleLayout = new LinearLayout(getActivity());
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.setBackgroundResource(R.drawable._dialog_body_background_white_underlined);
        linearLayout.addView(titleLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleLayout.setWeightSum(2);

        // имя ученика
        TextView name = new TextView(getActivity());
        name.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        name.setSingleLine(true);
        name.setText(learnerName);
        name.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        name.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        nameParams.setMargins((int) getResources().getDimension(R.dimen.simple_margin), 0, (int) getResources().getDimension(R.dimen.simple_margin), 0);
        titleLayout.addView(name, nameParams);


        // контейнер отсутствия
        LinearLayout absContainer = new LinearLayout(getActivity());
        absContainer.setWeightSum(2);
        absContainer.setOrientation(LinearLayout.HORIZONTAL);
        absContainer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams absContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        absContainerParams.setMargins((int) getResources().getDimension(R.dimen.simple_margin), 0, (int) getResources().getDimension(R.dimen.simple_margin), 0);
        titleLayout.addView(absContainer, absContainerParams);

        // чекбокс отсутствия
        final ImageView checkImage = new ImageView(getActivity());
        if (grades[0] == -2) {
            checkImage.setImageResource(R.drawable.__checkbox_full);
        } else
            checkImage.setImageResource(R.drawable.__checkbox_empty);
        LinearLayout.LayoutParams checkImageParams = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.my_icon_small_size),
                (int) getResources().getDimension(R.dimen.my_icon_small_size));
        checkImageParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        checkImageParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        checkImageParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        absContainer.addView(checkImage, checkImageParams);

        // текст отсутствия
        TextView absText = new TextView(getActivity());
        absText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        absText.setText(R.string.lesson_activity_learner_absent_text);
        absText.setTextColor(Color.BLACK);
        absText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams absTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        absTextParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        absTextParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        absContainer.addView(absText, absTextParams);

        // нажатие на контейнер отсутствия
        absContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // если чекбокс был активен, дизактивируем
                if (grades[0] == -2) {
                    checkImage.setImageResource(R.drawable.__checkbox_empty);
                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                        // ставим позиции в массивы
                        grades[gradeI] = 0;
                        chosenTypes[gradeI] = 0;

                        // ставим выбор на первых позициях в спиннерах и адаптеры с оценками и типами
                        gradesSpinners[gradeI].setSelection(0, false);
                        gradesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.spinner_dropdown_element_subtitle,
                                gradesString[gradeI]
                        ));

                        typesSpinners[gradeI].setSelection(0, false);
                        typesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.spinner_dropdown_element_subtitle,
                                gradesTypesNames
                        ));
                    }

                } else {// иначе активируем его
                    checkImage.setImageResource(R.drawable.__checkbox_full);
                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                        // ставим Н вместо оценок
                        grades[gradeI] = -2;
                        chosenTypes[gradeI] = -2;
                        // и дизактивируем спиннеры
                        gradesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.spinner_dropdown_element_subtitle,
                                new String[]{" "}
                        ));
                        typesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.spinner_dropdown_element_subtitle,
                                new String[]{" "}
                        ));
                    }


                }
            }
        });


        // ---- контейнер для тела диалога ----
        ScrollView bodyLayout = new ScrollView(getActivity());
        //
        bodyLayout.setBackgroundResource(R.drawable._dialog_bottom_background_white);
        //bodyLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(bodyLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // контейнер со спиннерами
        LinearLayout spinnersContainer = new LinearLayout(getActivity());
        spinnersContainer.setOrientation(LinearLayout.VERTICAL);
        spinnersContainer.setGravity(Gravity.CENTER);
        spinnersContainer.setWeightSum(5);
        ScrollView.LayoutParams spinnersContainerParams = new ScrollView.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnersContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        bodyLayout.addView(spinnersContainer, spinnersContainerParams);

        // добавляем спиннеры
        gradesSpinners = new Spinner[grades.length];
        typesSpinners = new Spinner[grades.length];
        for (int gradeI = 0; gradeI < grades.length; gradeI++) {
            // создаем горизонтальный контейнер под спиннеры одной оценки
            LinearLayout gradeContainer = new LinearLayout(getActivity());
            gradeContainer.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams gradeContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            //gradeContainerParams.setMargins((int) getResources().getDimension(R.dimen.simple_margin), (int) getResources().getDimension(R.dimen.simple_margin), (int) getResources().getDimension(R.dimen.simple_margin), (int) getResources().getDimension(R.dimen.simple_margin));
            spinnersContainer.addView(gradeContainer, gradeContainerParams);

            // спиннер с оценкой
            gradesSpinners[gradeI] = new Spinner(getActivity());
            // ставим адаптер и выбор спиннеру в соответствии с текущей оценкой
            if (grades[gradeI] >= 0) {
                // адаптер спиннера
                gradesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                        getActivity(),
                        R.layout.spinner_dropdown_element_subtitle,
                        gradesString[gradeI]
                ));
                // ставим выбор
                if (grades[gradeI] <= maxGrade) {
                    gradesSpinners[gradeI].setSelection(grades[gradeI]);
                } else {
                    gradesSpinners[gradeI].setSelection(maxGrade+1);
                }
            } else {
                // адаптер спиннера
                gradesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                        getActivity(),
                        R.layout.spinner_dropdown_element_subtitle,
                        new String[]{" "}
                ));
            } // при выборе элемента из спиннера
            final int finalGradeI = gradeI;
            gradesSpinners[gradeI].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // если не Н
                    if (grades[0] >= 0)
                        if (position <= maxGrade) {
                            grades[finalGradeI] = position;
                        } else {
                            grades[finalGradeI] = dopGrades[finalGradeI];
                        }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            gradeContainer.addView(gradesSpinners[gradeI], LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);


            // спиннер с типом ответа
            typesSpinners[gradeI] = new Spinner(getActivity());
            // ставим адаптер и выбор спиннеру в соответствии с текущей оценкой
            if (grades[gradeI] >= 0) {
                // адаптер спиннера
                typesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                        getActivity(),
                        R.layout.spinner_dropdown_element_subtitle,
                        gradesTypesNames
                ));
                // ставим выбор
                typesSpinners[gradeI].setSelection(chosenTypes[gradeI]);
            } else {
                // адаптер спиннера
                typesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                        getActivity(),
                        R.layout.spinner_dropdown_element_subtitle,
                        new String[]{" "}
                ));
            }
            // при выборе элемента из спиннера
            typesSpinners[gradeI].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // если не Н
                    if (grades[0] >= 0)
                        chosenTypes[finalGradeI] = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            gradeContainer.addView(typesSpinners[gradeI], LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }


        // добавляем кнопку сохранить/ок

        // контейнер кнопки
        LinearLayout saveButtonContainer = new LinearLayout(getActivity());
        saveButtonContainer.setGravity(Gravity.CENTER);
        saveButtonContainer.setBackgroundResource(R.drawable._button_round_background_green);
        LinearLayout.LayoutParams saveButtonContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        );
        saveButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        saveButtonContainerParams.gravity = Gravity.CENTER;
        spinnersContainer.addView(saveButtonContainer, saveButtonContainerParams);

        TextView saveText = new TextView(getActivity());
        saveText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        saveText.setText(R.string.learners_and_grades_out_activity_dialog_button_save);
        saveText.setTextColor(getResources().getColor(R.color.backgroundWhite));
        saveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams saveTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        saveTextParams.leftMargin = (int) getResources().getDimension(R.dimen.double_margin);
        saveTextParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        saveTextParams.rightMargin = (int) getResources().getDimension(R.dimen.double_margin);
        saveTextParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        saveButtonContainer.addView(saveText, saveTextParams);

        // при нажатии на кнопку сохранить
        saveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // преобразуем {-2,-2,-2} в {-2,0,0}
                if (grades[0] == -2) {
                    chosenTypes[0] = 0;
                    for (int gradeI = 1; gradeI < grades.length; gradeI++) {
                        grades[gradeI] = 0;
                        chosenTypes[gradeI] = 0;
                    }
                }
                // возвращаем измененные оценки в активность
                ((EditGradeDialogInterface) getActivity()).editGrades(grades, chosenTypes, lessonPosition);

                // закрываем диалог
                dismiss();
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
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        //вызываем в активности метод разрешения изменения оценок
        ((EditGradeDialogInterface) getActivity()).gradesSelectNothing();

    }
}

interface EditGradeDialogInterface {
    void editGrades(int[] grades, int[] chosenTypesNumbers, int lessonPoz);

    void gradesSelectNothing();
}


/*
*
public class EditGradeDialogFragment extends DialogFragment {//входные данные оценка, id оценки

    //передаваемые данные
    public static final String GRADES = "grades";

    public static final String INDEXES = "indexes";

    // максимальная оценка из базы данных
    int maxGrade;
    // массив с выбранными оценками
    int[] grades;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i("TeachersApp", "LessonListEditDialogFragment - onCreateDialog");
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
            String gradesText[];

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
                            finalGrades,
                            getArguments().getIntArray(INDEXES)
                    );

                    // убираем с текста краску
                    ((EditGradeDialogInterface) getActivity()).returnSimpleColorForText(getArguments().getIntArray(INDEXES));
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
                            "EditGradeDialogFragment: you must give grade or id( Bungle putLongArray(\"" + INDEXES + "\",indexes[]) )"
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
                    // убираем с текста краску
                    ((EditGradeDialogInterface) getActivity()).returnSimpleColorForText(getArguments().getIntArray(INDEXES));
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс AllowUserEditGradesInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditGradeDialogFragment: you must implements AllowUserEditGradesInterface in your activity"
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
            // убираем с текста краску
            ((EditGradeDialogInterface) getActivity()).returnSimpleColorForText(getArguments().getIntArray(INDEXES));
        } catch (java.lang.ClassCastException e) {
            //в вызвающей активности должен быть имплементирован класс AllowUserEditGradesInterface
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "EditGradeDialogFragment: you must implements AllowUserEditGradesInterface in your activity"
            );
        }
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface EditGradeDialogInterface {
    void editGrade(int[] grades, int[] indexes);

    void returnSimpleColorForText(int[] indexes);
}

interface AllowUserEditGradesInterface {
    void allowUserEditGrades();
}

*
* */