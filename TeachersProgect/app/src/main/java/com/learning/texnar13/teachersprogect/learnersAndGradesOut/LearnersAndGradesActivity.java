package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics.LearnersGradesStatisticsActivity;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LearnersAndGradesActivity extends AppCompatActivity implements CreateLearnerInterface, EditLearnerDialogInterface, EditGradeDialogInterface, AllowUserEditGradesInterface, UpdateTableInterface, SubjectNameLearnersDialogInterface, SubjectRemoveLearnersDialogInterface {


    // todo разнести по классам диалоги! И другие исправления
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
    //максимальная оценка
    static long maxAnswersCount;
    // цветные оценки
    static boolean areTheGradesColored = false;

//--созданные в активности--

    //выводимая дата
    static GregorianCalendar viewCalendar = null;
    //таблица с учениками
    TableLayout learnersNamesTable;
    //таблица с оценками
    TableLayout learnersGradesTable;
    //используется для вывода прогресс бара, содержит в себе таблицу с учениками
    static RelativeLayout gradesRoom;
    //предметы
    Spinner subjectSpinner;

//-переменные блокираторы

    //переменная разрешающая потоку загрузки оценок работать
    boolean flag = true;
    //переменная разрешающая взаимодействие с оценками(например диалоги), от одновременных касаний
    boolean canEditGrades = true;
    //переменная разрешающая диалог изменения учеников(кроме создания)
    boolean canEditLearners = true;

    //оценки
    static NewGradeUnit[][][] allGrades = {};//[ученик][день][урок]

//=====================меню======================

    //раздуваем наше меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.learners_and_grades_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //назначаем функции меню
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        //кнопка статистики
        menu.findItem(R.id.learners_and_grades_menu_statistics).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // запускаем активность статистики
                Intent intent = new Intent(LearnersAndGradesActivity.this, LearnersGradesStatisticsActivity.class);
                intent.putExtra(LearnersGradesStatisticsActivity.INTENT_SUBJECT_ID, subjectsId[changingSubjectPosition]);
                startActivity(intent);
//                Toast toast = Toast.makeText(
//                        getApplicationContext(),
//                        R.string.learners_and_grades_out_activity_toast_in_develop,
//                        Toast.LENGTH_SHORT
//                );
//                toast.show();
                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }


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
        outLearnersNamesInTable();
        getGradesFromDB();
        db.close();
    }

    @Override
    public void updateAll() {
        //останавливаем поток загрузки данных
        flag = false;
        //обновляем список учеников
        getLearnersFromDB();
        //обновляем таблицу
        outLearnersNamesInTable();
        getGradesFromDB();
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
        // ставим текущее время
        GregorianCalendar currentCalendar = new GregorianCalendar();
        currentCalendar.setTime(new Date());
        //обновляем таблицу
        outLearnersNamesInTable();
        getGradesFromDB();
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
        outLearnersNamesInTable();
        getGradesFromDB();
        db.close();
    }

    //---редактирование оценки ученика---
    @Override
    public void editGrade(int[] grades, int[] indexes) {
        if (indexes.length == 3) {

            //сохраняем оценки в массив
            allGrades[indexes[0]][indexes[1]][indexes[2]].grades = grades;
            //ставим оценки во view
            allGrades[indexes[0]][indexes[1]][indexes[2]].doText();

            //сохраняем оценки в бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            for (int i = 0; i < grades.length; i++) {
                //если оценка новая
                if (allGrades[indexes[0]][indexes[1]][indexes[2]].gradeId[i] == -1) {
                    if (allGrades[indexes[0]][indexes[1]][indexes[2]].grades[i] != 0) {
                        db.createGrade(
                                allGrades[indexes[0]][indexes[1]][indexes[2]].learnerId,
                                allGrades[indexes[0]][indexes[1]][indexes[2]].grades[i],
                                allGrades[indexes[0]][indexes[1]][indexes[2]].subjectId,
                                allGrades[indexes[0]][indexes[1]][indexes[2]].date
                        );
                    }
                } else if (allGrades[indexes[0]][indexes[1]][indexes[2]].grades[i] != 0) {
                    db.editGrade(
                            allGrades[indexes[0]][indexes[1]][indexes[2]].gradeId[i],
                            allGrades[indexes[0]][indexes[1]][indexes[2]].grades[i]
                    );
                } else {
                    db.removeGrade(allGrades[indexes[0]][indexes[1]][indexes[2]].gradeId[i]);
                }
            }

            //разрешаем пользователю менять оценки
            canEditGrades = true;

            Toast toast = Toast.makeText(this, R.string.learners_and_grades_out_activity_toast_grades_saved, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, R.string.learners_and_grades_out_activity_toast_grades_not_saved, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void returnSimpleColorForText(int[] indexes) {
/*
     Process: com.learning.texnar13.teachersprogect, PID: 6047
    java.lang.NullPointerException: Attempt to invoke virtual method 'void android.widget.TextView.setBackgroundColor(int)' on a null object reference
        at com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesActivity.returnSimpleColorForText(LearnersAndGradesActivity.java:256)
        at com.learning.texnar13.teachersprogect.learnersAndGradesOut.EditGradeDialogFragment.onCancel(EditGradeDialogFragment.java:251)
        at android.app.Dialog$ListenersHandler.handleMessage(Dialog.java:1536)
        at android.os.Handler.dispatchMessage(Handler.java:102)
        at android.os.Looper.loop(Looper.java:158)
        at android.app.ActivityThread.main(ActivityThread.java:7237)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1230)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1120)
*/
        allGrades[indexes[0]][indexes[1]][indexes[2]].textView.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
        allGrades[indexes[0]][indexes[1]][indexes[2]].textView.setTextColor(Color.BLACK);
    }

    //разрешаем пользователю изменять оценки
    @Override
    public void allowUserEditGrades() {
        canEditGrades = true;
    }

//===========старт активности===========

    @Override
    protected void onCreate(Bundle savedInstanceState) {

// ---------- подготовка активности ----------

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

//----находим спинер с предметами----
        subjectSpinner = (Spinner) findViewById(R.id.learners_and_grades_activity_subject_spinner);

//----переключение даты----
        //константы
        final String[] monthsNames = getResources().getStringArray(R.array.months_names);
        //изменяющийся календарь
        if (viewCalendar == null) {//если зашли в активность(создали) а не переворачивали экран ставим текущую дату
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
                //outLearnersNamesInTable();
                getGradesFromDB();
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
                //outLearnersNamesInTable();
                getGradesFromDB();
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
        //максимальная оценка
        maxAnswersCount = db.getSettingsMaxGrade(1);
        // цветные оценки
        areTheGradesColored = db.getSettingsAreTheGradesColoredByProfileId(1);

//----название класса в заголовок----
        //название класса
        Cursor classCursor = db.getClasses(classId);
        classCursor.moveToFirst();
        getSupportActionBar().setTitle(R.string.title_activity_learners_and_grades);
        getSupportActionBar().setTitle(
                getSupportActionBar().getTitle() + " " +
                        classCursor.getString(
                                classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)
                        )
        );

        classCursor.close();

//----вывод данных при старте----
        availableLessonsOut(classId, 0);
        getLearnersFromDB();

//        //инициализируем массив
//        allGrades = new NewGradeUnit
//                [learnersId.size()]
//                [viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)]
//                [9];
//        //[ученик][день][урок]
//
//        for (int i = 0; i < allGrades.length; i++) {
//            for (int j = 0; j < allGrades[i].length; j++) {
//                for (int k = 0; k < allGrades[i][j].length; k++) {
//                    allGrades[i][j][k] = new NewGradeUnit();
//                }
//            }
//        }

        outLearnersNamesInTable();
        getGradesFromDB();
        db.close();
    }

//==============================вывод предметов========================================

    void availableLessonsOut(final long classViewId, final int position) {

        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor cursor = db.getSubjectsByClassId(classViewId);
        final int count = cursor.getCount();
        final String[] stringLessons;
        if (count == 0) {
            stringLessons = new String[cursor.getCount() + 1];
        } else {
            stringLessons = new String[cursor.getCount() + 2];
        }
        final String[] stringOnlyLessons = new String[cursor.getCount()];
        subjectsId = new long[cursor.getCount()];
        //Log.i("TeachersApp", "LessonRedactorActivity - " + stringLessons.length);
        for (int i = 0; i < stringLessons.length - 2; i++) {
            cursor.moveToNext();
            subjectsId[i] = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID));
            stringLessons[i] = cursor.getString(cursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
            stringOnlyLessons[i] = stringLessons[i];
        }

        if (count == 0) {
            stringLessons[stringLessons.length - 1] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject);
        } else {
            stringLessons[stringLessons.length - 1] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_remove_subject);
            stringLessons[stringLessons.length - 2] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject);
        }
        cursor.close();
        db.close();

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_dropdown_element_learners_and_grades_subjects,
                stringLessons
        );
        subjectSpinner.setAdapter(adapter);
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Log.w("TeachersApp", "LessonRedactorActivity - availableLessonsOut onItemSelected " + pos);
                if (count != 0 && stringLessons.length - 1 == pos) {
                    Log.w("TeachersApp", "LessonRedactorActivity - remove lesson");
                    //данные передаваемые в диалог
                    Bundle args = new Bundle();
                    args.putStringArray("stringOnlyLessons", stringOnlyLessons);
                    args.putLongArray("lessonsId", subjectsId);
                    args.putInt("position", position);
                    //диалог по удалению
                    RemoveLearnerSubjectDialogFragment removeDialog = new RemoveLearnerSubjectDialogFragment();
                    removeDialog.setArguments(args);
                    removeDialog.show(getFragmentManager(), "removeLessons");

                } else if ((count != 0 && stringLessons.length - 2 == pos) || (count == 0 && stringLessons.length - 1 == pos)) {
                    //диалог создания предмета
                    Log.w("TeachersApp", "LessonRedactorActivity - new lesson");
                    //данные для диалога
                    Bundle args = new Bundle();
                    args.putStringArray("stringLessons", stringLessons);
                    args.putInt("position", position);
                    //диалог по созданию нового предмета
                    SubjectNameLearnersDialogFragment lessonNameDialogFragment = new SubjectNameLearnersDialogFragment();
                    lessonNameDialogFragment.setArguments(args);
                    lessonNameDialogFragment.show(getFragmentManager(), "createSubject");
                } else if (pos != 0) {
                    Log.w("TeachersApp", "LessonRedactorActivity - chosen lesson id = " + subjectsId[pos - 1]);
                    //останавливаем загрузку оценок
                    flag = false;
                    changingSubjectPosition = pos;
                    outLearnersNamesInTable();
                    getGradesFromDB();
                } else {
                    Log.w("TeachersApp", "LessonRedactorActivity - no lesson selected");
                    //останавливаем загрузку оценок
                    flag = false;
                    changingSubjectPosition = 0;
                    outLearnersNamesInTable();
                    getGradesFromDB();
                }
            }
        });
        subjectSpinner.setSelection(position, false);
    }

