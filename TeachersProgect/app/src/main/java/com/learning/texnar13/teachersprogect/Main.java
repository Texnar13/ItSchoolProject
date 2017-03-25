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

public class Main extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        {
            long classId = db.createClass("a1");

            long lerner1Id = db.createLearner("Зинченко", "Сократ", classId);
            long lerner2Id = db.createLearner("Шумякин", "Феофан", classId);
            long lerner3Id = db.createLearner("Рябец", "Валентин", classId);
            long lerner4Id = db.createLearner("Гроша", "Любава", classId);
            long lerner5Id = db.createLearner("Авдонина", "Алиса", classId);

            long cabinetId = db.createCabinet("кабинет 406");


            ArrayList<Long> desks = new ArrayList<Long>();
            ArrayList<Long> places = new ArrayList<Long>();
            {
                long desk1Id = db.createDesk(2, 160, 200, cabinetId);//1
                places.add(db.createPlace(desk1Id));
                places.add(db.createPlace(desk1Id));
                desks.add(desk1Id);
            }
            {
                long desk2Id = db.createDesk(2, 40, 200, cabinetId);//2
                places.add(db.createPlace(desk2Id));
                places.add(db.createPlace(desk2Id));
                desks.add(desk2Id);
            }
            {
                long desk3Id = db.createDesk(2, 160, 120, cabinetId);//3
                places.add(db.createPlace(desk3Id));
                places.add(db.createPlace(desk3Id));
                desks.add(desk3Id);
            }
            {
                long desk4Id = db.createDesk(2, 40, 120, cabinetId);//4
                places.add(db.createPlace(desk4Id));
                places.add(db.createPlace(desk4Id));
                desks.add(desk4Id);
            }
            {
                long desk5Id = db.createDesk(2, 160, 40, cabinetId);//5
                places.add(db.createPlace(desk5Id));
                places.add(db.createPlace(desk5Id));
                desks.add(desk5Id);
            }
            {
                long desk6Id = db.createDesk(2, 40, 40, cabinetId);//6
                places.add(db.createPlace(desk6Id));
                places.add(db.createPlace(desk6Id));
                desks.add(desk6Id);
            }
            //   |6|  |5|   |    |  |  |  |
            //   |4|  |3|   |    | 4|  |  |
            //   |2|  |1|   |    |35|  |21|
            //       [-]

            db.setLearnerOnPlace(lerner1Id, places.get(2));
            db.setLearnerOnPlace(lerner2Id, places.get(1));
            db.setLearnerOnPlace(lerner3Id, places.get(3));
            db.setLearnerOnPlace(lerner4Id, places.get(8));
            db.setLearnerOnPlace(lerner5Id, places.get(4));

            for (int i = 0; i < desks.size(); i++) {
                Log.w("MyLog",""+desks.get(i));
            }
        }

        //заполнили базу данных дальше только можем доставать данные


        ArrayList<Long> learnersId = new ArrayList<Long>();

        RelativeLayout room = (RelativeLayout) findViewById(R.id.room_layout);
        Cursor cursor = db.getDesksXYByClassId(1);
        cursor.moveToFirst();
        do {
            //создание парты
            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
            tempRelativeLayoutDesk.setBackgroundColor(Color.parseColor("#bce4af00"));

            RelativeLayout.LayoutParams tempLayoutParams = new RelativeLayout.LayoutParams((int) dpFromPx(80), (int) dpFromPx(40));
            tempLayoutParams.leftMargin = (int) dpFromPx(cursor.getLong(cursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)));
            tempLayoutParams.topMargin = (int) dpFromPx(cursor.getLong(cursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)));
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

//            //создание места
//            Cursor cursorPlace = db.getPlacesIdByDeskId(cursor.getLong(cursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
//            cursor.moveToFirst();
//            do {
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
//            } while (cursorPlace.moveToNext());

            //добавление парты уже с местами
            room.addView(tempRelativeLayoutDesk, tempLayoutParams);
        } while (cursor.moveToNext());

        db.close();
    }

    private float dpFromPx(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
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
