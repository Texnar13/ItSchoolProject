package com.learning.texnar13.teachersprogect.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataBaseOpenHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 9;


    public DataBaseOpenHelper(Context context) {
        super(context, SchoolContract.DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateDatabase(db, 0, 1);
    }

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
        Log.i("DataBaseOpenHelper", "updateDatabase old=" + oldVersion + " new=" + newVersion);

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
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + ";");
            db.execSQL("PRAGMA foreign_keys = ON;");

            //настройки
            String sql = "CREATE TABLE " + SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS + "( " + SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME + " VARCHAR, " +
                    SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE + " INTEGER ); ";
            db.execSQL(sql);

            //кабинет
            sql = "CREATE TABLE " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "( " + SchoolContract.TableCabinets.KEY_CABINET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableCabinets.COLUMN_NAME + " VARCHAR ); ";
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
                    SchoolContract.TableLearners.KEY_CLASS_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearners.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            //ученик-место
            sql = "CREATE TABLE " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + " ( " + SchoolContract.TableLearnersOnPlaces.KEY_ATTITUDES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " INTEGER, " +
                    SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + ") REFERENCES " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " (" + SchoolContract.TablePlaces.KEY_PLACE_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            //оценки учеников
            sql = "CREATE TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + " ( " + SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " INTEGER, " +
                    SchoolContract.TableLearnersGrades.COLUMN_GRADE + " INTEGER, " +
                    SchoolContract.TableLearnersGrades.KEY_LESSON_ID + " INTEGER, " +
                    SchoolContract.TableLearnersGrades.COLUMN_TIME_STAMP + " TEXT DEFAULT '0000-00-00 00:00:00', " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " (" + SchoolContract.TableLessons.KEY_LESSON_ID + ") ON DELETE CASCADE ); " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
            //уроки
            sql = "CREATE TABLE " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " ( " + SchoolContract.TableLessons.KEY_LESSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLessons.COLUMN_NAME + " VARCHAR, " +
                    SchoolContract.TableLessons.KEY_CLASS_ID + " INTEGER, " +
                    //SchoolContract.TableLessons.KEY_CABINET_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLessons.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ON DELETE CASCADE ); ";
            //"FOREIGN KEY(" + SchoolContract.TableLessons.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + " (" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ON DELETE CASCADE, " +
            db.execSQL(sql);
            //урок и его время проведения
            sql = "CREATE TABLE " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + " ( " + SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID + " INTEGER, " +
                    SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID + " INTEGER, " +
                    SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " INTEGER, " +
                    SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + " INTEGER, " +
                    SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " INTEGER DEFAULT " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_NEVER + ", " +
                    "FOREIGN KEY(" + SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + " (" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ON DELETE CASCADE ," +
                    "FOREIGN KEY(" + SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " (" + SchoolContract.TableLessons.KEY_LESSON_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);
        } else {//иначе г@внокод//todo удалить это как можно скорее
            if (oldVersion < 5) {//если база версии 5 и выше, то она не запустит этот код //создаём пустые таблицы без за полнения
                db.execSQL("PRAGMA foreign_keys = OFF;");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableDesks.NAME_TABLE_DESKS + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + ";");
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + ";");
                db.execSQL("PRAGMA foreign_keys = ON;");


//настройки
                String sql = "CREATE TABLE " + SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS + "( " + SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME + " VARCHAR, " +
                        SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE + " INTEGER ); ";
                db.execSQL(sql);

//кабинет
                sql = "CREATE TABLE " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "( " + SchoolContract.TableCabinets.KEY_CABINET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SchoolContract.TableCabinets.COLUMN_NAME + " VARCHAR ); ";
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
                        SchoolContract.TableLearners.KEY_CLASS_ID + " INTEGER, " +
                        "FOREIGN KEY(" + SchoolContract.TableLearners.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ON DELETE CASCADE ); ";
                db.execSQL(sql);
//ученик-место
                sql = "CREATE TABLE " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + " ( " + SchoolContract.TableLearnersOnPlaces.KEY_ATTITUDES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " INTEGER, " +
                        SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " INTEGER, " +
                        "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE, " +
                        "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + ") REFERENCES " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " (" + SchoolContract.TablePlaces.KEY_PLACE_ID + ") ON DELETE CASCADE ); ";
                db.execSQL(sql);