//---------------------удаление предметов-----------------

    public static class RemoveLearnerSubjectDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            //начинаем строить диалог
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            //layout диалога
            LinearLayout out = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_lesson_redactor_dialog_lesson_name, null);
            builder.setView(out);

            //--LinearLayout в layout файле--
            LinearLayout linearLayout = (LinearLayout) out.findViewById(R.id.create_lesson_dialog_fragment_linear_layout);
            linearLayout.setBackgroundResource(R.color.colorBackGround);
            linearLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
            linearLayout.setLayoutParams(linearLayoutParams);

//--заголовок--
            TextView title = new TextView(getActivity());
            title.setText(R.string.lesson_redactor_activity_dialog_title_remove_subjects);
            title.setTextColor(Color.BLACK);
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            title.setAllCaps(true);
            title.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

            linearLayout.addView(title, titleParams);

//--список выбранных предметов--
            final ArrayList<Integer> mSelectedItems = new ArrayList<>();
            //--------ставим диалогу список в виде view--------
            //список названий
            String[] subjectNames = getArguments().getStringArray("stringOnlyLessons");

            //контейнеры для прокрутки
            ScrollView scrollView = new ScrollView(getActivity());
            linearLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F));
            LinearLayout linear = new LinearLayout(getActivity());
            linear.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(linear);


            for (int i = 0; i < subjectNames.length; i++) {
//--------пункт списка--------
                //контейнер
                LinearLayout item = new LinearLayout(getActivity());
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setGravity(Gravity.LEFT);
                LinearLayout.LayoutParams itemParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (int) (pxFromDp(40) * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))
                        );
                itemParams.setMargins(
                        (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier)))
                );
                linear.addView(item, itemParams);

                //чекбокс
                final CheckBox checkBox = new CheckBox(getActivity());
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                checkBox.setChecked(false);
                item.addView(checkBox);

                //текст в нем
                TextView text = new TextView(getActivity());
                text.setText(subjectNames[i]);
                text.setTextColor(Color.BLACK);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                item.addView(text);

                //нажатие на пункт списка
                final int number = i;
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkBox.isChecked()) {
                            checkBox.setChecked(false);
                            if (mSelectedItems.contains(number)) {
                                // Else, if the item is already in the array, remove it
                                mSelectedItems.remove(Integer.valueOf(number));
                            }
                        } else {
                            // If the user checked the item, add it to the selected items
                            checkBox.setChecked(true);
                            mSelectedItems.add(number);
                        }
                    }
                });
            }

