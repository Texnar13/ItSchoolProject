package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class CabinetRedactorActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    //--константы--
    // ID редактируемого обьекта
    public static final String EDITED_OBJECT_ID = "id";
    // шаг клетки
    static final float GIRD_SPACING = 60F;

    ArrayList<CabinetRedactorPoint> deskCoordinatesList = new ArrayList<>();
    float multiplier = 0;//множитель
    long checkedDeskId;
    ImageView instrumentalImage;
    RelativeLayout out;
    long cabinetId;
    TextView stateText;
//    //
//    RelativeLayout relativeLayout;
//    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_redactor);
        out = (RelativeLayout) findViewById(R.id.redactor_out);

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        //коэффициент размера интерфейса
        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;

        Log.i("TeachersApp", "" + multiplier);
        //кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

////----------------вывод сетки---------------
//
//        int x = 0;
//        int y = 0;
//        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
//        //пробежка по x
//        while (x < displaymetrics.widthPixels) {
//            //вывод вертикальной полосы
//            x = x + (int) GIRD_SPACING;
//            final RelativeLayout verticalLine = new RelativeLayout(this);
//            RelativeLayout.LayoutParams verticalLineLayoutParams = new RelativeLayout.LayoutParams(
//                    3,
//                    displaymetrics.heightPixels
//            );
//            verticalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//            verticalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            verticalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//            //deskLayout.setRotation(60);
//            verticalLineLayoutParams.leftMargin = x;
//            verticalLineLayoutParams.topMargin = 0;
//            verticalLine.setLayoutParams(verticalLineLayoutParams);
//            verticalLine.setBackgroundColor(Color.LTGRAY);
//            out.addView(verticalLine);
//        }
//        //пробежка по y
//        while (y < displaymetrics.heightPixels) {
//            //вывод горизонтальной полосы
//            y = y + (int) GIRD_SPACING;
//            final RelativeLayout horizontalLine = new RelativeLayout(this);
//            RelativeLayout.LayoutParams horizontalLineLayoutParams = new RelativeLayout.LayoutParams(
//                    displaymetrics.heightPixels,
//                    3
//            );
//            horizontalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//            horizontalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            horizontalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//            //deskLayout.setRotation(60);
//            horizontalLineLayoutParams.leftMargin = 0;
//            horizontalLineLayoutParams.topMargin = y;
//            horizontalLine.setLayoutParams(horizontalLineLayoutParams);
//            horizontalLine.setBackgroundColor(Color.LTGRAY);
//            out.addView(horizontalLine);
//
//
//        }


        //какой view появился позже тот и отображаться будет выше

        cabinetId = getIntent().getLongExtra(EDITED_OBJECT_ID, 1);//получаем id кабинета
        Log.i("TeachersApp", "CabinetRedactorActivity - onCreate editedObjectId = " + cabinetId);

        Cursor cabinetCursor = db.getCabinets(cabinetId);
        cabinetCursor.moveToFirst();
        setTitle("парты в \"" +
                cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)) +
                "\"");
        cabinetCursor.close();


        instrumentalImage = (ImageView) findViewById(R.id.activity_cabinet_redactor_instrumental_image);
        instrumentalImage.setOnClickListener(this);

        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);//курсор с партами
        while (desksCursor.moveToNext()) {//начальный вывод парт
            long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID));
            long deskX = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X));
            long deskY = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y));
            final RelativeLayout deskLayout = new RelativeLayout(this);
            deskCoordinatesList.add(new CabinetRedactorPoint(deskId, deskLayout, deskX, deskY));
            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(2000 * multiplier), (int) pxFromDp(1000 * multiplier));
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            //deskLayout.setRotation(60);
            deskLayoutParams.leftMargin = (int) pxFromDp(deskX * 25 * multiplier);
            deskLayoutParams.topMargin = (int) pxFromDp(deskY * 25 * multiplier);
            deskLayout.setLayoutParams(deskLayoutParams);
            deskLayout.setBackgroundColor(Color.parseColor("#f1bd7d"));
            out.addView(deskLayout);
        }
        desksCursor.close();

        //текст в центре
        stateText = (TextView) findViewById(R.id.cabinet_redactor_state_text);
        stateText.setText("Добавьте парту нажатием на '+', и поставьте ее так как она стоит в кабинете. Для удаления нажмите на парту и перетащите ее к появившемуся красному крестику");
        //если парт нет, то показываем текст
        if (deskCoordinatesList.size() == 0) {
            stateText.setText("Добавьте парту нажатием на '+', и поставьте ее так как она стоит в кабинете. Для удаления нажмите на парту и перетащите ее к появившемуся красному крестику");
        } else {
            stateText.setText("");
        }

