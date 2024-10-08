package com.learning.texnar13.teachersprogect.lesson;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SCursor;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;
import com.learning.texnar13.teachersprogect.learnersAndGradesOut.GradeEditDialogFragment;
import com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesActivity;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Locale;

/*
todo переделать описание?
 * onCreate(),
 * подгружаются все ученики, id кабинета, класса, предмета, зав. урок-время
 * подгружаются массивы с оценками
 *
 *
 * */

public class LessonActivity extends AppCompatActivity implements View.OnTouchListener, GradeEditDialogFragment.EditGradeDialogInterface {

    // ---------- константы для аргументов ----------
    // константа по которой получаеми id зависимости
    public static final String ARGS_LESSON_ATTITUDE_ID = "lessonAttitudeId";
    // константа по которой получаем время урока
    public static final String ARGS_LESSON_DATE = "lessonDate";
    public static final String ARGS_LESSON_NUMBER = "lessonNumber";


    // обьект для преобразования календаря в строку
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // ---------- Переменные ----------

    // view слоя с партами
    private LessonOutView outView;

    // View от bottomSheet
    TextView commentText;
    TextView commentTitleText;

    // плотность экрана нужна для расчета размеров парт
    static float density;

    // --- параметры из бд не исчезающие при повороте экрана, но обновляющиеся при заходе на активность ----------

    // сам обьект базы данных
    DataBaseOpenHelper db;

    // максимальная оценка, типи пропусков, итд
    private static GraduationSettings graduationSettings;
    // неизменные данные об уроке
    private static LessonBaseData lessonBaseData;
    // текущий обьект дз
    private static  HomeWorkUnit homeWork;

    // массив учеников
    private static LessonLearnerAndHisGrades[] learnersAndTheirGrades;
    // номер выбранного ученика
    private static int chosenLearnerPosition;
    // лист с обьектами парт
    private static ArrayList<DeskUnit> desksList;


    // растяжение по осям
    private static float multiplier = 0;//0,1;10 (обновляется в updateCabinetTransformFromDB и onTouch)
    // текущее смещение по осям
    private static float xAxisPXOffset = 0;
    private static float yAxisPXOffset = 0;


    // ---------------------------------------------------------------------------------------------
    // ------ Меню
    // ---------------------------------------------------------------------------------------------

