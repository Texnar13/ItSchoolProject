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
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.ScheduleMonthActivity;
import com.learning.texnar13.teachersprogect.cabinetsOut.CabinetsOutActivity;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;
import com.learning.texnar13.teachersprogect.gradesPeriods.GradesPeriodsActivity;
import com.learning.texnar13.teachersprogect.learnersClassesOut.LearnersClassesOutActivity;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.lessonRedactor.LessonRedactorActivity;
import com.learning.texnar13.teachersprogect.settings.SettingsActivity;
import com.yandex.mobile.ads.banner.AdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class StartScreenActivity extends AppCompatActivity implements RateInterface {

    // todo раз в сколько запусков показываются диалоги


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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.start_screen_top_sheet_color, getTheme()));
        }


        // работаем с view

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
        ImageView relButtonSettings = findViewById(R.id.start_screen_button_settings);
        relButtonSettings.setOnClickListener(v -> startActivity(new Intent(
                StartScreenActivity.this, SettingsActivity.class
        )));


        if (savedInstanceState == null) {// при создании активности


            // проверяем статус подписки
            checkSubscriptionStatusAndSavePrefs();

            // ------ сохраненные параметры ------
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            {// переходим со старого SharedPreferences на новое

                SharedPreferences oldPref = getPreferences(MODE_PRIVATE);
                // а конкретно перенос счетчика оцените нас
                int oldEntersCount = oldPref.getInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, -1);
                if (oldEntersCount != -1) {

                    // начинаем перенос в новые
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, oldEntersCount);

                    // и правим старые
                    SharedPreferences.Editor oldEditor = oldPref.edit();
                    oldEditor.putInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, -1);
                    oldEditor.apply();

                    // заодно копируем и этот параметр из старых
                    if (oldPref.contains(SharedPrefsContract.PREFS_BOOLEAN_IS_RATE)) {
                            // перенос в новые
                            editor.putBoolean(SharedPrefsContract.PREFS_BOOLEAN_IS_RATE,
                                    oldPref.getBoolean(SharedPrefsContract.PREFS_BOOLEAN_IS_RATE, false));
                    }

                    editor.commit();// специально взял такую версию метода
                }
            }

            //начинаем редактировать
            SharedPreferences.Editor editor = sharedPreferences.edit();


            // ---- счетчик "оцените нас" ----
            // через семь заходов в приложение открывает диалог 'оцените'
            if (!sharedPreferences.getBoolean(SharedPrefsContract.PREFS_BOOLEAN_IS_RATE, false)) {
                editor.putInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, sharedPreferences.getInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, 0) + 1);
                if (sharedPreferences.getInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, 0) == 15) {

                    // обнуляем счетчик
                    editor.putInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, 1);

                    editor.putBoolean(SharedPrefsContract.PREFS_BOOLEAN_IS_RATE, false);
                    //создать диалог
                    StartScreenRateUsDialog startScreenRateUsDialog = new StartScreenRateUsDialog();
                    //показать диалог
                    startScreenRateUsDialog.show(getSupportFragmentManager(), SharedPrefsContract.PREFS_BOOLEAN_IS_RATE);
                }
            }

            // ---- диалог что нового ----
            //если уже создано
            if (sharedPreferences.contains(SharedPrefsContract.PREFS_INT_WHATS_NEW)) {
                //если версия старая
                if (sharedPreferences.getInt(SharedPrefsContract.PREFS_INT_WHATS_NEW, -1) < SharedPrefsContract.PREFS_INT_NOW_VERSION) {
                    // меняем версию
                    editor.putInt(SharedPrefsContract.PREFS_INT_WHATS_NEW, SharedPrefsContract.PREFS_INT_NOW_VERSION);
                    // показываем диалог что нового
                    WhatsNewDialogFragment dialogFragment = new WhatsNewDialogFragment();
                    dialogFragment.show(getFragmentManager(), SharedPrefsContract.PREFS_INT_WHATS_NEW);
                }
            } else {
                //если еще не созданно
                //создаем переменную с версией
                editor.putInt(SharedPrefsContract.PREFS_INT_WHATS_NEW, SharedPrefsContract.PREFS_INT_NOW_VERSION);
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

        // выводим рекламу если нет подписки
        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(SharedPrefsContract.PREFS_BOOLEAN_PREMIUM_STATE, false)) {
            // реклама яндекса
            loadAdd();
        }


    }

    // проверка состояния подписки, результат в SharedPrefs
    void checkSubscriptionStatusAndSavePrefs() {
        final BillingClient billingClient = BillingClient.newBuilder(this)
                .setListener((billingResult, purchases) -> {
                }).enablePendingPurchases().build();
        // пытаемся подключиться
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                billingClient.endConnection();
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult connectResult) {
                // связь установлена
                if (connectResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // получаем данные о подписках
                    billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, (queryCheckResult, purchasesList) -> {
                        // данные получены
                        if (queryCheckResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                            // проверяем есть ли среди подписок подписка на премиум
                            boolean subsPurchasedFlag = false;
                            for (Purchase purchase : purchasesList) {
                                // получаем sku покупок для анализа
                                ArrayList<String> tempSkus = purchase.getSkus();
                                if (tempSkus.contains(getResources().getString(R.string.subscription_id_month_sponsor)) ||
                                        tempSkus.contains(getResources().getString(R.string.subscription_id_year_sponsor)) ||
                                        purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    subsPurchasedFlag = true;
                                    // заодно, если что, подтверждаем их
                                    //  (тк нужно обязательно отпарвить в google уведомление о том, что контент предоставлен пользователю)
                                    if (!purchase.isAcknowledged()) {
                                        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                                                .newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                                        // подтверждение на подтверждение
                                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult12 -> {
                                        });
                                    }
                                }
                            }

                            // сохраняем параметр в SharedPreferences
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                                    .putBoolean(SharedPrefsContract.PREFS_BOOLEAN_PREMIUM_STATE, subsPurchasedFlag).apply();
                        }
                        billingClient.endConnection();
                    });
                } else {
                    billingClient.endConnection();
                }
            }
        });

    }

    // показ раеламы
    void loadAdd() {
        // создаем рекламу яндекса внизу календаря
        BannerAdView mAdView = findViewById(R.id.start_screen_ad_banner);
        mAdView.setBlockId(getResources().getString(R.string.banner_id_start_screen));
        mAdView.setAdSize(AdSize.BANNER_320x50);
        // Создание объекта таргетирования рекламы и загрузка объявления.
        mAdView.loadAd(new AdRequest.Builder().build());
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

    void outCurrentLesson() {
        // контейнер текущего урока
        LinearLayout currentLessonContainer = findViewById(R.id.start_screen_layout_now);
        // кнопка создать/начать урок
        TextView lessonButtonText = findViewById(R.id.start_screen_button_start_lesson_text);

        currentLessonContainer.removeAllViews();

        // получаем текущее время
        GregorianCalendar nowCalendar = new GregorianCalendar();

        // получаем стандартное время уроков
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        int[][] times = db.getSettingsTime(1);
        if (times == null) times = new int[0][0];

        // определяем текущий урок
        int lessonNumber = 0;
        for (int lessonI = 0; lessonI < times.length; lessonI++) {
            if (nowCalendar.get(Calendar.HOUR_OF_DAY) > times[lessonI][0] || (
                    nowCalendar.get(Calendar.HOUR_OF_DAY) == times[lessonI][0] &&
                            nowCalendar.get(Calendar.MINUTE) >= times[lessonI][1]
            )) {
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
            absText.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_semibold));
            absText.setText(R.string.start_screen_activity_title_current_no_lesson);
            absText.setGravity(Gravity.CENTER_VERTICAL);
            absText.setTextColor(getResources().getColor(R.color.start_screen_top_sheet_lesson_not_active_text_color));
            absText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.simple_buttons_text_size));
            LinearLayout.LayoutParams absTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.simple_buttons_height)
            );
            absTextParams.leftMargin = (int) getResources().getDimension(R.dimen.double_margin);
            currentLessonContainer.addView(absText, absTextParams);


            // меняем кнопку на создание урока
            lessonButtonText.setBackgroundResource(R.drawable.start_screen_activity_background_button_create_lesson);
            lessonButtonText.setTextColor(getResources().getColor(R.color.text_color_simple));
            lessonButtonText.setText(R.string.start_screen_activity_title_current_create_lesson);

            // назначаем создание при нажатии
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

        } else {// если урок есть

            // получаем поля урока
            attitude.moveToFirst();
            final long lessonId = attitude.getLong(attitude.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID));
            long subjectId = attitude.getLong(attitude.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
            long cabinetId = attitude.getLong(attitude.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
            final String savedLessonDate = attitude.getString(attitude.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE));
            // имя предмета
            Cursor subjectCursor = db.getSubjectById(subjectId);
            subjectCursor.moveToFirst();
            String subjectName = subjectCursor.getString(subjectCursor.getColumnIndexOrThrow(SchoolContract.TableSubjects.COLUMN_NAME));
            long learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndexOrThrow(SchoolContract.TableSubjects.KEY_CLASS_ID));
            subjectCursor.close();
            // имя класса
            Cursor classCursor = db.getLearnersClases(learnersClassId);
            classCursor.moveToFirst();
            String className = classCursor.getString(classCursor.getColumnIndexOrThrow(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            classCursor.close();
            // имя кабинета
            Cursor cabinetCursor = db.getCabinet(cabinetId);
            cabinetCursor.moveToFirst();
            String cabinetName = cabinetCursor.getString(cabinetCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_NAME));
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
                    R.layout.start_screen_current_lesson_pattern,
                    currentLessonContainer
            );
            // выводим в них текст
            // первое время
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_pattern_start_time)).setText(
                    getResources().getString(
                            R.string.start_screen_activity_time_field,
                            times[lessonNumber][0],
                            times[lessonNumber][1]
                    ));
            // второе время
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_pattern_end_time)).setText(
                    getResources().getString(
                            R.string.start_screen_activity_time_field,
                            times[lessonNumber][2],
                            times[lessonNumber][3]
                    ));
            // предмет
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_pattern_subject)).setText(subjectName);
            // класс
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_pattern_class)).setText(className);
            // кабинет
            ((TextView) lessonField.findViewById(R.id.start_screen_current_lesson_pattern_cabinet)).setText(cabinetName);

            // делаем кнопку видимой если она была скрыта
            lessonButtonText.setBackgroundResource(R.drawable.start_screen_activity_background_button_start_lesson);
            lessonButtonText.setTextColor(getResources().getColor(R.color.text_color_inverse));
            lessonButtonText.setText(R.string.start_screen_activity_title_current_start_lesson);

            // назначаем открытие урока при нажатии
            final int finalLessonNumber = lessonNumber;
            View.OnClickListener clickListener = v -> {
                // создаем намерение
                Intent intent = new Intent(getApplicationContext(), LessonActivity.class);
                // соединяем и отправляем
                intent.putExtra(LessonActivity.ARGS_LESSON_DATE, savedLessonDate);
                intent.putExtra(LessonActivity.ARGS_LESSON_NUMBER, finalLessonNumber);
                // отправляем id
                intent.putExtra(LessonActivity.ARGS_LESSON_ATTITUDE_ID, lessonId);
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //начинаем редактировать
        SharedPreferences.Editor ed = sharedPreferences.edit();
        switch (rateId) {
            case 0://оценить
                ed.putBoolean(SharedPrefsContract.PREFS_BOOLEAN_IS_RATE, true);
                ed.putInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, 0);

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
                ed.putInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, 1);
                break;
            case 2://не оценивать
                ed.putBoolean(SharedPrefsContract.PREFS_BOOLEAN_IS_RATE, true);
                ed.putInt(SharedPrefsContract.PREFS_INT_ENTERS_COUNT, 2);
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

// узнать размер тулбара
android:layout_marginTop="?attr/actionBarSize"

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




// подчеркивание текста
subjectText.setPaintFlags(subjectText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

// tint программно
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shadeLayer.getBackground().setTint(getResources().getColor(
                    (absCheckState) ?
                            (R.color.grade_edit_bottom_shadow_color) :
                            (R.color.transparent)
            ));
        } else {
            shadeLayer.getBackground().setColorFilter(getResources().getColor(
                    (absCheckState) ?
                            (R.color.grade_edit_bottom_shadow_color) :
                            (R.color.transparent)
            ), PorterDuff.Mode.SRC_ATOP);
        }


todo на будущее, кстати, всем кнопкам закрыть и кнопкам подтверждения итд можно ставить стандартный id, чтобы каждый раз не придумывать его ведь в рамках одной разметки он не повторяется (да мне это даже при раздувании списков не мешало)

todo во первых число записей в таблице вполне можно получить средствами sql, тянуть для этого все данные мягко говоря неэффективно и плохой тон
* */

