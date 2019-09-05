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
import android.widget.Spinner;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;


public class GradeDialogFragment extends DialogFragment {

    public static final String ARGS_LEARNER_NAME = "name";
    public static final String ARGS_STRING_GRADES_TYPES_ARRAY = "gradesTypes";
    public static final String ARGS_INT_GRADES_ARRAY = "grades";
    public static final String ARGS_INT_MAX_GRADE = "maxGrade";
    public static final String ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY = "chosenTypes";

    public static final String ARGS_INT_CHOSEN_GRADE_POSITION = "gradePosition";

    // массив позиций выбранных оценок
    int[] grades;
    // массив позиций выбранных типов
    int[] chosenTypes;

    // массив спиннеров с оценками
    Spinner[] gradesSpinners;
    // массив спиннеров с типами оценок
    Spinner[] typesSpinners;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // ---- layout диалога ----
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_grade_edit, null);
        builder.setView(view);


        // получаем данные из intent

        // имя редактируемого ученика
        String learnerName = getArguments().getString(ARGS_LEARNER_NAME);

        // оценки
        grades = getArguments().getIntArray(ARGS_INT_GRADES_ARRAY);

        // выбранные номера типов оценок
        chosenTypes = getArguments().getIntArray(ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY);

        // названия типов оценок
        final String[] gradesTypesNames = getArguments().getStringArray(ARGS_STRING_GRADES_TYPES_ARRAY);

        // создаем массив с оценками в текстовом виде
        int maxGrade = getArguments().getInt(ARGS_INT_MAX_GRADE, 0);
        final String[] gradesString = new String[maxGrade + 1];
        gradesString[0] = "-";
        for (int gradeI = 1; gradeI < maxGrade + 1; gradeI++) {
            gradesString[gradeI] = "" + gradeI;
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
        nameParams.setMargins(pxFromDp(10), 0, pxFromDp(10), 0);
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
        absContainerParams.setMargins(pxFromDp(10), 0, pxFromDp(10), 0);
        titleLayout.addView(absContainer, absContainerParams);

        // чекбокс отсутствия
        final ImageView checkImage = new ImageView(getActivity());
        if (grades[0] == -2) {
            checkImage.setBackgroundResource(R.drawable.__checkbox_full);
        } else
            checkImage.setBackgroundResource(R.drawable.__checkbox_empty);
        LinearLayout.LayoutParams checkImageParams = new LinearLayout.LayoutParams(pxFromDp(20), pxFromDp(20));
        checkImageParams.topMargin = pxFromDp(10);
        checkImageParams.bottomMargin = pxFromDp(10);
        checkImageParams.rightMargin = pxFromDp(10);
        absContainer.addView(checkImage, checkImageParams);

        // текст отсутствия
        TextView absText = new TextView(getActivity());
        absText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        absText.setText(R.string.lesson_activity_learner_absent_text);
        absText.setTextColor(Color.BLACK);
        absText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        LinearLayout.LayoutParams absTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        absTextParams.topMargin = pxFromDp(10);
        absTextParams.bottomMargin = pxFromDp(10);
        absContainer.addView(absText, absTextParams);

        // нажатие на контейнер отсутствия
        absContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // если чекбокс был активен, дизактивируем
                if (grades[0] == -2) {
                    checkImage.setBackgroundResource(R.drawable.__checkbox_empty);
                    for (int gradeI = 0; gradeI < grades.length; gradeI++) {
                        // ставим позиции в массивы
                        grades[gradeI] = 0;
                        chosenTypes[gradeI] = 0;

                        // ставим выбор на первых позициях в спиннерах и адаптеры с оценками и типами
                        gradesSpinners[gradeI].setSelection(0, false);
                        gradesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.spinner_dropdown_element_subtitle,
                                gradesString
                        ));

                        typesSpinners[gradeI].setSelection(0, false);
                        typesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                                getActivity(),
                                R.layout.spinner_dropdown_element_subtitle,
                                gradesTypesNames
                        ));
                    }

                } else {// иначе активируем его
                    checkImage.setBackgroundResource(R.drawable.__checkbox_full);
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
        spinnersContainerParams.setMargins(pxFromDp(5), pxFromDp(5), pxFromDp(5), pxFromDp(5));
        bodyLayout.addView(spinnersContainer, spinnersContainerParams);

        // добавляем спиннеры
        gradesSpinners = new Spinner[grades.length];
        typesSpinners = new Spinner[grades.length];
        for (int gradeI = 0; gradeI < grades.length; gradeI++) {
            // создаем горизонтальный контейнер под спиннеры одной оценки
            LinearLayout gradeContainer = new LinearLayout(getActivity());
            // закрашиваем фон выбранной оценки
            if(chosenGradePosition == gradeI){
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
            // ставим адаптер и выбор спиннеру в соответствии с текущей оценкой
            if (grades[gradeI] >= 0) {
                // адаптер спиннера
                gradesSpinners[gradeI].setAdapter(new ArrayAdapter<>(
                        getActivity(),
                        R.layout.spinner_dropdown_element_subtitle,
                        gradesString
                ));
                // ставим выбор
                gradesSpinners[gradeI].setSelection(grades[gradeI]);
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
                        grades[finalGradeI] = position;
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
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                2
        );
        saveButtonContainerParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
        saveButtonContainerParams.gravity = Gravity.CENTER;
        spinnersContainer.addView(saveButtonContainer, saveButtonContainerParams);

        TextView saveText = new TextView(getActivity());
        saveText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        saveText.setText(R.string.lesson_list_activity_menu_text_save);
        saveText.setTextColor(getResources().getColor(R.color.backgroundWhite));
        saveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        LinearLayout.LayoutParams saveTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextParams.leftMargin = pxFromDp(10);
        saveTextParams.topMargin = pxFromDp(5);
        saveTextParams.rightMargin = pxFromDp(10);
        saveTextParams.bottomMargin = pxFromDp(5);
        saveButtonContainer.addView(saveText, saveTextParams);

        // при нажатии на кнопку сохранить
        saveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // возвращаем измененные оценки в активность
                ((GradesDialogInterface) getActivity()).setGrades(grades, chosenTypes);

                // закрываем диалог
                dismiss();
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }


    // преобразование зависимой величины в пиксели
    private int pxFromDp(float dp) {
        return (int) (dp * getActivity().getResources().getDisplayMetrics().density);
    }
}


interface GradesDialogInterface {

    // установить предмет стоящий на этой позиции как выбранный
    void setGrades(int[] grades, int[] chosenTypesNumbers);

}