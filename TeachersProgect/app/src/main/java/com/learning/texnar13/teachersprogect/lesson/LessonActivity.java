package com.learning.texnar13.teachersprogect.lesson;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

/*
 * onCreate(),
 * подгружаются все ученики, id кабинета, класса, предмета, зав. урок-время
 * создаются массивы с оценками//todo (или подгружаются;)),
 *
 * эти поля должны быть статичными, и с проверкой на существование(для переворота)
 * при переходе удалить
 *
 * OnStart(),
 * выводятся парты и ученики
 *
 * */

public class LessonActivity extends AppCompatActivity implements View.OnTouchListener, GradesDialogInterface, EndLessonInterface {


    public static final String TAG = "TeachersApp";

    // режимы зума
    private static final int NONE = 0;
    private static final int ZOOM = 2;
    // размер одноместной парты
    private static final int NO_ZOOMED_DESK_SIZE = 40;
    // ширина границы вокруг клетки ученика на парте
    private static final int NO_ZOOMED_LEARNER_BORDER_SIZE = NO_ZOOMED_DESK_SIZE / 20;


    // межстраничный баннер открывающийся на экране вывода всех оценок при их сохранении
    InterstitialAd lessonEndBanner;


    // лист с партами
    static ArrayList<DeskUnit> desksList = new ArrayList<>();
    // режим: нет, перемещение, zoom
    int mode = NONE;
    // растяжение по осям
    static float multiplier = 0;//0,1;10
    // текущее смещение по осям
    static float xAxisPXOffset = 0;
    static float yAxisPXOffset = 0;


    // слой с партами
    RelativeLayout out;

    // максимальная оценка
    static int maxAnswersCount;
    // названия типов ответов и их id
    static AnswersType[] answersTypes;

    // точка середины между пальцами за предыдущую итерацию
    Point oldMid = new Point();
    // множитель за предыдущую итерацию
    float oldMultiplier = 0;
    // предыдущее растояние между пальцам
    float oldDist = 1f;


    // константа по которой получаеми id зависимости
    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    // константа по которой получаем время урока (для повторяющихся уроков)//todo костылище!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static final String LESSON_TIME = "startTime";

    // id зависимости
    static long lessonAttitudeId;
    // id класса
    private static long learnersClassId;
    // имя класса
    private static String className;
    // id предмета
    private static long subjectId;
    // имя предмета
    private static String subjectName;
    // id кабинета
    private static long cabinetId;
    // имя предмета
    private static String cabinetName;

    // массив учеников
    static private MyLearnerAndHisGrades[] learnersAndTheirGrades;

    // номер выбранного ученика
    static int chosenLearnerPosition;


