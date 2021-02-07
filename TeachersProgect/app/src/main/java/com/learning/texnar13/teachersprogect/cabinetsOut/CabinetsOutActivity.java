package com.learning.texnar13.teachersprogect.cabinetsOut;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;

public class CabinetsOutActivity extends AppCompatActivity implements EditCabinetDialogInterface, CreateCabinetInterface {

    //static потом (для переворота)
    Long[] cabinetsId;
    String[] cabinetsNames;
    LinearLayout room;


    // создаем активность
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // обновляем значение локали
        MyApplication.updateLangForContext(this);


        // раздуваем layout
        setContentView(R.layout.cabinets_out_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar((Toolbar) findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        getSupportActionBar().setTitle("");
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(R.string.title_activity_cabinets_out);


        // вертикальная ориентация
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        // ------ кнопка добавления кабинета ------
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // плавающая кнопка с низу
            findViewById(R.id.cabinets_out_fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // диалог создания
                    // инициализируем диалог
                    CreateCabinetDialogFragment createCabinetDialog = new CreateCabinetDialogFragment();
                    // показать диалог
                    createCabinetDialog.show(getFragmentManager(), "createCabinetDialog");
                }
            });
        } else {
            // настраиваем программный вывод векторных изображений
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        //--------экран со списком---------
        //создание
        room = findViewById(R.id.cabinets_out_room);

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


            //создаем контейнер
            RelativeLayout cabinetContainer = new RelativeLayout(this);
            cabinetContainer.setBackgroundResource(R.drawable.base_dialog_background_dwhite_full_round);
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
            room.addView(cabinetContainer, containerParams);

            // создаём текст
            TextView item = new TextView(this);
            item.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            item.setTextColor(Color.BLACK);
            item.setText(cabinetsNames[i]);
            //параметры пункта
            RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,//ш
                    RelativeLayout.LayoutParams.WRAP_CONTENT//в
            );
            itemParams.addRule(RelativeLayout.CENTER_VERTICAL);
            itemParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin), 0,
                    (int) (getResources().getDimension(R.dimen.my_icon_small_size)
                            + 2 * getResources().getDimension(R.dimen.simple_margin)), 0
            );
            cabinetContainer.addView(item, itemParams);

            // стрелочка
            ImageView arrow = new ImageView(this);
            arrow.setImageResource(R.drawable.base_button_forward_blue);
            RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.my_icon_small_size),
                    (int) getResources().getDimension(R.dimen.my_icon_small_size)
            );
            arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            arrowParams.addRule(RelativeLayout.CENTER_VERTICAL);
            arrowParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.half_more_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.half_more_margin)
            );
            cabinetContainer.addView(arrow, arrowParams);


            // короткое нажатие на пункт списка
            // номер пункта в списке
            final long finalId = cabinetsId[i];
            cabinetContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

//                    // переходим на редактирование этого кабинета
//                    Intent intent = new Intent(getApplicationContext(), CabinetRedactorActivity.class);
//                    // передаём id выбранного кабинета
//                    intent.putExtra(CabinetRedactorActivity.EDITED_CABINET_ID, finalId);
//                    startActivity(intent);
//                }
//            });
//
//            // долгое
//            cabinetContainer.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {

                    // инициализируем диалог
                    EditCabinetDialogFragment editDialog = new EditCabinetDialogFragment();

                    // данные для диалога
                    // получаем из бд
                    DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                    // создаем обьект с данными
                    Bundle args = new Bundle();
                    args.putLong(EditCabinetDialogFragment.ARG_CABINET_ID, finalId);
                    // имя кабинета по Id
                    Cursor cabinetCursor = db.getCabinet(finalId);
                    cabinetCursor.moveToFirst();
                    args.putString(EditCabinetDialogFragment.ARG_CABINET_NAME, cabinetCursor.getString(
                            cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)
                    ));
                    cabinetCursor.close();
                    // получаем список классов
                    Cursor classesCursor = db.getLearnersClass();
                    long[] classesIds = new long[classesCursor.getCount()];
                    String[] classesNames = new String[classesCursor.getCount()];
                    for (int classesI = 0; classesI < classesCursor.getCount(); classesI++) {
                        classesCursor.moveToNext();
                        // id классов
                        classesIds[classesI] = classesCursor.getLong(classesCursor.getColumnIndex(
                                SchoolContract.TableClasses.KEY_CLASS_ID
                        ));
                        // имена классов
                        classesNames[classesI] = classesCursor.getString(classesCursor.getColumnIndex(
                                SchoolContract.TableClasses.COLUMN_CLASS_NAME
                        ));
                    }
                    args.putLongArray(EditCabinetDialogFragment.ARG_ARRAY_CLASSES_ID, classesIds);
                    args.putStringArray(EditCabinetDialogFragment.ARG_ARRAY_CLASSES_NAMES, classesNames);
                    classesCursor.close();

                    // данные диалогу
                    editDialog.setArguments(args);

                    // показать диалог
                    editDialog.show(getSupportFragmentManager(), "editCabinetDialog");
                    // заканчиваем работу с бд

                    db.close();
                    //return true;
                }
            });
        }

