package com.learning.texnar13.teachersprogect.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity implements EditMaxAnswersDialogInterface, EditTimeDialogFragmentInterface, EditLocaleDialogFragmentInterface, EditGradesTypeDialogFragmentInterface, EditAbsentTypeDialogFragmentInterface, SettingsRemoveInterface {

    TextView maxGradeText;

    boolean isColoredGrades;

    // межстраничный баннер открывающийся при выходе из настроек
    //InterstitialAd settingsBack;
    com.yandex.mobile.ads.InterstitialAd settingsBack;

    // путь для файла с данными
    static final String SAVE_DATA_DIRECTORY = "/teachersAssistant";
    static final String SAVE_DATA_FILE_NAME = "saved_data.xml";


    // создание экрана
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // обновляем значение локали
        MyApplication.updateLangForContext(this);


        // разрешаем только вертикальную ориентацию
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // ================ начинаем загрузку межстраничного баннера конца урока ================
        settingsBack = new com.yandex.mobile.ads.InterstitialAd(this);
        settingsBack.setBlockId(getResources().getString(R.string.banner_id_after_settings));
        // Создание объекта таргетирования рекламы.
        final com.yandex.mobile.ads.AdRequest adRequest =
                new com.yandex.mobile.ads.AdRequest.Builder().build();
        // Загрузка объявления.
        settingsBack.loadAd(adRequest);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        // кнопка назад
        findViewById(R.id.activity_settings_back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выходим из активности
                onBackPressed();
            }
        });

        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);

//-------------максимальный ответ-------------


        // ставим прошлый максимум
        maxGradeText = ((TextView) findViewById(R.id.activity_settings_edit_max_answers_count_text));
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
                typesDialogFragment.show(getSupportFragmentManager(), "editGradesTypesDialogFragment");
            }
        });

// -------------- кнопка для изменения типов пропусков -----------

        RelativeLayout editAbsentTypesButton = findViewById(R.id.activity_settings_edit_absent_type_button);
        editAbsentTypesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                Cursor types = db.getAbsentTypes();

                // массивы из базы данных
                long[] typesId = new long[types.getCount()];
                String[] typesNames = new String[types.getCount()];
                String[] typesLongNames = new String[types.getCount()];
                for (int i = 0; i < types.getCount(); i++) {
                    types.moveToNext();

                    typesId[i] = types.getLong(types.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID));
                    typesNames[i] = types.getString(types.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME));
                    typesLongNames[i] = types.getString(types.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME));
                }
                // запуск диалога
                EditAbsentTypesDialogFragment typesDialogFragment = new EditAbsentTypesDialogFragment();
                // данные
                Bundle args = new Bundle();
                args.putLongArray(EditAbsentTypesDialogFragment.ARGS_TYPES_ID_ARRAY_TAG, typesId);
                args.putStringArray(EditAbsentTypesDialogFragment.ARGS_TYPES_NAMES_ARRAY_TAG, typesNames);
                args.putStringArray(EditAbsentTypesDialogFragment.ARGS_TYPES_LONG_NAMES_ARRAY_TAG, typesLongNames);
                typesDialogFragment.setArguments(args);
                // запуск
                typesDialogFragment.show(getSupportFragmentManager(), "editAbsentTypesDialogFragment");
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
                editTimeDialogFragment.show(getSupportFragmentManager(), "editTime");
            }
        });

