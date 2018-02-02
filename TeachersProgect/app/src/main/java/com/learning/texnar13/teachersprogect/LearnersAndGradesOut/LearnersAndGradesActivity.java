package com.learning.texnar13.teachersprogect.LearnersAndGradesOut;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LearnersAndGradesActivity extends AppCompatActivity implements CreateLearnerInterface, EditLearnerDialogInterface {

    public static final String CLASS_ID = "classId";

    //----переменные выводящиеся из бд----
    //класс
    static long classId = -1;
    //ученики
    static ArrayList<Long> learnersId = new ArrayList<>();//id учеников
    static ArrayList<String> learnersTitles = new ArrayList<>();//имена учеников
    //массив с уроками
    static long[] subjectsId;
    //выбранный урок
    static int changingSubjectPosition = 0;
    //оценки
    static int[][][][] grades = {};//[ученик][день][урок][оценка]
    //выводимая дата
    static GregorianCalendar viewCalendar = null;
    //таблица с учениками
    TableLayout learnersNamesTable;
    //таблица с оценками
    TableLayout learnersGradesTable;
    //поток загрузки
    Thread progressThread;
    //переменная отвечающая за работу потока
    boolean flag = true;

    //методы работы с диалогом
    @Override
    public void createLearner(String lastName, String name, long classId) {
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
    public void editLearner(String lastName, String name, long learnerId) {
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

    @Override
    public void removeLearner(long learnerId) {
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


    //меню
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.learners_and_grades_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.findItem(R.id.learners_and_grades_menu_help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                Log.i("TeachersApp", "LearnersAndGradesActivity - menu - help");
//                //преход на шпаргалку
//                Intent intent = new Intent(getApplicationContext(), LearnersAndGradesHelp.class);
//                startActivity(intent);
//
//                return true;
//            }
//        });
//
//        return true;
//    }


    //метод старта активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learners_and_grades);
        Toolbar toolbar = (Toolbar) findViewById(R.id.cabinets_out_toolbar);
        setSupportActionBar(toolbar);
        //текст в центре
        //TextView stateText = (TextView) findViewById(R.id.learners_and_grades_state_text);
        //stateText.setText("Здесь выводится список ваших учеников и их оценки по датам. Кнопка '+' добавит ученика, долгоге нажатие на его имя, откроет окно, где можно переименовать или удалить ученика. Сверху можно выбрать дисциплину в которой ставились оценки, и месяц за который выводятся оценки.");
//----переданные данные----
        Intent intent = getIntent();
        classId = intent.getLongExtra(CLASS_ID, -1);
        if (classId == -1) {
            finish();//выходим если не передан класс
        }
//----кнопка добавить ученика----
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.learners_and_grades_add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //создаем диалог
                CreateLearnerDialogFragment createLearnerDialog = new CreateLearnerDialogFragment();
                //создаем параметры
                Bundle args = new Bundle();
                args.putLong("classId", classId);
                createLearnerDialog.setArguments(args);
                //показываем диалог
                createLearnerDialog.show(getFragmentManager(), "createLearnerDialog");
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
//----база данных----
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
//----вывод учеников из бд----
        getLearnersFromDB();
//----заголовок----
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar
        //название класса
        Cursor classCursor = db.getClasses(classId);
        classCursor.moveToFirst();
        getSupportActionBar().setTitle("Ученики в классе " +
                classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
        classCursor.close();
//----переключение даты----
        //константы
        final String[] monthsNames = {
                "ЯНВАРЬ", "ФЕВРАЛЬ", "МАРТ", "АПРЕЛЬ", "МАЙ", "ИЮНЬ", "ИЮЛЬ", "АВГУСТ",
                "СЕНТЯБРЬ", "ОКТЯБРЬ", "НОЯБРЬ", "ДЕКАБРЬ"
        };
        //изменяющийся календарь
        if (viewCalendar == null) {//если зашли в активность а не переворачивали экран ставим тек дату
            viewCalendar = new GregorianCalendar();
            viewCalendar.setTime(new Date());
        }
        //обьявление кнопок и текста
        ImageView imageButtonPrevious = (ImageView) findViewById(R.id.learners_and_grades_activity_button_previous);
        final TextView dateText = (TextView) findViewById(R.id.learners_and_grades_activity_date_text);
        ImageView imageButtonNext = (ImageView) findViewById(R.id.learners_and_grades_activity_button_next);
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
//----спинер с предметами----
        Spinner subjectSpinner = (Spinner) findViewById(R.id.learners_and_grades_activity_subject_spinner);
        //выводим из базы данных список предметов
        Cursor subjectsCursor = db.getSubjectsByClassId(classId);
        subjectsId = new long[subjectsCursor.getCount()];
        final String[] subjectsNames = new String[subjectsCursor.getCount()];
        for (int i = 0; i < subjectsCursor.getCount(); i++) {
            subjectsCursor.moveToNext();
            subjectsId[i] = (subjectsCursor.getLong(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID)));
            subjectsNames[i] = (subjectsCursor.getString(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME)));
        }
        subjectsCursor.close();
        //адаптер для спиннера
        CustomAdapter subjectAdapter = new CustomAdapter(this, R.layout.spiner_dropdown_element_learners_and_grades_subjects, subjectsNames);
        subjectSpinner.setAdapter(subjectAdapter);
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changingSubjectPosition = i;
                updateTable();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //текст с месяцем
        dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));

        //таблица с именами
        learnersNamesTable = (TableLayout) findViewById(R.id.learners_and_grades_table_names);
        //таблица с оценками
        learnersGradesTable = (TableLayout) findViewById(R.id.learners_and_grades_table);
        //выводим имена и оценки при старте
        updateTable();
        db.close();
    }

    //вывод имен учеников в таблицу
    void updateTable() {
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
        headNameOut.setBackgroundColor(Color.BLACK);//parseColor("#1f5b85")
        //текст заголовка ученика
        TextView headName = new TextView(this);
        headName.setText("Ф.И.");
        headName.setTextSize(20);
        headName.setBackgroundColor(Color.WHITE);//светло синий"#bed7e9"Color.parseColor()
        headName.setGravity(Gravity.CENTER);
        headName.setTextColor(Color.BLACK);//тёмно синий parseColor("#1f5b85")
        //отступы рамки
        LinearLayout.LayoutParams headNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        headNameParams.setMargins(0, 0, 0, (int) pxFromDp(2));
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
            learnerNameOut.setBackgroundColor(Color.BLACK);//parseColor("#1f5b85")
            //контейнер для текста
            LinearLayout dateContainer = new LinearLayout(this);
            dateContainer.setBackgroundColor(Color.WHITE);
            //текст ученика
            TextView learnerName = new TextView(this);
            learnerName.setTextSize(20);
            learnerName.setTextColor(Color.BLACK);//parseColor("#1f5b85")
            learnerName.setBackgroundColor(Color.WHITE);//"#bed7e9"
            learnerName.setGravity(Gravity.BOTTOM);
            learnerName.setText(learnersTitles.get(i));
            //отступы контейнера в рамке
            LinearLayout.LayoutParams learnerNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            learnerNameParams.setMargins((int) pxFromDp(10), 0, (int) pxFromDp(10), 0);
            //текст в контейнер
            dateContainer.addView(learnerName, learnerNameParams);
            //отступы рамки
            LinearLayout.LayoutParams dateContainerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            dateContainerParams.setMargins(0, 0, 0, (int) pxFromDp(2));
            //контейнер в рамку
            learnerNameOut.addView(dateContainer, dateContainerParams);
            //рамку в строку
            learner.addView(learnerNameOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            //действия при нажатии на имя ученика
            final int finalI = i;
            learnerNameOut.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
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
            learnersNamesTable.addView(learner, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        }
        getGradesFromDB();
    }

    //ученики из базы
    void getLearnersFromDB() {//вывод учеников из бд
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

    RelativeLayout gradesRoom;

    //оценки из базы
    void getGradesFromDB() {//вывод оценок из бд//----вывод оценок из бд----
        //если метод вызывается опять, то разрешаем подгрузку данных
        flag = true;

        //область с таблицей
        gradesRoom = (RelativeLayout) findViewById(R.id.learners_and_grades_table_relative);

        //вывод оценок перед загрузкой данных, чтобы таблица не была пустая
        outGradesInTable();

        //выводим прогресс бар
        final ProgressBar progressBar = new ProgressBar(getApplicationContext());
        gradesRoom.addView(progressBar, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        //вывод оценок по завершении получения данных
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //вывод оценок
                outGradesInTable();
                //удаляем прогресс бар
                gradesRoom.removeView(progressBar);
            }
        };

        //делаем всё в потоке
        progressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //-- заполняем бд
                //чистим массив от предыдущих значений
                grades = new int
                        [learnersId.size()]
                        [viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)]
                        [9]
                        [3];
                //[ученик][день][урок][оценка]

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
                                if(!flag){
                                    //если была команда закрыть поток без подгрузки
                                    //удаляем прогресс бар
                                    //выводим оценки после загрузки
                                    handler.sendEmptyMessage(0);
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
                                        learnersId.get(i),
                                        subjectsId[changingSubjectPosition],

                                        outHelpStartCalendar,
                                        outHelpEndCalendar
                                );
                                //по оценкам
                                int n;
                                if (gradesLessonCursor.getCount() <= 3) {
                                    n = gradesLessonCursor.getCount();
                                } else n = 3;
                                for (int l = 0; l < n; l++) {

                                    gradesLessonCursor.moveToPosition(l);
                                    //выводим наконец оценку
                                    grades[i][j][k][l] =
                                            gradesLessonCursor.getInt(
                                                    gradesLessonCursor.getColumnIndex(
                                                            SchoolContract.TableLearnersGrades.COLUMN_GRADE
                                                    )
                                            );
                                }
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

    //вывод оценок в таблицу
    void outGradesInTable() {
        //оценки
        learnersGradesTable.removeAllViews();
//---шапка
        //дни
        TableRow headGrades = new TableRow(this);
        for (int i = 0; i < viewCalendar.getMaximum(Calendar.DAY_OF_MONTH); i++) {
            //рамка
            LinearLayout headDateOut = new LinearLayout(this);
            headDateOut.setBackgroundColor(Color.BLACK);
            //текст заголовка ученика
            TextView headDate = new TextView(this);
            headDate.setTextColor(Color.BLACK);
            headDate.setTextSize(20);
            headDate.setBackgroundColor(Color.WHITE);
            headDate.setGravity(Gravity.CENTER);
            headDate.setText(" " + (i + 1) + " ");
            //отступы рамки
            LinearLayout.LayoutParams headDateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            headDateParams.setMargins(0, 0, 0, (int) pxFromDp(2));
            //текст в рамку
            headDateOut.addView(headDate, headDateParams);
            //рамку в строку
            headGrades.addView(headDateOut, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }
        //строку с шапкой в таблицу
        learnersGradesTable.addView(headGrades, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//---тело
        //пробегаемся по ученикам
        for (int i = 0; i < grades.length; i++) {
            //новая строка
            TableRow learnerGrades = new TableRow(this);

            GregorianCalendar gradesOutCalendar = viewCalendar;
            gradesOutCalendar.set(Calendar.DAY_OF_MONTH, 1);
            gradesOutCalendar.set(Calendar.HOUR_OF_DAY, 0);
            gradesOutCalendar.set(Calendar.MINUTE, 0);
            gradesOutCalendar.set(Calendar.SECOND, 0);

            // и по датам
            for (int j = 0; j < grades[i].length; j++) {

                //рамка
                LinearLayout dateOut = new LinearLayout(this);
                dateOut.setBackgroundColor(Color.BLACK);
                //текст
                TextView learnerGrade = new TextView(this);
                learnerGrade.setTextColor(Color.BLACK);
                learnerGrade.setTextSize(20);
                learnerGrade.setBackgroundColor(Color.WHITE);
                learnerGrade.setGravity(Gravity.CENTER);
                //по урокам в дне
                for (int k = 0; k < grades[i][j].length; k++) {
                    //по оценкам в уроке
                    for (int l = 0; l < grades[i][j][k].length; l++) {
                        switch (
                                grades[i][j][k][l]) {
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
                                    learnerGrade.setText(learnerGrade.getText().toString() + grades[i][j][k][l] + " ");
                                } else
                                    learnerGrade.setText(learnerGrade.getText().toString() + " " + grades[i][j][k][l] + " ");
                        }
                    }

                }
                if (learnerGrade.getText().toString().equals("")) {
                    learnerGrade.setText(" - ");
                }

                //отступы рамки
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                textParams.setMargins(0, 0, 0, (int) pxFromDp(2));
                //текст в рамку
                dateOut.addView(learnerGrade, textParams);

//                //при нажатии на оценку
//                dateOut.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //вызываем диалог по ее изменению
//                        EditGradeDialogFragment editGrade = new EditGradeDialogFragment();
//                        //параметры
//                        Bundle bundle = new Bundle();
//                        bundle.putIntArray(EditGradeDialogFragment.GRADES, );
//                        //bundle.putLongArray(EditGradeDialogFragment.GRADES_ID, );
//                        editGrade.show(getFragmentManager(),"EditGrade");
//                    }
//                });



                //добавляем всё в строку
                learnerGrades.addView(dateOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            }
            //добавляем строку в таблицу
            learnersGradesTable.addView(learnerGrades, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        }
    }

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

//-------при закрытии активности-----------

    @Override
    protected void onStop() {
        //останавливаем поток
        flag = false;

        super.onStop();
    }

    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }
}

class CustomAdapter extends ArrayAdapter {
    private Context context;
    private int textViewResourceId;
    private String[] objects;
    //private boolean isFirstElementVisible;
    boolean flag = false;

    CustomAdapter(Context context, int textViewResourceId, String[] objects //,boolean isFirstElementVisible
    ) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.objects = objects;
        //this.isFirstElementVisible = isFirstElementVisible;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context, textViewResourceId, null);
        //if (flag || isFirstElementVisible) {
        TextView tv = (TextView) convertView;
        tv.setGravity(Gravity.CENTER);
        //tv.setBackgroundColor(Color.parseColor("#bed7e9"));//светло синий
        tv.setBackgroundColor(Color.WHITE);
        tv.setText(objects[position]);
        //}
        return convertView;
    }
}