package com.learning.texnar13.teachersprogect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.yandex.mobile.ads.AdSize;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ScheduleMonthActivity extends AppCompatActivity {

    private GestureLibrary gestureLib;

    // обьект для преобразования календаря в строку
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // поле вывода календаря
    private LinearLayout calendarOut;
    // поле для названия
    TextView dateText;
    // поле для рекламы
    LinearLayout addOut;

    // размер одной ячейки календаря
    float cellSize;

    // названия месяцев из ресурсов
    private String[] monthsNames;

    // календарь с выбранной датой
    private GregorianCalendar viewCalendar;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // вертикальная ориентация
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        // задаем слой жестов
        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
        View inflate = getLayoutInflater().inflate(R.layout.activity_schedule_month, null);
        gestureOverlayView.addView(inflate);
        gestureOverlayView.setGestureColor(Color.TRANSPARENT);//делаем невидимым
        gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);
        // проверяем наличие библиотеки жестов
        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!gestureLib.load()) {
            finish();
            return;
        }
        // выводим слой жестов вместе с основной разметкой
        setContentView(gestureOverlayView);


        // получаем названия месяцев из ресурсов
        monthsNames = getResources().getStringArray(R.array.months_names);


        // при старте выставляем в основной календарь текущую дату
        viewCalendar = new GregorianCalendar();
        viewCalendar.setLenient(false);
        viewCalendar.setTime(new Date());


        // получаем поле вывода календаря
        calendarOut = findViewById(R.id.schedule_month_table);
        // получаем размер ячеек календаря
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);
        cellSize = metricsB.widthPixels / 7F;

        // получаем поле вывода заголовка
        dateText = findViewById(R.id.schedule_month_date_text);

        // получаем поле вывода рекламы
        addOut = findViewById(R.id.shedule_month_ad_banner_place);


        // определяем переключение месяцев жестами
        gestureOverlayView.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
                ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
                for (Prediction prediction : predictions) {
                    if (prediction.score > 1.0) {
                        switch (prediction.name) {
                            case "forward_swipe":
                                if ((viewCalendar.get(Calendar.MONTH)) == 11) {//месяц: 0 - 11
                                    viewCalendar.set(Calendar.MONTH, 0);
                                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) + 1);
                                } else {
                                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) + 1);
                                }
                                // по выбранной дате выводим месяц и заголовок
                                outMonth();
                                outCurrentData();
                                break;
                            case "back_swipe":
                                if ((viewCalendar.get(Calendar.MONTH)) == 0) {
                                    viewCalendar.set(Calendar.MONTH, 11);
                                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) - 1);
                                } else {
                                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) - 1);
                                }
                                // по выбранной дате выводим месяц и заголовок
                                outMonth();
                                outCurrentData();
                                break;
                        }
                    }
                }
            }
        });

        // нажатие на кнопку предыдущий месяц
        findViewById(R.id.schedule_month_button_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((viewCalendar.get(Calendar.MONTH)) == 0) {
                    viewCalendar.set(Calendar.MONTH, 11);
                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) - 1);
                } else {
                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) - 1);
                }
                // по выбранной дате выводим месяц и заголовок
                outMonth();
                outCurrentData();
            }
        });

        // нажатие на кнопку следующий месяц
        findViewById(R.id.schedule_month_button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((viewCalendar.get(Calendar.MONTH)) == 11) {
                    viewCalendar.set(Calendar.MONTH, 0);
                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) + 1);
                } else {
                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) + 1);
                }
                // по выбранной дате выводим месяц и заголовок
                outMonth();
                outCurrentData();
            }
        });

        //кнопка назад
        findViewById(R.id.schedule_month_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выходим из активности
                onBackPressed();
            }
        });


        // по выбранной дате выводим месяц и заголовок
        outMonth();
        outCurrentData();
    }

    // вывод текущей даты в заголовок
    void outCurrentData() {
        dateText.setText(
                monthsNames[viewCalendar.get(Calendar.MONTH)] +
                        " " + viewCalendar.get(Calendar.YEAR)
        );
    }

    // вывод текущего месяца в поле
    void outMonth() {
        calendarOut.removeAllViews();


        //получаем день недели с которого начинается месяц
        int dayOfWeek;
        viewCalendar.set(Calendar.DAY_OF_MONTH, 1);
        {
            dayOfWeek = viewCalendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == 1) {//2345671->0123456 нумерация
                dayOfWeek = 6;
            } else {
                dayOfWeek -= 2;
            }
        }
        // задаем количество дней в месяце
        int countOfDays = viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        // получаем текущую дату
        GregorianCalendar currTime = new GregorianCalendar();
        currTime.setTime(new Date());
        // получаем количество недель в месяце
        int countOfWeeks = 1 + (int) Math.ceil((viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) + dayOfWeek - 7) / 7F);


        //  создаем рекламу яндекса внизу календаря
        com.yandex.mobile.ads.AdView mAdView = new com.yandex.mobile.ads.AdView(this);
        addOut.removeAllViews();
        addOut.addView(mAdView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // выбираем размер рекламы
        if (countOfWeeks > 5) {
            mAdView.setBlockId(getResources().getString(R.string.banner_id_calendar));
            mAdView.setAdSize(AdSize.BANNER_320x50);
        } else {
            mAdView.setBlockId(getResources().getString(R.string.banner_id_calendar_big));
            mAdView.setAdSize(AdSize.BANNER_320x100);
        }
        // Создание объекта таргетирования рекламы.
        final com.yandex.mobile.ads.AdRequest adRequest = new com.yandex.mobile.ads.AdRequest.Builder().build();
        // Загрузка объявления.
        mAdView.loadAd(adRequest);


        // выставляем размеры календаря
        calendarOut.setLayoutParams(new LinearLayout.LayoutParams((int) (cellSize * 7), (int) (cellSize * (countOfWeeks + 1))));

        // создаем 7 tableRow и помещаем их в таблицу
        LinearLayout[] weekLinearRows = new LinearLayout[countOfWeeks + 1];
        //1 на шапку с днями недели
        weekLinearRows[0] = new LinearLayout(this);
        weekLinearRows[0].setGravity(LinearLayout.VERTICAL);
        weekLinearRows[0].setWeightSum(7f);
        calendarOut.addView(weekLinearRows[0],
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1.13f)
        );
        // и 6 на календарь
        for (int i = 1; i < weekLinearRows.length; i++) {
            weekLinearRows[i] = new LinearLayout(this);
            weekLinearRows[i].setGravity(LinearLayout.VERTICAL);
            weekLinearRows[i].setWeightSum(7f);
            calendarOut.addView(weekLinearRows[i],
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            );
        }

        // выводим в шапку дни недели
        String[] weekDaysNames = getResources().getStringArray(R.array.schedule_month_activity_week_days_short_array);
        for (int i = 0; i < 7; i++) {
            TextView day = new TextView(this);
            day.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
            day.setText(weekDaysNames[i]);
            day.setAllCaps(true);
            day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            day.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            day.setGravity(Gravity.CENTER);
            //day.setBackgroundColor(getResources().getColor(R.color.colorBackGround));//Color.LTGRAY"#e4ea7e""#fbffb9""#fdffdf"
            weekLinearRows[0].addView(
                    day,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            );
        }


        // выводим дни месяца
        int weekDay = 0;//счетчик дней недели
        int monthDay = 0;//счетчик дней месяца
        int weekOfMonth = 1;//счетчик недель
        boolean flag = false; //начат ли вывод дней месяца
        for (int i = 0; i < (countOfWeeks) * 7; i++) {


            //---текст---//на заднюю часть текста можно поставить drawable
            TextView day = new TextView(this);
            day.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
            day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
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

            LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            textContainerParams.gravity = Gravity.CENTER;
            textContainerParams.setMargins((int) pxFromDp(1.3f * getResources().getInteger(R.integer.desks_screen_multiplier)), (int) pxFromDp(1.3f * getResources().getInteger(R.integer.desks_screen_multiplier)), (int) pxFromDp(1.3f * getResources().getInteger(R.integer.desks_screen_multiplier)), (int) pxFromDp(1.3f * getResources().getInteger(R.integer.desks_screen_multiplier)));


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


            // выводим обычный день
            if (flag && monthDay != countOfDays) {
                // ставим в ячейку текущее число
                day.setText((monthDay + 1) + "");

                // меняем в отображаемом календаре число на текущее
                viewCalendar.set(Calendar.DAY_OF_MONTH, monthDay + 1);

                // формируем из календаря строку с датой
                final String date = dateFormat.format(viewCalendar.getTime());

                // получаем уроки в дне
                DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                Cursor lessonsAttitudes = db.getSubjectAndTimeCabinetAttitudesByDateAndLessonNumbersPeriod(
                        date,
                        0,
                        10 //todo получать количество из бд
                );

                // если в дне есть уроки, помечаем его
                if (lessonsAttitudes.getCount() != 0) {
                    day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                    day.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_bold));
                    day.setTextColor(Color.BLACK);//"#469500"
                }
                lessonsAttitudes.close();
                db.close();

                // при нажатии на день
                day.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ScheduleMonthActivity.this, ScheduleDayActivity.class);
                        intent.putExtra(ScheduleDayActivity.INTENT_DATE, date);
                        startActivity(intent);
                    }
                });

