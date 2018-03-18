package com.learning.texnar13.teachersprogect.LearnersAndGradesOut;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LearnersAndGradesActivity extends AppCompatActivity implements CreateLearnerInterface, EditLearnerDialogInterface, EditGradeDialogInterface,UpdateTableInterface {

//--получаем из intent--

    public static final String CLASS_ID = "classId";
    //id класса
    static long classId = -1;

//--выводящиеся из бд--

    //ученики
    // id учеников
    static ArrayList<Long> learnersId = new ArrayList<>();
    //имена учеников
    static ArrayList<String> learnersTitles = new ArrayList<>();
    //массив с уроками
    static long[] subjectsId;
    //выбранный урок
    static int changingSubjectPosition = 0;

//--созданные в активности--

    //выводимая дата
    static GregorianCalendar viewCalendar = null;
    //таблица с учениками
    TableLayout learnersNamesTable;
    //таблица с оценками
    TableLayout learnersGradesTable;
    //используется для вывода прогресс бара, содержит в себе таблицу с учениками
    static RelativeLayout gradesRoom;
    //переменная разрешающая потоку работать
    boolean flag = true;

    //оценки
    static GradeUnit[][][][] grades = {};//[ученик][день][урок][оценка]

//===========методы работы с диалогом============

    //---создание ученика---
    @Override
    public void createLearner(String lastName, String name, long classId) {
        //останавливаем поток загрузки данных
        flag = false;
        //создание ученика вызываемое диалогом CreateLearnerDialogFragment
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.createLearner(lastName, name, classId);
        //обновляем список учеников
        getLearnersFromDB();
        //обновляем таблицу
        GregorianCalendar currentCalendar = new GregorianCalendar();
        currentCalendar.setTime(new Date());
        updateTable();
        db.close();
    }

    @Override
    public void updateAll() {
        //останавливаем поток загрузки данных
        flag = false;
        //обновляем список учеников
        getLearnersFromDB();
        //обновляем таблицу
        updateTable();
    }

    //---переименование ученика---
    @Override
    public void editLearner(String lastName, String name, long learnerId) {
        //останавливаем поток загрузки данных
        flag = false;
        //редактирование ученика вызываемое диалогом EditLearnerDialogFragment
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> learnersId = new ArrayList<>();
        learnersId.add(learnerId);
        db.setLearnerNameAndLastName(learnersId, name, lastName);
        //обновляем список учеников
        getLearnersFromDB();
        //обновляем таблицу
        GregorianCalendar currentCalendar = new GregorianCalendar();
        currentCalendar.setTime(new Date());
        updateTable();
        db.close();
    }

    //---удаление ученика---
    @Override
    public void removeLearner(long learnerId) {
        //останавливаем поток загрузки данных
        flag = false;
        //редактирование ученика вызываемое диалогом EditLearnerDialogFragment
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> learnersId = new ArrayList<>();
        learnersId.add(learnerId);
        db.deleteLearners(learnersId);
        //обновляем список учеников
        getLearnersFromDB();
        //обновляем таблицу
        GregorianCalendar currentCalendar = new GregorianCalendar();
        currentCalendar.setTime(new Date());
        updateTable();
        db.close();
    }

    //---редактирование оценки ученика---
    @Override
    public void editGrade(long[] gradesId, long learnersId, int[] grades, long subjectsId, String[] dates) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        for (int i = 0; i < gradesId.length; i++) {
            if (gradesId[i] == -1) {//если оценки нет то создаем ее
                if (grades[i] != 0) {
                    db.createGrade(learnersId, grades[i], subjectsId, dates[i]);
                }
            } else {//если есть
                if (grades[i] == 0) {//удаляем
                    db.removeGrade(gradesId[i]);
                } else {//или меняем
                    db.editGrade(gradesId[i], grades[i]);
                }
            }
        }
        updateTable();
        Toast toast = Toast.makeText(this, "оценки сохранены", Toast.LENGTH_SHORT);
        toast.show();

    }


//===========старт активности===========

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//=====подготовка активности=====

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learners_and_grades);
        Toolbar toolbar = (Toolbar) findViewById(R.id.cabinets_out_toolbar);
        setSupportActionBar(toolbar);

