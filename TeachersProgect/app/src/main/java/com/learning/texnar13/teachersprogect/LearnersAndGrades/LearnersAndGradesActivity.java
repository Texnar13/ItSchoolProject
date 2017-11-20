package com.learning.texnar13.teachersprogect.LearnersAndGrades;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LearnersAndGradesActivity extends AppCompatActivity {

    public static final String CLASS_ID = "classId";

    //переменные выводящиеся из бд
    ArrayList<Long> learnersId;
    ArrayList<String> learnersNames;
    int changingSubjectPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learners_and_grades);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //----переданные данные----
        Intent intent = getIntent();
        long classId = intent.getLongExtra(CLASS_ID, -1);
        if (classId == -1) {
            finish();//выходим если не передан класс
        }
        //----база данных----
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersCursor = db.getLearnersByClassId(classId);

        learnersId = new ArrayList<>();//id учеников
        learnersNames = new ArrayList<>();//имена учеников
        while (learnersCursor.moveToNext()) {
            learnersId.add(learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
            learnersNames.add(
                    learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " +
                            learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME))
            );
        }
        learnersCursor.close();

        //todo в общем так, загружаем список предметов и создаём общую переменную (в классе) в которую заносим id выбранного предмета, по нажатию на пункт спинера обновить оценки, сделать на это отдельный метод


        //заголовок
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar
        //название класса
        Cursor classCursor = db.getClasses(classId);
        classCursor.moveToFirst();
        getSupportActionBar().setTitle("в разработкеУченики в классе " +
                classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
        classCursor.close();

        //----переключение даты----
        //константы
        final String[] monsesNames = {"ЯНВАРЬ", "ФЕВРАЛЬ", "МАРТ", "АПРЕЛЬ", "МАЙ", "ИЮНЬ", "ИЮЛЬ", "АВГУСТ", "СЕНТЯБРЬ", "ОКТЯБРЬ", "НОЯБРЬ", "ДЕКАБРЬ"};
        //изменяющийся календарь
        final GregorianCalendar changingCalendar = new GregorianCalendar();
        changingCalendar.setTime(new Date());
        //обьявление кнопок и текста
        ImageView imageButtonPrevious = (ImageView) findViewById(R.id.learners_and_grades_activity_button_previous);
        final TextView dateText = (TextView) findViewById(R.id.learners_and_grades_activity_date_text);
        ImageView imageButtonNext = (ImageView) findViewById(R.id.learners_and_grades_activity_button_next);
        //кнопка назад
        imageButtonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (changingCalendar.get(Calendar.MONTH) == 0) {
                    changingCalendar.set(Calendar.MONTH, 11);
                    changingCalendar.set(Calendar.YEAR, changingCalendar.get(Calendar.YEAR) - 1);
                } else {
                    changingCalendar.set(Calendar.MONTH, changingCalendar.get(Calendar.MONTH) - 1);
                }
                dateText.setText(monsesNames[changingCalendar.get(Calendar.MONTH)] + " " + changingCalendar.get(Calendar.YEAR));
                updateTable(changingSubjectPosition, changingCalendar);
            }
        });
        //кнопка впеёд
        imageButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (changingCalendar.get(Calendar.MONTH) == 11) {
                    changingCalendar.set(Calendar.MONTH, 0);
                    changingCalendar.set(Calendar.YEAR, changingCalendar.get(Calendar.YEAR) + 1);
                } else {
                    changingCalendar.set(Calendar.MONTH, changingCalendar.get(Calendar.MONTH) + 1);
                }
                dateText.setText(monsesNames[changingCalendar.get(Calendar.MONTH)] + " " + changingCalendar.get(Calendar.YEAR));
                updateTable(changingSubjectPosition, changingCalendar);
            }
        });

        //----спинер с предметами----
        Spinner subjectSpinner = (Spinner) findViewById(R.id.learners_and_grades_activity_subject_spinner);
        //выводим из базы данных список предметов
        Cursor subjectsCursor = db.getLessonsByClassId(classId);
        final long[] subjectsId = new long[subjectsCursor.getCount()];
        final String[] subjectsNames = new String[subjectsCursor.getCount()];
        for (int i = 0; i < subjectsCursor.getCount(); i++) {
            subjectsCursor.moveToNext();
            subjectsId[i] = (subjectsCursor.getLong(subjectsCursor.getColumnIndex(SchoolContract.TableLessons.KEY_LESSON_ID)));
            subjectsNames[i] = (subjectsCursor.getString(subjectsCursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)));
        }
        subjectsCursor.close();
        //адаптер для спиннера
        CustomAdapter subjectAdapter = new CustomAdapter(this, R.layout.learners_and_grades_activity_subjects_spinner_element, subjectsNames);
        subjectSpinner.setAdapter(subjectAdapter);
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changingSubjectPosition = i;
                updateTable(subjectsId[i], changingCalendar);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        dateText.setText(monsesNames[changingCalendar.get(Calendar.MONTH)] + " " + changingCalendar.get(Calendar.YEAR));
        updateTable(subjectsId[0], changingCalendar);//выводим оценки при старте
    }

    void updateTable(long subjectId, GregorianCalendar viewCalendar) {
        //получаем оценки из бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        //таблицы
        //----------имена----------
        TableLayout learnersNamesTable = (TableLayout) findViewById(R.id.learners_and_grades_table_names);
        learnersNamesTable.removeAllViews();
        //----------оценки----------
        TableLayout learnersGradesTable = (TableLayout) findViewById(R.id.learners_and_grades_table);
        learnersGradesTable.removeAllViews();

        //---заголовок---
        //заголовок ученика
        TableRow headNameRaw = new TableRow(this);
        //рамка
        LinearLayout headNameOut = new LinearLayout(this);
        headNameOut.setBackgroundColor(Color.BLACK);//parseColor("#1f5b85")
        //текст заголовка ученика
        TextView headName = new TextView(this);
        headName.setText("Ф.И.");
        headName.setTextSize(20);
        headName.setBackgroundColor(Color.WHITE);//светло синий"#bed7e9"Color.parseColor()
        headName.setGravity(Gravity.CENTER);
        headName.setTextColor(Color.BLACK);//тёмно синий parseColor("#1f5b85")
        //отступы рамки
        LinearLayout.LayoutParams headNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        headNameParams.setMargins(0, 0, 0, (int) pxFromDp(2));
        //текст в рамку
        headNameOut.addView(headName, headNameParams);
        //рамку в строку
        headNameRaw.addView(headNameOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        //строку в таблицу
        learnersNamesTable.addView(headNameRaw, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);


        //дни
        TableRow headGrades = new TableRow(this);
        for (int i = 0; i < viewCalendar.getMaximum(Calendar.DAY_OF_MONTH); i++) {
            //рамка
            LinearLayout headDateOut = new LinearLayout(this);
            headDateOut.setBackgroundColor(Color.BLACK);
            //текст заголовка ученика
            TextView headDate = new TextView(this);
            headDate.setTextColor(Color.BLACK);
            headDate.setTextSize(20);
            headDate.setBackgroundColor(Color.WHITE);
            headDate.setGravity(Gravity.CENTER);
            if (i > 9) {
                headDate.setText(" " + (i + 1) + " ");
            } else {
                headDate.setText(" " + (i + 1) + " ");
            }
            //отступы рамки
            LinearLayout.LayoutParams headDateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            headDateParams.setMargins(0, 0, 0, (int) pxFromDp(2));
            //текст в рамку
            headDateOut.addView(headDate, headDateParams);
            //рамку в строку
            headGrades.addView(headDateOut, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        }
        //строку в таблицу
        learnersGradesTable.addView(headGrades, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        //---тело---
        for (int i = 0; i < learnersNames.size(); i++) {//пробегаемся по ученикам
            //строка с учеником и оценками
            TableRow learner = new TableRow(this);
            TableRow learnerGrades = new TableRow(this);

            //рамка
            LinearLayout learnerNameOut = new LinearLayout(this);
            learnerNameOut.setBackgroundColor(Color.BLACK);//parseColor("#1f5b85")
            //контейнер для текста
            LinearLayout dateContainer = new LinearLayout(this);
            dateContainer.setBackgroundColor(Color.WHITE);
            //текст ученика
            TextView learnerName = new TextView(this);
            learnerName.setTextSize(20);
            learnerName.setTextColor(Color.BLACK);//parseColor("#1f5b85")
            learnerName.setBackgroundColor(Color.WHITE);//"#bed7e9"
            learnerName.setGravity(Gravity.BOTTOM);
            learnerName.setText(learnersNames.get(i));
            //отступы контейнера в рамке
            LinearLayout.LayoutParams learnerNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            learnerNameParams.setMargins((int) pxFromDp(10), 0, (int) pxFromDp(10), 0);
            //текст в контейнер
            dateContainer.addView(learnerName, learnerNameParams);
            //отступы рамки
            LinearLayout.LayoutParams dateContainerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            dateContainerParams.setMargins(0, 0, 0, (int) pxFromDp(2));
            //контейнер в рамку
            learnerNameOut.addView(dateContainer, dateContainerParams);
            //рамку в строку
            learner.addView(learnerNameOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);

            //вывод дат
            for (int j = 0; j < viewCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); j++) {//и по датам
                //достаём текст из бд
                ArrayList<Integer> gradesArray = new ArrayList<>();

                Cursor grades = db.getGradesByLearnerIdAndDayTime(learnersId.get(i), viewCalendar);
                while (grades.moveToNext()) {
                    gradesArray.add(grades.getInt(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_GRADE)));

                }
                grades.close();

                //рамка
                LinearLayout dateOut = new LinearLayout(this);
                dateOut.setBackgroundColor(Color.BLACK);
                //текст
                TextView learnerGrade = new TextView(this);
                learnerGrade.setTextColor(Color.BLACK);
                learnerGrade.setTextSize(20);
                learnerGrade.setBackgroundColor(Color.WHITE);
                learnerGrade.setGravity(Gravity.CENTER);
                if (gradesArray.size() == 0) {
                    learnerGrade.setText(" - ");
                } else {
                    learnerGrade.setText(" " + gradesArray.get(0) + " ");
                }
                //отступы рамки
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                textParams.setMargins(0, 0, 0, (int) pxFromDp(2));

                //текст в рамку
                dateOut.addView(learnerGrade, textParams);
                //добавляем всё в строку
                learnerGrades.addView(dateOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);

            }
            //добавляем строку в таблицу
            learnersNamesTable.addView(learner, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            learnersGradesTable.addView(learnerGrades, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        }
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

    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
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
        //tv.setBackgroundColor(Color.parseColor("#bed7e9"));//светло синий
        tv.setBackgroundColor(Color.WHITE);
        tv.setText(objects[position]);
        //}
        return convertView;
    }
}