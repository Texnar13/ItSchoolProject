package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.SubjectsDialog.SubjectsDialogFragment;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics.LearnersGradesStatisticsActivity;
import com.learning.texnar13.teachersprogect.SubjectsDialog.SubjectsDialogInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class LearnersAndGradesActivity extends AppCompatActivity implements CreateLearnerInterface, EditLearnerDialogInterface, EditGradeDialogInterface, UpdateTableInterface, AllowEditGradesInterface, View.OnTouchListener, SubjectsDialogInterface {

    // ------------ константы ------------

    // тег для логов
    private static final String TAG = "TeachersApp";
    // константа по которой через intent передается id класса
    public static final String CLASS_ID = "classId";

    // ------------ данные из таблицы ------------

    // id класса полученное через intent
    static long classId = -1;

    // данные об учениках оценках и уроках
    static DataObject dataLearnersAndGrades;
    // переменная работающего в текущий момент потока
    static GetGradesThread loadGradesThread = null;

    // массив с предметами
    static NewSubjectUnit[] subjects;
    // номер выбранного предмета в массиве
    int chosenSubjectPosition;

    // названия типов ответов и их id
    static AnswersType[] answersTypes;
    // названия типов пропусков и их id
    static AbsentType[] absentTypes;
    // календарь хранящий в себе отображаемую дату
    static GregorianCalendar viewCalendar = null;
    // текущая дата
    GregorianCalendar currentCalendar;

    // ------------ данные для вывода графики ------------

    // локализованные названия месяцев
    String[] monthsNames;
    String[] transformedMonthsNames;
    // размер максимальной оценки
    int maxGrade;

    // view с таблицей
    static LearnersAndGradesTableView learnersAndGradesTableView;

    // текст вызывающий диалог предметов и отображающий текущий предмет
    TextView subjectTextView;


// -------------------------- меню --------------------------

    //раздуваем меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.learners_and_grades_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //назначаем функции меню
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu");
        //кнопка статистики
        menu.findItem(R.id.learners_and_grades_menu_statistics).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                // выбран ли урок
                if (chosenSubjectPosition < 0) {
                    // создаем диалог работы с предметами
                    SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();

                    // готоим и передаем массив названий предиметов
                    Bundle args = new Bundle();
                    String[] subjectsArray = new String[subjects.length];
                    for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                        subjectsArray[subjectI] = subjects[subjectI].getSubjectName();
                    }
                    args.putStringArray(SubjectsDialogFragment.ARGS_LEARNERS_NAMES_STRING_ARRAY, subjectsArray);
                    subjectsDialogFragment.setArguments(args);

                    // показываем диалог
                    subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment - hello");

                } else {
                    // запускаем активность статистики
                    Intent intent = new Intent(
                            LearnersAndGradesActivity.this,
                            LearnersGradesStatisticsActivity.class
                    );
                    // и передаем ему id предмета
                    intent.putExtra(
                            LearnersGradesStatisticsActivity.INTENT_SUBJECT_ID,
                            subjects[chosenSubjectPosition].getSubjectId()
                    );
                    startActivity(intent);
                }
                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }


// -------------------------- создание активности --------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // раздуваем layout
        setContentView(R.layout.activity_learners_and_grades);
        // раздуваем тулбар
        Toolbar toolbar = /*(Toolbar)*/ findViewById(R.id.cabinets_out_toolbar);
        setSupportActionBar(toolbar);
        // кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // кнопка назад
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.__button_back_arrow_blue));
            if (toolbar.getOverflowIcon() != null)
                toolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.baseBlue), PorterDuff.Mode.SRC_ATOP);
        }

        // получаем id класса
        classId = getIntent().getLongExtra(CLASS_ID, -1);
        if (classId == -1) {// выходим если не передан класс
            finish();
        }

        // получаем название класса из бд и ставим в заголовок
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        //название класса
        Cursor classCursor = db.getLearnersClass(classId);
        classCursor.moveToFirst();
        ((TextView) findViewById(R.id.learners_and_grades_activity_text_title)).setText(
                classCursor.getString(
                        classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)
                )
        );
        classCursor.close();

        // параллельно находим максимальную оценку
        maxGrade = db.getSettingsMaxGrade(1);

        // названия типов ответов
        Cursor typesCursor = db.getGradesTypes();
        answersTypes = new AnswersType[typesCursor.getCount()];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < answersTypes.length; typeI++) {
            typesCursor.moveToPosition(typeI);
            // добавляем новый тип во внутренний список
            answersTypes[typeI] = new AnswersType(
                    typesCursor.getLong(typesCursor.getColumnIndex(SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID)),
                    typesCursor.getString(typesCursor.getColumnIndex(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE))
            );
        }
        typesCursor.close();

        // названия типов пропусков
        Cursor absCursor = db.getAbsentTypes();
        absentTypes = new AbsentType[absCursor.getCount()];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < absentTypes.length; typeI++) {
            absCursor.moveToPosition(typeI);
            // добавляем новый тип во внутренний список
            absentTypes[typeI] = new AbsentType(
                    absCursor.getLong(absCursor.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID)),
                    absCursor.getString(absCursor.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME)),
                    absCursor.getString(absCursor.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME))

            );
        }
        absCursor.close();


        // находим view с таблицей
        learnersAndGradesTableView = findViewById(R.id.learners_and_grades_activity_table_view);
        // получаем размер максимальной оценки
        if (db.getSettingsAreTheGradesColoredByProfileId(1)) {
            learnersAndGradesTableView.setMaxAnswersCount(maxGrade);
        } else {
            // не раскрашиваем оценки в таблице
            learnersAndGradesTableView.setMaxAnswersCount(-1);
        }
        // назначаем талице обработчик касаний
        learnersAndGradesTableView.setOnTouchListener(this);
        db.close();


        // находим кнопку вызова диалога предметов и ставим ей обработчик
        subjectTextView = findViewById(R.id.learners_and_grades_activity_subject_text_button);
        subjectTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // создаем диалог работы с предметами
                SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();

                // готоим и передаем массив названий предиметов и позицию выбранного предмета
                Bundle args = new Bundle();
                String[] subjectsArray = new String[subjects.length];
                for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                    subjectsArray[subjectI] = subjects[subjectI].getSubjectName();
                }
                args.putStringArray(SubjectsDialogFragment.ARGS_LEARNERS_NAMES_STRING_ARRAY, subjectsArray);
                subjectsDialogFragment.setArguments(args);

                // показываем диалог
                subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment - hello");
            }
        });

        // ---- переключение даты ----

        // инициализируем календарь текущей датой если он пуст, если не пуст, то был перевернут экран
        if (viewCalendar == null) {
            viewCalendar = new GregorianCalendar();
            viewCalendar.setTime(new Date());
            viewCalendar.setLenient(false);
            viewCalendar.set(Calendar.MILLISECOND, 0);
            // выствляем первый день, чтобы небыло путанницы с днями при переключении даты
            viewCalendar.set(Calendar.DAY_OF_MONTH, 1);
        }

        // выставляем в таблицу текущую дату
        currentCalendar = new GregorianCalendar();
        currentCalendar.setTime(new Date());
        currentCalendar.setLenient(false);
