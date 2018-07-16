package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
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

public class CabinetRedactorActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {

    public static final String TAG = "TeachersApp";
    static final int MY_SETTINGS_PROFILE_ID = 1;
    static final String MY_SETTINGS_PROFILE_NAME = "default";


    //--константы--
    // ID редактируемого обьекта
    public static final String EDITED_CABINET_ID = "id";
    //--переменные--
    // выравнивание
    boolean isGird = true;
    // вывод сетки
    boolean isGirdLines = false;


    ArrayList<CabinetRedactorPoint> deskCoordinatesList = new ArrayList<>();
    //множитель
    float multiplier = 0;
    long checkedDeskId;
    ImageView instrumentalImageBackground;
    ImageView instrumentalImage;
    //слой с партами
    RelativeLayout out;
    //слой позади out, например для сетки
    RelativeLayout outBackground;
    long cabinetId;
    TextView stateText;


//-------------------------------меню сверху--------------------------------------------------------

    //раздуваем наше меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cabinet_redactor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //назначаем функции меню
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        //чекбокс выравнивания
        menu.findItem(R.id.cabinet_redactor_menu_gird).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //если чекбокс нажат
                if (menuItem.isChecked()) {
                    //убираем подведение парт
                    isGird = false;
                    //убираем галочку
                    menuItem.setChecked(false);
                } else {
                    //возвращаем подведение парт
                    isGird = true;
                    //ставим галочку
                    menuItem.setChecked(true);
                }
                return true;
            }
        });
        //чекбокс сетки
        menu.findItem(R.id.cabinet_redactor_menu_gird_lines).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //если чекбокс нажат
                if (menuItem.isChecked()) {
                    //убираем галочку
                    isGirdLines = false;
                    menuItem.setChecked(false);
                    //убираем сетку
                    outLines();
                } else {
                    //ставим галочку
                    menuItem.setChecked(true);
                    isGirdLines = true;
                    //возвращаем подведение парт
                    outLines();
                }
                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

//---------------создание экрана---------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_redactor);
        out = (RelativeLayout) findViewById(R.id.redactor_out);
        outBackground = (RelativeLayout) findViewById(R.id.redactor_out_background);

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        //коэффициент размера интерфейса
        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 40f * getResources().getInteger(R.integer.desks_screen_multiplier);

        Log.i("TeachersApp", "multiplier = " + multiplier);
        //кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //2 это редактор кабинетов
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

//---вывод сетки---
        outLines();


//---------------загружаем данные из бд---------------

        //какой view появился позже тот и отображаться будет выше

        cabinetId = getIntent().getLongExtra(EDITED_CABINET_ID, 1);//получаем id кабинета
        Log.i("TeachersApp", "CabinetRedactorActivity - onCreate editedObjectId = " + cabinetId);

        Cursor cabinetCursor = db.getCabinets(cabinetId);
        cabinetCursor.moveToFirst();
        setTitle(getTitle() + " " +
                cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)));
        cabinetCursor.close();

//---------------ВЫВОД ПАРТ---------------

        instrumentalImage = (ImageView) findViewById(R.id.activity_cabinet_redactor_instrumental_image);

        instrumentalImageBackground = (ImageView) findViewById(R.id.activity_cabinet_redactor_instrumental_image_background);
        instrumentalImageBackground.setOnClickListener(this);
        instrumentalImageBackground.setOnLongClickListener(this);

        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);//курсор с партами
        while (desksCursor.moveToNext()) {//начальный вывод парт
            //добываем данные из бд
            long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID));
            long deskX = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X));
            long deskY = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y));
            int numberOfPlaces = desksCursor.getInt(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES));
            //наша парта
            final RelativeLayout deskLayout = new RelativeLayout(this);
            //ставим парте Drawable
            deskLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_2_yellow_spot));
            //ее параметры
            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(40 * numberOfPlaces * multiplier), (int) pxFromDp(40 * multiplier));
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            //deskLayout.setRotation(60);
            deskLayoutParams.leftMargin = (int) pxFromDp(deskX * multiplier);
            deskLayoutParams.topMargin = (int) pxFromDp(deskY * multiplier);
            //ставим параметры
            deskLayout.setLayoutParams(deskLayoutParams);
            //выводим парту
            out.addView(deskLayout);
            //добавлем парту и данные в массив
            deskCoordinatesList.add(new CabinetRedactorPoint(deskId, deskLayout, deskX, deskY, numberOfPlaces));

        }
        desksCursor.close();

        //текст в центре
        stateText = (TextView) findViewById(R.id.cabinet_redactor_state_text);
        //если парт нет, то показываем текст
        if (deskCoordinatesList.size() == 0) {
            stateText.setText(R.string.cabinet_redactor_activity_text_help);
        } else {
            stateText.setText("");
        }

