package com.learning.texnar13.teachersprogect.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

public class SettingsActivity extends AppCompatActivity implements EditMaxAnswersDialogInterface, EditTimeDialogFragmentInterface, EditLocaleDialogFragmentInterface, EditGradesTypeDialogFragmentInterface, SettingsRemoveInterface {

    TextView maxGradeText;

    // межстраничный баннер открывающийся при выходе из настроек
    InterstitialAd mInterstitialAd;

    // создание экрана
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // отключаем поворот
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // загружаем межстраничный баннер настроек
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5709922862247260/3501279089");// работает
        // создаем запрос
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)// тестовая реклама"239C7C3FF5E172E5131C0FAA9994FDBF"
                .addTestDevice("239C7C3FF5E172E5131C0FAA9994FDBF")
                .build();
        mInterstitialAd.loadAd(adRequest);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);

//-------------максимальный ответ-------------



        // ставим прошлый максимум
        maxGradeText = ((TextView)findViewById(R.id.activity_settings_edit_max_answers_count_text));
        maxGradeText.setText(
                String.format(
                        getResources().getString(R.string.settings_activity_button_edit_max_answer),
                        "" + db.getSettingsMaxGrade(1)
                )
        );
        //кнопка вызова диалога по изменению
        findViewById(R.id.activity_settings_edit_max_answers_count_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                // аргументы
                Bundle args = new Bundle();
                args.putInt(EditMaxAnswersCountDialogFragment.ARGUMENT_LAST_MAX, db.getSettingsMaxGrade(1));
                // показываем диалог
                EditMaxAnswersCountDialogFragment editMaxAnswersCountDialogFragment = new EditMaxAnswersCountDialogFragment();
                editMaxAnswersCountDialogFragment.setArguments(args);
                editMaxAnswersCountDialogFragment.show(getFragmentManager(), "EditMaxAnswersDialogInterface");
                db.close();
            }
        });


// -------------- кнопка для изменения типов оценок -----------

        RelativeLayout editGradesTypesButton = findViewById(R.id.activity_settings_edit_grades_type_button);
        editGradesTypesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                Cursor types = db.getGradesTypes();

                // массивы из базы данных
                long[] typesId = new long[types.getCount()];
                String[] typesStrings = new String[types.getCount()];
                for (int i = 0; i < types.getCount(); i++) {
                    types.moveToNext();

                    typesId[i] = types.getLong(types.getColumnIndex(SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID));
                    typesStrings[i] = types.getString(types.getColumnIndex(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE));
                }
                // запуск диалога
                EditGradesTypesDialogFragment typesDialogFragment = new EditGradesTypesDialogFragment();
                // данные
                Bundle args = new Bundle();
                args.putLongArray(EditGradesTypesDialogFragment.ARGS_TYPES_ID_ARRAY_TAG, typesId);
                args.putStringArray(EditGradesTypesDialogFragment.ARGS_TYPES_NAMES_ARRAY_TAG, typesStrings);
                typesDialogFragment.setArguments(args);
                // запуск
                typesDialogFragment.show(getFragmentManager(), "editGradesTypesDialogFragment");
            }
        });


//--------------изменить время-----------

        //кнопка  изменения
        RelativeLayout editTimeButton = findViewById(R.id.activity_settings_edit_time_button);
        //слушатель
        editTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                int[][] arrays = db.getSettingsTime(1);
                db.close();
                Log.e("TeachersApp", "editTimeButton*********" + Arrays.toString(arrays));

                int[] arr0 = arrays[0];// TODO error ArrayIndexOutOfBoundsException
                int[] arr1 = arrays[1];
                int[] arr2 = arrays[2];
                int[] arr3 = arrays[3];
                int[] arr4 = arrays[4];
                int[] arr5 = arrays[5];
                int[] arr6 = arrays[6];
                int[] arr7 = arrays[7];
                int[] arr8 = arrays[8];

                //диалог
                EditTimeDialogFragment editTimeDialogFragment = new EditTimeDialogFragment();
                //данные
                Bundle args = new Bundle();
                args.putIntArray("arr0", arr0);
                args.putIntArray("arr1", arr1);
                args.putIntArray("arr2", arr2);
                args.putIntArray("arr3", arr3);
                args.putIntArray("arr4", arr4);
                args.putIntArray("arr5", arr5);
                args.putIntArray("arr6", arr6);
                args.putIntArray("arr7", arr7);
                args.putIntArray("arr8", arr8);
                editTimeDialogFragment.setArguments(args);
                editTimeDialogFragment.show(getFragmentManager(), "editTime");
            }
        });