// -------------- экспорт и импорт данных --------------

        findViewById(R.id.activity_settings_export_all_data_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // диалог разрешения на запись файлов
                if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED) {// если нет разрешения запрашиваем его

                    Toast.makeText(SettingsActivity.this, "#Чтобы экспортировать данные, нужно разрешение на запись файлов#", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
                } else {// если разрешение есть

                    // проверяем доступность SD
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        Toast.makeText(SettingsActivity.this, "SD-карта не доступна ", Toast.LENGTH_LONG).show();
                        Log.d("test", "SD-карта не доступна: " + Environment.getExternalStorageState());
                        return;
                    }
                    // получаем путь к SD
                    File sdPath = Environment.getExternalStorageDirectory();
                    // добавляем свой каталог к пути
                    sdPath = new File(sdPath.getAbsolutePath() + SAVE_DATA_DIRECTORY);
                    // создаем каталог
                    Log.e("test", Boolean.toString(sdPath.mkdirs()));
                    // формируем объект File, который содержит путь к файлу
                    File sdFile = new File(sdPath, SAVE_DATA_FILE_NAME);
                    try {
                        // открываем поток для записи
                        FileWriter fw = new FileWriter(sdFile);
                        // пишем данные
                        DataBaseOpenHelper db = new DataBaseOpenHelper(SettingsActivity.this);
                        db.writeXMLDataBaseInFile(fw);
                        // закрываем поток
                        fw.close();
                        Toast.makeText(SettingsActivity.this, "Файл записан на SD ", Toast.LENGTH_LONG).show();
                        Log.d("test", "Файл записан на SD: " + sdFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        findViewById(R.id.activity_settings_import_all_data_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                // проверяем доступность SD
//                if (!Environment.getExternalStorageState().equals(
//                        Environment.MEDIA_MOUNTED)) {
//                    Toast.makeText(this, "SD-карта не доступна: ", Toast.LENGTH_LONG).show();
//                    Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
//                    return;
//                }
//                // получаем путь к SD
//                File sdPath = Environment.getExternalStorageDirectory();
//                // добавляем свой каталог к пути
//                sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
//                // формируем объект File, который содержит путь к файлу
//                File sdFile = new File(sdPath, FILENAME_SD);
//                try {
//                    // открываем поток для чтения
//                    BufferedReader br = new BufferedReader(new FileReader(sdFile));
//                    String str;
//                    // читаем содержимое
//                    while ((str = br.readLine()) != null) {
//                        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
//                        Log.d(LOG_TAG, str);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }


//                public void getXMLFromDB(){
//
//                }
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
            ((TextView) findViewById(R.id.activity_settings_edit_locale_button_locale_text)).setText(localeNames[lastLocaleNumber]);
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
                localeDialog.show(getSupportFragmentManager(), "editLocaleDialog");
            }
        });

        // цветные оценки
        final RelativeLayout coloredGradesContainer = findViewById(R.id.activity_settings_are_grades_colored_container);
        final ImageView coloredGradesSwitch = findViewById(R.id.activity_settings_are_grades_colored_switch);
        // ставим переключатель в состояние из бд
        isColoredGrades = db.getSettingsAreTheGradesColoredByProfileId(1);
        if (isColoredGrades) {
            coloredGradesSwitch.setImageResource(R.drawable.switch_4);
        } else {
            coloredGradesSwitch.setImageResource(R.drawable.switch_0);
        }

        // обработчик контейнеру
        coloredGradesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isColoredGrades = !isColoredGrades;
                db.setSettingsAreTheGradesColoredByProfileId(1, isColoredGrades);

                if (isColoredGrades) {
                    coloredGradesSwitch.setImageResource(R.drawable.switch_4);
                } else {
                    coloredGradesSwitch.setImageResource(R.drawable.switch_0);
                }
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


    // =============================================================================================
    //                                      Обратная связь от диалогов
    // =============================================================================================


    // обратная связь от диалога разрешений на запись файлов
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1234:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    //readContacts();
                } else {
                    // permission denied
                }
        }
    }

    //удаление настроек
    @Override
    public void settingsRemove() {
        DataBaseOpenHelper dbOpenHelper = new DataBaseOpenHelper(getApplicationContext());
        dbOpenHelper.restartTable();

//        dbOpenHelper.createClass("1\"A\"");
//        long classId = dbOpenHelper.createClass("2\"A\"");
//
//        long lerner1Id = dbOpenHelper.createLearner("Зинченко", "Сократ", classId);
//        long lerner2Id = dbOpenHelper.createLearner("Шумякин", "Феофан", classId);
//        long lerner3Id = dbOpenHelper.createLearner("Рябец", "Валентин", classId);
//        long lerner4Id = dbOpenHelper.createLearner("Гроша", "Любава", classId);
//        long lerner5Id = dbOpenHelper.createLearner("Авдонина", "Алиса", classId);
//
//
//        long cabinetId = dbOpenHelper.createCabinet("406");
//
//        ArrayList<Long> places = new ArrayList<>();
//        {
//            long desk1Id = dbOpenHelper.createDesk(2, 160, 200, cabinetId);//1
//            places.add(dbOpenHelper.createPlace(desk1Id, 1));
//            places.add(dbOpenHelper.createPlace(desk1Id, 2));
//        }
//        {
//            long desk2Id = dbOpenHelper.createDesk(2, 40, 200, cabinetId);//2
//            places.add(dbOpenHelper.createPlace(desk2Id, 1));
//            places.add(dbOpenHelper.createPlace(desk2Id, 2));
//        }
//        {
//            long desk3Id = dbOpenHelper.createDesk(2, 160, 120, cabinetId);//3
//            places.add(dbOpenHelper.createPlace(desk3Id, 1));
//            places.add(dbOpenHelper.createPlace(desk3Id, 2));
//        }
//        {
//            long desk4Id = dbOpenHelper.createDesk(2, 40, 120, cabinetId);//4
//            places.add(dbOpenHelper.createPlace(desk4Id, 1));
//            places.add(dbOpenHelper.createPlace(desk4Id, 2));
//        }
//        {
//            long desk5Id = dbOpenHelper.createDesk(2, 160, 40, cabinetId);//5
//            places.add(dbOpenHelper.createPlace(desk5Id, 1));
//            places.add(dbOpenHelper.createPlace(desk5Id, 2));
//        }
//        {
//            long desk6Id = dbOpenHelper.createDesk(2, 40, 40, cabinetId);//6
//            places.add(dbOpenHelper.createPlace(desk6Id, 1));
//            places.add(dbOpenHelper.createPlace(desk6Id, 2));
//        }
//        //   |6|  |5|   |    |  |  |  |
//        //   |4|  |3|   |    | 4|  |  |
//        //   |2|  |1|   |    |35|  |21|
//        //       [-]
//
//
//        long lessonId = dbOpenHelper.createSubject("физика", classId
//                //, cabinetId
//        );
//        dbOpenHelper.setLessonTimeAndCabinet(lessonId, cabinetId, "2017-10-17", 1, SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER);
//
//        //создание настроек после удаления таблицы
//        //db.createNewSettingsProfileWithId1("default", 50);
//
//        dbOpenHelper.setLearnerOnPlace(//lessonId,
//                lerner1Id, places.get(1));
//        dbOpenHelper.setLearnerOnPlace(//lessonId,
//                lerner2Id, places.get(0));
//        dbOpenHelper.setLearnerOnPlace(//lessonId,
//                lerner3Id, places.get(2));
//        dbOpenHelper.setLearnerOnPlace(//lessonId,
//                lerner4Id, places.get(7));
//        dbOpenHelper.setLearnerOnPlace(//lessonId,
//                lerner5Id, places.get(3));
//        dbOpenHelper.close();

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
        return db.editGradesType(typeId, newName) >= 0;
    }

    @Override
    public boolean removeGradesType(long typeId) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        return db.removeGradesType(typeId) >= 0;
    }


    // -- типы пропусков --

    @Override
    public long createAbsentType(String name, String longName) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        return db.createAbsentType(name, longName);
    }

    @Override
    public boolean editAbsentType(long typeId, String newName, String newLongName) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        return db.editAbsentType(typeId, newName, newLongName) >= 0;
    }

    @Override
    public boolean removeAbsentType(long typeId) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        return db.removeAbsentType(typeId) >= 0;
    }


    // =================================== вспомогательные методы ==================================


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
        if (settingsBack.isLoaded()) {
            settingsBack.show();
        }
    }
}


