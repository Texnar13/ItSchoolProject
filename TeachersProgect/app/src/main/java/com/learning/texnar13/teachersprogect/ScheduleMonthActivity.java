package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ScheduleMonthActivity extends AppCompatActivity {

    private GestureLibrary gestureLib;

    LinearLayout linearLayout;
    GregorianCalendar changingCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.activity_schedule_month, null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.setGestureColor(Color.TRANSPARENT);//делаем невидимым
        gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);

        setTitle("КАЛЕНДАРЬ");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//вертикальная ориентация

        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gestureLib.load()) {
            finish();
            return;
        }
        setContentView(gestureOverlayView);
        //setContentView(R.layout.activity_schedule_month);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        linearLayout = (LinearLayout) findViewById(R.id.schedule_month_table);

        ImageView previous = (ImageView) findViewById(R.id.schedule_month_button_previous);
        ImageView next = (ImageView) findViewById(R.id.schedule_month_button_next);
        final TextView dateText = (TextView) findViewById(R.id.schedule_month_date_text);

        final String months[] = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль",
                "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        Date date = new Date();//получаем текущую дату
        final Calendar currentCalendar = new GregorianCalendar();//календарь
        currentCalendar.setTime(date);
        changingCalendar = new GregorianCalendar();//календарь
        changingCalendar.setTime(date);

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

//------размеры окна-------
        int rectangleSize = 0;
        {
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics metricsB = new DisplayMetrics();
            display.getMetrics(metricsB);

            rectangleSize = metricsB.widthPixels / 7;
        }

        linearOut.setLayoutParams(new LinearLayout.LayoutParams(rectangleSize * 7, rectangleSize * 7));

        LinearLayout weekLinearRows[] = new LinearLayout[7];
        for (int i = 0; i < weekLinearRows.length; i++) {
            weekLinearRows[i] = new LinearLayout(this);
            //weekTableRows[i].setWeight(1);
            weekLinearRows[i].setBackgroundColor(getResources().getColor(R.color.colorBackGround));
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
            day.setBackgroundColor(getResources().getColor(R.color.colorBackGround));//Color.LTGRAY"#e4ea7e""#fbffb9""#fdffdf"
            weekLinearRows[0].addView(
                    day,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            );
        }
//todo убрать одинаковые записи

        {
            int weekDay = 0;//счетчик дней недели
            int monthDay = 0;//счетчик дней месяца
            int weekOfMonth = 1;//счетчик недель
            boolean flag = false; //начат ли вывод дней месяца
            for (int i = 0; i < 6 * 7; i++) {


                //---текст---//на заднюю часть текста можно постоавить drawable
                TextView day = new TextView(this);
                day.setTextSize(20);
                day.setTextColor(Color.BLACK);
                day.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                dayParams.gravity = Gravity.CENTER;
                day.setLayoutParams(dayParams);


                //---контейнер в контейнере---ему можно поставить фон и еще им можно отредактировать размер границы клеток
                LinearLayout textContainer = new LinearLayout(this);
                textContainer.setGravity(Gravity.CENTER);
                textContainer.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
                if (weekDay == 5 || weekDay == 6) {
                    textContainer.setBackgroundColor(Color.parseColor("#ebffd6"));//"#fbffb9"#fdffdf
                } else {
                    textContainer.setBackgroundColor(getResources().getColor(R.color.colorBackGround));//"#fdffdf"parseColor()
                }

                LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                textContainerParams.gravity = Gravity.CENTER;
                textContainerParams.setMargins((int) pxFromDp(0.5f), (int) pxFromDp(1.3f), (int) pxFromDp(0.5f), (int) pxFromDp(1.3f));


                //---контейнер в таблице---  цвет границ клеток
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams linearLayoutParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1F
                        );
                linearLayoutParams.gravity = Gravity.CENTER;


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
                        ArrayList<Long> lessonsAttitudesId = db.getSubjectAndTimeCabinetAttitudesIdByTimePeriod(
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
                            day.setTextColor(getResources().getColor(R.color.colorPrimary));//"#469500"
                        }
                    }
//----------выделяем текущую дату------------
                    int currDay = currentCalendar.get(Calendar.DATE);
                    if (viewCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                            viewCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            dayForIntent == currDay) {

                        LinearLayout.LayoutParams tempP = new LinearLayout.LayoutParams(
                                (int) (rectangleSize / 1.2F),
                                (int) (rectangleSize / 1.2F)
                        );
                        tempP.gravity = Gravity.CENTER;
                        day.setGravity(Gravity.CENTER);
                        day.setLayoutParams(tempP);

                        //добавляем красный круг
                        day.setBackgroundResource(R.drawable.calendar_current_day_circle);
                        //dayParams.setMargins((int) pxFromDp(2.08f), (int) pxFromDp(2.08f), (int) pxFromDp(2.08f), (int) pxFromDp(2.08f));
                    }
                    monthDay++;
                }
//--------все в таблицу--------
                //текст в контейнер
                textContainer.addView(day);
                //контейнер в контейнер
                linearLayout.addView(textContainer, textContainerParams);//250
                //верхний контейнер в строку
                weekLinearRows[weekOfMonth].addView(linearLayout, linearLayoutParams
                );//260

                if (weekDay == 6) {
                    weekOfMonth++;
                    weekDay = 0;
                } else {
                    weekDay++;
                }
            }
        }
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

    @Override
    protected void onResume() {
        GregorianCalendar currTime = new GregorianCalendar();
        currTime.setTime(new Date());
        outMonth(changingCalendar, currTime, linearLayout);
        super.onResume();
    }
}
