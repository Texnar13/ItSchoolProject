package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.listOf.ListOfActivity;

import java.util.Date;
import java.util.GregorianCalendar;

public class StartScreenActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout relButtonNow;//текущий урок
    RelativeLayout relButtonSchedule;//расписание
    RelativeLayout relButtonCabinets;//кабинеты
    RelativeLayout relButtonClasses;//классы
    RelativeLayout relButtonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        relButtonNow = (RelativeLayout) findViewById(R.id.start_menu_button_now);
        relButtonSchedule = (RelativeLayout) findViewById(R.id.start_menu_button_schedule);
        relButtonCabinets = (RelativeLayout) findViewById(R.id.start_menu_button_my_cabinets);
        relButtonClasses = (RelativeLayout) findViewById(R.id.start_menu_button_my_classes);
        relButtonSettings = (RelativeLayout) findViewById(R.id.start_menu_button_reload);

        relButtonNow.setOnClickListener(this);
        relButtonSchedule.setOnClickListener(this);
        relButtonCabinets.setOnClickListener(this);
        relButtonClasses.setOnClickListener(this);
        relButtonSettings.setOnClickListener(this);

        //setTitle("помощник учителя");
    }

    @Override
    protected void onStart() {
        //при входе на эту активность
        //проверка на запись настроек
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor settingCursor = db.getSettingProfileById(1);//получаем первый профиль настроек
        Log.e("TeachersApp","settingCursor.getCount() = "+settingCursor.getCount());
        if(settingCursor.getCount() == 0){//если нет профиля настроек
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
            case R.id.start_menu_button_now: {//запуск текущего урока
                intent = new Intent(this, LessonActivity.class);
                GregorianCalendar currentCalendar = new GregorianCalendar();//получаем текущее время
                currentCalendar.setTime(new Date());
                DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                long attitudeId = db.getSubjectAndTimeCabinetAttitudeIdByTime(currentCalendar);
                if (attitudeId == -1) {
                    Toast toast = Toast.makeText(this,"на текущий момент нет доступных уроков",Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    intent.putExtra(LessonActivity.LESSON_ATTITUDE_ID, attitudeId);
                    startActivity(intent);
                }
            }
            break;
            case R.id.start_menu_button_schedule://переход в список расписаний
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
            case R.id.start_menu_button_my_cabinets://переход в список кабинетов
                intent = new Intent(this, ListOfActivity.class);
                intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
                startActivity(intent);
                break;
            case R.id.start_menu_button_my_classes: {//переход в список классов
                intent = new Intent(this, ListOfActivity.class);
                intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                startActivity(intent);
                break;
            }
            case R.id.start_menu_button_reload: {
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