//--кнопки согласия/отмены--
            //контейнер для них
            LinearLayout container = new LinearLayout(getActivity());
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setGravity(Gravity.CENTER);

            //кнопка отмены
            Button neutralButton = new Button(getActivity());
            neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
            neutralButton.setText(R.string.lesson_redactor_activity_dialog_button_cancel);
            neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            neutralButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size)
            );
            neutralButtonParams.weight = 1;
            neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

            //кнопка согласия
            Button positiveButton = new Button(getActivity());
            positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
            positiveButton.setText(R.string.lesson_redactor_activity_dialog_button_remove);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            positiveButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size)
            );
            positiveButtonParams.weight = 1;
            positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));


            //кнопки в контейнер
            container.addView(neutralButton, neutralButtonParams);
            container.addView(positiveButton, positiveButtonParams);

            //контейнер в диалог
            linearLayout.addView(container);


            //при нажатии...
            //согласие
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long[] lessonsId = getArguments().getLongArray("lessonsId");
                    ArrayList<Long> deleteList = new ArrayList<>(mSelectedItems.size());
                    for (int itemPoz : mSelectedItems) {
                        deleteList.add(lessonsId[itemPoz]);
                    }
                    //возвращаем все в активность
                    ((SubjectRemoveLearnersDialogInterface) getActivity())
                            .RemoveSubjectDialogMethod(
                                    0,
                                    0,
                                    deleteList
                            );

                    dismiss();
                }
            });

            //отмена
            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //возвращаем все в активность
                    ((SubjectRemoveLearnersDialogInterface) getActivity())
                            .RemoveSubjectDialogMethod(
                                    1,
                                    getArguments().getInt("position"),
                                    new ArrayList<Long>()
                            );
                    dismiss();
                }
            });
            return builder.create();

        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            ((SubjectRemoveLearnersDialogInterface) getActivity())
                    .RemoveSubjectDialogMethod(
                            1,
                            getArguments().getInt("position"),
                            new ArrayList<Long>()
                    );
        }

        //---------форматы----------

        private float pxFromDp(float px) {
            return px * getActivity().getResources().getDisplayMetrics().density;
        }
    }

