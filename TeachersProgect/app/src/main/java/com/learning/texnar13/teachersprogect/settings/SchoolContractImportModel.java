package com.learning.texnar13.teachersprogect.settings;

import android.provider.BaseColumns;

import com.learning.texnar13.teachersprogect.data.SchoolContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;

// класс буфер для преобразования данных в xml
public final class SchoolContractImportModel extends SchoolContract {


    public int fileVersion;


    SettingsImportData settingsData;
    LinkedList<StatisticsProfilesImportData> statisticsData;
    LinkedList<CabinetsImportData> cabinetsImportData;
    LinkedList<DesksImportData> desksImportData;


    // конструктор
    SchoolContractImportModel() {
        super();

        this.fileVersion = -1;

        this.settingsData = null;
        this.statisticsData = new LinkedList<>();
        this.cabinetsImportData = new LinkedList<>();
        this.desksImportData = new LinkedList<>();

    }


    // ------------------------------ классы-интерпретации таблиц ----------------------------------

    // переменная проверки для всех id
    private static final ImportFieldData.ConditionCheckable idCheck = (checkable) -> {
        long id = (long) checkable;
        return id >= 1;
    };

    // проверка для текстовых полей без проверки содержимого
    //todo не забудь про огрраничения в количество символов, хотя наверное не надо
    private static final ImportFieldData.ConditionCheckable emptyCheck = (checkAble) -> true;


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


//        public long keyId;
//        public String profileName;
//        public String locale;
//        public int interfaceSize;
//        public int maxAnswer;
//        public String timePeriodsJson;
//        public boolean coloredGrades;
//
//
//        public SettingsImportData(long keyId, String profileName,
//                                  String locale,
//                                  int interfaceSize,
//                                  int maxAnswer,
//                                  String timePeriodsJson, boolean coloredGrades) {
//            this.keyId = keyId;
//            this.profileName = profileName;
//            this.locale = locale;
//            this.interfaceSize = interfaceSize;
//            this.maxAnswer = maxAnswer;
//            this.timePeriodsJson = timePeriodsJson;
//            this.coloredGrades = coloredGrades;
//        }
    }

    public static final class StatisticsProfilesImportData extends SchoolContract.TableStatisticsProfiles {

//        public long keyId;
//        public String profileName;
//        public String startDate;
//        public String endDate;
//
//        public StatisticsProfilesImportData(long keyId, String profileName, String startDate, String endDate) {
//            this.keyId = keyId;
//            this.profileName = profileName;
//            this.startDate = startDate;
//            this.endDate = endDate;
//        }
    }

    public static final class CabinetsImportData extends SchoolContract.TableCabinets {

        ImportFieldData[] rowData;

        public CabinetsImportData() {
            this.rowData = new ImportFieldData[]{
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

        //        long keyId;
//        long cabinetMultiplier;
//        long offsetX;
//        long offsetY;
//        String cabinetName;
//
//        public CabinetsImportData(long keyId, long cabinetMultiplier, long offsetX, long offsetY, String cabinetName) {
//            this.keyId = keyId;
//            this.cabinetMultiplier = cabinetMultiplier;
//            this.offsetX = offsetX;
//            this.offsetY = offsetY;
//            this.cabinetName = cabinetName;
//        }
    }

    public static final class DesksImportData extends SchoolContract.TableDesks {

        long keyId;
        long posX;
        long posY;
        long numberOfPlaces;
        CabinetsImportData refCabinet;

        public DesksImportData(long keyId, long posX, long posY, long numberOfPlaces, CabinetsImportData refCabinet) {
            this.keyId = keyId;
            this.posX = posX;
            this.posY = posY;
            this.numberOfPlaces = numberOfPlaces;
            this.refCabinet = refCabinet;
        }
    }

    public static final class TablePlaces {// todo помоему нужно убрать эту таблицу, а все данные переместить в парты
        public static final String NAME_TABLE_PLACES = "places";
        public static final String KEY_PLACE_ID = BaseColumns._ID;
        public static final String KEY_DESK_ID = "deskId";
        public static final String COLUMN_ORDINAL = "number";//какое по счету место
    }

    public static final class TableClasses {
        public static final String NAME_TABLE_CLASSES = "classes";
        public static final String KEY_CLASS_ID = BaseColumns._ID;
        public static final String COLUMN_CLASS_NAME = "className";
    }

    public static final class TableLearners {
        public static final String NAME_TABLE_LEARNERS = "learners";
        public static final String KEY_LEARNER_ID = BaseColumns._ID;
        public static final String COLUMN_FIRST_NAME = "firstName";
        public static final String COLUMN_SECOND_NAME = "secondName";
        public static final String COLUMN_COMMENT = "comment";
        public static final String KEY_CLASS_ID = "classId";
    }

    public static final class TableLearnersOnPlaces {
        public static final String NAME_TABLE_LEARNERS_ON_PLACES = "learnersOnPlaces";
        public static final String KEY_ATTITUDE_ID = BaseColumns._ID;
        public static final String KEY_LEARNER_ID = "learnerId";
        public static final String KEY_PLACE_ID = "placeId";
    }

    public static final class TableLearnersGrades {
        public static final String NAME_TABLE_LEARNERS_GRADES = "learnersGrades";
        public static final String KEY_GRADE_ID = BaseColumns._ID;

        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LESSON_NUMBER = "lessonNumber";

        public static final String[] COLUMNS_GRADE = {"grade1", "grade2", "grade3"};
        public static final String[] KEYS_GRADES_TITLES_ID = {"title1Id", "title2Id", "title3Id"};
        public static final String KEY_ABSENT_TYPE_ID = "absentTypeId";// внимание! может храниться как нулевое поле (обозначает что пропуска нет)

        public static final String KEY_SUBJECT_ID = "subjectId";
        public static final String KEY_LEARNER_ID = "learnerId";

        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContractImportModel.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + " ( " +
                SchoolContractImportModel.TableLearnersGrades.KEY_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContractImportModel.TableLearnersGrades.COLUMN_DATE + " TIMESTRING DEFAULT \"0000-00-00\" NOT NULL," +
                SchoolContractImportModel.TableLearnersGrades.COLUMN_LESSON_NUMBER + " INTEGER DEFAULT 0 NOT NULL," +

                SchoolContractImportModel.TableLearnersGrades.COLUMNS_GRADE[0] + " INTEGER DEFAULT 0 NOT NULL, " +
                SchoolContractImportModel.TableLearnersGrades.COLUMNS_GRADE[1] + " INTEGER DEFAULT 0 NOT NULL, " +
                SchoolContractImportModel.TableLearnersGrades.COLUMNS_GRADE[2] + " INTEGER DEFAULT 0 NOT NULL, " +

                SchoolContractImportModel.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0] + " INTEGER DEFAULT 1 NOT NULL, " +
                SchoolContractImportModel.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1] + " INTEGER DEFAULT 1 NOT NULL, " +
                SchoolContractImportModel.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2] + " INTEGER DEFAULT 1 NOT NULL, " +
                SchoolContractImportModel.TableLearnersGrades.KEY_ABSENT_TYPE_ID + " INTEGER DEFAULT NULL, " +

                SchoolContractImportModel.TableLearnersGrades.KEY_LEARNER_ID + " INTEGER NOT NULL, " +
                SchoolContractImportModel.TableLearnersGrades.KEY_SUBJECT_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + SchoolContractImportModel.TableLearnersGrades.KEY_SUBJECT_ID + ") REFERENCES " + SchoolContractImportModel.TableSubjects.NAME_TABLE_SUBJECTS + " (" + SchoolContractImportModel.TableSubjects.KEY_SUBJECT_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + SchoolContractImportModel.TableLearnersGrades.KEY_LEARNER_ID + ") REFERENCES " + SchoolContractImportModel.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContractImportModel.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE, " +

                "FOREIGN KEY(" + SchoolContractImportModel.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0] + ") REFERENCES " + SchoolContractImportModel.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + " (" + SchoolContractImportModel.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID + ") ON DELETE SET DEFAULT, " +
                "FOREIGN KEY(" + SchoolContractImportModel.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1] + ") REFERENCES " + SchoolContractImportModel.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + " (" + SchoolContractImportModel.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID + ") ON DELETE SET DEFAULT, " +
                "FOREIGN KEY(" + SchoolContractImportModel.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2] + ") REFERENCES " + SchoolContractImportModel.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + " (" + SchoolContractImportModel.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID + ") ON DELETE SET DEFAULT, " +
                "FOREIGN KEY(" + SchoolContractImportModel.TableLearnersGrades.KEY_ABSENT_TYPE_ID + ") REFERENCES " + SchoolContractImportModel.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES + " (" + SchoolContractImportModel.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID + ") ON DELETE CASCADE ); ";
    }

    public static final class TableLearnersGradesTitles {
        public static final String NAME_TABLE_LEARNERS_GRADES_TITLES = "learnersGradesTitles";
        public static final String KEY_LEARNERS_GRADES_TITLE_ID = BaseColumns._ID;
        public static final String COLUMN_LEARNERS_GRADES_TITLE = "title";

    }

    public static final class TableLearnersAbsentTypes {
        public static final String NAME_TABLE_LEARNERS_ABSENT_TYPES = "learnersAbsentTypes";
        public static final String KEY_LEARNERS_ABSENT_TYPE_ID = BaseColumns._ID;
        public static final String COLUMN_LEARNERS_ABSENT_TYPE_NAME = "absentName";
        public static final String COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME = "absentLongName";
    }

    public static final class TableSubjects {
        public static final String NAME_TABLE_SUBJECTS = "subjects";
        public static final String KEY_SUBJECT_ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String KEY_CLASS_ID = "classId";
    }

    // таблица уроков (предмет-время-кабинет)
    public static final class TableSubjectAndTimeCabinetAttitude {
        public static final String NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE = "lessonAndTimeWithCabinet";
        public static final String KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID = BaseColumns._ID;
        public static final String KEY_SUBJECT_ID = "subjectId";
        public static final String KEY_CABINET_ID = "cabinetId";

        public static final String COLUMN_LESSON_NUMBER = "lessonNumber";
        public static final String COLUMN_LESSON_DATE = "lessonDate";// todo исправить баг с исправлением даты
        public static final String COLUMN_END_REPEAT_DATE = "endRepeatDate";// todo TODO TODO!!!!!
        public static final String COLUMN_REPEAT = "repeat";

        // repeat constants(посмотри LessonRedactorDialogFragment):
        public static final int CONSTANT_REPEAT_NEVER = 0;
        public static final int CONSTANT_REPEAT_DAILY = 1;
        public static final int CONSTANT_REPEAT_WEEKLY = 2;
        public static final int CONSTANT_REPEAT_ON_WORKING_DAYS = 3;//todo CONSTANT_REPEAT_ON_WORKING_DAYS
        public static final int CONSTANT_REPEAT_MONTHLY = 4;
    }

    public static final class TableLessonComment {
        public static final String NAME_TABLE_LESSON_TEXT = "lessonComment";
        public static final String KEY_LESSON_TEXT_ID = BaseColumns._ID;
        public static final String KEY_LESSON_ID = "commentLessonId";
        // номер урока не нужен, тк даже две математики в один день будут иметь разный id
        public static final String COLUMN_LESSON_DATE = "commentDate";
        public static final String COLUMN_LESSON_TEXT = "commentText";
    }

}
