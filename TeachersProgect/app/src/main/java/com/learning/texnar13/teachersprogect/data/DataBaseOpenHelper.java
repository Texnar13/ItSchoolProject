package com.learning.texnar13.teachersprogect.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;

public class DataBaseOpenHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;


    public DataBaseOpenHelper(Context context) {
        super(context, SchoolContract.DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        int i = 1/0;//та самая строка
//        db.execSQL("DROP TABLE IF EXIST " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + ";");
//        db.execSQL("DROP TABLE IF EXIST " + SchoolContract.TableDesks.NAME_TABLE_DESKS + ";");
//        db.execSQL("DROP TABLE IF EXIST " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + ";");
//        db.execSQL("DROP TABLE IF EXIST " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + ";");
//        db.execSQL("DROP TABLE IF EXIST " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + ";");
//        db.execSQL("DROP TABLE IF EXIST " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + ";");
//        db.execSQL("DROP TABLE IF EXIST " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + ";");
        updateDatabase(db, 0, 1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {//если база версии 1 и выше, то она не запустит этот код

            String sql = "CREATE TABLE " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "( " + SchoolContract.TableCabinets.KEY_CABINET_ID + " INTEGER AUTO_INCREMENT, " +//<--- работает только так а это--> не даёт сохранить INTEGER PRIMARY KEY AUTOINCREMENT
                    SchoolContract.TableCabinets.COLUMN_NAME + " VARCHAR ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableDesks.NAME_TABLE_DESKS + "( " + SchoolContract.TableDesks.KEY_DESK_ID + " INTEGER AUTO_INCREMENT, " +
                    SchoolContract.TableDesks.COLUMN_X + " INTEGER, " +
                    SchoolContract.TableDesks.COLUMN_Y + " INTEGER, " +
                    SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES + " INTEGER, " +
                    SchoolContract.TableDesks.KEY_CABINET_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableDesks.KEY_CABINET_ID + ") REFERENCES " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "(" + SchoolContract.TableCabinets.KEY_CABINET_ID + ") ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " ( " + SchoolContract.TablePlaces.KEY_PLACE_ID + " INTEGER AUTO_INCREMENT, " +
                    SchoolContract.TablePlaces.KEY_DESK_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TablePlaces.KEY_DESK_ID + ") REFERENCES " + SchoolContract.TableDesks.NAME_TABLE_DESKS + " (" + SchoolContract.TableDesks.KEY_DESK_ID + ") ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " ( " + SchoolContract.TableClasses.KEY_CLASS_ID + " INTEGER AUTO_INCREMENT, " +
                    SchoolContract.TableClasses.COLUMN_CLASS_NAME + " VARCHAR ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " ( " + SchoolContract.TableLearners.KEY_LEARNER_ID + " INTEGER AUTO_INCREMENT, " +
                    SchoolContract.TableLearners.COLUMN_FIRST_NAME + " VARCHAR, " +
                    SchoolContract.TableLearners.COLUMN_SECOND_NAME + " VARCHAR, " +
                    SchoolContract.TableLearners.KEY_CLASS_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearners.KEY_CLASS_ID + ") REFERENCES " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + " (" + SchoolContract.TableClasses.KEY_CLASS_ID + ") ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + " ( " + SchoolContract.TableLearnersOnPlaces.KEY_ATTITUDES_ID + " INTEGER AUTO_INCREMENT, " +
                    SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + " INTEGER, " +
                    SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + " INTEGER, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + "), " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID + ") REFERENCES " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + " (" + SchoolContract.TablePlaces.KEY_PLACE_ID + ") ); ";
            db.execSQL(sql);

            sql = "CREATE TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + " ( " + SchoolContract.TableLearnersGrades.KEY_GRADE_ID + " INTEGER AUTO_INCREMENT, " +
                    SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " INTEGER, " +
                    SchoolContract.TableLearnersGrades.COLUMN_GRADE + " INTEGER, " +
                    SchoolContract.TableLearnersGrades.COLUMN_DATE + " VARCHAR, " +
                    "FOREIGN KEY(" + SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + ") REFERENCES " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + " (" + SchoolContract.TableLearners.KEY_LEARNER_ID + ") ); ";
            db.execSQL(sql);
        }
    }

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

    public long createLearner(String secondName, String name, long class_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearners.COLUMN_SECOND_NAME, secondName);
        values.put(SchoolContract.TableLearners.COLUMN_FIRST_NAME, name);
        values.put(SchoolContract.TableClasses.KEY_CLASS_ID, class_id);
        long temp = db.insert(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, values);//-1 = ошибка ввода

        db.close();
        Log.i("DBOpenHelper", "createLearner returnId= " + temp + " secondName= " + secondName + " name= " + name + " class_id= " + class_id);
        return temp;
    }

    public long createCabinet(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableCabinets.COLUMN_NAME, name);
        long temp = db.insert(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createCabinet returnId = " + temp + " name= " + name);
        return temp;
    }

    public long createDesk(int numberOfPlaces, int x, int y, long cabinet_id) {//исправил--- при создании парты должны создаваться места, и может возвращать всё одним обьектом? Или в главном методе устроить алгоритм, создания мест в цикле и сохранения их в массив.
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES, numberOfPlaces);
        values.put(SchoolContract.TableDesks.COLUMN_X, x);
        values.put(SchoolContract.TableDesks.COLUMN_Y, y);
        values.put(SchoolContract.TableCabinets.KEY_CABINET_ID, cabinet_id);
        long temp = db.insert(SchoolContract.TableDesks.NAME_TABLE_DESKS, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createDesk returnId = " + temp + " numberOfPlaces= " + numberOfPlaces + " x= " + x + " y= " + y + " cabinet_id= " + cabinet_id);
        return temp;
    }

    public long createPlace(long deskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableDesks.KEY_DESK_ID, deskId);
        long temp = db.insert(SchoolContract.TablePlaces.NAME_TABLE_PLACES, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "createPlace returnId = " + temp + " desk_id= " + deskId);
        return temp;
    }

    public long setLearnerOnPlace(long learner_id, long place_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearners.KEY_LEARNER_ID, learner_id);
        values.put(SchoolContract.TablePlaces.KEY_PLACE_ID, place_id);
        long temp = db.insert(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES, null, values);//-1 = ошибка ввода
        db.close();
        Log.i("DBOpenHelper", "setLearnerOnPlace returnId = " + temp + " learner_id= " + learner_id + " place_id= " + place_id);
        return temp;
    }


    public Cursor getDesksByCabinetId(long cabinetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        //String[] selectionArgs = {cabinetId + ""};
        Cursor cursor = db.query(SchoolContract.TableDesks.NAME_TABLE_DESKS, null, SchoolContract.TableDesks.KEY_CABINET_ID + " = ?", new String[]{cabinetId + ""}, null, null, null);//выяснил, не работает проверка в методе query
        Log.i("DBOpenHelper", "getDesksByCabinetId cabinetId=" + cabinetId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    /*todo если поменять SchoolContract.TableDesks.KEY_CABINET_ID на SchoolContract.TableCabinets.KEY_CABINET_ID почему-то выдаёт нужный запрос
    * по идее метод выше должен извлекать из таблицы парты все ряды в которых id кабинета равен искомому, следовательно нужно использовать имя столбца из таблицы парты, но ничего невыходит
    * пробовал удалять foreign key тот же результат
    * */


    public Cursor getPlacesByDeskId(long deskId) {//todo также
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TablePlaces.NAME_TABLE_PLACES, null, SchoolContract.TablePlaces.KEY_DESK_ID + " =?", new String[] {deskId + ""}, null, null, null);
        Log.i("DBOpenHelper", "getPlacesByDeskId deskId=" + deskId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getClasses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableClasses.NAME_TABLE_CLASSES, null, null, null, null, null, null);
        Log.i("DBOpenHelper", "getClasses " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getCabinets() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableCabinets.NAME_TABLE_CABINETS, null, null, null, null, null, null);
        Log.i("DBOpenHelper", "getCabinets " + cursor + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }

    public Cursor getLearnersByClassId(long classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {classId + ""};
        Cursor cursor = db.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS, null, SchoolContract.TableLearners.KEY_CLASS_ID + " = ?", selectionArgs, null, null, null);
        Log.i("DBOpenHelper", "getLearnersByClassId classId=" + classId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
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

