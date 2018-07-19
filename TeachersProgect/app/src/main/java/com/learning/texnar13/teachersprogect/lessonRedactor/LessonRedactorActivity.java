package com.learning.texnar13.teachersprogect.lessonRedactor;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.SeatingRedactorActivity;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LessonRedactorActivity extends AppCompatActivity implements SubjectNameDialogInterface, RemoveSubjectDialogFragmentInterface {

    // id передаваемых данных (трансферные константы)
    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    public static final String LESSON_START_TIME = "lessonStartTime";
    public static final String LESSON_END_TIME = "lessonEndTime";

    // ----- id и полученные данные-----
    //id главной зависимости
    long attitudeId = -1;
    //выбранные класс и кабинет//todo предусмотреть вариант если уроков вобще нет. проверка нет ли на этом времени урока.
    long classId = -1;
    long cabinetId = -1;
    //урок
    long chosenSubjectId = -1;
    // время урока в календарях (берем не из урока, тк если урок не создан то выбранного времени в бд нет)
    GregorianCalendar calendarStartTime;
    GregorianCalendar calendarEndTime;
    //какие повторения
    long repeat = 0;

    // ----- Layout-ы -----
    //индикатор состояния рассадки
    TextView seatingStateText;
    // --- спиннеры ---
    // -- спиннер классов --
    Spinner classSpinner;
    // -- спиннер кабинетов --
    Spinner cabinetSpinner;
    // -- спиннер предметов --
    Spinner subjectSpinner;
    // -- спиннер времени --
    Spinner timeSpinner;
    // -- спиннер повторов --
    Spinner lessonRepeatSpinner;


    // ----- созданные в процессе работы -----
    String[] repeatPeriodsNames;
    // номер выбранного предмета в списке
    int subjectPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_redactor);
        repeatPeriodsNames = new String[]{
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_never),
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_daily),
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_weekly)
                //, "ежемесячно"//to-do
        };

        // ------ спиннеры и кнопки ------
        // -- спиннер классов --
        classSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_class_spinner);
        // -- спиннер кабинетов --
        cabinetSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_cabinet_spinner);
        // -- спиннер предметов --
        subjectSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_lesson_name_spinner);
        // -- спиннер времени --
        timeSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_time_spinner);
        // -- спиннер повторов --
        lessonRepeatSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_lesson_repeat_spinner);
        // - рассадка -
        TextView editSeatingButton = (TextView) findViewById(R.id.activity_lesson_redactor_seating_redactor_button);
        seatingStateText = (TextView) findViewById(R.id.activity_lesson_redactor_seating_state);
        // -- кнопки сохранения удаления отмены --
        // - и их контейнер -
        LinearLayout buttonsOut = (LinearLayout) findViewById(R.id.activity_lesson_redactor_buttons_out);
        TextView removeButton = (TextView) findViewById(R.id.activity_lesson_redactor_remove_button);
        TextView backButton = (TextView) findViewById(R.id.activity_lesson_redactor_back_button);
        TextView saveButton = (TextView) findViewById(R.id.activity_lesson_redactor_save_button);
        // -- кнопка назад в actionBar --
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


