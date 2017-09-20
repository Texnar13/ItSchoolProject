package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;

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

    static LessonTimePeriod[] lessonStandardTimePeriods = new LessonTimePeriod[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_day);

        setTitle("Расписание на день");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        day = getIntent().getIntExtra(INTENT_DAY, -1);
        month = getIntent().getIntExtra(INTENT_MONTH, -1);
        year = getIntent().getIntExtra(INTENT_YEAR, -1);
        if (day == -1 || month == -1 || year == -1) {
            Log.wtf("TeachersApp", "wtf intent: ScheduleDayActivity day = " + day + " month = " + month + " year = " + year);
            finish();
        }

        ImageView previous = (ImageView) findViewById(R.id.schedule_day_button_previous);
        ImageView next = (ImageView) findViewById(R.id.schedule_day_button_next);
        final TextView dateText = (TextView) findViewById(R.id.schedule_day_date_text);
        final TableLayout table = (TableLayout) findViewById(R.id.schedule_day_table);

        final String months[] = {"Янв.", "Фев.", "Мар.", "Апр.", "Май", "Июнь", "Июль", "Авг.", "Сен.", "Окт.", "Ноя.", "Дек."};
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
        dateText.setText(day + " " + months[month]);
        dateText.setGravity(Gravity.CENTER);

        final Calendar calendar = new GregorianCalendar(year, month, day);

        outViewDay(calendar, table);


        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                    if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1);
                        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                    } else {
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
                    }
                    calendar.set(Calendar.DAY_OF_MONTH,
                            monthCapacity[calendar.get(Calendar.MONTH)]);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 1);
                }
                dateText.setText(calendar.get(Calendar.DAY_OF_MONTH) + " " + months[calendar.get(Calendar.MONTH)]);
                outViewDay(calendar, table);

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendar.get(Calendar.DAY_OF_MONTH) == monthCapacity[calendar.get(Calendar.MONTH)]) {
                    if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
                        calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    } else {
                        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
                    }
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
                }
                dateText.setText(calendar.get(Calendar.DAY_OF_MONTH) + " " + months[calendar.get(Calendar.MONTH)]);
                outViewDay(calendar, table);
            }
        });
    }

    void outViewDay(Calendar viewDay, TableLayout table) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        table.removeAllViews();
        String tableHeadStrings[] = {"№", "время", "предмет", "класс", "каб.", "доп."};

        //шапка таблицы
        {
            TextView tableHeadTexts[] = new TextView[tableHeadStrings.length];
            TableRow head = new TableRow(this);
            for (int i = 0; i < tableHeadStrings.length; i++) {
                //textView
                tableHeadTexts[i] = new TextView(this);
                tableHeadTexts[i].setText("  " + tableHeadStrings[i] + "  ");
                tableHeadTexts[i].setTextSize(20);
                tableHeadTexts[i].setTextColor(Color.BLACK);
                tableHeadTexts[i].setGravity(Gravity.CENTER);
                tableHeadTexts[i].setBackgroundColor(Color.parseColor("#e4ea7e"));
                //параметры для клеток
                RelativeLayout.LayoutParams tableRelativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                tableRelativeParams.rightMargin = (int) pxFromDp(1f);//10
                tableRelativeParams.bottomMargin = (int) pxFromDp(1f);//10
                tableRelativeParams.leftMargin = 0;
                tableRelativeParams.topMargin = (int) pxFromDp(1f);
                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setBackgroundColor(Color.parseColor("#fdffdf"));
                relativeLayout.addView(tableHeadTexts[i], tableRelativeParams);
                head.addView(relativeLayout, RelativeLayout.LayoutParams.MATCH_PARENT, (int) pxFromDp(50f));

                head.setBackgroundColor(Color.RED);//todo красный только на время отладки
            }
            table.addView(head);
        }


        //тело таблицы
        String timePeriodsString[] = {"8:30-9:15", "9:30-10:15", "10:30-11:15", "11:30-12:15", "12:25-13:10", "13:30-14:15", "14:25-15:10", "15:20-16:05", "внеурочное время"};

        lessonStandardTimePeriods[0] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 8, 30), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 9, 15));
        lessonStandardTimePeriods[1] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 9, 30), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 10, 15));
        lessonStandardTimePeriods[2] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 10, 30), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 11, 15));
        lessonStandardTimePeriods[3] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 11, 30), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 12, 15));
        lessonStandardTimePeriods[4] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 12, 25), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 13, 10));
        lessonStandardTimePeriods[5] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 13, 30), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 14, 15));
        lessonStandardTimePeriods[6] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 14, 25), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 15, 10));
        lessonStandardTimePeriods[7] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 15, 20), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 16, 5));
        lessonStandardTimePeriods[8] = new LessonTimePeriod(new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 16, 6), new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 23, 59));


