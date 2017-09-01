package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ScheduleDayActivity extends AppCompatActivity {

    public static final String INTENT_DAY = "day";
    public static final String INTENT_MONTH = "month";
    public static final String INTENT_YEAR = "year";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_day);

        int day = getIntent().getIntExtra(INTENT_DAY, -1);
        final int month = getIntent().getIntExtra(INTENT_MONTH, -1);
        int year = getIntent().getIntExtra(INTENT_YEAR, -1);
        if (day == -1 || month == -1 || year == -1) {
            Log.wtf("TeachersApp", "wtf intent: ScheduleDayActivity day = " + day + " month = " + month + " year = " + year);
            finish();
        }

        Button previous = (Button) findViewById(R.id.schedule_day_button_previous);
        Button next = (Button) findViewById(R.id.schedule_day_button_next);
        final TextView dateText = (TextView) findViewById(R.id.schedule_day_date_text);
        final TableLayout table = (TableLayout) findViewById(R.id.schedule_day_table);

        final String months[] = {"Янв.", "Фев.", "Мар.", "Апр.", "Май", "Июнь", "Июль", "Авг.", "Сен.", "Окт.", "Ноя.", "Дек."};
        final int monthCapacity[] = {31, 28,//високосный 29
                31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        dateText.setText(day + " " + months[month]);
        dateText.setGravity(Gravity.CENTER);

        final Calendar calendar = new GregorianCalendar(year, month, day);

        outDay(calendar, table);


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
                outDay(calendar, table);

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
                outDay(calendar, table);
            }
        });
    }

    void outDay(Calendar viewDay, TableLayout table) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        table.removeAllViews();

        //шапка таблицы
        String tableHeadStrings[] = {"No", "время", "название ур.", "класс", "кабинет"};
        TextView tableHeadTexts[] = new TextView[tableHeadStrings.length];
        TableRow head = new TableRow(this);
        for (int i = 0; i < tableHeadStrings.length; i++) {
            //textView
            tableHeadTexts[i] = new TextView(this);
            tableHeadTexts[i].setText("  " + tableHeadStrings[i] + "  ");
            tableHeadTexts[i].setTextSize(20);
            tableHeadTexts[i].setTextColor(Color.BLACK);
            tableHeadTexts[i].setGravity(Gravity.CENTER);
            tableHeadTexts[i].setBackgroundColor(Color.WHITE);
            //параметры для клеток
            RelativeLayout.LayoutParams tableRelativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
            tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            tableRelativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tableRelativeParams.rightMargin = 10;
            tableRelativeParams.bottomMargin = 10;
            tableRelativeParams.leftMargin = 0;
            tableRelativeParams.topMargin = 0;
            //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
            RelativeLayout relativeLayout = new RelativeLayout(this);
            relativeLayout.setBackgroundColor(Color.GRAY);
            relativeLayout.addView(tableHeadTexts[i], tableRelativeParams);
            head.addView(relativeLayout, RelativeLayout.LayoutParams.MATCH_PARENT, 200);


            head.setBackgroundColor(Color.RED);//todo красный только на время отладки
        }
