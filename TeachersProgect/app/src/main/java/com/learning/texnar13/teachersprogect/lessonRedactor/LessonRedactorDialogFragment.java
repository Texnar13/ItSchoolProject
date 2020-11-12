package com.learning.texnar13.teachersprogect.lessonRedactor;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.LessonRedactorActivity;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.SubjectsDialog.SubjectsDialogFragment;
import com.learning.texnar13.teachersprogect.SubjectsDialog.SubjectsDialogInterface;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;
import com.yandex.mobile.ads.AdSize;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class LessonRedactorDialogFragment extends DialogFragment implements SubjectsDialogInterface {


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
    Spinner repeatSpinner;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // получаем контекст
        final Activity context = getActivity();
        if (context == null) {
            dismiss();
            return null;
        }

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // layout диалога 
        RelativeLayout out = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.activity_lesson_redactor, null);
        builder.setView(out);

        // --- загрузка рекламы яндекса ---
        com.yandex.mobile.ads.AdView mAdView = out.findViewById(R.id.activity_lesson_redactor_banner);
        mAdView.setBlockId(getResources().getString(R.string.banner_id_lesson_redactor));
        mAdView.setAdSize(AdSize.BANNER_320x100);
        // Загрузка объявления.
        final com.yandex.mobile.ads.AdRequest adRequest = new com.yandex.mobile.ads.AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // ------------------------------------ получение данных -----------------------------------
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        // id зависимости 
        attitudeId = args.getLong(LESSON_ATTITUDE_ID, -2L);
        if (attitudeId == -2) {
            Toast toast = Toast.makeText(context, "error", Toast.LENGTH_SHORT);
            toast.show();
            dismiss();
            return null;
        }
        // получаем время из intent
        lessonDate = args.getString(LESSON_DATE);
        lessonNumber = args.getInt(LESSON_NUMBER, 0);
        // TODO получать здесь списки сназваниями времени, классов, предметов, кабинетов
        // получаем остальное из базы данных 
        if (attitudeId != -1) {
            ((TextView)out.findViewById(R.id.activity_lesson_redactor_head_text)).setText(R.string.title_activity_lesson_redactor_edit);

            DataBaseOpenHelper db = new DataBaseOpenHelper(context);
            // главная зависимость(урок)
            Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(attitudeId);
            attitudeCursor.moveToFirst();
            //  предмет 
            long chosenSubjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
            Cursor subjectCursor = db.getSubjectById(chosenSubjectId);
            subjectCursor.moveToFirst();
            //  id класса(получаем из предмета) 
            classId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
            //  id кабинета 
            cabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
            //  повторы 
            repeat = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT));
            subjectCursor.close();
            attitudeCursor.close();
            db.close();
        }else{
            // раз нет id, это создание урока
            ((TextView)out.findViewById(R.id.activity_lesson_redactor_head_text)).setText(R.string.title_activity_lesson_redactor_create);
        }


        // ----------------------------------- компоненты экрана -----------------------------------

        // текст отображаемой даты
        ((TextView) out.findViewById(R.id.activity_lesson_redactor_current_date_text)).setText(
                //getResources().getTextArray(R.array.week_days_simple)[calendar.get(Calendar.DAY_OF_WEEK) - 1] + ", "+
                Integer.parseInt(lessonDate.substring(8, 10)) + " " +
                        getResources().getStringArray(R.array.months_names_low_case)[Integer.parseInt(lessonDate.substring(5, 7)) - 1]
        );
