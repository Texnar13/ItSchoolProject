package com.learning.texnar13.teachersprogect.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Xml;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.settings.ImportModel.ImportDataBaseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataBaseOpenHelper extends SQLiteOpenHelper {
    private static final boolean IS_DEBUG = true;
    private static final int DB_VERSION = 21; // googlePlay -> 18
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

        if (oldVersion < 1) {
            // приложение либо новое, либо удаляются данные

            // чистка бд
            // todo (проверка перед каждым выпуском) этот список нужно синхронизировать
            //  - со списком в обновлениях БД,
            //  - с моделью импорта всех данных,
            //  - с методом экспорта данных,
            //  - тестовой моделью БД
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
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES + ";");
            db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT + ";");
            db.execSQL("PRAGMA foreign_keys = ON;");

            // настройки
            db.execSQL(SchoolContract.TableSettingsData.CREATE_TABLE_STRING);

            // кабинет
            db.execSQL(SchoolContract.TableCabinets.CREATE_TABLE_STRING);

            // парта
            db.execSQL(SchoolContract.TableDesks.CREATE_TABLE_STRING);

            // место
            db.execSQL(SchoolContract.TablePlaces.CREATE_TABLE_STRING);

            // класс
            db.execSQL(SchoolContract.TableClasses.CREATE_TABLE_STRING);

            //ученик
            db.execSQL(SchoolContract.TableLearners.CREATE_TABLE_STRING);

            //ученик-место
            db.execSQL(SchoolContract.TableLearnersOnPlaces.CREATE_TABLE_STRING);

            // типы оценок
            db.execSQL(SchoolContract.TableLearnersGradesTitles.CREATE_TABLE_STRING);

            // типы пропусков
            db.execSQL(SchoolContract.TableLearnersAbsentTypes.CREATE_TABLE_STRING);

            //оценки учеников
            db.execSQL(SchoolContract.TableLearnersGrades.CREATE_TABLE_STRING);

            //предмет
            db.execSQL(SchoolContract.TableSubjects.CREATE_TABLE_STRING);

            //урок и его время проведения
            db.execSQL(SchoolContract.TableSubjectAndTimeCabinetAttitude.CREATE_TABLE_STRING);

            // профили статистики
            db.execSQL(SchoolContract.TableStatisticsProfiles.CREATE_TABLE_STRING);

            // комментарий к уроку
            db.execSQL(SchoolContract.TableLessonComment.CREATE_TABLE_STRING);


            // --- дефолтные записи ---

            {// ---- вставляем одну запись настроек ----
                ContentValues values = new ContentValues();
                values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, "simpleName");
                db.insert(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, values);
            }
            {// ---- вставляем одну запись "работа на уроке" ----  <-- эту запись нельзя удалить но можно переименовать
                String name = context.getResources().getString(R.string.db_table_grade_text_first_default_value);
                ContentValues values = new ContentValues();
                values.put(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, name);
                values.put(SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID, 1);
                db.insert(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, null, values);
            }
            {// ---- вставляем 2 записи "н", "бол." ----  <-- запись "н" нельзя удалить но можно переименовать
                ContentValues values1 = new ContentValues();
                values1.put(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID, 1);
                values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
                        context.getResources().getString(R.string.db_table_grades_abs_1_default_value));
                values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
                        context.getResources().getString(R.string.db_table_grades_abs_1_long_default_value));
                db.insert(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, null, values1);

                ContentValues values2 = new ContentValues();
                values2.put(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID, 2);
                values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
                        context.getResources().getString(R.string.db_table_grades_abs_2_default_value));
                values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
                        context.getResources().getString(R.string.db_table_grades_abs_2_long_default_value));
                db.insert(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, null, values2);
            }

        } else {

            // -------------------------------------------------------------------------------------
            // --------------- иначе операции по сохранению данных на старых версиях ---------------
            // -------------------------------------------------------------------------------------

            if (oldVersion < 17) {

                // -- добавляем колонку номера урока и даты урока, заменяющие собой время урока --
                db.execSQL("ALTER TABLE lessonAndTimeWithCabinet ADD COLUMN lessonNumber INTEGER DEFAULT 0 NOT NULL;");
                db.execSQL("ALTER TABLE lessonAndTimeWithCabinet ADD COLUMN lessonDate TIMESTRING DEFAULT \"0000-00-00\" NOT NULL;");

                // получаем время уроков
                int[][] time;
                {
                    Cursor timeCursor = db.query("settingsData", null, "_id = 1", null, null, null, null);
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
                            json = new JSONObject(timeCursor.getString(timeCursor.getColumnIndexOrThrow("time")));
                            beginHour = json.getJSONArray("beginHour");
                            beginMinute = json.getJSONArray("beginMinute");
                            endHour = json.getJSONArray("endHour");
                            endMinute = json.getJSONArray("endMinute");

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
                SimpleDateFormat timeDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


                // получаем все уроки
                Cursor cursor = db.query("lessonAndTimeWithCabinet", null, null, null, null, null, null);

                // пробегаемся по урокам
                while (cursor.moveToNext()) {

                    // получаем время этого урока
                    GregorianCalendar beginDate = new GregorianCalendar();
                    GregorianCalendar endDate = new GregorianCalendar();

                    try {
                        beginDate.setTime(timeDateFormat.parse(
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(
                                                "lessonDateBegin"
                                        )
                                )
                        ));
                        endDate.setTime(timeDateFormat.parse(
                                cursor.getString(
                                        cursor.getColumnIndexOrThrow(
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
                            values.put("lessonNumber", i);
                            db.update("lessonAndTimeWithCabinet", values,
                                    "_id = " + cursor.getLong(cursor.getColumnIndexOrThrow("_id")), null);
                            break;
                        }
                    }
                    // если вдруг время не нашлось, оставляем номер урока нулевым
                    // а дату ставим просто из полученного календаря
                    ContentValues values = new ContentValues();
                    values.put("lessonDate", dateFormat.format(beginDate.getTime()));
                    db.update("lessonAndTimeWithCabinet", values,
                            "_id = " + cursor.getLong(cursor.getColumnIndexOrThrow("_id")), null);
                }

                cursor.close();


                // -- добавляем оценкам эти поля --
                db.execSQL("ALTER TABLE learnersGrades ADD COLUMN lessonNumber INTEGER DEFAULT 0 NOT NULL;");
                db.execSQL("ALTER TABLE learnersGrades ADD COLUMN date TIMESTRING DEFAULT \"0000-00-00\" NOT NULL;");

                // заполняем поля даты и номера урока в оценках

                // получаем все оценки
                Cursor gradesCursor = db.query("learnersGrades", null, null, null, null, null, null);

                // пробегаемся по оценкам
                while (gradesCursor.moveToNext()) {

                    // получаем время этого урока
                    GregorianCalendar date = new GregorianCalendar();

                    try {
                        date.setTime(timeDateFormat.parse(
                                gradesCursor.getString(gradesCursor.getColumnIndexOrThrow("time"))
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
                            values.put("lessonNumber", i);
                            db.update("learnersGrades", values,
                                    "_id = " + gradesCursor.getLong(gradesCursor.getColumnIndexOrThrow("_id")),
                                    null);
                            break;
                        }
                    }

                    // если вдруг время не нашлось, оставляем номер урока нулевым
                    // а дату ставим просто из полученного календаря
                    ContentValues values = new ContentValues();
                    values.put(
                            "date",
                            dateFormat.format(date.getTime())
                    );
                    db.update("learnersGrades", values,
                            "_id = " + gradesCursor.getLong(gradesCursor.getColumnIndexOrThrow("_id")),
                            null
                    );
                }
                gradesCursor.close();
            }

            if (oldVersion < 18) {
                SimpleDateFormat timeDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // меняем в статистике

                // -- добавляем статистике эти поля --
                db.execSQL("ALTER TABLE statisticsProfiles ADD COLUMN startDate TIMESTRING DEFAULT \"1000-00-00\" NOT NULL;");
                db.execSQL("ALTER TABLE statisticsProfiles ADD COLUMN endDate TIMESTRING DEFAULT \"1000-00-00\" NOT NULL;");


                // получаем все статистики
                Cursor statCursor = db.query("statisticsProfiles", null, null, null, null, null, null);


                // пробегаемся по статистикам
                while (statCursor.moveToNext()) {

                    // -- начальная дата --

                    // проверяем строку
                    String startDate = statCursor.getString(statCursor.getColumnIndexOrThrow("startTime"));
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
                    values.put("startDate", startDate);
                    db.update("statisticsProfiles", values,
                            "_id = " + statCursor.getLong(statCursor.getColumnIndexOrThrow("_id")),
                            null);


                    // -- конечная дата --

                    // проверяем строку
                    String endDate = statCursor.getString(statCursor.getColumnIndexOrThrow("endTime"));
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
                    valuesEnd.put("endDate", endDate);
                    db.update("statisticsProfiles", valuesEnd,
                            "_id = " + statCursor.getLong(statCursor.getColumnIndexOrThrow("_id")),
                            null);

                }
                statCursor.close();
            }

            // -------------------------------------------------------------------------------------
            // ---------------------------------- еще не на проде ----------------------------------
            // -------------------------------------------------------------------------------------

            if (oldVersion < 19) {
                // типы оценок
                db.execSQL(
                        "CREATE TABLE " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES +
                                " ( " + SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE + " TEXT);"
                );
                {// ---- вставляем одну запись "работа на уроке" ----  <-- эту запись нельзя удалить но можно переименовать
                    String name = context.getResources().getString(R.string.db_table_grade_text_first_default_value);
                    ContentValues values = new ContentValues();
                    values.put(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, name);
                    values.put(SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID, 1);
                    db.insert(
                            SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES,
                            null,
                            values);
                }

                // добавляем типы пропусков
                db.execSQL(
                        "CREATE TABLE " + SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES +
                                " ( " + SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME + " TEXT NOT NULL, " +
                                SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME + " TEXT NOT NULL);"
                );
                {// ---- вставляем три записи "н", "бол.", "спр."  ----  <-- запись "н" нельзя удалить но можно переименовать
                    ContentValues values1 = new ContentValues();
                    values1.put(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID, 1);
                    values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
                            context.getResources().getString(R.string.db_table_grades_abs_1_default_value));
                    values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
                            context.getResources().getString(R.string.db_table_grades_abs_1_long_default_value));
                    db.insert(
                            SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                            null,
                            values1);

                    ContentValues values2 = new ContentValues();
                    values2.put(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID, 2);
                    values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
                            context.getResources().getString(R.string.db_table_grades_abs_2_default_value));
                    values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
                            context.getResources().getString(R.string.db_table_grades_abs_2_long_default_value));
                    db.insert(
                            SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                            null, values2);
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

                // переименовываем старую таблицу
                db.execSQL("ALTER TABLE learnersGrades RENAME TO learnersGrades_old;");
                // создаём новую таблицу
                db.execSQL(SchoolContract.TableLearnersGrades.CREATE_TABLE_STRING);
                // переносим значения
                db.execSQL("INSERT INTO " +
                        SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES +
                        " SELECT " +
                        SchoolContract.TableLearnersGrades.KEY_ROW_ID + ", " +
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
                            SchoolContract.TableLearnersGrades.COLUMN_DATE + " == \"" + allGrades.getString(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMN_DATE)) + "\" AND " +
                                    SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER + " == " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER)) + " AND " +
                                    SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID + " == " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID)) + " AND " +
                                    SchoolContract.TableLearnersGrades.KEY_LEARNER_ID + " == " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID)) + " AND " +
                                    SchoolContract.TableLearnersGrades.KEY_ROW_ID + " != " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                            null, null, null, null
                    );

                    // Если стоит пропуск
                    if (allGrades.getInt(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0])) == -2) {
                        // выставляем первый тип пропуска и пустую оценку
                        ContentValues content = new ContentValues();
                        content.put(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, 1);
                        content.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], 0);
                        db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                content,
                                SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                                null
                        );

                        // удаляем все остальные оценки (которых на самом деле и не должно быть)
                        while (sameGrades.moveToNext()) {
                            db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                    SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
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

                            long grade = sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0]));
                            ContentValues content = new ContentValues();
                            if (grade == -2) {

                                // выставляем первый тип пропуска и пустую оценку
                                content.put(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, 1);
                                content.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], 0);
                                content.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], 1);
                                db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                        content,
                                        SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                                        null
                                );


                                // удаляем все остальные оценки (которых на самом деле и не должно быть)
                                while (sameGrades.moveToNext()) {
                                    db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                            SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                                            null
                                    );
                                }

                            } else {
                                // ставим на вторую позицию оценку из первой временной
                                content.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[1],
                                        sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0]))
                                );
                                content.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1],
                                        sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0]))
                                );
                                db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                        content,
                                        SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                                        null
                                );
                                // удаляем временную
                                db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                        SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                                        null
                                );

                                // переходим к проверке второй временной оценки
                                if (sameGrades.moveToNext()) {
                                    grade = sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0]));
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
                                                SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                                                null
                                        );


                                        // удаляем все остальные оценки (которых на самом деле и не должно быть)
                                        while (sameGrades.moveToNext()) {
                                            db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                                    SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                                                    null
                                            );
                                        }

                                    } else {
                                        // ставим на третью позицию оценку из первой временной
                                        content2.put(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[2],
                                                sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0]))
                                        );
                                        content2.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2],
                                                sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0]))
                                        );
                                        db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                                content2,
                                                SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + allGrades.getLong(allGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
                                                null
                                        );
                                        // удаляем временную
                                        db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                                                SchoolContract.TableLearnersGrades.KEY_ROW_ID + " == " + sameGrades.getLong(sameGrades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)),
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
                                SchoolContract.TableLessonComment.KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                SchoolContract.TableLessonComment.KEY_LESSON_ID + " INTEGER NOT NULL, " +
                                SchoolContract.TableLessonComment.COLUMN_LESSON_DATE + " TIMESTRING NOT NULL, " +
                                SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT + " TEXT NOT NULL, " +
                                "FOREIGN KEY(" + SchoolContract.TableLessonComment.KEY_LESSON_ID + ") REFERENCES " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE + " (" + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID + ") ON DELETE CASCADE);"
                );

            }

            if (oldVersion < 20) {
                // добавляем комментарий ученику
                db.execSQL("ALTER TABLE " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS +
                        " ADD COLUMN " + SchoolContract.TableLearners.COLUMN_COMMENT + " VARCHAR;");
                // удаляем колонку с размером интерфейса из настроек
                //db.execSQL("");
            }

            if (oldVersion < 21) {
                // конец повторов урока
                db.execSQL("ALTER TABLE " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE +
                        " ADD COLUMN " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE + " TIMESTRING;");
            }
        }
    }


