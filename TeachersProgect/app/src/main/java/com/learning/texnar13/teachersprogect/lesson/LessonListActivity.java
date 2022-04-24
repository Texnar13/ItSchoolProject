package com.learning.texnar13.teachersprogect.lesson;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;

public class LessonListActivity extends AppCompatActivity implements GradesDialogInterface {


    // данные об урооке
    private static long lessonAttitudeId;
    private static String lessonDate;
    private static int lessonNumber;
    private static long subjectId;
    private static long learnersClassId;

    // макс. оценка, типы пропусков и оценок
    private static GraduationSettings graduationSettings;

    // данные учеников
    LessonListLearnerAndGradesData[] learners;

    public static final int RESULT_BACK = 100;
    public static final int RESULT_SAVE = 101;

    // номер выбранного ученика
    int chosenLearnerPos;


    // создание активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // раздуваем layout
        setContentView(R.layout.lesson_list_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar(findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свойAction
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }
        ((TextView) findViewById(R.id.base_blue_toolbar_title))
                .setText(R.string.title_activity_lesson_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.base_background_color, getTheme()));

            // включен ли ночной режим
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            if (Configuration.UI_MODE_NIGHT_YES != currentNightMode)
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        // получаем зависимость из intent
        lessonAttitudeId = getIntent().getLongExtra(LessonActivity.ARGS_LESSON_ATTITUDE_ID, -1);
        // -1 он будет равен только при ошибке
        if (lessonAttitudeId == -1) {
            finish();
            return;
        }

        // проверяем был создан новый экран или он просто переворачивался
        if (savedInstanceState == null) {
            // получаем данные из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            initData(db);
            db.close();
        }

        // контейнер вывода учеников
        LinearLayout outContainer =
                findViewById(R.id.lesson_list_activity_out_container);
        // выводим учеников
        outLearnersView(outContainer);