//---инициализируем кнопки zoom---
        final ZoomControls zoomControls = (ZoomControls) findViewById(R.id.cabinet_redactor_zoom_controls);
        //приближение
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //--изменяем размер--
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());


                //проверяем можем ли изменять
                int last = (int) db.getInterfaceSizeBySettingsProfileId(1);
                if (last < 96) {
                    db.setSettingsProfileParameters(
                            1,
                            "default",
                            last + 3
                    );
                    db.close();

                    //активируем другую если приближать можно
                    zoomControls.setIsZoomOutEnabled(true);


                    //выводим все
                    multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        //создаем новые параметры
                        RelativeLayout.LayoutParams deskLayoutParams =
                                new RelativeLayout.LayoutParams(
                                        (int) pxFromDp(2000 * multiplier),
                                        (int) pxFromDp(1000 * multiplier)
                                );
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        deskLayoutParams.leftMargin = (int) pxFromDp(
                                deskCoordinatesList.get(i).x * 25 * multiplier
                        );
                        deskLayoutParams.topMargin = (int) pxFromDp(
                                deskCoordinatesList.get(i).y * 25 * multiplier
                        );
                        //и присваиваем из партам
                        deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                    }
                } else {//деактивируем кнопку если приближать нельзя
                    zoomControls.setIsZoomInEnabled(false);
                }
            }
        });
        //отдаление
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //изменяем размер
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                //проверяем можем ли изменять
                int last = (int) db.getInterfaceSizeBySettingsProfileId(1);
                if (last > 5) {
                    db.setSettingsProfileParameters(
                            1,
                            "default",
                            (int) db.getInterfaceSizeBySettingsProfileId(1) - 3
                    );
                    db.close();

                    //активируем другую если приближать можно
                    zoomControls.setIsZoomInEnabled(true);

                    //выводим все
                    multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        //создаем новые параметры
                        RelativeLayout.LayoutParams deskLayoutParams =
                                new RelativeLayout.LayoutParams(
                                        (int) pxFromDp(2000 * multiplier),
                                        (int) pxFromDp(1000 * multiplier)
                                );
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                        deskLayoutParams.leftMargin = (int) pxFromDp(
                                deskCoordinatesList.get(i).x * 25 * multiplier
                        );
                        deskLayoutParams.topMargin = (int) pxFromDp(
                                deskCoordinatesList.get(i).y * 25 * multiplier
                        );
                        //и присваиваем из партам
                        deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                    }
                } else {//деактивируем кнопку если отдалять нельзя
                    zoomControls.setIsZoomOutEnabled(false);
                }
            }
        });


//        //красная точка
//        relativeLayout = new RelativeLayout(this);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(8, 4);//фиксированный размер
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//        layoutParams.leftMargin = 50;
//        layoutParams.topMargin = 50;

        //relativeLayout.setLayoutParams(layoutParams);
        out.setOnTouchListener(this);
        //relativeLayout.setBackgroundColor(Color.RED);
        //out.addView(relativeLayout);

    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //если парт нет, то показываем текст
        if (deskCoordinatesList.size() == 0) {
            stateText.setText("Добавьте парту нажатием на '+', и поставьте ее так как она стоит в кабинете. Для удаления нажмите на парту и перетащите ее к появившемуся красному крестику");
        } else {
            stateText.setText("");
        }

        RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(2000 * multiplier), (int) pxFromDp(1000 * multiplier));
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if ((motionEvent.getX() >= pxFromDp(deskCoordinatesList.get(i).x * 25 * multiplier)) &&
                            (motionEvent.getX() <= pxFromDp((deskCoordinatesList.get(i).x * 25 + 2000) * multiplier)) &&
                            (motionEvent.getY() >= pxFromDp(deskCoordinatesList.get(i).y * 25 * multiplier)) &&
                            (motionEvent.getY() <= pxFromDp((deskCoordinatesList.get(i).y * 25 + 1000) * multiplier))) {

                        instrumentalImage.setImageResource(R.drawable.ic_delete);

                        checkedDeskId = deskCoordinatesList.get(i).deskId;
                        //совмещаем точку нажатия и центр(-40;-20) парты
                        deskLayoutParams.leftMargin = (int) (motionEvent.getX() - pxFromDp(1000 * multiplier));
                        deskLayoutParams.topMargin = (int) (motionEvent.getY() - pxFromDp(500 * multiplier));
                        deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE://старые + новая - x нажатия
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
                            instrumentalImage.setImageResource(R.drawable.ic_input_add);
                        } else {

                            deskLayoutParams.leftMargin = (int) (motionEvent.getX() - pxFromDp(1000 * multiplier));
                            deskLayoutParams.topMargin = (int) (motionEvent.getY() - pxFromDp(500 * multiplier));
                            deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                        }
                    }
                }
                break;