//        Calendar calendarStartTime[] = {
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 8, 30),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 9, 30),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 10, 30),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 11, 30),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 12, 25),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 13, 30),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 14, 25),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 15, 20)
//        };
//        Calendar calendarEndTime[] = {
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 9, 15),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 10, 15),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 11, 15),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 12, 15),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 13, 10),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 14, 15),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 15, 10),
//                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 16, 5)
//        };

        TableRow tableRows[] = new TableRow[9];
        for (int i = 0; i < tableRows.length; i++) {
            tableRows[i] = new TableRow(this);
            boolean isLessonReady = false;
            long lessonAttitudeId = -1;
            long lessonClassId = -1;
            long lessonCabinetId = -1;
            String lessonName;
            String lessonClass;
            String lessonCabinet;
            {
                ArrayList<Long> arrayList = db.getLessonsAttitudesIdByTimePeriod(lessonStandardTimePeriods[i].calendarStartTime, lessonStandardTimePeriods[i].calendarEndTime);
                if (arrayList.size() != 0) {
                    lessonAttitudeId = arrayList.get(0);

                    Cursor lessonAttitudeCursor = db.getLessonAttitudeById(lessonAttitudeId);
                    lessonAttitudeCursor.moveToFirst();
                    //имя
                    Cursor lessonCursor = db.getLessonById(
                            lessonAttitudeCursor.getLong(
                                    lessonAttitudeCursor.getColumnIndex(
                                            SchoolContract.TableLessonAndTimeWithCabinet.
                                                    KEY_LESSON_ID)));
                    lessonCursor.moveToFirst();
                    lessonName = lessonCursor.getString(
                            lessonCursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)
                    );

                    //класс
                    lessonClassId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableLessons.KEY_CLASS_ID));
                    lessonCursor.close();
                    Cursor classCursor = db.getClasses(lessonClassId);
                    classCursor.moveToFirst();
                    lessonClass = classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
                    classCursor.close();


                    //кабинет
                    lessonCabinetId = lessonAttitudeCursor.getLong(lessonAttitudeCursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID));
                    lessonAttitudeCursor.close();
                    Cursor cabinetCursor = db.getCabinets(lessonCabinetId);
                    cabinetCursor.moveToFirst();
                    lessonCabinet = cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
                    cabinetCursor.close();

                } else {

                    lessonName = "---";
                    lessonClass = "---";
                    lessonCabinet = "---";
                }
            }
            //проверяем все ли ученики рассажены
            if (db.getNotPutLearnersIdByCabinetIdAndClassId(lessonCabinetId, lessonClassId).size() == 0) {
                isLessonReady = true;
            }

            for (int j = 0; j < tableHeadStrings.length; j++) {
                //параметры TextView
                TextView bodyText = new TextView(this);
                bodyText.setTextSize(20);
                bodyText.setGravity(Gravity.CENTER);
                if (lessonAttitudeId == -1) {
                    bodyText.setTextColor(Color.GRAY);
                    bodyText.setBackgroundColor(Color.LTGRAY);
                } else {
                    bodyText.setTextColor(Color.BLACK);
                    if (isLessonReady) {
                        bodyText.setBackgroundColor(Color.parseColor("#fdffdf"));//светло салатовый
                    } else {

                        bodyText.setBackgroundColor(Color.parseColor("#ff7700"));//оранжевый
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


                    //начать урок
                    final Intent intentForStartLesson = new Intent(this, LessonActivity.class);
                    intentForStartLesson.putExtra(LessonActivity.LESSON_ATTITUDE_ID, lessonAttitudeId);

                    //редактировать рассадку
                    final Toast toastSeatingRedactor = Toast.makeText(this, "Вы не можете начать урок пока не рассадите учеников!", Toast.LENGTH_LONG);
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
                                startActivityForResult(intentForStartSeatingRedactor, 1);//редактировать
                            }
                        }
                    });

                    bodyText.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (!(lessonAttitudeIdForIntent == -1)) {//редактирование урока
                                startActivityForResult(intentForLessonEditor, 1);
                            }
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
                bodyParams.rightMargin = (int) pxFromDp(1f);//10
                bodyParams.leftMargin = 0;
                bodyParams.bottomMargin = 0;//5
                bodyParams.topMargin = 0;


                if (j == 0) {//параметры для No
                    bodyText.setText(" " + (i + 1) + " ");

                } else if (j == 1) {//параметры для времени
                    bodyText.setText("  " + timePeriodsString[i] + "  ");

                } else if (j == 2) {//параметры для названия ур
                    bodyText.setText("  " + lessonName + "  ");

                } else if (j == 3) {//параметры для класса
                    bodyText.setText("  " + lessonClass + "  ");

                } else if (j == 4) {//параметры для кабинета
                    bodyText.setText("  " + lessonCabinet + "  ");

                } else if (j == 5) {//параметры для доп.
                    if (lessonAttitudeId == -1) {
                        bodyText.setText("  нет урока, добавить?  ");
                    } else if (!isLessonReady) {
                        bodyText.setText("  рассадите учеников!  ");
                    } else {
                        bodyText.setText("  готово, начать урок?  ");
                    }

                }

                Calendar calendar = new GregorianCalendar();//получаем текущее время
                calendar.setTime(new Date());

                RelativeLayout relativeLayout = new RelativeLayout(this);
                if (calendar.getTime().getTime() >= lessonStandardTimePeriods[i].calendarStartTime.getTime().getTime() && calendar.getTime().getTime() <= lessonStandardTimePeriods[i].calendarEndTime.getTime().getTime()) {
                    relativeLayout.setBackgroundColor(Color.RED);
                    bodyParams.bottomMargin = (int) pxFromDp(3f);//5
                    bodyParams.topMargin = (int) pxFromDp(3f);
                } else
                    relativeLayout.setBackgroundColor(Color.parseColor("#fdffdf"));
                relativeLayout.addView(bodyText, bodyParams);
                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, (int) pxFromDp(50f));//200
            }
