package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class LessonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO 3 это класс урока
        /*
        * вывод учеников, парт и мест, по нажатию наученика открывается доп.меню*
        где можно выбрать оценку, нет оценки/1/2/3/4/5
        после окончания урока учитель нажимает закончить урок
        выводим стастику за весь урок(2)
        *
        *
        *
        *    _________________________________
        *   |название урока     закончить урок|
        *   |_________________________________|
        *   |                                 |
        *   |                                 |
        *   |    _______         _______      |
        *   |   |имя|имя|       |имя|имя|     |
        *   |   |фам|фам|       |фам|фам|     |
        *   |    _______         _______      |
        *   |   |имя|имя|       |имя|имя|     |
        *   |   |фам|фам|       |фам|фам|     |
        *   |    _______         _______      |
        *   |   |имя|имя|       |имя|имя|     |
        *   |   |фам|фам|       |фам|фам|     |
        *   |    _______         _______      |
        *   |   |имя|имя|       |имя|имя|     |
        *   |   |фам|фам|       |фам|фам|     |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |_________________________________|
        *
        *
        *
        * (2)
        *    _________________________________
        *   |  имя урока            сохранить |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |_________________________________|
        *
        *
        * нажимаем сохранить и сохраняем в таблицу ученик-оценка
        *
        *
        *
        *
        *
        *
        * */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataBaseOpenHelper dbOpenHelper = new DataBaseOpenHelper(this);

        generate(dbOpenHelper);//начальные данные (для отладки)

        RelativeLayout room = (RelativeLayout) findViewById(R.id.room_layout);
        Cursor cursor = dbOpenHelper.getDesksByCabinetId(2);
        while (cursor.moveToNext()) {
            //создание парты
            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
            tempRelativeLayoutDesk.setBackgroundColor(Color.parseColor("#bce4af00"));

            RelativeLayout.LayoutParams tempLayoutParams = new RelativeLayout.LayoutParams((int) dpFromPx(80), (int) dpFromPx(40));
            tempLayoutParams.leftMargin = (int) dpFromPx(cursor.getLong(cursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)));
            tempLayoutParams.topMargin = (int) dpFromPx(cursor.getLong(cursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)));
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            Log.i("LessonActivity", "view desk:" + cursor.getLong(cursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));

            //создание места
            Cursor cursorPlace = dbOpenHelper.getPlacesByDeskId(cursor.getLong(cursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
            while (cursorPlace.moveToNext()) {
                Log.i("LessonActivity", "view place:" + cursorPlace.getLong(cursorPlace.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));
//                RelativeLayout tempRelativeLayoutPlace = new RelativeLayout(this);
//                tempRelativeLayoutDesk.setBackgroundColor(Color.parseColor("#bc8e6d02"));
//
//                RelativeLayout.LayoutParams tempPlaceLayoutParams = new RelativeLayout.LayoutParams((int) dpFromPx(30), (int) dpFromPx(30));
//                tempLayoutParams.leftMargin = (int) dpFromPx(5);
//                tempLayoutParams.topMargin = (int) dpFromPx(5);
//                tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//
//                tempRelativeLayoutDesk.addView(tempRelativeLayoutPlace, tempPlaceLayoutParams);
            }
            cursorPlace.close();

            //добавление парты уже с местами
            room.addView(tempRelativeLayoutDesk, tempLayoutParams);
        }
        cursor.close();
        dbOpenHelper.close();
    }

    private float dpFromPx(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private void generate(DataBaseOpenHelper dbOpenHelper) {
        dbOpenHelper.restartTable();
        dbOpenHelper.createClass("a1");
        long classId = dbOpenHelper.createClass("a2");

        long lerner1Id = dbOpenHelper.createLearner("Зинченко", "Сократ", classId);
        long lerner2Id = dbOpenHelper.createLearner("Шумякин", "Феофан", classId);
        long lerner3Id = dbOpenHelper.createLearner("Рябец", "Валентин", classId);
        long lerner4Id = dbOpenHelper.createLearner("Гроша", "Любава", classId);
        long lerner5Id = dbOpenHelper.createLearner("Авдонина", "Алиса", classId);

        long cabinetId = dbOpenHelper.createCabinet("кабинет 406");


        ArrayList<Long> desks = new ArrayList<Long>();
        ArrayList<Long> places = new ArrayList<Long>();
        {
            long desk1Id = dbOpenHelper.createDesk(2, 160, 200, cabinetId);//1
            places.add(dbOpenHelper.createPlace(desk1Id));
            places.add(dbOpenHelper.createPlace(desk1Id));
            desks.add(desk1Id);
        }
        {
            long desk2Id = dbOpenHelper.createDesk(2, 40, 200, cabinetId);//2
            places.add(dbOpenHelper.createPlace(desk2Id));
            places.add(dbOpenHelper.createPlace(desk2Id));
            desks.add(desk2Id);
        }
        {
            long desk3Id = dbOpenHelper.createDesk(2, 160, 120, cabinetId);//3
            places.add(dbOpenHelper.createPlace(desk3Id));
            places.add(dbOpenHelper.createPlace(desk3Id));
            desks.add(desk3Id);
        }
        {
            long desk4Id = dbOpenHelper.createDesk(2, 40, 120, cabinetId);//4
            places.add(dbOpenHelper.createPlace(desk4Id));
            places.add(dbOpenHelper.createPlace(desk4Id));
            desks.add(desk4Id);
        }
        {
            long desk5Id = dbOpenHelper.createDesk(2, 160, 40, cabinetId);//5
            places.add(dbOpenHelper.createPlace(desk5Id));
            places.add(dbOpenHelper.createPlace(desk5Id));
            desks.add(desk5Id);
        }
        {
            long desk6Id = dbOpenHelper.createDesk(2, 40, 40, cabinetId);//6
            places.add(dbOpenHelper.createPlace(desk6Id));
            places.add(dbOpenHelper.createPlace(desk6Id));
            desks.add(desk6Id);
        }
        //   |6|  |5|   |    |  |  |  |
        //   |4|  |3|   |    | 4|  |  |
        //   |2|  |1|   |    |35|  |21|
        //       [-]

        dbOpenHelper.setLearnerOnPlace(lerner1Id, places.get(2));
        dbOpenHelper.setLearnerOnPlace(lerner2Id, places.get(1));
        dbOpenHelper.setLearnerOnPlace(lerner3Id, places.get(3));
        dbOpenHelper.setLearnerOnPlace(lerner4Id, places.get(8));
        dbOpenHelper.setLearnerOnPlace(lerner5Id, places.get(4));

    }
}

