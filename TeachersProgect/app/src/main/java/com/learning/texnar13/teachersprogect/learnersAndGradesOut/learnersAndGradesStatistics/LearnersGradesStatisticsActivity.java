package com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class LearnersGradesStatisticsActivity extends AppCompatActivity implements PeriodsDialogInterface {
    public static final String TAG = "TeachersApp";

    // для получения id предмета
    public static final String INTENT_SUBJECT_ID = "subjectID";


    // id полученного предмета
    private static long subjectId = -1;

    // массив периодов
    private static ArrayList<PeriodUnit> periods;
    // номер выбранного периода
    private static int periodPosition;

    // массив учеников
    private static LearnerUnit[] learners;

    // размер максимально допустимой оценки
    private static int maxAnswersCount;
    // окрашиваем ли оценки
    private static boolean areTheGradesColored = false;

    // переменная разрешающая проверять поля дат и сохранять даты
    private static boolean isTextCheckRun = true;

    // показщывается ли сейчас диалог
    static boolean isDialogShowed;


    // текстовые поля времени
    private EditText startDayEditText;
    private EditText startMonthEditText;
    private EditText startYearEditText;
    private EditText endDayEditText;
    private EditText endMonthEditText;
    private EditText endYearEditText;

    // колонка со списком оценок
    private LinearLayout gradesColumn;
    // колонка со списком пропусков
    private LinearLayout absentsColumn;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ставим содержимое экрана
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        setContentView(R.layout.activity_learners_grades_statistics);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.baseBlue));
        }


        // вертикальная ориентация
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // кнопка назад
        findViewById(R.id.learners_and_grades_statistics_toolbar_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        // получаем id предмета
        subjectId = getIntent().getLongExtra(INTENT_SUBJECT_ID, -1);
        if (subjectId == -1) {
            //id не передан
            finish();
        }


        // проверяем на поворот экрана
        if (periods == null) {

            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            // максимальная оценка
            maxAnswersCount = db.getSettingsMaxGrade(1);
            // цветные оценки
            areTheGradesColored = db.getSettingsAreTheGradesColoredByProfileId(1);


            // получаем периоды из бд
            Cursor periodsCursor = db.getAllStatisticsProfiles();
            // создаем список
            periods = new ArrayList<>(periodsCursor.getCount());
            while (periodsCursor.moveToNext()) {
                // дата начала
                String startDate = periodsCursor.getString(periodsCursor.getColumnIndex(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE));
                // дата конца
                String endDate = periodsCursor.getString(periodsCursor.getColumnIndex(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE));
                // переводим дату из строки в числа
                int[] dates = new int[6];
                dates[0] = Integer.parseInt(startDate.substring(8, 10));
                dates[1] = Integer.parseInt(startDate.substring(5, 7));
                dates[2] = Integer.parseInt(startDate.substring(0, 4));
                dates[3] = Integer.parseInt(endDate.substring(8, 10));
                dates[4] = Integer.parseInt(endDate.substring(5, 7));
                dates[5] = Integer.parseInt(endDate.substring(0, 4));

                // создаем период
                periods.add(new PeriodUnit(
                        periodsCursor.getLong(periodsCursor.getColumnIndex(SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID)),
                        periodsCursor.getString(periodsCursor.getColumnIndex(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME)),
                        dates
                ));
            }
            periodsCursor.close();


            // получаем предмет
            Cursor subjectCursor = db.getSubjectById(subjectId);
            subjectCursor.moveToFirst();
            // получаем id класса
            long classId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
            subjectCursor.close();


            // получаем учеников из бд
            Cursor learnersCursor = db.getLearnersByClassId(classId);
            // создаем массив
            learners = new LearnerUnit[learnersCursor.getCount()];
            for (int learnerI = 0; learnerI < learners.length; learnerI++) {
                learnersCursor.moveToPosition(learnerI);
                learners[learnerI] = new LearnerUnit(
                        learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)),
                        learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
                        learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME))
                );
            }
            learnersCursor.close();
        }


        // находим текстовые поля дат
        startDayEditText = (EditText) findViewById(R.id.learners_grades_statistics_begin_day);
        startMonthEditText = (EditText) findViewById(R.id.learners_grades_statistics_begin_month);
        startYearEditText = (EditText) findViewById(R.id.learners_grades_statistics_begin_year);
        endDayEditText = (EditText) findViewById(R.id.learners_grades_statistics_end_day);
        endMonthEditText = (EditText) findViewById(R.id.learners_grades_statistics_end_month);
        endYearEditText = (EditText) findViewById(R.id.learners_grades_statistics_end_year);

        // назначаем слушатели изменения текста, которые при изменении поля запускают проверку, сохранение и вывод
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isTextCheckRun)
                    // проверяем, можем ли получать оценки
                    if (periodPosition != -1) {
                        // проверяем даты на соответствие
                        if (isDateGood(startDayEditText, startMonthEditText, startYearEditText, endDayEditText, endMonthEditText, endYearEditText)) {
                            // помещаем в массив
                            Log.e("TeachersApp", "endYearEditText.afterTextChanged: saveDate");
                            periods.get(periodPosition).dates[0] = Integer.parseInt(startDayEditText.getText().toString());
                            periods.get(periodPosition).dates[1] = Integer.parseInt(startMonthEditText.getText().toString());
                            periods.get(periodPosition).dates[2] = Integer.parseInt(startYearEditText.getText().toString());
                            periods.get(periodPosition).dates[3] = Integer.parseInt(endDayEditText.getText().toString());
                            periods.get(periodPosition).dates[4] = Integer.parseInt(endMonthEditText.getText().toString());
                            periods.get(periodPosition).dates[5] = Integer.parseInt(endYearEditText.getText().toString());
                            // сохраняем в бд
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                            db.setStatisticTime(
                                    periods.get(periodPosition).periodId,
                                    "" + periods.get(periodPosition).dates[2] + "-" + getTwoSymbols(periods.get(periodPosition).dates[1]) + "-" + getTwoSymbols(periods.get(periodPosition).dates[0]),
                                    "" + periods.get(periodPosition).dates[5] + "-" + getTwoSymbols(periods.get(periodPosition).dates[4]) + "-" + getTwoSymbols(periods.get(periodPosition).dates[3])
                            );
                            db.close();
                            // выводим оценки
                            getAndOutGrades();
                        }
                    } else {
                        // если нет периода, не даем редактировать дату
                        s.clear();

                        if (!isDialogShowed) {
                            isDialogShowed = true;

                            // и вызываем диалог создания периода
                            PeriodDialogFragment periodDialog = new PeriodDialogFragment();
                            // аргументы массив названий предметов
                            String[] periodsNames = new String[periods.size()];
                            for (int periodI = 0; periodI < periods.size(); periodI++) {
                                periodsNames[periodI] = periods.get(periodI).periodName;
                            }
                            Bundle args = new Bundle();
                            args.putStringArray(PeriodDialogFragment.ARGS_PERIODS_STRING_ARRAY, periodsNames);
                            periodDialog.setArguments(args);
                            periodDialog.show(getSupportFragmentManager(), "periodDialog - Hello");
                        }
                    }
            }
        };
        startDayEditText.addTextChangedListener(watcher);
        startMonthEditText.addTextChangedListener(watcher);
        startYearEditText.addTextChangedListener(watcher);
        endDayEditText.addTextChangedListener(watcher);
        endMonthEditText.addTextChangedListener(watcher);
        endYearEditText.addTextChangedListener(watcher);


        // выводим учеников в таблицу
        LinearLayout learnersColumn = findViewById(R.id.learners_grades_learners_table);
        learnersColumn.removeAllViews();
        // выводим самих учеников
        for (LearnerUnit learner : learners) {
            // текст ученика
            TextView learnerText = new TextView(this);
            learnerText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
            learnerText.setTextColor(Color.BLACK);
            learnerText.setBackgroundColor(getResources().getColor(R.color.backgroundDarkWhite));
            learnerText.setGravity(Gravity.CENTER_VERTICAL);
            learnerText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            learnerText.setPadding(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin)
            );
            learnerText.setText(
                    learner.learnerSurname + " " + learner.learnerName
            );
            LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            learnerTextParams.bottomMargin = (int) getResources().getDimension(R.dimen.gird_lines_width);
            learnersColumn.addView(learnerText, learnerTextParams);
        }


        // выводим название периода в кнопку
        if (periods.size() == 0) {
            // ставим пустой выбор
            periodPosition = -1;
            ((TextView) findViewById(R.id.learners_grades_statistics_period_button_text)).
                    setText(R.string.learners_and_grades_statistics_activity_spinner_text_add);
        } else {
            // ставим выбор на первом предмете
            periodPosition = 0;
            // сокращаем название до 15 символов
            String shortName = periods.get(0).periodName;
            if (shortName.length() > 15) {
                shortName = shortName.substring(0, 14) + "…";
            }
            ((TextView) findViewById(R.id.learners_grades_statistics_period_button_text)).
                    setText(shortName);
        }
        // нажатие на кнопку периода
        findViewById(R.id.learners_grades_statistics_period_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isDialogShowed) {
                    isDialogShowed = true;

                    // вызываем диалог периодов
                    PeriodDialogFragment periodDialog = new PeriodDialogFragment();
                    // аргументы массив названий предметов
                    String[] periodsNames = new String[periods.size()];
                    for (int periodI = 0; periodI < periods.size(); periodI++) {
                        periodsNames[periodI] = periods.get(periodI).periodName;
                    }
                    Bundle args = new Bundle();
                    args.putStringArray(PeriodDialogFragment.ARGS_PERIODS_STRING_ARRAY, periodsNames);
                    periodDialog.setArguments(args);
                    periodDialog.show(getSupportFragmentManager(), "periodDialog - Hello");
                }
            }
        });


        // выводим дату в поля
        outDates();


        // колонка со списком оценок
        gradesColumn = (LinearLayout) findViewById(R.id.learners_grades_statistics_grade_column);
        // колонка со списком оценок
        absentsColumn = (LinearLayout) findViewById(R.id.learners_grades_statistics_absent_column);

        // выводим оценки
        getAndOutGrades();

    }


    // вывод данных в поля времени
    void outDates() {
        // чтобы текстовые поля не проверяли и не сохраняли старые данные приостанавливаем их работу
        isTextCheckRun = false;
        // меняем тексты
        if (periodPosition != -1) {
            startDayEditText.setText("" + periods.get(periodPosition).dates[0]);
            startDayEditText.setBackgroundResource(R.drawable._underlined_black);
            startMonthEditText.setText("" + periods.get(periodPosition).dates[1]);
            startMonthEditText.setBackgroundResource(R.drawable._underlined_black);
            startYearEditText.setText("" + periods.get(periodPosition).dates[2]);
            startYearEditText.setBackgroundResource(R.drawable._underlined_black);
            endDayEditText.setText("" + periods.get(periodPosition).dates[3]);
            endDayEditText.setBackgroundResource(R.drawable._underlined_black);
            endMonthEditText.setText("" + periods.get(periodPosition).dates[4]);
            endMonthEditText.setBackgroundResource(R.drawable._underlined_black);
            endYearEditText.setText("" + periods.get(periodPosition).dates[5]);
            endYearEditText.setBackgroundResource(R.drawable._underlined_black);
        }else{
            startDayEditText.setText("");
            startDayEditText.setBackgroundResource(R.drawable._underlined_black);
            startMonthEditText.setText("");
            startMonthEditText.setBackgroundResource(R.drawable._underlined_black);
            startYearEditText.setText("");
            startYearEditText.setBackgroundResource(R.drawable._underlined_black);
            endDayEditText.setText("");
            endDayEditText.setBackgroundResource(R.drawable._underlined_black);
            endMonthEditText.setText("");
            endMonthEditText.setBackgroundResource(R.drawable._underlined_black);
            endYearEditText.setText("");
            endYearEditText.setBackgroundResource(R.drawable._underlined_black);

        }
        // разрешаем проверку
        isTextCheckRun = true;
    }


    //  вывод оценок в таблицу
    void getAndOutGrades() {

        // чистим колонки
        gradesColumn.removeAllViews();
        absentsColumn.removeAllViews();

        // загружаем оценки из базы данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // пробегаемся по ученикам
        for (LearnerUnit learner : learners) {

            // вычисляем среднее значение и считаем 'н'
            long nCount = 0;
            float gradesSum = 0;
            float gradesCount = 0;

            // проверяем, можем ли получать оценки
            if (periodPosition != -1) {
                // получаем оценки за указанный период по выбранному ученику
                Cursor grades = db.getGradesByLearnerIdSubjectDate(
                        learner.learnerId,
                        subjectId,
                        periods.get(periodPosition).dates[2] + "-" + getTwoSymbols(periods.get(periodPosition).dates[1]) + "-" + getTwoSymbols(periods.get(periodPosition).dates[0]),
                        periods.get(periodPosition).dates[5] + "-" + getTwoSymbols(periods.get(periodPosition).dates[4]) + "-" + getTwoSymbols(periods.get(periodPosition).dates[3])
                );

                for (int j = 0; j < grades.getCount(); j++) {
                    grades.moveToPosition(j);
                    int grade = grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_GRADE));
                    switch (grade) {
                        case -1:
                            Log.wtf(TAG, "LearnersGradesStatisticsActivity.getAndOutGrades - grade is -1!");
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
                grades.close();
            }

            // выводим среднюю оценку ученику
            TextView gradeText = new TextView(this);
            gradeText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
            gradeText.setGravity(Gravity.CENTER);
            gradeText.setBackgroundColor(getResources().getColor(R.color.backgroundWhite));
            gradeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            gradeText.setPadding(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin)
            );
            LinearLayout.LayoutParams gradeTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            gradeTextParams.bottomMargin = (int) getResources().getDimension(R.dimen.gird_lines_width);
            gradesColumn.addView(gradeText, gradeTextParams);
            // задаем текст и цвет
            if (gradesCount == 0) {
                gradeText.setText("-");
                gradeText.setTextColor(Color.BLACK);
            } else {
                // вычисляем среднюю оценку и округляем до сотых (к ближайшему)
                gradesSum = ((int) ((gradesSum / gradesCount) * 100 + 0.5)) / 100F;
                // текст оценки
                gradeText.setText(new DecimalFormat("#0.00").format(gradesSum));
                // выбраны ли цветные оценки
                if (areTheGradesColored) {
                    // выбираем цвет оценки
                    //1
                    if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 20) {
                        gradeText.setTextColor(getResources().getColor(R.color.grade1));
                    } else
                        //2
                        if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 41) {
                            gradeText.setTextColor(getResources().getColor(R.color.grade2));
                        } else
                            //3
                            if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 60) {
                                gradeText.setTextColor(getResources().getColor(R.color.grade3));
                            } else
                                //4
                                if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 80) {
                                    gradeText.setTextColor(getResources().getColor(R.color.grade4));
                                } else
                                    //5
                                    if ((int) (((gradesSum) / (float) maxAnswersCount) * 100F) <= 100) {
                                        gradeText.setTextColor(getResources().getColor(R.color.grade5));
                                    } else
                                        // за пределами значений
                                        gradeText.setTextColor(Color.BLACK);
                }
            }


            // выводим количество пропусков
            TextView absText = new TextView(this);
            absText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
            absText.setGravity(Gravity.CENTER);
            absText.setTextColor(Color.BLACK);
            absText.setBackgroundColor(getResources().getColor(R.color.backgroundWhite));
            absText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            absText.setPadding(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin)
            );
            LinearLayout.LayoutParams absTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            absTextParams.bottomMargin = (int) getResources().getDimension(R.dimen.gird_lines_width);
            absentsColumn.addView(absText, absTextParams);
            // выводим количество пропусков
            absText.setText("" + nCount);
        }
        db.close();

    }

    // проверка дат
    boolean isDateGood(EditText editStartDay, EditText editStartMonth, EditText editStartYear, EditText editEndDay, EditText editEndMonth, EditText editEndYear) {
        //переменная отвечающая за то подходят данные или нет
        boolean isGood = true;

        // - дата начала -

        // календарь для проверки даты
        GregorianCalendar startCalendar = new GregorianCalendar(0, 0, 1, 0, 0);

        // размеры текста
        if (editStartYear.getText().toString().length() != 4) {
            editStartYear.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editStartYear.getText().toString()) < 1000 || Integer.parseInt(editStartYear.getText().toString()) > 9999) {
                editStartYear.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                isGood = false;
            } else {
                //убираем красный
                startDayEditText.setBackgroundResource(R.drawable._underlined_black);
                // год в календарь
                startCalendar.set(GregorianCalendar.YEAR, Integer.parseInt(editStartYear.getText().toString()));

            }
        }

        // размеры текста
        if (editStartMonth.getText().toString().length() <= 0 || editStartMonth.getText().toString().length() > 2) {
            editStartMonth.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editStartMonth.getText().toString()) < 1 || Integer.parseInt(editStartMonth.getText().toString()) > 12) {
                editStartMonth.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                isGood = false;
            } else {
                //убираем красный
                editStartMonth.setBackgroundResource(R.drawable._underlined_black);
                // месяц в календарь
                startCalendar.set(GregorianCalendar.MONTH, Integer.parseInt(editStartMonth.getText().toString()) - 1);
            }
        }

        //if (isGood) {
        // размеры текста
        if (editStartDay.getText().toString().length() <= 0 || editStartDay.getText().toString().length() > 2) {
            editStartDay.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editStartDay.getText().toString()) < 1 || Integer.parseInt(editStartDay.getText().toString()) > startCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)) {
                editStartDay.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                isGood = false;
            } else {
                //убираем красный
                editStartDay.setBackgroundResource(R.drawable._underlined_black);
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
            editEndYear.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editEndYear.getText().toString()) < 1000 || Integer.parseInt(editEndYear.getText().toString()) > 9999) {
                editEndYear.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                isGood = false;
            } else {
                //убираем красный
                editEndYear.setBackgroundResource(R.drawable._underlined_black);
                // год в календарь
                endCalendar.set(GregorianCalendar.YEAR, Integer.parseInt(editEndYear.getText().toString()));
            }
        }

        // размеры текста
        if (editEndMonth.getText().toString().length() <= 0 || editEndMonth.getText().toString().length() > 2) {
            editEndMonth.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editEndMonth.getText().toString()) < 1 || Integer.parseInt(editEndMonth.getText().toString()) > 12) {
                editEndMonth.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                isGood = false;
            } else {
                //убираем красный
                editEndMonth.setBackgroundResource(R.drawable._underlined_black);
                // месяц в календарь
                endCalendar.set(GregorianCalendar.MONTH, Integer.parseInt(editEndMonth.getText().toString()) - 1);
            }
        }


        //if (isGood) {
        // размеры текста
        if (editEndDay.getText().toString().length() <= 0 || editEndDay.getText().toString().length() > 2) {
            editEndDay.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
            isGood = false;
        } else {
            // диапазон чисел
            if (Integer.parseInt(editEndDay.getText().toString()) < 1 || Integer.parseInt(editEndDay.getText().toString()) > endCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)) {
                editEndDay.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                isGood = false;
            } else {
                //убираем красный
                editEndDay.setBackgroundResource(R.drawable._underlined_black);
                // день в календарь
                endCalendar.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(editEndDay.getText().toString()));
            }
        }
        //}

        if (isGood) {
            // проверка по времени
            if (startCalendar.getTime().getTime() > endCalendar.getTime().getTime()) {
                editStartDay.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                editStartMonth.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                editStartYear.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                editEndDay.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                editEndMonth.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                editEndYear.setBackground(getResources().getDrawable(R.drawable._underlined_black_pink));
                isGood = false;
            } else {
                editStartDay.setBackgroundResource(R.drawable._underlined_black);
                editStartMonth.setBackgroundResource(R.drawable._underlined_black);
                editStartYear.setBackgroundResource(R.drawable._underlined_black);
                editEndDay.setBackgroundResource(R.drawable._underlined_black);
                editEndMonth.setBackgroundResource(R.drawable._underlined_black);
                editEndYear.setBackgroundResource(R.drawable._underlined_black);
            }
        }

        // все в порядке возвращаем истину
        return isGood;
    }


    // обратная связь от диаологов

    @Override
    public void setPeriodPosition(int position) {
        // ставим выбор на последнем периоде
        periodPosition = position;

        // сокращаем название до 15 символов
        String shortName = periods.get(periodPosition).periodName;
        if (shortName.length() > 15) {
            shortName = shortName.substring(0, 14) + "…";
        }
        // выводим поле в название
        ((TextView) findViewById(R.id.learners_grades_statistics_period_button_text)).setText(shortName);

        // выводим в поля даты
        outDates();

        // и выводим оценки
        getAndOutGrades();
    }

    @Override
    public void createPeriod(String name) {

        // сохраняем новую дату в бд и выставляем ее в поля
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // получаем текущую дату
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.setLenient(false);
        nowCalendar.setTime(new Date());
        int[] dates = new int[6];
        dates[0] = nowCalendar.get(GregorianCalendar.DAY_OF_MONTH);
        dates[1] = nowCalendar.get(GregorianCalendar.MONTH) + 1;
        dates[2] = nowCalendar.get(GregorianCalendar.YEAR);
        // берем завтрашнюю дату
        nowCalendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
        dates[3] = nowCalendar.get(GregorianCalendar.DAY_OF_MONTH);
        dates[4] = (nowCalendar.get(GregorianCalendar.MONTH) + 1);
        dates[5] = nowCalendar.get(GregorianCalendar.YEAR);

        String startDate = dates[2] + "-" + getTwoSymbols(dates[1]) + "-" + getTwoSymbols(dates[0]);
        String endDate = dates[5] + "-" + getTwoSymbols(dates[4]) + "-" + getTwoSymbols(dates[3]);

        long periodId = db.createStatistic(name, startDate, endDate);

        // добавляем профиль в список
        periods.add(new PeriodUnit(periodId, name, dates));


        // ставим выбор на последнем периоде
        periodPosition = periods.size() - 1;

        // сокращаем название до 15 символов
        if (name.length() > 15) {
            name = name.substring(0, 14) + "…";
        }
        // выводим поле в название
        ((TextView) findViewById(R.id.learners_grades_statistics_period_button_text)).setText(name);

        // выводим в поля даты
        outDates();

        // и выводим оценки
        getAndOutGrades();

        db.close();
    }

    @Override
    public void deletePeriods(boolean[] deleteList) {
        for (int periodI = deleteList.length - 1; periodI >= 0; periodI--) {

            if (deleteList[periodI]) {
                // удаляем период из базы данных
                DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                db.removeStatisticProfile(periods.get(periodI).periodId);
                db.close();

                // удаляем период из списка
                periods.remove(periodI);
            }
        }

        // выводим первый или нулевой период
        if (periods.size() == 0) {
            // ставим пустой выбор
            periodPosition = -1;
            ((TextView) findViewById(R.id.learners_grades_statistics_period_button_text)).
                    setText(R.string.learners_and_grades_statistics_activity_spinner_text_add);
            // и пустые даты
            outDates();
        } else {
            // ставим выбор на первом предмете
            periodPosition = 0;
            // сокращаем название до 15 символов
            String shortName = periods.get(0).periodName;
            if (shortName.length() > 15) {
                shortName = shortName.substring(0, 14) + "…";
            }
            ((TextView) findViewById(R.id.learners_grades_statistics_period_button_text)).
                    setText(shortName);
        }
        outDates();
        getAndOutGrades();
    }

    @Override
    public void renamePeriods(String[] newPeriodsNames) {

        // переименоввываем все периоды
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        for (int periodI = 0; periodI < periods.size(); periodI++) {
            // меняем их в списке
            periods.get(periodI).periodName = newPeriodsNames[periodI];
            // меняем их в базе данных
            db.setStatisticName(periods.get(periodI).periodId, periods.get(periodI).periodName);
        }
        db.close();

        // проверяем не пустой ли писок
        if (periodPosition != -1) {
            // выводим переименованный предмет
            // сокращаем название до 15 символов
            String shortName = periods.get(periodPosition).periodName;
            if (shortName.length() > 15) {
                shortName = shortName.substring(0, 14) + "…";
            }
            ((TextView) findViewById(R.id.learners_grades_statistics_period_button_text)).
                    setText(shortName);
        }
        outDates();
        getAndOutGrades();
    }

    @Override
    public void onPeriodDialogClosed() {
        isDialogShowed = false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//кнопка назад в actionBar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // чистим данные, чтобы при следующем заходе загрузить заново
        periods = null;
    }


    // метод трансформации числа в текст с двумя позициями
    String getTwoSymbols(int number) {
        return new DecimalFormat("#00").format(number);
    }


    // класс хранящий в себе данные одного периода
    class PeriodUnit {

        // id периода
        long periodId;
        // имя преиода
        String periodName;
        // даты {day,month,year, day,month,year}
        int[] dates;


        public PeriodUnit(long periodId, String periodName, int[] dates) {
            this.periodId = periodId;
            this.periodName = periodName;
            this.dates = dates;
        }
    }

    // класс хранящий одного ученика
    class LearnerUnit {

        // id ученика
        long learnerId;
        // имя ученика
        String learnerName;
        // фамилия ученика
        String learnerSurname;


        public LearnerUnit(long learnerId, String learnerName, String learnerSurname) {
            this.learnerId = learnerId;
            this.learnerName = learnerName;
            this.learnerSurname = learnerSurname;
        }
    }
}