package com.learning.texnar13.teachersprogect.settings.ImportModel;

import android.provider.BaseColumns;

import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.settings.ImportFieldData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

public class ImportModel_v1 {


    //public int fileVersion;// todo здесь ли это должно быть?

    // структура содержащая в себе описание таблиц и их данные
    public TableModel[] dataBaseImportData;

    // конструктор с описанием данных
    public ImportModel_v1(String[] localeCodes) {
        this.dataBaseImportData = new TableModel[]{

                // описание таблицы настроек
                new TableModel(
                        SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_LOCALE,
                                        ColumnCheck.TYPE_STRING, checkAble -> {
                                    // локаль приложения
                                    String locale = (String) checkAble;
                                    return (Arrays.asList(localeCodes).contains(locale));
                                }),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER,
                                        ColumnCheck.TYPE_LONG, checkAble -> {
                                    int value = (int) checkAble;
                                    return (0 < value && value <= 100);
                                }),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_TIME,
                                        ColumnCheck.TYPE_STRING, (checkAble) -> {
                                    // пытаемся парсить обьект json
                                    String rowValue = (String) checkAble;
                                    try {
                                        new JSONObject(rowValue);
                                        return true;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        return false;
                                    }
                                }),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED,
                                        ColumnCheck.TYPE_BOOLEAN, (checkAble) -> true)
                        }),

                // описание таблицы статистики
                new TableModel(
                        SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE, ColumnCheck.TYPE_STRING, dateCheck),
                                new ColumnCheck(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE, ColumnCheck.TYPE_STRING, dateCheck)
                        }),

                // описание таблицы кабинетов
                new TableModel(
                        SchoolContract.TableCabinets.NAME_TABLE_CABINETS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER, ColumnCheck.TYPE_LONG, (checkAble) -> {
                                    long multiplier = Long.parseLong((String) checkAble);
                                    return (1 <= multiplier && multiplier <= 100);
                                }),
                                new ColumnCheck(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X, ColumnCheck.TYPE_LONG, emptyCheck),
                                new ColumnCheck(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y, ColumnCheck.TYPE_LONG, emptyCheck),
                                new ColumnCheck(SchoolContract.TableCabinets.COLUMN_NAME, ColumnCheck.TYPE_STRING, emptyCheck)
                        }),

                // описание таблицы парт
                new TableModel(
                        SchoolContract.TableDesks.NAME_TABLE_DESKS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableDesks.COLUMN_X, ColumnCheck.TYPE_LONG, emptyCheck),
                                new ColumnCheck(SchoolContract.TableDesks.COLUMN_Y, ColumnCheck.TYPE_LONG, emptyCheck),
                                new ColumnCheck(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES, ColumnCheck.TYPE_LONG, placesNumberCheck),
                                new ColumnCheck(SchoolContract.TableDesks.KEY_CABINET_ID, ColumnCheck.TYPE_REF, emptyCheck)
                        }),

                // описание таблицы мест
                new TableModel(
                        SchoolContract.TablePlaces.NAME_TABLE_PLACES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TablePlaces.KEY_DESK_ID, ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TablePlaces.COLUMN_ORDINAL, ColumnCheck.TYPE_LONG, placesNumberCheck)
                        }),

                // описание таблицы слассов
                new TableModel(
                        SchoolContract.TableClasses.NAME_TABLE_CLASSES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableClasses.COLUMN_CLASS_NAME, ColumnCheck.TYPE_STRING, emptyCheck)
                        }),

                // описание таблицы учеников
                new TableModel(
                        SchoolContract.TableLearners.NAME_TABLE_LEARNERS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableLearners.COLUMN_FIRST_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearners.COLUMN_SECOND_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearners.COLUMN_COMMENT, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearners.KEY_CLASS_ID, ColumnCheck.TYPE_REF, emptyCheck)
                        }),

                // описание таблицы ученик-место
                new TableModel(
                        SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID, ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID, ColumnCheck.TYPE_REF, emptyCheck)
                        }),

                // описание таблицы оценок
                new TableModel(
                        SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMN_DATE, ColumnCheck.TYPE_STRING, dateCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER, ColumnCheck.TYPE_LONG, numberCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], ColumnCheck.TYPE_LONG, numberCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[1], ColumnCheck.TYPE_LONG, numberCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[2], ColumnCheck.TYPE_LONG, numberCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1], ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2], ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID, ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID, ColumnCheck.TYPE_REF, emptyCheck)
                        }),

                // описание таблицы типов оценок
                new TableModel(
                        SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, ColumnCheck.TYPE_STRING, emptyCheck)
                        }),

                // описание таблицы типов пропусков
                new TableModel(
                        SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME, ColumnCheck.TYPE_STRING, emptyCheck)
                        }),

                // описание таблицы предметов
                new TableModel(
                        SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableSubjects.COLUMN_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableSubjects.KEY_CLASS_ID, ColumnCheck.TYPE_REF, emptyCheck)
                        }),

                // описание таблицы уроков (предмет-время-кабинет)
                new TableModel(
                        SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID, ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID, ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER, ColumnCheck.TYPE_LONG, numberCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE, ColumnCheck.TYPE_STRING, dateCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT, ColumnCheck.TYPE_LONG, (checkAble) -> {
                                    long number = Long.parseLong((String) checkAble);
                                    return (SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER <= number &&
                                            number <= SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY);
                                })
                        }),

                // описание таблицы комментариев к уроку
                new TableModel(
                        SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, idCheck),
                                new ColumnCheck(SchoolContract.TableLessonComment.KEY_LESSON_ID, ColumnCheck.TYPE_REF, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLessonComment.COLUMN_LESSON_DATE, ColumnCheck.TYPE_STRING, dateCheck),
                                new ColumnCheck(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT, ColumnCheck.TYPE_STRING, emptyCheck)
                        })
        };
    }

    // ------------------------------ ВВспомогательные методы ------------------------------

    public TableModel getTableModelByName(String tableName) {
        // ищем таблицу с таким именем
        int tableI = 0;
        while (!dataBaseImportData[tableI].tableName.equals(tableName)) tableI++;

        // если нашли, возвращаем
        if (dataBaseImportData[tableI].tableName.equals(tableName)) {
            return dataBaseImportData[tableI];
        } else return null;
    }


    // ------------------------------ экземпляры стандартных проверок ------------------------------


    // переменная проверки для всех id
    private static final ColumnCheck.ConditionCheckable idCheck = (checkable) -> {
        long id = (long) checkable;
        return id >= 1;
    };

    // проверка для текстовых полей без проверки содержимого
    //todo не забудь про огрраничения в количество символов, хотя наверное не надо
    private static final ColumnCheck.ConditionCheckable emptyCheck = (checkAble) -> true;

    // проверка для дат
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final ColumnCheck.ConditionCheckable dateCheck = (checkAble) -> {
        try {
            dateFormat.format(checkAble);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    };
    // проверка номера урока
    private static final ColumnCheck.ConditionCheckable numberCheck = (checkAble) -> {
        long number = Long.parseLong((String) checkAble);
        return (number >= 1);
    };

    // стандартная проверка для количества мест за партой
    private static final ColumnCheck.ConditionCheckable placesNumberCheck = (checkAble) -> {
        long multiplier = Long.parseLong((String) checkAble);
        return (multiplier >= 1);
    };


    // ----------------------- модель таблицы с проверками колонок и данными -----------------------

    public static final class TableModel {

        // название таблицы
        private final String tableName;
        // данные о хранимых типах
        public final ColumnCheck[] tableHead;
        // данные строк
        private final LinkedList<Row> rows;

        public TableModel(String tableName, ColumnCheck[] tableHead) {
            this.tableName = tableName;
            this.tableHead = tableHead;
            this.rows = new LinkedList<>();
        }


        // ------------------------------ геттеры и сеттеры для данных ------------------------------

        public int getRowsCount (){
            return rows.size();
        }

        // получаем не нулевые данные, проверяем их и помещаем в массив
        public void addRow(Object[] data){

            // todo Это все херня, переделывай (в плане параметров функции)
//            // неверное количество аргументов
//            if(tableHead.length != data.length) throw new RuntimeException("entered data error 0");
//
//            // проверяем поля на валидность
//            for (int cellI = 0; cellI < tableHead.length; cellI++) {
//                if(!tableHead[cellI].conditionChecker.checkCondition(data[cellI])){
//                    throw new RuntimeException("entered data error 1");
//                }
//            }
//
//            // если все верно
//            rows.add(new Row(data)){}

           // throw new RuntimeException("My code error " + this.getClass().getName());

        }


        // обьект одной строки
        static class Row {
            // массив с данными поддерживающий динамическую типизацию
            java.lang.Object[] data;

            public Row(int[] types) {
                this.data = new Object[types.length];
                for (int elementTypeI = 0; elementTypeI < types.length; elementTypeI++)
                    data[elementTypeI] = null;
            }
            public Row(java.lang.Object[] data) {
                this.data = data;
            }
//            public Row(int [] types) {
//                this.data = new Object[types.length];
//
//                for (int elementTypeI = 0; elementTypeI < types.length; elementTypeI++) {
//
//                    switch (types[elementTypeI]){
//                        case  ColumnCheck.TYPE_LONG:
//                            data[elementTypeI] = 0;
//                        case  ColumnCheck.TYPE_STRING:
//                            data[elementTypeI] = "";
//                        case  ColumnCheck.TYPE_BOOLEAN:
//                            data[elementTypeI] = false;
//                        case  ColumnCheck.TYPE_REF:
//                            data[elementTypeI] = null;
//                    }
//                }
//            }
        }
    }


    /*
        // различные переменные для хранения данных
        private long fieldLongValue;
        private String fieldStringValue;
        private boolean fieldBooleanValue;
        private com.learning.texnar13.teachersprogect.settings.ColumnCheck.RefTableField fieldRefValue;
    */


    // описание типа колонки таблицы
    public static class ColumnCheck {

        // какие типы данных может хранить этот обьект
        public static final int TYPE_LONG = 0;
        public static final int TYPE_STRING = 1;
        public static final int TYPE_BOOLEAN = 2;
        public static final int TYPE_REF = 3;// ссылка на другую таблицу


        // ----------------------------------- переменные экземпляра -----------------------------------

        // название поля в бд
        private final String dbFieldName;
        // тип элемента
        private final int elementType;
        // если это проверка ссылочного элемента,
        //  то должно быть название таблицы, на которую элемент ссылается
        private String refTableName;


        // обьект для проверки вводимых данных
        // переменная хранящая в себе условие проверки как лямбду
        private final ConditionCheckable conditionChecker;


        // ---------------------------------------- конструкторы ----------------------------------------


        // при создании обьекта задаем: название поля, тип поля, проверку этого поля на валидность

        // конструктор одного из трех типов
        public ColumnCheck(String dbFieldName, int elementType, ConditionCheckable condition) {
            this.dbFieldName = dbFieldName;

            if (elementType == TYPE_REF)
                throw new RuntimeException("My code error " + this.getClass().getName());
            this.elementType = elementType;
            this.conditionChecker = condition;
        }

        // конструктор ссылочного типа
        public ColumnCheck(String dbFieldName, String refTableName, ConditionCheckable condition) {
            this.dbFieldName = dbFieldName;
            elementType = TYPE_REF;
            this.refTableName = refTableName;
            this.conditionChecker = condition;
        }


        // ------------------------------------------ методы ------------------------------------------


        public String getFieldDBName() {
            return dbFieldName;
        }

        // проверка правильности поля к которому привязан этот экземпляр класса
        public boolean check(Object checkAble) {
            return this.conditionChecker.checkCondition(checkAble);
        }


        // проверка типа поля, к которому привязан этот класс
        public int getElementType() {
            return elementType;
        }

        // класс содержащий условие
        public interface ConditionCheckable {
            // наследуемый метод проверки для вводимых значений
            // ошибки эта штука кидает только в случае моего косяка, а в остальных случаях false
            boolean checkCondition(Object checkable);
        }
    }

    // класс ссылки на другую таблицу
    public static class RefTableField {

        private final String REF_TABLE_NAME;
        private final long rowIdRef; // просто считанный id
        public Object ref; // ссылка на другую запись

        public RefTableField(String REF_TABLE_NAME, long rowIdRef) {
            this.REF_TABLE_NAME = REF_TABLE_NAME;
            this.rowIdRef = rowIdRef;
        }

        public String getREF_TABLE_NAME() {
            return REF_TABLE_NAME;
        }

        public long getRowIdRef() {
            return rowIdRef;
        }
    }

}