//        // если отображаемый календарь уже был создан, сравниваем с ним текущую дату
//        if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
//                viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
//            // текущая дата
//            learnersAndGradesTableView.currentDate = currentCalendar.get(Calendar.DAY_OF_MONTH) - 1;
//            // текущий урок
//            int[][] times = db.getSettingsTime(1);// стандартное время уроков
//            int lessonNumber = 0;// текущий урок
//            for (int lessonI = 0; lessonI < times.length; lessonI++) {
//                if ((currentCalendar.get(Calendar.HOUR_OF_DAY) > times[lessonI][0] ||
//                        (currentCalendar.get(Calendar.HOUR_OF_DAY) == times[lessonI][0] && currentCalendar.get(Calendar.MINUTE) >= times[lessonI][1])) &&
//                        (currentCalendar.get(Calendar.HOUR_OF_DAY) < times[lessonI][2] || (currentCalendar.get(Calendar.HOUR_OF_DAY) == times[lessonI][2] && currentCalendar.get(Calendar.MINUTE) <= times[lessonI][3]))
//                ) {
//                    lessonNumber = lessonI;
//                }
//            }
//            learnersAndGradesTableView.currentLesson = lessonNumber;
//        } else
//            learnersAndGradesTableView.currentDate = -1;


        // получаем локализованные названия месяцев
        monthsNames = getResources().getStringArray(R.array.months_names);
        transformedMonthsNames = getResources().getStringArray(R.array.months_names_low_case);
        // текстовое поле под название даты
        final TextView dateText = findViewById(R.id.learners_and_grades_activity_date_text);
        dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));

        // кнопки переключения даты
        // кнопка назад
        findViewById(R.id.learners_and_grades_activity_button_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // останавливаем поток загрузки оценок и уничтожаем все его данные
                if (loadGradesThread != null) {
                    loadGradesThread.stopThread();
                    loadGradesThread = null;
                }

                // переключаем дату
                if (viewCalendar.get(Calendar.MONTH) == Calendar.JANUARY) {
                    viewCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) - 1);
                } else {
                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) - 1);
                }
                dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));

//                // выставляем в таблицу текущую дату
//                if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
//                        viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
//                    learnersAndGradesTableView.currentDate = currentCalendar.get(Calendar.DAY_OF_MONTH) - 1;
//                } else
//                    learnersAndGradesTableView.currentDate = -1;

                // получаем оценки из бд по новой дате
                getGradesFromDB();
            }
        });

        // кнопка вперед
        findViewById(R.id.learners_and_grades_activity_button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // останавливаем поток загрузки оценок и уничтожаем все его данные
                if (loadGradesThread != null) {
                    loadGradesThread.stopThread();
                    loadGradesThread = null;
                }

                // переключаем дату
                if (viewCalendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                    viewCalendar.set(Calendar.MONTH, Calendar.JANUARY);
                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) + 1);
                } else {
                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) + 1);
                }
                dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));

