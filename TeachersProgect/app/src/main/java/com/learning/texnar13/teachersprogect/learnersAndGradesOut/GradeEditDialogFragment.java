package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;

public class GradeEditDialogFragment extends DialogFragment {//входные данные оценка, id оценки

    // переменные константы позволяющие получить данные через intent
    public static final String ARGS_LEARNER_NAME = "name";
    public static final String ARGS_STRING_GRADES_TYPES_ARRAY = "gradesTypes";
    public static final String ARGS_INT_GRADES_ARRAY = "grades";
    public static final String ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY = "chosenTypes";
    public static final String ARGS_INT_GRADES_ABSENT_TYPE_NUMBER = "chosenAbsTypeNumber";
    public static final String ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY = "absentTypeLongNameArray";
    public static final String ARGS_INT_MAX_GRADE = "maxGrade";
    public static final String ARGS_INT_MAX_LESSONS_COUNT = "maxLessonsCount";
    public static final String ARGS_STRING_CURRENT_DATE = "currentDate";
    public static final String ARGS_INT_LESSON_NUMBER = "lessonNumber";
    public static final String ARGS_BOOLEAN_IS_LESSON_NUMBER_LOCKED = "lessonNumberLocked";// можно не передавать
    public static final String ARGS_INT_CHOSEN_GRADE_POSITION = "chosenGradePos";// можно не передавать


    // названия типов оценок
    private String[] gradesTypesNames;
    // названия типов пропусков
    String[] absentTypesLongNames;
    // максимальная оценка
    private int maxGrade;
    // максимальное количество уроков
    private int maxLessonsCount;
    // на какой оценке сейчас стоит выбор
    private int chosenGradePosition;


    // массив позиций выбранных оценок
    private int[] grades;
    // массив позиций выбранных типов
    private int[] chosenTypes;
    // выбранный тип отсутствия
    // нажат ли чекбокс отсутствия
    private boolean absCheckState;
    // текущая позиция спиннера отсутствия (не зависит от чекбокса) ( = 0, если вместо спиннера TextView)
    //  Не принимает значение -1 никогда!
    private int absentSpinnerPos;


    // спиннер с номером выбранного урока
    private int currentLessonNumber;
    private boolean isLessonNumberLocked;
    private Spinner lessonNumberSpinner;
    // контейнер со спиннерами
    // разделительная линия
    private FrameLayout dividingLine;
    // затемняющее View
    private FrameLayout shadeLayer;
    // массив спиннеров с оценками
    private Spinner[] gradesSpinners;
    // массив спиннеров с типами оценок
    private Spinner[] typesSpinners;
    // чекбокс отсутствия
    private CheckBox absentCheckbox;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // layout диалога
        View dialogRoot = getLayoutInflater().inflate(R.layout.learners_and_grades_dialog_grade_edit, null);
        builder.setView(dialogRoot);

        // получаем данные из intent
        // текст текущей даты
        String dateText = getArguments().getString(ARGS_STRING_CURRENT_DATE);
        // имя редактируемого ученика
        String learnerName = getArguments().getString(ARGS_LEARNER_NAME);
        // выбранные номера типов оценок
        chosenTypes = getArguments().getIntArray(ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY);
        // оценки
        grades = getArguments().getIntArray(ARGS_INT_GRADES_ARRAY);// todo для шлифовки кода можно убрать эту строчку(или отправить null) и исправить ошибки если они вдруг появятся
        {// получаем выбранный номер типа пропуска
            int absTypePos = getArguments().getInt(ARGS_INT_GRADES_ABSENT_TYPE_NUMBER);
            // нажат ли чекбокс отсутствия
            absCheckState = absTypePos != -1;
            // текущая позиция спиннера отсутствия
            absentSpinnerPos = (absCheckState) ? absTypePos : 0;
        }
        // названия типов оценок
        gradesTypesNames = getArguments().getStringArray(ARGS_STRING_GRADES_TYPES_ARRAY);
        // названия типов пропусков
        absentTypesLongNames = getArguments().getStringArray(ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY);
        // максимальная оценка
        maxGrade = getArguments().getInt(ARGS_INT_MAX_GRADE, 0);
        // максимальное количество уроков
        maxLessonsCount = getArguments().getInt(ARGS_INT_MAX_LESSONS_COUNT, 1);
        // спиннер с номером выбранного урока
        currentLessonNumber = getArguments().getInt(ARGS_INT_LESSON_NUMBER);
        isLessonNumberLocked = getArguments().getBoolean(ARGS_BOOLEAN_IS_LESSON_NUMBER_LOCKED, false);
        // на какой оценке сейчас стоит выбор
        chosenGradePosition = getArguments().getInt(ARGS_INT_CHOSEN_GRADE_POSITION, -1);


