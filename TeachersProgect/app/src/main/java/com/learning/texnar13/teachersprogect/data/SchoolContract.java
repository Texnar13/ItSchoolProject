package com.learning.texnar13.teachersprogect.data;

import android.provider.BaseColumns;

public class SchoolContract {

    public static final String DB_NAME = "school";
    public static final String DECODING_TIMES_STRING = "yyyy-MM-dd HH:mm:ss";

    public static class TableSettingsData extends Table {
        public static final String NAME_TABLE_SETTINGS = "settingsData";
        public static final String COLUMN_PROFILE_NAME = "profileName";
        public static final String COLUMN_LOCALE = "locale";
        public static final String COLUMN_INTERFACE_SIZE = "interfaceSize";
        public static final String COLUMN_MAX_ANSWER = "maxAnswer";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_ARE_THE_GRADES_COLORED = "areTheGradesColored";
        // название полей-массивов в json
        public static final String COLUMN_TIME_BEGIN_HOUR_NAME = "beginHour";
        public static final String COLUMN_TIME_BEGIN_MINUTE_NAME = "beginMinute";
        public static final String COLUMN_TIME_END_HOUR_NAME = "endHour";
        public static final String COLUMN_TIME_END_MINUTE_NAME = "endMinute";

        // константы
        public static final String COLUMN_LOCALE_DEFAULT_CODE = "defaultLocale";


        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS +"( " + SchoolContract.TableSettingsData.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME + " VARCHAR, " +
                SchoolContract.TableSettingsData.COLUMN_LOCALE + " TEXT DEFAULT '" + SchoolContract.TableSettingsData.COLUMN_LOCALE_DEFAULT_CODE + "', " +
                SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER + " INTEGER DEFAULT 5, " +
                SchoolContract.TableSettingsData.COLUMN_TIME + " TEXT DEFAULT " +
                "'{\"" + SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_HOUR_NAME + "\":" +
                //часы начала
                "[\"8\",\"9\",\"10\",\"11\",\"12\",\"13\",\"14\",\"15\",\"16\"]," +
                //минуты начала
                "\"" + SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_MINUTE_NAME + "\":" +
                "[\"30\",\"30\",\"30\",\"30\",\"25\",\"30\",\"25\",\"20\",\"6\"]," +
                //часы конца
                "\"" + SchoolContract.TableSettingsData.COLUMN_TIME_END_HOUR_NAME + "\":" +
                "[\"9\",\"10\",\"11\",\"12\",\"13\",\"14\",\"15\",\"16\",\"23\"]," +
                //минуты конца
                "\"" + SchoolContract.TableSettingsData.COLUMN_TIME_END_MINUTE_NAME + "\":" +
                "[\"15\",\"15\",\"15\",\"15\",\"10\",\"15\",\"10\",\"5\",\"59\"]" +
                "}'," +
                SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE + " INTEGER, " +
                SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED + " INTEGER DEFAULT 1);";
    }

    public static class TableStatisticsProfiles extends Table {
        public static final String NAME_TABLE_STATISTICS_PROFILES = "statisticsProfiles";
        public static final String COLUMN_PROFILE_NAME = "profileName";
        public static final String COLUMN_START_DATE = "startDate";
        public static final String COLUMN_END_DATE = "endDate";

        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES +"( " + SchoolContract.TableStatisticsProfiles.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME + " TEXT, " +
                SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE + " TIMESTRING DEFAULT \"1000-00-00\" NOT NULL, " +
                SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE + " TIMESTRING DEFAULT \"1000-00-00\" NOT NULL); ";
    }

    public static class TableCabinets extends Table {
        public static final String NAME_TABLE_CABINETS = "cabinets";
        public static final String COLUMN_CABINET_MULTIPLIER = "sizeMultiplier";
        public static final String COLUMN_CABINET_OFFSET_X = "offsetX";
        public static final String COLUMN_CABINET_OFFSET_Y = "offsetY";
        public static final String COLUMN_NAME = "name";

        public static final String CREATE_TABLE_STRING ="CREATE TABLE " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS +"( " + SchoolContract.TableCabinets.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableCabinets.COLUMN_NAME + " VARCHAR, " +
                SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER + " INTEGER DEFAULT \"15\", " +
                SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X + " INTEGER DEFAULT \"0\", " +
                SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y + " INTEGER DEFAULT \"0\" ); ";
    }


//    // https://stackoverflow.com/questions/2371025/abstract-variables-in-java
//
//    public static abstract class TableRowUnit {
//        // public final String TEST_STRING;
//        // TableRowUnit(String abc){this.TEST_STRING = abc;}
//
//        public static []
//
//        // может попороовать создать новый класс и совместить в нем ImportDBModel_v1 и SchoolContract?
//    }


