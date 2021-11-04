package com.learning.texnar13.teachersprogect.learnersClassesOut;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import android.util.Log;
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

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.acceptDialog.AcceptDialog;
import com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesActivity;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

public class LearnersClassesOutActivity extends AppCompatActivity implements
        EditLearnersClassDialogInterface, CreateLearnersClassDialogInterface, AcceptDialog.AcceptDialogInterface {

    //static потом (для переворота)
    Long[] learnersClassesId;
    String[] classesNames;
    LinearLayout room;

    long selectedId = -1;
    long selectedIdForDelete = -1;

//------------------------------создаем активность--------------------------------------------------

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // обновляем значение локали
        MyApplication.updateLangForContext(this);


        // раздуваем layout
        setContentView(R.layout.learners_classes_out_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar(findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }
        ((TextView) findViewById(R.id.base_blue_toolbar_title))
                .setText(R.string.title_activity_learners_classes_out);


        // вертикальная ориентация
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // ------ кнопка добавления класса ------
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // плавающая кнопка с низу
            findViewById(R.id.learners_classes_out_add_fab).setOnClickListener(view -> {
                // диалог создания
                // инициализируем диалог
                CreateLearnersClassDialogFragment createClassDialog = new CreateLearnersClassDialogFragment();
                // показать диалог
                createClassDialog.show(getSupportFragmentManager(), "createClassDialog");
            });
        } else {
            // настраиваем программный вывод векторных изображений
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }


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
        Cursor learnersClass = db.getLearnersClases();
        //инициализируем и очищаем массивы
        learnersClassesId = new Long[learnersClass.getCount()];
        classesNames = new String[learnersClass.getCount()];
        //пробегаемся по курсору
        for (int i = 0; i < learnersClassesId.length; i++) {
            learnersClass.moveToPosition(i);
            //получаем id класса
            learnersClassesId[i] = learnersClass.getLong(
                    learnersClass.getColumnIndex(SchoolContract.TableClasses.KEY_ROW_ID)
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
            learnersClassContainer.setBackgroundResource(R.drawable.base_background_button_round_gray);
            // параметры контейнера
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.simple_buttons_height)
            );
            containerParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    0
            );
            room.addView(learnersClassContainer, containerParams);

            // создаём текст
            TextView item = new TextView(this);
            item.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_semibold));
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.simple_buttons_text_size));
            item.setTextColor(Color.BLACK);
            item.setText(classesNames[i]);
            //параметры пункта
            RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,//ш
                    RelativeLayout.LayoutParams.WRAP_CONTENT//в
            );
            itemParams.addRule(RelativeLayout.CENTER_VERTICAL);
            itemParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    0,
                    (int) (getResources().getDimension(R.dimen.base_buttons_arrow_size)
                            + 2 * getResources().getDimension(R.dimen.simple_margin)),
                    0
            );

            learnersClassContainer.addView(item, itemParams);

            // стрелочка
            ImageView arrow = new ImageView(this);
            arrow.setImageResource(R.drawable.base_button_arrow_forward_blue);
            RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.base_buttons_arrow_size),
                    (int) getResources().getDimension(R.dimen.base_buttons_arrow_size)
            );
            arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            arrowParams.addRule(RelativeLayout.CENTER_VERTICAL);
            arrowParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.half_more_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.half_more_margin)
            );
            learnersClassContainer.addView(arrow, arrowParams);


            // короткое нажатие на пункт списка
            final long finalId = learnersClassesId[i];
            learnersClassContainer.setOnClickListener(view -> {
                //переходим к ученикам этого класса
                Intent intent = new Intent(getApplicationContext(), LearnersAndGradesActivity.class);
                //передаём id выбранного класса
                intent.putExtra(LearnersAndGradesActivity.CLASS_ID, finalId);
                startActivity(intent);
            });

            // долгое
            learnersClassContainer.setOnLongClickListener(view -> {

                // ставим класс как выбранный
                selectedId = finalId;

                //инициализируем диалог
                EditLearnersClassDialogFragment editDialog = new EditLearnersClassDialogFragment();
                //-данные для диалога-
                //получаем из бд
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                //классы по Id
                Cursor classCursor = db.getLearnersClases(finalId);
                classCursor.moveToFirst();
                //создаем обьект с данными
                Bundle args = new Bundle();
                args.putString("name", classCursor.getString(
                        classCursor.getColumnIndex(
                                SchoolContract.TableClasses.COLUMN_CLASS_NAME)
                ));
                //данные диалогу
                editDialog.setArguments(args);
                //показать диалог
                editDialog.show(getSupportFragmentManager(), "editClassDialog");
                //заканчиваем работу с бд
                classCursor.close();
                db.close();
                return true;
            });
        }

