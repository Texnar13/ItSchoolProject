package com.learning.texnar13.teachersprogect;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.ScrollView;
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
import java.util.Locale;

public class LessonRedactorActivity extends AppCompatActivity implements LessonNameDialogInterface {

    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    public static final String LESSON_START_TIME = "lessonStartTime";
    public static final String LESSON_END_TIME = "lessonEndTime";

    //id главной зависимости
    long attitudeId = -1;

    //класс-кабинет
    TextView seatingStateText;
    //выбранные класс и кабинет
    final ClassCabinet classCabinetId = new ClassCabinet(-1, -1);//todo предусмотреть вариант если уроков вобще нет или у принимаемого урока не стандартное время. проверка нет ли на этом времени урока.
    //урок
    long chosenLessonId = -1;
    Spinner lessonNameSpinner;
    //время
    LinearLayout timeOut;
    LessonTimePeriod lessonTime;

    long repeat = 0;
    String[] repeatPeriodsNames;
    boolean isTmeYours = false;
    static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_redactor);
        repeatPeriodsNames = new String[]{
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_never),
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_daily),
                getResources().getString(R.string.lesson_redactor_activity_spinner_text_repeat_weekly)
                //, "ежемесячно"//todo
        };

        attitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -2);
        if (attitudeId == -2) {
            Toast toast = Toast.makeText(this, R.string.lesson_redactor_activity_toast_subject_not_select, Toast.LENGTH_SHORT);
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
        TextView editSeatingButton = (TextView) findViewById(R.id.activity_lesson_redactor_seating_redactor_button);
        //урок
        lessonNameSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_lesson_name_spinner);
        //время
        timeOut = (LinearLayout) findViewById(R.id.activity_lesson_redactor_time_layout);
        CheckBox timeCheckBox = (CheckBox) findViewById(R.id.activity_lesson_redactor_time_check_box);
        Spinner lessonRepeatSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_lesson_repeat_spinner);
        //общие
        LinearLayout buttonsOut = (LinearLayout) findViewById(R.id.activity_lesson_redactor_buttons_out);
        TextView removeButton = (TextView) findViewById(R.id.activity_lesson_redactor_remove_button);
        TextView backButton = (TextView) findViewById(R.id.activity_lesson_redactor_back_button);
        TextView saveButton = (TextView) findViewById(R.id.activity_lesson_redactor_save_button);


        lessonTime = new LessonTimePeriod(new GregorianCalendar(), new GregorianCalendar());
        if (attitudeId == -1) {

            //заголовок
            setTitle(getResources().getString(R.string.title_activity_lesson_redactor_create));
            lessonTime.calendarStartTime.setTime(new Date(getIntent().getLongExtra(LESSON_START_TIME, 1)));
            lessonTime.calendarEndTime.setTime(new Date(getIntent().getLongExtra(LESSON_END_TIME, 1)));
        } else {

            setTitle(getString(R.string.title_activity_lesson_redactor_edit));

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

            final CustomAdapter adapter = new CustomAdapter(this, R.layout.spiner_dropdown_element_lesson_redactor, stringClasses);
            adapter.setDropDownViewResource(R.layout.spiner_dropdown_element_lesson_redactor);
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

            final CustomAdapter adapter = new CustomAdapter(this, R.layout.spiner_dropdown_element_lesson_redactor, stringCabinets);
            adapter.setDropDownViewResource(R.layout.spiner_dropdown_element_lesson_redactor);
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

        {//назначение повторений
            final CustomAdapter adapter = new CustomAdapter(this, R.layout.spiner_dropdown_element_lesson_redactor, repeatPeriodsNames);

            adapter.setDropDownViewResource(R.layout.spiner_dropdown_element_lesson_redactor);
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
                        Toast toast = Toast.makeText(getApplicationContext(), "не выбран предмет!", Toast.LENGTH_SHORT);
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
            SimpleDateFormat textTimeFormat = new SimpleDateFormat("H.m", Locale.getDefault());
            for (int i = 0; i < textTime.length; i++) {
                textTime[i] = "  " + (i + 1) + " " + getResources().getString(R.string.lesson_redactor_activity_spinner_title_lesson) + " " +
                        textTimeFormat.format(ScheduleDayActivity.lessonStandardTimePeriods[i].calendarStartTime.getTime()) + "-" +

                        textTimeFormat.format(ScheduleDayActivity.lessonStandardTimePeriods[i].calendarEndTime.getTime()) + "  ";
            }


            final CustomAdapter adapter = new CustomAdapter(this, R.layout.spiner_dropdown_element_lesson_redactor, textTime);
            adapter.setDropDownViewResource(R.layout.spiner_dropdown_element_lesson_redactor);
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


/*          <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_gravity="center"
                android:orientation="vertical">

                <Spinner
                    android:id="@+id/activity_lesson_redactor_lesson_repeat_spinner"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:layout_marginLeft="20dp"
                    android:layout_marginStart="20dp" />

                <View
                    android:layout_width="match_parent"
                    android:layout_height="2dp"
                    android:layout_marginEnd="18dp"
                    android:layout_marginLeft="18dp"
                    android:layout_marginRight="18dp"
                    android:layout_marginStart="18dp"
                    android:background="@drawable/line_gray" />
            </LinearLayout>
*/
            //контейнер для спиннера и подчеркивания
            LinearLayout timeContainer = new LinearLayout(this);
            timeContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams timeContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            timeContainerParams.gravity = Gravity.CENTER;

            //спиннер в контейнер
            timeContainer.addView(spinner, spinnerTimeParams);

            //подчеркивание под спиннером
            LinearLayout line = new LinearLayout(this);
            line.setBackground(getResources().getDrawable(R.drawable.line_gray));
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) pxFromDp(2)
            );
            lineParams.setMargins((int) pxFromDp(18), 0, (int) pxFromDp(18), 0);
            timeContainer.addView(line, lineParams);

            //добавляем все в timeOut
            timeOut.addView(timeContainer, timeContainerParams);

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
            seatingStateText.setText(R.string.lesson_redactor_activity_text_learners_ready);
            seatingStateText.setTextColor(Color.parseColor("#469500"));
        } else {
            seatingStateText.setText(R.string.lesson_redactor_activity_text_learners_not_ready);
            seatingStateText.setTextColor(getResources().getColor(R.color.colorAccentRed));
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
            stringLessons[stringLessons.length - 1] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject);
        } else {
            stringLessons[stringLessons.length - 1] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_remove_subject);
            stringLessons[stringLessons.length - 2] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_create_subject);
        }

        stringLessons[0] = getResources().getString(R.string.lesson_redactor_activity_spinner_text_select_subject);
        cursor.close();
        db.close();

        final String[] finalStringLessons = stringLessons;
        final CustomAdapter adapter = new CustomAdapter(this, R.layout.spiner_dropdown_element_lesson_redactor, stringLessons);
        adapter.setDropDownViewResource(R.layout.spiner_dropdown_element_lesson_redactor);
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
                    //обратная связь от диалога
                    handler = new Handler() {
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == 0) {
                                availableLessonsOut(classViewId, 0);
                            }
                            if (msg.what == 1) {
                                availableLessonsOut(classViewId, position);
                            }
                        }
                    };
                    //данные передаваемые в диалог
                    Bundle args = new Bundle();
                    args.putStringArray("stringOnlyLessons", stringOnlyLessons);
                    args.putLongArray("lessonsId", lessonsId);
                    args.putLongArray("lessonsId", lessonsId);
                    args.putInt("position", position);
                    //диалог по удалению
                    RemoveDialogFragment removeDialog = new RemoveDialogFragment();
                    removeDialog.setArguments(args);
                    removeDialog.show(getFragmentManager(), "removeLessons");

                } else if ((count != 0 && stringLessons.length - 2 == pos) || (count == 0 && finalStringLessons.length - 1 == pos)) {
                    //диалог создания предмета
                    Log.i("TeachersApp", "LessonRedactorActivity - new lesson");
                    //данные для диалога
                    Bundle args = new Bundle();
                    args.putStringArray("stringLessons", stringLessons);
                    args.putInt("position", position);
                    //диалог по созданию нового предмета
                    LessonNameDialogFragment lessonNameDialogFragment = new LessonNameDialogFragment();
                    lessonNameDialogFragment.setArguments(args);
                    lessonNameDialogFragment.show(getFragmentManager(), "createSubject");
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

//-------------диалог удаления предметов-------------

    public static class RemoveDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //начинаем строить диалог
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            //layout диалога
            LinearLayout out = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_lesson_redactor_dialog_lesson_name, null);
            builder.setView(out);

            //--LinearLayout в layout файле--
            LinearLayout linearLayout = (LinearLayout) out.findViewById(R.id.create_lesson_dialog_fragment_linear_layout);
            linearLayout.setBackgroundResource(R.color.colorBackGround);
            linearLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
            linearLayout.setLayoutParams(linearLayoutParams);

