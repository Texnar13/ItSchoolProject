package com.learning.texnar13.teachersprogect.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Xml;

import com.learning.texnar13.teachersprogect.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataBaseOpenHelper extends SQLiteOpenHelper {
    private static final boolean IS_DEBUG = true;
    private static final int DB_VERSION = 20;
    private final Context context;

    private static final String LOG_TAG = "DBOpenHelper";


    // передаем норамльную версию
    public DataBaseOpenHelper(Context context) {
        super(context, SchoolContract.DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, 1);
    }// единица только для проверок какие обновления выполнять

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON");//нужен для удаления каскадом
        super.onOpen(db);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (IS_DEBUG)
            Log.i(LOG_TAG, "updateDatabase old=" + oldVersion + " new=" + newVersion);

        if (oldVersion < 1) {//то приложение либо новое, либо удаляются данные

            //чистка бд
            db.execSQL("PRAGMA foreign_keys = OFF;");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableDesks.NAME_TABLE_DESKS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT + ";");
            db.execSQL("PRAGMA foreign_keys = ON;");

            //настройки
            String sql = "CREATE TABLE " + SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS + "( " + SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
            db.execSQL(sql);
            {// ---- вставляем одну запись настроек ----
                ContentValues values = new ContentValues();
                values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, "simpleName");
                values.put(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, 1);
                db.insert(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, values);
            }

            //кабинет
            sql = "CREATE TABLE " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "( " + SchoolContract.TableCabinets.KEY_CABINET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableCabinets.COLUMN_NAME + " VARCHAR, " +
                    SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER + " INTEGER DEFAULT \"15\", " +
                    SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X + " INTEGER DEFAULT \"0\", " +
                    SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y + " INTEGER DEFAULT \"0\" ); ";
            db.execSQL(sql);
            //парта
            sql = "CREATE TABLE " + SchoolContract.TableDesks.NAME_TABLE_DESKS + "( " + SchoolContract.TableDesks.KEY_DESK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableDesks.COLUMN_X + " INTEGER, " +
                    SchoolContract.TableDesks.COLUMN_Y + " INTEGER, " +
                    SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES + " INTEGER, " +
                    SchoolContract.TableDesks.KEY_CABINET_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableDesks.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "(" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            //место
            sql = "CREATE TABLE " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " ( " + SchoolContract.TablePlaces.KEY_PLACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TablePlaces.KEY_DESK_ID + " INTEGER, " +
                    SchoolContract.TablePlaces.COLUMN_ORDINAL + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TablePlaces.KEY_DESK_ID + ") REFERENCES " + SchoolContract.TableDesks.NAME_TABLE_DESKS + " (" + SchoolContract.TableDesks.KEY_DESK_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            //класс
            sql = "CREATE TABLE " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " ( " + SchoolContract.TableClasses.KEY_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableClasses.COLUMN_CLASS_NAME + " VARCHAR ); ";
            db.execSQL(sql);
            //ученик
            sql = "CREATE TABLE " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " ( " + SchoolContract.TableLearners.KEY_LEARNER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLearners.COLUMN_FIRST_NAME + " VARCHAR, " +
                    SchoolContract.TableLearners.COLUMN_SECOND_NAME + " VARCHAR, " +
                    SchoolContract.TableLearners.COLUMN_COMMENT + " VARCHAR, " +
                    SchoolContract.TableLearners.KEY_CLASS_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearners.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            //ученик-место
            sql = "CREATE TABLE " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + " ( " + SchoolContract.TableLearnersOnPlaces.KEY_ATTITUDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " INTEGER, " +
                    SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + ") REFERENCES " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " (" + SchoolContract.TablePlaces.KEY_PLACE_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            //оценки учеников
            db.execSQL(SchoolContract.TableLearnersGrades.CREATE_TABLE_STRING);

            //предмет
            sql = "CREATE TABLE " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + " ( " + SchoolContract.TableSubjects.KEY_SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableSubjects.COLUMN_NAME + " VARCHAR, " +
                    SchoolContract.TableSubjects.KEY_CLASS_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableSubjects.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            //урок и его время проведения
            sql = "CREATE TABLE " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE + " ( " + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID + " INTEGER, " +
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID + " INTEGER, " +
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " INTEGER DEFAULT 0 NOT NULL," +
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " TIMESTRING DEFAULT \"0000-00-00\" NOT NULL," +
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " INTEGER DEFAULT " + SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER + ", " +
                    "FOREIGN KEY(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + " (" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ON DELETE CASCADE ," +
                    "FOREIGN KEY(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID + ") REFERENCES " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + " (" + SchoolContract.TableSubjects.KEY_SUBJECT_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            // профили статистики
            db.execSQL("CREATE TABLE " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES + " ( " + SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME + " TEXT, " +
                    SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE + " TIMESTRING DEFAULT \"1000-00-00\" NOT NULL, " +
                    SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE + " TIMESTRING DEFAULT \"1000-00-00\" NOT NULL); ");
            // типы оценок
            db.execSQL(
                    "CREATE TABLE " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES +
                            " ( " + SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE + " TEXT);"
            );
            {// ---- вставляем одну запись "работа на уроке" ----  <-- эту запись нельзя удалить но можно переименовать
                String name = context.getResources().getString(R.string.db_table_grade_text_first_default_value);
                ContentValues values = new ContentValues();
                values.put(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, name);
                values.put(SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID, 1);
                db.insert(
                        SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES,
                        null,
                        values);
            }
            // типы пропусков
            db.execSQL(
                    "CREATE TABLE " + SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES +
                            " ( " + SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME + " TEXT NOT NULL, " +
                            SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME + " TEXT NOT NULL);"
            );
            {// ---- вставляем 2 записи "н", "бол." ----  <-- запись "н" нельзя удалить но можно переименовать
                ContentValues values1 = new ContentValues();
                values1.put(SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID, 1);
                values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
                        context.getResources().getString(R.string.db_table_grades_abs_1_default_value));
                values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
                        context.getResources().getString(R.string.db_table_grades_abs_1_long_default_value));
                db.insert(
                        SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                        null,
                        values1);

                ContentValues values2 = new ContentValues();
                values2.put(SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID, 2);
                values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
                        context.getResources().getString(R.string.db_table_grades_abs_2_default_value));
                values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
                        context.getResources().getString(R.string.db_table_grades_abs_2_long_default_value));
                db.insert(
                        SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                        null,
                        values2);
            }
            // комментарий к уроку
            db.execSQL(
                    "CREATE TABLE " + SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT + " ( " +
                            SchoolContract.TableLessonComment.KEY_LESSON_TEXT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            SchoolContract.TableLessonComment.KEY_LESSON_ID + " INTEGER NOT NULL, " +
                            SchoolContract.TableLessonComment.COLUMN_LESSON_DATE + " TIMESTRING NOT NULL, " +
                            SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT + " TEXT NOT NULL, " +
                            "FOREIGN KEY(" + SchoolContract.TableLessonComment.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE + " (" + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID + ") ON DELETE CASCADE);"
            );


        } else {//иначе г@внокод
            //операции по сохранению данных на старых версиях
            if (oldVersion < 17) {

                // -- добавляем колонку номера урока и даты урока, заменяющие собой время урока --
                db.execSQL("ALTER TABLE " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE +
                        " ADD COLUMN " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " INTEGER DEFAULT 0 NOT NULL;"
                );
                db.execSQL("ALTER TABLE " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE +
                        " ADD COLUMN " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " TIMESTRING DEFAULT \"0000-00-00\" NOT NULL;"
                );

                // получаем время уроков
                int[][] time;
                {
                    Cursor timeCursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = 1", null, null, null, null);
                    if (timeCursor.getCount() == 0) {
                        timeCursor.close();
                        time = new int[][]{};
                    } else {
                        time = new int[9][4];
                        timeCursor.moveToFirst();

                        JSONObject json;
                        JSONArray beginHour;
                        JSONArray beginMinute;
                        JSONArray endHour;
                        JSONArray endMinute;
                        try {
                            json = new JSONObject(
                                    timeCursor.getString(timeCursor.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_TIME))
                            );

                            beginHour = json.getJSONArray(SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_HOUR_NAME);
                            beginMinute = json.getJSONArray(SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_MINUTE_NAME);
                            endHour = json.getJSONArray(SchoolContract.TableSettingsData.COLUMN_TIME_END_HOUR_NAME);
                            endMinute = json.getJSONArray(SchoolContract.TableSettingsData.COLUMN_TIME_END_MINUTE_NAME);

                            for (int i = 0; i < 9; i++) {
                                time[i][0] = beginHour.getInt(i);
                                time[i][1] = beginMinute.getInt(i);
                                time[i][2] = endHour.getInt(i);
                                time[i][3] = endMinute.getInt(i);
                            }
                        } catch (JSONException e) {
                            time = new int[][]{};
                        }
                        timeCursor.close();
                    }
                }
                SimpleDateFormat timeDateFormat = new SimpleDateFormat(SchoolContract.DECODING_TIMES_STRING);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


                // получаем все уроки
                Cursor cursor = db.query(
                        SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                // пробегаемся по урокам
                while (cursor.moveToNext()) {

                    // получаем время этого урока
                    GregorianCalendar beginDate = new GregorianCalendar();
                    GregorianCalendar endDate = new GregorianCalendar();


                    try {
                        beginDate.setTime(timeDateFormat.parse(
                                cursor.getString(
                                        cursor.getColumnIndex(
                                                "lessonDateBegin"
                                        )
                                )
                        ));
                        endDate.setTime(timeDateFormat.parse(
                                cursor.getString(
                                        cursor.getColumnIndex(
                                                "lessonDateEnd"
                                        )
                                )
                        ));
                    } catch (Exception e) {
                        e.printStackTrace();
                        // в случае ошибок дату берем текущую
                        beginDate.setTime(new Date());
                        endDate.setTime(new Date());
                    }

                    // сравниваем полученное врекмя, с записанным в настройках
                    for (int i = 0; i < time.length; i++) {

                        if (time[i][0] == beginDate.get(Calendar.HOUR_OF_DAY) &&
                                time[i][1] == beginDate.get(Calendar.MINUTE) &&
                                time[i][2] == endDate.get(Calendar.HOUR_OF_DAY) &&
                                time[i][3] == endDate.get(Calendar.MINUTE)
                        ) {
                            ContentValues values = new ContentValues();
                            values.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER, i);
                            db.update(
                                    SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE,
                                    values,
                                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID + " = " +
                                            cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID)),
                                    null
                            );
                            break;
                        }
                    }
                    // если вдруг время не нашлось, оставляем номер урока нулевым
                    // а дату ставим просто из полученного календаря
                    ContentValues values = new ContentValues();
                    values.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE, dateFormat.format(beginDate.getTime()));
                    db.update(
                            SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE,
                            values,
                            SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID + " = " +
                                    cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID)),
                            null
                    );


                }

                cursor.close();


                // -- добавляем оценкам эти поля --
                db.execSQL("ALTER TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES +
                        " ADD COLUMN " + SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + " INTEGER DEFAULT 0 NOT NULL;"
                );
                db.execSQL("ALTER TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES +
                        " ADD COLUMN " + SchoolContract.TableLearnersGrades.COLUMN_DATE + " TIMESTRING DEFAULT \"0000-00-00\" NOT NULL;"
                );

                // заполняем поля даты и номера урока в оценках

                // получаем все оценки
                Cursor gradesCursor = db.query(
                        SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                // пробегаемся по оценкам
                while (gradesCursor.moveToNext()) {

                    // получаем время этого урока
                    GregorianCalendar date = new GregorianCalendar();

                    try {
                        date.setTime(timeDateFormat.parse(
                                gradesCursor.getString(gradesCursor.getColumnIndex("time"))
                        ));
                    } catch (Exception e) {
                        e.printStackTrace();
                        // в случае ошибок дату берем текущую
                        date.setTime(new Date());
                    }


                    // сравниваем полученное врекмя, с записанным в настройках
                    for (int i = 0; i < time.length; i++) {

                        if (time[i][0] == date.get(Calendar.HOUR_OF_DAY) &&
                                time[i][1] == date.get(Calendar.MINUTE)
                        ) {
                            ContentValues values = new ContentValues();
                            values.put(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER, i);
                            db.update(
                                    SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                    values,
                                    SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " = " +
                                            gradesCursor.getLong(gradesCursor.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                    null
                            );
                            break;
                        }
                    }

                    // если вдруг время не нашлось, оставляем номер урока нулевым
                    // а дату ставим просто из полученного календаря
                    ContentValues values = new ContentValues();
                    values.put(
                            SchoolContract.TableLearnersGrades.COLUMN_DATE,
                            dateFormat.format(date.getTime())
                    );
                    db.update(
                            SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                            values,
                            SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " = " +
                                    gradesCursor.getLong(gradesCursor.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                            null
                    );
                }

                gradesCursor.close();


            }

            if (oldVersion < 18) {
                SimpleDateFormat timeDateFormat = new SimpleDateFormat(SchoolContract.DECODING_TIMES_STRING);
                // меняем в статистике

                // -- добавляем статистике эти поля --
                db.execSQL("ALTER TABLE " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES +
                        " ADD COLUMN " + SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE + " TIMESTRING DEFAULT \"1000-00-00\" NOT NULL;"
                );
                db.execSQL("ALTER TABLE " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES +
                        " ADD COLUMN " + SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE + " TIMESTRING DEFAULT \"1000-00-00\" NOT NULL;"
                );


                // получаем все статистики
                Cursor statCursor = db.query(
                        SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );


                // пробегаемся по статистикам
                while (statCursor.moveToNext()) {

                    // -- начальная дата --

                    // проверяем строку
                    String startDate = statCursor.getString(statCursor.getColumnIndex("startTime"));
                    try {
                        timeDateFormat.parse(startDate);
                        // если ошибок нет
                        startDate = startDate.substring(0, 10);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        // если дата неправильная, берем по умолчанию
                        startDate = "1000-00-00";

                    }

                    // сохраняем полученное в бд
                    ContentValues values = new ContentValues();
                    values.put(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE, startDate);
                    db.update(
                            SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                            values,
                            SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID + " = " +
                                    statCursor.getLong(statCursor.getColumnIndex(SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID)),
                            null
                    );


                    // -- конечная дата --

                    // проверяем строку
                    String endDate = statCursor.getString(statCursor.getColumnIndex("endTime"));
                    try {
                        timeDateFormat.parse(endDate);
                        // если ошибок нет
                        endDate = endDate.substring(0, 10);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        // если дата неправильная, берем по умолчанию
                        endDate = "1000-00-00";
                    }

                    // сохраняем полученное в бд
                    ContentValues valuesEnd = new ContentValues();
                    valuesEnd.put(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE, endDate);
                    db.update(
                            SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                            valuesEnd,
                            SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID + " = " +
                                    statCursor.getLong(statCursor.getColumnIndex(SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID)),
                            null
                    );

                }
                statCursor.close();
            }

            if (oldVersion < 19) {
                // добавляем типы пропусков
                db.execSQL(
                        "CREATE TABLE " + SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES +
                                " ( " + SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME + " TEXT NOT NULL, " +
                                SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME + " TEXT NOT NULL);"
                );
                {// ---- вставляем три записи "н", "бол.", "спр."  ----  <-- запись "н" нельзя удалить но можно переименовать
                    ContentValues values1 = new ContentValues();
                    values1.put(SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID, 1);
                    values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
                            context.getResources().getString(R.string.db_table_grades_abs_1_default_value));
                    values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
                            context.getResources().getString(R.string.db_table_grades_abs_1_long_default_value));
                    db.insert(
                            SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                            null,
                            values1);

                    ContentValues values2 = new ContentValues();
                    values2.put(SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID, 2);
                    values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
                            context.getResources().getString(R.string.db_table_grades_abs_2_default_value));
                    values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
                            context.getResources().getString(R.string.db_table_grades_abs_2_long_default_value));
                    db.insert(
                            SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                            null,
                            values2);
                }

                // изменяем поля оценок


                /*
                 * переделываем старую таблицу оценок в специальную таблицу оценок
                 * переименовываем старые поля COLUMNS_GRADE_NAMES[0] KEYS_GRADES_TITLES_ID[0]
                 * добавляем новые поля
                 *
                 * делаем запрос в бд, получая все оценки
                 * группируем оценки по дням, по урокам, по предметам и по ученикам
                 *
                 * переписываем все оценки и заголовки к первой оценке, и удаляем все кроме первой в этот урок
                 * если при этом заголовки оценок не проставлены, ставим по умолчанию
                 *
                 * если стояла н удаляем все остальные оценки
                 * (которых на самом деле и не должно быть)
                 * и записываем как пропуск первого типа
                 *
                 *
                 *
                 *
                 * */

                // --- переделываем таблицу ---
                db.execSQL("PRAGMA foreign_keys = OFF");
                // переделываем старую таблицу оценок в специальную таблицу оценок
                // переименовываем старые колонки
