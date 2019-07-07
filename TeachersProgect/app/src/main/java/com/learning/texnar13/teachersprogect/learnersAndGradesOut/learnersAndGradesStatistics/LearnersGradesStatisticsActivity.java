package com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;

public class LearnersGradesStatisticsActivity extends AppCompatActivity implements CreateStatisticDialogInterface, RemoveStatisticDialogInterface, EditStatisticDialogInterface {
    public static final String TAG = "TeachersApp";

    // трансферные переменные
    public static final String INTENT_SUBJECT_ID = "subjectID";

    // ---------- layout-ы -----------
    // --- время ---
    // спиннер с названиями периодов
    private Spinner periodsNamesSpinner;
    // текстовые поля
    private EditText startDayEditText;
    private EditText startMonthEditText;
    private EditText startYearEditText;
    private EditText endDayEditText;
    private EditText endMonthEditText;
    private EditText endYearEditText;
    // --- таблица ---
    // колонка со списком учеников
    private LinearLayout learnersColumn;
    // колонка со списком оценок
    private LinearLayout gradesColumn;
    // колонка со списком оценок
    private LinearLayout absentsColumn;

    // ------ переменные созданные в процессе работы ------
    private long subjectId = -1;
    private long learnersClassId = -1;
    private long[] learnersId;

    // параметры профилей
    private long[] profilesId;
    private String[] profilesNames;
    private String[] outProfilesStrings;
    private int[][] dates;
    private int chosenProfileNumber;
    private int maxAnswersCount;
    private boolean areTheGradesColored = false;

