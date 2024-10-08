package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;
import com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics.LearnersGradesStatisticsActivity;
import com.learning.texnar13.teachersprogect.lessonRedactor.LessonRedactorActivity;
import com.learning.texnar13.teachersprogect.subjectsDialog.SubjectsDialogFragment;
import com.learning.texnar13.teachersprogect.subjectsDialog.SubjectsDialogInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

// todo может использовать graduationSettings из урока


public class LearnersAndGradesActivity extends AppCompatActivity implements CreateLearnerInterface,
        EditLearnerDialogInterface, GradeEditDialogFragment.EditGradeDialogInterface, UpdateTableInterface,
        View.OnTouchListener, SubjectsDialogInterface {// todo разбей меня уже на методы и классы! Мне плохо быть таким большим!

    // ------------ константы ------------

    // тег для логов
    private static final String TAG = "TeachersApp";
    // константа по которой через intent передается id класса
    public static final String CLASS_ID = "classId";


    // ------------ обратная связь активностей ------------

    ActivityResultLauncher<Integer> learnersAndGradesImportLauncher;

    // ------------ данные из таблицы ------------

    // id класса полученное через intent
    static long classId = -1;

    // данные об учениках оценках и уроках
    static DataObject dataLearnersAndGrades;
    // переменная работающего в текущий момент потока
    static GetGradesThread loadGradesThread = null;

    // переменные отвечающие за открытые диалоги
    private static boolean isStartedDialogLearnerEdit = false;// 1 - ученик
    private static boolean isStartedDialogLessonRedactor = false;// 2 - дата
    private static boolean isStartedDialogEditGrade = false;// 3 - оценка
    private static boolean isStartedDialogLearnerCreate = false;// 4 - добавить ученика
    //private static boolean isStartedDialogSubjects = false;// 5 - предмет todo


    // массив с предметами
    static NewSubjectUnit[] subjects;
    // номер выбранного предмета в массиве
    int chosenSubjectPosition;

    // число уроков из бд
    static int maxLessonsCount;
    // названия типов ответов и их id
    static AnswersType[] answersTypes;
    // названия типов пропусков и их id
    static AbsentType[] absentTypes;
    // календарь хранящий в себе отображаемую дату
    static GregorianCalendar viewCalendar = null;
    // текущая дата
    GregorianCalendar currentCalendar;

    // ------------ данные для вывода графики ------------

    // локализованные названия месяцев
    String[] monthsNames;
    // названия месяцев с правильным окончанием
    String[] transformedMonthsNames;
    // размер максимальной оценки
    int maxGrade;

    // view с таблицей
    static LearnersAndGradesTableView learnersAndGradesTableView;

    // текст вызывающий диалог предметов и отображающий текущий предмет
    TextView subjectTextView;


// -------------------------- меню --------------------------

    //раздуваем меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.learners_and_grades_menu, menu);

        // если активность перевернулась считываем старое значение todo проверить
        if (learnersAndGradesTableView != null) {
            menu.findItem(R.id.learners_and_grades_menu_show_all).setChecked(
                    learnersAndGradesTableView.isAllDaysShowed
            );
        }

        return super.onCreateOptionsMenu(menu);
    }

    //назначаем функции меню
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu");
        // кнопка статистики
        menu.findItem(R.id.learners_and_grades_menu_statistics).setOnMenuItemClickListener(menuItem -> {

            // выбран ли урок
            if (chosenSubjectPosition < 0) {
                // создаем диалог работы с предметами
                SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();

                // готоим и передаем массив названий предиметов
                Bundle args = new Bundle();
                String[] subjectsArray = new String[subjects.length];
                for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                    subjectsArray[subjectI] = subjects[subjectI].getSubjectName();
                }
                args.putStringArray(SubjectsDialogFragment.ARGS_STRING_ARRAY_SUBJECTS_NAMES, subjectsArray);
                subjectsDialogFragment.setArguments(args);

                // показываем диалог
                subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment - hello");

            } else {
                // запускаем активность статистики
                Intent intent = new Intent(
                        LearnersAndGradesActivity.this,
                        LearnersGradesStatisticsActivity.class
                );
                // и передаем ему id предмета
                intent.putExtra(
                        LearnersGradesStatisticsActivity.INTENT_SUBJECT_ID,
                        subjects[chosenSubjectPosition].getSubjectId()
                );
                startActivity(intent);
            }
            return true;
        });

        // показывать пустые оценки
        menu.findItem(R.id.learners_and_grades_menu_show_all).setOnMenuItemClickListener(item -> {
            // при нажатии на элемент читаем состояние чекбокса
            if (learnersAndGradesTableView != null) {
                item.setChecked(!item.isChecked());
                learnersAndGradesTableView.isAllDaysShowed = item.isChecked();
                learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
                learnersAndGradesTableView.invalidate();
            }
            return true;
        });

//        // импорт учеников и оценок через excel
//        menu.findItem(R.id.learners_and_grades_menu_learners_import).setOnMenuItemClickListener(item -> {
//            // запускаем активность импрота
//            learnersAndGradesImportLauncher.launch(null);
//            return true;
//        });
//
//        // экспорт учеников и оценок в excel
//        menu.findItem(R.id.learners_and_grades_menu_learners_export).setOnMenuItemClickListener(item -> {
//
//            // приводим данные об учениках
//            ArrayList<String> names = new ArrayList<>();
//            for (NewLearnerAndHisGrades learner : dataLearnersAndGrades.learnersAndHisGrades) {
//                names.add(learner.surname + " " + learner.name);
//            }
//            // и экспортруем их
//            LearnersAndGradesExportHelper.shareLearners(this, names);
//            return true;
//        });

        return super.onPrepareOptionsMenu(menu);
    }


