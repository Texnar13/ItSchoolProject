package com.learning.texnar13.teachersprogect.seatingRedactor;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    private static long learnersClassId;
    // id кабинета
    private static long cabinetId;

    // массив с учениками
    private static MyLearner[] learners;
    // массив с партами
    private static DeskUnit[] desks;

    // номер выбранной парты
    private static int chosenDeskPosition;
    // номер выбранного места
    private static int chosenPlacePosition;

    // выводятся ли плюсы
    boolean isPlus;


    // layout для вывода всего
    private RelativeLayout out;


    //- не отсортировано


    // размер одноместной парты
    static final int NO_ZOOMED_DESK_SIZE = 40;
    // ширина границы вокруг клетки ученика на парте
    static final int NO_ZOOMED_LEARNER_BORDER_SIZE = NO_ZOOMED_DESK_SIZE / 20;

    // режимы зума
    private static final int NONE = 0;
    private static final int ZOOM = 2;
    // константа выбранного режима
    private int mode = NONE;

    // растяжение по осям
    private float multiplier = 0;//0,1;10
    // текущее смещение по осям
    private float xAxisPXOffset = 0;
    private float yAxisPXOffset = 0;


    // точка середины между пальцами за предыдущую итерацию
    private Point oldMid = new Point();
    // множитель за предыдущую итерацию
    private float oldMultiplier = 0;
    // предыдущее растояние между пальцам
    private float oldDist = 1f;


    //-


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // проверить/получить данные
        // заплнить учеников
        // заполнить парты связав с зависимостями на парте учеников которые уже рассажены


        // вывести учеников и парты


        // при нажатии на место на парте записываем номера выбранной парты и места


        /////


        // получаем id класса и кабинета из intent
        learnersClassId = getIntent().getLongExtra(CLASS_ID, -1);
        cabinetId = getIntent().getLongExtra(CABINET_ID, -1);
        if (learnersClassId == -1 || cabinetId == -1) {
            finish();
            return;
        }


        // layout для вывода всего
        out = new RelativeLayout(this);
        out.setOnTouchListener(this);
        setContentView(out);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar


        // проверяем был ли это просто поворот или создание активности
        if (learners == null) {
            // получаем данные из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);


            // получаем кабинет из бд
            Cursor cabinetCursor = db.getCabinet(cabinetId);
            cabinetCursor.moveToFirst();
            // получаем множитель из кабинета  (0.25 <-> 4)
            multiplier = 0.0375F * cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER))
                    + 0.25F;
            // и отступы
            xAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X));
            yAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y));
            // получаем название кабинета
            String cabinetName = cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
            cabinetCursor.close();


            // получаем класс из бд
            Cursor classCursor = db.getLearnersClass(learnersClassId);
            classCursor.moveToFirst();
            // получаем название класса
            String learnersClassName = classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            classCursor.close();
            // выставляем заголовок
            setTitle(learnersClassName + ", " + cabinetName);


            // получаем учеников
            Cursor learnersCursor = db.getLearnersByClassId(learnersClassId);
            learners = new MyLearner[learnersCursor.getCount()];
            for (int learnerI = 0; learnerI < learnersCursor.getCount(); learnerI++) {
                learnersCursor.moveToPosition(learnerI);
                learners[learnerI] = new MyLearner(
                        learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)),
                        learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
                        learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME))
                );
            }
            learnersCursor.close();


            // получаем парты
            Cursor desksCursor = db.getDesksByCabinetId(cabinetId);
            desks = new DeskUnit[desksCursor.getCount()];
            for (int deskI = 0; deskI < desksCursor.getCount(); deskI++) {
                desksCursor.moveToPosition(deskI);

                // id парты
                long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID));

                // получаем места на парте
                Cursor placesCursor = db.getPlacesByDeskId(deskId);
                // количество мест на парте
                int countOfPlaces = desksCursor.getInt(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES));
                // id мест
                long[] placesId = new long[countOfPlaces];
                int[] learnersIndexes = new int[countOfPlaces];
                long[] attitudesId = new long[countOfPlaces];
                // по местам
                for (int placeI = 0; placeI < placesId.length; placeI++) {
                    placesCursor.moveToPosition(placeI);

                    // номер места на парте
                    int placePoz = (int) placesCursor.getLong(placesCursor.getColumnIndex(SchoolContract.TablePlaces.COLUMN_ORDINAL)) - 1;
                    // находим id места
                    placesId[placePoz] = placesCursor.getLong(placesCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID));

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
                            attitudesId[placePoz] =
                                    attitudesCursor.getLong(attitudesCursor.getColumnIndex(SchoolContract.TableLearnersOnPlaces.KEY_ATTITUDE_ID));
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
                RelativeLayout deskLayout = new RelativeLayout(getApplicationContext());
                out.addView(deskLayout);

                // создаем парту
                desks[deskI] = new DeskUnit(
                        deskLayout,
                        pxFromDp(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * multiplier) + xAxisPXOffset,
                        pxFromDp(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * multiplier) + yAxisPXOffset,
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
                desk.outEmpty();
            }

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
            desk.outEmpty();
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
                    oldMultiplier = multiplier;
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
                            if (i == 12)
                                Log.i(TAG, "onTouch: ------>answer =" + (nowMid.x - ((int) (scale * (nowMid.x - desks[i].pxX))) / 100) +
                                        " nowMid.x=" + nowMid.x + " scale=" + scale + " desksList.get(i).pxX=" + desks[i].pxX
                                );

                            // новые координаты и размеры
                            desks[i].setDeskParams(
                                    // трансформация координаты относительно центра пальцев
                                    nowMid.x - ((scale * (nowMid.x - desks[i].pxX))) / 100,
                                    nowMid.y - ((scale * (nowMid.y - desks[i].pxY))) / 100,
                                    // трансформация размера за счет мультипликатора
                                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * desks[i].placesId.length * multiplier),
                                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
                            );
                            // новые размеры элементам ученика
                            for (int placeI = 0; placeI < desks[i].learnersIndexes.length; placeI++) {
//                                if (desks[i].learnersIndexes[placeI] != -1)
//                                    learnersAndTheirGrades[desksList.get(i).seatingLearnerNumber[placeI]].setSizes(
//                                            multiplier, placeI
//                                    );
                            }

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
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.backgroundLiteGray));
            this.viewDesk.setBackground(rectDrawable);


            // созаем view места
            this.viewPlaceOut = new LinearLayout[placesId.length];
            // и текстове поля
            learnersTextViews = new TextView[placesId.length];
            for (int placeI = 0; placeI < placesId.length; placeI++) {
                this.viewPlaceOut[placeI] = new LinearLayout(getApplicationContext());
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
                this.viewPlaceOut[placeI].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // если на этом месте сидит ученик
                        if (learnersIndexes[finalPlaceI] != -1) {

                            viewPlaceOut[finalPlaceI].removeAllViews();

                            // удаляем из базы данных
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
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
                                desk.outEmpty();
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

                    }
                });


                // если на этом месте сидит ученик
                if (learnersIndexes[placeI] != -1) {


                    // создаем картинку ученика
                    ImageView lernerImage = new ImageView(getApplicationContext());
                    LinearLayout.LayoutParams lernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
                    lernerImage.setImageResource(R.drawable.lesson_learner_0_gray);
                    this.viewPlaceOut[placeI].addView(lernerImage, lernerImageParams);

                    // создаем текст ученика
                    TextView learnerText = new TextView(getApplicationContext());
                    learnerText.setTextSize(8 * multiplier);
                    learnerText.setAllCaps(true);
                    learnerText.setGravity(Gravity.CENTER_HORIZONTAL);
                    learnerText.setTextColor(Color.BLACK);
                    learnerText.setSingleLine(true);
                    if (learners[learnersIndexes[placeI]].name.length() == 0) {
                        learnerText.setText(learners[learnersIndexes[placeI]].lastName);
                    } else
                        learnerText.setText(learners[learnersIndexes[placeI]].name.charAt(0) + " " + learners[learnersIndexes[placeI]].lastName);
                    LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 3F);
                    this.viewPlaceOut[placeI].addView(learnerText, learnerTextParams);
                    // сохраняем ссылку на textView чтобы потом при зуме  менять его
                    this.learnersTextViews[placeI] = learnerText;

                    // место LinearLayout
                    // -- картинка ImageView
                    // -- текст TextView


