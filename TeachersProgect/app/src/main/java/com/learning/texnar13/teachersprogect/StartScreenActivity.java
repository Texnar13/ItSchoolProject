package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;
import com.learning.texnar13.teachersprogect.listOf.ListOfActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class StartScreenActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonNow;//текущий урок
    Button buttonSchedule;//расписание
    Button buttonCabinets;//кабинеты
    Button buttonClasses;//классы
    Button tempButtonSeatingRedactor;
    Button reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        buttonNow = (Button) findViewById(R.id.start_menu_button_now);
        buttonSchedule = (Button) findViewById(R.id.start_menu_button_schedule);
        buttonCabinets = (Button) findViewById(R.id.start_menu_button_my_cabinets);
        buttonClasses = (Button) findViewById(R.id.start_menu_button_my_classes);
        tempButtonSeatingRedactor = (Button) findViewById(R.id.start_menu_button_temp_seating_redactor);
        reload = (Button) findViewById(R.id.start_menu_button_reload);

        buttonNow.setOnClickListener(this);
        buttonSchedule.setOnClickListener(this);
        buttonCabinets.setOnClickListener(this);
        buttonClasses.setOnClickListener(this);
        tempButtonSeatingRedactor.setOnClickListener(this);
        reload.setOnClickListener(this);

        getSupportActionBar().setTitle("помощник учителя");

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
            case R.id.start_menu_button_temp_seating_redactor: {
                intent = new Intent(this, SeatingRedactorActivity.class);
                intent.putExtra(SeatingRedactorActivity.CABINET_ID,1L);
                intent.putExtra(SeatingRedactorActivity.CLASS_ID,2L);
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

                ArrayList<Long> desks = new ArrayList<Long>();
                ArrayList<Long> places = new ArrayList<Long>();
                {
                    long desk1Id = dbOpenHelper.createDesk(2, 160, 200, cabinetId);//1
                    places.add(dbOpenHelper.createPlace(desk1Id, 1));
                    places.add(dbOpenHelper.createPlace(desk1Id, 2));
                    desks.add(desk1Id);
                }
                {
                    long desk2Id = dbOpenHelper.createDesk(2, 40, 200, cabinetId);//2
                    places.add(dbOpenHelper.createPlace(desk2Id, 1));
                    places.add(dbOpenHelper.createPlace(desk2Id, 2));
                    desks.add(desk2Id);
                }
                {
                    long desk3Id = dbOpenHelper.createDesk(2, 160, 120, cabinetId);//3
                    places.add(dbOpenHelper.createPlace(desk3Id, 1));
                    places.add(dbOpenHelper.createPlace(desk3Id, 2));
                    desks.add(desk3Id);
                }
                {
                    long desk4Id = dbOpenHelper.createDesk(2, 40, 120, cabinetId);//4
                    places.add(dbOpenHelper.createPlace(desk4Id, 1));
                    places.add(dbOpenHelper.createPlace(desk4Id, 2));
                    desks.add(desk4Id);
                }
                {
                    long desk5Id = dbOpenHelper.createDesk(2, 160, 40, cabinetId);//5
                    places.add(dbOpenHelper.createPlace(desk5Id, 1));
                    places.add(dbOpenHelper.createPlace(desk5Id, 2));
                    desks.add(desk5Id);
                }
                {
                    long desk6Id = dbOpenHelper.createDesk(2, 40, 40, cabinetId);//6
                    places.add(dbOpenHelper.createPlace(desk6Id, 1));
                    places.add(dbOpenHelper.createPlace(desk6Id, 2));
                    desks.add(desk6Id);
                }
                //   |6|  |5|   |    |  |  |  |
                //   |4|  |3|   |    | 4|  |  |
                //   |2|  |1|   |    |35|  |21|
                //       [-]


                long lessonId = dbOpenHelper.createLesson("физика", classId
                        //, cabinetId
                );
                Date startLessonTime = new GregorianCalendar(2017, 7, 10, 8, 30).getTime();//1502343000000 --10 августа
                Date endLessonTime = new GregorianCalendar(2017, 7, 10, 9, 15).getTime();//  1502345700000
                long lessonTimeId = dbOpenHelper.setLessonTimeAndCabinet(lessonId,cabinetId, startLessonTime, endLessonTime);


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
