package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.listOf.ListOfActivity;


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
        Intent intent;
        switch (view.getId()) {
            case R.id.start_menu_button_now: {//запуск текущего урока
                intent = new Intent(this, LessonActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.start_menu_button_schedule://переход в список расписаний
                //TODO 1 надо адаптировать список под вывод расписаний(пример: расписание на понедельник, вторник, итд) в каждый из которых входят уроки
                /* таблицы в бд уже реализованы
                переименование также по диалогу
                у уроков ещё и редактирование всех параметров
                * существуют рассадки, то есть как ученики класса сидят в конкретном кабинете (в разных кабинетах ученики одного класса сидят по разному)
                * если уроку присвоены класс и кабинет, но нет рассадки этого класса в этом кабинете,
                или не у всех учеников назначено место, то, например, подсвечивать урок
                красным и не давать возможности начать этот урок(надо предусмотреть вариант,
                когда учеников больше чем мест)
                * при нажатии на урок можно будет его начать -> запустить LessonActivity с параметрами этого
                урока(урок ограничение по времени не имеет, время начала и конца нужны только для поиска текущего по времени урока)
                * должен открываться редактор рассадки, скорее всего надо будет
                сдеать кнопку в диалоге, что-то вроде "редактировать рассадку учеников" */
                break;
            case R.id.start_menu_button_my_cabinets://переход в список кабинетов
                intent = new Intent(this, ListOfActivity.class);
                intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
                startActivity(intent);
                break;
            case R.id.start_menu_button_my_classes: {//переход в список классов
                intent = new Intent(this, ListOfActivity.class);
                intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                startActivity(intent);
                break;
            }

        }

    }

    @Override
    public void onBackPressed() {
        //finish();
        Log.i("StartScreenActivity", "back");
        super.onBackPressed();
    }
}