// -------------------------- создание активности --------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // цвет статус бара
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

        // раздуваем layout
        setContentView(R.layout.learners_and_grades_activity);

        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar(findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }


        // кнопка назад в actionBar
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // кнопка назад
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getSupportActionBar().setHomeAsUpIndicator(getResources().getDrawable(R.drawable.__button_back_arrow_blue));
//            if (toolbar.getOverflowIcon() != null)
//                toolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.baseBlue), PorterDuff.Mode.SRC_ATOP);
//        }

        // получаем id класса
        classId = getIntent().getLongExtra(CLASS_ID, -1);
        if (classId == -1) {// выходим если не передан класс
            finish();
        }

        // получаем название класса из бд и ставим в заголовок
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // название класса
        Cursor classCursor = db.getLearnersClases(classId);
        classCursor.moveToFirst();
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(classCursor.getString(classCursor.getColumnIndexOrThrow(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
        classCursor.close();
        // параллельно находим максимальную оценку
        maxGrade = db.getSettingsMaxGrade(1);

        // и максимальное количество уроков
        maxLessonsCount = db.getSettingsTime(1).length;// todo оптимизировать...

        // названия типов ответов
        Cursor typesCursor = db.getGradesTypes();
        answersTypes = new AnswersType[typesCursor.getCount()];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < answersTypes.length; typeI++) {
            typesCursor.moveToPosition(typeI);
            // добавляем новый тип во внутренний список
            answersTypes[typeI] = new AnswersType(
                    typesCursor.getLong(typesCursor.getColumnIndexOrThrow(SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID)),
                    typesCursor.getString(typesCursor.getColumnIndexOrThrow(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE))
            );
        }
        typesCursor.close();

        // названия типов пропусков
        Cursor absCursor = db.getAbsentTypes();
        absentTypes = new AbsentType[absCursor.getCount()];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < absentTypes.length; typeI++) {
            absCursor.moveToPosition(typeI);
            // добавляем новый тип во внутренний список
            absentTypes[typeI] = new AbsentType(
                    absCursor.getLong(absCursor.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID)),
                    absCursor.getString(absCursor.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME)),
                    absCursor.getString(absCursor.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME))

            );
        }
        absCursor.close();


        // находим view с таблицей
        learnersAndGradesTableView = findViewById(R.id.learners_and_grades_activity_table_view);
        // получаем размер максимальной оценки
        if (db.getSettingsAreTheGradesColoredByProfileId(1)) {
            learnersAndGradesTableView.setMaxAnswersCount(maxGrade);
        } else {
            // не раскрашиваем оценки в таблице
            learnersAndGradesTableView.setMaxAnswersCount(-1);
        }
        // назначаем талице обработчик касаний
        learnersAndGradesTableView.setOnTouchListener(this);
        db.close();

        // получаем ориентацию экрана и в зависимости от неё выводим кнопки во view
        learnersAndGradesTableView.isSubjectAndDateInTable =
                this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        // находим кнопку вызова диалога предметов и ставим ей обработчик
        subjectTextView = findViewById(R.id.learners_and_grades_activity_subject_text_button);
        if (subjectTextView != null)
            subjectTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // создаем диалог работы с предметами
                    SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();

                    // готоим и передаем массив названий предиметов и позицию выбранного предмета
                    Bundle args = new Bundle();
                    String[] subjectsArray = new String[subjects.length];
                    for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                        subjectsArray[subjectI] = subjects[subjectI].getSubjectName();
                    }
                    args.putStringArray(SubjectsDialogFragment.ARGS_STRING_ARRAY_SUBJECTS_NAMES, subjectsArray);
                    subjectsDialogFragment.setArguments(args);

                    // показываем диалог
                    subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment - hello");
                }
            });

        // ---- переключение даты ----

        // инициализируем календарь текущей датой если он пуст, если не пуст, то был перевернут экран
        if (viewCalendar == null) {
            viewCalendar = new GregorianCalendar();
            viewCalendar.setTime(new Date());
            viewCalendar.setLenient(false);
            viewCalendar.set(Calendar.MILLISECOND, 0);
            // выствляем первый день, чтобы небыло путанницы с днями при переключении даты
            viewCalendar.set(Calendar.DAY_OF_MONTH, 1);
        }

        // выставляем в таблицу текущую дату
        currentCalendar = new GregorianCalendar();
        currentCalendar.setTime(new Date());
        currentCalendar.setLenient(false);

        // получаем локализованные названия месяцев
        monthsNames = getResources().getStringArray(R.array.months_names);
        transformedMonthsNames = getResources().getStringArray(R.array.months_names_with_ending);
        // текстовое поле под название даты
        final TextView dateText = findViewById(R.id.learners_and_grades_activity_date_text);
        if (dateText != null)
            dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));
        learnersAndGradesTableView.currentDateTitle =
                monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR);

        // кнопки переключения даты
        // кнопка назад
        ImageView previous = findViewById(R.id.learners_and_grades_activity_button_previous);
        if (previous != null)
            previous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // останавливаем поток загрузки оценок и уничтожаем все его данные
                    if (loadGradesThread != null) {
                        loadGradesThread.stopThread();
                        loadGradesThread = null;
                    }

                    // переключаем дату
                    if (viewCalendar.get(Calendar.MONTH) == Calendar.JANUARY) {
                        viewCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
                        viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) - 1);
                    } else {
                        viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) - 1);
                    }
                    if (dateText != null)
                        dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));
                    learnersAndGradesTableView.currentDateTitle =
                            monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR);

                    // получаем оценки и уроки из бд по новой дате
                    getLessonsFromDB();
                    getGradesFromDB();
                }
            });

        // кнопка вперед
        ImageView next = findViewById(R.id.learners_and_grades_activity_button_next);
        if (next != null)
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // останавливаем поток загрузки оценок и уничтожаем все его данные
                    if (loadGradesThread != null) {
                        loadGradesThread.stopThread();
                        loadGradesThread = null;
                    }

                    // переключаем дату
                    if (viewCalendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                        viewCalendar.set(Calendar.MONTH, Calendar.JANUARY);
                        viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) + 1);
                    } else {
                        viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) + 1);
                    }
                    if (dateText != null)
                        dateText.setText(monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR));
                    learnersAndGradesTableView.currentDateTitle =
                            monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR);

                    // получаем оценки и уроки из бд по новой дате
                    getLessonsFromDB();
                    getGradesFromDB();
                }
            });


        if (dataLearnersAndGrades == null) {
            dataLearnersAndGrades = new DataObject();
        }

        // проводим различные проверки, чтобы не перезагружать данные если например экран перевернулся
        if (subjects == null) {

            // получаем предметы
            getSubjectsFromDB();

            // получаем учеников только здесь
            getLearnersFromDB();

            // говорим компоненту отрисовать полученных учеников
            learnersAndGradesTableView.invalidate();

            // получаем оценки и уроки из базы данных (отрисоввываться они будут по handler-у)
            getLessonsFromDB();
            getGradesFromDB();
        } else {
            // раз есть предметы вываодим название выбранного
            // ставим выбор на прошлом предмете
            if (subjects.length != 0) {
                // выводим название предмета
                if (subjectTextView != null)
                    subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());
                learnersAndGradesTableView.currentSubjectTitle = subjects[chosenSubjectPosition].getSubjectName();

            } else { // если проедметов в базе данных нет не выбираем ничего
                chosenSubjectPosition = -1;
                // выводим текст о том, что предмета нет
                if (subjectTextView != null)
                    subjectTextView.setText(getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject));
                learnersAndGradesTableView.currentSubjectTitle = getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject);
            }

            // не пустой ли массив учеников
            if (dataLearnersAndGrades.learnersAndHisGrades == null) {

                // получаем учеников только здесь
                getLearnersFromDB();

                // говорим компоненту отрисовать полученных учеников
                learnersAndGradesTableView.invalidate();

                // получаем оценки и уроки  из базы данных (отрисовываться они будут по handler-у)
                getLessonsFromDB();
                getGradesFromDB();
            } else {
                // если все на месте то просто передаем пересозданным view старые данные
                learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
            }
        }

        // регистрируем обратную связь для импорта учеников и оценок
        learnersAndGradesImportLauncher = registerForActivityResult(new LearnersAndGradesImportActivity.LearnersImportActivityResultContract(), result -> {
            if (result != null) {
                // todo
                Log.e(TAG, "onPrepareOptionsMenu: learnersUnits=" + result.learnersUnits);
            }
        });
    }

    // -------------------------- основные методы --------------------------

    // метод получения предметов из базы данных
    void getSubjectsFromDB() {
        Log.i(TAG, "getSubjectsFromDB");

        // останавливаем поток загрузки и уничтожаем все его данные
        if (loadGradesThread != null) {
            loadGradesThread.stopThread();
            loadGradesThread = null;
        }

        // получаем предметы из базы данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor subjectsCursor = db.getSubjectsByClassId(classId);

        // инициализируем массив предметов
        subjects = new NewSubjectUnit[subjectsCursor.getCount()];
        for (int subjectsIterator = 0; subjectsIterator < subjectsCursor.getCount(); subjectsIterator++) {
            subjectsCursor.moveToNext();
            subjects[subjectsIterator] = new NewSubjectUnit(
                    subjectsCursor.getLong(subjectsCursor.getColumnIndexOrThrow(SchoolContract.TableSubjects.KEY_ROW_ID)),
                    subjectsCursor.getString(subjectsCursor.getColumnIndexOrThrow(SchoolContract.TableSubjects.COLUMN_NAME))
            );
        }

        // ставим выбор на первом предмете
        if (subjects.length != 0) {
            chosenSubjectPosition = 0;
            // выводим название предмета
            if (subjectTextView != null)
                subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());
            learnersAndGradesTableView.currentSubjectTitle = subjects[chosenSubjectPosition].getSubjectName();

        } else { // если проедметов в базе данных нет не выбираем ничего
            chosenSubjectPosition = -1;
            // выводим текст о том, что предмета нет
            if (subjectTextView != null)
                subjectTextView.setText(getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject));
            learnersAndGradesTableView.currentSubjectTitle = getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject);
        }

        // курсор больше не нужен
        subjectsCursor.close();
        db.close();
    }


    // метод получения учеников из базы данных (которые потом передаются во view)
    void getLearnersFromDB() {
        Log.i(TAG, "LearnersAndGradesActivity - getLearnersFromDB");

        // ждем пока копирование переменных завершится
        while (dataLearnersAndGrades.isInCopyProcess) ;

        // запрещаем пользователю редактировать учеников
        isStartedDialogLearnerEdit = true;
        isStartedDialogLearnerCreate = true;


        // получаем учеников из базы данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersCursor = db.getLearnersByClassId(classId);

        // чистим лист от старых учеников
        dataLearnersAndGrades.learnersAndHisGrades = new NewLearnerAndHisGrades[learnersCursor.getCount()];
        // и заполняем новыми
        for (int learnerI = 0; learnerI < dataLearnersAndGrades.learnersAndHisGrades.length; learnerI++) {
            learnersCursor.moveToNext();
            // создаем нового ученика
            dataLearnersAndGrades.learnersAndHisGrades[learnerI] = new NewLearnerAndHisGrades(
                    learnersCursor.getLong(learnersCursor.getColumnIndexOrThrow(
                            SchoolContract.TableLearners.KEY_ROW_ID)),
                    learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(
                            SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
                    learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(
                            SchoolContract.TableLearners.COLUMN_SECOND_NAME)),
                    learnersCursor.getString(learnersCursor.getColumnIndexOrThrow(
                            SchoolContract.TableLearners.COLUMN_COMMENT)),
                    new GradeUnit[][]{}
            );
        }
        learnersCursor.close();
        db.close();

        // передаем данные во view (пока только ученики)
        learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);

        //разрешаем редактировать учеников, когда все вывели
        allowUserEditLearners();
    }


    // класс handler-а активности для обращения из сторонних потоков
    static class UpdateTableHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // вывод оценок по завершении получения данных
            if (msg.what == 10) {
                // обновляем данные в таблице
                learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
                // перерисоввываем окно
                learnersAndGradesTableView.invalidate();

                // разрешаем менять оценки
                dataLearnersAndGrades.chosenLearnerPosition = -1;
                dataLearnersAndGrades.chosenDayPosition = -1;
                dataLearnersAndGrades.chosenLessonPosition = -1;
            }
        }
    }


    // handler активности для обращения из сторонних потоков
    UpdateTableHandler updateTableHandler = new UpdateTableHandler();

    // парсинг дат из календаря
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // сторонний поток для загрузки оценок
    class GetGradesThread extends Thread {
        private boolean runFlag;

        void stopThread() {
            Log.i(TAG, "LoadGradesThread-stoppedEarly");
            runFlag = false;
        }

        @Override
        public void run() {
            Log.i(TAG, "LoadGradesThread-Start");
            // разрешаем подгрузку данных
            runFlag = true;


            // ---- инициализируем ----

            // копируем оригинальный обьект с данными чтобы пока изменять только копию
            DataObject copy = new DataObject();
            copy.isInCopyProcess = false;
            copy.chosenLearnerPosition = -1;
            copy.chosenDayPosition = -1;
            copy.chosenLessonPosition = -1;

            // выставляем дату за которую берутся оценки
            GregorianCalendar loadGradesTempCalendar = new GregorianCalendar();
            loadGradesTempCalendar.set(
                    viewCalendar.get(Calendar.YEAR),
                    viewCalendar.get(Calendar.MONTH),
                    viewCalendar.get(Calendar.DAY_OF_MONTH)
            );
            copy.yearAndMonth = loadGradesTempCalendar.getTime();

            // уроки
            copy.lessonsUnits = dataLearnersAndGrades.lessonsUnits;


            // не можем начать копирование пока старый поток все не закончит
            while (dataLearnersAndGrades.isInCopyProcess) ;

            // начинаем копирование
            dataLearnersAndGrades.isInCopyProcess = true;

            // создаем копию учеников с пустыми оценками todo обязательно ли клонировать самих учеников?
            copy.learnersAndHisGrades = new NewLearnerAndHisGrades[dataLearnersAndGrades.learnersAndHisGrades.length];
            for (int learnerI = 0; learnerI < dataLearnersAndGrades.learnersAndHisGrades.length; learnerI++) {
                copy.learnersAndHisGrades[learnerI] = new NewLearnerAndHisGrades(
                        dataLearnersAndGrades.learnersAndHisGrades[learnerI].id,
                        "" + dataLearnersAndGrades.learnersAndHisGrades[learnerI].name,
                        "" + dataLearnersAndGrades.learnersAndHisGrades[learnerI].surname,// тут мы именно клонируем строки
                        dataLearnersAndGrades.learnersAndHisGrades[learnerI].comment,
                        new GradeUnit[loadGradesTempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][maxLessonsCount]
                );
            }

            // завершаем копирование данных
            dataLearnersAndGrades.isInCopyProcess = false;


            // ---- получаем данные ----

            // если не выбран предмет возвращаем пустые ячейки
            if (chosenSubjectPosition >= 0) {

                // открываем бд
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());

                // по ученикам
                for (int learnerI = 0; learnerI < copy.learnersAndHisGrades.length; learnerI++) {
                    // по дням
                    for (int dayI = 0; dayI < copy.learnersAndHisGrades[learnerI].learnerGrades.length; dayI++) {

                        if (!runFlag) {// если была команда закрыть поток без подгрузки
                            // выходим  из метода подгрузки
                            return;
                        }// todo надо чтобы новый поток ждал завенршения старого прежде чем начать загрузку данных


                        // получаем оценки(уроки) этого ученика за этот день
                        loadGradesTempCalendar.set(Calendar.DAY_OF_MONTH, dayI + 1);
                        Cursor allDayGrades = db.getGradesByLearnerIdSubjectDateAndLessonsPeriod(
                                copy.learnersAndHisGrades[learnerI].id,
                                subjects[chosenSubjectPosition].getSubjectId(),
                                dateFormat.format(loadGradesTempCalendar.getTime()),
                                0,
                                copy.learnersAndHisGrades[learnerI].learnerGrades[dayI].length-1
                        );


                        while (allDayGrades.moveToNext()) {
                            if (!runFlag) {// если была команда закрыть поток без подгрузки
                                // выходим  из метода подгрузки
                                allDayGrades.close();
                                return;
                            }

                            // id оценки
                            long gradesId = allDayGrades.getLong(allDayGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID));
                            // номер урока
                            int lessonPoz = allDayGrades.getInt(allDayGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER));

                            // оценки
                            int[] grades = new int[3];
                            for (int gradeI = 0; gradeI < 3; gradeI++) {
                                grades[gradeI] = allDayGrades.getInt(allDayGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[gradeI]));
                            }

                            // номера типов оценок
                            int[] gradesTypesIndexes = new int[3];
                            for (int gradesI = 0; gradesI < 3; gradesI++) {
                                int typeId = allDayGrades.getInt(allDayGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[gradesI]));
                                for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                                    if (typeId == answersTypes[typeI].id) {
                                        gradesTypesIndexes[gradesI] = typeI;
                                        break;
                                    }
                                }
                            }

                            // номер типа пропуска
                            int absTypePoz = -1;
                            if (!allDayGrades.isNull(allDayGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID))) {
                                int absTypeId = allDayGrades.getInt(allDayGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID));
                                for (int absI = 0; absI < absentTypes.length; absI++) {
                                    if (absTypeId == absentTypes[absI].id) {
                                        absTypePoz = absI;
                                        break;
                                    }
                                }
                            }

                            // если оценки здесь еще не стояли, создаем их
                            if (copy.learnersAndHisGrades[learnerI].learnerGrades[dayI][lessonPoz] == null)
                                copy.learnersAndHisGrades[learnerI].learnerGrades[dayI][lessonPoz] = new GradeUnit(
                                        grades,
                                        gradesId,
                                        gradesTypesIndexes,
                                        absTypePoz
                                );
                        }

                        allDayGrades.close();
                    }
                }
            }

            // если этот поток не был прерван ранее
            if (runFlag) {
                // присваиваем измененную копию
                dataLearnersAndGrades = copy;
                // говорим активности что закончили подгрузку
                updateTableHandler.sendEmptyMessage(10);

                // разрешаем диалог оценок
                isStartedDialogEditGrade = false;
            }

            Log.i(TAG, "threadRunEnded");
        }
    }

    // метод получения оценок из бд
    void getGradesFromDB() {
        Log.i(TAG, "LearnersAndGradesActivity - getGradesFromDB");

        // класс потока загрузки
        loadGradesThread = new GetGradesThread();

        // запрещаем изменение оценок
        isStartedDialogEditGrade = true;

        // запускаем поток
        loadGradesThread.setName("loadGradesThread - hello");
        loadGradesThread.start();
    }

    // метод получения уроков из бд
    void getLessonsFromDB() {
        // инициализируем уроки по дням в null
        dataLearnersAndGrades.lessonsUnits = new LessonUnit[viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][maxLessonsCount];

        // если не выбран предмет возвращаем пустые ячейки
        if (chosenSubjectPosition >= 0) {

            // открываем бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());

            // по дням
            for (int dayI = 0; dayI < dataLearnersAndGrades.lessonsUnits.length; dayI++) {
                // получаем уроки за день (пустой урок null)
                String checkDate = String.format(new Locale("en"), "%04d-%02d-%02d", viewCalendar.get(Calendar.YEAR), viewCalendar.get(Calendar.MONTH) + 1, dayI + 1);
                Cursor baseLessons = db.getSubjectAndTimeCabinetAttitudesByDateAndLessonNumbersPeriod(
                        subjects[chosenSubjectPosition].getSubjectId(),
                        checkDate,
                        0,
                        maxLessonsCount - 1
                );

                while (baseLessons.moveToNext()) {
                    int lessonNumber = baseLessons.getInt(baseLessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER));
                    long lessonId = baseLessons.getLong(baseLessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID));
                    long subjectId = baseLessons.getLong(baseLessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
                    long cabinetId = baseLessons.getLong(baseLessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
                    String lessonDate = baseLessons.getString(baseLessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE));
                    int repeat = baseLessons.getInt(baseLessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT));

                    // получаем дз
                    Cursor comment = db.getLessonCommentsByDateAndLesson(lessonId, checkDate);
                    if (comment.moveToFirst()) {
                        // записываем полученный урок (если его еще не было)
                        if (dataLearnersAndGrades.lessonsUnits[dayI][lessonNumber] == null)
                            dataLearnersAndGrades.lessonsUnits[dayI][lessonNumber] = new LessonUnit(
                                    lessonId,
                                    subjectId,
                                    cabinetId,
                                    lessonDate,
                                    lessonNumber,
                                    repeat,
                                    comment.getLong(comment.getColumnIndexOrThrow(SchoolContract.TableLessonComment.KEY_ROW_ID)),
                                    comment.getString(comment.getColumnIndexOrThrow(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT))
                            );
                        // todo мне кажется здесь нужна ветка else с заданием домашки в уже созданный урок, наверное по этому я и ловлю null. А может и нет

                    } else {
                        // записываем полученный урок (если его еще не было)
                        if (dataLearnersAndGrades.lessonsUnits[dayI][lessonNumber] == null)
                            dataLearnersAndGrades.lessonsUnits[dayI][lessonNumber] = new LessonUnit(
                                    lessonId,
                                    subjectId,
                                    cabinetId,
                                    lessonDate,
                                    lessonNumber,
                                    repeat
                            );
                    }
                }

                baseLessons.close();
            }
        }
    }


