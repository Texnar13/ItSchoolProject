package com.learning.texnar13.teachersprogect.settings;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import androidx.documentfile.provider.DocumentFile;

import com.learning.texnar13.teachersprogect.MyApplication;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;
import com.learning.texnar13.teachersprogect.settings.ImportModel.ImportDataBaseData;
import com.learning.texnar13.teachersprogect.settings.ImportModel.SettingsImportHelper;
import com.yandex.mobile.ads.common.AdError;
import com.yandex.mobile.ads.common.AdRequestConfiguration;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.interstitial.InterstitialAd;
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener;
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader;

import java.io.Serializable;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, EditMaxAnswersDialogInterface, EditTimeDialogFragmentInterface, EditLocaleDialogFragmentInterface, EditDarkModeDialogFragmentInterface, EditGradesTypeDialogFragmentInterface, EditAbsentTypeDialogFragmentInterface, SettingsRemoveInterface {

    private TextView maxGradeText;
    private boolean isColoredGrades;
    private ImageView coloredGradesSwitch;
    private ImageView silentLessonSwitch;
    // межстраничный баннер открывающийся при выходе из настроек
    private InterstitialAd settingsBack = null;


    // -------------------------- помощники запуска с callBack-ами --------------------------

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


        // выводим рекламу яндекса
        // начинаем загрузку межстраничного баннера конца урока
        InterstitialAdLoader mInterstitialAdLoader = new InterstitialAdLoader(this);
        mInterstitialAdLoader.setAdLoadListener(new InterstitialAdLoadListener() {
            @Override
            public void onAdLoaded(@NonNull final InterstitialAd interstitialAd) {
                settingsBack = interstitialAd;
                // The ad was loaded successfully.
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError adRequestError) {
                // Ad failed to load with AdRequestError.
                // Attempting to load a new ad from the onAdFailedToLoad() method is strongly discouraged.
            }
        });
        mInterstitialAdLoader.loadAd(new AdRequestConfiguration.Builder(
                getResources().getString(R.string.banner_id_after_settings)
        ).build());


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
        //findViewById(R.id.activity_settings_button_export_all_data).setOnClickListener(this);
        //findViewById(R.id.activity_settings_button_import_all_data).setOnClickListener(this);
        // беззвучный режим на уроке
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            findViewById(R.id.activity_settings_lesson_silent_mode_container).setOnClickListener(this);
        // цветные оценки
        findViewById(R.id.activity_settings_are_grades_colored_container).setOnClickListener(this);
        // удаление данных
        findViewById(R.id.activity_settings_button_remove_data).setOnClickListener(this);

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
            intent.setData(Uri.parse(getResources().getString(R.string.rate_app_link)));
            if (isActivityNotStarted(intent)) {
                Toast.makeText(
                        this,
                        "Не открывается магазин приложений. Could not open market.",
                        Toast.LENGTH_SHORT).show();
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
        // // экспорт данных
        // else if (vId == R.id.activity_settings_button_export_all_data) {
        //     SettingsExportHelper.exportDB(this);
        //     //Toast.makeText(this, "ddd", Toast.LENGTH_SHORT).show();
        // }
        // // импорт данных
        // else if (vId == R.id.activity_settings_button_import_all_data) {
        //     // необходимо получить доступ к памяти
        //     if (ContextCompat.checkSelfPermission(this,
        //             Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        //         // разрешение выдано, сразу запускаем выбор файла
        //         selectFileLaunchHelper.launch(null);
        //     } else {
        //         // запрашиваем разрешение, а затем, возможно, запускаем выбор файла
        //         requestPermissionHelper.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        //     }
        // }
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
        long result = db.createGradeType(name);
        db.close();
        return result;
    }

    @Override
    public boolean editGradesType(long typeId, String newName) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        boolean result = db.editGradesType(typeId, newName) >= 0;
        db.close();
        return result;
    }

    @Override
    public boolean removeGradesType(long typeId) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        boolean result = db.removeGradesType(typeId) >= 0;
        db.close();
        return result;
    }


    // -- типы пропусков --

    @Override
    public long createAbsentType(String name, String longName) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long result = db.createAbsentType(name, longName);
        db.close();
        return result;
    }

    @Override
    public boolean editAbsentType(long typeId, String newName, String newLongName) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        boolean result = db.editAbsentType(typeId, newName, newLongName) >= 0;
        db.close();
        return result;
    }

    @Override
    public boolean removeAbsentType(long typeId) {
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        boolean result = db.removeAbsentType(typeId) > 0;
        db.close();
        return result;
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
        if (settingsBack != null) {
            settingsBack.setAdEventListener(new InterstitialAdEventListener() {
                @Override
                public void onAdShown() {
                    // Called when ad is shown.
                }

                @Override
                public void onAdFailedToShow(@NonNull final AdError adError) {
                    // Called when an InterstitialAd failed to show.
                }

                @Override
                public void onAdDismissed() {
                    // Called when ad is dismissed.
                    // Clean resources after Ad dismissed
                    if (settingsBack != null) {
                        settingsBack.setAdEventListener(null);
                        settingsBack = null;
                    }

                    // Now you can preload the next interstitial ad.
                    // loadInterstitialAd();
                }

                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                }

                @Override
                public void onAdImpression(@Nullable final ImpressionData impressionData) {
                    // Called when an impression is recorded for an ad.
                }
            });

            settingsBack.show(this);
        }
    }
}