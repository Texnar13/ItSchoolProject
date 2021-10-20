package com.learning.texnar13.teachersprogect.lesson;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SCursor;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * onCreate(),
 * подгружаются все ученики, id кабинета, класса, предмета, зав. урок-время
 * подгружаются массивы с оценками
 *
 * эти поля должны быть статичными, и с проверкой на существование(для переворота)
 * при переходе удалить
 *
 * OnResume(), todo сделать как ActivityResult
 * выводятся парты и ученики
 *
 * */

public class LessonActivity extends AppCompatActivity implements View.OnTouchListener, GradesDialogInterface {


    // ---------- константы для аргументов ----------
    // константа по которой получаеми id зависимости
    public static final String ARGS_LESSON_ATTITUDE_ID = "lessonAttitudeId";
    // константа по которой получаем время урока
    public static final String ARGS_LESSON_DATE = "lessonDate";
    public static final String ARGS_LESSON_NUMBER = "lessonNumber";


    // ---------- константы ----------

    public static final String TAG = "TeachersApp";

    // режимы зума
    private static final int NONE = 0;
    private static final int ZOOM = 2;

    // размер одноместной парты
    private static final int NO_ZOOMED_DESK_SIZE = 40;
    // ширина границы вокруг клетки ученика на парте
    private static final int NO_ZOOMED_LEARNER_BORDER_SIZE = NO_ZOOMED_DESK_SIZE / 20;

    private static final float SMALL_GRADE_SIZE = 7;
    private static final float MEDIUM_GRADE_SIZE = 7;
    private static final float LARGE_GRADE_SIZE = 10;

    private static final float SMALL_GRADE_SIZE_DOUBLE = 4;
    private static final float MEDIUM_GRADE_SIZE_DOUBLE = 7;
    private static final float LARGE_GRADE_SIZE_DOUBLE = 9;


    // ---------- переменные для промежуточных расчётов ----------

    // для перемещения и зума
    // режим: нет, перемещение, zoom
    private int mode = NONE;
    // точка середины между пальцами за предыдущую итерацию
    private Point oldMid = new Point();
    // предыдущее растояние между пальцам
    private float oldDist = 1f;

    // слой с партами
    private RelativeLayout out;

    // ---------- данные не исчезающие при повороте экрана ----------

    // -- параметры из бд --
    // максимальная оценка
    private static int maxAnswersCount;
    // названия типов ответов и их id
    private static AnswersType[] answersTypes;
    // названия типов ответов и их id
    private static AbsentType[] absentTypes;
    // то же самое переведенное в текстовый массив
    private static String[] stringAnswersTypes;
    private static String[] stringAbsTypes;
    private static String[] stringAbsLongTypes;


    // -- текущие данные --
    // растяжение по осям
    private static float multiplier = 0;//0,1;10
    // текущее смещение по осям
    private static float xAxisPXOffset = 0;
    private static float yAxisPXOffset = 0;
    // лист с обьектами парт // todo?(по нему определяется переворот активности)
    private static ArrayList<DeskUnit> desksList;
    // id зависимости урока
    private static long lessonAttitudeId;
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
    // название кабинета
    private static String cabinetName;
    // время урока
    private static String lessonDate;
    private static int lessonNumber;
    // массив учеников
    private static MyLearnerAndHisGrades[] learnersAndTheirGrades;// убрать его и все данные об учениках переместить в парты
    // номер выбранного ученика
    private static int chosenLearnerPosition;


