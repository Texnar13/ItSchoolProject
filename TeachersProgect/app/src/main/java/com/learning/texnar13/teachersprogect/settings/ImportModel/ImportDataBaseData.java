package com.learning.texnar13.teachersprogect.settings.ImportModel;

public class ImportDataBaseData {


    // лог вывода отчета по парсингу файла
    private StringBuilder outputLog;
    // была ли критическая ошибка
    public boolean criticalErrorFlag = false;

    // локализированный тег ошибки
    private String errorLocaleTag;

    // хранилище данных с текущей структурой таблиц
    //  (недоступно извне, но можно получить ссылки на таблицы содержащиеся в нём)
    private ImportDBModel dataBaseOutput;


    // ------------------------------ конструкторы и инициализаторы ------------------------------

    /**
     * Конструктор который работает в самом начале импорта, самих данных и таблицы может не быть, но файл лога будет обязательно
     */
    ImportDataBaseData(String errorTag) {
        outputLog = new StringBuilder();

        errorLocaleTag = errorTag;

        // модель бд определится при прочтении первого тега бд в ImportHelper
        dataBaseOutput = null;
    }

    /**
     * Когда на вход поступил тэг определяющий версию файла, инициализируется dataBaseOutput
     */
    void initializeModelVersion(String dbVersion, String[] localeCodes) throws IllegalArgumentException {
        try {
            // сначала пытаемся парсить строку
            int version = Integer.parseInt(dbVersion);
            // пока что есть только первая версия файла
            if (version != 1)
                throw new IllegalArgumentException("Incorrect db version \"" + dbVersion + "\"");

            // если все в порядке создаем обьект БД
            dataBaseOutput = new ImportDBModel_v1(localeCodes);

        } catch (NumberFormatException e) {
            // ошибка парсинга строки
            throw new IllegalArgumentException("Incorrect db version \"" + dbVersion + "\"");
        }
    }


    /**
     * а была ли вообще иниализирована бд
     */
    boolean wasModelNotInitialized() {
        return dataBaseOutput == null;
    }


    /**
     * Добавить ошибку
     * */
    void addError(String errorMessage){
        outputLog.append(errorLocaleTag).append(errorMessage);
    }
    void addMessage(String message){
        outputLog.append(message);
    }

    public String getErrorsLog(){
        return outputLog.toString();
    }

    // ------------------------------ классы сущностей ------------------------------

    /**
     * Класс от которого наследуются реализации моделей бд, бредставляет из себя абстрактную модель БД
     */
    static abstract class ImportDBModel {
        // структура содержащая в себе описание таблиц и их данные
        protected TableModel[] tables;
    }


    /**
     * Описание типа колонки таблицы
     */
    static class ColumnCheck {

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


    /**
     * Класс ссылки на другую таблицу
     */
    static class RefTableField {

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


    // ------------------------------ Вспомогательные методы ------------------------------

    /**
     * Метод позволяющий получить ссылку на таблицу из приватного класса по названию
     */
    TableModel getTableModelByName(String tableName) {
        // ищем таблицу с таким именем
        for (TableModel table : dataBaseOutput.tables) {
            // если нашли, возвращаем
            if (table.tableName.equals(tableName))
                return table;
        }
        return null;
    }

}
