package com.learning.texnar13.teachersprogect.LearnersAndGrades;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
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
        if(classId == -1){finish();}//выходим если не передан класс

        //база данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersCursor = db.getLearnersByClassId(classId);

        ArrayList<Long> learnersId = new ArrayList<>();//id учеников
        ArrayList<String> learnersNames = new ArrayList<>();//имена учеников
        while(learnersCursor.moveToNext()){
            learnersId.add(learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
            learnersNames.add(
                    learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) +" "+
                            learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME))
            );
        }
        learnersCursor.close();

        //todo в общем так, загружаем список предметов и создаём общую переменную (в классе) в которую заносим id выбранного предмета, по нажатию на пункт спинера обновить оценки, сделать на это отдельный метод

        //таблица
        //----------имена----------
        TableLayout learnersNamesTable = (TableLayout) findViewById(R.id.learners_and_grades_table_names);
        //----------оценки----------
        TableLayout learnersGradesTable = (TableLayout) findViewById(R.id.learners_and_grades_table);

        //---заголовок---
        //имена
        TableRow headNameRaw = new TableRow(this);

        TextView headName = new TextView(this);
        headName.setText("Ф.И.");
        headName.setGravity(Gravity.CENTER);
        headName.setTextColor(Color.BLACK);
        headNameRaw.addView(headName, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        learnersNamesTable.addView(headNameRaw, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        //дни
        TableRow headGrades = new TableRow(this);
        for (int i = 0; i < countOfDays; i++) {
            TextView headDate = new TextView(this);
            headDate.setTextColor(Color.BLACK);
            headDate.setGravity(Gravity.CENTER);
            if (i > 9) {
                headDate.setText(" " + (i + 1) + " ");
            } else {
                headDate.setText(" " + (i + 1) + "  ");
            }
            headGrades.addView(headDate, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        }
        learnersGradesTable.addView(headGrades, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        //---тело---
        for (int i = 0; i < learnersNames.size(); i++) {//пробегаемся по ученикам
            //строка ученика
            TableRow learner = new TableRow(this);
            TableRow learnerGrades = new TableRow(this);

            //текст ученика
            TextView learnerName = new TextView(this);
            learnerName.setTextColor(Color.BLACK);
            learnerName.setGravity(Gravity.CENTER);
            learnerName.setText(learnersNames.get(i));

            //текст в строку
            learner.addView(learnerName, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

            for (int j = 0; j < countOfDays; j++) {//и по датам
                TextView learnerGrade = new TextView(this);
                learnerGrade.setTextColor(Color.BLACK);
                learnerGrade.setGravity(Gravity.CENTER);
                learnerGrade.setText(" " + "-" + "  ");
                learnerGrades.addView(learnerGrade, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            }
            //добавляем строку в таблицу
            learnersNamesTable.addView(learner, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            learnersGradesTable.addView(learnerGrades, TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
        }
    }

}
