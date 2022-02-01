package com.learning.texnar13.teachersprogect.settings.ImportModel;

import android.util.Log;

import java.util.LinkedList;

/**
 * Модель таблицы с проверками колонок и данными
 */
class TableModel {

    // название таблицы
    final String tableName;
    // данные о хранимых типах
    final ImportDataBaseData.ColumnCheck[] tableHead;
    // данные строк
    final LinkedList<Row> rows;

    TableModel(String tableName, ImportDataBaseData.ColumnCheck[] tableHead) {
        this.tableName = tableName;
        this.tableHead = tableHead;
        this.rows = new LinkedList<>();
    }


    // ------------------------------ геттеры и сеттеры для данных ------------------------------

    int getRowsCount() {
        return rows.size();
    }

    // получаем не нулевые данные, проверяем их и помещаем в массив
    void addRow(String[] rowData) throws IllegalArgumentException {

        // неверное количество аргументов
        if (tableHead.length != rowData.length)
            throw new IllegalArgumentException("(" + tableName + ")tag \"element\" has too many params (" +
                    rowData.length + "), instead of(" + tableHead.length + ")");

        // Массив для хранения уже проверенных данных
        Object[] parsedData = new Object[tableHead.length];

        // проверяем поочередно каждый из аргументов и запаковываем их в object
        for (int fieldI = 0; fieldI < tableHead.length; fieldI++) {

            Object checkedType;
            // проверяем полученное поле согласно тому типу в который его запишем
            switch (tableHead[fieldI].getElementType()) {

                case ImportDataBaseData.ColumnCheck.TYPE_REF: {


                    // проверяем сам тип данных
                    long longValue;

                    // в id может быть null
                    if (rowData[fieldI].equals("null")) {
                        longValue = -1;
                    } else {
                        try {
                            // пытаемся прочитать обычное число
                            longValue = Long.parseLong(rowData[fieldI]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("(" + tableName + ")\"" + rowData[fieldI] + "\" <- not a number");
                        }
                    }
                    checkedType = longValue;
                    break;
                }

                case ImportDataBaseData.ColumnCheck.TYPE_LONG:
                    // проверяем сам тип данных
                    long longValue;
                    try {
                        longValue = Long.parseLong(rowData[fieldI]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("(" + tableName + ")\"" + rowData[fieldI] + "\" <- not a number");
                    }
                    checkedType = longValue;
                    break;

                case ImportDataBaseData.ColumnCheck.TYPE_STRING:
                    // поскольку на входе уже строка, проверять тип не надо
                    checkedType = rowData[fieldI];
                    break;

                case ImportDataBaseData.ColumnCheck.TYPE_BOOLEAN:
                    // проверяем сам тип данных
                    boolean booleanValue;
                    try {
                        booleanValue = Boolean.parseBoolean(rowData[fieldI]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("(" + tableName + ")\"" + rowData[fieldI] + "\" <- not true or false");
                    }
                    checkedType = booleanValue;
                    break;

                default:
                    Log.wtf("TableModel.class", "Wrong ElementType!");
                    throw new RuntimeException("WTF Wrong ElementType!");
            }

            // Проверяем валидность поля и диапазоны
            if (tableHead[fieldI].check(checkedType)) {
                // если все правильно, то сохраняем значение в распарсенный массив
                parsedData[fieldI] = checkedType;
            } else {
                // данные не совпадают с логикой бд
                throw new IllegalArgumentException("(" + tableName + ")\"" + rowData[fieldI] + "\" <- wrong parameter");
            }

        }
        // если все верно
        rows.add(new Row(parsedData));
    }


    // обьект одной строки
    static class Row {
        // массив с данными поддерживающий динамическую типизацию
        Object[] data;

        public Row(Object[] data) {
            this.data = data;
        }
    }
}