//                db.execSQL("ALTER TABLE learnersGrades RENAME COLUMN grade to grade1;"
//                );
//                db.execSQL("ALTER TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES +
//                        " RENAME COLUMN titleId TO " + SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0] + ";"
//                );

                // переименовываем старую таблицу
                db.execSQL("ALTER TABLE " +
                        SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES +
                        " RENAME TO learnersGrades_old;");
                // создаём новую таблицу
                db.execSQL(SchoolContract.TableLearnersGrades.CREATE_TABLE_STRING);
                //переносим значения
                db.execSQL("INSERT INTO " +
                        SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES +
                        " SELECT " +
                        SchoolContract.TableLearnersGrades.KEY_GRADE_ID + ", " +
                        SchoolContract.TableLearnersGrades.COLUMN_DATE + ", " +
                        SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + ", " +
                        "grade , " +
                        "0, " +
                        "0, " +
                        "titleId, " +
                        "1, " +
                        "1, " +
                        "null, " +
                        SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + ", " +
                        SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID +
                        " FROM learnersGrades_old;");// с дополнительной колонкой и значением "1" и null
                //удаляем старую таблицу
                db.execSQL("DROP TABLE IF EXISTS learnersGrades_old;");
                // закончили переделку
                db.execSQL("PRAGMA foreign_keys = ON");


                // делаем запрос в бд, получая все оценки
                Cursor allGrades = db.query(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null, null, null, null, null, null);

                // группируем оценки по дням, по урокам, по предметам и по ученикам
                while (allGrades.moveToNext()) {//-----
                    // ищем оценки которые имеют тот же день, урок, предмет и ученика
                    Cursor sameGrades = db.query(
                            SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                            null,
                            SchoolContract.TableLearnersGrades.COLUMN_DATE + " == \"" + allGrades.getString(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_DATE)) + "\" AND " +
                                    SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + " == " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER)) + " AND " +
                                    SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID + " == " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID)) + " AND " +
                                    SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " == " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID)) + " AND " +
                                    SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " != " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                            null, null, null, null
                    );

                    // Если стоит пропуск
                    if (allGrades.getInt(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0])) == -2) {
                        // выставляем первый тип пропуска и пустую оценку
                        ContentValues content = new ContentValues();
                        content.put(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, 1);
                        content.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], 0);
                        db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                content,
                                SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                null
                        );

                        // удаляем все остальные оценки (которых на самом деле и не должно быть)
                        while (sameGrades.moveToNext()) {
                            db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                    SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                    null
                            );
                        }

                        // сканируем еще раз
                        allGrades.close();
                        allGrades = db.query(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null, null, null, null, null, null);

                    } else {
                        // если с одинаковыми параметрами есть несколько оценок
                        if (sameGrades.getCount() > 0) {

                            // переписываем 2 оценки и заголовки к первой оценке, и удаляем все кроме первой в этот урок

                            sameGrades.moveToNext();

                            long grade = sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0]));
                            ContentValues content = new ContentValues();
                            if (grade == -2) {

                                // выставляем первый тип пропуска и пустую оценку
                                content.put(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, 1);
                                content.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], 0);
                                content.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], 1);
                                db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                        content,
                                        SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                        null
                                );


                                // удаляем все остальные оценки (которых на самом деле и не должно быть)
                                while (sameGrades.moveToNext()) {
                                    db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                            SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                            null
                                    );
                                }

                            } else {
                                // ставим на вторую позицию оценку из первой временной
                                content.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[1],
                                        sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0]))
                                );
                                content.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1],
                                        sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0]))
                                );
                                db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                        content,
                                        SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                        null
                                );
                                // удаляем временную
                                db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                        SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                        null
                                );

                                // переходим к проверке второй временной оценки
                                if (sameGrades.moveToNext()) {
                                    grade = sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0]));
                                    ContentValues content2 = new ContentValues();
                                    if (grade == -2) {

                                        // выставляем первый тип пропуска и пустую оценку
                                        content2.put(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, 1);
                                        content2.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], 0);
                                        content2.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[1], 0);
                                        content2.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], 1);
                                        content2.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1], 1);
                                        db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                                content2,
                                                SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                                null
                                        );


                                        // удаляем все остальные оценки (которых на самом деле и не должно быть)
                                        while (sameGrades.moveToNext()) {
                                            db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                                    SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                                    null
                                            );
                                        }

                                    } else {
                                        // ставим на третью позицию оценку из первой временной
                                        content2.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[2],
                                                sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0]))
                                        );
                                        content2.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2],
                                                sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0]))
                                        );
                                        db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                                content2,
                                                SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + allGrades.getLong(allGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                                null
                                        );
                                        // удаляем временную
                                        db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                                SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)),
                                                null
                                        );
                                    }
                                }
                            }


                            // сканируем еще раз
                            allGrades.close();
                            allGrades = db.query(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null, null, null, null, null, null);
                        }
                    }
                    sameGrades.close();
                }
                allGrades.close();

                // комментарий к уроку
                db.execSQL(
                        "CREATE TABLE " + SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT + " ( " +
                                SchoolContract.TableLessonComment.KEY_LESSON_TEXT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                SchoolContract.TableLessonComment.KEY_LESSON_ID + " INTEGER NOT NULL, " +
                                SchoolContract.TableLessonComment.COLUMN_LESSON_DATE + " TIMESTRING NOT NULL, " +
                                SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT + " TEXT NOT NULL, " +
                                "FOREIGN KEY(" + SchoolContract.TableLessonComment.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE + " (" + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID + ") ON DELETE CASCADE);"
                );

            }

            if (oldVersion < 20) {
                // добавляем комментарий ученику
                db.execSQL("ALTER TABLE " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS +
                        " ADD COLUMN " + SchoolContract.TableLearners.COLUMN_COMMENT + " VARCHAR;");
            }
        }
    }

