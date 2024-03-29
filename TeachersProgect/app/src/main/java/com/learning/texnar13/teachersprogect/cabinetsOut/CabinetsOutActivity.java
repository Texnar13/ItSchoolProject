package com.learning.texnar13.teachersprogect.cabinetsOut;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

public class CabinetsOutActivity extends AppCompatActivity implements CreateCabinetInterface {

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
        setSupportActionBar(findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(R.string.title_activity_cabinets_out);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.base_background_color, getTheme()));

            // включен ли ночной режим
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            if (Configuration.UI_MODE_NIGHT_YES != currentNightMode)
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        // ------ кнопка добавления кабинета ------
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // плавающая кнопка с низу
            findViewById(R.id.cabinets_out_fab).setOnClickListener(view -> {
                // диалог создания
                // инициализируем диалог
                CreateCabinetDialogFragment createCabinetDialog = new CreateCabinetDialogFragment();
                // показать диалог
                createCabinetDialog.show(getFragmentManager(), "createCabinetDialog");
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
                    cabinets.getColumnIndexOrThrow(SchoolContract.TableCabinets.KEY_ROW_ID)
            );
            cabinetsNames[i] = cabinets.getString(
                    cabinets.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_NAME)
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
            cabinetContainer.setBackgroundResource(R.drawable.base_background_button_round_gray);
            // параметры контейнера
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,//ш
                    getResources().getDimensionPixelSize(R.dimen.simple_buttons_height)//в
            );
            containerParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    0
            );
            room.addView(cabinetContainer, containerParams);

            // создаём текст
            TextView item = new TextView(this);
            item.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_semibold));
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.simple_buttons_text_size));
            item.setTextColor(getResources().getColor(R.color.text_color_simple));
            item.setText(cabinetsNames[i]);
            //параметры пункта
            RelativeLayout.LayoutParams itemParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,//ш
                    RelativeLayout.LayoutParams.WRAP_CONTENT//в
            );
            itemParams.addRule(RelativeLayout.CENTER_VERTICAL);
            itemParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin), 0,
                    (int) (getResources().getDimension(R.dimen.base_buttons_arrow_size)
                            + getResources().getDimension(R.dimen.double_margin)), 0
            );
            cabinetContainer.addView(item, itemParams);

            // стрелочка
            ImageView arrow = new ImageView(this);
            arrow.setImageResource(R.drawable._base_button_arrow_forward_blue);
            RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.base_buttons_arrow_size),
                    (int) getResources().getDimension(R.dimen.base_buttons_arrow_size)
            );
            arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            arrowParams.addRule(RelativeLayout.CENTER_VERTICAL);
            arrowParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.one_and_half_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.one_and_half_margin)
            );
            cabinetContainer.addView(arrow, arrowParams);


            // короткое нажатие на пункт списка
            // номер пункта в списке
            final long finalId = cabinetsId[i];
            cabinetContainer.setOnClickListener(view -> {
                // активность редактирования имени кабинета
                Intent editCabinetNameIntent = new Intent(CabinetsOutActivity.this, CabinetEditActivity.class);
                // id кабинета
                editCabinetNameIntent.putExtra(CabinetEditActivity.ARG_CABINET_ID, finalId);
                // запускаем
                startActivityForResult(editCabinetNameIntent, CabinetEditActivity.CABINET_EDIT_REQUEST_CODE);
            });
        }

// ------ в конце выводим текст с подсказкой и кнопку добавить ------

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
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    0
            );
            room.addView(learnersClassContainer, containerParams);

            // создаём текст
            TextView item = new TextView(this);
            item.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_medium));
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            item.setTextColor(getResources().getColor(R.color.text_color_simple));
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
                    (int) (getResources().getDimension(R.dimen.base_buttons_arrow_size)
                            + 2 * getResources().getDimension(R.dimen.simple_margin)),
                    0
            );
            learnersClassContainer.addView(item, itemParams);

            // стрелочка
            ImageView arrow = new ImageView(this);
            arrow.setImageResource(R.drawable.cabinets_activity_button_add___kitkat);
            RelativeLayout.LayoutParams arrowParams = new RelativeLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.base_buttons_arrow_size),
                    (int) getResources().getDimension(R.dimen.base_buttons_arrow_size)
            );
            arrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            arrowParams.addRule(RelativeLayout.CENTER_VERTICAL);
            arrowParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.one_and_half_margin),
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.one_and_half_margin)
            );
            learnersClassContainer.addView(arrow, arrowParams);


            // нажатие на пункт списка
            learnersClassContainer.setOnClickListener(view -> {
                // диалог создания
                // инициализируем диалог
                CreateCabinetDialogFragment createCabinetDialog = new CreateCabinetDialogFragment();
                // показать диалог
                createCabinetDialog.show(getFragmentManager(), "createCabinetDialog");
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
//        containerParams.setMargins(
//                (int) getResources().getDimension(R.dimen.simple_margin),
//                (int) getResources().getDimension(R.dimen.simple_margin),
//                (int) getResources().getDimension(R.dimen.simple_margin),
//                (int) getResources().getDimension(R.dimen.simple_margin)
//        );

        // подсказка
//        //создаем
//        TextView helpText = new TextView(this);
//        helpText.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_medium));
//        helpText.setGravity(Gravity.CENTER);
//        helpText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
//        helpText.setTextColor(getResources().getColor(R.color.backgroundLiteGray));
//        helpText.setText(R.string.cabinets_out_activity_text_help);
//        //добавляем
//        container.addView(
//                helpText,
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//
//        //---выводим контейнер в экран---
//        room.addView(container, containerParams);
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

    @Override
    // обратная связь от активности CabinetEditActivity
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CabinetEditActivity.CABINET_EDIT_REQUEST_CODE) {
            switch (resultCode) {
                case CabinetEditActivity.RESULT_NONE:
                    break;
                case CabinetEditActivity.RESULT_REMOVE_CABINET:
                case CabinetEditActivity.RESULT_RENAME_CABINET:
                    //опять выводим списки
                    getCabinets();
                    outCabinets();
                    break;
            }
        }
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