//--------------удаление данных-----------

        RelativeLayout removeDataButton = findViewById(R.id.activity_settings_remove_data_button);
        removeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //создаем диалог
                SettingsRemoveDataDialogFragment removeDialog =
                        new SettingsRemoveDataDialogFragment();
                // запускаем
                removeDialog.show(getFragmentManager(), "removeSettingsDialog");
            }
        });

//--------------настройка локализации-----------------
        // находим кнопку
        LinearLayout editLocaleButton = findViewById(R.id.activity_settings_edit_locale_button);
        // ставим текст
        {
            // достаем коды языков
            String[] localeСodes = getResources().getStringArray(R.array.locale_code);
            // получаем текущий код локализации из бд
            String lastLocale = db.getSettingsLocale(1);
            // находим прошлую локализацию в списке
            int lastLocaleNumber = 0;
            for (int i = 0; i < localeСodes.length; i++) {
                if (localeСodes[i].equals(lastLocale)) {
                    lastLocaleNumber = i;
                }
            }
            // достаем названия языков
            String[] localeNames = getResources().getStringArray(R.array.locale_names);
            // выводим название
            ((TextView)findViewById(R.id.activity_settings_edit_locale_button_locale_text)).setText(localeNames[lastLocaleNumber]);
        }
        // ставим обработчик
        editLocaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //текущая локаль из бд
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());

                Bundle args = new Bundle();
                args.putString("locale", db.getSettingsLocale(1));
                //создаем диалог
                EditLocaleDialogFragment localeDialog = new EditLocaleDialogFragment();
                localeDialog.setArguments(args);
                // запускаем
                localeDialog.show(getFragmentManager(), "editLocaleDialog");
            }
        });

        // цветные оценки
        final RelativeLayout coloredGradesContainer = findViewById(R.id.activity_settings_are_grades_colored_container);
        final Switch coloredGradesSwitch = findViewById(R.id.activity_settings_are_grades_colored_switch);
        // ставим переключатель в состояние из бд
        coloredGradesSwitch.setChecked(db.getSettingsAreTheGradesColoredByProfileId(1));
        // обработчик контейнеру
        coloredGradesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.setSettingsAreTheGradesColoredByProfileId(1, !coloredGradesSwitch.isChecked());
                coloredGradesSwitch.setChecked(db.getSettingsAreTheGradesColoredByProfileId(1));
//                if(coloredGradesSwitch.isChecked()){
//                    coloredGradesSwitch.color(getResources().getColor(R.color.baseBlue));
//                } else {
//                    coloredGradesSwitch.setHighlightColor(getResources().getColor(R.color.backgroundWhite));
//
//                }
            }
        });
        // переключатель не нажимается
        coloredGradesSwitch.setClickable(false);

