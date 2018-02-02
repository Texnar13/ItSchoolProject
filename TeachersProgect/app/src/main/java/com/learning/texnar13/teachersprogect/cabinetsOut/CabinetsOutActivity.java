package com.learning.texnar13.teachersprogect.cabinetsOut;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class CabinetsOutActivity extends AppCompatActivity implements EditCabinetDialogInterface, CreateCabinetInterface {

    //static потом (для переворота)
    Long[] cabinetsId;
    String[] cabinetsNames;
    LinearLayout room;

//---------------------------------методы диалогов--------------------------------------------------


//-----создание-----

    @Override
    public void createCabinet(String name) {
        //создаем кабинет
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.createCabinet(name);
        db.close();
        //опять выводим списки
        getCabinets();
        outCabinets();
    }

//-----редактирование-----

    //переименование
    @Override
    public void editCabinet(String name, long cabinetId) {
        //изменяем кабинет
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> arrayList = new ArrayList<>();
        arrayList.add(cabinetId);
        db.setCabinetName(arrayList, name);
        db.close();
        //опять выводим списки
        getCabinets();
        outCabinets();
    }

    //удаление
    @Override
    public void removeCabinet(long cabinetId) {
        //удаляем кабинет
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        ArrayList<Long> arrayList = new ArrayList<>();
        arrayList.add(cabinetId);
        db.deleteCabinets(arrayList);
        db.close();
        //опять выводим списки
        getCabinets();
        outCabinets();
    }

//-------------------------------меню сверху--------------------------------------------------------

//убрал за ненадобностью в подсказке
//    //раздуваем неаше меню
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.cabinets_out_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    //назначаем функции меню
//    @Override
//    public boolean onPrepareOptionsMenu(final Menu menu) {
//        //кнопка помощь
//        menu.findItem(R.id.cabinets_out_menu_item_help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                Toast toast = Toast.makeText(getApplicationContext(),"В разработке ¯\\_(ツ)_/¯",Toast.LENGTH_LONG);
//                toast.show();
//
//                return true;
//            }
//        });
//        return super.onPrepareOptionsMenu(menu);
//    }

//------------------------------создаем активность--------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinets_out);
        Toolbar toolbar = (Toolbar) findViewById(R.id.cabinets_out_toolbar);
        setSupportActionBar(toolbar);

        //------заголовок--------
        setTitle("Мои кабинеты");

        //кнопка назад
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //------плавающая кнопка с низу--------
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //-----диалог создания-----
                //инициализируем диалог
                CreateCabinetDialogFragment createCabinetDialog = new CreateCabinetDialogFragment();
                //показать диалог
                createCabinetDialog.show(getFragmentManager(), "createCabinetDialog");

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#f5ce9d")));

        //--------экран со списком---------
        //создание
        room = (LinearLayout) findViewById(R.id.cabinets_out_room);

        //обновляем данные
        getCabinets();
        //выводим с новыми данными
        outCabinets();
    }

//-----------------------------------обновляем список кабинетов-------------------------------------

    void getCabinets() {
        //выводим кабинеты из бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        Cursor cabinets = db.getCabinets();
        //инициализируем и очищаем массивы
        cabinetsId = new Long[cabinets.getCount()];
        cabinetsNames = new String[cabinets.getCount()];
        //пробегаемся по курсору
        for (int i = 0; i < cabinetsId.length; i++) {
            cabinets.moveToPosition(i);
            //получаем id кабинета
            cabinetsId[i] = cabinets.getLong(
                    cabinets.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID)
            );
            cabinetsNames[i] = cabinets.getString(
                    cabinets.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)
            );
        }
        //заканчиваем работу
        cabinets.close();
        db.close();
    }

//-----------------------------------выводим список кабинетов---------------------------------------

    void outCabinets() {
        //удаляем все что было
        room.removeAllViews();
        //пробегаемся по кабинетам и создаем список из LinearLayout
        for (int i = 0; i < cabinetsId.length; i++) {
//создаем пункт списка-----------
            //список
            //-контейнер
            //--текст

//------контейнер----
            //создаем LinearLayout
            LinearLayout container = new LinearLayout(this);
            container.setBackgroundColor(Color.parseColor("#f5ce9d"));

            //параметры контейнера(т.к. элемент находится в LinearLayout то и параметры используем его)
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,//ш
                    ViewGroup.LayoutParams.WRAP_CONTENT//в
            );
            containerParams.setMargins(2, 2, 2, 3);

//------текст------
            //создаём текст
            TextView item = new TextView(this);
            item.setGravity(Gravity.CENTER);
            item.setTextSize(25);
            item.setTextColor(Color.parseColor("#88591d"));

            //параметры пункта(т.к. элемент находится в LinearLayout то и параметры используем его)
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,//ш
                    ViewGroup.LayoutParams.WRAP_CONTENT//в
            );
            itemParams.gravity = Gravity.CENTER;
            // отступы текста в рамке
            itemParams.setMargins((int) pxFromDp(3), (int) pxFromDp(9), (int) pxFromDp(3), (int) pxFromDp(9));

            //--выводим текст--
            item.setText(cabinetsNames[i]);

//------помещаем текст в контейнер--------
            container.addView(item, itemParams);
//------помещаем контейнер в список-------
            room.addView(container, containerParams);

//------нажатие на пункт списка-------
            //номер пункта в списке
            final long finalId = cabinetsId[i];
//--короткое--
            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //переходим на редактирование этого кабинета
                    Intent intent = new Intent(getApplicationContext(), CabinetRedactorActivity.class);
                    //передаём id выбранного кабинета
                    intent.putExtra(CabinetRedactorActivity.EDITED_OBJECT_ID, finalId);
                    startActivity(intent);
                }
            });

//--долгое--
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //инициализируем диалог
                    EditCabinetDialogFragment editDialog = new EditCabinetDialogFragment();
                    //-данные для диалога-
                    //получаем из бд
                    DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                    //кабинеты по Id
                    Cursor cabinetCursor = db.getCabinets(finalId);
                    cabinetCursor.moveToFirst();
                    //создаем обьект с данными
                    Bundle args = new Bundle();
                    args.putLong("cabinetId", finalId);
                    args.putString("name", cabinetCursor.getString(
                            cabinetCursor.getColumnIndex(
                                    SchoolContract.TableCabinets.COLUMN_NAME)
                    ));
                    //данные диалогу
                    editDialog.setArguments(args);
                    //показать диалог
                    editDialog.show(getFragmentManager(), "editCabinetDialog");
                    //заканчиваем работу с бд
                    cabinetCursor.close();
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
        helpText1.setTextColor(Color.GRAY);
        helpText1.setText("Чтобы создать кабинет, нажмите \"+\" и введите его название. ");
        //добавляем
        container.addView(
                helpText1,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //---2 текст---
        //создаем
        TextView helpText2 = new TextView(this);
        helpText2.setGravity(Gravity.CENTER);
        helpText2.setTextSize(20);
        helpText2.setTextColor(Color.GRAY);
        helpText2.setText("Чтобы расставить парты, нажмите на нужный вам кабинет.");
        //добавляем
        container.addView(
                helpText2,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //---3 текст---
        //создаем
        TextView helpText3 = new TextView(this);
        helpText3.setGravity(Gravity.CENTER);
        helpText3.setTextSize(20);
        helpText3.setTextColor(Color.GRAY);
        helpText3.setText("Чтобы переименовать или удалить кабинет, нажмите на него и удерживайте.");
        //добавляем
        container.addView(
                helpText3,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //---выводим контейнер в экран---
        room.addView(container, containerParams);

    }

//------системные кнопки--------

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