//---------------------интерфейс удаления предметов-----------------

    @Override
    public void RemoveSubjectDialogMethod(int code, int position, ArrayList<Long> deleteList) {
        if (code == 0) {
            //останавливаем загрузку оценок
            flag = false;
            //удаляем
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            db.deleteSubjects(deleteList);
            db.close();
            //выводим
            availableLessonsOut(classId, position);
        } else {
            availableLessonsOut(classId, position);
        }
    }

//---------------------диалог по созданию предметов-----------------

    public static class SubjectNameLearnersDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //начинаем строить диалог
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            //layout диалога
            LinearLayout out = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_lesson_redactor_dialog_lesson_name, null);
            builder.setView(out);

            //--LinearLayout в layout файле--
            LinearLayout linearLayout = (LinearLayout) out.findViewById(R.id.create_lesson_dialog_fragment_linear_layout);
            linearLayout.setBackgroundResource(R.color.colorBackGround);
            linearLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
            linearLayout.setLayoutParams(linearLayoutParams);

//--заголовок--
            TextView title = new TextView(getActivity());
            title.setText(R.string.lesson_redactor_activity_dialog_title_add_subject);
            title.setTextColor(Color.BLACK);
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            title.setAllCaps(true);
            title.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

            linearLayout.addView(title, titleParams);