//----------------------------------------методы доступа--------------------------------------------

    //настройки
    public long createNewSettingsProfileWithId1(String profileName, int interfaceSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID, 1);
        values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, profileName);
        values.put(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, interfaceSize);
        long temp = db.insert(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createSettingsProfile returnId = " + temp + " profileName= " + profileName + " interfaceSize= " + interfaceSize);
        return temp;
    }

//    public long createNewSettingsProfile(String profileName, int interfaceSize) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, profileName);
//        values.put(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, interfaceSize);
//        long temp = db.insert(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, values);//-1 = ошибка ввода
//        if (IS_DEBUG)
//            Log.i(LOG_TAG, "createSettingsProfile returnId = " + temp + " profileName= " + profileName + " interfaceSize= " + interfaceSize);
//        return temp;
//    }
//
//    public int setSettingsProfileParameters(long profileId, String profileName, int interfaceSize) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, profileName);
//        values.put(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, interfaceSize);
//        int temp = db.update(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, values, SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = ?", new String[]{"" + profileId});
//        if (IS_DEBUG)
//            Log.i(LOG_TAG, "setSettingsProfileParameters return = " + temp + " profileId= " + profileId + " profileName= " + profileName + " interfaceSize= " + interfaceSize);
//        return temp;
//    }

    public int setSettingsLocale(long profileId, String locale) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSettingsData.COLUMN_LOCALE, locale);
        int temp = db.update(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, values,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + profileId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsProfileParameters return = " + temp + " profileId= " + profileId + " locale= " + locale);
        return temp;
    }

    public String getSettingsLocale(long profileId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + profileId,
                null, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return SchoolContract.TableSettingsData.COLUMN_LOCALE_DEFAULT_CODE;
        }
        cursor.moveToFirst();
        String answer = cursor.getString(cursor.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_LOCALE));
        cursor.close();
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsProfileParameters return = " + answer + " profileId= " + profileId);
        return answer;
    }

    public int setSettingsMaxGrade(long profileId, int maxGrade) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER, maxGrade);
        int temp = db.update(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, values,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + profileId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsProfileParameters return = " + temp + " profileId= " + profileId + " maxGrade= " + maxGrade);
        return temp;
    }

    public int getSettingsMaxGrade(long profileId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + profileId,
                null, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return -1;
        }
        cursor.moveToFirst();
        int answer = cursor.getInt(cursor.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER));
        cursor.close();
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsProfileParameters return = " + answer + " profileId= " + profileId);
        return answer;
    }

    public int setSettingsTime(long profileId, int[][] time) {// metod param {{hh,mm,hh,mm},{hh,mm,hh,mm},...} todo че за жесть, используй готовую библиотеку gson
        SQLiteDatabase db = this.getWritableDatabase();

        // переделываем расписание
        // парсим в json для проверки
        StringBuilder gsonString = new StringBuilder();
        for (int fieldI = 0; fieldI < 4; fieldI++) {
            switch (fieldI) {
                case 0:// часы начала
                    gsonString.append("{\"").append(SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_HOUR_NAME).append("\":[\"");
                    break;
                case 1:// минуты начала
                    gsonString.append("\"], \"").append(SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_MINUTE_NAME).append("\":[\"");
                    break;
                case 2:// часы конца
                    gsonString.append("\"], \"").append(SchoolContract.TableSettingsData.COLUMN_TIME_END_HOUR_NAME).append("\":[\"");
                    break;
                case 3:// минуты конца
                    gsonString.append("\"], \"").append(SchoolContract.TableSettingsData.COLUMN_TIME_END_MINUTE_NAME).append("\":[\"");
                    break;
            }
            for (int lessonI = 0; lessonI < time.length; lessonI++) {
                gsonString.append(time[lessonI][fieldI]);
                if (lessonI != time.length - 1) gsonString.append("\",\"");
            }
        }
        gsonString.append("\"]}");

        // обьект json
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(gsonString.toString());
        } catch (JSONException e) {
            Log.e("TeachersApp", "setSettingsTime-error-JSONParse", e);
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSettingsData.COLUMN_TIME, jsonObject.toString());
        int temp = db.update(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, values,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + profileId, null);

        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsTime return = " + temp + " profileId= " + profileId + " gsonString= " + gsonString.toString());
        return temp;
    }

    public int[][] getSettingsTime(long profileId) {//return урок [[hh],[mm],[hh],[mm]] , [[hh],[mm],[hh],[mm]], ...
        SQLiteDatabase db = this.getReadableDatabase();
        // получаем сырые данные из бд
        Cursor settingsCursor = db.query(
                SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS,
                null,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + profileId,
                null, null, null, null
        );

        // если в бд пусто возвращаем null
        if (settingsCursor.getCount() == 0) {
            settingsCursor.close();
            return null;
        }
        // если провиль настроек найден, переходим к нему
        settingsCursor.moveToFirst();

        // создаем временные обьекты для хранения
        ArrayList<int[]> answerList = new ArrayList<>();
        JSONObject json;
        JSONArray beginHour;
        JSONArray beginMinute;
        JSONArray endHour;
        JSONArray endMinute;
        // пытаемся прочитать json
        try {
            json = new JSONObject(settingsCursor.getString(settingsCursor.getColumnIndex(
                    SchoolContract.TableSettingsData.COLUMN_TIME)));
            // получаем нужные массивы
            beginHour = json.getJSONArray(SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_HOUR_NAME);
            beginMinute = json.getJSONArray(SchoolContract.TableSettingsData.COLUMN_TIME_BEGIN_MINUTE_NAME);
            endHour = json.getJSONArray(SchoolContract.TableSettingsData.COLUMN_TIME_END_HOUR_NAME);
            endMinute = json.getJSONArray(SchoolContract.TableSettingsData.COLUMN_TIME_END_MINUTE_NAME);
            // пишем их в лист
            for (int i = 0; i < beginHour.length(); i++) {
                int[] temp = new int[4];
                temp[0] = beginHour.getInt(i);
                temp[1] = beginMinute.getInt(i);
                temp[2] = endHour.getInt(i);
                temp[3] = endMinute.getInt(i);
                answerList.add(temp);
            }
        } catch (JSONException e) {
            if (IS_DEBUG) Log.e("TeachersApp", "getSettingsTime-error-JSONParse", e);
            return null;
        }
        settingsCursor.close();

        // преобразуем в масиив и отправляем
        int[][] answer = new int[answerList.size()][4];
        answerList.toArray(answer);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getSettingsTime return = " + Arrays.toString(answer) + " profileId= " + profileId);
        return answer;
    }