//                // выставляем в таблицу текущую дату
//                if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
//                        viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
//                    learnersAndGradesTableView.currentDate = currentCalendar.get(Calendar.DAY_OF_MONTH) - 1;
//                } else
//                    learnersAndGradesTableView.currentDate = -1;


                // получаем оценки из бд по новой дате
                getGradesFromDB();
            }
        });


        if (dataLearnersAndGrades == null) {
            dataLearnersAndGrades = new DataObject();
        }

        // проводим различные проверки, чтобы не перезагружать данные если например экран перевернулся
        if (subjects == null) {

            // получаем предметы
            getSubjectsFromDB();

            // получаем учеников только здесь
            getLearnersFromDB();

            // говорим компоненту отрисовать полученных учеников
            learnersAndGradesTableView.invalidate();

            // получаем оценки из базы данных (отрисоввываться они будут по handler-у)
            getGradesFromDB();
        } else {
            // раз есть предметы вываодим название выбранного
            // ставим выбор на прошлом предмете
            if (subjects.length != 0) {
                // выводим название предмета
                subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());

            } else { // если проедметов в базе данных нет не выбираем ничего
                chosenSubjectPosition = -1;
                // выводим текст о том, что предмета нет
                subjectTextView.setText(getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject));
            }

            // не пустой ли массив учеников
            if (dataLearnersAndGrades.learnersAndHisGrades == null) {

                // получаем учеников только здесь
                getLearnersFromDB();

                // говорим компоненту отрисовать полученных учеников
                learnersAndGradesTableView.invalidate();

                // получаем оценки из базы данных (отрисовываться они будут по handler-у)
                getGradesFromDB();
            } else {
                // если все на месте то просто передаем пересозданным view старые данные
                learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
            }
        }
    }

    // -------------------------- основные методы --------------------------

    // метод получения предметов из базы данных
    void getSubjectsFromDB() {
        Log.i(TAG, "getSubjectsFromDB");

        // останавливаем поток загрузки и уничтожаем все его данные
        if (loadGradesThread != null) {
            loadGradesThread.stopThread();
            loadGradesThread = null;
        }

        // получаем предметы из базы данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor subjectsCursor = db.getSubjectsByClassId(classId);

        // инициализируем массив предметов
        subjects = new NewSubjectUnit[subjectsCursor.getCount()];
        for (int subjectsIterator = 0; subjectsIterator < subjectsCursor.getCount(); subjectsIterator++) {
            subjectsCursor.moveToNext();
            subjects[subjectsIterator] = new NewSubjectUnit(
                    subjectsCursor.getLong(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID)),
                    subjectsCursor.getString(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME))
            );
        }

        // ставим выбор на первом предмете
        if (subjects.length != 0) {
            chosenSubjectPosition = 0;
            // выводим название предмета
            subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());

        } else { // если проедметов в базе данных нет не выбираем ничего
            chosenSubjectPosition = -1;
            // выводим текст о том, что предмета нет
            subjectTextView.setText(getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject));
        }

        // курсор больше не нужен
        subjectsCursor.close();
        db.close();
    }


    // метод получения учеников из базы данных (которые потом передаются во view)
    void getLearnersFromDB() {
        Log.i(TAG, "LearnersAndGradesActivity - getLearnersFromDB");

        // ждем пока копирование переменных завершится
        while (dataLearnersAndGrades.isInCopyProcess) ;

        // запрещаем пользователю редактировать учеников
        dataLearnersAndGrades.chosenLearnerPosition = 0;


        // получаем учеников из базы данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersCursor = db.getLearnersByClassId(classId);

        // чистим лист от старых учеников
        dataLearnersAndGrades.learnersAndHisGrades = new NewLearnerAndHisGrades[learnersCursor.getCount()];
        // и заполняем новыми
        for (int learnerI = 0; learnerI < dataLearnersAndGrades.learnersAndHisGrades.length; learnerI++) {
            learnersCursor.moveToNext();
            // создаем нового ученика
            dataLearnersAndGrades.learnersAndHisGrades[learnerI] = new NewLearnerAndHisGrades(
                    learnersCursor.getLong(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.KEY_LEARNER_ID)),
                    learnersCursor.getString(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
                    learnersCursor.getString(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_SECOND_NAME)),
                    new GradeUnit[][]{}
            );
        }
        learnersCursor.close();
        db.close();

        // передаем данные во view (пока только ученики)
        learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);

        //разрешаем редактировать учеников, когда все вывели
        dataLearnersAndGrades.chosenLearnerPosition = -1;
    }


    // класс handler-а активности для обращения из сторонних потоков
    static class UpdateTableHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            // вывод оценок по завершении получения данных
            if (msg.what == 10) {
                // обновляем данные в таблице
                learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
                // перерисоввываем окно
                learnersAndGradesTableView.invalidate();

                // разрешаем менять оценки
                dataLearnersAndGrades.chosenLearnerPosition = -1;
                dataLearnersAndGrades.chosenDayPosition = -1;
                dataLearnersAndGrades.chosenLessonPosition = -1;
            }
        }
    }


    // handler активности для обращения из сторонних потоков
    UpdateTableHandler updateTableHandler = new UpdateTableHandler();

    // парсинг дат из календаря
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // сторонний поток для загрузки оценок
    class GetGradesThread extends Thread {
        private boolean runFlag;

        void stopThread() {
            Log.i(TAG, "LoadGradesThread-stoppedEarly");
            runFlag = false;
        }

        @Override
        public void run() {
            Log.i(TAG, "LoadGradesThread-Start");
            // разрешаем подгрузку данных
            runFlag = true;


            // ---- инициализируем ----

            // копируем оригинальный обьект с данными чтобы пока изменять только копию
            DataObject copy = new DataObject();
            copy.isInCopyProcess = false;
            copy.chosenLearnerPosition = -1;
            copy.chosenDayPosition = -1;
            copy.chosenLessonPosition = -1;

            // выставляем дату за которую берутся оценки
            GregorianCalendar loadGradesTempCalendar = new GregorianCalendar();
            loadGradesTempCalendar.set(
                    viewCalendar.get(Calendar.YEAR),
                    viewCalendar.get(Calendar.MONTH),
                    viewCalendar.get(Calendar.DAY_OF_MONTH)
            );
            copy.yearAndMonth = loadGradesTempCalendar.getTime();

            // инициализируем уроки по дням в null
            copy.lessonsUnits = new LessonUnit[loadGradesTempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][9];


            // не можем начать копирование пока старый поток все не закончит
            while (dataLearnersAndGrades.isInCopyProcess) ;

            // начинаем копирование
            dataLearnersAndGrades.isInCopyProcess = true;

            // создаем копию учеников с пустыми оценками todo обязательно ли клонировать самих учеников?
            copy.learnersAndHisGrades = new NewLearnerAndHisGrades[dataLearnersAndGrades.learnersAndHisGrades.length];
            for (int learnerI = 0; learnerI < dataLearnersAndGrades.learnersAndHisGrades.length; learnerI++) {
                copy.learnersAndHisGrades[learnerI] = new NewLearnerAndHisGrades(
                        dataLearnersAndGrades.learnersAndHisGrades[learnerI].id,
                        "" + dataLearnersAndGrades.learnersAndHisGrades[learnerI].name,
                        "" + dataLearnersAndGrades.learnersAndHisGrades[learnerI].surname,
                        new GradeUnit[loadGradesTempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][9]
                );
            }

            // завершаем копирование данных
            dataLearnersAndGrades.isInCopyProcess = false;


            // ---- получаем данные ----

            // если не выбран предмет возвращаем пустые ячейки
            if (chosenSubjectPosition >= 0) {

                // открываем бд
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());

                // получаем уроки
                for (int dayI = 0; dayI < copy.lessonsUnits.length; dayI++) {
                    // получаем уроки за день (пустой урок null)
                    String checkDate = String.format(new Locale("en"), "%04d-%02d-%02d", viewCalendar.get(Calendar.YEAR), viewCalendar.get(Calendar.MONTH) + 1, dayI + 1);
                    Cursor baseLessons = db.getSubjectAndTimeCabinetAttitudesByDateAndLessonNumbersPeriod(
                            subjects[chosenSubjectPosition].getSubjectId(),
                            checkDate,
                            0,
                            9
                    );

                    while (baseLessons.moveToNext()) {
                        int lessonNumber = baseLessons.getInt(baseLessons.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER));
                        long lessonId = baseLessons.getLong(baseLessons.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID));
                        long subjectId = baseLessons.getLong(baseLessons.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
                        long cabinetId = baseLessons.getLong(baseLessons.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
                        String lessonDate = baseLessons.getString(baseLessons.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE));
                        int repeat = baseLessons.getInt(baseLessons.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT));

                        // получаем дз
                        Cursor comment = db.getLessonCommentsByDateAndLesson(lessonId, checkDate);
                        if (comment.moveToFirst()) {
                            // записываем полученный урок (если его еще не было)
                            if (copy.lessonsUnits[dayI][lessonNumber] == null)
                                copy.lessonsUnits[dayI][lessonNumber] = new LessonUnit(
                                        lessonId,
                                        subjectId,
                                        cabinetId,
                                        lessonDate,
                                        lessonNumber,
                                        repeat,
                                        comment.getLong(comment.getColumnIndex(SchoolContract.TableLessonComment.KEY_LESSON_TEXT_ID)),
                                        comment.getString(comment.getColumnIndex(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT))
                                );
                        } else {
                            // записываем полученный урок (если его еще не было)
                            if (copy.lessonsUnits[dayI][lessonNumber] == null)
                                copy.lessonsUnits[dayI][lessonNumber] = new LessonUnit(
                                        lessonId,
                                        subjectId,
                                        cabinetId,
                                        lessonDate,
                                        lessonNumber,
                                        repeat
                                );
                        }
                    }

                    baseLessons.close();
                }

                // по ученикам
                for (int learnerI = 0; learnerI < copy.learnersAndHisGrades.length; learnerI++) {
                    // по дням
                    for (int dayI = 0; dayI < copy.learnersAndHisGrades[learnerI].learnerGrades.length; dayI++) {

                        if (!runFlag) {// если была команда закрыть поток без подгрузки
                            // выходим  из метода подгрузки
                            return;
                        }// todo надо чтобы новый поток ждал завенршения старого прежде чем начать загрузку данных


                        // получаем оценки(уроки) этого ученика за этот день
                        loadGradesTempCalendar.set(Calendar.DAY_OF_MONTH, dayI + 1);
                        Cursor allDayGrades = db.getGradesByLearnerIdSubjectDateAndLessonsPeriod(
                                copy.learnersAndHisGrades[learnerI].id,
                                subjects[chosenSubjectPosition].getSubjectId(),
                                dateFormat.format(loadGradesTempCalendar.getTime()),
                                0,
                                10 // todo получать количество из бд
                        );


                        while (allDayGrades.moveToNext()) {
                            if (!runFlag) {// если была команда закрыть поток без подгрузки
                                // выходим  из метода подгрузки
                                allDayGrades.close();
                                return;
                            }

                            // id оценки
                            long gradesId = allDayGrades.getLong(allDayGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID));
                            // номер урока
                            int lessonPoz = allDayGrades.getInt(allDayGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER));

                            // оценки
                            int[] grades = new int[3];
                            for (int gradeI = 0; gradeI < 3; gradeI++) {
                                grades[gradeI] = allDayGrades.getInt(allDayGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[gradeI]));
                            }

                            // номера типов оценок
                            int[] gradesTypesIndexes = new int[3];
                            for (int gradesI = 0; gradesI < 3; gradesI++) {
                                int typeId = allDayGrades.getInt(allDayGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[gradesI]));
                                for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                                    if (typeId == answersTypes[typeI].id) {
                                        gradesTypesIndexes[gradesI] = typeI;
                                        break;
                                    }
                                }
                            }

                            // номер типа пропуска
                            int absTypePoz = -1;
                            if (!allDayGrades.isNull(allDayGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER))) {
                                int absTypeId = allDayGrades.getInt(allDayGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER));
                                for (int absI = 0; absI < absentTypes.length; absI++) {
                                    if (absTypeId == absentTypes[absI].id) {
                                        absTypePoz = absI;
                                        break;
                                    }
                                }
                            }

                            // если оценки здесь еще не стояли, создаем их
                            if (copy.learnersAndHisGrades[learnerI].learnerGrades[dayI][lessonPoz] == null)
                                copy.learnersAndHisGrades[learnerI].learnerGrades[dayI][lessonPoz] = new GradeUnit(
                                        grades,
                                        gradesId,
                                        gradesTypesIndexes,
                                        absTypePoz
                                );
                        }

                        allDayGrades.close();
                    }
                }
            }

            // если этот поток не был прерван ранее
            if (runFlag) {
                // присваиваем измененную копию
                dataLearnersAndGrades = copy;
                // говорим активности что закончили подгрузку
                updateTableHandler.sendEmptyMessage(10);
            }

            Log.i(TAG, "threadRunEnded");
        }
    }

    // метод получения оценок из бд
    void getGradesFromDB() {
        Log.i(TAG, "LearnersAndGradesActivity - getGradesFromDB");

        // класс потока загрузки
        loadGradesThread = new GetGradesThread();

        // запрещаем изменение оценок
        dataLearnersAndGrades.chosenLearnerPosition = 0;
        dataLearnersAndGrades.chosenDayPosition = -1;
        dataLearnersAndGrades.chosenLessonPosition = -1;

        // запускаем поток
        loadGradesThread.setName("loadGradesThread - hello");
        loadGradesThread.start();
    }


