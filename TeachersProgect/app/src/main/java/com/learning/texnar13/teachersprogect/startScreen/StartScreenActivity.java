package com.learning.texnar13.teachersprogect.startScreen;

import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.ScheduleMonthActivity;
import com.learning.texnar13.teachersprogect.TestActivity;
import com.learning.texnar13.teachersprogect.cabinetsOut.CabinetsOutActivity;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.learnersClassesOut.CreateLearnersClassDialogFragment;
import com.learning.texnar13.teachersprogect.learnersClassesOut.LearnersClassesOutActivity;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.settings.SettingsActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class StartScreenActivity extends AppCompatActivity implements View.OnClickListener, RateInterface {

    //текущий урок
    RelativeLayout relButtonNow;
    // расписание
    RelativeLayout relButtonSchedule;
    // кабинеты
    RelativeLayout relButtonCabinets;
    // классы
    RelativeLayout relButtonClasses;
    // настройки
    RelativeLayout relButtonSettings;

//--константы--

    //счетчик заходов в приложение для оценки в sharedPreferences
    static final String ENTERS_COUNT = "entersCount";
    //оценено?
    static final String IS_RATE = "isRate";
    //версия
    static final String WHATS_NEW = "whatsNew";
    static final int NOW_VERSION = 39;

//-----------------------------------метод диалога--------------------------------------------------

    //диалог оценить
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


//-------------------------------меню сверху--------------------------------------------------------

    //раздуваем неаше меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start_screen_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //назначаем функции меню
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        //кнопка помощь
        menu.findItem(R.id.start_screen_menu_item_help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getApplicationContext(), StartScreenHelp.class);
                startActivity(intent);
//                Toast toast = Toast.makeText(getApplicationContext(),"В разработке ¯\\_(ツ)_/¯",Toast.LENGTH_LONG);
//                toast.show();
                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }


//----------------------------------------при создании----------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        //находим все кнопки
        relButtonNow = (RelativeLayout) findViewById(R.id.start_screen_button_now);
        relButtonSchedule = (RelativeLayout) findViewById(R.id.start_screen_button_schedule);
        relButtonCabinets = (RelativeLayout) findViewById(R.id.start_screen_button_my_cabinets);
        relButtonClasses = (RelativeLayout) findViewById(R.id.start_screen_button_my_classes);
        relButtonSettings = (RelativeLayout) findViewById(R.id.start_screen_button_reload);

        //назначаем кликеры
        relButtonNow.setOnClickListener(this);
        relButtonSchedule.setOnClickListener(this);
        relButtonCabinets.setOnClickListener(this);
        relButtonClasses.setOnClickListener(this);
        relButtonSettings.setOnClickListener(this);

        //setTitle("помощник учителя");

//------сохраненные параметры------
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        //начинаем редактировать
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //todo перенести все подсчеты в создание самого класса, а то при перевороте срабатывают

//----счетчик "оцените нас"----
        // через пять заходов в приложение открывает диалог 'оцените'
        if (!sharedPreferences.getBoolean(IS_RATE, false)) {
            editor.putInt(ENTERS_COUNT, sharedPreferences.getInt(ENTERS_COUNT, 0) + 1);
            if (sharedPreferences.getInt(ENTERS_COUNT, 0) == 5) {
                //на всякий случай обнуляем счетчик
                editor.putInt(ENTERS_COUNT, 1);
                editor.putBoolean(IS_RATE, false);
                //создать диалог
                StartScreenRateUsDialog startScreenRateUsDialog = new StartScreenRateUsDialog();
                //показать диалог
                startScreenRateUsDialog.show(getFragmentManager(), IS_RATE);

            }
        }

//        WhatsNewDialogFragment dialogFragment = new WhatsNewDialogFragment();
//        dialogFragment.show(getFragmentManager(), WHATS_NEW);
//----диалог что нового----
        //если уже создано
        if (sharedPreferences.contains(WHATS_NEW)) {
            //если версия старая
            if (sharedPreferences.getInt(WHATS_NEW, -1) < NOW_VERSION) {
                //меняем версию
                editor.putInt(WHATS_NEW, NOW_VERSION);
                //показываем диалог что нового
                WhatsNewDialogFragment dialogFragment = new WhatsNewDialogFragment();
                dialogFragment.show(getFragmentManager(), WHATS_NEW);
            }
        } else {
            //если еще не созданно
            //создаем переменную с версией
            editor.putInt(WHATS_NEW, NOW_VERSION);
            //начальный диалог...
            WhatsNewDialogFragment dialogFragment = new WhatsNewDialogFragment();
            dialogFragment.show(getFragmentManager(), WHATS_NEW);
        }

        //завершаем редактирование сохраненных параметров
        editor.commit();

    }

//----------------------------------------при запуске-----------------------------------------------

    @Override
    protected void onStart() {
        //при входе на эту активность
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
            case R.id.start_screen_button_now: {//запуск текущего урока
                intent = new Intent(this, LessonActivity.class);
                GregorianCalendar currentCalendar = new GregorianCalendar();//получаем текущее время
                currentCalendar.setTime(new Date());
                DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                long attitudeId = db.getSubjectAndTimeCabinetAttitudeIdByTime(currentCalendar);
                if (attitudeId == -1) {
                    Toast toast = Toast.makeText(this, R.string.start_screen_activity_toast_no_lessons, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    //получаем предмет
                    Cursor attitude = db.getSubjectAndTimeCabinetAttitudeById(attitudeId);
                    attitude.moveToFirst();
                    //берем его время
                    String dBTime = attitude.getString(attitude.getColumnIndex(
                            SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_DATE_BEGIN
                    ));
                    attitude.close();
                    //получаем текущий месяц и год
                    SimpleDateFormat nowDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String nowDate = nowDateFormat.format(new Date());
                    //соединяем и отправляем
                    intent.putExtra(LessonActivity.LESSON_TIME, nowDate + dBTime.substring(10, 19));
                    //отправляем id
                    intent.putExtra(LessonActivity.LESSON_ATTITUDE_ID, attitudeId);

                    startActivity(intent);
                }
            }
            break;
            case R.id.start_screen_button_schedule://переход в список расписаний
                intent = new Intent(this, ScheduleMonthActivity.class);
                startActivity(intent);


                /*
                * существуют рассадки, то есть как ученики класса сидят в конкретном кабинете (в разных кабинетах ученики одного класса сидят по разному)
                * если уроку присвоены класс и кабинет, но нет рассадки этого класса в этом кабинете,
                или не у всех учеников назначено место, то, например, подсвечивать урок
                красным и не давать возможности начать этот урок(надо предусмотреть вариант,
                когда учеников больше чем мест)
                * при нажатии на урок можно будет его начать -> запустить LessonActivity с параметрами этого
                урока(урок ограничение по времени не имеет, время начала и конца нужны только для поиска текущего по времени урока)
                * должен открываться редактор рассадки, скорее всего надо будет
                сдеать кнопку в диалоге, что-то вроде "редактировать рассадку учеников" */
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

    @Override
    public void onBackPressed() {
        Log.i("teachersApp", "StartScreenActivity-back");
        //finish();
        super.onBackPressed();
    }


}
