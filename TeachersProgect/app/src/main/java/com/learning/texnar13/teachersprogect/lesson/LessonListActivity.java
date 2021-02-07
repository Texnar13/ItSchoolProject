package com.learning.texnar13.teachersprogect.lesson;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

public class LessonListActivity extends AppCompatActivity implements GradesDialogInterface {

    //public static final String LESSON_TIME = "startTime";
    public static final String LESSON_DATE = "lessonDate";
    public static final String LESSON_NUMBER = "lessonNumber";
    public static final String SUBJECT_ID = "subjectId";
    public static final String ARGS_STRING_ARRAY_LEARNERS_NAMES = "learnersNames";
    public static final String ARGS_INT_ARRAY_LEARNERS_ID = "learnersID";
    public static final String STRING_GRADES_TYPES_ARRAY = "gradesTypesArray";
    public static final String INT_GRADES_TYPES_ID_ARRAY = "gradesTypesIdArray";
    public static final String STRING_ABSENT_TYPES_ARRAY = "absentTypesArray";
    public static final String STRING_ABSENT_TYPES_LONG_ARRAY = "absentTypesLongArray";
    public static final String LONG_ABSENT_TYPES_ID_ARRAY = "absentTypesIdArray";
    public static final String INT_MAX_GRADE = "maxGrade";

    public static final String FIRST_GRADES = "grades1";
    public static final String SECOND_GRADES = "grades2";
    public static final String THIRD_GRADES = "grades3";
    public static final String FIRST_GRADES_TYPES = "gradesTypes1";
    public static final String SECOND_GRADES_TYPES = "gradesTypes2";
    public static final String THIRD_GRADES_TYPES = "gradesTypes3";
    public static final String INT_ABSENT_TYPE_POZ_ARRAY = "absTypePoz";

    public static final int RESULT_BACK = 100;
    public static final int RESULT_SAVE = 101;

    // номер выбранного ученика
    int chosenLearnerPoz;

    // тост выводящийся после сохранения оценок
    Toast toast;


    // ---------
    // массив учеников и их оценок
    //static LearnerAndHisGrades[] learnerAndHisGrades;

    // id предмета
    static long subjectId;

    // ученики
    static long[] learnersId;
    static String[] learnersNames;

    // массив с TextView учеников
    static TextView[] learnersGradesTexts;

    // массив типов оценок
    static long[] gradesTypesId;
    static String[] gradesTypesNames;

    // массив типов пропусков
    static long[] absentTypesId;
    static String[] absentTypesNames;
    static String[] absentTypesLongNames;

    // размер максимальной оценки
    static int maxGrade;

    // массивы оценок и выбранных типов
    static int[][] grades;
    static int[][] gradesTypesIndexes;
    static int[] absentPoses;


