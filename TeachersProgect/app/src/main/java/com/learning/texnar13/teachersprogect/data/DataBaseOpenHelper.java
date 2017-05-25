package com.learning.texnar13.teachersprogect.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class DataBaseOpenHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 4;


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

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DataBaseOpenHelper", "updateDatabase old=" + oldVersion + " new=" + newVersion);

        if (oldVersion < 5) {//если база версии 5 и выше, то она не запустит этот код
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableDesks.NAME_TABLE_DESKS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + ";");
            //--------
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + ";");


            String sql = "CREATE TABLE " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "( " + SchoolContract.TableCabinets.KEY_CABINET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableCabinets.COLUMN_NAME + " VARCHAR ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableDesks.NAME_TABLE_DESKS + "( " + SchoolContract.TableDesks.KEY_DESK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableDesks.COLUMN_X + " INTEGER, " +
                    SchoolContract.TableDesks.COLUMN_Y + " INTEGER, " +
                    SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES + " INTEGER, " +
                    SchoolContract.TableDesks.KEY_CABINET_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableDesks.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "(" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " ( " + SchoolContract.TablePlaces.KEY_PLACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TablePlaces.KEY_DESK_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TablePlaces.KEY_DESK_ID + ") REFERENCES " + SchoolContract.TableDesks.NAME_TABLE_DESKS + " (" + SchoolContract.TableDesks.KEY_DESK_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " ( " + SchoolContract.TableClasses.KEY_CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableClasses.COLUMN_CLASS_NAME + " VARCHAR ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " ( " + SchoolContract.TableLearners.KEY_LEARNER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLearners.COLUMN_FIRST_NAME + " VARCHAR, " +
                    SchoolContract.TableLearners.COLUMN_SECOND_NAME + " VARCHAR, " +
                    SchoolContract.TableLearners.KEY_CLASS_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearners.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + " ( " + SchoolContract.TableLearnersOnPlaces.KEY_ATTITUDES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " INTEGER, " +
                    SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + ") REFERENCES " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " (" + SchoolContract.TablePlaces.KEY_PLACE_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + " ( " + SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " INTEGER, " +
                    SchoolContract.TableLearnersGrades.COLUMN_GRADE + " INTEGER, " +
                    SchoolContract.TableLearnersGrades.COLUMN_DATE + " VARCHAR, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);

            //------------------------------------------

            sql = "CREATE TABLE " + SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES + " ( " + SchoolContract.TableSchedules.KEY_SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableSchedules.COLUMN_NAME + " VARCHAR ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " ( " + SchoolContract.TableLessons.KEY_LESSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLessons.COLUMN_NAME + " VARCHAR, " +
                    SchoolContract.TableLessons.KEY_SCHEDULE_ID + " INTEGER, " +
                    SchoolContract.TableLessons.COLUMN_DATE_BEGIN + " INTEGER, " +
                    SchoolContract.TableLessons.COLUMN_DATE_END + " INTEGER, " +
                    SchoolContract.TableLessons.KEY_CLASS_ID + " INTEGER, " +
                    SchoolContract.TableLessons.KEY_CABINET_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLessons.KEY_SCHEDULE_ID + ") REFERENCES " + SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES + " (" + SchoolContract.TableSchedules.KEY_SCHEDULE_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + SchoolContract.TableLessons.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + SchoolContract.TableLessons.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + " (" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ON DELETE CASCADE ); ";
            db.execSQL(sql);


        }
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
        //db.close();
        return cursor;
    }

    public Cursor getClasses(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {classId + ""};
        Cursor cursor = db.query(SchoolContract.TableClasses.NAME_TABLE_CLASSES, null, SchoolContract.TableClasses.KEY_CLASS_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getClasses " + cursor + "id=" + classId + "number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        //db.close();
        return cursor;
    }

    public int setClassesNames(ArrayList<Long> classId, String name) {
        /*  что если в schoolContract добавить классы в которые можно поместь (сконвертировав) обьект таблицы бд
         * со всеми прилагающимися а потом создать метод на подобие
         * createClass(SchoolContract.TableClasses.ClassObject classObject); и генерировать обьект rename и
         * изменять его, затем отправлять на поыторное создание. сортировка курсора
         * */
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

    public long setLearnerOnPlace(long learner_id, long place_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID, learner_id);
        values.put(SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID, place_id);
        long temp = db.insert(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "setLearnerOnPlace returnId = " + temp + " learner_id= " + learner_id + " place_id= " + place_id);
        return temp;
    }

    public Cursor getLearnersByClassId(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {classId + ""};
        //Cursor cursor = db.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, null, null, null, null, null);
        Cursor cursor = db.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, SchoolContract.TableLearners.KEY_CLASS_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLearnersByClassId classId=" + classId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getLearner(long learnerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {learnerId + ""};
        //Cursor cursor = db.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, null, null, null, null, null);
        Cursor cursor = db.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, SchoolContract.TableLearners.KEY_LEARNER_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLearner learnerId=" + learnerId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
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

    public Cursor getDesksByCabinetId(long cabinetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        //String[] selectionArgs = {cabinetId + ""};
        Cursor cursor = db.query(SchoolContract.TableDesks.NAME_TABLE_DESKS, null, SchoolContract.TableDesks.KEY_CABINET_ID + " = ?", new String[]{cabinetId + ""}, null, null, null);//выяснил, не работает проверка в методе query
        Log.i("DBOpenHelper", "getDesksByCabinetId cabinetId=" + cabinetId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        //db.close();
        return cursor;
    }


    //место
    public long createPlace(long deskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TablePlaces.KEY_DESK_ID, deskId);
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

    //расписание
    public long createSchedule(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSchedules.COLUMN_NAME, name);
        long temp = db.insert(SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createSchedule returnId = " + temp + " name= " + name);
        return temp;
    }

    public Cursor getSchedules() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, null, null, null, null, null, null);
        Log.i("DBOpenHelper", "getSchedules " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getSchedules(long scheduleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {scheduleId + ""};
        Cursor cursor = db.query(SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, null, SchoolContract.TableSchedules.KEY_SCHEDULE_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getSchedules " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public int setSchedulesName(ArrayList<Long> schedulesId, String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableSchedules.COLUMN_NAME, name);
        int answer = 0;
        String stringSchedulesId = "";
        for (int i = 0; i < schedulesId.size(); i++) {
            stringSchedulesId = stringSchedulesId + schedulesId.get(i) + " | ";
            if (db.update(SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, contentName, SchoolContract.TableSchedules.KEY_SCHEDULE_ID + " = ?",new String[]{"" + schedulesId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "setSchedulesName name= " + name + " id= " + stringSchedulesId + " return = " + answer);
        db.close();
        return answer;
    }

    public int deleteSchedules(ArrayList<Long> schedulesId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = 0;
        String stringSchedulesId = "";
        for (int i = 0; i < schedulesId.size(); i++) {
            stringSchedulesId = stringSchedulesId + schedulesId.get(i) + " | ";
            if (db.delete(SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, SchoolContract.TableSchedules.KEY_SCHEDULE_ID + " = ?", new String[]{"" + schedulesId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "deleteSchedules id= " + stringSchedulesId + " return = " + answer);
        db.close();
        return answer;
    }


    //уроки
    public long createLesson(String name, long scheduleId, long dateBegin, long dateEnd, long classId, long cabinetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLessons.COLUMN_NAME, name);
        values.put(SchoolContract.TableLessons.KEY_SCHEDULE_ID, scheduleId);
        values.put(SchoolContract.TableLessons.COLUMN_DATE_BEGIN, dateBegin);
        values.put(SchoolContract.TableLessons.COLUMN_DATE_END, dateEnd);
        values.put(SchoolContract.TableLessons.KEY_CLASS_ID, classId);
        values.put(SchoolContract.TableLessons.KEY_CABINET_ID, cabinetId);
        long temp = db.insert(SchoolContract.TableLessons.NAME_TABLE_LESSONS, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createLesson returnId = " + temp + " name= " + name + " scheduleId=" + scheduleId + " dateBegin=" + dateBegin + " dateEnd=" + dateEnd + " classId=" + classId + " cabinetId=" + cabinetId);
        return temp;
    }

    public int setLessonsNames(ArrayList<Long> lessonId, String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableSchedules.COLUMN_NAME, name);
        int answer = 0;
        String stringLessonsId = "";
        for (int i = 0; i < lessonId.size(); i++) {
            stringLessonsId = stringLessonsId + lessonId.get(i) + " | ";
            if (db.update(SchoolContract.TableLessons.NAME_TABLE_LESSONS, contentName, SchoolContract.TableLessons.KEY_LESSON_ID + " = ?", new String[]{"" + lessonId.get(i)}) == 1)
                answer++;
        }
        Log.i("DBOpenHelper", "setLessonsNames name= " + name + " id= " + stringLessonsId + " return = " + answer);
        db.close();
        return answer;
    }

    public int setLessonParameters(long lessonId,String lessonNames, long scheduleId, long dateBegin, long dateEnd, long classId, long cabinetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableLessons.COLUMN_NAME, lessonNames);
        contentName.put(SchoolContract.TableLessons.KEY_SCHEDULE_ID, scheduleId);
        contentName.put(SchoolContract.TableLessons.COLUMN_DATE_BEGIN, dateBegin);
        contentName.put(SchoolContract.TableLessons.COLUMN_DATE_END, dateEnd);
        contentName.put(SchoolContract.TableLessons.KEY_CLASS_ID, classId);
        contentName.put(SchoolContract.TableLessons.KEY_CABINET_ID, cabinetId);
        int answer = db.update(SchoolContract.TableLessons.NAME_TABLE_LESSONS, contentName, SchoolContract.TableLessons.KEY_LESSON_ID + " = ?", new String[]{Long.toString(lessonId)});
        db.close();
        return answer;
    }

    public Cursor getLessonsByScheduleId(long scheduleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {scheduleId + ""};
        Cursor cursor = db.query(SchoolContract.TableLessons.NAME_TABLE_LESSONS, null, SchoolContract.TableLessons.KEY_SCHEDULE_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLessonsByScheduleId scheduleId=" + scheduleId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
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


    //работа с бд
    public void restartTable() {//создание бд заново
        onUpgrade(this.getReadableDatabase(), 0, 100);
    }


//    public boolean addPerson(String COLUMN_NAME, String add) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//
//        contentValues.put(COLUMN_NAME, COLUMN_NAME);
//        contentValues.put(COLUMN_ADD, add);
//
//        db.insert(TABLE_NAME, null, contentValues);
//        db.close();
//        return true;
//    }
//
//    public Cursor getPerson(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String sql = "SELECT * FROM Persons WHERE id=" + id + ";";
//        Cursor c = db.rawQuery(sql, null);
//        return c;
//    }
}