    // переменная разрешающая проверять поля дат и сохранять даты
    boolean isTextCheckRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ставим содержимое экрана
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learners_grades_statistics);
        // кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // заголовок активности
        setTitle(R.string.title_activity_learners_and_grades_statistics);

        // ---------- layout-ы -----------
        // --- время ---
        // спиннер с названиями периодов
        periodsNamesSpinner = (Spinner) findViewById(R.id.learners_grades_statistics_spinner);
        // текстовые поля
        startDayEditText = (EditText) findViewById(R.id.learners_grades_statistics_begin_day);
        startMonthEditText = (EditText) findViewById(R.id.learners_grades_statistics_begin_month);
        startYearEditText = (EditText) findViewById(R.id.learners_grades_statistics_begin_year);
        endDayEditText = (EditText) findViewById(R.id.learners_grades_statistics_end_day);
        endMonthEditText = (EditText) findViewById(R.id.learners_grades_statistics_end_month);
        endYearEditText = (EditText) findViewById(R.id.learners_grades_statistics_end_year);
        // --- таблица ---
        // колонка со списком учеников
        learnersColumn = (LinearLayout) findViewById(R.id.learners_grades_learners_table);
        // колонка со списком оценок
        gradesColumn = (LinearLayout) findViewById(R.id.learners_grades_statistics_grade_column);
        // колонка со списком оценок
        absentsColumn = (LinearLayout) findViewById(R.id.learners_grades_statistics_absent_column);


        // получаем id предмета
        subjectId = getIntent().getLongExtra(INTENT_SUBJECT_ID, -1);
        if (subjectId == -1) {
            //id не передан
            finish();
        }

        // ------------------------ получаем данные из бд ------------------------
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // максимальная оценка
        maxAnswersCount = db.getSettingsMaxGrade(1);
        // цветные оценки
        areTheGradesColored = db.getSettingsAreTheGradesColoredByProfileId(1);
        // курсор предмета
        Cursor subjectCursor = db.getSubjectById(subjectId);
        subjectCursor.moveToNext();
        // id класса
        learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
        subjectCursor.close();
        db.close();

        // ------------------------ вывод данных ------------------------
        // выводим учеников
        outLearners();
        // выводим названия и, если есть, даты, по ним оценки
        outSpinner(-1);

        // ----- слушатели изменения текста, при изменении поля запускают проверку, сохранение и вывод -----
        startDayEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTextCheckRun)
                    if (isDateGood(startDayEditText, startMonthEditText, startYearEditText, endDayEditText, endMonthEditText, endYearEditText)) {
                        // помещаем в массив
                        Log.e("TeachersApp", "startDayEditText.afterTextChanged: saveDate");
                        dates[chosenProfileNumber][0] = Integer.parseInt(startYearEditText.getText().toString());
                        dates[chosenProfileNumber][1] = Integer.parseInt(startMonthEditText.getText().toString());
                        dates[chosenProfileNumber][2] = Integer.parseInt(startDayEditText.getText().toString());
                        dates[chosenProfileNumber][3] = Integer.parseInt(endYearEditText.getText().toString());
                        dates[chosenProfileNumber][4] = Integer.parseInt(endMonthEditText.getText().toString());
                        dates[chosenProfileNumber][5] = Integer.parseInt(endDayEditText.getText().toString());
                        // сохраняем в бд
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.setStatisticTime(
                                profilesId[chosenProfileNumber],
                                "" + dates[chosenProfileNumber][0] + "-" + getTwoSymbols(dates[chosenProfileNumber][1]) + "-" + getTwoSymbols(dates[chosenProfileNumber][2]) + " 00:00:00",
                                "" + dates[chosenProfileNumber][3] + "-" + getTwoSymbols(dates[chosenProfileNumber][4]) + "-" + getTwoSymbols(dates[chosenProfileNumber][5]) + " 23:59:59"
                        );
                        db.close();
                        // выводим оценки
                        outGrades();
                    }
            }
        });
        startMonthEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTextCheckRun)
                    if (isDateGood(startDayEditText, startMonthEditText, startYearEditText, endDayEditText, endMonthEditText, endYearEditText)) {
                        // помещаем в массив
                        Log.e("TeachersApp", "startMonthEditText.afterTextChanged: saveDate");
                        dates[chosenProfileNumber][0] = Integer.parseInt(startYearEditText.getText().toString());
                        dates[chosenProfileNumber][1] = Integer.parseInt(startMonthEditText.getText().toString());
                        dates[chosenProfileNumber][2] = Integer.parseInt(startDayEditText.getText().toString());
                        dates[chosenProfileNumber][3] = Integer.parseInt(endYearEditText.getText().toString());
                        dates[chosenProfileNumber][4] = Integer.parseInt(endMonthEditText.getText().toString());
                        dates[chosenProfileNumber][5] = Integer.parseInt(endDayEditText.getText().toString());
                        // сохраняем в бд
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.setStatisticTime(
                                profilesId[chosenProfileNumber],
                                "" + dates[chosenProfileNumber][0] + "-" + getTwoSymbols(dates[chosenProfileNumber][1]) + "-" + getTwoSymbols(dates[chosenProfileNumber][2]) + " 00:00:00",
                                "" + dates[chosenProfileNumber][3] + "-" + getTwoSymbols(dates[chosenProfileNumber][4]) + "-" + getTwoSymbols(dates[chosenProfileNumber][5]) + " 23:59:59"
                        );
                        db.close();
                        // выводим оценки
                        outGrades();
                    }
            }
        });
        startYearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTextCheckRun)
                    if (isDateGood(startDayEditText, startMonthEditText, startYearEditText, endDayEditText, endMonthEditText, endYearEditText)) {
                        // помещаем в массив
                        Log.e("TeachersApp", "startYearEditText.afterTextChanged: saveDate");
                        dates[chosenProfileNumber][0] = Integer.parseInt(startYearEditText.getText().toString());
                        dates[chosenProfileNumber][1] = Integer.parseInt(startMonthEditText.getText().toString());
                        dates[chosenProfileNumber][2] = Integer.parseInt(startDayEditText.getText().toString());
                        dates[chosenProfileNumber][3] = Integer.parseInt(endYearEditText.getText().toString());
                        dates[chosenProfileNumber][4] = Integer.parseInt(endMonthEditText.getText().toString());
                        dates[chosenProfileNumber][5] = Integer.parseInt(endDayEditText.getText().toString());
                        // сохраняем в бд
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.setStatisticTime(
                                profilesId[chosenProfileNumber],
                                "" + dates[chosenProfileNumber][0] + "-" + getTwoSymbols(dates[chosenProfileNumber][1]) + "-" + getTwoSymbols(dates[chosenProfileNumber][2]) + " 00:00:00",
                                "" + dates[chosenProfileNumber][3] + "-" + getTwoSymbols(dates[chosenProfileNumber][4]) + "-" + getTwoSymbols(dates[chosenProfileNumber][5]) + " 23:59:59"
                        );
                        db.close();
                        // выводим оценки
                        outGrades();
                    }
            }
        });
        endDayEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTextCheckRun)
                    if (isDateGood(startDayEditText, startMonthEditText, startYearEditText, endDayEditText, endMonthEditText, endYearEditText)) {
                        // помещаем в массив
                        Log.e("TeachersApp", "endDayEditText.afterTextChanged: saveDate");
                        dates[chosenProfileNumber][0] = Integer.parseInt(startYearEditText.getText().toString());
                        dates[chosenProfileNumber][1] = Integer.parseInt(startMonthEditText.getText().toString());
                        dates[chosenProfileNumber][2] = Integer.parseInt(startDayEditText.getText().toString());
                        dates[chosenProfileNumber][3] = Integer.parseInt(endYearEditText.getText().toString());
                        dates[chosenProfileNumber][4] = Integer.parseInt(endMonthEditText.getText().toString());
                        dates[chosenProfileNumber][5] = Integer.parseInt(endDayEditText.getText().toString());
                        // сохраняем в бд
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.setStatisticTime(
                                profilesId[chosenProfileNumber],
                                "" + dates[chosenProfileNumber][0] + "-" + getTwoSymbols(dates[chosenProfileNumber][1]) + "-" + getTwoSymbols(dates[chosenProfileNumber][2]) + " 00:00:00",
                                "" + dates[chosenProfileNumber][3] + "-" + getTwoSymbols(dates[chosenProfileNumber][4]) + "-" + getTwoSymbols(dates[chosenProfileNumber][5]) + " 23:59:59"
                        );
                        db.close();
                        // выводим оценки
                        outGrades();
                    }
            }
        });
        endMonthEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTextCheckRun)
                    if (isDateGood(startDayEditText, startMonthEditText, startYearEditText, endDayEditText, endMonthEditText, endYearEditText)) {
                        // помещаем в массив
                        Log.e("TeachersApp", "endMonthEditText.afterTextChanged: saveDate");
                        dates[chosenProfileNumber][0] = Integer.parseInt(startYearEditText.getText().toString());
                        dates[chosenProfileNumber][1] = Integer.parseInt(startMonthEditText.getText().toString());
                        dates[chosenProfileNumber][2] = Integer.parseInt(startDayEditText.getText().toString());
                        dates[chosenProfileNumber][3] = Integer.parseInt(endYearEditText.getText().toString());
                        dates[chosenProfileNumber][4] = Integer.parseInt(endMonthEditText.getText().toString());
                        dates[chosenProfileNumber][5] = Integer.parseInt(endDayEditText.getText().toString());
                        // сохраняем в бд
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.setStatisticTime(
                                profilesId[chosenProfileNumber],
                                "" + dates[chosenProfileNumber][0] + "-" + getTwoSymbols(dates[chosenProfileNumber][1]) + "-" + getTwoSymbols(dates[chosenProfileNumber][2]) + " 00:00:00",
                                "" + dates[chosenProfileNumber][3] + "-" + getTwoSymbols(dates[chosenProfileNumber][4]) + "-" + getTwoSymbols(dates[chosenProfileNumber][5]) + " 23:59:59"
                        );
                        db.close();
                        // выводим оценки
                        outGrades();
                    }
            }
        });
        endYearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTextCheckRun)
                    if (isDateGood(startDayEditText, startMonthEditText, startYearEditText, endDayEditText, endMonthEditText, endYearEditText)) {
                        // помещаем в массив
                        Log.e("TeachersApp", "endYearEditText.afterTextChanged: saveDate");
                        dates[chosenProfileNumber][0] = Integer.parseInt(startYearEditText.getText().toString());
                        dates[chosenProfileNumber][1] = Integer.parseInt(startMonthEditText.getText().toString());
                        dates[chosenProfileNumber][2] = Integer.parseInt(startDayEditText.getText().toString());
                        dates[chosenProfileNumber][3] = Integer.parseInt(endYearEditText.getText().toString());
                        dates[chosenProfileNumber][4] = Integer.parseInt(endMonthEditText.getText().toString());
                        dates[chosenProfileNumber][5] = Integer.parseInt(endDayEditText.getText().toString());
                        // сохраняем в бд
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.setStatisticTime(
                                profilesId[chosenProfileNumber],
                                "" + dates[chosenProfileNumber][0] + "-" + getTwoSymbols(dates[chosenProfileNumber][1]) + "-" + getTwoSymbols(dates[chosenProfileNumber][2]) + " 00:00:00",
                                "" + dates[chosenProfileNumber][3] + "-" + getTwoSymbols(dates[chosenProfileNumber][4]) + "-" + getTwoSymbols(dates[chosenProfileNumber][5]) + " 23:59:59"
                        );
                        db.close();
                        // выводим оценки
                        outGrades();
                    }
            }
        });


    }

    // ----------- вывод данных в спиннер и поля времени -----------

    void outSpinner(long getId) {// -1 вывести первый элемент
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // получаем профили
        Cursor profiles = db.getAllStatisticsProfiles();
        // создам массив с id
        profilesId = new long[profiles.getCount()];
        // создам массив с названиями и пунктами
        outProfilesStrings = new String[profiles.getCount() + 2];
        // создам массив с названиями
        profilesNames = new String[profiles.getCount()];
        // создаем массив с датами
        dates = new int[profiles.getCount()][6];
        // по профилям
        for (int i = 0; i < profiles.getCount(); i++) {
            profiles.moveToPosition(i);
            // достаем данные
            // id
            profilesId[i] = profiles.getLong(profiles.getColumnIndex(SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID));
            // имя
            profilesNames[i] = profiles.getString(profiles.getColumnIndex(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME));
            outProfilesStrings[i] = profiles.getString(profiles.getColumnIndex(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME));
            // дата начала
            String startDate = profiles.getString(profiles.getColumnIndex(SchoolContract.TableStatisticsProfiles.COLUMN_START_PERIOD_TIME));
            // дата конца
            String endDate = profiles.getString(profiles.getColumnIndex(SchoolContract.TableStatisticsProfiles.COLUMN_END_PERIOD_TIME));
            dates[i][0] = Integer.parseInt(startDate.substring(0, 4));
            dates[i][1] = Integer.parseInt(startDate.substring(5, 7));
            dates[i][2] = Integer.parseInt(startDate.substring(8, 10));
            dates[i][3] = Integer.parseInt(endDate.substring(0, 4));
            dates[i][4] = Integer.parseInt(endDate.substring(5, 7));
            dates[i][5] = Integer.parseInt(endDate.substring(8, 10));
        }

        // ------- вывод данных в спиннер -------
        // -- дополнительные пункты --
        outProfilesStrings[outProfilesStrings.length - 2] = getResources().getString(R.string.learners_and_grades_statistics_activity_spinner_text_add);
        outProfilesStrings[outProfilesStrings.length - 1] = getResources().getString(R.string.learners_and_grades_statistics_activity_spinner_text_remove);
        // -- передаем строки --
        ArrayAdapter<String> namesSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_element_learners_and_grades_subjects, outProfilesStrings);
        periodsNamesSpinner.setAdapter(namesSpinnerAdapter);

        // ------- при выборе пункта спиннера -------
        // ----- выбор -----
        periodsNamesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == outProfilesStrings.length - 2) {
                    // -- показываем диалог по созданию --
                    CreatePeriodDialogFragment createPeriodDialogFragment = new CreatePeriodDialogFragment();
                    createPeriodDialogFragment.show(getFragmentManager(), "createPeriodDialogFragment");
                } else if (position == outProfilesStrings.length - 1) {
                    // -- показываем диалог по удалению --
                    RemovePeriodDialogFragment removePeriodDialogFragment = new RemovePeriodDialogFragment();
                    // параметры
                    Bundle bundle = new Bundle();
                    bundle.putLongArray(RemovePeriodDialogFragment.PROFILES_ID_INTENT, profilesId);
                    bundle.putStringArray(RemovePeriodDialogFragment.PROFILES_NAMES_INTENT, profilesNames);
                    removePeriodDialogFragment.setArguments(bundle);
                    removePeriodDialogFragment.show(getFragmentManager(), "removePeriodDialogFragment");
                } else {
                    // -- выбран период --
                    chosenProfileNumber = position;
                    // выводим даты
                    outDates();
                    // выводим оценки
                    outGrades();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // по id выводим пункт
        if (getId != -1)
            for (int i = 0; i < profilesId.length; i++) {

                if (profilesId[i] == getId) {
                    periodsNamesSpinner.setSelection(i, false);
                }
            }

        // ----- долгое нажатие -----
        periodsNamesSpinner.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "periodsNamesSpinner.onItemLongClick");
                if (position < outProfilesStrings.length - 2) {// только профили
                    // -- показываем диалог по изменению --
                    EditPeriodDialogFragment editPeriodDialogFragment = new EditPeriodDialogFragment();
                    // параметры
                    Bundle bundle = new Bundle();
                    bundle.putLong(EditPeriodDialogFragment.ARGUMENT_ID, profilesId[position]);
                    bundle.putString(EditPeriodDialogFragment.ARGUMENT_NAME, profilesNames[position]);
                    bundle.putIntArray(EditPeriodDialogFragment.ARGUMENT_DATE_ARRAY, dates[position]);
                    editPeriodDialogFragment.setArguments(bundle);
                    editPeriodDialogFragment.show(getFragmentManager(), "editPeriodDialogFragment");
                }
                return true;
            }
        });

        // закрываем курсор
        profiles.close();
    }