//оценки учеников
                sql = "CREATE TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + " ( " + SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " INTEGER, " +
                        SchoolContract.TableLearnersGrades.COLUMN_GRADE + " INTEGER, " +
                        SchoolContract.TableLearnersGrades.KEY_LESSON_ID + " INTEGER, " +
                        "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE ); ";
                db.execSQL(sql);
//уроки
                sql = "CREATE TABLE " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " ( " + SchoolContract.TableLessons.KEY_LESSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SchoolContract.TableLessons.COLUMN_NAME + " VARCHAR, " +
                        SchoolContract.TableLessons.KEY_CLASS_ID + " INTEGER, " +
                        //SchoolContract.TableLessons.KEY_CABINET_ID + " INTEGER, " +
                        "FOREIGN KEY(" + SchoolContract.TableLessons.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ON DELETE CASCADE ); ";
                //"FOREIGN KEY(" + SchoolContract.TableLessons.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + " (" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ON DELETE CASCADE, " +
                db.execSQL(sql);
//--
//урок и его время проведения
                sql = "CREATE TABLE " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + " ( " + SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID + " INTEGER, " +
                        //--
                        SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID + " INTEGER, " +
                        //--
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " INTEGER, " +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + " INTEGER, " +
                        "FOREIGN KEY(" + SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " (" + SchoolContract.TableLessons.KEY_LESSON_ID + ") ON DELETE CASCADE ); ";
                db.execSQL(sql);
            }
            if (oldVersion < 6) {
                db.execSQL("ALTER TABLE " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + " ADD COLUMN " + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " INTEGER DEFAULT " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_NEVER + ";");//колонка для повторения уроков
            }
            if (oldVersion < 7) {
                db.execSQL("ALTER TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + " ADD COLUMN " + SchoolContract.TableLearnersGrades.COLUMN_TIME_STAMP + " TEXT DEFAULT '0000-00-00 00:00:00';");//время поставленной оценки
            }

            if (oldVersion < 8) {//добавляем foreign key
                db.execSQL("PRAGMA foreign_keys = OFF;");
                // --для кабинетов--

                //db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + "_old;");

                //переименовываем таблицу
                db.execSQL("ALTER TABLE " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + " RENAME TO " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + "_old;");
                //создаём новую таблицу
                db.execSQL("CREATE TABLE " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + " ( " + SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID + " INTEGER, " +
                        SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID + " INTEGER, " +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " INTEGER, " +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + " INTEGER, " +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " INTEGER DEFAULT " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_NEVER + ", " +
                        "FOREIGN KEY(" + SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + " (" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ON DELETE CASCADE ," +
                        "FOREIGN KEY(" + SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " (" + SchoolContract.TableLessons.KEY_LESSON_ID + ") ON DELETE CASCADE ); "
                );
                //переносим значения
                db.execSQL("INSERT INTO " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + " SELECT * FROM " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET + "_old;");
                //удаляем старую таблицу
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + ";");

                // --для оценок--
                db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + ";");
                String sql = "CREATE TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + " ( " + SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " INTEGER, " +
                        SchoolContract.TableLearnersGrades.COLUMN_GRADE + " INTEGER, " +
                        SchoolContract.TableLearnersGrades.COLUMN_TIME_STAMP + " TEXT DEFAULT '0000-00-00 00:00:00', " +
                        SchoolContract.TableLearnersGrades.KEY_LESSON_ID + " INTEGER, " +
                        "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " (" + SchoolContract.TableLessons.KEY_LESSON_ID + ") ON DELETE CASCADE ); " +
                        "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE ); ";
                db.execSQL(sql);
                db.execSQL("PRAGMA foreign_keys = ON");
            }
        }

    }

    //настройки
    public long createNewSettingsProfile(String profileName, int interfaceSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, profileName);
        values.put(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, interfaceSize);
        long temp = db.insert(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, values);//-1 = ошибка ввода
        Log.i("DBOpenHelper", "createSettingsProfile returnId = " + temp + " profileName= " + profileName + " interfaceSize= " + interfaceSize);
        db.close();
        return temp;
    }

    public int setSettingsProfileParameters(long profileId, String profileName, int interfaceSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, profileName);
        values.put(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, interfaceSize);
        int temp = db.update(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, values, SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = ?", new String[]{"" + profileId});
        Log.i("DBOpenHelper", "setSettingsProfileParameters return = " + temp + " profileId= " + profileId + " profileName= " + profileName + " interfaceSize= " + interfaceSize);
        db.close();
        return temp;
    }

    public long getInterfaceSizeBySettingsProfileId(long profileId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {profileId + ""};
        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, SchoolContract.TableSettingsData.KEY_SETTINGS_PROFILE_ID + " = ?", selectionArgs, null, null, null);
        long answer;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            answer = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE));
        } else {
            answer = -1;
        }
        Log.i("DBOpenHelper", "getInterfaceSizeBySettingsProfileId profileId=" + profileId + " return=" + answer);

        cursor.close();
        return answer;
    }


    //класс
    public long createClass(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
//        String sql = "INSERT INTO NAME_TABLE_CLASSES( " + SchoolContract.TableClasses.COLUMN_CLASS_NAME + " ) " +
//                "VALUES ( '" + name + "' );";
//        db.execSQL(sql);
//        db.close();
//        return true;
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableClasses.COLUMN_CLASS_NAME, name);
        long temp = db.insert(SchoolContract.TableClasses.NAME_TABLE_CLASSES, null, values);//-1 = ошибка ввода
        Log.i("DBOpenHelper", "createClass returnId = " + temp + " name= " + name);
        db.close();
        return temp;
    }

    public Cursor getClasses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableClasses.NAME_TABLE_CLASSES, null, null, null, null, null, null);
        Log.i("DBOpenHelper", "getClasses " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getClasses(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {classId + ""};
        Cursor cursor = db.query(SchoolContract.TableClasses.NAME_TABLE_CLASSES, null, SchoolContract.TableClasses.KEY_CLASS_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getClasses " + cursor + "id=" + classId + "number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public int setClassesNames(ArrayList<Long> classId, String name) {
        // todo сортировка курсора
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableClasses.COLUMN_CLASS_NAME, name);
        int answer = 0;
        String stringClassId = "";
        for (int i = 0; i < classId.size(); i++) {
            stringClassId = stringClassId + classId.get(i) + " | ";
            if (db.update(SchoolContract.TableClasses.NAME_TABLE_CLASSES, contentName, SchoolContract.TableClasses.KEY_CLASS_ID + " = ?", new String[]{"" + classId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "setClassesNames name = " + name + " id= " + stringClassId + " return = " + answer);
        db.close();
        return answer;
    }

    public int deleteClasses(ArrayList<Long> classesId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = 0;
        String stringClassId = "";
        for (int i = 0; i < classesId.size(); i++) {
            stringClassId = stringClassId + classesId.get(i) + " | ";
            if (db.delete(SchoolContract.TableClasses.NAME_TABLE_CLASSES, SchoolContract.TableClasses.KEY_CLASS_ID + " = ?", new String[]{"" + classesId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "deleteClasses id= " + stringClassId + " return = " + answer);
        db.close();
        return answer;
    }


    //ученик
    public long createLearner(String secondName, String name, long class_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearners.COLUMN_SECOND_NAME, secondName);
        values.put(SchoolContract.TableLearners.COLUMN_FIRST_NAME, name);
        values.put(SchoolContract.TableLearners.KEY_CLASS_ID, class_id);
        long temp = db.insert(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createLearner returnId= " + temp + " secondName= " + secondName + " name= " + name + " class_id= " + class_id);
        return temp;
    }

    public Cursor getLearnersByClassId(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {classId + ""};
        Cursor cursor = db.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, SchoolContract.TableLearners.KEY_CLASS_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLearnersByClassId classId=" + classId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public long getLearnerIdByClassIdAndPlaceId(long classId, long placeId) {//todo КОСТЫЛИЩЕЕЕЕ!!!!111 во всех отношениях
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor learnersCursor = this.getLearnersByClassId(classId);
        ArrayList<Long> learnersId = new ArrayList<>(learnersCursor.getCount());//получаем по классу id учеников
        while (learnersCursor.moveToNext()) {
            learnersId.add(learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
        }
        learnersCursor.close();

        long answer = -1;//ищем совпадение ученик - место
        Cursor cursor;
        for (int i = 0; i < learnersId.size(); i++) {
            String[] selectionArgs = {learnersId.get(i) + "", placeId + ""};
            cursor = db.query(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES, null, SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " = ? AND " + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " = ?", selectionArgs, null, null, null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                answer = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID));
            }
            cursor.close();
        }
        Log.i("DBOpenHelper", "getLearnerIdByClassIdAndPlaceId classId=" + classId + " placeId=" + placeId + " answer=" + answer);
        return answer;
    }

    public Cursor getLearner(long learnerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {learnerId + ""};
        Cursor cursor = db.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, SchoolContract.TableLearners.KEY_LEARNER_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLearner learnerId=" + learnerId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
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
        Log.i("DBOpenHelper", "getNotPutLearnersIdByCabinetIdAndClassId cabinetId=" + cabinetId + " classId=" + classId + " return=" + answer);
        return answer;
    }


    public int setLearnerNameAndLastName(ArrayList<Long> learnersId, String name, String lastName) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableLearners.COLUMN_FIRST_NAME, name);
        contentName.put(SchoolContract.TableLearners.COLUMN_SECOND_NAME, lastName);
        int answer = 0;
        String stringLearnersId = "";
        for (int i = 0; i < learnersId.size(); i++) {
            stringLearnersId = stringLearnersId + learnersId.get(i) + " | ";
            if (db.update(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, contentName, SchoolContract.TableLearners.KEY_LEARNER_ID + " = ?", new String[]{"" + learnersId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "setLearnerNameAndLastName name= " + name + " lastName= " + lastName + " id= " + stringLearnersId + " return = " + answer);
        db.close();
        return answer;
    }

    public int deleteLearners(ArrayList<Long> learnersId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = 0;
        String stringClassId = "";
        for (int i = 0; i < learnersId.size(); i++) {
            stringClassId = stringClassId + learnersId.get(i) + " | ";
            if (db.delete(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, SchoolContract.TableLearners.KEY_LEARNER_ID + " = ?", new String[]{"" + learnersId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "deleteLearners id= " + stringClassId + " return = " + answer);
        db.close();
        return answer;
    }


    //кабинет
    public long createCabinet(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableCabinets.COLUMN_NAME, name);
        long temp = db.insert(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createCabinet returnId = " + temp + " name= " + name);
        return temp;
    }

    public Cursor getCabinets() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, null, null, null, null, null, null);
        Log.i("DBOpenHelper", "getCabinets " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        //db.close();
        return cursor;
    }

    public Cursor getCabinets(long cabinetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {cabinetId + ""};
        Cursor cursor = db.query(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, null, SchoolContract.TableCabinets.KEY_CABINET_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getCabinets " + cursor + "id=" + cabinetId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public int setCabinetName(ArrayList<Long> cabinetId, String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableCabinets.COLUMN_NAME, name);
        int answer = 0;
        String stringCabinetsId = "";
        for (int i = 0; i < cabinetId.size(); i++) {
            stringCabinetsId = stringCabinetsId + cabinetId.get(i) + " | ";
            if (db.update(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, contentName, SchoolContract.TableCabinets.KEY_CABINET_ID + " = ?", new String[]{"" + cabinetId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "setCabinetName name= " + name + " id= " + stringCabinetsId + " return = " + answer);
        db.close();
        return answer;
    }

    public int deleteCabinets(ArrayList<Long> cabinetsId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = 0;
        String stringClassId = "";
        for (int i = 0; i < cabinetsId.size(); i++) {
            stringClassId = stringClassId + cabinetsId.get(i) + " | ";
            if (db.delete(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, SchoolContract.TableCabinets.KEY_CABINET_ID + " = ?", new String[]{"" + cabinetsId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "deleteCabinets id= " + stringClassId + " return = " + answer);
        db.close();
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
        db.close();
        Log.i("DBOpenHelper", "createDesk returnId = " + temp + " numberOfPlaces= " + numberOfPlaces + " x= " + x + " y= " + y + " cabinet_id= " + cabinet_id);
        return temp;
    }

    public long setDeskCoordinates(long deskId, long x, long y) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues content = new ContentValues();
        content.put(SchoolContract.TableDesks.COLUMN_X, x);
        content.put(SchoolContract.TableDesks.COLUMN_Y, y);
        int answer = 0;
        if (db.update(SchoolContract.TableDesks.NAME_TABLE_DESKS, content, SchoolContract.TableDesks.KEY_DESK_ID + " = ?", new String[]{"" + deskId}) == 1)
            answer++;

        Log.i("DBOpenHelper", "setDeskCoordinates id= " + deskId + " x= " + x + " y= " + y + " return = " + answer);
        db.close();
        return answer;
    }

    public Cursor getDesksByCabinetId(long cabinetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableDesks.NAME_TABLE_DESKS, null, SchoolContract.TableDesks.KEY_CABINET_ID + " = ?", new String[]{cabinetId + ""}, null, null, null);//выяснил, не работает проверка в методе query
        Log.i("DBOpenHelper", "getDesksByCabinetId cabinetId=" + cabinetId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public int deleteDesk(long deskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableDesks.NAME_TABLE_DESKS, SchoolContract.TableDesks.KEY_DESK_ID + " = ?", new String[]{"" + deskId});
        Log.i("DBOpenHelper", "deleteDesk id= " + deskId + " return = " + answer);
        db.close();
        return answer;
    }


    //место
    public long createPlace(long deskId, long ordinal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TablePlaces.KEY_DESK_ID, deskId);
        values.put(SchoolContract.TablePlaces.COLUMN_ORDINAL, ordinal);
        long temp = db.insert(SchoolContract.TablePlaces.NAME_TABLE_PLACES, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createPlace returnId = " + temp + " desk_id= " + deskId);
        return temp;
    }

    public Cursor getPlacesByDeskId(long deskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TablePlaces.NAME_TABLE_PLACES, null, SchoolContract.TablePlaces.KEY_DESK_ID + " =?", new String[]{deskId + ""}, null, null, null);
        Log.i("DBOpenHelper", "getPlacesByDeskId deskId=" + deskId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        //db.close();
        return cursor;
    }


    //ученик-место
    public long setLearnerOnPlace(long learnerId, long placeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID, learnerId);
        values.put(SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID, placeId);
        long temp = db.insert(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "setLearnerOnPlace returnId = " + temp + " learnerId= " + learnerId + " placeId= " + placeId);
        return temp;
    }

    public Cursor getAttitudeByLearnerIdAndPlaceId(Long learnerId, Long placeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES, null, SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " = ? AND " + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " = ?", new String[]{Long.toString(learnerId), Long.toString(placeId)}, null, null, null);
        Log.i("DBOpenHelper", "getAttitudeByLearnerIdAndPlaceId learnerId=" + learnerId + " placeId=" + placeId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames())
        );
        return cursor;
    }

    public long deleteAttitudeByLearnerIdAndPlaceId(long learnerId, long placeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES, SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " = ? and " + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " = ?", new String[]{Long.toString(learnerId), Long.toString(placeId)});
        Log.i("DBOpenHelper", "deleteAttitudeByLearnerIdAndPlaceId learnerId=" + learnerId + " placeId=" + placeId + " return = " + answer);
        db.close();
        return answer;
    }


    //оценки учеников
    public long createGrade(long learnerId, long grade, long lessonId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID, learnerId);
        values.put(SchoolContract.TableLearnersGrades.COLUMN_GRADE, grade);
        values.put(SchoolContract.TableLearnersGrades.KEY_LESSON_ID, lessonId);
        values.put(SchoolContract.TableLearnersGrades.COLUMN_TIME_STAMP, date);
        long temp = db.insert(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createGrade returnId = " + temp + " learnerId= " + learnerId + " grade= " + grade + " lessonId= " + lessonId + " date= " + date);
        return temp;
    }

    public Cursor getGradeById(long gradeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {gradeId + ""};
        Cursor cursor = db.query(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null, SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getGrade gradeId=" + gradeId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getGradesByLearnerIdAndDayTime(long learnerId, GregorianCalendar viewCalendar) {
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//%Y-%m-%dT%H:%M:%S
        GregorianCalendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(new Date(viewCalendar.getTime().getTime() + 86400));
        Cursor cursor = db.query(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null, SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " = ? AND " + SchoolContract.TableLearnersGrades.COLUMN_TIME_STAMP + " BETWEEN ? AND ?", new String[]{Long.toString(learnerId), dateFormat.format(viewCalendar.getTime()), dateFormat.format(endCalendar.getTime())}, null, null, null);
        Log.i("DBOpenHelper", "getAttitudeByLearnerIdAndPlaceId learnerId=" + learnerId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames())
        );
        return cursor;
    }

    public long editGrade(long gradeId, long grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGrades.COLUMN_GRADE, grade);
        long temp = db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, values, SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " = ?", new String[]{Long.toString(gradeId)});//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "editGrade returnId = " + temp + " grade= " + grade + " gradeId= " + gradeId);
        return temp;
    }


    //уроки(предметы)
    public long createLesson(String name, long classId//,long cabinetId
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLessons.COLUMN_NAME, name);
        values.put(SchoolContract.TableLessons.KEY_CLASS_ID, classId);
        //values.put(SchoolContract.TableLessons.KEY_CABINET_ID, cabinetId);
        long temp = db.insert(SchoolContract.TableLessons.NAME_TABLE_LESSONS, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createLesson returnId = " + temp + " name= " + name +
                        " classId=" + classId
                //+ " cabinetId=" + cabinetId
        );
        return temp;
    }


    public int setLessonParameters(long lessonId, String lessonName, long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableLessons.COLUMN_NAME, lessonName);
        contentName.put(SchoolContract.TableLessons.KEY_CLASS_ID, classId);
        //contentName.put(SchoolContract.TableLessons.KEY_CABINET_ID, cabinetId);
        int answer = db.update(SchoolContract.TableLessons.NAME_TABLE_LESSONS, contentName, SchoolContract.TableLessons.KEY_LESSON_ID + " = ?", new String[]{Long.toString(lessonId)});
        db.close();
        return answer;
    }

    public Cursor getLessonById(long lessonId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {lessonId + ""};
        Cursor cursor = db.query(SchoolContract.TableLessons.NAME_TABLE_LESSONS, null, SchoolContract.TableLessons.KEY_LESSON_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLessonById " + cursor + " id=" + lessonId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getLessonsById(ArrayList<Long> lessonsId) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (lessonsId.size() == 0) {
            return db.query(SchoolContract.TableLessons.NAME_TABLE_LESSONS, null, SchoolContract.TableLessons.KEY_LESSON_ID + " = ? ", new String[]{"-1"}, null, null, null);
        }
        StringBuilder selection = new StringBuilder();
        String[] selectionArgs = new String[lessonsId.size()];
        selectionArgs[0] = lessonsId.get(0).toString();
        selection.append(SchoolContract.TableLessons.KEY_LESSON_ID + " = ? ");
        for (int i = 1; i < lessonsId.size(); i++) {
            selection.append("OR " + SchoolContract.TableLessons.KEY_LESSON_ID + " = ? ");

        }
        Cursor cursor = db.query(SchoolContract.TableLessons.NAME_TABLE_LESSONS, null, selection.toString(), selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLessonById  id=" + Arrays.toString(selectionArgs) + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getLessonsByClassId(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {classId + ""};
        Cursor cursor = db.query(SchoolContract.TableLessons.NAME_TABLE_LESSONS, null, SchoolContract.TableLessons.KEY_CLASS_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLessonByClassId " + cursor + " classId=" + classId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public int deleteLessons(ArrayList<Long> lessonsId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = 0;
        String stringLessonsId = "";
        for (int i = 0; i < lessonsId.size(); i++) {
            stringLessonsId = stringLessonsId + lessonsId.get(i) + " | ";
            if (db.delete(SchoolContract.TableLessons.NAME_TABLE_LESSONS, SchoolContract.TableLessons.KEY_LESSON_ID + " = ?", new String[]{"" + lessonsId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "deleteLessons id= " + stringLessonsId + " return = " + answer);
        db.close();
        return answer;
    }


    //урок и его время проведения

    public long setLessonTimeAndCabinet(long lessonId, long cabinetId, Date startTime, Date endTime, long repeatPeriod) {
        //date и database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID, lessonId);
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID, cabinetId);
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN, startTime.getTime());
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END, endTime.getTime());
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT, repeatPeriod);
        long answer = db.insert(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET, null, contentValues);
        Log.i("DBOpenHelper", "setLessonTime lessonId= " + lessonId + " cabinetId= " + cabinetId + " startTime= " + startTime.toString() + " endTime= " + endTime.toString() + " repeatPeriod= " + repeatPeriod + " return = " + answer);
        return answer;
    }

    public int editLessonTimeAndCabinet(long attitudeId, long lessonId, long cabinetId, Date startTime, Date endTime, long repeatPeriod) {
        //date и database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID, lessonId);
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID, cabinetId);
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN, startTime.getTime());
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END, endTime.getTime());
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT, repeatPeriod);
        int answer = db.update(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET, contentValues, SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " = ?", new String[]{Long.toString(attitudeId)});

        Log.i("DBOpenHelper", "editLessonTimeAndCabinet attitudeId= " + attitudeId + " lessonId= " + lessonId + " cabinetId= " + cabinetId + " startTime= " + startTime.toString() + " endTime= " + endTime.toString() + " return = " + answer);
        return answer;
    }

    public Cursor getLessonAttitudeById(long attitudeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {attitudeId + ""};
        Cursor cursor = db.query(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET, null, SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLessonAttitudeById " + " id=" + attitudeId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public ArrayList<Long> getLessonsAttitudesIdByTimePeriod(Calendar periodStart, Calendar periodEnd) {//todo , long repeatPeriod
        //проверяется только дата начала уроков
        SQLiteDatabase db = this.getReadableDatabase();
//        String[] selectionArgs = {
//                periodStart.getTime().getTime() + ""
//                , periodEnd.getTime().getTime() + ""
//                //--1
//                , ((periodStart.getTime().getTime() - 75600000) % 86400000) + "",//делим на день, оставшееся - время прошедшеее с 00.00
//                ((periodEnd.getTime().getTime() - 75600000) % 86400000) + "",//делим на день, оставшееся - время прошедшеее с 00.00
//                ((periodStart.getTime().getTime() - 75600000) % 604800000) + "",//на неделю
//                ((periodEnd.getTime().getTime() - 75600000) % 604800000) + ""//на неделю
//                //--1
//        };
//        Cursor cursor = db.query(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET, null,
//                //SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " >= ? AND " +
//                //       SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " <= ?",
//                "(" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " >= ? AND " + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " <= ?) OR ((" +//                                                                    Todo здесь
//                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " = " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_DAILY + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " - 75600000) % 86400000) >= ?) AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " - 75600000) % 86400000) <= ?)) OR ((" +
//                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " = " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_WEEKLY + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " - 75600000) % 604800000) >= ?) AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " - 75600000) % 604800000) <= ?))",
//                selectionArgs, null, null, null);
        Cursor cursor = db.rawQuery("SELECT * FROM " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET +
                        " WHERE ((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " >= " + periodStart.getTime().getTime() + " AND " + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " <= " + periodEnd.getTime().getTime() + ") OR ((" +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " = " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_DAILY + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + "- 75600000) % 86400000) >= " + ((periodStart.getTime().getTime() - 75600000) % 86400000) + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + "- 75600000) % 86400000)<= " + ((periodEnd.getTime().getTime() - 75600000) % 86400000) + ")) OR ((" +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " = " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_WEEKLY + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + "- 75600000) % 604800000) >= " + ((periodStart.getTime().getTime() - 75600000) % 604800000) + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + "- 75600000) % 604800000)<= " + ((periodEnd.getTime().getTime() - 75600000) % 604800000) + ")))"
                , null);
