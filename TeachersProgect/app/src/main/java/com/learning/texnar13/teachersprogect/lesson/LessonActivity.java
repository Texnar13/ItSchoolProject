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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
todo переделать описание?
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
 *
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
    static final int NO_ZOOMED_DESK_SIZE = 40;


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
    private static GraduationSettings graduationSettings;

    // данные об уроке
    private static LessonBaseData lessonBaseData;
    // массив учеников
    private static MyLearnerAndHisGrades[] learnersAndTheirGrades;
    // номер выбранного ученика
    private static int chosenLearnerPosition;

    // лист с обьектами парт
    private static ArrayList<DeskUnit> desksList;

    // растяжение по осям
    private static float multiplier = 0;//0,1;10
    // текущее смещение по осям
    private static float xAxisPXOffset = 0;
    private static float yAxisPXOffset = 0;


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
        // посмотреть оценки списком
        menu.findItem(R.id.lesson_menu_end_lesson).setOnMenuItemClickListener(menuItem -> {

            // переходим к активности списка оценок
            Intent intent = new Intent(getApplicationContext(), LessonListActivity.class);
            // передаем координаты этого урока
            intent.putExtra(ARGS_LESSON_ATTITUDE_ID, lessonBaseData.lessonAttitudeId);
            intent.putExtra(ARGS_LESSON_DATE, lessonBaseData.lessonDate);
            intent.putExtra(ARGS_LESSON_NUMBER, lessonBaseData.lessonNumber);
            // запускаем активность
            startActivityForResult(intent, 1);

            return true;
        });
        // посадить учеников
        menu.findItem(R.id.lesson_menu_edit_seating).setOnMenuItemClickListener(menuItem -> {
            // намерение перехода на редактор рассадки
            Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
            // кладем id в intent
            intent.putExtra(SeatingRedactorActivity.CLASS_ID, lessonBaseData.learnersClassId);
            intent.putExtra(SeatingRedactorActivity.CABINET_ID, lessonBaseData.cabinetId);
            // переходим
            startActivity(intent);
            return true;
        });
        // расставить парты
        menu.findItem(R.id.lesson_menu_edit_tables).setOnMenuItemClickListener(menuItem -> {
            // намерение перехода на редактор кабинета
            Intent intent = new Intent(getApplicationContext(), CabinetRedactorActivity.class);
            // кладем id в intent
            intent.putExtra(CabinetRedactorActivity.EDITED_CABINET_ID, lessonBaseData.cabinetId);
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
        // разметка
        {
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
        }


        // проверяем был создан новый экран или он просто переворачивался
        if (savedInstanceState == null) {
            // todo написать ошибку и проверить все моменты с подгрузкой данных при повороте

            // получаем зависимость и дату урока из intent
            long lessonAttitudeId = getIntent().getLongExtra(ARGS_LESSON_ATTITUDE_ID, -1);
            String lessonDate = getIntent().getStringExtra(ARGS_LESSON_DATE);
            if (lessonAttitudeId == -1 || lessonDate.equals("")) {// -1 он будет равен только при ошибке
                finish();
                return;
            }
            // получаем начальные данные об уроке из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            lessonBaseData = LessonBaseData.newInstance(
                    db,
                    lessonAttitudeId,
                    lessonDate,
                    getIntent().getIntExtra(ARGS_LESSON_NUMBER, 0)
            );

            // подгружаем все остальное
            initData(db);
            db.close();
        }


        // слой с партами
        out = findViewById(R.id.activity_lesson_room_layout);
        out.setOnTouchListener(this);


        // выставляем название предмета и класса в заголовок
        // укорачиваем поля если они слишком длинные …
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(// abcde -> abc…  abcd->abcd
                ((lessonBaseData.subjectName.length() > 10) ? (lessonBaseData.subjectName.substring(0, 9) + "…") : (lessonBaseData.subjectName)) + ", " +
                        ((lessonBaseData.className.length() > 4) ? (lessonBaseData.className.substring(0, 3) + "…") : (lessonBaseData.className)) + ", " +
                        ((lessonBaseData.cabinetName.length() > 4) ? (lessonBaseData.cabinetName.substring(0, 3) + "…") : (lessonBaseData.cabinetName))
        );
    }

    // начальная подгрузка данных из бд
    void initData(DataBaseOpenHelper db) {

        // ------ получаем не меняющиеся настроки для всех классов ------
        graduationSettings = GraduationSettings.newInstance(db);

        // создаем список парт
        desksList = new ArrayList<>();

        // получаем учеников
        getLearnersFromDB(db);

        // получаем их ооценки
        getGradesFromDB(db);
    }

    // получаем учеников с пустыми оценками
    void getLearnersFromDB(DataBaseOpenHelper db) {
        // получаем учеников по id класса
        Cursor learnersCursor = db.getLearnersByClassId(lessonBaseData.learnersClassId);

        // инициализируем массив с учениками
        learnersAndTheirGrades = new MyLearnerAndHisGrades[learnersCursor.getCount()];
        // заполняем его
        for (int i = 0; i < learnersAndTheirGrades.length; i++) {
            learnersCursor.moveToPosition(i);

            long learnerId = learnersCursor.getLong(learnersCursor.getColumnIndex(
                    SchoolContract.TableLearners.KEY_ROW_ID
            ));

            // создаем нового ученика с пустыми оценками
            String firstName = learnersCursor.getString(learnersCursor.getColumnIndex(
                    SchoolContract.TableLearners.COLUMN_FIRST_NAME));
            String secondName = learnersCursor.getString(learnersCursor.getColumnIndex(
                    SchoolContract.TableLearners.COLUMN_SECOND_NAME));

            LessonListActivity.LessonListLearnerAndGradesData.GradeUnit[] emptyGrades =
                    new LessonListActivity.LessonListLearnerAndGradesData.GradeUnit[3];
            for (int gradeI = 0; gradeI < 3; gradeI++)
                emptyGrades[gradeI] = new LessonListActivity.LessonListLearnerAndGradesData.GradeUnit();

            learnersAndTheirGrades[i] = new MyLearnerAndHisGrades(learnerId,
                    (firstName.length() == 0) ? (secondName) : (firstName.charAt(0) + " " + secondName),
                    secondName + " " + firstName, -1, emptyGrades, -1
            );


        }
        learnersCursor.close();

        // номер выбранного ученика
        chosenLearnerPosition = -1;
    }

    // обновляем оценки учеников из бд
    private void getGradesFromDB(DataBaseOpenHelper db) {
        for (MyLearnerAndHisGrades currentLearner : learnersAndTheirGrades) {

            // получаем оценки ученика за этот урок (если уже проставлялись)
            Cursor grades = db.getGradesByLearnerIdSubjectDateAndLesson(currentLearner.learnerId,
                    lessonBaseData.subjectId, lessonBaseData.lessonDate, lessonBaseData.lessonNumber);

            // пробегаемся по оценкам
            if (grades.moveToNext()) {
                // сразу получаем id оценки
                currentLearner.gradeId =
                        grades.getLong(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_ROW_ID));

                // если пропуска нет
                if (grades.isNull(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID))) {// если пропуска нет
                    for (int gradeI = 0; gradeI < SchoolContract.TableLearnersGrades.COLUMNS_GRADE.length; gradeI++) {
                        // оценка
                        currentLearner.gradesUnits[gradeI].grade =
                                grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[gradeI]));
                        // тип оценки
                        long typeId = grades.getLong(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[gradeI]));
                        // проходимся в цикле по всем типам оценок запоминая номер попавшегося
                        currentLearner.gradesUnits[gradeI].gradeTypePoz = -1;// единица всегда должна перекрываться другим значение иначе будет ошибка
                        int poz = 0;
                        while (graduationSettings.answersTypes.length > poz) {
                            if (graduationSettings.answersTypes[poz].id != typeId) {
                                poz++;
                            } else {
                                currentLearner.gradesUnits[gradeI].gradeTypePoz = poz;
                                break;
                            }
                        }
                    }
                    // пропуск по умолчанию пустой
                    currentLearner.absTypePozNumber = -1;
                } else {
                    int absId = grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID));

                    // проходимся в цикле по всем типам оценок и пишем в ученика уже не id а номер в массиве
                    currentLearner.absTypePozNumber = -1;
                    int poz = 0;
                    while (graduationSettings.absentTypes.length > poz) {
                        if (graduationSettings.absentTypes[poz].id != absId) {
                            poz++;
                        } else {
                            currentLearner.absTypePozNumber = poz;
                            break;
                        }
                    }
                    // проставляем оценки
                    currentLearner.gradesUnits[0].grade = 0;
                    currentLearner.gradesUnits[1].grade = 0;
                    currentLearner.gradesUnits[2].grade = 0;
                    currentLearner.gradesUnits[0].gradeTypePoz = 0;
                    currentLearner.gradesUnits[1].gradeTypePoz = 0;
                    currentLearner.gradesUnits[2].gradeTypePoz = 0;
                }
            } else {
                // если оценок у ученика на этом уроке вообще не найдено, то зануляем все
                for (int gradeI = 0; gradeI < 3; gradeI++) {
                    currentLearner.gradesUnits[gradeI].grade = 0;
                    currentLearner.gradesUnits[gradeI].gradeTypePoz = 0;
                }
                currentLearner.absTypePozNumber = -1;
            }
            grades.close();
        }
    }


    // при каждом показе экрана или если экран перекрыли, а потом показали еще раз
    @Override
    protected void onResume() {
        super.onResume();

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // обновляем трансформацию (размеры и отступы) кабинета
        updateCabinetTransformFromDB(db);
        // обновляем список парт и рассадку учеников
        checkDesksAndAndPlacesFromDB(db);
        db.close();

        // и выводим все
        outAll();
    }


    // обновляем трансформацию (размеры и отступы) кабинета
    void updateCabinetTransformFromDB(DataBaseOpenHelper db) {
        Cursor cabinetCursor = db.getCabinet(lessonBaseData.cabinetId);
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
        Cursor desksCursor = db.getDesksByCabinetId(lessonBaseData.cabinetId);
        while (desksCursor.moveToNext()) {

            // данные одной парты
            long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_ROW_ID));
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
                long placeId = placesCursor.getLong(SchoolContract.TablePlaces.KEY_ROW_ID);

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
                    MyLearnerAndHisGrades learner = learnersAndTheirGrades[learnerArrPos];
                    learner.viewData = learner.new LearnerViewData(
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
                            placeRoot.findViewById(R.id.lesson_desk_place_element_learner_background),
                            graduationSettings,
                            this
                    );

                    // при нажатии на контейнер ученика
                    int finalLearnerArrPos = learnerArrPos;
                    learnersAndTheirGrades[learnerArrPos].viewData.viewPlaceOut.setOnClickListener(view -> {
                        // меняем его оценку
                        tapOnLearner(learnersAndTheirGrades[finalLearnerArrPos]);
                    });

                    // при долгом клике на ученика
                    learnersAndTheirGrades[learnerArrPos].viewData.viewPlaceOut.setOnLongClickListener(view -> {

                        // выставляем этого ученика как выбранного
                        chosenLearnerPosition = finalLearnerArrPos;

                        // вызываем диалог изменения оценок
                        GradeEditLessonDialogFragment gradeDialog = new GradeEditLessonDialogFragment();
                        // передаем на вход данные
                        Bundle args = new Bundle();
                        args.putString(GradeEditLessonDialogFragment.ARGS_LEARNER_NAME,
                                learnersAndTheirGrades[finalLearnerArrPos].fullName);
                        args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY,
                                graduationSettings.getAnswersTypesArray());
                        args.putIntArray(GradeEditLessonDialogFragment.ARGS_INT_GRADES_ARRAY,
                                learnersAndTheirGrades[finalLearnerArrPos].getGradesArray());
                        args.putIntArray(GradeEditLessonDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY,
                                learnersAndTheirGrades[finalLearnerArrPos].getGradesTypesArray());
                        args.putStringArray(GradeEditLessonDialogFragment.ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY,
                                graduationSettings.getAbsentTypesLongNames());
                        args.putInt(GradeEditLessonDialogFragment.ARGS_INT_GRADES_ABSENT_TYPE_NUMBER,
                                learnersAndTheirGrades[finalLearnerArrPos].absTypePozNumber);
                        args.putInt(GradeEditLessonDialogFragment.ARGS_INT_MAX_GRADE,
                                graduationSettings.maxAnswersCount);
                        args.putInt(GradeEditLessonDialogFragment.ARGS_INT_CHOSEN_GRADE_POSITION,
                                learnersAndTheirGrades[finalLearnerArrPos].chosenGradePosition);
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

    // перерисовываем содержимое всего кабинета с новыми размерами и текстами из данных
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
                    learnersAndTheirGrades[learnerArrPos].viewData.updateSizePlaceView(multiplier, placeI);
                    // иконка ученика
                    learnersAndTheirGrades[learnerArrPos].viewData.updateLearnerBackground();
                    // имя ученика
                    learnersAndTheirGrades[learnerArrPos].viewData.updateLearnerNameText();
                    learnersAndTheirGrades[learnerArrPos].viewData.updateSizeLearnerNameView(multiplier);
                    // обновляем размеры и содержимое оценок
                    learnersAndTheirGrades[learnerArrPos].viewData.updateGradesTexts();
                    learnersAndTheirGrades[learnerArrPos].viewData.updateSizesGradesViews(multiplier);
                }
            }
        }
    }


    // нажатие на ученика
    void tapOnLearner(MyLearnerAndHisGrades tappedLearner) {

        // если стоит попуск, оценку менять нельзя
        if (tappedLearner.absTypePozNumber == -1) {

            // увеличиваем значение оценки
            LessonListActivity.LessonListLearnerAndGradesData.GradeUnit tappedGrade =
                    tappedLearner.gradesUnits[tappedLearner.chosenGradePosition];
            tappedGrade.grade = (tappedGrade.grade % graduationSettings.maxAnswersCount) + 1;

            // вызываем перерисовку у ученика
            tappedLearner.viewData.updateGradesTexts();

            // сохраняем результат в бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            if (tappedLearner.gradeId == -1) {
                tappedLearner.gradeId = db.createGrade(
                        tappedLearner.learnerId,
                        tappedLearner.gradesUnits[0].grade,
                        tappedLearner.gradesUnits[1].grade,
                        tappedLearner.gradesUnits[2].grade,
                        graduationSettings.answersTypes[tappedLearner.gradesUnits[0].gradeTypePoz].id,
                        graduationSettings.answersTypes[tappedLearner.gradesUnits[1].gradeTypePoz].id,
                        graduationSettings.answersTypes[tappedLearner.gradesUnits[2].gradeTypePoz].id,
                        (tappedLearner.absTypePozNumber == -1) ? (-1) : (graduationSettings.absentTypes[tappedLearner.absTypePozNumber].id),
                        lessonBaseData.subjectId, lessonBaseData.lessonDate, lessonBaseData.lessonNumber
                );
            } else {
                // если все поля нулевые удаляем оценку
                if (tappedLearner.gradesUnits[0].grade == 0 && tappedLearner.gradesUnits[1].grade == 0 &&
                        tappedLearner.gradesUnits[2].grade == 0 && tappedLearner.absTypePozNumber == -1) {
                    db.removeGrade(tappedLearner.gradeId);
                } else
                    db.editGrade(tappedLearner.gradeId,
                            tappedLearner.gradesUnits[0].grade,
                            tappedLearner.gradesUnits[1].grade,
                            tappedLearner.gradesUnits[2].grade,
                            graduationSettings.answersTypes[tappedLearner.gradesUnits[0].gradeTypePoz].id,
                            graduationSettings.answersTypes[tappedLearner.gradesUnits[1].gradeTypePoz].id,
                            graduationSettings.answersTypes[tappedLearner.gradesUnits[2].gradeTypePoz].id,
                            (tappedLearner.absTypePozNumber == -1) ? (-1) : (graduationSettings.absentTypes[tappedLearner.absTypePozNumber].id));
            }
            db.close();

        }
    }

    // обратная связь от диалога оценок GradeDialogFragment
    @Override
    public void setGrades(int[] grades, int[] chosenTypesNumbers, int chosenAbsPoz) {

        if (chosenLearnerPosition != -1) {
            MyLearnerAndHisGrades chosenOne = learnersAndTheirGrades[chosenLearnerPosition];

            // меняем списки

            // если стоят оценки
            chosenOne.absTypePozNumber = chosenAbsPoz;
            if (chosenAbsPoz == -1) {
                for (int i = 0; i < 3; i++) {
                    chosenOne.gradesUnits[i].grade = grades[i];
                    chosenOne.gradesUnits[i].gradeTypePoz = chosenTypesNumbers[i];
                }
            } else {// стоит пропуск
                for (int i = 0; i < 3; i++) {
                    chosenOne.gradesUnits[i].grade = 0;
                    chosenOne.gradesUnits[i].gradeTypePoz = 0;
                }
            }

            // сохраняем значения в бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            if (chosenOne.gradeId == -1) {
                chosenOne.gradeId = db.createGrade(
                        chosenOne.learnerId,
                        chosenOne.gradesUnits[0].grade,
                        chosenOne.gradesUnits[1].grade,
                        chosenOne.gradesUnits[2].grade,
                        graduationSettings.answersTypes[chosenOne.gradesUnits[0].gradeTypePoz].id,
                        graduationSettings.answersTypes[chosenOne.gradesUnits[1].gradeTypePoz].id,
                        graduationSettings.answersTypes[chosenOne.gradesUnits[2].gradeTypePoz].id,
                        (chosenAbsPoz == -1) ? (-1) : (graduationSettings.absentTypes[chosenAbsPoz].id),
                        lessonBaseData.subjectId, lessonBaseData.lessonDate, lessonBaseData.lessonNumber
                );
            } else {
                // если все поля нулевые удаляем оценку
                if (chosenOne.gradesUnits[0].grade == 0 && chosenOne.gradesUnits[1].grade == 0 &&
                        chosenOne.gradesUnits[2].grade == 0 && chosenAbsPoz == -1) {
                    db.removeGrade(chosenOne.gradeId);
                } else
                    db.editGrade(chosenOne.gradeId,
                            chosenOne.gradesUnits[0].grade,
                            chosenOne.gradesUnits[1].grade,
                            chosenOne.gradesUnits[2].grade,
                            graduationSettings.answersTypes[chosenOne.gradesUnits[0].gradeTypePoz].id,
                            graduationSettings.answersTypes[chosenOne.gradesUnits[1].gradeTypePoz].id,
                            graduationSettings.answersTypes[chosenOne.gradesUnits[2].gradeTypePoz].id,
                            (chosenAbsPoz == -1) ? (-1) : (graduationSettings.absentTypes[chosenAbsPoz].id));
            }
            db.close();

            // выводим изменения в интерфейс
            // ставим выбранной следующую оценку
            chosenOne.chosenGradePosition = (chosenOne.chosenGradePosition + 1) % 3;
            // обновляем текст и картинки на ученике
            chosenOne.viewData.updateGradesTexts();
            chosenOne.viewData.updateSizesGradesViews(multiplier);
        }
        // убираем выбор с ученика
        chosenLearnerPosition = -1;

    }


    // зум и перемещение экрана
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
                                    learnersAndTheirGrades[deskUnit.seatingLearnerNumber[placeI]].viewData
                                            .updateSizesForZoom(multiplier, placeI);
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
                        lessonBaseData.cabinetId,
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

        if (LessonListActivity.RESULT_BACK == resultCode) {

            // пользователь вернулся с активности в которой можно редактировать оценки
            // значит надо подгрузить их из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(LessonActivity.this);
            getGradesFromDB(db);
            db.close();

            // и вывести подгруженное
            outAll();
        } else if (LessonListActivity.RESULT_SAVE == resultCode) {
            // если получили сигнал, значит пользователь нажал кнопку сохранить,
            // и эта активность больше не нужна

            //обнуляем данные
            chosenLearnerPosition = -1;
            lessonBaseData = null;
            learnersAndTheirGrades = null;

            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //кнопка назад в actionBar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private int pxFromDp(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }


    private static class LessonBaseData {

        // id зависимости урока
        private long lessonAttitudeId;
        // id класса
        private long learnersClassId;
        // id предмета
        private long subjectId;
        // id кабинета
        private long cabinetId;
        // время урока
        private String lessonDate;
        private int lessonNumber;

        // имя класса todo проверить нужность этих полей, возможно их можно заменить например такими вещами как заголовок, описание, итд
        private String className;
        // имя предмета
        private String subjectName;
        // название кабинета
        private String cabinetName;


        // -------- фабрика обьектов --------
        static LessonBaseData newInstance(DataBaseOpenHelper db, long lessonAttitudeId, String lessonDate, int lessonNumber) {
            LessonBaseData result = new LessonBaseData();

            result.lessonAttitudeId = lessonAttitudeId;
            result.lessonDate = lessonDate;
            result.lessonNumber = lessonNumber;


            // получаем зависимость
            Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(lessonAttitudeId);
            attitudeCursor.moveToFirst();
            // получаем из завмсимости id кабинета
            result.cabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID
            ));
            // получаем из завмсимости id предмета
            result.subjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID
            ));
            attitudeCursor.close();

            // получаем предмет
            Cursor subjectCursor = db.getSubjectById(result.subjectId);
            subjectCursor.moveToFirst();
            // получаем имя предмета
            result.subjectName = subjectCursor.getString(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
            // получаем id класса
            result.learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(
                    SchoolContract.TableSubjects.KEY_CLASS_ID
            ));
            subjectCursor.close();

            // получаем имя класа
            Cursor learnersClass = db.getLearnersClases(result.learnersClassId);
            learnersClass.moveToFirst();
            result.className = learnersClass.getString(learnersClass.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            learnersClass.close();

            // получаем имя кабинета
            Cursor cabinetNameCursor = db.getCabinet(result.cabinetId);
            cabinetNameCursor.moveToFirst();
            result.cabinetName = cabinetNameCursor.getString(cabinetNameCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
            cabinetNameCursor.close();

            return result;
        }

        private LessonBaseData() {
        }
    }


    // класс содержащий в себе парту // todo static
    private class DeskUnit {
        int numberOfPlaces;
        int[] seatingLearnerNumber;// ссылки на учеников сидящих за партой
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