//--------------оцените нас-----------------

        RelativeLayout rateUsButton = findViewById(R.id.settings_rate_button);
        rateUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.learning.texnar13.teachersprogect"));
                if (!isActivityStarted(intent)) {
                    intent.setData(Uri
                            .parse("https://play.google.com/store/apps/details?id=com.learning.texnar13.teachersprogect"));
                    if (!isActivityStarted(intent)) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Could not open Android market, please check if the market app installed or not. Try again later",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        db.close();
    }


    //удаление настроек
    @Override
    public void settingsRemove() {
        DataBaseOpenHelper dbOpenHelper = new DataBaseOpenHelper(getApplicationContext());
        dbOpenHelper.restartTable();

        dbOpenHelper.createClass("1\"A\"");
        long classId = dbOpenHelper.createClass("2\"A\"");

        long lerner1Id = dbOpenHelper.createLearner("Зинченко", "Сократ", classId);
        long lerner2Id = dbOpenHelper.createLearner("Шумякин", "Феофан", classId);
        long lerner3Id = dbOpenHelper.createLearner("Рябец", "Валентин", classId);
        long lerner4Id = dbOpenHelper.createLearner("Гроша", "Любава", classId);
        long lerner5Id = dbOpenHelper.createLearner("Авдонина", "Алиса", classId);


        long cabinetId = dbOpenHelper.createCabinet("406");

        ArrayList<Long> places = new ArrayList<>();
        {
            long desk1Id = dbOpenHelper.createDesk(2, 160, 200, cabinetId);//1
            places.add(dbOpenHelper.createPlace(desk1Id, 1));
            places.add(dbOpenHelper.createPlace(desk1Id, 2));
        }
        {
            long desk2Id = dbOpenHelper.createDesk(2, 40, 200, cabinetId);//2
            places.add(dbOpenHelper.createPlace(desk2Id, 1));
            places.add(dbOpenHelper.createPlace(desk2Id, 2));
        }
        {
            long desk3Id = dbOpenHelper.createDesk(2, 160, 120, cabinetId);//3
            places.add(dbOpenHelper.createPlace(desk3Id, 1));
            places.add(dbOpenHelper.createPlace(desk3Id, 2));
        }
        {
            long desk4Id = dbOpenHelper.createDesk(2, 40, 120, cabinetId);//4
            places.add(dbOpenHelper.createPlace(desk4Id, 1));
            places.add(dbOpenHelper.createPlace(desk4Id, 2));
        }
        {
            long desk5Id = dbOpenHelper.createDesk(2, 160, 40, cabinetId);//5
            places.add(dbOpenHelper.createPlace(desk5Id, 1));
            places.add(dbOpenHelper.createPlace(desk5Id, 2));
        }
        {
            long desk6Id = dbOpenHelper.createDesk(2, 40, 40, cabinetId);//6
            places.add(dbOpenHelper.createPlace(desk6Id, 1));
            places.add(dbOpenHelper.createPlace(desk6Id, 2));
        }
        //   |6|  |5|   |    |  |  |  |
        //   |4|  |3|   |    | 4|  |  |
        //   |2|  |1|   |    |35|  |21|
        //       [-]


        long lessonId = dbOpenHelper.createSubject("физика", classId
                //, cabinetId
        );
        Date startLessonTime = new GregorianCalendar(2017, 10, 17, 8, 30).getTime();//1502343000000 --10 августа
        Date endLessonTime = new GregorianCalendar(2017, 10, 17, 9, 15).getTime();//на 7 месяц  1502345700000
        dbOpenHelper.setLessonTimeAndCabinet(lessonId, cabinetId, startLessonTime, endLessonTime, SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER);

        //создание настроек после удаления таблицы
        //db.createNewSettingsProfileWithId1("default", 50);

        dbOpenHelper.setLearnerOnPlace(//lessonId,
                lerner1Id, places.get(1));
        dbOpenHelper.setLearnerOnPlace(//lessonId,
                lerner2Id, places.get(0));
        dbOpenHelper.setLearnerOnPlace(//lessonId,
                lerner3Id, places.get(2));
        dbOpenHelper.setLearnerOnPlace(//lessonId,
                lerner4Id, places.get(7));
        dbOpenHelper.setLearnerOnPlace(//lessonId,
                lerner5Id, places.get(3));
        dbOpenHelper.close();

        Toast toast = Toast.makeText(this, R.string.settings_activity_toast_data_delete_success, Toast.LENGTH_LONG);
        toast.show();
    }

    //редактирование времени
    @Override
    public void editTime(int[][] time) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        int answer = db.setSettingsTime(1, time);
        db.close();
        if (answer == 1) {
            Toast toast = Toast.makeText(this, R.string.settings_activity_toast_time_successfully_saved, Toast.LENGTH_LONG);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, R.string.settings_activity_toast_time_no_saved, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    //смена локали
    @Override
    public void editLocale(String newLocale) {
        //извлекаем язык
        String lang = newLocale;//здесь просто получение строки из диалога (а в нем из бд) ..default..en..ru..
        //новая локализация в бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.setSettingsLocale(1, lang);


        //Toast toast = Toast.makeText(getApplicationContext(),"0000", Toast.LENGTH_LONG);неработает
        // и перезапуск
        System.exit(0);

    }

    // настройка максимального ответа
    @Override
    public void editMaxAnswer(int max) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        db.setSettingsMaxGrade(1, max);
        db.close();
        // ставим новый максимум
        maxGradeText.setText(
                String.format(
                        getResources().getString(R.string.settings_activity_button_edit_max_answer),
                        "" + max
                )
        );
    }

    // -- типы оценок --

    @Override
    public long createGradesType(String name) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        return db.createGradeType(name);
    }

    @Override
    public boolean editGradesType(long typeId, String newName) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        if (db.editGradesType(typeId, newName) >= 0) {
            return true;
        } else
            return false;
    }

    @Override
    public boolean removeGradesType(long typeId) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        if (db.removeGradesType(typeId) >= 0) {
            return true;
        } else
            return false;
    }




    //для кнопки оцените нас
    private boolean isActivityStarted(Intent aIntent) {
        try {
            startActivity(aIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }



    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//кнопка назад в actionBar
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // выводим рекламу при закрытии активности настроек
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
    }
}