//--текстовое поле имени--
            final EditText editName = new EditText(getActivity());
            editName.setTextColor(Color.BLACK);
            editName.setHint(R.string.lesson_redactor_activity_dialog_hint_subject_name);
            editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            editName.setInputType(InputType.TYPE_CLASS_TEXT);
            editName.setHintTextColor(Color.GRAY);

            LinearLayout editNameContainer = new LinearLayout(getActivity());
            LinearLayout.LayoutParams editNameContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            editNameContainerParams.setMargins((int) pxFromDp(25), 0, (int) pxFromDp(25), 0);
            //добавляем текстовое поле
            editNameContainer.addView(
                    editName,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    ));
            linearLayout.addView(editNameContainer, editNameContainerParams);

//--кнопки согласия/отмены--
            //контейнер для них
            LinearLayout container = new LinearLayout(getActivity());
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setGravity(Gravity.CENTER);

            //кнопка отмены
            Button neutralButton = new Button(getActivity());
            neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
            neutralButton.setText(R.string.lesson_redactor_activity_dialog_button_cancel);
            neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            neutralButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size)
            );
            neutralButtonParams.weight = 1;
            neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

            //кнопка согласия
            Button positiveButton = new Button(getActivity());
            positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
            positiveButton.setText(R.string.lesson_redactor_activity_dialog_button_add);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            positiveButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size)
            );
            positiveButtonParams.weight = 1;
            positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));


            //кнопки в контейнер
            container.addView(neutralButton, neutralButtonParams);
            container.addView(positiveButton, positiveButtonParams);

            //контейнер в диалог
            linearLayout.addView(container);


            //при нажатии...
            //согласие
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int poz;

                    poz = getArguments().getStringArray("stringLessons").length - 2;
                    ((SubjectNameLearnersDialogInterface) getActivity())
                            .lessonNameDialogMethod(
                                    1,
                                    poz,
                                    editName.getText().toString()
                            );

                    dismiss();
                }
            });

            //отмена
            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int poz;
                    //если предметов нет то диалог появляется снова и снова
                    if (getArguments().getStringArray("stringLessons").length == 2) {
                        poz = getArguments().getStringArray("stringLessons").length - 2;
                    } else
                        //иначе переводит на последний
                        poz = getArguments().getStringArray("stringLessons").length - 3;
                    ((SubjectNameLearnersDialogInterface) getActivity())
                            .lessonNameDialogMethod(
                                    0,
                                    poz,
                                    ""
                            );
                    dismiss();
                }
            });

            return builder.create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            int poz;
            if (getArguments().getStringArray("stringLessons").length == 2) {
                poz = getArguments().getStringArray("stringLessons").length - 2;
            } else
                //иначе переводит на последний
                poz = getArguments().getStringArray("stringLessons").length - 3;
            ((SubjectNameLearnersDialogInterface) getActivity())
                    .lessonNameDialogMethod(
                            0,
                            poz,
                            ""
                    );
            super.onCancel(dialog);
        }

        //---------форматы----------

        private float pxFromDp(float px) {
            return px * getActivity().getResources().getDisplayMetrics().density;
        }
    }

//--------обратная связь от диалога-------

    @Override
    public void lessonNameDialogMethod(int code, int position, String classNameText) {
        if (code == 1) {
            (new DataBaseOpenHelper(this)).createSubject(classNameText, classId);
            availableLessonsOut(classId, position);
        } else {
            availableLessonsOut(classId, position);
        }

    }


//===========получаем учеников из базы===========

    void getLearnersFromDB() {
        Log.i("TeachersApp", "LearnersAndGradesActivity - getLearnersFromDB");
        //запрещаем пользователю редактировать учеников
        canEditLearners = false;
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
    void outLearnersNamesInTable() {
        Log.i("TeachersApp", "LearnersAndGradesActivity - updateTable");
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
        headName.setText("  " + getResources().getString(R.string.learners_and_grades_out_activity_title_table_names) + "  ");
        headName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
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
            learnerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
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
                    if (canEditLearners) {
                        canEditLearners = false;
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
                    }
                    return true;
                }
            });

            //добавляем строку в таблицу
            learnersNamesTable.addView(learner, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        }
        //разрешаем редактировать учеников, когда все вывели
        canEditLearners = true;
    }

