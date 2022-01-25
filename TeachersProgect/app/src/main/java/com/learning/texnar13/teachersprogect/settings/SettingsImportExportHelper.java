package com.learning.texnar13.teachersprogect.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.settings.ImportModel.ImportModel_v1;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsImportExportHelper {

    // --------------------------------------- экспорт данных ---------------------------------------

    private static final String INNER_BUFFER_FILE_NAME = "settings export.tadb";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public static void exportDB(Context fromContext) {
        boolean saved = false;

        // Записываем всё в файл
        FileOutputStream fileOutputStream = null;
        try {
            // открываем файл во внутреннем хранилище приложения
            fileOutputStream = fromContext.openFileOutput(INNER_BUFFER_FILE_NAME, Context.MODE_PRIVATE);

            // база данных пишет туда данные
            DataBaseOpenHelper db = new DataBaseOpenHelper(fromContext);
            db.writeXMLDataBaseInFile(new OutputStreamWriter(fileOutputStream));
            db.close();

            Log.e("TEST", "Данные сохранены");
            saved = true;
        } catch (Exception e) {
            Log.e("TEST", "Ошибка сохранения", e);
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // если файла нет, то и отправлять нечего
        if (!saved) return;

        // отправляем файл
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sharingIntent.setType("*/*");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, fromContext.getResources().getString(
                R.string.settings_activity_data_export_theme,
                dateFormat.format(new Date())
        ));
        // sharingIntent.putExtra(Intent.EXTRA_TEXT, "aaaaaaaaaaaaaaa fuck");

        // находим свой (inner/внутренний) каталог для файла
        File innerPath = new File(fromContext.getFilesDir().getAbsolutePath());
        // формируем объект File, который находится в sdPath
        File innerFile = new File(innerPath, INNER_BUFFER_FILE_NAME);

        sharingIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                fromContext,
                // псевдоним пути предоставляемого для экспорта (оригинал прописан в манифесте)
                fromContext.getResources().getString(R.string.share_internal_directory_authorities),
                innerFile
        ));
        fromContext.startActivity(Intent.createChooser(sharingIntent,
                fromContext.getResources().getString(R.string.settings_activity_data_export_title)));
    }


    // --------------------------------------- импорт данных ---------------------------------------

    public static ImportDataBaseData importDataBase(
            Context toContext, Uri selectedUriPath) {

        // обьект с прочитанными данными
        ImportDataBaseData returnData = new ImportDataBaseData();

        // начинаем чтение из файла
        FileInputStream inputStream = null;
        try {
            inputStream = (FileInputStream) toContext.getContentResolver().openInputStream(selectedUriPath);
            // пытаемся прочитать данные из файла
            parseDBFile(inputStream, returnData,
                    toContext.getResources().getStringArray(R.array.locale_code));

        } catch (IOException e) {
            e.printStackTrace();
            // говорим, что приложение не может прочитать этот файл
            returnData.outputLog.append(toContext.getResources().getText(
                    R.string.settings_activity_data_import_message_unrtedable_file));
            returnData.outputLog.append(e.toString());
        } finally {
            // закрываем поток чтения
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // после чтения возвращем готовый обьект
        return returnData;
    }

    // магия чтения из потока файла
    private static void parseDBFile(FileInputStream inputStream,
                                    ImportDataBaseData dataOutput, String[] localecodes) {
        try {
            // создаем обьект парсера и передаем ему поток для чтения
            XmlPullParser xpp;
            {
                XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
                xppf.setNamespaceAware(true);
                xpp = xppf.newPullParser();
                xpp.setInput(inputStream, null);
            }

            int type = xpp.getEventType();
            while (type != XmlPullParser.END_DOCUMENT && !dataOutput.criticalErrorFlag) {
                switch (type) {
                    case XmlPullParser.START_DOCUMENT: {
                        dataOutput.outputLog.append("In start document").append('\n');
                        break;
                    }
                    case XmlPullParser.START_TAG: {

                        parseTag(xpp, dataOutput, localecodes);


                        dataOutput.outputLog.append("In start tag = ").append(xpp.getName()).append('\n');
                        break;
                    }
                    case XmlPullParser.END_TAG: {
                        dataOutput.outputLog.append("In end tag = ").append(xpp.getName()).append('\n');
                        break;
                    }
                    case XmlPullParser.TEXT: {
                        dataOutput.outputLog.append("Have text = ").append(xpp.getText()).append('\n');

                        if (xpp.isWhitespace()) {
                            dataOutput.outputLog.append("Whitespace").append('\n');
                        } else {
                            dataOutput.outputLog.append("strquery").append('\n');
                            // String strquery = xpp.getText();
                            // db.execSQL(strquery);
                        }
                    }
                }
                type = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // текущий тег
    private static String currentTable;
    // версия файла
    private static String currentFileVersion;

    private static void parseTag(XmlPullParser xpp, ImportDataBaseData output,
                                 String[] localeCodes) {

//
//        if (xpp.getName().equals("element")) {
//            // парсим строку таблицы
//            switch (currentTable) {
//
//                // базовый тег
//                case SchoolContract.DB_NAME: {
//                    // проверяем был ли уже этот тег
//                    if (output.dataOutput == null) {
//                        try {
//
//                            // ищем в теге номер версии
//                            int fileVersion = Integer.parseInt(xpp.getAttributeValue("", "db_file_version"));
//                            Log.e("TAG", "db_file_version" + xpp.getAttributeValue("", "db_file_version"));
//                            if (fileVersion != 1)
//                                throw new NumberFormatException("incorrect db_file_version");
//
//                            // открываем как модель первой версии
//                            output.dataOutput = new ImportModel_v1(localeCodes);
//
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                            output.outputLog.append("error:incorrect db_file_version").append('\n');
//                            output.criticalErrorFlag = true;
//                        }
//                    } else {
//                        output.outputLog.append("error:duplicate tag: " + SchoolContract.DB_NAME).append('\n');
//                        output.criticalErrorFlag = true;
//                    }
//                    break;
//                }
//
//                case SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS:
//                    // импортируем таблицу настроек
//                    ImportModel_v1.TableModel table =
//                            output.dataOutput.getTableModelByName(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS);
//                    if (table.rows.size() == 0){
//
//                    }
//
//
//
//                    if (output.dataOutput.settingsData == null) {
//
//                        // создаем новый обьект с данными таблицы
//                        SchoolContractImportModel.SettingsImportData newSettingsProfile =
//                                new SchoolContractImportModel.SettingsImportData();
//
//                        // флаг ошибки
//                        boolean errorFlag = false;
//
//                        // проходимся по всем полям записи и читаем их из файла
//                        for (int i = 0; i < newSettingsProfile.rowData.length && !errorFlag; i++) {
//                            // получаем значение поля по названию
//                            String rowValue = xpp.getAttributeValue("",
//                                    newSettingsProfile.rowData[i].getFieldDBName());
//                            // сразу отбрасываем пустые поля
//                            if (rowValue != null) {
//                                // пытаемся его обработать
//                                try {
//                                    switch (newSettingsProfile.rowData[i].getElementType()) {
//                                        case ImportFieldData.TYPE_LONG:
//                                            // парсим цифру
//                                            long longValue = Long.parseLong(rowValue);
//                                            // пытаемся записать значение (там же и проверяем на валидность)
//                                            newSettingsProfile.rowData[i].setLongValue(longValue);
//                                            break;
//                                        case ImportFieldData.TYPE_STRING:
//                                            // пытаемся записать значение (там же и проверяем на валидность)
//                                            newSettingsProfile.rowData[i].setStringValue(rowValue);
//                                            break;
//                                        case ImportFieldData.TYPE_BOOLEAN:
//                                            // парсим boolean
//                                            boolean booleanValue = Boolean.parseBoolean(rowValue);
//                                            // пытаемся записать значение (там же и проверяем на валидность)
//                                            newSettingsProfile.rowData[i].setBooleanValue(booleanValue);
//                                            break;
//                                        case ImportFieldData.TYPE_REF:
//                                            // парсим цифру
//                                            long refIdValue = Long.parseLong(rowValue);
//                                            // пытаемся записать значение (там же и проверяем на валидность)
//                                            newSettingsProfile.rowData[i].setRefId(refIdValue);
//                                            break;
//                                    }
//                                } catch (Exception e) {
//                                    output.outputLog.append("error: table-\"").append(currentTable)
//                                            .append("\", field-\"").append(newSettingsProfile.rowData[i].getFieldDBName()).append("\"").append('\n');
//                                    e.printStackTrace();
//                                    errorFlag = true;
//                                }
//                            } else {
//                                output.outputLog.append("error e: table-\"").append(currentTable)
//                                        .append("\", field-\"").append(newSettingsProfile.rowData[i].getFieldDBName()).append("\"").append('\n');
//                                errorFlag = true;
//                            }
//                        }
////                        String profileName;
////                        String locale;
////                        int maxAnswer;
////                        String timePeriods;
////                        boolean coloredGrades;
//
////                        // название профиля
////                        profileName = xpp.getAttributeValue("",
////                                SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME);
////                        if (profileName == null) {
////                            output.outputLog.append("error: empty settings profile name").append('\n');
////                            break;
////                        }
////
////                        // локаль приложения
////                        locale = xpp.getAttributeValue("",
////                                SchoolContract.TableSettingsData.COLUMN_LOCALE);
////                        if (locale == null) break;// todo прописать сообщения ошибок
////                        if (!Arrays.asList(localecodes).contains(locale)) break;
////
////                        try {
////                            maxAnswer = Integer.parseInt(xpp.getAttributeValue("",
////                                    SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER));
////                            if (maxAnswer < 1) throw new NumberFormatException();
////                        } catch (NumberFormatException e) {
////                            e.printStackTrace();
////                            break;
////                        }
////                        // время уроков
////                        timePeriods = xpp.getAttributeValue("",
////                                SchoolContract.TableSettingsData.COLUMN_TIME);
////                        try {
////                            new JSONObject(timePeriods);
////                        } catch (Exception e) {
////                            output.outputLog.append("error: settings time periods error").append('\n');
////                            break;
////                        }
////                        // время уроков
////                        try {
////                            coloredGrades = Boolean.parseBoolean(xpp.getAttributeValue("",
////                                    SchoolContract.TableSettingsData.COLUMN_TIME));
////                        } catch (Exception e) {
////                            output.outputLog.append("error: settings colored grades error").append('\n');
////                            break;
////                        }
////
////
////                        // если в записи есть ошибки, просто оставим ее пустой
////                        // сохраняем загруженное в файл
////                        output.dataOutput.settingsData =
////                                new SchoolContractImportModel.SettingsImportData(
////                                        1,
////                                        profileName,
////                                        locale,
////                                        50,// не парюсь по поводу размера интерфейса, он все равно нигде не используется
////                                        maxAnswer,
////                                        timePeriods,
////                                        coloredGrades
////                                );
//                    } else {
//                        // вторая запись настроек, не читаем её
//                        output.outputLog.append("error:duplicate settingsRecord: " +
//                                SchoolContract.DB_NAME).append('\n');
//                    }
//                    break;
//
//                case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
//
//
//                    // создаем новый обьект с данными таблицы
//                    SchoolContractImportModel.CabinetsImportData tempImportProfile =
//                            new SchoolContractImportModel.CabinetsImportData();
//
//                    // проверяем запись на ошибки
//                    boolean errorFlag = false;
//
//                    // проходимся по всем полям записи и читаем их из файла
//                    for (int columnI = 0; columnI < tempImportProfile.rowData.length && !errorFlag; columnI++) {
//
//                        // получаем значение поля по названию
//                        String rowValue = xpp.getAttributeValue("",
//                                tempImportProfile.rowData[columnI].getFieldDBName());
//
//                        // сразу отбрасываем пустые поля
//                        if (rowValue != null) {
//
//                            // пытаемся обработать полученное значение
//                            try {
//                                switch (tempImportProfile.rowData[columnI].getElementType()) {
//                                    case ImportFieldData.TYPE_LONG:
//                                        // парсим цифру
//                                        long longValue = Long.parseLong(rowValue);
//                                        // пытаемся записать значение (там же и проверяем на валидность)
//                                        tempImportProfile.rowData[columnI].setLongValue(longValue);
//                                        break;
//                                    case ImportFieldData.TYPE_STRING:
//                                        // пытаемся записать значение (там же и проверяем на валидность)
//                                        tempImportProfile.rowData[columnI].setStringValue(rowValue);
//                                        break;
//                                    case ImportFieldData.TYPE_BOOLEAN:
//                                        // парсим boolean
//                                        boolean booleanValue = Boolean.parseBoolean(rowValue);
//                                        // пытаемся записать значение (там же и проверяем на валидность)
//                                        tempImportProfile.rowData[columnI].setBooleanValue(booleanValue);
//                                        break;
//                                    case ImportFieldData.TYPE_REF:
//                                        // парсим цифру
//                                        long refIdValue = Long.parseLong(rowValue);
//                                        // пытаемся записать значение (там же и проверяем на валидность)
//                                        tempImportProfile.rowData[columnI].setRefId(refIdValue);
//                                        break;
//                                }
//                            } catch (Exception e) {
//                                output.outputLog.append("error: table-\"")
//                                        .append(currentTable)
//                                        .append("\", field-\"")
//                                        .append(tempImportProfile.rowData[columnI].getFieldDBName())
//                                        .append("\"\n");
//                                e.printStackTrace();
//                                errorFlag = true;
//                            }
//
//                        } else {
//                            output.outputLog.append("error e: table-\"")
//                                    .append(currentTable)
//                                    .append("\", field-\"")
//                                    .append(tempImportProfile.rowData[columnI].getFieldDBName())
//                                    .append("\"\n");
//                            errorFlag = true;
//                        }
//                    }
//
//                    // если все поля в порядке, сохраняем запись
//                    if (!errorFlag) output.dataOutput.cabinetsImportData.add(tempImportProfile);
//                    break;
//                }
//                //
//                case SchoolContract.TableDesks.NAME_TABLE_DESKS: {// парсим запись таблицы парт
//
//                    // создаем новый обьект с данными таблицы
//                    SchoolContractImportModel.DesksImportData tempImportProfile =
//                            new SchoolContractImportModel.DesksImportData();
//
//                    // парсим содержимое тега проверяя на ошибки
//                    boolean errorFlag = parseParseTag(xpp, currentTable, output.outputLog, tempImportProfile.rowData);
//
//                    // если все поля в порядке, сохраняем запись
//                    if (!errorFlag) output.dataOutput.desksImportData.add(tempImportProfile);
//                    break;
//                }
//
//                //
//                case SchoolContract.TablePlaces.NAME_TABLE_PLACES: {// парсим запись таблицы мест
//
//                    // создаем новый обьект с данными таблицы
//                    SchoolContractImportModel.PlacesImportData tempImportProfile =
//                            new SchoolContractImportModel.PlacesImportData();
//
//                    // парсим содержимое тега проверяя на ошибки
//                    boolean errorFlag = parseParseTag(xpp, currentTable, output.outputLog, tempImportProfile.rowData);
//
//                    // если все поля в порядке, сохраняем запись
//                    if (!errorFlag) output.dataOutput.placesImportData.add(tempImportProfile);
//                    break;
//                }
//                //
//                case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
//                    break;
//                }
//                //
//                case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
//                    break;
//                }
//                //
//                case SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES: {
//                    break;
//                }
//                //
//                case SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES: {
//
//                    // todo там где пропуск, может храниться null, по этому надо будет добавить дополнительные проверки
//                    //  например, если null то ставить в id -1
//                    break;
//                }
//                //
//                case SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS: {
//                    break;
//                }
//                //
//                case SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE: {
//                    break;
//                }
//                //
//                case SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES: {
//                    break;
//                }
//                //
//                case SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES: {
//                    break;
//                }
//                //
//                case SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES: {
//                    break;
//                }
//                //
//                case SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT: {
//                    break;
//                }
//
//                default:// другие теги не распознаются
//                    output.outputLog.append("error:incorrect tag: ").append(currentTable).append('\n');
//                    output.criticalErrorFlag = true;
//            }
//
//        } else {
//            // меняем считываемую таблицу
//            currentTable = xpp.getName();
//        }

    }

    private static boolean parseParseTag(XmlPullParser xpp, String parseTable, StringBuilder outputLog,
                                         ImportFieldData[] rowData) {
        // проверяем запись на ошибки
        boolean errorFlag = false;

        // проходимся по всем полям записи и читаем их из файла
        for (int columnI = 0; columnI < rowData.length && !errorFlag; columnI++) {

            // получаем значение поля по названию
            String rowValue = xpp.getAttributeValue("",
                    rowData[columnI].getFieldDBName());

            // сразу отбрасываем пустые поля
            if (rowValue != null) {

                // пытаемся обработать полученное значение
                try {
                    switch (rowData[columnI].getElementType()) {
                        case ImportFieldData.TYPE_LONG:
                            // парсим цифру
                            long longValue = Long.parseLong(rowValue);
                            // пытаемся записать значение (там же и проверяем на валидность)
                            rowData[columnI].setLongValue(longValue);
                            break;
                        case ImportFieldData.TYPE_STRING:
                            // пытаемся записать значение (там же и проверяем на валидность)
                            rowData[columnI].setStringValue(rowValue);
                            break;
                        case ImportFieldData.TYPE_BOOLEAN:
                            // парсим boolean
                            boolean booleanValue = Boolean.parseBoolean(rowValue);
                            // пытаемся записать значение (там же и проверяем на валидность)
                            rowData[columnI].setBooleanValue(booleanValue);
                            break;
                        case ImportFieldData.TYPE_REF:
                            // парсим цифру
                            long refIdValue = Long.parseLong(rowValue);
                            // пытаемся записать значение (там же и проверяем на валидность)
                            rowData[columnI].setRefId(refIdValue);
                            break;
                    }
                } catch (Exception e) {
                    outputLog.append("error: table-\"")
                            .append(parseTable)
                            .append("\", field-\"")
                            .append(rowData[columnI].getFieldDBName())
                            .append("\"\n");
                    e.printStackTrace();
                    errorFlag = true;
                }

            } else {
                outputLog.append("error e: table-\"")
                        .append(parseTable)
                        .append("\", field-\"")
                        .append(rowData[columnI].getFieldDBName())
                        .append("\"\n");
                errorFlag = true;
            }
        }
        return errorFlag;
    }


    // пытаемся парсить id записей из файла
    private static long getIdFromXml(XmlPullParser from, String tableName, StringBuilder outputLog) {
        long id;
        String scanned = from.getAttributeValue("", BaseColumns._ID);
        try {
            id = Long.parseLong(scanned);
            if (id < 1) throw new NumberFormatException();
        } catch (Exception e) {
            outputLog.append("error: parse ").append(tableName).append(" id:").append(scanned).append('\n');
            id = -1;
        }
        return id;
    }


    // куда попадают данные после прочтения
    public static class ImportDataBaseData {
        // лог вывода отчета по парсингу файла
        StringBuilder outputLog;
        // была ли критическая ошибка
        boolean criticalErrorFlag = false;


        // класс буфер для преобразования данных в xml
        ImportModel_v1 dataOutput;

        public ImportDataBaseData() {
            outputLog = new StringBuilder();
            dataOutput = null;
        }
    }


}


/*
* как я писал файлы во внешнюю память телефона (внутренний накопитель)
*
*
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
                    db.close();
                    // закрываем поток
                    fw.close();
                    Toast.makeText(SettingsActivity.this, "Файл записан на SD ", Toast.LENGTH_LONG).show();
                    Log.d("test", "Файл записан на SD: " + sdFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
* */