    // подготовка меню
    // раздуваем
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_menu, menu);
        return true;
    }

    // назначаем действия
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // закончить урок
        menu.findItem(R.id.lesson_menu_end_lesson).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {


                long[] learnersId = new long[learnersAndTheirGrades.length];

                int[] grades1 = new int[learnersAndTheirGrades.length];
                int[] grades2 = new int[learnersAndTheirGrades.length];
                int[] grades3 = new int[learnersAndTheirGrades.length];

                int[] gradesTypes1 = new int[learnersAndTheirGrades.length];
                int[] gradesTypes2 = new int[learnersAndTheirGrades.length];
                int[] gradesTypes3 = new int[learnersAndTheirGrades.length];

                String[] learnersNames = new String[learnersAndTheirGrades.length];

                for (int j = 0; j < learnersAndTheirGrades.length; j++) {

                    grades1[j] = learnersAndTheirGrades[j].learnerGrades[0];
                    grades2[j] = learnersAndTheirGrades[j].learnerGrades[1];
                    grades3[j] = learnersAndTheirGrades[j].learnerGrades[2];

                    gradesTypes1[j] = learnersAndTheirGrades[j].learnerGradesTypes[0];
                    gradesTypes2[j] = learnersAndTheirGrades[j].learnerGradesTypes[1];
                    gradesTypes3[j] = learnersAndTheirGrades[j].learnerGradesTypes[2];

                    learnersId[j] = learnersAndTheirGrades[j].learnerId;
                    learnersNames[j] = learnersAndTheirGrades[j].lastName + " " + learnersAndTheirGrades[j].name;
                }

                // переходим к активности списка оценок
                Intent intent = new Intent(getApplicationContext(), LessonListActivity.class);
                intent.putExtra(LessonListActivity.SUBJECT_ID, subjectId);
                intent.putExtra(LessonListActivity.ARGS_STRING_ARRAY_LEARNERS_NAMES, learnersNames);

                intent.putExtra(LessonListActivity.ARGS_INT_ARRAY_LEARNERS_ID, learnersId);


                // конвертируем типы оценок

                long[] typesId = new long[answersTypes.length];
                String[] typesNames = new String[answersTypes.length];
                for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                    typesId[typeI] = answersTypes[typeI].id;
                    typesNames[typeI] = answersTypes[typeI].typeName;
                }

                intent.putExtra(LessonListActivity.STRING_GRADES_TYPES_ARRAY, typesNames);
                intent.putExtra(LessonListActivity.INT_GRADES_TYPES_ID_ARRAY, typesId);


                intent.putExtra(LessonListActivity.INT_MAX_GRADE, maxAnswersCount);

                intent.putExtra(LessonListActivity.FIRST_GRADES, grades1);
                intent.putExtra(LessonListActivity.SECOND_GRADES, grades2);
                intent.putExtra(LessonListActivity.THIRD_GRADES, grades3);

                intent.putExtra(LessonListActivity.FIRST_GRADES_TYPES, gradesTypes1);
                intent.putExtra(LessonListActivity.SECOND_GRADES_TYPES, gradesTypes2);
                intent.putExtra(LessonListActivity.THIRD_GRADES_TYPES, gradesTypes3);


                // передаем полученную дату урока
                intent.putExtra(LESSON_TIME, getIntent().getStringExtra(LESSON_TIME));

                // передаем загруженный баннер в контейнер
                //LessonListActivity.EndLessonIntentContainer intentContainer = new LessonListActivity.EndLessonIntentContainer();
                //  intentContainer.addBanner = lessonEndBanner;
                // передаем контейнер
                //intent.putExtra(LessonListActivity.ADD_BANNER, intentContainer);

                // выводим рекламму
                if(lessonEndBanner.isLoaded()){
                    lessonEndBanner.show();
                }

                startActivity(intent);


                //обнуляем данные
                chosenLearnerPosition = -1;
                lessonAttitudeId = -1;
                subjectId = 0;
                learnersClassId = 0;
                cabinetId = 0;
                subjectName = null;
                learnersAndTheirGrades = null;
                finish();

                return true;
            }
        });
        // посадить учеников
        menu.findItem(R.id.lesson_menu_edit_seating).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // намерение перехода на редактор рассадки
                Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
                // кладем id в intent
                intent.putExtra(SeatingRedactorActivity.CLASS_ID, learnersClassId);
                intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
                // переходим
                startActivity(intent);
                return true;
            }
        });
        // расставить парты
        menu.findItem(R.id.lesson_menu_edit_tables).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                // намерение перехода на редактор кабинета
                Intent intent = new Intent(getApplicationContext(), CabinetRedactorActivity.class);
                // кладем id в intent
                intent.putExtra(CabinetRedactorActivity.EDITED_CABINET_ID, cabinetId);
                // переходим
                startActivity(intent);
                return true;
            }
        });
        return true;
    }


    // создание экрана
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_main);

        // начинаем загрузку межстраничного баннера конца урока
        lessonEndBanner = new InterstitialAd(this);
        lessonEndBanner.setAdUnitId("ca-app-pub-5709922862247260/2817934566");// работает
        // создаем запрос
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("239C7C3FF5E172E5131C0FAA9994FDBF")// тестовая реклама"239C7C3FF5E172E5131C0FAA9994FDBF"
                .build();
        lessonEndBanner.loadAd(adRequest);


        // цвет кнопки меню
        getSupportActionBar().getThemedContext().setTheme(R.style.LessonStyle);

        // вставляем в actionBar заголовок активности
        LinearLayout titleContainer = new LinearLayout(this);
        titleContainer.setGravity(Gravity.CENTER);
        titleContainer.setBackgroundResource(R.drawable._button_round_background_pink);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.__button_back_arrow_pink));


        // для того, чтобы векторные изображения созданные в коде отображались нормально todo разобраться бы что это
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);


        // получаем зависимость из intent
        lessonAttitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -1);
        // -1 он будет равен только при ошибке
        if (lessonAttitudeId == -1) {
            finish();
            return;
        }

        // (проверяем по одному из полей) был создан новый экран или он просто переворачивался
        if (learnersAndTheirGrades == null) {
            // получаем данные из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // получаем зависимость
            Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(lessonAttitudeId);
            attitudeCursor.moveToFirst();
            // получаем из завмсимости id кабинета
            cabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID
            ));
            // получаем из завмсимости id предмета
            subjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID
            ));
            attitudeCursor.close();

            // получаем предмет
            Cursor subjectCursor = db.getSubjectById(subjectId);
            subjectCursor.moveToFirst();
            // получаем имя предмета
            subjectName = subjectCursor.getString(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
            // получаем id класса
            learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(
                    SchoolContract.TableSubjects.KEY_CLASS_ID
            ));
            subjectCursor.close();

            // получаем имя класа
            Cursor learnersClass = db.getLearnersClass(learnersClassId);
            learnersClass.moveToFirst();
            className = learnersClass.getString(learnersClass.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            learnersClass.close();

            // получаем имя кабинета
            Cursor cabinetNameCursor = db.getCabinet(cabinetId);
            cabinetNameCursor.moveToFirst();
            cabinetName = cabinetNameCursor.getString(cabinetNameCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
            cabinetNameCursor.close();


            // получаем учеников по id класса
            Cursor learnersCursor = db.getLearnersByClassId(learnersClassId);

            // инициализируем массив с учениками
            learnersAndTheirGrades = new MyLearnerAndHisGrades[learnersCursor.getCount()];
            //заполняем его
            for (int i = 0; i < learnersAndTheirGrades.length; i++) {
                learnersCursor.moveToPosition(i);

                // создаем нового ученика
                learnersAndTheirGrades[i] = new MyLearnerAndHisGrades(
                        learnersCursor.getLong(learnersCursor.getColumnIndex(
                                SchoolContract.TableLearners.KEY_LEARNER_ID
                        )),
                        learnersCursor.getString(learnersCursor.getColumnIndex(
                                SchoolContract.TableLearners.COLUMN_FIRST_NAME
                        )),
                        learnersCursor.getString(learnersCursor.getColumnIndex(
                                SchoolContract.TableLearners.COLUMN_SECOND_NAME
                        ))
                );
            }
            learnersCursor.close();

            // номер выбранного ученика
            chosenLearnerPosition = -1;

            //максимальная оценка
            maxAnswersCount = db.getSettingsMaxGrade(1);

            // названия типов ответов
            Cursor typesCursor = db.getGradesTypes();
            answersTypes = new AnswersType[typesCursor.getCount()];
            // извлекаем данные из курсора
            for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                typesCursor.moveToPosition(typeI);
                // добавляем новый тип во внутренний список
                answersTypes[typeI] = new AnswersType(
                        typesCursor.getLong(typesCursor.getColumnIndex(SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID)),
                        typesCursor.getString(typesCursor.getColumnIndex(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE))
                );
            }
            typesCursor.close();
            db.close();
        }


        // слой с партами
        out = findViewById(R.id.activity_lesson_room_layout);
        out.setOnTouchListener(this);


        // укорачиваем поля если они слишком длинные …
        String shortSubjectName;
        String shortClassName;
        String shortCabinetName;
        if (subjectName.length() > 10) {
            shortSubjectName = subjectName.substring(0, 9) + "…";
        } else
            shortSubjectName = subjectName;
        if (className.length() > 4) {
            shortClassName = className.substring(0, 3) + "…";// abcde -> abc…  abcd->abcd
        } else
            shortClassName = className;
        if (cabinetName.length() > 4) {
            shortCabinetName = cabinetName.substring(0, 3) + "…";
        } else
            shortCabinetName = cabinetName;

        // выставляем название предмета и класса в заголовок
        title.setText(shortSubjectName + ", " + shortClassName + ", " + shortCabinetName);


        /*
         * подгружаются все ученики, id кабинета, класса, предмета, зав. урок-время
         * создаются массивы с оценками(или подгружаются;)),
         *
         * эти поля должны быть статичными, и с проверкой на существование(для переворота)
         * при переходе (окончании урока) удалить
         */
        //3 это класс урока
