package com.learning.texnar13.teachersprogect;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        final Spinner classSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_class_spinner);
        Spinner cabinetSpinner = (Spinner) findViewById(R.id.activity_lesson_redactor_cabinet_spinner);
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
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            classSpinner.setAdapter(adapter);
            classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    availableLessonsOut(classesId[pos], lessonNameSpinner);
                    //classSpinner.setSelection(pos);

                    // Set adapter flag that something has been chosen
                    adapter.flag = true;
                }
            });
            classesCursor.close();
        }

        //вывод кабинетов в спиннер
        Cursor cabinetsCursor = db.getCabinets();


        cabinetsCursor.close();


        db.close();
    }

    void availableLessonsOut(long classViewId, Spinner spinner) {

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor cursor = db.getLessonsByClassId(classViewId);
        String[] stringLessons = new String[cursor.getCount()];
        for (int i = 0; i < stringLessons.length; i++) {
            cursor.moveToNext();
            stringLessons[i] = cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME));
        }
        cursor.close();
        db.close();

        final CustomAdapter adapter = new CustomAdapter(this, android.R.layout.simple_spinner_item, stringLessons);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {


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
    boolean flag = false;

    CustomAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context, textViewResourceId, null);
        //if (flag) {
        //if(){

        //}
            TextView tv = (TextView) convertView;
            tv.setText(objects[position]);
        //}
        return convertView;
    }
}