    public static class TableDesks extends Table {
        public static final String NAME_TABLE_DESKS = "desks";
        public static final String COLUMN_X = "x";
        public static final String COLUMN_Y = "y";
        public static final String COLUMN_NUMBER_OF_PLACES = "numberOfPlaces";
        public static final String KEY_CABINET_ID = "cabinetId";

        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContract.TableDesks.NAME_TABLE_DESKS +"( " + SchoolContract.TableDesks.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableDesks.COLUMN_X + " INTEGER, " +
                SchoolContract.TableDesks.COLUMN_Y + " INTEGER, " +
                SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES + " INTEGER, " +
                SchoolContract.TableDesks.KEY_CABINET_ID + " INTEGER, " +
                "FOREIGN KEY(" + SchoolContract.TableDesks.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "(" + SchoolContract.TableCabinets.KEY_ROW_ID + ") ON DELETE CASCADE ); ";
    }

    public static class TablePlaces extends Table {// todo помоему нужно убрать эту таблицу, а все данные переместить в парты
        public static final String NAME_TABLE_PLACES = "places";
        public static final String KEY_DESK_ID = "deskId";
        public static final String COLUMN_ORDINAL = "number";//какое по счету место

        public static final String CREATE_TABLE_STRING ="CREATE TABLE " + SchoolContract.TablePlaces.NAME_TABLE_PLACES +" ( " + SchoolContract.TablePlaces.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TablePlaces.KEY_DESK_ID + " INTEGER, " +
                SchoolContract.TablePlaces.COLUMN_ORDINAL + " INTEGER, " +
                "FOREIGN KEY(" + SchoolContract.TablePlaces.KEY_DESK_ID + ") REFERENCES " + SchoolContract.TableDesks.NAME_TABLE_DESKS + " (" + SchoolContract.TableDesks.KEY_ROW_ID + ") ON DELETE CASCADE ); ";
    }

    public static class TableClasses extends Table {
        public static final String NAME_TABLE_CLASSES = "classes";
        public static final String COLUMN_CLASS_NAME = "className";

        public static final String CREATE_TABLE_STRING ="CREATE TABLE " + SchoolContract.TableClasses.NAME_TABLE_CLASSES +" ( " + SchoolContract.TableClasses.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableClasses.COLUMN_CLASS_NAME + " VARCHAR ); ";
    }

    public static class TableLearners extends Table {
        public static final String NAME_TABLE_LEARNERS = "learners";
        public static final String COLUMN_FIRST_NAME = "firstName";
        public static final String COLUMN_SECOND_NAME = "secondName";
        public static final String COLUMN_COMMENT = "comment";
        public static final String KEY_CLASS_ID = "classId";

        public static final String CREATE_TABLE_STRING ="CREATE TABLE " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS +" ( " + SchoolContract.TableLearners.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableLearners.COLUMN_FIRST_NAME + " VARCHAR, " +
                SchoolContract.TableLearners.COLUMN_SECOND_NAME + " VARCHAR, " +
                SchoolContract.TableLearners.COLUMN_COMMENT + " VARCHAR, " +
                SchoolContract.TableLearners.KEY_CLASS_ID + " INTEGER, " +
                "FOREIGN KEY(" + SchoolContract.TableLearners.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_ROW_ID + ") ON DELETE CASCADE ); ";
    }

