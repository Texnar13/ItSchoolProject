package com.learning.texnar13.teachersprogect.LearnersAndGradesOut;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.learning.texnar13.teachersprogect.R;

public class LearnersAndGradesHelp extends AppCompatActivity {

    //TODO удали меня


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learners_and_grades_help);
        //заголовок
        setTitle("подсказка");
        //кнопка назад
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
