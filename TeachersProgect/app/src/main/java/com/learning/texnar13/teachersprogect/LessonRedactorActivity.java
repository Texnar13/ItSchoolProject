package com.learning.texnar13.teachersprogect;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LessonRedactorActivity extends AppCompatActivity {

    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    public static final String LESSON_START_TIME = "lessonStartTime";
    public static final String LESSON_END_TIME = "lessonEndTime";

    long attitudeId = -1;//todo почему она не нужна?

    //класс-кабинет
    TextView seatingStateText;
    final ClassCabinet classCabinetId = new ClassCabinet(1, 1);//todo предусмотреть вариант если уроков вобще нет или у принимаемого урока не стандартное время. проверка нет ли на этом времени урока. переворот экрана.
    //урок
    long chosenLessonId = -1;
    Spinner lessonNameSpinner;
    //время
    LinearLayout timeOut;
    LessonTimePeriod lessonTime;

    long repeat = 0;
    String[] repeatPeriodsNames = {"никогда", "ежедневно", "еженедельно"
            //, "ежемесячно"//todo
    };
    boolean isTmeYours = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_redactor);
        attitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -2);
        if (attitudeId == -2) {
            Toast toast = Toast.makeText(this, "не выбран урок!", Toast.LENGTH_SHORT);
            toast.show();
            finish();
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);


        //класс-кабинет
        Spinner classSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_class_spinner);
        Spinner cabinetSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_cabinet_spinner);
        seatingStateText = (TextView) findViewById(R.id.activity_lesson_redactor_seating_state);
        Button editSeatingButton = (Button) findViewById(R.id.activity_lesson_redactor_seating_redactor_button);
        //урок
        lessonNameSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_lesson_name_spinner);
        //время
        timeOut = (LinearLayout) findViewById(R.id.activity_lesson_redactor_time_layout);
        CheckBox timeCheckBox = (CheckBox) findViewById(R.id.activity_lesson_redactor_time_check_box);
        Spinner lessonRepeatSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_lesson_repeat_spinner);
        //общие
        TextView title = (TextView) findViewById(R.id.activity_lesson_redactor_title);
        LinearLayout buttonsOut = (LinearLayout) findViewById(R.id.activity_lesson_redactor_buttons_out);
        Button removeButton = (Button) findViewById(R.id.activity_lesson_redactor_remove_button);
        Button backButton = (Button) findViewById(R.id.activity_lesson_redactor_back_button);
        Button saveButton = (Button) findViewById(R.id.activity_lesson_redactor_save_button);


        lessonTime = new LessonTimePeriod(new GregorianCalendar(), new GregorianCalendar());
        if (attitudeId == -1) {
            title.setText("Создание урока");
            lessonTime.calendarStartTime.setTime(new Date(getIntent().getLongExtra(LESSON_START_TIME, 1)));
            lessonTime.calendarEndTime.setTime(new Date(getIntent().getLongExtra(LESSON_END_TIME, 1)));
        } else {
            title.setText("Редактирование урока");

            Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(attitudeId);
            attitudeCursor.moveToFirst();
            attitudeId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID));

            chosenLessonId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
            Cursor lessonCursor = db.getSubjectById(chosenLessonId);
            lessonCursor.moveToFirst();

            lessonTime.calendarStartTime.setTime(new Date(attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_DATE_BEGIN))));
            lessonTime.calendarEndTime.setTime(new Date(attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_DATE_END))));

            repeat = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT));

            classCabinetId.classId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
            classCabinetId.cabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));


            lessonCursor.close();
            attitudeCursor.close();
        }

        //кнопка рассадить учеников
        editSeatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
                intent.putExtra(SeatingRedactorActivity.CLASS_ID, classCabinetId.classId);
                intent.putExtra(SeatingRedactorActivity.CABINET_ID, classCabinetId.cabinetId);
                startActivityForResult(intent, 1);
            }
        });

        timeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isTmeYours = b;
                changeTimeFormat(isTmeYours);
            }
        });


        seatingTextUpdate(classCabinetId);//обновляем текст с информацией о рассадке
        changeTimeFormat(isTmeYours);//обновляем вывод настройки времени, по умолчанию стандартный


        {//вывод классов в спиннер
            Cursor classesCursor = db.getClasses();
            String[] stringClasses = new String[classesCursor.getCount()];
            final long[] classesId = new long[stringClasses.length];
            for (int i = 0; i < stringClasses.length; i++) {
                classesCursor.moveToNext();
                stringClasses[i] = classesCursor.getString(classesCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME));
                classesId[i] = classesCursor.getLong(classesCursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));
            }
            classesCursor.close();

            final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, stringClasses);
            adapter.setDropDownViewResource(R.layout.lesson_redactor_spiner_dropdown_item);
            classSpinner.setAdapter(adapter);
            classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    availableLessonsOut(classesId[pos], 0);
                    Log.i("TeachersApp", "LessonRedactorActivity - class spinner onItemSelected pos =" + pos + " id =" + classesId[pos]);
                    //classSpinner.setSelection(pos);
                    classCabinetId.classId = classesId[pos];
                    seatingTextUpdate(classCabinetId);
                    // Set adapter flag that something has been chosen
                    adapter.flag = true;
                }
            });

            for (int i = 0; i < classesId.length; i++) {//ставим текущий класс в спиннер
                if (classesId[i] == classCabinetId.classId) {
                    classSpinner.setSelection(i, false);
                }
            }
        }

        {//вывод кабинетов в спиннер
            Cursor cabinetsCursor = db.getCabinets();
            String[] stringCabinets = new String[cabinetsCursor.getCount()];
            final long[] cabinetsId = new long[stringCabinets.length];
            for (int i = 0; i < stringCabinets.length; i++) {
                cabinetsCursor.moveToNext();
                stringCabinets[i] = cabinetsCursor.getString(cabinetsCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME));
                cabinetsId[i] = cabinetsCursor.getLong(cabinetsCursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));
            }
            cabinetsCursor.close();

            final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, stringCabinets);
            adapter.setDropDownViewResource(R.layout.lesson_redactor_spiner_dropdown_item);
            cabinetSpinner.setAdapter(adapter);
            cabinetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Log.i("TeachersApp", "LessonRedactorActivity - cabinet spinner onItemSelected " + pos + "id =" + cabinetsId[pos]);
                    classCabinetId.cabinetId = cabinetsId[pos];
                    seatingTextUpdate(classCabinetId);

                    // Set adapter flag that something has been chosen
                    adapter.flag = true;
                }
            });

            for (int i = 0; i < cabinetsId.length; i++) {//ставим текущий класс в спиннер
                if (cabinetsId[i] == classCabinetId.cabinetId) {
                    cabinetSpinner.setSelection(i, false);
                }
            }
        }

        {//назначение повторений todo error
            final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, repeatPeriodsNames);

            //  адаптер
