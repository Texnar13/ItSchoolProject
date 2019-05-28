package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class CabinetRedactorActivity extends AppCompatActivity implements View.OnTouchListener {

    public static final String TAG = "TeachersApp";

    // режимы зума
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    // размер одноместной парты
    static final int NO_ZOOMED_DESK_SIZE = 40;
    // половина дноместной парты
    static final int NO_ZOOMED_DESK_HALF_SIZE = NO_ZOOMED_DESK_SIZE / 2;
    // размер ячейки сетки
    static final int NO_ZOOMED_GIRD_MARGIN = NO_ZOOMED_DESK_SIZE / 2;
    // размер ячейки сетки (при оключенной галочке выравнивания)
    static final int NO_ZOOMED_SMALL_GIRD_MARGIN = NO_ZOOMED_DESK_SIZE / 4;

    static final int MY_SETTINGS_PROFILE_ID = 1;
    static final String MY_SETTINGS_PROFILE_NAME = "default";
    // intent ID редактируемого обьекта
    public static final String EDITED_CABINET_ID = "id";

    // переданный в intent id кабинета
    long cabinetId;


    // чекбоксы
    //  выравнивание
    boolean isLeveling = true;
    //  вывод сетки
    boolean isGird = false;

    // лист с партами
    ArrayList<CabinetRedactorPoint> desksList = new ArrayList<>();
    // режим: нет, перемещение, zoom
    int mode = NONE;
    // растяжение по осям//TODO переделать в int со значениями от 1 до 100?
    float multiplier = 0;//0,1;10
    // текущее смещение по осям
    int xAxisPXOffset = 0;
    int yAxisPXOffset = 0;
    // id выбранной парты
    long checkedDeskId = -1;
    // координаты выбранной парты до того как ее начали перемещать
    // (нужны если при переходе к зуму нужно вернуть ее на место)
    int checkedDeskLastPxPositionX;
    int checkedDeskLastPxPositionY;
    // координаты кнопки добавить парту
    static Rect addButtonSizeRect;


    // слой с партами
    RelativeLayout out;
    // слой позади out(например для сетки)
    RelativeLayout outBackground;
    // текст в центре экрана
    TextView stateText;
    // картинка с иконками
    ImageView instrumentalImage;


    // точка середины между пальцами при первой постановке
    // (нужна, чтобы при зуме был общий центр относительно которого все менялось)
    //int[] xDeskPxStart = {};
    //int[] yDeskPxStart = {};
    // точка середины между пальцами за предыдущую итерацию
    Point oldMid = new Point();
    // множитель за предыдущую итерацию
    float oldMultiplier = 0;
    // предыдущее растояние между пальцам
    float oldDist = 1f;


// ================ не одобренные переменные =======================================================//todo
    //
    //
//


    //    // начальная середина касания 2 пальцев
//    PointF startMid = new PointF();
//    // текущая середина
//    PointF nowMid = new PointF();


//    //+при зуме происходит смещение осей, и их растягивание (за растягивание отвечаем переменная multiplier)+
//    //нулевые координаты (используются только для view при перемещении двумя пальцами координаты для бд не меняются)
//    //меняются координаты и идет смещение касания
//    //начальные
//    float xAxisPXOffsetStart = 0;
//    float yAxisPXOffsetStart = 0;

//    //изначальный множитель
//    float startMultiplier = multiplier;


    //начальные параметры обьекта
//    float[] widthOld = {};
//    float[] heightOld = {};
//    float[] xOld = {};
//    float[] yOld = {};

//    float[] afterZoomX = {};
//    float[] afterZoomY = {};


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
                    isLeveling = false;
                    //убираем галочку
                    menuItem.setChecked(false);
                } else {
                    //возвращаем подведение парт
                    isLeveling = true;
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
                    isGird = false;
                    menuItem.setChecked(false);
                    //убираем сетку
                    outLines();
                } else {
                    //ставим галочку
                    menuItem.setChecked(true);
                    isGird = true;
                    //возвращаем подведение парт
                    outLines();
                }
                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }


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


