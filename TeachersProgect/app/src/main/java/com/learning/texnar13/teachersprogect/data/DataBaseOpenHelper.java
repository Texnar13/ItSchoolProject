package com.learning.texnar13.teachersprogect.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON");//нужен для удаления каскадом
        super.onOpen(db);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DataBaseOpenHelper", "updateDatabase old=" + oldVersion + " new=" + newVersion);

        if (oldVersion < 5) {//если база версии 5 и выше, то она не запустит этот код
            db.execSQL("PRAGMA foreign_keys = OFF");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableDesks.NAME_TABLE_DESKS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME + ";");
            db.execSQL("PRAGMA foreign_keys = ON");

//кабинет
            String sql = "CREATE TABLE " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "( " + SchoolContract.TableCabinets.KEY_CABINET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
            sql = "CREATE TABLE " + SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME + " ( " + SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID + " INTEGER, " +
                    //--
                    SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID + " INTEGER, " +
                    //--
                    SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " INTEGER, " +
                    SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableLessons.NAME_TABLE_LESSONS + " (" + SchoolContract.TableLessons.KEY_LESSON_ID + ") ON DELETE CASCADE ); ";
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

    public long getLearnerIdByClassIdAndPlaceId(long classId, long placeId) {
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

    private Cursor getAttitudeByLearnerIdAndPlaceId(Long learnerId, Long placeId) {
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
    public long createGrade(long learnerId, long grade, long lessonId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID, learnerId);
        values.put(SchoolContract.TableLearnersGrades.COLUMN_GRADE, grade);
        values.put(SchoolContract.TableLearnersGrades.KEY_LESSON_ID, lessonId);
        long temp = db.insert(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createGrade returnId = " + temp + " learnerId= " + learnerId + " grade= " + grade + " lessonId= " + lessonId);
        return temp;
    }


    //уроки
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

    //todo лучше вообще убрать этот метод, т.к. он будет бесполезен
    public int setLessonsNames(ArrayList<Long> lessonId, String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableLessons.COLUMN_NAME, name);
        //todo вместо tableLessons строкой выше стояло tableSchedules я заменил правильно ли это? надо проверить
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

    public int setLessonParameters(long lessonId, String lessonName, long classId
                                   //,long cabinetId
    ) {
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

    public long setLessonTimeAndCabinet(long lessonId, long cabinetId, Date startTime, Date endTime) {
        //date и database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID, lessonId);
        //--
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID, cabinetId);
        //--
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN, startTime.getTime());
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END, endTime.getTime());
        long answer = db.insert(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME, null, contentValues);
        Log.i("DBOpenHelper", "setLessonTime lessonId= " + lessonId + " cabinetId= " + cabinetId + " startTime= " + startTime.toString() + " endTime= " + endTime.toString() + " return = " + answer);
        return answer;
    }

    public int editLessonTimeAndCabinet(long attitudeId, long lessonId, long cabinetId, Date startTime, Date endTime) {
        //date и database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID, lessonId);
        //--
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID, cabinetId);
        //--
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN, startTime.getTime());
        contentValues.put(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END, endTime.getTime());
        int answer = db.update(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME, contentValues, SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " = ?", new String[]{Long.toString(attitudeId)});

        Log.i("DBOpenHelper", "editLessonTimeAndCabinet attitudeId= " + attitudeId + " lessonId= " + lessonId + " cabinetId= " + cabinetId + " startTime= " + startTime.toString() + " endTime= " + endTime.toString() + " return = " + answer);
        return answer;
    }

    public Cursor getLessonAttitudeById(long attitudeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {attitudeId + ""};
        Cursor cursor = db.query(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME, null, SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLessonAttitudeById " + " id=" + attitudeId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public ArrayList<Long> getLessonsAttitudesIdByTimePeriod(Calendar periodStart, Calendar periodEnd) {
        //проверяется только дата начала уроков
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {
                periodStart.getTime().getTime() + ""
                , periodEnd.getTime().getTime() + ""
        };
        Cursor cursor = db.query(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME,
                null, SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " >= ? AND " +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " <= ?",
                selectionArgs, null, null, null);

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

    public long getLessonsAttitudesIdByTime(Calendar time) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {
                time.getTime().getTime() + ""
                , time.getTime().getTime() + ""
        };
        Cursor cursor = db.query(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME,
                null, SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN + " <= ? AND " +
                        SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_END + " >= ?",
                selectionArgs, null, null, null);
        long answer;
        if(cursor.getCount() !=0){
        cursor.moveToFirst();
            answer = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID));
            cursor.close();
            Log.i("DBOpenHelper", "getLessonsAttitudesIdByTimePeriod time=" + time.getTime().getTime()  + " answer=" + answer);
            return answer;
        }else{
            cursor.close();
            Log.i("DBOpenHelper", "getLessonsAttitudesIdByTimePeriod time=" + time.getTime().getTime()  + " answer=" + (-1));
            return -1;
        }
    }

    public int deleteLessonTimeAndCabinet(long attitudeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableLessonAndTimeWithCabinet.NAME_TABLE_LESSONS_AND_TIME, SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_AND_TIME_ATTITUDE_ID+ " = ?", new String[]{"" + attitudeId});
        Log.i("DBOpenHelper", "deleteLessonTimeAndCabinet attitudeId= " + attitudeId + " return = " + answer);
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
