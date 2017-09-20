package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScheduleMonthActivity extends AppCompatActivity {

    private GestureLibrary gestureLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.activity_schedule_month, null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.setGestureColor(Color.TRANSPARENT);//делаем невидимым
        gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);

        setTitle("Календарь");
        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gestureLib.load()) {
            finish();
            return;
        }
        setContentView(gestureOverlayView);
        //setContentView(R.layout.activity_schedule_month);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.schedule_month_table);

        ImageView previous = (ImageView) findViewById(R.id.schedule_month_button_previous);
        ImageView next = (ImageView) findViewById(R.id.schedule_month_button_next);
        final TextView dateText = (TextView) findViewById(R.id.schedule_month_date_text);

        final String months[] = {"Янв.", "Фев.", "Мар.", "Апр.", "Май", "Июнь", "Июль",
                "Авг.", "Сен.", "Окт.", "Ноя.", "Дек."};

        final Calendar currentCalendar = new GregorianCalendar();//календарь
        currentCalendar.setTime(new Date());//получаем текущую дату
        final Calendar changingCalendar = new GregorianCalendar();//календарь
        currentCalendar.setTime(new Date());//получаем текущую дату

        outMonth(currentCalendar, currentCalendar, linearLayout);
        dateText.setText(
                months[currentCalendar.get(Calendar.MONTH)] +
                        " " + currentCalendar.get(Calendar.YEAR)
        );

        //жесты
        gestureOverlayView.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
                ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
                for (Prediction prediction : predictions) {
                    if (prediction.score > 1.0) {
                        switch (prediction.name) {
                            case "forward_swipe":
                                if ((changingCalendar.get(Calendar.MONTH)) == 11) {
                                    changingCalendar.set(Calendar.MONTH, 0);
                                    changingCalendar.set(Calendar.YEAR, changingCalendar.get(Calendar.YEAR) + 1);
                                } else {
                                    changingCalendar.set(Calendar.MONTH, changingCalendar.get(Calendar.MONTH) + 1);
                                }
                                dateText.setText(
                                        months[changingCalendar.get(Calendar.MONTH)] +
                                                " " + changingCalendar.get(Calendar.YEAR)
                                );
                                outMonth(changingCalendar, currentCalendar, linearLayout);
                                break;
                            case "back_swipe":
                                if ((changingCalendar.get(Calendar.MONTH)) == 0) {
                                    changingCalendar.set(Calendar.MONTH, 11);
                                    changingCalendar.set(Calendar.YEAR, changingCalendar.get(Calendar.YEAR) - 1);
                                } else {
                                    changingCalendar.set(Calendar.MONTH, changingCalendar.get(Calendar.MONTH) - 1);
                                }
                                dateText.setText(
                                        months[changingCalendar.get(Calendar.MONTH)] +
                                                " " + changingCalendar.get(Calendar.YEAR)
                                );
                                outMonth(changingCalendar, currentCalendar, linearLayout);
                                break;
                        }
                        //Toast.makeText(getApplicationContext(), prediction.name, Toast.LENGTH_SHORT)
                               // .show();
                    }
                }
            }
        });


        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((changingCalendar.get(Calendar.MONTH)) == 0) {
                    changingCalendar.set(Calendar.MONTH, 11);
                    changingCalendar.set(Calendar.YEAR, changingCalendar.get(Calendar.YEAR) - 1);
                } else {
                    changingCalendar.set(Calendar.MONTH, changingCalendar.get(Calendar.MONTH) - 1);
                }
                dateText.setText(
                        months[changingCalendar.get(Calendar.MONTH)] +
                                " " + changingCalendar.get(Calendar.YEAR)
                );
                outMonth(changingCalendar, currentCalendar, linearLayout);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((changingCalendar.get(Calendar.MONTH)) == 11) {
                    changingCalendar.set(Calendar.MONTH, 0);
                    changingCalendar.set(Calendar.YEAR, changingCalendar.get(Calendar.YEAR) + 1);
                } else {
                    changingCalendar.set(Calendar.MONTH, changingCalendar.get(Calendar.MONTH) + 1);
                }
                dateText.setText(
                        months[changingCalendar.get(Calendar.MONTH)] +
                                " " + changingCalendar.get(Calendar.YEAR)
                );
                outMonth(changingCalendar, currentCalendar, linearLayout);
            }
        });


    }

    /*
4213-24213/com.learning.texnar13.teachersprogect I/DBOpenHelper: getLessonsAttitudesIdByTimePeriod periodStart=1627333200000 periodEnd=1627419599000 answer=
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect I/DBOpenHelper: getLessonsAttitudesIdByTimePeriod periodStart=1627419600000 periodEnd=1627505999000 answer=
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect W/SQLiteLog: (28) failed to open "/data/user/0/com.learning.texnar13.teachersprogect/databases/school-journal" with flag (131072) and mode_t (1b0) due to error (24)
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect E/SQLiteLog: (14) cannot open file at line 31517 of [2ef4f3a5b1]
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect E/SQLiteLog: (14) os_unix.c:31517: (24) open(/data/user/0/com.learning.texnar13.teachersprogect/databases/school-journal) -
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect W/SQLiteLog: (28) failed to open "/data/user/0/com.learning.texnar13.teachersprogect/databases/school-journal" with flag (131074) and mode_t (1b0) due to error (24)
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect W/SQLiteLog: (28) failed to open "/data/user/0/com.learning.texnar13.teachersprogect/databases/school-journal" with flag (131072) and mode_t (1b0) due to error (24)
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect E/SQLiteLog: (14) cannot open file at line 31517 of [2ef4f3a5b1]
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect E/SQLiteLog: (14) os_unix.c:31517: (24) open(/data/user/0/com.learning.texnar13.teachersprogect/databases/school-journal) -
09-03 17:17:49.864 24213-24213/com.learning.texnar13.teachersprogect E/SQLiteLog: (2062) statement aborts at 15: [SELECT * FROM lessonsAnd WHERE lessonDateBegin >= ? AND lessonDateBegin <= ?] unable to open database file
09-03 17:17:49.874 24213-24213/com.learning.texnar13.teachersprogect E/SQLiteQuery: exception: unable to open database file (code 2062)
                                                                                    #################################################################
                                                                                    Error Code : 2062 (SQLITE_CANTOPEN_EMFILE)
                                                                                    Caused By : Application has opened two many files. Maximum of available file descriptors in one process is 1024 in default.
                                                                                    	(unable to open database file (code 2062))
                                                                                    #################################################################; query: SELECT * FROM lessonsAnd WHERE lessonDateBegin >= ? AND lessonDateBegin <= ?
09-03 17:17:49.884 24213-24213/com.learning.texnar13.teachersprogect E/AndroidRuntime: FATAL EXCEPTION: main
                                                                                       Process: com.learning.texnar13.teachersprogect, PID: 24213
                                                                                       android.database.sqlite.SQLiteCantOpenDatabaseException: unable to open database file (code 2062)
                                                                                       #################################################################
                                                                                       Error Code : 2062 (SQLITE_CANTOPEN_EMFILE)
                                                                                       Caused By : Application has opened two many files. Maximum of available file descriptors in one process is 1024 in default.
                                                                                       	(unable to open database file (code 2062))
                                                                                       #################################################################
                                                                                           at android.database.sqlite.SQLiteConnection.nativeExecuteForCursorWindow(Native Method)
                                                                                           at android.database.sqlite.SQLiteConnection.executeForCursorWindow(SQLiteConnection.java:980)
                                                                                           at android.database.sqlite.SQLiteSession.executeForCursorWindow(SQLiteSession.java:836)
                                                                                           at android.database.sqlite.SQLiteQuery.fillWindow(SQLiteQuery.java:62)
                                                                                           at android.database.sqlite.SQLiteCursor.fillWindow(SQLiteCursor.java:143)
                                                                                           at android.database.sqlite.SQLiteCursor.getCount(SQLiteCursor.java:132)
                                                                                           at android.database.AbstractCursor.moveToPosition(AbstractCursor.java:219)
                                                                                           at android.database.AbstractCursor.moveToNext(AbstractCursor.java:268)
                                                                                           at com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper.getLessonsAttitudesIdByTimePeriod(DataBaseOpenHelper.java:603)
                                                                                           at com.learning.texnar13.teachersprogect.ScheduleMonthActivity.outMonth(ScheduleMonthActivity.java:212)
                                                                                           at com.learning.texnar13.teachersprogect.ScheduleMonthActivity$2.onClick(ScheduleMonthActivity.java:68)
                                                                                           at android.view.View.performClick(View.java:5702)
                                                                                           at android.widget.TextView.performClick(TextView.java:10887)
                                                                                           at android.view.View$PerformClick.run(View.java:22546)
                                                                                           at android.os.Handler.handleCallback(Handler.java:739)
                                                                                           at android.os.Handler.dispatchMessage(Handler.java:95)
                                                                                           at android.os.Looper.loop(Looper.java:158)
                                                                                           at android.app.ActivityThread.main(ActivityThread.java:7237)
                                                                                           at java.lang.reflect.Method.invoke(Native Method)
                                                                                           at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1230)
                                                                                           at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1120)
*/

    //месяц: 0 - 11
    void outMonth(final Calendar viewCalendar, Calendar currentCalendar, LinearLayout linearOut) {
        //на вход отображаемая дата, сегодняшний день, окно вывода
        linearOut.removeAllViews();

        //получаем день недели с которого начинается месяц
        int dayOfWeek;
        {
            Calendar calendar = new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
                    viewCalendar.get(Calendar.MONTH), 1);
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == 1) {//2345671->0123456 нумерация
                dayOfWeek = 6;
            } else {
                dayOfWeek = dayOfWeek - 2;
            }
        }
        //задаем количество дней в месяце
        int monthCapacity[];
        if (viewCalendar.get(Calendar.YEAR) % 4 == 0 &&
                viewCalendar.get(Calendar.YEAR) % 100 != 0 ||
                viewCalendar.get(Calendar.YEAR) % 400 == 0) {
            monthCapacity = new int[]{31, 29,//високосный
                    31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        } else {
            monthCapacity = new int[]{31, 28,//не високосный
                    31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        }
        int countOfDays = monthCapacity[viewCalendar.get(Calendar.MONTH)];
        //создаем 7 tableRow и помещаем их в таблицу (1 на день недели и 6 на календарь)
        //linearOut.setWeightSum(1f);
        LinearLayout weekLinearRows[] = new LinearLayout[7];
        for (int i = 0; i < weekLinearRows.length; i++) {
            weekLinearRows[i] = new LinearLayout(this);
            //weekTableRows[i].setWeight(1);
            weekLinearRows[i].setBackgroundColor(Color.GRAY);
            weekLinearRows[i].setGravity(LinearLayout.VERTICAL);
            weekLinearRows[i].setWeightSum(7f);
            if (i == 0) {
                linearOut.addView(
                        weekLinearRows[i],
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1.13f)
                );
            } else {
                linearOut.addView(
                        weekLinearRows[i],
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                );
            }
        }

        //выводим ряд дни недели
        String week[] = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};
        for (int i = 0; i < 7; i++) {
            TextView day = new TextView(this);
            day.setText(week[i]);
            day.setTextSize(15);
            day.setTextColor(Color.BLACK);
            day.setGravity(Gravity.CENTER);
            day.setBackgroundColor(Color.parseColor("#e4ea7e"));
            weekLinearRows[0].addView(
                    day,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            );
        }
        //Log.i("TeachersApp", "" + (25/7));
