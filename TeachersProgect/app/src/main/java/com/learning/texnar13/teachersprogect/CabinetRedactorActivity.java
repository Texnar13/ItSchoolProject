package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class CabinetRedactorActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    public static final String EDITED_OBJECT_ID = "id";//ID редактируемого обьекта
    ArrayList<CabinetRedactorPoint> deskCoordinatesList = new ArrayList<>();
    int multiplier = 0;//множитель, задаётся с физических размеров экрана
    long checkedDeskId;
    ImageView instrumentalImage;
    RelativeLayout out;
    long cabinetId;
    DisplayMetrics metrics;
    //
    RelativeLayout relativeLayout;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_redactor);
        out = (RelativeLayout) findViewById(R.id.redactor_out);
        //TODO 2 это редактор кабинетов, пока понадобятся только двухместные парты,
//        /* выводим все парты находящиеся в этом кабинете
//        * при нажатии на картинку "плюс" в центре экрана появляется парта,
//        * которую перетаскиванием можно установить на нужное место
//        * парту можно удалить перетащив на картинку "корзина" или долгим нажатием, незнаю как будет удобнее реализовать
//        *    _________________________________
//        *   |                             сохр|
//        *   |_________________________________|
//        *   |   _                             |
//        *   |  |К|                       +    |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |             новая               |
//        *   |             _______             |
//        *   |            |   |   |            |
//        *   |            |___|___|            |
//        *   |                                 |
//        *   |                                 |
//        *   |    _______                      |
//        *   |   |   |   |                     |
//        *   |   |___|___|                     |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |_________________________________|
//        *
//        * в меню есть кнопка сохранить, если какие-то парты удалениы они удаляются из таблицы и добавленные парты
//        добавляются в таблицу + обязательно добавляются два новых места(если парта двухместная)
//        *
//        *при удалении парты каскадом удаляются все места ссылающиеся на неё и все зависимости ученик - место, вверх удаление не идёт
//        *
//        * -----------------------------------------------------------------------
//        */

        //какой view появился позже тот и отображаться будет выше

        cabinetId = getIntent().getLongExtra(EDITED_OBJECT_ID, 1);//получаем id кабинета
        Log.i("TeachersApp", "CabinetRedactorActivity - onCreate editedObjectId = " + cabinetId);
        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);//доступ к базе данных

        Cursor cabinetCursor = db.getCabinets(cabinetId);
        cabinetCursor.moveToFirst();
        setTitle("редактирование кабинета \"" +
                cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)) +
                "\"");
        cabinetCursor.close();

        //узнаём размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);