//        TextView headNo = new TextView(this);
//        headNo.setText("No");
//        headNo.setTextSize(20);
//        headNo.setTextColor(Color.BLACK);
//        headNo.setGravity(Gravity.CENTER);
//        headNo.setBackgroundColor(Color.WHITE);
//
//        TextView headTime = new TextView(this);
//        headTime.setText("время");
//        headTime.setTextSize(20);
//        headTime.setTextColor(Color.BLACK);
//        headTime.setGravity(Gravity.CENTER);
//        headTime.setBackgroundColor(Color.WHITE);
//        TextView headLesson = new TextView(this);
//        headLesson.setText("название ур.");
//        headLesson.setTextSize(20);
//        headLesson.setTextColor(Color.BLACK);
//        headLesson.setGravity(Gravity.CENTER);
//        headLesson.setBackgroundColor(Color.WHITE);
//        TextView headClass = new TextView(this);
//        headClass.setText("класс");
//        headClass.setTextSize(20);
//        headClass.setTextColor(Color.BLACK);
//        headClass.setGravity(Gravity.CENTER);
//        headClass.setBackgroundColor(Color.WHITE);
//        TextView headCabinet = new TextView(this);
//        headCabinet.setText("кабинет");
//        headCabinet.setTextSize(20);
//        headCabinet.setTextColor(Color.BLACK);
//        headCabinet.setGravity(Gravity.CENTER);
//        headCabinet.setBackgroundColor(Color.WHITE);
//
//        TableRow head = new TableRow(this);
//        head.addView(headNo, 200, 200);
//        head.addView(headTime, 290, 200);
//        head.addView(headLesson, 580, 200);
//        head.addView(headClass, 300, 200);
//        head.addView(headCabinet, 380, 200);
        table.addView(head);

        //тело таблицы

        String timePeriodsString[] = {"8:30-9:15", "9:30-10:15", "10:30-11:15", "11:30-12:15", "12:25-13:10", "13:30-14:15", "14:25-15:10", "15:20-16:05"};
        Calendar calendarStartTime[] = {
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 8, 30),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 9, 30),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 10, 30),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 11, 30),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 12, 25),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 13, 30),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 14, 25),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 15, 20)
        };
        Calendar calendarEndTime[] = {
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 9, 15),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 10, 15),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 11, 15),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 12, 15),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 13, 10),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 14, 15),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 15, 10),
                new GregorianCalendar(viewDay.get(Calendar.YEAR), viewDay.get(Calendar.MONTH), viewDay.get(Calendar.DAY_OF_MONTH), 16, 5)
        };

        TableRow tableRows[] = new TableRow[8];
        for (int i = 0; i < tableRows.length; i++) {
            tableRows[i] = new TableRow(this);
            long lessonAttitudeId = -1;
            {
                ArrayList<Long> arrayList = db.getLessonsAttitudesIdByTimePeriod(calendarStartTime[i], calendarEndTime[i]);
                if (arrayList.size() != 0) {
                    lessonAttitudeId = arrayList.get(0);
                }
            }


            String lessonName;
            String lessonClass;
            String lessonCabinet;
            if (lessonAttitudeId != -1) {
                Cursor lessonAttitudeCursor = db.getLessonAttitudeById(lessonAttitudeId);
                lessonAttitudeCursor.moveToFirst();
                //имя
                Cursor lessonCursor = db.getLessonById(
                        lessonAttitudeCursor.getLong(
                                lessonAttitudeCursor.getColumnIndex(
                                        SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID)));
                lessonCursor.moveToFirst();
                lessonName = lessonCursor.getString(lessonCursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME));

                //класс
                Cursor classCursor = db.getClasses(lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableLessons.KEY_CLASS_ID)));
                classCursor.moveToFirst();
                lessonClass = classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
                classCursor.close();

                lessonCursor.close();

                //кабинет
                Cursor cabinetCursor = db.getCabinets(lessonAttitudeCursor.getLong(lessonAttitudeCursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID)));
                cabinetCursor.moveToFirst();
                lessonCabinet = cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
                cabinetCursor.close();
                lessonAttitudeCursor.close();
            } else {
                lessonName = "---";
                lessonClass = "---";
                lessonCabinet = "---";
            }


            //клетка в столбце No
            TextView bodyNo = new TextView(this);
            {
                bodyNo.setText("  " + (i + 1) + "  ");
                bodyNo.setTextSize(20);
                if (lessonAttitudeId == -1) {
                    bodyNo.setTextColor(Color.GRAY);
                    bodyNo.setBackgroundColor(Color.LTGRAY);
                } else {
                    bodyNo.setTextColor(Color.BLACK);
                    bodyNo.setBackgroundColor(Color.WHITE);
                }
                bodyNo.setGravity(Gravity.CENTER);

                //параметры TextView
                //параметры для клеток
                RelativeLayout.LayoutParams bodyNoParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
                bodyNoParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                bodyNoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                bodyNoParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                bodyNoParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                bodyNoParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                bodyNoParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                bodyNoParams.rightMargin = 10;
                bodyNoParams.bottomMargin = 5;
                bodyNoParams.leftMargin = 0;
                bodyNoParams.topMargin = 0;
                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setBackgroundColor(Color.GRAY);
                relativeLayout.addView(bodyNo, bodyNoParams);
                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
            }


            //клетка в столбце время
            TextView bodyTime = new TextView(this);
            {
                bodyTime.setText("  " + timePeriodsString[i] + "  ");
                bodyTime.setTextSize(20);
                if (lessonAttitudeId == -1) {
                    bodyTime.setTextColor(Color.GRAY);
                    bodyTime.setBackgroundColor(Color.LTGRAY);
                } else {
                    bodyTime.setTextColor(Color.BLACK);
                    bodyTime.setBackgroundColor(Color.WHITE);
                }
                bodyTime.setGravity(Gravity.CENTER);
                //параметры TextView
                //параметры для клеток
                RelativeLayout.LayoutParams bodyTimeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
                bodyTimeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                bodyTimeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                bodyTimeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                bodyTimeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                bodyTimeParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                bodyTimeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                bodyTimeParams.rightMargin = 10;
                bodyTimeParams.bottomMargin = 5;
                bodyTimeParams.leftMargin = 0;
                bodyTimeParams.topMargin = 0;
                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setBackgroundColor(Color.GRAY);
                relativeLayout.addView(bodyTime, bodyTimeParams);
                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
            }


            //клетка в столбце время
            TextView bodyLessonName = new TextView(this);
            {

                bodyLessonName.setTextSize(20);
                if (lessonAttitudeId == -1) {
                    bodyLessonName.setTextColor(Color.GRAY);
                    bodyLessonName.setBackgroundColor(Color.LTGRAY);
                } else {
                    bodyLessonName.setTextColor(Color.BLACK);
                    bodyLessonName.setBackgroundColor(Color.WHITE);
                }
                bodyLessonName.setText("  " + lessonName + "  ");
                bodyLessonName.setGravity(Gravity.CENTER);
                //параметры TextView
                //параметры для клеток
                RelativeLayout.LayoutParams bodyLessonNameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
                bodyLessonNameParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                bodyLessonNameParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                bodyLessonNameParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                bodyLessonNameParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                bodyLessonNameParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                bodyLessonNameParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                bodyLessonNameParams.rightMargin = 10;
                bodyLessonNameParams.bottomMargin = 5;
                bodyLessonNameParams.leftMargin = 0;
                bodyLessonNameParams.topMargin = 0;
                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setBackgroundColor(Color.GRAY);
                relativeLayout.addView(bodyLessonName, bodyLessonNameParams);
                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
            }


            //клетка в столбце класс
            TextView bodyClass = new TextView(this);
            {

                bodyClass.setTextSize(20);
                if (lessonAttitudeId == -1) {
                    bodyClass.setTextColor(Color.GRAY);
                    bodyClass.setBackgroundColor(Color.LTGRAY);
                } else {
                    bodyClass.setTextColor(Color.BLACK);
                    bodyClass.setBackgroundColor(Color.WHITE);
                }
                bodyClass.setText("  " + lessonClass + "  ");
                bodyClass.setGravity(Gravity.CENTER);
                //параметры TextView
                //параметры для клеток
                RelativeLayout.LayoutParams bodyClassParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
                bodyClassParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                bodyClassParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                bodyClassParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                bodyClassParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                bodyClassParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                bodyClassParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                bodyClassParams.rightMargin = 10;
                bodyClassParams.bottomMargin = 5;
                bodyClassParams.leftMargin = 0;
                bodyClassParams.topMargin = 0;
                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setBackgroundColor(Color.GRAY);
                relativeLayout.addView(bodyClass, bodyClassParams);
                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
            }


            //клетка в столбце кабинет
            TextView bodyCabinet = new TextView(this);
            {

                bodyCabinet.setTextSize(20);
                if (lessonAttitudeId == -1) {
                    bodyCabinet.setTextColor(Color.GRAY);
                    bodyCabinet.setBackgroundColor(Color.LTGRAY);
                } else {
                    bodyCabinet.setTextColor(Color.BLACK);
                    bodyCabinet.setBackgroundColor(Color.WHITE);
                }
                bodyCabinet.setText("  " + lessonCabinet + "  ");
                bodyCabinet.setGravity(Gravity.CENTER);
                //параметры TextView
                //параметры для клеток
                RelativeLayout.LayoutParams bodyCabinetParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, 190);
                bodyCabinetParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                bodyCabinetParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                bodyCabinetParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                bodyCabinetParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                bodyCabinetParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                bodyCabinetParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                bodyCabinetParams.rightMargin = 10;
                bodyCabinetParams.bottomMargin = 5;
                bodyCabinetParams.leftMargin = 0;
                bodyCabinetParams.topMargin = 0;
                //RelativeLayout в котором находится textView(нужен для создания границ в таблице)
                RelativeLayout relativeLayout = new RelativeLayout(this);
                relativeLayout.setBackgroundColor(Color.GRAY);
                relativeLayout.addView(bodyCabinet, bodyCabinetParams);
                tableRows[i].addView(relativeLayout, RelativeLayout.LayoutParams.WRAP_CONTENT, 200);
            }


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


        }
    }
}