//===========получаем оценки из базы===========

    void getGradesFromDB() {
        Log.i("TeachersApp", "LearnersAndGradesActivity - getGradesFromDB");

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
        gradesRoom.addView(forwardScreen, RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);


//----возобновляем, начинаем подгрузку данных----
        flag = true;
//----запрещаем изменение оценок----
        canEditGrades = false;

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
                Log.i("TeachersApp", "StartGradesLoadThread");
                //для перевода даты в пустые оценки
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //-- заполняем бд
                //чистим массив от предыдущих значений

                allGrades = new NewGradeUnit
                        [learnersId.size()]
                        [viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)]
                        [9];
                //[ученик][день][урок][оценка]
                // и инициализируем его()
                for (int g = 0; g < allGrades.length; g++) {
                    for (int j = 0; j < allGrades[g].length; j++) {
                        for (int k = 0; k < allGrades[g][j].length; k++) {
                            allGrades[g][j][k] = new NewGradeUnit();
                        }
                    }
                }

                //изменяющися строки для запросов
                String startQestionString;
                String endQestionString;

                if (subjectsId.length != 0) {
                    DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                    //получаем время уроков из бд
                    int[][] timeOfLessons = db.getSettingsTime(1);

                    //обновляем массивы свежими данными
                    // по ученикам
                    for (int i = 0; i < learnersId.size(); i++) {
                        //по дням
                        for (int j = 0; j < viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); j++) {
                            //сначала проверяем весь день целиком
                            //--время--

                            startQestionString = "" + viewCalendar.get(Calendar.YEAR) + "-" + getTwoSymbols(viewCalendar.get(Calendar.MONTH) + 1) + "-" + getTwoSymbols(j + 1) + " 00:00:00";
                            endQestionString = "" + viewCalendar.get(Calendar.YEAR) + "-" + getTwoSymbols(viewCalendar.get(Calendar.MONTH) + 1) + "-" + getTwoSymbols(j + 1) + " 23:59:00";


                            Cursor tDay = db.getGradesByLearnerIdSubjectAndTimePeriod(
                                    learnersId.get(i),
                                    subjectsId[changingSubjectPosition],
                                    startQestionString,
                                    endQestionString
                            );//может посчитать их здесь, а потом если вывели все оценки то остальные выводим уже с прочерками без проверки

                            if (tDay.getCount() != 0) {

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
                                    // сторки для запроса
                                    startQestionString = "" + viewCalendar.get(Calendar.YEAR) + "-" + getTwoSymbols(viewCalendar.get(Calendar.MONTH) + 1) + "-" + getTwoSymbols(j + 1) + " " + getTwoSymbols(timeOfLessons[k][0]) + ":" + getTwoSymbols(timeOfLessons[k][1]) + ":00";
                                    endQestionString = "" + viewCalendar.get(Calendar.YEAR) + "-" + getTwoSymbols(viewCalendar.get(Calendar.MONTH) + 1) + "-" + getTwoSymbols(j + 1) + " " + getTwoSymbols(timeOfLessons[k][2]) + ":" + getTwoSymbols(timeOfLessons[k][3]) + ":00";

                                    //получаем оценки по времени и предмету
                                    Cursor gradesLessonCursor = db.getGradesByLearnerIdSubjectAndTimePeriod(
                                            learnersId.get(i),//todo !!!!!!!!!!!! здесь все еще есть ошибка !!!!!!!!!!!!!!! здесь ошибка java.lang.ArrayIndexOutOfBoundsException
                                            subjectsId[changingSubjectPosition],
                                            startQestionString,
                                            endQestionString
                                    );
                                    //по оценкам

                                    //выбираем массивы оценок из курсора
                                    int grade[] = new int[3];
                                    long gradesId[] = new long[3];
                                    String timeS = "";
                                    for (int l = 0; l < 3; l++) {
                                        if (gradesLessonCursor.moveToPosition(l)) {
                                            //есть оценка
                                            grade[l] = gradesLessonCursor.getInt(gradesLessonCursor.getColumnIndex(
                                                    SchoolContract.TableLearnersGrades.COLUMN_GRADE
                                            ));
                                            gradesId[l] = gradesLessonCursor.getLong(gradesLessonCursor.getColumnIndex(
                                                    SchoolContract.TableLearnersGrades.KEY_GRADE_ID
                                            ));
                                            timeS = gradesLessonCursor.getString(gradesLessonCursor.getColumnIndex(
                                                    SchoolContract.TableLearnersGrades.COLUMN_TIME_STAMP
                                            ));
                                        } else {
                                            //нет оценки
                                            grade[l] = 0;
                                            gradesId[l] = -1;
                                        }
                                    }

                                    allGrades[i][j][k] = new NewGradeUnit(learnersId.get(i),
                                            gradesId,
                                            grade,
                                            subjectsId[changingSubjectPosition],
                                            timeS
                                    );

                                    gradesLessonCursor.close();
                                }
                            } else {
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
//                                    //начало урока
//                                    outHelpStartCalendar.set(Calendar.HOUR_OF_DAY,
//                                            timeOfLessons[k][0]
//                                    );
//                                    outHelpStartCalendar.set(Calendar.MINUTE,
//                                            timeOfLessons[k][1]
//                                    );
                                    startQestionString = "" + viewCalendar.get(Calendar.YEAR) + "-" + getTwoSymbols(viewCalendar.get(Calendar.MONTH) + 1) + "-" + getTwoSymbols(j + 1) + " " + getTwoSymbols(timeOfLessons[k][0]) + ":" + getTwoSymbols(timeOfLessons[k][1]) + ":00";

                                    //по оценкам
                                    for (int l = 0; l < 3; l++) {
                                        //нет оценки
                                        allGrades[i][j][k] = new NewGradeUnit(
                                                learnersId.get(i),
                                                new long[]{-1, -1, -1},
                                                new int[]{0, 0, 0},
                                                subjectsId[changingSubjectPosition],
                                                startQestionString
                                        );

                                    }
                                }
                            }
                            tDay.close();
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
        Log.i("TeachersApp", "LearnersAndGradesActivity - outGradesInTable");
        // в таблице дни раскрашиваются в шахматном порядке