// ------------------ обработка касаний таблицы ------------------

    // точка начала касания
    PointF downPointF = new PointF();
    // время начала касания
    long startTouchTime = 0;

    // нет касания, нажатие или перемещение
    static final int TOUCH_NONE = 0;
    static final int TOUCH_DOWN = 1;
    static final int TOUCH_MOVE = 2;
    int mode = TOUCH_NONE;

    // метод отвечающий за обработку касания таблицы
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:// поставили первый палец
                // режим нажатия
                mode = TOUCH_DOWN;
                // считываем координаты
                downPointF.x = motionEvent.getX();
                downPointF.y = motionEvent.getY();
                // и время нажатия
                startTouchTime = System.currentTimeMillis();
                //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_DOWN: x=" + downPointF.x + " y=" + downPointF.y + " startTouchTime=" + startTouchTime);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == TOUCH_DOWN) {
                    // --- движение при режиме нажатия ---
                    // находим расстояние от точки начала касания
                    if (Math.sqrt(
                            Math.pow(motionEvent.getX() - downPointF.x, 2) + Math.pow(motionEvent.getY() - downPointF.y, 2)
                    ) > pxFromDp(20)) {
                        // режим движения
                        mode = TOUCH_MOVE;
                        // режим движения только начался по этому заново считываем координаты
                        downPointF.x = motionEvent.getX();
                        downPointF.y = motionEvent.getY();
                        //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_MOVE(TOUCH_MOVE->active): x=" + downPointF.x + " y=" + downPointF.y);

                    }
                    //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_MOVE(TOUCH_DOWN): x=" + downPointF.x + " y=" + downPointF.y);

                } else {
                    // --- движение при режиме движения ---
                    ((LearnersAndGradesTableView) view).setMove(downPointF, motionEvent.getX(), motionEvent.getY());
                    //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_MOVE(TOUCH_MOVE): x=" + downPointF.x + " y=" + downPointF.y);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mode == TOUCH_DOWN) {
                    // --- было только нажатие ---

                    // проверяем долгое ли было нажатие
                    //boolean isLongClick = System.currentTimeMillis() - startTouchTime >= 500;

                    // отправляем нажатие и получаем индексы нажатия в таблице (номер ученика, дня, урока)
                    int[] touchData = ((LearnersAndGradesTableView) view).touch(
                            downPointF,
                            true // isLongClick
                    );
                    // вызываем перерисовку, чтобы view нарисовал нажатые клетки
                    learnersAndGradesTableView.invalidate();

                    //Log.e(TAG, "onTouch: TOUCH_DOWN " + touchData[0] + " " + touchData[1] + " " + touchData[2]);


                    // если пользователь нажал что-то
                    if (touchData[0] != -1 || touchData[1] != -1 || touchData[2] != -1) {

                        // если пользователь нажал на создание ученика
                        if (touchData[0] == -2) {

                            // чтобы не было нескольких вызовов одновременно
                            if (dataLearnersAndGrades.chosenLearnerPosition == -1) {
                                // запрещаем пока работать с учениками
                                dataLearnersAndGrades.chosenLearnerPosition = 0;

                                // создаем диалог добавления ученика
                                LearnerCreateDialogFragment createLearnerDialog = new LearnerCreateDialogFragment();
                                //показываем диалог
                                createLearnerDialog.show(getSupportFragmentManager(), "createLearnerDialog");
                            }

                        } else {// иначе проверяем что нажато, оценки или ученки
                            if (touchData[1] != -1) {// если нажата область оценок
                                // чтобы не было нескольких вызовов одновременно
                                if (dataLearnersAndGrades.chosenLearnerPosition == -1 &&
                                        dataLearnersAndGrades.chosenDayPosition == -1 &&
                                        dataLearnersAndGrades.chosenLessonPosition == -1) {

                                    if (touchData[0] == -1) {// если нажата дата над оценкой

                                        // если есть предметы
                                        if (subjects.length > 0) {
                                            // отключаем нажатия
                                            // чтобы не было нескольких вызовов одновременно
                                            dataLearnersAndGrades.chosenLearnerPosition = -1;
                                            dataLearnersAndGrades.chosenDayPosition = touchData[1];
                                            dataLearnersAndGrades.chosenLessonPosition = touchData[2];

                                            // показываем диалог комментария к уроку
                                            LessonCommentDialogFragment dialogFragment = new LessonCommentDialogFragment();

                                            // если предыдущий текст не нулевой
                                            if (dataLearnersAndGrades.lessonComments[touchData[1]][touchData[2]] != null) {
                                                // выводим туда прошлый текст
                                                Bundle args = new Bundle();
                                                args.putString(LessonCommentDialogFragment.ARGS_PREVIOUS_TEXT,
                                                        dataLearnersAndGrades.lessonComments[touchData[1]][touchData[2]].lessonCommentText);
                                                dialogFragment.setArguments(args);
                                            }
                                            dialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment");
                                        }

                                    } else {// если нажаты оценки
                                        // если не выбран предмет то не можем редактировать оценки
                                        if (chosenSubjectPosition < 0) {
                                            // создаем диалог работы с предметами
                                            SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();

                                            // готоим и передаем массив названий предиметов
                                            Bundle args = new Bundle();
                                            String[] subjectsArray = new String[subjects.length];
                                            for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                                                subjectsArray[subjectI] = subjects[subjectI].getSubjectName();
                                            }
                                            args.putStringArray(SubjectsDialogFragment.ARGS_LEARNERS_NAMES_STRING_ARRAY, subjectsArray);
                                            subjectsDialogFragment.setArguments(args);

                                            // показываем диалог
                                            subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment - hello");
                                        } else {

                                            // ставим полученные координаты оценки как выбранные
                                            // тем самым отключая нажатия
                                            dataLearnersAndGrades.chosenLearnerPosition = touchData[0];
                                            dataLearnersAndGrades.chosenDayPosition = touchData[1];
                                            dataLearnersAndGrades.chosenLessonPosition = touchData[2];

                                            // вызываем диалог изменения оценок
                                            GradeEditDialogFragment editGrade = new GradeEditDialogFragment();
                                            //параметры
                                            Bundle bundle = new Bundle();

                                            // имя ученика
                                            bundle.putString(
                                                    GradeEditDialogFragment.ARGS_LEARNER_NAME,
                                                    dataLearnersAndGrades.learnersAndHisGrades[touchData[0]].surname + " " + dataLearnersAndGrades.learnersAndHisGrades[touchData[0]].name
                                            );

                                            // названия типов оценок в массиве
                                            String[] stringTypes = new String[answersTypes.length];
                                            for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                                                stringTypes[typeI] = answersTypes[typeI].typeName;
                                            }
                                            bundle.putStringArray(
                                                    GradeEditDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY,
                                                    stringTypes
                                            );

                                            // массив оценок
                                            bundle.putIntArray(
                                                    GradeEditDialogFragment.ARGS_INT_GRADES_ARRAY,
                                                    dataLearnersAndGrades.learnersAndHisGrades[touchData[0]].learnerGrades[touchData[1]][touchData[2]].grades.clone()
                                            );

                                            // массив с номерами выбранных типов
                                            bundle.putIntArray(
                                                    GradeEditDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY,
                                                    dataLearnersAndGrades.learnersAndHisGrades[touchData[0]].learnerGrades[touchData[1]][touchData[2]].gradesTypesIndexes.clone()
                                            );

                                            // выбранный номер пропуска
                                            bundle.putInt(
                                                    GradeEditDialogFragment.ARGS_INT_GRADES_ABSENT_TYPE_NUMBER,
                                                    dataLearnersAndGrades.learnersAndHisGrades[touchData[0]].learnerGrades[touchData[1]][touchData[2]].absTypePoz
                                            );

                                            // названия типов пропусков в массиве
                                            String[] stringAbsTypes = new String[absentTypes.length];
                                            for (int typeI = 0; typeI < absentTypes.length; typeI++) {
                                                stringAbsTypes[typeI] = absentTypes[typeI].absTypeName;
                                            }
                                            bundle.putStringArray(
                                                    GradeEditDialogFragment.ARGS_STRING_ABSENT_TYPES_NAMES_ARRAY,
                                                    stringAbsTypes
                                            );
                                            // длинные названия типов пропусков в массиве
                                            String[] stringLongAbsTypes = new String[absentTypes.length];
                                            for (int typeI = 0; typeI < absentTypes.length; typeI++) {
                                                stringLongAbsTypes[typeI] = absentTypes[typeI].absTypeLongName;
                                            }
                                            bundle.putStringArray(
                                                    GradeEditDialogFragment.ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY,
                                                    stringLongAbsTypes
                                            );

                                            // максимальный размер оценки
                                            bundle.putInt(
                                                    GradeEditDialogFragment.ARGS_INT_MAX_GRADE,
                                                    maxGrade
                                            );

                                            // текущая дата в строке
                                            bundle.putString(
                                                    GradeEditDialogFragment.ARGS_STRING_CURRENT_DATE,
                                                    (touchData[1] + 1) + " " + transformedMonthsNames[viewCalendar.get(Calendar.MONTH)]
                                            );

                                            // выбранный номер урока
                                            bundle.putInt(
                                                    GradeEditDialogFragment.ARGS_INT_LESSON_NUMBER,
                                                    touchData[2]
                                            );

                                            //запускаем
                                            editGrade.setArguments(bundle);
                                            editGrade.show(getSupportFragmentManager(), "editGradeDialog - hello");// illegalState - вызов в неподходящее время

                                        }
                                    }
                                }
                            } else {// если нажат ученик

                                // чтобы не было нескольких вызовов одновременно
                                // и учеников меняем по долгому нажатию
                                if (dataLearnersAndGrades.chosenLearnerPosition == -1) { // isLongClick
                                    // выбираем ученика
                                    dataLearnersAndGrades.chosenLearnerPosition = touchData[0];


                                    // останавливаем поток загрузки оценок и уничтожаем все его данные
                                    if (loadGradesThread != null) {
                                        loadGradesThread.stopThread();
                                        loadGradesThread = null;
                                    }

                                    // создаем диалог
                                    LearnerEditDialogFragment editDialog = new LearnerEditDialogFragment();

                                    // создаем обьект с данными для диалога
                                    Bundle args = new Bundle();
                                    args.putString(LearnerEditDialogFragment.ARGS_LEARNER_NAME,
                                            dataLearnersAndGrades.learnersAndHisGrades[touchData[0]].name);
                                    args.putString(LearnerEditDialogFragment.ARGS_LEARNER_LAST_NAME,
                                            dataLearnersAndGrades.learnersAndHisGrades[touchData[0]].surname);

                                    // передаем данные диалогу
                                    editDialog.setArguments(args);

                                    // показываем диалог
                                    editDialog.show(getSupportFragmentManager(), "editLearnerDialog - hello");

                                }
                            }
                        }
                    }

                } else {
                    // --- было движение ---
                    ((LearnersAndGradesTableView) view).endMove();
                    //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_UP(moveEnded): x=" + downPointF.x + " y=" + downPointF.y);
                }
                // отключаем нажатие
                mode = TOUCH_DOWN;
                break;

        }
        return true;
    }