    // создание активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // раздуваем layout
        setContentView(R.layout.leson_list_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar((Toolbar) findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(R.string.title_activity_lesson_list);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // базовый контейнер
        RelativeLayout out = findViewById(R.id.lesson_list_out);

        // скролящийся контейнер
        ScrollView scrollView = new ScrollView(this);
        out.addView(scrollView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        // поле вывода данных
        LinearLayout table = new LinearLayout(this);
        table.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(table, ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);


        // перед получением данных убеждаемся, что еще не получили их
        if (learnersId == null) {

            // todo будет время рассортируй по классам :)
            // получаем данные из intent
            subjectId = getIntent().getLongExtra(SUBJECT_ID, -1);

            // получаем учеников
            learnersId = getIntent().getLongArrayExtra(ARGS_INT_ARRAY_LEARNERS_ID);
            learnersNames = getIntent().getStringArrayExtra(ARGS_STRING_ARRAY_LEARNERS_NAMES);
            // убираем выбор с ученика
            chosenLearnerPoz = -1;

            // массив с TextView учеников
            learnersGradesTexts = new TextView[learnersId.length];

            // получаем массив предметов
            gradesTypesId = getIntent().getLongArrayExtra(INT_GRADES_TYPES_ID_ARRAY);
            gradesTypesNames = getIntent().getStringArrayExtra(STRING_GRADES_TYPES_ARRAY);


            // получаем размер максимальной оценки
            maxGrade = getIntent().getIntExtra(INT_MAX_GRADE, -1);

            // получаем массив данных о пропусках
            absentTypesId = getIntent().getLongArrayExtra(LONG_ABSENT_TYPES_ID_ARRAY);
            absentTypesNames = getIntent().getStringArrayExtra(STRING_ABSENT_TYPES_ARRAY);
            absentTypesLongNames = getIntent().getStringArrayExtra(STRING_ABSENT_TYPES_LONG_ARRAY);

            // получаем массив с пропусками
            absentPoses = getIntent().getIntArrayExtra(INT_ABSENT_TYPE_POZ_ARRAY);

            // получаем массивы оценок и выбранных типов
            grades = new int[learnersId.length][3];
            gradesTypesIndexes = new int[learnersId.length][3];
            {
                int[] grades1 = getIntent().getIntArrayExtra(FIRST_GRADES);
                int[] grades2 = getIntent().getIntArrayExtra(SECOND_GRADES);
                int[] grades3 = getIntent().getIntArrayExtra(THIRD_GRADES);

                int[] gradesTypes1 = getIntent().getIntArrayExtra(FIRST_GRADES_TYPES);
                int[] gradesTypes2 = getIntent().getIntArrayExtra(SECOND_GRADES_TYPES);
                int[] gradesTypes3 = getIntent().getIntArrayExtra(THIRD_GRADES_TYPES);

                for (int learnerI = 0; learnerI < learnersId.length; learnerI++) {
                    grades[learnerI][0] = grades1[learnerI];
                    grades[learnerI][1] = grades2[learnerI];
                    grades[learnerI][2] = grades3[learnerI];

                    gradesTypesIndexes[learnerI][0] = gradesTypes1[learnerI];
                    gradesTypesIndexes[learnerI][1] = gradesTypes2[learnerI];
                    gradesTypesIndexes[learnerI][2] = gradesTypes3[learnerI];
                }
            }
        }

        // выводим учеников и их оценки
        for (int learnersI = 0; learnersI < learnersId.length; learnersI++) {

            // контейнер ученика
            LinearLayout learnerRow = new LinearLayout(this);
            learnerRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams learnerRowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            learnerRowParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            learnerRowParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            learnerRowParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            learnerRowParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            table.addView(learnerRow, learnerRowParams);


            // текст имени ученика
            TextView learnerText = new TextView(this);
            learnerText.setSingleLine(true);
            learnerText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
            learnerText.setText(learnersNames[learnersI]);
            learnerText.setTextColor(Color.BLACK);
            learnerText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    4F
            );
            learnerTextParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            learnerRow.addView(learnerText, learnerTextParams);

            // текст с оценками
            TextView learnerGradesText = new TextView(this);
            learnersGradesTexts[learnersI] = learnerGradesText;
            learnerGradesText.setGravity(Gravity.CENTER);
            learnerGradesText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));

            // выводим оценки
            StringBuilder gradeText = new StringBuilder();

            if (absentPoses[learnersI] != -1) {// пропуск
                gradeText.append(absentTypesNames[absentPoses[learnersI]]);
            } else {// обычные оценки
                if (grades[learnersI][0] == 0 && grades[learnersI][1] == 0 && grades[learnersI][2] == 0) {
                    gradeText.append('-');
                } else {
                    if (grades[learnersI][0] != 0) {
                        gradeText.append(grades[learnersI][0]);
                        if (grades[learnersI][1] != 0 || grades[learnersI][2] != 0) {
                            gradeText.append(' ');
                        }
                    }
                    if (grades[learnersI][1] != 0) {
                        gradeText.append(grades[learnersI][1]);

                        if (grades[learnersI][2] != 0) {
                            gradeText.append(' ');
                        }
                    }
                    if (grades[learnersI][2] != 0) {
                        gradeText.append(grades[learnersI][2]);
                    }
                }
            }

            learnerGradesText.setText(gradeText.toString());
            learnerGradesText.setTextColor(Color.BLACK);
            learnerGradesText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            LinearLayout.LayoutParams learnerGradesTextParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    2F
            );
            learnerRow.addView(learnerGradesText, learnerGradesTextParams);

            // при нажатии на контейнер
            final int finalLearnersI = learnersI;
            learnerRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // ставим этого ученика как выбранного
                    chosenLearnerPoz = finalLearnersI;