//    public int getLessonNumberByTime(GregorianCalendar calendarTime) {
//        int[][] times = getSettingsTime(1);
//        int answer = -1;
//
//
//        // определяем текущий урок
//        for (int lessonI = 0; lessonI < times.length; lessonI++) {
//            if ((calendarTime.get(Calendar.HOUR_OF_DAY) > times[lessonI][0] ||
//                    (calendarTime.get(Calendar.HOUR_OF_DAY) == times[lessonI][0] && calendarTime.get(Calendar.MINUTE) >= times[lessonI][1])
//            ) &&
//                    (calendarTime.get(Calendar.HOUR_OF_DAY) < times[lessonI][2] ||
//                            (calendarTime.get(Calendar.HOUR_OF_DAY) == times[lessonI][2] && calendarTime.get(Calendar.MINUTE) <= times[lessonI][3])
//                    )
//            ) {
//                answer = lessonI;
//            }
//        }
//
//        return answer;
//    }
//
//    public long getInterfaceSizeBySettingsProfileId(long profileId) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String[] selectionArgs = {profileId + ""};
//        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = ?", selectionArgs, null, null, null);
//        long answer;
//        if (cursor.getCount() != 0) {
//            cursor.moveToFirst();
//            answer = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE));
//        } else {
//            answer = -1;
//        }
//        if (IS_DEBUG)
//            Log.i(LOG_TAG, "getInterfaceSizeBySettingsProfileId profileId=" + profileId + " return=" + answer);
//        cursor.close();
//        return answer;
//    }

    public long setSettingsAreTheGradesColoredByProfileId(long profileId, boolean areColored) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (areColored) {
            values.put(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED, 1);
        } else {
            values.put(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED, 0);
        }
        int temp = db.update(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, values,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + profileId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsAreTheGradesColoredByProfileId return = " + temp + " profileId= " + profileId + " areColored= " + areColored);

        return temp;
    }

    public boolean getSettingsAreTheGradesColoredByProfileId(long profileId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + profileId,
                null, null, null, null);
        boolean answer = false;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            if (cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED)) != 0) {
                answer = true;
            }
        }
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getSettingsAreTheGradesColoredByProfileId profileId=" + profileId + " return=" + answer);
        cursor.close();
        return answer;
    }

    public Cursor getSettingProfileById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null,
                SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = " + id,
                null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getInterfaceSizeBySettingsProfileId profileId=" + id + " return=" + cursor);
        return cursor;
    }

    // статистика
    public Cursor getAllStatisticsProfiles() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES + " ORDER BY " + SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME + " ASC;", null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getAllStatistics return=" + cursor);

        return cursor;
    }

    public long createStatistic(String name, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME, name);
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE, startDate);
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE, endDate);
        long temp = db.insert(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createStatisticProfile returnId = " + temp + " name= " + name + " startDate= " + startDate + " endDate= " + endDate);
        return temp;
    }

