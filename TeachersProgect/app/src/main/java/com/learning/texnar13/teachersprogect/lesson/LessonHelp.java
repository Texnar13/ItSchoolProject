package com.learning.texnar13.teachersprogect.lesson;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.learning.texnar13.teachersprogect.R;

public class LessonHelp extends AppCompatActivity {
    //создание активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_help);
        //заголовок
        setTitle("подсказка");
        //кнопка назад
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
//функциональные кнопки

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //кнопка назад в actionBar
            case android.R.id.home:
                onBackPressed();
            default:

        }
        return super.onOptionsItemSelected(item);
    }
}