//--------------------------------------------------------------------------------------------------
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// -------------------------------------- методы доступа -------------------------------------------
// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//--------------------------------------------------------------------------------------------------


    //настройки
    public void createNewSettingsProfileWithId1(String profileName, int interfaceSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSettingsData.KEY_ROW_ID, 1);
        values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, profileName);
        values.put(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, interfaceSize);
        long temp = db.insert(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, values);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "createSettingsProfile returnId = " + temp + " profileName= " + profileName);
    }

    public void setSettingsLocale(long profileId, String locale) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSettingsData.COLUMN_LOCALE, locale);
        int temp = db.update(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, values,
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + profileId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsProfileParameters return = " + temp + " profileId= " + profileId + " locale= " + locale);
    }

    public String getSettingsLocale(long profileId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null,
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + profileId,
                null, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return SchoolContract.TableSettingsData.COLUMN_LOCALE_DEFAULT_CODE;
        }
        cursor.moveToFirst();
        String answer = cursor.getString(cursor.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_LOCALE));
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
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + profileId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsProfileParameters return = " + temp + " profileId= " + profileId + " maxGrade= " + maxGrade);
        return temp;
    }

    public int getSettingsMaxGrade(long profileId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null,
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + profileId,
                null, null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return -1;
        }
        cursor.moveToFirst();
        int answer = cursor.getInt(cursor.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER));
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
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + profileId, null);

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
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + profileId,
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
            json = new JSONObject(settingsCursor.getString(settingsCursor.getColumnIndexOrThrow(
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

    public long setSettingsAreTheGradesColoredByProfileId(long profileId, boolean areColored) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (areColored) {
            values.put(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED, 1);
        } else {
            values.put(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED, 0);
        }
        int temp = db.update(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, values,
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + profileId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSettingsAreTheGradesColoredByProfileId return = " + temp + " profileId= " + profileId + " areColored= " + areColored);

        return temp;
    }

    public boolean getSettingsAreTheGradesColoredByProfileId(long profileId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null,
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + profileId,
                null, null, null, null);
        boolean answer = false;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            if (cursor.getLong(cursor.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED)) != 0) {
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
                SchoolContract.TableSettingsData.KEY_ROW_ID + " = " + id,
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

    public void setStatisticTime(long id, String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE, startDate);
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE, endDate);
        int temp = db.update(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                values, SchoolContract.TableStatisticsProfiles.KEY_ROW_ID + " = " + id, null);
        db.close();
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setStatisticTime id=" + id + " startDate=" + startDate + " endDate=" + endDate + " return=" + temp);
    }

    public void setStatisticName(long periodId, String periodName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME, periodName);
        int temp = db.update(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES, values,
                SchoolContract.TableStatisticsProfiles.KEY_ROW_ID + " = " + periodId, null);
        db.close();
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setStatisticParameters id=" + periodId + " periodName=" + periodName + " return=" + temp);
    }

    public void removeStatisticProfile(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int temp = db.delete(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                SchoolContract.TableStatisticsProfiles.KEY_ROW_ID + " = " + id, null);
        db.close();
        if (IS_DEBUG)
            Log.i(LOG_TAG, "removeStatisticProfile id=" + id + " return=" + temp);
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
                SchoolContract.TableClasses.KEY_ROW_ID + " = " + classId,
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
                SchoolContract.TableClasses.KEY_ROW_ID + " = " + classId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setClassesNames name = " + name + " id= " + classId + " return = " + answer);
        return answer;
    }

    public int deleteClass(long classesId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableClasses.NAME_TABLE_CLASSES,
                SchoolContract.TableClasses.KEY_ROW_ID + " = " + classesId, null);
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
                SchoolContract.TableLearners.KEY_ROW_ID + " = " + learnerId, null, null, null, null);
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
                    desksId.add(desksCursor.getLong(desksCursor.getColumnIndexOrThrow(SchoolContract.TableDesks.KEY_ROW_ID)));
                }
                desksCursor.close();
            }

            for (int i = 0; i < desksId.size(); i++) {
                Cursor placeCursor = this.getPlacesByDeskId(desksId.get(i));
                while (placeCursor.moveToNext()) {
                    placesId.add(placeCursor.getLong(placeCursor.getColumnIndexOrThrow(SchoolContract.TablePlaces.KEY_ROW_ID)));
                }
                placeCursor.close();
            }
        }
        ArrayList<Long> learnersId = new ArrayList<>();
        {//получаем id
            Cursor learnersCursor = this.getLearnersByClassId(classId);
            while (learnersCursor.moveToNext()) {
                learnersId.add(learnersCursor.getLong(learnersCursor.getColumnIndexOrThrow(SchoolContract.TableLearners.KEY_ROW_ID)));
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
                SchoolContract.TableLearners.KEY_ROW_ID + " = " + learnerId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setLearnerNameAndLastName name= " + name + " lastName= " + lastName + " id= " + learnerId + " return = " + answer);
        return answer;
    }

    public int deleteLearner(long learnerId) {// метод удаления ученика
        SQLiteDatabase db = this.getWritableDatabase();
        // удаляем ученика
        int answer = db.delete(SchoolContract.TableLearners.NAME_TABLE_LEARNERS,
                SchoolContract.TableLearners.KEY_ROW_ID + " = " + learnerId, null);
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
                SchoolContract.TableCabinets.KEY_ROW_ID + " = " + cabinetId, null, null, null, null);
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
                SchoolContract.TableCabinets.KEY_ROW_ID + " = " + cabinetId,
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
                contentName, SchoolContract.TableCabinets.KEY_ROW_ID + " = " + cabinetId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setCabinetName name= " + name + " id= " + cabinetId + " return = " + answer);
        return answer;
    }

    public int deleteCabinets(Long cabinetsId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableCabinets.NAME_TABLE_CABINETS,
                SchoolContract.TableCabinets.KEY_ROW_ID + " = " + cabinetsId, null);
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
                SchoolContract.TableDesks.KEY_ROW_ID + " = " + deskId, null);
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
                SchoolContract.TableDesks.KEY_ROW_ID + " = " + deskId, null);
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
                SchoolContract.TableLearnersOnPlaces.KEY_ROW_ID + " = " + attitudeId, null);
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
        else
            values.putNull(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID);
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
                SchoolContract.TableLearnersGrades.KEY_ROW_ID + " = " + gradeId,
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
        if (absTypeId == -1) {// если пропуска нет (-1), передаем просто null
            values.putNull(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID);
        } else {
            values.put(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, absTypeId);
        }
        long temp = db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                values, SchoolContract.TableLearnersGrades.KEY_ROW_ID + " = " + gradeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editGrade affectedRowsCount = " + temp + " gradeId= " + gradeId + " grade1= " + grade1 +
                    " grade2= " + grade2 + " grade3= " + grade3 + " absTypeId=" + absTypeId);
        return temp;//-1 = ошибка ввода
    }

    public long editGradeType(long gradeId, long type1, long type2, long type3) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], type1);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1], type2);
        values.put(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2], type3);
        long temp = db.update(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                values, SchoolContract.TableLearnersGrades.KEY_ROW_ID + " = " + gradeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editGradeType returnId = " + temp + " type= " + type1 + " gradeId= " + gradeId);
        return temp;//-1 = ошибка ввода
    }

    public long removeGrade(long gradeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long temp = db.delete(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                SchoolContract.TableLearnersGrades.KEY_ROW_ID + " = " + gradeId, null);
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
        long temp = db.update(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, values, SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID + " = " + typeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editGradesType returnId = " + temp + " newName= " + newName + " typeId= " + typeId);
        return temp;//-1 = ошибка ввода
    }

    public long removeGradesType(long gradeTypeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // удаляем этот тип (при удалении тип во всех оценках автоматически изменяется на тип по умолчанию)
        long temp = db.delete(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID + " = " + gradeTypeId, null);//-1 = ошибка ввода

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
        long temp = db.update(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, values, SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID + " = " + typeId, null);//-1 = ошибка ввода
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editAbsentType returnId = " + temp + " newName= " + newName + " typeId= " + typeId);
        return temp;
    }

    public long removeAbsentType(long absTypeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // удаляем этот тип (todo при удалении все пропуски с этим типом удалятся ???)
        long temp = db.delete(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID + " = " + absTypeId, null);//-1 = ошибка ввода
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

    public void setSubjectName(long subjectId, String subjectName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentName = new ContentValues();
        contentName.put(SchoolContract.TableSubjects.COLUMN_NAME, subjectName);
        db.update(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS, contentName, SchoolContract.TableSubjects.KEY_ROW_ID + " = " + subjectId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "setSubjectName id=" + subjectId + " subjectName=" + subjectName);
    }

    public Cursor getSubjectById(long subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS, null,
                SchoolContract.TableSubjects.KEY_ROW_ID + " = " + subjectId,
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

    public void deleteSubjects(ArrayList<Long> subjectsId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = 0;
        StringBuilder stringSubjectsId = new StringBuilder();
        for (int i = 0; i < subjectsId.size(); i++) {
            stringSubjectsId.append(stringSubjectsId).append(subjectsId.get(i)).append('|');
            if (db.delete(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS,
                    SchoolContract.TableSubjects.KEY_ROW_ID + " = " + subjectsId.get(i), null
            ) == 1)
                answer++;
        }
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteSubjects id= " + stringSubjectsId + " return = " + answer);
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
        contentValues.putNull(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE);
        long answer = db.insert(SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE, null, contentValues);
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
        int answer = db.update(SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE,
                contentValues,
                SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID + " = " + attitudeId,
                null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "editLessonTimeAndCabinet attitudeId= " + attitudeId + ' ' + contentValues + " return = " + answer);
        return answer;
    }

    public Cursor getSubjectAndTimeCabinetAttitudeById(long attitudeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE,
                null, SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID + " = " + attitudeId,
                null, null, null, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "getSubjectAndTimeCabinetAttitudeById " + " id=" + attitudeId + " number=" + cursor.getCount() + " content=" + Arrays.toString(cursor.getColumnNames()));
        return cursor;
    }


    public Cursor getSubjectAndTimeCabinetAttitudesByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE +
                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " = ?) OR ((" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?)))" +
                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ? AND (" +
                        SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE
                        + " ISNULL OR ? < " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE + ")))",
                new String[]{
                        date,
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
                        date,
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
	    		  AND (COLUMN_END_REPEAT_DATE ISNULL OR date < COLUMN_END_REPEAT_DATE)
	    	)
	    )
	    */
    }


    public Cursor getSubjectAndTimeCabinetAttitudesByDateAndLessonNumbersPeriod(String date, long startLessonNumber, long endLessonNumber) {

        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE +
                        // по времени
                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " BETWEEN ? AND ?) AND (" +
                        // по дате
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " = ?) OR ((" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?)))" +
                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ? AND (" +
                        SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE +
                        " ISNULL OR ? < " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE + "))))",
                new String[]{
                        Long.toString(startLessonNumber),
                        Long.toString(endLessonNumber),
                        date,
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
                        date,
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
	    		      AND (COLUMN_END_REPEAT_DATE ISNULL OR date < COLUMN_END_REPEAT_DATE)
	    		)
	    	)
	    )
	    */
    }


    public Cursor getSubjectAndTimeCabinetAttitudesByDateAndLessonNumbersPeriod(long subjectId, String date, long startLessonNumber, long endLessonNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE +
                        // по времени
                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " BETWEEN ? AND ?) AND (" +
                        // по дате
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " = ?) OR ((" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?)))" +
                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ? AND (" +
                        SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE +
                        " ISNULL OR ? < " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE + ")))" +
                        " AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID + " = " + subjectId + ")",
                new String[]{
                        Long.toString(startLessonNumber),
                        Long.toString(endLessonNumber),
                        date,
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
                        date,
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
	    		      AND (COLUMN_END_REPEAT_DATE ISNULL OR date < COLUMN_END_REPEAT_DATE)
	    		)
	    	) AND KEY_SUBJECT_ID = subjectId
	    )
	    */
    }



    public Cursor getSubjectAndTimeCabinetAttitudeByDateAndLessonNumber(String date, long lessonNumber) {//попадает ли переданное время в какой-либо из уроков
        return this.getReadableDatabase().rawQuery(
                "SELECT * FROM " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE +
                        " WHERE ((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER + " = ?) AND (" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " = ?) OR (" +
                        "(" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) OR " +
                        "((" + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT + " = ?) AND (strftime(\"%w\"," + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + ") == strftime(\"%w\", ?))" +
                        ") AND " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE + " < ? AND (" +
                        SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE +
                        " ISNULL OR ? < " + SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE + "))))",
                new String[]{
                        Long.toString(lessonNumber),
                        date,
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_DAILY),
                        Integer.toString(SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY),
                        date,
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
	    		      AND (COLUMN_END_REPEAT_DATE ISNULL OR date < COLUMN_END_REPEAT_DATE)
                )
            )
        )
        */
    }

    public void setEndRepeatSubjectAndTimeCabinetAttitude(long attitudeId, String endRepeat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE, endRepeat);
        db.update(SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE, values, SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID + " == " + attitudeId, null);
    }

    // удалить урок
    public void deleteSubjectAndTimeCabinetAttitude(long attitudeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int answer = db.delete(SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE,
                SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID + " = " + attitudeId, null);
        if (IS_DEBUG)
            Log.i(LOG_TAG, "deleteSubjectAndTimeCabinetAttitude attitudeId= " + attitudeId + " return = " + answer);
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
        db.update(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, values, SchoolContract.TableLessonComment.KEY_ROW_ID + " == " + commentId, null);
    }

    public Cursor getLessonCommentsByDateAndLesson(long lessonId, String date) {
        return getReadableDatabase().query(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, null,
                SchoolContract.TableLessonComment.KEY_LESSON_ID + " == " + lessonId + " AND " + SchoolContract.TableLessonComment.COLUMN_LESSON_DATE + " == \"" + date + "\"",
                null, null, null, null);
    }

    public void removeLessonCommentById(long lessonCommentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, SchoolContract.TableLessonComment.KEY_ROW_ID + " == " + lessonCommentId, null);
    }

    //работа с бд
    public void restartTable() {//создание бд заново
        onUpgrade(this.getReadableDatabase(), 0, 100);
    }


    public void writeXMLDataBaseInFile(OutputStreamWriter bw) throws RuntimeException, IOException {
        XmlSerializer serializer = Xml.newSerializer();
        SQLiteDatabase sqDB = getReadableDatabase();

        {
            // назначаем файл в который будем писать
            serializer.setOutput(bw);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", SchoolContract.DB_NAME);
            serializer.attribute("", "db_file_version", "1");
            // пишем содержимое таблиц
            {
                // таблица настроек
                {
                    serializer.startTag("", SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS);
                    Cursor settings = sqDB.query(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS,
                            null, null, null, null, null, null);
                    while (settings.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableSettingsData.KEY_ROW_ID, settings.getString(settings.getColumnIndexOrThrow(SchoolContract.TableSettingsData.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, settings.getString(settings.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME)));
                        serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_LOCALE, settings.getString(settings.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_LOCALE)));
                        serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE,
                                settings.isNull(settings.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE)) ?
                                        "null" :
                                        settings.getString(settings.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE))
                        );
                        serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER, settings.getString(settings.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER)));
                        serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_TIME, settings.getString(settings.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_TIME)));
                        serializer.attribute("", SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED, settings.getString(settings.getColumnIndexOrThrow(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED)));
                        serializer.endTag("", "element");
                    }
                    settings.close();
                    serializer.endTag("", SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS);
                }
                // таблица кабинетов
                {
                    serializer.startTag("", SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
                    Cursor cabinets = getCabinets();
                    while (cabinets.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableCabinets.KEY_ROW_ID, cabinets.getString(cabinets.getColumnIndexOrThrow(SchoolContract.TableCabinets.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER, cabinets.getString(cabinets.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER)));
                        serializer.attribute("", SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X, cabinets.getString(cabinets.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X)));
                        serializer.attribute("", SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y, cabinets.getString(cabinets.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y)));
                        serializer.attribute("", SchoolContract.TableCabinets.COLUMN_NAME, cabinets.getString(cabinets.getColumnIndexOrThrow(SchoolContract.TableCabinets.COLUMN_NAME)));
                        serializer.endTag("", "element");
                    }
                    cabinets.close();
                    serializer.endTag("", SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
                }
                // таблица парт
                {
                    serializer.startTag("", SchoolContract.TableDesks.NAME_TABLE_DESKS);
                    Cursor desks = getDesks();
                    while (desks.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableDesks.KEY_ROW_ID, desks.getString(desks.getColumnIndexOrThrow(SchoolContract.TableDesks.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableDesks.COLUMN_X, desks.getString(desks.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_X)));
                        serializer.attribute("", SchoolContract.TableDesks.COLUMN_Y, desks.getString(desks.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_Y)));
                        serializer.attribute("", SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES, desks.getString(desks.getColumnIndexOrThrow(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES)));
                        serializer.attribute("", SchoolContract.TableDesks.KEY_CABINET_ID, desks.getString(desks.getColumnIndexOrThrow(SchoolContract.TableDesks.KEY_CABINET_ID)));
                        serializer.endTag("", "element");
                    }
                    desks.close();
                    serializer.endTag("", SchoolContract.TableDesks.NAME_TABLE_DESKS);
                }
                // таблица мест за партами
                {
                    serializer.startTag("", SchoolContract.TablePlaces.NAME_TABLE_PLACES);
                    Cursor places = sqDB.query(SchoolContract.TablePlaces.NAME_TABLE_PLACES,
                            null, null, null, null, null, null);
                    while (places.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TablePlaces.KEY_ROW_ID, places.getString(places.getColumnIndexOrThrow(SchoolContract.TablePlaces.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TablePlaces.KEY_DESK_ID, places.getString(places.getColumnIndexOrThrow(SchoolContract.TablePlaces.KEY_DESK_ID)));
                        serializer.attribute("", SchoolContract.TablePlaces.COLUMN_ORDINAL, places.getString(places.getColumnIndexOrThrow(SchoolContract.TablePlaces.COLUMN_ORDINAL)));
                        serializer.endTag("", "element");
                    }
                    places.close();
                    serializer.endTag("", SchoolContract.TablePlaces.NAME_TABLE_PLACES);
                }
                // таблица классов с учениками
                {
                    serializer.startTag("", SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                    Cursor learnersClasses = sqDB.query(SchoolContract.TableClasses.NAME_TABLE_CLASSES,
                            null, null, null, null, null, null);
                    while (learnersClasses.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableClasses.KEY_ROW_ID, learnersClasses.getString(learnersClasses.getColumnIndexOrThrow(SchoolContract.TableClasses.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableClasses.COLUMN_CLASS_NAME, learnersClasses.getString(learnersClasses.getColumnIndexOrThrow(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
                        serializer.endTag("", "element");
                    }
                    learnersClasses.close();
                    serializer.endTag("", SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                }
                // таблица с учениками
                {
                    serializer.startTag("", SchoolContract.TableLearners.NAME_TABLE_LEARNERS);
                    Cursor learners = sqDB.query(SchoolContract.TableLearners.NAME_TABLE_LEARNERS,
                            null, null, null, null, null, null);
                    while (learners.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableLearners.KEY_ROW_ID, learners.getString(learners.getColumnIndexOrThrow(SchoolContract.TableLearners.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableLearners.COLUMN_FIRST_NAME, learners.getString(learners.getColumnIndexOrThrow(SchoolContract.TableLearners.COLUMN_FIRST_NAME)));
                        serializer.attribute("", SchoolContract.TableLearners.COLUMN_SECOND_NAME, learners.getString(learners.getColumnIndexOrThrow(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
                        serializer.attribute("", SchoolContract.TableLearners.COLUMN_COMMENT, learners.getString(learners.getColumnIndexOrThrow(SchoolContract.TableLearners.COLUMN_COMMENT)));
                        serializer.attribute("", SchoolContract.TableLearners.KEY_CLASS_ID, learners.getString(learners.getColumnIndexOrThrow(SchoolContract.TableLearners.KEY_CLASS_ID)));
                        serializer.endTag("", "element");
                    }
                    learners.close();
                    serializer.endTag("", SchoolContract.TableLearners.NAME_TABLE_LEARNERS);
                }
                // таблица зависимостей ученик-место
                {
                    serializer.startTag("", SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES);
                    Cursor attitude = sqDB.query(SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES,
                            null, null, null, null, null, null);
                    while (attitude.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableLearnersOnPlaces.KEY_ROW_ID, attitude.getString(attitude.getColumnIndexOrThrow(SchoolContract.TableLearnersOnPlaces.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID, attitude.getString(attitude.getColumnIndexOrThrow(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID)));
                        serializer.attribute("", SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID, attitude.getString(attitude.getColumnIndexOrThrow(SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID)));
                        serializer.endTag("", "element");
                    }
                    attitude.close();
                    serializer.endTag("", SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES);
                }
                // таблица оценок
                {
                    serializer.startTag("", SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES);
                    Cursor grades = sqDB.query(SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                            null, null, null, null, null, null);
                    while (grades.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableLearnersGrades.KEY_ROW_ID, grades.getString(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableLearnersGrades.COLUMN_DATE, grades.getString(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMN_DATE)));
                        serializer.attribute("", SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER, grades.getString(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER)));
                        for (int i = 0; i < SchoolContract.TableLearnersGrades.COLUMNS_GRADE.length; i++)
                            serializer.attribute("", SchoolContract.TableLearnersGrades.COLUMNS_GRADE[i], grades.getString(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[i])));
                        for (int i = 0; i < SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID.length; i++)
                            serializer.attribute("", SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[i], grades.getString(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[i])));
                        if (!grades.isNull(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID))) {
                            serializer.attribute("", SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, grades.getString(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID)));
                        } else {
                            serializer.attribute("", SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, "null");
                        }
                        serializer.attribute("", SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID, grades.getString(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID)));
                        serializer.attribute("", SchoolContract.TableLearnersGrades.KEY_LEARNER_ID, grades.getString(grades.getColumnIndexOrThrow(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID)));

                        serializer.endTag("", "element");
                    }
                    grades.close();
                    serializer.endTag("", SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES);
                }
                // таблица предметов
                {
                    serializer.startTag("", SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS);
                    Cursor subjects = sqDB.query(SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS,
                            null, null, null, null, null, null);
                    while (subjects.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableSubjects.KEY_ROW_ID, subjects.getString(subjects.getColumnIndexOrThrow(SchoolContract.TableSubjects.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableSubjects.COLUMN_NAME, subjects.getString(subjects.getColumnIndexOrThrow(SchoolContract.TableSubjects.COLUMN_NAME)));
                        serializer.attribute("", SchoolContract.TableSubjects.KEY_CLASS_ID, subjects.getString(subjects.getColumnIndexOrThrow(SchoolContract.TableSubjects.KEY_CLASS_ID)));
                        serializer.endTag("", "element");
                    }
                    subjects.close();
                    serializer.endTag("", SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS);
                }
                // таблица уроков (предмет-время-кабинет)
                {
                    serializer.startTag("", SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE);
                    Cursor lessons = sqDB.query(SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE,
                            null, null, null, null, null, null);
                    while (lessons.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID, lessons.getString(lessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID, lessons.getString(lessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID)));
                        serializer.attribute("", SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID, lessons.getString(lessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID)));
                        serializer.attribute("", SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER, lessons.getString(lessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER)));
                        serializer.attribute("", SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE, lessons.getString(lessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE)));
                        //serializer.attribute("", SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE, desks.getString(desks.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_END_REPEAT_DATE)));// todo это поле еще не реализовано
                        serializer.attribute("", SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT, lessons.getString(lessons.getColumnIndexOrThrow(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT)));
                        serializer.endTag("", "element");
                    }
                    lessons.close();
                    serializer.endTag("", SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE);
                }
                // таблица профилей статистики
                {
                    serializer.startTag("", SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES);
                    Cursor statistics = sqDB.query(SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                            null, null, null, null, null, null);
                    while (statistics.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableStatisticsProfiles.KEY_ROW_ID, statistics.getString(statistics.getColumnIndexOrThrow(SchoolContract.TableStatisticsProfiles.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME, statistics.getString(statistics.getColumnIndexOrThrow(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME)));
                        serializer.attribute("", SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE, statistics.getString(statistics.getColumnIndexOrThrow(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE)));
                        serializer.attribute("", SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE, statistics.getString(statistics.getColumnIndexOrThrow(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE)));
                        serializer.endTag("", "element");
                    }
                    statistics.close();
                    serializer.endTag("", SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES);
                }
                // таблица типов оценок
                {
                    serializer.startTag("", SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES);
                    Cursor gradesTypes = sqDB.query(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES,
                            null, null, null, null, null, null);
                    while (gradesTypes.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID, gradesTypes.getString(gradesTypes.getColumnIndexOrThrow(SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, gradesTypes.getString(gradesTypes.getColumnIndexOrThrow(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE)));
                        serializer.endTag("", "element");
                    }
                    gradesTypes.close();
                    serializer.endTag("", SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES);
                }
                // таблица типов пропусков
                {
                    serializer.startTag("", SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES);
                    Cursor absTypes = sqDB.query(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                            null, null, null, null, null, null);
                    while (absTypes.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID, absTypes.getString(absTypes.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME, absTypes.getString(absTypes.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME)));
                        serializer.attribute("", SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME, absTypes.getString(absTypes.getColumnIndexOrThrow(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME)));
                        serializer.endTag("", "element");
                    }
                    absTypes.close();
                    serializer.endTag("", SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES);
                }
                // таблица комментариев к уроку
                {
                    serializer.startTag("", SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT);
                    Cursor comments = sqDB.query(SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT, null, null, null, null, null, null);
                    while (comments.moveToNext()) {
                        serializer.startTag("", "element");
                        serializer.attribute("", SchoolContract.TableLessonComment.KEY_ROW_ID, comments.getString(comments.getColumnIndexOrThrow(SchoolContract.TableLessonComment.KEY_ROW_ID)));
                        serializer.attribute("", SchoolContract.TableLessonComment.KEY_LESSON_ID, comments.getString(comments.getColumnIndexOrThrow(SchoolContract.TableLessonComment.KEY_LESSON_ID)));
                        serializer.attribute("", SchoolContract.TableLessonComment.COLUMN_LESSON_DATE, comments.getString(comments.getColumnIndexOrThrow(SchoolContract.TableLessonComment.COLUMN_LESSON_DATE)));
                        serializer.attribute("", SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT, comments.getString(comments.getColumnIndexOrThrow(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT)));
                        serializer.endTag("", "element");
                    }
                    comments.close();
                    serializer.endTag("", SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT);
                }
            }
            serializer.endTag("", SchoolContract.DB_NAME);
            serializer.endDocument();

        }
    }


    public void testParsedData(ImportDataBaseData data) {

    }