//    public void setStatisticParameters(long id, String newName, String startDate, String endDate) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME, newName);
//        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE, startDate);
//        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE, endDate);
//        int temp = db.update(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES, values, SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID + " = ?", new String[]{"" + id});//-1 = ошибка ввода
//        db.close();
//        if (IS_DEBUG)
//            Log.i(LOG_TAG, "setStatisticParameters id=" + id + " newName=" + newName + " startDate=" + startDate + " endDate=" + endDate + " return=" + temp);
//    }

    public void setStatisticTime(long id, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE, startDate);
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE, endDate);
        int temp = db.update(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                values, SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID + " = " + id, null);
        db.close();
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setStatisticTime id=" + id + " startDate=" + startDate + " endDate=" + endDate + " return=" + temp);
    }

    public void setStatisticName(long periodId, String periodName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME, periodName);
        int temp = db.update(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES, values,
                SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID + " = " + periodId, null);
        db.close();
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setStatisticParameters id=" + periodId + " periodName=" + periodName + " return=" + temp);
    }

    public int removeStatisticProfile(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int temp = db.delete(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                SchoolContract.TableStatisticsProfiles.KEY_STATISTICS_PROFILE_ID + " = " + id, null);
        db.close();
        if (IS_DEBUG)
            Log.i(LOG_TAG, "removeStatisticProfile id=" + id + " return=" + temp);
        return temp;//-1 = ошибка ввода
    }

    //класс
    public long createClass(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableClasses.COLUMN_CLASS_NAME, name);
        long temp = db.insert(SchoolContract.TableClasses.NAME_TABLE_CLASSES, null, values);//-1 = ошибка ввода
        if (IS_DEBUG) Log.i(LOG_TAG, "createClass returnId = " + temp + " name= " + name);
        return temp;
    }

    public Cursor getLearnersClases() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableClasses.NAME_TABLE_CLASSES, null, null, null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getLearnersClass " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getLearnersClases(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableClasses.NAME_TABLE_CLASSES, null,
                SchoolContract.TableClasses.KEY_CLASS_ID + " = " + classId,
                null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getLearnersClass " + cursor + "id=" + classId + "number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public int setClassName(long classId, String name) {
        // todo сортировка курсора
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableClasses.COLUMN_CLASS_NAME, name);
        int answer = db.update(SchoolContract.TableClasses.NAME_TABLE_CLASSES, contentName,
                SchoolContract.TableClasses.KEY_CLASS_ID + " = " + classId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setClassesNames name = " + name + " id= " + classId + " return = " + answer);
        return answer;
    }

    public int deleteClass(long classesId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableClasses.NAME_TABLE_CLASSES,
                SchoolContract.TableClasses.KEY_CLASS_ID + " = " + classesId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteClasses id= " + classesId + " return = " + answer);
        return answer;
    }


    // ученик
    public long createLearner(String secondName, String name, String comment, long class_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearners.COLUMN_SECOND_NAME, secondName);
        values.put(SchoolContract.TableLearners.COLUMN_FIRST_NAME, name);
        values.put(SchoolContract.TableLearners.COLUMN_COMMENT, comment);
        values.put(SchoolContract.TableLearners.KEY_CLASS_ID, class_id);
        long temp = db.insert(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, values);//-1 = ошибка ввода
        if (IS_DEBUG) Log.i(LOG_TAG, "createLearner returnId= " + temp + " " + values.toString());
        return temp;
    }

    public Cursor getLearnersByClassId(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " +
                SchoolContract.TableLearners.NAME_TABLE_LEARNERS +
                " WHERE (" + SchoolContract.TableLearners.KEY_CLASS_ID + "= " + classId + ") ORDER BY " +
                SchoolContract.TableLearners.COLUMN_SECOND_NAME + " ASC, " +
                SchoolContract.TableLearners.COLUMN_FIRST_NAME + " ASC;", null);
        if (IS_DEBUG) Log.i(LOG_TAG, "getLearnersByClassId classId=" + classId +
                " number=" + cursor.getCount() +
                " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getLearner(long learnerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null,
                SchoolContract.TableLearners.KEY_LEARNER_ID + " = " + learnerId, null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getLearner learnerId=" + learnerId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public ArrayList<Long> getNotPutLearnersIdByCabinetIdAndClassId(long cabinetId, long classId) {
        ArrayList<Long> placesId = new ArrayList<>();
        {//получаем id мест
            ArrayList<Long> desksId = new ArrayList<>();
            {
                Cursor desksCursor = this.getDesksByCabinetId(cabinetId);
                while (desksCursor.moveToNext()) {
                    desksId.add(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
                }
                desksCursor.close();
            }

            for (int i = 0; i < desksId.size(); i++) {
                Cursor placeCursor = this.getPlacesByDeskId(desksId.get(i));
                while (placeCursor.moveToNext()) {
                    placesId.add(placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));
                }
                placeCursor.close();
            }
        }
        ArrayList<Long> learnersId = new ArrayList<>();
        {//получаем id
            Cursor learnersCursor = this.getLearnersByClassId(classId);
            while (learnersCursor.moveToNext()) {
                learnersId.add(learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
            }
            learnersCursor.close();
        }
        ArrayList<Long> answer = new ArrayList<>();
        for (int i = 0; i < learnersId.size(); i++) {//пробегаемся по ученикам
            boolean flag = false;
            for (int j = 0; j < placesId.size(); j++) {//пробегаемся с учеником по местам
                Cursor cursor = this.getAttitudeByLearnerIdAndPlaceId(learnersId.get(i), placesId.get(j));
                if (cursor.getCount() != 0) {//если есть совпадение ученик - место
                    flag = true;
                }
                cursor.close();
            }
            if (!flag) {//если совпадений нет, то,
                answer.add(learnersId.get(i));//запоминаем не рассаженного ученика
            }
        }
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getNotPutLearnersIdByCabinetIdAndClassId cabinetId=" + cabinetId + " classId=" + classId + " return=" + answer);
        return answer;
    }

    public int setLearnerNameAndLastName(long learnerId, String name, String lastName, String comment) {// todo нужен ли комментарий здесь или вынсти его в отдельный метод
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableLearners.COLUMN_FIRST_NAME, name);
        contentName.put(SchoolContract.TableLearners.COLUMN_SECOND_NAME, lastName);
        contentName.put(SchoolContract.TableLearners.COLUMN_COMMENT, comment);
        int answer = db.update(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, contentName,
                SchoolContract.TableLearners.KEY_LEARNER_ID + " = " + learnerId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setLearnerNameAndLastName name= " + name + " lastName= " + lastName + " id= " + learnerId + " return = " + answer);
        return answer;
    }

    public int deleteLearner(long learnerId) {// метод удаления ученика
        SQLiteDatabase db = this.getWritableDatabase();
        // удаляем ученика
        int answer = db.delete(SchoolContract.TableLearners.NAME_TABLE_LEARNERS,
                SchoolContract.TableLearners.KEY_LEARNER_ID + " = " + learnerId, null);
        // выводим лог
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteLearner id= " + learnerId + " return = " + answer);
        return answer;
    }


    //кабинет
    public long createCabinet(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableCabinets.COLUMN_NAME, name);
        values.put(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER, 15);
        long temp = db.insert(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, null, values);//-1 = ошибка ввода
        if (IS_DEBUG) Log.i(LOG_TAG, "createCabinet returnId = " + temp + " name= " + name);
        return temp;
    }

    public Cursor getCabinets() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, null, null, null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getCabinets " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getCabinet(long cabinetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, null,
                SchoolContract.TableCabinets.KEY_CABINET_ID + " = " + cabinetId, null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getCabinets " + cursor + "id=" + cabinetId +
                    " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public void setCabinetMultiplierOffsetXOffsetY(long cabinetId, int multiplier, int offsetX, int offsetY) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER, multiplier);
        content.put(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X, offsetX);
        content.put(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y, offsetY);

        db.update(
                SchoolContract.TableCabinets.NAME_TABLE_CABINETS,
                content,
                SchoolContract.TableCabinets.KEY_CABINET_ID + " = " + cabinetId,
                null
        );

        if (IS_DEBUG)
            Log.i(LOG_TAG,
                    "setCabinetMultiplierOffsetXOffsetY multiplier= " + multiplier +
                            "offsetX= " + offsetX +
                            "offsetY= " + offsetY +
                            " cabinetId= " + cabinetId
            );
    }

    public int setCabinetName(Long cabinetId, String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableCabinets.COLUMN_NAME, name);
        int answer = db.update(SchoolContract.TableCabinets.NAME_TABLE_CABINETS,
                contentName, SchoolContract.TableCabinets.KEY_CABINET_ID + " = " + cabinetId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setCabinetName name= " + name + " id= " + cabinetId + " return = " + answer);
        return answer;
    }

    public int deleteCabinets(Long cabinetsId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableCabinets.NAME_TABLE_CABINETS,
                SchoolContract.TableCabinets.KEY_CABINET_ID + " = " + cabinetsId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteCabinets id= " + cabinetsId + " return = " + answer);
        return answer;
    }


    //парта
    public long createDesk(int numberOfPlaces, int x, int y, long cabinet_id) {//исправил--- при создании парты должны создаваться места, и может возвращать всё одним обьектом? Или в главном методе устроить алгоритм, создания мест в цикле и сохранения их в массив.
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES, numberOfPlaces);
        values.put(SchoolContract.TableDesks.COLUMN_X, x);
        values.put(SchoolContract.TableDesks.COLUMN_Y, y);
        values.put(SchoolContract.TableDesks.KEY_CABINET_ID, cabinet_id);
        long temp = db.insert(SchoolContract.TableDesks.NAME_TABLE_DESKS, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createDesk returnId = " + temp + " numberOfPlaces= " + numberOfPlaces + " x= " + x + " y= " + y + " cabinet_id= " + cabinet_id);
        return temp;
    }

    public long setDeskCoordinates(long deskId, long x, long y) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues content = new ContentValues();
        content.put(SchoolContract.TableDesks.COLUMN_X, x);
        content.put(SchoolContract.TableDesks.COLUMN_Y, y);
        int answer = db.update(SchoolContract.TableDesks.NAME_TABLE_DESKS, content,
                SchoolContract.TableDesks.KEY_DESK_ID + " = " + deskId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setDeskCoordinates id= " + deskId + " x= " + x + " y= " + y + " return = " + answer);
        return answer;
    }

    public Cursor getDesks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableDesks.NAME_TABLE_DESKS, null, null, null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getDesks" + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getDesksByCabinetId(long cabinetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                SchoolContract.TableDesks.NAME_TABLE_DESKS,
                null, SchoolContract.TableDesks.KEY_CABINET_ID + " = " + cabinetId, null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getDesksByCabinetId cabinetId=" + cabinetId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public int deleteDesk(long deskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableDesks.NAME_TABLE_DESKS,
                SchoolContract.TableDesks.KEY_DESK_ID + " = " + deskId, null);
        if (IS_DEBUG) Log.i(LOG_TAG, "deleteDesk id= " + deskId + " return = " + answer);
        return answer;
    }


    //место
    public long createPlace(long deskId, long ordinal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TablePlaces.KEY_DESK_ID, deskId);
        values.put(SchoolContract.TablePlaces.COLUMN_ORDINAL, ordinal);
        long temp = db.insert(SchoolContract.TablePlaces.NAME_TABLE_PLACES, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createPlace returnId = " + temp + " desk_id= " + deskId);
        return temp;
    }

    public Cursor getPlacesByDeskId(long deskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TablePlaces.NAME_TABLE_PLACES, null, SchoolContract.TablePlaces.KEY_DESK_ID + " =?", new String[]{deskId + ""}, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getPlacesByDeskId deskId=" + deskId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }


    // ученик-место
    public long setLearnerOnPlace(long learnerId, long placeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID, learnerId);
        values.put(SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID, placeId);
        long temp = db.insert(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setLearnerOnPlace returnId = " + temp + " learnerId= " + learnerId + " placeId= " + placeId);
        return temp;
    }

    public Cursor getAttitudeByLearnerIdAndPlaceId(Long learnerId, Long placeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES,
                null, SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " = " + learnerId + " AND " +
                        SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " = " + placeId,
                null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getAttitudeByLearnerIdAndPlaceId learnerId=" + learnerId + " placeId=" + placeId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames())
            );
        return cursor;
    }

    public Cursor getAttitudesByPlaceId(Long placeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES,
                null, SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " = " + placeId,
                null, null, null, null);
    }

    public long deleteLearnerAndPlaceAttitudeById(long attitudeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long temp = db.delete(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES,
                SchoolContract.TableLearnersOnPlaces.KEY_ATTITUDE_ID + " = " + attitudeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteLearnerAndPlaceAttitudeById returnId = " + temp + " attitudeId= " + attitudeId);
        return temp;//-1 = ошибка ввода
    }

    public long deleteAttitudeByLearnerIdAndPlaceId(long learnerId, long placeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES,
                SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " = " + learnerId + " AND " +
                        SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " = " + placeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteAttitudeByLearnerIdAndPlaceId learnerId=" + learnerId + " placeId=" + placeId + " return = " + answer);
        return answer;
    }


    //оценки учеников
    public long createGrade(long learnerId, long grade1, long grade2, long grade3,
                            long typeId1, long typeId2, long typeId3, long absTypeId,
                            long subjectId, String date, long lessonNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID, learnerId);
        values.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], grade1);
        values.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[1], grade2);
        values.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[2], grade3);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], typeId1);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1], typeId2);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2], typeId3);
        if (absTypeId != -1)// если пропуска нет (-1), передаем просто null
            values.put(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, absTypeId);
        values.put(SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID, subjectId);
        values.put(SchoolContract.TableLearnersGrades.COLUMN_DATE, date);
        values.put(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER, lessonNumber);
        long temp = db.insert(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createGrade returnId = " + temp + " learnerId= " + learnerId +
                    " grade1= " + grade1 + " grade2= " + grade2 + " grade3= " + grade3 +
                    " typeId1= " + typeId1 + " typeId2= " + typeId2 + " typeId3= " + typeId3 +
                    " subjectId= " + subjectId + " date= " + date);
        return temp;
    }

    public Cursor getGradeById(long gradeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null,
                SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " = " + gradeId,
                null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getGrade gradeId=" + gradeId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getGradesByLearnerIdSubjectDate(long learnerId, long subjectId, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        //запрашиваем оценки ученика по уроку находящиеся между startTime и endTime
        return db.query(
                SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                null,
                SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " = " + learnerId + " AND " +
                        SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID + " = " + subjectId + " AND (" +
                        SchoolContract.TableLearnersGrades.COLUMN_DATE + " BETWEEN \"" + startDate + "\" AND  \"" + endDate + "\")",
                null,
                null,
                null,
                null
        );
    }

    public Cursor getGradesByLearnerIdSubjectDateAndLessonsPeriod(long learnerId, long subjectId, String date, int startLessonNumber, int endLessonNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        // запрашиваем оценки ученика по уроку и дате
        return db.query(
                SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                null,

                SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " = " + learnerId + " AND " +
                        SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID + " = " + subjectId + " AND " +
                        SchoolContract.TableLearnersGrades.COLUMN_DATE + " = \"" + date + "\" AND " +
                        SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + " >= \"" + startLessonNumber + "\" AND " +
                        SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + " <= \"" + endLessonNumber + "\""
                //null
                ,
                null,
                null,
                null,
                null
        );
    }

    public Cursor getGradesBySubjectDateAndLesson(long subjectId, String date, int lessonNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        // запрашиваем оценки ученика по уроку и дате
        return db.query(
                SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                null,
                SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID + " = " + subjectId + " AND " +
                        SchoolContract.TableLearnersGrades.COLUMN_DATE + " = \"" + date + "\" AND " +
                        SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + " = \"" + lessonNumber + "\"",
                null,
                null,
                null,
                null
        );
    }

    public Cursor getGradesByLearnerIdSubjectDateAndLesson(long learnerId, long subjectId, String date, int lessonNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        // запрашиваем оценки ученика по уроку и дате
        return db.query(
                SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                null,
                SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " = " + learnerId + " AND " +
                        SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID + " = " + subjectId + " AND " +
                        SchoolContract.TableLearnersGrades.COLUMN_DATE + " = \"" + date + "\" AND " +
                        SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + " = \"" + lessonNumber + "\"",
                null,
                null,
                null,
                null
        );
    }

    public long editGrade(long gradeId,
                          long grade1, long grade2, long grade3,
                          long gradeTypeId1, long gradeTypeId2, long gradeTypeId3,
                          long absTypeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], grade1);
        values.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[1], grade2);
        values.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[2], grade3);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], gradeTypeId1);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1], gradeTypeId2);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2], gradeTypeId3);
        if (absTypeId != -1)// если пропуска нет (-1), передаем просто null
            values.put(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, absTypeId);
        long temp = db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                values, SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " = " + gradeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editGrade returnId = " + temp + " grade1= " + grade1 + " grade2= " + grade2 + " grade3= " + grade3 + " gradeId= " + gradeId);
        return temp;//-1 = ошибка ввода
    }

    public long editGradeType(long gradeId, long type1, long type2, long type3) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], type1);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1], type2);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2], type3);
        long temp = db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                values, SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " = " + gradeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editGradeType returnId = " + temp + " type= " + type1 + " gradeId= " + gradeId);
        return temp;//-1 = ошибка ввода
    }

    public long removeGrade(long gradeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long temp = db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " = " + gradeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "removeGrade returnId = " + temp + " gradeId= " + gradeId);
        return temp;//-1 = ошибка ввода
    }


    // типы оценок
    public long createGradeType(String typeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, typeName);
        long temp = db.insert(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, null, values);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createGradeType returnId = " + temp + " typeName= " + typeName);
        return temp;//-1 = ошибка ввода
    }

    public Cursor getGradesTypes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, null, null, null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getGradesTypes " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public long editGradesType(long typeId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, newName);
        long temp = db.update(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, values, SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID + " = " + typeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editGradesType returnId = " + temp + " newName= " + newName + " typeId= " + typeId);
        return temp;//-1 = ошибка ввода
    }

    public long removeGradesType(long gradeTypeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // удаляем этот тип (при удалении тип во всех оценках автоматически изменяется на тип по умолчанию)
        long temp = db.delete(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, SchoolContract.TableLearnersGradesTitles.KEY_LEARNERS_GRADES_TITLE_ID + " = " + gradeTypeId, null);//-1 = ошибка ввода

        if (IS_DEBUG)
            Log.i(LOG_TAG, "removeGradesType returnId = " + temp + " gradeTypeId= " + gradeTypeId);
        return temp;
    }


    // типы пропусков
    public long createAbsentType(String absName, String absLongName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME, absName);
        values.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME, absLongName);
        long temp = db.insert(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, null, values);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createAbsentType returnId = " + temp + " absName= " + absName);
        return temp;
    }

    public Cursor getAbsentTypes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, null, null, null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getAbsentTypes " + cursor + " number=" + cursor.getCount());
        return cursor;
    }

    public long editAbsentType(long typeId, String newName, String absLongName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME, newName);
        values.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME, absLongName);
        long temp = db.update(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, values, SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID + " = " + typeId, null);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editAbsentType returnId = " + temp + " newName= " + newName + " typeId= " + typeId);
        return temp;
    }

    public long removeAbsentType(long absTypeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // удаляем этот тип (todo при удалении все пропуски с этим типом удалятся ???)
        long temp = db.delete(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, SchoolContract.TableLearnersAbsentTypes.KEY_LEARNERS_ABSENT_TYPE_ID + " = " + absTypeId, null);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "removeAbsentType returnId = " + temp + " absTypeId= " + absTypeId);
        return temp;
    }


    //уроки(предметы)
    public long createSubject(String name, long classId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSubjects.COLUMN_NAME, name);
        values.put(SchoolContract.TableSubjects.KEY_CLASS_ID, classId);
        long temp = db.insert(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createSubject returnId = " + temp + " name= " + name + " classId=" + classId);
        return temp;
    }


