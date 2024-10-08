package com.learning.texnar13.teachersprogect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.lessonRedactor.LessonRedactorActivity;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ScheduleMonthActivity extends AppCompatActivity {

    // todo разнеси это по фрагментам, читать невозможно


    // обьект для преобразования календаря в строку
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // поле для названия
    private TextView dateText;
    // поле вывода календаря
    private LinearLayout calendarOut;
    // поле для отображаемого дня
    private LinearLayout dayOut;


    // стандартное время уроков
    int[][] standardLessonsPeriods;
    int lessonsCount;

    // размер одной ячейки календаря
    private float cellSize;
    // названия месяцев из ресурсов
    private String[] monthsNames;
    private String[] monthsNamesWithEnding;


    // календарь с выбранной датой
    private static GregorianCalendar viewCalendar;
    // отображаемый день -1 / 1-31
    private static int chosenOutDayNumber = -1;
    // является ли нажатая ячейка текущей
    private static boolean isPressedCellCurrentDay;

    // ссылка на нажатую ячейку в календаре на месяц
    private TextView pressedCell;


    // получаем информацию о текущем языке
    int currentLanguageFileNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // цвет статус бара
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.base_background_color, getTheme()));

        // включен ли ночной режим
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (Configuration.UI_MODE_NIGHT_YES != currentNightMode)
            window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // получаем информацию о текущем языке
        currentLanguageFileNumber = getResources().getInteger(R.integer.current_locale_code);


        // выводим разметку
        setContentView(R.layout.schedule_month_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar(findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(R.string.title_activity_schedule_month);


        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // получаем стандартное время уроков
        standardLessonsPeriods = db.getSettingsTime(1);
        lessonsCount = standardLessonsPeriods.length;


        // получаем поле вывода календаря
        calendarOut = findViewById(R.id.schedule_month_table);
        // получаем размер ячеек календаря
        if (getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels) {
            // горизонтальная ориентация
            cellSize = getResources().getDisplayMetrics().widthPixels / 14F;
        } else {
            // вертикальная ориентация
            float screenWidth = getResources().getDisplayMetrics().widthPixels;
            // проверяем больше ли размер ячеек чем максимальная ширина
            float maxWidth = getResources().getDimension(R.dimen.tablet_max_content_width);
            if (maxWidth < screenWidth) {
                cellSize = maxWidth / 7F;
            } else {
                cellSize = screenWidth / 7F;
            }
        }
        cellSize -= getResources().getDimensionPixelOffset(R.dimen.shedule_month_calendar_margin);
        // получаем поле вывода заголовка
        dateText = findViewById(R.id.schedule_month_date_text);
        // получаем поле вывода дня
        dayOut = findViewById(R.id.schedule_month_day_table);
        // получаем поле вывода рекламы
        LinearLayout adOut = findViewById(R.id.shedule_month_ad_banner_place);


        // получаем названия месяцев из ресурсов
        monthsNames = getResources().getStringArray(R.array.months_names);
        monthsNamesWithEnding = getResources().getStringArray(R.array.months_names_with_ending);


        // нажатие на кнопку предыдущий месяц
        findViewById(R.id.schedule_month_button_previous).setOnClickListener(v -> {
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
        });

        // нажатие на кнопку следующий месяц
        findViewById(R.id.schedule_month_button_next).setOnClickListener(v -> {
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
        });


        // выводим рекламу
        // создаем рекламу яндекса внизу календаря
        BannerAdView mAdView = new BannerAdView(this);
        adOut.removeAllViews();
        adOut.addView(mAdView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // выбираем размер рекламы
        mAdView.setAdUnitId(getResources().getString(R.string.banner_id_calendar_and_lesson_redactor_big));
        mAdView.setAdSize(getAdSize(mAdView));
        // Создание объекта таргетирования рекламы и загрузка объявления.
        mAdView.loadAd(new AdRequest.Builder().build());


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

    @NonNull
    private BannerAdSize getAdSize(BannerAdView mAdView) {
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final int screenHeight = Math.round(displayMetrics.heightPixels / displayMetrics.density);
        // Calculate the width of the ad, taking into account the padding in the ad container.
        int adWidthPixels = mAdView.getWidth();
        if (adWidthPixels == 0) {
            // If the ad hasn't been laid out, default to the full screen width
            adWidthPixels = displayMetrics.widthPixels;
        }

        final int adWidth = Math.round(adWidthPixels / displayMetrics.density);
        final int maxAdHeight = screenHeight / 7;
        return BannerAdSize.inlineSize(this, adWidth, maxAdHeight);
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
        LinearLayout.LayoutParams calendarOutParams = new LinearLayout.LayoutParams((int) (cellSize * 7), (int) (cellSize * (countOfWeeks + 1)));
        calendarOutParams.gravity = Gravity.CENTER;
        calendarOutParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.forth_margin);
        calendarOut.setLayoutParams(calendarOutParams);
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
            //day.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_medium));
            day.setText(weekDaysNames[i]);
            day.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_semibold));

            // у китайцев текст не помещается
            day.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.shedule_month_calendar_day_text_size)
                            * ((currentLanguageFileNumber == 4) ? (0.8F) : (1.0F))
            );

            day.setTextColor((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                    (getResources().getColor(R.color.text_color_not_active, getTheme())) :
                    (getResources().getColor(R.color.text_color_not_active))
            );
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
                day.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.shedule_month_calendar_day_text_size));
                day.setPadding(0, 0, 0, 0);
                day.setTextColor(getResources().getColor(R.color.text_color_simple));
                day.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams dayParams = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                dayParams.gravity = Gravity.CENTER;
                dayParams.setMargins(
                        getResources().getDimensionPixelOffset(R.dimen.shedule_month_days_cells_free_space),
                        getResources().getDimensionPixelOffset(R.dimen.shedule_month_days_cells_free_space),
                        getResources().getDimensionPixelOffset(R.dimen.shedule_month_days_cells_free_space),
                        getResources().getDimensionPixelOffset(R.dimen.shedule_month_days_cells_free_space)
                );
                weekLinearRows[weekOfMonthI + 1].addView(day, dayParams);

                // этим условием пропускаем пустые клетки в начале и в конце и не ставим в них текст
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
                        day.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_black));
                    } else {
                        day.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_semibold));
                    }
                    lessonsAttitudes.close();
                    db.close();

                    // это текущая дата?
                    final boolean isItCurrentDay = viewCalendar.get(Calendar.YEAR) == currTime.get(Calendar.YEAR) &&
                            viewCalendar.get(Calendar.MONTH) == currTime.get(Calendar.MONTH) &&
                            viewCalendar.get(Calendar.DAY_OF_MONTH) == currTime.get(Calendar.DATE);
                    // выделяем текущую дату
                    if (isItCurrentDay)
                        day.setTextColor((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                                (getResources().getColor(R.color.base_orange, getTheme())) :
                                (getResources().getColor(R.color.base_orange))
                        );

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
                        day.setTextColor((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                                (getResources().getColor(R.color.text_color_inverse, getTheme())) :
                                (getResources().getColor(R.color.text_color_inverse))
                        );
                    }


                    // при нажатии на день
                    final int dayNumber = monthDay + 1;
                    day.setOnClickListener(v -> {
                        // чистим фон у предыдущей ячейки
                        if (pressedCell != null) {
                            pressedCell.setBackground(null);
                            if (isPressedCellCurrentDay) {
                                pressedCell.setTextColor((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                                        (getResources().getColor(R.color.base_orange, getTheme())) :
                                        (getResources().getColor(R.color.base_orange))
                                );
                            } else {
                                pressedCell.setTextColor(getResources().getColor(R.color.text_color_simple));
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
                        day.setTextColor((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                                (getResources().getColor(R.color.base_background_color, getTheme())) :
                                (getResources().getColor(R.color.base_background_color))
                        );
                        // и выводим по ней день
                        outDay();
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


            // выводим заголовок
            TextView head = new TextView(this);
            head.setBackgroundResource(R.drawable.base_background_dialog_head_round_blue);

            head.setTextColor((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                    (getResources().getColor(R.color.base_background_color, getTheme())) :
                    (getResources().getColor(R.color.base_background_color))
            );
            head.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_semibold));
            head.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.shedule_month_day_title_text_size));
            head.setGravity(Gravity.CENTER);
            head.setPadding(
                    getResources().getDimensionPixelOffset(R.dimen.double_margin),
                    getResources().getDimensionPixelOffset(R.dimen.double_margin),
                    getResources().getDimensionPixelOffset(R.dimen.double_margin),
                    getResources().getDimensionPixelOffset(R.dimen.double_margin)
            );
            dayOut.addView(head,
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


            DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // получаем текущий номер урока (-1 - не сегодня)
            int currentLesson = -1;
            // отображаемая дата в текстовом виде (календарь берём от месяца, а день меняем на chosenOutDayNumber)
            // todo поменять код так, чтобы сюда (outDay) передавалась строка с датой или готовый календарь,
            //  чтобы расписание дня не зависело от того что отображается в месяце
            final String outStringDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    viewCalendar.get(Calendar.YEAR),
                    viewCalendar.get(Calendar.MONTH) + 1,
                    chosenOutDayNumber
            );
            // если просматриваем сегодняшний день
            if (dateFormat.format(new Date()).equals(outStringDate)) {

                // получаем текущее время
                GregorianCalendar nowCalendar = new GregorianCalendar();
                nowCalendar.setTime(new Date());
                int hour = nowCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = nowCalendar.get(Calendar.MINUTE);

                // определяем текущий урок
                for (int lessonI = 0; lessonI < lessonsCount; lessonI++) {
                    if ((hour > standardLessonsPeriods[lessonI][0] ||
                            (hour == standardLessonsPeriods[lessonI][0] && minute >= standardLessonsPeriods[lessonI][1])
                    ) && (hour < standardLessonsPeriods[lessonI][2] ||
                            (hour == standardLessonsPeriods[lessonI][2] && minute <= standardLessonsPeriods[lessonI][3])
                    )) currentLesson = lessonI;
                }

                // выводим в заголовок слово сегодня
                head.setText(R.string.schedule_month_activity_current_day_title);
            } else {
                // иначе просто выставляем число в заголовок
                head.setText(chosenOutDayNumber + " " + monthsNamesWithEnding[viewCalendar.get(Calendar.MONTH)] + ':');
            }

            // пробегаемся по урокам
            for (int lessonI = 0; lessonI < lessonsCount; lessonI++) {
                final int finalLessonI = lessonI;

                // ищем в базе данных урок
                Cursor attitudeId = db.getSubjectAndTimeCabinetAttitudeByDateAndLessonNumber(outStringDate, lessonI);

                // раздуваем корневой view одного элемента
                View rootElement = getLayoutInflater().inflate(R.layout.schedule_month_lesson_pattern, null);
                dayOut.addView(rootElement, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                TextView lessonNumberText = rootElement.findViewById(R.id.schedule_month_lesson_pattern_number);
                TextView startTimeText = rootElement.findViewById(R.id.schedule_month_lesson_pattern_start_time);
                TextView endTimeText = rootElement.findViewById(R.id.schedule_month_lesson_pattern_end_time);
                TextView subjectText = rootElement.findViewById(R.id.schedule_month_lesson_pattern_subject);
                TextView classText = rootElement.findViewById(R.id.schedule_month_lesson_pattern_class);
                TextView cabinetText = rootElement.findViewById(R.id.schedule_month_lesson_pattern_cabinet);


                lessonNumberText.setText(((lessonI >= 9) ? ((lessonI + 1)) : (" " + (lessonI + 1))) + ".");
                startTimeText.setText(getTwoSymbols(standardLessonsPeriods[lessonI][0]) + ':' + getTwoSymbols(standardLessonsPeriods[lessonI][1]));
                endTimeText.setText(getTwoSymbols(standardLessonsPeriods[lessonI][2]) + ':' + getTwoSymbols(standardLessonsPeriods[lessonI][3]));

                // проставляем цвета
                int textColor;
                if (currentLesson == lessonI) {// если урок текущй
                    rootElement.setBackgroundResource(R.color.base_orange);
                    textColor = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                            (getResources().getColor(R.color.text_color_inverse, getTheme())) :
                            (getResources().getColor(R.color.text_color_inverse));
                } else {
                    if (lessonI == 0) {
                        rootElement.setBackgroundResource(R.color.base_background_color);
                    } else {
                        if (currentLesson == lessonI - 1) {
                            rootElement.setBackgroundResource(R.color.base_background_color);
                        } else {
                            rootElement.setBackgroundResource(R.drawable.shedule_month_activity_background_lesson_not_active);
                        }
                    }
                    // если не нашли зависимость урока
                    if (attitudeId.getCount() == 0) {
                        textColor = getResources().getColor(R.color.text_color_not_active);
                    } else {
                        textColor = getResources().getColor(R.color.text_color_simple);
                    }
                }
                lessonNumberText.setTextColor(textColor);
                startTimeText.setTextColor(textColor);
                endTimeText.setTextColor(textColor);
                subjectText.setTextColor(textColor);
                classText.setTextColor(textColor);
                cabinetText.setTextColor(textColor);


                // если не нашли зависимость урока
                if (attitudeId.getCount() == 0) {
                    // чистим поля
                    subjectText.setText("");
                    classText.setText("");
                    cabinetText.setText("");
                    // при нажатиина контейнер
                    rootElement.setOnClickListener(v -> {
                        // диалог создания урока
                        Intent intent = new Intent(ScheduleMonthActivity.this, LessonRedactorActivity.class);
                        intent.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, -1L);
                        intent.putExtra(LessonRedactorActivity.LESSON_CHECK_DATE, outStringDate);
                        intent.putExtra(LessonRedactorActivity.LESSON_NUMBER, finalLessonI);
                        startActivityForResult(intent, LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID);
                    });
                } else {// если нашли
                    attitudeId.moveToFirst();

                    // получаем id самого урока
                    final long lessonId = attitudeId.getLong(attitudeId.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID));
                    // получаем id предмета
                    long subjectId = attitudeId.getLong(attitudeId.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
                    // получаем id кабинета
                    final long cabinetId = attitudeId.getLong(attitudeId.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
                    attitudeId.close();

                    // получаем предмет
                    Cursor subjectCursor = db.getSubjectById(subjectId);
                    subjectCursor.moveToFirst();
                    // получаем имя предмета
                    String subjectName = subjectCursor.getString(subjectCursor.getColumnIndexOrThrow(SchoolContract.TableSubjects.COLUMN_NAME));
                    // получаем id класса
                    final long learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndexOrThrow(SchoolContract.TableSubjects.KEY_CLASS_ID));
                    subjectCursor.close();

                    // получаем класс
                    Cursor classCursor = db.getLearnersClases(learnersClassId);
                    classCursor.moveToFirst();
                    // получаем имя класса
                    String learnersClassName = classCursor.getString(classCursor.getColumnIndexOrThrow(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
                    classCursor.close();

                    // получаем кабинет
                    Cursor cabinetCursor = db.getCabinet(cabinetId);
                    cabinetCursor.moveToFirst();
                    // получаем имя кабинета
                    String cabinetName = cabinetCursor.getString(cabinetCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_NAME));
                    cabinetCursor.close();

                    // текст предмета
                    subjectText.setText(subjectName);
                    // текст класса
                    classText.setText(learnersClassName);
                    // текст кабинета
                    cabinetText.setText(cabinetName);


                    // при нажатиина контейнер
                    rootElement.setOnClickListener(v -> {
                        //проверяем все ли ученики рассажены
                        DataBaseOpenHelper db1 = new DataBaseOpenHelper(getApplicationContext());
                        if (db1.getNotPutLearnersIdByCabinetIdAndClassId(cabinetId, learnersClassId).size() != 0)
                            Toast.makeText(getApplicationContext(), R.string.schedule_month_activity_toast_learners, Toast.LENGTH_SHORT).show();
                        db1.close();
                        //начать урок
                        Intent intentForStartLesson = new Intent(getApplicationContext(), LessonActivity.class);
                        intentForStartLesson.putExtra(LessonActivity.ARGS_LESSON_ATTITUDE_ID, lessonId);
                        intentForStartLesson.putExtra(LessonActivity.ARGS_LESSON_DATE, outStringDate);
                        intentForStartLesson.putExtra(LessonActivity.ARGS_LESSON_NUMBER, finalLessonI);
                        startActivity(intentForStartLesson);
                    });

                    // при долгом нажатиина контейнер
                    rootElement.setOnLongClickListener(v -> {
                        // редактирование урока
                        Intent intent = new Intent(ScheduleMonthActivity.this, LessonRedactorActivity.class);
                        intent.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, lessonId);
                        intent.putExtra(LessonRedactorActivity.LESSON_CHECK_DATE, outStringDate);
                        intent.putExtra(LessonRedactorActivity.LESSON_NUMBER, finalLessonI);
                        startActivityForResult(intent, LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID);
                        return true;
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
            outMonth();// чтобы если менялись повторы вывести изменения в календарь
            outDay();
        }
    }

    // -- метод трансформации числа в текст с двумя позициями --
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0)
            return '0' + Integer.toString(number);
        return Integer.toString(number);
    }

    // кнопка назад в actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
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