//        * вывод учеников, парт и мест, по нажатию на ученика открывается доп.меню*
//        где можно выбрать оценку, нет оценки/1/2/3/4/5
//        после окончания урока учитель нажимает закончить урок
//        выводим стастику за весь урок(2)
//        *
//        *
//        *
//        *    _________________________________
//        *   |название урока     закончить урок|
//        *   |_________________________________|
//        *   |                                 |
//        *   |                                 |
//        *   |    _______         _______      |
//        *   |   |имя|имя|       |имя|имя|     |
//        *   |   |фам|фам|       |фам|фам|     |
//        *   |    _______         _______      |
//        *   |   |имя|имя|       |имя|имя|     |
//        *   |   |фам|фам|       |фам|фам|     |
//        *   |    _______         _______      |
//        *   |   |имя|имя|       |имя|имя|     |
//        *   |   |фам|фам|       |фам|фам|     |
//        *   |    _______         _______      |
//        *   |   |имя|имя|       |имя|имя|     |
//        *   |   |фам|фам|       |фам|фам|     |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |_________________________________|
//        *
//        *
//        *
//        * (2)
//        *    _________________________________
//        *   |  имя урока            сохранить |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |_________________________________|
//        *
//        *
//        * нажимаем сохранить и сохраняем в таблицу ученик-оценка
    }

    // при каждом запуске экрана
    @Override
    protected void onStart() {
        super.onStart();

        // обновляем список парт и положение учеников
        // и выводим все
        outAll();
    }


    // метод вывода учеников с партами
    void outAll() {

        // загружаем из базы данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        // получаем кабинет из бд чтобы достать размеры которые могли измениться
        Cursor cabinetCursor = db.getCabinet(cabinetId);
        Log.e(TAG, "cabinetId" + cabinetCursor.getCount());
        cabinetCursor.moveToFirst();

        // получаем множитель  (0.25 <-> 4)
        multiplier = 0.0375F *
                cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER))
                + 0.25F;
        // и отступы
        xAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X));
        yAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y));
        cabinetCursor.close();


        // загружаем парты из бд
        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);
        // и выводим
        out.removeAllViews();
        desksList.clear();
        while (desksCursor.moveToNext()) {
            // достаем данные из бд
            int numberOfPlaces = desksCursor.getInt(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES));
            long deskXDp = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X));
            long deskYDp = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y));

            // создаем view парты
            final RelativeLayout deskLayout = new RelativeLayout(this);
            //выводим парту
            out.addView(deskLayout);

            //добавлем парту и данные в массив (в конструкторе заполняяпозицию и размеры view)
            desksList.add(new DeskUnit(
                    pxFromDp(deskXDp * multiplier) + xAxisPXOffset,
                    pxFromDp(deskYDp * multiplier) + yAxisPXOffset,
                    numberOfPlaces,
                    deskLayout
            ));


            // ---- выводим на парте учеников и их оценки ----

            // получаем места на парте
            Cursor placesCursor = db.getPlacesByDeskId(
                    desksCursor.getLong(desksCursor.getColumnIndex(
                            SchoolContract.TableDesks.KEY_DESK_ID
                    ))
            );
            // пробегаемся по ним
            while (placesCursor.moveToNext()) {

//---контейнер места          placeOut
//-----оценки                 grade1Text grade2Text
//-----контейнер ученика
//-------контейнер картинки
//---------картинка ученика
//---------контейнер текста
//-----------главный текст
//-------имя ученика


                // получаем id ученика, который сидит на этом месте
                long learnerId = db.getLearnerIdByClassIdAndPlaceId(
                        learnersClassId,
                        placesCursor.getLong(placesCursor.getColumnIndex(
                                SchoolContract.TablePlaces.KEY_PLACE_ID
                        ))
                );

                // если ученика нет, то нет и смысла выводить здесь что-либо
                if (learnerId >= 0) {

                    // ищем ученика в массиве с оценками
                    int temp = -1;

                    for (int i = 0; i < learnersAndTheirGrades.length; i++) {
                        if (learnersAndTheirGrades[i].learnerId == learnerId) {
                            temp = i;
                            break;
                        }
                    }
                    final int learnerPosition = temp;

                    desksList.get(desksList.size() - 1).seatingLearnerNumber[
                            (int) (placesCursor.getLong(placesCursor.getColumnIndex(SchoolContract.TablePlaces.COLUMN_ORDINAL)) - 1)
                            ] = learnerPosition;

                    // создаем layout с контейнером ученика и оценками
                    RelativeLayout placeOut = new RelativeLayout(this);
                    // сохраняем контейнер чтобы потом менять ему размеры
                    learnersAndTheirGrades[learnerPosition].viewPlaceOut = placeOut;
                    //ставим размеры
                    RelativeLayout.LayoutParams placeOutParams = new RelativeLayout.LayoutParams(
                            (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier),
                            (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier)
                    );
                    // выравниваем его относительно парты
                    placeOutParams.leftMargin = (int) pxFromDp((NO_ZOOMED_DESK_SIZE
                            * (placesCursor.getLong(placesCursor.getColumnIndex(SchoolContract.TablePlaces.COLUMN_ORDINAL)) - 1)
                            + NO_ZOOMED_LEARNER_BORDER_SIZE) * multiplier);
                    placeOutParams.topMargin = (int) pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * multiplier);
                    placeOutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    placeOutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    deskLayout.addView(placeOut, placeOutParams);


                    // контейнер ученика
                    LinearLayout learnerContainer = new LinearLayout(this);
                    learnerContainer.setOrientation(LinearLayout.VERTICAL);
                    learnerContainer.setWeightSum(4);
                    //параметры контейнера
                    LinearLayout.LayoutParams learnerContainerParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    placeOut.addView(learnerContainer, learnerContainerParams);


                    // контейнер картинки
                    RelativeLayout imageContainer = new RelativeLayout(this);
                    // ставим размеры
                    LinearLayout.LayoutParams imageContainerParams = new LinearLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            0,
                            3
                    );
                    learnerContainer.addView(imageContainer, imageContainerParams);


                    // создаем картинку ученика
                    final ImageView tempLernerImage = new ImageView(this);
                    RelativeLayout.LayoutParams tempLernerImageParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    // сохраняем картинку чтобы потом менять её
                    learnersAndTheirGrades[learnerPosition].viewLearnerImage = tempLernerImage;
                    //добавляем картинку в контейнер
                    imageContainer.addView(tempLernerImage, tempLernerImageParams);

                    //находим последнюю поставленную оценку
                    int currentGrade = learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition];//..

                    // ставим в tempLernerImage изображение по последней оценке(из памяти)
                    switch (currentGrade) {
                        case -2:
                            tempLernerImage.setImageResource(R.drawable.lesson_learner_abs_white);
                            break;
                        case 0:
                            tempLernerImage.setImageResource(R.drawable.lesson_learner_0_gray);
                            break;
                        default:
                            if ((int) (((float) currentGrade / (float) maxAnswersCount) * 100F) <= 20) {
                                //1
                                tempLernerImage.setImageResource(R.drawable.lesson_learner_1);
                            } else if ((int) (((float) currentGrade / (float) maxAnswersCount) * 100F) <= 41) {
                                //2
                                tempLernerImage.setImageResource(R.drawable.lesson_learner_2);
                            } else if ((int) (((float) currentGrade / (float) maxAnswersCount) * 100F) <= 60) {
                                //3
                                tempLernerImage.setImageResource(R.drawable.lesson_learner_3);
                            } else if ((int) (((float) currentGrade / (float) maxAnswersCount) * 100F) <= 80) {
                                //4
                                tempLernerImage.setImageResource(R.drawable.lesson_learner_4);
                            } else if ((int) (((float) currentGrade / (float) maxAnswersCount) * 100F) <= 100) {
                                //5
                                tempLernerImage.setImageResource(R.drawable.lesson_learner_5);
                            }
                    }


                    // выбираем очередность оценок//..

                    int grade1Pos;
                    int grade2Pos;
                    if (learnersAndTheirGrades[learnerPosition].chosenGradePosition == 0) {
                        //g12
                        grade1Pos = 1;
                        grade2Pos = 2;
                    } else if (learnersAndTheirGrades[learnerPosition].chosenGradePosition == 1) {
                        //1g2
                        grade1Pos = 0;
                        grade2Pos = 2;
                    } else {
                        //12g
                        grade1Pos = 0;
                        grade2Pos = 1;
                    }

                    // первый текст с оценкой
                    final TextView grade1Text = new TextView(this);
                    grade1Text.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.geometria));
                    grade1Text.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                    // сохраняем текст чтобы потом менять ему размеры
                    learnersAndTheirGrades[learnerPosition].viewGrade1 = grade1Text;
                    grade1Text.setTextColor(Color.BLACK);
                    grade1Text.setTextSize(13 * multiplier);// 325
                    // если в массивах есть данные об оценках
                    if (learnersAndTheirGrades[learnerPosition].learnerGrades[grade1Pos] > 0) {
                        grade1Text.setText(Integer.toString(learnersAndTheirGrades[learnerPosition].learnerGrades[grade1Pos]));
                    } else
                        grade1Text.setText("");
                    RelativeLayout.LayoutParams grade1TextParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    placeOut.addView(grade1Text, grade1TextParams);

                    // второй текст с оценкой
                    final TextView grade2Text = new TextView(this);
                    grade2Text.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.geometria));
                    grade2Text.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                    // сохраняем текст чтобы потом менять ему размеры
                    learnersAndTheirGrades[learnerPosition].viewGrade2 = grade2Text;
                    grade2Text.setTextColor(Color.BLACK);
                    grade2Text.setTextSize(13 * multiplier);// 325
                    if (learnersAndTheirGrades[learnerPosition].learnerGrades[grade2Pos] > 0) {
                        grade2Text.setText(Integer.toString(learnersAndTheirGrades[learnerPosition].learnerGrades[grade2Pos]));
                    } else
                        grade2Text.setText("");
                    RelativeLayout.LayoutParams grade2TextParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    placeOut.addView(grade2Text, grade2TextParams);

                    // текст главной оценки
                    final TextView bigText = new TextView(this);
                    bigText.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.geometria));
                    bigText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                    // сохраняем текст чтобы потом менять ему размеры
                    learnersAndTheirGrades[learnerPosition].viewMainGradeText = bigText;
                    bigText.setTextColor(Color.BLACK);
                    bigText.setTextSize(14 * multiplier);//340
                    if (currentGrade > 0) {
                        bigText.setText("" + currentGrade);
                    } else
                        bigText.setText("");
                    bigText.setGravity(Gravity.BOTTOM);
                    // параметры текста
                    RelativeLayout.LayoutParams bigTextParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    //bigTextParams.gravity = Gravity.BOTTOM;
                    bigTextParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    bigTextParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    imageContainer.addView(bigText, bigTextParams);


                    // текст ученика
                    TextView learnerNameText = new TextView(this);
                    learnerNameText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria));
                    // сохраняем текст чтобы потом менять ему размеры
                    learnersAndTheirGrades[learnerPosition].viewLearnerNameText = learnerNameText;
                    learnerNameText.setTextSize(8 * multiplier);//200
                    learnerNameText.setSingleLine(true);
                    learnerNameText.setGravity(Gravity.TOP);
                    learnerNameText.setTextColor(Color.BLACK);
                    // выставляем в текстовое поле имя и фамилию
                    if (learnersAndTheirGrades[learnerPosition].name.length() == 0) {
                        learnerNameText.setText(learnersAndTheirGrades[learnerPosition].lastName);

                    } else
                        learnerNameText.setText(learnersAndTheirGrades[learnerPosition].name.charAt(0) + " " +
                                learnersAndTheirGrades[learnerPosition].lastName);
                    //параметры текста
                    LinearLayout.LayoutParams learnerNameTextParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            0,
                            1
                    );
                    learnerContainer.addView(learnerNameText, learnerNameTextParams);

                    // при нажатии на контейнер ученика
                    learnerContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // меняем его оценку

                            //..------_____

                            // если стоит Н, то ничего не делаем
                            if (learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition] != -2) {
                                // если все же стоит какая-то оценка,

                                // смотрим можем ли ее увеличивать
                                if (learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition] != maxAnswersCount) {// если да
                                    // увеличиваем ее на один пункт
                                    learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition]++;
                                } else {
                                    // если увеличить нельзя сбрасываем до минимума
                                    learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition] = 1;
                                }

                                // выводим в текстовое поле
                                learnersAndTheirGrades[learnerPosition].viewMainGradeText.setText(
                                        "" + learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition]//..
                                );

                                // ставим соответствующую картинку
                                if ((int) (((float) learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 20) {
                                    //1
                                    tempLernerImage.setImageResource(R.drawable.lesson_learner_1);
                                } else if ((int) (((float) learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 41) {
                                    //2
                                    tempLernerImage.setImageResource(R.drawable.lesson_learner_2);
                                } else if ((int) (((float) learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 60) {
                                    //3
                                    tempLernerImage.setImageResource(R.drawable.lesson_learner_3);
                                } else if ((int) (((float) learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 80) {
                                    //4
                                    tempLernerImage.setImageResource(R.drawable.lesson_learner_4);
                                } else if ((int) (((float) learnersAndTheirGrades[learnerPosition].learnerGrades[learnersAndTheirGrades[learnerPosition].chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 100) {
                                    //5
                                    tempLernerImage.setImageResource(R.drawable.lesson_learner_5);
                                }
                            }
                        }
                    });

                    // при долгом клике на ученика
                    learnerContainer.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            // выставляем этого ученика как выбранного
                            chosenLearnerPosition = learnerPosition;

                            // вызываем диалог изменения оценок
                            GradeDialogFragment gradeDialog = new GradeDialogFragment();
                            // передаем на вход данные
                            Bundle args = new Bundle();
                            args.putString(GradeDialogFragment.ARGS_LEARNER_NAME,
                                    learnersAndTheirGrades[learnerPosition].lastName + " "
                                            + learnersAndTheirGrades[learnerPosition].name
                            );

                            // переводим названия предметов в строковый массив
                            String[] stringTypes = new String[answersTypes.length];
                            for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                                stringTypes[typeI] = answersTypes[typeI].typeName;
                            }
                            args.putStringArray(GradeDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY,
                                    stringTypes
                            );

                            args.putIntArray(GradeDialogFragment.ARGS_INT_GRADES_ARRAY,
                                    learnersAndTheirGrades[learnerPosition].learnerGrades.clone()
                            );

                            args.putIntArray(GradeDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY,
                                    learnersAndTheirGrades[learnerPosition].learnerGradesTypes.clone()
                            );

                            args.putInt(GradeDialogFragment.ARGS_INT_MAX_GRADE,
                                    maxAnswersCount
                            );

                            args.putInt(GradeDialogFragment.ARGS_INT_CHOSEN_GRADE_POSITION,
                                    learnersAndTheirGrades[learnerPosition].chosenGradePosition//..
                            );

                            gradeDialog.setArguments(args);
                            // показываем диалог
                            gradeDialog.show(getFragmentManager(), "gradeDialog - Hello");


                            return true;
                        }
                    });
                }
            }
            placesCursor.close();
        }
        desksCursor.close();
        db.close();
    }


    // обратная связь от диалога оценок GradeDialogFragment
    @Override
    public void setGrades(int[] grades, int[] chosenTypesNumbers) {
        // ставим выбранной следующую оценку
        if (learnersAndTheirGrades[chosenLearnerPosition].chosenGradePosition != 2) {
            learnersAndTheirGrades[chosenLearnerPosition].chosenGradePosition++;
        } else
            learnersAndTheirGrades[chosenLearnerPosition].chosenGradePosition = 0;

        // передаем измененные массивы в общий список
        learnersAndTheirGrades[chosenLearnerPosition].learnerGrades = grades;
        learnersAndTheirGrades[chosenLearnerPosition].learnerGradesTypes = chosenTypesNumbers;

        // обновляем текст и картинки на ученике
        learnersAndTheirGrades[chosenLearnerPosition].updateViewsData();
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
                            // новые размеры элементам ученика
                            for (int placeI = 0; placeI < desksList.get(i).seatingLearnerNumber.length; placeI++) {
                                if (desksList.get(i).seatingLearnerNumber[placeI] != -1)
                                    learnersAndTheirGrades[desksList.get(i).seatingLearnerNumber[placeI]].setSizes(
                                            multiplier, placeI
                                    );
                            }

                        }
                    }

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

        return true;// todo не везде->?
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //кнопка назад в actionBar
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // показываем диалог подтверждения выхода из активности
        EndLessonDialogFragment endLessonDialogFragment = new EndLessonDialogFragment();
        endLessonDialogFragment.show(getSupportFragmentManager(), "EndLesson - Hello");
    }

    // обратная связь от диалога EndLessonDialogFragment
    @Override
    public void endLesson() {

        //обнуляем данные
        chosenLearnerPosition = -1;
        lessonAttitudeId = -1;
        subjectId = 0;
        learnersClassId = 0;
        cabinetId = 0;
        subjectName = null;
        learnersAndTheirGrades = null;

        // выходим из активности
        finish();
    }

    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }


    // класс для хранения типов ответов
    class AnswersType {
        long id;
        String typeName;

        AnswersType(long id, String typeName) {
            this.id = id;
            this.typeName = typeName;
        }
    }

    // класс для хранения ученика и его оценок
    class MyLearnerAndHisGrades {

        // параметры ученика
        long learnerId;
        String name;
        String lastName;

        // массив оценок
        int[] learnerGrades;
        // массив номеров типов оценок
        int[] learnerGradesTypes;

        // номер выбранной оценки //..
        int chosenGradePosition = 0;

        MyLearnerAndHisGrades(long learnerId, String name, String lastName) {
            this.learnerId = learnerId;
            this.name = name;
            this.lastName = lastName;
            this.learnerGrades = new int[]{0, 0, 0};
            this.learnerGradesTypes = new int[]{0, 0, 0};
        }

        // контейнер места ученика
        RelativeLayout viewPlaceOut;
        // текст имени ученика
        TextView viewLearnerNameText;
        // текст главной оценки
        TextView viewMainGradeText;
        // текст побочной оценки 1
        TextView viewGrade1;
        // текст побочной оценки 2
        TextView viewGrade2;

        // картинка ученика
        ImageView viewLearnerImage;

        void setSizes(float multiplier, int placeNumber) {
            // контейнер места ученика
            ((RelativeLayout.LayoutParams) viewPlaceOut.getLayoutParams()).leftMargin = (int) pxFromDp((NO_ZOOMED_DESK_SIZE
                    * placeNumber
                    + NO_ZOOMED_LEARNER_BORDER_SIZE) * multiplier);

            ((RelativeLayout.LayoutParams) viewPlaceOut.getLayoutParams()).topMargin =
                    (int) pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * multiplier);

            ((RelativeLayout.LayoutParams) viewPlaceOut.getLayoutParams()).width =
                    (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier);

            ((RelativeLayout.LayoutParams) viewPlaceOut.getLayoutParams()).height =
                    (int) pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier);


            // текст побочной оценки 1
            this.viewGrade1.setTextSize(13 * multiplier);


            // текст побочной оценки 2
            this.viewGrade2.setTextSize(13 * multiplier);


            // текст главной оценки
            this.viewMainGradeText.setTextSize(14 * multiplier);


            // текст имени ученика
            this.viewLearnerNameText.setTextSize(8 * multiplier);
        }

        void updateViewsData() {
            //..
            int grade1Pos;
            int grade2Pos;
            if (chosenGradePosition == 0) {
                //g12
                grade1Pos = 1;
                grade2Pos = 2;
            } else if (chosenGradePosition == 1) {
                //1g2
                grade1Pos = 0;
                grade2Pos = 2;
            } else {
                //12g
                grade1Pos = 0;
                grade2Pos = 1;
            }


            // текст главной оценки
            if (learnerGrades[chosenGradePosition] > 0) {
                this.viewMainGradeText.setText("" + learnerGrades[chosenGradePosition]);
            } else
                this.viewMainGradeText.setText("");

            // текст побочной оценки 1
            if (learnerGrades[grade1Pos] > 0) {
                this.viewGrade1.setText("" + learnerGrades[grade1Pos]);
            } else
                this.viewGrade1.setText("");

            // текст побочной оценки 2
            if (learnerGrades[grade2Pos] > 0) {
                this.viewGrade2.setText("" + learnerGrades[grade2Pos]);
            } else
                this.viewGrade2.setText("");

            Log.e(TAG, "--------- " + learnerGrades[chosenGradePosition]);
            // меняем изображение на учненике в соответствии с оценкой
            switch (learnerGrades[chosenGradePosition]) {
                case -2:
                    viewLearnerImage.setImageResource(R.drawable.lesson_learner_abs_white);
                    break;
                case 0:
                    viewLearnerImage.setImageResource(R.drawable.lesson_learner_0_gray);
                    break;
                default:
                    if ((int) (((float) learnerGrades[chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 20) {
                        //1
                        viewLearnerImage.setImageResource(R.drawable.lesson_learner_1);
                    } else if ((int) (((float) learnerGrades[chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 41) {
                        //2
                        viewLearnerImage.setImageResource(R.drawable.lesson_learner_2);
                    } else if ((int) (((float) learnerGrades[chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 60) {
                        //3
                        viewLearnerImage.setImageResource(R.drawable.lesson_learner_3);
                    } else if ((int) (((float) learnerGrades[chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 80) {
                        //4
                        viewLearnerImage.setImageResource(R.drawable.lesson_learner_4);
                    } else if ((int) (((float) learnerGrades[chosenGradePosition] / (float) maxAnswersCount) * 100F) <= 100) {
                        //5
                        viewLearnerImage.setImageResource(R.drawable.lesson_learner_5);
                    }
            }

        }
    }

    // класс содержащий в себе парту
    class DeskUnit {
        int numberOfPlaces;
        int[] seatingLearnerNumber;
        RelativeLayout desk;
        float pxX;
        float pxY;

        DeskUnit(float pxX, float pxY, int numberOfPlaces, RelativeLayout relativeLayout) {
            this.pxX = pxX;
            this.pxY = pxY;
            this.numberOfPlaces = numberOfPlaces;
            this.seatingLearnerNumber = new int[numberOfPlaces];

            // заполняем места на партах пустыми учениками
            for (int learnerI = 0; learnerI < seatingLearnerNumber.length; learnerI++) {
                seatingLearnerNumber[learnerI] = -1;
            }

            // --- получаем графический контейнер ---
            this.desk = relativeLayout;

            // и создаем для него параметры
            RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams(
                    (int) pxFromDp(NO_ZOOMED_DESK_SIZE * numberOfPlaces * multiplier),
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
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.backgroundLiteGray));

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
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.backgroundLiteGray));
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
            rectDrawable.getPaint().setColor(getResources().getColor(R.color.backgroundLiteGray));
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

/*
 * getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
//getSupportActionBar().setHomeAsUpIndicator(R.drawable._button_back_arrow_blue);
 * */