    public static class TableLearnersOnPlaces extends Table {
        public static final String NAME_TABLE_LEARNERS_ON_PLACES = "learnersOnPlaces";
        public static final String KEY_LEARNER_ID = "learnerId";
        public static final String KEY_PLACE_ID = "placeId";

        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES +" ( " + SchoolContract.TableLearnersOnPlaces.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " INTEGER, " +
                SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " INTEGER, " +
                "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_ROW_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + ") REFERENCES " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " (" + SchoolContract.TablePlaces.KEY_ROW_ID + ") ON DELETE CASCADE ); ";
    }

    public static class TableLearnersGrades extends Table {
        public static final String NAME_TABLE_LEARNERS_GRADES = "learnersGrades";

        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LESSON_NUMBER = "lessonNumber";

        public static final String[] COLUMNS_GRADE = {"grade1", "grade2", "grade3"};
        public static final String[] KEYS_GRADES_TITLES_ID = {"title1Id", "title2Id", "title3Id"};
        public static final String KEY_ABSENT_TYPE_ID = "absentTypeId";// внимание! может храниться как нулевое поле (обозначает что пропуска нет)

        public static final String KEY_SUBJECT_ID = "subjectId";
        public static final String KEY_LEARNER_ID = "learnerId";

        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES +" ( " +
                SchoolContract.TableLearnersGrades.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableLearnersGrades.COLUMN_DATE + " TIMESTRING DEFAULT \"0000-00-00\" NOT NULL," +
                SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + " INTEGER DEFAULT 0 NOT NULL," +

                SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0] + " INTEGER DEFAULT 0 NOT NULL, " +
                SchoolContract.TableLearnersGrades.COLUMNS_GRADE[1] + " INTEGER DEFAULT 0 NOT NULL, " +
                SchoolContract.TableLearnersGrades.COLUMNS_GRADE[2] + " INTEGER DEFAULT 0 NOT NULL, " +

                SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0] + " INTEGER DEFAULT 1 NOT NULL, " +
                SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1] + " INTEGER DEFAULT 1 NOT NULL, " +
                SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2] + " INTEGER DEFAULT 1 NOT NULL, " +
                SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID + " INTEGER DEFAULT NULL, " +

                SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " INTEGER NOT NULL, " +
                SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID + ") REFERENCES " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + " (" + SchoolContract.TableSubjects.KEY_ROW_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_ROW_ID + ") ON DELETE CASCADE, " +

                "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0] + ") REFERENCES " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + " (" + SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID + ") ON DELETE SET DEFAULT, " +
                "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1] + ") REFERENCES " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + " (" + SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID + ") ON DELETE SET DEFAULT, " +
                "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2] + ") REFERENCES " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + " (" + SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID + ") ON DELETE SET DEFAULT, " +
                "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID + ") REFERENCES " + SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES + " (" + SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID + ") ON DELETE CASCADE ); ";
    }

    public static class TableLearnersGradesTitles extends Table {
        public static final String NAME_TABLE_LEARNERS_GRADES_TITLES = "learnersGradesTitles";
        public static final String COLUMN_LEARNERS_GRADES_TITLE = "title";

        public static final String CREATE_TABLE_STRING ="CREATE TABLE " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES +"( " + SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE + " TEXT);";

    }

    public static class TableLearnersAbsentTypes extends Table {
        public static final String NAME_TABLE_LEARNERS_ABSENT_TYPES = "learnersAbsentTypes";
        public static final String COLUMN_LEARNERS_ABSENT_TYPE_NAME = "absentName";
        public static final String COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME = "absentLongName";

        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES +"( " + SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME + " TEXT NOT NULL, " +
                SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME + " TEXT NOT NULL);";
    }

    public static class TableSubjects extends Table {
        public static final String NAME_TABLE_SUBJECTS = "subjects";
        public static final String COLUMN_NAME = "name";
        public static final String KEY_CLASS_ID = "classId";

        public static final String CREATE_TABLE_STRING ="CREATE TABLE " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS +"( " + SchoolContract.TableSubjects.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableSubjects.COLUMN_NAME + " VARCHAR, " +
                SchoolContract.TableSubjects.KEY_CLASS_ID + " INTEGER, " +
                "FOREIGN KEY(" + SchoolContract.TableSubjects.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_ROW_ID + ") ON DELETE CASCADE ); ";
    }

    // таблица уроков (предмет-время-кабинет)
    public static class TableSubjectAndTimeCabinetAttitude extends Table {
        public static final String NAME_TABLE = "lessonAndTimeWithCabinet";
        public static final String KEY_SUBJECT_ID = "subjectId";
        public static final String KEY_CABINET_ID = "cabinetId";

        public static final String COLUMN_LESSON_NUMBER = "lessonNumber";
        public static final String COLUMN_LESSON_DATE = "lessonDate";// todo исправить баг с исправлением даты
        public static final String COLUMN_END_REPEAT_DATE = "endRepeatDate";
        public static final String COLUMN_REPEAT = "repeat";

        // repeat constants(посмотри LessonRedactorDialogFragment):
        public static final int CONSTANT_REPEAT_NEVER = 0;
        public static final int CONSTANT_REPEAT_DAILY = 1;
        public static final int CONSTANT_REPEAT_WEEKLY = 2;
        public static final int CONSTANT_REPEAT_ON_WORKING_DAYS = 3;//todo CONSTANT_REPEAT_ON_WORKING_DAYS
        public static final int CONSTANT_REPEAT_MONTHLY = 4;


        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE +"( " + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID + " INTEGER, " +
                SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID + " INTEGER, " +
                SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " INTEGER DEFAULT 0 NOT NULL," +
                SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " TIMESTRING DEFAULT \"0000-00-00\" NOT NULL," +
                SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE + " TIMESTRING," +// может быть пустое значение (null) это значит что повторы не прекратятся
                SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " INTEGER DEFAULT " + SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER + ", " +
                "FOREIGN KEY(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + " (" + SchoolContract.TableCabinets.KEY_ROW_ID + ") ON DELETE CASCADE ," +
                "FOREIGN KEY(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID + ") REFERENCES " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + " (" + SchoolContract.TableSubjects.KEY_ROW_ID + ") ON DELETE CASCADE ); ";
    }

    public static class TableLessonComment extends Table {
        public static final String NAME_TABLE_LESSON_TEXT = "lessonComment";
        public static final String KEY_LESSON_ID = "commentLessonId";
        // если возникнут вопросы: номер урока не нужен, тк даже две математики в один день будут иметь разный id
        public static final String COLUMN_LESSON_DATE = "commentDate";
        public static final String COLUMN_LESSON_TEXT = "commentText";

        public static final String CREATE_TABLE_STRING = "CREATE TABLE " + SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT +"( " +
                SchoolContract.TableLessonComment.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SchoolContract.TableLessonComment.KEY_LESSON_ID + " INTEGER NOT NULL, " +
                SchoolContract.TableLessonComment.COLUMN_LESSON_DATE + " TIMESTRING NOT NULL, " +
                SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + SchoolContract.TableLessonComment.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE + " (" + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID + ") ON DELETE CASCADE);";
    }

    private static abstract class Table {
        public static final String KEY_ROW_ID = BaseColumns._ID;
    }

}