//        Log.i("Teachers app", "***" + (((1510896600000L - 75600000) % 86400000) <= (1510865999000L - 75600000) % 86400000));
//        Log.i("Teachers app", "----" + (periodStart.getTime().getTime()) + "     " + new GregorianCalendar(2017, 10, 17, 8, 30).getTime().getTime());
//        Log.i("Teachers app", "++++" + "(" + new GregorianCalendar(2017, 10, 17, 8, 30).getTime().getTime() + " >= " + periodStart.getTime().getTime() + " AND " + new GregorianCalendar(2017, 10, 17, 8, 30).getTime().getTime() + " <= " + periodEnd.getTime().getTime() + ") OR ((COLUMN_REPEAT = CONSTANT_REPEAT_DAILY) AND (" + ((new GregorianCalendar(2017, 10, 17, 8, 30).getTime().getTime() - 75600000) % 86400000) + " >= =-----" + ((periodStart.getTime().getTime() - 75600000) % 86400000) + ") AND (" + ((new GregorianCalendar(2017, 10, 17, 8, 30).getTime().getTime() - 75600000) % 86400000) + "<= " + ((periodEnd.getTime().getTime() - 75600000) % 86400000) + ")) OR ((" +
//                SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " = " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_WEEKLY + ") AND (" + ((new GregorianCalendar(2017, 10, 17, 8, 30).getTime().getTime() - 75600000) % 604800000) + " >= " + ((periodStart.getTime().getTime() - 75600000) % 604800000) + ") AND (" + ((new GregorianCalendar(2017, 10, 17, 8, 30).getTime().getTime() - 75600000) % 604800000) + " <= " + ((periodEnd.getTime().getTime() - 75600000) % 604800000) + ")))" +
//                "     " + new GregorianCalendar(2017, 10, 17, 8, 30).getTime().getTime() % 86400000);
        ArrayList<Long> answer = new ArrayList<>();
        StringBuilder stringLessonsAttitudesId = new StringBuilder();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID));
            stringLessonsAttitudesId.append(id);
            stringLessonsAttitudesId.append(" | ");
            answer.add(id);
        }
        cursor.close();
        Log.i("DBOpenHelper", "getLessonsAttitudesIdByTimePeriod periodStart=" + periodStart.getTime().getTime() + " periodEnd=" + periodEnd.getTime().getTime() + " answer=" + stringLessonsAttitudesId);
        return answer;
    }

    public long getLessonsAttitudesIdByTime(Calendar time) {//попадает ли переданное время в какое-либо из событий
        SQLiteDatabase db = this.getReadableDatabase();
//        String[] selectionArgs = {
//                time.getTime().getTime() + ""
//                , time.getTime().getTime() + ""
//
//        };
//        Cursor cursor = db.query(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET,
//                null, SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " <= ? AND " +
//                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + " >= ?",
//                //(COLUMN_DATE_BEGIN >= ? AND COLUMN_DATE_BEGIN <= ?) OR
//                //(COLUMN_REPEAT == 1 AND (COLUMN_DATE_BEGIN % 86400000) >= ? AND (COLUMN_DATE_BEGIN % 86400000)<= ?) OR
//                //(COLUMN_REPEAT == 2 AND (COLUMN_DATE_BEGIN % 604800000) >= ? AND (COLUMN_DATE_BEGIN % 604800000)<= ?)"
//                selectionArgs, null, null, null);

        Cursor cursor = db.rawQuery("SELECT * FROM " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET +
                        " WHERE ((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " <= " + time.getTime().getTime() + " AND " +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + " >= " + time.getTime().getTime() + ") OR ((" +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " = " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_DAILY + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + "- 75600000) % 86400000) <= " + ((time.getTime().getTime() - 75600000) % 86400000) + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + "- 75600000) % 86400000)>= " + ((time.getTime().getTime() - 75600000) % 86400000) + ")) OR ((" +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_REPEAT + " = " + SchoolContract.TableLessonAndTimeWithCabinet.CONSTANT_REPEAT_WEEKLY + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + "- 75600000) % 604800000) <= " + ((time.getTime().getTime() - 75600000) % 604800000) + ") AND (((" + SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + "- 75600000) % 604800000)>= " + ((time.getTime().getTime() - 75600000) % 604800000) + ")))"
                , null);
        long answer;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            answer = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID));
            cursor.close();
            Log.i("DBOpenHelper", "getLessonsAttitudesIdByTimePeriod time=" + time.getTime().getTime() + " answer=" + answer);
            return answer;
        } else {
            cursor.close();
            Log.i("DBOpenHelper", "getLessonsAttitudesIdByTimePeriod time=" + time.getTime().getTime() + " answer=" + (-1));
            return -1;
        }
    }

    public int deleteLessonTimeAndCabinet(long attitudeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME_WITH_CABINET, SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " = ?", new String[]{"" + attitudeId});
        Log.i("DBOpenHelper", "deleteLessonTimeAndCabinet attitudeId= " + attitudeId + " return = " + answer);
        db.close();
        return answer;
    }

    //работа с бд
    public void restartTable() {//создание бд заново
        onUpgrade(this.getReadableDatabase(), 0, 100);
    }


}