//----получаем id класса----
        Intent intent = getIntent();
        classId = intent.getLongExtra(CLASS_ID, -1);
        if (classId == -1) {
            finish();//выходим если не передан класс
        }

//----кнопка назад в actionBar----
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//----таблицы вывода----
        //таблица с именами
        learnersNamesTable = (TableLayout) findViewById(R.id.learners_and_grades_table_names);
        //таблица с оценками
        learnersGradesTable = (TableLayout) findViewById(R.id.learners_and_grades_table);

//----переключение даты----
        //константы
        final String[] monthsNames = getResources().getStringArray(R.array.months_names);
        //изменяющийся календарь
        if (viewCalendar == null) {//если зашли в активность а не переворачивали экран ставим тек дату
            viewCalendar = new GregorianCalendar();
            viewCalendar.setTime(new Date());
        }
        //обьявление кнопок и текста
        ImageView imageButtonPrevious = (ImageView) findViewById(R.id.learners_and_grades_activity_button_previous);
        ImageView imageButtonNext = (ImageView) findViewById(R.id.learners_and_grades_activity_button_next);
        final TextView dateText = (TextView) findViewById(R.id.learners_and_grades_activity_date_text);
//ставим месяц
        dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));
//кнопка назад
        imageButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = false;
                if (viewCalendar.get(Calendar.MONTH) == 0) {
                    viewCalendar.set(Calendar.MONTH, 11);
                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) - 1);
                } else {
                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) - 1);
                }
                dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));
                updateTable();
            }
        });
//кнопка вперёд
        imageButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = false;
                if (viewCalendar.get(Calendar.MONTH) == 11) {
                    viewCalendar.set(Calendar.MONTH, 0);
                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) + 1);
                } else {
                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) + 1);
                }
                dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));
                updateTable();
            }
        });

//----плавающая кнопка добавить ученика----
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.learners_and_grades_add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //останавливаем подгрузку данных, чтобы не мешали
                flag = false;
                //--создаем диалог--
                CreateLearnerDialogFragment createLearnerDialog = new CreateLearnerDialogFragment();
                //передаем параметры
                Bundle args = new Bundle();
                args.putLong("classId", classId);
                createLearnerDialog.setArguments(args);
                //показываем диалог
                createLearnerDialog.show(getFragmentManager(), "createLearnerDialog");
            }
        });

//=====работа с базой данных=====

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

//----название класса в заголовок----
        //название класса
        Cursor classCursor = db.getClasses(classId);
        classCursor.moveToFirst();
        getSupportActionBar().setTitle(R.string.title_activity_learners_and_grades);
        getSupportActionBar().setTitle(
                getSupportActionBar().getTitle() +" "+
                        classCursor.getString(
                                classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)
                        )
        );

        classCursor.close();

//----спинер с предметами----
        //--выводим из базы данных список предметов--
        Cursor subjectsCursor = db.getSubjectsByClassId(classId);
        //их id
        subjectsId = new long[subjectsCursor.getCount()];
        //и название
        final String[] subjectsNames = new String[subjectsCursor.getCount()];
        for (int i = 0; i < subjectsCursor.getCount(); i++) {
            subjectsCursor.moveToNext();
            subjectsId[i] = (subjectsCursor.getLong(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID)));
            subjectsNames[i] = (subjectsCursor.getString(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME)));
        }
        subjectsCursor.close();

        //--создаем--
        Spinner subjectSpinner = (Spinner) findViewById(R.id.learners_and_grades_activity_subject_spinner);
        //адаптер для спиннера
        subjectSpinner.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.spiner_dropdown_element_learners_and_grades_subjects,
                subjectsNames
        ));
        //при выборе пункта
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                flag = false;
                changingSubjectPosition = i;
                updateTable();//todo эта штука выполняестся при старте, надо что-то делать
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

