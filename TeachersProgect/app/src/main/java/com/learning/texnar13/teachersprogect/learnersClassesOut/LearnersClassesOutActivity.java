package com.learning.texnar13.teachersprogect.learnersClassesOut;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesActivity;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class LearnersClassesOutActivity extends AppCompatActivity implements EditLearnersClassDialogInterface, CreateLearnersClassDialogInterface {

    //static потом (для переворота)
    Long[] learnersClassesId;
    String[] classesNames;
    LinearLayout room;

//---------------------------------методы диалогов--------------------------------------------------


//-----создание-----

    @Override
    public void createLearnersClass(String name) {
        //создаем класс
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.createClass(name);
        db.close();
        //опять выводим списки
        getLearnersClasses();
        outLearnersClasses();
    }

//-----редактирование-----

    //переименование
    @Override
    public void editLearnersClass(String name, long classId) {
        //изменяем класс
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> arrayList = new ArrayList<>();
        arrayList.add(classId);
        db.setClassesNames(arrayList, name);
        db.close();
        //опять выводим списки
        getLearnersClasses();
        outLearnersClasses();
    }

    //удаление
    @Override
    public void removeLearnersClass(long classId) {
        //удаляем класс
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> arrayList = new ArrayList<>();
        arrayList.add(classId);
        db.deleteClasses(arrayList);
        db.close();
        //опять выводим списки
        getLearnersClasses();
        outLearnersClasses();
    }

//------------------------------создаем активность--------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learners_classes_out);

        // вертикальная ориентация
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        //кнопка назад
        findViewById(R.id.learners_classes_out_toolbar_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выходим из активности
                onBackPressed();
            }
        });

        //------плавающая кнопка с низу--------
        findViewById(R.id.learners_classes_out_add_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //-----диалог создания-----
                //инициализируем диалог
                CreateLearnersClassDialogFragment createClassDialog = new CreateLearnersClassDialogFragment();
                //показать диалог
                createClassDialog.show(getFragmentManager(), "createClassDialog");
            }
        });

        //--------экран со списком---------
        //создание
        room = findViewById(R.id.learners_classes_out_room);

        //обновляем данные
        getLearnersClasses();
        //выводим с новыми данными
        outLearnersClasses();
    }

//-----------------------------------обновляем список классов-------------------------------------

    void getLearnersClasses() {
        //выводим классы из бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersClass = db.getLearnersClass();
        //инициализируем и очищаем массивы
        learnersClassesId = new Long[learnersClass.getCount()];
        classesNames = new String[learnersClass.getCount()];
        //пробегаемся по курсору
        for (int i = 0; i < learnersClassesId.length; i++) {
            learnersClass.moveToPosition(i);
            //получаем id класса
            learnersClassesId[i] = learnersClass.getLong(
                    learnersClass.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID)
            );
            classesNames[i] = learnersClass.getString(
                    learnersClass.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)
            );
        }
        //заканчиваем работу
        learnersClass.close();
        db.close();
    }

//-----------------------------------выводим список классов---------------------------------------

    void outLearnersClasses() {
        //удаляем все что было
        room.removeAllViews();
        //пробегаемся по классам и создаем список из LinearLayout
        for (int i = 0; i < learnersClassesId.length; i++) {

            //создаем контейнер
            RelativeLayout learnersClassContainer = new RelativeLayout(this);
            learnersClassContainer.setBackgroundResource(R.drawable.__background_round_simple_full_dark_white);
            // параметры контейнера
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,//ш
                    ViewGroup.LayoutParams.WRAP_CONTENT//в
            );
            containerParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    0
            );
            room.addView(learnersClassContainer, containerParams);

            // создаём текст
            TextView item = new TextView(this);
            item.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_light));
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            item.setTextColor(Color.BLACK);
            item.setText(classesNames[i]);
            //параметры пункта
            RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,//ш
                    RelativeLayout.LayoutParams.WRAP_CONTENT//в
            );
            itemParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) (getResources().getDimension(R.dimen.my_icon_small_size)
                            + 2 * getResources().getDimension(R.dimen.simple_margin)),
                    (int) getResources().getDimension(R.dimen.double_margin)
            );
            learnersClassContainer.addView(item, itemParams);

            // стрелочка
            LinearLayout arrow = new LinearLayout(this);
            arrow.setBackgroundResource(R.drawable.__button_forward_arrow_blue);
            RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.my_icon_small_size),
                    (int) getResources().getDimension(R.dimen.my_icon_small_size)
            );
            arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            arrowParams.addRule(RelativeLayout.CENTER_VERTICAL);
            arrowParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin)
            );
            learnersClassContainer.addView(arrow, arrowParams);


            // короткое нажатие на пункт списка
            final long finalId = learnersClassesId[i];
            learnersClassContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //переходим к ученикам этого класса
                    Intent intent = new Intent(getApplicationContext(), LearnersAndGradesActivity.class);
                    //передаём id выбранного класса
                    intent.putExtra(LearnersAndGradesActivity.CLASS_ID, finalId);
                    startActivity(intent);
                }
            });

            // долгое
            learnersClassContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //инициализируем диалог
                    EditLearnersClassDialogFragment editDialog = new EditLearnersClassDialogFragment();
                    //-данные для диалога-
                    //получаем из бд
                    DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                    //классы по Id
                    Cursor classCursor = db.getLearnersClass(finalId);
                    classCursor.moveToFirst();
                    //создаем обьект с данными
                    Bundle args = new Bundle();
                    args.putLong("classId", finalId);
                    args.putString("name", classCursor.getString(
                            classCursor.getColumnIndex(
                                    SchoolContract.TableClasses.COLUMN_CLASS_NAME)
                    ));
                    //данные диалогу
                    editDialog.setArguments(args);
                    //показать диалог
                    editDialog.show(getFragmentManager(), "editClassDialog");
                    //заканчиваем работу с бд
                    classCursor.close();
                    db.close();
                    return true;
                }
            });
        }

//------в конце выводим текст с подсказкой------

        //экран
        //-...
        //-контейнер
        //--текст
        //--текст
        //--текст
        //-контейнер
        //экран

        //---контейнер---
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        //параметры контейнера
        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        containerParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));

        //---1 текст---
        //создаем
        TextView helpText1 = new TextView(this);
        helpText1.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_light));
        helpText1.setGravity(Gravity.CENTER);
        helpText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        helpText1.setTextColor(getResources().getColor(R.color.backgroundLiteGray));
        helpText1.setText(R.string.learners_classes_out_activity_text_help);
        //добавляем
        container.addView(
                helpText1,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //---выводим контейнер в экран---
        room.addView(container, containerParams);

    }

    //системные кнопки
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//кнопка назад в actionBar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
    }
}