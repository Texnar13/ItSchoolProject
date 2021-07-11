package com.learning.texnar13.teachersprogect.startScreen;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.BuildConfig;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.ScheduleMonthActivity;
import com.learning.texnar13.teachersprogect.cabinetsOut.CabinetsOutActivity;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.learnersClassesOut.LearnersClassesOutActivity;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.lessonRedactor.LessonRedactorActivity;
import com.learning.texnar13.teachersprogect.settings.SettingsActivity;
import com.yandex.mobile.ads.AdSize;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class StartScreenActivity extends AppCompatActivity implements RateInterface {
    // константы для SharedPreferences
    // счетчик заходов в приложение для оценки в sharedPreferences
    static final String ENTERS_COUNT = "entersCount";
    // оценено?
    static final String IS_RATE = "isRate";
    // версия
    static final String WHATS_NEW = "whatsNew";
    static final int NOW_VERSION = BuildConfig.VERSION_CODE;


    // была ли начальная инициализация
    static boolean isInit = false;


    // контейнер текущего урока
    LinearLayout currentLessonContainer;
    // кнопка создать/начать урок
    TextView lessonButtonText;


    // при создании
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // отключаем поворот
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        // ставим разметку
        setContentView(R.layout.start_screen_activity);

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
        relButtonSchedule.setOnClickListener(v -> startActivity(new Intent(
                StartScreenActivity.this, ScheduleMonthActivity.class
        )));
        // кабинеты
        ImageView relButtonCabinets = findViewById(R.id.start_screen_button_my_cabinets);
        relButtonCabinets.setOnClickListener(v -> startActivity(new Intent(
                StartScreenActivity.this, CabinetsOutActivity.class
        )));
        // классы
        ImageView relButtonClasses = findViewById(R.id.start_screen_button_my_classes);
        relButtonClasses.setOnClickListener(v -> startActivity(new Intent(
                StartScreenActivity.this, LearnersClassesOutActivity.class
        )));
        // настройки
        ImageView relButtonSettings = findViewById(R.id.start_screen_button_reload);
        relButtonSettings.setOnClickListener(v -> startActivity(new Intent(
                StartScreenActivity.this, SettingsActivity.class
        )));


        // контейнер текущего урока
        currentLessonContainer = findViewById(R.id.start_screen_layout_now);
        lessonButtonText = findViewById(R.id.start_screen_button_start_lesson_text);


        // при начале работы активности
        if (!isInit) {
            isInit = true;
            // ------ сохраненные параметры ------
            SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
            //начинаем редактировать
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // ---- счетчик "оцените нас" ----
            // через семь заходов в приложение открывает диалог 'оцените'
            if (!sharedPreferences.getBoolean(IS_RATE, false)) {
                editor.putInt(ENTERS_COUNT, sharedPreferences.getInt(ENTERS_COUNT, 0) + 1);
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


            // проверка существует ли хотя бы один профиль настроек
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            Cursor settingCursor = db.getSettingProfileById(1);// получаем первый профиль настроек
            if (settingCursor.getCount() == 0) {// если нет профиля настроек
                db.createNewSettingsProfileWithId1("default", 50);// тогда создем его
            }
            settingCursor.close();
            db.close();
        }
    }

    // при запуске/при входе на эту активность
    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {

        // получаем текущее время и выводим его в поля
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.setTime(new Date());
        // текст времени
        ((TextView) findViewById(R.id.start_screen_text_time)).setText(String.format(
                Locale.getDefault(),
                "%02d:%02d",
                nowCalendar.get(Calendar.HOUR_OF_DAY),
                nowCalendar.get(Calendar.MINUTE)
        ));
        // текст даты
        ((TextView) findViewById(R.id.start_screen_text_date)).setText(
                nowCalendar.get(Calendar.DAY_OF_MONTH) + " " +
                        getResources().getStringArray(R.array.months_names_with_ending)[nowCalendar.get(Calendar.MONTH)]
        );
        // текст дня недели
        ((TextView) findViewById(R.id.start_screen_text_day_of_week)).setText(
                getResources().getStringArray(R.array.week_days_simple)[nowCalendar.get(Calendar.DAY_OF_WEEK) - 1]
        );


        // выводим текущий урок
        outCurrentLesson();

        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // чистим данные
        isInit = false;
    }


    void outCurrentLesson() {
        currentLessonContainer.removeAllViews();

        // получаем текущее время
        GregorianCalendar nowCalendar = new GregorianCalendar();

        // получаем стандартное время уроков
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        int[][] times = db.getSettingsTime(1);
        if (times == null) times = new int[0][0];

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
        final String lessonDate = (new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()))
                .format(nowCalendar.getTime());

        // получаем сведения о текущем уроке
        Cursor attitude = db.getSubjectAndTimeCabinetAttitudeByDateAndLessonNumber(
                lessonDate,
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
            currentLessonContainer.addView(absText, absTextParams);


            // меняем кнопку на создание урока
            lessonButtonText.setBackgroundResource(R.drawable.start_screen_activity_button_background_create_lesson);
            lessonButtonText.setText(R.string.start_screen_activity_title_current_create_lesson);

            // назначаем создание при нажатии
            if (lessonNumber != -1) {
                final int finalLessonNumber = lessonNumber;
                View.OnClickListener clickListener = v -> {
                    // создаем активность
                    Intent intent = new Intent(StartScreenActivity.this, LessonRedactorActivity.class);
                    intent.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, -1L);
                    intent.putExtra(LessonRedactorActivity.LESSON_CHECK_DATE, lessonDate);
                    intent.putExtra(LessonRedactorActivity.LESSON_NUMBER, finalLessonNumber);
                    startActivityForResult(intent, LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID);
                };
                // нажатие на контейнер с датой и кнопку урока
                currentLessonContainer.setOnClickListener(clickListener);
                lessonButtonText.setOnClickListener(clickListener);
            }
        } else {// если урок есть

            // получаем поля урока
            attitude.moveToFirst();
            final long lessonId = attitude.getLong(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID));
            long subjectId = attitude.getLong(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
            long cabinetId = attitude.getLong(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
            final String savedLessonDate = attitude.getString(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE));
            // имя предмета
            Cursor subjectCursor = db.getSubjectById(subjectId);
            subjectCursor.moveToFirst();
            String subjectName = subjectCursor.getString(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
            long learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
            subjectCursor.close();
            // имя класса
            Cursor classCursor = db.getLearnersClases(learnersClassId);
            classCursor.moveToFirst();
            String className = classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            classCursor.close();
            // имя кабинета
            Cursor cabinetCursor = db.getCabinet(cabinetId);
            cabinetCursor.moveToFirst();
            String cabinetName = cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
            cabinetCursor.close();


            // укорачиваем поля если они слишком длинные
            if (subjectName.length() > 18) {// abcde -> abc…  abcd->abcd
                subjectName = subjectName.substring(0, 17) + "…";
            }
            if (className.length() > 25) {
                className = className.substring(0, 24) + "…";
            }
            if (cabinetName.length() > 10) {
                cabinetName = cabinetName.substring(0, 9) + "…";
            }

            // раздуваем поля в контейнере
            LinearLayout lessonField = (LinearLayout) getLayoutInflater().inflate(
                    R.layout.start_screen_current_lesson_field,
                    currentLessonContainer
            );
            // выводим в них текст
            // первое время
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_field_time_1)).setText(
                    getResources().getString(
                            R.string.start_screen_activity_time_field,
                            times[lessonNumber][0],
                            times[lessonNumber][1]
                    ));
            // второе время
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_field_time_2)).setText(
                    getResources().getString(
                            R.string.start_screen_activity_time_field,
                            times[lessonNumber][2],
                            times[lessonNumber][3]
                    ));
            // предмет
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_field_subject)).setText(subjectName);
            // класс
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_field_class)).setText(className);
            // кабинет
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_field_cabinet)).setText(cabinetName);

            // делаем кнопку видимой если она была скрыта
            lessonButtonText.setBackgroundResource(R.drawable.start_screen_activity_button_background_start_lesson);
            lessonButtonText.setText(R.string.start_screen_activity_title_current_start_lesson);

            // назначаем открытие урока при нажатии
            final int finalLessonNumber = lessonNumber;
            View.OnClickListener clickListener = v -> {
                // создаем намерение
                Intent intent = new Intent(getApplicationContext(), LessonActivity.class);
                // соединяем и отправляем
                intent.putExtra(LessonActivity.LESSON_DATE, savedLessonDate);
                intent.putExtra(LessonActivity.LESSON_NUMBER, finalLessonNumber);
                // отправляем id
                intent.putExtra(LessonActivity.LESSON_ATTITUDE_ID, lessonId);
                startActivity(intent);
            };
            // нажатие на контейнер с датой
            currentLessonContainer.setOnClickListener(clickListener);
            // и кнопку урока
            lessonButtonText.setOnClickListener(clickListener);
        }
        attitude.close();
        db.close();
    }


    // обратная связь от диалога оценить
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
                boolean isActivityNotStarted = true;

                intent.setData(Uri.parse("market://details?id=com.learning.texnar13.teachersprogect"));
                try {
                    startActivity(intent);
                    isActivityNotStarted = false;
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                if (isActivityNotStarted) {
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.learning.texnar13.teachersprogect"));
                    try {
                        startActivity(intent);
                        isActivityNotStarted = false;
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (isActivityNotStarted)
                        Toast.makeText(this,
                                "Could not open Android market, please check if the market app installed or not. Try again later",
                                Toast.LENGTH_SHORT
                        ).show();
                }
                break;
            case 1://перенести на потом
                ed.putInt(ENTERS_COUNT, 1);
                break;
            case 2://не оценивать
                ed.putBoolean(IS_RATE, true);
                ed.putInt(ENTERS_COUNT, 2);
        }
        // завершаем редактирование сохраненных параметров
        ed.apply();
    }

    // обратная связь от активности LessonRedactorActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // обратная связь от редактора урока
        if (requestCode == LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID
                && resultCode == LessonRedactorActivity.LESSON_REDACTOR_RESULT_CODE_UPDATE) {
            outCurrentLesson();
        }
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


// Убираем панель уведомлений
this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // настраиваем программный вывод векторных изображений в диалогах
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

* */