// ------------------ обработка касаний таблицы ------------------

    // точка начала касания
    PointF downPointF = new PointF();
    // время начала касания
    long startTouchTime = 0;

    // нет касания, нажатие или перемещение
    static final int TOUCH_NONE = 0;
    static final int TOUCH_DOWN = 1;
    static final int TOUCH_MOVE = 2;
    int mode = TOUCH_NONE;

    // метод отвечающий за обработку касания таблицы
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:// поставили первый палец
                // режим нажатия
                mode = TOUCH_DOWN;
                // считываем координаты
                downPointF.x = motionEvent.getX();
                downPointF.y = motionEvent.getY();
                // и время нажатия
                startTouchTime = System.currentTimeMillis();
                //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_DOWN: x=" + downPointF.x + " y=" + downPointF.y + " startTouchTime=" + startTouchTime);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == TOUCH_DOWN) {
                    // --- движение при режиме нажатия ---
                    // находим расстояние от точки начала касания
                    if (Math.sqrt(
                            Math.pow(motionEvent.getX() - downPointF.x, 2) + Math.pow(motionEvent.getY() - downPointF.y, 2)
                    ) > pxFromDp(20)) {
                        // режим движения
                        mode = TOUCH_MOVE;
                        // режим движения только начался по этому заново считываем координаты
                        downPointF.x = motionEvent.getX();
                        downPointF.y = motionEvent.getY();
                        //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_MOVE(TOUCH_MOVE->active): x=" + downPointF.x + " y=" + downPointF.y);

                    }
                    //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_MOVE(TOUCH_DOWN): x=" + downPointF.x + " y=" + downPointF.y);

                } else {
                    // --- движение при режиме движения ---
                    ((LearnersAndGradesTableView) view).setMove(downPointF, motionEvent.getX(), motionEvent.getY());
                    //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_MOVE(TOUCH_MOVE): x=" + downPointF.x + " y=" + downPointF.y);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mode == TOUCH_DOWN) {
                    // --- было только нажатие ---
                    // проверяем долгое ли было нажатие
                    //boolean isLongClick = System.currentTimeMillis() - startTouchTime >= 500;


                    // отправляем нажатие и получаем индексы нажатия в таблице (номер ученика, дня, урока)
                    int tableTouchPoz;
                    int touchDataLearnerNumber;
                    int touchDataDayNumber;
                    int touchDataLessonNumber;
                    {
                        int[] touchData = ((LearnersAndGradesTableView) view).touch(
                                downPointF,
                                true // isLongClick
                        );
                        tableTouchPoz = touchData[0];
                        touchDataLearnerNumber = touchData[1];
                        touchDataDayNumber = touchData[2];
                        touchDataLessonNumber = touchData[3];
                    }
                    // вызываем перерисовку, чтобы view нарисовал нажатие на клетки
                    learnersAndGradesTableView.invalidate();

                    switch (tableTouchPoz) {
                        default: // 0 - мимо
                            break;
                        case 1: {// 1 - ученик


                            // чтобы не было нескольких вызовов одновременно и перекрытий
                            if (!isStartedDialogLearnerEdit) {

                                // чтобы не было нескольких вызовов одновременно выбираем ученика
                                isStartedDialogLearnerEdit = true;

                                dataLearnersAndGrades.chosenLearnerPosition = touchDataLearnerNumber;// todo нужна ли эта строка

                                // останавливаем поток загрузки оценок и уничтожаем все его данные
                                if (loadGradesThread != null) {
                                    loadGradesThread.stopThread();
                                    loadGradesThread = null;
                                }

                                // создаем диалог
                                LearnerEditDialogFragment editDialog = new LearnerEditDialogFragment();

                                // создаем обьект с данными для диалога
                                Bundle args = new Bundle();
                                args.putString(LearnerEditDialogFragment.ARGS_LEARNER_NAME,
                                        dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].name);
                                args.putString(LearnerEditDialogFragment.ARGS_LEARNER_LAST_NAME,
                                        dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].surname);
                                args.putString(LearnerEditDialogFragment.ARGS_LEARNER_COMMENT,
                                        dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].comment);


                                // передаем данные диалогу
                                editDialog.setArguments(args);

                                // показываем диалог
                                editDialog.show(getSupportFragmentManager(), "editLearnerDialog - hello");
                            }
                            break;
                        }
                        case 2: {// 2 - дата
                            // чтобы не было нескольких вызовов одновременно и перекрытий
                            if (!isStartedDialogLessonRedactor) {
                                // если есть предметы
                                if (subjects.length > 0) {

                                    // отключаем нажатия чтобы не было нескольких вызовов одновременно
                                    isStartedDialogLessonRedactor = true;

                                    dataLearnersAndGrades.chosenLearnerPosition = -1;
                                    dataLearnersAndGrades.chosenDayPosition = touchDataDayNumber;
                                    dataLearnersAndGrades.chosenLessonPosition = touchDataLessonNumber;// todo нужна ли эта строка

                                    // показываем редактор урока
                                    Intent intent = new Intent(this, LessonRedactorActivity.class);
                                    if (dataLearnersAndGrades.lessonsUnits[touchDataDayNumber][touchDataLessonNumber] == null) {
                                        intent.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, -1L);
                                        // передаем id класса чтобы он выставился по умолчанию
                                        intent.putExtra(LessonRedactorActivity.LESSON_CLASS_ID, classId);

                                    } else {
                                        intent.putExtra(LessonRedactorActivity.LESSON_ATTITUDE_ID, dataLearnersAndGrades.lessonsUnits[touchDataDayNumber][touchDataLessonNumber].lessonId);
                                    }
                                    intent.putExtra(LessonRedactorActivity.LESSON_CHECK_DATE, String.format(Locale.getDefault(), "%04d-%02d-%02d", viewCalendar.get(Calendar.YEAR), viewCalendar.get(Calendar.MONTH) + 1, touchDataDayNumber + 1));
                                    intent.putExtra(LessonRedactorActivity.LESSON_NUMBER, touchDataLessonNumber);
                                    startActivityForResult(intent, LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID);

                                }
                            }
                            break;
                        }
                        case 3: {// 3 - оценка
                            // чтобы не было нескольких вызовов одновременно и перекрытий
                            if (!isStartedDialogEditGrade) {
                                // если не выбран предмет то не можем редактировать оценки
                                if (chosenSubjectPosition < 0) {
                                    // создаем диалог работы с предметами
                                    SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();

                                    // готоим и передаем массив названий предиметов
                                    Bundle args = new Bundle();
                                    String[] subjectsArray = new String[subjects.length];
                                    for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                                        subjectsArray[subjectI] = subjects[subjectI].getSubjectName();
                                    }
                                    args.putStringArray(SubjectsDialogFragment.ARGS_STRING_ARRAY_SUBJECTS_NAMES, subjectsArray);
                                    subjectsDialogFragment.setArguments(args);

                                    // показываем диалог
                                    subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment - hello");

                                } else {
                                    // ставим полученные координаты оценки как выбранные тем самым отключая нажатия
                                    isStartedDialogEditGrade = true;

                                    dataLearnersAndGrades.chosenLearnerPosition = touchDataLearnerNumber;
                                    dataLearnersAndGrades.chosenDayPosition = touchDataDayNumber;
                                    dataLearnersAndGrades.chosenLessonPosition = touchDataLessonNumber;// todo сделать эти переменные для каждого диалога свои

                                    // вызываем диалог изменения оценок
                                    GradeEditDialogFragment editGrade = new GradeEditDialogFragment();
                                    // параметры
                                    Bundle bundle = new Bundle();

                                    // имя ученика
                                    bundle.putString(
                                            GradeEditDialogFragment.ARGS_LEARNER_NAME,
                                            dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].surname + " " + dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].name
                                    );

                                    // названия типов оценок в массиве
                                    String[] stringTypes = new String[answersTypes.length];
                                    for (int typeI = 0; typeI < answersTypes.length; typeI++) {
                                        stringTypes[typeI] = answersTypes[typeI].typeName;
                                    }
                                    bundle.putStringArray(GradeEditDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY, stringTypes);

                                    // если оценки еще нет в базе
                                    int[] grades;
                                    int[] gradesTypesPoz;
                                    int absPoz;
                                    if (dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].learnerGrades[touchDataDayNumber][touchDataLessonNumber] == null) {
                                        grades = new int[3];
                                        gradesTypesPoz = new int[3];
                                        absPoz = -1;
                                    } else {
                                        grades = dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].learnerGrades[touchDataDayNumber][touchDataLessonNumber].grades.clone();
                                        gradesTypesPoz = dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].learnerGrades[touchDataDayNumber][touchDataLessonNumber].gradesTypesIndexes.clone();
                                        absPoz = dataLearnersAndGrades.learnersAndHisGrades[touchDataLearnerNumber].learnerGrades[touchDataDayNumber][touchDataLessonNumber].absTypePoz;
                                    }
                                    // массив оценок
                                    bundle.putIntArray(GradeEditDialogFragment.ARGS_INT_GRADES_ARRAY, grades);
                                    // массив с номерами выбранных типов
                                    bundle.putIntArray(GradeEditDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY, gradesTypesPoz);
                                    // выбранный номер пропуска
                                    bundle.putInt(GradeEditDialogFragment.ARGS_INT_GRADES_ABSENT_TYPE_NUMBER, absPoz);
                                    // длинные названия типов пропусков в массиве
                                    String[] stringLongAbsTypes = new String[absentTypes.length];
                                    for (int typeI = 0; typeI < absentTypes.length; typeI++) {
                                        stringLongAbsTypes[typeI] = absentTypes[typeI].absTypeLongName;
                                    }
                                    bundle.putStringArray(
                                            GradeEditDialogFragment.ARGS_STRING_ABSENT_TYPES_LONG_NAMES_ARRAY,
                                            stringLongAbsTypes
                                    );

                                    // максимальный размер оценки
                                    bundle.putInt(GradeEditDialogFragment.ARGS_INT_MAX_GRADE, maxGrade);
                                    // максимальное количество уроков
                                    bundle.putInt(GradeEditDialogFragment.ARGS_INT_MAX_LESSONS_COUNT, maxLessonsCount);

                                    // текущая дата в строке
                                    bundle.putString(GradeEditDialogFragment.ARGS_STRING_CURRENT_DATE,
                                            (touchDataDayNumber + 1) + " " + transformedMonthsNames[viewCalendar.get(Calendar.MONTH)]
                                    );

                                    // выбранный номер урока
                                    bundle.putInt(GradeEditDialogFragment.ARGS_INT_LESSON_NUMBER, touchDataLessonNumber);

                                    //запускаем
                                    editGrade.setArguments(bundle);
                                    editGrade.show(getSupportFragmentManager(), "editGradeDialog - hello");// illegalState - вызов в неподходящее время
                                }
                            }
                            break;
                        }
                        case 4: {// 4 - добавить ученика
                            // чтобы не было нескольких вызовов одновременно и перекрытий
                            if (!isStartedDialogLearnerCreate) {

                                // запрещаем пока работать с учениками
                                isStartedDialogLearnerCreate = true;

                                dataLearnersAndGrades.chosenLearnerPosition = 0;// todo нужна ли эта строка

                                // создаем диалог добавления ученика
                                LearnerCreateDialogFragment createLearnerDialog = new LearnerCreateDialogFragment();
                                //показываем диалог
                                createLearnerDialog.show(getSupportFragmentManager(), "createLearnerDialog");
                            }
                            break;
                        }
                        case 5: { // 5 - предмет
                            // создаем диалог работы с предметами
                            SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();

                            // готоим и передаем массив названий предиметов и позицию выбранного предмета
                            Bundle args = new Bundle();
                            String[] subjectsArray = new String[subjects.length];
                            for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                                subjectsArray[subjectI] = subjects[subjectI].getSubjectName();
                            }
                            args.putStringArray(SubjectsDialogFragment.ARGS_STRING_ARRAY_SUBJECTS_NAMES, subjectsArray);
                            subjectsDialogFragment.setArguments(args);

                            // показываем диалог
                            subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment");
                            break;
                        }
                        case 6: {// 6 - дата назад
                            // останавливаем поток загрузки оценок и уничтожаем все его данные
                            if (loadGradesThread != null) {
                                loadGradesThread.stopThread();
                                loadGradesThread = null;
                            }

                            // переключаем дату
                            if (viewCalendar.get(Calendar.MONTH) == Calendar.JANUARY) {
                                viewCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
                                viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) - 1);
                            } else {
                                viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) - 1);
                            }
                            learnersAndGradesTableView.currentDateTitle =
                                    monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR);

                            // получаем оценки и уроки из бд по новой дате
                            getLessonsFromDB();
                            getGradesFromDB();
                            break;
                        }
                        case 7: {// 7 - дата вперед
                            // останавливаем поток загрузки оценок и уничтожаем все его данные
                            if (loadGradesThread != null) {
                                loadGradesThread.stopThread();
                                loadGradesThread = null;
                            }

                            // переключаем дату
                            if (viewCalendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                                viewCalendar.set(Calendar.MONTH, Calendar.JANUARY);
                                viewCalendar.set(Calendar.YEAR, viewCalendar.get(Calendar.YEAR) + 1);
                            } else {
                                viewCalendar.set(Calendar.MONTH, viewCalendar.get(Calendar.MONTH) + 1);
                            }
                            learnersAndGradesTableView.currentDateTitle =
                                    monthsNames[viewCalendar.get(Calendar.MONTH)] + " " + viewCalendar.get(Calendar.YEAR);

                            // получаем оценки и уроки из бд по новой дате
                            getLessonsFromDB();
                            getGradesFromDB();
                            break;
                        }
                    }


                } else {
                    // --- было движение ---
                    ((LearnersAndGradesTableView) view).endMove();
                    //Log.e(TAG, "LearnersAndGradesActivity.onTouch - ACTION_UP(moveEnded): x=" + downPointF.x + " y=" + downPointF.y);
                }
                // отключаем нажатие
                mode = TOUCH_DOWN;
                break;
        }
        return true;
    }