//
//    // очистка/создание буферных таблиц
//    private void clearTestBuffer(SQLiteDatabase db){
//        db.execSQL("PRAGMA foreign_keys = OFF;");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableDesks.NAME_TABLE_DESKS + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES + "buffer" + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT + "buffer" + ";");
//        db.execSQL("PRAGMA foreign_keys = ON;");
//
//
//        // настройки
//        db.execSQL("CREATE TABLE " + SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS + "buffer" +
//                SchoolContract.TableSettingsData.CREATE_TABLE_STRING);
//
//        // кабинет
//        db.execSQL("CREATE TABLE " + SchoolContract.TableCabinets.NAME_TABLE_CABINETS + "buffer" +
//                SchoolContract.TableCabinets.CREATE_TABLE_STRING);
//
//        // парта
//        db.execSQL("CREATE TABLE " + SchoolContract.TableDesks.NAME_TABLE_DESKS + "buffer" +
//                SchoolContract.TableDesks.CREATE_TABLE_STRING);
//
//        // место
//        db.execSQL("CREATE TABLE " + SchoolContract.TablePlaces.NAME_TABLE_PLACES + "buffer" +
//                SchoolContract.TablePlaces.CREATE_TABLE_STRING);
//
//        // класс
//        db.execSQL("CREATE TABLE " + SchoolContract.TableClasses.NAME_TABLE_CLASSES + "buffer" +
//                SchoolContract.TableClasses.CREATE_TABLE_STRING);
//
//        //ученик
//        db.execSQL("CREATE TABLE " + SchoolContract.TableLearners.NAME_TABLE_LEARNERS + "buffer" +
//                SchoolContract.TableLearners.CREATE_TABLE_STRING);
//
//        //ученик-место
//        db.execSQL( "CREATE TABLE " + SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES + "buffer" +
//                SchoolContract.TableLearnersOnPlaces.CREATE_TABLE_STRING);
//
//        // типы оценок
//        db.execSQL("CREATE TABLE " + SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES + "buffer" +
//                SchoolContract.TableLearnersGradesTitles.CREATE_TABLE_STRING
//        );
//
//        // типы пропусков
//        db.execSQL("CREATE TABLE " + SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES + "buffer" +
//                SchoolContract.TableLearnersAbsentTypes.CREATE_TABLE_STRING);
//
//        //оценки учеников
//        db.execSQL("CREATE TABLE " + SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES + "buffer" +
//                SchoolContract.TableLearnersGrades.CREATE_TABLE_STRING);
//
//        //предмет
//        db.execSQL( "CREATE TABLE " + SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS + "buffer" +
//                SchoolContract.TableSubjects.CREATE_TABLE_STRING);
//
//        //урок и его время проведения
//        db.execSQL("CREATE TABLE " + SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE_SUBJECT_AND_TIME_CABINET_ATTITUDE + "buffer" +
//                SchoolContract.TableSubjectAndTimeCabinetAttitude.CREATE_TABLE_STRING);
//
//        // профили статистики
//        db.execSQL("CREATE TABLE " + SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES + "buffer" +
//                SchoolContract.TableStatisticsProfiles.CREATE_TABLE_STRING);
//
//        // комментарий к уроку
//        db.execSQL(
//                "CREATE TABLE " + SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT + "buffer" +
//                        SchoolContract.TableLessonComment.CREATE_TABLE_STRING
//        );
//
//
//        // --- дефолтные записи ---
//
//        {// ---- вставляем одну запись настроек ----
//            ContentValues values = new ContentValues();
//            values.put(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, "simpleName");
//            db.insert(SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS, null, values);
//        }
//        {// ---- вставляем одну запись "работа на уроке" ----  <-- эту запись нельзя удалить но можно переименовать
//            String name = context.getResources().getString(R.string.db_table_grade_text_first_default_value);
//            ContentValues values = new ContentValues();
//            values.put(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, name);
//            values.put(SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID, 1);
//            db.insert(SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, null, values);
//        }
//        {// ---- вставляем 2 записи "н", "бол." ----  <-- запись "н" нельзя удалить но можно переименовать
//            ContentValues values1 = new ContentValues();
//            values1.put(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID, 1);
//            values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
//                    context.getResources().getString(R.string.db_table_grades_abs_1_default_value));
//            values1.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
//                    context.getResources().getString(R.string.db_table_grades_abs_1_long_default_value));
//            db.insert(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, null, values1);
//
//            ContentValues values2 = new ContentValues();
//            values2.put(SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID, 2);
//            values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME,
//                    context.getResources().getString(R.string.db_table_grades_abs_2_default_value));
//            values2.put(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME,
//                    context.getResources().getString(R.string.db_table_grades_abs_2_long_default_value));
//            db.insert(SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, null, values2);
//        }
//    }

}