//
//    void writeFile() {
//        try {
//            /*openFileOutput – открыть файл на запись
//
//openFileInput – открыть файл на чтение
//
//deleteFile – удалить файл
//
//И есть метод getFilesDir – возвращает объект File, соответствующий каталогу для файлов вашей программы. Используйте его, чтобы работать напрямую, без методов-оболочек.
//*/
//
//            // отрываем поток для записи
//            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
//                    openFileOutput(FILENAME, MODE_PRIVATE)));
//            // пишем данные
//            bw.write("Содержимое файла");
//            // закрываем поток
//            bw.close();
//            Toast.makeText(this, "Файл записан", Toast.LENGTH_LONG).show();
//            Log.d(LOG_TAG, "Файл записан");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    void readFile() {
//        try {
//            // открываем поток для чтения
//            BufferedReader br = new BufferedReader(new InputStreamReader(
//                    openFileInput(FILENAME)));
//            String str;
//            // читаем содержимое
//            while ((str = br.readLine()) != null) {
//                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
//                Log.d(LOG_TAG, str);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//
//
//
//    public void onclick(View v) {
//        switch (v.getId()) {
//            case R.id.btnWrite:
//                writeFile();
//                break;
//            case R.id.btnRead:
//                readFile();
//                break;
//            case R.id.btnWriteSD:
//                writeFileSD();
//                break;
//            case R.id.btnReadSD:
//                readFileSD();
//                break;
//        }
//    }
//
//
//    void writeFile() {
//        try {
//            // отрываем поток для записи
//            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
//                    openFileOutput(FILENAME, MODE_PRIVATE)));
//            // пишем данные
//            bw.write("Содержимое файла");
//            // закрываем поток
//            bw.close();
//            Toast.makeText(this, "Файл записан", Toast.LENGTH_LONG).show();
//            Log.d(LOG_TAG, "Файл записан");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    void readFile() {
//        try {
//            // открываем поток для чтения
//            BufferedReader br = new BufferedReader(new InputStreamReader(
//                    openFileInput(FILENAME)));
//            String str;
//            // читаем содержимое
//            while ((str = br.readLine()) != null) {
//                Toast.makeText(this, str, Toast.LENGTH_LONG).show();
//                Log.d(LOG_TAG, str);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//