//todo убрать одинаковые записи

        {
            int weekDay = 0;//счетчик дней недели
            int monthDay = 0;//счетчик дней месяца
            int weekOfMonth = 1;//счетчик недель
            boolean flag = false; //начат ли вывод дней месяца
            for (int i = 0; i < 6 * 7; i++) {

                TextView day = new TextView(this);
                day.setTextSize(20);
                day.setTextColor(Color.BLACK);
                if (weekDay == 5 || weekDay == 6) {
                    day.setBackgroundColor(Color.parseColor("#fbffb9"));
                } else
                    //day.setBackgroundColor(Color.WHITE);
                    day.setBackgroundColor(Color.parseColor("#fdffdf"));
                day.setGravity(Gravity.CENTER);
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                dayParams.setMargins((int) pxFromDp(0.25f), (int) pxFromDp(1.3f), (int) pxFromDp(0.25f), (int) pxFromDp(1.3f));

                if (weekDay == dayOfWeek) {
                    flag = true;
                }
//                if (!flag) {
//                    //пустая до календаря
//                }
//
//                if (monthDay == countOfDays) {
//                    //пустая после календаря
//                }
                if (flag && monthDay != countOfDays) {
                    //вывод дня
                    final int dayForIntent = monthDay + 1;
                    day.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            goToScheduleDayActivity(
                                    dayForIntent,
                                    viewCalendar.get(Calendar.MONTH),
                                    viewCalendar.get(Calendar.YEAR)
                            );
                        }
                    });
                    day.setText(dayForIntent + "");
                    {//проверка наличия уроков
                        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                        ArrayList<Long> lessonsAttitudesId = db.getLessonsAttitudesIdByTimePeriod(
                                new GregorianCalendar(
                                        viewCalendar.get(Calendar.YEAR),
                                        viewCalendar.get(Calendar.MONTH), dayForIntent, 0, 0, 0),
                                new GregorianCalendar(
                                        viewCalendar.get(Calendar.YEAR),
                                        viewCalendar.get(Calendar.MONTH), dayForIntent, 23, 59, 59)
                        );
                        db.close();
                        if (lessonsAttitudesId.size() != 0) {
                            day.setTextSize(30);
                            day.setTextColor(Color.parseColor("#469500"));
                        }
                    }
                    //выделяем текущую дату
                    int currDay = currentCalendar.get(Calendar.DATE);
                    if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            viewCalendar.get(Calendar.MONTH) ==
                                    currentCalendar.get(Calendar.MONTH) &&
                            dayForIntent == currDay) {
                        linearLayout.setBackgroundColor(Color.RED);
                        dayParams.setMargins((int) pxFromDp(2.08f), (int) pxFromDp(2.08f), (int) pxFromDp(2.08f), (int) pxFromDp(2.08f));
                    }
                    monthDay++;
                }

                linearLayout.addView(day, dayParams);//250
                weekLinearRows[weekOfMonth].addView(linearLayout,
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT, 1f));//260

                if (weekDay == 6) {
                    weekOfMonth++;
                    weekDay = 0;
                } else {
                    weekDay++;
                }
            }
        }
