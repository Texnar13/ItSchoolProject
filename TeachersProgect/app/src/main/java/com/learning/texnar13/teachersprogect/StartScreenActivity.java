package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class StartScreenActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonNow;//текущий урок
    Button buttonSchedule;//расписание
    Button buttonCabinets;//кабинеты
    Button buttonClasses;//классы


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        buttonNow = (Button) findViewById(R.id.start_menu_button_now);
        buttonSchedule = (Button) findViewById(R.id.start_menu_button_schedule);
        buttonCabinets = (Button) findViewById(R.id.start_menu_button_my_cabinets);
        buttonClasses = (Button) findViewById(R.id.start_menu_button_my_classes);

        buttonNow.setOnClickListener(this);
        buttonSchedule.setOnClickListener(this);
        buttonCabinets.setOnClickListener(this);
        buttonClasses.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.start_menu_button_now: {
                Intent intent = new Intent(this, LessonActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.start_menu_button_schedule:

                break;
            case R.id.start_menu_button_my_cabinets:

                break;
            case R.id.start_menu_button_my_classes: {
                Intent intent = new Intent(this, ListOfClassesActivity.class);
                startActivity(intent);
            }
            break;
        }

    }
}
