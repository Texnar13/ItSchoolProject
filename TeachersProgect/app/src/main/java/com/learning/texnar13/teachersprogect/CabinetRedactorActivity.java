package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    // половина одноместной парты
    static final int NO_ZOOMED_DESK_HALF_SIZE = NO_ZOOMED_DESK_SIZE / 2;

    // размер ячейки сетки
    static final int NO_ZOOMED_GIRD_MARGIN = NO_ZOOMED_DESK_SIZE / 2;
    // размер ячейки сетки (при оключенной галочке выравнивания)
    static final int NO_ZOOMED_SMALL_GIRD_MARGIN = NO_ZOOMED_DESK_SIZE / 8;

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
    // растяжение по осям
    float multiplier = 0;//0,1;10
    // текущее смещение по осям
    float xAxisPXOffset = 0;
    float yAxisPXOffset = 0;
    // id выбранной парты
    long checkedDeskId = -1;
    // координаты выбранной парты до того как ее начали перемещать
    // (нужны если при переходе к зуму нужно вернуть ее на место)
    float checkedDeskLastPxPositionX;
    float checkedDeskLastPxPositionY;
    // координаты кнопки добавить парту
    static Rect addButtonSizeRect;
    // размеры дисплея
    int widthDisplaySize;
    int heightDisplaySize;


    // слой с партами
    RelativeLayout out;
    // слой позади out(например для сетки)
    RelativeLayout outBackground;
    // текст в центре экрана
    TextView stateText;
    // картинка с иконками
    ImageView instrumentalImage;
    // фон картинки с иконками
    ImageView instrumentalImageBackground;


    // точка середины между пальцами за предыдущую итерацию
    Point oldMid = new Point();
    // множитель за предыдущую итерацию
    float oldMultiplier = 0;
    // предыдущее растояние между пальцам
    float oldDist = 1f;


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

        // цвет кнопки меню
        getSupportActionBar().getThemedContext().setTheme(R.style.LessonStyle);

        // вставляем в actionBar заголовок активности
        LinearLayout titleContainer = new LinearLayout(this);
        titleContainer.setGravity(Gravity.CENTER);
        titleContainer.setBackgroundResource(R.drawable._button_round_background_orange);
        ActionBar.LayoutParams titleContainerParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.my_toolbar_text_container_height_size),
                Gravity.CENTER
        );
        titleContainerParams.leftMargin = (int) getResources().getDimension(R.dimen.double_margin);
        titleContainerParams.rightMargin = (int) getResources().getDimension(R.dimen.double_margin);
        //android:layout_centerInParent="true"

        TextView title = new TextView(this);
        title.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
        title.setSingleLine(true);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(getResources().getColor(R.color.backgroundWhite));
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        titleParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        titleContainer.addView(title, titleParams);
        getSupportActionBar().setCustomView(titleContainer, titleContainerParams);
        // выставляем свой заголовок
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


        // кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // убираем тень
        getSupportActionBar().setElevation(0);
        // цвет фона
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.backgroundWhite));
        // кнопка назад
        getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.__button_back_arrow_orange));


        // размеры экрана
        // узнаем размеры экрана из класса Display
        {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            widthDisplaySize = metrics.widthPixels;
            heightDisplaySize = metrics.heightPixels;
        }


        out = findViewById(R.id.redactor_out);
        outBackground = findViewById(R.id.redactor_out_background);

        //кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

// id кабинета из намерения
        cabinetId = getIntent().getLongExtra(EDITED_CABINET_ID, 1);//получаем id кабинета
        Log.i("TeachersApp", "CabinetRedactorActivity - onCreate editedObjectId = " + cabinetId);

// --------- загружаем данные из бд ---------
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

