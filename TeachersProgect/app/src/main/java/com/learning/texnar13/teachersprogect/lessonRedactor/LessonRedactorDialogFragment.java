package com.learning.texnar13.teachersprogect.lessonRedactor;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.SubjectsDialog.SubjectsDialogFragment;
import com.learning.texnar13.teachersprogect.SubjectsDialog.SubjectsDialogInterface;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;
import com.yandex.mobile.ads.AdSize;

import java.util.ArrayList;
import java.util.Locale;

public class LessonRedactorDialogFragment extends DialogFragment implements SubjectsDialogInterface {


    // id передаваемых данных (трансферные константы)
    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    public static final String LESSON_CHECK_DATE = "lessonDate";
    public static final String LESSON_NUMBER = "lessonNumber";

    //todo предусмотреть вариант если уроков вообще нет. проверка нет ли на этом времени урока. (получать через метод возвращающий дату пересечения с другими уроками)

    // полученное извне время урока
    String checkLessonDate;
    int lessonNumber;

    // массив с текстами времени
    String[] timeTexts;
    // массив с текстами повторов
    String[] repeatTexts;
    // массив с классами
    LearnersClassUnit_1[] classUnits;
    // массив с кабинетами
    static CabinetUnit_1[] cabinetUnits;

    LessonUnit_1 lessonUnit;


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


