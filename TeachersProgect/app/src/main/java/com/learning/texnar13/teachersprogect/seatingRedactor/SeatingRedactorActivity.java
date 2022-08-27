package com.learning.texnar13.teachersprogect.seatingRedactor;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

//класс с редактором рассадки учеников принимает на вход id урока
/*
 * выводим уже рассаженных учеников
 *    _________________________________
 *   |                             сохр|
 *   |_________________________________|
 *   |                                 |
 *   |    _______         _______      |
 *   |   | + |имя|       |имя|имя|     |
 *   |   |___|фам|       |фам|фам|     |
 *   |    _______         _______      |
 *   |   |имя|имя|       |имя| + |     |
 *   |   |фам|фам|       |фам|___|     |
 *   |    _______         _______      |
 *   |   | + |имя|       | + | + |     |
 *   |   |___|фам|       |___|___|     |
 *   |    _______         _______      |
 *   |   | + | + |       | + | + |     |
 *   |   |___|___|       |___|___|     |
 *   |                                 |
 *   |                                 |
 *   |                                 |
 *   |                                 |
 *   |_________________________________|
 * по нажатию на + открывается список с нерассаженными учениками,
 * из которого по нажатию выбираем ученика, которого хотим посадить на это место
 *
 * при сохранении добавляем / удаляем зависимости ученик-место
 * */
public class SeatingRedactorActivity extends AppCompatActivity implements View.OnTouchListener, ChooseLearnerDialogInterface {
    public static final String TAG = "TeachersApp";

    // константы по которым получаем id класса и кабинета из intent
    final public static String CLASS_ID = "classId";
    final public static String CABINET_ID = "cabinetId";


    // id класса
    static private long learnersClassId;
    // id кабинета
    static private long cabinetId;

    // массив с учениками
    private static MyLearner[] learners;
    // массив с партами
    private static DeskUnit[] desks;

    // номер выбранной парты
    private static int chosenDeskPosition;
    // номер выбранного места
    private static int chosenPlacePosition;

    // выводятся ли плюсы
    static boolean isPlus;


    // layout для вывода всего
    private RelativeLayout out;


    //- не отсортировано

    float currentDensity;

    // размер одноместной парты
    static final int NO_ZOOMED_DESK_SIZE = 40;
    // ширина границы вокруг клетки ученика на парте
    static final int NO_ZOOMED_LEARNER_BORDER_SIZE = NO_ZOOMED_DESK_SIZE / 10;

    // режимы зума
    private static final int NONE = 0;
    private static final int ZOOM = 2;
    // константа выбранного режима
    private int mode = NONE;

    // растяжение по осям
    static private float multiplier;//0,1;10
    // текущее смещение по осям
    static private float xAxisPXOffset;
    static private float yAxisPXOffset;


    // точка середины между пальцами за предыдущую итерацию
    static private Point oldMid;
    // предыдущее растояние между пальцам
    static private float oldDist;