// -------------------------- обратная связь диалогов --------------------------

    // метод создания ученика вызываемый из диалога LearnerCreateDialogFragment
    @Override
    public void createLearner(String lastName, String name) {
        Log.i(TAG, "fromDialog-createLearner");

        // останавливаем поток загрузки оценок и уничтожаем все его данные
        if (loadGradesThread != null) {
            loadGradesThread.stopThread();
            loadGradesThread = null;
        }

        // создаем ученика в базе данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);


        // ---- создаем ученика в локальном списке ----
        // создаем для него пустой список оценок
        GradeUnit[][] emptyGradeUnitsArray = new GradeUnit[viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][9];
        // пробегаемся по дням
        for (int dayI = 0; dayI < viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); dayI++) {
            // пробегаемся по урокам
            for (int lessonI = 0; lessonI < emptyGradeUnitsArray[dayI].length; lessonI++) {

                // заполняем ячейки уроков пустыми оценками
                emptyGradeUnitsArray[dayI][lessonI] =
                        new GradeUnit(
                                new int[]{0, 0, 0},
                                -1,
                                new int[]{0, 0, 0},
                                -1
                        );
            }
        }


        // добавляем ученика в локальный список
        // сравниваем этого ученика поочереди со всеми остальными
        int poz = -1;
        for (int learnerI = 0; learnerI < dataLearnersAndGrades.learnersAndHisGrades.length; learnerI++) {
            // если полученное имя лексикографически меньше текущего в списке то ставим полученное перед текущим
            if ((lastName.compareTo(dataLearnersAndGrades.learnersAndHisGrades[learnerI].surname) < 0) ||
                    // при равных фамилиях сравниваем имена
                    (lastName.compareTo(dataLearnersAndGrades.learnersAndHisGrades[learnerI].surname) == 0 &&
                            name.compareTo(dataLearnersAndGrades.learnersAndHisGrades[learnerI].name) < 0)
            ) {
                dataLearnersAndGrades.learnersAndHisGrades.add(learnerI, new NewLearnerAndHisGrades(
                        db.createLearner(lastName, name, classId),
                        name,
                        lastName,
                        emptyGradeUnitsArray
                ));
                poz = learnerI;
                break;
            }
        }
        // если строка больше всех других строк, помещаем в конец списка
        if (poz == -1) {

            dataLearnersAndGrades.learnersAndHisGrades.add(new NewLearnerAndHisGrades(
                    db.createLearner(lastName, name, classId),
                    name,
                    lastName,
                    emptyGradeUnitsArray
            ));
        }
        db.close();


        // выводим изменившихся учеников во view
        learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
        learnersAndGradesTableView.invalidate();

        // разрешаем редактировать учеников
        dataLearnersAndGrades.chosenLearnerPosition = -1;
    }

    @Override
    public void allowUserEditLearners() {
        Log.i(TAG, "fromDialog-allowUserEditLearners");

        // разрешаем редактировать учеников
        dataLearnersAndGrades.chosenLearnerPosition = -1;
    }

    // метод переименования ученика вызываемый из диалога LearnerEditDialogFragment
    @Override
    public void editLearner(String lastName, String name) {
        Log.i(TAG, "fromDialog-editLearner");

//        // останавливаем поток загрузки оценок и уничтожаем все его данные
//        if (loadGradesThread != null) {
//            loadGradesThread.stopThread();
//            loadGradesThread = null;
//        } todo останавливать поток можно только если после изменений его продолжишь например перезагрузкой учеников

        // на всякий случай
        if (dataLearnersAndGrades.chosenLearnerPosition != -1) {

            // меняем имя и фамилию ученика в базе данных
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            db.setLearnerNameAndLastName(dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition).id, name, lastName)
            ;
            db.close();

            // меняем локальный список учеников todo сортировка
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].name = name;
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].surname = lastName;

            // выводим изменившихся учеников во view
            learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
            learnersAndGradesTableView.invalidate();

            // разрешаем редактировать учеников
            dataLearnersAndGrades.chosenLearnerPosition = -1;
        }
    }

    // метод удаления ученика вызываемый из диалога LearnerEditDialogFragment
    @Override
    public void removeLearner() {
        Log.i(TAG, "fromDialog-removeLearner");

//        // останавливаем поток загрузки оценок и уничтожаем все его данные
//        if (loadGradesThread != null) {
//            loadGradesThread.stopThread();
//            loadGradesThread = null;
//        }

        // удаляем ученика из базы данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.deleteLearner(dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].id);
        db.close();

        // удаляем ученика из списка
        dataLearnersAndGrades.learnersAndHisGrades.remove(dataLearnersAndGrades.chosenLearnerPosition);

        // выводим изменившихся учеников во view
        learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
        learnersAndGradesTableView.invalidate();

        // разрешаем редактировать учеников
        dataLearnersAndGrades.chosenLearnerPosition = -1;
    }

    // метод редактирования оценки ученика из диалога GradeEditDialogFragment
    @Override
    public void editGrades(int[] grades, int absTypePoz, int[] chosenTypesNumbers, int lessonPoz) {
        Log.i(TAG, "fromDialog-editGrade");
        // проверяем выбрана ли все еще дата
        if (dataLearnersAndGrades.chosenLearnerPosition != -1 && dataLearnersAndGrades.chosenDayPosition != -1 && lessonPoz != -1) {

            // сохраняем оценки в массив
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                    learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz].grades = grades;
            // типы
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                    learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz].gradesTypesIndexes = chosenTypesNumbers;
            // отсутствие
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                    learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz].absTypePoz = absTypePoz;

            // сохраняем оценки в бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // по индексам вычисляем время когда оценка была поставлена
            int[][] timeOfLessons = db.getSettingsTime(1);

            viewCalendar.set(Calendar.DAY_OF_MONTH, dataLearnersAndGrades.chosenDayPosition + 1);
            String gradeDate = dateFormat.format(viewCalendar.getTime());

            // id оценки -1, значит оценка новая
            if (dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                    learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz].gradeId == -1) {
                // если оценка не нулевая то создаем ее
                if (grades[0] != 0 ||
                        grades[1] != 0 ||
                        grades[2] != 0 ||
                        absTypePoz != -1) {

                    long abs;
                    if (absTypePoz != -1) {
                        abs = absentTypes[absTypePoz].id;
                    } else
                        abs = -1;
                    // и сохраняем id в массив
                    dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                            learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz].gradeId =
                            db.createGrade(
                                    dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].id,
                                    grades[0],
                                    grades[1],
                                    grades[2],
                                    answersTypes[chosenTypesNumbers[0]].id,
                                    answersTypes[chosenTypesNumbers[1]].id,
                                    answersTypes[chosenTypesNumbers[2]].id,
                                    abs,
                                    subjects[chosenSubjectPosition].getSubjectId(),
                                    gradeDate,
                                    lessonPoz
                            );
                }
            } else {// id есть значит меняем текущую

                // если все оценки пустые, удаляем
                if (grades[0] == 0 &&
                        grades[1] == 0 &&
                        grades[2] == 0 &&
                        absTypePoz == -1) {
                    // из бд
                    db.removeGrade(dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                            learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz].gradeId);
                    // и из списка
                    dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                            learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz].gradeId = -1;
                } else {
                    long abs;
                    if (absTypePoz != -1) {
                        abs = absentTypes[absTypePoz].id;
                    } else
                        abs = -1;
                    // не нулевые меняем в бд
                    db.editGrade(
                            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                                    learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz].gradeId,
                            grades[0],
                            grades[1],
                            grades[2],
                            answersTypes[chosenTypesNumbers[0]].id,
                            answersTypes[chosenTypesNumbers[1]].id,
                            answersTypes[chosenTypesNumbers[2]].id,
                            abs
                    );
                }
            }

            // выводим изменившиеся данные во view
            learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
            learnersAndGradesTableView.invalidate();
        }
        //разрешаем пользователю менять оценки
        dataLearnersAndGrades.chosenLearnerPosition = -1;
        dataLearnersAndGrades.chosenDayPosition = -1;
        dataLearnersAndGrades.chosenLessonPosition = -1;
    }

    // метод разрешающий пользователю изменять оценки из диалога GradeEditDialogFragment
    @Override
    public void allowUserEditGrades() {
        Log.i(TAG, "fromDialog-allowUserEditGrades");

        //разрешаем пользователю менять оценки
        dataLearnersAndGrades.chosenLearnerPosition = -1;
        dataLearnersAndGrades.chosenDayPosition = -1;
        dataLearnersAndGrades.chosenLessonPosition = -1;

        // и обновляем таблицу, чтобы исчезли синие квадраты
        learnersAndGradesTableView.invalidate();
    }

    // метод выбора текущего предмета вызываемый из диалога SubjectsDialogFragment
    @Override
    public void setSubjectPosition(int position) {
        // выбираем позицию текущего предмета
        chosenSubjectPosition = position;

        // выводим название выбранного урока в текстовое поле
        subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());

        // обновляем список оценок по выбранному предмету
        getGradesFromDB();
    }

    // метод создания предмета вызываемый из диалога SubjectsDialogFragment
    @Override
    public void createSubject(String name, int position) {
        // создаем предмет в базе данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long createdSubjectId = db.createSubject(name, classId);
        db.close();

        // добавляем предмет в список под нужным номером
        NewSubjectUnit[] newSubjectUnits = new NewSubjectUnit[subjects.length + 1];
        for (int subjectI = 0; subjectI < position; subjectI++) {
            newSubjectUnits[subjectI] = subjects[subjectI];
        }
        newSubjectUnits[position] = new NewSubjectUnit(createdSubjectId, name);
        for (int subjectsI = position; subjectsI < subjects.length; subjectsI++) {
            newSubjectUnits[subjectsI + 1] = subjects[subjectsI];
        }
        subjects = newSubjectUnits;

        // выбираем этот предмет и ставим его имя в заголовок
        chosenSubjectPosition = position;
        subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());

        // загружаем оценки по выбранному предмету
        getGradesFromDB();
    }

    // метод удаления предмета вызываемый из диалога SubjectsDialogFragment
    @Override
    public void deleteSubjects(boolean[] deleteList) {

        // удаляем предметы
        ArrayList<Long> deleteId = new ArrayList<>();
        for (int subjectI = 0; subjectI < subjects.length; subjectI++) {
            if (deleteList[subjectI]) {
                deleteId.add(subjects[subjectI].getSubjectId());
                // смотрим не удален ли выбранный предмет
                if (chosenSubjectPosition == subjectI)
                    chosenSubjectPosition = -1;
            }
        }

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.deleteSubjects(deleteId);
        db.close();

        // создаем новый список
        NewSubjectUnit[] newSubjectUnits = new NewSubjectUnit[subjects.length - deleteId.size()];
        int newArrayIterator = 0;
        for (int subjectI = 0; subjectI < subjects.length; subjectI++) {
            if (!deleteList[subjectI]) {
                // сохраняем не удаленные предметы
                newSubjectUnits[newArrayIterator] = subjects[subjectI];

                // и оставляем выбранную позицию
                if (chosenSubjectPosition == subjectI)
                    chosenSubjectPosition = newArrayIterator;

                newArrayIterator++;
            }
        }
        subjects = newSubjectUnits;

        // если текущий предмет был удален
        if (chosenSubjectPosition == -1) {
            // ставим выбор на первом предмете
            if (subjects.length != 0) {
                chosenSubjectPosition = 0;
                // выводим название предмета
                subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());

            } else { // если проедметов в базе данных нет не выбираем ничего
                // выводим текст о том, что предмета нет
                subjectTextView.setText(getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject));
            }
            // получаем оценки
            getGradesFromDB();
        }
    }

    // метод переименования предметов вызываемый из диалога SubjectsDialogFragment
    @Override
    public void renameSubjects(String[] newSubjectsNames) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        // в цикле переименовываем все предметы в массиве активности
        for (int subjectI = 0; subjectI < subjects.length; subjectI++) {

            subjects[subjectI].setSubjectName(newSubjectsNames[subjectI]);

            // и сохраняем все изменения в базу данных
            db.setSubjectName(
                    subjects[subjectI].getSubjectId(),
                    subjects[subjectI].getSubjectName()
            );
        }
        db.close();

        // сортируем текущий список (пузырьком)
        for (int out = subjects.length - 1; out > 0; out--) {
            for (int in = 0; in < out; in++) {
                if (subjects[in].getSubjectName().compareTo(subjects[in + 1].getSubjectName()) > 0) {
                    if (chosenSubjectPosition == in) {
                        chosenSubjectPosition = in + 1;
                    } else if (chosenSubjectPosition == in + 1) {
                        chosenSubjectPosition = in;
                    }
                    NewSubjectUnit temp = subjects[in + 1];
                    subjects[in + 1] = subjects[in];
                    subjects[in] = temp;
                }
            }
        }

        // обновляем надпись на тексте с названием предмета
        if (subjects.length != 0) {
            // выводим название предмета
            subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());

        } else { // если проедметов в базе данных нет не выбираем ничего
            // выводим текст о том, что предмета нет
            subjectTextView.setText(getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject));
        }
    }


    // при выходе из активности
    @Override
    protected void onStop() {
        Log.i(TAG, " onStop");

        // останавливаем поток загрузки оценок и уничтожаем все его данные
        if (loadGradesThread != null) {
            loadGradesThread.stopThread();
            loadGradesThread = null;
        }

        super.onStop();
    }

    //кнопка назад в actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);

    }

    // выход из активности по желанию пользователя
    @Override
    public void onBackPressed() {
        // сначала чистим все данные
        classId = -1;
        if (dataLearnersAndGrades != null) {
            dataLearnersAndGrades.learnersAndHisGrades = null;
            dataLearnersAndGrades.chosenLearnerPosition = -1;
            dataLearnersAndGrades.chosenDayPosition = -1;
            dataLearnersAndGrades.chosenLessonPosition = -1;
            dataLearnersAndGrades = null;
        }
        subjects = null;
        chosenSubjectPosition = -1;
        viewCalendar = null;
        loadGradesThread = null;
        learnersAndGradesTableView = null;

        // а потом выходим
        super.onBackPressed();
    }

    // преобразование зависимой величины в пиксели
    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }

}


