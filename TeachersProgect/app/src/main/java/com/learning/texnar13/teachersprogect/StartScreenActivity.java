package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.listOf.ListOfActivity;

import java.util.ArrayList;
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
//09-16 23:11:20.094 4549-4549/com.learning.texnar13.teachersprogect E/AndroidRuntime: FATAL EXCEPTION: main
//        Process: com.learning.texnar13.teachersprogect, PID: 4549
//        java.lang.OutOfMemoryError: Failed to allocate a 43261452 byte allocation with 5103456 free bytes and 4MB until OOM
//        at dalvik.system.VMRuntime.newNonMovableArray(Native Method)
//        at android.graphics.BitmapFactory.nativeDecodeAsset(Native Method)
//        at android.graphics.BitmapFactory.decodeStream(BitmapFactory.java:856)
//        at android.graphics.BitmapFactory.decodeResourceStream(BitmapFactory.java:675)
//        at android.graphics.drawable.Drawable.createFromResourceStream(Drawable.java:2228)
//        at android.content.res.Resources.loadDrawableForCookie(Resources.java:4215)
//        at android.content.res.Resources.loadDrawable(Resources.java:4089)
//        at android.content.res.Resources.getDrawable(Resources.java:2005)
//        at android.content.res.Resources.getDrawable(Resources.java:1987)
//        at android.content.Context.getDrawable(Context.java:464)
//        at android.support.v4.content.ContextCompatApi21.getDrawable(ContextCompatApi21.java:30)
//        at android.support.v4.content.ContextCompat.getDrawable(ContextCompat.java:372)
//        at android.support.v7.widget.AppCompatDrawableManager.getDrawable(AppCompatDrawableManager.java:202)
//        at android.support.v7.widget.AppCompatDrawableManager.getDrawable(AppCompatDrawableManager.java:190)
//        at android.support.v7.content.res.AppCompatResources.getDrawable(AppCompatResources.java:100)
//        at android.support.v7.widget.AppCompatImageHelper.loadFromAttributes(AppCompatImageHelper.java:54)
//        at android.support.v7.widget.AppCompatImageView.<init>(AppCompatImageView.java:66)
//        at android.support.v7.widget.AppCompatImageView.<init>(AppCompatImageView.java:56)
//        at android.support.v7.app.AppCompatViewInflater.createView(AppCompatViewInflater.java:106)
//        at android.support.v7.app.AppCompatDelegateImplV9.createView(AppCompatDelegateImplV9.java:1029)
//        at android.support.v7.app.AppCompatDelegateImplV9.onCreateView(AppCompatDelegateImplV9.java:1087)
//        at android.support.v4.view.LayoutInflaterCompatHC$FactoryWrapperHC.onCreateView(LayoutInflaterCompatHC.java:47)
//        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:758)
//        at android.view.LayoutInflater.createViewFromTag(LayoutInflater.java:716)
//        at android.view.LayoutInflater.rInflate(LayoutInflater.java:847)
//        at android.view.LayoutInflater.rInflateChildren(LayoutInflater.java:810)
//        at android.view.LayoutInflater.rInflate(LayoutInflater.java:855)
//        at android.view.LayoutInflater.rInflateChildren(LayoutInflater.java:810)
//        at android.view.LayoutInflater.rInflate(LayoutInflater.java:855)
//        at android.view.LayoutInflater.rInflateChildren(LayoutInflater.java:810)
//        at android.view.LayoutInflater.rInflate(LayoutInflater.java:855)
//        at android.view.LayoutInflater.rInflateChildren(LayoutInflater.java:810)
//        at android.view.LayoutInflater.inflate(LayoutInflater.java:527)
//        at android.view.LayoutInflater.inflate(LayoutInflater.java:429)
//        at android.view.LayoutInflater.inflate(LayoutInflater.java:380)
//        at android.support.v7.app.AppCompatDelegateImplV9.setContentView(AppCompatDelegateImplV9.java:292)
//        at android.support.v7.app.AppCompatActivity.setContentView(AppCompatActivity.java:140)
//        at com.learning.texnar13.teachersprogect.StartScreenActivity.onCreate(StartScreenActivity.java:32)
//        at android.app.Activity.performCreate(Activity.java:6876)
//        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1135)
//        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3207)
//        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3350)
//        at android.app.ActivityThread.handleRelaunchActivity(ActivityThread.java:5395)
//        at android.app.ActivityThread.access$1200(ActivityThread.java:222)
//        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1801)
//        at android.os.Handler.dispatchMessage(Handler.java:102)
//        at android.os.Looper.loop(Looper.java:158)
//        at android.app.ActivityThread.main(ActivityThread.java:7237)
//        at java.lang.reflect.Method.invoke(Native Method)
//        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1230)
//        at com.andr
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

        setTitle("помощник учителя");

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.start_menu_button_now: {//запуск текущего урока
                intent = new Intent(this, LessonActivity.class);
                intent.putExtra(LessonActivity.LESSON_ATTITUDE_ID, (long) -1);//TODO сделать запуск текущего урока()расчеты вести здесь
                startActivity(intent);
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
                DataBaseOpenHelper dbOpenHelper = new DataBaseOpenHelper(this);
                dbOpenHelper.restartTable();

                dbOpenHelper.createClass("1\"A\"");
                long classId = dbOpenHelper.createClass("2\"A\"");

                long lerner1Id = dbOpenHelper.createLearner("Зинченко", "Сократ", classId);
                long lerner2Id = dbOpenHelper.createLearner("Шумякин", "Феофан", classId);
                long lerner3Id = dbOpenHelper.createLearner("Рябец", "Валентин", classId);
                long lerner4Id = dbOpenHelper.createLearner("Гроша", "Любава", classId);
                long lerner5Id = dbOpenHelper.createLearner("Авдонина", "Алиса", classId);


                long cabinetId = dbOpenHelper.createCabinet("406");

                ArrayList<Long> places = new ArrayList<>();
                {
                    long desk1Id = dbOpenHelper.createDesk(2, 160, 200, cabinetId);//1
                    places.add(dbOpenHelper.createPlace(desk1Id, 1));
                    places.add(dbOpenHelper.createPlace(desk1Id, 2));
                }
                {
                    long desk2Id = dbOpenHelper.createDesk(2, 40, 200, cabinetId);//2
                    places.add(dbOpenHelper.createPlace(desk2Id, 1));
                    places.add(dbOpenHelper.createPlace(desk2Id, 2));
                }
                {
                    long desk3Id = dbOpenHelper.createDesk(2, 160, 120, cabinetId);//3
                    places.add(dbOpenHelper.createPlace(desk3Id, 1));
                    places.add(dbOpenHelper.createPlace(desk3Id, 2));
                }
                {
                    long desk4Id = dbOpenHelper.createDesk(2, 40, 120, cabinetId);//4
                    places.add(dbOpenHelper.createPlace(desk4Id, 1));
                    places.add(dbOpenHelper.createPlace(desk4Id, 2));
                }
                {
                    long desk5Id = dbOpenHelper.createDesk(2, 160, 40, cabinetId);//5
                    places.add(dbOpenHelper.createPlace(desk5Id, 1));
                    places.add(dbOpenHelper.createPlace(desk5Id, 2));
                }
                {
                    long desk6Id = dbOpenHelper.createDesk(2, 40, 40, cabinetId);//6
                    places.add(dbOpenHelper.createPlace(desk6Id, 1));
                    places.add(dbOpenHelper.createPlace(desk6Id, 2));
                }
                //   |6|  |5|   |    |  |  |  |
                //   |4|  |3|   |    | 4|  |  |
                //   |2|  |1|   |    |35|  |21|
                //       [-]


                long lessonId = dbOpenHelper.createLesson("физика", classId
                        //, cabinetId
                );
                Date startLessonTime = new GregorianCalendar(2017, 8, 10, 8, 30).getTime();//1502343000000 --10 августа
                Date endLessonTime = new GregorianCalendar(2017, 8, 10, 9, 15).getTime();//на 7 месяц  1502345700000
                dbOpenHelper.setLessonTimeAndCabinet(lessonId,cabinetId, startLessonTime, endLessonTime);


                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner1Id, places.get(1));
                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner2Id, places.get(0));
                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner3Id, places.get(2));
                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner4Id, places.get(7));
                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner5Id, places.get(3));
                dbOpenHelper.close();
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
