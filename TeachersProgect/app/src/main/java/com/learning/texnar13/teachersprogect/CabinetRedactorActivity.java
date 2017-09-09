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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class CabinetRedactorActivity extends AppCompatActivity implements View.OnTouchListener {

    public static final String EDITED_OBJECT_ID = "id";//ID редактируемого обьекта
    final ArrayList<CabinetRedactorPoint> deskCoordinatesList = new ArrayList<>();
    int multiplier = 0;//множитель, задаётся с физических размеров экрана
    long checkedDeskId;
    RelativeLayout instrumentalImageRelative;

    //
    RelativeLayout relativeLayout;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_redactor);
        RelativeLayout out = (RelativeLayout) findViewById(R.id.redactor_out);
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

        final long cabinetId = getIntent().getLongExtra(EDITED_OBJECT_ID, 1);//получаем id кабинета
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
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        if (metrics.widthPixels > metrics.heightPixels) {
            multiplier = metrics.heightPixels / 250;//500
        } else {
            multiplier = metrics.widthPixels / 250;
        }

        instrumentalImageRelative = (RelativeLayout) findViewById(R.id.activity_cabinet_redactor_instrumental_relative);
//        instrumentalImageRelative.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                deskCoordinatesList.add(new CabinetRedactorPoint(
//                        db.createDesk(2, metrics.widthPixels - 40 * multiplier,
//                                metrics.heightPixels - 20 * multiplier, cabinetId),
//                        new RelativeLayout(this),
//                        deskX, deskY));
//            }
//        });todo создание

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
                        instrumentalImageRelative.removeAllViews();
                        ImageView image = new ImageView(this);
                        image.setImageResource(R.drawable.ic_delete);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        instrumentalImageRelative.addView(image, params);

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
                        deskLayoutParams.leftMargin = (int) (motionEvent.getX() - 40 * multiplier);
                        deskLayoutParams.topMargin = (int) (motionEvent.getY() - 20 * multiplier);
                        deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if (deskCoordinatesList.get(i).deskId == checkedDeskId) {
                        instrumentalImageRelative.removeAllViews();
                        ImageView image = new ImageView(this);
                        image.setImageResource(R.drawable.ic_input_add);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        instrumentalImageRelative.addView(image, params);
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