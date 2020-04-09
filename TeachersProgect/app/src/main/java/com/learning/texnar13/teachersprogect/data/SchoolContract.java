package com.learning.texnar13.teachersprogect.data;

import android.provider.BaseColumns;

public final class SchoolContract {

    public static final String DB_NAME = "school";
    public static final String DECODING_TIMES_STRING = "yyyy-MM-dd HH:mm:ss";

    SchoolContract() {
    }

    public static final class TableSettingsData {
        public static final String NAME_TABLE_SETTINGS = "settingsData";
        public static final String KEY_SETTINGS_PROFILE_ID = BaseColumns._ID;
        public static final String COLUMN_PROFILE_NAME = "profileName";
        public static final String COLUMN_LOCALE = "locale";
        public static final String COLUMN_LOCALE_DEFAULT_CODE = "defaultLocale";
        public static final String COLUMN_INTERFACE_SIZE = "interfaceSize";
        public static final String COLUMN_MAX_ANSWER = "maxAnswer";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_ARE_THE_GRADES_COLORED = "areTheGradesColored";
        //массивы в json
        public static final String COLUMN_TIME_BEGIN_HOUR_NAME = "beginHour";
        public static final String COLUMN_TIME_BEGIN_MINUTE_NAME = "beginMinute";
        public static final String COLUMN_TIME_END_HOUR_NAME = "endHour";
        public static final String COLUMN_TIME_END_MINUTE_NAME = "endMinute";
    }

    public static final class TableStatisticsProfiles {
        public static final String NAME_TABLE_STATISTICS_PROFILES = "statisticsProfiles";
        public static final String KEY_STATISTICS_PROFILE_ID = BaseColumns._ID;
        public static final String COLUMN_PROFILE_NAME = "profileName";
        public static final String COLUMN_START_DATE = "startDate";
        public static final String COLUMN_END_DATE = "endDate";
        //public static final String COLUMN_START_PERIOD_TIME = "startTime";
        //public static final String COLUMN_END_PERIOD_TIME = "endTime";
    }

    public static final class TableCabinets {
        public static final String NAME_TABLE_CABINETS = "cabinets";
        public static final String KEY_CABINET_ID = BaseColumns._ID;
        public static final String COLUMN_CABINET_MULTIPLIER = "sizeMultiplier";
        public static final String COLUMN_CABINET_OFFSET_X = "offsetX";
        public static final String COLUMN_CABINET_OFFSET_Y = "offsetY";
        public static final String COLUMN_NAME = "name";
    }

    public static final class TableDesks {
        public static final String NAME_TABLE_DESKS = "desks";
        public static final String KEY_DESK_ID = BaseColumns._ID;
        public static final String COLUMN_X = "x";
        public static final String COLUMN_Y = "y";
        public static final String COLUMN_NUMBER_OF_PLACES = "numberOfPlaces";
        public static final String KEY_CABINET_ID = "cabinetId";
    }

    public static final class TablePlaces {
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
        public static final String COLUMN_GRADE = "grade";

        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LESSON_NUMBER = "lessonNumber";

        //public static final String COLUMN_TIME_STAMP = "time";// в timestamp"yyyy-MM-dd HH:mm:ss"
        public static final String KEY_SUBJECT_ID = "subjectId";
        public static final String KEY_LEARNER_ID = "learnerId";
        public static final String KEY_GRADE_TITLE_ID = "titleId";
        //constants:
    }

    public static final class TableLearnersGradesTitles {
        public static final String NAME_TABLE_LEARNERS_GRADES_TITLES = "learnersGradesTitles";
        public static final String KEY_LEARNERS_GRADES_TITLE_ID = BaseColumns._ID;
        public static final String COLUMN_LEARNERS_GRADES_TITLE = "title";

    }

    public static final class TableSubjects {
        public static final String NAME_TABLE_SUBJECTS = "subjects";//rename to subject
        public static final String KEY_SUBJECT_ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String KEY_CLASS_ID = "classId";
    }

    public static final class TableSubjectAndTimeCabinetAttitude {
        public static final String NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE = "lessonAndTimeWithCabinet";//rename to lessonAndTimeWithCabinet
        public static final String KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID = BaseColumns._ID;
        public static final String KEY_SUBJECT_ID = "subjectId";
        public static final String KEY_CABINET_ID = "cabinetId";

        public static final String COLUMN_LESSON_DATE = "lessonDate";
        public static final String COLUMN_LESSON_NUMBER = "lessonNumber";

        //public static final String COLUMN_DATE_BEGIN = "lessonDateBegin";
        //public static final String COLUMN_DATE_END = "lessonDateEnd";
        public static final String COLUMN_REPEAT = "repeat";

        // repeat constants:
        public static final int CONSTANT_REPEAT_NEVER = 0;
        public static final int CONSTANT_REPEAT_DAILY = 1;
        public static final int CONSTANT_REPEAT_WEEKLY = 2;
        public static final int CONSTANT_REPEAT_ON_WORKING_DAYS = 3;//todo CONSTANT_REPEAT_ON_WORKING_DAYS
        public static final int CONSTANT_REPEAT_MONTHLY = 4;
    }
}
