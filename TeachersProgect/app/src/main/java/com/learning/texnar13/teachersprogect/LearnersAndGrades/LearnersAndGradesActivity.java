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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class LearnersAndGradesActivity extends AppCompatActivity {

    public static final String CLASS_ID = "classId";

    int countOfDays = 30;

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

        //переданные данные
        Intent intent = getIntent();
        long classId = intent.getLongExtra(CLASS_ID, -1);
        if (classId == -1) {
            finish();
        }//выходим если не передан класс

        //база данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersCursor = db.getLearnersByClassId(classId);

        ArrayList<Long> learnersId = new ArrayList<>();//id учеников
        ArrayList<String> learnersNames = new ArrayList<>();//имена учеников
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


        //спинер с предметами
        //Spinner


        //таблицы
        //----------имена----------
        TableLayout learnersNamesTable = (TableLayout) findViewById(R.id.learners_and_grades_table_names);
        //----------оценки----------
        TableLayout learnersGradesTable = (TableLayout) findViewById(R.id.learners_and_grades_table);

        //---заголовок---
        //заголовок ученика
        TableRow headNameRaw = new TableRow(this);
        //рамка
        LinearLayout headNameOut = new LinearLayout(this);
        headNameOut.setBackgroundColor(Color.parseColor("#1f5b85"));
        //текст заголовка ученика
        TextView headName = new TextView(this);
        headName.setText("Ф.И.");
        headName.setBackgroundColor(Color.parseColor("#bed7e9"));
        headName.setGravity(Gravity.CENTER);
        headName.setTextColor(Color.parseColor("#1f5b85"));
        //отступы рамки
        LinearLayout.LayoutParams headNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        headNameParams.setMargins(1,3,1,3);
        //текст в рамку
        headNameOut.addView(headName,headNameParams);
        //рамку в строку
        headNameRaw.addView(headNameOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        //строку в таблицу
        learnersNamesTable.addView(headNameRaw, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);


        //дни
        TableRow headGrades = new TableRow(this);
        for (int i = 0; i < countOfDays; i++) {
            //рамка
            LinearLayout headDateOut = new LinearLayout(this);
            headDateOut.setBackgroundColor(Color.LTGRAY);
            //текст заголовка ученика
            TextView headDate = new TextView(this);
            headDate.setTextColor(Color.BLACK);
            headDate.setBackgroundColor(Color.WHITE);
            headDate.setGravity(Gravity.CENTER);
            if (i > 9) {
                headDate.setText(" " + (i + 1) + " ");
            } else {
                headDate.setText(" " + (i + 1) + "  ");
            }
            //отступы рамки
            LinearLayout.LayoutParams headDateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            headDateParams.setMargins(1,3,1,3);
            //текст в рамку
            headDateOut.addView(headDate,headDateParams);
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
            learnerNameOut.setBackgroundColor(Color.parseColor("#1f5b85"));
            //текст ученика
            TextView learnerName = new TextView(this);
            learnerName.setTextColor(Color.parseColor("#1f5b85"));
            learnerName.setBackgroundColor(Color.parseColor("#bed7e9"));
            learnerName.setGravity(Gravity.CENTER);
            learnerName.setText(learnersNames.get(i));
            //отступы рамки
            LinearLayout.LayoutParams learnerNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            learnerNameParams.setMargins(1,3,1,3);
            //текст в рамку
            learnerNameOut.addView(learnerName,learnerNameParams);
            //рамку в строку
            learner.addView(learnerNameOut, TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);

            //вывод дат
            for (int j = 0; j < countOfDays; j++) {//и по датам
                //рамка
                LinearLayout dateOut = new LinearLayout(this);
                dateOut.setBackgroundColor(Color.LTGRAY);
                //текст
                TextView learnerGrade = new TextView(this);
                learnerGrade.setTextColor(Color.BLACK);
                learnerGrade.setBackgroundColor(Color.WHITE);
                learnerGrade.setGravity(Gravity.CENTER);
                learnerGrade.setText(" " + "-" + "  ");
                //отступы рамки
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                textParams.setMargins(1,3,1,3);
                //текст в рамку
                dateOut.addView(learnerGrade,textParams);
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