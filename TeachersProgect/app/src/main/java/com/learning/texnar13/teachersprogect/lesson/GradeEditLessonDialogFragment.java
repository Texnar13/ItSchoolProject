package com.learning.texnar13.teachersprogect.lesson;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;


public class GradeEditLessonDialogFragment extends DialogFragment {

    static final String ARGS_LEARNER_NAME = "name";
    static final String ARGS_STRING_GRADES_TYPES_ARRAY = "gradesTypes";
    static final String ARGS_INT_GRADES_ABSENT_TYPE_NUMBER = "chosenAbsTypeNumber";
    static final String ARGS_STRING_ABSENT_TYPES_NAMES_ARRAY = "absentTypeNameArray";
    static final String ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY = "absentTypeLongNameArray";
    static final String ARGS_INT_GRADES_ARRAY = "grades";
    static final String ARGS_INT_MAX_GRADE = "maxGrade";
    static final String ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY = "chosenGradesTypes";

    static final String ARGS_INT_CHOSEN_GRADE_POSITION = "gradePosition";

    // массив позиций выбранных оценок
    int[] grades;
    // массив позиций выбранных типов
    int[] chosenTypes;
    // номер пропуска
    int absTypePoz;


    // массив спиннеров с оценками
    Spinner[] gradesSpinners;
    // массив спиннеров с типами оценок
    Spinner[] typesSpinners;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // ---- layout диалога ----
        View view = getActivity().getLayoutInflater().inflate(R.layout.____dialog_fragment_layout_grade_edit, null);
        builder.setView(view);


        // получаем данные из intent

        // имя редактируемого ученика
        String learnerName = getArguments().getString(ARGS_LEARNER_NAME);

        // оценки
        grades = getArguments().getIntArray(ARGS_INT_GRADES_ARRAY);

        // выбранные номера типов оценок
        chosenTypes = getArguments().getIntArray(ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY);

        // тип пропуска
        absTypePoz = getArguments().getInt(ARGS_INT_GRADES_ABSENT_TYPE_NUMBER, -1);

        // названия типов пропусков
        final String[] absentTypesNames = getArguments().getStringArray(ARGS_STRING_ABSENT_TYPES_NAMES_ARRAY);
        final String[] absentTypesLongNames = getArguments().getStringArray(ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY);

        // названия типов оценок
        final String[] gradesTypesNames = getArguments().getStringArray(ARGS_STRING_GRADES_TYPES_ARRAY);


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


        // ---- контейнер для шапки ----
        LinearLayout titleLayout = view.findViewById(R.id.dialog_fragment_layout_edit_grade_head);
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
        nameParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        titleLayout.addView(name, nameParams);


        // контейнер отсутствия
        RelativeLayout absContainer = new RelativeLayout(getActivity());
        absContainer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams absContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
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
        final ImageView checkImage = new ImageView(getActivity());
        if (absTypePoz != -1) {
            checkImage.setBackgroundResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_full);
        } else
            checkImage.setBackgroundResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_empty);
        RelativeLayout.LayoutParams checkImageParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.my_icon_small_size),
                (int) getResources().getDimension(R.dimen.my_icon_small_size)
        );
        checkImageParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        checkImageParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        checkImageParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        checkImageParams.addRule(RelativeLayout.CENTER_VERTICAL);
        absContainer.addView(checkImage, checkImageParams);

        // спиннер отсутствия
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
            absText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
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
        final Spinner finalAbsSpinner = absSpinner;
        absContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // если чекбокс был активен, дизактивируем
                if (absTypePoz != -1) {
                    checkImage.setBackgroundResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_empty);
                    absTypePoz = -1;
                    // если типов пропуска больше чем 1
                    if (absentTypesLongNames.length > 1){
                        finalAbsSpinner.setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                                new String[]{" "}
                        ));
                    }


                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                        // ставим позиции в массивы
                        grades[gradeI] = 0;
                        chosenTypes[gradeI] = 0;


                        // ставим выбор на первых позициях в спиннерах и адаптеры с оценками и типами
                        gradesSpinners[gradeI].setSelection(0, false);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                gradesString[gradeI]
                        );
                        gradesSpinners[gradeI].setAdapter(adapter);
                        adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);

                        typesSpinners[gradeI].setSelection(0, false);
                        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                                getActivity(),
                                R.layout._____spinner_dropdown_element_subtitle_transparent,
                                gradesTypesNames
                        );
                        typesSpinners[gradeI].setAdapter(adapter2);
                        adapter2.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
                    }

                } else {// иначе активируем его
                    checkImage.setBackgroundResource(R.drawable.learners_and_grades_activity_abs_checkbox_background_full);


                    // пропуски
                    absTypePoz = 0;
                    // если типов пропуска больше чем 1
                    if (absentTypesLongNames.length > 1){
                        finalAbsSpinner.setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.______spinner_dropdown_element_subtitle_transparent_dark_gray,
                                absentTypesLongNames
                        ));
                        finalAbsSpinner.setSelection(0, false);
                    }


                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                        // убираем оценки
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
        LinearLayout bodyLayout = view.findViewById(R.id.dialog_fragment_layout_edit_grade_bottom);

        // на какой оценке сейчас стоит выбор
        int chosenGradePosition = getArguments().getInt(ARGS_INT_CHOSEN_GRADE_POSITION);


        // контейнер со спиннерами
        LinearLayout spinnersContainer = new LinearLayout(getActivity());
        spinnersContainer.setOrientation(LinearLayout.VERTICAL);
        spinnersContainer.setWeightSum(5);
        LinearLayout.LayoutParams spinnersContainerParams = new LinearLayout.LayoutParams(
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
            // закрашиваем фон выбранной оценки
            if (chosenGradePosition == gradeI) {
                gradeContainer.setBackgroundColor(getResources().getColor(R.color.backgroundLiteGray));
            }
            gradeContainer.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams gradeContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            //gradeContainerParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
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
                gradesSpinners[gradeI].setSelection(grades[gradeI]);
            } else {
                // адаптер спиннера
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        getActivity(),
                        R.layout._____spinner_dropdown_element_subtitle_transparent,
                        new String[]{" "}
                );
                gradesSpinners[gradeI].setAdapter(adapter);
                adapter.setDropDownViewResource(R.layout.___base_spinner_dropdown_element_subtitle);
            }
            // при выборе элемента из спиннера
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
                typesSpinners[gradeI].setSelection(chosenTypes[gradeI]);
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
        saveButtonContainer.setBackgroundResource(R.drawable.base_button_round_background_orange);
        LinearLayout.LayoutParams saveButtonContainerParams = new LinearLayout.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
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
        saveText.setText(R.string.button_save);
        saveText.setTextColor(getResources().getColor(R.color.backgroundWhite));
        saveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams saveTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextParams.leftMargin = (int) getResources().getDimension(R.dimen.forth_margin);
        saveTextParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        saveTextParams.rightMargin = (int) getResources().getDimension(R.dimen.forth_margin);
        saveTextParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        saveButtonContainer.addView(saveText, saveTextParams);

        // при нажатии на кнопку сохранить
        saveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // возвращаем измененные оценки в активность
                ((GradesDialogInterface) getActivity()).setGrades(grades, chosenTypes,absTypePoz);

                // закрываем диалог
                dismiss();
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}


interface GradesDialogInterface {

    // установить предмет стоящий на этой позиции как выбранный
    void setGrades(int[] grades, int[] chosenTypesNumbers, int chosenAbsPoz);

}