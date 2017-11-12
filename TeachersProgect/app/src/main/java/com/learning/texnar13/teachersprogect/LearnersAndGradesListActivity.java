package com.learning.texnar13.teachersprogect;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class LearnersAndGradesListActivity extends AppCompatActivity {

    TableLayout learnersAndGradesTable;
    int monthCapacity = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learners_and_grades_list);
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

        //таблица
        learnersAndGradesTable = (TableLayout) findViewById(R.id.content_learners_and_grades_list_table);
        //заголовок таблицы
        TableRow titleRaw = new TableRow(this);
        LinearLayout.LayoutParams TitleTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView titleName = new TextView(this);
        titleName.setText("Ф.И.О.");
        titleRaw.addView(titleName, TitleTextParams);

        for (int i = 0; i < monthCapacity; i++) {
            TextView day = new TextView(this);
            day.setText(""+i);
            titleRaw.addView(day, TitleTextParams);
        }

        learnersAndGradesTable.addView(titleRaw,TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT);

    }

}
