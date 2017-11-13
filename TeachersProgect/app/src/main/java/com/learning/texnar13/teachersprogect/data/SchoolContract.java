package com.learning.texnar13.teachersprogect.data;

import android.provider.BaseColumns;

public final class SchoolContract {

    public static final String DB_NAME = "school";

    SchoolContract() {
    }

    public final class TableSettingsData {
        public static final String NAME_TABLE_SETTINGS = "settingsData";
        public static final String KEY_SETTINGS_PROFILE_ID = BaseColumns._ID;
        public static final String COLUMN_PROFILE_NAME = "profileName";
        public static final String COLUMN_INTERFACE_SIZE = "interfaceSize";
    }

    public final class TableCabinets {
        public static final String NAME_TABLE_CABINETS = "cabinets";
        public static final String KEY_CABINET_ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
    }

    public final class TableDesks {
        public static final String NAME_TABLE_DESKS = "desks";
        public static final String KEY_DESK_ID = BaseColumns._ID;
        public static final String COLUMN_X = "x";
        public static final String COLUMN_Y = "y";
        public static final String COLUMN_NUMBER_OF_PLACES = "numberOfPlaces";
        public static final String KEY_CABINET_ID = "cabinetId";
    }

    public final class TablePlaces {
        public static final String NAME_TABLE_PLACES = "places";
        public static final String KEY_PLACE_ID = BaseColumns._ID;
        public static final String KEY_DESK_ID = "deskId";
        public static final String COLUMN_ORDINAL = "number";//какое по счету место
    }

    public final class TableClasses {
        public static final String NAME_TABLE_CLASSES = "classes";
        public static final String KEY_CLASS_ID = BaseColumns._ID;
        public static final String COLUMN_CLASS_NAME = "className";
    }

    public final class TableLearners {
        public static final String NAME_TABLE_LEARNERS = "learners";
        public static final String KEY_LEARNER_ID = BaseColumns._ID;
        public static final String COLUMN_FIRST_NAME = "firstName";
        public static final String COLUMN_SECOND_NAME = "secondName";
        public static final String KEY_CLASS_ID = "classId";
    }

    public final class TableLearnersOnPlaces {
        public static final String NAME_TABLE_LEARNERS_ON_PLACES = "learnersOnPlaces";
        public static final String KEY_ATTITUDES_ID = BaseColumns._ID;
        public static final String KEY_LEARNER_ID = "learnerId";
        public static final String KEY_PLACE_ID = "placeId";
    }

    public final class TableLearnersGrades {
        public static final String NAME_TABLE_LEARNERS_GRADES = "learnersGrades";
        public static final String KEY_GRADE_ID = BaseColumns._ID;
        public static final String COLUMN_GRADE = "grade";
        public static final String COLUMN_TIME_STAMP = "time";//в timestamp
        public static final String KEY_LESSON_ID = "lessonId";
        public static final String KEY_LEARNER_ID = "learnerId";
    }

    public final class TableLessons {
        public static final String NAME_TABLE_LESSONS = "lessons";
        public static final String KEY_LESSON_ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String KEY_CLASS_ID = "classId";
    }

    //--
    public final class TableLessonAndTimeWithCabinet {
        public static final String NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET = "lessonsAnd";
        public static final String KEY_LESSON_AND_TIME_ATTITUDE_ID = BaseColumns._ID;
        public static final String KEY_LESSON_ID = "lessonId";
        public static final String KEY_CABINET_ID = "cabinetId";
        public static final String COLUMN_DATE_BEGIN = "lessonDateBegin";//todo timestamp
        public static final String COLUMN_DATE_END = "lessonDateEnd";//todo timestamp
        //--1
        public static final String COLUMN_REPEAT = "repeat";
        //constants:
        public static final int CONSTANT_REPEAT_NEVER = 0;
        public static final int CONSTANT_REPEAT_DAILY = 1;
        public static final int CONSTANT_REPEAT_WEEKLY = 2;
        public static final int CONSTANT_REPEAT_MONTHLY = 3;
        //--1
    }
    //--
}