//----вывод данных при старте----
        getLearnersFromDB();

        //инициализируем массив
        grades = new GradeUnit
                [learnersId.size()]
                [viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)]
                [9]
                [3];
        //[ученик][день][урок][оценка]

        for (int i = 0; i < grades.length; i++) {
            for (int j = 0; j < grades[i].length; j++) {
                for (int k = 0; k < grades[i][j].length; k++) {
                    for (int l = 0; l < grades[i][j][k].length; l++) {
                        grades[i][j][k][l] = new GradeUnit();
                    }
                }
            }
        }

        updateTable();
        db.close();
    }


//===========получаем учеников из базы===========

    void getLearnersFromDB() {
        Log.i("TeachersApp","LearnersAndGradesActivity - getLearnersFromDB");
        //чистим массивы от предыдущих значений
        learnersId = new ArrayList<>();
        learnersTitles = new ArrayList<>();
        //обновляем массивы свежими данными
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersCursor = db.getLearnersByClassId(classId);
        while (learnersCursor.moveToNext()) {
            learnersId.add(
                    learnersCursor.getLong(
                            learnersCursor.getColumnIndex(
                                    SchoolContract.TableLearners.KEY_LEARNER_ID
                            )
                    )
            );
            learnersTitles.add(
                    learnersCursor.getString(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_SECOND_NAME
                    )) + " " +
                            learnersCursor.getString(learnersCursor.getColumnIndex(
                                    SchoolContract.TableLearners.COLUMN_FIRST_NAME
                            ))
            );
        }
        learnersCursor.close();
        db.close();
    }

    //вывод имен учеников в таблицу
    void updateTable() {
        Log.i("TeachersApp","LearnersAndGradesActivity - updateTable");
        //чистка
        //имена
        learnersNamesTable.removeAllViews();


//таблицы
//-шапка
//--имена
        //заголовок ученика
        TableRow headNameRaw = new TableRow(this);
        //рамка
        LinearLayout headNameOut = new LinearLayout(this);
        headNameOut.setBackgroundColor(getResources().getColor(R.color.colorBackGroundDark));//parseColor("#1f5b85")
        //текст заголовка ученика
        TextView headName = new TextView(this);
        headName.setMinWidth((int) pxFromDp(140));
        headName.setText("  Ф.И.  ");
        headName.setTextSize(20);
        headName.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));//светло синий"#bed7e9"Color.parseColor()Color.WHITE
        headName.setGravity(Gravity.START);
        headName.setTextColor(Color.BLACK);//тёмно синий parseColor("#1f5b85")
        //отступы рамки
        LinearLayout.LayoutParams headNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        headNameParams.setMargins(0, 0, 0, (int) pxFromDp(1));
        //текст в рамку
        headNameOut.addView(headName, headNameParams);
        //рамку в строку
        headNameRaw.addView(headNameOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        //строку в таблицу
        learnersNamesTable.addView(headNameRaw, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);

//-тело таблицы
        for (int i = 0; i < learnersTitles.size(); i++) {//пробегаемся по ученикам
//--строка с учеником
            TableRow learner = new TableRow(this);
            //рамка
            LinearLayout learnerNameOut = new LinearLayout(this);
            learnerNameOut.setBackgroundColor(getResources().getColor(R.color.colorBackGroundDark));//parseColor("#1f5b85")
            //контейнер для текста
            LinearLayout dateContainer = new LinearLayout(this);
            dateContainer.setBackgroundColor(getResources().getColor(R.color.colorBackGround));//Color.WHITE
            //текст ученика
            TextView learnerName = new TextView(this);
            learnerName.setTextSize(20);
            learnerName.setTextColor(Color.BLACK);//parseColor("#1f5b85")
            learnerName.setBackgroundColor(getResources().getColor(R.color.colorBackGround));//"#bed7e9"//Color.WHITE
            learnerName.setGravity(Gravity.BOTTOM);
            learnerName.setText(learnersTitles.get(i));
            //отступы контейнера в рамке
            LinearLayout.LayoutParams learnerNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            learnerNameParams.setMargins((int) pxFromDp(10), 0, (int) pxFromDp(10), 0);
            //текст в контейнер
            dateContainer.addView(learnerName, learnerNameParams);
            //отступы рамки
            LinearLayout.LayoutParams dateContainerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            dateContainerParams.setMargins(0, 0, 0, (int) pxFromDp(1));
            //контейнер в рамку
            learnerNameOut.addView(dateContainer, dateContainerParams);
            //рамку в строку
            learner.addView(learnerNameOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            //действия при нажатии на имя ученика
            final int finalI = i;
            learnerNameOut.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //останавливаем поток загрузки данных
                    flag = false;
                    //диалог
                    EditLearnerDialogFragment editDialog = new EditLearnerDialogFragment();
                    //-данные для диалога-
                    //получаем из бд
                    DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                    Cursor learnerCursor = db.getLearner(learnersId.get(finalI));
                    learnerCursor.moveToFirst();
                    //создаем обьект с данными
                    Bundle args = new Bundle();
                    args.putLong("learnerId", learnersId.get(finalI));
                    args.putString("name", learnerCursor.getString(
                            learnerCursor.getColumnIndex(
                                    SchoolContract.TableLearners.COLUMN_FIRST_NAME)
                    ));
                    args.putString("lastName", learnerCursor.getString(
                            learnerCursor.getColumnIndex(
                                    SchoolContract.TableLearners.COLUMN_SECOND_NAME)
                    ));
                    //данные диалогу
                    editDialog.setArguments(args);
                    //показать диалог
                    editDialog.show(getFragmentManager(), "editLearnerDialog");
                    learnerCursor.close();
                    db.close();
                    return true;
                }
            });

            //добавляем строку в таблицу
            learnersNamesTable.addView(learner, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        }
        getGradesFromDB();
    }