//----------инициализируем кнопки zoom-----------
        final ZoomControls zoomControls = (ZoomControls) findViewById(R.id.cabinet_redactor_zoom_controls);
        //приближение
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //--изменяем размер--
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());


                //проверяем можем ли изменять
                int last = (int) db.getInterfaceSizeBySettingsProfileId(1);
                if (last < 95) {
                    db.setSettingsProfileParameters(
                            1,
                            "default",
                            last + 4
                    );
                    db.close();

                    //активируем другую если приближать можно
                    zoomControls.setIsZoomOutEnabled(true);

                    //выводим все
                    multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 40f * getResources().getInteger(R.integer.desks_screen_multiplier);
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        //создаем новые параметры
                        deskCoordinatesList.get(i).setDeskParams(
                                pxFromDp(deskCoordinatesList.get(i).getDpX()) * multiplier,
                                pxFromDp(deskCoordinatesList.get(i).getDpY()) * multiplier,
                                (int) pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                (int) pxFromDp(40 * multiplier),
                                multiplier
                        );

//                        RelativeLayout.LayoutParams deskLayoutParams =
//                                new RelativeLayout.LayoutParams(
//                                        (int) pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
//                                        (int) pxFromDp(40 * multiplier)
//                                );
//                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                        deskLayoutParams.leftMargin = (int) pxFromDp(
//                                pxFromDp(deskCoordinatesList.get(i).getDpX()) * multiplier
//                        );
//                        deskLayoutParams.topMargin = (int) pxFromDp(
//                                pxFromDp(deskCoordinatesList.get(i).getDpY()) * multiplier
//                        );
//                        //и присваиваем их партам
//                        deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                    }
                    //---перевывод сетки---
                    outLines();
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
                if (last > 6) {
                    db.setSettingsProfileParameters(
                            1,
                            "default",
                            (int) db.getInterfaceSizeBySettingsProfileId(1) - 4
                    );
                    db.close();

                    //активируем другую если приближать можно
                    zoomControls.setIsZoomInEnabled(true);

                    //выводим все
                    multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 40f * getResources().getInteger(R.integer.desks_screen_multiplier);
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {

                        //создаем новые параметры
                        deskCoordinatesList.get(i).setDeskParams(
                                pxFromDp(deskCoordinatesList.get(i).getDpX()) * multiplier,
                                pxFromDp(deskCoordinatesList.get(i).getDpY()) * multiplier,
                                (int) pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                (int) pxFromDp(40 * multiplier),
                                multiplier
                        );
                    }
                    //---перевывод сетки---
                    outLines();
                } else {//деактивируем кнопку если отдалять нельзя
                    zoomControls.setIsZoomOutEnabled(false);
                }
            }
        });
        out.setOnTouchListener(this);

    }


    //переменные zoom
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    //середина касания пальцев
    PointF startMid = new PointF();
    //текущая середина
    PointF nowMid = new PointF();
    //изначальное растояние между пальцам
    float oldDist = 1f;

    //+при зуме происходит смещение осей, и их растягивание (за растягивание отвечаем переменная multiplier)+
    //нулевые координаты (используются только для view при перемещении двумя пальцами координаты для бд не меняются)
    //меняются координаты и идет смещение касания
    //начальные
    float startZeroX = 0;
    float startZeroY = 0;
    //текущие
    float zeroX = 0;
    float zeroY = 0;
    //изначальный множитель
    float startMultiplier = multiplier;


    //начальные параметры обьекта
    float[] widthOld = {};
    float[] heightOld = {};
    float[] xOld = {};
    float[] yOld = {};

    float[] afterZoomX = {};
    float[] afterZoomY = {};

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //если парт нет, то показываем текст
        if (deskCoordinatesList.size() == 0) {
            stateText.setText(R.string.cabinets_out_activity_text_help);
        } else {
            stateText.setText("");
        }

        //-действие
        //=состояние
        // комментарий

        //первое нажатие
        //  =перемещение=
        //  -ищем парту и омечаем-
        //нажатие
        //  -заканчиваем перемещение, сохраняем-
        //  =zoom=
        //  -инициализируем зум-
        //движение
        //  -при перемещении меняем-
        //  -при зуме зумим-
        //отпускание
        //   если есть прекращаем зум, не начиная касания
        //  =none=
        //последнее отпускание
        //  -если есть завершаем перемещение-
        //  =none=


        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {//todo разобраться с & MotionEvent.ACTION_MASK(что это?)
            case MotionEvent.ACTION_DOWN:

                //режим перемещения
                mode = DRAG;
                //стартовое смещение координат
                startZeroX = zeroX;
                startZeroY = zeroY;

                // находим нажатую парту
                for (int i = deskCoordinatesList.size() - 1; i >= 0; i--) {
                    if ((motionEvent.getX() >= pxFromDp(deskCoordinatesList.get(i).getDpX() * multiplier)) &&
                            (motionEvent.getX() <= pxFromDp((deskCoordinatesList.get(i).getDpX() + 40 * deskCoordinatesList.get(i).numberOfPlaces) * multiplier)) &&
                            (motionEvent.getY() >= pxFromDp(deskCoordinatesList.get(i).getDpY() * multiplier)) &&
                            (motionEvent.getY() <= pxFromDp((deskCoordinatesList.get(i).getDpY() + 40) * multiplier))) {

                        instrumentalImage.setImageResource(R.drawable.ic_menu_delete_standart);

                        checkedDeskId = deskCoordinatesList.get(i).deskId;
                        //новые параметры парты
                        //совмещаем точку нажатия и центр(-40;-20) парты

                        deskCoordinatesList.get(i).setDeskParams(
                                motionEvent.getX() - pxFromDp(20 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                motionEvent.getY() - pxFromDp(20 * multiplier),
                                (int) pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                (int) pxFromDp(40 * multiplier),
                                multiplier
                        );

//                        ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).leftMargin =
//                                (int) (motionEvent.getX() - pxFromDp(20 * deskCoordinatesList.get(i).numberOfPlaces * multiplier));
//                        ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).topMargin =
//                                (int) (motionEvent.getY() - pxFromDp(20 * multiplier));
                        //самую последнюю парту и выходим
                        break;
                    }
                }
                Log.i(TAG, "MotionEvent.ACTION_DOWN count:" + motionEvent.getPointerCount() + " mode:" + mode);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                //если поставлен второй палец на остальные не реагируем
                if (motionEvent.getPointerCount() == 2) {
                    //будем зумить, прекращаем перемещение
                    //---отпускаем парту---
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        //находим нажатую парту по id
                        if (deskCoordinatesList.get(i).deskId == checkedDeskId) {
                            //ставим изображение в плюс
                            instrumentalImage.setImageResource(R.drawable.ic_white_plus);

                            //расчитываем новые координаты
                            //              координата     -          расстояние до центра пальца
                            float x = (motionEvent.getX() - pxFromDp(20 * deskCoordinatesList.get(i).numberOfPlaces * multiplier));
                            float y = (motionEvent.getY() - pxFromDp(20 * multiplier));

                            //отступ от границы клетки
                            if (isGird) {//на до ли отнимать/прибавлять
                                // смотрим куда ближе отнимать или прибавлять
                                //x
                                if (((x - (zeroX % (40 * multiplier))) % (40 * multiplier)) < (40 * multiplier) / 2) {
                                    x = x - ((x - (zeroX % (40 * multiplier))) % (40 * multiplier));
                                } else {
                                    x = x + (40 * multiplier) - ((x - (zeroX % (40 * multiplier))) % (40 * multiplier));
                                }
                                //y
                                if (((y - (zeroX % (40 * multiplier))) % (40 * multiplier)) < (40 * multiplier) / 2) {
                                    y = y - ((y - (zeroX % (40 * multiplier))) % (40 * multiplier));
                                } else {
                                    y = y + (40 * multiplier) - ((y - (zeroX % (40 * multiplier))) % (40 * multiplier));
                                }
                            }

                            //новые координаты в список и новые координаты графической парте
                            deskCoordinatesList.get(i).setDeskParams(
                                    x,
                                    y,
                                    (int) pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                    (int) pxFromDp(40 * multiplier),
                                    multiplier
                            );

                            //новые координаты в бд
                            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                            db.setDeskCoordinates(deskCoordinatesList.get(i).deskId,
                                    (long) ((x - zeroX) / pxFromDp(multiplier)),
                                    (long) ((y - zeroY) / pxFromDp(multiplier))
                            );
                        }
                    }
                    Log.i("TeachersApp", "X = " + (motionEvent.getX() - zeroX) +
                            " ; Y = " + (motionEvent.getY() - zeroY) + " ;");
                    checkedDeskId = -1;

                    //---------начинаем zoom-----------
                    //находим изначальное растояние между пальцами
                    oldDist = spacing(motionEvent);
                    if (oldDist > 10f) {
                        //начальная середина между пальцами
                        findMidPoint(startMid, motionEvent);
                        //текущая середина между пальцами(будем искать в процессе)
                        findMidPoint(nowMid, motionEvent);
                        //начальный множитель
                        startMultiplier = multiplier;
                        mode = ZOOM;
                    }
                    //стартовые параметры обьекта
                    widthOld = new float[deskCoordinatesList.size()];
                    heightOld = new float[deskCoordinatesList.size()];
                    xOld = new float[deskCoordinatesList.size()];
                    yOld = new float[deskCoordinatesList.size()];
                    afterZoomX = new float[deskCoordinatesList.size()];
                    afterZoomY = new float[deskCoordinatesList.size()];

                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        afterZoomX[i] = pxFromDp(deskCoordinatesList.get(i).getDpX()) * multiplier;
                        afterZoomY[i] = pxFromDp(deskCoordinatesList.get(i).getDpY()) * multiplier;
                        //начальные размеры обьекта
                        widthOld[i] = pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier);
                        heightOld[i] = pxFromDp(40 * multiplier);
                        //начальные координаты обьекта
                        xOld[i] = pxFromDp(deskCoordinatesList.get(i).getDpX()) * multiplier;
                        yOld[i] = pxFromDp(deskCoordinatesList.get(i).getDpY()) * multiplier;
                    }
                }
                Log.i(TAG, "MotionEvent.ACTION_POINTER_DOWN count:" + motionEvent.getPointerCount() + " mode:" + mode);
