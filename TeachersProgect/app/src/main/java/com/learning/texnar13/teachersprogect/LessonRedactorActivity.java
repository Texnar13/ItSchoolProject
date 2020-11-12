package com.learning.texnar13.teachersprogect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;


import com.learning.texnar13.teachersprogect.SubjectsDialog.SubjectsDialogFragment;
import com.learning.texnar13.teachersprogect.SubjectsDialog.SubjectsDialogInterface;
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

public class LessonRedactorActivity extends AppCompatActivity implements SubjectsDialogInterface {

    // id передаваемых данных (трансферные константы)
    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    public static final String LESSON_DATE = "lessonDate";
    public static final String LESSON_NUMBER = "lessonNumber";

    // ----- id и полученные данные-----
    //id главной зависимости
    long attitudeId = -1;
    //выбранные класс и кабинет//todo предусмотреть вариант если уроков вобще нет. проверка нет ли на этом времени урока.
    long classId = -1;
    long cabinetId = -1;
    // время урока в календарях (берем не из урока, тк если урок не создан то выбранного времени в бд нет)
    String lessonDate;
    int lessonNumber;
    // какие повторения
    long repeat = 0;

    // массив с предметами
    static ArrayList<SubjectUnit> subjects;
    // номер выбранного предмета в массиве
    int chosenSubjectPosition = -1;

    // текстовое поле предмета
    TextView subjectText;
    // индикатор состояния рассадки
    ImageView seatingStateImage;
    // --- спиннеры ---
    Spinner classSpinner;
    Spinner cabinetSpinner;
    Spinner timeSpinner;



    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);

        setContentView(R.layout.activity_lesson_redactor);
        // вертикальная ориентация
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // цвета статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setTheme(android.R.style.Theme_Dialog);
        //this.setFinishOnTouchOutside(false);


        // ******************** загружаем данные ********************

        // ================ загрузка рекламы яндекса ================
        com.yandex.mobile.ads.AdView mAdView = findViewById(R.id.activity_lesson_redactor_banner);
        mAdView.setBlockId(getResources().getString(R.string.banner_id_lesson_redactor));
        mAdView.setAdSize(AdSize.BANNER_320x100);
        // Создание объекта таргетирования рекламы.
        final com.yandex.mobile.ads.AdRequest adRequest = new com.yandex.mobile.ads.AdRequest.Builder().build();
        // Загрузка объявления.
        mAdView.loadAd(adRequest);


        // -- id зависимости --
        attitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -2L);
        if (attitudeId == -2) {
            Toast toast = Toast.makeText(this, "error", Toast.LENGTH_SHORT);
            toast.show();
            finish();
            return;
        }

        // получаем время из intent
        lessonDate = getIntent().getStringExtra(LESSON_DATE);
        lessonNumber = getIntent().getIntExtra(LESSON_NUMBER, 0);


        if (attitudeId == -1) {
            //заголовок
            setTitle(getResources().getString(R.string.title_activity_lesson_redactor_create));
        } else {
            // -- заголовок --
            setTitle(getString(R.string.title_activity_lesson_redactor_edit));

            // ----- база данных -----
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // -- главная зависимость(урок) --
            Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(attitudeId);
            attitudeCursor.moveToFirst();
            // -- предмет --
            long chosenSubjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
            Cursor subjectCursor = db.getSubjectById(chosenSubjectId);
            subjectCursor.moveToFirst();
            // -- id класса(получаем из предмета) --
            classId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
            // -- id кабинета --
            cabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
            // -- повторы --
            repeat = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT));

            subjectCursor.close();
            attitudeCursor.close();
            db.close();
        }


        // ------ спиннеры и кнопки ------
        // текстовое поле предмета
        subjectText = findViewById(R.id.activity_lesson_redactor_lesson_name_text_button);
        subjectText.setPaintFlags(subjectText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        // -- спиннер классов --
        classSpinner = findViewById(R.id.activity_lesson_redactor_class_spinner);
        // -- спиннер кабинетов --
        cabinetSpinner = findViewById(R.id.activity_lesson_redactor_cabinet_spinner);
        // -- спиннер времени --
        timeSpinner = findViewById(R.id.activity_lesson_redactor_time_spinner);
        // - рассадка -
        RelativeLayout editSeatingButton = findViewById(R.id.activity_lesson_redactor_seating_redactor_button);


        // картинка сигнализирующая о рассадке
        seatingStateImage = findViewById(R.id.activity_lesson_redactor_seating_state);
        // кнопки сохранения удаления
        TextView removeButton = findViewById(R.id.activity_lesson_redactor_remove_button);
        LinearLayout saveButton = findViewById(R.id.activity_lesson_redactor_save_button);
        // текст текущей даты
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            GregorianCalendar calendar = new GregorianCalendar();
            try {
                calendar.setTime(simpleDateFormat.parse(lessonDate));
            } catch (ParseException e) {
                e.printStackTrace();
                calendar.setTime(new Date());
            }
            ((TextView) findViewById(R.id.activity_lesson_redactor_current_date_text)).setText(
                    getResources().getTextArray(R.array.week_days_simple)[calendar.get(Calendar.DAY_OF_WEEK) - 1] + ", "
                            + calendar.get(Calendar.DAY_OF_MONTH) + " " + getResources().getTextArray(R.array.months_names_low_case)[calendar.get(Calendar.MONTH)]
            );
        }