// ----------- вывод данных в поля времени -----------

    void outDates() {
        Log.e("TeachersApp", "outDates");
        // чтобы текстовые поля не проверяли и не сохраняли старые данные приостанавливаем их работу
        isTextCheckRun = false;
        // меняем тексты
        startDayEditText.setText("" + dates[chosenProfileNumber][2]);
        startDayEditText.setBackgroundResource(R.drawable.button_lite_gray);
        startMonthEditText.setText("" + dates[chosenProfileNumber][1]);
        startMonthEditText.setBackgroundResource(R.drawable.button_lite_gray);
        startYearEditText.setText("" + dates[chosenProfileNumber][0]);
        startYearEditText.setBackgroundResource(R.drawable.button_lite_gray);
        endDayEditText.setText("" + dates[chosenProfileNumber][5]);
        endDayEditText.setBackgroundResource(R.drawable.button_lite_gray);
        endMonthEditText.setText("" + dates[chosenProfileNumber][4]);
        endMonthEditText.setBackgroundResource(R.drawable.button_lite_gray);
        endYearEditText.setText("" + dates[chosenProfileNumber][3]);
        endYearEditText.setBackgroundResource(R.drawable.button_lite_gray);
        // разрешаем проверку
        isTextCheckRun = true;
    }

// ----------- вывод учеников в таблицу -----------

    void outLearners() {
        // достаем учеников из базы
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersCursor = db.getLearnersByClassId(learnersClassId);

        // - чистим колонку -
        learnersColumn.removeAllViews();
        // - чистим массив с id -
        learnersId = new long[learnersCursor.getCount()];

// ---- выводим заголовок ----
        // контейнер
        LinearLayout titleContainer = new LinearLayout(this);
        titleContainer.setBackgroundColor(Color.GRAY);
        learnersColumn.addView(titleContainer,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // текст
        TextView columnTitle = new TextView(this);
        columnTitle.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
        columnTitle.setTextColor(Color.BLACK);
        columnTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        columnTitle.setText(R.string.learners_and_grades_statistics_activity_title_column_learner_name);
        LinearLayout.LayoutParams columnTitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        //columnTitleParams.setMargins(5, 5, 0, 5);
        columnTitleParams.setMargins(0, 0, 0, 0);
        titleContainer.addView(columnTitle, columnTitleParams);


// ---- проходим по ученикам выводя ячейки ----
        for (int i = 0; i < learnersCursor.getCount(); i++) {
            learnersCursor.moveToPosition(i);

            // --- получаем id ученика ---
            learnersId[i] = learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID));

            // --- ячейка таблицы ---
            // контейнер
            LinearLayout learnerContainer = new LinearLayout(this);
            learnerContainer.setBackgroundColor(Color.GRAY);
            learnersColumn.addView(
                    learnerContainer,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            // текст
            TextView learnerText = new TextView(this);
            learnerText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            learnerText.setText(
                    learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) +
                            " " +
                            learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME))
            );
            learnerText.setTextColor(Color.BLACK);
            learnerText.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            textParams.setMargins(5, 0, 0, 5);
            learnerContainer.addView(learnerText, textParams);
        }

        // закрываем базу
        learnersCursor.close();
        db.close();
    }

