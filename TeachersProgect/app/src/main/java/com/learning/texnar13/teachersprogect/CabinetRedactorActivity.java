package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
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

    //--константы--
    // ID редактируемого обьекта
    public static final String EDITED_OBJECT_ID = "id";
    // шаг клетки
    static float girdSpacing = 60F;

    //--переменные--
    // выравнивание
    boolean isGird = true;
    // вывод сетки
    boolean isGirdLines = false;


    ArrayList<CabinetRedactorPoint> deskCoordinatesList = new ArrayList<>();
    float multiplier = 0;//множитель
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

    //раздуваем неаше меню
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
        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;
        //по коэффициенту расчитываем ширину сетки
        girdSpacing = 600 * multiplier;

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


//todo сделать ее вывод из доп меню
//---вывод сетки---
        outLines();


//---------------загружаем данные из бд---------------

        //какой view появился позже тот и отображаться будет выше

        cabinetId = getIntent().getLongExtra(EDITED_OBJECT_ID, 1);//получаем id кабинета
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
            long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID));
            long deskX = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X));
            long deskY = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y));
            int numberOfPlaces = desksCursor.getInt(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES));
            final RelativeLayout deskLayout = new RelativeLayout(this);
            deskCoordinatesList.add(new CabinetRedactorPoint(deskId, deskLayout, deskX, deskY, numberOfPlaces));
            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(1000 * numberOfPlaces * multiplier), (int) pxFromDp(1000 * multiplier));
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            //deskLayout.setRotation(60);
            deskLayoutParams.leftMargin = (int) pxFromDp(deskX * 25 * multiplier);
            deskLayoutParams.topMargin = (int) pxFromDp(deskY * 25 * multiplier);
            deskLayout.setLayoutParams(deskLayoutParams);
            //ставим парте Drawable
            deskLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_2_yellow_spot));
            out.addView(deskLayout);
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
                    multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        //создаем новые параметры
                        RelativeLayout.LayoutParams deskLayoutParams =
                                new RelativeLayout.LayoutParams(
                                        (int) pxFromDp(1000 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
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
                    girdSpacing = 600 * multiplier;
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
                    multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;
                    for (int i = 0; i < deskCoordinatesList.size(); i++) {
                        //создаем новые параметры
                        RelativeLayout.LayoutParams deskLayoutParams =
                                new RelativeLayout.LayoutParams(
                                        (int) pxFromDp(1000 * deskCoordinatesList.get(i).numberOfPlaces * multiplier),
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
                    girdSpacing = 600 * multiplier;
                    //---перевывод сетки---
                    outLines();
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
            stateText.setText(R.string.cabinets_out_activity_text_help);
        } else {
            stateText.setText("");
        }


        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if ((motionEvent.getX() >= pxFromDp(deskCoordinatesList.get(i).x * 25 * multiplier)) &&
                            (motionEvent.getX() <= pxFromDp((deskCoordinatesList.get(i).x * 25 + 1000 * deskCoordinatesList.get(i).numberOfPlaces) * multiplier)) &&
                            (motionEvent.getY() >= pxFromDp(deskCoordinatesList.get(i).y * 25 * multiplier)) &&
                            (motionEvent.getY() <= pxFromDp((deskCoordinatesList.get(i).y * 25 + 1000) * multiplier))) {

                        instrumentalImage.setImageResource(R.drawable.ic_menu_delete_standart);

                        checkedDeskId = deskCoordinatesList.get(i).deskId;
                        //новые параметры парты
                        RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(1000 * deskCoordinatesList.get(i).numberOfPlaces * multiplier), (int) pxFromDp(1000 * multiplier));
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

                        //совмещаем точку нажатия и центр(-40;-20) парты
                        deskLayoutParams.leftMargin = (int) (motionEvent.getX() - pxFromDp(500 * deskCoordinatesList.get(i).numberOfPlaces * multiplier));
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
                            instrumentalImage.setImageResource(R.drawable.ic_white_plus);
                        } else {
                            //новые параметры парты
                            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(1000 * deskCoordinatesList.get(i).numberOfPlaces * multiplier), (int) pxFromDp(1000 * multiplier));
                            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

                            deskLayoutParams.leftMargin = (int) (motionEvent.getX() - pxFromDp(500 * deskCoordinatesList.get(i).numberOfPlaces * multiplier));
                            deskLayoutParams.topMargin = (int) (motionEvent.getY() - pxFromDp(500 * multiplier));
                            deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                        }
                    }
                }
                break;

