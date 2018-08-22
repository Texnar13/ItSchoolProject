package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.lessonRedactor.LessonRedactorActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScheduleDayActivity extends AppCompatActivity {

    public static final String INTENT_DAY = "day";
    public static final String INTENT_MONTH = "month";
    public static final String INTENT_YEAR = "year";

    int day = -1;
    int month = -1;
    int year = -1;

    //время уроков из бд
    int[][] lessonsTime;

    //время уроков с сегодняшней датой и временем из бд
    static LessonTimePeriod[] lessonStandardTimePeriods = new LessonTimePeriod[9];

//-------------------------------меню сверху--------------------------------------------------------

    //раздуваем неаше меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shedule_day_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //назначаем функции меню
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        //кнопка помощь
        menu.findItem(R.id.shedule_day_menu_item_help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.schedule_day_activity_toast_develop, Toast.LENGTH_LONG);
                toast.show();

                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_day);

        //setTitle("Расписание на день");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        day = getIntent().getIntExtra(INTENT_DAY, -1);
        month = getIntent().getIntExtra(INTENT_MONTH, -1);
        year = getIntent().getIntExtra(INTENT_YEAR, -1);
        if (day == -1 || month == -1 || year == -1) {
            Log.wtf("TeachersApp", "wtf intent: ScheduleDayActivity day = " + day + " month = " + month + " year = " + year);
            finish();
        }
