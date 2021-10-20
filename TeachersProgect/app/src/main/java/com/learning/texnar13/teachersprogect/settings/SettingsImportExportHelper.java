package com.learning.texnar13.teachersprogect.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;

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

    public static ImportDataBaseData importDataBase(Context toContext, Uri selectedUriPath) {

        // обьект с прочитанными данными
        ImportDataBaseData returnData = new ImportDataBaseData();

        // начинаем чтение из файла
        FileInputStream inputStream = null;
        try {
            inputStream = (FileInputStream) toContext.getContentResolver().openInputStream(selectedUriPath);
            // пытаемся прочитать данные из файла
            parseDBFile(inputStream, returnData);

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
    private static void parseDBFile(FileInputStream inputStream, ImportDataBaseData dataOutput) {
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
            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_DOCUMENT: {
                        dataOutput.outputLog.append("In start document").append('\n');
                        break;
                    }
                    case XmlPullParser.START_TAG: {
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
                            //String strquery = xpp.getText();
                            //db.execSQL(strquery);
                        }
                    }
                }
//                if (type == XmlPullParser.START_DOCUMENT) {
//                    dataOutput.outputLog.append("In start document").append('\n');
//                } else if (type == XmlPullParser.START_TAG) {
//                    dataOutput.outputLog.append("In start tag = ").append(xpp.getName()).append('\n');
//                } else if (type == XmlPullParser.END_TAG) {
//                    dataOutput.outputLog.append("In end tag = ").append(xpp.getName()).append('\n');
//
//                } else if (type == XmlPullParser.TEXT) {
//                    dataOutput.outputLog.append("Have text = ").append(xpp.getText()).append('\n');
//                    if (xpp.isWhitespace()) {
//                        dataOutput.outputLog.append("Whitespace").append('\n');
//                    } else {
//                        dataOutput.outputLog.append("strquery").append('\n');
//                        //String strquery = xpp.getText();
//                        //db.execSQL(strquery);
//                    }
//
//                }
                type = xpp.next();
            }

//
//            // назначаем файл в который будем писать
//            serializer.setOutput(bw);
//            serializer.startDocument("UTF-8", true);
//            serializer.startTag("", SchoolContract.DB_NAME);
//            serializer.attribute("", "db_file_version", "1");
//            // пишем содержимое таблиц
//
//            serializer.endTag("", SchoolContract.DB_NAME);
//            serializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // куда попадают данные после протения
    public static class ImportDataBaseData {
        StringBuilder outputLog;

        public ImportDataBaseData() {
            outputLog = new StringBuilder();
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