    //-


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.seating_redactor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.seating_redactor_menu_clear).setOnMenuItemClickListener(item -> {

            // убираем парту и место из выбранного
            chosenDeskPosition = -1;
            chosenPlacePosition = -1;

            // если количество учеников не нулевое выводим картинку + на всех местах
            isPlus = (learners.length != 0);

            // открываем бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(SeatingRedactorActivity.this);

            for (DeskUnit desk : desks)// по массиву с партами
                for (int placeI = 0; placeI < desk.placesId.length; placeI++) {// по местам за партой
                    if (desk.learnersIndexes[placeI] != -1) {// если на этом месте сидит ученик
                        // удаляем из базы данных
                        db.deleteLearnerAndPlaceAttitudeById(desk.attitudesId[placeI]);

                        // разрываем связи в списках
                        learners[desk.learnersIndexes[placeI]].deskNumber = -1;
                        learners[desk.learnersIndexes[placeI]].placeNumber = -1;
                        desk.learnersIndexes[placeI] = -1;
                        desk.attitudesId[placeI] = -1;
                    }

                    // чистим рзметку
                    desk.viewPlaceOut[placeI].removeAllViews();
                    // если можно выводим плюс
                    if (isPlus) {
                        // выводим картинку +
                        ImageView lernerImage = new ImageView(SeatingRedactorActivity.this);
                        LinearLayout.LayoutParams lernerImageParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        lernerImage.setImageResource(R.drawable.lesson_activity_learner_add);
                        desk.viewPlaceOut[placeI].addView(lernerImage, lernerImageParams);

                        // если отображается кнопка плюс, то назначаем отступы контейнеру
                        int padding = (int) pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * 1 * multiplier);
                        desk.viewPlaceOut[placeI].setPadding(padding, padding, padding, padding);
                    }
                }
            db.close();
            return true;
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // получение плотности экрана сейчас, чтобы не получать его при вызове pxFromDP
        currentDensity = SeatingRedactorActivity.this.getResources()
                .getDisplayMetrics().density;

        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // раздуваем layout
        setContentView(R.layout.seating_redactor_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar(findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }

        // цвета статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.base_background_color, getTheme()));

            // включен ли ночной режим
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            if (Configuration.UI_MODE_NIGHT_YES != currentNightMode)
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        out = findViewById(R.id.seating_redactor_room);
        out.setOnTouchListener(this);


        // получаем id класса и кабинета из intent
        learnersClassId = getIntent().getLongExtra(CLASS_ID, -1L);
        cabinetId = getIntent().getLongExtra(CABINET_ID, -1L);
        if (learnersClassId == -1 || cabinetId == -1) {
            finish();
            return;
        }


        // проверяем был ли это просто поворот или создание активности
        //if (learners == null) {//todo придумать как выполнять этот код только один раз но при этом каждый переворот выводить парты
        // получаем данные из бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);


        // получаем кабинет из бд
        Cursor cabinetCursor = db.getCabinet(cabinetId);
        cabinetCursor.moveToFirst();
        // получаем множитель из кабинета  (0.25 <-> 4)
        multiplier = 0.0375F * cabinetCursor.getLong(cabinetCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER))
                + 0.25F;
        // и отступы
        xAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X));
        yAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y));
        // получаем название кабинета
        String cabinetName = cabinetCursor.getString(cabinetCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_NAME));
        cabinetCursor.close();


        // получаем класс из бд
        Cursor classCursor = db.getLearnersClases(learnersClassId);
        classCursor.moveToFirst();
        // получаем название класса
        String learnersClassName = classCursor.getString(classCursor.getColumnIndexOrThrow(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
        classCursor.close();

        // укорачиваем поля если они слишком длинные Loading…
        if (learnersClassName.length() > 6) {
            learnersClassName = learnersClassName.substring(0, 5) + "…";// absde -> abc…  abcd->abcd
        }
        if (cabinetName.length() > 6) {
            cabinetName = cabinetName.substring(0, 5) + "…";
        }


        // выставляем заголовок
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(learnersClassName + ", " + cabinetName);


        // получаем учеников
        Cursor learnersCursor = db.getLearnersByClassId(learnersClassId);
        learners = new MyLearner[learnersCursor.getCount()];
        for (int learnerI = 0; learnerI < learnersCursor.getCount(); learnerI++) {
            learnersCursor.moveToPosition(learnerI);
            learners[learnerI] = new MyLearner(
                    learnersCursor.getLong(learnersCursor.getColumnIndexOrThrow(SchoolContract.TableLearners.KEY_ROW_ID)),
                    learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
                    learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(SchoolContract.TableLearners.COLUMN_SECOND_NAME))
            );
        }
        learnersCursor.close();


        // получаем парты
        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);
        desks = new DeskUnit[desksCursor.getCount()];
        for (int deskI = 0; deskI < desksCursor.getCount(); deskI++) {
            desksCursor.moveToPosition(deskI);

            // id парты
            long deskId = desksCursor.getLong(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.KEY_ROW_ID));

            // получаем места на парте
            Cursor placesCursor = db.getPlacesByDeskId(deskId);
            // количество мест на парте
            int countOfPlaces = desksCursor.getInt(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES));
            // id мест
            long[] placesId = new long[countOfPlaces];
            int[] learnersIndexes = new int[countOfPlaces];
            long[] attitudesId = new long[countOfPlaces];
            // по местам
            for (int placeI = 0; placeI < placesId.length; placeI++) {
                placesCursor.moveToPosition(placeI);

                // номер места на парте
                int placePoz = (int) placesCursor.getLong(placesCursor.getColumnIndexOrThrow(SchoolContract.TablePlaces.COLUMN_ORDINAL)) - 1;
                // находим id места
                placesId[placePoz] = placesCursor.getLong(placesCursor.getColumnIndexOrThrow(SchoolContract.TablePlaces.KEY_ROW_ID));

                // получаем id зависимости и номер ученика сидящего на этой парте
                attitudesId[placePoz] = -1;
                learnersIndexes[placePoz] = -1;
                // пробегаемся по ученикам (берем первую найденную)
                for (int learnerI = 0; learnerI < learners.length && attitudesId[placePoz] == -1; learnerI++) {

                    // опрашиваем, есть ли место
                    Cursor attitudesCursor = db.getAttitudeByLearnerIdAndPlaceId(learners[learnerI].learnerId, placesId[placePoz]);
                    // если она есть
                    if (attitudesCursor.moveToFirst()) {

                        // сохраняем id зависимоси в парту
                        attitudesId[placePoz] = attitudesCursor.getLong(attitudesCursor.getColumnIndexOrThrow(
                                SchoolContract.TableLearnersOnPlaces.KEY_ROW_ID));
                        // и номер ученика в парту
                        learnersIndexes[placePoz] = learnerI;

                        // и сохраняем в ученика его позицию
                        learners[learnerI].deskNumber = deskI;
                        learners[learnerI].placeNumber = placePoz;
                    }
                    attitudesCursor.close();
                }
            }
            placesCursor.close();


            // создаем view для парты и выводим его
            RelativeLayout deskLayout = new RelativeLayout(SeatingRedactorActivity.this);
            out.addView(deskLayout);

            // создаем парту
            desks[deskI] = new DeskUnit(
                    deskLayout,
                    pxFromDp(desksCursor.getLong(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_X))
                            * multiplier) + xAxisPXOffset,
                    pxFromDp(desksCursor.getLong(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_Y))
                            * multiplier) + yAxisPXOffset,
                    learnersIndexes,
                    deskId,
                    placesId,
                    attitudesId,
                    deskI
            );
        }
        desksCursor.close();
        db.close();

        // проверяем все ли ученики рассажены
        isPlus = false;
        for (MyLearner learner : learners) {
            if (learner.deskNumber == -1)
                isPlus = true;
        }

        // заполняем все ячейки без учеников
        for (DeskUnit desk : desks) {
            desk.outOrClearEmptyPlus();
        }
    }


    // обратная связь от диалога выбора ученика
    @Override
    public void chooseLearner(int learnerPosition) {
        Log.e(TAG, "chooseLearner: " + learners[learnerPosition].name);

        // добавляем связи в списках
        learners[learnerPosition].setDeskAndPlaceNumber(chosenDeskPosition, chosenPlacePosition);
        desks[chosenDeskPosition].learnersIndexes[chosenPlacePosition] = learnerPosition;
        //добавляем зависимость в базу получаем id
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        desks[chosenDeskPosition].attitudesId[chosenPlacePosition] = db.setLearnerOnPlace(
                learners[learnerPosition].learnerId,
                desks[chosenDeskPosition].placesId[chosenPlacePosition]
        );
        db.close();

        // выводим картинку и текст ученика
        desks[chosenDeskPosition].outLearner(chosenPlacePosition);


        // убираем парту и место из выбранного
        chosenDeskPosition = -1;
        chosenPlacePosition = -1;

        // проверяем все ли ученики рассажены
        isPlus = false;
        for (MyLearner learner : learners) {
            if (learner.deskNumber == -1)
                isPlus = true;
        }

        // заполняем все ячейки без учеников
        for (DeskUnit desk : desks) {
            desk.outOrClearEmptyPlus();
        }
    }


    // обработка зума
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

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {// поставили первый палец
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN:// поставили следующий палец
                // если уже поставлен второй палец на остальные не реагируем
                if (motionEvent.getPointerCount() == 2) {

                    // --------- начинаем zoom -----------
                    // находим изначальное растояние между пальцами
                    oldDist = spacing(motionEvent);
                    // готовимся к зуму
                    mode = ZOOM;
                    // начальные данные о пальцах
                    oldMid = findMidPoint(motionEvent);

                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {//зумим
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
                        for (int i = 0; i < desks.length; i++) {
//                            if (i == 12)
//                                Log.i(TAG, "onTouch: ------>answer =" + (nowMid.x - ((int) (scale * (nowMid.x - desks[i].pxX))) / 100) +
//                                        " nowMid.x=" + nowMid.x + " scale=" + scale + " desksList.get(i).pxX=" + desks[i].pxX
//                                );

                            // новые координаты и размеры
                            desks[i].setDeskParams(
                                    // трансформация координаты относительно центра пальцев
                                    nowMid.x - ((scale * (nowMid.x - desks[i].pxX))) / 100,
                                    nowMid.y - ((scale * (nowMid.y - desks[i].pxY))) / 100,
                                    // трансформация размера за счет мультипликатора
                                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * desks[i].placesId.length * multiplier),
                                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
                            );
                        }
                    }

                    // -------- перемещение центра пальцев --------
                    // пробегаемся по партам
                    for (DeskUnit desk : desks) {
                        // обновляем координаты изменяя только положение парт
                        desk.setDeskPosition(
                                desk.pxX + nowMid.x - oldMid.x,
                                desk.pxY + nowMid.y - oldMid.y
                        );
                    }
                    //переназначаем центр осей по перемещению центра пальцев
                    xAxisPXOffset = xAxisPXOffset + nowMid.x - oldMid.x;
                    yAxisPXOffset = yAxisPXOffset + nowMid.y - oldMid.y;

                    // текущие позиции пальцев становятся предыдущими
                    oldDist = nowDist;
                    oldMid = nowMid;
                }
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
                db.close();
                mode = NONE;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // прекращаем перемещение
                mode = NONE;
        }

        return true;
    }

    // Расстояние между первым и вторым пальцами из event
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // координата середины между первым и вторым пальцами из event
    private Point findMidPoint(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new Point((int) (x / 2), (int) (y / 2));
    }


    // класс ученика
    class MyLearner {

        // id ученика
        long learnerId;
        // имя ученика
        String name;
        // фамилия ученика
        String lastName;

        // номер парты за которой сидит ученик
        int deskNumber = -1;
        // номер места на котором сидит ученик
        int placeNumber = -1;

        MyLearner(long learnerId, String name, String lastName) {
            this.learnerId = learnerId;
            this.name = name;
            this.lastName = lastName;
        }

        void setDeskAndPlaceNumber(int deskNumber, int placeNumber) {
            this.deskNumber = deskNumber;
            // номер места на котором сидит ученик
            this.placeNumber = placeNumber;
        }
    }

    // класс парты
    class DeskUnit {

        // id парты
        private long deskId;
        // id мест на парте
        private long[] placesId;
        // id зависимостей ученик-место
        private long[] attitudesId;

        // индексы учеников сидящих на местах (длинна соответствует количеству учеников)
        private int[] learnersIndexes;
        // контейнеры мест учеников
        private LinearLayout[] viewPlaceOut;
        // тексты имен учеников
        private TextView[] learnersTextViews;

        // контейнер парты
        private RelativeLayout viewDesk;

        // координаты смещения парты
        private float pxX;
        private float pxY;


        DeskUnit(RelativeLayout deskLayout, float pxX, float pxY, int[] tempLearnersIndexes, long deskId, long[] placesId, long[] tempAttitudesId, final int deskPoz) {
            this.pxX = pxX;
            this.pxY = pxY;
            this.deskId = deskId;
            this.placesId = placesId;
            this.viewDesk = deskLayout;

            this.learnersIndexes = tempLearnersIndexes;
            this.attitudesId = tempAttitudesId;


            // назначаем параметры парте
            RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams(
                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * placesId.length * multiplier),
                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
            );
            newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            newDeskLayoutParams.leftMargin = (int) pxX;
            newDeskLayoutParams.topMargin = (int) pxY;
            this.viewDesk.setLayoutParams(newDeskLayoutParams);
            // ставим парте Drawable
            int radius = (int) (pxFromDp(7) * multiplier);
            ShapeDrawable rectDrawable = new ShapeDrawable(new RoundRectShape(
                    new float[]{radius, radius, radius, radius, radius, radius, radius, radius},
                    new RectF(0, 0, 0, 0),
                    new float[]{0, 0, 0, 0, 0, 0, 0, 0}
            ));
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.base_light));
            this.viewDesk.setBackground(rectDrawable);


            // созаем view места
            this.viewPlaceOut = new LinearLayout[placesId.length];
            // и текстове поля
            learnersTextViews = new TextView[placesId.length];
            for (int placeI = 0; placeI < placesId.length; placeI++) {
                this.viewPlaceOut[placeI] = new LinearLayout(SeatingRedactorActivity.this);
                this.viewPlaceOut[placeI].setOrientation(LinearLayout.VERTICAL);
                this.viewPlaceOut[placeI].setGravity(Gravity.CENTER);
                this.viewPlaceOut[placeI].setWeightSum(4);
                // выравниваем его относительно парты
                RelativeLayout.LayoutParams viewPlaceOutParams = new RelativeLayout.LayoutParams(
                        (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier),
                        (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier)
                );
                viewPlaceOutParams.leftMargin = (int) pxFromDp((NO_ZOOMED_DESK_SIZE * placeI + NO_ZOOMED_LEARNER_BORDER_SIZE) * multiplier);
                viewPlaceOutParams.topMargin = (int) pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * multiplier);
                viewPlaceOutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                viewPlaceOutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                this.viewDesk.addView(viewPlaceOut[placeI], viewPlaceOutParams);

                // при нажатии на контейнер ученика
                final int finalPlaceI = placeI;
                this.viewPlaceOut[placeI].setOnClickListener(v -> {
                    // если на этом месте сидит ученик
                    if (learnersIndexes[finalPlaceI] != -1) {

                        viewPlaceOut[finalPlaceI].removeAllViews();

                        // удаляем из базы данных
                        DataBaseOpenHelper db = new DataBaseOpenHelper(SeatingRedactorActivity.this);
                        db.deleteLearnerAndPlaceAttitudeById(attitudesId[finalPlaceI]);
                        db.close();


                        // разрываем связи в списках
                        learners[learnersIndexes[finalPlaceI]].deskNumber = -1;
                        learners[learnersIndexes[finalPlaceI]].placeNumber = -1;

                        learnersIndexes[finalPlaceI] = -1;
                        attitudesId[finalPlaceI] = -1;


                        // выводим картинку + на всех местах

                        isPlus = true;
                        for (DeskUnit desk : desks) {
                            desk.outOrClearEmptyPlus();
                        }

                    } else if (isPlus) {
                        // ставим номер парты и места как выбранные
                        chosenDeskPosition = deskPoz;
                        chosenPlacePosition = finalPlaceI;

                        // вызываем диалог выбора ученика
                        ChooseLearnerDialogFragment chooseDialogFragment = new ChooseLearnerDialogFragment();

                        // передаем в параметры имена не посаженных учеников
                        ArrayList<String> notPutLearnersNames = new ArrayList<>();
                        // и их индексы
                        ArrayList<Integer> learnersIndexes = new ArrayList<>();

                        for (int learnerI = 0; learnerI < learners.length; learnerI++) {
                            if (learners[learnerI].deskNumber == -1) {
                                notPutLearnersNames.add(learners[learnerI].lastName + " " + learners[learnerI].name);
                                learnersIndexes.add(learnerI);
                            }
                        }
                        Bundle args = new Bundle();
                        args.putStringArrayList(ChooseLearnerDialogFragment.ARGS_LEARNERS_NAMES_ARRAY, notPutLearnersNames);
                        args.putIntegerArrayList(ChooseLearnerDialogFragment.ARGS_LEARNERS_INDEXES_ARRAY, learnersIndexes);
                        chooseDialogFragment.setArguments(args);
                        // показываем диалог
                        chooseDialogFragment.show(getSupportFragmentManager(), "chooseDialogFragment - Hello");

                    }
                });

                // если на этом месте сидит ученик
                if (learnersIndexes[placeI] != -1) {

                    // создаем картинку ученика
                    ImageView lernerImage = new ImageView(SeatingRedactorActivity.this);
                    LinearLayout.LayoutParams lernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
                    lernerImage.setImageResource(R.drawable.lesson_activity_learner);
                    this.viewPlaceOut[placeI].addView(lernerImage, lernerImageParams);

                    // создаем текст ученика
                    TextView learnerText = new TextView(SeatingRedactorActivity.this);
                    learnerText.setTypeface(ResourcesCompat.getFont(SeatingRedactorActivity.this, R.font.montserrat_semibold));
                    learnerText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimension(R.dimen.lesson_activity_learner_name_text_size) * 0.8F * multiplier);
                    learnerText.setAllCaps(true);
                    learnerText.setGravity(Gravity.CENTER_HORIZONTAL);
                    learnerText.setTextColor(getResources().getColor(R.color.text_color_simple));
                    learnerText.setSingleLine(true);
                    if (learners[learnersIndexes[placeI]].name.length() == 0) {
                        learnerText.setText(learners[learnersIndexes[placeI]].lastName);
                    } else
                        learnerText.setText(learners[learnersIndexes[placeI]].name.charAt(0) + " " +
                                learners[learnersIndexes[placeI]].lastName);
                    LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 3F);
                    this.viewPlaceOut[placeI].addView(learnerText, learnerTextParams);
                    // сохраняем ссылку на textView чтобы потом при зуме  менять его
                    this.learnersTextViews[placeI] = learnerText;
                }
            }

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
            viewDesk.setLayoutParams(newDeskLayoutParams);

            // ставим парте Drawable
            int radius = (int) (pxFromDp(7) * multiplier);
            ShapeDrawable rectDrawable = new ShapeDrawable(new RoundRectShape(
                    new float[]{radius, radius, radius, radius, radius, radius, radius, radius},
                    new RectF(0, 0, 0, 0),
                    new float[]{0, 0, 0, 0, 0, 0, 0, 0}
            ));
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.base_light));
            viewDesk.setBackground(rectDrawable);


            // меняем размеры мест
            for (int placeI = 0; placeI < placesId.length; placeI++) {

                // отступы и размеры места
                this.viewPlaceOut[placeI].getLayoutParams().width =
                        (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier);
                this.viewPlaceOut[placeI].getLayoutParams().height =
                        (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier);
                ((RelativeLayout.LayoutParams) this.viewPlaceOut[placeI].getLayoutParams()).leftMargin =
                        (int) pxFromDp((NO_ZOOMED_DESK_SIZE * placeI + NO_ZOOMED_LEARNER_BORDER_SIZE) * multiplier);
                ((RelativeLayout.LayoutParams) this.viewPlaceOut[placeI].getLayoutParams()).topMargin =
                        (int) pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * multiplier);

                // размеры текста ученика если он есть
                if (learnersTextViews[placeI] != null)
                    learnersTextViews[placeI].setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            getResources().getDimension(R.dimen.lesson_activity_learner_name_text_size) * 0.8F * multiplier);

                // если на месте сидит ученик
                if (learnersIndexes[placeI] != -1) {
                    // то убираем отступы
                    viewPlaceOut[placeI].setPadding(0, 0, 0, 0);
                } else if (isPlus) {
                    // если отображается кнопка плюс, то назначаем отступы контейнеру
                    int padding = (int) pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * 1 * multiplier);
                    viewPlaceOut[placeI].setPadding(padding, padding, padding, padding);
                }
            }
        }

        void setDeskPosition(float pxX, float pxY) {
            //desk.getLayoutParams().width и desk.getWidth() это совершенно разные переменные
            // ----- двигаем координаты в списке -----
            this.pxX = pxX;
            this.pxY = pxY;

            // ----- двигаем парту -----
            RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                    viewDesk.getLayoutParams().width,
                    viewDesk.getLayoutParams().height
            );
            newParams.leftMargin = (int) pxX;
            newParams.topMargin = (int) pxY;
            viewDesk.setLayoutParams(newParams);

            // ----- создаем drawable -----
            // радиус скругления
            int radius = (int) (pxFromDp(7) * multiplier);
            ShapeDrawable rectDrawable = new ShapeDrawable(new RoundRectShape(
                    new float[]{radius, radius, radius, radius, radius, radius, radius, radius},// внешний радиус скругления
                    new RectF(0, 0, 0, 0),// размеры внутренней пустой области
                    new float[]{0, 0, 0, 0, 0, 0, 0, 0}// внутренний радиус скругления
            ));
            // задаем цвет
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.base_light));
            // и ставим его на задний фон layout
            viewDesk.setBackground(rectDrawable);
        }

        void outOrClearEmptyPlus() {
            for (int placesI = 0; placesI < viewPlaceOut.length; placesI++) {

                // если на месте нет ученика и можно выводить плюсы
                if (learnersIndexes[placesI] == -1)
                    if (isPlus) {
                        viewPlaceOut[placesI].removeAllViews();

                        // выводим картинку +
                        ImageView lernerImage = new ImageView(SeatingRedactorActivity.this);
                        LinearLayout.LayoutParams lernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        lernerImage.setImageResource(R.drawable.lesson_activity_learner_add);
                        viewPlaceOut[placesI].addView(lernerImage, lernerImageParams);

                        // если отображается кнопка плюс, то назначаем отступы контейнеру
                        int padding = (int) pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * 1 * multiplier);
                        viewPlaceOut[placesI].setPadding(padding, padding, padding, padding);

                    } else {
                        viewPlaceOut[placesI].removeAllViews();
                    }
            }
        }

        void outLearner(int place) {
            this.viewPlaceOut[place].removeAllViews();

            // если не отображается кнопка плюс, то убираем отступы
            viewPlaceOut[place].setPadding(0, 0, 0, 0);

            // создаем картинку ученика
            ImageView lernerImage = new ImageView(SeatingRedactorActivity.this);
            LinearLayout.LayoutParams lernerImageParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
            lernerImage.setImageResource(R.drawable.lesson_activity_learner);
            this.viewPlaceOut[place].addView(lernerImage, lernerImageParams);

            // создаем текст ученика
            TextView learnerText = new TextView(SeatingRedactorActivity.this);
            learnerText.setTypeface(ResourcesCompat.getFont(SeatingRedactorActivity.this, R.font.montserrat_semibold));
            learnerText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.lesson_activity_learner_name_text_size) * 0.8F * multiplier);
            learnerText.setAllCaps(true);
            learnerText.setGravity(Gravity.CENTER_HORIZONTAL);
            learnerText.setTextColor(getResources().getColor(R.color.text_color_simple));
            learnerText.setSingleLine(true);
            if (learners[learnersIndexes[place]].name.length() == 0) {
                learnerText.setText(learners[learnersIndexes[place]].lastName);
            } else
                learnerText.setText(learners[learnersIndexes[place]].name.charAt(0) + " " + learners[learnersIndexes[place]].lastName);
            LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 3F);
            this.viewPlaceOut[place].addView(learnerText, learnerTextParams);
            // сохраняем ссылку на textView чтобы потом при зуме  менять его
            this.learnersTextViews[place] = learnerText;
        }
    }

    private float pxFromDp(float dp) {
        return dp * currentDensity;
    }


    // нажатие кнопки назад
    @Override
    public void onBackPressed() {

        // чтобы если на активность зашли снова, то данные перезаписались
        learners = null;

        super.onBackPressed();
    }

    // нажатие кнопки назад в toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