// заголовок и размеры интерфейса
        // получаем кабинет из бд
        Cursor cabinetCursor = db.getCabinet(cabinetId);
        cabinetCursor.moveToFirst();

        // выводим его имя
        title.setText(// todo переделать через файл строк
                cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME))
        );
        // множитель    0.25 <-> 4
        multiplier = 0.0375F *
                cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER))
                + 0.25F;
        // и отступы
        xAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X));
        yAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y));

        // закрываем курсор
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
            //выводим парту
            out.addView(deskLayout);

            //добавлем парту и данные в массив (в конструкторе заполняя view)
            desksList.add(new CabinetRedactorPoint(
                    deskId,
                    pxFromDp(deskXDp * multiplier) + xAxisPXOffset,
                    pxFromDp(deskYDp * multiplier) + yAxisPXOffset,
                    numberOfPlaces,
                    deskLayout
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
                        out.getWidth() / 2 - (int) pxFromDp(50) / 2,
                        out.getHeight() - (int) pxFromDp(50),
                        out.getWidth() / 2 + (int) pxFromDp(50) / 2,
                        out.getHeight()
                );
            }
        });

// кнопка добавить парту
        // размеры и положение
        instrumentalImage = findViewById(R.id.activity_cabinet_redactor_instrumental_image);
        instrumentalImageBackground = findViewById(R.id.activity_cabinet_redactor_instrumental_image_background);
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
        stateText = findViewById(R.id.cabinet_redactor_state_text);
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
    }


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
                                motionEvent.getX() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * desksList.get(i).numberOfPlaces * multiplier),
                                motionEvent.getY() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * multiplier)
                        );
                        // ставим иконку для удаления
                        instrumentalImage.setImageResource(0);
                        instrumentalImageBackground.setImageResource(R.drawable.__button_bucket);
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
                        instrumentalImage.setImageResource(R.drawable.__button_add);
                        instrumentalImageBackground.setImageResource(R.drawable.cabinet_redactor_add_desk_button);
                        // снимаем выбор с парты
                        checkedDeskId = -1;
                    }
                    // --------- начинаем zoom -----------
                    // находим изначальное растояние между пальцами
                    oldDist = spacing(motionEvent);
                    // готовимся к зуму
                    mode = ZOOM;
                    // начальные данные о пальцах
                    oldMultiplier = multiplier;
                    oldMid = findMidPoint(motionEvent);

                }
                Log.i(TAG, "MotionEvent.ACTION_POINTER_DOWN: count=" + motionEvent.getPointerCount() + " mode:" + mode);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {// режим перемещения парты
                    // находим нажатую парту по id
                    int i = getCheckedDeskNumber();
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
                        instrumentalImage.setImageResource(R.drawable.__button_add);
                        instrumentalImageBackground.setImageResource(R.drawable.cabinet_redactor_add_desk_button);

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
                                motionEvent.getX() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * desksList.get(i).numberOfPlaces * multiplier),
                                motionEvent.getY() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * multiplier)
                        );
                    }
                } else if (mode == ZOOM) {//зумим
                    // текущая середина касания пальцев
                    Point nowMid = findMidPoint(motionEvent);
                    //находим коэффициент разницы между новым и изначальным расстоянием между пальцами
                    float nowDist = spacing(motionEvent);
                    float scale = nowDist * 100 / oldDist;
                    // -------- сам зум --------
                    if ((multiplier * scale / 100 >= 0.25f) &&//слишком маленький размер
                            (multiplier * scale / 100 <= 4f)//слишком большой размер
                    ) {
                        // переназначаем смещение осей из-за зума
                        xAxisPXOffset = nowMid.x - (((nowMid.x - xAxisPXOffset) * scale)) / 100;
                        yAxisPXOffset = nowMid.y - (((nowMid.y - yAxisPXOffset) * scale)) / 100;
                        // меняя множитель назначаем растяжение осей
                        multiplier = multiplier * scale / 100;
                        // пробегаемся по партам
                        for (int i = 0; i < desksList.size(); i++) {
                            if (i == 12)
                                Log.i(TAG, "onTouch: ------>answer =" + (nowMid.x - ((int) (scale * (nowMid.x - desksList.get(i).pxX))) / 100) +
                                        " nowMid.x=" + nowMid.x + " scale=" + scale + " desksList.get(i).pxX=" + desksList.get(i).pxX
                                );

                            // новые координаты и размеры
                            desksList.get(i).setDeskParams(
                                    // трансформация координаты относительно центра пальцев
                                    nowMid.x - ((scale * (nowMid.x - desksList.get(i).pxX))) / 100,
                                    nowMid.y - ((scale * (nowMid.y - desksList.get(i).pxY))) / 100,
                                    // трансформация размера за счет мультипликатора
                                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * desksList.get(i).numberOfPlaces * multiplier),
                                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
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
                DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                // сохраняем множитель и смещение для этого кабинета
                db.setCabinetMultiplierOffsetXOffsetY(
                        cabinetId,
                        (int) ((multiplier - 0.25F) / 0.0375F),
                        (int) xAxisPXOffset,
                        (int) yAxisPXOffset
                );
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
                    PointF fingerP = new PointF(// координата - расстояние до центра пальца
                            motionEvent.getX() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * desksList.get(deskNumber).numberOfPlaces * multiplier),
                            motionEvent.getY() - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * multiplier)
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
                    instrumentalImage.setImageResource(R.drawable.__button_add);
                    instrumentalImageBackground.setImageResource(R.drawable.cabinet_redactor_add_desk_button);
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

            int linesWidth = (int) pxFromDp(1F);
            if (linesWidth == 0)
                linesWidth = 1;


            //пробежка по x
            while (x < widthDisplaySize) {
                //вывод вертикальной полосы
                final RelativeLayout verticalLine = new RelativeLayout(this);
                RelativeLayout.LayoutParams verticalLineLayoutParams = new RelativeLayout.LayoutParams(
                        linesWidth,
                        heightDisplaySize
                );
                verticalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                verticalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                //deskLayout.setRotation(60);
                verticalLineLayoutParams.leftMargin = (int) x;
                verticalLineLayoutParams.topMargin = 0;
                verticalLine.setLayoutParams(verticalLineLayoutParams);
                verticalLine.setBackgroundColor(getResources().getColor(R.color.backgroundDarkWhite));
                outBackground.addView(verticalLine);
                x = x + pxFromDp(NO_ZOOMED_GIRD_MARGIN * multiplier);
            }
            //пробежка по y
            while (y < heightDisplaySize) {
                //вывод горизонтальной полосы
                final RelativeLayout horizontalLine = new RelativeLayout(this);
                RelativeLayout.LayoutParams horizontalLineLayoutParams = new RelativeLayout.LayoutParams(
                        widthDisplaySize,
                        linesWidth
                );
                horizontalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                horizontalLineLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                //deskLayout.setRotation(60);
                horizontalLineLayoutParams.leftMargin = 0;
                horizontalLineLayoutParams.topMargin = (int) y;
                horizontalLine.setLayoutParams(horizontalLineLayoutParams);
                horizontalLine.setBackgroundColor(getResources().getColor(R.color.backgroundDarkWhite));
                outBackground.addView(horizontalLine);
                y = y + pxFromDp(NO_ZOOMED_GIRD_MARGIN * multiplier);

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
    private void levelDeskCoordinates(PointF notLevelPx) {// TODO: 05.05.2019 попробовать расстановку парт со старой версии перенести на новую версию, сохранят ли парты свое местоположение?

        // если чекбокс выравнивания не нажат берем минимальный размер сетки
        float girdSpacingPx;
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

    private float pxFromDp(float dp) {// может перевести множитель в этот метод если мультипликатор будет везде, где переводим в пиксели
        return (dp * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    private float dpFromPx(float px) {
        return px / getApplicationContext().getResources().getDisplayMetrics().density;
    }

    @Override
    public void onStop() {
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
        // открываем бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // сохраняем созданную парту в базу данных
        long deskId = db.createDesk(numberOfPlaces, (int) (dpFromPx(widthDisplaySize / 2F) / multiplier), (int) (dpFromPx(heightDisplaySize / 2F) / multiplier), cabinetId);
        // создаем в базе данных места на парте
        for (int i = 0; i < numberOfPlaces; i++) {
            db.createPlace(deskId, i + 1);
        }

        // создаем layout парты
        RelativeLayout newDeskLayout = new RelativeLayout(this);
        // и выводим ее
        out.addView(newDeskLayout);

        // создаем объект парты и добавляем его в лист
        desksList.add(new CabinetRedactorPoint(
                deskId,
                widthDisplaySize / 2F - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * numberOfPlaces * multiplier),
                heightDisplaySize / 2F - pxFromDp(NO_ZOOMED_DESK_HALF_SIZE * multiplier),
                numberOfPlaces,
                newDeskLayout
        ));

        //появилась парта, удаляем текст
        stateText.setText("");
    }

    // --------------------------------- класс содержащий в себе парту ---------------------------------
    class CabinetRedactorPoint {
        long deskId;
        int numberOfPlaces;
        RelativeLayout desk;
        float pxX;
        float pxY;

        CabinetRedactorPoint(long id, float pxX, float pxY, int numberOfPlaces, RelativeLayout relativeLayout) {
            this.deskId = id;
            this.pxX = pxX;
            this.pxY = pxY;
            this.numberOfPlaces = numberOfPlaces;

            // --- получаем графический контейнер ---
            this.desk = relativeLayout;

            // и создаем для него параметры
            RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams(
                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * numberOfPlaces * multiplier),// todo везде * getResources().getInteger(R.integer.desks_screen_multiplier)
                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
            );
            newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            newDeskLayoutParams.leftMargin = (int) pxX;
            newDeskLayoutParams.topMargin = (int) pxY;
            this.desk.setLayoutParams(newDeskLayoutParams);

            // ставим парте Drawable
            int radius = (int) (pxFromDp(7) * multiplier);
            ShapeDrawable rectDrawable = new ShapeDrawable(new RoundRectShape(
                    new float[]{radius, radius, radius, radius, radius, radius, radius, radius},
                    new RectF(0, 0, 0, 0),
                    new float[]{0, 0, 0, 0, 0, 0, 0, 0}
            ));
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.baseOrange));

            this.desk.setBackground(rectDrawable);
        }

        void setDeskParams(float pxX, float pxY, int pxWidth, int pxHeight) {
            this.pxX = pxX;
            this.pxY = pxY;

            // создаем новые параметры
            RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams(
                    pxWidth,
                    pxHeight
            );
            newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            newDeskLayoutParams.leftMargin = (int) pxX;
            newDeskLayoutParams.topMargin = (int) pxY;
            // и присваиваем их парте
            desk.setLayoutParams(newDeskLayoutParams);

            // ставим парте Drawable
            int radius = (int) (pxFromDp(7) * multiplier);
            ShapeDrawable rectDrawable = new ShapeDrawable(new RoundRectShape(
                    new float[]{radius, radius, radius, radius, radius, radius, radius, radius},
                    new RectF(0, 0, 0, 0),
                    new float[]{0, 0, 0, 0, 0, 0, 0, 0}
            ));
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.baseOrange));
            desk.setBackground(rectDrawable);
        }

        void setDeskPosition(float pxX, float pxY) {//desk.getLayoutParams().width и desk.getWidth() это совершенно разные переменные
            // ----- двигаем координаты в списке -----
            this.pxX = pxX;
            this.pxY = pxY;

            // ----- двигаем парту -----
            RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                    desk.getLayoutParams().width,
                    desk.getLayoutParams().height
            );
            newParams.leftMargin = (int) pxX;
            newParams.topMargin = (int) pxY;
            desk.setLayoutParams(newParams);

            // ----- создаем drawable -----
            // радиус скругления
            int radius = (int) (pxFromDp(7) * multiplier);
            ShapeDrawable rectDrawable = new ShapeDrawable(new RoundRectShape(
                    new float[]{radius, radius, radius, radius, radius, radius, radius, radius},// внешний радиус скругления
                    new RectF(0, 0, 0, 0),// размеры внутренней пустой области
                    new float[]{0, 0, 0, 0, 0, 0, 0, 0}// внутренний радиус скругления
            ));
            // задаем цвет
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.baseOrange));
            // и ставим его на задний фон layout
            desk.setBackground(rectDrawable);

            //    |    A   |     new float[]{A, B, A, B, A, B, A, B}
            // _  _________________________
            //    |       /|
            //    |    /   |
            // B  |  /     |
            // _  |/_______|
            //    |
        }
    }
}