// класс который хранит данные об учениках оценках и уроках
class DataObject {

    // сюда положен месяц и год от даты за которую берутся данные
    Date yearAndMonth;

    // массив с названиями пропусков
    //String[] absNames;

    // массив уроков [день, урок]
    LessonUnit[][] lessonsUnits;

    // массив с учениками и их оценками
    NewLearnerAndHisGrades[] learnersAndHisGrades;


    // позиция выбранной оценки/урока/ученика (ученик,день,урок)
    int chosenLearnerPosition = -1;
    int chosenDayPosition = -1;
    int chosenLessonPosition = -1;
    /*
      только ученик -> ученик
      только дата и урок -> нажат урок
      и ученик, и дата , и урок -> клетка с оценкой
    */


    // не занят ли обьект копированием
    boolean isInCopyProcess = false;
}

// данные об одном ученике в таблице
class NewLearnerAndHisGrades {

    // ученик
    long id;
    String name;
    String surname;
    // его оценки        ( [номер дня][номер урока] ).[номер оценки]
    GradeUnit[][] learnerGrades;

    NewLearnerAndHisGrades(long id, String name, String surname, GradeUnit[][] learnerGrades) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.learnerGrades = learnerGrades;
    }

    // конструктор копии
    NewLearnerAndHisGrades(NewLearnerAndHisGrades other) {
        this.id = other.id;
        this.name = "" + other.name;
        this.surname = "" + other.surname;

        this.learnerGrades = new GradeUnit[other.learnerGrades.length][];
        for (int dayI = 0; dayI < other.learnerGrades.length; dayI++) {
            this.learnerGrades[dayI] = new GradeUnit[other.learnerGrades[dayI].length];
            for (int lessonI = 0; lessonI < other.learnerGrades[dayI].length; lessonI++) {
                // клонируем обьект
                this.learnerGrades[dayI][lessonI] = new GradeUnit(other.learnerGrades[dayI][lessonI]);
            }
        }
    }
}