//---------------создание экрана---------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_redactor);
        out = (RelativeLayout) findViewById(R.id.redactor_out);
        outBackground = (RelativeLayout) findViewById(R.id.redactor_out_background);

        //кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

// id кабинета из намерения
        cabinetId = getIntent().getLongExtra(EDITED_CABINET_ID, 1);//получаем id кабинета
        Log.i("TeachersApp", "CabinetRedactorActivity - onCreate editedObjectId = " + cabinetId);

// --------- загружаем данные из бд ---------
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
// размеры интерфейса
        // коэффициент
        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 40f * getResources().getInteger(R.integer.desks_screen_multiplier);
        //todo смещение по оси X
        //todo смещение по оси Y


// заголовок
        Cursor cabinetCursor = db.getCabinets(cabinetId);
        cabinetCursor.moveToFirst();
        setTitle(
                getTitle() + " " + cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME))
        );
        cabinetCursor.close();

// ВЫВОД ПАРТ
        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);//курсор с партами
        while (desksCursor.moveToNext()) {//начальный вывод парт (какой view появился позже тот и отображаться будет выше)
            // достаем данные из бд
            long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID));
            int numberOfPlaces = desksCursor.getInt(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES));
            long deskXDp = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X));
            long deskYDp = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y));
            // создаем view парты
            final RelativeLayout deskLayout = new RelativeLayout(this);
            deskLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_2_yellow_spot));
            // размеры парты
            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams(
                    pxFromDp(NO_ZOOMED_DESK_SIZE * numberOfPlaces * multiplier),
                    pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
            );
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            deskLayoutParams.leftMargin = pxFromDp(deskXDp * multiplier);//todo а где смещение?
            deskLayoutParams.topMargin = pxFromDp(deskYDp * multiplier);
            //ставим параметры
            deskLayout.setLayoutParams(deskLayoutParams);
            //выводим парту
            out.addView(deskLayout);
            //добавлем парту и данные в массив
            desksList.add(new CabinetRedactorPoint(
                    deskId,
                    deskLayout,
                    deskLayoutParams.leftMargin,
                    deskLayoutParams.topMargin,
                    numberOfPlaces
            ));

        }
        desksCursor.close();

// -------- инициализация интерфейса --------
        // ставим размеры области удаления через размеры out
        out.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                out.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                addButtonSizeRect = new Rect(
                        out.getWidth() / 2 - pxFromDp(50) / 2,
                        out.getHeight() - pxFromDp(50),
                        out.getWidth() / 2 + pxFromDp(50) / 2,
                        out.getHeight()
                );
            }
        });

// кнопка добавить парту
        // размеры и положение
        instrumentalImage = (ImageView) findViewById(R.id.activity_cabinet_redactor_instrumental_image);
        ImageView instrumentalImageBackground;
        instrumentalImageBackground = (ImageView) findViewById(R.id.activity_cabinet_redactor_instrumental_image_background);
        // нажатие на +
        instrumentalImageBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // выбираем количество мест на парте и создаем
                createDesk(2);
            }
        });
        instrumentalImageBackground.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // выбираем количество мест на парте и создаем
                createDesk(1);
                return true;
            }
        });

//текст в центре
        stateText = (TextView) findViewById(R.id.cabinet_redactor_state_text);
        //если парт нет, то показываем текст
        if (desksList.size() == 0) {
            stateText.setText(R.string.cabinet_redactor_activity_text_help);
        } else {
            stateText.setText("");
        }
// вывод сетки
        outLines();

// касание вывода парт
        out.setOnTouchListener(this);
// TODO ------------------------------------------------------------------------------------ до сюда

// инициализируем кнопки zoom
        final ImageView buttonZoomOut = (ImageView) findViewById(R.id.cabinet_redactor_button_zoom_out);
        final ImageView buttonZoomIn = (ImageView) findViewById(R.id.cabinet_redactor_button_zoom_in);
        //приближение