// ******************** загружаем данные ********************

        // -- id зависимости --
        attitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -2);
        if (attitudeId == -2) {
            Toast toast = Toast.makeText(this, R.string.lesson_redactor_activity_toast_subject_not_select, Toast.LENGTH_SHORT);
            toast.show();
            finish();
            return;
        }
        calendarStartTime = new GregorianCalendar();
        calendarEndTime = new GregorianCalendar();
        if (attitudeId == -1) {
            //заголовок
            setTitle(getResources().getString(R.string.title_activity_lesson_redactor_create));
            calendarStartTime.setTime(new Date(getIntent().getLongExtra(LESSON_START_TIME, 1)));
            calendarEndTime.setTime(new Date(getIntent().getLongExtra(LESSON_END_TIME, 1)));
        } else {
            // -- заголовок --
            setTitle(getString(R.string.title_activity_lesson_redactor_edit));

            // ----- база данных -----
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // -- главная зависимость(урок) --
            Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(attitudeId);
            attitudeCursor.moveToFirst();
            attitudeId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID));
            // -- id предмета --
            chosenSubjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
            Cursor subjectCursor = db.getSubjectById(chosenSubjectId);
            subjectCursor.moveToFirst();
            // -- id класса(получаем из предмета) --
            classId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));//todo
            // выбираем переданный предмет в спиннере(-1 говорит об этом методу)
            subjectPosition = -1;
            // -- id кабинета --
            cabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
            // -- время --
            calendarStartTime.setTime(new Date(getIntent().getLongExtra(LESSON_START_TIME, 1)));
            calendarEndTime.setTime(new Date(getIntent().getLongExtra(LESSON_END_TIME, 1)));
            // -- повторы --
            repeat = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT));

            subjectCursor.close();
            attitudeCursor.close();
            db.close();
        }

// ******************** вывод данных в поля ********************

        // -- спиннер классов --
        outClasses();
        // -- спиннер кабинетов --
        outCabinets();
        // -- спиннер предметов --

        // -- спиннер времени --
        outTime();
        // -- спиннер повторов --
        outRepeats();
        // - рассадка -
        // ------ обновляем текст с информацией о рассадке ------
        seatingTextUpdate();