//                //todo-- (для отладки)
//                for (int i = 0; i < deskCoordinatesList.size(); i++) {
//                    RelativeLayout relativeLayout = new RelativeLayout(this);
//                    relativeLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
//
//                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(100, 100);
//                    layoutParams.leftMargin = (int) pxFromDp(deskCoordinatesList.get(i).getDpX() * multiplier);
//                    layoutParams.topMargin = (int) pxFromDp(deskCoordinatesList.get(i).getDpY() * multiplier);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//
//                    out.addView(relativeLayout, layoutParams);
//                }
//                //todo--
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {//перемещаем парту
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        if (deskCoordinatesList.get(i).deskId == checkedDeskId) {
                            //если палец находится в пределах крестика то удаляем парту
                            if ((motionEvent.getX() - zeroX >= out.getWidth() / 2 - pxFromDp(50 * getResources().getInteger(R.integer.desks_screen_multiplier)) / 2) &&
                                    (motionEvent.getX() - zeroX <= out.getWidth() / 2 + pxFromDp(50 * getResources().getInteger(R.integer.desks_screen_multiplier)) / 2) &&
                                    (motionEvent.getY() - zeroY >= out.getHeight() - pxFromDp(50 * getResources().getInteger(R.integer.desks_screen_multiplier))) &&
                                    (motionEvent.getY() - zeroY <= out.getHeight())) {
                                Log.i("TeachersApp", "удалена парта");
                                //удаляем парту
                                DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                                db.deleteDesk(deskCoordinatesList.get(i).deskId);
                                out.removeView(deskCoordinatesList.get(i).desk);
                                deskCoordinatesList.remove(i);
                                instrumentalImage.setImageResource(R.drawable.ic_white_plus);
                            } else {
                                //новые параметры парты(в массив все сохраняется в конце касания)
                                deskCoordinatesList.get(i).setDeskParams(
                                        motionEvent.getX() - pxFromDp(20 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                        motionEvent.getY() - pxFromDp(20 * multiplier),
                                        (int) pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                        (int) pxFromDp(40 * multiplier),
                                        multiplier
                                );
                            }
                        }
                    }
                } else if (mode == ZOOM) {//зумим
                    //новое расстояние между пальцами
                    float newDist = spacing(motionEvent);
                    //находим коэффициент разницы между изначальным и новым расстоянием
                    float scale = newDist / oldDist;
                    // текущая середина между пальцами
                    findMidPoint(nowMid, motionEvent);

                    //todo можно будет еще сделать ограничение на слишком маленький зум, тогда будет работать только перемещение if(Math.abs(newDist-oldDist) > 0,5)

                    if (newDist > 10f) {//слишком маленькое расстояние между пальцами
                        if (scale > 0.01f &&//слишком маленький коэффициент
                                (startMultiplier * 40f / getResources().getInteger(R.integer.desks_screen_multiplier) * scale >= 3f) &&//слишком маленький размер
                                (startMultiplier * 40f / getResources().getInteger(R.integer.desks_screen_multiplier) * scale <= 100f)//слишком большой размер
                                ) {
                            //--перемещение осей при зуме

                            //переназначаем центр осей на зуме
                            zeroX = ((startZeroX - startMid.x) * scale) + nowMid.x;
                            zeroY = ((startZeroY - startMid.y) * scale) + nowMid.y;
                            //назначаем растяжение осей меняя множитель
                            multiplier = startMultiplier * scale;

                            for (int i = 0; i < deskCoordinatesList.size(); i++) {
                                //новые парамеры
                                deskCoordinatesList.get(i).setDeskParams(
                                        //-----трансформация координаты-----
                                        ((xOld[i] - startMid.x) * scale) + nowMid.x,
                                        ((yOld[i] - startMid.y) * scale) + nowMid.y,
                                        //-----трансформация размера-----
                                        (int) (widthOld[i] * scale),
                                        (int) (heightOld[i] * scale),
                                        multiplier
                                );
                                //-перемещение обьекта-
                                // относительно центра зуммирования и перемещение пальцев в процессе зума
                                //ставим обьекту координаты
//                                ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).leftMargin = (int) (((xOld[i] - startMid.x) * scale) + nowMid.x);
//                                ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).topMargin = (int) (((yOld[i] - startMid.y) * scale) + nowMid.y);
//
//                                //массива координат (координаты форматированы под бд)
//                                deskCoordinatesList.get(i).x = (long) (dpFromPx(
//                                        ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).leftMargin
//                                ) / multiplier);
//                                deskCoordinatesList.get(i).y = (long) (dpFromPx(
//                                        ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).topMargin
//                                ) / multiplier);
                            }
                            //запоминаем координаты парты после отработки зума
                            for (int i = 0; i < deskCoordinatesList.size(); i++) {
                                afterZoomX[i] = pxFromDp(deskCoordinatesList.get(i).getDpX()) * multiplier;
                                afterZoomY[i] = pxFromDp(deskCoordinatesList.get(i).getDpY()) * multiplier;
                            }

                        } else {//todo проверить// TODO оси координат,что происходит при работе зума как перемещения
                            //если не можем использовать изменение размера,
                            // тогда просто перемещаем
                            for (int i = 0; i < deskCoordinatesList.size(); i++) {
                                //обновляем координаты изменяя только положение
                                deskCoordinatesList.get(i).setDeskParams(
                                        afterZoomX[i] + nowMid.x - startMid.x,//todo zeroX
                                        afterZoomY[i] + nowMid.y - startMid.y,
                                        (int) pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                        (int) pxFromDp(40 * multiplier),
                                        multiplier
                                );
//                                ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).leftMargin =
//                                        (int) ();
//                                ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).topMargin =
//                                        (int) (afterZoomY[i] + nowMid.y - startMid.y);
                            }
                            //переназначаем центр осей
                            zeroX = startZeroX + nowMid.x - startMid.x;
                            zeroY = startZeroY + nowMid.y - startMid.y;
                        }
                        //Log.e(TAG, "zeroX= "+zeroX+" zeroY= "+zeroY);
                    }
                }
                //выводим сетку после зума
                outLines();
                Log.i(TAG, "MotionEvent.ACTION_MOVE count:" + motionEvent.getPointerCount() + " mode:" + mode);
                Log.i(TAG, "MotionEvent.ACTION_MOVE zeroX:" + zeroX + "  zeroY:" + zeroY);
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                db.setSettingsProfileParameters(MY_SETTINGS_PROFILE_ID, MY_SETTINGS_PROFILE_NAME, (int) (multiplier * 40f / getResources().getInteger(R.integer.desks_screen_multiplier)));
                mode = NONE;
                outLines();
                Log.i(TAG, "MotionEvent.ACTION_POINTER_UP count:" + motionEvent.getPointerCount() + " mode:" + mode);
                break;
            }

            case MotionEvent.ACTION_UP:
                if (mode == DRAG) {
                    //---отпускаем парту---
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        //находим нажатую парту по id
                        if (deskCoordinatesList.get(i).deskId == checkedDeskId) {
                            //ставим изображение в плюс
                            instrumentalImage.setImageResource(R.drawable.ic_white_plus);

                            //расчитываем новые координаты
                            //              координата     -          расстояние до центра пальца
                            float x = (motionEvent.getX() - pxFromDp(20 * deskCoordinatesList.get(i).numberOfPlaces * multiplier));
                            float y = (motionEvent.getY() - pxFromDp(20 * multiplier));

                            //отступ от границы клетки
                            if (isGird) {//на до ли отнимать/прибавлять
                                // смотрим куда ближе отнимать или прибавлять
                                //todo все еще косяки
                                if (((x - (zeroX % (40 * multiplier))) % (40 * multiplier)) < (40 * multiplier) / 2) {
                                    x = x - ((x - (zeroX % (40 * multiplier))) % (40 * multiplier));
                                } else {
                                    x = x + (40 * multiplier) - ((x - (zeroX % (40 * multiplier))) % (40 * multiplier));
                                }
                                //y
                                if (((y - (zeroX % (40 * multiplier))) % (40 * multiplier)) < (40 * multiplier) / 2) {
                                    y = y - ((y - (zeroX % (40 * multiplier))) % (40 * multiplier));
                                } else {
                                    y = y + (40 * multiplier) - ((y - (zeroX % (40 * multiplier))) % (40 * multiplier));
                                }
                            }
                            //новые координаты в список и новые координаты графической парте
                            deskCoordinatesList.get(i).setDeskParams(
                                    x,
                                    y,
                                    (int) pxFromDp(40 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
                                    (int) pxFromDp(40 * multiplier),
                                    multiplier
                            );

                            //новые координаты в бд
                            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                            db.setDeskCoordinates(deskCoordinatesList.get(i).deskId,
                                    (long) ((x - zeroX) / pxFromDp(multiplier)),
                                    (long) ((y - zeroY) / pxFromDp(multiplier))
                            );

//                            //новые координаты в список
//                            deskCoordinatesList.get(i).x = (long) (x / pxFromDp(multiplier));
//                            deskCoordinatesList.get(i).y = (long) (y / pxFromDp(multiplier));
//                            //новые координаты в бд
//                            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
//                            db.setDeskCoordinates(deskCoordinatesList.get(i).deskId,
//                                    (long) (deskCoordinatesList.get(i).x - (zeroX / pxFromDp(multiplier))),
//                                    (long) (deskCoordinatesList.get(i).y - (zeroY / pxFromDp(multiplier)))
//                            );
//                            //новые координаты графической парте
//                            ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).leftMargin = (int) (pxFromDp(deskCoordinatesList.get(i).x * multiplier));
//                            ((RelativeLayout.LayoutParams) deskCoordinatesList.get(i).desk.getLayoutParams()).topMargin = (int) (pxFromDp(deskCoordinatesList.get(i).y * multiplier));
                        }
                    }
                    Log.i("TeachersApp", "X = " + (motionEvent.getX() - zeroX) +
                            " ; Y = " + (motionEvent.getY() - zeroY) + " ;");
                    checkedDeskId = -1;
                }
                mode = NONE;
                Log.i(TAG, "MotionEvent.ACTION_UP count:" + motionEvent.getPointerCount() + " mode:" + mode);
                break;

            case MotionEvent.ACTION_CANCEL:
        }
        return true;
    }

    // ---- Расстояние между первым и вторым пальцами из event ----
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // ---- координата середины между первым и вторым пальцами из event ----
    private void findMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