/*
        buttonZoomIn.setOnClickListener(new View.OnClickListener() {
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
                    buttonZoomOut.setEnabled(true);
                    buttonZoomOut.setImageResource(R.drawable.ic_vector_zoom_out_dark);
                    //выводим все
                    multiplier = (last+4) / 40f * getResources().getInteger(R.integer.desks_screen_multiplier);
                    for (int i = 0; i < desksList.size(); i++) {
                        //создаем новые параметры
                        desksList.get(i).setDeskParams(
                                pxFromDp(desksList.get(i).x) * multiplier,
                                pxFromDp(desksList.get(i).y) * multiplier,
                                pxFromDp(NO_ZOOMED_DESK_SIZE * desksList.get(i).numberOfPlaces * multiplier),
                                pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
                        );
                    }
                    //---перевывод сетки---
                    outLines();
                } else {//деактивируем кнопку если приближать нельзя
                    buttonZoomIn.setEnabled(false);
                    buttonZoomIn.setImageResource(R.drawable.ic_vector_zoom_in_light);
                }
            }
        });
        //отдаление
        buttonZoomOut.setOnClickListener(new View.OnClickListener() {
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
                            last - 4
                    );
                    db.close();
                    //активируем другую если приближать можно
                    buttonZoomIn.setEnabled(true);
                    buttonZoomIn.setImageResource(R.drawable.ic_vector_zoom_in_dark);
                    //выводим все
                    multiplier = (last - 4) / 40f * getResources().getInteger(R.integer.desks_screen_multiplier);
                    for (int i = 0; i < desksList.size(); i++) {

                        //создаем новые параметры
                        desksList.get(i).setDeskParams(
                                pxFromDp(desksList.get(i).x) * multiplier,
                                pxFromDp(desksList.get(i).y) * multiplier,
                                (int) pxFromDp(NO_ZOOMED_DESK_SIZE * desksList.get(i).numberOfPlaces * multiplier),
                                (int) pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier),
                                multiplier
                        );
                    }
                    //---перевывод сетки---
                    outLines();
                } else {//деактивируем кнопку если отдалять нельзя
                    buttonZoomOut.setEnabled(false);
                    buttonZoomOut.setImageResource(R.drawable.ic_vector_zoom_out_light);
                }
            }
        });
*/

    }// TODO размер скругления по мультипликатору


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

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
            case MotionEvent.ACTION_DOWN: {// поставили первый палец

//                //todo-- (для отладки)
//                for (int i = 0; i < desksList.size(); i++) {
//                    RelativeLayout relativeLayout = new RelativeLayout(this);
//                    relativeLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
//
//                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(100, 100);
//                    layoutParams.leftMargin = (int) motionEvent.getX()-50;
//                    layoutParams.topMargin = (int) motionEvent.getY()-50;
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//
//                    out.addView(relativeLayout, layoutParams);
//                }
//                //todo--


                // ищем совпадение координат пальца и парты (с конца списка)
                int i = desksList.size() - 1;
                //checkedDeskId = -1;
                while (i >= 0 && checkedDeskId == -1) {
                    if (// проверяем координаты парты
                            (motionEvent.getX() >= desksList.get(i).pxX) &&
                                    (motionEvent.getX() <= (desksList.get(i).pxX + pxFromDp(NO_ZOOMED_DESK_SIZE * desksList.get(i).numberOfPlaces) * multiplier)) &&
                                    (motionEvent.getY() >= desksList.get(i).pxY) &&
                                    (motionEvent.getY() <= (desksList.get(i).pxY + pxFromDp(NO_ZOOMED_DESK_SIZE) * multiplier))
                    ) {
                        // если нашли
                        // получаем id выбранной парты
                        checkedDeskId = desksList.get(i).deskId;

                        // ставим режим: перемещение
                        mode = DRAG;
                        // запоминаем старые координаты парты
                        checkedDeskLastPxPositionX = desksList.get(i).pxX;
                        checkedDeskLastPxPositionY = desksList.get(i).pxY;

                        // совмещаем точку нажатия(-NO_ZOOMED_DESK_SIZE;-NO_ZOOMED_DESK_SIZE/2) и центр парты
                        desksList.get(i).setDeskPosition(
                                (int) motionEvent.getX() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * desksList.get(i).numberOfPlaces * multiplier),
                                (int) motionEvent.getY() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * multiplier)
                        );
                        // ставим иконку для удаления
                        instrumentalImage.setImageResource(R.drawable.ic_vector_basket);
                    }
                    i--;
                }
                Log.i(TAG, "MotionEvent.ACTION_DOWN count:" + motionEvent.getPointerCount() + " mode:" + mode);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:// поставили следующий палец
                // если уже поставлен второй палец на остальные не реагируем
                if (motionEvent.getPointerCount() == 2) {
                    // если какая-то парта была сдвинута, то ставим ее обратно
                    if (mode == DRAG) {
                        // старые координаты графической парте (находим нажатую парту по id)
                        desksList.get(getCheckedDeskNumber()).setDeskPosition(
                                checkedDeskLastPxPositionX,
                                checkedDeskLastPxPositionY
                        );
                        // прекращаем перемещение
                        mode = NONE;
                        // ставим изображение в плюс
                        instrumentalImage.setImageResource(R.drawable.ic_vector_plus);
                        // снимаем выбор с парты
                        checkedDeskId = -1;
                    }
                    // --------- начинаем zoom -----------
                    // находим изначальное растояние между пальцами
                    oldDist = spacing(motionEvent);
                    if (oldDist > 10f) {// todo нужна ли эта проверка (+ проверка деления на 0 в scale)
                        mode = ZOOM;
                        // начальные данные о пальцах
                        oldMultiplier = multiplier;
                        oldMid = findMidPoint(motionEvent);

                    }
                }
                Log.i(TAG, "MotionEvent.ACTION_POINTER_DOWN: count=" + motionEvent.getPointerCount() + " mode:" + mode);
                break;
            case MotionEvent.ACTION_MOVE:
//                //todo-- (для отладки)
//
//                    RelativeLayout relativeLayout = new RelativeLayout(this);
//                    relativeLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
//
//                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(100, 100);
//                    layoutParams.leftMargin =  (xAxisPXOffset);
//                    layoutParams.topMargin =  (yAxisPXOffset);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//
//                    out.addView(relativeLayout, layoutParams);
//                Log.i(TAG, "onTouch: multiplier=" + multiplier);
//
//                //todo--

                if (mode == DRAG) {// режим перемещения парты
                    // находим нажатую парту по id
                    int i = getCheckedDeskNumber();
                    //desksList.get(i).setDeskPosition(1000,1000);
                    //если палец находится в пределах крестика то удаляем парту
                    if ((motionEvent.getX() >= addButtonSizeRect.left) &&
                            (motionEvent.getX() <= addButtonSizeRect.right) &&
                            (motionEvent.getY() >= addButtonSizeRect.top) &&
                            (motionEvent.getY() <= addButtonSizeRect.bottom)
                    ) {
                        Log.i("TeachersApp", "ACTION_MOVE - удалена парта " + desksList.get(i).deskId);
                        // удаляем парту
                        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                        // из базы данных
                        db.deleteDesk(desksList.get(i).deskId);
                        // с экрана
                        out.removeView(desksList.get(i).desk);
                        // из списка
                        desksList.remove(i);
                        // возвращаем картинку
                        instrumentalImage.setImageResource(R.drawable.ic_vector_plus);

                        // прекращаем перемещение парты
                        mode = NONE;
                        // снимаем выбор
                        checkedDeskId = -1;

                        // нет парт, то показываем текст
                        if (desksList.size() == 0) {
                            stateText.setText(R.string.cabinet_redactor_activity_text_help);
                        } else {
                            stateText.setText("");
                        }
                    } else {
                        // новые параметры парте на экране
                        desksList.get(i).setDeskPosition(
                                (int) motionEvent.getX() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * desksList.get(i).numberOfPlaces * multiplier),
                                (int) motionEvent.getY() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * multiplier)
                        );
                    }
                } else if (mode == ZOOM) {//зумим
                    // текущая середина касания пальцев
                    Point nowMid = findMidPoint(motionEvent);
                    //находим коэффициент разницы между новым и изначальным расстоянием между пальцами
                    float nowDist = spacing(motionEvent);
                    float scale = nowDist * 100 / oldDist;
                    // -------- сам зум --------
                    //todo можно будет еще сделать ограничение на слишком маленький зум, тогда будет работать только перемещение if(Math.abs(newDist-oldDist) > 0,5); только разница между предыдущим и новым не велика, надо смотреть
                    if (
                        //Math.abs(nowDist - oldDist) > 0.8 &&
                        //(scale / 100) > 0.001f &&//слишком маленький коэффициент//todo подобрать методом тыка
                            (multiplier * scale / 100 >= 0.25f) &&//слишком маленький размер//todo подобрать методом тыка
                                    (multiplier * scale / 100 <= 4f)//слишком большой размер//todo подобрать методом тыка
                    ) {
                        // переназначаем смещение осей из-за зума
                        xAxisPXOffset = nowMid.x - ((int) ((nowMid.x - xAxisPXOffset) * scale)) / 100;
                        yAxisPXOffset = nowMid.y - ((int) ((nowMid.y - yAxisPXOffset) * scale)) / 100;
                        // меняя множитель назначаем растяжение осей
                        multiplier = multiplier * scale / 100;
                        // пробегаемся по партам
                        for (int i = 0; i < desksList.size(); i++) {
                            if(i == 12)Log.i(TAG, "onTouch: ------>answer =" + (nowMid.x - ((int) (scale * (nowMid.x - desksList.get(i).pxX))) / 100) +
                                    " nowMid.x=" + nowMid.x + " scale=" + scale +" desksList.get(i).pxX="+desksList.get(i).pxX
                            );


                            // новые координаты и размеры
                            desksList.get(i).setDeskParams(
                                    // трансформация координаты относительно центра пальцев
                                    nowMid.x - ((int) (scale * (nowMid.x - desksList.get(i).pxX))) / 100,
                                    nowMid.y - ((int) (scale * (nowMid.y - desksList.get(i).pxY))) / 100,
                                    // трансформация размера за счет мультипликатора
                                    pxFromDp(NO_ZOOMED_DESK_SIZE * desksList.get(i).numberOfPlaces * multiplier),
                                    pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
                            );
                        }
                    } //else {//-----------------------

                    // -------- перемещение центра пальцев --------
                    // пробегаемся по партам
                    for (int i = 0; i < desksList.size(); i++) {
                        // обновляем координаты изменяя только положение парт
                        desksList.get(i).setDeskPosition(
                                desksList.get(i).pxX + nowMid.x - oldMid.x,
                                desksList.get(i).pxY + nowMid.y - oldMid.y
                        );
                    }
                    //переназначаем центр осей по перемещению центра пальцев
                    xAxisPXOffset = xAxisPXOffset + nowMid.x - oldMid.x;
                    yAxisPXOffset = yAxisPXOffset + nowMid.y - oldMid.y;
                    // }//-------------------------------

                    //выводим сетку после зума и перемещения
                    outLines();
                    // текущие позиции пальцев становятся предыдущими
                    oldDist = nowDist;
                    oldMid = nowMid;
                }
                //Log.i(TAG, "MotionEvent.ACTION_MOVE count:" + motionEvent.getPointerCount() + " mode:" + mode);
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                // todo может сделать выравнивание парт еще здесь?
                DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                //todo сделать отдельный метод для редактирования коэффициента растяжения без имени
                db.setSettingsProfileParameters(
                        MY_SETTINGS_PROFILE_ID,
                        MY_SETTINGS_PROFILE_NAME,
                        //todo смещение по оси X
                        //todo смещение по оси Y
                        (int) (multiplier * 40f / getResources().getInteger(R.integer.desks_screen_multiplier)));
                mode = NONE;
                outLines();
                Log.i(TAG, "MotionEvent.ACTION_POINTER_UP count:" + motionEvent.getPointerCount() + " mode:" + mode);
                break;
            }
            case MotionEvent.ACTION_UP:
                if (mode == DRAG) {// заканчиваем перемещение парты
                    // номер парты в списке
                    int deskNumber = getCheckedDeskNumber();

                    // точка заданная пальцем
                    Point fingerP = new Point(// координата - расстояние до центра пальца
                            (int) motionEvent.getX() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * desksList.get(deskNumber).numberOfPlaces * multiplier),
                            (int) motionEvent.getY() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * multiplier)
                    );
                    // выравнивание выбранной точки по сетке
                    levelDeskCoordinates(fingerP);

                    // новые координаты графической парте
                    desksList.get(deskNumber).setDeskPosition(fingerP.x, fingerP.y);
                    // новые координаты в бд
                    DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                    db.setDeskCoordinates(checkedDeskId,
                            (long) (dpFromPx(fingerP.x - xAxisPXOffset) / multiplier),
                            (long) (dpFromPx(fingerP.y - yAxisPXOffset) / multiplier)
                    );
                    db.close();

                    // ставим изображение в плюс
                    instrumentalImage.setImageResource(R.drawable.ic_vector_plus);
                    // снимаем с парты выбор
                    checkedDeskId = -1;
                }
                // прекращаем перемещение
                mode = NONE;
                Log.i(TAG, "MotionEvent.ACTION_UP count:" + motionEvent.getPointerCount() + " mode:" + mode);
                break;
            case MotionEvent.ACTION_CANCEL:
        }
        return true;
    }

    // вывести сетку
    void outLines() {
        outBackground.removeAllViews();
        //выводится ли полоски
        if (isGird) {
            float x;
            float y;
            x = (xAxisPXOffset % pxFromDp(NO_ZOOMED_GIRD_MARGIN * multiplier));
            y = (yAxisPXOffset % pxFromDp(NO_ZOOMED_GIRD_MARGIN * multiplier));

            DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
            //пробежка по x
            while (x < displaymetrics.widthPixels) {
                //вывод вертикальной полосы
                x = x + pxFromDp(NO_ZOOMED_GIRD_MARGIN * multiplier);
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
                y = y + pxFromDp(NO_ZOOMED_GIRD_MARGIN * multiplier);
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

    // получаем из листа порядковый номер по списку выбранной парты
    private int getCheckedDeskNumber() {
        int ans = -1;
        int i = 0;
        while (i < desksList.size() && ans == -1)
            if (desksList.get(i).deskId == checkedDeskId) {
                ans = i;
            } else
                i++;
        return ans;
    }

    // выравнивание парты по сетке (координаты парты от границ экрана)(но выравнивание по смещённым осям)
    private void levelDeskCoordinates(Point notLevelPx) {// TODO: 05.05.2019 попробовать расстановку парт со старой версии перенести на новую версию, сохранят ли парты свое местоположение?

        // если чекбокс выравнивания не нажат берем минимальный размер сетки
        int girdSpacingPx;
        if (isLeveling) {
            girdSpacingPx = pxFromDp(NO_ZOOMED_GIRD_MARGIN * multiplier);
        } else
            girdSpacingPx = pxFromDp(NO_ZOOMED_SMALL_GIRD_MARGIN * multiplier);

        // смотрим куда ближе отнимать или прибавлять
        // x
        if ((notLevelPx.x - xAxisPXOffset) % girdSpacingPx < girdSpacingPx / 2) {
            notLevelPx.x = notLevelPx.x - ((notLevelPx.x - xAxisPXOffset) % girdSpacingPx);// <= смещаем влево
        } else
            notLevelPx.x = notLevelPx.x + girdSpacingPx - ((notLevelPx.x - xAxisPXOffset) % girdSpacingPx);// => смещаем вправо
        // y
        if ((notLevelPx.y - yAxisPXOffset) % girdSpacingPx < girdSpacingPx / 2) {
            notLevelPx.y = notLevelPx.y - ((notLevelPx.y - yAxisPXOffset) % girdSpacingPx);// <= смещаем вверх
        } else
            notLevelPx.y = notLevelPx.y + girdSpacingPx - ((notLevelPx.y - yAxisPXOffset) % girdSpacingPx);// => смещаем вниз
    }

    // ---- Расстояние между первым и вторым пальцами из event ----
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // ---- координата середины между первым и вторым пальцами из event ----
    private Point findMidPoint(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new Point((int) (x / 2), (int) (y / 2));
    }

    private int pxFromDp(float dp) {// TODO: 05.05.2019 перевести множитель в этот метод если мультипликатор будет везде, где переводим в пиксели
        return (int) (dp * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    private float dpFromPx(int px) {
        return px / getApplicationContext().getResources().getDisplayMetrics().density;
    }

    @Override
    public void onStop() {// TODO я так понимаю содержимое этого метода уже не правильно
        //чистим данные перед выходом
        xAxisPXOffset = 0;
        yAxisPXOffset = 0;
        super.onStop();
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

    // --------------------- метод по созданию парты ---------------------
    void createDesk(int numberOfPlaces) {
        // узнаем размеры экрана из класса Display
        DisplayMetrics metricsB = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metricsB);

        // создаем графическую парту
        RelativeLayout newDeskLayout = new RelativeLayout(this);
        // и ее параметры
        RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(NO_ZOOMED_DESK_SIZE * numberOfPlaces * multiplier), (int) pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier));
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        newDeskLayoutParams.leftMargin = metricsB.widthPixels / 2 - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * numberOfPlaces * multiplier);
        newDeskLayoutParams.topMargin = metricsB.heightPixels / 2 - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * multiplier);
        newDeskLayout.setLayoutParams(newDeskLayoutParams);
        //ставим парте Drawable
        newDeskLayout.setBackground(getResources().getDrawable(R.drawable.start_screen_3_2_yellow_spot));
        // и выводим ее
        out.addView(newDeskLayout);

        // открываем бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // сохраняем созданную парту в базу данных
        long deskId = db.createDesk(numberOfPlaces, (int) (dpFromPx(metricsB.widthPixels / 2) / multiplier), (int) (dpFromPx(metricsB.heightPixels / 2) / multiplier), cabinetId);
        // создаем в базе данных места на парте
        for (int i = 0; i < numberOfPlaces; i++) {
            db.createPlace(deskId, i + 1);
        }

        // добавляем созданную парту в лист
        desksList.add(new CabinetRedactorPoint(
                deskId,
                newDeskLayout,
                newDeskLayoutParams.leftMargin,
                newDeskLayoutParams.topMargin,
                numberOfPlaces
        ));

        //появилась парта, удаляем текст
        stateText.setText("");
    }

    // --------------------------------- класс содержащий в себе парту ---------------------------------
    class CabinetRedactorPoint {
        long deskId;
        int numberOfPlaces;
        RelativeLayout desk;
        int pxX;
        int pxY;

        CabinetRedactorPoint(long id, RelativeLayout relativeDesk, int pxX, int pxY, int numberOfPlaces) {
            this.deskId = id;
            this.desk = relativeDesk;
            this.pxX = pxX;
            this.pxY = pxY;
            this.numberOfPlaces = numberOfPlaces;
        }

        void setDeskParams(int pxX, int pxY, int pxWidth, int pxHeight) {
            //создаем новые параметры
            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams(
                    pxWidth,
                    pxHeight
            );
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            deskLayoutParams.leftMargin = pxX;
            deskLayoutParams.topMargin = pxY;
            //и присваиваем их парте
            desk.setLayoutParams(deskLayoutParams);

            this.pxX = pxX;
            this.pxY = pxY;
        }

        void setDeskPosition(int pxX, int pxY) {
            //desk.getLayoutParams().width и desk.getWidth() это совершенно разные переменные

            // двигаем парту
            RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                    desk.getLayoutParams().width,
                    desk.getLayoutParams().height
            );
            newParams.leftMargin = pxX;
            newParams.topMargin = pxY;
            desk.setLayoutParams(newParams);


            //desk.setLeft(pxX);
            //desk.setTop(pxY);
            // двигаем координаты в списке
            this.pxX = pxX;
            this.pxY = pxY;
        }

    }
}