//        // кнопка не повторять урок
//        findViewById(R.id.activity_lesson_redactor_lesson_repeat_no).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // переключаем цвета
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_no)).
//                        setTextColor(getResources().getColor(R.color.baseGreen));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_no)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_medium));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly)).
//                        setTextColor(getResources().getColor(R.color.backgroundDarkGray));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_family));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily)).
//                        setTextColor(getResources().getColor(R.color.backgroundDarkGray));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_family));
//                // и переменные
//                repeat = SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER;
//            }
//        });
//        // кнопка повторять урок каждый день
//        findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // переключаем цвета
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_no)).
//                        setTextColor(getResources().getColor(R.color.backgroundDarkGray));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_no)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_family));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly)).
//                        setTextColor(getResources().getColor(R.color.backgroundDarkGray));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_family));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily)).
//                        setTextColor(getResources().getColor(R.color.baseGreen));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_medium));
//                // и переменные
//                repeat = SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY;
//
//            }
//        });
//        // кнопка повторять урок каждую неделю
//        findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // переключаем цвета
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_no)).
//                        setTextColor(getResources().getColor(R.color.backgroundDarkGray));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_no)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_family));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly)).
//                        setTextColor(getResources().getColor(R.color.baseGreen));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_medium));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily)).
//                        setTextColor(getResources().getColor(R.color.backgroundDarkGray));
//                ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily)).
//                        setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_family));
//                // и переменные
//                repeat = SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY;
//
//            }
//        });
//
//        // закрашиваем выбранный тип повторов при старте активности
//        if (repeat == SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER) {
//            ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_no)).
//                    setTextColor(getResources().getColor(R.color.baseGreen));
//            ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_no)).
//                    setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_medium));
//        } else if (repeat == SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY) {
//            ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily)).
//                    setTextColor(getResources().getColor(R.color.baseGreen));
//            ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_daily)).
//                    setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_medium));
//        } else {
//            ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly)).
//                    setTextColor(getResources().getColor(R.color.baseGreen));
//            ((TextView) findViewById(R.id.activity_lesson_redactor_lesson_repeat_weekly)).
//                    setTypeface(ResourcesCompat.getFont(LessonRedactorActivity.this, R.font.geometria_medium));
//        }

// ******************** вывод данных в поля ********************

        // -- спиннер классов --
        outClasses();
        // -- спиннер кабинетов --
        getAndOutCabinets();
        // -- спиннер времени --
        getAndOutTime();
        // - рассадка -
        // ------ обновляем текст с информацией о рассадке ------
        seatingTextUpdate();