// --------------- нажатие на + ---------------

    @Override
    public void onClick(View view) {
        // узнаем размеры экрана из класса Display
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);

        int numberOfPlaces = 2;

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long deskId = db.createDesk(numberOfPlaces, (int) (dpFromPx(metricsB.widthPixels / 2) / multiplier), (int) (dpFromPx(metricsB.heightPixels / 2) / multiplier), cabinetId);

        for (int i = 0; i < numberOfPlaces; i++) {
            db.createPlace(deskId, i + 1);
        }

        RelativeLayout newDeskLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(40 * numberOfPlaces * multiplier), (int) pxFromDp(40 * multiplier));
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        newDeskLayoutParams.leftMargin = (int) (metricsB.widthPixels / 2 - pxFromDp(20 * numberOfPlaces * multiplier));
        newDeskLayoutParams.topMargin = (int) (metricsB.heightPixels / 2 - pxFromDp(20 * multiplier));
        newDeskLayout.setLayoutParams(newDeskLayoutParams);
        //ставим парте Drawable
        newDeskLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_2_yellow_spot));

        deskCoordinatesList.add(new CabinetRedactorPoint(deskId, newDeskLayout, (int) (dpFromPx(metricsB.widthPixels / 2 - pxFromDp(20 * numberOfPlaces * multiplier)) / (multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2 - pxFromDp(20 * multiplier)) / (multiplier)), numberOfPlaces));

        out.addView(newDeskLayout);
        //появилась парта, удаляем текст
        stateText.setText("");

    }

    @Override
    public boolean onLongClick(View view) {
        // узнаем размеры экрана из класса Display
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);

        int numberOfPlaces = 1;

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long deskId = db.createDesk(numberOfPlaces, (int) (dpFromPx(metricsB.widthPixels / 2) / (multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2) / (multiplier)), cabinetId);

        for (int i = 0; i < numberOfPlaces; i++) {
            db.createPlace(deskId, i + 1);
        }

        RelativeLayout newDeskLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(40 * numberOfPlaces * multiplier), (int) pxFromDp(40 * multiplier));
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        newDeskLayoutParams.leftMargin = (int) (metricsB.widthPixels / 2 - pxFromDp(20 * numberOfPlaces * multiplier));
        newDeskLayoutParams.topMargin = (int) (metricsB.heightPixels / 2 - pxFromDp(20 * multiplier));
        newDeskLayout.setLayoutParams(newDeskLayoutParams);
        //ставим парте Drawable
        newDeskLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_2_yellow_spot));

        deskCoordinatesList.add(new CabinetRedactorPoint(deskId, newDeskLayout, (int) (dpFromPx(metricsB.widthPixels / 2 - pxFromDp(20 * numberOfPlaces * multiplier)) / (multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2 - pxFromDp(20 * multiplier)) / (multiplier)), numberOfPlaces));

        out.addView(newDeskLayout);
        //появилась парта, удаляем текст
        stateText.setText("");
        return true;
    }

    @Override
    public void onStop() {
        //чистим данные перед выходом
        zeroX = 0;
        zeroY = 0;
        super.onStop();
    }

