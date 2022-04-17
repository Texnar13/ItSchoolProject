package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.AlertDialog;
import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
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
    static final String ARGS_LEARNER_NAME = "name";
    static final String ARGS_STRING_GRADES_TYPES_ARRAY = "gradesTypes";
    static final String ARGS_INT_GRADES_ARRAY = "grades";
    static final String ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY = "chosenTypes";
    static final String ARGS_INT_GRADES_ABSENT_TYPE_NUMBER = "chosenAbsTypeNumber";
    static final String ARGS_STRING_ABSENT_TYPES_NAMES_ARRAY = "absentTypeNameArray";
    static final String ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY = "absentTypeLongNameArray";
    static final String ARGS_INT_MAX_GRADE = "maxGrade";
    static final String ARGS_INT_MAX_LESSONS_COUNT = "maxLessonsCount";
    static final String ARGS_STRING_CURRENT_DATE = "currentDate";
    static final String ARGS_INT_LESSON_NUMBER = "lessonNumber";


    // массив позиций выбранных оценок
    private int[] grades;
    // массив позиций выбранных типов
    private int[] chosenTypes;
    // выбранный тип отсутствия
    private int absTypePoz;

    // номер выбранного урока
    private int lessonPosition;

    // массив спиннеров с оценками
    private Spinner[] gradesSpinners;
    // массив спиннеров с типами оценок
    private Spinner[] typesSpinners;

    // максимальная оценка
    int maxGrade;


    // чекбокс отсутствия
    ImageView checkImage;
    // спиннер отсутствия
    Spinner finalAbsSpinner;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


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
        grades = getArguments().getIntArray(ARGS_INT_GRADES_ARRAY);// todo для шлифовки кода можно убрать эту строчку(или отправить null) и исправить ошибки если они вдруг появятся
        // получаем выбранный номер типа пропуска
        absTypePoz = getArguments().getInt(ARGS_INT_GRADES_ABSENT_TYPE_NUMBER);
        // названия типов оценок
        final String[] gradesTypesNames = getArguments().getStringArray(ARGS_STRING_GRADES_TYPES_ARRAY);
        // названия типов пропусков
        //final String[] absentTypesNames = getArguments().getStringArray(ARGS_STRING_ABSENT_TYPES_NAMES_ARRAY);
        final String[] absentTypesLongNames = getArguments().getStringArray(ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY);
        // максимальная оценка
        maxGrade = getArguments().getInt(ARGS_INT_MAX_GRADE, 0);
        // максимальное количество уроков
        int maxLessonsCount = getArguments().getInt(ARGS_INT_MAX_LESSONS_COUNT, 1);



        // если оценок нет, то проставляем типы по умолчанию
        if (absTypePoz == -1) {
            // первая оценка остается с типом по умолчанию
            // вторая со вторым
            if (grades[1] == 0 && gradesTypesNames.length > 1) {
                chosenTypes[1] = 1;
            }
            // третья с третьим
            if (grades[2] == 0 && gradesTypesNames.length > 2) {
                chosenTypes[2] = 2;
            }
        }


        // создаем массив с оценками в текстовом виде
        final String[][] gradesString = new String[grades.length][];
        // массив для хранения дополнительных оценок
        final int[] dopGrades = new int[grades.length];
        for (int gradeNumberI = 0; gradeNumberI < gradesString.length; gradeNumberI++) {
            // если вдруг оценка в поле больше максимальной
            if (grades[gradeNumberI] > maxGrade) {
                // создаем дополнительное поле
                gradesString[gradeNumberI] = new String[maxGrade + 2];
                // и интициализируем его дополнительной оценкой
                gradesString[gradeNumberI][maxGrade + 1] = grades[gradeNumberI] + " ";

                dopGrades[gradeNumberI] = grades[gradeNumberI];
            } else
                gradesString[gradeNumberI] = new String[maxGrade + 1];

            // инициализируем оценки от прочерка до максимальной
            gradesString[gradeNumberI][0] = "- ";
            for (int gradeI = 1; gradeI < maxGrade + 1; gradeI++) {
                gradesString[gradeNumberI][gradeI] = "" + gradeI + " ";
            }
        }


        // контейнер текущей даты и номера урока
        RelativeLayout currentContainer = new RelativeLayout(getActivity());
        currentContainer.setBackgroundResource(R.drawable.base_background_dialog_head_round_lgray);
        LinearLayout.LayoutParams currentContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayout.addView(currentContainer, currentContainerParams);

        // текст текущей даты
        TextView dateText = new TextView(getActivity());
        dateText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
        dateText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
        dateText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        dateText.setText(getArguments().getString(ARGS_STRING_CURRENT_DATE));
        RelativeLayout.LayoutParams dateTextParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dateTextParams.leftMargin = (int) getResources().getDimension(R.dimen.double_margin);
        dateTextParams.addRule(RelativeLayout.CENTER_VERTICAL);
        dateTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        currentContainer.addView(dateText, dateTextParams);

        // спиннер номера урока
        Spinner lessonNumberSpinner = new Spinner(getActivity());
        RelativeLayout.LayoutParams lessonNumberSpinnerParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lessonNumberSpinnerParams.rightMargin = (int) getResources().getDimension(R.dimen.double_margin);
        lessonNumberSpinnerParams.topMargin = (int) getResources().getDimension(R.dimen.half_margin);
        lessonNumberSpinnerParams.bottomMargin = (int) getResources().getDimension(R.dimen.half_margin);
        lessonNumberSpinnerParams.addRule(RelativeLayout.CENTER_VERTICAL);
        lessonNumberSpinnerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        currentContainer.addView(lessonNumberSpinner, lessonNumberSpinnerParams);
        // адаптер спиннера
        String[] lessonsTexts = new String[maxLessonsCount];
        for (int lessonsI = 0; lessonsI < lessonsTexts.length; lessonsI++) {
            lessonsTexts[lessonsI] = (lessonsI + 1) + " " + getResources().getString(R.string.learners_and_grades_out_activity_dialog_title_lesson);
        }
        ArrayAdapter<String> lessonAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                lessonsTexts
        );
        lessonAdapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
        lessonNumberSpinner.setAdapter(lessonAdapter);
        // получаем выбранный номер урока
        lessonPosition = getArguments().getInt(ARGS_INT_LESSON_NUMBER);
        // выставляем его в спиннер
        lessonNumberSpinner.setSelection(lessonPosition, false);
        // при выборе номера урока (не отрабатывает при запуске)
        lessonNumberSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // если оценки нулевые, обнуляем типы к первому из списка
                for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                    if (grades[gradeI] == 0) {
                        chosenTypes[gradeI] = 0;
                    }
                }
                // сохраняем измененные оценки в активность
                ((EditGradeDialogInterface) getActivity()).editGrades(grades, absTypePoz, chosenTypes, lessonPosition);

                // получаем оценки с другого урока
                GradeUnit temp = ((EditGradeDialogInterface) getActivity()).getLessonGrades(position);
                if (temp != null) {
                    grades = temp.grades;
                    chosenTypes = temp.gradesTypesIndexes;
                    absTypePoz = temp.absTypePoz;
                } else {
                    grades = new int[]{0, 0, 0};
                    chosenTypes = new int[]{0, 0, 0};
                    absTypePoz = -1;
                }


                // если чекбокс пропуска не активен
                if (absTypePoz == -1) {
                    checkImage.setImageResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_empty);

                    // если типов пропуска больше чем 1
                    if (absentTypesLongNames.length > 1) {
                        finalAbsSpinner.setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                                new String[]{" "}
                        ));
                    }

                    // оценки
                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {

                        // адаптер спиннера оценок
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                gradesString[gradeI]
                        );
                        gradesSpinners[gradeI].setAdapter(adapter);
                        adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);

                        // ставим выбор
                        if (grades[gradeI] <= maxGrade) {
                            gradesSpinners[gradeI].setSelection(grades[gradeI]);
                        } else {
                            gradesSpinners[gradeI].setSelection(maxGrade + 1);
                        }


                        // адаптер спиннера типов
                        ArrayAdapter<String> typesSpinnersAdapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                gradesTypesNames
                        );
                        typesSpinnersAdapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
                        typesSpinners[gradeI].setAdapter(typesSpinnersAdapter);
                        // ставим выбор
                        typesSpinners[gradeI].setSelection(chosenTypes[gradeI], false);
                    }

                } else {
                    checkImage.setImageResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_full);

                    // если типов пропуска больше чем 1
                    if (absentTypesLongNames.length > 1) {
                        finalAbsSpinner.setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                                absentTypesLongNames
                        ));
                        finalAbsSpinner.setSelection(absTypePoz, false);
                    }

                    // оценки
                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {

                        // чистим оценки
                        grades[gradeI] = 0;
                        chosenTypes[gradeI] = 0;

                        // и дизактивируем спиннеры
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                new String[]{" "}
                        );
                        gradesSpinners[gradeI].setAdapter(adapter);
                        adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);


                        // адаптер спиннера типов
                        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                new String[]{" "}
                        );
                        typesSpinners[gradeI].setAdapter(adapter2);
                        adapter2.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);

                    }

                }


                // выставляем новую позицию
                lessonPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // контейнер для имени и поля с отсутствием
        LinearLayout titleLayout = new LinearLayout(getActivity());
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        titleLayout.setBackgroundResource(R.color.backgroundLiteGray);
        linearLayout.addView(titleLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleLayout.setWeightSum(2);

        // имя ученика
        TextView name = new TextView(getActivity());
        name.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
        name.setSingleLine(true);
        name.setText(learnerName);
        name.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        name.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        nameParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                0,
                (int) getResources().getDimension(R.dimen.double_margin),
                0
        );
        titleLayout.addView(name, nameParams);


        // контейнер отсутствия
        RelativeLayout absContainer = new RelativeLayout(getActivity());
        absContainer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams absContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        absContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                0,
                (int) getResources().getDimension(R.dimen.double_margin),
                0
        );
        titleLayout.addView(absContainer, absContainerParams);

        // чекбокс отсутствия
        checkImage = new ImageView(getActivity());
        if (absTypePoz != -1) {
            checkImage.setImageResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_full);
        } else {
            checkImage.setImageResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_empty);
        }
        RelativeLayout.LayoutParams checkImageParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.my_icon_small_size),
                (int) getResources().getDimension(R.dimen.my_icon_small_size));
        checkImageParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        checkImageParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        checkImageParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        checkImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
        absContainer.addView(checkImage, checkImageParams);

        Spinner absSpinner = null;
        if (absentTypesLongNames.length > 1) { // если элементов больше одного используем спиннер
            // спиннер отсутствия
            absSpinner = new Spinner(getActivity());
            RelativeLayout.LayoutParams absSpinnerParams = new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            absSpinnerParams.addRule(RelativeLayout.CENTER_VERTICAL);
            absSpinnerParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            absContainer.addView(absSpinner, absSpinnerParams);
            // адаптер спиннера

            ArrayAdapter<String> absentAdapter;
            // получаем выбранный номер пропуска
            if (absTypePoz == -1) {// если пропуска нет, выводим пустой спиннер
                absentAdapter = new ArrayAdapter<>(
                        getActivity(),
                        R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                        new String[]{" "}
                );
                absentAdapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
                absSpinner.setAdapter(absentAdapter);
            } else {
                absentAdapter = new ArrayAdapter<>(
                        getActivity(),
                        R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                        absentTypesLongNames
                );
                absentAdapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
                absSpinner.setAdapter(absentAdapter);
                absSpinner.setSelection(absTypePoz, false);
            }

            // при выборе номера пропуска
            absSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (absTypePoz != -1)
                        absTypePoz = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


        } else {
            TextView absText = new TextView(getActivity());
            absText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
            absText.setText(R.string.lesson_activity_learner_absent_text);
            absText.setTextColor(Color.BLACK);
            absText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            RelativeLayout.LayoutParams absTextParams = new RelativeLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            absTextParams.addRule(RelativeLayout.CENTER_VERTICAL);
            absTextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            absTextParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            absTextParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            absContainer.addView(absText, absTextParams);
        }

        // нажатие на контейнер отсутствия
        finalAbsSpinner = absSpinner;
        absContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // если чекбокс был активен, дизактивируем
                if (absTypePoz != -1) {
                    checkImage.setImageResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_empty);

                    // пропуски
                    absTypePoz = -1;
                    // если типов пропуска больше чем 1
                    if (absentTypesLongNames.length > 1) {
                        finalAbsSpinner.setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                                new String[]{" "}
                        ));
                    }

                    // оценки
                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                        // ставим позиции в массивы
                        grades[gradeI] = 0;
                        chosenTypes[gradeI] = 0;

                        // ставим выбор на первых позициях в спиннерах и адаптеры с оценками и типами
                        gradesSpinners[gradeI].setSelection(0, false);
                        ArrayAdapter<String> gradesSpinnersAdapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                gradesString[gradeI]
                        );
                        gradesSpinnersAdapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
                        gradesSpinners[gradeI].setAdapter(gradesSpinnersAdapter);

                        typesSpinners[gradeI].setSelection(0, false);
                        ArrayAdapter<String> typesSpinnersAdapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                gradesTypesNames
                        );
                        typesSpinnersAdapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
                        typesSpinners[gradeI].setAdapter(typesSpinnersAdapter);
                    }

                } else {// иначе активируем его
                    checkImage.setImageResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_full);

                    // пропуски
                    absTypePoz = 0;
                    // если типов пропуска больше чем 1
                    if (absentTypesLongNames.length > 1) {
                        finalAbsSpinner.setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                                absentTypesLongNames
                        ));
                        finalAbsSpinner.setSelection(0, false);
                    }

                    // оценки
                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                        // чистим оценки
                        grades[gradeI] = 0;
                        chosenTypes[gradeI] = 0;
                        // и дизактивируем спиннеры
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                new String[]{" "}
                        );
                        gradesSpinners[gradeI].setAdapter(adapter);
                        adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);

                        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                new String[]{" "}
                        );
                        typesSpinners[gradeI].setAdapter(adapter2);
                        adapter2.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
                    }


                }
            }
        });


        // ---- контейнер для тела диалога ----
        ScrollView bodyLayout = new ScrollView(getActivity());
        //
        bodyLayout.setBackgroundResource(R.drawable.base_background_dialog_bottom_round_wite);
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
            // если пропуска нет
            if (absTypePoz == -1) {
                // адаптер спиннера
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getActivity(),
                        R.layout._____spinner_dropdown_element_subtitle_transparent,
                        gradesString[gradeI]
                );
                gradesSpinners[gradeI].setAdapter(adapter);
                adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);

                // ставим выбор
                if (grades[gradeI] <= maxGrade) {
                    gradesSpinners[gradeI].setSelection(grades[gradeI]);
                } else {
                    gradesSpinners[gradeI].setSelection(maxGrade + 1);
                }
            } else {
                // адаптер спиннера
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getActivity(),
                        R.layout._____spinner_dropdown_element_subtitle_transparent,
                        new String[]{" "}
                );
                gradesSpinners[gradeI].setAdapter(adapter);
                adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
            } // при выборе элемента из спиннера
            final int finalGradeI = gradeI;
            gradesSpinners[gradeI].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // если не Н
                    if (absTypePoz == -1)
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
            if (absTypePoz == -1) {
                // адаптер спиннера
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getActivity(),
                        R.layout._____spinner_dropdown_element_subtitle_transparent,
                        gradesTypesNames
                );
                typesSpinners[gradeI].setAdapter(adapter);
                adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
                // ставим выбор
                typesSpinners[gradeI].setSelection(chosenTypes[gradeI], false);
            } else {
                // адаптер спиннера
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getActivity(),
                        R.layout._____spinner_dropdown_element_subtitle_transparent,
                        new String[]{" "}
                );
                typesSpinners[gradeI].setAdapter(adapter);
                adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);

            }
            // при выборе элемента из спиннера
            typesSpinners[gradeI].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // если не Н
                    if (absTypePoz == -1)
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
        saveButtonContainer.setBackgroundResource(R.drawable.base_background_button_circle_orange);
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
        saveText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
        saveText.setText(R.string.button_save);
        saveText.setTextColor(getResources().getColor(R.color.backgroundWhite));
        saveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams saveTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        saveTextParams.setMargins(
                (int) getResources().getDimension(R.dimen.forth_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.forth_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        saveButtonContainer.addView(saveText, saveTextParams);

        // при нажатии на кнопку сохранить
        saveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // если оценки нулевые, обнуляем типы к первому
                for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                    if (grades[gradeI] == 0) {
                        chosenTypes[gradeI] = 0;
                    }
                }

                // возвращаем измененные оценки в активность
                ((EditGradeDialogInterface) getActivity()).editGrades(grades, absTypePoz, chosenTypes, lessonPosition);

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
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        //вызываем в активности метод разрешения изменения оценок
        ((AllowEditGradesInterface) getActivity()).allowUserEditGrades();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        //вызываем в активности метод разрешения изменения оценок
        ((AllowEditGradesInterface) getActivity()).allowUserEditGrades();

    }
}

interface EditGradeDialogInterface {
    void editGrades(int[] grades, int absTypePoz, int[] chosenTypesNumbers, int lessonPoz);

    GradeUnit getLessonGrades(int lessonNumber);
}