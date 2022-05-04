package com.learning.texnar13.teachersprogect.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.acceptDialog.AcceptDialog;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;
import com.learning.texnar13.teachersprogect.settings.ImportModel.ImportDataBaseData;
import com.learning.texnar13.teachersprogect.settings.ImportModel.SettingsImportHelper;
import com.learning.texnar13.teachersprogect.sponsor.SponsorActivity;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.interstitial.InterstitialAd;

import java.io.Serializable;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, EditMaxAnswersDialogInterface, EditTimeDialogFragmentInterface, EditLocaleDialogFragmentInterface, EditDarkModeDialogFragmentInterface, EditGradesTypeDialogFragmentInterface, EditAbsentTypeDialogFragmentInterface, SettingsRemoveInterface {

    TextView maxGradeText;
    boolean isColoredGrades;
    ImageView coloredGradesSwitch;
    ImageView silentLessonSwitch;
    // межстраничный баннер открывающийся при выходе из настроек
    InterstitialAd settingsBack;

    // статус подписки обновляемый в onStart
    boolean isSubscribe;


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
                ImportDataBaseData data =
                        SettingsImportHelper.importDataBase(SettingsActivity.this, selectedUriPath);

                // запускаем диалог с результатами обработки
                DataImportLogDialog logDialog = new DataImportLogDialog();
                Bundle arguments = new Bundle();
                arguments.putString(DataImportLogDialog.PARAM_LOG_MESSAGE, data.getErrorsLog());
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
            window.setStatusBarColor(getResources().getColor(R.color.base_background_color, getTheme()));

            // включен ли ночной режим
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            if (Configuration.UI_MODE_NIGHT_YES != currentNightMode)
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // начинаем загрузку межстраничного баннера конца урока
        settingsBack = new InterstitialAd(this);
        settingsBack.setBlockId(getResources().getString(R.string.banner_id_after_settings));
        // Создание объекта таргетирования рекламы и загрузка объявления.
        settingsBack.loadAd(new AdRequest.Builder().build());

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
        findViewById(R.id.activity_settings_button_edit_locale).setOnClickListener(this);
        // настройка темы
        findViewById(R.id.activity_settings_button_edit_dark_mode).setOnClickListener(this);
        // изменить время
        findViewById(R.id.activity_settings_button_edit_time).setOnClickListener(this);
        // максимальое число ответов
        findViewById(R.id.activity_settings_button_edit_max_answers_count).setOnClickListener(this);
        // изменение типов оценок
        findViewById(R.id.activity_settings_button_edit_grades_type).setOnClickListener(this);
        // изменение типов пропусков
        findViewById(R.id.activity_settings_button_edit_absent_type).setOnClickListener(this);
        // экспорт и импорт данных
        findViewById(R.id.activity_settings_button_export_all_data).setOnClickListener(this);
        findViewById(R.id.activity_settings_button_import_all_data).setOnClickListener(this);
        // беззвучный режим на уроке
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            findViewById(R.id.activity_settings_lesson_silent_mode_container).setOnClickListener(this);
        // цветные оценки
        findViewById(R.id.activity_settings_are_grades_colored_container).setOnClickListener(this);
        // удаление данных
        findViewById(R.id.activity_settings_button_remove_data).setOnClickListener(this);
        // подписка
        findViewById(R.id.settings_activity_button_subscribe_background).setOnClickListener(this);
        // оцените нас
        findViewById(R.id.settings_button_rate).setOnClickListener(this);


        // получаем данные из бд
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        // -- максимальный ответ
        maxGradeText = findViewById(R.id.activity_settings_button_edit_max_answers_count);
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
        ((TextView) findViewById(R.id.activity_settings_button_edit_locale)).setText(getResources().getString(
                R.string.settings_activity_button_edit_locale, localeNames[lastLocaleNumber]));


        // вывод текста в кнопку тем
        // достаем названия тем
        String[] themesNames = getResources().getStringArray(R.array.day_night);
        // выводим название
        ((TextView) findViewById(R.id.activity_settings_button_edit_dark_mode)).setText(getResources().getString(
                R.string.settings_activity_button_edit_dark_mode,
                themesNames[PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getInt(SharedPrefsContract.PREFS_DAY_NIGHT_MODE, 0)]
        ));

        // -- цветные оценки
        coloredGradesSwitch = findViewById(R.id.activity_settings_are_grades_colored_switch);
        coloredGradesSwitch.setClickable(false);
        // ставим переключатель и внутреннюю переменную в состояние из бд
        isColoredGrades = db.getSettingsAreTheGradesColoredByProfileId(1);
        coloredGradesSwitch.setImageResource(
                (isColoredGrades) ? (R.drawable.test_switch_4) : (R.drawable.test_switch_0));

        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // проверка статуса подписки
        isSubscribe = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(SharedPrefsContract.PREFS_BOOLEAN_PREMIUM_STATE, false);
        // todo менять кнопку подписки

        // -- настройка тихого урока при переходе на эту активность
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // получаем информацию о разрешениях
            NotificationManager notificationManager =
                    (NotificationManager) SettingsActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
            // ставим переключатель в состояние из менеджера
            silentLessonSwitch = findViewById(R.id.activity_settings_lesson_silent_mode_switch);
            silentLessonSwitch.setClickable(false);
            silentLessonSwitch.setImageResource(
                    (notificationManager.isNotificationPolicyAccessGranted()) ?
                            (R.drawable.test_switch_4) : (R.drawable.test_switch_0));
        }
    }

    // =================================== обработка всех кнопок ===================================
    @Override
    public void onClick(View v) {
        int vId = v.getId();
        // кнопка оцените нас
        if (vId == R.id.settings_button_rate) {
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
        else if (vId == R.id.activity_settings_button_edit_max_answers_count) {
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
        else if (vId == R.id.activity_settings_button_remove_data) {
            //создаем диалог
            SettingsRemoveDataDialogFragment removeDialog = new SettingsRemoveDataDialogFragment();
            // запускаем
            removeDialog.show(getSupportFragmentManager(), "removeSettingsDialog");
        }
        // экспорт данных
        else if (vId == R.id.activity_settings_button_export_all_data) {
            SettingsExportHelper.exportDB(this);
            //Toast.makeText(this, "ddd", Toast.LENGTH_SHORT).show();
        }
        // импорт данных
        else if (vId == R.id.activity_settings_button_import_all_data) {
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
        else if (vId == R.id.activity_settings_button_edit_time) {
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
        else if (vId == R.id.activity_settings_button_edit_absent_type) {
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            Cursor types = db.getAbsentTypes();

            // массивы из базы данных
            long[] typesId = new long[types.getCount()];
            String[] typesNames = new String[types.getCount()];
            String[] typesLongNames = new String[types.getCount()];
            for (int i = 0; i < types.getCount(); i++) {
                types.moveToNext();

                typesId[i] = types.getLong(types.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID));
                typesNames[i] = types.getString(types.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME));
                typesLongNames[i] = types.getString(types.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME));
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
            // ограничеиваем число типов, если подписки нет
            args.putInt(EditAbsentTypesDialogFragment.ARGS_TYPES_MAX_COUNT, -1);

            typesDialogFragment.setArguments(args);
            // запуск
            typesDialogFragment.show(getSupportFragmentManager(), "editAbsentTypesDialogFragment");
        }
        // изменение типов оценок
        else if (vId == R.id.activity_settings_button_edit_grades_type) {
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            Cursor types = db.getGradesTypes();

            // массивы из базы данных
            long[] typesId = new long[types.getCount()];
            String[] typesNames = new String[types.getCount()];
            for (int i = 0; i < types.getCount(); i++) {
                types.moveToNext();

                typesId[i] = types.getLong(types.getColumnIndexOrThrow(SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID));
                typesNames[i] = types.getString(types.getColumnIndexOrThrow(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE));
            }
            types.close();
            db.close();

            // запуск диалога
            EditGradesTypesDialogFragment typesDialogFragment = new EditGradesTypesDialogFragment();
            // данные
            Bundle args = new Bundle();
            args.putLongArray(EditGradesTypesDialogFragment.ARGS_TYPES_ID_ARRAY_TAG, typesId);
            args.putStringArray(EditGradesTypesDialogFragment.ARGS_TYPES_NAMES_ARRAY_TAG, typesNames);
            // ограничеиваем число типов, если подписки нет
            if (!isSubscribe)
                args.putInt(EditGradesTypesDialogFragment.ARGS_TYPES_MAX_COUNT,
                        SharedPrefsContract.PREMIUM_PARAM_GRADES_TYPES_MAXIMUM);
            else
                args.putInt(EditGradesTypesDialogFragment.ARGS_TYPES_MAX_COUNT, -1);

            typesDialogFragment.setArguments(args);
            // запуск
            typesDialogFragment.show(getSupportFragmentManager(), "editGradesTypesDialogFragment");
        }
        // беззвучный режим на уроке
        else if (vId == R.id.activity_settings_lesson_silent_mode_container) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // получаем информацию о разрешениях
                NotificationManager notificationManager =
                        (NotificationManager) SettingsActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
                // показываем диалог, где можно редактировать разрешения
                startActivity(new Intent(
                        android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                // говорим пользователю что ему там делать вообще
                Toast.makeText(getApplicationContext(),
                        R.string.settings_activity_toast_silent_lesson,
                        Toast.LENGTH_LONG).show();
                // https://stackoverflow.com/questions/11699603/is-it-possible-to-turn-off-the-silent-mode-programmatically-in-android
                // https://stackoverflow.com/questions/39151453/in-android-7-api-level-24-my-app-is-not-allowed-to-mute-phone-set-ringer-mode
            }
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
        else if (vId == R.id.activity_settings_button_edit_locale) {
            // создаем диалог
            EditLocaleDialogFragment localeDialog = new EditLocaleDialogFragment();
            Bundle args = new Bundle();
            // текущая локаль из бд
            DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
            args.putString(EditLocaleDialogFragment.ARGS_CURRENT_LOCALE, db.getSettingsLocale(1));
            db.close();
            localeDialog.setArguments(args);
            // запускаем
            localeDialog.show(getSupportFragmentManager(), "editLocaleDialog");
        }
        // настройка темы
        else if (vId == R.id.activity_settings_button_edit_dark_mode) {
            // создаем диалог
            EditDarkModeDialogFragment localeDialog = new EditDarkModeDialogFragment();
            Bundle args = new Bundle();
            // текущая локаль из Shared preferences
            args.putInt(EditDarkModeDialogFragment.ARGS_CURRENT_DAY_NIGHT_MODE,
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getInt(SharedPrefsContract.PREFS_DAY_NIGHT_MODE, 0)
            );
            localeDialog.setArguments(args);
            // запускаем
            localeDialog.show(getSupportFragmentManager(), "editDarkModeDialog");
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
//        if (requestCode == 1234) {
//            if (grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // permission granted
//                //readContacts();
//            } else {
//                // permission denied
//            }
//        }
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
            Toast toast = Toast.makeText(this, R.string.settings_activity_toast_time_successfully_saved, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(this, R.string.settings_activity_toast_time_no_saved, Toast.LENGTH_SHORT);
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

    // смена темной темы
    @SuppressLint("ApplySharedPref")
    @Override
    public void editDarkMode(int newMode) {

        // сохраняем изменения в shared preferences
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                // специально делаю в одном потоке (не apply) чтобы изменения успели примениться до закрытия программы
                .putInt(SharedPrefsContract.PREFS_DAY_NIGHT_MODE, newMode).commit();
        // перезапуск (применение темы в MyApplication)
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
        return db.removeAbsentType(typeId) > 0;
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
            settingsBack.show(); //todo wtf? не работает, Only fullscreen activities can request orientation
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