/*
 ArrayList<SchoolClass> NAME_TABLE_CLASSES;
        {
            ArrayList<Learner> a1 = new ArrayList<>();
            Зинченко Сократ
            Шумякин Феофан
            Рябец Валентин
            Гроша Любава
            Авдонина Алиса

            ArrayList<Learner> a2 = new ArrayList<>();
            a2.add(new Learner("Воробьев Прокофий"));//4
            a2.add(new Learner("Родзянко Леондий"));//5
            a2.add(new Learner("Таттар Ульян"));//6
            a2.add(new Learner("Завразина Ксения"));//23
            a2.add(new Learner("Чепурина Валентина"));//24

            ArrayList<Learner> a3 = new ArrayList<>();
            a3.add(new Learner("Юсупов Серафим"));//7
            a3.add(new Learner("Кайназаров Демьян"));//8
            a3.add(new Learner("Малыхин Гаврила"));//9
            a3.add(new Learner("Лутугина Аза"));//25
            a3.add(new Learner("Лаврентьева Ирина"));//26

            ArrayList<Learner> a4 = new ArrayList<>();
            a4.add(new Learner("Дегтярев Василий"));//10
            a4.add(new Learner("Шастин Афанасий"));//11
            a4.add(new Learner("Челомцев Евграф"));//12
            a4.add(new Learner("Краснокутская Варвара"));//27
            a4.add(new Learner("Живкова Антонина"));//28

            ArrayList<Learner> b1 = new ArrayList<>();
            b1.add(new Learner("Русанов Владлен"));//13
            b1.add(new Learner("Виноградов Радион"));//14
            b1.add(new Learner("Фукин Лукьян"));//15
            b1.add(new Learner("Бессонова Ольга"));//29
            b1.add(new Learner("Витковская Эвелина"));//30

            ArrayList<Learner> b2 = new ArrayList<>();
            b2.add(new Learner("Новомейский Казимир"));//16
            b2.add(new Learner("Шайнюк Александр"));//17
            b2.add(new Learner("Кошляк Николай"));//18
            b2.add(new Learner("Артёмова Каролина"));//31
            b2.add(new Learner("Косыгина Ева"));//32

            ArrayList<Learner> c1 = new ArrayList<>();
            c1.add(new Learner("Сальков Архип"));//19
            c1.add(new Learner("Шаньгин Игорь"));//20
            c1.add(new Learner("Ясинская Анна"));//33
            c1.add(new Learner("Ярцева Лариса"));//34
            c1.add(new Learner("Турбина Александра"));//35

            NAME_TABLE_CLASSES = new ArrayList<>();
            NAME_TABLE_CLASSES.add(new SchoolClass("a1", a1));
            NAME_TABLE_CLASSES.add(new SchoolClass("a2", a2));
            NAME_TABLE_CLASSES.add(new SchoolClass("a3", a3));
            NAME_TABLE_CLASSES.add(new SchoolClass("a4", a4));
            NAME_TABLE_CLASSES.add(new SchoolClass("b1", b1));
            NAME_TABLE_CLASSES.add(new SchoolClass("b2", b2));
            NAME_TABLE_CLASSES.add(new SchoolClass("c1", c1));
        }

        Cabinet cabinet1;
        {
            ArrayList<Desk> NAME_TABLE_DESKS = new ArrayList<Desk>();
            NAME_TABLE_DESKS.add(new Desk(1, 160, 200, 2));//1
            NAME_TABLE_DESKS.add(new Desk(2, 40, 200, 2));//2
            NAME_TABLE_DESKS.add(new Desk(3, 160, 120, 2));//3
            NAME_TABLE_DESKS.add(new Desk(4, 40, 120, 2));//4
            NAME_TABLE_DESKS.add(new Desk(5, 160, 40, 2));//5
            NAME_TABLE_DESKS.add(new Desk(6, 40, 40, 2));//6
            //   |6|  |5|
            //   |4|  |3|
            //   |2|  |1|
            //       [-]

            cabinet1 = new Cabinet("cab1", NAME_TABLE_DESKS, 0);
        }

        RelativeLayout room = (RelativeLayout) findViewById(R.id.room_layout);
        for (int i = 0; i < cabinet1.NAME_TABLE_DESKS.size(); i++) {
            RelativeLayout tempRelativeLayout = new RelativeLayout(this);
            tempRelativeLayout.setBackgroundColor(Color.parseColor("#bce4af00"));

            RelativeLayout.LayoutParams tempLayoutParams = new RelativeLayout.LayoutParams((int) dpFromPx(80), (int) dpFromPx(40));
            tempLayoutParams.leftMargin = (int) dpFromPx(cabinet1.NAME_TABLE_DESKS.get(i).getCoordinateX());
            tempLayoutParams.topMargin = (int) dpFromPx(cabinet1.NAME_TABLE_DESKS.get(i).getCoordinateY());
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

            room.addView(tempRelativeLayout, tempLayoutParams);
        }
 */