//    public int setSubjectParameters(long subjectId, String subjectName, long classId) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentName = new ContentValues();
//        contentName.put(SchoolContract.TableSubjects.COLUMN_NAME, subjectName);
//        contentName.put(SchoolContract.TableSubjects.KEY_CLASS_ID, classId);
//        int answer = db.update(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS, contentName, SchoolContract.TableSubjects.KEY_SUBJECT_ID + " = ?", new String[]{Long.toString(subjectId)});
//        return answer;
//    }

    public void setSubjectName(long subjectId, String subjectName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableSubjects.COLUMN_NAME, subjectName);
        db.update(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS, contentName, SchoolContract.TableSubjects.KEY_SUBJECT_ID + " = " + subjectId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSubjectName id=" + subjectId + " subjectName=" + subjectName);
    }

    public Cursor getSubjectById(long subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS, null,
                SchoolContract.TableSubjects.KEY_SUBJECT_ID + " = " + subjectId,
                null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getSubjectById " + cursor + " id=" + subjectId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getSubjectsByClassId(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + " WHERE (" + SchoolContract.TableSubjects.KEY_CLASS_ID + "= " + classId + ") ORDER BY " + SchoolContract.TableSubjects.COLUMN_NAME + " ASC;", null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getSubjectsByClassId " + cursor + " classId=" + classId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public int deleteSubjects(ArrayList<Long> subjectsId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = 0;
        StringBuilder stringSubjectsId = new StringBuilder();
        for (int i = 0; i < subjectsId.size(); i++) {
            stringSubjectsId.append(stringSubjectsId).append(subjectsId.get(i)).append('|');
            if (db.delete(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS,
                    SchoolContract.TableSubjects.KEY_SUBJECT_ID + " = " + subjectsId.get(i), null
            ) == 1)
                answer++;
        }
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteSubjects id= " + stringSubjectsId + " return = " + answer);
        return answer;
    }


    //урок и его время проведения
    public long createLessonTimeAndCabinetAttitude(long subjectId, long cabinetId, String date, long lessonNumber, long repeatPeriod) {
        // date и database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID, subjectId);
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID, cabinetId);
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE, date);
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER, lessonNumber);
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT, repeatPeriod);
        long answer = db.insert(SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE, null, contentValues);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setLessonTime subjectId= " + subjectId + " cabinetId= " + cabinetId + " lessonNumber= " + lessonNumber + " lessonNumber= " + lessonNumber + " repeatPeriod= " + repeatPeriod + " return = " + answer);
        return answer;
    }

    public int editLessonTimeAndCabinet(long attitudeId, long subjectId, long cabinetId, long lessonNumber, long repeatPeriod) {
        // date и database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID, subjectId);
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID, cabinetId);
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER, lessonNumber);
        contentValues.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT, repeatPeriod);
        int answer = db.update(SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE,
                contentValues,
                SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID + " = " + attitudeId,
                null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editLessonTimeAndCabinet attitudeId= " + attitudeId + ' ' + contentValues + " return = " + answer);
        return answer;
    }

    public Cursor getSubjectAndTimeCabinetAttitudeById(long attitudeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE,
                null, SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID + " = " + attitudeId,
                null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getSubjectAndTimeCabinetAttitudeById " + " id=" + attitudeId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

//    public Cursor getSubjectAndTimeCabinetAttitudesByDatePeriodAndLessonNumbersPeriod(String startDate, String endDate, long endLessonNumber) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.rawQuery("SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE +
//                        // по времени
//                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " <= ?) AND (" +
//                        // по дате
//                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " BETWEEN ? AND ?) OR ((" +
//                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
//                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?)))" +
//                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ?))" +
//                        ")",
//                new String[]{
//                        Long.toString(endLessonNumber),
//                        startDate,
//                        endDate,
//                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
//                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
//                        date,
//                        startDate
//                });
//
//        /*
//        * WHERE
//	    (
//	    	(COLUMN_LESSON_NUMBER <= endLessonNumber) AND (
//	    		(COLUMN_LESSON_DATE BETWEEN date AND date) OR (
//	    			(
//	    				(COLUMN_REPEAT = CONSTANT_REPEAT_DAILY) OR
//	    				(
//	    					(COLUMN_REPEAT = CONSTANT_REPEAT_WEEKLY) AND (strftime("%w",COLUMN_LESSON_DATE) == strftime("%w", date))
//	    				)
//	    			) AND COLUMN_LESSON_DATE < dateS
//	    		)
//	    	)
//	    )
//	    */
//    }

    public Cursor getSubjectAndTimeCabinetAttitudesByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE +
                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " = ?) OR ((" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?)))" +
                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ?))",
                new String[]{
                        date,
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
                        date,
                        date
                });

        /*
        * WHERE(
	    	(COLUMN_LESSON_DATE = date) OR (
	    		(
	    			(COLUMN_REPEAT = CONSTANT_REPEAT_DAILY) OR
	    			(
	    				(COLUMN_REPEAT = CONSTANT_REPEAT_WEEKLY) AND (strftime("%w",COLUMN_LESSON_DATE) == strftime("%w", date))
	    			)
	    		) AND COLUMN_LESSON_DATE < date
	    	)
	    )
	    */
    }


    public Cursor getSubjectAndTimeCabinetAttitudesByDateAndLessonNumbersPeriod(String date, long startLessonNumber, long endLessonNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE +
                        // по времени
                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " BETWEEN ? AND ?) AND (" +
                        // по дате
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " = ?) OR ((" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?)))" +
                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ?))" +
                        ")",
                new String[]{
                        Long.toString(startLessonNumber),
                        Long.toString(endLessonNumber),
                        date,
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
                        date,
                        date
                });

        /*
        * WHERE
	    (
	    	(COLUMN_LESSON_NUMBER BETWEEN startLessonNumber AND endLessonNumber) AND (
	    		(COLUMN_LESSON_DATE = date) OR (
	    			(
	    				(COLUMN_REPEAT = CONSTANT_REPEAT_DAILY) OR
	    				(
	    					(COLUMN_REPEAT = CONSTANT_REPEAT_WEEKLY) AND (strftime("%w",COLUMN_LESSON_DATE) == strftime("%w", date))
	    				)
	    			) AND COLUMN_LESSON_DATE < date
	    		)
	    	)
	    )
	    */
    }

    public Cursor getSubjectAndTimeCabinetAttitudesByDateAndLessonNumbersPeriod(long subjectId, String date, long startLessonNumber, long endLessonNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE +
                        // по времени
                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " BETWEEN ? AND ?) AND (" +
                        // по дате
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " = ?) OR ((" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?)))" +
                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ?))" +
                        " AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID + " = " + subjectId + ")",
                new String[]{
                        Long.toString(startLessonNumber),
                        Long.toString(endLessonNumber),
                        date,
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
                        date,
                        date
                });

        /*
        * WHERE
	    (
	    	(COLUMN_LESSON_NUMBER BETWEEN startLessonNumber AND endLessonNumber) AND (
	    		(COLUMN_LESSON_DATE = date) OR (
	    			(
	    				(COLUMN_REPEAT = CONSTANT_REPEAT_DAILY) OR
	    				(
	    					(COLUMN_REPEAT = CONSTANT_REPEAT_WEEKLY) AND (strftime("%w",COLUMN_LESSON_DATE) == strftime("%w", date))
	    				)
	    			) AND COLUMN_LESSON_DATE < date
	    		)
	    	)
	    )
	    */
    }

    public Cursor getSubjectAndTimeCabinetAttitudeByDateAndLessonNumber(String date, long lessonNumber) {//попадает ли переданное время в какой-либо из уроков
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE +
                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " = ?) AND (" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " = ?) OR (" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?))" +
                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ?)" +
                        "))",
                new String[]{
                        Long.toString(lessonNumber),
                        date,
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
                        date,
                        date
                });
        /*
        WHERE
        (
            (COLUMN_LESSON_NUMBER = ?) AND (
                (COLUMN_LESSON_DATE = ?) OR (COLUMN_REPEAT = ?) OR (
                    (
                        (COLUMN_REPEAT = ?) AND (strftime("%w",COLUMN_LESSON_DATE) == strftime("%w", ?))
                    ) AND COLUMN_LESSON_DATE > date
                )
            )
        )
        */
    }

    public int deleteSubjectAndTimeCabinetAttitude(long attitudeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE,
                SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_AND_TIME_CABINET_ATTITUDE_ID + " = " + attitudeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteSubjectAndTimeCabinetAttitude attitudeId= " + attitudeId + " return = " + answer);
        return answer;
    }

    // комментарий к дате и уроку (номер урока не нужен, тк повторы идут по датам, а не по номерам уроков)
    public long createLessonComment(String date, long lessonId, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLessonComment.KEY_LESSON_ID, lessonId);
        values.put(SchoolContract.TableLessonComment.COLUMN_LESSON_DATE, date);
        values.put(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT, text);
        long temp = db.insert(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createLessonComment returnId = " + temp + " text= " + text);
        return temp;
    }

    public void setLessonCommentStringById(long commentId, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT, text);
        db.update(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, values, SchoolContract.TableLessonComment.KEY_LESSON_TEXT_ID + " == " + commentId, null);
    }

