package com.learning.texnar13.teachersprogect.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class DataBaseObjectContract {

    @MySqliteTable(sqlTableName = "settingsData")
    class SettingsData{

        @MySqliteVarcharColumn(
                sqlColumnName = "profileName",
                sqlDefault = "")
        String profileName;
        @MySqliteVarcharColumn(
                sqlColumnName = "locale",
                sqlDefault = SchoolContract.TableSettingsData.COLUMN_LOCALE_DEFAULT_CODE)
        String locale;
        @MySqliteIntegerColumn(
                sqlColumnName = "interfaceSize",
                sqlDefault = 0)
        String interfaceSize;



    }



//
//    public static class TableSettingsData extends SchoolContract.Table {
//        public static final String COLUMN_PROFILE_NAME = "profileName";
//        public static final String COLUMN_LOCALE = "locale";
//        public static final String COLUMN_INTERFACE_SIZE = "interfaceSize";
//
//
//        public static final String COLUMN_MAX_ANSWER = "maxAnswer";
//        public static final String COLUMN_TIME = "time";
//        public static final String COLUMN_ARE_THE_GRADES_COLORED = "areTheGradesColored";
//        // название полей-массивов в json
//        public static final String COLUMN_TIME_BEGIN_HOUR_NAME = "beginHour";
//        public static final String COLUMN_TIME_BEGIN_MINUTE_NAME = "beginMinute";
//        public static final String COLUMN_TIME_END_HOUR_NAME = "endHour";
//        public static final String COLUMN_TIME_END_MINUTE_NAME = "endMinute";
//
//        // константы
//        public static final String COLUMN_LOCALE_DEFAULT_CODE = "defaultLocale";
//
//
//        public static final String CREATE_TABLE_STRING = "( " + SchoolContract.TableSettingsData.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME + " VARCHAR, " +
//                SchoolContract.TableSettingsData.COLUMN_LOCALE + " TEXT DEFAULT '" + SchoolContract.TableSettingsData.COLUMN_LOCALE_DEFAULT_CODE + "', " +
//                SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER + " INTEGER DEFAULT 5, " +
//                SchoolContract.TableSettingsData.COLUMN_TIME + " TEXT DEFAULT " +
//                "'{\"" + SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_HOUR_NAME + "\":" +
//                //часы начала
//                "[\"8\",\"9\",\"10\",\"11\",\"12\",\"13\",\"14\",\"15\",\"16\"]," +
//                //минуты начала
//                "\"" + SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_MINUTE_NAME + "\":" +
//                "[\"30\",\"30\",\"30\",\"30\",\"25\",\"30\",\"25\",\"20\",\"6\"]," +
//                //часы конца
//                "\"" + SchoolContract.TableSettingsData.COLUMN_TIME_END_HOUR_NAME + "\":" +
//                "[\"9\",\"10\",\"11\",\"12\",\"13\",\"14\",\"15\",\"16\",\"23\"]," +
//                //минуты конца
//                "\"" + SchoolContract.TableSettingsData.COLUMN_TIME_END_MINUTE_NAME + "\":" +
//                "[\"15\",\"15\",\"15\",\"15\",\"10\",\"15\",\"10\",\"5\",\"59\"]" +
//                "}'," +
//                SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE + " INTEGER, " +
//                SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED + " INTEGER DEFAULT 1);";
//    }
//


    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface MySqliteTable {
        String sqlTableName();
    }


    // TIMESTRING
    // INTEGER
    // VARCHAR
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface MySqliteVarcharColumn {
        String sqlColumnName();
        String sqlDefault();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface MySqliteIntegerColumn {
        String sqlColumnName();
        long sqlDefault();
    }


}
