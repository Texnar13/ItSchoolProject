package com.learning.texnar13.teachersprogect.settings;

// обьект одного поля в строке
public class ImportFieldData {

    // какие типы данных может хранить этот обьект
    public static final int TYPE_LONG = 0;
    public static final int TYPE_STRING = 1;
    public static final int TYPE_BOOLEAN = 2;
    public static final int TYPE_REF = 3;// ссылка на другую таблицу


    // ----------------------------------- переменные экземпляра -----------------------------------

    // название поля
    private final String dbFieldName;
    // тип элемента
    private final int elementType;

    // различные переменные для хранения данных
    private long fieldLongValue;
    private String fieldStringValue;
    private boolean fieldBooleanValue;
    private RefTableField fieldRefValue;
    // если это проверка ссылочного элемента,
    //  то должно быть название таблицы, на которую элемент ссылается
    private String refTableName;

    // обьект для проверки вводимых данных
    // переменная хранящая в себе условие проверки как лямбду
    private final ConditionCheckable conditionChecker;


    // ---------------------------------------- конструкторы ----------------------------------------


    // при создании обьекта задаем: название поля, тип поля, проверку этого поля на валидность

    // конструктор одного из трех типов
    public ImportFieldData(String dbFieldName,int elementType, ConditionCheckable condition) {
        this.dbFieldName = dbFieldName;

        if (elementType == TYPE_REF)
            throw new RuntimeException("My code error " + this.getClass().getName());
        this.elementType = elementType;
        this.conditionChecker = condition;
    }

    // конструктор ссылочного типа
    public ImportFieldData(String dbFieldName, String refTableName, ConditionCheckable condition) {
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


    // ------------------------ геттеры ---------------------------
    public long getLongValue() {
        if (isNotType(TYPE_LONG)) myError();
        return fieldLongValue;
    }

    public String getStringValue() {
        if (isNotType(TYPE_STRING)) myError();
        return fieldStringValue;
    }

    public boolean getBooleanValue() {
        if (isNotType(TYPE_BOOLEAN)) myError();
        return fieldBooleanValue;
    }

    public RefTableField getRefValue() {
        if (isNotType(TYPE_REF)) myError();
        return fieldRefValue;
    }


    // ------------------------ сеттеры ---------------------------
    public void setLongValue(long longValue) {
        if (isNotType(TYPE_LONG)) myError();
        if (check(longValue)) dataError();

        // проверки пройдены, выставляем
        this.fieldLongValue = longValue;
    }

    public void setStringValue(String stringValue) {
        if (isNotType(TYPE_STRING)) myError();
        if (check(stringValue)) dataError();

        // проверки пройдены, выставляем
        this.fieldStringValue = stringValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        if (isNotType(TYPE_BOOLEAN)) myError();
        if (check(booleanValue)) dataError();

        // проверки пройдены, выставляем
        this.fieldBooleanValue = booleanValue;
    }

    public void setRefId(long refId) {
        if (isNotType(TYPE_REF)) myError();

        // создаем новый обьект ссылки ипроверяем данные
        RefTableField ref = new RefTableField(refTableName, refId);
        if (check(ref)) dataError();

        // проверки пройдены, выставляем
        this.fieldRefValue = ref;
    }

    // метод вызываемый когда уже все данные считаны,
    //  и осталось только расставить ссылки вместо обычных id.
    //  собственно сюда и передаются ссылки.
    public void completeRefValue(Object refRow) {
        this.fieldRefValue.ref = refRow;
    }


    // класс содержащий условие
    public interface ConditionCheckable {
        // наследуемый метод проверки для вводимых значений
        // ошибки эта штука кидает только в случае моего косяка, а в остальных случаях false
        boolean checkCondition(Object checkable);
    }


    // -------------------------------------- вспомогательные --------------------------------------

    // более удобная проверка метода выше
    private boolean isNotType(int type) {
        return elementType != type;
    }

    // сокращенная запись ошибок
    private void myError() {
        throw new RuntimeException("My code error " + this.getClass().getName());
    }


    private void dataError() {
        throw new RuntimeException("entered data error");
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
