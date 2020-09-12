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
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics.LearnersGradesStatisticsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class LearnersAndGradesActivity extends AppCompatActivity implements CreateLearnerInterface, EditLearnerDialogInterface, EditGradeDialogInterface, UpdateTableInterface, View.OnTouchListener, SubjectsDialogInterface {

    // тег для логов
    private static final String TAG = "TeachersApp";
    // константа по которой через intent передается id класса
    public static final String CLASS_ID = "classId";


    // id класса полученное через intent
    static long classId = -1;

    static DataObject dataLearnersAndGrades;

    // массив с предметами
    static NewSubjectUnit[] subjects;
    // номер выбранного предмета в массиве
    int chosenSubjectPosition;

    // локализованные названия месяцев
    String[] monthsNames;
    String[] transformedMonthsNames;
    // размер максимальной оценки
    int maxGrade;
    // названия типов ответов и их id
    AnswersType[] answersTypes;
    // названия типов пропусков и их id
    AbsentType[] absentTypes;


    // календарь хранящий в себе отображаемую дату
    static GregorianCalendar viewCalendar = null;
    // текущая дата
    GregorianCalendar currentCalendar;

    // переменная работающего в текущий момент потока
    static GetGradesThread loadGradesThread = null;


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
                    subjectsDialogFragment.show(getFragmentManager(), "subjectsDialogFragment - hello");

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
                subjectsDialogFragment.show(getFragmentManager(), "subjectsDialogFragment - hello");
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
        // если отображаемый календарь уже был создан, сравниваем с ним текущую дату
        if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
            learnersAndGradesTableView.currentDate = currentCalendar.get(Calendar.DAY_OF_MONTH) - 1;
        } else
            learnersAndGradesTableView.currentDate = -1;


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

                // выставляем в таблицу текущую дату
                if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
                    learnersAndGradesTableView.currentDate = currentCalendar.get(Calendar.DAY_OF_MONTH) - 1;
                } else
                    learnersAndGradesTableView.currentDate = -1;

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

                // выставляем в таблицу текущую дату
                if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
                    learnersAndGradesTableView.currentDate = currentCalendar.get(Calendar.DAY_OF_MONTH) - 1;
                } else
                    learnersAndGradesTableView.currentDate = -1;


                // получаем оценки из бд по новой дате
                getGradesFromDB();
            }
        });


        if (dataLearnersAndGrades == null) {
            dataLearnersAndGrades = new DataObject();
        }

        // сразу выставим названия пропусков которые до этого достали
        dataLearnersAndGrades.absNames = new String[absentTypes.length];
        for (int i = 0; i < absentTypes.length; i++) {
            dataLearnersAndGrades.absNames[i] = absentTypes[i].absTypeName;
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
                learnersAndGradesTableView.setData(dataLearnersAndGrades);
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
        dataLearnersAndGrades.learnersAndHisGrades = new ArrayList<>();
        // и заполняем новыми
        while (learnersCursor.moveToNext()) {
            // создаем нового ученика
            dataLearnersAndGrades.learnersAndHisGrades.add(new NewLearnerAndHisGrades(
                    learnersCursor.getLong(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.KEY_LEARNER_ID)),
                    learnersCursor.getString(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
                    learnersCursor.getString(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_SECOND_NAME)),
                    new NewGradeUnit[][]{}
            ));
        }
        learnersCursor.close();
        db.close();

        // передаем данные во view (пока только ученики)
        learnersAndGradesTableView.setData(dataLearnersAndGrades);

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
                learnersAndGradesTableView.setData(dataLearnersAndGrades);
                // перерисоввываем окно
                learnersAndGradesTableView.invalidate();

                // разрешаем менять оценки
                dataLearnersAndGrades.chosenGradePosition = new int[]{-1, -1, -1};
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
            Log.i(TAG, "loadGradesThread-stoppedEarly");
            runFlag = false;
        }

        @Override
        public void run() {
            Log.i(TAG, "StartLoadGradesThread");
            // разрешаем подгрузку данных
            runFlag = true;

            // копируем оригинальный массив с учениками чтобы пока изменять только копию
            dataLearnersAndGrades.isInCopyProcess = true;
            ArrayList<NewLearnerAndHisGrades> copy = new ArrayList<>();
            for (int learnersI = 0; learnersI < dataLearnersAndGrades.learnersAndHisGrades.size(); learnersI++) {
                copy.add(
                        new NewLearnerAndHisGrades(dataLearnersAndGrades.learnersAndHisGrades.get(learnersI))
                );
            }
            dataLearnersAndGrades.isInCopyProcess = false;

            // а также копируем календарь
            GregorianCalendar loadGradesTempCalendar = new GregorianCalendar();
            loadGradesTempCalendar.set(
                    viewCalendar.get(Calendar.YEAR),
                    viewCalendar.get(Calendar.MONTH),
                    viewCalendar.get(Calendar.DAY_OF_MONTH)
            );

            // выставляем текущую дату
            dataLearnersAndGrades.yearAndMonth = loadGradesTempCalendar.getTime();

            // если не выбран урок возвращаем пустые ячейки
            if (chosenSubjectPosition < 0) {
                // пробегаемся по ученикам
                for (int learnerI = 0; learnerI < copy.size(); learnerI++) {
                    if (!runFlag) {// если была команда закрыть поток без подгрузки
                        // выходим  из метода подгрузки
                        return;
                    }

                    // инициализируем по дням
                    copy.get(learnerI).learnerGrades =
                            new NewGradeUnit[loadGradesTempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][];
                    // пробегаемся по дням копируя
                    for (int dayI = 0; dayI < copy.get(learnerI).learnerGrades.length; dayI++) {
                        if (!runFlag) {// если была команда закрыть поток без подгрузки
                            // выходим  из метода подгрузки
                            return;
                        }

                        // инициализируем по урокам
                        copy.get(learnerI).learnerGrades[dayI] = new NewGradeUnit[9];
                        // пробегаемся по урокам
                        for (int lessonI = 0; lessonI < copy.get(learnerI).learnerGrades[dayI].length; lessonI++) {
                            if (!runFlag) {// если была команда закрыть поток без подгрузки
                                // выходим  из метода подгрузки
                                return;
                            }

                            // заполняем ячейки уроков пустыми оценками
                            copy.get(learnerI).learnerGrades[dayI][lessonI] = new NewGradeUnit(
                                    new int[]{0, 0, 0},
                                    -1,
                                    new int[]{0, 0, 0},
                                    -1
                            );
                        }
                    }
                }
            } else {

                // открываем бд
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                // получаем время уроков
                int[][] timeOfLessons = db.getSettingsTime(1);

                // пробегаемся по ученикам
                for (int learnerI = 0; learnerI < copy.size(); learnerI++) {

                    // инициализируем по дням
                    copy.get(learnerI).learnerGrades =
                            new NewGradeUnit[loadGradesTempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][];

                    // пробегаемся по дням копируя
                    for (int dayI = 0; dayI < copy.get(learnerI).learnerGrades.length; dayI++) {

                        if (!runFlag) {// если была команда закрыть поток без подгрузки
                            // выходим  из метода подгрузки
                            return;
                        }// todo надо чтобы новый поток ждал завенршения старого прежде чем начать загрузку данных


                        // сделаем запрос всего дня и если он пуст заполним его нулевыми значениями
                        loadGradesTempCalendar.set(Calendar.DAY_OF_MONTH, dayI + 1);

                        Cursor allDayGradesCursor = db.getGradesByLearnerIdSubjectDateAndLessonsPeriod(
                                copy.get(learnerI).id,
                                subjects[chosenSubjectPosition].getSubjectId(),
                                dateFormat.format(loadGradesTempCalendar.getTime()),
                                0,
                                10 // todo получать количество из бд
                        );


                        // инициализируем по урокам
                        copy.get(learnerI).learnerGrades[dayI] = new NewGradeUnit[9];

                        // если в дне есть оценки
                        if (allDayGradesCursor.getCount() != 0) {

                            if (!runFlag) {// если была команда закрыть поток без подгрузки
                                // выходим  из метода подгрузки
                                allDayGradesCursor.close();
                                return;
                            }

                            // пробегаемся по урокам копируя
                            for (int lessonI = 0; lessonI < copy.get(learnerI).learnerGrades[dayI].length; lessonI++) {

                                if (!runFlag) {// если была команда закрыть поток без подгрузки
                                    // выходим  из метода подгрузки
                                    allDayGradesCursor.close();
                                    return;
                                }

                                // задаем время урока по которому будем запрашивать оценки
                                loadGradesTempCalendar.set(Calendar.DAY_OF_MONTH, dayI + 1);

                                // получаем оценки по времени и предмету
                                Cursor gradesLessonCursor = db.getGradesByLearnerIdSubjectDateAndLesson(
                                        copy.get(learnerI).id,
                                        subjects[chosenSubjectPosition].getSubjectId(),//todo !!!!!!!!!!!! здесь все еще есть ошибка !!!!!!!!!!!!!!! здесь ошибка java.lang.ArrayIndexOutOfBoundsException
                                        dateFormat.format(loadGradesTempCalendar.getTime()),
                                        lessonI
                                );

                                // массивы оценок
                                int[] grades = new int[3];
                                long gradeId;
                                int[] gradesTypesId = {1, 1, 1};
                                int absTypePoz;

                                // (первая запись в курсоре соответствуют последней добавленной в бд)
                                // переходим к первой записи (если она есть)
                                if (gradesLessonCursor.moveToNext()) {
                                    for (int i = 0; i < SchoolContract.TableLearnersGrades.COLUMNS_GRADE.length; i++) {
                                        // оценка
                                        grades[i] = gradesLessonCursor.getInt(gradesLessonCursor.getColumnIndex(
                                                SchoolContract.TableLearnersGrades.COLUMNS_GRADE[i]
                                        ));
                                        // тип
                                        gradesTypesId[i] = gradesLessonCursor.getInt(gradesLessonCursor.getColumnIndex(
                                                SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[i]
                                        ));
                                        // ищем соответствующий тип в типах оценок todo сделать красивей + (-1)(хотя это уже есть не явно)
                                        for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                                            if (gradesTypesId[i] == answersTypes[typeI].id) {
                                                gradesTypesId[i] = typeI;
                                                break;
                                            }
                                        }
                                    }
                                    // id
                                    gradeId = gradesLessonCursor.getLong(gradesLessonCursor.getColumnIndex(
                                            SchoolContract.TableLearnersGrades.KEY_GRADE_ID
                                    ));
                                    // пропуск
                                    if (gradesLessonCursor.isNull(gradesLessonCursor.getColumnIndex(
                                            SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID
                                    ))) {
                                        absTypePoz = -1;// если null
                                    } else
                                        absTypePoz = gradesLessonCursor.getInt(gradesLessonCursor.getColumnIndex(
                                                SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID
                                        ));
                                    // ищем соответствующий тип в типах оценок todo сделать красивей + (-1)
                                    for (int typeI = 0; typeI < absentTypes.length; typeI++) {
                                        if (absTypePoz == absentTypes[typeI].id) {
                                            absTypePoz = typeI;
                                            break;
                                        }
                                    }
                                    /*
                                    * тот самый красивый код (lesson Activity)
                                    * // проходимся в цикле по всем типам оценок запоминая номер попавшегося
                        learnersAndTheirGrades[i].absTypePozNumber = -1;
                        int poz = 0;
                        while (absentTypes.length > poz) {
                            if (absentTypes[poz].id != absId) {
                                poz++;
                            } else {
                                learnersAndTheirGrades[i].absTypePozNumber = poz;
                                break;
                            }
                        }
                                    *
                                    * */

                                    // сохраняем
                                    copy.get(learnerI).learnerGrades[dayI][lessonI] = new NewGradeUnit(
                                            grades,
                                            gradeId,
                                            gradesTypesId,
                                            absTypePoz
                                    );
                                } else { // нет оценки
                                    copy.get(learnerI).learnerGrades[dayI][lessonI] = new NewGradeUnit(
                                            new int[]{0, 0, 0},
                                            -1,
                                            new int[]{0, 0, 0},
                                            -1
                                    );
                                }
                                gradesLessonCursor.close();
                            }
                        } else {// если оценок в дне нет заполняем его нулевыми значениями

                            if (!runFlag) {// если была команда закрыть поток без подгрузки
                                // выходим  из метода подгрузки
                                allDayGradesCursor.close();
                                return;
                            }

                            // пробегаемся по урокам
                            for (int lessonI = 0; lessonI < copy.get(learnerI).learnerGrades[dayI].length; lessonI++) {

                                if (!runFlag) {// если была команда закрыть поток без подгрузки
                                    // выходим  из метода подгрузки
                                    allDayGradesCursor.close();
                                    return;
                                }

                                // заполняем ячейки уроков пустыми оценками
                                copy.get(learnerI).learnerGrades[dayI][lessonI] = new NewGradeUnit(
                                        new int[]{0, 0, 0},
                                        -1,
                                        new int[]{0, 0, 0},
                                        -1
                                );
                            }
                        }
                        allDayGradesCursor.close();
                    }
                }
            }

            // если этот поток не был прерван ранее
            if (runFlag) {
                // присваиваем измененную копию
                dataLearnersAndGrades.learnersAndHisGrades = copy;
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
        dataLearnersAndGrades.chosenGradePosition = new int[]{0, -1, -1};

        // запускаем поток
        loadGradesThread.setName("loadGradesThread - hello");
        loadGradesThread.start();
    }