// ------ в конце выводим текст с подсказкой и кнопку добавить ------

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

            // создаем контейнер
            RelativeLayout learnersClassContainer = new RelativeLayout(this);
            learnersClassContainer.setBackgroundResource(R.drawable.base_dialog_background_dwhite_full_round);
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
            item.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            item.setTextColor(Color.BLACK);
            item.setText(R.string.cabinets_out_activity_dialog_title_create_cabinet);
            // параметры пункта
            RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,//ш
                    RelativeLayout.LayoutParams.WRAP_CONTENT//в
            );
            itemParams.addRule(RelativeLayout.CENTER_VERTICAL);
            itemParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    0,
                    (int) (getResources().getDimension(R.dimen.my_icon_small_size)
                            + 2 * getResources().getDimension(R.dimen.simple_margin)),
                    0
            );
            learnersClassContainer.addView(item, itemParams);

            // стрелочка
            ImageView arrow = new ImageView(this);
            arrow.setImageResource(R.drawable.cabinets_activity_button_add___kitkat);
            RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.my_icon_small_size),
                    (int) getResources().getDimension(R.dimen.my_icon_small_size)
            );
            arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            arrowParams.addRule(RelativeLayout.CENTER_VERTICAL);
            arrowParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.half_more_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.half_more_margin)
            );
            learnersClassContainer.addView(arrow, arrowParams);


            // нажатие на пункт списка
            learnersClassContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // диалог создания
                    // инициализируем диалог
                    CreateCabinetDialogFragment createCabinetDialog = new CreateCabinetDialogFragment();
                    // показать диалог
                    createCabinetDialog.show(getFragmentManager(), "createCabinetDialog");
                }
            });
        }

        //---контейнер---
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        //параметры контейнера
        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        containerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );

        // подсказка
        //создаем
        TextView helpText = new TextView(this);
        helpText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
        helpText.setGravity(Gravity.CENTER);
        helpText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        helpText.setTextColor(getResources().getColor(R.color.backgroundLiteGray));
        helpText.setText(R.string.cabinets_out_activity_text_help);
        //добавляем
        container.addView(
                helpText,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //---выводим контейнер в экран---
        room.addView(container, containerParams);
    }


    // методы диалогов

    // создание
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


    // переименование
    @Override
    public void editCabinetName(long cabinetId, String name) {
        // изменяем кабинет
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.setCabinetName(cabinetId, name);
        db.close();
        // опять выводим списки
        getCabinets();
        outCabinets();
    }

    // расставить парты
    @Override
    public void arrangeCabinetDesks(long cabinetId) {
        // переходим на редактирование этого кабинета
        Intent intent = new Intent(getApplicationContext(), CabinetRedactorActivity.class);
        // передаём id выбранного кабинета
        intent.putExtra(CabinetRedactorActivity.EDITED_CABINET_ID, cabinetId);
        startActivity(intent);
    }

    // посадить учеников
    @Override
    public void arrangeLearnersInCabinet(long cabinetId, long classId) {
        // переходим на редактирование рассадки
        Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
        // передаём id выбранного кабинета
        intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
        intent.putExtra(SeatingRedactorActivity.CLASS_ID, classId);
        startActivity(intent);
    }

    // удаление
    @Override
    public void removeCabinet(long cabinetId) {
        //удаляем кабинет
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.deleteCabinets(cabinetId);
        db.close();
        //опять выводим списки
        getCabinets();
        outCabinets();
    }


    // кнопка назад в actionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

}

// Убираем панель уведомлений
//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
