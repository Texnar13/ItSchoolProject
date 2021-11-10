package com.learning.texnar13.teachersprogect.settings;

import android.provider.BaseColumns;

import com.learning.texnar13.teachersprogect.data.SchoolContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

// класс буфер для преобразования данных в xml
public final class SchoolContractImportModel {


    public int fileVersion;

    SettingsImportData settingsData;

    LinkedList<StatisticsProfilesImportData> statisticsData;
    LinkedList<CabinetsImportData> cabinetsImportData;
    LinkedList<DesksImportData> desksImportData;
    LinkedList<PlacesImportData> placesImportData;
    // todo переделать все так, чтобы данные проверки сохранялись не для ячейки, а для колонки целиком


    // конструктор
    SchoolContractImportModel() {
        super();

        this.fileVersion = -1;

        this.settingsData = null;

        this.statisticsData = new LinkedList<>();
        this.cabinetsImportData = new LinkedList<>();
        this.desksImportData = new LinkedList<>();
        this.placesImportData = new LinkedList<>();

    }


    // ---------------------------------- стандартные проверки --------------------------------------

    // переменная проверки для всех id
    private static final ImportFieldData.ConditionCheckable idCheck = (checkable) -> {
        long id = (long) checkable;
        return id >= 1;
    };

    // проверка для текстовых полей без проверки содержимого
    //todo не забудь про огрраничения в количество символов, хотя наверное не надо
    private static final ImportFieldData.ConditionCheckable emptyCheck = (checkAble) -> true;