//---------------вывод сетки---------------

    void outLines() {
        outBackground.removeAllViews();
        //выводится ли полоски
        if (isGirdLines) {
            float x;
            float y;
            x = (zeroX % (40 * multiplier));
            y = (zeroY % (40 * multiplier));

            DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
            //пробежка по x
            while (x < displaymetrics.widthPixels) {
                //вывод вертикальной полосы
                x = x + (40 * multiplier);
                final RelativeLayout verticalLine = new RelativeLayout(this);
                RelativeLayout.LayoutParams verticalLineLayoutParams = new RelativeLayout.LayoutParams(
                        3,
                        displaymetrics.heightPixels
                );
                verticalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                verticalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                verticalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                //deskLayout.setRotation(60);
                verticalLineLayoutParams.leftMargin = (int) x;
                verticalLineLayoutParams.topMargin = 0;
                verticalLine.setLayoutParams(verticalLineLayoutParams);
                verticalLine.setBackgroundColor(Color.LTGRAY);
                outBackground.addView(verticalLine);
            }
            //пробежка по y
            while (y < displaymetrics.heightPixels) {
                //вывод горизонтальной полосы
                y = y + (40 * multiplier);
                final RelativeLayout horizontalLine = new RelativeLayout(this);
                RelativeLayout.LayoutParams horizontalLineLayoutParams = new RelativeLayout.LayoutParams(
                        displaymetrics.widthPixels,
                        3
                );
                horizontalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                horizontalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                horizontalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                //deskLayout.setRotation(60);
                horizontalLineLayoutParams.leftMargin = 0;
                horizontalLineLayoutParams.topMargin = (int) y;
                horizontalLine.setLayoutParams(horizontalLineLayoutParams);
                horizontalLine.setBackgroundColor(Color.LTGRAY);
                outBackground.addView(horizontalLine);

            }
        }
    }