//--заголовок--
            TextView title = new TextView(getActivity());
            title.setText(R.string.lesson_redactor_activity_dialog_title_remove_subjects);
            title.setTextColor(Color.BLACK);
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            title.setAllCaps(true);
            title.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

            linearLayout.addView(title, titleParams);

//--список выбранных предметов--
            final ArrayList<Integer> mSelectedItems = new ArrayList<>();
            //--------ставим диалогу список в виде view--------
            //список названий
            String[] subjectNames = getArguments().getStringArray("stringOnlyLessons");

            //контейнеры для прокрутки
            ScrollView scrollView = new ScrollView(getActivity());
            linearLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F));
            LinearLayout linear = new LinearLayout(getActivity());
            linear.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(linear);


            for (int i = 0; i < subjectNames.length; i++) {
//--------пункт списка--------
                //контейнер
                LinearLayout item = new LinearLayout(getActivity());
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setGravity(Gravity.LEFT);
                LinearLayout.LayoutParams itemParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (int) (pxFromDp(40) * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))
                        );
                itemParams.setMargins(
                        (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier)))
                );
                linear.addView(item, itemParams);

                //чекбокс
                final CheckBox checkBox = new CheckBox(getActivity());
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                checkBox.setChecked(false);
                item.addView(checkBox);

                //текст в нем
                TextView text = new TextView(getActivity());
                text.setText(subjectNames[i]);
                text.setTextColor(Color.BLACK);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                item.addView(text);

                //нажатие на пункт списка
                final int number = i;
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkBox.isChecked()) {
                            checkBox.setChecked(false);
                            if (mSelectedItems.contains(number)) {
                                // Else, if the item is already in the array, remove it
                                mSelectedItems.remove(Integer.valueOf(number));
                            }
                        } else {
                            // If the user checked the item, add it to the selected items
                            checkBox.setChecked(true);
                            mSelectedItems.add(number);
                        }
                    }
                });
            }

