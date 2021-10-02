package com.learning.texnar13.teachersprogect.lessonRedactor;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.subjectsDialog.SubjectsDialogFragment;
import com.learning.texnar13.teachersprogect.subjectsDialog.SubjectsDialogInterface;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;
import com.yandex.mobile.ads.AdSize;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class LessonRedactorActivity extends FragmentActivity implements SubjectsDialogInterface {


    // id передаваемых данных (трансферные константы)
    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    public static final String LESSON_CHECK_DATE = "lessonDate";
    public static final String LESSON_NUMBER = "lessonNumber";

    // константы результата
    public static final int LESSON_REDACTOR_RESULT_ID = 10;
    public static final int LESSON_REDACTOR_RESULT_CODE_UPDATE = -1;
    public static final int LESSON_REDACTOR_RESULT_CODE_CANCELED = 0;


    //todo предусмотреть вариант если уроков вообще нет. проверка нет ли на этом времени урока. (получать через метод возвращающий дату пересечения с другими уроками)

    // полученное извне время урока
    String checkLessonDate;
    int lessonNumber;

    // массив с текстами времени
    String[] timeTexts;
    // массив с текстами повторов
    String[] repeatTexts;
    // массив с классами
    LearnersClassUnit[] classUnits;
    // массив с кабинетами
    static CabinetUnit[] cabinetUnit1s;

    LessonUnit lessonUnit1;


    // --------------------


    // текстовое поле предмета
    TextView subjectText;
    // текстовое поле дз
    EditText homeworkEdit;
    // индикатор состояния рассадки
    ImageView seatingStateImage;
    // --- спиннеры ---
    Spinner classSpinner;
    Spinner cabinetSpinner;
    Spinner timeSpinner;
    Spinner repeatSpinner;


    // обьект для преобразования календаря в строку
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // отключаем заголовок у диалогового стиля активности
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        // получаем разметку
        setContentView(R.layout.lesson_redactor_activity);

        // ширина диалога по ширине экрана
        findViewById(R.id.activity_lesson_redactor_main_container).getLayoutParams().width =
                getResources().getDisplayMetrics().widthPixels -
                        getResources().getDimensionPixelOffset(R.dimen.forth_margin);

        // --- загрузка рекламы яндекса ---
        com.yandex.mobile.ads.AdView mAdView = findViewById(R.id.activity_lesson_redactor_banner);
        mAdView.setBlockId(getResources().getString(R.string.banner_id_lesson_redactor));
        mAdView.setAdSize(AdSize.BANNER_320x100);
        // Загрузка объявления.
        final com.yandex.mobile.ads.AdRequest adRequest = new com.yandex.mobile.ads.AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // если редактор закроется, ставим по умолчанию сообщение, что ничего не поменялось
        setResult(LESSON_REDACTOR_RESULT_CODE_CANCELED, new Intent());

        // ---------------------------------- получение аргументов ---------------------------------
        // id зависимости 
        long dbAttitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -2);
        if (dbAttitudeId == -2) {
            Toast toast = Toast.makeText(this, "error", Toast.LENGTH_SHORT);
            toast.show();
            finish();
            return;
        }

        // получаем время из intent
        checkLessonDate = getIntent().getStringExtra(LESSON_CHECK_DATE);
        lessonNumber = getIntent().getIntExtra(LESSON_NUMBER, 0);


        // ------------------------------ получение данных урока из бд -----------------------------
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        long tempSubjectId;
        long tempClassId;
        long tempCabinetId;
        int tempRepeat;
        HomeWorkUnit tempHomeworkUnit1;


        // получаем календарь с текущей датой, для выставления в заголовок
        GregorianCalendar calendar = new GregorianCalendar();
        try {
            calendar.setTime(dateFormat.parse(checkLessonDate));
        } catch (ParseException e) {
            e.printStackTrace();
            calendar.setTime(new Date());
        }


        // получаем данные урока из базы данных
        if (dbAttitudeId != -1) {

            // заголовок
            ((TextView) findViewById(R.id.activity_lesson_redactor_head_text)).setText(getResources().getString(
                    R.string.title_activity_lesson_redactor_edit,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    getResources().getStringArray(R.array.months_names_with_ending)[calendar.get(Calendar.MONTH)],
                    getResources().getStringArray(R.array.week_days_simple)[calendar.get(Calendar.DAY_OF_WEEK) - 1]
            ));

            // главная зависимость(урок)
            Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(dbAttitudeId);
            attitudeCursor.moveToFirst();
            // предмет
            tempSubjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
            Cursor subjectCursor = db.getSubjectById(tempSubjectId);
            subjectCursor.moveToFirst();
            // id класса (получаем из предмета)
            tempClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
            // id кабинета
            tempCabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
            // повторы
            tempRepeat = attitudeCursor.getInt(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT));
            subjectCursor.close();
            attitudeCursor.close();
            // дз
            Cursor homework = db.getLessonCommentsByDateAndLesson(dbAttitudeId, checkLessonDate);
            if (homework.moveToNext()) {
                tempHomeworkUnit1 = new HomeWorkUnit(
                        homework.getLong(homework.getColumnIndex(SchoolContract.TableLessonComment.KEY_LESSON_TEXT_ID)),
                        homework.getString(homework.getColumnIndex(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT))
                );
            } else {
                tempHomeworkUnit1 = null;
            }
            homework.close();

        } else {
            // раз нет id, это создание урока
            ((TextView) findViewById(R.id.activity_lesson_redactor_head_text)).setText(getResources().getString(
                    R.string.title_activity_lesson_redactor_create,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    getResources().getStringArray(R.array.months_names_with_ending)[calendar.get(Calendar.MONTH)],
                    getResources().getStringArray(R.array.week_days_simple)[calendar.get(Calendar.DAY_OF_WEEK) - 1]
            ));

            tempClassId = -1;
            tempSubjectId = -1;
            tempCabinetId = -1;
            tempRepeat = 0;

            // дз
            tempHomeworkUnit1 = null;
        }


        // ---------------------------- получение массивов с названиями ----------------------------
        // расставляем позиции
        int tempClassPos = -1;
        int tempSubjectPos = -1;
        int tempCabinetPos = -1;

        {// время
            int[][] time = db.getSettingsTime(1);

            timeTexts = new String[time.length];
            for (int i = 0; i < timeTexts.length; i++) {
                //'  4 урок 11:30 - 12:15  '
                timeTexts[i] = getResources().getString(
                        R.string.lesson_redactor_activity_spinner_title_lesson,
                        i + 1, time[i][0], time[i][1], time[i][2], time[i][3]);
            }
        }

        // повторы
        repeatTexts = new String[]{
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_never),
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_daily),
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_weekly)
        };

        {// классы
            Cursor classesCursor = db.getLearnersClases();

            classUnits = new LearnersClassUnit[classesCursor.getCount()];
            for (int i = 0; i < classUnits.length; i++) {
                classesCursor.moveToNext();
                long classId = classesCursor.getLong(classesCursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));


                // получаем предметы
                Cursor subjectsCursor = db.getSubjectsByClassId(classId);
                ArrayList<SubjectUnit> subjects = new ArrayList<>(subjectsCursor.getCount());
                for (int subjectsI = 0; subjectsI < subjectsCursor.getCount(); subjectsI++) {
                    subjectsCursor.moveToNext();

                    subjects.add(new SubjectUnit(
                            subjectsCursor.getLong(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID)),
                            subjectsCursor.getString(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME))
                    ));

                    // считаем позицию выбранного в уроке
                    if (tempSubjectId == subjects.get(subjectsI).subjectId) {
                        tempSubjectPos = subjectsI;
                    }
                }
                subjectsCursor.close();

                // создаем класс
                classUnits[i] = new LearnersClassUnit(
                        classId,
                        classesCursor.getString(classesCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)),
                        subjects
                );

                // считаем позицию выбранного в уроке
                if (tempClassId == classId) {
                    tempClassPos = i;
                }
            }
            classesCursor.close();
        }
        {// кабинеты
            Cursor cabinetsCursor = db.getCabinets();

            cabinetUnit1s = new CabinetUnit[cabinetsCursor.getCount()];
            for (int i = 0; i < cabinetUnit1s.length; i++) {
                cabinetsCursor.moveToNext();
                cabinetUnit1s[i] = new CabinetUnit(
                        cabinetsCursor.getLong(cabinetsCursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID)),
                        cabinetsCursor.getString(cabinetsCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME))
                );

                // считаем позицию выбранного в уроке
                if (tempCabinetId == cabinetUnit1s[i].cabinetId) {
                    tempCabinetPos = i;
                }
            }
            cabinetsCursor.close();
        }

        db.close();

        // автоматически расставляем не проставленные позиции
        if (classUnits.length != 0) {
            // позиция класса
            if (tempClassPos == -1) {
                tempClassPos = 0;
            }
            // позиция предмета
            if (classUnits[tempClassPos].subjects.size() != 0 && tempSubjectPos == -1) {
                tempSubjectPos = 0;
            }
        }
        // позиция кабинета
        if (cabinetUnit1s.length != 0 && tempCabinetPos == -1) {
            tempCabinetPos = 0;
        }


        // --------------------------------- создаем обьект урока ----------------------------------

        lessonUnit1 = new LessonUnit(
                dbAttitudeId,
                tempRepeat,
                tempClassPos,
                tempCabinetPos,
                tempSubjectPos,
                tempHomeworkUnit1
        );

        // ----------------------------------- компоненты экрана -----------------------------------



        // TODO -------------------------------------------------
        // TODO -------------------------------------------------
        // TODO -------------------------------------------------
        // TODO -------------------------------------------------
        // TODO -------------------------------------------------
        // TODO -------------------------------------------------
        // TODO -------------------------------------------------
        // TODO -------------------------------------------------
        // TODO -------------------------------------------------
        // TODO -------------------------------------------------

        // текстовое поле предмета
        subjectText = findViewById(R.id.activity_lesson_redactor_lesson_name_text_button);
        subjectText.setPaintFlags(subjectText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        //  спиннер классов 
        classSpinner = findViewById(R.id.activity_lesson_redactor_class_spinner);
        outClasses();

        //  спиннер кабинетов 
        cabinetSpinner = findViewById(R.id.activity_lesson_redactor_cabinet_spinner);
        getAndOutCabinets();

        //  спиннер времени 
        timeSpinner = findViewById(R.id.activity_lesson_redactor_time_spinner);
        getAndOutTime();

        //  спиннер повторов 
        repeatSpinner = findViewById(R.id.activity_lesson_redactor_repeat_spinner);
        getAndOutRepeats();

        // текстовое поле дз
        homeworkEdit = findViewById(R.id.activity_lesson_redactor_homework_text_edit);
        // выставляем предыдущий текст
        if (lessonUnit1.homework != null) {
            homeworkEdit.setText(lessonUnit1.homework.homeworkString);
        } else {
            homeworkEdit.setText("");
        }
        // ставим ограничение в 3 строки (70 символов)
        homeworkEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(70)});

        // ---------------------------------- настраиваем кнопки -----------------------------------

        // кнопка удаления урока
        TextView removeButton = findViewById(R.id.activity_lesson_redactor_remove_button);
        if (lessonUnit1.attitudeId == -1) {
            ((LinearLayout) findViewById(R.id.activity_lesson_redactor_buttons_container)).removeView(removeButton);
        } else {
            //buttonsOut.removeView(backButton);
            // удаление урока
            removeButton.setOnClickListener(view -> {
                DataBaseOpenHelper db12 = new DataBaseOpenHelper(LessonRedactorActivity.this);
                db12.deleteSubjectAndTimeCabinetAttitude(lessonUnit1.attitudeId);
                db12.close();
                // сообщаем активности, что редактор закрылся
                Intent closeIntent = new Intent();
                setResult(LESSON_REDACTOR_RESULT_CODE_UPDATE, closeIntent);
                // закрываем редактор
                finish();
            });
        }

        // ---- кнопка выбора предмета ----
        subjectText.setOnClickListener(view -> {
            if (lessonUnit1.chosenClassPosition != -1) {
                // создаем диалог работы с предметами
                SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();
                // готоим и передаем массив названий предметов и позицию выбранного предмета
                Bundle args = new Bundle();
                String[] subjectsArray = new String[classUnits[lessonUnit1.chosenClassPosition].subjects.size()];
                for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                    subjectsArray[subjectI] = classUnits[lessonUnit1.chosenClassPosition].subjects.get(subjectI).subjectName;
                }
                args.putStringArray(SubjectsDialogFragment.ARGS_LEARNERS_NAMES_STRING_ARRAY, subjectsArray);
                subjectsDialogFragment.setArguments(args);

                // показываем диалог
                subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment - hello");
            }
        });

        // ---- кнопка рассадить учеников ----
        RelativeLayout editSeatingButton = findViewById(R.id.activity_lesson_redactor_seating_redactor_button);
        seatingStateImage = findViewById(R.id.activity_lesson_redactor_seating_state);
        editSeatingButton.setOnClickListener(view -> {

            if (lessonUnit1.chosenClassPosition == -1) {
                Toast toast = Toast.makeText(LessonRedactorActivity.this, R.string.lesson_redactor_activity_toast_text_class_not_chosen, Toast.LENGTH_LONG);
                toast.show();
            } else if (lessonUnit1.chosenCabinetPosition == -1) {
                Toast toast = Toast.makeText(LessonRedactorActivity.this, R.string.lesson_redactor_activity_toast_text_cabinet_not_chosen, Toast.LENGTH_LONG);
                toast.show();
            } else {
                Intent intent = new Intent(LessonRedactorActivity.this, SeatingRedactorActivity.class);
                intent.putExtra(SeatingRedactorActivity.CLASS_ID, classUnits[lessonUnit1.chosenClassPosition].classId);
                intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetUnit1s[lessonUnit1.chosenCabinetPosition].cabinetId);
                startActivity(intent);
            }
        });

        // ---- кнопка сохранения изменений ----
        View saveButton = findViewById(R.id.activity_lesson_redactor_save_button);
        saveButton.setOnClickListener(view -> {
            if (lessonUnit1.chosenCabinetPosition != -1) {//выбран ли кабинет
                if (lessonUnit1.chosenSubjectPosition != -1) {//выбран ли предмет

                    DataBaseOpenHelper db1 = new DataBaseOpenHelper(LessonRedactorActivity.this);

                    // сохраняем урок
                    if (lessonUnit1.attitudeId == -1) {//создание
                        lessonUnit1.attitudeId = db1.createLessonTimeAndCabinetAttitude(
                                classUnits[lessonUnit1.chosenClassPosition].subjects.get(lessonUnit1.chosenSubjectPosition).subjectId,
                                cabinetUnit1s[lessonUnit1.chosenCabinetPosition].cabinetId,
                                checkLessonDate,
                                lessonNumber,
                                lessonUnit1.repeat
                        );
                    } else {//или изменение
                        db1.editLessonTimeAndCabinet(
                                lessonUnit1.attitudeId,
                                classUnits[lessonUnit1.chosenClassPosition].subjects.get(lessonUnit1.chosenSubjectPosition).subjectId,
                                cabinetUnit1s[lessonUnit1.chosenCabinetPosition].cabinetId,
                                lessonNumber,
                                lessonUnit1.repeat
                        );
                    }

                    // сохраняем дз
                    String lessonComment = homeworkEdit.getText().toString().trim();
                    if (lessonComment.length() > 0) {
                        if (lessonUnit1.homework == null) {
                            db1.createLessonComment(
                                    checkLessonDate,
                                    lessonUnit1.attitudeId,
                                    lessonComment
                            );
                        } else {
                            db1.setLessonCommentStringById(
                                    lessonUnit1.homework.homeworkId,
                                    lessonComment
                            );
                        }
                    }
                    db1.close();

                    // сообщаем активности, что редактор закрылся
                    Intent closeIntent = new Intent();
                    setResult(LESSON_REDACTOR_RESULT_CODE_UPDATE, closeIntent);
                    // закрываем редактор
                    finish();

                } else {
                    Toast toast = Toast.makeText(LessonRedactorActivity.this, R.string.lesson_redactor_activity_toast_text_subject_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                }
            } else {
                Toast toast = Toast.makeText(LessonRedactorActivity.this, R.string.lesson_redactor_activity_toast_text_cabinet_not_chosen, Toast.LENGTH_LONG);
                toast.show();
            }
        });

        // кнопка назад
        findViewById(R.id.activity_lesson_redactor_back_button).setBackground(
                getResources().getDrawable(R.drawable.base_button_close_background_round));
        findViewById(R.id.activity_lesson_redactor_back_button).setOnClickListener(f -> finish());
    }


    @Override
    public void onStart() {
        super.onStart();

        // обновляем текст с информацией о рассадке
        seatingTextUpdate();
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    // ==================== методы обновления ====================

    // вывод классов в спиннер
    void outClasses() {

        // преобразуем в строковый массив
        String[] stringClasses = new String[classUnits.length];
        for (int classI = 0; classI < classUnits.length; classI++)
            stringClasses[classI] = classUnits[classI].className;

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.lesson_redactor_spinner_dropdown_element, stringClasses);
        classSpinner.setAdapter(arrayAdapter);
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // меняем позицию выбранного класса
                lessonUnit1.chosenClassPosition = pos;
                // выводим предметы
                getAndOutSubjects();
                seatingTextUpdate();
            }
        });

        if (classUnits.length != 0) {
            if (lessonUnit1.chosenClassPosition == -1)
                lessonUnit1.chosenClassPosition = 0;
            classSpinner.setSelection(lessonUnit1.chosenClassPosition, false);
        }
    }

    // вывод кабинетов в спиннер 
    void getAndOutCabinets() {
        // преобразуем в строковый массив
        String[] stringCabinets = new String[cabinetUnit1s.length];
        for (int cabinetI = 0; cabinetI < cabinetUnit1s.length; cabinetI++) {
            stringCabinets[cabinetI] = cabinetUnit1s[cabinetI].cabinetName;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.lesson_redactor_spinner_dropdown_element, stringCabinets);
        cabinetSpinner.setAdapter(adapter);
        cabinetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // меняем позицию выбранного кабинета
                lessonUnit1.chosenCabinetPosition = pos;
                seatingTextUpdate();
            }
        });


        if (cabinetUnit1s.length != 0) {
            if (lessonUnit1.chosenCabinetPosition == -1)
                lessonUnit1.chosenCabinetPosition = 0;
            cabinetSpinner.setSelection(lessonUnit1.chosenCabinetPosition, false);
        }
    }

    // спиннер времени 
    void getAndOutTime() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.lesson_redactor_spinner_dropdown_element, timeTexts);
        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // при выборе пункта меняем выбранный урок
                lessonNumber = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // ставим выбранное время
        timeSpinner.setSelection(lessonNumber, false);
    }

    // спиннер предметов 
    void getAndOutSubjects() {
        if (classUnits[lessonUnit1.chosenClassPosition].subjects.size() == 0) {
            subjectText.setText(R.string.lesson_redactor_activity_spinner_text_create_subject);
            lessonUnit1.chosenSubjectPosition = -1;
        } else {
            subjectText.setText(classUnits[lessonUnit1.chosenClassPosition].subjects.get(lessonUnit1.chosenSubjectPosition).subjectName);
        }

    }

    // спиннер повторов
    void getAndOutRepeats() {


        repeatSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.lesson_redactor_spinner_dropdown_element, repeatTexts));
        repeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // при выборе пункта меняем выбранный урок
                lessonUnit1.repeat = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (lessonUnit1.repeat == -1) {
            lessonUnit1.repeat = 0;
        }
        repeatSpinner.setSelection(lessonUnit1.repeat, false);

    }

    // текст рассадки 
    void seatingTextUpdate() {

        if (lessonUnit1.chosenCabinetPosition != -1 && lessonUnit1.chosenClassPosition != -1) {
            // проверяем рассажены ли ученики
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            ArrayList<Long> arrayList = db.getNotPutLearnersIdByCabinetIdAndClassId(
                    cabinetUnit1s[lessonUnit1.chosenCabinetPosition].cabinetId,
                    classUnits[lessonUnit1.chosenClassPosition].classId
            );
            if (arrayList.size() == 0) {// если да
                seatingStateImage.setImageResource(R.drawable.lesson_redactor_activity_icon_correct);
            } else {// если нет
                seatingStateImage.setImageResource(R.drawable.lesson_redactor_activity_icon_wrong);
            }
            db.close();
        } else {
            seatingStateImage.setBackgroundColor(Color.TRANSPARENT);
        }
    }


    // ----- обратная связь от диалога предметов -----

    @Override
    public void setSubjectPosition(int position) {
        lessonUnit1.chosenSubjectPosition = position;
        subjectText.setText(classUnits[lessonUnit1.chosenClassPosition].subjects.get(position).subjectName);
    }

    @Override
    public void createSubject(String name, int position) {
        // создаем предмет в базе данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long createdSubjectId = db.createSubject(name, classUnits[lessonUnit1.chosenClassPosition].classId);
        db.close();
        // добавляем предмет в список под нужным номером
        classUnits[lessonUnit1.chosenClassPosition].subjects.add(position, new SubjectUnit(createdSubjectId, name));
        // выбираем этот предмет и ставим его имя в заголовок
        lessonUnit1.chosenSubjectPosition = position;
        subjectText.setText(classUnits[lessonUnit1.chosenClassPosition].subjects.get(lessonUnit1.chosenSubjectPosition).subjectName);
    }

    @Override
    public void deleteSubjects(boolean[] deleteList) {
        // удаляем предметы
        ArrayList<Long> deleteId = new ArrayList<>();
        // пробегаемся в обратном направлении, удаляя записи из листа и
        for (int subjectI = classUnits[lessonUnit1.chosenClassPosition].subjects.size() - 1; subjectI >= 0; subjectI--) {
            if (deleteList[subjectI]) {
                // добавляем id предмета в расстрельный список
                deleteId.add(0, classUnits[lessonUnit1.chosenClassPosition].subjects.get(subjectI).subjectId);
                // удаляем предмет из листа
                classUnits[lessonUnit1.chosenClassPosition].subjects.remove(subjectI);

                // смотрим не удален ли выбранный предмет
                if (lessonUnit1.chosenSubjectPosition == subjectI) {
                    lessonUnit1.chosenSubjectPosition = -1;
                    // ставим выбор на первом предмете
                    if (classUnits[lessonUnit1.chosenClassPosition].subjects.size() != 0) {
                        lessonUnit1.chosenSubjectPosition = 0;
                        // выводим название предмета
                        subjectText.setText(
                                classUnits[lessonUnit1.chosenClassPosition].subjects.get(lessonUnit1.chosenSubjectPosition).subjectName
                        );

                    } else { // если проедметов в базе данных нет не выбираем ничего
                        // выводим текст о том, что предмета нет
                        subjectText.setText(getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject));
                    }
                }
            }
        }

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // прежде чем удалить предметы, заменим в текущем уроке предмет на первый в списке
        if ((cabinetUnit1s[lessonUnit1.chosenCabinetPosition].cabinetId != -1) && (lessonUnit1.chosenSubjectPosition != -1) && (lessonUnit1.attitudeId != -1)) {
            db.editLessonTimeAndCabinet(
                    lessonUnit1.attitudeId,
                    classUnits[lessonUnit1.chosenClassPosition].subjects.get(lessonUnit1.chosenSubjectPosition).subjectId,
                    cabinetUnit1s[lessonUnit1.chosenCabinetPosition].cabinetId,
                    lessonNumber,
                    lessonUnit1.repeat
            );
        }
        db.deleteSubjects(deleteId);
        db.close();
    }

    @Override
    public void renameSubjects(String[] newSubjectsNames) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        // в цикле переименовываем все предметы в массиве активности
        for (int subjectI = 0; subjectI < classUnits[lessonUnit1.chosenClassPosition].subjects.size(); subjectI++) {

            classUnits[lessonUnit1.chosenClassPosition].subjects.get(subjectI).subjectName = newSubjectsNames[subjectI];

            // и сохраняем все изменения в базу данных
            db.setSubjectName(
                    classUnits[lessonUnit1.chosenClassPosition].subjects.get(subjectI).subjectId,
                    classUnits[lessonUnit1.chosenClassPosition].subjects.get(subjectI).subjectName
            );
        }
        db.close();

        // сортируем текущий список (пузырьком)
        for (int out = classUnits[lessonUnit1.chosenClassPosition].subjects.size() - 1; out > 0; out--) {
            for (int in = 0; in < out; in++) {
                if (classUnits[lessonUnit1.chosenClassPosition].subjects.get(in).subjectName.compareTo(classUnits[lessonUnit1.chosenClassPosition].subjects.get(in + 1).subjectName) > 0) {
                    if (lessonUnit1.chosenSubjectPosition == in) {
                        lessonUnit1.chosenSubjectPosition = in + 1;
                    } else if (lessonUnit1.chosenSubjectPosition == in + 1) {
                        lessonUnit1.chosenSubjectPosition = in;
                    }
                    SubjectUnit temp = classUnits[lessonUnit1.chosenClassPosition].subjects.get(in + 1);
                    classUnits[lessonUnit1.chosenClassPosition].subjects.set(in + 1, classUnits[lessonUnit1.chosenClassPosition].subjects.get(in));
                    classUnits[lessonUnit1.chosenClassPosition].subjects.set(in, temp);
                }
            }
        }

        // обновляем надпись на тексте с названием предмета
        if (classUnits[lessonUnit1.chosenClassPosition].subjects.size() == 0) {
            subjectText.setText(R.string.lesson_redactor_activity_spinner_text_create_subject);
            lessonUnit1.chosenSubjectPosition = -1;
        } else {
            subjectText.setText(classUnits[lessonUnit1.chosenClassPosition].subjects.get(lessonUnit1.chosenSubjectPosition).subjectName);
        }
    }


}