//                    //создание места
//                    final LinearLayout tempPlaceLayout = new LinearLayout(this);
//                    tempPlaceLayout.setOrientation(LinearLayout.VERTICAL);
//                    //tempPlaceLayout.setBackgroundColor(Color.parseColor("#e4ea7e"));
//                    //настраиваем параметры под конкретное место
//                    tempRelativeLayoutPlaceParams = new RelativeLayout.LayoutParams(
//                            (int) pxFromDp((1000 - 50) * multiplier),
//                            (int) pxFromDp((1000 - 50) * multiplier));
//                    tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                    tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                    tempRelativeLayoutPlaceParams.leftMargin = (int) pxFromDp((25 + (1000 * (placeUnit.ordinalNumber - 1))) * multiplier);
//                    tempRelativeLayoutPlaceParams.topMargin = (int) pxFromDp(25 * multiplier);
//                    Log.i("TeachersApp", "SeatingRedactorActivity - draw view place:" + placeUnit.id);
//                    //сажаем ученика на место
//                    if (learnerId != -1) {//если id ученика не равно -1 то выводим ученика иначе кнопку добавить ученика
//                        //final переменная
//                        final long finalLearnerId = learnerId;
//                        final int finalAttitudeIndex = attitudeIndex;
//                        //создание картинки ученика
//                        final ImageView tempLernerImage = new ImageView(this);
//                        final LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
//                        tempLernerImage.setImageResource(R.drawable.lesson_learner_0_gray);//по умолчанию серая картинка
//
//                        //создание текста ученика
//                        final TextView tempLearnerText = new TextView(this);
//                        tempLearnerText.setTextSize(200 * multiplier);
//                        final LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 3F);
//                        tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
//                        tempLearnerText.setTextColor(Color.GRAY);
//                        String learnerLastName = "";
//                        //получаем текст ученика
//                        for (int i = 0; i < learnersList.size(); i++) {
//                            if (learnersList.get(i).id == learnerId) {
//                                learnerLastName = learnersList.get(i).lastName;
//                                break;
//                            }
//                        }
//                        tempLearnerText.setText(learnerLastName);
//                        tempLearnerText.setText(learnerLastName);
//                        //картинка ученика
//                        tempLernerImage.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                db.deleteAttitudeByLearnerIdAndPlaceId(finalLearnerId, placeUnit.id);//сразу удаляем запись по id ученика и урока
//                                attitudesList.remove(finalAttitudeIndex);
//                                drawDesks(db);
//                            }
//                        });
//                        tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);
//
//                        //текст ученика
//                        tempLearnerText.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                db.deleteAttitudeByLearnerIdAndPlaceId(finalLearnerId, placeUnit.id);//сразу удаляем запись по id ученика и урока
//                                attitudesList.remove(finalAttitudeIndex);
//                                drawDesks(db);
//
//                            }
//                        });
//                        tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
//                    } else {
//                        //создание кнопки добавить ученика
//                        final ImageView tempImageAdd = new ImageView(getApplicationContext());
//                        final LinearLayout.LayoutParams tempImageAddParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                        tempImageAdd.setImageResource(R.drawable.ic_menu_add_standart);
//                        tempImageAdd.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                tempPlaceLayout.removeAllViews();
//                                Bundle bundle = new Bundle();
//                                bundle.putLong("cabinetId", cabinetId);
//                                bundle.putLong("classId", classId);
//                                ChooseLearnerDialogFragment dialogFragment = new ChooseLearnerDialogFragment();
//                                dialogFragment.setArguments(bundle);
//                                dialogFragment.show(getFragmentManager(), "chooseLearners");
//                                handler = new Handler() {
//                                    public void handleMessage(android.os.Message msg) {
//                                        //возврат -1 если ничего не выбрано иначе id ученика
//                                        if (msg.what != -1) {
//                                            //добавляем запись по id ученика и урока
//
//                                            attitudesList.add(//добавляем зависимость в локальный массив
//                                                    new AttitudeUnit(
//                                                            db.setLearnerOnPlace(msg.what, placeUnit.id),//добавляем зависимость в базу получаем id
//                                                            msg.what,//id ученика
//                                                            placeUnit.id//d места
//                                                    )
//                                            );
//                                        }
//                                        drawDesks(db);
//                                    }
//                                };
//                            }
//                        });
//                        if (learnersList.size() != attitudesList.size())
//                            tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);
//                    }
//
//
//                    //добавление места в парту
//                    tempRelativeLayoutDesk.addView(tempPlaceLayout, tempRelativeLayoutPlaceParams);
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
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.backgroundLiteGray));
            viewDesk.setBackground(rectDrawable);


            // меняем размеры мест
            for (int placeI = 0; placeI < placesId.length; placeI++) {

                // отступы и размеры места
                ((RelativeLayout.LayoutParams) this.viewPlaceOut[placeI].getLayoutParams()).width =
                        (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier);
                ((RelativeLayout.LayoutParams) this.viewPlaceOut[placeI].getLayoutParams()).height =
                        (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier);
                ((RelativeLayout.LayoutParams) this.viewPlaceOut[placeI].getLayoutParams()).leftMargin =
                        (int) pxFromDp((NO_ZOOMED_DESK_SIZE * placeI + NO_ZOOMED_LEARNER_BORDER_SIZE) * multiplier);
                ((RelativeLayout.LayoutParams) this.viewPlaceOut[placeI].getLayoutParams()).topMargin =
                        (int) pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * multiplier);

                // размеры текста если он есть
                if (learnersTextViews[placeI] != null) {
                    // меняем размер текста ученика
                    learnersTextViews[placeI].setTextSize(8 * multiplier);
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
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.backgroundLiteGray));
            // и ставим его на задний фон layout
            viewDesk.setBackground(rectDrawable);
        }

        void outEmpty() {
            for (int placesI = 0; placesI < viewPlaceOut.length; placesI++) {

                // если на месте нет ученика и можно выводить плюсы
                if (learnersIndexes[placesI] == -1 && isPlus) {
                    viewPlaceOut[placesI].removeAllViews();

                    // выводим картинку +
                    ImageView lernerImage = new ImageView(getApplicationContext());
                    LinearLayout.LayoutParams lernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    lernerImage.setImageResource(R.drawable._button_circle_plus);
                    viewPlaceOut[placesI].addView(lernerImage, lernerImageParams);
                }
            }
        }

        void outLearner(int place) {
            this.viewPlaceOut[place].removeAllViews();

            // создаем картинку ученика
            ImageView lernerImage = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams lernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
            lernerImage.setImageResource(R.drawable.lesson_learner_0_gray);
            this.viewPlaceOut[place].addView(lernerImage, lernerImageParams);

            // создаем текст ученика
            TextView learnerText = new TextView(getApplicationContext());
            learnerText.setTextSize(8 * multiplier);
            learnerText.setAllCaps(true);
            learnerText.setGravity(Gravity.CENTER_HORIZONTAL);
            learnerText.setTextColor(Color.BLACK);
            learnerText.setSingleLine(true);
            if (learners[learnersIndexes[place]].name.length() == 0) {
                learnerText.setText(learners[learnersIndexes[place]].lastName);
            } else
                learnerText.setText(learners[learnersIndexes[place]].name.charAt(0) + " " + learners[learnersIndexes[place]].lastName);
            LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 3F);
            this.viewPlaceOut[place].addView(learnerText, learnerTextParams);
            // сохраняем ссылку на textView чтобы потом при зуме  менять его
            this.learnersTextViews[place] = learnerText;
        }
    }

    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    // нажатие кнопки назад
    @Override
    public void onBackPressed() {
        //setResult(RESULT_OK);

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


//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//        //----выводим всё----
//        drawDesks(db);
//    }
//
//
//    //------------вывести парты------------
//    private void drawDesks(final DataBaseOpenHelper db) {
//        RelativeLayout room = findViewById(R.id.seating_redactor_room);
//        room.removeAllViews();
//
//        for (DeskUnit deskUnit : desksList) {//пробегаемся по выгруженным партам
//
//            //создание парты
//            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
//            tempRelativeLayoutDesk.setBackgroundColor(Color.LTGRAY);
//            tempRelativeLayoutDesk.setBackgroundResource(R.drawable.start_screen_3_3_green_spot);
//            //настраиваем параметры под конкретную парту
//            tempRelativeLayoutDeskParams = new RelativeLayout.LayoutParams(
//                    (int) pxFromDp(1000 * deskUnit.countOfPlaces * multiplier),
//                    (int) pxFromDp(1000 * multiplier));//размеры проставляются далее индивидуально
//            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            tempRelativeLayoutDeskParams.leftMargin =
//                    (int) pxFromDp(deskUnit.x * 25 * multiplier);
//            tempRelativeLayoutDeskParams.topMargin =
//                    (int) pxFromDp(deskUnit.y * 25 * multiplier);
//            Log.i("TeachersApp", "SeatingRedactorActivity - draw - view desk:" + deskUnit.id);
//
//            for (final PlaceUnit placeUnit : deskUnit.placesList) {//проходим по местам на парте
//                //создание места и ученика
//
//                //получаем id ученика
//                long learnerId = -1;
//                int attitudeIndex = -1;
//                for (int i = 0; i < attitudesList.size(); i++) {
//                    if (attitudesList.get(i).placeId == placeUnit.id) {
//                        learnerId = attitudesList.get(i).learnerId;
//                        attitudeIndex = i;
//                        break;
//                    }
//                }
//                //создание места
//                final LinearLayout tempPlaceLayout = new LinearLayout(this);
//                tempPlaceLayout.setOrientation(LinearLayout.VERTICAL);
//                //tempPlaceLayout.setBackgroundColor(Color.parseColor("#e4ea7e"));
//                //настраиваем параметры под конкретное место
//                tempRelativeLayoutPlaceParams = new RelativeLayout.LayoutParams(
//                        (int) pxFromDp((1000 - 50) * multiplier),
//                        (int) pxFromDp((1000 - 50) * multiplier));
//                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                tempRelativeLayoutPlaceParams.leftMargin = (int) pxFromDp((25 + (1000 * (placeUnit.ordinalNumber - 1))) * multiplier);
//                tempRelativeLayoutPlaceParams.topMargin = (int) pxFromDp(25 * multiplier);
//                Log.i("TeachersApp", "SeatingRedactorActivity - draw view place:" + placeUnit.id);
//                //сажаем ученика на место
//                if (learnerId != -1) {//если id ученика не равно -1 то выводим ученика иначе кнопку добавить ученика
//                    //final переменная
//                    final long finalLearnerId = learnerId;
//                    final int finalAttitudeIndex = attitudeIndex;
//                    //создание картинки ученика
//                    final ImageView tempLernerImage = new ImageView(this);
//                    final LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
//                    tempLernerImage.setImageResource(R.drawable.lesson_learner_0_gray);//по умолчанию серая картинка
//
//                    //создание текста ученика
//                    final TextView tempLearnerText = new TextView(this);
//                    tempLearnerText.setTextSize(200 * multiplier);
//                    final LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 3F);
//                    tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
//                    tempLearnerText.setTextColor(Color.GRAY);
//                    String learnerLastName = "";
//                    //получаем текст ученика
//                    for (int i = 0; i < learnersList.size(); i++) {
//                        if (learnersList.get(i).id == learnerId) {
//                            learnerLastName = learnersList.get(i).lastName;
//                            break;
//                        }
//                    }
//                    tempLearnerText.setText(learnerLastName);
//                    //картинка ученика
//                    tempLernerImage.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            db.deleteAttitudeByLearnerIdAndPlaceId(finalLearnerId, placeUnit.id);//сразу удаляем запись по id ученика и урока
//                            attitudesList.remove(finalAttitudeIndex);
//                            drawDesks(db);
//                        }
//                    });
//                    tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);
//
//                    //текст ученика
//                    tempLearnerText.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            db.deleteAttitudeByLearnerIdAndPlaceId(finalLearnerId, placeUnit.id);//сразу удаляем запись по id ученика и урока
//                            attitudesList.remove(finalAttitudeIndex);
//                            drawDesks(db);
//
//                        }
//                    });
//                    tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
//                } else {
//                    //создание кнопки добавить ученика
//                    final ImageView tempImageAdd = new ImageView(getApplicationContext());
//                    final LinearLayout.LayoutParams tempImageAddParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                    tempImageAdd.setImageResource(R.drawable.ic_menu_add_standart);
//                    tempImageAdd.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            tempPlaceLayout.removeAllViews();
//                            Bundle bundle = new Bundle();
//                            bundle.putLong("cabinetId", cabinetId);
//                            bundle.putLong("classId", classId);
//                            ChooseLearnerDialogFragment dialogFragment = new ChooseLearnerDialogFragment();
//                            dialogFragment.setArguments(bundle);
//                            dialogFragment.show(getFragmentManager(), "chooseLearners");
//                            handler = new Handler() {
//                                public void handleMessage(android.os.Message msg) {
//                                    //возврат -1 если ничего не выбрано иначе id ученика
//                                    if (msg.what != -1) {
//                                        //добавляем запись по id ученика и урока
//
//                                        attitudesList.add(//добавляем зависимость в локальный массив
//                                                new AttitudeUnit(
//                                                        db.setLearnerOnPlace(msg.what, placeUnit.id),//добавляем зависимость в базу получаем id
//                                                        msg.what,//id ученика
//                                                        placeUnit.id//d места
//                                                )
//                                        );
//                                    }
//                                    drawDesks(db);
//                                }
//                            };
//                        }
//                    });
//                    if (learnersList.size() != attitudesList.size())
//                        tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);
//                }
//
//
//                //добавление места в парту
//                tempRelativeLayoutDesk.addView(tempPlaceLayout, tempRelativeLayoutPlaceParams);
//            }
//
//            //добавление парты в комнату
//            room.addView(tempRelativeLayoutDesk, tempRelativeLayoutDeskParams);
//        }
//
////--------размеры комнаты по самой дальней парте-------
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                (maxDeskX + (int) pxFromDp(3000 * multiplier)),
//                (maxDeskY + (int) pxFromDp(3000 * multiplier))
//        );
//
//        //если ширина экрана все-таки больше
//        if (getResources().getDisplayMetrics().widthPixels >= maxDeskX + (int) (pxFromDp(3000) * multiplier)) {
//            layoutParams.width = getResources().getDisplayMetrics().widthPixels;
//        }
//        //если высота экрана всетаки больше
//        if (getResources().getDisplayMetrics().heightPixels - (int) pxFromDp(72) >= maxDeskY + (int) (pxFromDp(3000) * multiplier)) {
//            layoutParams.height = getResources().getDisplayMetrics().heightPixels - (int) pxFromDp(72);
//        }
//        room.setLayoutParams(layoutParams);
//
//        Log.i("TeachersProject", "" + (maxDeskX + (int) dpFromPx((2000 + 1000) * multiplier)) + "" + (maxDeskY + (int) dpFromPx((2000 + 1000) * multiplier)));
//    }
//
//    //---------------системные методы---------------
//
//    private float pxFromDp(float dp) {
//        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
//    }
//
//    private float dpFromPx(float px) {
//        return px / getApplicationContext().getResources().getDisplayMetrics().density;
//
//    }
//
//
//
////----------------диалог выбора ученика-----------------
//
//    public static class ChooseLearnerDialogFragment extends DialogFragment {//диалог по выбору не распределенного ученика
//
//        long cabinetId;
//        long classId;
//
//        public ChooseLearnerDialogFragment() {
//
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            //
//            // инициализация переменных
//            this.cabinetId = getArguments().getLong("cabinetId");
//            this.classId = getArguments().getLong("classId");
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//билдер диалога
//            final DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());//база
//            final ArrayList<Long> learnersId = db.getNotPutLearnersIdByCabinetIdAndClassId(cabinetId, classId);//лист с id нераспределенных по местам учеников
//            String[] learnersNames = new String[learnersId.size()];//массив с именами учеников(пустой)
//            for (int i = 0; i < learnersNames.length; i++) {//заполняем
//                Cursor learnerTempCursor = db.getLearner(learnersId.get(i));//получаем ученика
//                learnerTempCursor.moveToFirst();
//                //получаем имя
//                learnersNames[i] = learnerTempCursor.getString(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + learnerTempCursor.getString(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME));
//                learnerTempCursor.close();
//            }
//            //--------ставим диалогу список в виде view--------
//            ScrollView scroll = new ScrollView(getActivity());
//
//            //контейнер список
//            LinearLayout linearLayout = new LinearLayout(getActivity());
//            linearLayout.setOrientation(LinearLayout.VERTICAL);
//            linearLayout.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
//            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            ));
//            scroll.addView(linearLayout);
//
//            for (int i = 0; i < learnersNames.length; i++) {
//                //пункт списка
//                LinearLayout item = new LinearLayout(getActivity());
//                item.setOrientation(LinearLayout.VERTICAL);
//                item.setGravity(Gravity.CENTER);
//                LinearLayout.LayoutParams itemParams =
//                        new LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                (int) (pxFromDp(40) * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))
//                        );
//                itemParams.setMargins(
//                        (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
//                        (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
//                        (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
//                        (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier)))
//                );
//                linearLayout.addView(item, itemParams);
//                //текст в нем
//                TextView text = new TextView(getActivity());
//                text.setText(learnersNames[i]);
//                text.setTextColor(Color.BLACK);
//                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
//                item.addView(text);
//
//                //нажатие на пункт списка
//                final int number = i;
//                item.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Cursor learnerTempCursor = db.getLearner(learnersId.get(number));//получаем выбранного ученика
//                        learnerTempCursor.moveToFirst();
//                        //посылаем id выбранного ученика
//                        SeatingRedactorActivity.handler.sendEmptyMessage((int)
//                                learnerTempCursor.getLong(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
//                        learnerTempCursor.close();
//                        dismiss();
//                    }
//                });
//            }
//            builder.setView(scroll);
//            return builder.create();
//        }
//
//        @Override
//        public void onDismiss(DialogInterface dialog) {
//            Log.i("teachersApp", "SeatingRedactorActivity/ChooseLearnerDialogFragment/onDismiss");
//            super.onDismiss(dialog);
//            SeatingRedactorActivity.handler.sendEmptyMessage(-1);
//        }
//
//        //---------------системные методы(здесь свои)---------------
//
//        private float pxFromDp(float dp) {
//            return dp * getActivity().getResources().getDisplayMetrics().density;
//        }
//
//
//    }


//class DeskUnit {//хранит в себе одну парту
//
//
//    long id;
//    long x;
//    long y;
//    long countOfPlaces;
//    ArrayList<PlaceUnit> placesList;
//
//    DeskUnit(long id, long x, long y, long countOfPlaces) {
//        this.id = id;
//        this.x = x;
//        this.y = y;
//        this.countOfPlaces = countOfPlaces;
//        this.placesList = new ArrayList<>();
//    }
//}
//
//class PlaceUnit {//хранит в себе одно место
//    long id;
//    long deskId;
//    long ordinalNumber;
//
//    PlaceUnit(long id, long deskId, long ordinalNumber) {
//        this.id = id;
//        this.deskId = deskId;
//        this.ordinalNumber = ordinalNumber;
//    }
//}
//
//class LearnerUnit {//хранит в себе одного ученика
//    long id;
//    String name;
//    String lastName;
//    long classId;
//
//    LearnerUnit(long id, String name, String lastName, long classId) {
//        this.id = id;
//        this.name = name;
//        this.lastName = lastName;
//        this.classId = classId;
//    }
//}
//

//class AttitudeUnit {//хранит в себе зависимость ученик место
//    long id;
//    long learnerId;
//    long placeId;
//
//    AttitudeUnit(long id, long learnerId, long placeId) {
//        this.id = id;
//        this.learnerId = learnerId;
//        this.placeId = placeId;
//    }
//}