    // подготовка меню
    // раздуваем
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    // назначаем действия
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // закончить урок
        menu.findItem(R.id.lesson_menu_end_lesson).setOnMenuItemClickListener(menuItem -> {

            long[] learnersId = new long[learnersAndTheirGrades.length];

            int[] grades1 = new int[learnersAndTheirGrades.length];
            int[] grades2 = new int[learnersAndTheirGrades.length];
            int[] grades3 = new int[learnersAndTheirGrades.length];

            int[] gradesTypes1 = new int[learnersAndTheirGrades.length];
            int[] gradesTypes2 = new int[learnersAndTheirGrades.length];
            int[] gradesTypes3 = new int[learnersAndTheirGrades.length];

            int[] absentPoz = new int[learnersAndTheirGrades.length];

            String[] learnersNames = new String[learnersAndTheirGrades.length];

            for (int j = 0; j < learnersAndTheirGrades.length; j++) {

                // оценки
                grades1[j] = learnersAndTheirGrades[j].learnerGrades[0];
                grades2[j] = learnersAndTheirGrades[j].learnerGrades[1];
                grades3[j] = learnersAndTheirGrades[j].learnerGrades[2];
                // типы оценок
                gradesTypes1[j] = learnersAndTheirGrades[j].learnerGradesTypes[0];
                gradesTypes2[j] = learnersAndTheirGrades[j].learnerGradesTypes[1];
                gradesTypes3[j] = learnersAndTheirGrades[j].learnerGradesTypes[2];
                // пропуски
                absentPoz[j] = learnersAndTheirGrades[j].absTypePozNumber;
                // ученики
                learnersId[j] = learnersAndTheirGrades[j].learnerId;
                learnersNames[j] = learnersAndTheirGrades[j].lastName + " " + learnersAndTheirGrades[j].name;
            }
            // todo сделать передачу одного лишь id с промежуточным сохраннием. А не всего подряд. а после перейти к todo 3
            // переходим к активности списка оценок
            Intent intent = new Intent(getApplicationContext(), LessonListActivity.class);
            intent.putExtra(LessonListActivity.SUBJECT_ID, subjectId);
            // ученики
            intent.putExtra(LessonListActivity.ARGS_STRING_ARRAY_LEARNERS_NAMES, learnersNames);
            intent.putExtra(LessonListActivity.ARGS_INT_ARRAY_LEARNERS_ID, learnersId);
            // типы оценок
            long[] typesId = new long[answersTypes.length];
            String[] typesNames = new String[answersTypes.length];
            for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                typesId[typeI] = answersTypes[typeI].id;
                typesNames[typeI] = answersTypes[typeI].typeName;
            }
            intent.putExtra(LessonListActivity.STRING_GRADES_TYPES_ARRAY, typesNames);
            intent.putExtra(LessonListActivity.INT_GRADES_TYPES_ID_ARRAY, typesId);
            // типы пропусков
            long[] absId = new long[absentTypes.length];
            String[] absNames = new String[absentTypes.length];
            String[] absLongNames = new String[absentTypes.length];
            for (int typeI = 0; typeI < absentTypes.length; typeI++) {
                absId[typeI] = absentTypes[typeI].id;
                absNames[typeI] = absentTypes[typeI].typeAbsName;
                absLongNames[typeI] = absentTypes[typeI].typeAbsLongName;
            }
            intent.putExtra(LessonListActivity.STRING_ABSENT_TYPES_ARRAY, absNames);
            intent.putExtra(LessonListActivity.STRING_ABSENT_TYPES_LONG_ARRAY, absLongNames);
            intent.putExtra(LessonListActivity.LONG_ABSENT_TYPES_ID_ARRAY, absId);
            // максимальная оценка
            intent.putExtra(LessonListActivity.INT_MAX_GRADE, maxAnswersCount);
            // оценки
            intent.putExtra(LessonListActivity.FIRST_GRADES, grades1);
            intent.putExtra(LessonListActivity.SECOND_GRADES, grades2);
            intent.putExtra(LessonListActivity.THIRD_GRADES, grades3);
            // типы выбранных оценок
            intent.putExtra(LessonListActivity.FIRST_GRADES_TYPES, gradesTypes1);
            intent.putExtra(LessonListActivity.SECOND_GRADES_TYPES, gradesTypes2);
            intent.putExtra(LessonListActivity.THIRD_GRADES_TYPES, gradesTypes3);
            // выбранные пропуски
            intent.putExtra(LessonListActivity.INT_ABSENT_TYPE_POZ_ARRAY, absentPoz);

            // передаем полученную дату урока
            intent.putExtra(LessonListActivity.LESSON_DATE, lessonDate);
            intent.putExtra(LessonListActivity.LESSON_NUMBER, lessonNumber);


            startActivityForResult(intent, 1);

            return true;

        });
        // посадить учеников
        menu.findItem(R.id.lesson_menu_edit_seating).setOnMenuItemClickListener(menuItem -> {
            // намерение перехода на редактор рассадки
            Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
            // кладем id в intent
            intent.putExtra(SeatingRedactorActivity.CLASS_ID, learnersClassId);
            intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
            // переходим
            startActivity(intent);
            return true;
        });
        // расставить парты
        menu.findItem(R.id.lesson_menu_edit_tables).setOnMenuItemClickListener(menuItem -> {
            // намерение перехода на редактор кабинета
            Intent intent = new Intent(getApplicationContext(), CabinetRedactorActivity.class);
            // кладем id в intent
            intent.putExtra(CabinetRedactorActivity.EDITED_CABINET_ID, cabinetId);
            // переходим
            startActivity(intent);
            return true;
        });
        return true;
    }

    // создание экрана
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        // раздуваем layout
        setContentView(R.layout.lesson_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar(findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }
        ((TextView) findViewById(R.id.base_blue_toolbar_title))
                .setText(R.string.title_activity_learners_classes_out);

        // цвета статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        // для того, чтобы векторные изображения созданные в коде отображались нормально
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);


        // получаем зависимость из intent
        lessonAttitudeId = getIntent().getLongExtra(ARGS_LESSON_ATTITUDE_ID, -1);
        // -1 он будет равен только при ошибке
        if (lessonAttitudeId == -1) {
            finish();
            return;
        }

        // (проверяем по одному из полей) был создан новый экран или он просто переворачивался
        if (savedInstanceState == null) {
            // получаем данные из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            initData(db);
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
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(shortSubjectName + ", " + shortClassName + ", " + shortCabinetName);
    }

    // начальная подгрузка данных из бд
    void initData(DataBaseOpenHelper db) {

        // получаем переданную дату урока
        lessonDate = getIntent().getStringExtra(ARGS_LESSON_DATE);
        lessonNumber = getIntent().getIntExtra(ARGS_LESSON_NUMBER, 0);
        if (lessonDate.equals("")) {
            finish();
        }

        // создаем список парт
        desksList = new ArrayList<>();

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
        Cursor learnersClass = db.getLearnersClases(learnersClassId);
        learnersClass.moveToFirst();
        className = learnersClass.getString(learnersClass.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
        learnersClass.close();

        // получаем имя кабинета
        Cursor cabinetNameCursor = db.getCabinet(cabinetId);
        cabinetNameCursor.moveToFirst();
        cabinetName = cabinetNameCursor.getString(cabinetNameCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
        cabinetNameCursor.close();


        //максимальная оценка
        maxAnswersCount = db.getSettingsMaxGrade(1);


        // названия типов ответов
        Cursor typesCursor = db.getGradesTypes();
        answersTypes = new AnswersType[typesCursor.getCount()];
        // строковый эквивалент для передачи в диалоги
        stringAnswersTypes = new String[answersTypes.length];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < answersTypes.length; typeI++) {
            typesCursor.moveToNext();
            // добавляем новый тип во внутренний список
            answersTypes[typeI] = new AnswersType(
                    typesCursor.getLong(typesCursor.getColumnIndex(SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID)),
                    typesCursor.getString(typesCursor.getColumnIndex(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE))
            );
            stringAnswersTypes[typeI] = answersTypes[typeI].typeName;
        }
        typesCursor.close();


        // названия типов пропусков
        Cursor typesAbsCursor = db.getAbsentTypes();
        absentTypes = new AbsentType[typesAbsCursor.getCount()];
        // строковые эквиваленты для передачи в диалоги
        stringAbsTypes = new String[absentTypes.length];
        stringAbsLongTypes = new String[absentTypes.length];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < absentTypes.length; typeI++) {
            typesAbsCursor.moveToNext();
            // добавляем новый тип во внутренний список
            absentTypes[typeI] = new AbsentType(
                    typesAbsCursor.getLong(typesAbsCursor.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID)),
                    typesAbsCursor.getString(typesAbsCursor.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME)),
                    typesAbsCursor.getString(typesAbsCursor.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME))
            );
            stringAbsTypes[typeI] = absentTypes[typeI].typeAbsName;
            stringAbsLongTypes[typeI] = absentTypes[typeI].typeAbsLongName;
        }
        typesAbsCursor.close();

        // получаем учеников по id класса
        Cursor learnersCursor = db.getLearnersByClassId(learnersClassId);

        // инициализируем массив с учениками
        learnersAndTheirGrades = new MyLearnerAndHisGrades[learnersCursor.getCount()];
        //заполняем его
        for (int i = 0; i < learnersAndTheirGrades.length; i++) {
            learnersCursor.moveToPosition(i);

            long learnerId = learnersCursor.getLong(learnersCursor.getColumnIndex(
                    SchoolContract.TableLearners.KEY_LEARNER_ID
            ));

            // создаем нового ученика
            learnersAndTheirGrades[i] = new MyLearnerAndHisGrades(
                    learnerId,
                    learnersCursor.getString(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_FIRST_NAME
                    )),
                    learnersCursor.getString(learnersCursor.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_SECOND_NAME
                    ))
            );

            // получаем оценки ученика за этот урок
            Cursor grades = db.getGradesByLearnerIdSubjectDateAndLesson(learnerId, subjectId, lessonDate, lessonNumber);

            if (grades.moveToNext()) {// если оценки за этот урок уже проставлялись
                int absId;
                if (grades.isNull(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID))) {
                    absId = -1;
                } else {
                    absId = grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID));
                }
                if (absId != -1) {// если стоит пропуск
                    // проходимся в цикле по всем типам оценок запоминая номер попавшегося
                    learnersAndTheirGrades[i].absTypePozNumber = -1;
                    int poz = 0;
                    while (absentTypes.length > poz) {
                        if (absentTypes[poz].id != absId) {
                            poz++;
                        } else {
                            learnersAndTheirGrades[i].absTypePozNumber = poz;
                            break;
                        }
                    }
                    learnersAndTheirGrades[i].learnerGrades[0] = 0;
                    learnersAndTheirGrades[i].learnerGrades[1] = 0;
                    learnersAndTheirGrades[i].learnerGrades[2] = 0;
                    learnersAndTheirGrades[i].learnerGradesTypes[0] = 1;
                    learnersAndTheirGrades[i].learnerGradesTypes[1] = 1;
                    learnersAndTheirGrades[i].learnerGradesTypes[2] = 1;
                } else {// если пропуска нет
                    for (int gradeI = 0; gradeI < SchoolContract.TableLearnersGrades.COLUMNS_GRADE.length; gradeI++) {
                        // оценка
                        learnersAndTheirGrades[i].learnerGrades[gradeI] =
                                grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[gradeI]));
                        // тип оценки
                        long typeId = grades.getLong(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[gradeI]));
                        // проходимся в цикле по всем типам оценок запоминая номер попавшегося
                        learnersAndTheirGrades[i].learnerGradesTypes[gradeI] = -1;// единица всегда должна перекрываться другим значение иначе будет ошибка
                        int poz = 0;
                        while (answersTypes.length > poz) {
                            if (answersTypes[poz].id != typeId) {
                                poz++;
                            } else {
                                learnersAndTheirGrades[i].learnerGradesTypes[gradeI] = poz;
                                break;
                            }
                        }
                    }
                }
            }// если не проставлялись оставляем пустые значения
            grades.close();
        }
        learnersCursor.close();

        // номер выбранного ученика
        chosenLearnerPosition = -1;
    }


    // при каждом показе экрана или если экран перекрыли, а потом показали еще раз
    @Override
    protected void onResume() {
        super.onResume();

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // обновляем трансформацию (размеры и отступы) кабинета
        checkCabinetSizesFromDB(db);
        // обновляем список парт и рассадку учеников
        checkDesksAndAndPlacesFromDB(db);
        db.close();

        // и выводим все
        outAll();

        Log.e(TAG, "onResume: " + learnersAndTheirGrades.length);

    }


    // обновляем трансформацию (размеры и отступы) кабинета
    void checkCabinetSizesFromDB(DataBaseOpenHelper db) {
        Cursor cabinetCursor = db.getCabinet(cabinetId);
        cabinetCursor.moveToFirst();
        // получаем множитель  (0.25 <-> 4)
        multiplier = 0.0375F * cabinetCursor.getLong(cabinetCursor.getColumnIndex(
                SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER)) + 0.25F;
        // и отступы
        xAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X));
        yAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y));
        cabinetCursor.close();
    }


    /**
     * получаем данные о партах, а также зависимости ученик-место
     * на этих партах связывая учеников с их местом за партой.
     * Также инициализируем все view (только инициализируем, без размеров и фонов)
     */
    void checkDesksAndAndPlacesFromDB(DataBaseOpenHelper db) {

        // чистим список парт
        desksList.clear();
        // чистим view
        out.removeAllViews();

        // загружаем парты из бд
        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);
        while (desksCursor.moveToNext()) {

            // данные одной парты
            long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID));
            int numberOfPlaces = desksCursor.getInt(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES));
            long deskXDp = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X));
            long deskYDp = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y));

            // создаем новую парту  парту и данные в массив (в конструкторе заполняя позицию и размеры view)
            DeskUnit currentDeskUnit = new DeskUnit(
                    pxFromDp(deskXDp * multiplier) + xAxisPXOffset,
                    pxFromDp(deskYDp * multiplier) + yAxisPXOffset,
                    numberOfPlaces,
                    new RelativeLayout(this)// создаем view парты
            );
            desksList.add(currentDeskUnit);
            // выводим парту
            out.addView(currentDeskUnit.desk);


            // получаем места на парте
            SCursor placesCursor = new SCursor(db.getPlacesByDeskId(deskId));
            while (placesCursor.moveToNext()) {

                // получаем информацию об этом месте
                // id места
                long placeId = placesCursor.getLong(SchoolContract.TablePlaces.KEY_PLACE_ID);

                // получаем номер ученика (который сидит на этом месте) в массиве с учениками и их оценками
                int learnerArrPos = -1;
                {
                    // все записи с таким местом
                    SCursor placesAttitudes = new SCursor(db.getAttitudesByPlaceId(placeId));
                    while (placesAttitudes.moveToNext() && learnerArrPos == -1) {
                        // id ученика связанного с этим местом
                        long learnerId = placesAttitudes
                                .getLong(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID);
                        // есть ли такой id в этом классе
                        for (int learnerPoz = 0; learnerPoz < learnersAndTheirGrades.length && learnerArrPos == -1; learnerPoz++)
                            if (learnersAndTheirGrades[learnerPoz].learnerId == learnerId)
                                learnerArrPos = learnerPoz;
                    }
                    placesAttitudes.close();

                    // позиция ученика за партой
                    int learnerOrdinal = (int) (placesCursor.getLong(SchoolContract.TablePlaces.COLUMN_ORDINAL));
                    // помещаем в текущую парту номер связанного ученика на позицию на которой он сидит
                    currentDeskUnit.seatingLearnerNumber[learnerOrdinal - 1] = learnerArrPos;
                }

                // если нашли ученика, получаем его данные и ссылки на view места
                if (learnerArrPos != -1) {
                    // инициализируем разметку места на парте
                    View placeRoot = getLayoutInflater().inflate(
                            R.layout.lesson_desk_place_element, null);
                    currentDeskUnit.desk.addView(placeRoot);

                    // сохраняем ссылки на все view
                    learnersAndTheirGrades[learnerArrPos].setViews(
                            // контейнер места на парте
                            placeRoot.findViewById(R.id.lesson_desk_place_element_place_out),
                            // имя ученика
                            placeRoot.findViewById(R.id.lesson_desk_place_element_learner_text),
                            // главная оценка
                            placeRoot.findViewById(R.id.lesson_desk_place_element_center_grade),
                            // оценка слева сверху
                            placeRoot.findViewById(R.id.lesson_desk_place_element_left_grade),
                            // оценка справа сверху
                            placeRoot.findViewById(R.id.lesson_desk_place_element_right_grade),
                            // иконка ученика
                            placeRoot.findViewById(R.id.lesson_desk_place_element_learner_background)
                    );

                    // при нажатии на контейнер ученика
                    MyLearnerAndHisGrades pressedLearner = learnersAndTheirGrades[learnerArrPos];
                    pressedLearner.viewPlaceOut.setOnClickListener(view -> {
                        // меняем его оценку
                        pressedLearner.tapOnLearner();
                    });

                    // при долгом клике на ученика
                    final int finalLearnerArrPos = learnerArrPos;
                    pressedLearner.viewPlaceOut.setOnLongClickListener(view -> {

                        // выставляем этого ученика как выбранного
                        chosenLearnerPosition = finalLearnerArrPos;

                        // вызываем диалог изменения оценок
                        GradeEditLessonDialogFragment gradeDialog = new GradeEditLessonDialogFragment();
                        // передаем на вход данные
                        Bundle args = new Bundle();
                        args.putString(GradeEditLessonDialogFragment.ARGS_LEARNER_NAME,
                                pressedLearner.lastName + " " + pressedLearner.name);
                        args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY,
                                stringAnswersTypes);
                        args.putIntArray(GradeEditLessonDialogFragment.ARGS_INT_GRADES_ARRAY,
                                pressedLearner.learnerGrades.clone());
                        args.putIntArray(GradeEditLessonDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY,
                                pressedLearner.learnerGradesTypes.clone());
                        args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_ABSENT_TYPES_NAMES_ARRAY,
                                stringAbsTypes);
                        args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY,
                                stringAbsLongTypes);
                        args.putInt(GradeEditLessonDialogFragment.ARGS_INT_GRADES_ABSENT_TYPE_NUMBER,
                                pressedLearner.absTypePozNumber);
                        args.putInt(GradeEditLessonDialogFragment.ARGS_INT_MAX_GRADE,
                                maxAnswersCount);
                        args.putInt(GradeEditLessonDialogFragment.ARGS_INT_CHOSEN_GRADE_POSITION,
                                pressedLearner.chosenGradePosition);
                        gradeDialog.setArguments(args);
                        // показываем диалог
                        gradeDialog.show(getFragmentManager(), "gradeDialog - Hello");
                        return true;
                    });
                }
            }
            placesCursor.close();
        }
        desksCursor.close();
        db.close();
    }

    // обновляем размеры и содержимое всего кабинета
    @SuppressLint("ResourceType")
    void outAll() {
        // парты (сами view парт инициализируются при создании обьеекта парта)
        for (DeskUnit currentDesk : desksList) {
            // по местам за партой
            for (int placeI = 0; placeI < currentDesk.numberOfPlaces; placeI++) {
                // если на этом месте сидит ученик
                int learnerArrPos = currentDesk.seatingLearnerNumber[placeI];
                if (learnerArrPos != -1) {
                    // контейнер места на парте, ставим размеры
                    learnersAndTheirGrades[learnerArrPos].updateSizePlaceView(multiplier, placeI);
                    // иконка ученика
                    learnersAndTheirGrades[learnerArrPos].updateLearnerBackground();
                    // имя ученика
                    learnersAndTheirGrades[learnerArrPos].updateLearnerNameText();
                    learnersAndTheirGrades[learnerArrPos].updateSizeLearnerNameView(multiplier);
                    // обновляем размеры и содержимое оценок
                    learnersAndTheirGrades[learnerArrPos].updateGradesTexts();
                    learnersAndTheirGrades[learnerArrPos].updateSizesGradesViews(multiplier);

                }
            }
        }
    }

    // обратная связь от диалога оценок GradeDialogFragment
    @Override
    public void setGrades(int[] grades, int[] chosenTypesNumbers, int chosenAbsPoz) {
        if (chosenLearnerPosition != -1) {
            MyLearnerAndHisGrades chosenOne = learnersAndTheirGrades[chosenLearnerPosition];
            // ставим выбранной следующую оценку
            chosenOne.chosenGradePosition = (chosenOne.chosenGradePosition + 1) % 3;

            // передаем измененные массивы в общий список
            chosenOne.learnerGrades = grades;
            chosenOne.learnerGradesTypes = chosenTypesNumbers;

            // тип пропуска
            chosenOne.absTypePozNumber = chosenAbsPoz;

            // обновляем текст и картинки на ученике
            chosenOne.updateGradesTexts();
            chosenOne.updateSizesGradesViews(multiplier);
        }
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
        //   если есть зум прекращаем его, не начиная касания
        //  =none=
        //последнее отпускание
        //  -если есть перемещение завершаем его-
        //  =none=

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // поставили первый палец
                break;
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
                        for (DeskUnit deskUnit : desksList) {
                            // новые координаты и размеры
                            deskUnit.setDeskParams(
                                    // трансформация координаты относительно центра пальцев
                                    nowMid.x - ((scale * (nowMid.x - deskUnit.pxX))) / 100,
                                    nowMid.y - ((scale * (nowMid.y - deskUnit.pxY))) / 100,
                                    // трансформация размера за счет мультипликатора
                                    pxFromDp(NO_ZOOMED_DESK_SIZE * deskUnit.numberOfPlaces * multiplier),
                                    pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
                            );
                            // новые размеры элементам ученика
                            for (int placeI = 0; placeI < deskUnit.seatingLearnerNumber.length; placeI++) {
                                if (deskUnit.seatingLearnerNumber[placeI] != -1) {
                                    learnersAndTheirGrades[deskUnit.seatingLearnerNumber[placeI]]
                                            .updateSizesForZoom(multiplier, placeI);
                                    Log.e(TAG, "onTouch: " + deskUnit.seatingLearnerNumber[placeI]);
                                    Log.e(TAG, "onTouch: " + learnersAndTheirGrades[deskUnit.seatingLearnerNumber[placeI]].leftGrade);
                                }
                            }

                        }
                    }

                    // -------- перемещение центра пальцев --------
                    // пробегаемся по партам
                    for (DeskUnit deskUnit : desksList) {
                        // обновляем координаты изменяя только положение парт
                        deskUnit.setDeskPosition(
                                deskUnit.pxX + nowMid.x - oldMid.x,
                                deskUnit.pxY + nowMid.y - oldMid.y
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


    // обратная связь
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (LessonListActivity.RESULT_SAVE == resultCode) {
            //обнуляем данные
            chosenLearnerPosition = -1;
            lessonAttitudeId = -1;
            subjectId = 0;
            learnersClassId = 0;
            cabinetId = 0;
            subjectName = null;
            learnersAndTheirGrades = null;

//            // выводим рекламму
//            if (lessonEndBanner.isLoaded()) {
//                lessonEndBanner.show();
//            }
            finish();
        }
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

        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
        // todo перенести все в клас бд

        // удаляем все предыдущие оценки в этом дне
        Cursor deleteGrades = db.getGradesBySubjectDateAndLesson(
                subjectId,
                lessonDate,
                lessonNumber
        );
        while (deleteGrades.moveToNext())
            db.removeGrade(deleteGrades.getLong(deleteGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)));
        deleteGrades.close();

        // сохраняем оценки в бд
        for (MyLearnerAndHisGrades currentLearner : learnersAndTheirGrades) {

            if (currentLearner.absTypePozNumber != -1) {// пропуск
                db.createGrade(
                        currentLearner.learnerId,
                        0, 0, 0,
                        1, 1, 1,
                        absentTypes[currentLearner.absTypePozNumber].id,
                        subjectId,
                        lessonDate,
                        lessonNumber
                );
            } else if (currentLearner.learnerGrades[0] != 0 || currentLearner.learnerGrades[1] != 0 || currentLearner.learnerGrades[2] != 0) {
                db.createGrade(
                        currentLearner.learnerId,
                        currentLearner.learnerGrades[0], currentLearner.learnerGrades[1], currentLearner.learnerGrades[2],
                        answersTypes[currentLearner.learnerGradesTypes[0]].id, answersTypes[currentLearner.learnerGradesTypes[1]].id, answersTypes[currentLearner.learnerGradesTypes[2]].id,
                        -1,
                        subjectId,
                        lessonDate,
                        lessonNumber
                );
            }

        }
        db.close();


        // выходим из активности
        super.onBackPressed();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // обнуляем данные это вроде не нужно
//        chosenLearnerPosition = -1;
//        lessonAttitudeId = -1;
//        subjectId = 0;
//        learnersClassId = 0;
//        cabinetId = 0;
//        subjectName = null;
//        learnersAndTheirGrades = null;
//        maxAnswersCount = -1;
//        answersTypes = null;
//        absentTypes = null;
//        stringAnswersTypes = null;
//        stringAbsTypes = null;
//        stringAbsLongTypes = null;
//        multiplier = 0;
//        xAxisPXOffset = 0;
//        yAxisPXOffset = 0;
//        desksList = null;
//        learnersClassId = -1;
//        className = null;
//        subjectId = -1;
//        cabinetId = -1;
//        cabinetName = null;
//        lessonDate = null;
//        lessonNumber = -1;
//    }

    private int pxFromDp(float dp) {
        return (int) (dp * getApplicationContext().getResources().getDisplayMetrics().density);
    }


    // класс для хранения ученика и его оценок// todo static
    private class MyLearnerAndHisGrades {

        // параметры ученика
        long learnerId;
        String name;
        String lastName;// todo 3 помещать сюда форматированное имя сразу при создании, а не использовать для этого два поля

        // массив оценок
        int[] learnerGrades;
        // массив номеров типов оценок
        int[] learnerGradesTypes;
        // тип пропуска
        int absTypePozNumber;

        // номер выбранной оценки //..
        int chosenGradePosition = 0;

        MyLearnerAndHisGrades(long learnerId, String name, String lastName) {
            this.learnerId = learnerId;
            this.name = name;
            this.lastName = lastName;
            this.learnerGrades = new int[]{0, 0, 0};
            this.learnerGradesTypes = new int[]{0, 0, 0};
            this.absTypePozNumber = -1;
        }

        // контейнер места ученика
        RelativeLayout viewPlaceOut;
        // текст имени ученика
        TextView viewLearnerNameText;
        // текст главной оценки
        TextView centerGrade;
        // текст побочной оценки 1
        TextView leftGrade;
        // текст побочной оценки 2
        TextView rightGrade;
        // картинка ученика
        ImageView viewLearnerImage;

        void setViews(RelativeLayout viewPlaceOut, TextView viewLearnerNameText,
                      TextView viewMainGradeText, TextView viewGrade1,
                      TextView viewGrade2, ImageView viewLearnerImage) {
            // контейнер места ученика
            this.viewPlaceOut = viewPlaceOut;
            // текст имени ученика
            this.viewLearnerNameText = viewLearnerNameText;
            // текст главной оценки
            this.centerGrade = viewMainGradeText;
            // текст побочной оценки 1
            this.leftGrade = viewGrade1;
            // текст побочной оценки 2
            this.rightGrade = viewGrade2;
            // картинка ученика
            this.viewLearnerImage = viewLearnerImage;
        }


        // увеличение оценки этого ученика
        void tapOnLearner() {
            // если стоит Н, то ничего не делаем
            if (absTypePozNumber == -1) {
                // если все же стоит какая-то оценка,

                // смотрим можем ли ее увеличивать
                if (learnerGrades[chosenGradePosition] != maxAnswersCount) {// если да
                    // увеличиваем ее на один пункт
                    learnerGrades[chosenGradePosition]++;
                } else// если увеличить нельзя сбрасываем до минимума
                    learnerGrades[chosenGradePosition] = 1;
                // выводим ее в текстовое поле
                centerGrade.setText("" + learnerGrades[chosenGradePosition]);

                // обновляем размер текста главной оценки
                if (learnerGrades[getLeftGradeArrayPos()] == 0 && learnerGrades[getRightGradeArrayPos()] == 0) {
                    centerGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                            ((learnerGrades[chosenGradePosition] < 10) ?
                                    (LARGE_GRADE_SIZE) : (LARGE_GRADE_SIZE_DOUBLE)) * multiplier);
                } else
                    centerGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                            ((learnerGrades[chosenGradePosition] < 10) ?
                                    (MEDIUM_GRADE_SIZE) : (MEDIUM_GRADE_SIZE_DOUBLE)) * multiplier);

                // ставим соответствующую картинку
                updateLearnerBackground();
            }
        }


        // ---- обновление размеров контейнеров и текста ----

        // обновление размеров контейнеров и текста для зума
        void updateSizesForZoom(float multiplier, int placeDeskPosition) {
            updateSizePlaceView(multiplier, placeDeskPosition);
            updateSizesGradesViews(multiplier);
            updateSizeLearnerNameView(multiplier);
        }

        // обновление размеров контейнера места
        void updateSizePlaceView(float multiplier, int placeDeskPosition) {
            // контейнер места ученика
            RelativeLayout.LayoutParams viewPlaceOutParams = (RelativeLayout.LayoutParams) viewPlaceOut.getLayoutParams();
            viewPlaceOutParams.leftMargin = pxFromDp((NO_ZOOMED_DESK_SIZE
                    * placeDeskPosition + NO_ZOOMED_LEARNER_BORDER_SIZE) * multiplier);
            viewPlaceOutParams.topMargin =
                    pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * multiplier);
            viewPlaceOutParams.width =
                    pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier);
            viewPlaceOutParams.height = viewPlaceOutParams.width;
        }

        // обновление размеров текста оценок
        void updateSizesGradesViews(float multiplier) {
            // текст побочной оценки 1
            leftGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                    ((learnerGrades[getLeftGradeArrayPos()] < 10) ?
                            (SMALL_GRADE_SIZE) : (SMALL_GRADE_SIZE_DOUBLE)) * multiplier);
            ((RelativeLayout.LayoutParams) leftGrade.getLayoutParams()).leftMargin = (int) (10 * multiplier);

            // текст побочной оценки 2
            rightGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                    ((learnerGrades[getRightGradeArrayPos()] < 10) ?
                            (SMALL_GRADE_SIZE) : (SMALL_GRADE_SIZE_DOUBLE)) * multiplier);
            ((RelativeLayout.LayoutParams) rightGrade.getLayoutParams()).rightMargin = (int) (10 * multiplier);

            // текст главной оценки
            if (learnerGrades[getLeftGradeArrayPos()] == 0 && learnerGrades[getRightGradeArrayPos()] == 0) {
                centerGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                        ((learnerGrades[chosenGradePosition] < 10) ?
                                (LARGE_GRADE_SIZE) : (LARGE_GRADE_SIZE_DOUBLE)) * multiplier);
            } else
                centerGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                        ((learnerGrades[chosenGradePosition] < 10) ?
                                (MEDIUM_GRADE_SIZE) : (MEDIUM_GRADE_SIZE_DOUBLE)) * multiplier);
        }

        // выставляем в текстовое поле имя ученика
        void updateSizeLearnerNameView(float multiplier) {
            viewLearnerNameText.setTextSize(7 * multiplier);
        }


        // ---- выставляем данные из полей во view ----
        void updateGradesTexts() {

            // текст главной оценки
            setViewGradeText(centerGrade, learnerGrades[chosenGradePosition]);
            // текст побочной оценки 1
            setViewGradeText(leftGrade, learnerGrades[(chosenGradePosition == 0) ? (1) : (0)]);
            // текст побочной оценки 2
            setViewGradeText(rightGrade, learnerGrades[(chosenGradePosition == 2) ? (1) : (2)]);

            // меняем изображение на учненике в соответствии с оценкой
            updateLearnerBackground();
        }

        // выставляем в текстовое поле имя ученика
        void updateLearnerNameText() {
            viewLearnerNameText.setText((name.length() == 0) ? (lastName) : (name.charAt(0) + " " + lastName));
        }

        // ставим в tempLernerImage изображение по оценке
        void updateLearnerBackground() {
            // находим последнюю поставленную оценку
            int currentGrade = learnerGrades[chosenGradePosition];
            ImageView learnerBackground = viewLearnerImage;

            if (absTypePozNumber != -1) {// пропуск
                learnerBackground.setImageResource(R.drawable.lesson_activity_learner_icon_abs);
            } else if (currentGrade == 0) {
                learnerBackground.setImageResource(R.drawable.lesson_activity_learner_icon_gray_0);
            } else if (((float) currentGrade / maxAnswersCount) <= 0.2F) {
                //1
                learnerBackground.setImageResource(R.drawable.lesson_activity_learner_icon_1);
            } else if (((float) currentGrade / maxAnswersCount) <= 0.41F) {
                //2
                learnerBackground.setImageResource(R.drawable.lesson_activity_learner_icon_2);
            } else if (((float) currentGrade / maxAnswersCount) <= 0.60F) {
                //3
                learnerBackground.setImageResource(R.drawable.lesson_activity_learner_icon_3);
            } else if (((float) currentGrade / maxAnswersCount) <= 0.80F) {
                //4
                learnerBackground.setImageResource(R.drawable.lesson_activity_learner_icon_4);
            } else if (((float) currentGrade / maxAnswersCount) <= 1F) {
                //5
                learnerBackground.setImageResource(R.drawable.lesson_activity_learner_icon_5);
            }
        }

        // ---- внутренние вспомогательные методы ----

        // выставление текста в оценку
        private void setViewGradeText(TextView gradeView, int grade) {
            gradeView.setText((grade > 0) ? ("" + grade) : (""));
        }

        // позиции оценок в массиве в зависимости от главной оценки
        private int getLeftGradeArrayPos() {
            return (chosenGradePosition == 0) ? (1) : (0);
        }

        private int getRightGradeArrayPos() {
            return (chosenGradePosition == 2) ? (1) : (2);
        }

    }

    // класс для хранения типов ответов
    private static class AnswersType {
        long id;
        String typeName;

        AnswersType(long id, String typeName) {
            this.id = id;
            this.typeName = typeName;
        }
    }

    // класс для хранения типов пропусков
    private static class AbsentType {
        long id;
        String typeAbsName;
        String typeAbsLongName;

        AbsentType(long id, String typeAbsName, String typeAbsLongName) {
            this.id = id;
            this.typeAbsName = typeAbsName;
            this.typeAbsLongName = typeAbsLongName;
        }
    }

    // класс содержащий в себе парту // todo static
    private class DeskUnit {
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
            Arrays.fill(seatingLearnerNumber, -1);

            // --- получаем графический контейнер ---
            this.desk = relativeLayout;

            // и создаем для него параметры
            RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams(
                    pxFromDp(NO_ZOOMED_DESK_SIZE * numberOfPlaces * multiplier),
                    pxFromDp(NO_ZOOMED_DESK_SIZE * multiplier)
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