                    // вызываем диалог оценок
                    GradeEditLessonDialogFragment gradeEditLessonDialogFragment = new GradeEditLessonDialogFragment();
                    // выводим аргументы
                    Bundle args = new Bundle();
                    args.putString(GradeEditLessonDialogFragment.ARGS_LEARNER_NAME, learnersNames[finalLearnersI]);
                    args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY, gradesTypesNames.clone());
                    args.putInt(GradeEditLessonDialogFragment.ARGS_INT_GRADES_ABSENT_TYPE_NUMBER, absentPoses[finalLearnersI]);
                    args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_ABSENT_TYPES_NAMES_ARRAY, absentTypesNames.clone());
                    args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY, absentTypesLongNames.clone());
                    args.putIntArray(GradeEditLessonDialogFragment.ARGS_INT_GRADES_ARRAY, grades[finalLearnersI].clone());
                    args.putInt(GradeEditLessonDialogFragment.ARGS_INT_MAX_GRADE, maxGrade);
                    args.putIntArray(GradeEditLessonDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY, gradesTypesIndexes[finalLearnersI].clone());
                    args.putInt(GradeEditLessonDialogFragment.ARGS_INT_CHOSEN_GRADE_POSITION, -1);

                    gradeEditLessonDialogFragment.setArguments(args);
                    // показываем диалог
                    gradeEditLessonDialogFragment.show(getFragmentManager(), "gradeDialogFragment - Hello");
                }
            });
        }

        // подсказка в низу
        TextView helpText = new TextView(this);
        helpText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
        helpText.setTextColor(getResources().getColor(R.color.backgroundLiteGray));
        helpText.setGravity(Gravity.CENTER);
        helpText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        helpText.setText(R.string.lesson_list_activity_text_help);
        //параметры
        LinearLayout.LayoutParams helpTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        helpTextParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        table.addView(helpText, helpTextParams);

        // тост говорящий, что оценки сохранены
        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.lesson_list_activity_menu_toast_grades_saved), Toast.LENGTH_SHORT);


        // кнопка сохранить
        //контейнер для кнопки
        LinearLayout saveButtonContainer = new LinearLayout(this);
        saveButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
        saveButtonContainer.setBackgroundResource(R.drawable._button_round_background_orange);
        saveButtonContainer.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams saveButtonContainerParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveButtonContainerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        saveButtonContainerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        saveButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.half_more_margin));
        //контейнер в диалог
        out.addView(saveButtonContainer, saveButtonContainerParams);


        //кнопка сохранения
        TextView saveButton = new TextView(this);
        saveButton.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
        saveButton.setGravity(Gravity.CENTER);
        saveButton.setText(getResources().getString(R.string.lesson_list_activity_menu_text_save));
        saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        saveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams saveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        saveButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.forth_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.forth_margin),
                (int) getResources().getDimension(R.dimen.simple_margin));
        saveButtonContainer.addView(saveButton, saveButtonParams);


        // нажатие на кнопку сохранить
        saveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i("TeachersApp", "LessonListActivity - button save click");

                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());

                // удаляем все предыдущие оценки в этом дне
                Cursor deleteGrades = db.getGradesBySubjectDateAndLesson(
                        subjectId,
                        getIntent().getStringExtra(LESSON_DATE),
                        getIntent().getIntExtra(LESSON_NUMBER, 0)
                );
                while (deleteGrades.moveToNext())
                    db.removeGrade(deleteGrades.getLong(deleteGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)));
                deleteGrades.close();


                // сохраняем оценки в бд
                for (int learnerI = 0; learnerI < learnersId.length; learnerI++) {
                    if (absentPoses[learnerI] != -1) {// пропуск
                        db.createGrade(
                                learnersId[learnerI],
                                0, 0, 0,
                                1, 1, 1,
                                absentTypesId[absentPoses[learnerI]],
                                subjectId,
                                getIntent().getStringExtra(LESSON_DATE),
                                getIntent().getIntExtra(LESSON_NUMBER, 0)
                        );
                    } else {
                        if (grades[learnerI][0] != 0 || grades[learnerI][1] != 0 || grades[learnerI][2] != 0) {
                            db.createGrade(
                                    learnersId[learnerI],
                                    grades[learnerI][0], grades[learnerI][1], grades[learnerI][2],
                                    gradesTypesId[gradesTypesIndexes[learnerI][0]], gradesTypesId[gradesTypesIndexes[learnerI][1]], gradesTypesId[gradesTypesIndexes[learnerI][2]],
                                    -1,
                                    subjectId,
                                    getIntent().getStringExtra(LESSON_DATE),
                                    getIntent().getIntExtra(LESSON_NUMBER, 0)
                            );
                        }
                    }
                    toast.show();
                }
                db.close();

                //очистка данных
                learnersId = null;

                // закрываем урок
                setResult(RESULT_SAVE);
                // выходим из активности
                finish();
            }
        });


    }


    // обратная связь от диалога оценок GradeDialogFragment
    @Override
    public void setGrades(int[] newGrades, int[] chosenTypesNumbers, int chosenAbsPoz) {

        // меняем массивы
        grades[chosenLearnerPoz] = newGrades;
        gradesTypesIndexes[chosenLearnerPoz] = chosenTypesNumbers;
        absentPoses[chosenLearnerPoz] = chosenAbsPoz;

        // меняем текст оценки выбранного ученика
        StringBuilder gradeText = new StringBuilder();
        // пропуск
        if (absentPoses[chosenLearnerPoz] != -1) {
            gradeText.append(absentTypesNames[absentPoses[chosenLearnerPoz]]);
        } else {// обычные оценки
            if (grades[chosenLearnerPoz][0] == 0 && grades[chosenLearnerPoz][1] == 0 && grades[chosenLearnerPoz][2] == 0) {
                gradeText.append('-');
            } else {
                if (grades[chosenLearnerPoz][0] != 0) {
                    gradeText.append(grades[chosenLearnerPoz][0]);
                    if (grades[chosenLearnerPoz][1] != 0 || grades[chosenLearnerPoz][2] != 0) {
                        gradeText.append(' ');
                    }
                }
                if (grades[chosenLearnerPoz][1] != 0) {
                    gradeText.append(grades[chosenLearnerPoz][1]);

                    if (grades[chosenLearnerPoz][2] != 0) {
                        gradeText.append(' ');
                    }
                }
                if (grades[chosenLearnerPoz][2] != 0) {
                    gradeText.append(grades[chosenLearnerPoz][2]);
                }
            }
        }
        learnersGradesTexts[chosenLearnerPoz].setText(gradeText.toString());

        // убираем выбор с ученика
        chosenLearnerPoz = -1;
    }


    // кнопка назад в actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        // очистка данных
        learnersId = null;
        // возвращаемся к уроку
        setResult(RESULT_BACK);
        // выходим из активности
        super.onBackPressed();
    }


}