//        {
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//            GregorianCalendar calendar = new GregorianCalendar();
//            try {
//                calendar.setTime(simpleDateFormat.parse(lessonDate));
//            } catch (ParseException e) {
//                e.printStackTrace();
//                calendar.setTime(new Date());
//            }
//            ((TextView) out.findViewById(R.id.activity_lesson_redactor_current_date_text)).setText(
//                    getResources().getTextArray(R.array.week_days_simple)[calendar.get(Calendar.DAY_OF_WEEK) - 1] + ", "
//                            + calendar.get(Calendar.DAY_OF_MONTH) + " " + getResources().getTextArray(R.array.months_names_low_case)[calendar.get(Calendar.MONTH)]
//            );
//        }

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
        subjectText = out.findViewById(R.id.activity_lesson_redactor_lesson_name_text_button);
        subjectText.setPaintFlags(subjectText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        //  спиннер классов 
        classSpinner = out.findViewById(R.id.activity_lesson_redactor_class_spinner);
        outClasses();

        //  спиннер кабинетов 
        cabinetSpinner = out.findViewById(R.id.activity_lesson_redactor_cabinet_spinner);
        getAndOutCabinets();

        //  спиннер времени 
        timeSpinner = out.findViewById(R.id.activity_lesson_redactor_time_spinner);
        getAndOutTime();

        //  спиннер повторов 
        repeatSpinner = out.findViewById(R.id.activity_lesson_redactor_repeat_spinner);
        getAndOutRepeats();


        // ---------------------------------- настраиваем кнопки -----------------------------------

        // кнопка удаления урока
        TextView removeButton = out.findViewById(R.id.activity_lesson_redactor_remove_button);
        if (attitudeId == -1) {
            ((LinearLayout) out.findViewById(R.id.activity_lesson_redactor_buttons_container)).removeView(removeButton);
        } else {
            //buttonsOut.removeView(backButton);
            // удаление урока
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataBaseOpenHelper db = new DataBaseOpenHelper(context);
                    db.deleteSubjectAndTimeCabinetAttitude(attitudeId);
                    db.close();
                    dismiss();
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

                        subjectsArray[subjectI] = subjects.get(subjectI).subjectName;
                    }
                    args.putStringArray(SubjectsDialogFragment.ARGS_LEARNERS_NAMES_STRING_ARRAY, subjectsArray);
                    subjectsDialogFragment.setArguments(args);

                    // показываем диалог
                    subjectsDialogFragment.show(getFragmentManager(), "subjectsDialogFragment - hello");
                }
            }
        });

        // ---- кнопка рассадить учеников ----
        RelativeLayout editSeatingButton = out.findViewById(R.id.activity_lesson_redactor_seating_redactor_button);
        seatingStateImage = out.findViewById(R.id.activity_lesson_redactor_seating_state);
        editSeatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (classId == -1) {
                    Toast toast = Toast.makeText(context, R.string.lesson_redactor_activity_toast_text_class_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                } else if (cabinetId == -1) {
                    Toast toast = Toast.makeText(context, R.string.lesson_redactor_activity_toast_text_cabinet_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Intent intent = new Intent(context, SeatingRedactorActivity.class);
                    intent.putExtra(SeatingRedactorActivity.CLASS_ID, classId);
                    intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
                    startActivity(intent);
                }
            }
        });

        // ---- кнопка сохранения изменений ----
        RelativeLayout saveButton = out.findViewById(R.id.activity_lesson_redactor_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cabinetId != -1) {//выбран ли кабинет
                    if (chosenSubjectPosition != -1) {//выбран ли предмет
                        if (attitudeId == -1) {//создание
                            Log.i("TeachersApp", "LessonRedactorActivity - create lesson chosenSubjectId =" + subjects.get(chosenSubjectPosition).subjectId + " cabinetId =" + cabinetId + " lessonDate =" + lessonDate + " lessonNumber =" + lessonNumber);
                            DataBaseOpenHelper db = new DataBaseOpenHelper(context);
                            db.setLessonTimeAndCabinet(
                                    subjects.get(chosenSubjectPosition).subjectId,
                                    cabinetId,
                                    lessonDate,
                                    lessonNumber,
                                    repeat
                            );
                            db.close();
                        } else {//или изменение
                            Log.i("TeachersApp", "LessonRedactorActivity - edit lesson chosenSubjectId =" + subjects.get(chosenSubjectPosition).subjectId + " cabinetId =" + cabinetId + " lessonDate =" + lessonDate + " lessonNumber =" + lessonNumber);
                            DataBaseOpenHelper db = new DataBaseOpenHelper(context);
                            db.editLessonTimeAndCabinet(
                                    attitudeId,
                                    subjects.get(chosenSubjectPosition).subjectId,
                                    cabinetId,
                                    lessonDate,
                                    lessonNumber,
                                    repeat
                            );
                            db.close();
                        }
                        dismiss();
                    } else {
                        Toast toast = Toast.makeText(context, R.string.lesson_redactor_activity_toast_text_subject_not_chosen, Toast.LENGTH_LONG);
                        toast.show();
                    }
                } else {
                    Toast toast = Toast.makeText(context, R.string.lesson_redactor_activity_toast_text_cabinet_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        // кнопка назад
        out.findViewById(R.id.activity_lesson_redactor_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }


    @Override
    public void onStart() {
        super.onStart();

        // ------ обновляем текст с информацией о рассадке ------
        seatingTextUpdate();
    }


    // ==================== методы обновления ====================

    // вывод классов в спиннер
    void outClasses() {
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
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

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_element_lesson_redactor, stringClasses);
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

    // вывод кабинетов в спиннер 
    void getAndOutCabinets() {
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
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

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_element_lesson_redactor, stringCabinets);
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

    // спиннер времени 
    void getAndOutTime() {
        //получаем стандартное время уроков
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
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


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_element_lesson_redactor, textTime);
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

    // спиннер предметов 
    void getAndOutSubjects() {
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
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
            subjectText.setText(subjects.get(chosenSubjectPosition).subjectName);
        }

    }

    // спиннер повторов
    void getAndOutRepeats() {

        String[] repeatNames = {
                getActivity().getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_never),
                getActivity().getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_daily),
                getActivity().getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_weekly)
        };
        repeatSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.spinner_dropdown_element_lesson_redactor, repeatNames));
        repeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // при выборе пункта меняем выбранный урок
                repeat = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (repeat == -1) {
            repeatSpinner.setSelection(lessonNumber, false);
        }
    }

    // текст рассадки 
    void seatingTextUpdate() {

        // проверяем рассажены ли ученики
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
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
        subjectText.setText(subjects.get(position).subjectName);
    }

    @Override
    public void createSubject(String name, int position) {
        // создаем предмет в базе данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
        long createdSubjectId = db.createSubject(name, classId);
        db.close();
        // добавляем предмет в список под нужным номером
        subjects.add(position, new SubjectUnit(createdSubjectId, name));
        // выбираем этот предмет и ставим его имя в заголовок
        chosenSubjectPosition = position;
        subjectText.setText(subjects.get(chosenSubjectPosition).subjectName);
    }

    @Override
    public void deleteSubjects(boolean[] deleteList) {
        // удаляем предметы
        ArrayList<Long> deleteId = new ArrayList<>();
        // пробегаемся в обратном направлении, для того, чтобы  параллельно удалять из листа
        for (int subjectI = subjects.size() - 1; subjectI >= 0; subjectI--) {
            if (deleteList[subjectI]) {
                // добавляем id предмета в расстрельный список
                deleteId.add(0, subjects.get(subjectI).subjectId);
                // удаляем предмет из листа
                subjects.remove(subjectI);

                // смотрим не удален ли выбранный предмет
                if (chosenSubjectPosition == subjectI) {
                    chosenSubjectPosition = -1;
                    // ставим выбор на первом предмете
                    if (subjects.size() != 0) {
                        chosenSubjectPosition = 0;
                        // выводим название предмета
                        subjectText.setText(subjects.get(chosenSubjectPosition).subjectName);

                    } else { // если проедметов в базе данных нет не выбираем ничего
                        // выводим текст о том, что предмета нет
                        subjectText.setText(getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject));
                    }
                }
            }
        }
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
        // прежде чем удалить предметы, заменим в текущем уроке предмет на первый в списке
        if ((cabinetId != -1) && (chosenSubjectPosition != -1) && (attitudeId != -1)) {
            db.editLessonTimeAndCabinet(
                    attitudeId,
                    subjects.get(chosenSubjectPosition).subjectId,
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
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());

        // в цикле переименовываем все предметы в массиве активности
        for (int subjectI = 0; subjectI < subjects.size(); subjectI++) {

            subjects.get(subjectI).subjectName = newSubjectsNames[subjectI];

            // и сохраняем все изменения в базу данных
            db.setSubjectName(
                    subjects.get(subjectI).subjectId,
                    subjects.get(subjectI).subjectName
            );
        }
        db.close();

        // сортируем текущий список (пузырьком)
        for (int out = subjects.size() - 1; out > 0; out--) {
            for (int in = 0; in < out; in++) {
                if (subjects.get(in).subjectName.compareTo(subjects.get(in + 1).subjectName) > 0) {
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
            subjectText.setText(subjects.get(chosenSubjectPosition).subjectName);
        }
    }


    // класс для хранения предмета
    static class SubjectUnit {
        long subjectId;
        String subjectName;

        SubjectUnit(long subjectId, String subjectName) {
            this.subjectId = subjectId;
            this.subjectName = subjectName;
        }
    }
}

