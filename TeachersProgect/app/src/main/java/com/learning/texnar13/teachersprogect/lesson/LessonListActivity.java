package com.learning.texnar13.teachersprogect.lesson;

import android.content.SharedPreferences;
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


//    public static final String ARGS_STRING_ARRAY_LEARNERS_NAMES = "learnersNames";
//    public static final String ARGS_INT_ARRAY_LEARNERS_ID = "learnersID";
//    public static final String STRING_GRADES_TYPES_ARRAY = "gradesTypesArray";
//    public static final String INT_GRADES_TYPES_ID_ARRAY = "gradesTypesIdArray";
//    public static final String STRING_ABSENT_TYPES_ARRAY = "absentTypesArray";
//    public static final String STRING_ABSENT_TYPES_LONG_ARRAY = "absentTypesLongArray";
//    public static final String LONG_ABSENT_TYPES_ID_ARRAY = "absentTypesIdArray";
//    public static final String INT_MAX_GRADE = "maxGrade";
//
//    public static final String FIRST_GRADES = "grades1";
//    public static final String SECOND_GRADES = "grades2";
//    public static final String THIRD_GRADES = "grades3";
//    public static final String FIRST_GRADES_TYPES = "gradesTypes1";
//    public static final String SECOND_GRADES_TYPES = "gradesTypes2";
//    public static final String THIRD_GRADES_TYPES = "gradesTypes3";
//    public static final String INT_ABSENT_TYPE_POZ_ARRAY = "absTypePoz";

    public static final int RESULT_BACK = 100;
    public static final int RESULT_SAVE = 101;

    // номер выбранного ученика
    int chosenLearnerPos;


    // ---------
    // массив учеников и их оценок
    //static LearnerAndHisGrades[] learnerAndHisGrades;