//---отпускаем кнопку---
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    //находим нажатую парту по id
                    if (deskCoordinatesList.get(i).deskId == checkedDeskId) {
                        //ставим изображение в плюс
                        instrumentalImage.setImageResource(R.drawable.ic_input_add);
                        //расчитываем новые координаты
                        //              координата     -          расстояние до центра пальца
                        float x = ((motionEvent.getX() - pxFromDp(1000 * multiplier)));
                        //отступ от границы клетки
                        if ((x % GIRD_SPACING) < GIRD_SPACING / 2) {//смотрим куда ближе отнимать или прибавлять
                            x = x - (x % GIRD_SPACING);
                        } else {
                            x = x + GIRD_SPACING - (x % GIRD_SPACING);
                        }
                        float y = ((motionEvent.getY() - pxFromDp(500 * multiplier)));
                        if ((y % GIRD_SPACING) < GIRD_SPACING / 2) {
                            y = y - (y % GIRD_SPACING);
                        } else {
                            y = y + GIRD_SPACING - (y % GIRD_SPACING);
                        }

                        //новые координаты в список
                        deskCoordinatesList.get(i).x = (long) (x / pxFromDp(25 * multiplier));
                        deskCoordinatesList.get(i).y = (long) (y / pxFromDp(25 * multiplier));
                        //новые координаты в бд
                        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                        db.setDeskCoordinates(deskCoordinatesList.get(i).deskId,
                                deskCoordinatesList.get(i).x,
                                deskCoordinatesList.get(i).y
                        );
                        //новые координаты парте
                        deskLayoutParams.leftMargin = (int) pxFromDp((deskCoordinatesList.get(i).x * 25 * multiplier));
                        deskLayoutParams.topMargin = (int) pxFromDp((deskCoordinatesList.get(i).y * 25 * multiplier));
                        deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
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
        long deskId = db.createDesk(2, (int) (dpFromPx(metricsB.widthPixels / 2) / (25 * multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2) / (25 * multiplier)), cabinetId);

        db.createPlace(deskId, 1);
        db.createPlace(deskId, 2);

        RelativeLayout newDeskLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(2000 * multiplier), (int) pxFromDp(1000 * multiplier));
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        newDeskLayoutParams.leftMargin = (int) (metricsB.widthPixels / 2 - pxFromDp(1000 * multiplier));
        newDeskLayoutParams.topMargin = (int) (metricsB.heightPixels / 2 - pxFromDp(500 * multiplier));
        newDeskLayout.setLayoutParams(newDeskLayoutParams);
        newDeskLayout.setBackgroundColor(Color.parseColor("#f1bd7d"));

        deskCoordinatesList.add(new CabinetRedactorPoint(deskId, newDeskLayout, (int) (dpFromPx(metricsB.widthPixels / 2 - pxFromDp(1000 * multiplier)) / (25 * multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2 - pxFromDp(500 * multiplier)) / (25 * multiplier))));

        out.addView(newDeskLayout);
        //появилась парта, удаляем текст
        stateText.setText("");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://кнопка назад в actionBar
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private float pxFromDp(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px) {
        return px / getApplicationContext().getResources().getDisplayMetrics().density;
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