//----------выделяем текущую дату------------


                if (viewCalendar.get(Calendar.YEAR) == currTime.get(Calendar.YEAR) &&
                        viewCalendar.get(Calendar.MONTH) == currTime.get(Calendar.MONTH) &&
                        viewCalendar.get(Calendar.DAY_OF_MONTH) == currTime.get(Calendar.DATE)) {

                    LinearLayout.LayoutParams tempP = new LinearLayout.LayoutParams(
                            (int) (cellSize / 1.2F),
                            (int) (cellSize / 1.2F)
                    );
                    tempP.gravity = Gravity.CENTER;
                    day.setGravity(Gravity.CENTER);
                    day.setLayoutParams(tempP);

                    // добавляем круг текущий
                    day.setTextColor(getResources().getColor(R.color.backgroundWhite));
                    day.setBackgroundResource(R.drawable._button_round_background_orange);
                }
                monthDay++;
            }
//--------все в таблицу--------
            //текст в контейнер
            textContainer.addView(day);
            //контейнер в контейнер
            linearLayout.addView(textContainer, textContainerParams);//250
            //верхний контейнер в строку
            weekLinearRows[weekOfMonth].addView(linearLayout, linearLayoutParams);//260

            if (weekDay == 6) {
                weekOfMonth++;
                weekDay = 0;
            } else {
                weekDay++;
            }
        }

    }

    float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onResume() {
        outMonth();
        super.onResume();
    }
}
