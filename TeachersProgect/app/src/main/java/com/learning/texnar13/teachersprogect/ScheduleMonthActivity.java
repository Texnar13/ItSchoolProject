package com.learning.texnar13.teachersprogect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.lessonRedactor.LessonRedactorActivity;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;
import com.yandex.mobile.ads.AdSize;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ScheduleMonthActivity extends AppCompatActivity {

    //private GestureLibrary gestureLib;

    // обьект для преобразования календаря в строку
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // поле для названия
    private TextView dateText;
    // поле вывода календаря
    private LinearLayout calendarOut;
    // поле для отображаемого дня
    private LinearLayout dayOut;


    // размер одной ячейки календаря
    private float cellSize;
    // названия месяцев из ресурсов
    private String[] monthsNames;


    // календарь с выбранной датой
    private static GregorianCalendar viewCalendar;
    // отображаемый день -1 / 1-31
    private static int chosenOutDayNumber = -1;
    // является ли нажатая ячейка текущей
    private static boolean isPressedCellCurrentDay;

    // ссылка на нажатую ячейку в календаре на месяц
    private TextView pressedCell;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // цвет статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
//        // задаем слой жестов
//        GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
//        View inflate = getLayoutInflater().inflate(R.layout.activity_schedule_month, null);
//        gestureOverlayView.addView(inflate);
//        gestureOverlayView.setGestureColor(Color.TRANSPARENT);//делаем невидимым
//        gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);
//        // проверяем наличие библиотеки жестов
//        gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
//        if (!gestureLib.load()) {
//            finish();
//            return;
//        }
//        // выводим слой жестов вместе с основной разметкой
//        setContentView(gestureOverlayView);
        // выводим разметку
        setContentView(R.layout.schedule_month_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar((Toolbar) findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(R.string.title_activity_schedule_month);


        // получаем поле вывода календаря
        calendarOut = findViewById(R.id.schedule_month_table);
        // получаем размер ячеек календаря
        if (getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels) {
            // горизонтальная ориентация
            cellSize = getResources().getDisplayMetrics().widthPixels / 14F;
        } else {
            // вертикальная ориентация
            cellSize = getResources().getDisplayMetrics().widthPixels / 7F;
        }
        // получаем поле вывода заголовка
        dateText = findViewById(R.id.schedule_month_date_text);
        // получаем поле вывода дня
        dayOut = findViewById(R.id.schedule_month_day_table);
        // получаем поле вывода рекламы
        LinearLayout adOut = findViewById(R.id.shedule_month_ad_banner_place);


        // получаем названия месяцев из ресурсов
        monthsNames = getResources().getStringArray(R.array.months_names);

//        // определяем переключение месяцев жестами
//        gestureOverlayView.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
//            @Override
//            public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
//                ArrayList<Prediction> predictions = gestureLib.recognize(gesture);
//                for (Prediction prediction : predictions) {
//                    if (prediction.score > 1.0) {
//                        switch (prediction.name) {
//                            case "forward_swipe":
//                                if ((viewCalendar.get(Calendar.MONTH)) == 11) {//месяц: 0 - 11
//                                    viewCalendar.set(Calendar.MONTH, 0);
//                                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) + 1);
//                                } else {
//                                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) + 1);
//                                }
//                                // по выбранной дате выводим месяц и заголовок
//                                outMonth();
//                                outCurrentData();
//                                break;
//                            case "back_swipe":
//                                if ((viewCalendar.get(Calendar.MONTH)) == 0) {
//                                    viewCalendar.set(Calendar.MONTH, 11);
//                                    viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) - 1);
//                                } else {
//                                    viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) - 1);
//                                }
//                                // по выбранной дате выводим месяц и заголовок
//                                outMonth();
//                                outCurrentData();
//                                break;
//                        }
//                    }
//                }
//            }
//        });

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
                chosenOutDayNumber = -1;
                outMonth();
                outDay();
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
                chosenOutDayNumber = -1;
                outMonth();
                outDay();
                outCurrentData();
            }
        });


        // создаем рекламу яндекса внизу календаря
        com.yandex.mobile.ads.AdView mAdView = new com.yandex.mobile.ads.AdView(this);
        adOut.removeAllViews();
        adOut.addView(mAdView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // выбираем размер рекламы
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// countOfWeeks > 5
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


        // при старте выставляем в основной календарь текущую дату
        if (viewCalendar == null) {
            viewCalendar = new GregorianCalendar();
            viewCalendar.setLenient(false);
            viewCalendar.setTime(new Date());

            // при старте выводим текущий день
            chosenOutDayNumber = viewCalendar.get(Calendar.DAY_OF_MONTH);
        }
        // по выбранной дате выводим месяц и заголовок
        outMonth();
        outCurrentData();
    }


    // вывод текущей даты в заголовок
    @SuppressLint("SetTextI18n")
    void outCurrentData() {
        dateText.setText(
                monthsNames[viewCalendar.get(Calendar.MONTH)] +
                        " " + viewCalendar.get(Calendar.YEAR)
        );
    }

    // вывод текущего месяца в поле
    @SuppressLint("SetTextI18n")
    void outMonth() {
        calendarOut.removeAllViews();

        // инициализируем поля
        // получаем день недели с которого начинается месяц
        int firstDayOfFirstWeek;
        viewCalendar.set(Calendar.DAY_OF_MONTH, 1);
        firstDayOfFirstWeek = viewCalendar.get(Calendar.DAY_OF_WEEK);
        //2345671->0123456 нумерация
        firstDayOfFirstWeek = (firstDayOfFirstWeek == 1) ? (6) : (firstDayOfFirstWeek - 2);
        // задаем количество дней в месяце
        int countOfDaysInMonth = viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        // получаем текущую дату
        GregorianCalendar currTime = new GregorianCalendar();
        currTime.setTime(new Date());
        // получаем количество недель в месяце
        int countOfWeeks = 1 + (int) Math.ceil((viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) + firstDayOfFirstWeek - 7) / 7F);

        // выставляем размеры linear-а календаря
        calendarOut.setLayoutParams(new LinearLayout.LayoutParams((int) (cellSize * 7), (int) (cellSize * (countOfWeeks + 1))));
        calendarOut.setWeightSum(countOfWeeks + 0.8f);
        // создаем 7 строк и помещаем их в контейнер
        LinearLayout[] weekLinearRows = new LinearLayout[countOfWeeks + 1];
        // 1 на шапку с днями недели (0.8) и 6 на календарь
        for (int i = 0; i < weekLinearRows.length; i++) {
            weekLinearRows[i] = new LinearLayout(this);
            weekLinearRows[i].setGravity(LinearLayout.VERTICAL);
            weekLinearRows[i].setWeightSum(7f);
            calendarOut.addView(weekLinearRows[i], new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    (i == 0) ? (0.8f) : (1f)
            ));
        }

        // выводим в шапку дни недели
        String[] weekDaysNames = getResources().getStringArray(R.array.schedule_month_activity_week_days_short_array);
        for (int i = 0; i < 7; i++) {
            TextView day = new TextView(this);
            //day.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
            day.setText(weekDaysNames[i]);
            day.setAllCaps(true);
            day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            day.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            day.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1f
            );
            dayParams.setMargins(
                    getResources().getDimensionPixelOffset(R.dimen.half_margin),
                    getResources().getDimensionPixelOffset(R.dimen.half_margin),
                    getResources().getDimensionPixelOffset(R.dimen.half_margin),
                    getResources().getDimensionPixelOffset(R.dimen.half_margin)
            );
            weekLinearRows[0].addView(day, dayParams);
        }

        // выводим дни месяца
        int monthDay = -firstDayOfFirstWeek;// счетчик дней месяца
        for (int weekOfMonthI = 0; weekOfMonthI < countOfWeeks; weekOfMonthI++) {
            for (int dayOfWeekI = 0; dayOfWeekI < 7; dayOfWeekI++) {

                // создаем текст дня
                final TextView day = new TextView(this);
                day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                day.setTextColor(Color.BLACK);
                day.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                dayParams.gravity = Gravity.CENTER;
                dayParams.setMargins(
                        getResources().getDimensionPixelOffset(R.dimen.half_margin),
                        getResources().getDimensionPixelOffset(R.dimen.half_margin),
                        getResources().getDimensionPixelOffset(R.dimen.half_margin),
                        getResources().getDimensionPixelOffset(R.dimen.half_margin)
                );
                weekLinearRows[weekOfMonthI + 1].addView(day, dayParams);

                // выводим обычный день (пропуская пустые клетки в начале и в конце)
                if (0 <= monthDay && monthDay < countOfDaysInMonth) {
                    // ставим в ячейку текущее число
                    day.setText(Integer.toString(monthDay + 1));

                    // проверяем, есть ли в этом дне уроки
                    DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                    // получаем уроки в дне
                    viewCalendar.set(Calendar.DAY_OF_MONTH, monthDay + 1);
                    Cursor lessonsAttitudes = db.getSubjectAndTimeCabinetAttitudesByDate(
                            dateFormat.format(viewCalendar.getTime())
                    );
                    // если в дне есть уроки, помечаем его
                    if (lessonsAttitudes.getCount() != 0) {
                        day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                        day.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_bold));
                        day.setTextColor(Color.BLACK);
                    }
                    lessonsAttitudes.close();
                    db.close();

                    // это текущая дата?
                    final boolean isItCurrentDay = viewCalendar.get(Calendar.YEAR) == currTime.get(Calendar.YEAR) &&
                            viewCalendar.get(Calendar.MONTH) == currTime.get(Calendar.MONTH) &&
                            viewCalendar.get(Calendar.DAY_OF_MONTH) == currTime.get(Calendar.DATE);
                    // выделяем текущую дату
                    if (isItCurrentDay)
                        day.setTextColor(getResources().getColor(R.color.baseOrange));


                    // если этот день стоял как выбранный
                    if (chosenOutDayNumber == monthDay + 1) {
                        // ставим view этой ячейки как выбранную
                        pressedCell = day;

                        // закрашиваем фон у новой ячейки
                        if (isItCurrentDay) {
                            isPressedCellCurrentDay = true;
                            day.setBackgroundResource(R.drawable.shedule_month_activity_background_chosen_cell_current_day);
                        } else {
                            isPressedCellCurrentDay = false;
                            day.setBackgroundResource(R.drawable.shedule_month_activity_background_chosen_cell);
                        }
                        day.setTextColor(getResources().getColor(R.color.backgroundWhite));
                    }


                    // при нажатии на день
                    final int dayNumber = monthDay + 1;
                    day.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // чистим фон у предыдущей ячейки
                            if (pressedCell != null) {
                                pressedCell.setBackground(null);
                                if (isPressedCellCurrentDay) {
                                    pressedCell.setTextColor(getResources().getColor(R.color.baseOrange));
                                } else {
                                    pressedCell.setTextColor(Color.BLACK);
                                }
                            }

                            // ставим эту ячейку выбранной
                            chosenOutDayNumber = dayNumber;
                            pressedCell = day;

                            // закрашиваем фон у новой ячейки
                            if (isItCurrentDay) {
                                isPressedCellCurrentDay = true;
                                day.setBackgroundResource(R.drawable.shedule_month_activity_background_chosen_cell_current_day);
                            } else {
                                isPressedCellCurrentDay = false;
                                day.setBackgroundResource(R.drawable.shedule_month_activity_background_chosen_cell);
                            }
                            day.setTextColor(getResources().getColor(R.color.backgroundWhite));

                            // и выводим по ней день
                            outDay();
                        }
                    });
                }
                // считаем день месяца
                monthDay++;
            }
        }


        // и выводим по нажатой или пустой ячейке день
        outDay();
    }


    /**
     * Вывод отображаемого дня в поле
     * выводимая дата (-1 / 0-30)
     */
    @SuppressLint("SetTextI18n")
    void outDay() {
        dayOut.removeAllViews();

        // вывод не пустого дня
        if (chosenOutDayNumber != -1) {

            // отображаемая дата в текстовом виде
            final String outStringDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    viewCalendar.get(Calendar.YEAR),
                    viewCalendar.get(Calendar.MONTH) + 1,
                    chosenOutDayNumber
            );


            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            // получаем стандартное время уроков
            int[][] standardLessonsPeriods = db.getSettingsTime(1);


            // текущий номер урока (-1 - не сегодня)
            int currentLesson = -1;
            // если просматриваем сегодняшний день
            if (dateFormat.format(new Date()).equals(outStringDate)) {

                // получаем текущее время
                GregorianCalendar nowCalendar = new GregorianCalendar();
                nowCalendar.setTime(new Date());
                int hour = nowCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = nowCalendar.get(Calendar.MINUTE);

                // определяем текущий урок
                for (int lessonI = 0; lessonI < standardLessonsPeriods.length; lessonI++) {
                    if ((hour > standardLessonsPeriods[lessonI][0] ||
                            (hour == standardLessonsPeriods[lessonI][0] && minute >= standardLessonsPeriods[lessonI][1])
                    ) && (hour < standardLessonsPeriods[lessonI][2] ||
                            (hour == standardLessonsPeriods[lessonI][2] && minute <= standardLessonsPeriods[lessonI][3])
                    )) currentLesson = lessonI;
                }
            }

            // пробегаемся по урокам
            for (int lessonI = 0; lessonI < standardLessonsPeriods.length; lessonI++) {
                final int finalLessonI = lessonI;

                // выводим общую пустую разметку урока
                // контейнер урока
                TableRow lessonContainer = new TableRow(this);
                lessonContainer.setGravity(Gravity.CENTER_VERTICAL);
                lessonContainer.setOrientation(LinearLayout.HORIZONTAL);
                dayOut.addView(lessonContainer, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                // текст номера урока
                TextView lessonNumberText = new TextView(this);
                lessonNumberText.setGravity(Gravity.CENTER);
                lessonNumberText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
                lessonNumberText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                lessonNumberText.setText(" " + (lessonI + 1) + ".");
                TableRow.LayoutParams lessonNumberTextParams = new TableRow.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lessonNumberTextParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                lessonNumberTextParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                lessonContainer.addView(lessonNumberText, lessonNumberTextParams);


                // контейнер времени
                LinearLayout timeContainer = new LinearLayout(this);
                timeContainer.setGravity(Gravity.CENTER);
                timeContainer.setOrientation(LinearLayout.VERTICAL);
                TableRow.LayoutParams timeContainerParams = new TableRow.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );
                timeContainerParams.setMargins(
                        0,
                        (int) getResources().getDimension(R.dimen.half_margin),
                        0,
                        (int) getResources().getDimension(R.dimen.half_margin)
                );
                lessonContainer.addView(timeContainer, timeContainerParams);

                // текст времени начала урока
                TextView lessonBeginTimeText = new TextView(this);
                lessonBeginTimeText.setGravity(Gravity.CENTER);
                lessonBeginTimeText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                lessonBeginTimeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                lessonBeginTimeText.setText(getTwoSymbols(standardLessonsPeriods[lessonI][0]) + ':' + getTwoSymbols(standardLessonsPeriods[lessonI][1]));
                timeContainer.addView(lessonBeginTimeText, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                // текст времени конца урока
                TextView lessonEndTimeText = new TextView(this);
                lessonEndTimeText.setGravity(Gravity.CENTER);
                lessonEndTimeText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                lessonEndTimeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                lessonEndTimeText.setText(getTwoSymbols(standardLessonsPeriods[lessonI][2]) + ':' + getTwoSymbols(standardLessonsPeriods[lessonI][3]));
                timeContainer.addView(lessonEndTimeText, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                // если урок текущй
                if (lessonI == 0) {
                    if (currentLesson == lessonI) {
                        lessonContainer.setBackgroundResource(R.color.baseOrange);
                        lessonNumberText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                        lessonBeginTimeText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                        lessonEndTimeText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                    } else {
                        lessonContainer.setBackgroundResource(R.color.backgroundWhite);
                        lessonNumberText.setTextColor(Color.BLACK);
                        lessonBeginTimeText.setTextColor(getResources().getColor(R.color.backgroundMediumGray));
                        lessonEndTimeText.setTextColor(getResources().getColor(R.color.backgroundMediumGray));
                    }
                } else {
                    if (currentLesson == lessonI) {
                        lessonContainer.setBackgroundResource(R.drawable.shedule_month_activity_background_lesson_active);
                        lessonNumberText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                        lessonBeginTimeText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                        lessonEndTimeText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                    } else {
                        lessonContainer.setBackgroundResource(R.drawable.shedule_month_activity_background_lesson_not_active);
                        lessonNumberText.setTextColor(Color.BLACK);
                        lessonBeginTimeText.setTextColor(getResources().getColor(R.color.backgroundMediumGray));
                        lessonEndTimeText.setTextColor(getResources().getColor(R.color.backgroundMediumGray));
                    }
                }

                // ищем в базе данных урок
                Cursor attitudeId = db.getSubjectAndTimeCabinetAttitudeByDateAndLessonNumber(outStringDate, lessonI);
                if (attitudeId.getCount() == 0) {// если не нашли зависимость урока
                    // при нажатиина контейнер
                    lessonContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // диалог создания урока

                            Intent intent = new Intent(ScheduleMonthActivity.this, LessonRedactorActivity.class);
                            intent.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, -1L);
                            intent.putExtra(LessonRedactorActivity.LESSON_CHECK_DATE, outStringDate);
                            intent.putExtra(LessonRedactorActivity.LESSON_NUMBER, finalLessonI);
                            startActivityForResult(intent, LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID);
                        }
                    });
                } else {// если нашли
                    attitudeId.moveToFirst();

                    // получаем id самого урока
                    final long lessonId = attitudeId.getLong(attitudeId.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID));
                    // получаем id предмета
                    long subjectId = attitudeId.getLong(attitudeId.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
                    // получаем id кабинета
                    final long cabinetId = attitudeId.getLong(attitudeId.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
                    attitudeId.close();

                    // получаем предмет
                    Cursor subjectCursor = db.getSubjectById(subjectId);
                    subjectCursor.moveToFirst();
                    // получаем имя предмета
                    String subjectName = subjectCursor.getString(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
                    // получаем id класса
                    final long learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
                    subjectCursor.close();

                    // получаем класс
                    Cursor classCursor = db.getLearnersClass(learnersClassId);
                    classCursor.moveToFirst();
                    // получаем имя класса
                    String learnersClassName = classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
                    classCursor.close();

                    // получаем кабинет
                    Cursor cabinetCursor = db.getCabinet(cabinetId);
                    cabinetCursor.moveToFirst();
                    // получаем имя кабинета
                    String cabinetName = cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
                    cabinetCursor.close();


                    // текст предмета
                    TextView subjectText = new TextView(this);
                    subjectText.setGravity(Gravity.CENTER);
                    subjectText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                    subjectText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                    subjectText.setText(subjectName);
                    TableRow.LayoutParams subjectTextParams = new TableRow.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    subjectTextParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                    subjectTextParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                    lessonContainer.addView(subjectText, subjectTextParams);


                    // текст класса
                    TextView classText = new TextView(this);
                    classText.setGravity(Gravity.CENTER);
                    classText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_medium));
                    classText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                    classText.setText(learnersClassName);
                    TableRow.LayoutParams classTextParams = new TableRow.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    classTextParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                    lessonContainer.addView(classText, classTextParams);


                    // текст кабинета
                    TextView cabinetText = new TextView(this);
                    cabinetText.setGravity(Gravity.CENTER);
                    cabinetText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
                    cabinetText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                    cabinetText.setText(cabinetName);
                    TableRow.LayoutParams cabinetTextParams = new TableRow.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    cabinetTextParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                    lessonContainer.addView(cabinetText, cabinetTextParams);

                    // если урок текущй
                    if (currentLesson == lessonI) {
                        subjectText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                        classText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                        cabinetText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                    } else {
                        subjectText.setTextColor(Color.BLACK);
                        classText.setTextColor(Color.BLACK);
                        cabinetText.setTextColor(Color.BLACK);
                    }

                    // при нажатиина контейнер
                    lessonContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                            //проверяем все ли ученики рассажены
                            if (db.getNotPutLearnersIdByCabinetIdAndClassId(cabinetId, learnersClassId).size() == 0) {
                                //начать урок
                                Intent intentForStartLesson = new Intent(getApplicationContext(), LessonActivity.class);
                                intentForStartLesson.putExtra(LessonActivity.LESSON_ATTITUDE_ID, lessonId);
                                intentForStartLesson.putExtra(LessonActivity.LESSON_DATE, outStringDate);
                                intentForStartLesson.putExtra(LessonActivity.LESSON_NUMBER, finalLessonI);
                                startActivity(intentForStartLesson);
                            } else {
                                // тост о не рассаженных учениках
                                Toast.makeText(getApplicationContext(), R.string.schedule_day_activity_toast_learners, Toast.LENGTH_LONG).show();
                                //редактировать рассадку
                                Intent intentForStartSeatingRedactor = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
                                intentForStartSeatingRedactor.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
                                intentForStartSeatingRedactor.putExtra(SeatingRedactorActivity.CLASS_ID, learnersClassId);
                                startActivity(intentForStartSeatingRedactor);
                            }
                            db.close();
                        }
                    });

                    // при долгом нажатиина контейнер
                    lessonContainer.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            // редактирование урока
                            Intent intent = new Intent(ScheduleMonthActivity.this, LessonRedactorActivity.class);
                            intent.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, lessonId);
                            intent.putExtra(LessonRedactorActivity.LESSON_CHECK_DATE, outStringDate);
                            intent.putExtra(LessonRedactorActivity.LESSON_NUMBER, finalLessonI);
                            startActivityForResult(intent, LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID);
                            return true;
                        }
                    });

                }
            }

            db.close();

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // обратная связь от редактора урока
        if (requestCode == LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID
                && resultCode == LessonRedactorActivity.LESSON_REDACTOR_RESULT_CODE_UPDATE) {
            outDay();
        }
    }

    // -- метод трансформации числа в текст с двумя позициями --
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0) {
            return '0' + Integer.toString(number);
        } else {
            return Integer.toString(number);
        }
    }

    // кнопка назад в actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // стираем данные
        chosenOutDayNumber = -1;
        viewCalendar = null;

    }


}