//---------------функциональные клавиши---------------

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

    //---------------системные методы---------------

    private float pxFromDp(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px) {
        return px / getApplicationContext().getResources().getDisplayMetrics().density;
    }

    class CabinetRedactorPoint {
        long deskId;
        int numberOfPlaces;
        RelativeLayout desk;
        private float x;
        private float y;

        CabinetRedactorPoint(long id, RelativeLayout relativeDesk, float x, float y, int numberOfPlaces) {
            this.deskId = id;
            this.desk = relativeDesk;
            this.x = x;
            this.y = y;
            this.numberOfPlaces = numberOfPlaces;
        }

        void setDeskParams(float pxX, float pxY, int width, int height, float multiplier) {
            //создаем новые параметры
            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams(
                    width,
                    height
            );
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            deskLayoutParams.leftMargin = (int) pxX;
            deskLayoutParams.topMargin = (int) pxY;
            //и присваиваем их парте
            desk.setLayoutParams(deskLayoutParams);

            x = dpFromPx(pxX) / multiplier;
            y = dpFromPx(pxY) / multiplier;
        }

        float getDpX() {
            return x;
        }

        float getDpY() {
            return y;
        }
    }

}




/*
    //переменные zoom
    static final int NONE = 0;
    static final int ZOOM = 2;
    int mode = NONE;
    //середина касания пальцев
    PointF startMid = new PointF();
    //текущая середина
    PointF nowMid = new PointF();
    //изначальное растояние между пальцам
    float oldDist = 1f;
    //начальные параметры обьекта
    int widthOld = 1;
    int heightOld = 1;
    int xOld = 1;
    int yOld = 1;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                //если поставлен второй палец,назначаем новые координаты
                if (event.getPointerCount() == 2) {
                    //начальные размеры обьекта
                    widthOld = myRectangle.getWidth();
                    heightOld = myRectangle.getHeight();
                    //начальные координаты обьекта
                    xOld = (int) myRectangle.getX();
                    yOld = (int) myRectangle.getY();
                    //находим изначальное растояние между пальцами
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        findMidPoint(startMid, event);
                        findMidPoint(nowMid, event);
                        mode = ZOOM;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    //новое расстояние между пальцами
                    float newDist = spacing(event);
                    //находим коэффициент разницы между изначальным и новым расстоянием
                    float scale = newDist / oldDist;

                    if (newDist > 10f) {//слишком маленькое расстояние между пальцами
                        if (scale > 0.01f &&//слишком маленький коэффициент
                                (widthOld * scale > 10f && heightOld * scale > 10f) &&//слишком маленький размер
                                (widthOld * scale < 1500f && heightOld * scale < 1500f)//слишком большой размер
                                ) {
                            //-----трансформация размера-----
                            rectParams.width = (int) (widthOld * scale);
                            rectParams.height = (int) (heightOld * scale);
                            myRectangle.setLayoutParams(rectParams);

                            //-----трансформация координаты-----
                            //текущая середина пальцев
                            findMidPoint(nowMid, event);
                            //-перемещение обьекта-
                            // относительно центра зуммирования и перемещение пальцев в процессе зума
                            //ставим обьекту координаты
                            myRectangle.setX(((xOld - startMid.x) * scale) + nowMid.x);
                            myRectangle.setY(((yOld - startMid.y) * scale) + nowMid.y);
                        } else {
                            //если не можем использовать изменение размера,
                            // тогда просто перемещаем
                            //берем прошлую середину
                            float lastX = nowMid.x;
                            float lastY = nowMid.y;
                            // и текущую
                            findMidPoint(nowMid, event);
                            //и сравниваем их
                            myRectangle.setX(myRectangle.getX() + nowMid.x - lastX);
                            myRectangle.setY(myRectangle.getY() + nowMid.y - lastY);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                //больше двух - ничего не меняется
                //если пальцев осталось два переназначаем начальные координаты
                if (event.getPointerCount() - 1 == 2) {
                    //начальные размеры обьекта
                    widthOld = myRectangle.getWidth();
                    heightOld = myRectangle.getHeight();
                    //начальные координаты обьекта
                    xOld = (int) myRectangle.getX();
                    yOld = (int) myRectangle.getY();
                    //находим изначальное растояние между пальцами
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        findMidPoint(startMid, event);
                        mode = ZOOM;
                    }
                }
                //один палец - ничего
                if (event.getPointerCount() - 1 < 2) {
                    mode = NONE;
                }
                break;
        }
        return true;
    }

    //******************* Расстояние между первым и вторым пальцами из event
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    //************* координата середины между первым и вторым пальцами из event
    private void findMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
*/