//если первый столбец в дне то выводим иначе если нет оценок, то не выводим

        //чистка
        learnersGradesTable.removeAllViews();

        //дни
        TableRow headGrades = new TableRow(this);
        //строку с шапкой в таблицу
        learnersGradesTable.addView(headGrades, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        //строки с учениками
        TableRow[] learnerTableRows = new TableRow[allGrades.length];
        for (int i = 0; i < allGrades.length; i++) {
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
                for (int k = 0; k < allGrades.length; k++) {
                    //по оценкам за этот день
                    for (int l = 0; l < 3; l++) {
                        //[ученик][день][урок][оценка]
                        if (allGrades[k][i][j].grades[l]//todo здесь ошибка java.lang.NullPointerException
                                != 0) {
                            flag = true;
                            break out;
                        }
                    }
                }
                //выводим новый столбец урока (может быть несколько за день)
                if (j == 0 || flag) {
//---шапка
                    //рамка
                    LinearLayout headDateOut = new LinearLayout(this);
                    headDateOut.setBackgroundColor(getResources().getColor(R.color.colorBackGroundDark));
                    //текст заголовка ученика
                    TextView headDate = new TextView(this);

                    headDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
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
                    for (int k = 0; k < allGrades.length; k++) {
                        //рамка
                        LinearLayout dateOut = new LinearLayout(this);
                        dateOut.setBackgroundColor(getResources().getColor(R.color.colorBackGroundDark));
                        //текст
                        final TextView learnerGrade = new TextView(this);
                        learnerGrade.setTextColor(Color.BLACK);
                        learnerGrade.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                        learnerGrade.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
                        learnerGrade.setGravity(Gravity.CENTER);
                        //добавляем ссылку на view к оценкам
                        allGrades[k][i][j].textView = learnerGrade;

                        //ставим текст
                        allGrades[k][i][j].doText();

                        //параметры текста
                        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        //отступы рамки
                        textParams.setMargins((int) pxFromDp(1), 0, 0, (int) pxFromDp(1));
                        //текст в рамку
                        dateOut.addView(learnerGrade, textParams);
                        //добавляем всё в строку
                        learnerTableRows[k].addView(dateOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);

                        final int[] arrayGrade = new int[allGrades[k][i][j].grades.length];
                        final int finalK = k;
                        final int finalI = i;
                        final int finalJ = j;


                        for (int l = 0; l < arrayGrade.length; l++) {
                            arrayGrade[l] = allGrades[k][i][j].grades[l];
                        }
                        //при нажатии на оценку
                        dateOut.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //останавливаем подгрузку данных, чтобы не мешали
                                //flag = false;//TO=DO надо остановить подгрузку данных, чтобы при переоценивании новых не получить ошибку, надо придумать как это сделать, может через метод?
                                if (canEditGrades) {
                                    // --- красим ---
                                    learnerGrade.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
                                    learnerGrade.setTextColor(getResources().getColor(R.color.colorBackGround));
                                    // --- чтобы не было нескольких вызовов одновременно ---
                                    canEditGrades = false;
                                    // --- вызываем диалог по ее изменению ---
                                    EditGradeDialogFragment editGrade = new EditGradeDialogFragment();
                                    //параметры
                                    Bundle bundle = new Bundle();
                                    // оценки
                                    bundle.putIntArray(EditGradeDialogFragment.GRADES, arrayGrade);
                                    // номер ученика, номер дня, номер урока
                                    bundle.putIntArray(EditGradeDialogFragment.INDEXES, new int[]{finalK, finalI, finalJ});
                                    //запускаем
                                    editGrade.setArguments(bundle);
                                    editGrade.show(getFragmentManager(), "EditGrade");
                                }
                            }
                        });


                    }
                }
            }

        }