//-----кнопки вперед назад------

        final TableLayout table = (TableLayout) findViewById(R.id.schedule_day_table);

        final String months[] = getResources().getStringArray(R.array.months_names_low_case);
        final int monthCapacity[];
        if (year % 4 == 0 &&
                year % 100 != 0 ||
                year % 400 == 0) {
            monthCapacity = new int[]{31, 29,//високосный
                    31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        } else {
            monthCapacity = new int[]{31, 28,//не високосный
                    31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        }
//        dateText.setText(day + " " + months[month]);
        setTitle(getTitle() + " " + day + " " + months[month]);
//        dateText.setGravity(Gravity.CENTER);

        final Calendar calendar = new GregorianCalendar(year, month, day);

        outViewDay(calendar, table);


//        previous.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
//                    if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
//                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
//                        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
//                    } else {
//                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
//                    }
//                    calendar.set(Calendar.DAY_OF_MONTH,
//                            monthCapacity[calendar.get(Calendar.MONTH)]);
//                } else {
//                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
//                }
//                dateText.setText(calendar.get(Calendar.DAY_OF_MONTH) + " " + months[calendar.get(Calendar.MONTH)]);
//                setTitle("Расписание на " +calendar.get(Calendar.DAY_OF_MONTH) + " " + months[calendar.get(Calendar.MONTH)]);
//                outViewDay(calendar, table);
//
//            }
//        });
//
//        next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (calendar.get(Calendar.DAY_OF_MONTH) == monthCapacity[calendar.get(Calendar.MONTH)]) {
//                    if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
//                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
//                        calendar.set(Calendar.MONTH, Calendar.JANUARY);
//                    } else {
//                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
//                    }
//                    calendar.set(Calendar.DAY_OF_MONTH, 1);
//                } else {
//                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
//                }
//                dateText.setText(calendar.get(Calendar.DAY_OF_MONTH) + " " + months[calendar.get(Calendar.MONTH)]);
//                setTitle("Расписание на " +calendar.get(Calendar.DAY_OF_MONTH) + " " + months[calendar.get(Calendar.MONTH)]);
//                outViewDay(calendar, table);
//            }
//        });
    }

    void outViewDay(Calendar viewDay, TableLayout table) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        table.removeAllViews();
        String tableHeadStrings[] = getResources().getStringArray(R.array.schedule_day_activity_table_titles_array);

        //шапка таблицы
        {
            TextView tableHeadTexts[] = new TextView[tableHeadStrings.length];
            TableRow head = new TableRow(this);
            for (int i = 0; i < tableHeadStrings.length; i++) {
                //textView
                tableHeadTexts[i] = new TextView(this);
                tableHeadTexts[i].setAllCaps(true);
                tableHeadTexts[i].setText("  " + tableHeadStrings[i] + "  ");
                tableHeadTexts[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                tableHeadTexts[i].setTextColor(Color.BLACK);
                tableHeadTexts[i].setGravity(Gravity.CENTER);
//цвет заголовка
                tableHeadTexts[i].setBackgroundColor(getResources().getColor(R.color.colorPrimaryGreen));
                //tableHeadTexts[i].setBackgroundColor(Color.parseColor("#e4ea7e"));
                //параметры для клеток
                RelativeLayout.LayoutParams tableRelativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                tableRelativeParams.rightMargin = 0;//(int) pxFromDp(1f);//10
                tableRelativeParams.bottomMargin = (int) pxFromDp(1f * getResources().getInteger(R.integer.desks_screen_multiplier));//10
                tableRelativeParams.leftMargin = 0;
                tableRelativeParams.topMargin = (int) pxFromDp(1f * getResources().getInteger(R.integer.desks_screen_multiplier));
                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setBackgroundColor(Color.LTGRAY);//"#fdffdf"
                relativeLayout.addView(tableHeadTexts[i], tableRelativeParams);
                head.addView(relativeLayout, RelativeLayout.LayoutParams.MATCH_PARENT, (int) pxFromDp(50f * getResources().getInteger(R.integer.desks_screen_multiplier)));

                head.setBackgroundColor(Color.RED);//красный только на время отладки
            }
            table.addView(head);
        }

        lessonsTime = db.getSettingsTime(1);

        //тело таблицы
        String timePeriodsString[] = {
                "" + lessonsTime[0][0] + ":" + lessonsTime[0][1] + "—" + lessonsTime[0][2] + ":" + lessonsTime[0][3],
                lessonsTime[1][0] + ":" + lessonsTime[1][1] + "—" + lessonsTime[1][2] + ":" + lessonsTime[1][3],
                lessonsTime[2][0] + ":" + lessonsTime[2][1] + "—" + lessonsTime[2][2] + ":" + lessonsTime[2][3],
                lessonsTime[3][0] + ":" + lessonsTime[3][1] + "—" + lessonsTime[3][2] + ":" + lessonsTime[3][3],
                lessonsTime[4][0] + ":" + lessonsTime[4][1] + "—" + lessonsTime[4][2] + ":" + lessonsTime[4][3],
                lessonsTime[5][0] + ":" + lessonsTime[5][1] + "—" + lessonsTime[5][2] + ":" + lessonsTime[5][3],
                lessonsTime[6][0] + ":" + lessonsTime[6][1] + "—" + lessonsTime[6][2] + ":" + lessonsTime[6][3],
                lessonsTime[7][0] + ":" + lessonsTime[7][1] + "—" + lessonsTime[7][2] + ":" + lessonsTime[7][3],
                getResources().getString(R.string.schedule_day_activity_table_time_string_no_working_time)
        };

        //Todo на заметку, замени когда будешь пересобирать
        //получаем текущий месяц и год
        //SimpleDateFormat nowDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //nowDateFormat.format(new Date());
        //


        lessonStandardTimePeriods[0] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[0][0], lessonsTime[0][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[0][2], lessonsTime[0][3]));
        lessonStandardTimePeriods[1] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[1][0], lessonsTime[1][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[1][2], lessonsTime[1][3]));
        lessonStandardTimePeriods[2] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[2][0], lessonsTime[2][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[2][2], lessonsTime[2][3]));
        lessonStandardTimePeriods[3] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[3][0], lessonsTime[3][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[3][2], lessonsTime[3][3]));
        lessonStandardTimePeriods[4] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[4][0], lessonsTime[4][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[4][2], lessonsTime[4][3]));
        lessonStandardTimePeriods[5] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[5][0], lessonsTime[5][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[5][2], lessonsTime[5][3]));
        lessonStandardTimePeriods[6] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[6][0], lessonsTime[6][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[6][2], lessonsTime[6][3]));
        lessonStandardTimePeriods[7] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[7][0], lessonsTime[7][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[7][2], lessonsTime[7][3]));
        lessonStandardTimePeriods[8] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[8][0], lessonsTime[8][1]), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), lessonsTime[8][2], lessonsTime[8][3]));

        TableRow tableRows[] = new TableRow[9];//строки таблицы
        for (int i = 0; i < tableRows.length; i++) {
            tableRows[i] = new TableRow(this);//инициализация
            boolean isLessonReady = false;//можно ли начать урок
            long lessonAttitudeId = -1;//id-шники
            long lessonClassId = -1;
            long lessonCabinetId = -1;
            String lessonName;//строки которые выводятся в таблицу
            String lessonClass;
            String lessonCabinet;
            {
                //id урока в выбарнном интервале времени (45 мин)
                ArrayList<Long> arrayList = db.getSubjectAndTimeCabinetAttitudesIdByTimePeriod(lessonStandardTimePeriods[i].calendarStartTime, lessonStandardTimePeriods[i].calendarEndTime);
                if (arrayList.size() != 0) {//есть ли в это время урок
                    lessonAttitudeId = arrayList.get(0);//id зависимости урока
                    Cursor lessonAttitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(lessonAttitudeId);//зависимость по id
                    lessonAttitudeCursor.moveToFirst();
                    //имя
                    Cursor lessonCursor = db.getSubjectById(
                            lessonAttitudeCursor.getLong(
                                    lessonAttitudeCursor.getColumnIndex(
                                            SchoolContract.TableSubjectAndTimeCabinetAttitude.
                                                    KEY_SUBJECT_ID)));
                    lessonCursor.moveToFirst();
                    lessonName = lessonCursor.getString(//ставим имя урока
                            lessonCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME)
                    );

                    //класс
                    lessonClassId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
                    lessonCursor.close();
                    Cursor classCursor = db.getClasses(lessonClassId);
                    classCursor.moveToFirst();
                    lessonClass = classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
                    classCursor.close();


                    //кабинет
                    lessonCabinetId = lessonAttitudeCursor.getLong(lessonAttitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
                    lessonAttitudeCursor.close();
                    Cursor cabinetCursor = db.getCabinets(lessonCabinetId);
                    cabinetCursor.moveToFirst();
                    lessonCabinet = cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
                    cabinetCursor.close();

                } else {

                    lessonName = "—";
                    lessonClass = "—";
                    lessonCabinet = "—";
                }
            }
            //проверяем все ли ученики рассажены
            if (db.getNotPutLearnersIdByCabinetIdAndClassId(lessonCabinetId, lessonClassId).size() == 0) {
                isLessonReady = true;
            }

            for (int j = 0; j < tableHeadStrings.length; j++) {
                //параметры TextView
                TextView bodyText = new TextView(this);
                bodyText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                bodyText.setGravity(Gravity.CENTER);
                if (lessonAttitudeId == -1) {
                    bodyText.setTextColor(Color.GRAY);
//цвет строки"#efeaea"
                    bodyText.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
                } else {
                    bodyText.setTextColor(Color.BLACK);
                    if (isLessonReady) {
                        bodyText.setBackgroundColor(Color.WHITE);//светло салатовый"#fdffdf"
                    } else {

                        bodyText.setBackgroundColor(getResources().getColor(R.color.colorAccentRed));//оранжевый
                    }
                }
                {//обработка нажатия
                    final boolean intentIsLessonReady = isLessonReady;

                    final long lessonAttitudeIdForIntent = lessonAttitudeId;

                    //создание/редактирование урока
                    final Intent intentForLessonEditor = new Intent(this, LessonRedactorActivity.class);
                    intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, lessonAttitudeId);
                    intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_START_TIME, lessonStandardTimePeriods[i].calendarStartTime.getTime().getTime());
                    intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_END_TIME, lessonStandardTimePeriods[i].calendarEndTime.getTime().getTime());

                    //парсим дату
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //начать урок
                    final Intent intentForStartLesson = new Intent(this, LessonActivity.class);
                    intentForStartLesson.putExtra(LessonActivity.LESSON_ATTITUDE_ID, lessonAttitudeId);
                    intentForStartLesson.putExtra(LessonActivity.LESSON_TIME, dateFormat.format(lessonStandardTimePeriods[i].calendarStartTime.getTime()));

                    //редактировать рассадку
                    final Toast toastSeatingRedactor = Toast.makeText(this, R.string.schedule_day_activity_toast_learners, Toast.LENGTH_LONG);
                    final Intent intentForStartSeatingRedactor = new Intent(this, SeatingRedactorActivity.class);
                    intentForStartSeatingRedactor.putExtra(SeatingRedactorActivity.CABINET_ID, lessonCabinetId);
                    intentForStartSeatingRedactor.putExtra(SeatingRedactorActivity.CLASS_ID, lessonClassId);

                    bodyText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (lessonAttitudeIdForIntent == -1) {//создание урока
                                startActivityForResult(intentForLessonEditor, 1);
                            } else if (intentIsLessonReady) {//начать урок
                                startActivity(intentForStartLesson);
                            } else {
                                toastSeatingRedactor.show();
                                startActivityForResult(intentForStartSeatingRedactor, 1);//редактировать рассадку
                            }
                        }
                    });

                    bodyText.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            startActivityForResult(intentForLessonEditor, 1);//создание/редактирование урока
                            return true;
                        }
                    });
                }

                //параметры для клеток
                RelativeLayout.LayoutParams bodyParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                bodyParams.rightMargin = 0;//(int) pxFromDp(1f);//10
                bodyParams.leftMargin = 0;
                bodyParams.bottomMargin = 0;//5
                bodyParams.topMargin = 0;


                if (j == 0) {//параметры для No
                    bodyText.setText(" " + (i + 1) + " ");

                } else if (j == 1) {//параметры для времени
                    bodyText.setText("" + timePeriodsString[i] + "");

                } else if (j == 2) {//параметры для названия ур
                    bodyText.setText("  " + lessonName + "  ");

                } else if (j == 3) {//параметры для класса
                    bodyText.setText("  " + lessonClass + "  ");

                } else if (j == 4) {//параметры для кабинета
                    bodyText.setText("  " + lessonCabinet + "  ");

                } else if (j == 5) {//параметры для доп.
                    if (lessonAttitudeId == -1) {
                        bodyText.setText("  " + getString(R.string.schedule_day_activity_table_additional_title_empty) + "  ");
                    } else if (!isLessonReady) {
                        bodyText.setText("  " + getString(R.string.schedule_day_activity_table_additional_title_learners) + "  ");
                    } else {
                        bodyText.setText("  " + getString(R.string.schedule_day_activity_table_additional_title_ready) + "  ");
                    }

                }

                Calendar calendar = new GregorianCalendar();//получаем текущее время
                calendar.setTime(new Date());
                //ищем текущий
                RelativeLayout relativeLayout = new RelativeLayout(this);
                if (calendar.getTime().getTime() >= lessonStandardTimePeriods[i].calendarStartTime.getTime().getTime() && calendar.getTime().getTime() <= lessonStandardTimePeriods[i].calendarEndTime.getTime().getTime()) {
                    relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimaryGreen));
                    bodyParams.bottomMargin = (int) pxFromDp(2f * getResources().getInteger(R.integer.desks_screen_multiplier));//5
                    bodyParams.topMargin = (int) pxFromDp(2f * getResources().getInteger(R.integer.desks_screen_multiplier));
                } else
                    relativeLayout.setBackgroundColor(Color.WHITE);//"#fdffdf"
                relativeLayout.addView(bodyText, bodyParams);
                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, (int) pxFromDp(50f * getResources().getInteger(R.integer.desks_screen_multiplier)));//200
            }
//цвет ряда"#efeaea"
            //добавляем в таблицу ряд
            tableRows[i].setBackgroundColor(getResources().getColor(R.color.colorBackGround));
            table.addView(tableRows[i], new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
            db.close();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TableLayout table = (TableLayout) findViewById(R.id.schedule_day_table);
        Calendar calendar = new GregorianCalendar(year, month, day);
        outViewDay(calendar, table);
    }

    float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
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
}

class LessonTimePeriod {
    GregorianCalendar calendarStartTime;
    GregorianCalendar calendarEndTime;

    public LessonTimePeriod(GregorianCalendar calendarStartTime, GregorianCalendar calendarEndTime) {
        this.calendarStartTime = calendarStartTime;
        this.calendarEndTime = calendarEndTime;
    }
}