// класс для хранения предмета
class SubjectUnit {
    long subjectId;
    String subjectName;

    SubjectUnit(long subjectId, String subjectName) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }
}

class LearnersClassUnit {
    long classId;
    String className;
    // массив с предметами
    ArrayList<SubjectUnit> subjects;

    public LearnersClassUnit(long classId, String className, ArrayList<SubjectUnit> subjects) {
        this.classId = classId;
        this.className = className;
        this.subjects = subjects;
    }
}

class CabinetUnit {
    long cabinetId;
    String cabinetName;

    public CabinetUnit(long cabinetId, String cabinetName) {
        this.cabinetId = cabinetId;
        this.cabinetName = cabinetName;
    }
}


// класс для хранения урока
class LessonUnit {

    // id урока
    long attitudeId;
    // какие повторения
    int repeat;

    // номера класса, кабинета и предмета в массивах
    int chosenClassPosition;
    int chosenCabinetPosition;
    int chosenSubjectPosition;

    // дз
    HomeWorkUnit homework;

    public LessonUnit(long attitudeId, int repeat, int chosenClassPosition, int chosenCabinetPosition, int chosenSubjectPosition, HomeWorkUnit homework) {
        this.attitudeId = attitudeId;
        this.repeat = repeat;
        this.chosenClassPosition = chosenClassPosition;
        this.chosenCabinetPosition = chosenCabinetPosition;
        this.chosenSubjectPosition = chosenSubjectPosition;
        this.homework = homework;
    }
}

// класс для хранения дз
class HomeWorkUnit {

    // id
    long homeworkId;
    // текст дз
    String homeworkString;

    public HomeWorkUnit(long homeworkId, String homeworkString) {
        this.homeworkId = homeworkId;
        this.homeworkString = homeworkString;
    }
}