// -------------------------- обратная связь диалогов --------------------------

    // метод создания ученика вызываемый из диалога LearnerCreateDialogFragment
    @Override
    public void createLearner(String lastName, String name, String comment) {
        Log.i(TAG, "fromDialog-createLearner");

        // останавливаем поток загрузки оценок и уничтожаем все его данные
        if (loadGradesThread != null) {
            loadGradesThread.stopThread();
            loadGradesThread = null;
        }

        // создаем новый массив учеников
        NewLearnerAndHisGrades[] newArray = new NewLearnerAndHisGrades[dataLearnersAndGrades.learnersAndHisGrades.length + 1];
        int oldArrayPoz = 0;

        // первая часть массива
        if (dataLearnersAndGrades.learnersAndHisGrades.length > 0)
            while (lastName.compareTo(dataLearnersAndGrades.learnersAndHisGrades[oldArrayPoz].surname) > 0) {
                newArray[oldArrayPoz] = dataLearnersAndGrades.learnersAndHisGrades[oldArrayPoz];
                oldArrayPoz++;

                if (oldArrayPoz == dataLearnersAndGrades.learnersAndHisGrades.length)
                    break;
            }
        // создаем ученика в базе данных и
        // добавляем его в локальный список
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        newArray[oldArrayPoz] = new NewLearnerAndHisGrades(
                db.createLearner(lastName, name, comment, classId),
                name,
                lastName,
                comment,
                new GradeUnit[viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][maxLessonsCount] // заполняем все оценки в NULL
        );
        db.close();
        // вторая часть массива
        while (oldArrayPoz < dataLearnersAndGrades.learnersAndHisGrades.length) {
            newArray[oldArrayPoz + 1] = dataLearnersAndGrades.learnersAndHisGrades[oldArrayPoz];
            oldArrayPoz++;
        }
        // меняем список на новый
        dataLearnersAndGrades.learnersAndHisGrades = newArray;


        // выводим изменившихся учеников во view
        learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
        learnersAndGradesTableView.invalidate();

        // разрешаем редактировать учеников
        allowUserEditLearners();
    }

    @Override
    public void allowUserEditLearners() {
        Log.i(TAG, "fromDialog-allowUserEditLearners");

        // разрешаем редактировать учеников
        isStartedDialogLearnerEdit = false;
        isStartedDialogLearnerCreate = false;
    }

    // метод переименования ученика вызываемый из диалога LearnerEditDialogFragment
    @Override
    public void editLearner(String lastName, String name, String comment) {
        Log.i(TAG, "fromDialog-editLearner");

//        // останавливаем поток загрузки оценок и уничтожаем все его данные
//        if (loadGradesThread != null) {
//            loadGradesThread.stopThread();
//            loadGradesThread = null;
//        } останавливать поток можно только если после изменений его продолжишь например перезагрузкой учеников

        // на всякий случай
        if (dataLearnersAndGrades.chosenLearnerPosition != -1) {

            // меняем имя и фамилию ученика в базе данных
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            db.setLearnerNameAndLastName(
                    dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].id,
                    name,
                    lastName,
                    comment
            );
            db.close();

            // меняем локальный список учеников todo сортировка
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].name = name;
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].surname = lastName;
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].comment = comment;

            // выводим изменившихся учеников во view
            learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
            learnersAndGradesTableView.invalidate();

            // разрешаем редактировать учеников
            allowUserEditLearners();
        }
    }

    // метод удаления ученика вызываемый из диалога LearnerEditDialogFragment
    @Override
    public void removeLearner() {
        Log.i(TAG, "fromDialog-removeLearner");

//        // останавливаем поток загрузки оценок и уничтожаем все его данные
//        if (loadGradesThread != null) {
//            loadGradesThread.stopThread();
//            loadGradesThread = null;
//        }

        // удаляем ученика из базы данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.deleteLearner(dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].id);
        db.close();

        // удаляем ученика из списка
        dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition] = null;
        NewLearnerAndHisGrades[] newArray = new NewLearnerAndHisGrades[dataLearnersAndGrades.learnersAndHisGrades.length - 1];
        int newArrayPoz = 0;
        for (int i = 0; i < dataLearnersAndGrades.learnersAndHisGrades.length; i++) {
            if (i != dataLearnersAndGrades.chosenLearnerPosition) {
                newArray[newArrayPoz] = dataLearnersAndGrades.learnersAndHisGrades[i];
                newArrayPoz++;
            }
        }
        dataLearnersAndGrades.learnersAndHisGrades = newArray;


        // выводим изменившихся учеников во view
        learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
        learnersAndGradesTableView.invalidate();

        // разрешаем редактировать учеников
        allowUserEditLearners();
    }

    // метод редактирования оценки ученика из диалога GradeEditDialogFragment
    @Override
    public void editGrades(int[] grades, int absTypePoz, int[] chosenTypesNumbers, int lessonPoz) {
        // проверяем выбрана ли все еще дата
        if (dataLearnersAndGrades.chosenLearnerPosition != -1 && dataLearnersAndGrades.chosenDayPosition != -1 && lessonPoz != -1) {

            DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // копируем ссылку для удобства
            GradeUnit tempCell = dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                    learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz];


            // если оценок еще нет в базе
            if (tempCell == null) {
                // если оценка не нулевая то создаем ее
                if (grades[0] != 0 || grades[1] != 0 || grades[2] != 0 || absTypePoz != -1) {
                    tempCell = new GradeUnit(
                            grades,
                            db.createGrade(
                                    dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].id,
                                    grades[0],
                                    grades[1],
                                    grades[2],
                                    answersTypes[chosenTypesNumbers[0]].id,
                                    answersTypes[chosenTypesNumbers[1]].id,
                                    answersTypes[chosenTypesNumbers[2]].id,
                                    (absTypePoz == -1) ? absTypePoz : absentTypes[absTypePoz].id,
                                    subjects[chosenSubjectPosition].getSubjectId(),
                                    String.format(Locale.getDefault(), "%04d-%02d-%02d", viewCalendar.get(Calendar.YEAR), viewCalendar.get(Calendar.MONTH) + 1, dataLearnersAndGrades.chosenDayPosition + 1),
                                    lessonPoz
                            ),
                            chosenTypesNumbers,
                            absTypePoz
                    );
                }
            } else {// иначе запись есть значит меняем текущую
                // если все оценки пустые, удаляем
                if (grades[0] == 0 && grades[1] == 0 && grades[2] == 0 && absTypePoz == -1) {
                    // из бд
                    db.removeGrade(tempCell.gradeId);
                    // и из списка
                    tempCell = null;
                } else {

                    // сохраняем оценки в массив
                    tempCell.grades = grades;
                    // типы
                    tempCell.gradesTypesIndexes = chosenTypesNumbers;
                    // отсутствие
                    tempCell.absTypePoz = absTypePoz;

                    // не нулевые меняем в бд
                    db.editGrade(
                            tempCell.gradeId,
                            grades[0],
                            grades[1],
                            grades[2],
                            answersTypes[chosenTypesNumbers[0]].id,
                            answersTypes[chosenTypesNumbers[1]].id,
                            answersTypes[chosenTypesNumbers[2]].id,
                            (absTypePoz == -1) ? absTypePoz : absentTypes[absTypePoz].id
                    );
                }
            }

            db.close();

            // после работы копируем ссылку обратно
            dataLearnersAndGrades.learnersAndHisGrades[dataLearnersAndGrades.chosenLearnerPosition].
                    learnerGrades[dataLearnersAndGrades.chosenDayPosition][lessonPoz] = tempCell;

            // выводим изменившиеся данные во view
            learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
            learnersAndGradesTableView.invalidate();
        }
    }

    // метод получения оценок урока из диалога GradeEditDialogFragment
    @Override
    public GradeUnit getLessonGrades(int lessonNumber) {

        GradeUnit temp = null;
        if (dataLearnersAndGrades.chosenLearnerPosition != -1 && dataLearnersAndGrades.chosenDayPosition != -1)
            temp = dataLearnersAndGrades.learnersAndHisGrades[
                    dataLearnersAndGrades.chosenLearnerPosition
                    ].learnerGrades[
                    dataLearnersAndGrades.chosenDayPosition
                    ][lessonNumber];

        if (temp != null) {
            return new GradeUnit(temp);
        } else
            return null;
    }

    // метод разрешающий пользователю изменять оценки из диалога GradeEditDialogFragment
    @Override
    public void allowUserStartGradesDialog() {
        Log.i(TAG, "fromDialog-allowUserEditGrades");

        //разрешаем пользователю менять оценки
        // todo (правда при повороте диалог пересоздастся, а переменная уже будет false, хз че с этим делать)
        isStartedDialogEditGrade = false;

        // и обновляем таблицу, чтобы исчезли синие квадраты
        learnersAndGradesTableView.invalidate();
    }

    // метод выбора текущего предмета вызываемый из диалога SubjectsDialogFragment
    @Override
    public void setSubjectPosition(int position) {
        // выбираем позицию текущего предмета
        chosenSubjectPosition = position;

        // выводим название выбранного урока в текстовое поле
        if (subjectTextView != null)
            subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());
        learnersAndGradesTableView.currentSubjectTitle = subjects[chosenSubjectPosition].getSubjectName();

        // обновляем список оценок и уроков по выбранному предмету
        getLessonsFromDB();
        getGradesFromDB();
    }

    // метод создания предмета вызываемый из диалога SubjectsDialogFragment
    @Override
    public void createSubject(String name, int position) {
        // создаем предмет в базе данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long createdSubjectId = db.createSubject(name, classId);
        db.close();

        // добавляем предмет в список под нужным номером
        NewSubjectUnit[] newSubjectUnits = new NewSubjectUnit[subjects.length + 1];
        for (int subjectI = 0; subjectI < position; subjectI++) {
            newSubjectUnits[subjectI] = subjects[subjectI];
        }
        newSubjectUnits[position] = new NewSubjectUnit(createdSubjectId, name);
        for (int subjectsI = position; subjectsI < subjects.length; subjectsI++) {
            newSubjectUnits[subjectsI + 1] = subjects[subjectsI];
        }
        subjects = newSubjectUnits;

        // выбираем этот предмет и ставим его имя в заголовок
        chosenSubjectPosition = position;
        if (subjectTextView != null)
            subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());
        learnersAndGradesTableView.currentSubjectTitle = subjects[chosenSubjectPosition].getSubjectName();

        // загружаем оценки и уроки по выбранному предмету
        getLessonsFromDB();
        getGradesFromDB();
    }

    // метод удаления предмета вызываемый из диалога SubjectsDialogFragment
    @Override
    public void deleteSubjects(boolean[] deleteList) {

        // удаляем предметы
        ArrayList<Long> deleteId = new ArrayList<>();
        for (int subjectI = 0; subjectI < subjects.length; subjectI++) {
            if (deleteList[subjectI]) {
                deleteId.add(subjects[subjectI].getSubjectId());
                // смотрим не удален ли выбранный предмет
                if (chosenSubjectPosition == subjectI)
                    chosenSubjectPosition = -1;
            }
        }

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.deleteSubjects(deleteId);
        db.close();

        // создаем новый список
        NewSubjectUnit[] newSubjectUnits = new NewSubjectUnit[subjects.length - deleteId.size()];
        int newArrayIterator = 0;
        for (int subjectI = 0; subjectI < subjects.length; subjectI++) {
            if (!deleteList[subjectI]) {
                // сохраняем не удаленные предметы
                newSubjectUnits[newArrayIterator] = subjects[subjectI];

                // и оставляем выбранную позицию
                if (chosenSubjectPosition == subjectI)
                    chosenSubjectPosition = newArrayIterator;

                newArrayIterator++;
            }
        }
        subjects = newSubjectUnits;

        // если текущий предмет был удален
        if (chosenSubjectPosition == -1) {
            // ставим выбор на первом предмете
            if (subjects.length != 0) {
                chosenSubjectPosition = 0;
                // выводим название предмета
                if (subjectTextView != null)
                    subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());
                learnersAndGradesTableView.currentSubjectTitle = subjects[chosenSubjectPosition].getSubjectName();

            } else { // если проедметов в базе данных нет не выбираем ничего
                // выводим текст о том, что предмета нет
                if (subjectTextView != null)
                    subjectTextView.setText(getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject));
                learnersAndGradesTableView.currentSubjectTitle = getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject);
            }
            // получаем оценки и уроки
            getLessonsFromDB();
            getGradesFromDB();
        }
    }

    // метод переименования предметов вызываемый из диалога SubjectsDialogFragment
    @Override
    public void renameSubjects(String[] newSubjectsNames) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        // в цикле переименовываем все предметы в массиве активности
        for (int subjectI = 0; subjectI < subjects.length; subjectI++) {

            subjects[subjectI].setSubjectName(newSubjectsNames[subjectI]);

            // и сохраняем все изменения в базу данных
            db.setSubjectName(
                    subjects[subjectI].getSubjectId(),
                    subjects[subjectI].getSubjectName()
            );
        }
        db.close();

        // сортируем текущий список (пузырьком)
        for (int out = subjects.length - 1; out > 0; out--) {
            for (int in = 0; in < out; in++) {
                if (subjects[in].getSubjectName().compareTo(subjects[in + 1].getSubjectName()) > 0) {
                    if (chosenSubjectPosition == in) {
                        chosenSubjectPosition = in + 1;
                    } else if (chosenSubjectPosition == in + 1) {
                        chosenSubjectPosition = in;
                    }
                    NewSubjectUnit temp = subjects[in + 1];
                    subjects[in + 1] = subjects[in];
                    subjects[in] = temp;
                }
            }
        }

        // обновляем надпись на тексте с названием предмета
        if (subjects.length != 0) {
            // выводим название предмета
            if (subjectTextView != null)
                subjectTextView.setText(subjects[chosenSubjectPosition].getSubjectName());
            learnersAndGradesTableView.currentSubjectTitle = subjects[chosenSubjectPosition].getSubjectName();

        } else { // если проедметов в базе данных нет не выбираем ничего
            // выводим текст о том, что предмета нет
            if (subjectTextView != null)
                subjectTextView.setText(getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject));
            learnersAndGradesTableView.currentSubjectTitle = getResources().getString(R.string.learners_and_grades_out_activity_text_create_subject);
        }
    }

    // уведомить активность о закрытии диалога
    @Override
    public void onSubjectsDialogClosed() {
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // обратная связь от редактора урока
        if (requestCode == LessonRedactorActivity.LESSON_REDACTOR_RESULT_ID) {

            if (resultCode == LessonRedactorActivity.LESSON_REDACTOR_RESULT_CODE_UPDATE) {

                // получаем уроки и дз по новым зависимостям
                getLessonsFromDB();
                // выводим изменившиеся данные во view
                learnersAndGradesTableView.setData(dataLearnersAndGrades, absentTypes);
            }

            // теперь диалог можно вызвать снова
            isStartedDialogLessonRedactor = false;

            // и перерисовываем таблицу
            learnersAndGradesTableView.invalidate();


        }
    }


    // при выходе из активности
    @Override
    protected void onStop() {
        Log.i(TAG, " onStop");

        // останавливаем поток загрузки оценок и уничтожаем все его данные
        if (loadGradesThread != null) {
            loadGradesThread.stopThread();
            loadGradesThread = null;
        }

        super.onStop();
    }

    //кнопка назад в actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);

    }

    // выход из активности по желанию пользователя
    @Override
    public void onBackPressed() {
        // сначала чистим все данные
        classId = -1;
        if (dataLearnersAndGrades != null) {
            dataLearnersAndGrades.learnersAndHisGrades = null;
            dataLearnersAndGrades.chosenLearnerPosition = -1;
            dataLearnersAndGrades.chosenDayPosition = -1;
            dataLearnersAndGrades.chosenLessonPosition = -1;
            dataLearnersAndGrades = null;
        }
        // переменные отвечающие за открытые диалоги
        isStartedDialogLearnerEdit = false;
        isStartedDialogLessonRedactor = false;
        isStartedDialogEditGrade = false;
        isStartedDialogLearnerCreate = false;

        subjects = null;
        chosenSubjectPosition = -1;
        viewCalendar = null;
        loadGradesThread = null;
        learnersAndGradesTableView = null;

        // а потом выходим
        super.onBackPressed();
    }

    // преобразование зависимой величины в пиксели
    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }


    // класс хранящий в себе оценки одного ученика за 1 урок
    public static class GradeUnit {
        int[] grades;
        long gradeId;
        int[] gradesTypesIndexes;
        int absTypePoz;

        GradeUnit(int[] grades, long gradeId, int[] gradesTypesIndexes, int absTypePoz) {
            this.grades = grades;
            this.gradeId = gradeId;
            this.gradesTypesIndexes = gradesTypesIndexes;
            this.absTypePoz = absTypePoz;
        }

        // конструктор копии
        GradeUnit(GradeUnit other) {
            // никаких проверок на null! в конструктор нельзя предавать null!
            this.grades = other.grades.clone();
            this.gradeId = other.gradeId;
            this.gradesTypesIndexes = other.gradesTypesIndexes.clone();
            this.absTypePoz = other.absTypePoz;
        }
    }
}