//----разрешаем изменение оценок----
        canEditGrades = true;
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

// ============================= Класс где храним оценку ==========================================

    class NewGradeUnit {//оценки на уроке
        long learnerId = -1;//id ученика
        long subjectId = -1;// предмет
        String date;//время оценки(урока)
        long[] gradeId = {-1, -1, -1};
        int[] grades = {0, 0, 0};

        TextView textView;//поле в таблице

        NewGradeUnit(long learnerId, long[] gradesId, int[] grades, long subjectId, String date) {
            this.learnerId = learnerId;
            this.gradeId = gradesId;
            this.grades = grades;
            this.subjectId = subjectId;
            this.date = date;
        }

        NewGradeUnit() {
        }

        void doText() {
            SpannableStringBuilder s = new SpannableStringBuilder();
            // серый для прочерков
            ForegroundColorSpan style = new ForegroundColorSpan(
                    getResources().getColor(R.color.gradeColorGray)
            );
            for (int i = 0; i < grades.length; i++) {
                switch (grades[i]) {
                    case -1://ошибка
                        break;
                    case 0://нет оценки
                        break;
                    case -2://н
                        if (s.toString().equals("")) {
                            s.append(" ");
                        }
                        s.append(getResources()
                                .getString(R.string.learners_and_grades_out_activity_title_grade_n));
                        s.append(" ");
                        break;
                    default: {//оценка
                        // выбираем откуда начинаем красить
                        int start = s.length();
                        // ставим оценку
                        if (s.toString().equals(""))
                            s.append(" ");
                        s.append("" + grades[i]);
                        s.append(" ");
                        // ---- выбираем цвет оценки ----
                        if(areTheGradesColored){// выбраны ли цветные оценки
                            //5
                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 100) {
                                style = new ForegroundColorSpan(
                                        getResources().getColor(R.color.grade5Color)
                                );
                            }
                            //4
                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 80) {
                                style = new ForegroundColorSpan(
                                        getResources().getColor(R.color.grade4Color)
                                );
                            }
                            //3
                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 60) {
                                style = new ForegroundColorSpan(
                                        getResources().getColor(R.color.grade3Color)
                                );
                            }
                            //2
                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 41) {
                                style = new ForegroundColorSpan(
                                        getResources().getColor(R.color.grade2Color)
                                );
                            }
                            //1
                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 20) {
                                style = new ForegroundColorSpan(
                                        getResources().getColor(R.color.grade1Color)
                                );
                            }
                            s.setSpan(style, start, s.length() - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                    }
                    break;
                }
            }
            if (s.length() == 0) {
                s.append(" - ");
                s.setSpan(style, 0, s.length() - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            }
            textView.setText(s);
        }

    }

    // -- метод трансформации числа в текст с двумя позициями --
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

//    // -- метод трансформации числа в текст с четырьмя датами --
//    String getFourSymbols(int number) {
//        if (number < 10 && number >= 0) {
//            return "000" + number;
//        } else {
//            return "" + number;
//        }
//    }

}

interface SubjectNameLearnersDialogInterface {//обратная связь от диалога

    void lessonNameDialogMethod(int code, int position, String classNameText);
}

interface SubjectRemoveLearnersDialogInterface {//обратная связь от диалога

    void RemoveSubjectDialogMethod(int code, int position, ArrayList<Long> deleteList);
}