// ------ в конце выводим текст с подсказкой и кнопку ------

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

            // создаем контейнер
            RelativeLayout learnersClassContainer = new RelativeLayout(this);
            learnersClassContainer.setBackgroundResource(R.drawable.base_background_button_round_gray);
            // параметры контейнера
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.simple_buttons_height)
            );
            containerParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    0
            );
            room.addView(learnersClassContainer, containerParams);

            // создаём текст
            TextView item = new TextView(this);
            item.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_medium));
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            item.setTextColor(Color.BLACK);
            item.setText(R.string.learners_classes_out_activity_dialog_title_create_class);
            // параметры пункта
            RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,//ш
                    RelativeLayout.LayoutParams.WRAP_CONTENT//в
            );
            itemParams.addRule(RelativeLayout.CENTER_VERTICAL);
            itemParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    0,
                    (int) (getResources().getDimension(R.dimen.base_buttons_arrow_size)
                            + 2 * getResources().getDimension(R.dimen.simple_margin)),
                    0
            );
            learnersClassContainer.addView(item, itemParams);

            // стрелочка
            ImageView arrow = new ImageView(this);
            arrow.setImageResource(R.drawable.learners_classes_activity_button_add___kitkat);
            RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.base_buttons_arrow_size),
                    (int) getResources().getDimension(R.dimen.base_buttons_arrow_size)
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
            learnersClassContainer.setOnClickListener(view -> {
                // диалог создания
                // инициализируем диалог
                CreateLearnersClassDialogFragment createClassDialog = new CreateLearnersClassDialogFragment();
                // показать диалог
                createClassDialog.show(getSupportFragmentManager(), "createClassDialog");
            });
        }

//        //---контейнер---
//        LinearLayout container = new LinearLayout(this);
//        container.setOrientation(LinearLayout.VERTICAL);
//        //параметры контейнера
//        LinearLayout.LayoutParams containerParams =
//                new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                );
//        containerParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));
//
//        //---1 текст---
//        //создаем
//        TextView helpText1 = new TextView(this);
//        helpText1.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_medium));
//        helpText1.setGravity(Gravity.CENTER);
//        helpText1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
//        helpText1.setTextColor(getResources().getColor(R.color.backgroundLiteGray));
//        helpText1.setText(R.string.learners_classes_out_activity_text_help);
//        //добавляем
//        container.addView(
//                helpText1,
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//
//        //---выводим контейнер в экран---
//        room.addView(container, containerParams);

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
    public void editLearnersClass(String name) {
        //изменяем класс
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.setClassName(selectedId, name);
        db.close();
        selectedId = -1;
        //опять выводим списки
        getLearnersClasses();
        outLearnersClasses();
    }

    // удаление
    @Override
    public void removeLearnersClass() {
        // Создаем диалог AcceptDialog с соответствующими текстами
        AcceptDialog accept = new AcceptDialog();
        Bundle args = new Bundle();
        args.putString(AcceptDialog.ARG_ACCEPT_MESSAGE,
                getResources().getString(R.string.learners_classes_out_activity_dialog_button_delete_class_ask));
        args.putString(AcceptDialog.ARG_ACCEPT_BUTTON_TEXT,
                getResources().getString(R.string.learners_classes_out_activity_dialog_button_delete_class));
        accept.setArguments(args);
        accept.show(getSupportFragmentManager(), "delete accept");

        selectedIdForDelete = selectedId;
        selectedId = -1;
    }


    // обратная связь от диалога подтверждения удаления
    @Override
    public void accept() {
        if (selectedIdForDelete != -1) {
            //удаляем класс
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            db.deleteClass(selectedIdForDelete);
            db.close();
            // опять выводим списки
            getLearnersClasses();
            outLearnersClasses();
        }
        selectedIdForDelete = -1;
        selectedId = -1;
    }

}