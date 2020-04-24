package com.learning.texnar13.teachersprogect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

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

public class ScheduleDayActivity extends AppCompatActivity {

    //    public static final String INTENT_DAY = "day";
//    public static final String INTENT_MONTH = "month";
//    public static final String INTENT_YEAR = "year";
    public static final String INTENT_DATE = "intentDate";

    // переданные данные
    String lessonDate;

    // текущий номер урока (-1 - не сегодня)
    int currentLesson = -1;

    // парсинг дат из календаря
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    // главный контенйнер уроков
    TableLayout outLayout;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // вертикальная ориентация
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // цвета статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // ====== ====== ====== получение данных ====== ====== ======
        // получаем данные из intent
        lessonDate = getIntent().getStringExtra(INTENT_DATE);
        if (lessonDate == null) {
            finish();
        }


        // ====== ====== ====== создаем разметку ====== ====== ======

        // ставим разметку
        setContentView(R.layout.activity_schedule_day);

        // кнопка назад
        findViewById(R.id.schedule_day_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // заголовок
        ((TextView) findViewById(R.id.schedule_day_title)).setText(Integer.parseInt(lessonDate.substring(8, 10)) + " " +
                getResources().getStringArray(R.array.months_names_low_case)[Integer.parseInt(lessonDate.substring(5, 7))-1] + " " +
                Integer.parseInt(lessonDate.substring(0, 4)));


        // выводим поля в таблицу
        // скролящийся контейнер таблицы
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(getResources().getColor(R.color.backgroundWhite));
        ((LinearLayout) findViewById(R.id.schedule_day_body_container)).addView(
                scrollView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        // главный контенйнер уроков
        outLayout = new TableLayout(this);
        //outLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(outLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    @Override
    protected void onStart() {
        super.onStart();

        outLayout.removeAllViews();


        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // получаем стандартное время уроков
        int[][] standartLessonsPeriods = db.getSettingsTime(1);


        // если просматриваем сегодняшний день
        if (dateFormat.format(new Date()).equals(lessonDate)) {

            GregorianCalendar now = new GregorianCalendar();
            now.setTime(new Date());
            // определяем текущий урок
            for (int lessonI = 0; lessonI < standartLessonsPeriods.length; lessonI++) {
                if ((now.get(Calendar.HOUR_OF_DAY) > standartLessonsPeriods[lessonI][0] ||
                        (now.get(Calendar.HOUR_OF_DAY) == standartLessonsPeriods[lessonI][0] && now.get(Calendar.MINUTE) >= standartLessonsPeriods[lessonI][1])
                ) &&
                        (now.get(Calendar.HOUR_OF_DAY) < standartLessonsPeriods[lessonI][2] ||
                                (now.get(Calendar.HOUR_OF_DAY) == standartLessonsPeriods[lessonI][2] && now.get(Calendar.MINUTE) <= standartLessonsPeriods[lessonI][3])
                        )
                ) {
                    currentLesson = lessonI;
                }
            }
        }


        // пробегаемся по урокам
        for (int lessonI = 0; lessonI < standartLessonsPeriods.length; lessonI++) {
            final int finalLessonI = lessonI;

            // выводим общую пустую разметку урока
            // контейнер урока
            TableRow lessonContainer = new TableRow(this);
            lessonContainer.setGravity(Gravity.CENTER_VERTICAL);
            lessonContainer.setOrientation(LinearLayout.HORIZONTAL);
            outLayout.addView(lessonContainer, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
            lessonBeginTimeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            lessonBeginTimeText.setText(getTwoSymbols(standartLessonsPeriods[lessonI][0]) + ':' + getTwoSymbols(standartLessonsPeriods[lessonI][1]));
            timeContainer.addView(lessonBeginTimeText, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            // текст времени конца урока
            TextView lessonEndTimeText = new TextView(this);
            lessonEndTimeText.setGravity(Gravity.CENTER);
            lessonEndTimeText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
            lessonEndTimeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            lessonEndTimeText.setText(getTwoSymbols(standartLessonsPeriods[lessonI][2]) + ':' + getTwoSymbols(standartLessonsPeriods[lessonI][3]));
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
                    lessonContainer.setBackgroundResource(R.drawable.__current_lesson_background_uplined_orange);
                    lessonNumberText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                    lessonBeginTimeText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                    lessonEndTimeText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                } else {
                    lessonContainer.setBackgroundResource(R.drawable.__current_lesson_background_uplined_white);
                    lessonNumberText.setTextColor(Color.BLACK);
                    lessonBeginTimeText.setTextColor(getResources().getColor(R.color.backgroundMediumGray));
                    lessonEndTimeText.setTextColor(getResources().getColor(R.color.backgroundMediumGray));
                }
            }

            // ищем в базе данных урок
            Cursor attitudeId = db.getSubjectAndTimeCabinetAttitudeByDateAndLessonNumber(lessonDate, lessonI);
            if (attitudeId.getCount() == 0) {// если не нашли зависимость урока
                // при нажатиина контейнер
                lessonContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //создание/редактирование урока
                        Intent intentForLessonEditor = new Intent(getApplicationContext(), LessonRedactorActivity.class);
                        intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, -1L);
                        intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_DATE, lessonDate);
                        intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_NUMBER, finalLessonI);
                        startActivity(intentForLessonEditor);
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

                // укорачиваем поля если они слишком длинные Loading…
                if (subjectName.length() > 10) {
                    subjectName = subjectName.substring(0, 9) + "…";
                }
                if (learnersClassName.length() > 5) {
                    learnersClassName = learnersClassName.substring(0, 4) + "…";// absde -> abc…  abcd->abcd
                }
                if (cabinetName.length() > 5) {
                    cabinetName = cabinetName.substring(0, 4) + "…";
                }


                // дополнительно выводим разметку сведений урока

//                LinearLayout contentContainer = new LinearLayout(this);
//                contentContainer.setBackgroundColor(Color.MAGENTA);
//                TableRow.LayoutParams contentContainerParams = new TableRow.LayoutParams(
//                        TableRow.LayoutParams.MATCH_PARENT,
//                        100
//                );
//                lessonContainer.addView(contentContainer, contentContainerParams);



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
                            //парсим дату
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            //начать урок
                            Intent intentForStartLesson = new Intent(getApplicationContext(), LessonActivity.class);
                            intentForStartLesson.putExtra(LessonActivity.LESSON_ATTITUDE_ID, lessonId);
                            intentForStartLesson.putExtra(LessonActivity.LESSON_DATE, lessonDate);
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
                        //создание/редактирование урока
                        final Intent intentForLessonEditor = new Intent(getApplicationContext(), LessonRedactorActivity.class);
                        intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, lessonId);
                        intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_DATE, lessonDate);
                        intentForLessonEditor.putExtra(LessonRedactorActivity.LESSON_NUMBER, finalLessonI);
                        startActivity(intentForLessonEditor);
                        return true;
                    }
                });

            }
        }

        // в конце выводим рекламмный баннер
        com.yandex.mobile.ads.AdView mAdView = new com.yandex.mobile.ads.AdView(this);
        TableLayout.LayoutParams mAdViewParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        );
        mAdViewParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        outLayout.addView(mAdView, mAdViewParams);
        // выбираем размер рекламы
        mAdView.setBlockId(getResources().getString(R.string.banner_id_schedule_day));
        mAdView.setAdSize(AdSize.BANNER_320x100);
        // Создание объекта таргетирования рекламы.
        final com.yandex.mobile.ads.AdRequest adRequest = new com.yandex.mobile.ads.AdRequest.Builder().build();
        // Загрузка объявления.
        mAdView.loadAd(adRequest);

        db.close();
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

    // -- метод трансформации числа в текст с двумя позициями --
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }
}

//class LessonTimePeriod {
//    GregorianCalendar calendarStartTime;
//    GregorianCalendar calendarEndTime;
//
//    public LessonTimePeriod(GregorianCalendar calendarStartTime, GregorianCalendar calendarEndTime) {
//        this.calendarStartTime = calendarStartTime;
//        this.calendarEndTime = calendarEndTime;
//    }
//}