// класс который хранит данные об учениках оценках и уроках
class DataObject {

    // сюда положен месяц и год от даты за которую берутся данные
    Date yearAndMonth;

    // массив с названиями пропусков
    //String[] absNames;

    // массив уроков [день, урок]
    LessonUnit[][] lessonsUnits;

    // массив с учениками и их оценками
    NewLearnerAndHisGrades[] learnersAndHisGrades;


    // позиция выбранной оценки/урока/ученика (ученик,день,урок)
    int chosenLearnerPosition = -1;
    int chosenDayPosition = -1;
    int chosenLessonPosition = -1;
    /*
      только ученик -> ученик
      только дата и урок -> нажат урок
      и ученик, и дата , и урок -> клетка с оценкой
    */


    // не занят ли обьект копированием
    boolean isInCopyProcess = false;
}

// данные об одном ученике в таблице
class NewLearnerAndHisGrades {

    // ученик
    long id;
    String name;
    String surname;
    String comment;
    // его оценки        ( [номер дня][номер урока] ).[номер оценки]
    LearnersAndGradesActivity.GradeUnit[][] learnerGrades;

    NewLearnerAndHisGrades(long id, String name, String surname, String comment, LearnersAndGradesActivity.GradeUnit[][] learnerGrades) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.comment = comment;
        this.learnerGrades = learnerGrades;
    }

    // конструктор копии
    // todo При вызове метода clone(), Java проверяет, был ли у объекта интерфейс Cloneable.
    //  Если да — клонирует объект методом clone(), если нет — выкидывает исключение CloneNotSupportedException.
    NewLearnerAndHisGrades(NewLearnerAndHisGrades other) {
        this.id = other.id;
        this.name = "" + other.name;
        this.surname = "" + other.surname;
        this.comment = "" + other.comment;

        this.learnerGrades = new LearnersAndGradesActivity.GradeUnit[other.learnerGrades.length][];
        for (int dayI = 0; dayI < other.learnerGrades.length; dayI++) {
            this.learnerGrades[dayI] = new LearnersAndGradesActivity.GradeUnit[other.learnerGrades[dayI].length];
            for (int lessonI = 0; lessonI < other.learnerGrades[dayI].length; lessonI++) {
                // клонируем обьект
                if (other.learnerGrades[dayI][lessonI] != null)
                    this.learnerGrades[dayI][lessonI] = new LearnersAndGradesActivity.GradeUnit(other.learnerGrades[dayI][lessonI]);
            }
        }
    }
}

