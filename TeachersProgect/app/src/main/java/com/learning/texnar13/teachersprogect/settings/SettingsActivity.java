package com.learning.texnar13.teachersprogect.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.acceptDialog.AcceptDialog;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.sponsor.SponsorActivity;

import java.io.Serializable;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, EditMaxAnswersDialogInterface, EditTimeDialogFragmentInterface, EditLocaleDialogFragmentInterface, EditGradesTypeDialogFragmentInterface, EditAbsentTypeDialogFragmentInterface, SettingsRemoveInterface {

    TextView maxGradeText;
    boolean isColoredGrades;
    ImageView coloredGradesSwitch;
    // межстраничный баннер открывающийся при выходе из настроек
    com.yandex.mobile.ads.InterstitialAd settingsBack;


    // -------------------------- помощники запуска с callBack-ами --------------------------


    // помощник запуска активности SponsorActivity с результатом
    private final ActivityResultLauncher<Integer> showSponsorAndGetResultHelper = registerForActivityResult(
            new ActivityResultContract<Integer, Integer>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Integer input) {
                    return new Intent(context, SponsorActivity.class);
                }

                @Override
                public Integer parseResult(int resultCode, @Nullable Intent intent) {
                    return resultCode;
                }
            }, result -> {
                if (result == SponsorActivity.RESULT_DEAL_DONE) {

                    // todo показываем диалог, что все куплено
                    AcceptDialog dialog = new AcceptDialog();
                    Bundle args = new Bundle();
                    args.putString(AcceptDialog.ARG_ACCEPT_MESSAGE, "Всё");
                    args.putString(AcceptDialog.ARG_ACCEPT_BUTTON_TEXT, "Куплено");
                    dialog.setArguments(args);
                    dialog.show(getSupportFragmentManager(), "buyer");
                }
            });

    // регистрируем callback для диалога выбора файла
    private final ActivityResultLauncher<Integer> selectFileLaunchHelper = registerForActivityResult(
            new ActivityResultContract<Integer, Uri>() {// как будем запускать намерение/активность

                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Integer input) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    Intent.createChooser(intent,
                            getResources().getString(R.string.settings_activity_data_import_title));
                    return intent;
                }

                @Override
                public Uri parseResult(int resultCode, @Nullable Intent data) {
                    if (resultCode == RESULT_OK) {
                        if (data == null) return null;
                        // если какой-то путь есть, возвращаем его
                        return data.getData();
                    }
                    return null;
                }
            }, selectedUriPath -> {// что будем получать

                // получили путь на файл, проверяем сам путь
                if (selectedUriPath == null) return;
                if (!getFileName(selectedUriPath).trim().endsWith(".tadb")) {
                    // если файл не того формата
                    Toast.makeText(this, getResources().getText(R.string.settings_activity_data_import_message_incorrect_type),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // если какой-то путь есть, пытаемся его обработать
                // начинаем чтение из файла
                SettingsImportExportHelper.ImportDataBaseData data =
                        SettingsImportExportHelper.importDataBase(SettingsActivity.this, selectedUriPath);

                // запускаем диалог с результатами обработки
                DataImportLogDialog logDialog = new DataImportLogDialog();
                Bundle arguments = new Bundle();
                arguments.putString(DataImportLogDialog.PARAM_LOG_MESSAGE, data.outputLog.toString());
                logDialog.setArguments(arguments);
                logDialog.show(getSupportFragmentManager(), "importLogDialog");
            });

    // получение имени файла из uri
    private String getFileName(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        return (documentFile != null) ? documentFile.getName() : "";
    }

    // регистрируем callback для диалога разрешений
    private final ActivityResultLauncher<String> requestPermissionHelper = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // разрешение выдано, отлично!
                    selectFileLaunchHelper.launch(null);
                } else// Обьясняем, зачем это нужно
                    Toast.makeText(this,
                            R.string.learners_and_grades_import_give_me_a_reason,
                            Toast.LENGTH_SHORT).show();

            });


    // создание экрана
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // обновляем значение локали
        MyApplication.updateLangForContext(this);
        // цвет статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.backgroundWhite));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // разрешаем только вертикальную ориентацию
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        // начинаем загрузку межстраничного баннера конца урока
        settingsBack = new com.yandex.mobile.ads.InterstitialAd(this);
        settingsBack.setBlockId(getResources().getString(R.string.banner_id_after_settings));
        // Создание объекта таргетирования рекламы.
        final com.yandex.mobile.ads.AdRequest adRequest =
                new com.yandex.mobile.ads.AdRequest.Builder().build();
        // Загрузка объявления.
        settingsBack.loadAd(adRequest);

        // раздуваем layout
        setContentView(R.layout.settings_activity);
        // даем обработчикам из активити ссылку на тулбар (для кнопки назад и меню)
        setSupportActionBar((Toolbar) findViewById(R.id.base_blue_toolbar));
        // убираем заголовок, там свой
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setTitle("");
        }

        ((TextView) findViewById(R.id.base_blue_toolbar_title)).setText(R.string.title_activity_settings);

        // слушатели кнопкам
        // настройка локализации
        findViewById(R.id.activity_settings_edit_locale_button).setOnClickListener(this);
        // изменить время
        findViewById(R.id.activity_settings_edit_time_button).setOnClickListener(this);
        // максимальое число ответов
        findViewById(R.id.activity_settings_edit_max_answers_count_button).setOnClickListener(this);
        // изменение типов оценок
        findViewById(R.id.activity_settings_edit_grades_type_button).setOnClickListener(this);
        // изменение типов пропусков
        findViewById(R.id.activity_settings_edit_absent_type_button).setOnClickListener(this);
        // экспорт и импорт данных
        findViewById(R.id.activity_settings_export_all_data_button).setOnClickListener(this);
        findViewById(R.id.activity_settings_import_all_data_button).setOnClickListener(this);
        // цветные оценки
        findViewById(R.id.activity_settings_are_grades_colored_container).setOnClickListener(this);
        // удаление данных
        findViewById(R.id.activity_settings_remove_data_button).setOnClickListener(this);
        // подписка
        findViewById(R.id.settings_activity_button_subscribe_background).setOnClickListener(this);
        // оцените нас
        findViewById(R.id.settings_rate_button).setOnClickListener(this);


        // получаем данные из бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        // -- максимальный ответ
        maxGradeText = findViewById(R.id.activity_settings_edit_max_answers_count_button);
        maxGradeText.setText(String.format(
                getResources().getString(R.string.settings_activity_button_edit_max_answer),
                db.getSettingsMaxGrade(1)
        ));

        // -- настройка локализации
        // достаем коды языков
        String[] localeCodes = getResources().getStringArray(R.array.locale_code);
        // получаем текущий код локализации из бд
        String lastLocale = db.getSettingsLocale(1);
        // находим прошлую локализацию в списке
        int lastLocaleNumber = 0;
        for (int i = 0; i < localeCodes.length; i++) {
            if (localeCodes[i].equals(lastLocale)) {
                lastLocaleNumber = i;
            }
        }
        // достаем названия языков
        String[] localeNames = getResources().getStringArray(R.array.locale_names);
        // выводим название
        ((TextView) findViewById(R.id.activity_settings_edit_locale_button)).setText(getResources().getString(
                R.string.settings_activity_button_edit_locale, localeNames[lastLocaleNumber]));

        // -- цветные оценки
        coloredGradesSwitch = findViewById(R.id.activity_settings_are_grades_colored_switch);
        coloredGradesSwitch.setClickable(false);
        // ставим переключатель и внутреннюю переменную в состояние из бд
        isColoredGrades = db.getSettingsAreTheGradesColoredByProfileId(1);
        coloredGradesSwitch.setImageResource(
                (isColoredGrades) ? (R.drawable.test_switch_4) : (R.drawable.test_switch_0));

        db.close();
    }


    // =================================== обработка всех кнопок ===================================
    @Override
    public void onClick(View v) {
        int vId = v.getId();
        // кнопка оцените нас
        if (vId == R.id.settings_rate_button) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=com.learning.texnar13.teachersprogect"));
            if (isActivityNotStarted(intent)) {
                intent.setData(Uri
                        .parse("https://play.google.com/store/apps/details?id=com.learning.texnar13.teachersprogect"));
                if (isActivityNotStarted(intent)) {
                    Toast.makeText(
                            this,
                            "Could not open Android market, please check if the market app installed or not. Try again later",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        // кнопка максимальной оценки
        else if (vId == R.id.activity_settings_edit_max_answers_count_button) {
            // аргументы
            Bundle args = new Bundle();
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            args.putInt(EditMaxAnswersCountDialogFragment.ARGUMENT_LAST_MAX, db.getSettingsMaxGrade(1));
            db.close();
            // показываем диалог
            EditMaxAnswersCountDialogFragment editMaxAnswersCountDialogFragment = new EditMaxAnswersCountDialogFragment();
            editMaxAnswersCountDialogFragment.setArguments(args);
            editMaxAnswersCountDialogFragment.show(getFragmentManager(), "EditMaxAnswersDialogInterface");

        }
        // удаление данных
        else if (vId == R.id.activity_settings_remove_data_button) {
            //создаем диалог
            SettingsRemoveDataDialogFragment removeDialog = new SettingsRemoveDataDialogFragment();
            // запускаем
            removeDialog.show(getSupportFragmentManager(), "removeSettingsDialog");
        }
        // экспорт данных
        else if (vId == R.id.activity_settings_export_all_data_button) {
            SettingsImportExportHelper.exportDB(this);
        }
        // импорт данных
        else if (vId == R.id.activity_settings_import_all_data_button) {
            // необходимо получить доступ к памяти
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // разрешение выдано, сразу запускаем выбор файла
                selectFileLaunchHelper.launch(null);
            } else {
                // запрашиваем разрешение, а затем, возможно, запускаем выбор файла
                requestPermissionHelper.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        // изменить время
        else if (vId == R.id.activity_settings_edit_time_button) {
            //диалог
            EditTimeDialogFragment editTimeDialogFragment = new EditTimeDialogFragment();
            // получаем данные о времени из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            int[][] arrays = db.getSettingsTime(1);
            db.close();
            if (arrays != null) {
                // пакуем данные в обьект
                EditTimeDialogFragment.EditTimeDialogDataTransfer dataObject =
                        new EditTimeDialogFragment.EditTimeDialogDataTransfer(arrays);
                // и передаем диалогу
                Bundle args = new Bundle();
                args.putSerializable(
                        EditTimeDialogFragment.EditTimeDialogDataTransfer.PARAM_DATA,
                        (Serializable) dataObject
                );
                editTimeDialogFragment.setArguments(args);
                editTimeDialogFragment.show(getSupportFragmentManager(), "editTime");
            }
        }
        // изменение типов пропусков
        else if (vId == R.id.activity_settings_edit_absent_type_button) {
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            Cursor types = db.getAbsentTypes();

            // массивы из базы данных
            long[] typesId = new long[types.getCount()];
            String[] typesNames = new String[types.getCount()];
            String[] typesLongNames = new String[types.getCount()];
            for (int i = 0; i < types.getCount(); i++) {
                types.moveToNext();

                typesId[i] = types.getLong(types.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID));
                typesNames[i] = types.getString(types.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME));
                typesLongNames[i] = types.getString(types.getColumnIndex(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME));
            }
            types.close();
            db.close();
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
        // изменение типов оценок
        else if (vId == R.id.activity_settings_edit_grades_type_button) {
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            Cursor types = db.getGradesTypes();

            // массивы из базы данных
            long[] typesId = new long[types.getCount()];
            String[] typesStrings = new String[types.getCount()];
            for (int i = 0; i < types.getCount(); i++) {
                types.moveToNext();

                typesId[i] = types.getLong(types.getColumnIndex(SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID));
                typesStrings[i] = types.getString(types.getColumnIndex(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE));
            }
            types.close();
            db.close();
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
        // цветные оценки
        else if (vId == R.id.activity_settings_are_grades_colored_container) {
            // инвертируем поля в бд
            isColoredGrades = !isColoredGrades;
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
            db.setSettingsAreTheGradesColoredByProfileId(1, isColoredGrades);
            db.close();
            // инвертируем переключатель
            coloredGradesSwitch.setImageResource(
                    (isColoredGrades) ? (R.drawable.test_switch_4) : (R.drawable.test_switch_0)
            );
        }
        // настройка локализации
        else if (vId == R.id.activity_settings_edit_locale_button) {
            // создаем диалог
            EditLocaleDialogFragment localeDialog = new EditLocaleDialogFragment();
            Bundle args = new Bundle();
            // текущая локаль из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            args.putString("locale", db.getSettingsLocale(1));
            db.close();
            localeDialog.setArguments(args);
            // запускаем
            localeDialog.show(getSupportFragmentManager(), "editLocaleDialog");
        }
        // подписка
        else if (vId == R.id.settings_activity_button_subscribe_background) {
            showSponsorAndGetResultHelper.launch(0);
        }
    }


    // для кнопки оцените нас
    private boolean isActivityNotStarted(Intent aIntent) {
        try {
            startActivity(aIntent);
            return false;
        } catch (ActivityNotFoundException e) {
            return true;
        }
    }


    // =============================================================================================
    //                                      Обратная связь от диалогов
    // =============================================================================================


    // обратная связь от диалога разрешений на запись файлов
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        Toast toast = Toast.makeText(this, R.string.settings_activity_toast_data_delete_success, Toast.LENGTH_LONG);
        toast.show();
    }

    // редактирование времени
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

    // смена локали
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
                        max
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
            // settingsBack.show(); todo wtf? не работает, Only fullscreen activities can request orientation
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
