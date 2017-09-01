package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScheduleMonthActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_month);
        final TableLayout tableLayout = (TableLayout) findViewById(R.id.schedule_month_table);

        Button previous = (Button) findViewById(R.id.schedule_month_button_previous);
        Button next = (Button) findViewById(R.id.schedule_month_button_next);
        final TextView dateText = (TextView) findViewById(R.id.schedule_month_date_text);

        final String months[] = {"Янв.", "Фев.", "Мар.", "Апр.", "Май", "Июнь", "Июль", "Авг.", "Сен.", "Окт.", "Ноя.", "Дек."};

        final Calendar currentCalendar = new GregorianCalendar();//календарь
        currentCalendar.setTime(new Date());//получаем текущую дату
        final Calendar changingCalendar = new GregorianCalendar();//календарь
        currentCalendar.setTime(new Date());//получаем текущую дату

        outMonth(currentCalendar, currentCalendar, tableLayout);
        dateText.setText(months[currentCalendar.get(Calendar.MONTH)] + " " + currentCalendar.get(Calendar.YEAR));

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((changingCalendar.get(Calendar.MONTH)) == 0) {
                    changingCalendar.set(Calendar.MONTH, 11);
                    changingCalendar.set(Calendar.YEAR, changingCalendar.get(Calendar.YEAR) - 1);
                } else {
                    changingCalendar.set(Calendar.MONTH, changingCalendar.get(Calendar.MONTH) - 1);
                }
                dateText.setText(months[changingCalendar.get(Calendar.MONTH)] + " " + changingCalendar.get(Calendar.YEAR));
                outMonth(changingCalendar, currentCalendar, tableLayout);
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
                dateText.setText(months[changingCalendar.get(Calendar.MONTH)] + " " + changingCalendar.get(Calendar.YEAR));
                outMonth(changingCalendar, currentCalendar, tableLayout);
            }
        });


    }

    //месяц: 0 - 11
    void outMonth(final Calendar viewCalendar, Calendar currentCalendar, TableLayout tableLayout) {
        //void outMonth(int year, int month, TableLayout tableLayout) {
        tableLayout.removeAllViews();

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
        int monthCapacity[] = {31, 28,//високосный 29
                31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int countOfDays = monthCapacity[viewCalendar.get(Calendar.MONTH)];
        //создаем tableRow и помещаем их в таблицу
        TableRow weekTableRows[] = new TableRow[7];
        for (int i = 0; i < weekTableRows.length; i++) {
            weekTableRows[i] = new TableRow(this);
            //weekTableRows[i].setGravity(Gravity.CENTER);
            weekTableRows[i].setBackgroundColor(Color.GRAY);
            tableLayout.addView(weekTableRows[i]);
        }
        //выводим колонку дни недели
        String week[] = {"Пн.", "Вт.", "Ср.", "Чт.", "Пт.", "Сб.", "Вс."};
        for (int i = 0; i < 7; i++) {
            TextView day = new TextView(this);
            day.setText(week[i]);
            day.setTextSize(20);
            day.setTextColor(Color.BLACK);
            day.setGravity(Gravity.CENTER);
            weekTableRows[i].addView(day);
        }
        //Log.i("TeachersApp", "" + (25/7));
//todo убрать одинаковые записи
        //выводим клетки
        int j = 0;//счетчик дней недели
        int n = 1;//счетчик дней месяца
        // первая неделя
        for (int i = 0; i < 7; i++) {
            if (i < dayOfWeek) {//до дня недели
                //пустая
                TextView day = new TextView(this);
                day.setTextSize(20);
                day.setTextColor(Color.BLACK);
                day.setBackgroundColor(Color.WHITE);
                day.setGravity(Gravity.CENTER);
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setGravity(Gravity.CENTER);
                linearLayout.addView(day, new LinearLayout.LayoutParams(250, 250));
                weekTableRows[j].addView(linearLayout, new TableRow.LayoutParams(260, 260));
            } else {//после
                //вывод n
                TextView day = new TextView(this);
                final int dayForIntent = n;
                day.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToScheduleDayActivity(dayForIntent, viewCalendar.get(Calendar.MONTH), viewCalendar.get(Calendar.YEAR));
                    }
                });
                day.setText(n + "");
                {
                    DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                    ArrayList<Long> lessonsAttitudesId = db.getLessonsAttitudesIdByTimePeriod(new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
                                    viewCalendar.get(Calendar.MONTH), n, 0, 0, 0),
                            new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
                                    viewCalendar.get(Calendar.MONTH), n, 23, 59, 59));
                    if (lessonsAttitudesId.size() == 0) {
                        day.setTextSize(20);
                    } else {
                        day.setTextSize(30);
                    }
                    day.setTextColor(Color.BLACK);
                }
                day.setTextColor(Color.BLACK);
                day.setBackgroundColor(Color.WHITE);
                day.setGravity(Gravity.CENTER);
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setGravity(Gravity.CENTER);
                //выделяем текущую дату
                if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                        n == currentCalendar.get(Calendar.DATE)) {
                    linearLayout.setBackgroundColor(Color.RED);
                    linearLayout.addView(day, new LinearLayout.LayoutParams(230, 230));
                } else {
                    linearLayout.addView(day, new LinearLayout.LayoutParams(250, 250));
                }
                weekTableRows[j].addView(linearLayout, new TableRow.LayoutParams(260, 260));
                n++;
            }
            if (j == 6) {
                j = 0;
            } else {
                j++;
            }
        }
        //расчитываем количество колонок не считая первую неделю
        int countOfWeeks;
        if ((countOfDays - (7 - dayOfWeek)) % 7 > 0) {
            countOfWeeks = ((countOfDays - (7 - dayOfWeek)) - ((countOfDays - (7 - dayOfWeek)) % 7)) / 7 + 1;
        } else {
            countOfWeeks = (countOfDays - (7 - dayOfWeek)) / 7;
        }
        //выводим оставшиеся дни
        for (int i = 0; i < countOfWeeks * 7; i++) {
            if (n > countOfDays) {//если уже напечатали все дни
                //пустая
                TextView day = new TextView(this);
                day.setTextSize(20);
                day.setTextColor(Color.BLACK);
                day.setBackgroundColor(Color.WHITE);
                day.setGravity(Gravity.CENTER);
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setGravity(Gravity.CENTER);
                linearLayout.addView(day, new LinearLayout.LayoutParams(250, 250));
                weekTableRows[j].addView(linearLayout, new TableRow.LayoutParams(260, 260));
            } else {
                //вывод n
                TextView day = new TextView(this);
                final int dayForIntent = n;
                day.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToScheduleDayActivity(dayForIntent, viewCalendar.get(Calendar.MONTH), viewCalendar.get(Calendar.YEAR));
                    }
                });
                day.setText(n + "");
                {
                    DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                    ArrayList<Long> lessonsAttitudesId = db.getLessonsAttitudesIdByTimePeriod(new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
                                    viewCalendar.get(Calendar.MONTH), n, 0, 0, 0),
                            new GregorianCalendar(viewCalendar.get(Calendar.YEAR),
                                    viewCalendar.get(Calendar.MONTH), n, 23, 59, 59));
                    if (lessonsAttitudesId.size() == 0) {
                        day.setTextSize(20);
                    } else {
                        day.setTextSize(30);
                    }
                    day.setTextColor(Color.BLACK);
                }
                day.setBackgroundColor(Color.WHITE);
                day.setGravity(Gravity.CENTER);
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setGravity(Gravity.CENTER);
                //выделяем текущую дату
                if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                        currentCalendar.get(Calendar.DATE) == n) {
                    linearLayout.setBackgroundColor(Color.RED);
                    linearLayout.addView(day, new LinearLayout.LayoutParams(230, 230));
                } else {
                    linearLayout.addView(day, new LinearLayout.LayoutParams(250, 250));
                }

                weekTableRows[j].addView(linearLayout, new TableRow.LayoutParams(260, 260));
                n++;
            }
            if (j == 6) {
                j = 0;
            } else {
                j++;
            }
        }
    }

    void goToScheduleDayActivity(int day, int month, int year) {
        Intent intent = new Intent(this, ScheduleDayActivity.class);
        intent.putExtra(ScheduleDayActivity.INTENT_DAY, day);
        intent.putExtra(ScheduleDayActivity.INTENT_MONTH, month);
        intent.putExtra(ScheduleDayActivity.INTENT_YEAR, year);
        this.startActivity(intent);
    }
}