//        //выводим клетки
//        int j = 0;//счетчик дней недели
//        int n = 1;//счетчик дней месяца
//        // первая неделя
//        for (int i = 0; i < 7; i++) {
//            if (i < dayOfWeek) {//до дня недели
//                //пустая
//                TextView day = new TextView(this);
//                day.setTextSize(20);
//                day.setTextColor(Color.BLACK);
//                day.setBackgroundColor(Color.WHITE);
//                day.setGravity(Gravity.CENTER);
//                LinearLayout linearLayout = new LinearLayout(this);
//                linearLayout.setGravity(Gravity.CENTER);
//                linearLayout.addView(day, new LinearLayout.LayoutParams(250, 250));
//                weekTableRows[j].addView(linearLayout, new TableRow.LayoutParams(260, 260));
//            } else {//после
//                //вывод n
//                TextView day = new TextView(this);
//                final int dayForIntent = n;
//                day.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        goToScheduleDayActivity(dayForIntent, viewCalendar.get(Calendar.MONTH), viewCalendar.get(Calendar.YEAR));
//                    }
//                });
//                day.setText(n + "");
//                {
//                    DataBaseOpenHelper db = new DataBaseOpenHelper(this);
//                    ArrayList<Long> lessonsAttitudesId = db.getLessonsAttitudesIdByTimePeriod(new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
//                                    viewCalendar.get(Calendar.MONTH), n, 0, 0, 0),
//                            new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
//                                    viewCalendar.get(Calendar.MONTH), n, 23, 59, 59));
//                    if (lessonsAttitudesId.size() == 0) {
//                        day.setTextSize(20);
//                    } else {
//                        day.setTextSize(30);
//                    }
//                    day.setTextColor(Color.BLACK);
//                }
//                day.setTextColor(Color.BLACK);
//                day.setBackgroundColor(Color.WHITE);
//                day.setGravity(Gravity.CENTER);
//                LinearLayout linearLayout = new LinearLayout(this);
//                linearLayout.setGravity(Gravity.CENTER);
//                //выделяем текущую дату
//                if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
//                        viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
//                        n == currentCalendar.get(Calendar.DATE)) {
//                    linearLayout.setBackgroundColor(Color.RED);
//                    linearLayout.addView(day, new LinearLayout.LayoutParams(230, 230));
//                } else {
//                    linearLayout.addView(day, new LinearLayout.LayoutParams(250, 250));
//                }
//                weekTableRows[j].addView(linearLayout, new TableRow.LayoutParams(260, 260));
//                n++;
//            }
//            if (j == 6) {
//                j = 0;
//            } else {
//                j++;
//            }
//        }
//        //расчитываем количество колонок не считая первую неделю
//        int countOfWeeks;
//        if ((countOfDays - (7 - dayOfWeek)) % 7 > 0) {
//            countOfWeeks = ((countOfDays - (7 - dayOfWeek)) - ((countOfDays - (7 - dayOfWeek)) % 7)) / 7 + 1;
//        } else {
//            countOfWeeks = (countOfDays - (7 - dayOfWeek)) / 7;
//        }
//        //выводим оставшиеся дни
//        for (int i = 0; i < countOfWeeks * 7; i++) {
//            if (n > countOfDays) {//если уже напечатали все дни
//                //пустая
//                TextView day = new TextView(this);
//                day.setTextSize(20);
//                day.setTextColor(Color.BLACK);
//                day.setBackgroundColor(Color.WHITE);
//                day.setGravity(Gravity.CENTER);
//                LinearLayout linearLayout = new LinearLayout(this);
//                linearLayout.setGravity(Gravity.CENTER);
//                linearLayout.addView(day, new LinearLayout.LayoutParams(250, 250));
//                weekTableRows[j].addView(linearLayout, new TableRow.LayoutParams(260, 260));
//            } else {
//                //вывод n
//                TextView day = new TextView(this);
//                final int dayForIntent = n;
//                day.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        goToScheduleDayActivity(dayForIntent, viewCalendar.get(Calendar.MONTH), viewCalendar.get(Calendar.YEAR));
//                    }
//                });
//                day.setText(n + "");
//                {
//                    DataBaseOpenHelper db = new DataBaseOpenHelper(this);
//                    ArrayList<Long> lessonsAttitudesId = db.getLessonsAttitudesIdByTimePeriod(new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
//                                    viewCalendar.get(Calendar.MONTH), n, 0, 0, 0),
//                            new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
//                                    viewCalendar.get(Calendar.MONTH), n, 23, 59, 59));
//                    if (lessonsAttitudesId.size() == 0) {
//                        day.setTextSize(20);
//                    } else {
//                        day.setTextSize(30);
//                    }
//                    day.setTextColor(Color.BLACK);
//                }
//                day.setBackgroundColor(Color.WHITE);
//                day.setGravity(Gravity.CENTER);
//                LinearLayout linearLayout = new LinearLayout(this);
//                linearLayout.setGravity(Gravity.CENTER);
//                //выделяем текущую дату
//                if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
//                        viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
//                        currentCalendar.get(Calendar.DATE) == n) {
//                    linearLayout.setBackgroundColor(Color.RED);
//                    linearLayout.addView(day, new LinearLayout.LayoutParams(230, 230));
//                } else {
//                    linearLayout.addView(day, new LinearLayout.LayoutParams(250, 250));
//                }
//
//                weekTableRows[j].addView(linearLayout, new TableRow.LayoutParams(260, 260));
//                n++;
//            }
//            if (j == 6) {
//                j = 0;
//            } else {
//                j++;
//            }
//        }
    }

    float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    void goToScheduleDayActivity(int day, int month, int year) {
        Intent intent = new Intent(this, ScheduleDayActivity.class);
        intent.putExtra(ScheduleDayActivity.INTENT_DAY, day);
        intent.putExtra(ScheduleDayActivity.INTENT_MONTH, month);
        intent.putExtra(ScheduleDayActivity.INTENT_YEAR, year);
        this.startActivity(intent);
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