//    public Cursor getLessonCommentsBetweenDates(long lessonId, String startDate, String endDate) {
//        return getReadableDatabase().query(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, null,
//                SchoolContract.TableLessonComment.KEY_LESSON_ID + " == " + lessonId + " AND " + SchoolContract.TableLessonComment.COLUMN_LESSON_DATE + " BETWEEN \"" + startDate + "\" AND \"" + endDate + "\" ",
//                null, null, null, null);
//    }

    public Cursor getLessonCommentsByDateAndLesson(long lessonId, String date) {
        return getReadableDatabase().query(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, null,
                SchoolContract.TableLessonComment.KEY_LESSON_ID + " == " + lessonId + " AND " + SchoolContract.TableLessonComment.COLUMN_LESSON_DATE + " == \"" + date + "\"",
                null, null, null, null);
    }

    public void removeLessonCommentById(long lessonCommentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, SchoolContract.TableLessonComment.KEY_LESSON_TEXT_ID + " == " + lessonCommentId, null);
    }

    //работа с бд
    public void restartTable() {//создание бд заново
        onUpgrade(this.getReadableDatabase(), 0, 100);
    }


    public void writeXMLDataBaseInFile(FileWriter bw) {
        XmlSerializer serializer = Xml.newSerializer();
        try {
            // назначаем файл в который будем писать
            serializer.setOutput(bw);
            serializer.startDocument("UTF-8", true);

            // таблица настроек
            serializer.startTag("", SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS);
            Cursor settings = getReadableDatabase()
                    .query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, null, null, null, null, null);
            while (settings.moveToNext()) {
                serializer.startTag("", "element");
                serializer.attribute("", "elementId", settings.getString(settings.getColumnIndex(SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID)));
                serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, settings.getString(settings.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME)));
                serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_LOCALE, settings.getString(settings.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_LOCALE)));
                serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, settings.getString(settings.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE)));
                serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER, settings.getString(settings.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER)));
                serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_TIME, settings.getString(settings.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_TIME)));
                serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED, settings.getString(settings.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED)));
                serializer.endTag("", "element");
            }
            settings.close();
            serializer.endTag("", SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS);

            // таблица кабинетов
            serializer.startTag("", SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
            Cursor cabinets = getCabinets();
            while (cabinets.moveToNext()) {
                serializer.startTag("", "element");
                serializer.attribute("", "elementId", cabinets.getString(cabinets.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID)));
                serializer.attribute("", SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER, cabinets.getString(cabinets.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER)));
                serializer.attribute("", SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X, cabinets.getString(cabinets.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X)));
                serializer.attribute("", SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y, cabinets.getString(cabinets.getColumnIndex(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y)));
                serializer.attribute("", SchoolContract.TableCabinets.COLUMN_NAME, cabinets.getString(cabinets.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)));
                serializer.endTag("", "element");
            }
            cabinets.close();
            serializer.endTag("", SchoolContract.TableCabinets.NAME_TABLE_CABINETS);

            // таблица парт
            serializer.startTag("", SchoolContract.TableDesks.NAME_TABLE_DESKS);
            Cursor desks = getDesks();
            while (desks.moveToNext()) {
                serializer.startTag("", "element");
                serializer.attribute("", "elementId", desks.getString(desks.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
                serializer.attribute("", SchoolContract.TableDesks.COLUMN_X, desks.getString(desks.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)));
                serializer.attribute("", SchoolContract.TableDesks.COLUMN_Y, desks.getString(desks.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)));
                serializer.attribute("", SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES, desks.getString(desks.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES)));
                serializer.attribute("", SchoolContract.TableDesks.KEY_CABINET_ID, desks.getString(desks.getColumnIndex(SchoolContract.TableDesks.KEY_CABINET_ID)));
                serializer.endTag("", "element");
            }
            desks.close();
            serializer.endTag("", SchoolContract.TableDesks.NAME_TABLE_DESKS);

            //// таблица парт
            //            serializer.startTag("", SchoolContract..);
            //            Cursor desks = get();
            //            while (desks.moveToNext()){
            //                serializer.startTag("", "element");
            //                serializer.attribute("", "elementId", desks.getString(desks.getColumnIndex(SchoolContract..)));
            //                serializer.attribute("", SchoolContract.., desks.getString(desks.getColumnIndex(SchoolContract..)));
            //                serializer.endTag("", "element");
            //            }
            //            desks.close();
            //            serializer.endTag("", SchoolContract..);