//            //клетка в столбце No
//            TextView bodyNo = new TextView(this);
//            {
//                //параметры TextView
//                bodyNo.setText("  " + (i + 1) + "  ");
//                bodyNo.setTextSize(20);
//                if (lessonAttitudeId == -1) {
//                    bodyNo.setTextColor(Color.GRAY);
//                    bodyNo.setBackgroundColor(Color.LTGRAY);
//                } else {
//                    bodyNo.setTextColor(Color.BLACK);
//                    bodyNo.setBackgroundColor(Color.WHITE);
//                }
//                bodyNo.setGravity(Gravity.CENTER);
//
//                //параметры для клеток
//                RelativeLayout.LayoutParams bodyParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                bodyParams.rightMargin = 10;
//                bodyParams.bottomMargin = 5;
//                bodyParams.leftMargin = 0;
//                bodyParams.topMargin = 0;
//                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
//                RelativeLayout relativeLayout = new RelativeLayout(this);
//                relativeLayout.setBackgroundColor(Color.GRAY);
//                relativeLayout.addView(bodyNo, bodyParams);
//                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
//            }
//
//
//            //клетка в столбце время
//            TextView bodyTime = new TextView(this);
//            {
//                bodyTime.setText("  " + timePeriodsString[i] + "  ");
//                bodyTime.setTextSize(20);
//                if (lessonAttitudeId == -1) {
//                    bodyTime.setTextColor(Color.GRAY);
//                    bodyTime.setBackgroundColor(Color.LTGRAY);
//                } else {
//                    bodyTime.setTextColor(Color.BLACK);
//                    bodyTime.setBackgroundColor(Color.WHITE);
//                }
//                bodyTime.setGravity(Gravity.CENTER);
//                //параметры TextView
//                //параметры для клеток
//                RelativeLayout.LayoutParams bodyParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                bodyParams.rightMargin = 10;
//                bodyParams.bottomMargin = 5;
//                bodyParams.leftMargin = 0;
//                bodyParams.topMargin = 0;
//                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
//                RelativeLayout relativeLayout = new RelativeLayout(this);
//                relativeLayout.setBackgroundColor(Color.GRAY);
//                relativeLayout.addView(bodyTime, bodyParams);
//                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
//            }
//
//
//            //клетка в столбце время
//            TextView bodyLessonName = new TextView(this);
//            {
//
//                bodyLessonName.setTextSize(20);
//                if (lessonAttitudeId == -1) {
//                    bodyLessonName.setTextColor(Color.GRAY);
//                    bodyLessonName.setBackgroundColor(Color.LTGRAY);
//                } else {
//                    bodyLessonName.setTextColor(Color.BLACK);
//                    bodyLessonName.setBackgroundColor(Color.WHITE);
//                }
//                bodyLessonName.setText("  " + lessonName + "  ");
//                bodyLessonName.setGravity(Gravity.CENTER);
//                //параметры TextView
//                //параметры для клеток
//                RelativeLayout.LayoutParams bodyParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                bodyParams.rightMargin = 10;
//                bodyParams.bottomMargin = 5;
//                bodyParams.leftMargin = 0;
//                bodyParams.topMargin = 0;
//                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
//                RelativeLayout relativeLayout = new RelativeLayout(this);
//                relativeLayout.setBackgroundColor(Color.GRAY);
//                relativeLayout.addView(bodyLessonName, bodyParams);
//                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
//            }
//
//
//            //клетка в столбце класс
//            TextView bodyClass = new TextView(this);
//            {
//
//                bodyClass.setTextSize(20);
//                if (lessonAttitudeId == -1) {
//                    bodyClass.setTextColor(Color.GRAY);
//                    bodyClass.setBackgroundColor(Color.LTGRAY);
//                } else {
//                    bodyClass.setTextColor(Color.BLACK);
//                    bodyClass.setBackgroundColor(Color.WHITE);
//                }
//                bodyClass.setText("  " + lessonClass + "  ");
//                bodyClass.setGravity(Gravity.CENTER);
//                //параметры TextView
//                //параметры для клеток
//                RelativeLayout.LayoutParams bodyParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                bodyParams.rightMargin = 10;
//                bodyParams.bottomMargin = 5;
//                bodyParams.leftMargin = 0;
//                bodyParams.topMargin = 0;
//                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
//                RelativeLayout relativeLayout = new RelativeLayout(this);
//                relativeLayout.setBackgroundColor(Color.GRAY);
//                relativeLayout.addView(bodyClass, bodyParams);
//                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
//            }
//
//
//            //клетка в столбце кабинет
//            TextView bodyCabinet = new TextView(this);
//            {
//
//                bodyCabinet.setTextSize(20);
//                if (lessonAttitudeId == -1) {
//                    bodyCabinet.setTextColor(Color.GRAY);
//                    bodyCabinet.setBackgroundColor(Color.LTGRAY);
//                } else {
//                    bodyCabinet.setTextColor(Color.BLACK);
//                    bodyCabinet.setBackgroundColor(Color.WHITE);
//                }
//                bodyCabinet.setText("  " + lessonCabinet + "  ");
//                bodyCabinet.setGravity(Gravity.CENTER);
//                //параметры TextView
//                //параметры для клеток
//                RelativeLayout.LayoutParams bodyParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                bodyParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                bodyParams.rightMargin = 10;
//                bodyParams.bottomMargin = 5;
//                bodyParams.leftMargin = 0;
//                bodyParams.topMargin = 0;
//                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
//                RelativeLayout relativeLayout = new RelativeLayout(this);
//                relativeLayout.setBackgroundColor(Color.GRAY);
//                relativeLayout.addView(bodyCabinet, bodyParams);
//                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
//            }


            //----

//            TextView textView = new TextView(this);
//            textView.setText("test");
//            textView.setTextColor(Color.BLACK);
//            textView.setTextSize(20);
//            tableRows[i].addView(textView, 500, 200);
//            TextView textView2 = new TextView(this);
//            textView2.setText("test");
//            textView2.setTextColor(Color.BLACK);
//            textView2.setTextSize(20);
//            tableRows[i].addView(textView2, 500, 200);

            //добавляем в таблицу ряд
            tableRows[i].setBackgroundColor(Color.LTGRAY);
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