    // раздуваем меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    // нажатие кнопок меню, просто переход в разные активности
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // активность конца урока
        menu.findItem(R.id.lesson_menu_end_lesson).setOnMenuItemClickListener(menuItem -> {
            // переходим к активности списка оценок
            Intent intent = new Intent(getApplicationContext(), LessonListActivity.class);
            // передаем координаты этого урока
            intent.putExtra(ARGS_LESSON_ATTITUDE_ID, lessonBaseData.lessonAttitudeId);
            intent.putExtra(ARGS_LESSON_DATE, lessonBaseData.lessonDate);
            intent.putExtra(ARGS_LESSON_NUMBER, lessonBaseData.lessonNumber);
            startActivityForResult(intent, 1);
            return true;
        });
        // активность рассадки учеников
        menu.findItem(R.id.lesson_menu_edit_seating).setOnMenuItemClickListener(menuItem -> {
            Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
            intent.putExtra(SeatingRedactorActivity.CLASS_ID, lessonBaseData.learnersClassId);
            intent.putExtra(SeatingRedactorActivity.CABINET_ID, lessonBaseData.cabinetId);
            startActivity(intent);
            return true;
        });
        // активность редактор кабинета (расставить парты)
        menu.findItem(R.id.lesson_menu_edit_tables).setOnMenuItemClickListener(menuItem -> {
            Intent intent = new Intent(getApplicationContext(), CabinetRedactorActivity.class);
            intent.putExtra(CabinetRedactorActivity.EDITED_CABINET_ID, lessonBaseData.cabinetId);
            startActivity(intent);
            return true;
        });
        return true;
    }


    // ---------------------------------------------------------------------------------------------
    // ------ Жизненный цикл
    // ---------------------------------------------------------------------------------------------

    /**
     * Переменная храняящая значение звукового режима при входе на эту активность.
     * нужна для того, чтобы при выходе вернуть это значение на место
     */
    static int ringerMode = -1;

    // создание экрана
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // раздуваем layout
        setContentView(R.layout.lesson_activity);

        // -------- подготовка заголовка --------
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

        // -------- подгрузка данных --------
        // проверяем был создан новый экран или он просто переворачивался
        if (savedInstanceState == null) {
            // получаем зависимость и дату урока из intent
            long lessonAttitudeId = getIntent().getLongExtra(ARGS_LESSON_ATTITUDE_ID, -1);
            String lessonDate = getIntent().getStringExtra(ARGS_LESSON_DATE);
            if (lessonAttitudeId == -1 || lessonDate.equals("")) {// -1 он будет равен только при ошибке
                finish();
                return;
            }
            // получаем начальные данные об уроке из бд
            db = new DataBaseOpenHelper(this);
            lessonBaseData = LessonBaseData.newInstance(
                    db,
                    lessonAttitudeId,
                    lessonDate,
                    getIntent().getIntExtra(ARGS_LESSON_NUMBER, 0)
            );

            // подгружаем все остальное
            initData();
        }


        // -------- настройка view --------

        // плотность экрана нужна для расчета размеров парт
        density = getResources().getDisplayMetrics().density;

        // слой с партами
        outView = findViewById(R.id.lesson_activity_out_view);
        outView.setOnTouchListener(this);

        // заголовок - выставляем название предмета и класса
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(// abcde -> abc…  abcd->abcd укорачиваем поля если они слишком длинные …
                ((lessonBaseData.subjectName.length() > 10) ? (lessonBaseData.subjectName.substring(0, 9) + "…") : (lessonBaseData.subjectName)) + ", " +
                        ((lessonBaseData.className.length() > 4) ? (lessonBaseData.className.substring(0, 3) + "…") : (lessonBaseData.className)) + ", " +
                        ((lessonBaseData.cabinetName.length() > 4) ? (lessonBaseData.cabinetName.substring(0, 3) + "…") : (lessonBaseData.cabinetName))
        );

        // bottomSheet - (выдвигающейся с низу менюшкой)
        {
            // обращаемся к вью, как к элементу с поведением
            BottomSheetBehavior<LinearLayout> bottomSheetBehavior =
                    BottomSheetBehavior.from(findViewById(R.id.activity_lesson_bottom_sheet));

            // Находим заголовок для анимации
            final View textView = findViewById(R.id.activity_lesson_bottom_sheet_title_text);
            final View bottomContainer = findViewById(R.id.activity_lesson_bottom_sheet_bottom_content);

            // кнопки переключения дат дз
            // final View buttonForward = findViewById(R.id.activity_lesson_bottom_sheet_button_date_back);
            // final View buttonBack = findViewById(R.id.activity_lesson_bottom_sheet_button_date_forward);


            // выставляем данные полученные из бд
            commentTitleText = findViewById(R.id.activity_lesson_bottom_sheet_date_text);
            commentText = findViewById(R.id.activity_lesson_bottom_sheet_edit_text);

            commentTitleText.setText(homeWork.title);
            commentText.setText(homeWork.text);


            // назначаем раскрытие и закрытие по нажатию заголовка
            findViewById(R.id.activity_lesson_bottom_sheet_title_text).setOnClickListener(
                    view -> bottomSheetBehavior.setState(
                            (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) ?
                                    (BottomSheetBehavior.STATE_EXPANDED) :
                                    (BottomSheetBehavior.STATE_COLLAPSED)
                    )
            );

            // todo события пролистывания
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {

                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // анимация скрытия и показа bottomSheet
                    textView.setAlpha(slideOffset);
                    bottomContainer.setAlpha(slideOffset);
                    //.animate().alpha(slideOffset).setDuration(0).start();
                    //.setListener(new Animator.AnimatorListener() {
                }
            });
        }
    }


    // при каждом показе экрана или если экран перекрыли, а потом показали еще раз
    @Override
    protected void onResume() {
        super.onResume();

        // --- обновляем все данные из бд ---
        if (db == null)
            db = new DataBaseOpenHelper(this);
        // обновляем трансформацию (размеры и отступы) кабинета
        updateCabinetTransformFromDB();
        // обновляем список парт и рассадку учеников
        checkDesksAndAndPlacesFromDB();
        // и выводим все
        outAll();


        // --- настройка тихого урока --- (включение режима)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                    (NotificationManager) LessonActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
            // если настройка включена
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                AudioManager am = (AudioManager) getBaseContext().getSystemService(LessonActivity.AUDIO_SERVICE);
                // сохраняем старую настройку чтобы при выходе из активности вернуть её
                ringerMode = am.getRingerMode();
                // убираем звук
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // сохраняем дз
        String lessonComment = commentText.getText().toString().trim();
        if (lessonComment.length() > 0) {
            if (homeWork.dbId == -1) {
                db.createLessonComment(
                        lessonBaseData.lessonDate,
                        lessonBaseData.lessonAttitudeId,
                        lessonComment
                );
            } else {
                db.setLessonCommentStringById(
                        homeWork.dbId,
                        lessonComment
                );
            }
        }

        // закрываем базу данных после использования
        db.close();
        db = null;

        // --- настройка тихого урока --- (выключение)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                    (NotificationManager) LessonActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
            // если настройка включена
            if (ringerMode != -1 && notificationManager.isNotificationPolicyAccessGranted()) {
                AudioManager am = (AudioManager) getBaseContext().getSystemService(LessonActivity.AUDIO_SERVICE);
                // возвращаем старую настройку
                am.setRingerMode(ringerMode);
            }
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ------ Методы подгрузки данных из бд (при запуске активности)
    // ---------------------------------------------------------------------------------------------

    /**
     * Начальная подгрузка данных из бд
     * (Вызывается в OnCreate)
     */
    void initData() {

        // ------ получаем не меняющиеся настроки для всех классов ------
        graduationSettings = GraduationSettings.newInstance(db);

        // создаем список парт
        desksList = new ArrayList<>();

        // получаем учеников
        getLearnersFromDB();

        // получаем их оценки
        getGradesFromDB();


        // получаем дз

        GregorianCalendar calendar = new GregorianCalendar();
        try {
            calendar.setTime(dateFormat.parse(lessonBaseData.lessonDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // "17 Октября, ПН, 1 урок"
        String title = getResources().getString(R.string.lesson_activity_bottom_sheet_date_text,
                calendar.get(GregorianCalendar.DAY_OF_MONTH),
                getResources().getStringArray(R.array.months_names_with_ending)[
                        calendar.get(GregorianCalendar.MONTH)],
                getResources().getStringArray(R.array.schedule_month_activity_week_days_short_array)[
                        (calendar.get(GregorianCalendar.DAY_OF_WEEK) >= 2) ?
                                (calendar.get(GregorianCalendar.DAY_OF_WEEK) - 2) :
                                (calendar.get(GregorianCalendar.DAY_OF_WEEK) + 5)
                        ],
                lessonBaseData.lessonNumber + 1
        );


        Cursor homework = db.getLessonCommentsByDateAndLesson(lessonBaseData.lessonAttitudeId, lessonBaseData.lessonDate);
        if (homework.moveToNext()) {
            homeWork = new HomeWorkUnit(
                    homework.getLong(homework.getColumnIndexOrThrow(SchoolContract.TableLessonComment.KEY_ROW_ID)),
                    title,
                    homework.getString(homework.getColumnIndexOrThrow(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT))
            );
        } else {
            homeWork = new HomeWorkUnit(
                    -1,
                    title,
                    ""
            );
        }
        homework.close();

    }


    /**
     * Получаем учеников с пустыми оценками
     * (Вызывается в initData)
     */
    void getLearnersFromDB() {
        // получаем учеников по id класса
        Cursor learnersCursor = db.getLearnersByClassId(lessonBaseData.learnersClassId);

        // инициализируем массив с учениками
        learnersAndTheirGrades = new LessonLearnerAndHisGrades[learnersCursor.getCount()];
        // заполняем его
        for (int i = 0; i < learnersAndTheirGrades.length; i++) {
            learnersCursor.moveToPosition(i);

            long learnerId = learnersCursor.getLong(learnersCursor.getColumnIndexOrThrow(
                    SchoolContract.TableLearners.KEY_ROW_ID
            ));

            // создаем нового ученика с пустыми оценками
            String firstName = learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(
                    SchoolContract.TableLearners.COLUMN_FIRST_NAME));
            String secondName = learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(
                    SchoolContract.TableLearners.COLUMN_SECOND_NAME));

            LessonListActivity.LessonListLearnerAndGradesData.GradeUnit[] emptyGrades =
                    new LessonListActivity.LessonListLearnerAndGradesData.GradeUnit[3];
            for (int gradeI = 0; gradeI < 3; gradeI++)
                emptyGrades[gradeI] = new LessonListActivity.LessonListLearnerAndGradesData.GradeUnit();

            learnersAndTheirGrades[i] = new LessonLearnerAndHisGrades(
                    learnerId, firstName, secondName, -1, emptyGrades, -1);
        }
        learnersCursor.close();

        // номер выбранного ученика
        chosenLearnerPosition = -1;
    }


    /**
     * Обновляем оценки учеников из бд (+ отрабатывает при возврате из LessonListActivity)
     * (Вызывается в initData)
     * (Вызывается в onActivityResult(lesson list) )
     */
    private void getGradesFromDB() {
        for (LessonLearnerAndHisGrades currentLearner : learnersAndTheirGrades) {

            // получаем оценки ученика за этот урок (если уже проставлялись)
            Cursor grades = db.getGradesByLearnerIdSubjectDateAndLesson(currentLearner.learnerId,
                    lessonBaseData.subjectId, lessonBaseData.lessonDate, lessonBaseData.lessonNumber);

            // пробегаемся по оценкам
            if (grades.moveToNext()) {
                // сразу получаем id оценки
                currentLearner.gradeId =
                        grades.getLong(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID));

                // если пропуска нет
                if (grades.isNull(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID))) {

                    // пробегаемся по оценкам
                    for (int gradeI = 0; gradeI < SchoolContract.TableLearnersGrades.COLUMNS_GRADE.length; gradeI++) {

                        // поле оценки
                        currentLearner.gradesUnits[gradeI].grade =
                                grades.getInt(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[gradeI]));

                        // поле типа оценки
                        long typeId = grades.getLong(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[gradeI]));
                        {// Ищем этот тип по Id в списке, запоминая номер в массиве а не id
                            int poz = 0;
                            while (graduationSettings.answersTypes.length > poz && graduationSettings.answersTypes[poz].id != typeId)
                                poz++;
                            if (graduationSettings.answersTypes.length != poz) {
                                currentLearner.gradesUnits[gradeI].gradeTypePoz = poz;
                            } else {
                                // По идее такого не должно быть
                                currentLearner.gradesUnits[gradeI].gradeTypePoz = -1;
                                Log.wtf("teachersApp", "lesson - gradeTypePoz = -1");
                            }
                        }
                    }

                    // пропуск по умолчанию пустой
                    currentLearner.absTypePozNumber = -1;
                } else {
                    // оценок нет, зануляем все
                    for (int gradeI = 0; gradeI < 3; gradeI++) {
                        currentLearner.gradesUnits[gradeI].grade = 0;
                        currentLearner.gradesUnits[gradeI].gradeTypePoz = 0;
                    }

                    // пропуск
                    int absId = grades.getInt(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID));
                    {// Ищем этот тип по Id в списке, запоминая номер в массиве а не id
                        int poz = 0;
                        while (graduationSettings.absentTypes.length > poz && graduationSettings.absentTypes[poz].id != absId)
                            poz++;
                        if (graduationSettings.absentTypes.length != poz) {
                            currentLearner.absTypePozNumber = poz;
                        } else {
                            // По идее такого не должно быть
                            currentLearner.absTypePozNumber = -1;
                            Log.wtf("teachersApp", "lesson - absTypePozNumber = -1");
                        }
                    }
                }
            } else {
                // если оценок у ученика на этом уроке вообще не найдено, то зануляем все
                for (int gradeI = 0; gradeI < 3; gradeI++) {
                    currentLearner.gradesUnits[gradeI].grade = 0;
                    currentLearner.gradesUnits[gradeI].gradeTypePoz = 0;
                }

                // пропуск
                currentLearner.absTypePozNumber = -1;
            }
            grades.close();
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ------ Методы обновления данных активности из бд, и вывода графики
    // ---------------------------------------------------------------------------------------------

    /**
     * Обновляем трансформацию (размеры и отступы) кабинета
     * (Вызывается в OnResume)
     */
    void updateCabinetTransformFromDB() {
        Cursor cabinetCursor = db.getCabinet(lessonBaseData.cabinetId);
        cabinetCursor.moveToFirst();
        // получаем множитель  (0.25 <-> 4)
        multiplier = 0.0375F * cabinetCursor.getLong(cabinetCursor.getColumnIndexOrThrow(
                SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER)) + 0.25F;
        // и отступы
        xAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X));
        yAxisPXOffset = cabinetCursor.getLong(cabinetCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y));
        cabinetCursor.close();
    }

    /**
     * получаем данные о партах, а также зависимости ученик-место
     * на этих партах связывая учеников с их местом за партой.
     * Также инициализируем все view (только инициализируем, без размеров и фонов)
     * (Вызывается в OnResume)
     */
    void checkDesksAndAndPlacesFromDB() {

        // чистим список парт
        desksList.clear();

        // загружаем парты из бд
        Cursor desksCursor = db.getDesksByCabinetId(lessonBaseData.cabinetId);
        while (desksCursor.moveToNext()) {

            // создаем новую парту  парту и данные в массив (в конструкторе заполняя позицию и размеры view)
            DeskUnit currentDeskUnit = new DeskUnit(
                    desksCursor.getLong(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_X)),
                    desksCursor.getLong(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_Y)),
                    desksCursor.getInt(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES))
            );
            desksList.add(currentDeskUnit);

            // получаем места на парте из её id
            long deskId = desksCursor.getLong(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.KEY_ROW_ID));
            SCursor placesCursor = new SCursor(db.getPlacesByDeskId(deskId));
            while (placesCursor.moveToNext()) {

                // получаем информацию об этом месте
                // id места
                long placeId = placesCursor.getLong(SchoolContract.TablePlaces.KEY_ROW_ID);

                // получаем номер ученика (который сидит на этом месте) в массиве с учениками и их оценками
                int learnerArrPos = -1;
                // все записи с таким местом
                SCursor placesAttitudes = new SCursor(db.getAttitudesByPlaceId(placeId));
                while (placesAttitudes.moveToNext() && learnerArrPos == -1) {
                    // получаем из места id ученика связанного с этим местом
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
            placesCursor.close();
        }
        desksCursor.close();
    }

    /**
     * перерисовываем содержимое всего кабинета с новыми размерами и текстами из данных
     * (+ отрабатывает при возврате из LessonListActivity)
     * метод просто обновляет размеры парт и учеников,
     * (Вызывается в OnResume)
     * (Вызывается в onActivityResult(lesson list) )
     */
    @SuppressLint("ResourceType")
    void outAll() {

        // передаем данные во view
        outView.setData(graduationSettings.maxAnswersCount, learnersAndTheirGrades, desksList);
        // выводим графику
        outView.setNewScaleParams(multiplier, new PointF(xAxisPXOffset, yAxisPXOffset));

    }


    // ---------------------------------------------------------------------------------------------
    // ------ Зум, перемещение экрана, нажатие на экран
    // ---------------------------------------------------------------------------------------------


    // для перемещения и зума
    // режимы зума
    private static final int NONE = 0;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // точка середины между пальцами за предыдущую итерацию
    private Point oldMid = new Point();
    // предыдущее растояние между пальцам
    private float oldDist = 1f;


    // для долгого нажатия - продолжается ли оно ещё, или уже нет
    // коллбэк последнего не остановленного хэндлера
    abstract static class LongClickHandlerCallback implements Handler.Callback {
        boolean longPressCanBe = true;// переменная которая говорит хэндлеру нужен он ещё, или уже нет
    }

    LongClickHandlerCallback longClickHandlerCallback;


    // номер нажатого ученика в массиве
    int pressedLearner = -1;
    PointF currentPress = new PointF();

    // зум и перемещение экрана
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        /*
         * -действие
         * =состояние
         *  комментарий
         *
         * первое нажатие
         *   =перемещение=
         *   -ищем парту и омечаем-
         * нажатие
         *   -заканчиваем перемещение, сохраняем-
         *   =zoom=
         *   -инициализируем зум-
         * движение
         *   -при перемещении меняем-
         *   -при зуме зумим-
         * отпускание
         *    если есть зум прекращаем его, не начиная касания
         *   =none=
         * последнее отпускание
         *   -если есть перемещение завершаем его-
         *   =none=
         */

        currentPress.x = motionEvent.getX(0);
        currentPress.y = motionEvent.getY(0);

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // поставили первый палец

                // проверка было ли нажатие на ученика
                pressedLearner = outView.getPressedLearnerNumber(motionEvent.getX(0), motionEvent.getY(0));

                // действия выполняемые хэндлером
                longClickHandlerCallback = new LongClickHandlerCallback() {
                    @Override
                    public boolean handleMessage(@NonNull Message message) {
                        if (message.what == 101 && longPressCanBe) {
                            // останавливаем нажатие
                            longPressCanBe = false;
                            mode = NONE;

                            // если нажатие началось на ученике и палец пользователя до сих пор на нем
                            if (pressedLearner != -1 &&
                                    pressedLearner == outView.getPressedLearnerNumber(currentPress.x, currentPress.y)) {

                                // говорим системе, что нажатие обработано
                                view.performLongClick();
                                // говорим системе, чтобы та вызвала событие вибрации
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

                                // выполнение действия на длинном нажатии
                                learnerLongPress();
                            }
                        }
                        return false;
                    }
                };

                // создаем отложенный вызов хэндлера для срабатывания лонг клика
                (new Handler(getMainLooper(), longClickHandlerCallback))
                        .sendEmptyMessageDelayed(101, android.view.ViewConfiguration.getLongPressTimeout());
                break;

            case MotionEvent.ACTION_POINTER_DOWN:// поставили следующий палец
                // если уже поставлен второй палец на остальные не реагируем
                if (motionEvent.getPointerCount() == 2) {

                    // завершаем нажатие на ученика
                    pressedLearner = -1;
                    longClickHandlerCallback.longPressCanBe = false;

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
                // зумим и перемещаем
                if (mode == ZOOM) {
                    // текущая середина касания пальцев
                    Point nowMid = findMidPoint(motionEvent);
                    //находим коэффициент разницы между новым и изначальным расстоянием между пальцами
                    float nowDist = spacing(motionEvent);
                    float scale = nowDist * 100 / oldDist;
                    // -------- сам зум --------
                    if ((multiplier * scale / 100 >= 0.25f) &&//слишком маленький размер
                            (multiplier * scale / 100 <= 4f)//слишком большой размер
                    ) {// todo проверка на слишком большиее и маленькие xAxisPXOffset yAxisPXOffset
                        // переназначаем смещение осей из-за зума
                        xAxisPXOffset = nowMid.x - (((nowMid.x - xAxisPXOffset) * scale)) / 100;
                        yAxisPXOffset = nowMid.y - (((nowMid.y - yAxisPXOffset) * scale)) / 100;
                        // меняя множитель назначаем растяжение осей
                        multiplier = multiplier * scale / 100;
                    }

                    // переназначаем центр осей по перемещению центра пальцев
                    // todo проверка на слишком большиее и маленькие xAxisPXOffset yAxisPXOffset
                    xAxisPXOffset = xAxisPXOffset + nowMid.x - oldMid.x;
                    yAxisPXOffset = yAxisPXOffset + nowMid.y - oldMid.y;

                    // текущие позиции пальцев становятся предыдущими
                    oldDist = nowDist;
                    oldMid = nowMid;


                    // вызов у view отрисовки с новыми размерами
                    outView.setNewScaleParams(multiplier, new PointF(xAxisPXOffset, yAxisPXOffset));

                }
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                // прекращаем долгое нажатие
                longClickHandlerCallback.longPressCanBe = false;
                // прекращаем перемещение
                mode = NONE;

                // сохраняем множитель и смещение для этого кабинета
                db.setCabinetMultiplierOffsetXOffsetY(
                        lessonBaseData.cabinetId,
                        (int) ((multiplier - 0.25F) / 0.0375F),
                        (int) xAxisPXOffset,
                        (int) yAxisPXOffset
                );
                break;
            }
            case MotionEvent.ACTION_UP:

                // если долгое нажатие еще не успело отработать (longPressCanBe==true),
                // а пользователь уже отпустил палец (ACTION_UP) не дожидаясь
                if (longClickHandlerCallback.longPressCanBe) {

                    // прекращаем долгое нажатие
                    longClickHandlerCallback.longPressCanBe = false;
                    // прекращаем нажатие
                    mode = NONE;

                    // если нажатие началось на ученике и палец пользователя до сих пор на нем,
                    if (pressedLearner != -1 &&
                            pressedLearner == outView.getPressedLearnerNumber(motionEvent.getX(0), motionEvent.getY(0))) {
                        // говорим системе, что нажатие обработано
                        view.performClick();
                        // вызываем обновление ученика
                        tapOnLearner(pressedLearner);
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                // прекращаем долгое нажатие
                longClickHandlerCallback.longPressCanBe = false;
                // прекращаем любое перемещение/нажатие
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


    // нажатие на ученика
    void tapOnLearner(int pressedLearnerListNumber) {

        LessonLearnerAndHisGrades tappedLearner = learnersAndTheirGrades[pressedLearnerListNumber];

        Log.e("TAG", "tapOnLearner: learnerId=" + tappedLearner.learnerId);

        // если стоит попуск, оценку менять нельзя
        if (tappedLearner.absTypePozNumber == -1) {

            // увеличиваем значение оценки
            LessonListActivity.LessonListLearnerAndGradesData.GradeUnit tappedGrade =
                    tappedLearner.gradesUnits[tappedLearner.chosenGradePosition];
            tappedGrade.grade = (tappedGrade.grade % graduationSettings.maxAnswersCount) + 1;

            // передаем данные ученика во view
            outView.updateLearner(
                    pressedLearnerListNumber,
                    new int[]{
                            tappedLearner.gradesUnits[0].grade,
                            tappedLearner.gradesUnits[1].grade,
                            tappedLearner.gradesUnits[2].grade,
                    },
                    tappedLearner.chosenGradePosition,
                    tappedLearner.absTypePozNumber
            );
            // перерисовка тех данных, которые во view
            outView.setNewScaleParams(multiplier, new PointF(xAxisPXOffset, yAxisPXOffset));

            // сохраняем результат в бд
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
                    tappedLearner.gradeId = -1;
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
        }
    }

    // долгое нажатие на ученика
    private void learnerLongPress() {
        // выставляем этого ученика как выбранного
        chosenLearnerPosition = pressedLearner;

        // вызываем диалог изменения оценок
        GradeEditDialogFragment editGrade = new GradeEditDialogFragment();
        // параметры
        Bundle bundle = new Bundle();
        // имя ученика
        bundle.putString(GradeEditDialogFragment.ARGS_LEARNER_NAME,
                learnersAndTheirGrades[chosenLearnerPosition].secondName + " " +
                        learnersAndTheirGrades[chosenLearnerPosition].firstName);
        // названия типов оценок
        bundle.putStringArray(GradeEditDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY,
                graduationSettings.getAnswersTypesArray());
        // массив оценок
        bundle.putIntArray(GradeEditDialogFragment.ARGS_INT_GRADES_ARRAY,
                learnersAndTheirGrades[chosenLearnerPosition].getGradesArray());
        // массив с номерами выбранных типов
        bundle.putIntArray(GradeEditDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY,
                learnersAndTheirGrades[chosenLearnerPosition].getGradesTypesArray());
        // выбранный номер пропуска
        bundle.putInt(GradeEditDialogFragment.ARGS_INT_GRADES_ABSENT_TYPE_NUMBER,
                learnersAndTheirGrades[chosenLearnerPosition].absTypePozNumber);
        // названия типов пропусков
        bundle.putStringArray(
                GradeEditDialogFragment.ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY,
                graduationSettings.getAbsentTypesLongNames()
        );
        // максимальный размер оценки
        bundle.putInt(GradeEditDialogFragment.ARGS_INT_MAX_GRADE,
                graduationSettings.maxAnswersCount);
        // максимальное количество уроков
        bundle.putInt(GradeEditDialogFragment.ARGS_INT_MAX_LESSONS_COUNT, lessonBaseData.lessonNumber);
        // текущая дата в строке
        bundle.putString(GradeEditDialogFragment.ARGS_STRING_CURRENT_DATE,
                ""//(touchDataDayNumber + 1) + " " + transformedMonthsNames[viewCalendar.get(Calendar.MONTH)]
        );
        // выбранный номер урока
        bundle.putInt(GradeEditDialogFragment.ARGS_INT_LESSON_NUMBER, lessonBaseData.lessonNumber);

        // фиксируем один номер урока
        bundle.putBoolean(GradeEditDialogFragment.ARGS_BOOLEAN_IS_LESSON_NUMBER_LOCKED, true);
        // выделение оценки
        bundle.putInt(GradeEditDialogFragment.ARGS_INT_CHOSEN_GRADE_POSITION,
                learnersAndTheirGrades[chosenLearnerPosition].chosenGradePosition);

        //запускаем
        editGrade.setArguments(bundle);
        editGrade.show(getSupportFragmentManager(), "editGradeDialog");
    }


    // - дальше немного не отсортировано -----------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    // обратная связь от диалога оценок GradeEditDialogFragment
    @Override
    public void editGrades(int[] grades, int absTypePoz, int[] chosenTypesNumbers, int lessonPoz) {

        if (chosenLearnerPosition != -1) {
            LessonLearnerAndHisGrades chosenOne = learnersAndTheirGrades[chosenLearnerPosition];

            // меняем списки

            chosenOne.absTypePozNumber = absTypePoz;
            // если стоят оценки
            if (absTypePoz == -1) {
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
            if (chosenOne.gradeId == -1) {
                chosenOne.gradeId = db.createGrade(
                        chosenOne.learnerId,
                        chosenOne.gradesUnits[0].grade,
                        chosenOne.gradesUnits[1].grade,
                        chosenOne.gradesUnits[2].grade,
                        graduationSettings.answersTypes[chosenOne.gradesUnits[0].gradeTypePoz].id,
                        graduationSettings.answersTypes[chosenOne.gradesUnits[1].gradeTypePoz].id,
                        graduationSettings.answersTypes[chosenOne.gradesUnits[2].gradeTypePoz].id,
                        (absTypePoz == -1) ? (-1) : (graduationSettings.absentTypes[absTypePoz].id),
                        lessonBaseData.subjectId, lessonBaseData.lessonDate, lessonBaseData.lessonNumber
                );
            } else {

                if (db == null)
                    db = new DataBaseOpenHelper(this);

                // если все поля нулевые удаляем оценку
                if (chosenOne.gradesUnits[0].grade == 0 && chosenOne.gradesUnits[1].grade == 0 &&
                        chosenOne.gradesUnits[2].grade == 0 && absTypePoz == -1) {
                    db.removeGrade(chosenOne.gradeId);
                    chosenOne.gradeId = -1;
                } else
                    db.editGrade(chosenOne.gradeId,
                            chosenOne.gradesUnits[0].grade,
                            chosenOne.gradesUnits[1].grade,
                            chosenOne.gradesUnits[2].grade,
                            graduationSettings.answersTypes[chosenOne.gradesUnits[0].gradeTypePoz].id,
                            graduationSettings.answersTypes[chosenOne.gradesUnits[1].gradeTypePoz].id,
                            graduationSettings.answersTypes[chosenOne.gradesUnits[2].gradeTypePoz].id,
                            (absTypePoz == -1) ? (-1) : (graduationSettings.absentTypes[absTypePoz].id));
            }

            // ставим выбранной следующую оценку
            chosenOne.chosenGradePosition = (chosenOne.chosenGradePosition + 1) % 3;

            // обновляем данные ученика
            outView.updateLearner(chosenLearnerPosition, grades, chosenOne.chosenGradePosition, chosenOne.absTypePozNumber);
            // выводим графику
            outView.setNewScaleParams(multiplier, new PointF(xAxisPXOffset, yAxisPXOffset));

        }
        // убираем выбор с ученика
        chosenLearnerPosition = -1;
    }

    @Override
    public void allowUserStartGradesDialog() {
    }

    @Override
    public LearnersAndGradesActivity.GradeUnit getLessonGrades(int lessonNumber) {
        return null;
    }


    // обратная связь от активности LesonList (срабатывает раньше onResume)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (LessonListActivity.RESULT_BACK == resultCode) {

            // пользователь вернулся с активности в которой можно редактировать оценки
            // значит надо подгрузить их из бд
            if (db == null)
                db = new DataBaseOpenHelper(this);
            getGradesFromDB();

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

        // имя класса
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
            result.cabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndexOrThrow(
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID
            ));
            // получаем из завмсимости id предмета
            result.subjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndexOrThrow(
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID
            ));
            attitudeCursor.close();

            // получаем предмет
            Cursor subjectCursor = db.getSubjectById(result.subjectId);
            subjectCursor.moveToFirst();
            // получаем имя предмета
            result.subjectName = subjectCursor.getString(subjectCursor.getColumnIndexOrThrow(SchoolContract.TableSubjects.COLUMN_NAME));
            // получаем id класса
            result.learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndexOrThrow(
                    SchoolContract.TableSubjects.KEY_CLASS_ID
            ));
            subjectCursor.close();

            // получаем имя класа
            Cursor learnersClass = db.getLearnersClases(result.learnersClassId);
            learnersClass.moveToFirst();
            result.className = learnersClass.getString(learnersClass.getColumnIndexOrThrow(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            learnersClass.close();

            // получаем имя кабинета
            Cursor cabinetNameCursor = db.getCabinet(result.cabinetId);
            cabinetNameCursor.moveToFirst();
            result.cabinetName = cabinetNameCursor.getString(cabinetNameCursor.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_NAME));
            cabinetNameCursor.close();

            return result;
        }

        private LessonBaseData() {
        }
    }


    // класс содержащий в себе парту
    static class DeskUnit {
        int numberOfPlaces;
        int[] seatingLearnerNumber;// ссылки на учеников сидящих за партой
        float startNoZoomedDeskX;
        float startNoZoomedDeskY;

        DeskUnit(float startNoZoomedDeskX, float startNoZoomedDeskY, int numberOfPlaces) {
            this.startNoZoomedDeskX = startNoZoomedDeskX;
            this.startNoZoomedDeskY = startNoZoomedDeskY;
            this.numberOfPlaces = numberOfPlaces;
            this.seatingLearnerNumber = new int[numberOfPlaces];

            // заполняем места на партах пустыми учениками
            Arrays.fill(seatingLearnerNumber, -1);
        }
    }


    // класс для хранения ученика и его оценок
    static class LessonLearnerAndHisGrades {

        // параметры ученика
        long learnerId;
        String firstName;
        String secondName;

        // id оценки
        long gradeId;
        // номер текущей оценки
        int chosenGradePosition = 0;
        // массив оценок
        LessonListActivity.LessonListLearnerAndGradesData.GradeUnit[] gradesUnits;
        // тип пропуска (может быть -1)
        int absTypePozNumber;

        LessonLearnerAndHisGrades(long learnerId, String firstName, String secondName,
                                  long gradeId, LessonListActivity.LessonListLearnerAndGradesData.GradeUnit[] gradesUnits,
                                  int absTypePozNumber
        ) {
            this.learnerId = learnerId;
            this.firstName = firstName;
            this.secondName = secondName;
            this.gradeId = gradeId;
            this.gradesUnits = gradesUnits;
            this.absTypePozNumber = absTypePozNumber;
        }


        // заготовка аргументов для диалога оценок
        int[] getGradesArray() {
            int[] result = new int[gradesUnits.length];
            for (int i = 0; i < result.length; i++) result[i] = gradesUnits[i].grade;
            return result;
        }

        int[] getGradesTypesArray() {
            int[] result = new int[gradesUnits.length];
            for (int i = 0; i < result.length; i++) result[i] = gradesUnits[i].gradeTypePoz;
            return result;
        }
    }


    // класс для хранения дз
    static class HomeWorkUnit {

        // id
        long dbId;
        // текст даты
        String title;

        // текст дз
        String text;

        public HomeWorkUnit(long dbId, String title, String text) {
            this.dbId = dbId;
            this.title = title;
            this.text = text;
        }
    }
}