//===========получаем оценки из базы===========

    void getGradesFromDB() {
        Log.i("TeachersApp","LearnersAndGradesActivity - getGradesFromDB");
//----вывод оценок перед загрузкой данных, чтобы таблица не была пустая----
        //outGradesInTable();

//--------всякие штуки для загрузки--------
//область с таблицей, нужна для прогресс бара
        gradesRoom = (RelativeLayout) findViewById(R.id.learners_and_grades_table_relative);
//----выводим прогресс бар----

        final ProgressBar progressBar = new ProgressBar(getApplicationContext());
        gradesRoom.addView(progressBar, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//----закрашиваем серым----
        //создаем экран, который поставим спереди
        final RelativeLayout forwardScreen = new RelativeLayout(this);
        forwardScreen.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        forwardScreen.setBackgroundColor(Color.parseColor("#68505050"));
        //forwardScreen.setBackgroundColor(Color.RED);
        gradesRoom.addView(forwardScreen,RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);



//----разрешаем подгрузку данных----
        flag = true;

//----------делаем всё в потоке-----------

//----handler для обращения к активности----

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    //вывод оценок по завершении получения данных
                    case 0:
                        //вывод оценок
                        outGradesInTable();
                        //удаляем прогресс бар
                        gradesRoom.removeView(progressBar);
                        gradesRoom.removeView(forwardScreen);
                        break;
                    //если получение оценок прервано то не выводим их
                    case 1:
                        //удаляем прогресс бар
                        gradesRoom.removeView(progressBar);
                        gradesRoom.removeView(forwardScreen);
                        break;
                }
            }
        };