//---отпускаем парту---
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    //находим нажатую парту по id
                    if (deskCoordinatesList.get(i).deskId == checkedDeskId) {
                        //ставим изображение в плюс
                        instrumentalImage.setImageResource(R.drawable.ic_white_plus);

                        //расчитываем новые координаты
                        //              координата     -          расстояние до центра пальца
                        float x = ((motionEvent.getX() - pxFromDp(500 * deskCoordinatesList.get(i).numberOfPlaces * multiplier)));
                        float y = ((motionEvent.getY() - pxFromDp(500 * multiplier)));

                        //отступ от границы клетки
                        if (isGird) {//на до ли отнимать/прибавлять
                            // смотрим куда ближе отнимать или прибавлять
                            //x
                            if ((x % girdSpacing) < girdSpacing / 2) {
                                x = x - (x % girdSpacing);
                            } else {
                                x = x + girdSpacing - (x % girdSpacing);
                            }
                            //y
                            if ((y % girdSpacing) < girdSpacing / 2) {
                                y = y - (y % girdSpacing);
                            } else {
                                y = y + girdSpacing - (y % girdSpacing);
                            }
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
                        RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(1000 * deskCoordinatesList.get(i).numberOfPlaces * multiplier), (int) pxFromDp(1000 * multiplier));
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

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

//---------------нажатие на +---------------

    @Override
    public void onClick(View view) {
        //todo места к парте
        // узнаем размеры экрана из класса Display
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);

        int numberOfPlaces = 2;

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long deskId = db.createDesk(numberOfPlaces, (int) (dpFromPx(metricsB.widthPixels / 2) / (25 * multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2) / (25 * multiplier)), cabinetId);

        for (int i = 0; i < numberOfPlaces; i++) {
            db.createPlace(deskId, i + 1);
        }

        RelativeLayout newDeskLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(1000 * numberOfPlaces * multiplier), (int) pxFromDp(1000 * multiplier));
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        newDeskLayoutParams.leftMargin = (int) (metricsB.widthPixels / 2 - pxFromDp(500 * numberOfPlaces * multiplier));
        newDeskLayoutParams.topMargin = (int) (metricsB.heightPixels / 2 - pxFromDp(500 * multiplier));
        newDeskLayout.setLayoutParams(newDeskLayoutParams);
        //ставим парте Drawable
        newDeskLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_2_yellow_spot));

        deskCoordinatesList.add(new CabinetRedactorPoint(deskId, newDeskLayout, (int) (dpFromPx(metricsB.widthPixels / 2 - pxFromDp(500 * numberOfPlaces * multiplier)) / (25 * multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2 - pxFromDp(500 * multiplier)) / (25 * multiplier)), numberOfPlaces));

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
        long deskId = db.createDesk(numberOfPlaces, (int) (dpFromPx(metricsB.widthPixels / 2) / (25 * multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2) / (25 * multiplier)), cabinetId);

        for (int i = 0; i < numberOfPlaces; i++) {
            db.createPlace(deskId, i + 1);
        }

        RelativeLayout newDeskLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(1000 * numberOfPlaces * multiplier), (int) pxFromDp(1000 * multiplier));
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        newDeskLayoutParams.leftMargin = (int) (metricsB.widthPixels / 2 - pxFromDp(500 * numberOfPlaces * multiplier));
        newDeskLayoutParams.topMargin = (int) (metricsB.heightPixels / 2 - pxFromDp(500 * multiplier));
        newDeskLayout.setLayoutParams(newDeskLayoutParams);
        //ставим парте Drawable
        newDeskLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_2_yellow_spot));

        deskCoordinatesList.add(new CabinetRedactorPoint(deskId, newDeskLayout, (int) (dpFromPx(metricsB.widthPixels / 2 - pxFromDp(500 * numberOfPlaces * multiplier)) / (25 * multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2 - pxFromDp(500 * multiplier)) / (25 * multiplier)), numberOfPlaces));

        out.addView(newDeskLayout);
        //появилась парта, удаляем текст
        stateText.setText("");
        return true;
    }

//---------------вывод сетки---------------

    void outLines() {
        outBackground.removeAllViews();
        //выводится ли полоски
        if (isGirdLines) {
            float x = 0;
            float y = 0;
            DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
            //пробежка по x
            while (x < displaymetrics.widthPixels) {
                //вывод вертикальной полосы
                x = x + girdSpacing;
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
                y = y + girdSpacing;
                final RelativeLayout horizontalLine = new RelativeLayout(this);
                RelativeLayout.LayoutParams horizontalLineLayoutParams = new RelativeLayout.LayoutParams(
                        displaymetrics.heightPixels,
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


}

class CabinetRedactorPoint {
    long deskId;
    int numberOfPlaces;
    RelativeLayout desk;
    long x;
    long y;

    CabinetRedactorPoint(long id, RelativeLayout relativeDesk, long x, long y, int numberOfPlaces) {
        this.deskId = id;
        this.desk = relativeDesk;
        this.x = x;
        this.y = y;
        this.numberOfPlaces = numberOfPlaces;
    }
}