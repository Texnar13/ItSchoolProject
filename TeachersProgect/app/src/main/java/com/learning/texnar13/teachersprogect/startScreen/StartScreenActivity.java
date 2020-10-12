package com.learning.texnar13.teachersprogect.startScreen;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.ScheduleMonthActivity;
import com.learning.texnar13.teachersprogect.cabinetsOut.CabinetsOutActivity;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.learnersClassesOut.LearnersClassesOutActivity;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.settings.SettingsActivity;
import com.yandex.mobile.ads.AdSize;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class StartScreenActivity extends AppCompatActivity implements View.OnClickListener, RateInterface {


    //счетчик заходов в приложение для оценки в sharedPreferences
    static final String ENTERS_COUNT = "entersCount";
    //оценено?
    static final String IS_RATE = "isRate";
    //версия
    static final String WHATS_NEW = "whatsNew";
    static final int NOW_VERSION = 51;// todo получать автоматически

    static int i = 0;

    // обьект для преобразования календаря в строку
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    // при создании
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // отключаем поворот
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setContentView(R.layout.activity_start_screen);


        // ставим цвет статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.baseBlue));
        }


        // ================ реклама яндекса на главном экране ================
        com.yandex.mobile.ads.AdView mAdView = findViewById(R.id.start_screen_ad_banner);
        mAdView.setBlockId(getResources().getString(R.string.banner_id_start_screen));
        mAdView.setAdSize(AdSize.BANNER_320x50);
        // Создание объекта таргетирования рекламы.
        final com.yandex.mobile.ads.AdRequest adRequest = new com.yandex.mobile.ads.AdRequest.Builder().build();
        // Загрузка объявления.
        mAdView.loadAd(adRequest);


        // расписание
        ImageView relButtonSchedule = findViewById(R.id.start_screen_button_schedule);
        // кабинеты
        ImageView relButtonCabinets = findViewById(R.id.start_screen_button_my_cabinets);
        // классы
        ImageView relButtonClasses = findViewById(R.id.start_screen_button_my_classes);
        // настройки
        ImageView relButtonSettings = findViewById(R.id.start_screen_button_reload);

        //назначаем кликеры
        relButtonSchedule.setOnClickListener(this);
        relButtonCabinets.setOnClickListener(this);
        relButtonClasses.setOnClickListener(this);
        relButtonSettings.setOnClickListener(this);


        // ------ сохраненные параметры ------
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        //начинаем редактировать
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //todo перенести все подсчеты в создание самого класса, а то при перевороте срабатывают

        // ---- счетчик "оцените нас" ----
        // через семь заходов в приложение открывает диалог 'оцените'
        if (!sharedPreferences.getBoolean(IS_RATE, false)) {
            editor.putInt(ENTERS_COUNT, sharedPreferences.getInt(ENTERS_COUNT, 0) + 1);
            if (sharedPreferences.getInt(ENTERS_COUNT, 0) == 1) {
                // показываем диалог устаревшего устройства
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    DeviseDeprecatedDialogFragment dialog = new DeviseDeprecatedDialogFragment();
                    dialog.show(getFragmentManager(), "device api deprecated");
                }
            }
            if (sharedPreferences.getInt(ENTERS_COUNT, 0) == 10) {
                //на всякий случай обнуляем счетчик
                editor.putInt(ENTERS_COUNT, 1);
                editor.putBoolean(IS_RATE, false);
                //создать диалог
                StartScreenRateUsDialog startScreenRateUsDialog = new StartScreenRateUsDialog();
                //показать диалог
                startScreenRateUsDialog.show(getSupportFragmentManager(), IS_RATE);
            }
        }

        // ---- диалог что нового ----
        //если уже создано
        if (sharedPreferences.contains(WHATS_NEW)) {
            //если версия старая
            if (sharedPreferences.getInt(WHATS_NEW, -1) < NOW_VERSION) {
                i = -1;
                // меняем версию
                editor.putInt(WHATS_NEW, NOW_VERSION);
                // показываем диалог что нового
                WhatsNewDialogFragment dialogFragment = new WhatsNewDialogFragment();
                dialogFragment.show(getFragmentManager(), WHATS_NEW);
            }
        } else {
            //если еще не созданно
            //создаем переменную с версией
            editor.putInt(WHATS_NEW, NOW_VERSION);
            //начальный диалог...
        }

        //завершаем редактирование сохраненных параметров
        editor.apply();

    }


    // при запуске/при входе на эту активность
    @Override
    protected void onStart() {


        // получаем текущее время и выводим его в поля
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.setTime(new Date());
        // текст времени
        ((TextView) findViewById(R.id.start_screen_text_time)).setText(nowCalendar.get(Calendar.HOUR_OF_DAY) + ":" + getTwoSymbols(nowCalendar.get(Calendar.MINUTE)));
        // текст даты
        ((TextView) findViewById(R.id.start_screen_text_date)).setText(nowCalendar.get(Calendar.DAY_OF_MONTH) + " " + getResources().getStringArray(R.array.months_names_low_case)[nowCalendar.get(Calendar.MONTH)]);
        // текст дня недели
        ((TextView) findViewById(R.id.start_screen_text_day_of_week)).setText(getResources().getStringArray(R.array.week_days_simple)[nowCalendar.get(Calendar.DAY_OF_WEEK) - 1]);


        // выводим текущий урок
        {
            // находим контейнер для вывода
            LinearLayout containerNow = findViewById(R.id.start_screen_layout_now);
            containerNow.removeAllViews();

             DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // получаем стандартное время уроков
            int[][] times = db.getSettingsTime(1);

            // определяем текущий урок
            int lessonNumber = -1;
            for (int lessonI = 0; lessonI < times.length; lessonI++) {
                if ((nowCalendar.get(Calendar.HOUR_OF_DAY) > times[lessonI][0] ||
                        (nowCalendar.get(Calendar.HOUR_OF_DAY) == times[lessonI][0] && nowCalendar.get(Calendar.MINUTE) >= times[lessonI][1])) &&
                        (nowCalendar.get(Calendar.HOUR_OF_DAY) < times[lessonI][2] || (nowCalendar.get(Calendar.HOUR_OF_DAY) == times[lessonI][2] && nowCalendar.get(Calendar.MINUTE) <= times[lessonI][3]))
                ) {
                    lessonNumber = lessonI;
                }
            }

            // получаем сведения о текущем уроке
            final Cursor attitude = db.getSubjectAndTimeCabinetAttitudeByDateAndLessonNumber(
                    dateFormat.format(nowCalendar.getTime()),
                    lessonNumber
            );

            // если урока нет
            if (attitude.getCount() == 0) {

                // текст пустоты
                TextView absText = new TextView(this);
                absText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
                absText.setText(R.string.start_screen_activity_title_current_no_lesson);
                absText.setTextColor(getResources().getColor(R.color.backgroundGray));
                absText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                LinearLayout.LayoutParams absTextParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                absTextParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                absTextParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                absTextParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                containerNow.addView(absText, absTextParams);


                // убираем кнопку
                findViewById(R.id.start_screen_button_start_lesson_background).setBackgroundResource(android.R.color.transparent);
                ((TextView) (findViewById(R.id.start_screen_button_start_lesson_text))).setText("");

//                // меняем кнопку на создание урока todo
//                ((LinearLayout) (findViewById(R.id.start_screen_button_start_lesson_background))).setBackgroundResource(R.drawable._button_round_background_green);
//                ((TextView) (findViewById(R.id.start_screen_button_start_lesson_text))).setText(R.string.start_screen_activity_title_current_create_lesson);
//
//                // назначаем создание при нажатии
//                View.OnClickListener clickListener = new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // создаем диалог
//                        CreateLessonDialog createLessonDialog = new CreateLessonDialog();
//                        // показываем диалог
//                        createLessonDialog.show(getFragmentManager(), "createLessonDialog");
//                    }
//                };
//                // нажатие на контейнер с датой
//                containerNow.setOnClickListener(clickListener);
//                // и кнопку урока
//                findViewById(R.id.start_screen_button_start_lesson_background)
//                        .setOnClickListener(clickListener);

            } else {// если урок есть

                // получаем поля урока
                attitude.moveToFirst();
                final long lessonId = attitude.getLong(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID));
                long subjectId = attitude.getLong(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
                long cabinetId = attitude.getLong(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
                final String lessonDate = attitude.getString(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE));
                // имя предмета
                Cursor subjectCursor = db.getSubjectById(subjectId);
                subjectCursor.moveToFirst();
                String subjectName = subjectCursor.getString(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
                long learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
                subjectCursor.close();
                // имя класса
                Cursor classCursor = db.getLearnersClass(learnersClassId);
                classCursor.moveToFirst();
                String className = classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
                classCursor.close();
                // имя кабинета
                Cursor cabinetCursor = db.getCabinet(cabinetId);
                cabinetCursor.moveToFirst();
                String cabinetName = cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
                cabinetCursor.close();

                // укорачиваем поля если они слишком длинные Loading…
                if (subjectName.length() > 15) {
                    subjectName = subjectName.substring(0, 14) + "…";
                }
                if (className.length() > 6) {
                    className = className.substring(0, 5) + "…";// absde -> abc…  abcd->abcd
                }
                if (cabinetName.length() > 6) {
                    cabinetName = cabinetName.substring(0, 5) + "…";
                }

                // выводим поля в контейнер

                // контейнер времени
                LinearLayout timeContainer = new LinearLayout(this);
                timeContainer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams timeContainerParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                timeContainerParams.topMargin = (int) getResources().getDimension(R.dimen.half_margin);
                timeContainerParams.bottomMargin = (int) getResources().getDimension(R.dimen.half_margin);
                timeContainerParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                containerNow.addView(timeContainer, timeContainerParams);
                // первое время
                TextView firstTime = new TextView(this);
                firstTime.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                firstTime.setText(times[lessonNumber][0] + ":" + times[lessonNumber][1]);
                firstTime.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                firstTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                timeContainer.addView(firstTime,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                // второе время
                TextView secondTime = new TextView(this);
                secondTime.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                secondTime.setText(times[lessonNumber][2] + ":" + times[lessonNumber][3]);
                secondTime.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                secondTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                timeContainer.addView(secondTime,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

                // предмет
                TextView subjectText = new TextView(this);
                subjectText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                subjectText.setText(subjectName);
                subjectText.setTextColor(Color.BLACK);
                subjectText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                LinearLayout.LayoutParams subjectTextParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                subjectTextParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                containerNow.addView(subjectText, subjectTextParams);

                // класс
                TextView classText = new TextView(this);
                classText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_medium));
                classText.setText(className);
                classText.setTextColor(Color.BLACK);
                classText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                LinearLayout.LayoutParams classTextParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                classTextParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                containerNow.addView(classText, classTextParams);

                // кабинет
                TextView cabinetText = new TextView(this);
                cabinetText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
                cabinetText.setText(cabinetName);
                cabinetText.setTextColor(Color.BLACK);
                cabinetText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                LinearLayout.LayoutParams cabinetTextParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                cabinetTextParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
                containerNow.addView(cabinetText, cabinetTextParams);

                // делаем кнопку видимой если она была скрыта
                ((LinearLayout) (findViewById(R.id.start_screen_button_start_lesson_background))).setBackgroundResource(R.drawable._button_round_background_pink);
                ((TextView) (findViewById(R.id.start_screen_button_start_lesson_text))).setText(R.string.start_screen_activity_title_current_start_lesson);

                // назначаем открытие урока при нажатии
                final int finalLessonNumber = lessonNumber;
                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // создаем намерение
                        Intent intent = new Intent(getApplicationContext(), LessonActivity.class);


                        //соединяем и отправляем
                        intent.putExtra(LessonActivity.LESSON_DATE, lessonDate);
                        intent.putExtra(LessonActivity.LESSON_NUMBER, finalLessonNumber);
                        //отправляем id
                        intent.putExtra(LessonActivity.LESSON_ATTITUDE_ID, lessonId);

                        startActivity(intent);
                    }
                };
                // нажатие на контейнер с датой
                containerNow.setOnClickListener(clickListener);
                // и кнопку урока
                findViewById(R.id.start_screen_button_start_lesson_background)
                        .setOnClickListener(clickListener);
            }
            attitude.close();
            db.close();
        }


        //проверка на запись настроек
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor settingCursor = db.getSettingProfileById(1);//получаем первый профиль настроек
        Log.i("TeachersApp", "StartScreen - settingCursor.getCount() = " + settingCursor.getCount());
        if (settingCursor.getCount() == 0) {//если нет профиля настроек
            db.createNewSettingsProfileWithId1("default", 50);//тогда создем его
        }
        settingCursor.close();
        db.close();


        super.onStart();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.start_screen_button_schedule://переход в список расписаний
                intent = new Intent(this, ScheduleMonthActivity.class);
                startActivity(intent);
                break;
            case R.id.start_screen_button_my_cabinets://переход в список кабинетов
                intent = new Intent(this, CabinetsOutActivity.class);
                startActivity(intent);
                break;
            case R.id.start_screen_button_my_classes: {//переход в список классов
                intent = new Intent(this, LearnersClassesOutActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.start_screen_button_reload: {
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                break;
            }
        }
    }


    // метод диалога оценить
    @Override
    public void rate(int rateId) {

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        //начинаем редактировать
        SharedPreferences.Editor ed = sharedPreferences.edit();
        switch (rateId) {
            case 0://оценить
                ed.putBoolean(IS_RATE, true);
                ed.putInt(ENTERS_COUNT, 0);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.learning.texnar13.teachersprogect"));
                if (!isActivityStarted(intent)) {
                    intent.setData(Uri
                            .parse("https://play.google.com/store/apps/details?id=com.learning.texnar13.teachersprogect"));
                    if (!isActivityStarted(intent)) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Could not open Android market, please check if the market app installed or not. Try again later",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case 1://перенести на потом
                ed.putInt(ENTERS_COUNT, 1);
                break;
            case 2://не оценивать
                ed.putBoolean(IS_RATE, true);
                ed.putInt(ENTERS_COUNT, 2);
                break;
        }
        //завершаем редактирование сохраненных параметров
        ed.commit();
    }

    private boolean isActivityStarted(Intent aIntent) {
        try {
            startActivity(aIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    // число в текст с двумя позициями
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    // преобразование зависимой величины в пиксели
    private int pxFromDp(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        Log.i("teachersApp", "StartScreenActivity-back");
        //finish();
        super.onBackPressed();
    }


}


/*
todo хранилище полезного кода :)


if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
}

Используйте это свойство внутри своего тега activity чтобы избежать появления activity в списке недавно использованных приложений.
 android:excludeFromRecents="true"


  Надо отследить что пользователь открыл приложение, а не (например) повернул. Это можно сделать проверив
  if(savedInstanceState==null) это аргумент в onCreate методе активити. После её поворота (пересоздания)
  этот аргумент уже не null и условие выполнено не будет.


  // устаревшие диалоги надо перевести на библиотеку androidX
  // getSupportFragmentManager()
  // import androidx.fragment.app.DialogFragment;

  а также активити import androidx.appcompat.app.AppCompatActivity;


// выводим вступитльное сообщение
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(WAS_HELP_SHOWED, false)) {// выводилось ли оно до этого
            // изменяем на то, что оно было
            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putBoolean(WAS_HELP_SHOWED, true);
            ed.apply();
            // показываем
            Toast.makeText(this, R.string.cabinet_redactor_activity_toast_help, Toast.LENGTH_LONG).show();
        }

* */