// класс для хранения предмета
class NewSubjectUnit {
    private long subjectId;
    private String subjectName;

    NewSubjectUnit(long subjectId, String subjectName) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    long getSubjectId() {
        return subjectId;
    }

    String getSubjectName() {
        return subjectName;
    }

    void setSubjectName(String newSubjectName) {
        this.subjectName = newSubjectName;
    }
}

// класс для одного урока
class LessonUnit {

    long lessonId;
    CommentText commentText;

    // на всякий случай
    long subjectId;
    int lessonNumber;
    // нельзя получить из индесов
    int repeat;
    String lessonDate;
    long cabinetId;


    public LessonUnit(long lessonId, long subjectId, long cabinetId, String lessonDate, int lessonNumber, int repeat, long textId, String lessonCommentText) {
        this.lessonId = lessonId;
        this.subjectId = subjectId;
        this.cabinetId = cabinetId;
        this.lessonDate = lessonDate;
        this.lessonNumber = lessonNumber;
        this.repeat = repeat;
        this.commentText = new CommentText(textId, lessonCommentText);
    }

    public LessonUnit(long lessonId, long subjectId, long cabinetId, String lessonDate, int lessonNumber, int repeat) {
        this.lessonId = lessonId;
        this.subjectId = subjectId;
        this.cabinetId = cabinetId;
        this.lessonDate = lessonDate;
        this.lessonNumber = lessonNumber;
        this.repeat = repeat;
        this.commentText = null;
    }

    static class CommentText {

        long commentTextId;
        String lessonCommentText;

        public CommentText(long commentTextId, String lessonCommentText) {
            this.commentTextId = commentTextId;
            this.lessonCommentText = lessonCommentText;
        }
    }
}


//класс для хранения типов ответов
class AnswersType {
    long id;
    String typeName;

    AnswersType(long id, String typeName) {
        this.id = id;
        this.typeName = typeName;
    }
}

//класс для хранения типов пропусков
class AbsentType {
    long id;
    String absTypeName;
    String absTypeLongName;

    AbsentType(long id, String absTypeName, String absTypeLongName) {
        this.id = id;
        this.absTypeName = absTypeName;
        this.absTypeLongName = absTypeLongName;
    }
}