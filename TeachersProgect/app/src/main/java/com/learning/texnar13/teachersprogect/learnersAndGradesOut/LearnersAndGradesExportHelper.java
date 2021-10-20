package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.learning.texnar13.teachersprogect.R;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Locale;

public class LearnersAndGradesExportHelper {


    // имя файла
    static public final String FILE_NAME = "saved_data.xls";

    // Открытый файл
    static Workbook openedBook;

    // создать файл с учениками и "поделиться" им
    static public boolean shareLearners(Context context, List<String> names) {
        // генерируем новый файл EXEL
        openedBook = new HSSFWorkbook();
        Sheet sheet = openedBook.createSheet("Entrance Helper");


        {// шапка таблицы
            Row row = sheet.createRow(0);
            Cell[] cells = new Cell[7];
            for (int i = 0; i < cells.length; i++) cells[i] = row.createCell(i);
            cells[0].setCellValue("ФИО*");
            cells[1].setCellValue("Дата");
            cells[2].setCellValue("Время");
            cells[3].setCellValue("ФИО");
            cells[4].setCellValue("Группа");
            cells[5].setCellValue("Событие");
            cells[6].setCellValue("Корпус посещения");
        }

        Iterator<String> iterator = names.iterator();
        for (int nameI = 0; nameI < names.size(); nameI++) {
            String current = iterator.next();


            // строка
            Row row = sheet.createRow(nameI + 1);
            Cell cell = row.createCell(0);

            cell.setCellType(CellType.STRING);
            cell.setCellValue(current);
        }

//        // тело таблицы
//        int excelRowPoz = 1;
//        Iterator<EnteredUnit> iterator = studentsList.iterator();
//        while (iterator.hasNext()) {
//            // событие прохода
//            EnteredUnit current = iterator.next();
//
//            Date point = new Date(current.unixTimePoint);
//
//
//            // строка
//            Row row = sheet.createRow(excelRowPoz);
//            Cell[] cells = new Cell[7];
//            for (int i = 0; i < cells.length; i++) cells[i] = row.createCell(i);
//
//            cells[0].setCellType(CellType.STRING);
//            cells[0].setCellValue(current.moskvenokId);
//
//            cells[1].setCellValue(dateFormat.format(point));
//            cells[2].setCellValue(timeFormat.format(point));
//            if (current == null) {
//                cells[3].setCellValue("-");
//                cells[4].setCellValue("-");
//            } else {
//                cells[3].setCellValue(current.name);
//                cells[4].setCellValue(current.group);
//                //DataFormat format = openedBook.createDataFormat();
//                //        CellStyle dateStyle = openedBook.createCellStyle();
//                //        dateStyle.setDataFormat(format.getFormat("(ss.MM.hh) dd.mm.yyyy"));
//                //        birthdate.setCellStyle(dateStyle);
//            }
//            cells[5].setCellValue("вход");
//            cells[6].setCellValue("ГБОУ Школа № 1852");// корпус??
//
//
//            excelRowPoz++;
//        }

        // Записываем всё в файл
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            openedBook.write(fileOutputStream);

            Log.e("TEST", "Данные сохранены");
        } catch (IOException e) {
            Log.e("TEST", "Ошибка сохранения", e);
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (openedBook != null)
                openedBook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        openedBook = null;


        // отправляем файл
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sharingIntent.setType("*/*");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "theme");
        //sharingIntent.putExtra(Intent.EXTRA_TEXT, "aaaaaaaaaaaaaaa fuck");

        // находим свой каталог для файла
        File sdPath = new File(context.getFilesDir().getAbsolutePath());
        // формируем объект File, который находится в sdPath
        File sdFile = new File(sdPath, FILE_NAME);

        sharingIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                context,
                context.getResources().getString(R.string.share_internal_directory_authorities),
                sdFile
        ));
        context.startActivity(Intent.createChooser(sharingIntent, "Отправить через: "));


        return true;
    }


    // прочитать данные из файла
    public ParseData readLearnersAndGradesFromXLS(FileInputStream xlsFileInputStream) throws IOException {
        // создаем возвращаемые данные
        ParseData parseResult = new ParseData();
        Log.e("Test", "");

//        // создаем массив учеников
//        LinkedList<LearnersIOUnit> learnersList = new LinkedList<>();
//        // логи парсинга
//        StringBuilder parseErrorLog = new StringBuilder();


        //получаем лист xls из которого будем читать данные
        HSSFSheet sheet;
        {
            HSSFWorkbook book = new HSSFWorkbook(xlsFileInputStream);
            if (book.getNumberOfSheets() <= 0) {
                parseResult.parseLog.append("ошибка, файл пустой");//todo перенести в константы
                return parseResult;
            }
            sheet = book.getSheetAt(0);
        }

        // проверим наличие шапки и поля ФИО
        if (isTableHeadExistAndFIOCorrect(sheet)) {
            checkLearnersGradesAndDates(sheet, parseResult);
        } else {
            parseResult.parseLog.append("ошибка заголовка таблицы");//todo перенести в константы
        }
        return parseResult;
        // todo отсюда -------------
        //int successCounter = 0;
        //int errorCounter = 0;


        //int rowNumber = 1;
        //HSSFRow row = sheet.getRow(rowNumber);
//        while (row != null) {
//
//
//            ParseResult result = parseRow(row, rowNumber);
//            if (result.getErrorsCount() != 0) {
//                errorCounter++;
//                importLog.append("Ошибка в строке: ").append((rowNumber + 1)).append(" [\n").append(result.getErrorCodes()).append("];\n");
//            } else {
//                successCounter++;
//                // прповеряем id на уникальность
//                Iterator<Student> iterator = students.iterator();
//                boolean errorFlag = false;
//                int tempRowCounter = 0;
//                while (!errorFlag && iterator.hasNext()) {
//                    tempRowCounter++;
//                    Student student = iterator.next();
//                    if (student.moskvenokId == result.moskvenokId) {
//                        errorFlag = true;
//                        importLog.append("Ошибка в строке:").append((rowNumber + 1)).append("[\n")
//                                .append("\t0006: Такой id москвенка уже был (строка: ").append(tempRowCounter).append(")\n];\n");
//                    }
//                }
//
//                if (!errorFlag)
//                    students.add(new Student(
//                            result.moskvenokId,
//                            result.name,
//                            result.form
//                    ));
//            }
//            // к следующей
//            rowNumber++;
//            row = sheet.getRow(rowNumber);
//        }
//
//        // Обработали все строки
//        importLog.append("----------\nИмпорт завершён \n\tОшибок: ").append(errorCounter)
//                .append("\n\tПринято записей: ").append(successCounter);
//
//        Log.i("TAG", importLog.toString());
//
//        ImportConfirmDialogFragment dialogFragment = new ImportConfirmDialogFragment();
//        Bundle args = new Bundle();
//        args.putString(ImportConfirmDialogFragment.ARGS_LOG_TEXT, importLog.toString());
//        dialogFragment.setArguments(args);
//        dialogFragment.show(getSupportFragmentManager(), "acceptDialog");


        // возвращаем ответ
    }

    // проверить шапку таблицы
    static private boolean isTableHeadExistAndFIOCorrect(HSSFSheet sheet) {
        // (если какое-то из учловий не сработает сразу возвращаем false)

        // проверяем шапку таблицы
        HSSFRow headRow = sheet.getRow(0);
        if (headRow == null) {
            return false;
        }

        // проверяем первую ячейку
        HSSFCell fioCell = headRow.getCell(0);
        if (fioCell == null) {
            return false;
        }
        if (fioCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {//1
            if (!fioCell.getStringCellValue().trim().equals(
                    "ФИО")) //todo перенести в константы (а также при считывании можно проверять константы на всех языках)
                return false;
        } else return false;
        /*
        *   switch (fioCell.getCellType()) {
            case HSSFCell.CELL_TYPE_STRING://1
                if (fioCell.get) {
                    result = true;
                } else result = false;
                break;
            default:
//                    case HSSFCell.CELL_TYPE_NUMERIC://0
//                    case HSSFCell.CELL_TYPE_FORMULA://2
//                    case HSSFCell.CELL_TYPE_BLANK://3
//                    case HSSFCell.CELL_TYPE_BOOLEAN://4
//                    case HSSFCell.CELL_TYPE_ERROR://5
                return false;
        }
        *
        * */

        return true;
    }

    // проверить тело таблицы и даты в шапке
    static private void checkLearnersGradesAndDates(HSSFSheet table, ParseData output) {

        // получаем не пустую шапку с полем ФИО
        HSSFRow headRow = table.getRow(0);
        int lastCell = headRow.getLastCellNum();

        // получаем лист дат с уже проверенными датами (общ. кол-во ячеек - 1)
        Date[] checkedDates = new Date[lastCell];
        for (int cellI = 0; cellI < checkedDates.length; cellI++) {
            HSSFCell currentCell = headRow.getCell(cellI + 1);
            checkedDates[cellI] = getDataCellValue(currentCell); // если ячейка неправильная вернет null
            Log.e("TEST", "tableDatesCheck [" + cellI + "] date=" + checkedDates[cellI]);
        }


        // считываем имена учеников и оценки
        int lastRowPos = table.getLastRowNum();
        for (int currentLearnersRowI = 1; currentLearnersRowI <= lastRowPos; currentLearnersRowI++) {
            HSSFRow currentLearnerRow = table.getRow(currentLearnersRowI);
            // если эта строка пустая, проверять дальше не будем
            if (currentLearnerRow == null) break;

            // проверяем имя
            HSSFCell nameCell = currentLearnerRow.getCell(0);
            if (nameCell == null) break;
            if (nameCell.getCellType() != HSSFCell.CELL_TYPE_STRING) break;

            String[] learnerNamePeaces = nameCell.getStringCellValue().trim().split("\\s+");
            if (learnerNamePeaces.length == 0) break;
            // создаем ученика
            LearnersIOUnit learner = new LearnersIOUnit(learnerNamePeaces[0],
                    (learnerNamePeaces.length == 1) ? ("") : (learnerNamePeaces[1]), "");
            // сохраняем получившегося ученика
            output.learnersUnits.add(learner);


            // todo ищем оценки ученика в столбцах с корректными датами
            for (int dateCellPozI = 0; dateCellPozI < checkedDates.length; dateCellPozI++) {
                if (checkedDates[dateCellPozI] != null) {
                    HSSFCell gradeCell = currentLearnerRow.getCell(dateCellPozI);
                    if (gradeCell == null) {
                        switch (gradeCell.getCellType()) {
                            case HSSFCell.CELL_TYPE_STRING:
                                gradeCell.getStringCellValue();
                            case HSSFCell.CELL_TYPE_NUMERIC:
                                gradeCell.getNumericCellValue();
                            default:

                                // todo если вводятся два периода с одинаковой датой,
                                //  значит пользователь ввел оценки для двух разных урококов в одном дне,
                                //  вопрос в том, как это хранить, и как хранить сами оценки.

//                                gradesUnits
//
// остановился здесь
                        }
                    }
                }
            }


            // GradesIOUnit;

        }
    }

    // пробуем получить дату из ячейки
    @SuppressLint("ConstantLocale")
    static private final SimpleDateFormat dataCellFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private static Date getDataCellValue(HSSFCell dataCell) {
        if (dataCell == null) return null;
        switch (dataCell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:
                return dataCell.getDateCellValue();
            case HSSFCell.CELL_TYPE_STRING:
                try {
                    return dataCellFormat.parse(dataCell.getStringCellValue());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            default:
                return null;
        }
    }


    // класс-результат импорта xls
    static public class ParseData {
        List<LearnersAndGradesExportHelper.LearnersIOUnit> learnersUnits;
        List<LearnersAndGradesExportHelper.GradesIOUnit> gradesUnits;
        StringBuilder parseLog;

        // класс заполняется в процессе парсинга, по этому необходимо инициализировать значения
        public ParseData() {
            this.learnersUnits = new LinkedList<>();
            this.gradesUnits = new LinkedList<>();
            this.parseLog = new StringBuilder();
        }
    }

    // класс данных для импорта экспорта
    static public class LearnersAndGradesIOData implements Serializable {
        List<LearnersAndGradesExportHelper.LearnersIOUnit> learnersUnits;
        List<LearnersAndGradesExportHelper.GradesIOUnit> gradesUnits;

        public LearnersAndGradesIOData(List<LearnersIOUnit> learnersUnits, List<GradesIOUnit> gradesUnits) {
            this.learnersUnits = learnersUnits;
            this.gradesUnits = gradesUnits;
        }
    }

    // класс ученика для импорта экспорта
    static public class LearnersIOUnit implements Serializable {
        String learnerLastName; // Ivanov
        String learnerName;// Ivan
        String learnerDescription;

        public LearnersIOUnit(String learnerLastName, String learnerName, String learnerDescription) {
            this.learnerLastName = learnerLastName;
            this.learnerName = learnerName;
            this.learnerDescription = learnerDescription;
        }

        @NonNull
        @Override
        public String toString() {
            return "*last_name=" + learnerLastName + " name=" + learnerName + "* ";
        }
    }

    static public class GradesIOUnit implements Serializable {

        // дата выставления оценки


        // массив оценок
        int[] learnerGrades;
        // тип пропуска
        int absTypeId;

        public GradesIOUnit(int firstGrade, int secondGrade, int thirdGrade) {
            this.learnerGrades = new int[3];
            learnerGrades[0] = firstGrade;
            learnerGrades[1] = secondGrade;
            learnerGrades[2] = thirdGrade;
        }

        public GradesIOUnit(int absTypeId) {
            this.absTypeId = absTypeId;
            learnerGrades = null;
        }
    }

}