//        if (metrics.widthPixels > metrics.heightPixels) {
//            multiplier = metrics.heightPixels / 250;//500
//        } else {
//            multiplier = metrics.widthPixels / 250;
//        }
        multiplier = 5;

        instrumentalImage = (ImageView) findViewById(R.id.activity_cabinet_redactor_instrumental_image);
        instrumentalImage.setOnClickListener(this);//todo создание

        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);//курсор с партами
        while (desksCursor.moveToNext()) {//начальный вывод парт
            long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID));
            long deskX = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X));
            long deskY = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y));
            final RelativeLayout deskLayout = new RelativeLayout(this);
            deskCoordinatesList.add(new CabinetRedactorPoint(deskId, deskLayout, deskX, deskY));
            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams(80 * multiplier, 40 * multiplier);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            deskLayoutParams.leftMargin = (int) deskX * multiplier;
            deskLayoutParams.topMargin = (int) deskY * multiplier;
            deskLayout.setLayoutParams(deskLayoutParams);
            deskLayout.setBackgroundColor(Color.GRAY);
            out.addView(deskLayout);
        }

        //ставим id для view и по нему id парты; onTouch один для всех

        relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(8, 4);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        layoutParams.leftMargin = 50;
        layoutParams.topMargin = 50;

        relativeLayout.setLayoutParams(layoutParams);
        out.setOnTouchListener(this);
        relativeLayout.setBackgroundColor(Color.RED);
        out.addView(relativeLayout);

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        //
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(8, 4);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        //

        RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams(80 * multiplier, 40 * multiplier);
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //
                layoutParams.leftMargin = (int) (motionEvent.getX());
                layoutParams.topMargin = (int) (motionEvent.getY());
                relativeLayout.setLayoutParams(layoutParams);
                //
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if ((motionEvent.getX() >= deskCoordinatesList.get(i).x * multiplier) &&
                            (motionEvent.getX() <= (deskCoordinatesList.get(i).x + 80) * multiplier) &&
                            (motionEvent.getY() >= deskCoordinatesList.get(i).y * multiplier) &&
                            (motionEvent.getY() <= (deskCoordinatesList.get(i).y + 40) * multiplier)) {

                        instrumentalImage.setImageResource(R.drawable.ic_delete);

                        checkedDeskId = deskCoordinatesList.get(i).deskId;
                        //совмещаем точку нажатия и центр(-40;-20) парты
                        deskLayoutParams.leftMargin = (int) (motionEvent.getX() - 40 * multiplier);
                        deskLayoutParams.topMargin = (int) (motionEvent.getY() - 20 * multiplier);
                        deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE://старые + новая - x нажатия
                //
                layoutParams.leftMargin = (int) (motionEvent.getX());
                layoutParams.topMargin = (int) (motionEvent.getY());
                relativeLayout.setLayoutParams(layoutParams);
                //
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if (deskCoordinatesList.get(i).deskId == checkedDeskId) {
                        //если палец находится в пределах крестика то удаляем парту
                        if ((motionEvent.getX() >= out.getWidth() / 2 - 25 * getApplicationContext().getResources().getDisplayMetrics().density) &&
                                (motionEvent.getX() <= out.getWidth() / 2 + 25 * getApplicationContext().getResources().getDisplayMetrics().density) &&
                                (motionEvent.getY() >= out.getHeight() - 50 * getApplicationContext().getResources().getDisplayMetrics().density) &&
                                (motionEvent.getY() <= out.getHeight())) {
                            Log.i("TeachersApp", "yay!");
                            //удаляем парту
                            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                            db.deleteDesk(deskCoordinatesList.get(i).deskId);
                            out.removeView(deskCoordinatesList.get(i).desk);
                            deskCoordinatesList.remove(i);
                        } else {

                            deskLayoutParams.leftMargin = (int) (motionEvent.getX() - 40 * multiplier);
                            deskLayoutParams.topMargin = (int) (motionEvent.getY() - 20 * multiplier);
                            deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if (deskCoordinatesList.get(i).deskId == checkedDeskId) {

                        instrumentalImage.setImageResource(R.drawable.ic_input_add);

                        deskCoordinatesList.get(i).x = (long) ((motionEvent.getX() - 40 * multiplier) / multiplier);
                        deskCoordinatesList.get(i).y = (long) ((motionEvent.getY() - 20 * multiplier) / multiplier);
                        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                        db.setDeskCoordinates(deskCoordinatesList.get(i).deskId,
                                deskCoordinatesList.get(i).x,
                                deskCoordinatesList.get(i).y);
                    }
                }
                Log.i("TeachersApp", "X = " + motionEvent.getX() +
                        " ; Y = " + motionEvent.getY() + " ;");
                checkedDeskId = -1;
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        //todo места к парте
        // узнаем размеры экрана из класса Display
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long deskId = db.createDesk(2, (metricsB.widthPixels / 2) / multiplier, (metricsB.heightPixels / 2) / multiplier, cabinetId);

        db.createPlace(deskId, 1);
        db.createPlace(deskId, 2);

        RelativeLayout newDeskLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams(80 * multiplier, 40 * multiplier);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        newDeskLayoutParams.leftMargin = (metricsB.widthPixels / 2);
        newDeskLayoutParams.topMargin = (metricsB.heightPixels / 2);
        newDeskLayout.setLayoutParams(newDeskLayoutParams);
        newDeskLayout.setBackgroundColor(Color.GRAY);

        deskCoordinatesList.add(new CabinetRedactorPoint(deskId, newDeskLayout, (metricsB.widthPixels / 2) / multiplier, (metricsB.heightPixels / 2) / multiplier));

        out.addView(newDeskLayout);

    }
}

class CabinetRedactorPoint {
    long deskId;
    RelativeLayout desk;
    long x;
    long y;

    CabinetRedactorPoint(long id, RelativeLayout relativeDesk, long x, long y) {
        this.deskId = id;
        this.desk = relativeDesk;
        this.x = x;
        this.y = y;
    }
}