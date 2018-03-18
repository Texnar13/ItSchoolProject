package com.learning.texnar13.teachersprogect.learnersClassesOut;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.LearnersAndGradesOut.LearnersAndGradesActivity;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.learners_classes_out_toolbar);
        setSupportActionBar(toolbar);

//        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.test_lay);
//        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams((int) getApplicationContext().getResources().getDisplayMetrics().heightPixels, getApplicationContext().getResources().getDisplayMetrics().heightPixels));

//        //------заголовок--------
//        setTitle("Мои классы");

        //кнопка назад
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //------плавающая кнопка с низу--------
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.learners_classes_out_add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //-----диалог создания-----
                //инициализируем диалог
                CreateLearnersClassDialogFragment createClassDialog = new CreateLearnersClassDialogFragment();
                //показать диалог
                createClassDialog.show(getFragmentManager(), "createClassDialog");

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryBlue)));

        //--------экран со списком---------
        //создание
        room = (LinearLayout) findViewById(R.id.learners_classes_out_room);

        //обновляем данные
        getLearnersClasses();
        //выводим с новыми данными
        outLearnersClasses();
    }

//-----------------------------------обновляем список классов-------------------------------------

    void getLearnersClasses() {
        //выводим классы из бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor learnersClass = db.getClasses();
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
//создаем пункт списка-----------
            //список
            //-контейнер
            //--текст

//------контейнер----
            //создаем LinearLayout
            LinearLayout container = new LinearLayout(this);
            container.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);

            //параметры контейнера(т.к. элемент находится в LinearLayout то и параметры используем его)
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,//ш
                    ViewGroup.LayoutParams.WRAP_CONTENT//в
            );
            containerParams.setMargins((int)pxFromDp(4), (int)pxFromDp(4), (int)pxFromDp(4), (int)pxFromDp(0));

//------текст------
            //создаём текст
            TextView item = new TextView(this);
            item.setGravity(Gravity.CENTER);
            item.setTextSize(25);
            item.setTextColor(Color.WHITE);

            //параметры пункта(т.к. элемент находится в LinearLayout то и параметры используем его)
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,//ш
                    ViewGroup.LayoutParams.WRAP_CONTENT//в
            );
            itemParams.gravity = Gravity.CENTER;
            itemParams.setMargins((int) pxFromDp(3), (int) pxFromDp(9), (int) pxFromDp(3), (int) pxFromDp(9));

            //--выводим текст--
            item.setText(classesNames[i]);

//------помещаем текст в контейнер--------
            container.addView(item, itemParams);
//------помещаем контейнер в список-------
            room.addView(container, containerParams);

//------------------нажатие на пункт списка----------------------
            //номер пункта в списке
            final long finalId = learnersClassesId[i];
//--короткое--
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //переходим к ученикам этого класса
                    Intent intent = new Intent(getApplicationContext(), LearnersAndGradesActivity.class);
                    //передаём id выбранного класса
                    intent.putExtra(LearnersAndGradesActivity.CLASS_ID, finalId);
                    startActivity(intent);
                }
            });

//--долгое--
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //инициализируем диалог
                    EditLearnersClassDialogFragment editDialog = new EditLearnersClassDialogFragment();
                    //-данные для диалога-
                    //получаем из бд
                    DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                    //классы по Id
                    Cursor classCursor = db.getClasses(finalId);
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
        helpText1.setGravity(Gravity.CENTER);
        helpText1.setTextSize(20);
        helpText1.setTextColor(getResources().getColor(R.color.colorBackGroundDark));
        //helpText1.setText("Чтобы создать класс, нажмите \"+\" и введите его название. ");
        helpText1.setText(R.string.learners_classes_out_activity_text_help);
        //добавляем
        container.addView(
                helpText1,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

//        //---2 текст---
//        //создаем
//        TextView helpText2 = new TextView(this);
//        helpText2.setGravity(Gravity.CENTER);
//        helpText2.setTextSize(20);
//        helpText2.setTextColor(getResources().getColor(R.color.colorBackGroundDark));
//        helpText2.setText("Чтобы посмотреть список учеников и их оценки, нажмите на нужный вам класс.");
//        //добавляем
//        container.addView(
//                helpText2,
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//
//        //---3 текст---
//        //создаем
//        TextView helpText3 = new TextView(this);
//        helpText3.setGravity(Gravity.CENTER);
//        helpText3.setTextSize(20);
//        helpText3.setTextColor(getResources().getColor(R.color.colorBackGroundDark));
//        helpText3.setText("Чтобы переименовать или удалить класс с учениками, нажмите на него и удерживайте.");
//        //добавляем
//        container.addView(
//                helpText3,
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );

        //---выводим контейнер в экран---
        room.addView(container, containerParams);

    }

    //системные кнопки
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

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px) {
        return px / getApplicationContext().getResources().getDisplayMetrics().density;
    }
}