    // проверка для дат
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final ImportFieldData.ConditionCheckable dateCheck = (checkAble) -> {
        try {
            dateFormat.format(checkAble);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    };
    // проверка номера урока
    private static final ImportFieldData.ConditionCheckable numberCheck = (checkAble) -> {
        long number = Long.parseLong((String) checkAble);
        return (number >= 1);
    };


    // ------------------------------ классы-интерпретации таблиц ----------------------------------

    public static class SettingsImportData extends SchoolContract.TableSettingsData {

        ImportFieldData[] rowData;

        SettingsImportData(String[] localeCodes) {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, ImportFieldData.TYPE_STRING, emptyCheck),
                    new ImportFieldData(SchoolContract.TableSettingsData.COLUMN_LOCALE,
                            ImportFieldData.TYPE_STRING, checkAble -> {
                        // локаль приложения
                        String locale = (String) checkAble;
                        return (Arrays.asList(localeCodes).contains(locale));
                    }),
                    new ImportFieldData(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, ImportFieldData.TYPE_STRING, emptyCheck),
                    new ImportFieldData(SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER,
                            ImportFieldData.TYPE_LONG, checkAble -> {
                        int value = (int) checkAble;
                        return (0 < value && value <= 100);
                    }),
                    new ImportFieldData(SchoolContract.TableSettingsData.COLUMN_TIME,
                            ImportFieldData.TYPE_STRING, (checkAble) -> {
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
                    new ImportFieldData(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED,
                            ImportFieldData.TYPE_BOOLEAN, (checkAble) -> true)
            };
        }
    }

    public static final class StatisticsProfilesImportData extends SchoolContract.TableStatisticsProfiles {

        ImportFieldData[] rowData;

        public StatisticsProfilesImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_PROFILE_NAME, ImportFieldData.TYPE_STRING, emptyCheck),
                    new ImportFieldData(COLUMN_START_DATE, ImportFieldData.TYPE_STRING, dateCheck),
                    new ImportFieldData(COLUMN_END_DATE, ImportFieldData.TYPE_STRING, dateCheck)
            };
        }


        static class Row {
            // массив с данными поддерживающий динамическую типизацию
            java.lang.Object[] data;

            public Row(int[] types) {
                this.data = new Object[types.length];
                for (int elementTypeI = 0; elementTypeI < types.length; elementTypeI++)
                    data[elementTypeI] = null;
            }

//            public Row(int [] types) {
//                this.data = new Object[types.length];
//
//                for (int elementTypeI = 0; elementTypeI < types.length; elementTypeI++) {
//
//                    switch (types[elementTypeI]){
//                        case  ImportFieldData.TYPE_LONG:
//                            data[elementTypeI] = 0;
//                        case  ImportFieldData.TYPE_STRING:
//                            data[elementTypeI] = "";
//                        case  ImportFieldData.TYPE_BOOLEAN:
//                            data[elementTypeI] = false;
//                        case  ImportFieldData.TYPE_REF:
//                            data[elementTypeI] = null;
//                    }
//                }
//            }
        }
    }

    public static final class CabinetsImportData extends SchoolContract.TableCabinets {

        ImportFieldData[] rowData;

        public CabinetsImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_CABINET_MULTIPLIER, ImportFieldData.TYPE_LONG, multiplierCheck),
                    new ImportFieldData(COLUMN_CABINET_OFFSET_X, ImportFieldData.TYPE_LONG, emptyCheck),
                    new ImportFieldData(COLUMN_CABINET_OFFSET_Y, ImportFieldData.TYPE_LONG, emptyCheck),
                    new ImportFieldData(COLUMN_NAME, ImportFieldData.TYPE_STRING, emptyCheck)
            };
        }


        // стандартная проверка для множителей
        private static final ImportFieldData.ConditionCheckable multiplierCheck = (checkAble) -> {
            long multiplier = Long.parseLong((String) checkAble);
            return (1 <= multiplier && multiplier <= 100);
        };
    }

    public static final class DesksImportData extends SchoolContract.TableDesks {

        ImportFieldData[] rowData;

        public DesksImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_X, ImportFieldData.TYPE_LONG, emptyCheck),
                    new ImportFieldData(COLUMN_Y, ImportFieldData.TYPE_LONG, emptyCheck),
                    new ImportFieldData(COLUMN_NUMBER_OF_PLACES, ImportFieldData.TYPE_LONG, placesNumberCheck),
                    new ImportFieldData(KEY_CABINET_ID, ImportFieldData.TYPE_REF, emptyCheck)
            };
        }

        // стандартная проверка для количества мест
        private static final ImportFieldData.ConditionCheckable placesNumberCheck = (checkAble) -> {
            long multiplier = Long.parseLong((String) checkAble);
            return (multiplier >= 1);
        };
    }

    public static final class PlacesImportData extends SchoolContract.TablePlaces {// todo помоему нужно убрать эту таблицу, а все данные переместить в парты

        ImportFieldData[] rowData;

        public PlacesImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(KEY_DESK_ID, ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(COLUMN_ORDINAL, ImportFieldData.TYPE_LONG, placesNumberCheck)
            };
        }

        // стандартная проверка для количества мест за партой
        private static final ImportFieldData.ConditionCheckable placesNumberCheck = (checkAble) -> {
            long multiplier = Long.parseLong((String) checkAble);
            return (multiplier >= 1);
        };
    }

    public static final class ClassesImportData extends SchoolContract.TableClasses {

        ImportFieldData[] rowData;

        public ClassesImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_CLASS_NAME, ImportFieldData.TYPE_STRING, emptyCheck)
            };
        }
    }

    public static final class LearnersImportData extends SchoolContract.TableLearners {

        ImportFieldData[] rowData;

        public LearnersImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_FIRST_NAME, ImportFieldData.TYPE_STRING, emptyCheck),
                    new ImportFieldData(COLUMN_SECOND_NAME, ImportFieldData.TYPE_STRING, emptyCheck),
                    new ImportFieldData(COLUMN_COMMENT, ImportFieldData.TYPE_STRING, emptyCheck),
                    new ImportFieldData(KEY_CLASS_ID, ImportFieldData.TYPE_REF, emptyCheck)
            };
        }
    }

    public static final class LearnersOnPlacesImportData extends SchoolContract.TableLearnersOnPlaces {

        ImportFieldData[] rowData;

        public LearnersOnPlacesImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(KEY_LEARNER_ID, ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(KEY_PLACE_ID, ImportFieldData.TYPE_REF, emptyCheck)
            };
        }
    }

    public static final class LearnersGradesImportData extends SchoolContract.TableLearnersGrades {

        ImportFieldData[] rowData;

        public LearnersGradesImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_DATE, ImportFieldData.TYPE_STRING, dateCheck),
                    new ImportFieldData(COLUMN_LESSON_NUMBER, ImportFieldData.TYPE_LONG, numberCheck),
                    new ImportFieldData(COLUMNS_GRADE[0], ImportFieldData.TYPE_LONG, numberCheck),
                    new ImportFieldData(COLUMNS_GRADE[1], ImportFieldData.TYPE_LONG, numberCheck),
                    new ImportFieldData(COLUMNS_GRADE[2], ImportFieldData.TYPE_LONG, numberCheck),
                    new ImportFieldData(KEYS_GRADES_TITLES_ID[0], ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(KEYS_GRADES_TITLES_ID[1], ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(KEYS_GRADES_TITLES_ID[2], ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(KEY_ABSENT_TYPE_ID, ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(KEY_SUBJECT_ID, ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(KEY_LEARNER_ID, ImportFieldData.TYPE_REF, emptyCheck)
            };
        }
    }

    public static final class LearnersGradesTitlesImportData extends SchoolContract.TableLearnersGradesTitles {

        ImportFieldData[] rowData;

        public LearnersGradesTitlesImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_LEARNERS_GRADES_TITLE, ImportFieldData.TYPE_STRING, emptyCheck)
            };
        }
    }

    public static final class LearnersAbsentTypesImportData extends SchoolContract.TableLearnersAbsentTypes {

        ImportFieldData[] rowData;

        public LearnersAbsentTypesImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_LEARNERS_ABSENT_TYPE_NAME, ImportFieldData.TYPE_STRING, emptyCheck),
                    new ImportFieldData(COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME, ImportFieldData.TYPE_STRING, emptyCheck)
            };
        }
    }

    public static final class SubjectsImportData extends SchoolContract.TableSubjects {
        ImportFieldData[] rowData;

        public SubjectsImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(COLUMN_NAME, ImportFieldData.TYPE_STRING, emptyCheck),
                    new ImportFieldData(KEY_CLASS_ID, ImportFieldData.TYPE_REF, emptyCheck)
            };
        }
    }

    // таблица уроков (предмет-время-кабинет)
    public static final class SubjectAndTimeCabinetAttitudeImportData extends SchoolContract.TableSubjectAndTimeCabinetAttitude {

        ImportFieldData[] rowData;

        public SubjectAndTimeCabinetAttitudeImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(KEY_SUBJECT_ID, ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(KEY_CABINET_ID, ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(COLUMN_LESSON_NUMBER, ImportFieldData.TYPE_LONG, numberCheck),
                    new ImportFieldData(COLUMN_LESSON_DATE, ImportFieldData.TYPE_STRING, dateCheck),
                    new ImportFieldData(COLUMN_REPEAT, ImportFieldData.TYPE_LONG, repeatCheck)
            };
        }

        // проверка повторов
        private static final ImportFieldData.ConditionCheckable repeatCheck = (checkAble) -> {
            long number = Long.parseLong((String) checkAble);
            return (CONSTANT_REPEAT_NEVER <= number && number <= CONSTANT_REPEAT_WEEKLY);
        };
    }

    public static final class LessonCommentImportData extends SchoolContract.TableLessonComment {

        ImportFieldData[] rowData;

        public LessonCommentImportData() {
            this.rowData = new ImportFieldData[]{
                    new ImportFieldData(BaseColumns._ID, ImportFieldData.TYPE_LONG, idCheck),
                    new ImportFieldData(KEY_LESSON_ID, ImportFieldData.TYPE_REF, emptyCheck),
                    new ImportFieldData(COLUMN_LESSON_DATE, ImportFieldData.TYPE_STRING, dateCheck),
                    new ImportFieldData(COLUMN_LESSON_TEXT, ImportFieldData.TYPE_STRING, emptyCheck)
            };
        }
    }

}