//--кнопки согласия/отмены--
            //контейнер для них
            LinearLayout container = new LinearLayout(getActivity());
            container.setOrientation(LinearLayout.HORIZONTAL);

            //кнопка отмены
            Button neutralButton = new Button(getActivity());
            neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
            neutralButton.setText(R.string.lesson_redactor_activity_dialog_button_cancel);
            neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            neutralButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            neutralButtonParams.weight = 1;
            neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

            //кнопка согласия
            Button positiveButton = new Button(getActivity());
            positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
            positiveButton.setText(R.string.lesson_redactor_activity_dialog_button_remove);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            positiveButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            positiveButtonParams.weight = 1;
            positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));


            //кнопки в контейнер
            container.addView(neutralButton, neutralButtonParams);
            container.addView(positiveButton, positiveButtonParams);

            //контейнер в диалог
            linearLayout.addView(container);


            //при нажатии...
            //согласие
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long[] lessonsId = getArguments().getLongArray("lessonsId");
                    ArrayList<Long> deleteList = new ArrayList<Long>(mSelectedItems.size());
                    for (int itemPoz : mSelectedItems) {
                        deleteList.add(lessonsId[itemPoz]);
                    }
                    (new DataBaseOpenHelper(getActivity().getApplicationContext())).deleteSubjects(deleteList);
                    handler.sendEmptyMessage(0);

                    dismiss();
                }
            });

            //отмена
            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    handler.sendEmptyMessage(1);
                }
            });
            return builder.create();
        }

        //---------форматы----------

        private float pxFromDp(float px) {
            return px * getActivity().getResources().getDisplayMetrics().density;
        }

    }

//------------диалог создания пердмета------------

    public static class LessonNameDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //начинаем строить диалог
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            //layout диалога
            LinearLayout out = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_lesson_redactor_dialog_lesson_name, null);
            builder.setView(out);

            //--LinearLayout в layout файле--
            LinearLayout linearLayout = (LinearLayout) out.findViewById(R.id.create_lesson_dialog_fragment_linear_layout);
            linearLayout.setBackgroundResource(R.color.colorBackGround);
            linearLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
            linearLayout.setLayoutParams(linearLayoutParams);