//================поток загрузки оценок================

        //делаем всё в потоке
        Thread progressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //для перевода даты в пустые оценки
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //-- заполняем бд
                //чистим массив от предыдущих значений
                grades = new GradeUnit
                        [learnersId.size()]
                        [viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)]
                        [9]
                        [3];
                //[ученик][день][урок][оценка]
                // и инициализируем его()
                for (int i = 0; i < grades.length; i++) {
                    for (int j = 0; j < grades[i].length; j++) {
                        for (int k = 0; k < grades[i][j].length; k++) {
                            for (int l = 0; l < grades[i][j][k].length; l++) {
                                grades[i][j][k][l] = new GradeUnit();
                            }
                        }
                    }
                }

                //изменяющися календари для вывода
                GregorianCalendar outHelpStartCalendar = new GregorianCalendar(
                        viewCalendar.get(Calendar.YEAR),
                        viewCalendar.get(Calendar.MONTH),
                        1,
                        0,
                        0,
                        0
                );
                GregorianCalendar outHelpEndCalendar = new GregorianCalendar(
                        viewCalendar.get(Calendar.YEAR),
                        viewCalendar.get(Calendar.MONTH),
                        1,
                        0,
                        0,
                        0
                );
                if (subjectsId.length != 0) {
                    DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                    //обновляем массивы свежими данными
                    // по ученикам
                    for (int i = 0; i < learnersId.size(); i++) {
                        //по дням
                        for (int j = 0; j < viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); j++) {
                            outHelpStartCalendar.set(Calendar.DAY_OF_MONTH, j + 1);
                            outHelpEndCalendar.set(Calendar.DAY_OF_MONTH, j + 1);
                            //по урокам
                            for (int k = 0; k < 9; k++) {
                                //проверяем переменную потока
                                if (!flag) {
                                    //если была команда закрыть поток без подгрузки
                                    //удаляем прогресс бар
                                    //не выводим оценки после загрузки
                                    handler.sendEmptyMessage(1);
                                    //закрываем
                                    return;
                                }


                                //--время--
                                //начало урока
                                outHelpStartCalendar.set(Calendar.HOUR_OF_DAY,
                                        SchoolContract.TableSubjectAndTimeCabinetAttitude.STANDARD_LESSONS_TIMES[k][0][0]
                                );
                                outHelpStartCalendar.set(Calendar.MINUTE,
                                        SchoolContract.TableSubjectAndTimeCabinetAttitude.STANDARD_LESSONS_TIMES[k][0][1]
                                );
                                //конец урока
                                outHelpEndCalendar.set(Calendar.HOUR_OF_DAY,
                                        SchoolContract.TableSubjectAndTimeCabinetAttitude.STANDARD_LESSONS_TIMES[k][1][0]
                                );
                                outHelpEndCalendar.set(Calendar.MINUTE,
                                        SchoolContract.TableSubjectAndTimeCabinetAttitude.STANDARD_LESSONS_TIMES[k][1][1]
                                );
                                //получаем оценки по времени и предмету
                                Cursor gradesLessonCursor = db.getGradesByLearnerIdSubjectAndTimePeriod(
                                        learnersId.get(i),//todo !!!!!!!!!!!! здесь все еще есть ошибка !!!!!!!!!!!!!!! здесь ошибка java.lang.ArrayIndexOutOfBoundsException
                                        subjectsId[changingSubjectPosition],
                                        outHelpStartCalendar,
                                        outHelpEndCalendar
                                );
                                //по оценкам
                                for (int l = 0; l < 3; l++) {
                                    if (gradesLessonCursor.moveToPosition(l)) {
                                        //есть оценка
                                        grades[i][j][k][l] = new GradeUnit(learnersId.get(i),
                                                gradesLessonCursor.getLong(gradesLessonCursor.getColumnIndex(
                                                        SchoolContract.TableLearnersGrades.KEY_GRADE_ID
                                                )),
                                                gradesLessonCursor.getInt(gradesLessonCursor.getColumnIndex(
                                                        SchoolContract.TableLearnersGrades.COLUMN_GRADE
                                                )),
                                                subjectsId[changingSubjectPosition],
                                                gradesLessonCursor.getString(gradesLessonCursor.getColumnIndex(
                                                        SchoolContract.TableLearnersGrades.COLUMN_TIME_STAMP
                                                ))
                                        );
                                    } else {
                                        //нет оценки
                                        grades[i][j][k][l] = new GradeUnit(
                                                learnersId.get(i),
                                                -1,
                                                0,
                                                subjectsId[changingSubjectPosition],
                                                dateFormat.format(outHelpStartCalendar.getTime())
                                        );
                                    }
                                }
//                                int n;
//                                if (gradesLessonCursor.getCount() <= 3) {
//                                    n = gradesLessonCursor.getCount();
//                                } else n = 3;
//                                for (int l = 0; l < n; l++) {
//
//                                    gradesLessonCursor.moveToPosition(l);
//                                    //выводим наконец оценку
//                                    grades[i][j][k][l] = new GradeUnit(learnersId.get(i),
//                                            gradesLessonCursor.getLong(gradesLessonCursor.getColumnIndex(
//                                                    SchoolContract.TableLearnersGrades.KEY_GRADE_ID
//                                            )),
//                                            gradesLessonCursor.getInt(gradesLessonCursor.getColumnIndex(
//                                                    SchoolContract.TableLearnersGrades.COLUMN_GRADE
//                                            )),
//                                            subjectsId[changingSubjectPosition],
//                                            gradesLessonCursor.getString(gradesLessonCursor.getColumnIndex(
//                                                    SchoolContract.TableLearnersGrades.COLUMN_TIME_STAMP
//                                            ))
//                                    );
//                                }
                                gradesLessonCursor.close();
                            }
                        }
                    }
                    db.close();
                }

                //выводим оценки после загрузки
                handler.sendEmptyMessage(0);
            }
        });
        progressThread.setName("getGradesThread");
        progressThread.start();
    }