//            ArrayAdapter<?> adapter = new
//                    ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, repeatPeriodsNames);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            adapter.setDropDownViewResource(R.layout.lesson_redactor_spiner_dropdown_item);
            lessonRepeatSpinner
                    .setAdapter(
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

        final long finalAttitudeId = attitudeId;
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
                    db.deleteSubjectAndTimeCabinetAttitude(finalAttitudeId);
                    finish();
                }
            });
        }

        //сохранение изменений
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTmeYours) {//по формату вывода
                    if (chosenLessonId != -1) {//единственный пункт который может быть не выбран
                        if (finalAttitudeId == -1) {//создание
                            Log.i("TeachersApp", "LessonRedactorActivity - create lesson chosenLessonId =" + chosenLessonId + " cabinetId =" + classCabinetId.cabinetId + " calendarStartTime =" + lessonTime.calendarStartTime.getTime().getTime() + " calendarEndTime =" + lessonTime.calendarEndTime.getTime().getTime());
                            db.setLessonTimeAndCabinet(chosenLessonId, classCabinetId.cabinetId, lessonTime.calendarStartTime.getTime(), lessonTime.calendarEndTime.getTime(), repeat);
                            finish();
                        } else {//или изменение
                            Log.i("TeachersApp", "LessonRedactorActivity - edit lesson chosenLessonId =" + chosenLessonId + " cabinetId =" + classCabinetId.cabinetId + " calendarStartTime =" + lessonTime.calendarStartTime.getTime().getTime() + " calendarEndTime =" + lessonTime.calendarEndTime.getTime().getTime());
                            db.editLessonTimeAndCabinet(finalAttitudeId, chosenLessonId, classCabinetId.cabinetId, lessonTime.calendarStartTime.getTime(), lessonTime.calendarEndTime.getTime(), repeat);
                            finish();
                        }
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "не выбран урок!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });


        db.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        seatingTextUpdate(classCabinetId);
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

    void changeTimeFormat(boolean isTmeYours) {
        timeOut.removeAllViews();
        if (!isTmeYours) {
            //стандартное расписание
            Spinner spinner = new Spinner(this);

            //массив с calendar
            final String textTime[] = new String[ScheduleDayActivity.lessonStandardTimePeriods.length];
            SimpleDateFormat textTimeFormat = new SimpleDateFormat("H.m");
            for (int i = 0; i < textTime.length; i++) {
                textTime[i] = "  " + (i + 1) + " урок " +
                        textTimeFormat.format(ScheduleDayActivity.lessonStandardTimePeriods[i].calendarStartTime.getTime()) + "-" +

                        textTimeFormat.format(ScheduleDayActivity.lessonStandardTimePeriods[i].calendarEndTime.getTime()) + "  ";
            }


            final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, textTime);
            adapter.setDropDownViewResource(R.layout.lesson_redactor_spiner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    lessonTime = ScheduleDayActivity.lessonStandardTimePeriods[i];
                    Log.i("TeachersApp", "chooseStandardLesson :" + textTime[i]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            //ставим выбранное время
            for (int i = 0; i < ScheduleDayActivity.lessonStandardTimePeriods.length; i++) {
                if (lessonTime.calendarStartTime.get(Calendar.HOUR_OF_DAY) ==
                        ScheduleDayActivity.lessonStandardTimePeriods[i].calendarStartTime.get(Calendar.HOUR_OF_DAY) &&
                        lessonTime.calendarStartTime.get(Calendar.MINUTE) ==
                                ScheduleDayActivity.lessonStandardTimePeriods[i].calendarStartTime.get(Calendar.MINUTE) &&

                        lessonTime.calendarEndTime.get(Calendar.HOUR_OF_DAY) ==
                                ScheduleDayActivity.lessonStandardTimePeriods[i].calendarEndTime.get(Calendar.HOUR_OF_DAY) &&
                        lessonTime.calendarEndTime.get(Calendar.MINUTE) ==
                                ScheduleDayActivity.lessonStandardTimePeriods[i].calendarEndTime.get(Calendar.MINUTE)
                        ) {
                    Log.i("TeachersApp", "changeTimeFormat:chooseTime:" + (i + 1));
                    spinner.setSelection(i, false);
                }
            }


            LinearLayout.LayoutParams spinnerTimeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            spinnerTimeParams.gravity = Gravity.CENTER;

            timeOut.addView(spinner, spinnerTimeParams);
        } else {
            //своё время

        }
    }

    private class ClassCabinet {
        long classId;
        long cabinetId;

        ClassCabinet(long classId, long cabinetId) {
            this.classId = classId;
            this.cabinetId = cabinetId;
        }
    }

    //текст рассадки
    void seatingTextUpdate(ClassCabinet classCabinet) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> arrayList = db.getNotPutLearnersIdByCabinetIdAndClassId(classCabinet.cabinetId, classCabinet.classId);
        if (arrayList.size() == 0) {
            seatingStateText.setText("Ученики рассажены в этом кабинете!");
            seatingStateText.setTextColor(Color.parseColor("#469500"));
        } else {
            seatingStateText.setText("Ученики не рассажены!");
            seatingStateText.setTextColor(Color.parseColor("#ff7700"));
        }
    }

    //вывод предметов
    void availableLessonsOut(final long classViewId, final int position) {

        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor cursor = db.getSubjectsByClassId(classViewId);
        final int count = cursor.getCount();
        final String[] stringLessons;
        if (count == 0) {
            stringLessons = new String[cursor.getCount() + 2];
        } else {
            stringLessons = new String[cursor.getCount() + 3];
        }
        final String[] stringOnlyLessons = new String[cursor.getCount()];
        final long[] lessonsId = new long[cursor.getCount()];
        //Log.i("TeachersApp", "LessonRedactorActivity - " + stringLessons.length);
        for (int i = 1; i < stringLessons.length - 2; i++) {
            cursor.moveToNext();
            lessonsId[i - 1] = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID));
            stringLessons[i] = cursor.getString(cursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
            stringOnlyLessons[i - 1] = stringLessons[i];
        }

        if (count == 0) {
            stringLessons[stringLessons.length - 1] = "+ создать предмет для этого класса";
        } else {
            stringLessons[stringLessons.length - 1] = "-  удалить предмет(ы)";
            stringLessons[stringLessons.length - 2] = "+ создать предмет для этого класса";
        }

        stringLessons[0] = "выберите предмет:";
        cursor.close();
        db.close();

        final String[] finalStringLessons = stringLessons;
        final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, stringLessons);
        adapter.setDropDownViewResource(R.layout.lesson_redactor_spiner_dropdown_item);
        lessonNameSpinner.setAdapter(adapter);
        lessonNameSpinner.setSelection(position, false);
        lessonNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Log.i("TeachersApp", "LessonRedactorActivity - availableLessonsOut onItemSelected " + pos);
                if (count != 0 && finalStringLessons.length - 1 == pos) {
                    Log.i("TeachersApp", "LessonRedactorActivity - remove lesson");
                    DialogFragment removeDialog = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            final ArrayList<Integer> mSelectedItems = new ArrayList<>();
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("удаление предметов")
                                    .setMultiChoiceItems(stringOnlyLessons, null,
                                            new DialogInterface.OnMultiChoiceClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which,
                                                                    boolean isChecked) {
                                                    if (isChecked) {
                                                        // If the user checked the item, add it to the selected items
                                                        mSelectedItems.add(which);
                                                    } else if (mSelectedItems.contains(which)) {
                                                        // Else, if the item is already in the array, remove it
                                                        mSelectedItems.remove(Integer.valueOf(which));
                                                    }
                                                }
                                            })
                                    // Set the action buttons
                                    .setPositiveButton("удалить", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dismiss();
                                            ArrayList<Long> deleteList = new ArrayList<Long>(mSelectedItems.size());
                                            for (int itemPoz : mSelectedItems) {
                                                deleteList.add(lessonsId[itemPoz]);
                                            }
                                            db.deleteSubjects(deleteList);
                                            availableLessonsOut(classViewId, 0);
                                        }
                                    })
                                    .setNegativeButton("отмена", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            dismiss();
                                            availableLessonsOut(classViewId, position);
                                        }
                                    });
                            return builder.create();
                        }
                    };
                    removeDialog.show(getFragmentManager(), "removeLessons");
                } else if ((count != 0 && stringLessons.length - 2 == pos) || (count == 0 && finalStringLessons.length - 1 == pos)) {
                    //диалог создания предмета
                    Log.i("TeachersApp", "LessonRedactorActivity - new lesson");
                    DialogFragment lessonNameDialog = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            LinearLayout out = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_lesson_redactor_dialog_lesson_name, null);
                            final EditText lessonNameEditText = (EditText) out.findViewById(R.id.activity_lesson_redactor_dialog_lesson_name_edit_text);

                            builder.setTitle("добавление предмета");
                            builder.setView(out)
                                    .setPositiveButton("добавить", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            db.createSubject(lessonNameEditText.getText().toString(), classCabinetId.classId);
                                            availableLessonsOut(classCabinetId.classId, stringLessons.length - 2);
                                            dismiss();
                                        }
                                    })
                                    .setNegativeButton("отмена", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            availableLessonsOut(classCabinetId.classId, position);
                                            dismiss();

                                        }
                                    });

                            return builder.create();
                        }
                    };
                    lessonNameDialog.show(getFragmentManager(), "createSubject");
                } else if (pos != 0) {
                    Log.i("TeachersApp", "LessonRedactorActivity - chosen lesson id = " + lessonsId[pos - 1]);
                    chosenLessonId = lessonsId[pos - 1];
                } else {
                    Log.i("TeachersApp", "LessonRedactorActivity - no lesson selected");
                    chosenLessonId = -1;
                }

                // Set adapter flag that something has been chosen
                adapter.flag = true;
            }
        });
        //spinner.setSelection(2);//элемент по умолчанию
    }


}

class CustomAdapter extends ArrayAdapter {
    private Context context;
    private int textViewResourceId;
    private String[] objects;
    //private boolean isFirstElementVisible;
    boolean flag = false;

    CustomAdapter(Context context, int textViewResourceId, String[] objects //,boolean isFirstElementVisible
    ) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.objects = objects;
        //this.isFirstElementVisible = isFirstElementVisible;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context, textViewResourceId, null);
        //if (flag || isFirstElementVisible) {
        TextView tv = (TextView) convertView;
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.parseColor("#e4ea7e"));//светло салатовый
        //tv.setBackgroundColor(Color.WHITE);//светло салатовый
        tv.setText(objects[position]);
        //}
        return convertView;
    }
}