        // если пропуска нет и если оценок нет, то проставляем типы по умолчанию (+ без подписки эта опция не нужна)
        if (PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext())
                .getBoolean(SharedPrefsContract.PREFS_BOOLEAN_PREMIUM_STATE, false) && !absCheckState) {
            // первая оценка остается с типом по умолчанию
            // вторая со вторым
            if (grades[1] == 0 && gradesTypesNames.length > 1)
                chosenTypes[1] = 1;

            // третья с третьим
            if (grades[2] == 0 && gradesTypesNames.length > 2)
                chosenTypes[2] = 2;
        }


        // поиск View в разметке
        // спиннер номера урока
        lessonNumberSpinner = dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_spinner_lesson);
        // контейнер отсутствия
        absContainer = dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_absent_button);
        // чекбокс отсутствия
        absentCheckbox = dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_absent_button_checkbox);
        // разделительная линия контейнера со спиннерами
        dividingLine = dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_dividing_line);
        // затемняющее View
        shadeLayer = dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grades_shade);
        // спиннеры оценок
        gradesSpinners = new Spinner[]{
                dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_0),
                dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_1),
                dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_2)
        };
        // спиннеры типов
        typesSpinners = new Spinner[]{
                dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_0_type),
                dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_1_type),
                dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_2_type)
        };

        // подсветка текущей оценки для активности урока
        if (chosenGradePosition != -1) {
            LinearLayout[] backOfSpinners = new LinearLayout[]{
                    dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_0_container),
                    dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_1_container),
                    dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_grade_2_container)
            };
            backOfSpinners[chosenGradePosition].setBackgroundColor(getResources().getColor(R.color.base_light));
        }

        // кнопка назад
        dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_button_close).setOnClickListener(v -> dismiss());
        // текст текущей даты
        ((TextView) dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_title_date))
                .setText(dateText);
        // имя ученика
        ((TextView) dialogRoot.findViewById(R.id.learners_and_grades_dialog_grade_edit_name_text)).setText(learnerName);
        // спиннер номера урока
        outLessonNumber();
        // вывод пропусков
        outAbsentContainer(absentTypesLongNames);
        // выводим данные в спиннеры оценок и их типов
        outValuesInGradesFields();
        // выводим затемнение
        outShadowLayer();


        // нажатие на контейнер отсутствия
        absContainer.setOnClickListener(v -> {
            // если чекбокс был активен, дизактивируем и наоборот
            absCheckState = !absCheckState;
            absentCheckbox.setChecked(absCheckState);

            // поля оценок выведутся пустыми, если absCheckState будет true
            outValuesInGradesFields();
            // выводим затемнение
            outShadowLayer();

        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }


    // передача оценок в активность для сохранения
    private void saveGradesAndAbsInActivity() {
        // если оценки нулевые, обнуляем типы к первому из списка
        for (int gradeI = 0; gradeI < grades.length; gradeI++)
            if (grades[gradeI] == 0)
                chosenTypes[gradeI] = 0;

        // возвращаем измененные оценки в активность
        ((EditGradeDialogInterface) requireActivity()).editGrades(
                // массив оценок
                grades,
                // позиция выбранного пропуска
                (absCheckState) ? absentSpinnerPos : -1,
                // выбранные типы оценок
                chosenTypes,
                // номер выбранного урока
                currentLessonNumber
        );
    }

    // спиннер номера урока
    private void outLessonNumber() {

        // спиннер урока может быть залочен на одно значение
        String[] lessonsTexts;
        if (isLessonNumberLocked) {
            lessonsTexts = new String[]{
                    getResources().getString(R.string.learners_and_grades_out_activity_dialog_title_lesson, (currentLessonNumber + 1))
            };
        } else {
            lessonsTexts = new String[maxLessonsCount];
            for (int lessonsI = 0; lessonsI < lessonsTexts.length; lessonsI++) {
                lessonsTexts[lessonsI] = getResources().getString(R.string.learners_and_grades_out_activity_dialog_title_lesson, (lessonsI + 1));
            }
        }

        // адаптер спиннера
        ArrayAdapter<String> lessonAdapter = new ArrayAdapter<>(
                requireActivity(),
                R.layout.lesson_redactor_spinner_dropdown_element,
                lessonsTexts
        );
        lessonAdapter.setDropDownViewResource(R.layout.lesson_redactor_spinner_dropdown_element);
        lessonNumberSpinner.setAdapter(lessonAdapter);

        // получаем выбранный номер урока и выставляем его в спиннер
        if (isLessonNumberLocked) {
            lessonNumberSpinner.setSelection(0, false);
        } else
            lessonNumberSpinner.setSelection(currentLessonNumber, false);

        // при выборе номера урока переподгружаются данные оценок и пропусков (не отрабатывает при запуске)
        lessonNumberSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // сохраняем измененные оценки в активность
                saveGradesAndAbsInActivity();

                // и уже после сохранения старого урока получаем номер нового урока
                currentLessonNumber = position;

                // получаем оценки с другого урока
                LearnersAndGradesActivity.GradeUnit temp = ((EditGradeDialogInterface) getActivity()).getLessonGrades(position);
                if (temp != null) {
                    grades = temp.grades;
                    chosenTypes = temp.gradesTypesIndexes;

                    // нажат ли чекбокс отсутствия
                    absCheckState = temp.absTypePoz != -1;
                    // текущая позиция спиннера отсутствия
                    absentSpinnerPos = (absCheckState) ? temp.absTypePoz : 0;
                } else {
                    grades = new int[]{0, 0, 0};
                    chosenTypes = new int[]{0, 0, 0};

                    // нажат ли чекбокс отсутствия
                    absCheckState = false;
                    // текущая позиция спиннера отсутствия
                    absentSpinnerPos = 0;
                }

                // вывод пропусков
                outAbsentContainer(absentTypesLongNames);
                // выводим данные в спиннеры оценок и их типов
                outValuesInGradesFields();
                // выводим затемнение
                outShadowLayer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }


    // контейнер отсутствия
    private LinearLayout absContainer;
    // текущий созданный программно спинер/текст отсутствия
    private TextView currentAbsTextView = null;
    private Spinner currentAbsSpinner = null;

    private void outAbsentContainer(String[] absentTypesNames) {

        // чекбокс отсутствия
        absentCheckbox.setChecked(absCheckState);


        // удаляем одну из старых разметок (если она была)
        absContainer.removeView(currentAbsTextView);
        absContainer.removeView(currentAbsSpinner);

        if (absentTypesNames.length > 1) {
            // вывод спиннера с несколькими элементами
            currentAbsTextView = null;

            // создаем поле
            currentAbsSpinner = new Spinner(getActivity());
            LinearLayout.LayoutParams absSpinnerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            absContainer.addView(currentAbsSpinner, absSpinnerParams);


            // адаптер спиннера
            ArrayAdapter<String> absentAdapter = new ArrayAdapter<>(
                    requireActivity(),
                    R.layout.lesson_redactor_spinner_dropdown_element,
                    absentTypesNames
            );
            absentAdapter.setDropDownViewResource(R.layout.lesson_redactor_spinner_dropdown_element);
            currentAbsSpinner.setAdapter(absentAdapter);
            // выбираем текущий пропуск в спиннере (даже если чекбокс не нажат)
            currentAbsSpinner.setSelection(absentSpinnerPos, false);

            // при выборе номера пропуска
            currentAbsSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    absentSpinnerPos = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        } else if (absentTypesNames.length == 1) {
            // вывод текстового поля с одним элементом
            currentAbsSpinner = null;

            // создаем поле
            currentAbsTextView = new TextView(getActivity());

            currentAbsTextView.setTextColor(getResources().getColor(R.color.text_color_simple));
            currentAbsTextView.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_semibold));
            currentAbsTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.simple_buttons_text_size));
            currentAbsTextView.setGravity(Gravity.CENTER_VERTICAL);
            currentAbsTextView.setPadding(getResources().getDimensionPixelOffset(R.dimen.double_margin), 0, 0, 0);

            currentAbsTextView.setText(absentTypesNames[0]);

            LinearLayout.LayoutParams absTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );

            absContainer.addView(currentAbsTextView, absTextParams);
        }
    }


    // вывод спиннеров оценок и их типов
    private void outValuesInGradesFields() {

        // создаем массив с оценками в текстовом виде
        final String[][] gradesString = new String[grades.length][];
        // массив для хранения дополнительных оценок (три оценки - по одной доп оценке для каждого спиннера)
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
                gradesString[gradeNumberI][gradeI] = gradeI + " ";
            }
        }


        // вывод информации в спиннеры
        for (int gradeI = 0; gradeI < grades.length; gradeI++) {

            // адаптер спиннера с оценкой
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireActivity(),
                    R.layout.lesson_redactor_spinner_dropdown_element,
                    gradesString[gradeI]
            );
            adapter.setDropDownViewResource(R.layout.lesson_redactor_spinner_dropdown_element);
            gradesSpinners[gradeI].setAdapter(adapter);

            // ставим выбор
            if (absCheckState) {
                // если проставлена н-ка
                gradesSpinners[gradeI].setSelection(0);
            } else if (grades[gradeI] <= maxGrade) {
                // ставим в спиннере выбор по числу оценки
                gradesSpinners[gradeI].setSelection(grades[gradeI]);
            } else {
                // если оценка вне диапазона, ставим последний пункт (в котором она как раз и записана)
                gradesSpinners[gradeI].setSelection(maxGrade + 1);
            }

            // при выборе элемента из спиннера
            final int finalGradeI = gradeI;
            gradesSpinners[gradeI].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position <= maxGrade) {
                        grades[finalGradeI] = position;
                    } else {
                        grades[finalGradeI] = dopGrades[finalGradeI];
                    }// todo убрать эту строку и эти переменные (получать все из спиннера)
                    // если стоит пропуск эти оценки просто не будут учитываться при отправке данных
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            // адаптер спиннера с типом ответа
            ArrayAdapter<String> typeSpinnerAdapter;
            // проверяем подписку
            if (PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext())
                    .getBoolean(SharedPrefsContract.PREFS_BOOLEAN_PREMIUM_STATE, false)) {
                typeSpinnerAdapter = new ArrayAdapter<>(
                        requireActivity(),
                        R.layout.lesson_redactor_spinner_dropdown_element,
                        gradesTypesNames
                );
            } else {
                // если подписки нет
                // то либо просто ограничиваем число типов, ли бо ставим максимумом то, которое есть
                String[] arr = new String[
                        Math.max(chosenTypes[gradeI] + 1,
                                Math.min(gradesTypesNames.length,// ограничение может быть тупо больше длинны массива
                                        SharedPrefsContract.PREMIUM_PARAM_GRADES_TYPES_MAXIMUM)
                        )
                ];
                // копируем урезанный массив
                System.arraycopy(gradesTypesNames, 0, arr, 0, arr.length);

                typeSpinnerAdapter = new ArrayAdapter<>(
                        requireActivity(),
                        R.layout.lesson_redactor_spinner_dropdown_element,
                        arr
                );
            }
            typeSpinnerAdapter.setDropDownViewResource(R.layout.lesson_redactor_spinner_dropdown_element);
            typesSpinners[gradeI].setAdapter(typeSpinnerAdapter);

            // ставим выбор
            if (absCheckState) {
                // если проставлена н-ка
                gradesSpinners[gradeI].setSelection(0);
            } else
                typesSpinners[gradeI].setSelection(chosenTypes[gradeI], false);

            // при выборе элемента из спиннера
            typesSpinners[gradeI].setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // если стоит пропуск эти оценки просто не будут учитываться при отправке данных
                    chosenTypes[finalGradeI] = position;// todo убрать эту строку и эти переменные (получать все из спиннера)
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    // вывод затемнения
    private void outShadowLayer() {

        // закрашиваем полупрозрачным цветом слой над оценками
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shadeLayer.getBackground().setTint(getResources().getColor(
                    (absCheckState) ?
                            (R.color.grade_edit_bottom_shadow_color) :
                            (R.color.transparent)
            ));
        } else {
            shadeLayer.getBackground().setColorFilter(getResources().getColor(
                    (absCheckState) ?
                            (R.color.grade_edit_bottom_shadow_color) :
                            (R.color.transparent)
            ), PorterDuff.Mode.SRC_ATOP);
        }
        // а также полоску
        dividingLine.setBackgroundColor(getResources().getColor(
                (absCheckState) ?
                        (R.color.grade_edit_bottom_shadow_color) :
                        (R.color.text_color_not_active)
        ));


        // отключаем или включаем нажатие на оценки
        shadeLayer.setOnClickListener(null);
        shadeLayer.setClickable(absCheckState);
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // передача оценок в активность для сохранения
        saveGradesAndAbsInActivity();

        //вызываем в активности метод разрешения изменения оценок
        ((EditGradeDialogInterface) requireActivity()).allowUserStartGradesDialog();
    }

    public interface EditGradeDialogInterface {
        void editGrades(int[] grades, int absTypePoz, int[] chosenTypesNumbers, int lessonPoz);

        void allowUserStartGradesDialog();

        LearnersAndGradesActivity.GradeUnit getLessonGrades(int lessonNumber);
    }
}