// класс для хранения предмета
class NewSubjectUnit {
    private long subjectId;
    private String subjectName;

    NewSubjectUnit(long subjectId, String subjectName) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    long getSubjectId() {
        return subjectId;
    }

    String getSubjectName() {
        return subjectName;
    }

    void setSubjectName(String newSubjectName) {
        this.subjectName = newSubjectName;
    }
}

// класс для одного урока
class LessonUnit {

    long lessonId;
    CommentText commentText;

    // на всякий случай
    long subjectId;
    int lessonNumber;
    // нельзя получить из индесов
    int repeat;
    String lessonDate;
    long cabinetId;


    public LessonUnit(long lessonId, long subjectId, long cabinetId, String lessonDate, int lessonNumber, int repeat, long textId, String lessonCommentText) {
        this.lessonId = lessonId;
        this.subjectId = subjectId;
        this.cabinetId = cabinetId;
        this.lessonDate = lessonDate;
        this.lessonNumber = lessonNumber;
        this.repeat = repeat;
        this.commentText = new CommentText(textId, lessonCommentText);
    }

    public LessonUnit(long lessonId, long subjectId, long cabinetId, String lessonDate, int lessonNumber, int repeat) {
        this.lessonId = lessonId;
        this.subjectId = subjectId;
        this.cabinetId = cabinetId;
        this.lessonDate = lessonDate;
        this.lessonNumber = lessonNumber;
        this.repeat = repeat;
        this.commentText = null;
    }