//    // id предмета
//    static long subjectId;
//
//    // ученики
//    static long[] learnersId;
//    static String[] learnersNames;
//
//    // массив с TextView учеников
//    static TextView[] learnersGradesTexts;
//
//    // массив типов оценок
//    static long[] gradesTypesId;
//    static String[] gradesTypesNames;
//
//    // массив типов пропусков
//    static long[] absentTypesId;
//    static String[] absentTypesNames;
//    static String[] absentTypesLongNames;
//
//    // размер максимальной оценки
//    static int maxGrade;
//
//    // массивы оценок и выбранных типов
//    static int[][] grades;
//    static int[][] gradesTypesIndexes;
//    static int[] absentPoses;


    private static final String IS_HELP_TEXT_SHOWED_TAG = "is_lesson_list_help_text_showed";

    // создание активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // раздуваем layout
        setContentView(R.layout.lesson_list_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar((Toolbar) findViewById(R.id.base_blue_toolbar));
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
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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
        subjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(
                SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID
        ));
        attitudeCursor.close();

        // получаем предмет
        Cursor subjectCursor = db.getSubjectById(subjectId);
        subjectCursor.moveToFirst();
        // получаем id класса
        learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(
                SchoolContract.TableSubjects.KEY_CLASS_ID
        ));
        subjectCursor.close();


        // макс. оценка, типы пропусков и оценок
        graduationSettings = new GraduationSettings();
        // максимальная оценка
        graduationSettings.maxAnswersCount = db.getSettingsMaxGrade(1);
        // названия типов ответов
        Cursor typesCursor = db.getGradesTypes();
        graduationSettings.answersTypes = new GraduationSettings.AnswersType[typesCursor.getCount()];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < typesCursor.getCount(); typeI++) {
            typesCursor.moveToNext();
            // добавляем новый тип во внутренний список
            graduationSettings.answersTypes[typeI] = new GraduationSettings.AnswersType(
                    typesCursor.getLong(typesCursor.getColumnIndex(
                            SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID)),
                    typesCursor.getString(typesCursor.getColumnIndex(
                            SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE))
            );
        }
        typesCursor.close();
        // названия типов пропусков
        Cursor typesAbsCursor = db.getAbsentTypes();
        graduationSettings.absentTypes = new GraduationSettings.AbsentType[typesAbsCursor.getCount()];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < typesAbsCursor.getCount(); typeI++) {
            typesAbsCursor.moveToNext();
            // добавляем новый тип во внутренний список
            graduationSettings.absentTypes[typeI] = new GraduationSettings.AbsentType(
                    typesAbsCursor.getLong(typesAbsCursor.getColumnIndex(
                            SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID)),
                    typesAbsCursor.getString(typesAbsCursor.getColumnIndex(
                            SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME)),
                    typesAbsCursor.getString(typesAbsCursor.getColumnIndex(
                            SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME))
            );
        }
        typesAbsCursor.close();

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
        //заполняем его
        for (int learnerPoz = 0; learnerPoz < learners.length; learnerPoz++) {
            learnersCursor.moveToNext();

            // получаем id
            long learnerId = learnersCursor.getLong(learnersCursor.getColumnIndex(
                    SchoolContract.TableLearners.KEY_LEARNER_ID
            ));

            // получаем имя
            String name = learnersCursor.getString(learnersCursor.getColumnIndex(
                    SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " +
                    learnersCursor.getString(learnersCursor.getColumnIndex(
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
                gradeId = grades.getLong(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID));
                // получаем id пропуска
                int absId;
                if (grades.isNull(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID))) {
                    absId = -1;
                } else
                    absId = grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID));

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
                                grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[gradeI]));
                        // тип оценки
                        long typeId = grades.getLong(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[gradeI]));
                        // проходимся в цикле по всем типам оценок запоминая номер попавшегося
                        gradeUnits[gradeI].gradeTypePoz = -1;// единица всегда должна перекрываться другим значение иначе будет ошибка

                        for (int answerI = 0; answerI < graduationSettings.answersTypes.length
                                && gradeUnits[gradeI].gradeTypePoz == -1; answerI++)
                            if (graduationSettings.answersTypes[answerI].id == typeId)
                                gradeUnits[gradeI].gradeTypePoz = answerI;
                    }
                }
            } else {// если не проставлялись оставляем пустые значения
                gradeUnits = new LessonListLearnerAndGradesData.GradeUnit[3];
            }
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
            if (!sharedPreferences.contains(IS_HELP_TEXT_SHOWED_TAG)) {
                currentValue = 1;
            } else {
                currentValue = sharedPreferences.getInt(IS_HELP_TEXT_SHOWED_TAG, 0) + 1;
            }
            if (currentValue <= 2) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(IS_HELP_TEXT_SHOWED_TAG, currentValue);
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
            if (learners[learnersI].gradeUnit[0].grade == 0 &&
                    learners[learnersI].gradeUnit[1].grade == 0 &&
                    learners[learnersI].gradeUnit[2].grade == 0) {
                gradeText.append('-');
            } else {
                if (learners[learnersI].gradeUnit[0].grade != 0) {
                    gradeText.append(learners[learnersI].gradeUnit[0].grade);
                    if (learners[learnersI].gradeUnit[1].grade != 0 || learners[learnersI].gradeUnit[2].grade != 0) {
                        gradeText.append(' ');
                    }
                }
                if (learners[learnersI].gradeUnit[1].grade != 0) {
                    gradeText.append(learners[learnersI].gradeUnit[1].grade);

                    if (learners[learnersI].gradeUnit[2].grade != 0) {
                        gradeText.append(' ');
                    }
                }
                if (learners[learnersI].gradeUnit[2].grade != 0) {
                    gradeText.append(learners[learnersI].gradeUnit[2].grade);
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
                    curLearner.gradeUnit[i].grade = newGrades[i];
                    curLearner.gradeUnit[i].gradeTypePoz = chosenTypesNumbers[i];
                }

            } else {// стоит пропуск

                // меняем списки
                curLearner.absTypePozNumber = chosenAbsPoz;
                for (int i = 0; i < 3; i++) {
                    curLearner.gradeUnit[i].grade = 0;
                    curLearner.gradeUnit[i].gradeTypePoz = 1;
                }
            }

            // сохраняем значения в бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            if (curLearner.gradeId == -1) {
                db.createGrade(
                        curLearner.learnerId,
                        curLearner.gradeUnit[0].grade,
                        curLearner.gradeUnit[1].grade,
                        curLearner.gradeUnit[2].grade,
                        curLearner.gradeUnit[0].gradeTypePoz,
                        curLearner.gradeUnit[1].gradeTypePoz,
                        curLearner.gradeUnit[2].gradeTypePoz,
                        (chosenAbsPoz == -1) ? (-1) : (graduationSettings.absentTypes[chosenAbsPoz].id),
                        subjectId, lessonDate, lessonNumber
                );
            } else {
                db.editGrade(curLearner.gradeId,
                        curLearner.gradeUnit[0].grade,
                        curLearner.gradeUnit[1].grade,
                        curLearner.gradeUnit[2].grade,
                        curLearner.gradeUnit[0].gradeTypePoz,
                        curLearner.gradeUnit[1].gradeTypePoz,
                        curLearner.gradeUnit[2].gradeTypePoz,
                        (chosenAbsPoz == -1) ? (-1) : (graduationSettings.absentTypes[chosenAbsPoz].id));
            }
            db.close();

            // выводим изменения в интерфейс
            writeGradesToViewByPoz(chosenAbsPoz);
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


    private static class LessonListLearnerAndGradesData {
        // параметры ученика
        long learnerId;
        String name;

        // id оценки
        long gradeId;
        // массив оценок
        GradeUnit[] gradeUnit;
        // тип пропуска
        int absTypePozNumber;

        // View в котором выводится оценка
        TextView gradesView;

        private LessonListLearnerAndGradesData(long learnerId, String name,
                                               long gradeId, GradeUnit[] gradeUnit, int absTypePozNumber) {
            this.learnerId = learnerId;
            this.name = name;
            this.gradeId = gradeId;
            this.gradeUnit = gradeUnit;
            this.absTypePozNumber = absTypePozNumber;
        }

        // заготовка аргументов для диалога оценок
        int[] getGradesArray() {
            int[] result = new int[gradeUnit.length];
            for (int i = 0; i < result.length; i++) result[i] = gradeUnit[i].grade;
            return result;
        }

        int[] getGradesTypesArray() {
            int[] result = new int[gradeUnit.length];
            for (int i = 0; i < result.length; i++) result[i] = gradeUnit[i].gradeTypePoz;
            return result;
        }

        static class GradeUnit {
            // оценка
            int grade;
            // номер типа оценки
            int gradeTypePoz;

            public GradeUnit() {
                this.grade = 0;
                this.gradeTypePoz = 1;
            }
        }
    }

    private static class GraduationSettings {
        // размер максимальной оценки
        int maxAnswersCount;
        // массив типов оценок
        AnswersType[] answersTypes;
        // массив типов пропусков
        AbsentType[] absentTypes;

        // заготовка аргументов для диалога оценок
        String[] getAnswersTypesArray() {
            String[] result = new String[answersTypes.length];
            for (int i = 0; i < result.length; i++) result[i] = answersTypes[i].typeName;
            return result;
        }

        String[] getAbsentTypesLongNames() {
            String[] result = new String[absentTypes.length];
            for (int i = 0; i < result.length; i++) result[i] = absentTypes[i].typeAbsLongName;
            return result;
        }

        // класс для хранения типов ответов
        static class AnswersType {
            long id;
            String typeName;

            AnswersType(long id, String typeName) {
                this.id = id;
                this.typeName = typeName;
            }
        }

        // класс для хранения типов пропусков
        static class AbsentType {
            long id;
            String typeAbsName;
            String typeAbsLongName;

            AbsentType(long id, String typeAbsName, String typeAbsLongName) {
                this.id = id;
                this.typeAbsName = typeAbsName;
                this.typeAbsLongName = typeAbsLongName;
            }
        }
    }
}


