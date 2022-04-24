package com.learning.texnar13.teachersprogect.cabinetsOut;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.learning.texnar13.teachersprogect.acceptDialog.AcceptDialog;
import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.seatingRedactor.SeatingRedactorActivity;

public class CabinetEditActivity extends AppCompatActivity implements AcceptDialog.AcceptDialogInterface {

    public static final String ARG_CABINET_ID = "cabinetId";
    public static final int CABINET_EDIT_REQUEST_CODE = 200;
    public static final int RESULT_NONE = 0;
    public static final int RESULT_REMOVE_CABINET = 1;
    public static final int RESULT_RENAME_CABINET = 2;

    // быда ли нажата кнопка удалить
    boolean isDelete = false;

    // название кабинета
    String startName;
    // id кабинета
    long cabinetId;
    //
    long[] classesIds;
    //
    String[] classesNames;

    // текстовое поле имени
    EditText editName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // цвет статус бара
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

        setContentView(R.layout.cabinets_out_edit_activity);
        // тулбар
        // убираем заголовок, там свой
        setSupportActionBar(findViewById(R.id.base_blue_toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }
        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(
                R.string.cabinets_out_activity_title_edit_cabinet);


        // получаем входные данные

        // id кабинета
        cabinetId = getIntent().getLongExtra(ARG_CABINET_ID, -1);
        // ошибки
        if (cabinetId == -1) {
            Log.e("TeachersApp", "Ошибка cabinetId для " + this.getClass().getName() + '!');
            finish();
            return;
        }

        { // получаем из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());

            // текущее имя
            Cursor cabinetCursor = db.getCabinet(cabinetId);
            cabinetCursor.moveToFirst();
            startName = cabinetCursor.getString(cabinetCursor
                    .getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_NAME));
            cabinetCursor.close();

            // получаем список классов
            Cursor classesCursor = db.getLearnersClases();
            // id рассаживаемых классов
            classesIds = new long[classesCursor.getCount()];
            // названия рассаживаемых классов
            classesNames = new String[classesCursor.getCount()];
            for (int classesI = 0; classesI < classesCursor.getCount(); classesI++) {
                classesCursor.moveToNext();
                // id классов
                classesIds[classesI] = classesCursor.getLong(classesCursor.getColumnIndexOrThrow(
                        SchoolContract.TableClasses.KEY_ROW_ID));
                // имена классов
                classesNames[classesI] = classesCursor.getString(classesCursor.getColumnIndexOrThrow(
                        SchoolContract.TableClasses.COLUMN_CLASS_NAME));
            }
            db.close();
        }


        // вывод списка классов
        if (classesIds.length == 0) {
            ((LinearLayout) findViewById(R.id.cabinet_out_edit_cabinet_ful_container))
                    .removeView(findViewById(R.id.cabinet_out_edit_cabinet_classes_container));
        } else {
            LinearLayout classesOut = findViewById(R.id.cabinet_out_edit_cabinet_classes_out);

            LayoutInflater inflater = getLayoutInflater();
            for (int classI = 0; classI < classesIds.length; classI++) {
                // создаем кнопку с классом
                TextView element = (TextView) inflater.inflate(R.layout.base_list_element_text_blue, null);
                element.setText(classesNames[classI]);

                LinearLayout.LayoutParams elementParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getResources().getDimensionPixelSize(R.dimen.cabinet_edit_activity_sub_menu_buttons_height)
                );
                elementParams.gravity = Gravity.CENTER;
                elementParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.double_margin);
                classesOut.addView(element, elementParams);

                // нажатие
                final int finalClassI = classI;
                element.setOnClickListener(v -> {
                    // переходим на редактирование рассадки
                    Intent intent = new Intent(this, SeatingRedactorActivity.class);
                    // передаём id выбранного кабинета
                    intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
                    intent.putExtra(SeatingRedactorActivity.CLASS_ID, classesIds[finalClassI]);
                    startActivity(intent);
                });
            }
        }

        // текстовое поле имени
        editName = findViewById(R.id.cabinet_out_edit_cabinet_name_field);
        editName.setText(startName);

        // кнопка расставить парты
        findViewById(R.id.cabinet_out_edit_cabinet_arrange_button).setOnClickListener(v -> {
            // переходим на редактирование этого кабинета
            Intent intent = new Intent(this, CabinetRedactorActivity.class);
            // передаём id выбранного кабинета
            intent.putExtra(CabinetRedactorActivity.EDITED_CABINET_ID, cabinetId);
            startActivity(intent);
        });


        // кнопка удаление
        findViewById(R.id.cabinet_out_edit_cabinet_delete_button).setOnClickListener((View.OnClickListener) view -> {
            // Создаем диалог AcceptDialog с соответствующими текстами
            AcceptDialog accept = new AcceptDialog();
            Bundle args = new Bundle();
            args.putString(AcceptDialog.ARG_ACCEPT_MESSAGE,
                    getResources().getString(R.string.cabinets_out_activity_title_delete_cabinet_ask));
            args.putString(AcceptDialog.ARG_ACCEPT_BUTTON_TEXT,
                    getResources().getString(R.string.cabinets_out_activity_button_text_delete_cabinet));
            accept.setArguments(args);
            accept.show(getSupportFragmentManager(), "delete accept");
        });
    }


    @Override
    public void finish() {
        // сохраняем название кабинета
        if (!isDelete && !editName.getText().toString().equals(startName)) {
            // изменяем кабинет
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            db.setCabinetName(cabinetId, editName.getText().toString().trim());
            db.close();
            // устанавливаем результат
            setResult(RESULT_RENAME_CABINET);
        }
        super.finish();
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


    // Обратная связь от диалога AcceptDialog
    @Override
    public void accept() {
        isDelete = true;
        //удаляем кабинет
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.deleteCabinets(cabinetId);
        db.close();
        // устанавливаем результат
        setResult(RESULT_REMOVE_CABINET);
        finish();
    }
}