//            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + ";");
//            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + ";");
//            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + ";");
//            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + ";");

            // таблица оценок
            serializer.startTag("", SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES);
            Cursor grades = getReadableDatabase().query(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                    null, null, null, null, null, null);
            while (grades.moveToNext()) {
                serializer.startTag("", "element");
                serializer.attribute("", "elementId", grades.getString(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_GRADE_ID)));

                serializer.attribute("", SchoolContract.TableLearnersGrades.COLUMN_DATE,
                        grades.getString(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_DATE)));
                serializer.attribute("", SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER,
                        grades.getString(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER)));
                for (int i = 0; i < SchoolContract.TableLearnersGrades.COLUMNS_GRADE.length; i++) {
                    serializer.attribute("", SchoolContract.TableLearnersGrades.COLUMNS_GRADE[i],
                            grades.getString(grades.getColumnIndex(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[i])));
                }
                for (int i = 0; i < SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID.length; i++) {
                    serializer.attribute("", SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[i],
                            grades.getString(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[i])));
                }

                serializer.attribute("", SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID,
                        "" + grades.getString(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID)));
                serializer.attribute("", SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID,
                        grades.getString(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID)));
                serializer.attribute("", SchoolContract.TableLearnersGrades.KEY_LEARNER_ID,
                        grades.getString(grades.getColumnIndex(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID)));

                serializer.endTag("", "element");
            }
            grades.close();
            serializer.endTag("", SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES);


//            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + ";");
//            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE + ";");
//            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES + ";");
//            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + ";");

            // таблица комментариев к уроку
            serializer.startTag("", SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT);
            Cursor comments = getReadableDatabase().query(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, null, null, null, null, null, null);
            ;
            while (comments.moveToNext()) {
                serializer.startTag("", "element");
                serializer.attribute("", "elementId", comments.getString(comments.getColumnIndex(SchoolContract.TableLessonComment.KEY_LESSON_TEXT_ID)));
                serializer.attribute("", SchoolContract.TableLessonComment.KEY_LESSON_ID, comments.getString(comments.getColumnIndex(SchoolContract.TableLessonComment.KEY_LESSON_ID)));
                serializer.attribute("", SchoolContract.TableLessonComment.COLUMN_LESSON_DATE, comments.getString(comments.getColumnIndex(SchoolContract.TableLessonComment.COLUMN_LESSON_DATE)));
                serializer.attribute("", SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT, comments.getString(comments.getColumnIndex(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT)));
                serializer.endTag("", "element");
            }
            comments.close();
            serializer.endTag("", SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT);

            serializer.endDocument();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//
//    // класс буфер для преобразования данных в xml
//    public static class DataBaseClassInterpretation {
//
//        // file.TADB
//
//        public String dbVersion;
//
//        public static class TableSettingsData {
//            public long keyId;
//            public String COLUMN_PROFILE_NAME;
//            public String COLUMN_LOCALE;
//            public String COLUMN_LOCALE_DEFAULT_CODE;
//            public int COLUMN_INTERFACE_SIZE;
//            public int COLUMN_MAX_ANSWER;
//            public String COLUMN_TIME;
//            public int COLUMN_ARE_THE_GRADES_COLORED;
//        }
//
//        public static final class TableStatisticsProfiles {
//            public long keyId;
//            public String COLUMN_PROFILE_NAME;
//            public String COLUMN_START_DATE;
//            public String COLUMN_END_DATE;
//        }
//
//        public static final class TableCabinets {
//            public long keyId;
//            public int COLUMN_CABINET_MULTIPLIER;
//            public int COLUMN_CABINET_OFFSET_X;
//            public int COLUMN_CABINET_OFFSET_Y;
//            public String COLUMN_NAME;
//        }
//
//        public static final class TableDesks {
//            public long keyId;
//            public int COLUMN_X;
//            public int COLUMN_Y;
//            public int COLUMN_NUMBER_OF_PLACES;
//            public long KEY_CABINET_ID;
//        }
//
//        public static final class TablePlaces {
//            public long keyId;
//            public long KEY_DESK_ID;
//            public long COLUMN_ORDINAL;//какое по счету место
//        }
//
//        public static final class TableClasses {
//            public long keyId;
//            public String COLUMN_CLASS_NAME;
//        }
//
//        public static final class TableLearners {
//            public long keyId;
//            public String COLUMN_FIRST_NAME;
//            public String COLUMN_SECOND_NAME;
//            public long KEY_CLASS_ID;
//        }
//
//        public static final class TableLearnersOnPlaces {
//            public long keyId;
//            public long KEY_LEARNER_ID;
//            public long KEY_PLACE_ID;
//        }
//
//        public static final class TableLearnersGrades {
//            public long keyId;
//
//            public String COLUMN_DATE;
//            public int COLUMN_LESSON_NUMBER;
//
//            public int[] COLUMNS_GRADE;
//            public long[] KEYS_GRADES_TITLES_ID;
//            public long KEY_ABSENT_TYPE_ID;
//
//            public long KEY_SUBJECT_ID;
//            public long KEY_LEARNER_ID;
//        }
//
//        public static final class TableLearnersGradesTitles {
//            public long keyId;
//            public String COLUMN_LEARNERS_GRADES_TITLE;
//        }
//
//        public static final class TableLearnersAbsentTypes {
//            public long keyId;
//            public String COLUMN_LEARNERS_ABSENT_TYPE_NAME;
//            public String COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME;
//        }
//
//        public static final class TableSubjects {
//            public long keyId;
//            public String COLUMN_NAME;
//            public long KEY_CLASS_ID;
//        }
//
//        public static final class TableSubjectAndTimeCabinetAttitude {
//            public long keyId;
//            public long KEY_SUBJECT_ID;
//            public long KEY_CABINET_ID;
//
//            public String COLUMN_LESSON_DATE;
//            public int COLUMN_LESSON_NUMBER;
//
//            public int COLUMN_REPEAT;
//        }
//    }


}