//--заголовок--
            TextView title = new TextView(getActivity());
            title.setText(R.string.lesson_redactor_activity_dialog_title_add_subject);
            title.setTextColor(Color.BLACK);
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            title.setAllCaps(true);
            title.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

            linearLayout.addView(title, titleParams);


//--текстовое поле имени--
            final EditText editName = new EditText(getActivity());
            editName.setTextColor(Color.BLACK);
            editName.setHint(R.string.lesson_redactor_activity_dialog_hint_subject_name);
            editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            editName.setInputType(InputType.TYPE_CLASS_TEXT);
            editName.setHintTextColor(Color.GRAY);

            LinearLayout editNameContainer = new LinearLayout(getActivity());
            LinearLayout.LayoutParams editNameContainerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            editNameContainerParams.setMargins((int) pxFromDp(25), 0, (int) pxFromDp(25), 0);
            //добавляем текстовое поле
            editNameContainer.addView(
                    editName,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    ));
            linearLayout.addView(editNameContainer, editNameContainerParams);

//--кнопки согласия/отмены--
            //контейнер для них
            LinearLayout container = new LinearLayout(getActivity());
            container.setOrientation(LinearLayout.HORIZONTAL);

            //кнопка отмены
            Button neutralButton = new Button(getActivity());
            neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
            neutralButton.setText(R.string.lesson_redactor_activity_dialog_button_cancel);
            neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            neutralButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            neutralButtonParams.weight = 1;
            neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

            //кнопка согласия
            Button positiveButton = new Button(getActivity());
            positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
            positiveButton.setText(R.string.lesson_redactor_activity_dialog_button_add);
            positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            positiveButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            positiveButtonParams.weight = 1;
            positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));


            //кнопки в контейнер
            container.addView(neutralButton, neutralButtonParams);
            container.addView(positiveButton, positiveButtonParams);

            //контейнер в диалог
            linearLayout.addView(container);


            //при нажатии...
            //согласие
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((LessonNameDialogInterface) getActivity())
                            .lessonNameDialogMethod(
                                    1,
                                    getArguments().getStringArray("stringLessons").length - 2,
                                    editName.getText().toString()
                            );

                    dismiss();
                }
            });

            //отмена
            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((LessonNameDialogInterface) getActivity())
                            .lessonNameDialogMethod(
                                    0,
                                    getArguments().getInt("position"),
                                    ""
                            );
                    dismiss();
                }
            });
            return builder.create();

//           builder.setPositiveButton(getResources().getString(R.string.lesson_redactor_activity_dialog_button_add), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            ((LessonNameDialogInterface) getActivity())
//                                    .lessonNameDialogMethod(
//                                            1,
//                                            getArguments().getStringArray("stringLessons").length - 2,
//                                            lessonNameEditText.getText().toString()
//                                    );
//
//                            dismiss();
//                        }
//                    })
//                    .setNegativeButton(getResources().getString(R.string.lesson_redactor_activity_dialog_button_cancel), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            ((LessonNameDialogInterface) getActivity())
//                                    .lessonNameDialogMethod(
//                                            0,
//                                            getArguments().getInt("position"),
//                                            ""
//                                    );
//                            dismiss();
//                        }
//                    });

            //return builder.create();
        }

        //---------форматы----------

        private float pxFromDp(float px) {
            return px * getActivity().getResources().getDisplayMetrics().density;
        }
    }

    @Override//обратная связь от диалога
    public void lessonNameDialogMethod(int code, int position, String classNameText) {
        if (code == 1) {
            (new DataBaseOpenHelper(this)).createSubject(classNameText, classCabinetId.classId);
            availableLessonsOut(classCabinetId.classId, position);
        } else {
            availableLessonsOut(classCabinetId.classId, position);
        }

    }

    //------технические методы------
    private float pxFromDp(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
    }
}

interface LessonNameDialogInterface {//обратная связь от диалога

    void lessonNameDialogMethod(int code, int position, String classNameText);
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
        tv.setBackgroundColor(getContext().getResources().getColor(R.color.colorBackGround));//светло салатовый
        //tv.setBackgroundColor(Color.WHITE);//светло салатовый
        tv.setText(objects[position]);
        //}
        return convertView;
    }
}