        // кнопка сохранить изменения
        findViewById(R.id.lesson_list_activity_button_save).setOnClickListener(v -> {
            // передаем в предыдущую активность сообщение, чтобы она закрылась
            setResult(RESULT_SAVE);
            // выходим из активности
            finish();
        });
    }


    // ------------------------------------- подгрузка данных -------------------------------------
    // начальная подгрузка данных из бд
    void initData(DataBaseOpenHelper db) {

        // получаем переданную дату урока
        lessonDate = getIntent().getStringExtra(LessonActivity.ARGS_LESSON_DATE);
        lessonNumber = getIntent().getIntExtra(LessonActivity.ARGS_LESSON_NUMBER, 0);
        if (lessonDate.equals("")) {
            finish();
        }

        // получаем зависимость
        Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(lessonAttitudeId);
        attitudeCursor.moveToFirst();
        // получаем из завмсимости id предмета
        subjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndexOrThrow(
                SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID
        ));
        attitudeCursor.close();

        // получаем предмет
        Cursor subjectCursor = db.getSubjectById(subjectId);
        subjectCursor.moveToFirst();
        // получаем id класса
        learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndexOrThrow(
                SchoolContract.TableSubjects.KEY_CLASS_ID
        ));
        subjectCursor.close();

        // макс. оценка, типы пропусков и оценок
        graduationSettings = GraduationSettings.newInstance(db);

        // номер выбранного ученика
        chosenLearnerPos = -1;
        // инициализируем учеников
        initLearners(db);
        db.close();
    }

    // инициализируем учеников
    private void initLearners(DataBaseOpenHelper db) {

        // получаем учеников по id класса
        Cursor learnersCursor = db.getLearnersByClassId(learnersClassId);
        // инициализируем массив с учениками
        learners = new LessonListLearnerAndGradesData[learnersCursor.getCount()];
        // заполняем его
        for (int learnerPoz = 0; learnerPoz < learners.length; learnerPoz++) {
            learnersCursor.moveToNext();

            // получаем id
            long learnerId = learnersCursor.getLong(learnersCursor.getColumnIndexOrThrow(
                    SchoolContract.TableLearners.KEY_ROW_ID
            ));

            // получаем имя
            String name = learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(
                    SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " +
                    learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(
                            SchoolContract.TableLearners.COLUMN_FIRST_NAME));

            // инициализируем поля оценок
            int absTypePozNumber = -1;
            LessonListLearnerAndGradesData.GradeUnit[] gradeUnits = new LessonListLearnerAndGradesData.GradeUnit[3];
            for (int j = 0; j < 3; j++)
                gradeUnits[j] = new LessonListLearnerAndGradesData.GradeUnit();

            // получаем оценки ученика за этот урок
            Cursor grades = db.getGradesByLearnerIdSubjectDateAndLesson(learnerId, subjectId, lessonDate, lessonNumber);
            long gradeId = -1;
            if (grades.moveToNext()) {// если оценки за этот урок уже проставлялись
                // id самой оценки
                gradeId = grades.getLong(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID));
                // получаем id пропуска
                int absId;
                if (grades.isNull(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID))) {
                    absId = -1;
                } else
                    absId = grades.getInt(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID));

                if (absId != -1) {// если стоит пропуск
                    // ищем в списках пропуск с таким же id
                    for (int absentI = 0; absentI < graduationSettings.absentTypes.length
                            && absTypePozNumber == -1; absentI++)
                        if (graduationSettings.absentTypes[absentI].id == absId)
                            absTypePozNumber = absentI;
                } else {
                    // если пропуска нет
                    for (int gradeI = 0; gradeI < 3; gradeI++) {
                        // оценка
                        gradeUnits[gradeI].grade =
                                grades.getInt(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[gradeI]));
                        // тип оценки
                        long typeId = grades.getLong(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[gradeI]));
                        // проходимся в цикле по всем типам оценок запоминая номер попавшегося
                        gradeUnits[gradeI].gradeTypePoz = -1;// единица всегда должна перекрываться другим значение иначе будет ошибка

                        for (int answerI = 0; answerI < graduationSettings.answersTypes.length
                                && gradeUnits[gradeI].gradeTypePoz == -1; answerI++)
                            if (graduationSettings.answersTypes[answerI].id == typeId)
                                gradeUnits[gradeI].gradeTypePoz = answerI;
                    }
                }
            }// если не проставлялись оставляем пустые значения

            grades.close();

            // создаем нового ученика
            learners[learnerPoz] = new LessonListLearnerAndGradesData(
                    learnerId,
                    name,
                    gradeId,
                    gradeUnits,
                    absTypePozNumber
            );
        }
        learnersCursor.close();
    }


    // ----------------------------------- вывод данных во view -----------------------------------
    // выводим учеников и их оценки
    private void outLearnersView(LinearLayout out) {
        // выводим учеников и их оценки
        for (int learnersI = 0; learnersI < learners.length; learnersI++) {

            // раздуваем элемент ученика
            View element = getLayoutInflater().inflate(R.layout.lesson_list_pattern_learner, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.lesson_list_element_height)
            );
            out.addView(element, params);

            // выводим имя ученика
            ((TextView) element.findViewById(R.id.lesson_list_element_pattern_name)).setText(learners[learnersI].name);

            // выводим его оценки
            learners[learnersI].gradesView =
                    element.findViewById(R.id.lesson_list_element_pattern_grade);
            writeGradesToViewByPoz(learnersI);


            // при нажатии на контейнер
            final int finalLearnersI = learnersI;
            element.setOnClickListener(v -> {
                // ставим этого ученика как выбранного
                chosenLearnerPos = finalLearnersI;
                // вызываем диалог оценок
                GradeEditLessonDialogFragment gradeEditLessonDialogFragment = new GradeEditLessonDialogFragment();
                // выводим аргументы
                Bundle args = new Bundle();
                args.putString(GradeEditLessonDialogFragment.ARGS_LEARNER_NAME, learners[finalLearnersI].name);
                args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY, graduationSettings.getAnswersTypesArray());
                args.putInt(GradeEditLessonDialogFragment.ARGS_INT_GRADES_ABSENT_TYPE_NUMBER, learners[finalLearnersI].absTypePozNumber);
                args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY, graduationSettings.getAbsentTypesLongNames());
                args.putIntArray(GradeEditLessonDialogFragment.ARGS_INT_GRADES_ARRAY, learners[finalLearnersI].getGradesArray());
                args.putInt(GradeEditLessonDialogFragment.ARGS_INT_MAX_GRADE, graduationSettings.maxAnswersCount);
                args.putIntArray(GradeEditLessonDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY, learners[finalLearnersI].getGradesTypesArray());
                // этот диалог работает и в уроке, по этому нужно передавать номер главной оценки (а в этой активности их нет)
                args.putInt(GradeEditLessonDialogFragment.ARGS_INT_CHOSEN_GRADE_POSITION, -1);
                gradeEditLessonDialogFragment.setArguments(args);
                // показываем диалог
                gradeEditLessonDialogFragment.show(getFragmentManager(), "gradeDialogFragment - Hello");
            });
        }
        // подсказка в низу
        {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int currentValue;
            if (!sharedPreferences.contains(SharedPrefsContract.IS_LESSON_LIST_HELP_TEXT_SHOWED_TAG)) {
                currentValue = 1;
            } else {
                currentValue = sharedPreferences.getInt(SharedPrefsContract.IS_LESSON_LIST_HELP_TEXT_SHOWED_TAG, 0) + 1;
            }
            if (currentValue <= 2) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(SharedPrefsContract.IS_LESSON_LIST_HELP_TEXT_SHOWED_TAG, currentValue);
                editor.apply();

                TextView helpText = new TextView(this);
                helpText.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_medium));
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
                out.addView(helpText, helpTextParams);
            }
        }
    }

    private void writeGradesToViewByPoz(int learnersI) {

        // выводим оценки
        StringBuilder gradeText = new StringBuilder();
        if (learners[learnersI].absTypePozNumber == -1) {// обычные оценки
            if (learners[learnersI].gradesUnits[0].grade == 0 &&
                    learners[learnersI].gradesUnits[1].grade == 0 &&
                    learners[learnersI].gradesUnits[2].grade == 0) {
                gradeText.append('-');
            } else {
                if (learners[learnersI].gradesUnits[0].grade != 0) {
                    gradeText.append(learners[learnersI].gradesUnits[0].grade);
                    if (learners[learnersI].gradesUnits[1].grade != 0 || learners[learnersI].gradesUnits[2].grade != 0) {
                        gradeText.append(' ');
                    }
                }
                if (learners[learnersI].gradesUnits[1].grade != 0) {
                    gradeText.append(learners[learnersI].gradesUnits[1].grade);

                    if (learners[learnersI].gradesUnits[2].grade != 0) {
                        gradeText.append(' ');
                    }
                }
                if (learners[learnersI].gradesUnits[2].grade != 0) {
                    gradeText.append(learners[learnersI].gradesUnits[2].grade);
                }
            }
        } else {// пропуск
            gradeText.append(graduationSettings.absentTypes[learners[learnersI].absTypePozNumber].typeAbsName);
        }

        learners[learnersI].gradesView.setText(gradeText.toString());
    }


    // -------------------------------------- обратная связь --------------------------------------
    // обратная связь от диалога оценок GradeDialogFragment
    @Override
    public void setGrades(int[] newGrades, int[] chosenTypesNumbers, int chosenAbsPoz) {
        if (chosenLearnerPos != -1) {
            LessonListLearnerAndGradesData curLearner = learners[chosenLearnerPos];

            // если стоят оценки
            if (chosenAbsPoz == -1) {

                // меняем списки
                curLearner.absTypePozNumber = -1;
                for (int i = 0; i < 3; i++) {
                    curLearner.gradesUnits[i].grade = newGrades[i];
                    curLearner.gradesUnits[i].gradeTypePoz = chosenTypesNumbers[i];
                }

            } else {// стоит пропуск

                // меняем списки
                curLearner.absTypePozNumber = chosenAbsPoz;
                for (int i = 0; i < 3; i++) {
                    curLearner.gradesUnits[i].grade = 0;
                    curLearner.gradesUnits[i].gradeTypePoz = 0;
                }
            }

            // сохраняем значения в бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            if (curLearner.gradeId == -1) {
                curLearner.gradeId = db.createGrade(
                        curLearner.learnerId,
                        curLearner.gradesUnits[0].grade,
                        curLearner.gradesUnits[1].grade,
                        curLearner.gradesUnits[2].grade,
                        graduationSettings.answersTypes[curLearner.gradesUnits[0].gradeTypePoz].id,
                        graduationSettings.answersTypes[curLearner.gradesUnits[1].gradeTypePoz].id,
                        graduationSettings.answersTypes[curLearner.gradesUnits[2].gradeTypePoz].id,
                        (chosenAbsPoz == -1) ? (-1) : (graduationSettings.absentTypes[chosenAbsPoz].id),
                        subjectId, lessonDate, lessonNumber
                );
            } else {
                // если все поля нулевые удаляем оценку
                if (curLearner.gradesUnits[0].grade == 0 && curLearner.gradesUnits[1].grade == 0 &&
                        curLearner.gradesUnits[2].grade == 0 && chosenAbsPoz == -1) {
                    db.removeGrade(curLearner.gradeId);
                } else
                    db.editGrade(curLearner.gradeId,
                            curLearner.gradesUnits[0].grade,
                            curLearner.gradesUnits[1].grade,
                            curLearner.gradesUnits[2].grade,
                            graduationSettings.answersTypes[curLearner.gradesUnits[0].gradeTypePoz].id,
                            graduationSettings.answersTypes[curLearner.gradesUnits[1].gradeTypePoz].id,
                            graduationSettings.answersTypes[curLearner.gradesUnits[2].gradeTypePoz].id,
                            (chosenAbsPoz == -1) ? (-1) : (graduationSettings.absentTypes[chosenAbsPoz].id));
            }
            db.close();

            // выводим изменения в интерфейс
            writeGradesToViewByPoz(chosenLearnerPos);
        }
        // убираем выбор с ученика
        chosenLearnerPos = -1;
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
        // возвращаемся к уроку
        // (этот код не обрабатывается активностью урока, и она не закрывается)
        setResult(RESULT_BACK);
        // выходим из активности
        super.onBackPressed();
    }


    static class LessonListLearnerAndGradesData {
        // параметры ученика
        long learnerId;
        String name;

        // id оценки
        long gradeId;
        // массив оценок
        GradeUnit[] gradesUnits;
        // тип пропуска
        int absTypePozNumber;

        // View в котором выводится оценка
        TextView gradesView;

        private LessonListLearnerAndGradesData(long learnerId, String name,
                                               long gradeId, GradeUnit[] gradesUnits, int absTypePozNumber) {
            this.learnerId = learnerId;
            this.name = name;
            this.gradeId = gradeId;
            this.gradesUnits = gradesUnits;
            this.absTypePozNumber = absTypePozNumber;
        }

        // заготовка аргументов для диалога оценок
        int[] getGradesArray() {
            int[] result = new int[gradesUnits.length];
            for (int i = 0; i < result.length; i++) result[i] = gradesUnits[i].grade;
            return result;
        }

        int[] getGradesTypesArray() {
            int[] result = new int[gradesUnits.length];
            for (int i = 0; i < result.length; i++) result[i] = gradesUnits[i].gradeTypePoz;
            return result;
        }

        static class GradeUnit {
            // оценка
            int grade;
            // номер типа оценки
            int gradeTypePoz;

            public GradeUnit() {
                this.grade = 0;
                this.gradeTypePoz = 0;
            }
        }
    }

}


