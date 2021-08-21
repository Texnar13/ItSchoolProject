package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.learning.texnar13.teachersprogect.R;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class LearnersAndGradesImportActivity extends AppCompatActivity {

    private static final String SERIALIZED_IMPORT_DATA = "serializedData";


    // регистрируем callback для диалога выбора файла
    private final ActivityResultLauncher<Integer> selectFileLauncher = registerForActivityResult(
            // контракт для запуска диалога выбора файла с получением обратной связи
            new ActivityResultContract<Integer, Uri>() {

                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Integer input) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    Intent.createChooser(intent, "Select file to upload ");//todo перенести в константы

                    return intent;
                }

                @Override
                public Uri parseResult(int resultCode, @Nullable Intent data) {
                    if (resultCode == RESULT_OK) {
                        if (data == null) return null;
                        if (data.getData() == null) return null;
                        // если какой-то путь есть, пытаемся екго обработать
                        Uri path = data.getData();
                        if (getFileName(data.getData()).trim().endsWith(".xls")) {
                            // отлично, возвращаем результат
                            return path;
                        } else {
                            //todo говорим что файл не того формата
                            Toast.makeText(LearnersAndGradesImportActivity.this,
                                    "низя, низя! файл не того формата", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(this, getResources().getText(R.string.teacher_settings_toast_text_file_format), Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    }
                    return null;
                }
            }, selectedPath -> {
                if (selectedPath != null) {

//                    try {
//                        FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(selectedPath);
//                        HSSFWorkbook book = new HSSFWorkbook(inputStream);
//
//                    } catch (java.lang.Error | IOException e) {
//                        e.printStackTrace();
//                    }
//                    catch (org.apache.poi.util.RecordFormatException | IOException | ExceptionInInitializerError e) {
//                        Log.e("123", "class name = " + e.getClass().getName());
//                        Log.e("123", "Throwable = " + e.getCause());
//                        e.printStackTrace();
//                    }
//                    catch (ExceptionInInitializerError | IOException e) {
//                        Log.e("12345","gedrg45");
//                        e.printStackTrace();
//                    }

                    /*
                    java.lang.ExceptionInInitializerError
        at org.apache.poi.hssf.record.RecordFactory.createRecords(RecordFactory.java:489)
        at org.apache.poi.hssf.usermodel.HSSFWorkbook.<init>(HSSFWorkbook.java:356)
        at org.apache.poi.hssf.usermodel.HSSFWorkbook.<init>(HSSFWorkbook.java:413)
        at org.apache.poi.hssf.usermodel.HSSFWorkbook.<init>(HSSFWorkbook.java:394)
        at com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesImportActivity.lambda$new$0(LearnersAndGradesImportActivity.java:77)
        at com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesImportActivity.$r8$lambda$bdzu2tE8PQBoTJVg5Iu0SRJFGVo(Unknown Source:0)
        at com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesImportActivity$$ExternalSyntheticLambda2.onActivityResult(Unknown Source:4)
        at androidx.activity.result.ActivityResultRegistry$1.onStateChanged(ActivityResultRegistry.java:148)
        at androidx.lifecycle.LifecycleRegistry$ObserverWithState.dispatchEvent(LifecycleRegistry.java:354)
        at androidx.lifecycle.LifecycleRegistry.forwardPass(LifecycleRegistry.java:265)
        at androidx.lifecycle.LifecycleRegistry.sync(LifecycleRegistry.java:307)
        at androidx.lifecycle.LifecycleRegistry.moveToState(LifecycleRegistry.java:148)
        at androidx.lifecycle.LifecycleRegistry.handleLifecycleEvent(LifecycleRegistry.java:134)
        at androidx.lifecycle.ReportFragment.dispatch(ReportFragment.java:68)
        at androidx.lifecycle.ReportFragment$LifecycleCallbacks.onActivityPostStarted(ReportFragment.java:187)
        at android.app.Activity.dispatchActivityPostStarted(Activity.java:1396)
        at android.app.Activity.performStart(Activity.java:8259)
        at android.app.ActivityThread.handleStartActivity(ActivityThread.java:3818)
        at android.app.servertransaction.TransactionExecutor.performLifecycleSequence(TransactionExecutor.java:221)
        at android.app.servertransaction.TransactionExecutor.cycleToPath(TransactionExecutor.java:201)
        at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:173)
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:97)
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2307)
        at android.os.Handler.dispatchMessage(Handler.java:106)
        at android.os.Looper.loop(Looper.java:246)
        at android.app.ActivityThread.main(ActivityThread.java:8506)
        at java.lang.reflect.Method.invoke(Native Method)
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:602)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1130)
     Caused by: org.apache.poi.util.RecordFormatException: Unable to determine record types
        at org.apache.poi.hssf.record.RecordFactory.recordsToMap(RecordFactory.java:446)
        at org.apache.poi.hssf.record.RecordFactory.<clinit>(RecordFactory.java:292)
        at org.apache.poi.hssf.record.RecordFactory.createRecords(RecordFactory.java:489) 
        at org.apache.poi.hssf.usermodel.HSSFWorkbook.<init>(HSSFWorkbook.java:356) 
        at org.apache.poi.hssf.usermodel.HSSFWorkbook.<init>(HSSFWorkbook.java:413) 
        at org.apache.poi.hssf.usermodel.HSSFWorkbook.<init>(HSSFWorkbook.java:394) 
        at com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesImportActivity.lambda$new$0(LearnersAndGradesImportActivity.java:77) 
        at com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesImportActivity.$r8$lambda$bdzu2tE8PQBoTJVg5Iu0SRJFGVo(Unknown Source:0) 
        at com.learning.texnar13.teachersprogect.learnersAndGradesOut.LearnersAndGradesImportActivity$$ExternalSyntheticLambda2.onActivityResult(Unknown Source:4) 
        at androidx.activity.result.ActivityResultRegistry$1.onStateChanged(ActivityResultRegistry.java:148) 
        at androidx.lifecycle.LifecycleRegistry$ObserverWithState.dispatchEvent(LifecycleRegistry.java:354) 
        at androidx.lifecycle.LifecycleRegistry.forwardPass(LifecycleRegistry.java:265) 
        at androidx.lifecycle.LifecycleRegistry.sync(LifecycleRegistry.java:307) 
        at androidx.lifecycle.LifecycleRegistry.moveToState(LifecycleRegistry.java:148) 
        at androidx.lifecycle.LifecycleRegistry.handleLifecycleEvent(LifecycleRegistry.java:134) 
        at androidx.lifecycle.ReportFragment.dispatch(ReportFragment.java:68) 
        at androidx.lifecycle.ReportFragment$LifecycleCallbacks.onActivityPostStarted(ReportFragment.java:187) 
        at android.app.Activity.dispatchActivityPostStarted(Activity.java:1396) 
        at android.app.Activity.performStart(Activity.java:8259) 
        at android.app.ActivityThread.handleStartActivity(ActivityThread.java:3818) 
        at android.app.servertransaction.TransactionExecutor.performLifecycleSequence(TransactionExecutor.java:221) 
        at android.app.servertransaction.TransactionExecutor.cycleToPath(TransactionExecutor.java:201) 
        at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:173) 
        at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:97) 
        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2307) 
        at android.os.Handler.dispatchMessage(Handler.java:106) 
        at android.os.Looper.loop(Looper.java:246) 
        at android.app.ActivityThread.main(ActivityThread.java:8506) 
        at java.lang.reflect.Method.invoke(Native Method) 
        at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:602) 
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1130) 



                    после NoClassDefFoundError:
                     W/System.err:     at androidx.lifecycle.ReportFragment$LifecycleCallbacks.onActivityPostStarted(ReportFragment.java:187)
2021-08-18 19:11:30.715 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.Activity.dispatchActivityPostStarted(Activity.java:1396)
2021-08-18 19:11:30.715 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.Activity.performStart(Activity.java:8259)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.ActivityThread.handleStartActivity(ActivityThread.java:3818)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.servertransaction.TransactionExecutor.performLifecycleSequence(TransactionExecutor.java:221)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.servertransaction.TransactionExecutor.cycleToPath(TransactionExecutor.java:201)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:173)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:97)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2307)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.os.Handler.dispatchMessage(Handler.java:106)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.os.Looper.loop(Looper.java:246)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at android.app.ActivityThread.main(ActivityThread.java:8506)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at java.lang.reflect.Method.invoke(Native Method)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:602)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1130)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err: Caused by: org.apache.poi.util.RecordFormatException: Unable to determine record types
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at org.apache.poi.hssf.record.RecordFactory.recordsToMap(RecordFactory.java:446)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err:     at org.apache.poi.hssf.record.RecordFactory.<clinit>(RecordFactory.java:292)
2021-08-18 19:11:30.716 8043-8043/com.learning.texnar13.teachersprogect W/System.err: 	... 29 more






                    * */

                    // начинаем чтение из файла
                    FileInputStream inputStream = null;
                    try {
                        inputStream = (FileInputStream) getContentResolver().openInputStream(selectedPath);

                        // пытаемся прочитать данные из файла
                        LearnersAndGradesExportHelper helper = new LearnersAndGradesExportHelper();


                        LearnersAndGradesExportHelper.ParseData data =
                                helper.readLearnersAndGradesFromXLS(inputStream);

                        {// todo запускаем диалог
                            Log.e("Test", "Лог вывода:\n" + data.parseLog.toString());

                            // todo после нажатия кнопки подтвердить отправляем данные как результат и завершаем активность
                            //  (весь код ниже находится в этой активности в слушателе обратной связи диалога)
                            // ставим как результат обработанных учеников и оценки
                            Intent intent = new Intent();
                            intent.putExtra(SERIALIZED_IMPORT_DATA,
                                    new LearnersAndGradesExportHelper.LearnersAndGradesIOData(
                                            data.learnersUnits, data.gradesUnits));
                            setResult(RESULT_OK, intent);
                            // завершаем активность импорта
                            finish();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // todo говорим, что приложение не может прочитать этот файл
                        Toast.makeText(LearnersAndGradesImportActivity.this,
                                "низя, низя! файл не прочесть...", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(this, getResources().getText(R.string.teacher_out_list_element_name_text_error), Toast.LENGTH_SHORT).show();
                    } finally {
                        // закрываем поток чтения
                        try {
                            if (inputStream != null) inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    // получение имени файла из uri
    private String getFileName(Uri uri) {
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        return documentFile.getName();
    }


    // регистрируем callback для диалога разрешений
    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // разрешение выдано, отлично!
                    selectFileLauncher.launch(null);
                } else {
                    // Обьясняем, зачем это нужно
                    Toast.makeText(LearnersAndGradesImportActivity.this,
                            R.string.learners_and_grades_import_give_me_a_reason,
                            Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learners_and_grades_import);

        // кнопка выгрузки файла-шаблона
        findViewById(R.id.activity_learners_and_grades_import_button_get_sample).setOnClickListener(v -> {
            // приводим данные об учениках
            ArrayList<String> names = new ArrayList<>();
            names.add("test 123");
            names.add("test 223");
            names.add("test 323");
            names.add("test 423");
            names.add("test 523");
            // и экспортруем их
            LearnersAndGradesExportHelper.shareLearners(this, names);
        });

        // кнопка импорта учеников и оценок
        findViewById(R.id.activity_learners_and_grades_import_button_launch_import).setOnClickListener(v -> {
            // необходимо получить доступ к памяти
            if (ContextCompat.checkSelfPermission(LearnersAndGradesImportActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // разрешение выдано, отлично!
                selectFileLauncher.launch(null);
            } else {
                // запрашиваем разрешение
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });

        // по умолчанию никого не добавляем
        setResult(RESULT_CANCELED, null);
    }


    // контракт для запуска этой активности с получением обратной связи
    public static class LearnersImportActivityResultContract extends
            ActivityResultContract<Integer, LearnersAndGradesExportHelper.LearnersAndGradesIOData> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Integer input) {
            return new Intent(context, LearnersAndGradesImportActivity.class);
        }

        @Override
        public LearnersAndGradesExportHelper.LearnersAndGradesIOData parseResult(int resultCode, @Nullable Intent intent) {
            // если пользователь импортировал данные через текущую активность
            if (resultCode == RESULT_OK) {
                // возвращаем импортированный результат
                return (LearnersAndGradesExportHelper.LearnersAndGradesIOData)
                        intent.getSerializableExtra(SERIALIZED_IMPORT_DATA);
            } else return null;
        }
    }
}
//
//    private void readDataFromXlsFile(Uri uri) {
//
//        StringBuilder myExcelData = new StringBuilder();
//        FileInputStream fileInputStream = null;
//        students = new LinkedList<>();
//        try {
//
//            // читаем xls из файла
//            fileInputStream = (FileInputStream) getContentResolver().openInputStream(uri);//  new FileInputStream(filePath);
//            HSSFWorkbook book = new HSSFWorkbook(fileInputStream);
//
//            int number = book.getNumberOfSheets();
//            myExcelData.append(number);
//            if (number > 0) {
//                HSSFSheet sheet = book.getSheetAt(0);
//
//                int successCounter = 0;
//                int errorCounter = 0;
//                StringBuilder importLog = new StringBuilder();
//
//                int rowNumber = 1;
//                HSSFRow row = sheet.getRow(rowNumber);
//                while (row != null) {
//
//
//                    ParseResult result = parseRow(row, rowNumber);
//
//                    if (result.getErrorsCount() != 0) {
//                        errorCounter++;
//                        importLog.append("Ошибка в строке: ").append((rowNumber + 1)).append(" [\n").append(result.getErrorCodes()).append("];\n");
//                    } else {
//                        successCounter++;
//                        // прповеряем id на уникальность
//                        Iterator<Student> iterator = students.iterator();
//                        boolean errorFlag = false;
//                        int tempRowCounter = 0;
//                        while (!errorFlag && iterator.hasNext()) {
//                            tempRowCounter++;
//                            Student student = iterator.next();
//                            if (student.moskvenokId == result.moskvenokId) {
//                                errorFlag = true;
//                                importLog.append("Ошибка в строке:").append((rowNumber + 1)).append("[\n")
//                                        .append("\t0006: Такой id москвенка уже был (строка: ").append(tempRowCounter).append(")\n];\n");
//                            }
//                        }
//
//                        if (!errorFlag)
//                            students.add(new Student(
//                                    result.moskvenokId,
//                                    result.name,
//                                    result.form
//                            ));
//                    }
//                    // к следующей
//                    rowNumber++;
//                    row = sheet.getRow(rowNumber);
//                }
//
//                // Обработали все строки
//                importLog.append("----------\nИмпорт завершён \n\tОшибок: ").append(errorCounter)
//                        .append("\n\tПринято записей: ").append(successCounter);
//
//                Log.i("TAG", importLog.toString());
//
//                ImportConfirmDialogFragment dialogFragment = new ImportConfirmDialogFragment();
//                Bundle args = new Bundle();
//                args.putString(ImportConfirmDialogFragment.ARGS_LOG_TEXT, importLog.toString());
//                dialogFragment.setArguments(args);
//                dialogFragment.show(getSupportFragmentManager(), "acceptDialog");
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, getResources().getText(R.string.teacher_out_list_element_name_text_error), Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        } finally {
//            // закрываем поток чтения
//            try {
//                if (fileInputStream != null) fileInputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    private ParseResult parseRow(HSSFRow row, int rowPoz) {
//
//        ParseResult result = new ParseResult();
//
//        {// ячейка id
//            HSSFCell idCell = row.getCell(0);
//            if (idCell == null) {
//                result.addError("\t0001: Неправильный формат ячейки (строка:" + (rowPoz + 1) + ", колонка:1)\n");
//            } else {
//                long id = -1;
//                switch (idCell.getCellType()) {
//
//                    case 0://NUMERIC(0)
//                        id = (long) idCell.getNumericCellValue();
//                        break;
//                    case 1://STRING
//                        String idText = idCell.getStringCellValue().trim();
//                        try {
//                            id = Long.parseLong(idText);
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                            result.addError("неправильный id:\"" + idText + '\"');
//                        }
//                        break;
//                    default:
//                        result.addError("\t0002: Неправильный формат ячейки (строка:" + (rowPoz + 1) + ", колонка:1)\n");
//                }
//
//                if (0 < id && id < 9999999999L) {
//                    result.moskvenokId = id;
//                } else {
//                    result.addError("\t0003: Неправильный формат ячейки (строка:" + (rowPoz + 1) + ", колонка:1)\n");
//                }
//            }
//        }
//
//        {// имя
//            HSSFCell nameCell = row.getCell(1);
//            if (nameCell == null) {
//                result.name = "-";
//            } else {
//                switch (nameCell.getCellType()) {
//                    case 0://NUMERIC(0)
//                        result.name = Double.toString(nameCell.getNumericCellValue());
//                        break;
//                    case 1://STRING
//                        result.name = nameCell.getStringCellValue().trim();
//                        break;
//                    case -1:// _NONE(-1)
//                    case 2:// FORMULA(2)
//                    case 3:// BLANK(3)
//                    case 4:// BOOLEAN(4)
//                    case 5:// ERROR(5)
//                        result.form = "-";// todo
//                        break;
//                    default:
//                        result.addError("\t0004: Неправильный формат ячейки (строка:" + (rowPoz + 1) + ", колонка:2)\n");
//                }
//            }
//        }
//
//        {// класс
//            HSSFCell classCell = row.getCell(2);
//            if (classCell == null) {
//                result.form = "-";
//            } else {
//                Log.e("TAG", "classCell.getCellType()=" + classCell.getCellType());
//                switch (classCell.getCellType()) {
//                    case 0://NUMERIC(0)
//                        result.form = Double.toString(classCell.getNumericCellValue());
//                        break;
//                    case 1://STRING
//                        result.form = classCell.getStringCellValue().trim();
//                        break;
//                    case -1:// _NONE(-1)
//                    case 2:// FORMULA(2)
//                    case 3:// BLANK(3)
//                    case 4:// BOOLEAN(4)
//                    case 5:// ERROR(5)
//                        result.form = "-";// todo
//                        break;
//                    default:
//                        result.addError("\t0005: Неправильный формат ячейки (строка:" + (rowPoz + 1) + ", колонка:3)\n");
//                }
//            }
//        }
//
//        return result;
//    }
//
//
//
//class ParseResult {
//
//    private int error = 0;
//    private StringBuilder errorCodes;
//    long moskvenokId;
//    String name;
//    String form;
//
//    public ParseResult(long moskvenokId, String name, String form) {
//        this.moskvenokId = moskvenokId;
//        this.name = name;
//        this.form = form;
//        errorCodes = new StringBuilder();
//    }
//
//    public ParseResult() {
//        errorCodes = new StringBuilder();
//    }
//
//    public void addError(String errorCode) {
//        error++;
//        this.errorCodes.append(errorCode).append('\n').append(' ');
//    }
//
//    public int getErrorsCount() {
//        return error;
//    }
//
//    public String getErrorCodes() {
//        return errorCodes.toString();
//    }
//}