// ------------------ обработка касания таблицы ------------------

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

                    Log.e(TAG, "onTouch: TOUCH_DOWN " + touchData[0] + " " + touchData[1] + " " + touchData[2]);

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
                                createLearnerDialog.show(getFragmentManager(), "createLearnerDialog");
                            }

                        } else {// иначе проверяем что нажато, оценки или ученки
                            if (touchData[1] != -1) {// если нажата область оценок
                                if (touchData[0] == -1) {// если нажата дата над оценкой
                                    Toast.makeText(this, "YAY"+touchData[1]+touchData[2],Toast.LENGTH_SHORT).show();
                                    // показываем диалог комментария к уроку

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
                                        subjectsDialogFragment.show(getFragmentManager(), "subjectsDialogFragment - hello");
                                    } else {

                                        // чтобы не было нескольких вызовов одновременно
                                        if (dataLearnersAndGrades.chosenGradePosition[0] == -1) {

                                            // ставим полученные координаты оценки как выбранные
                                            dataLearnersAndGrades.chosenGradePosition = touchData;

                                            // вызываем диалог изменения оценок
                                            GradeEditDialogFragment editGrade = new GradeEditDialogFragment();
                                            //параметры
                                            Bundle bundle = new Bundle();

                                            // имя ученика
                                            bundle.putString(
                                                    GradeEditDialogFragment.ARGS_LEARNER_NAME,
                                                    dataLearnersAndGrades.learnersAndHisGrades.get(touchData[0]).surname + " " + dataLearnersAndGrades.learnersAndHisGrades.get(touchData[0]).name
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
                                                    dataLearnersAndGrades.learnersAndHisGrades.get(touchData[0]).learnerGrades[touchData[1]][touchData[2]].grades.clone()
                                            );

                                            // массив с номерами выбранных типов
                                            bundle.putIntArray(
                                                    GradeEditDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY,
                                                    dataLearnersAndGrades.learnersAndHisGrades.get(touchData[0]).learnerGrades[touchData[1]][touchData[2]].gradesTypesIndexes.clone()
                                            );

                                            // выбранный номер пропуска
                                            bundle.putInt(
                                                    GradeEditDialogFragment.ARGS_INT_GRADES_ABSENT_TYPE_NUMBER,
                                                    dataLearnersAndGrades.learnersAndHisGrades.get(touchData[0]).learnerGrades[touchData[1]][touchData[2]].absTypePoz
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
                                            dataLearnersAndGrades.learnersAndHisGrades.get(touchData[0]).name);
                                    args.putString(LearnerEditDialogFragment.ARGS_LEARNER_LAST_NAME,
                                            dataLearnersAndGrades.learnersAndHisGrades.get(touchData[0]).surname);

                                    // передаем данные диалогу
                                    editDialog.setArguments(args);

                                    // показываем диалог
                                    editDialog.show(getFragmentManager(), "editLearnerDialog - hello");

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
        NewGradeUnit[][] emptyGradeUnitsArray = new NewGradeUnit[viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][9];
        // пробегаемся по дням
        for (int dayI = 0; dayI < viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); dayI++) {
            // пробегаемся по урокам
            for (int lessonI = 0; lessonI < emptyGradeUnitsArray[dayI].length; lessonI++) {

                // заполняем ячейки уроков пустыми оценками
                emptyGradeUnitsArray[dayI][lessonI] =
                        new NewGradeUnit(
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
        for (int learnerI = 0; learnerI < dataLearnersAndGrades.learnersAndHisGrades.size(); learnerI++) {
            // если полученное имя лексикографически меньше текущего в списке то ставим полученное перед текущим
            if ((lastName.compareTo(dataLearnersAndGrades.learnersAndHisGrades.get(learnerI).surname) < 0) ||
                    // при равных фамилиях сравниваем имена
                    (lastName.compareTo(dataLearnersAndGrades.learnersAndHisGrades.get(learnerI).surname) == 0 &&
                            name.compareTo(dataLearnersAndGrades.learnersAndHisGrades.get(learnerI).name) < 0)
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
        learnersAndGradesTableView.setData(dataLearnersAndGrades);
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
            db.setLearnerNameAndLastName(dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenLearnerPosition).id, name, lastName);
            db.close();

            // меняем локальный список учеников todo сортировка
            dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenLearnerPosition).name = name;
            dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenLearnerPosition).surname = lastName;

            // выводим изменившихся учеников во view
            learnersAndGradesTableView.setData(dataLearnersAndGrades);
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
        db.deleteLearner(dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenLearnerPosition).id);
        db.close();

        // удаляем ученика из списка
        dataLearnersAndGrades.learnersAndHisGrades.remove(dataLearnersAndGrades.chosenLearnerPosition);

        // выводим изменившихся учеников во view
        learnersAndGradesTableView.setData(dataLearnersAndGrades);
        learnersAndGradesTableView.invalidate();

        // разрешаем редактировать учеников
        dataLearnersAndGrades.chosenLearnerPosition = -1;
    }

    // метод редактирования оценки ученика из диалога GradeEditDialogFragment
    @Override
    public void editGrades(int[] grades, int absTypePoz, int[] chosenTypesNumbers, int lessonPoz) {
        Log.i(TAG, "fromDialog-editGrade");
        // проверяем выбрана ли все еще дата
        if (dataLearnersAndGrades.chosenGradePosition[0] != -1 && dataLearnersAndGrades.chosenGradePosition[1] != -1 && lessonPoz != -1) {

            // сохраняем оценки в массив
            dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).
                    learnerGrades[dataLearnersAndGrades.chosenGradePosition[1]][lessonPoz].grades = grades;
            // типы
            dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).
                    learnerGrades[dataLearnersAndGrades.chosenGradePosition[1]][lessonPoz].gradesTypesIndexes = chosenTypesNumbers;
            // отсутствие
            dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).
                    learnerGrades[dataLearnersAndGrades.chosenGradePosition[1]][lessonPoz].absTypePoz = absTypePoz;

            // сохраняем оценки в бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // по индексам вычисляем время когда оценка была поставлена
            int[][] timeOfLessons = db.getSettingsTime(1);

            viewCalendar.set(Calendar.DAY_OF_MONTH, dataLearnersAndGrades.chosenGradePosition[1] + 1);
            String gradeDate = dateFormat.format(viewCalendar.getTime());

            // id оценки -1, значит оценка новая
            if (dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).
                    learnerGrades[dataLearnersAndGrades.chosenGradePosition[1]][lessonPoz].gradeId == -1) {
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
                    dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).
                            learnerGrades[dataLearnersAndGrades.chosenGradePosition[1]][lessonPoz].gradeId =
                            db.createGrade(
                                    dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).id,
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
                    db.removeGrade(dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).
                            learnerGrades[dataLearnersAndGrades.chosenGradePosition[1]][lessonPoz].gradeId);
                    // и из списка
                    dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).
                            learnerGrades[dataLearnersAndGrades.chosenGradePosition[1]][lessonPoz].gradeId = -1;
                } else {
                    long abs;
                    if (absTypePoz != -1) {
                        abs = absentTypes[absTypePoz].id;
                    } else
                        abs = -1;
                    // не нулевые меняем в бд
                    db.editGrade(
                            dataLearnersAndGrades.learnersAndHisGrades.get(dataLearnersAndGrades.chosenGradePosition[0]).
                                    learnerGrades[dataLearnersAndGrades.chosenGradePosition[1]][lessonPoz].gradeId,
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
            learnersAndGradesTableView.setData(dataLearnersAndGrades);
            learnersAndGradesTableView.invalidate();
        }
        //разрешаем пользователю менять оценки
        dataLearnersAndGrades.chosenGradePosition = new int[]{-1, -1, -1};
    }

    // метод разрешающий пользователю изменять оценки из диалога GradeEditDialogFragment
    @Override
    public void gradesSelectNothing() {
        Log.i(TAG, "fromDialog-allowUserEditGrades");

        //разрешаем пользователю менять оценки
        dataLearnersAndGrades.chosenGradePosition = new int[]{-1, -1, -1};

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
        dataLearnersAndGrades.learnersAndHisGrades = null;
        dataLearnersAndGrades.chosenLearnerPosition = -1;
        dataLearnersAndGrades.chosenGradePosition = new int[]{-1, -1, -1};
        dataLearnersAndGrades = null;
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

//    // -- метод трансформации числа в текст с двумя позициями --
//    String getTwoSymbols(int number) {
//        if (number < 10 && number >= 0) {
//            return "0" + number;
//        } else {
//            return "" + number;
//        }
//    }

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


// класс хранящий в себе оценки одного ученика за 1 урок
class NewGradeUnit {
    int[] grades;
    long gradeId;
    int[] gradesTypesIndexes;
    int absTypePoz;

    NewGradeUnit(int[] grades, long gradeId, int[] gradesTypesIndexes, int absTypePoz) {
        this.grades = grades;
        this.gradeId = gradeId;
        this.gradesTypesIndexes = gradesTypesIndexes;
        this.absTypePoz = absTypePoz;
    }

    // конструктор копии
    NewGradeUnit(NewGradeUnit other) {
        // todo добавить проверку на нулевые входные значения
        this.grades = other.grades.clone();
        this.gradeId = other.gradeId;
        this.gradesTypesIndexes = other.gradesTypesIndexes.clone();
        this.absTypePoz = other.absTypePoz;
    }
}

// класс который хранит все данные
class DataObject {
    // сюда положен месяц и год от даты за которую берутся данные
    Date yearAndMonth;

    // массив с названиями пропусков
    String[] absNames;

    // массив с учениками и их оценками
    ArrayList<NewLearnerAndHisGrades> learnersAndHisGrades;
    // не занят ли массив копированием
    boolean isInCopyProcess;

    // номер выбранного ученика
    int chosenLearnerPosition;
    // позиция выбранной оценки (ученик,день,урок)
    int[] chosenGradePosition;
}

// данные об одном ученике в таблице
class NewLearnerAndHisGrades {


    // ученик
    long id;
    String name;
    String surname;
    // его оценки        ( [номер дня][номер урока] ).[номер оценки]
    NewGradeUnit[][] learnerGrades;

    NewLearnerAndHisGrades(long id, String name, String surname, NewGradeUnit[][] learnerGrades) {
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

        this.learnerGrades = new NewGradeUnit[other.learnerGrades.length][];
        for (int dayI = 0; dayI < other.learnerGrades.length; dayI++) {
            this.learnerGrades[dayI] = new NewGradeUnit[other.learnerGrades[dayI].length];
            for (int lessonI = 0; lessonI < other.learnerGrades[dayI].length; lessonI++) {
                // клонируем обьект
                this.learnerGrades[dayI][lessonI] = new NewGradeUnit(other.learnerGrades[dayI][lessonI]);
            }
        }
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