//===========вывод оценок в таблицу===========

    void outGradesInTable() {
        Log.i("TeachersApp","LearnersAndGradesActivity - outGradesInTable");
        // в таблице дни раскрашиваются в шахматном порядке
//если первый столбец в дне то выводим иначе если нет оценок, то не выводим

        //чистка
        learnersGradesTable.removeAllViews();

        //дни
        TableRow headGrades = new TableRow(this);
        //строку с шапкой в таблицу
        learnersGradesTable.addView(headGrades, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        //строки с учениками
        TableRow[] learnerTableRows = new TableRow[grades.length];
        for (int i = 0; i < grades.length; i++) {
            //новая строка
            learnerTableRows[i] = new TableRow(this);

            GregorianCalendar gradesOutCalendar = viewCalendar;
            gradesOutCalendar.set(Calendar.DAY_OF_MONTH, 1);
            gradesOutCalendar.set(Calendar.HOUR_OF_DAY, 0);
            gradesOutCalendar.set(Calendar.MINUTE, 0);
            gradesOutCalendar.set(Calendar.SECOND, 0);
            //добавляем строку в таблицу
            learnersGradesTable.addView(learnerTableRows[i], TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        }

        //дни
        for (int i = 0; i < viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {

            //уроки в днях
            for (int j = 0; j < 9; j++) {
                //проверка есть ли в этом дне хоть у одного ученика оценки
                boolean flag = false;
                //по ученикам
                out:
                for (int k = 0; k < grades.length; k++) {
                    //по оценкам за этот день
                    for (int l = 0; l < 3; l++) {
                        //[ученик][день][урок][оценка]
                        if (grades[k][i][j][l].grade//todo здесь ошибка java.lang.NullPointerException
                                != 0) {
                            flag = true;
                            break out;
                        }
                    }
                }
                //выводим новый столбец
                if (j == 0 || flag) {
//---шапка
                    //рамка
                    LinearLayout headDateOut = new LinearLayout(this);
                    headDateOut.setBackgroundColor(getResources().getColor(R.color.colorBackGroundDark));
                    //текст заголовка ученика
                    TextView headDate = new TextView(this);

                    headDate.setTextSize(20);
                    if (i % 2 == 0) {
                        headDate.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));//Color.WHITE
                    } else {
                        headDate.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                        //headDate.setBackgroundColor(Color.parseColor("#bed7e9"));
                    }
                    headDate.setGravity(Gravity.CENTER);
                    if (j != 0) {
                        headDate.setTextColor(Color.WHITE);
                        headDate.setText(" " + (j + 1) + " ");
                    } else {
                        headDate.setTextColor(Color.BLACK);
                        headDate.setText(" " + (i + 1) + " ");
                    }
                    //отступы рамки
                    LinearLayout.LayoutParams headDateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    headDateParams.setMargins(0, 0, 0, (int) pxFromDp(1));
                    //текст в рамку
                    headDateOut.addView(headDate, headDateParams);
                    //рамку в строку
                    headGrades.addView(headDateOut, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//---тело
                    //по ученикам
                    for (int k = 0; k < grades.length; k++) {
                        //рамка
                        LinearLayout dateOut = new LinearLayout(this);
                        dateOut.setBackgroundColor(getResources().getColor(R.color.colorBackGroundDark));
                        //текст
                        TextView learnerGrade = new TextView(this);
                        learnerGrade.setTextColor(Color.BLACK);
                        learnerGrade.setTextSize(20);
                        learnerGrade.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
                        learnerGrade.setGravity(Gravity.CENTER);
                        //по оценкам в уроке
                        for (int l = 0; l < grades[k][i][j].length; l++) {
                            switch (
                                    grades[k][i][j][l].grade) {
                                case 0:

                                    break;
                                case -2:
                                    if (!learnerGrade.getText().toString().equals("")) {
                                        learnerGrade.setText(learnerGrade.getText().toString() + "Н ");
                                    } else
                                        learnerGrade.setText(learnerGrade.getText().toString() + " Н ");
                                    break;
                                default:
                                    if (!learnerGrade.getText().toString().equals("")) {
                                        learnerGrade.setText(learnerGrade.getText().toString() + grades[k][i][j][l].grade + " ");
                                    } else
                                        learnerGrade.setText(learnerGrade.getText().toString() + " " + grades[k][i][j][l].grade + " ");
                            }
                        }

                        if (learnerGrade.getText().toString().equals("")) {
                            learnerGrade.setText(" - ");
                        }

                        //параметры текста
                        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        //отступы рамки
                        textParams.setMargins((int) pxFromDp(1), 0, 0, (int) pxFromDp(1));
                        //текст в рамку
                        dateOut.addView(learnerGrade, textParams);
                        //добавляем всё в строку
                        learnerTableRows[k].addView(dateOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);

                        final int[] arrayGrade = new int[grades[k][i][j].length];
                        final long[] arrayGradeId = new long[grades[k][i][j].length];
                        final long finalLearnerId = grades[k][i][j][0].learnerId;
                        final long finalSubjectId = grades[k][i][j][0].subjectId;
                        final String finalDate[] = new String[grades[k][i][j].length];


                        for (int l = 0; l < arrayGrade.length; l++) {
                            arrayGrade[l] = grades[k][i][j][l].grade;
                            arrayGradeId[l] = grades[k][i][j][l].id;
                            finalDate[l] = grades[k][i][j][l].date;
                        }
                        //при нажатии на оценку
                        dateOut.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //останавливаем подгрузку данных, чтобы не мешали
                                //flag = false;//TO=DO надо остановить подгрузку данных, чтобы при переоценивании новых не получить ошибку, надо придумать как это сделать, может через метод?
                                //вызываем диалог по ее изменению
                                EditGradeDialogFragment editGrade = new EditGradeDialogFragment();
                                //параметры
                                Bundle bundle = new Bundle();
                                // id оценки
                                bundle.putLongArray(EditGradeDialogFragment.GRADES_ID, arrayGradeId);
                                // id ученика
                                bundle.putLong(EditGradeDialogFragment.LEARNER_ID, finalLearnerId);
                                // оценка
                                bundle.putIntArray(EditGradeDialogFragment.GRADES, arrayGrade);
                                // id предмета
                                bundle.putLong(EditGradeDialogFragment.SUBJECT_ID, finalSubjectId);
                                // дата
                                bundle.putStringArray(EditGradeDialogFragment.DATE, finalDate);

                                Log.e("" + finalDate[0] + " " + finalSubjectId, "---");

                                editGrade.setArguments(bundle);
                                editGrade.show(getFragmentManager(), "EditGrade");
                            }
                        });


                    }
                }
            }

        }
//----очищаем фон от серого после загрузки----
        //findViewById(R.id.learners_and_grades_table_grades_forward_screen)
        //        .setBackgroundColor(Color.parseColor("#00FFFFFF"));
    }

//===========при закрытии активности===========

    @Override
    protected void onStop() {
        //останавливаем поток
        flag = false;

        super.onStop();
    }

//===========системные кнопки===========

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://кнопка назад в actionBar
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //-------системные методы------
    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }

}

class GradeUnit {
    long learnerId = -1;
    long id = -1;
    int grade = 0;
    long subjectId = -1;
    String date;


    GradeUnit(long learnerId, long id, int grade, long subjectId, String date) {
        this.learnerId = learnerId;
        this.id = id;
        this.grade = grade;
        this.subjectId = subjectId;
        this.date = date;
    }

    GradeUnit() {

    }
}