// ******************** настраиваем кнопки ********************

        // ---- удаляем лишние кнопки ----
        if (attitudeId == -1) {
            ((LinearLayout)findViewById(R.id.activity_lesson_redactor_buttons_container)).removeView(removeButton);
        } else {
            //buttonsOut.removeView(backButton);
            //удаление урока
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                    db.deleteSubjectAndTimeCabinetAttitude(attitudeId);
                    db.close();
                    finish();
                }
            });
        }

        // ---- кнопка выбора предмета ----
        subjectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (classId != -1) {
                    // создаем диалог работы с предметами
                    SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();
                    // готоим и передаем массив названий предметов и позицию выбранного предмета
                    Bundle args = new Bundle();
                    String[] subjectsArray = new String[subjects.size()];
                    for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                        subjectsArray[subjectI] = subjects.get(subjectI).getSubjectName();
                    }
                    args.putStringArray(SubjectsDialogFragment.ARGS_LEARNERS_NAMES_STRING_ARRAY, subjectsArray);
                    subjectsDialogFragment.setArguments(args);

                    // показываем диалог
                    subjectsDialogFragment.show(getSupportFragmentManager(), "subjectsDialogFragment - hello");
                }
            }
        });

        // ---- кнопка рассадить учеников ----
        editSeatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (classId == -1) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.lesson_redactor_activity_toast_text_class_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                } else if (cabinetId == -1) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.lesson_redactor_activity_toast_text_cabinet_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
                    intent.putExtra(SeatingRedactorActivity.CLASS_ID, classId);
                    intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
                    startActivity(intent);
                }
            }
        });

        // ---- кнопка сохранения изменений ----
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cabinetId != -1) {//выбран ли кабинет
                    if (chosenSubjectPosition != -1) {//выбран ли предмет
                        if (attitudeId == -1) {//создание
                            Log.i("TeachersApp", "LessonRedactorActivity - create lesson chosenSubjectId =" + subjects.get(chosenSubjectPosition).getSubjectId() + " cabinetId =" + cabinetId + " lessonDate =" + lessonDate + " lessonNumber =" + lessonNumber);
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                            db.setLessonTimeAndCabinet(
                                    subjects.get(chosenSubjectPosition).getSubjectId(),
                                    cabinetId,
                                    lessonDate,
                                    lessonNumber,
                                    repeat
                            );
                            db.close();
                            finish();
                        } else {//или изменение
                            Log.i("TeachersApp", "LessonRedactorActivity - edit lesson chosenSubjectId =" + subjects.get(chosenSubjectPosition).getSubjectId() + " cabinetId =" + cabinetId + " lessonDate =" + lessonDate + " lessonNumber =" + lessonNumber);
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                            db.editLessonTimeAndCabinet(
                                    attitudeId,
                                    subjects.get(chosenSubjectPosition).getSubjectId(),
                                    cabinetId,
                                    lessonDate,
                                    lessonNumber,
                                    repeat
                            );
                            db.close();
                            finish();
                        }
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.lesson_redactor_activity_toast_text_subject_not_chosen, Toast.LENGTH_LONG);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.lesson_redactor_activity_toast_text_cabinet_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        // кнопка назад
        findViewById(R.id.activity_lesson_redactor_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        seatingTextUpdate();
    }

    // ==================== методы обновления ====================

    // ------ вывод классов в спиннер ------
    void outClasses() {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor classesCursor = db.getLearnersClass();
        String[] stringClasses = new String[classesCursor.getCount()];
        final long[] classesId = new long[stringClasses.length];
        for (int i = 0; i < stringClasses.length; i++) {
            classesCursor.moveToNext();
            stringClasses[i] = classesCursor.getString(classesCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            classesId[i] = classesCursor.getLong(classesCursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));
        }
        classesCursor.close();
        db.close();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_element_subtitle, stringClasses);
        classSpinner.setAdapter(arrayAdapter);
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // меняем id класса
                classId = classesId[pos];
                // выводим предметы
                getAndOutSubjects();
                Log.i("TeachersApp", "LessonRedactorActivity - class spinner onItemSelected pos =" + pos + " id =" + classesId[pos]);
                seatingTextUpdate();
            }
        });

        for (int i = 0; i < classesId.length; i++) {//ставим текущий класс в спиннер
            if (classesId[i] == classId) {
                classSpinner.setSelection(i, false);
            }
        }
    }

    // ------ вывод кабинетов в спиннер ------
    void getAndOutCabinets() {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor cabinetsCursor = db.getCabinets();
        String[] stringCabinets = new String[cabinetsCursor.getCount()];
        final long[] cabinetsId = new long[stringCabinets.length];
        for (int i = 0; i < stringCabinets.length; i++) {
            cabinetsCursor.moveToNext();
            stringCabinets[i] = cabinetsCursor.getString(cabinetsCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
            cabinetsId[i] = cabinetsCursor.getLong(cabinetsCursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));
        }
        cabinetsCursor.close();
        db.close();

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_element_subtitle, stringCabinets);
        cabinetSpinner.setAdapter(adapter);
        cabinetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Log.i("TeachersApp", "LessonRedactorActivity - cabinet spinner onItemSelected " + pos + "id =" + cabinetsId[pos]);
                cabinetId = cabinetsId[pos];
                seatingTextUpdate();
            }
        });

        for (int i = 0; i < cabinetsId.length; i++) {//ставим текущий класс в спиннер
            if (cabinetsId[i] == cabinetId) {
                cabinetSpinner.setSelection(i, false);
            }
        }
    }

    // -- спиннер предметов --
    void getAndOutSubjects() {

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // получаем id предмета из урока
        Cursor attitude = db.getSubjectAndTimeCabinetAttitudeById(attitudeId);
        long chosenSubjectId = -1;
        if (attitude.getCount() != 0) {
            attitude.moveToFirst();
            chosenSubjectId = attitude.getLong(attitude.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
        }
        attitude.close();

        // получаем предметы из бд
        Cursor subjectsCursor = db.getSubjectsByClassId(classId);
        subjects = new ArrayList<>();
        chosenSubjectPosition = 0;
        while (subjectsCursor.moveToNext()) {
            subjects.add(new SubjectUnit(
                    subjectsCursor.getLong(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID)),
                    subjectsCursor.getString(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME))
            ));
            // получаем выбранный предмет для текущего класса
            if (chosenSubjectId == subjectsCursor.getLong(subjectsCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID))) {
                chosenSubjectPosition = subjects.size() - 1;
            }
        }
        subjectsCursor.close();

        if (subjects.size() == 0) {
            subjectText.setText(R.string.lesson_redactor_activity_spinner_text_create_subject);
            chosenSubjectPosition = -1;
        } else {
            subjectText.setText(subjects.get(chosenSubjectPosition).getSubjectName());
        }
    }

    // -- спиннер времени --
    void getAndOutTime() {
        //получаем стандартное время уроков
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        final int[][] time = db.getSettingsTime(1);
        db.close();

        // создаем тексты в пунктах списка
        final String textTime[] = new String[time.length];
        //SimpleDateFormat textTimeFormat = new SimpleDateFormat("H:m", Locale.getDefault());
        for (int i = 0; i < textTime.length; i++) {
            //'  4 урок 11:30 - 12:15  '
            textTime[i] = "" + (i + 1) + " " + getResources().getString(R.string.lesson_redactor_activity_spinner_title_lesson) + ": " +
                    time[i][0] + ":" + time[i][1] + " - " +
                    time[i][2] + ":" + time[i][3] + "  ";
        }


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_element_subtitle, textTime);
        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // при выборе пункта меняем выбранный урок
                lessonNumber = i;
                Log.i("TeachersApp", "chooseStandardLesson :" + textTime[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // ставим выбранное время
        Log.i("TeachersApp", "outTime:chooseTime:" + (lessonNumber + 1));
        timeSpinner.setSelection(lessonNumber, false);


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

    // -- текст рассадки --
    void seatingTextUpdate() {

        // проверяем рассажены ли ученики
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> arrayList = db.getNotPutLearnersIdByCabinetIdAndClassId(cabinetId, classId);
        if (arrayList.size() == 0) {// если да
            seatingStateImage.setImageResource(R.drawable.__signal_correct_sitting);
        } else {// если нет
            seatingStateImage.setImageResource(R.drawable.__signal_wrong_sitting);
        }
        db.close();
    }


    // ----- обратная связь от диалога предметов -----

    @Override
    public void setSubjectPosition(int position) {
        chosenSubjectPosition = position;
        subjectText.setText(subjects.get(position).getSubjectName());
    }

    @Override
    public void createSubject(String name, int position) {
        // создаем предмет в базе данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long createdSubjectId = db.createSubject(name, classId);
        db.close();
        // добавляем предмет в список под нужным номером
        subjects.add(position, new SubjectUnit(createdSubjectId, name));
        // выбираем этот предмет и ставим его имя в заголовок
        chosenSubjectPosition = position;
        subjectText.setText(subjects.get(chosenSubjectPosition).getSubjectName());
    }

    @Override
    public void deleteSubjects(boolean[] deleteList) {
        // удаляем предметы
        ArrayList<Long> deleteId = new ArrayList<>();
        // пробегаемся в обратном направлении, для того, чтобы  параллельно удалять из листа
        for (int subjectI = subjects.size() - 1; subjectI >= 0; subjectI--) {
            if (deleteList[subjectI]) {
                // добавляем id предмета в расстрельный список
                deleteId.add(0, subjects.get(subjectI).getSubjectId());
                // удаляем предмет из листа
                subjects.remove(subjectI);

                // смотрим не удален ли выбранный предмет
                if (chosenSubjectPosition == subjectI) {
                    chosenSubjectPosition = -1;
                    // ставим выбор на первом предмете
                    if (subjects.size() != 0) {
                        chosenSubjectPosition = 0;
                        // выводим название предмета
                        subjectText.setText(subjects.get(chosenSubjectPosition).getSubjectName());

                    } else { // если проедметов в базе данных нет не выбираем ничего
                        // выводим текст о том, что предмета нет
                        subjectText.setText(getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject));
                    }
                }
            }
        }
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // прежде чем удалить предметы, заменим в текущем уроке предмет на первый в списке
        if ((cabinetId != -1) && (chosenSubjectPosition != -1) && (attitudeId != -1)) {
            db.editLessonTimeAndCabinet(
                    attitudeId,
                    subjects.get(chosenSubjectPosition).getSubjectId(),
                    cabinetId,
                    lessonDate,
                    lessonNumber,
                    repeat
            );
        }
        db.deleteSubjects(deleteId);
        db.close();
    }

    @Override
    public void renameSubjects(String[] newSubjectsNames) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        // в цикле переименовываем все предметы в массиве активности
        for (int subjectI = 0; subjectI < subjects.size(); subjectI++) {

            subjects.get(subjectI).setSubjectName(newSubjectsNames[subjectI]);

            // и сохраняем все изменения в базу данных
            db.setSubjectName(
                    subjects.get(subjectI).getSubjectId(),
                    subjects.get(subjectI).getSubjectName()
            );
        }
        db.close();

        // сортируем текущий список (пузырьком)
        for (int out = subjects.size() - 1; out > 0; out--) {
            for (int in = 0; in < out; in++) {
                if (subjects.get(in).getSubjectName().compareTo(subjects.get(in + 1).getSubjectName()) > 0) {
                    if (chosenSubjectPosition == in) {
                        chosenSubjectPosition = in + 1;
                    } else if (chosenSubjectPosition == in + 1) {
                        chosenSubjectPosition = in;
                    }
                    SubjectUnit temp = subjects.get(in + 1);
                    subjects.set(in + 1, subjects.get(in));
                    subjects.set(in, temp);
                }
            }
        }

        // обновляем надпись на тексте с названием предмета
        if (subjects.size() == 0) {
            subjectText.setText(R.string.lesson_redactor_activity_spinner_text_create_subject);
            chosenSubjectPosition = -1;
        } else {
            subjectText.setText(subjects.get(chosenSubjectPosition).getSubjectName());
        }
    }


    // класс для хранения предмета
    class SubjectUnit {
        private long subjectId;
        private String subjectName;

        SubjectUnit(long subjectId, String subjectName) {
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
}



/*
 * https://developer.android.com/guide/topics/ui/menus#PopupMenu
 * */