    @NonNull
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
        RelativeLayout out = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.lesson_redactor_activity, null);
        builder.setView(out);

        // --- загрузка рекламы яндекса ---
        com.yandex.mobile.ads.AdView mAdView = out.findViewById(R.id.activity_lesson_redactor_banner);
        mAdView.setBlockId(getResources().getString(R.string.banner_id_lesson_redactor));
        mAdView.setAdSize(AdSize.BANNER_320x100);
        // Загрузка объявления.
        final com.yandex.mobile.ads.AdRequest adRequest = new com.yandex.mobile.ads.AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // ---------------------------------- получение аргументов ---------------------------------
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        // id зависимости 
        long dbAttitudeId = args.getLong(LESSON_ATTITUDE_ID, -2L);
        if (dbAttitudeId == -2) {
            Toast toast = Toast.makeText(context, "error", Toast.LENGTH_SHORT);
            toast.show();
            dismiss();
            return null;
        }

        // получаем время из intent
        checkLessonDate = args.getString(LESSON_CHECK_DATE);
        lessonNumber = args.getInt(LESSON_NUMBER, 0);


        // ------------------------------ получение данных урока из бд -----------------------------
        DataBaseOpenHelper db = new DataBaseOpenHelper(context);

        long tempSubjectId;
        long tempClassId;
        long tempCabinetId;
        int tempRepeat;
        HomeWorkUnit_1 tempHomeworkUnit;

        // получаем данные урока из базы данных
        if (dbAttitudeId != -1) {
            // заголовок
            ((TextView) out.findViewById(R.id.activity_lesson_redactor_head_text)).setText(R.string.title_activity_lesson_redactor_edit);

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
                tempHomeworkUnit = new HomeWorkUnit_1(
                        homework.getLong(homework.getColumnIndex(SchoolContract.TableLessonComment.KEY_LESSON_TEXT_ID)),
                        homework.getString(homework.getColumnIndex(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT))
                );
            } else {
                tempHomeworkUnit = null;
            }
            homework.close();

        } else {
            // раз нет id, это создание урока
            ((TextView) out.findViewById(R.id.activity_lesson_redactor_head_text)).setText(R.string.title_activity_lesson_redactor_create);

            tempClassId = -1;
            tempSubjectId = -1;
            tempCabinetId = -1;
            tempRepeat = 0;

            // дз
            tempHomeworkUnit = null;
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
                timeTexts[i] = String.format(Locale.getDefault(),
                        "  %2d-%s  %2d:%02d - %2d:%02d  ",
                        i + 1,
                        getResources().getString(R.string.lesson_redactor_activity_spinner_title_lesson),
                        time[i][0],
                        time[i][1],
                        time[i][2],
                        time[i][3]
                );
            }
        }

        // повторы
        repeatTexts = new String[]{
                getActivity().getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_never),
                getActivity().getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_daily),
                getActivity().getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_weekly)
        };

        {// классы
            Cursor classesCursor = db.getLearnersClass();

            classUnits = new LearnersClassUnit_1[classesCursor.getCount()];
            for (int i = 0; i < classUnits.length; i++) {
                classesCursor.moveToNext();
                long classId = classesCursor.getLong(classesCursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));


                // получаем предметы
                Cursor subjectsCursor = db.getSubjectsByClassId(classId);
                ArrayList<SubjectUnit_1> subjects = new ArrayList<>(subjectsCursor.getCount());
                for (int subjectsI = 0; subjectsI < subjectsCursor.getCount(); subjectsI++) {
                    subjectsCursor.moveToNext();

                    subjects.add(new SubjectUnit_1(
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
                classUnits[i] = new LearnersClassUnit_1(
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

            cabinetUnits = new CabinetUnit_1[cabinetsCursor.getCount()];
            for (int i = 0; i < cabinetUnits.length; i++) {
                cabinetsCursor.moveToNext();
                cabinetUnits[i] = new CabinetUnit_1(
                        cabinetsCursor.getLong(cabinetsCursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID)),
                        cabinetsCursor.getString(cabinetsCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME))
                );

                // считаем позицию выбранного в уроке
                if (tempCabinetId == cabinetUnits[i].cabinetId) {
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
        if (cabinetUnits.length != 0 && tempCabinetPos == -1) {
            tempCabinetPos = 0;
        }


        // --------------------------------- создаем обьект урока ----------------------------------

        lessonUnit = new LessonUnit_1(
                dbAttitudeId,
                tempRepeat,
                tempClassPos,
                tempCabinetPos,
                tempSubjectPos,
                tempHomeworkUnit
        );

        // ----------------------------------- компоненты экрана -----------------------------------

        // текст отображаемой даты
        ((TextView) out.findViewById(R.id.activity_lesson_redactor_current_date_text)).setText(
                //getResources().getTextArray(R.array.week_days_simple)[calendar.get(Calendar.DAY_OF_WEEK) - 1] + ", "+
                Integer.parseInt(checkLessonDate.substring(8, 10)) + " " +
                        getResources().getStringArray(R.array.months_names_with_ending)[Integer.parseInt(checkLessonDate.substring(5, 7)) - 1]
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

        // текстовое поле дз
        homeworkEdit = out.findViewById(R.id.activity_lesson_redactor_homework_text_edit);
        // выставляем предыдущий текст
        if (lessonUnit.homework != null){
            homeworkEdit.setText(lessonUnit.homework.homeworkString);
        }else{
            homeworkEdit.setText("");
        }
        // ставим ограничение в 3 строки (70 символов)
        homeworkEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(70)});

        // ---------------------------------- настраиваем кнопки -----------------------------------

        // кнопка удаления урока
        TextView removeButton = out.findViewById(R.id.activity_lesson_redactor_remove_button);
        if (lessonUnit.attitudeId == -1) {
            ((LinearLayout) out.findViewById(R.id.activity_lesson_redactor_buttons_container)).removeView(removeButton);
        } else {
            //buttonsOut.removeView(backButton);
            // удаление урока
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataBaseOpenHelper db = new DataBaseOpenHelper(context);
                    db.deleteSubjectAndTimeCabinetAttitude(lessonUnit.attitudeId);
                    db.close();
                    // вызываем метод обновления активности из которой был вызван диалог
                    ((CloseEditLessonDialogInterface) getActivity()).onSaveLessonParams();
                    // закрываем диалог
                    dismiss();
                }
            });
        }

        // ---- кнопка выбора предмета ----
        subjectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lessonUnit.chosenClassPosition != -1) {
                    // создаем диалог работы с предметами
                    SubjectsDialogFragment subjectsDialogFragment = new SubjectsDialogFragment();
                    // готоим и передаем массив названий предметов и позицию выбранного предмета
                    Bundle args = new Bundle();
                    String[] subjectsArray = new String[classUnits[lessonUnit.chosenClassPosition].subjects.size()];
                    for (int subjectI = 0; subjectI < subjectsArray.length; subjectI++) {
                        subjectsArray[subjectI] = classUnits[lessonUnit.chosenClassPosition].subjects.get(subjectI).subjectName;
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

                if (lessonUnit.chosenClassPosition == -1) {
                    Toast toast = Toast.makeText(context, R.string.lesson_redactor_activity_toast_text_class_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                } else if (lessonUnit.chosenCabinetPosition == -1) {
                    Toast toast = Toast.makeText(context, R.string.lesson_redactor_activity_toast_text_cabinet_not_chosen, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Intent intent = new Intent(context, SeatingRedactorActivity.class);
                    intent.putExtra(SeatingRedactorActivity.CLASS_ID, classUnits[lessonUnit.chosenClassPosition].classId);
                    intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetUnits[lessonUnit.chosenCabinetPosition].cabinetId);
                    startActivity(intent);
                }
            }
        });

        // ---- кнопка сохранения изменений ----
        RelativeLayout saveButton = out.findViewById(R.id.activity_lesson_redactor_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lessonUnit.chosenCabinetPosition != -1) {//выбран ли кабинет
                    if (lessonUnit.chosenSubjectPosition != -1) {//выбран ли предмет

                        DataBaseOpenHelper db = new DataBaseOpenHelper(context);

                        // сохраняем урок
                        if (lessonUnit.attitudeId == -1) {//создание
                            lessonUnit.attitudeId = db.createLessonTimeAndCabinetAttitude(
                                    classUnits[lessonUnit.chosenClassPosition].subjects.get(lessonUnit.chosenSubjectPosition).subjectId,
                                    cabinetUnits[lessonUnit.chosenCabinetPosition].cabinetId,
                                    checkLessonDate,
                                    lessonNumber,
                                    lessonUnit.repeat
                            );
                        } else {//или изменение
                            db.editLessonTimeAndCabinet(
                                    lessonUnit.attitudeId,
                                    classUnits[lessonUnit.chosenClassPosition].subjects.get(lessonUnit.chosenSubjectPosition).subjectId,
                                    cabinetUnits[lessonUnit.chosenCabinetPosition].cabinetId,
                                    lessonNumber,
                                    lessonUnit.repeat
                            );
                        }

                        // сохраняем дз
                        String lessonComment = homeworkEdit.getText().toString().trim();
                        if (lessonComment.length() > 0) {
                            if (lessonUnit.homework == null) {
                                db.createLessonComment(
                                        checkLessonDate,
                                        lessonUnit.attitudeId,
                                        lessonComment
                                );
                            } else {
                                db.setLessonCommentStringById(
                                        lessonUnit.homework.homeworkId,
                                        lessonComment
                                );
                            }
                        }
                        db.close();
                        // вызываем метод обновления активности из которой был вызван диалог
                        ((CloseEditLessonDialogInterface) getActivity()).onSaveLessonParams();
                        // закрываем диалог
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

        // обновляем текст с информацией о рассадке
        seatingTextUpdate();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // сообщаем активности, что диалог закрылся
        ((CloseEditLessonDialogInterface) getActivity()).onEditLessonDialogClose();
    }

    // ==================== методы обновления ====================

    // вывод классов в спиннер
    void outClasses() {

        // преобразуем в строковый массив
        String[] stringClasses = new String[classUnits.length];
        for (int classI = 0; classI < classUnits.length; classI++) {
            stringClasses[classI] = classUnits[classI].className;
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.lesson_redactor_spinner_dropdown_element, stringClasses);
        classSpinner.setAdapter(arrayAdapter);
        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // меняем позицию выбранного класса
                lessonUnit.chosenClassPosition = pos;
                // выводим предметы
                getAndOutSubjects();
                Log.i("TeachersApp", "LessonRedactorActivity - class spinner onItemSelected pos =" + pos + " id =" + classUnits[pos]);
                seatingTextUpdate();
            }
        });

        if (classUnits.length != 0) {
            if (lessonUnit.chosenClassPosition == -1)
                lessonUnit.chosenClassPosition = 0;
            classSpinner.setSelection(lessonUnit.chosenClassPosition, false);
        }
    }

    // вывод кабинетов в спиннер 
    void getAndOutCabinets() {
        // преобразуем в строковый массив
        String[] stringCabinets = new String[cabinetUnits.length];
        for (int cabinetI = 0; cabinetI < cabinetUnits.length; cabinetI++) {
            stringCabinets[cabinetI] = cabinetUnits[cabinetI].cabinetName;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.lesson_redactor_spinner_dropdown_element, stringCabinets);
        cabinetSpinner.setAdapter(adapter);
        cabinetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // меняем позицию выбранного кабинета
                lessonUnit.chosenCabinetPosition = pos;
                seatingTextUpdate();
            }
        });


        if (cabinetUnits.length != 0) {
            if (lessonUnit.chosenCabinetPosition == -1)
                lessonUnit.chosenCabinetPosition = 0;
            cabinetSpinner.setSelection(lessonUnit.chosenCabinetPosition, false);
        }
    }

    // спиннер времени 
    void getAndOutTime() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.lesson_redactor_spinner_dropdown_element, timeTexts);
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
        if (classUnits[lessonUnit.chosenClassPosition].subjects.size() == 0) {
            subjectText.setText(R.string.lesson_redactor_activity_spinner_text_create_subject);
            lessonUnit.chosenSubjectPosition = -1;
        } else {
            subjectText.setText(classUnits[lessonUnit.chosenClassPosition].subjects.get(lessonUnit.chosenSubjectPosition).subjectName);
        }

    }

    // спиннер повторов
    void getAndOutRepeats() {


        repeatSpinner.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.lesson_redactor_spinner_dropdown_element, repeatTexts));
        repeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // при выборе пункта меняем выбранный урок
                lessonUnit.repeat = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (lessonUnit.repeat == -1) {
            lessonUnit.repeat = 0;
        }
        repeatSpinner.setSelection(lessonUnit.repeat, false);

    }

    // текст рассадки 
    void seatingTextUpdate() {

        // проверяем рассажены ли ученики
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
        ArrayList<Long> arrayList = db.getNotPutLearnersIdByCabinetIdAndClassId(
                cabinetUnits[lessonUnit.chosenCabinetPosition].cabinetId,
                classUnits[lessonUnit.chosenClassPosition].classId
        );
        if (arrayList.size() == 0) {// если да
            seatingStateImage.setImageResource(R.drawable.lesson_redactor_activity_icon_correct);
        } else {// если нет
            seatingStateImage.setImageResource(R.drawable.lesson_redactor_activity_icon_wrong);
        }
        db.close();
    }


    // ----- обратная связь от диалога предметов -----

    @Override
    public void setSubjectPosition(int position) {
        lessonUnit.chosenSubjectPosition = position;
        subjectText.setText(classUnits[lessonUnit.chosenClassPosition].subjects.get(position).subjectName);
    }

    @Override
    public void createSubject(String name, int position) {
        // создаем предмет в базе данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
        long createdSubjectId = db.createSubject(name, classUnits[lessonUnit.chosenClassPosition].classId);
        db.close();
        // добавляем предмет в список под нужным номером
        classUnits[lessonUnit.chosenClassPosition].subjects.add(position, new SubjectUnit_1(createdSubjectId, name));
        // выбираем этот предмет и ставим его имя в заголовок
        lessonUnit.chosenSubjectPosition = position;
        subjectText.setText(classUnits[lessonUnit.chosenClassPosition].subjects.get(lessonUnit.chosenSubjectPosition).subjectName);
    }

    @Override
    public void deleteSubjects(boolean[] deleteList) {
        // удаляем предметы
        ArrayList<Long> deleteId = new ArrayList<>();
        // пробегаемся в обратном направлении, удаляя записи из листа и
        for (int subjectI = classUnits[lessonUnit.chosenClassPosition].subjects.size() - 1; subjectI >= 0; subjectI--) {
            if (deleteList[subjectI]) {
                // добавляем id предмета в расстрельный список
                deleteId.add(0, classUnits[lessonUnit.chosenClassPosition].subjects.get(subjectI).subjectId);
                // удаляем предмет из листа
                classUnits[lessonUnit.chosenClassPosition].subjects.remove(subjectI);

                // смотрим не удален ли выбранный предмет
                if (lessonUnit.chosenSubjectPosition == subjectI) {
                    lessonUnit.chosenSubjectPosition = -1;
                    // ставим выбор на первом предмете
                    if (classUnits[lessonUnit.chosenClassPosition].subjects.size() != 0) {
                        lessonUnit.chosenSubjectPosition = 0;
                        // выводим название предмета
                        subjectText.setText(
                                classUnits[lessonUnit.chosenClassPosition].subjects.get(lessonUnit.chosenSubjectPosition).subjectName
                        );

                    } else { // если проедметов в базе данных нет не выбираем ничего
                        // выводим текст о том, что предмета нет
                        subjectText.setText(getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject));
                    }
                }
            }
        }

        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
        // прежде чем удалить предметы, заменим в текущем уроке предмет на первый в списке
        if ((cabinetUnits[lessonUnit.chosenCabinetPosition].cabinetId != -1) && (lessonUnit.chosenSubjectPosition != -1) && (lessonUnit.attitudeId != -1)) {
            db.editLessonTimeAndCabinet(
                    lessonUnit.attitudeId,
                    classUnits[lessonUnit.chosenClassPosition].subjects.get(lessonUnit.chosenSubjectPosition).subjectId,
                    cabinetUnits[lessonUnit.chosenCabinetPosition].cabinetId,
                    lessonNumber,
                    lessonUnit.repeat
            );
        }
        db.deleteSubjects(deleteId);
        db.close();
    }

    @Override
    public void renameSubjects(String[] newSubjectsNames) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());

        // в цикле переименовываем все предметы в массиве активности
        for (int subjectI = 0; subjectI < classUnits[lessonUnit.chosenClassPosition].subjects.size(); subjectI++) {

            classUnits[lessonUnit.chosenClassPosition].subjects.get(subjectI).subjectName = newSubjectsNames[subjectI];

            // и сохраняем все изменения в базу данных
            db.setSubjectName(
                    classUnits[lessonUnit.chosenClassPosition].subjects.get(subjectI).subjectId,
                    classUnits[lessonUnit.chosenClassPosition].subjects.get(subjectI).subjectName
            );
        }
        db.close();

        // сортируем текущий список (пузырьком)
        for (int out = classUnits[lessonUnit.chosenClassPosition].subjects.size() - 1; out > 0; out--) {
            for (int in = 0; in < out; in++) {
                if (classUnits[lessonUnit.chosenClassPosition].subjects.get(in).subjectName.compareTo(classUnits[lessonUnit.chosenClassPosition].subjects.get(in + 1).subjectName) > 0) {
                    if (lessonUnit.chosenSubjectPosition == in) {
                        lessonUnit.chosenSubjectPosition = in + 1;
                    } else if (lessonUnit.chosenSubjectPosition == in + 1) {
                        lessonUnit.chosenSubjectPosition = in;
                    }
                    SubjectUnit_1 temp = classUnits[lessonUnit.chosenClassPosition].subjects.get(in + 1);
                    classUnits[lessonUnit.chosenClassPosition].subjects.set(in + 1, classUnits[lessonUnit.chosenClassPosition].subjects.get(in));
                    classUnits[lessonUnit.chosenClassPosition].subjects.set(in, temp);
                }
            }
        }

        // обновляем надпись на тексте с названием предмета
        if (classUnits[lessonUnit.chosenClassPosition].subjects.size() == 0) {
            subjectText.setText(R.string.lesson_redactor_activity_spinner_text_create_subject);
            lessonUnit.chosenSubjectPosition = -1;
        } else {
            subjectText.setText(classUnits[lessonUnit.chosenClassPosition].subjects.get(lessonUnit.chosenSubjectPosition).subjectName);
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
    ArrayList<SubjectUnit_1> subjects;

    public LearnersClassUnit(long classId, String className, ArrayList<SubjectUnit_1> subjects) {
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
    HomeWorkUnit_1 homework;

    public LessonUnit(long attitudeId, int repeat, int chosenClassPosition, int chosenCabinetPosition, int chosenSubjectPosition, HomeWorkUnit_1 homework) {
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