// ----------- вывод оценок учеников в таблицу -----------

    void outGrades() {
        // --- чистим колонки ---
        gradesColumn.removeAllViews();
        absentsColumn.removeAllViews();

// ---- выводим заголовок оценок ----
        // контейнер
        LinearLayout titleGradesContainer = new LinearLayout(this);
        titleGradesContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
        gradesColumn.addView(titleGradesContainer,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // текст
        TextView columnGradesTitle = new TextView(this);
        columnGradesTitle.setGravity(Gravity.CENTER);
        columnGradesTitle.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
        columnGradesTitle.setTextColor(Color.BLACK);
        columnGradesTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        columnGradesTitle.setText(R.string.learners_and_grades_statistics_activity_title_column_grade);
        LinearLayout.LayoutParams columnGradesTitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
//        columnGradesTitleParams.setMargins(5, 5, 5, 5);
        columnGradesTitleParams.setMargins((int) pxFromDp(5), 0, (int) pxFromDp(5), 0);
        titleGradesContainer.addView(columnGradesTitle, columnGradesTitleParams);

// ---- выводим заголовок пропусков ----
        // контейнер
        LinearLayout titleAbsentContainer = new LinearLayout(this);
        titleAbsentContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
        absentsColumn.addView(titleAbsentContainer,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // текст
        TextView columnAbsentTitle = new TextView(this);
        columnAbsentTitle.setGravity(Gravity.CENTER);
        columnAbsentTitle.setBackgroundColor(getResources().getColor(R.color.colorPrimaryBlue));
        columnAbsentTitle.setTextColor(Color.BLACK);
        columnAbsentTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        columnAbsentTitle.setText(R.string.learners_and_grades_statistics_activity_title_column_absent);
        LinearLayout.LayoutParams columnAbsentTitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
//        columnAbsentTitleParams.setMargins(0, 5, 5, 5);
        columnGradesTitleParams.setMargins((int) pxFromDp(5), 0, (int) pxFromDp(5), 0);
        titleAbsentContainer.addView(columnAbsentTitle, columnAbsentTitleParams);

// ---- пробегаемся по ученикам ----
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        for (long aLearnersId : learnersId) {
            // ---- получаем оценки за указанный период по выбранному ученику ----
            Cursor grades = db.getGradesByLearnerIdSubjectAndTimePeriod(
                    aLearnersId,
                    subjectId,
                    "" + dates[chosenProfileNumber][0] + "-" + getTwoSymbols(dates[chosenProfileNumber][1]) + "-" + getTwoSymbols(dates[chosenProfileNumber][2]) + " 00:00:00",
                    "" + dates[chosenProfileNumber][3] + "-" + getTwoSymbols(dates[chosenProfileNumber][4]) + "-" + getTwoSymbols(dates[chosenProfileNumber][5]) + " 23:59:59"
            );


            //вычисляем среднее значение и считаем 'н'
            long nCount = 0;
            float gradesSum = 0;
            float gradesCount = 0;
            for (int j = 0; j < grades.getCount(); j++) {
                grades.moveToPosition(j);
                int grade = grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_GRADE));
                switch (grade) {
                    case -1:
                        Log.wtf(TAG, "LearnersGradesStatisticsActivity.outGrades - grade is -1!");
                        break;
                    case 0:
                        break;
                    case -2:
                        nCount++;
                        break;
                    default:
                        gradesSum = gradesSum + grade;
                        gradesCount++;
                        break;
                }
            }

            // -------- пункт списка с оценками --------
            // ---- контейнер оценки ----

            LinearLayout gradeContainer = new LinearLayout(this);
            gradeContainer.setOrientation(LinearLayout.VERTICAL);
            gradeContainer.setBackgroundColor(Color.GRAY);
            LinearLayout.LayoutParams gradeContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            //добавляем вывод
            gradesColumn.addView(
                    gradeContainer,
                    gradeContainerParams
            );

            // --- текст оценки ---

            TextView gradeText = new TextView(this);
            gradeText.setTextColor(Color.BLACK);
            gradeText.setGravity(Gravity.CENTER);
            gradeText.setSingleLine(true);
            gradeText.setEms(2);
            gradeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

            // строка для покраски
            SpannableStringBuilder s = new SpannableStringBuilder();
            // цвет
            ForegroundColorSpan style;
            // ставим текст
            if (gradesCount == 0) {
                // текст оценки
                s.append("-");
                // серый для прочерков
                style = new ForegroundColorSpan(
                        getResources().getColor(R.color.gradeColorGray)
                );
                s.setSpan(style, 0, s.length() - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                // вычисляем среднюю оценку и округляем до сотых (к ближайшему)
                gradesSum = ((int)((gradesSum / gradesCount) * 100 + 0.5))/100F;
                // текст оценки
                s.append("" + (gradesSum));
                // ---- выбираем цвет оценки ----
                if (areTheGradesColored) {// выбраны ли цветные оценки
                    //5
                    if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 100) {
                        style = new ForegroundColorSpan(
                                getResources().getColor(R.color.grade5Color)
                        );
                        s.setSpan(style, 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    //4
                    if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 80) {
                        style = new ForegroundColorSpan(
                                getResources().getColor(R.color.grade4Color)
                        );
                        s.setSpan(style, 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    //3
                    if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 60) {
                        style = new ForegroundColorSpan(
                                getResources().getColor(R.color.grade3Color)
                        );
                        s.setSpan(style, 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    //2
                    if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 41) {
                        style = new ForegroundColorSpan(
                                getResources().getColor(R.color.grade2Color)
                        );
                        s.setSpan(style, 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    //1
                    if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 20) {
                        style = new ForegroundColorSpan(
                                getResources().getColor(R.color.grade1Color)
                        );
                        s.setSpan(style, 0, s.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }
            }
            gradeText.setText(s);
            gradeText.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
            LinearLayout.LayoutParams gradeTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            gradeTextParams.setMargins(5, 0, 5, 5);
            gradeContainer.addView(gradeText, gradeTextParams);

            // ---- контейнер пропусков ----

            LinearLayout absContainer = new LinearLayout(this);
            absContainer.setOrientation(LinearLayout.VERTICAL);
            absContainer.setBackgroundColor(Color.GRAY);
            LinearLayout.LayoutParams absContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            //добавляем вывод
            absentsColumn.addView(
                    absContainer,
                    absContainerParams
            );

            // --- текст пропусков ---

            TextView absentText = new TextView(this);
            absentText.setTextColor(Color.BLACK);
            absentText.setGravity(Gravity.CENTER);
            absentText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            absentText.setText(" " + nCount);
            absentText.setSingleLine(true);
            absentText.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
            LinearLayout.LayoutParams absTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            absTextParams.setMargins(0, 0, 5, 5);
            absContainer.addView(absentText, absTextParams);

            // закончили работать с курсором
            grades.close();
        }


        db.close();
    }

// ---------- обратная связь от диалогов ----------

// ----- создание -----

    @Override
    public void createStatistic(String name, String startDate, String endDate) {
        Log.i(TAG, "CreateStatisticProfile name:" + name + " start:" + startDate + " end:" + endDate);
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long profileId = db.createStatistic(name, startDate, endDate);
        db.close();
        outSpinner(profileId);
    }

// ----- удаление -----

    @Override
    public void removeStatistic(ArrayList<Long> chosenProfilesIDArray) {
        Log.i(TAG, "RemoveStatisticProfile chosenProfilesIDArray:" + Arrays.toString(chosenProfilesIDArray.toArray()));
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        for (long id : chosenProfilesIDArray) {
            db.removeStatisticProfile(id);
        }
        outSpinner(-1);
        db.close();
    }

// ----- изменение -----

    @Override
    public void editStatistic(long id, String name, String startDate, String endDate) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.setStatisticParameters(id, name, startDate, endDate);
        outSpinner(id);
        db.close();
    }

    // ----- просто обновление -----
    @Override
    public void onlyUpdate() {
        outSpinner(-1);
    }


// ---------- полезные методы ----------

    // -- метод трансформации числа в текст с двумя позициями --
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    // -- проверка дат --
    boolean isDateGood(EditText editStartDay, EditText editStartMonth, EditText editStartYear, EditText editEndDay, EditText editEndMonth, EditText editEndYear) {
        //переменная отвечающая за то подходят данные или нет
        boolean isGood = true;

        // - дата начала -

        // календарь для проверки даты
        GregorianCalendar startCalendar = new GregorianCalendar(0, 0, 1, 0, 0);

        // размеры текста
        if (editStartYear.getText().toString().length() != 4) {
            editStartYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editStartYear.getText().toString()) < 1000 || Integer.parseInt(editStartYear.getText().toString()) > 9999) {
                editStartYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                startDayEditText.setBackgroundResource(R.drawable.button_lite_gray);
                // год в календарь
                startCalendar.set(GregorianCalendar.YEAR, Integer.parseInt(editStartYear.getText().toString()));

            }
        }

        // размеры текста
        if (editStartMonth.getText().toString().length() <= 0 || editStartMonth.getText().toString().length() > 2) {
            editStartMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editStartMonth.getText().toString()) < 1 || Integer.parseInt(editStartMonth.getText().toString()) > 12) {
                editStartMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editStartMonth.setBackgroundResource(R.drawable.button_lite_gray);
                // месяц в календарь
                startCalendar.set(GregorianCalendar.MONTH, Integer.parseInt(editStartMonth.getText().toString()) - 1);
            }
        }

        //if (isGood) {
        // размеры текста
        if (editStartDay.getText().toString().length() <= 0 || editStartDay.getText().toString().length() > 2) {
            editStartDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editStartDay.getText().toString()) < 1 || Integer.parseInt(editStartDay.getText().toString()) > startCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)) {
                editStartDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editStartDay.setBackgroundResource(R.drawable.button_lite_gray);
                // день в календарь
                startCalendar.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(editStartDay.getText().toString()));
            }
        }
        //}


        // - дата конца -

        // календарь для проверки даты
        GregorianCalendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(startCalendar.getTime());

        // размеры текста
        if (editEndYear.getText().toString().length() != 4) {
            editEndYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editEndYear.getText().toString()) < 1000 || Integer.parseInt(editEndYear.getText().toString()) > 9999) {
                editEndYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editEndYear.setBackgroundResource(R.drawable.button_lite_gray);
                // год в календарь
                endCalendar.set(GregorianCalendar.YEAR, Integer.parseInt(editEndYear.getText().toString()));
            }
        }

        // размеры текста
        if (editEndMonth.getText().toString().length() <= 0 || editEndMonth.getText().toString().length() > 2) {
            editEndMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editEndMonth.getText().toString()) < 1 || Integer.parseInt(editEndMonth.getText().toString()) > 12) {
                editEndMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editEndMonth.setBackgroundResource(R.drawable.button_lite_gray);
                // месяц в календарь
                endCalendar.set(GregorianCalendar.MONTH, Integer.parseInt(editEndMonth.getText().toString()) - 1);
            }
        }


        //if (isGood) {
        // размеры текста
        if (editEndDay.getText().toString().length() <= 0 || editEndDay.getText().toString().length() > 2) {
            editEndDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editEndDay.getText().toString()) < 1 || Integer.parseInt(editEndDay.getText().toString()) > endCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)) {
                editEndDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editEndDay.setBackgroundResource(R.drawable.button_lite_gray);
                // день в календарь
                endCalendar.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(editEndDay.getText().toString()));
            }
        }
        //}

        if (isGood) {
            // проверка по времени
            if (startCalendar.getTime().getTime() > endCalendar.getTime().getTime()) {
                editStartDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editStartMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editStartYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editEndDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editEndMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editEndYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                editStartDay.setBackgroundResource(R.drawable.button_lite_gray);
                editStartMonth.setBackgroundResource(R.drawable.button_lite_gray);
                editStartYear.setBackgroundResource(R.drawable.button_lite_gray);
                editEndDay.setBackgroundResource(R.drawable.button_lite_gray);
                editEndMonth.setBackgroundResource(R.drawable.button_lite_gray);
                editEndYear.setBackgroundResource(R.drawable.button_lite_gray);
            }
        }

        // все в порядке возвращаем истину
        return isGood;
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getResources().getDisplayMetrics().density;
    }

// ----------- системные кнопки -------------

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
}