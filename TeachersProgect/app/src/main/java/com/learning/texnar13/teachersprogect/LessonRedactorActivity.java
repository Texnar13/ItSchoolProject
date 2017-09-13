package com.learning.texnar13.teachersprogect;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class LessonRedactorActivity extends AppCompatActivity {

    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_redactor);
        long attitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -2);
        if (attitudeId == -2) {
            Toast toast = Toast.makeText(this, "не выбран урок!", Toast.LENGTH_SHORT);
            toast.show();
            finish();
            return;
        }

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);


        //класс-кабинет
        final ClassCabinet classCabinetId;
        if (attitudeId == -1) {
            classCabinetId = new ClassCabinet(1,1);//todo предусмотреть вариант если уроков вобще нет и обновление в спинерах
        } else {
            Cursor attitudeCursor = db.getLessonAttitudeById(attitudeId);
            attitudeCursor.moveToFirst();
            long lessonId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID));
            Cursor lessonCursor = db.getLessonById(lessonId);
            lessonCursor.moveToFirst();
            classCabinetId = new ClassCabinet(lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableLessons.KEY_CLASS_ID)), attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID)));
            lessonCursor.close();
            attitudeCursor.close();
        }
        final Spinner classSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_class_spinner);
        Spinner cabinetSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_cabinet_spinner);
        final TextView seatingStateText = (TextView) findViewById(R.id.activity_lesson_redactor_seating_state);
        Button editSeatingButton = (Button) findViewById(R.id.activity_lesson_redactor_seating_redactor_button);
        //урок
        final Spinner lessonNameSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_lesson_name_spinner);
        //время
        LinearLayout timeOut = (LinearLayout) findViewById(R.id.activity_lesson_redactor_time_layout);
        CheckBox timeCheckBox = (CheckBox) findViewById(R.id.activity_lesson_redactor_time_check_box);
        //общие
        Button saveButton = (Button) findViewById(R.id.activity_lesson_redactor_save_button);

        if (attitudeId == -1) {
            setTitle("создание урока");
        } else {
            setTitle("редактирование урока");

        }


        seatingTextUpdate(seatingStateText, classCabinetId);//обновляем текст с информацией о рассадке
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

            final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, stringClasses, true);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            classSpinner.setAdapter(adapter);
            classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    availableLessonsOut(classesId[pos], lessonNameSpinner);
                    Log.i("TeachersApp", "LessonRedactorActivity - class spinner onItemSelected " + pos + "id =" + classesId[pos]);
                    //classSpinner.setSelection(pos);
                    classCabinetId.classId = classesId[pos];
                    seatingTextUpdate(seatingStateText, classCabinetId);
                    // Set adapter flag that something has been chosen
                    adapter.flag = true;
                }
            });
            classesCursor.close();
            //classSpinner.setSelection();
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

            final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, stringCabinets, true);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cabinetSpinner.setAdapter(adapter);
            cabinetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    Log.i("TeachersApp", "LessonRedactorActivity - cabinet spinner onItemSelected " + pos + "id =" + cabinetsId[pos]);
                    classCabinetId.cabinetId = cabinetsId[pos];
                    seatingTextUpdate(seatingStateText, classCabinetId);

                    // Set adapter flag that something has been chosen
                    adapter.flag = true;
                }
            });
            cabinetsCursor.close();

        }
        db.close();
    }

    class ClassCabinet {
        long classId;
        long cabinetId;

        public ClassCabinet(long classId, long cabinetId) {
            this.classId = classId;
            this.cabinetId = cabinetId;
        }
    }

    void seatingTextUpdate(TextView stateText, ClassCabinet classCabinet) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> arrayList = db.getNotPutLearnersIdByCabinetIdAndClassId(classCabinet.cabinetId, classCabinet.classId);
        if (arrayList.size() == 0) {
            stateText.setText("ок");
            stateText.setTextColor(Color.GREEN);
        } else {
            stateText.setText("ученики не рассажены!");
            stateText.setTextColor(Color.RED);
        }
    }

    void availableLessonsOut(long classViewId, Spinner spinner) {

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor cursor = db.getLessonsByClassId(classViewId);
        final String[] stringLessons = new String[cursor.getCount() + 2];
        //Log.i("TeachersApp", "LessonRedactorActivity - " + stringLessons.length);
        for (int i = 1; i < stringLessons.length - 1; i++) {
            cursor.moveToNext();
            stringLessons[i] = cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME));
        }
        stringLessons[stringLessons.length - 1] = "+ создать урок?";
        stringLessons[0] = "выберите...";
        cursor.close();
        db.close();

        final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, stringLessons, false);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (stringLessons.length - 1 == pos) {
                    //диалог создания урока
                    Log.i("TeachersApp", "LessonRedactorActivity - new lesson");
                }
                Log.i("TeachersApp", "LessonRedactorActivity - availableLessonsOut onItemSelected " + pos);
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
    private boolean isFirstElementVisible;
    boolean flag = false;

    CustomAdapter(Context context, int textViewResourceId, String[] objects, boolean isFirstElementVisible) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.objects = objects;
        this.isFirstElementVisible = isFirstElementVisible;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context, textViewResourceId, null);
        //if (flag || isFirstElementVisible) {
        TextView tv = (TextView) convertView;
        tv.setText(objects[position]);
        tv.setGravity(Gravity.CENTER);
        //}
        return convertView;
    }
}