    static class CommentText {

        long commentTextId;
        String lessonCommentText;

        public CommentText(long commentTextId, String lessonCommentText) {
            this.commentTextId = commentTextId;
            this.lessonCommentText = lessonCommentText;
        }
    }
}

// класс хранящий в себе оценки одного ученика за 1 урок
class GradeUnit {
    int[] grades;
    long gradeId;
    int[] gradesTypesIndexes;
    int absTypePoz;

    GradeUnit(int[] grades, long gradeId, int[] gradesTypesIndexes, int absTypePoz) {
        this.grades = grades;
        this.gradeId = gradeId;
        this.gradesTypesIndexes = gradesTypesIndexes;
        this.absTypePoz = absTypePoz;
    }

    // конструктор копии
    GradeUnit(GradeUnit other) {
        // todo добавить проверку на нулевые входные значения
        this.grades = other.grades.clone();
        this.gradeId = other.gradeId;
        this.gradesTypesIndexes = other.gradesTypesIndexes.clone();
        this.absTypePoz = other.absTypePoz;
    }
}

//класс для хранения типов ответов
class AnswersType {
    long id;
    String typeName;

    AnswersType(long id, String typeName) {
        this.id = id;
        this.typeName = typeName;
    }
}

//класс для хранения типов пропусков
class AbsentType {
    long id;
    String absTypeName;
    String absTypeLongName;

    AbsentType(long id, String absTypeName, String absTypeLongName) {
        this.id = id;
        this.absTypeName = absTypeName;
        this.absTypeLongName = absTypeLongName;
    }
}