// ******************** настраиваем кнопки ********************

        // ---- удаляем лишние кнопки ----

        if (attitudeId == -1) {
            buttonsOut.removeView(removeButton);
            //отмена
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } else {
            buttonsOut.removeView(backButton);
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

        // ---- кнопка рассадить учеников ----
        editSeatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
                intent.putExtra(SeatingRedactorActivity.CLASS_ID, classId);
                intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
                startActivityForResult(intent, 1);
            }
        });

        // ---- кнопка сохранения изменений ----
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cabinetId != -1) {//выбран ли кабинет
                    if (chosenSubjectId != -1) {//выбран ли предмет
                        if (attitudeId == -1) {//создание
                            Log.i("TeachersApp", "LessonRedactorActivity - create lesson chosenSubjectId =" + chosenSubjectId + " cabinetId =" + cabinetId + " calendarStartTime =" + calendarStartTime.getTime().getTime() + " calendarEndTime =" + calendarEndTime.getTime().getTime());
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                            db.setLessonTimeAndCabinet(chosenSubjectId, cabinetId, calendarStartTime.getTime(), calendarEndTime.getTime(), repeat);
                            db.close();
                            finish();
                        } else {//или изменение
                            Log.i("TeachersApp", "LessonRedactorActivity - edit lesson chosenSubjectId =" + chosenSubjectId + " cabinetId =" + cabinetId + " calendarStartTime =" + calendarStartTime.getTime().getTime() + " calendarEndTime =" + calendarEndTime.getTime().getTime());
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                            db.editLessonTimeAndCabinet(attitudeId, chosenSubjectId, cabinetId, calendarStartTime.getTime(), calendarEndTime.getTime(), repeat);
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

    }

    // -- спиннер классов --
    // -- спиннер кабинетов --
    // -- спиннер предметов --
    // -- спиннер времени --
    // -- спиннер повторов --
    // - рассадка -
    // - обратная связь от диалогов -

// ==================== методы обновления ====================

    // ------ вывод классов в спиннер ------
    void outClasses() {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor classesCursor = db.getClasses();
        String[] stringClasses = new String[classesCursor.getCount()];
        final long[] classesId = new long[stringClasses.length];
        for (int i = 0; i < stringClasses.length; i++) {
            classesCursor.moveToNext();
            stringClasses[i] = classesCursor.getString(classesCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            classesId[i] = classesCursor.getLong(classesCursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));
        }
        classesCursor.close();
        db.close();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spiner_dropdown_element_lesson_redactor, stringClasses);
        arrayAdapter.setDropDownViewResource(R.layout.spiner_dropdown_element_lesson_redactor);
        classSpinner.setAdapter(arrayAdapter);
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // меняем id класса
                classId = classesId[pos];
                // переводим спинер предметов в начальное положение
                if (subjectPosition != -1)// в начале не обнуляем переменную, чтобы вывести предыдущий предмет
                    subjectPosition = 0;
                // выводим предметы
                outSubjects();
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
    void outCabinets() {
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

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spiner_dropdown_element_lesson_redactor, stringCabinets);
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
    void outSubjects() {

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        // получаем предметы из бд
        Cursor cursor = db.getSubjectsByClassId(classId);

        // количество предметов
        final int count = cursor.getCount();
        // все тексты
        final String[] stringLessons;
        // тексты уроков
        final String[] stringOnlyLessons = new String[cursor.getCount()];
        // id уроков
        final long[] lessonsId = new long[cursor.getCount()];

        // дополнительные кнопки
        if (count == 0) {
            stringLessons = new String[cursor.getCount() + 2];
            stringLessons[stringLessons.length - 1] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject);
        } else {
            stringLessons = new String[cursor.getCount() + 3];
            stringLessons[stringLessons.length - 1] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_remove_subject);
            stringLessons[stringLessons.length - 2] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject);
        }
        // и нулевая строка
        stringLessons[0] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_select_subject);

        // получаем данные из курсора
        for (int i = 1; i < stringLessons.length - 2; i++) {
            cursor.moveToNext();
            lessonsId[i - 1] = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID));
            stringLessons[i] = cursor.getString(cursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
            stringOnlyLessons[i - 1] = stringLessons[i];
        }
        cursor.close();
        db.close();

        // адаптер для спиннера
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spiner_dropdown_element_lesson_redactor, stringLessons);
        adapter.setDropDownViewResource(R.layout.spiner_dropdown_element_lesson_redactor);
        subjectSpinner.setAdapter(adapter);
        // позиция
        if (subjectPosition == -1) {
            for (int i = 0; i < lessonsId.length; i++) {
                if (chosenSubjectId == lessonsId[i]) {
                    subjectPosition = i + 1;
                }
            }
        }
        subjectSpinner.setSelection(subjectPosition, false);
        // слушатель
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Log.i("TeachersApp", "LessonRedactorActivity - outSubjects onItemSelected " + pos);
                if (count != 0 && pos == stringLessons.length - 1) {
                    // вызываем диалог удаления предмета
                    Log.i("TeachersApp", "LessonRedactorActivity - remove subject");
                    //данные передаваемые в диалог
                    Bundle args = new Bundle();
                    args.putStringArray("stringOnlyLessons", stringOnlyLessons);
                    args.putLongArray("lessonsId", lessonsId);
                    //диалог по удалению предмета
                    RemoveSubjectDialogFragment removeDialog = new RemoveSubjectDialogFragment();
                    removeDialog.setArguments(args);
                    removeDialog.show(getFragmentManager(), "removeLessons");

                } else if ((count != 0 && stringLessons.length - 2 == pos) || (count == 0 && stringLessons.length - 1 == pos)) {
                    //диалог создания предмета
                    Log.i("TeachersApp", "LessonRedactorActivity - new lesson");
                    //данные для диалога
                    Bundle args = new Bundle();
                    // передаем длинну списка, чтобы при создании переместиться на последний созданный
                    args.putStringArray("stringLessons", stringLessons);
                    //диалог по созданию нового предмета
                    CreateSubjectDialogFragment subjectNameDialogFragment = new CreateSubjectDialogFragment();
                    subjectNameDialogFragment.setArguments(args);
                    subjectNameDialogFragment.show(getFragmentManager(), "createSubject");
                } else if (pos != 0) {
                    Log.i("TeachersApp", "LessonRedactorActivity - chosen lesson id = " + lessonsId[pos - 1]);
                    chosenSubjectId = lessonsId[pos - 1];
                } else {
                    Log.i("TeachersApp", "LessonRedactorActivity - no lesson selected");
                    chosenSubjectId = -1;
                }
            }
        });
    }

    // -- спиннер времени --
    void outTime() {
        //получаем стандартное время уроков
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        final int[][] time = db.getSettingsTime(1);
        db.close();

        // создаем тексты в пунктах списка
        final String textTime[] = new String[time.length];
        //SimpleDateFormat textTimeFormat = new SimpleDateFormat("H:m", Locale.getDefault());
        for (int i = 0; i < textTime.length; i++) {
            //'  4 урок 11:30 - 12:15  '
            textTime[i] = "  " + (i + 1) + " " + getResources().getString(R.string.lesson_redactor_activity_spinner_title_lesson) + " " +
                    time[i][0] + ":" + time[i][1] + " - " +
                    time[i][2] + ":" + time[i][3] + "  ";
        }


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spiner_dropdown_element_lesson_redactor, textTime);
        adapter.setDropDownViewResource(R.layout.spiner_dropdown_element_lesson_redactor);
        timeSpinner.setAdapter(adapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //при выборе пункта ставим в календари новое время
                calendarStartTime = new GregorianCalendar(
                        calendarStartTime.get(GregorianCalendar.YEAR),
                        calendarStartTime.get(GregorianCalendar.MONTH),
                        calendarStartTime.get(GregorianCalendar.DAY_OF_MONTH),
                        time[i][0],
                        time[i][1],
                        0
                );
                calendarEndTime = new GregorianCalendar(
                        calendarEndTime.get(GregorianCalendar.YEAR),
                        calendarEndTime.get(GregorianCalendar.MONTH),
                        calendarEndTime.get(GregorianCalendar.DAY_OF_MONTH),
                        time[i][2],
                        time[i][3],
                        0
                );
                Log.i("TeachersApp", "chooseStandardLesson :" + textTime[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //ставим выбранное время
        for (int i = 0; i < time.length; i++) {
            if (calendarStartTime.get(Calendar.HOUR_OF_DAY) == time[i][0] &&
                    calendarStartTime.get(Calendar.MINUTE) == time[i][1] &&
                    calendarEndTime.get(Calendar.HOUR_OF_DAY) == time[i][2] &&
                    calendarEndTime.get(Calendar.MINUTE) == time[i][3]
                    ) {
                Log.i("TeachersApp", "outTime:chooseTime:" + (i + 1));
                timeSpinner.setSelection(i, false);
            }
        }
    }

    // -- спиннер повторов --
    void outRepeats() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spiner_dropdown_element_lesson_redactor, repeatPeriodsNames);
        lessonRepeatSpinner.setAdapter(
                adapter);
        lessonRepeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                repeat = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        lessonRepeatSpinner.setSelection((int) repeat, false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        seatingTextUpdate();
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
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> arrayList = db.getNotPutLearnersIdByCabinetIdAndClassId(cabinetId, classId);
        if (arrayList.size() == 0) {
            seatingStateText.setText(R.string.lesson_redactor_activity_text_learners_ready);
            seatingStateText.setTextColor(Color.parseColor("#469500"));
        } else {
            seatingStateText.setText(R.string.lesson_redactor_activity_text_learners_not_ready);
            seatingStateText.setTextColor(getResources().getColor(R.color.colorAccentRed));
        }
        db.close();
    }


    // ----- обратная связь от диалога переименования предметов -----
    @Override
    public void lessonNameDialogMethod(int code, int position, String classNameText) {
        if (code == 1) {
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            db.createSubject(classNameText, classId);
            db.close();
            // выбираем последний созданный урок
            subjectPosition = position;
        }
        // выводим предметы
        outSubjects();
    }

    // ----- обратная связь от диалога удаления предметов -----
    @Override
    public void removeSubjects(int message, ArrayList<Long> deleteList) {
        if (message == 0) {
            (new DataBaseOpenHelper(this)).deleteSubjects(deleteList);
            subjectPosition = 0;
            outSubjects();
        } else {
            outSubjects();
        }
    }
}