//<ScrollView
//        android:layout_weight="1"
//        android:layout_margin="10dp"
//        android:layout_width="match_parent"
//        android:layout_height="match_parent">
//        <RelativeLayout
//            android:id="@+id/activity_main_container"
//            android:layout_width="match_parent"
//            android:layout_height="wrap_content"/>
//    </ScrollView>
//
//    <ScrollView
//        android:layout_weight="1"
//        android:layout_margin="10dp"
//        android:layout_width="match_parent"
//        android:layout_height="match_parent">
//        <com.texnar13.writer_assistant.LongEditText
//            android:id="@+id/activity_main_text_2"
//            android:layout_width="match_parent"
//            android:layout_height="wrap_content"/>
//    </ScrollView>


//        myEdit2 = findViewById(R.id.activity_main_text_2);
//        myEdit2.setText(testText);
//        myEdit2.setEditable(false);
//        isEdit2 = false;
//        myEdit2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!isEdit2){
//                    isEdit2 = true;
//                    myEdit2.setEditable(true);
//                }
//            }
//        });
//
//        scrollContainer = findViewById(R.id.activity_main_container);
//
//        // создаем изначальный textView
//        isEdit = false;
//        setTextEditable(false, null);

//    RelativeLayout scrollContainer;
//    boolean isEdit;
//    boolean isEdit2;
//
//    LongEditText myEdit2;
//
//    @Override
//    public void onBackPressed() {
//        if (isEdit) {
//            isEdit = false;
//            // возвращаем обратно textView
//            setTextEditable(false, null);
//        } else
//            super.onBackPressed();
//    }
//
//
//    PointF startTouch = new PointF();
//
//    void setTextEditable(boolean editable, MotionEvent clickEvent) {
//        scrollContainer.removeAllViews();
//
//        if (editable) {
//            scrollContainer.setBackgroundColor(Color.GRAY);
//
//            EditText edit = new EditText(MainActivity.this);
//            edit.setText(testText);
//            edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
//            edit.setTextColor(Color.BLACK);
//            edit.setPadding(0, 0, 0, 0);
////            if (clickEvent != null) {
////                // программно вызванное прикосновение
////                edit.dispatchTouchEvent(
////                        MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, clickEvent.getX(), clickEvent.getY(), 0.5f, 5, 0, 1, 1, 0, 0)
////                );
////                edit.dispatchTouchEvent(
////                        MotionEvent.obtain(10, 10, MotionEvent.ACTION_UP, clickEvent.getX(), clickEvent.getY(), 0.5f, 5, 0, 1, 1, 0, 0)
////                );
////            }
//            RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.MATCH_PARENT,
//                    RelativeLayout.LayoutParams.MATCH_PARENT
//            );
//            scrollContainer.addView(edit, editParams);
//        } else {
//            scrollContainer.setBackgroundColor(Color.LTGRAY);
//
//            TextView viewText = new TextView(this);
//            viewText.setText(testText);
//            viewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
//            viewText.setTextColor(Color.BLACK);
//            // считываем нажатие на view
//            viewText.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//
//                    Log.e("WriterApp", "onTouch: getX()=" + event.getX() + " getY()=" + event.getY());
//                    // если пользователь отрывает палец от экрана и он не перемещал его далеко, то
//                    if ((event.getAction() == MotionEvent.ACTION_UP ||
//                            event.getAction() == MotionEvent.ACTION_CANCEL) &&
//                            Math.abs(startTouch.x - event.getX()) < 10.0F &&
//                            Math.abs(startTouch.y - event.getY()) < 10.0F &&
//                            !isEdit
//                    ) {
//                        // передаем нажатие текстовому полю, которое создадим
//                        isEdit = true;
//                        setTextEditable(true, event);
//
//                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                        // точка начала касания
//                        startTouch.x = event.getX();
//                        startTouch.y = event.getY();
//                    }
//
//                    return true;
//                }
//            });
//
//            RelativeLayout.LayoutParams viewTextParams = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.MATCH_PARENT,
//                    RelativeLayout.LayoutParams.MATCH_PARENT
//            );
//            scrollContainer.addView(viewText, viewTextParams);
//        }
//    }
//
//
//    String testText =
//            "ttttt0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067" +
//                    "ttttt0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067" +
//                    "ttttt0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067"+
//                    "0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067"+
//                    "0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067"+
//                    "0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067"+
//                    "0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067"+
//                    "0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067"+
//                    "0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252253254255256257258259260261262263264265266267268269270271272273274275276277278279280281282283284285286287288289290291292293294295296297298299300301302303304305306307308309310311312313314315316317318319320321322323324325326327328329330331332333334335336337338339340341342343344345346347348349350351352353354355356357358359360361362363364365366367368369370371372373374375376377378379380381382383384385386387388389390391392393394395396397398399400401402403404405406407408409410411412413414415416417418419420421422423424425426427428429430431432433434435436437438439440441442443444445446447448449450451452453454455456457458459460461462463464465466467468469470471472473474475476477478479480481482483484485486487488489490491492493494495496497498499500501502503504505506507508509510511512513514515516517518519520521522523524525526527528529530531532533534535536537538539540541542543544545546547548549550551552553554555556557558559560561562563564565566567568569570571572573574575576577578579580581582583584585586587588589590591592593594595596597598599600601602603604605606607608609610611612613614615616617618619620621622623624625626627628629630631632633634635636637638639640641642643644645646647648649650651652653654655656657658659660661662663664665666667668669670671672673674675676677678679680681682683684685686687688689690691692693694695696697698699700701702703704705706707708709710711712713714715716717718719720721722723724725726727728729730731732733734735736737738739740741742743744745746747748749750751752753754755756757758759760761762763764765766767768769770771772773774775776777778779780781782783784785786787788789790791792793794795796797798799800801802803804805806807808809810811812813814815816817818819820821822823824825826827828829830831832833834835836837838839840841842843844845846847848849850851852853854855856857858859860861862863864865866867868869870871872873874875876877878879880881882883884885886887888889890891892893894895896897898899900901902903904905906907908909910911912913914915916917918919920921922923924925926927928929930931932933934935936937938939940941942943944945946947948949950951952953954955956957958959960961962963964965966967968969970971972973974975976977978979980981982983984985986987988989990991992993994995996997998999100010011002100310041005100610071008100910101011101210131014101510161017101810191020102110221023102410251026102710281029103010311032103310341035103610371038103910401041104210431044104510461047104810491050105110521053105410551056105710581059106010611062106310641065106610671068106910701071107210731074107510761077107810791080108110821083108410851086108710881089109010911092109310941095109610971098109911001101110211031104110511061107110811091110111111121113111411151116111711181119112011211122112311241125112611271128112911301131113211331134113511361137113811391140114111421143114411451146114711481149115011511152115311541155115611571158115911601161116211631164116511661167116811691170117111721173117411751176117711781179118011811182118311841185118611871188118911901191119211931194119511961197119811991200120112021203120412051206120712081209121012111212121312141215121612171218121912201221122212231224122512261227122812291230123112321233123412351236123712381239124012411242124312441245124612471248124912501251125212531254125512561257125812591260126112621263126412651266126712681269127012711272127312741275127612771278127912801281128212831284128512861287128812891290129112921293129412951296129712981299130013011302130313041305130613071308130913101311131213131314131513161317131813191320132113221323132413251326132713281329133013311332133313341335133613371338133913401341134213431344134513461347134813491350135113521353135413551356135713581359136013611362136313641365136613671368136913701371137213731374137513761377137813791380138113821383138413851386138713881389139013911392139313941395139613971398139914001401140214031404140514061407140814091410141114121413141414151416141714181419142014211422142314241425142614271428142914301431143214331434143514361437143814391440144114421443144414451446144714481449145014511452145314541455145614571458145914601461146214631464146514661467146814691470147114721473147414751476147714781479148014811482148314841485148614871488148914901491149214931494149514961497149814991500150115021503150415051506150715081509151015111512151315141515151615171518151915201521152215231524152515261527152815291530153115321533153415351536153715381539154015411542154315441545154615471548154915501551155215531554155515561557155815591560156115621563156415651566156715681569157015711572157315741575157615771578157915801581158215831584158515861587158815891590159115921593159415951596159715981599160016011602160316041605160616071608160916101611161216131614161516161617161816191620162116221623162416251626162716281629163016311632163316341635163616371638163916401641164216431644164516461647164816491650165116521653165416551656165716581659166016611662166316641665166616671668166916701671167216731674167516761677167816791680168116821683168416851686168716881689169016911692169316941695169616971698169917001701170217031704170517061707170817091710171117121713171417151716171717181719172017211722172317241725172617271728172917301731173217331734173517361737173817391740174117421743174417451746174717481749175017511752175317541755175617571758175917601761176217631764176517661767176817691770177117721773177417751776177717781779178017811782178317841785178617871788178917901791179217931794179517961797179817991800180118021803180418051806180718081809181018111812181318141815181618171818181918201821182218231824182518261827182818291830183118321833183418351836183718381839184018411842184318441845184618471848184918501851185218531854185518561857185818591860186118621863186418651866186718681869187018711872187318741875187618771878187918801881188218831884188518861887188818891890189118921893189418951896189718981899190019011902190319041905190619071908190919101911191219131914191519161917191819191920192119221923192419251926192719281929193019311932193319341935193619371938193919401941194219431944194519461947194819491950195119521953195419551956195719581959196019611962196319641965196619671968196919701971197219731974197519761977197819791980198119821983198419851986198719881989199019911992199319941995199619971998199920002001200220032004200520062007200820092010201120122013201420152016201720182019202020212022202320242025202620272028202920302031203220332034203520362037203820392040204120422043204420452046204720482049205020512052205320542055205620572058205920602061206220632064206520662067"+
//                    "0123456789101112131415161718192021222324252627282930313233343536373839404142434445464748495051525354555657585960616263646566676869707172737475767778798081828384858687888990919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173174175176177178179180181182183184185186187188189190191192193194195196197198199200201202203204205206207208209210211212213214215216217218219220221222223224225226227228229230231232233234235236237238239240241242243244245246247248249250251252";
