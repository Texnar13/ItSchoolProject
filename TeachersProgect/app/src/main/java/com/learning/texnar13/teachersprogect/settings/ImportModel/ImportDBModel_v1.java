package com.learning.texnar13.teachersprogect.settings.ImportModel;

import android.provider.BaseColumns;

import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.settings.ImportFieldData;
import com.learning.texnar13.teachersprogect.settings.ImportModel.ImportDataBaseData.ColumnCheck;
import com.learning.texnar13.teachersprogect.settings.ImportModel.ImportDataBaseData.ImportDBModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class ImportDBModel_v1 extends ImportDBModel {

    // конструктор с описанием базы данных
    public ImportDBModel_v1(String[] localeCodes) {
        this.tables = new TableModel[]{

                // описание таблицы настроек
                new TableModel(
                        SchoolContract.TableSettingsData.NAME_TABLE_SETTINGS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ImportFieldData.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_PROFILE_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_LOCALE,
                                        ColumnCheck.TYPE_STRING, checkAble -> {
                                    // локаль приложения
                                    String locale = (String) checkAble;
                                    return (Arrays.asList(localeCodes).contains(locale));
                                }),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_INTERFACE_SIZE, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_MAX_ANSWER,
                                        ColumnCheck.TYPE_LONG, checkAble -> {
                                    long value = (long) checkAble;
                                    return (0 < value && value <= 100);
                                }),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_TIME,
                                        ColumnCheck.TYPE_STRING, (checkAble) -> {
                                    // пытаемся парсить обьект json
                                    String rowValue = (String) checkAble;
                                    try {
                                        new JSONObject(rowValue);
                                        return true;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        return false;
                                    }
                                }),
                                new ColumnCheck(SchoolContract.TableSettingsData.COLUMN_ARE_THE_GRADES_COLORED,
                                        ColumnCheck.TYPE_BOOLEAN, (checkAble) -> true)
                        }),

                // описание таблицы статистики
                new TableModel(
                        SchoolContract.TableStatisticsProfiles.NAME_TABLE_STATISTICS_PROFILES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableStatisticsProfiles.COLUMN_PROFILE_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableStatisticsProfiles.COLUMN_START_DATE, ColumnCheck.TYPE_STRING, dateCheck),
                                new ColumnCheck(SchoolContract.TableStatisticsProfiles.COLUMN_END_DATE, ColumnCheck.TYPE_STRING, dateCheck)
                        }),

                // описание таблицы кабинетов
                new TableModel(
                        SchoolContract.TableCabinets.NAME_TABLE_CABINETS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableCabinets.COLUMN_CABINET_MULTIPLIER, ColumnCheck.TYPE_LONG, (checkAble) -> {
                                    long multiplier = (long) checkAble;
                                    return (1 <= multiplier && multiplier <= 100);
                                }),
                                new ColumnCheck(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_X, ColumnCheck.TYPE_LONG, emptyCheck),
                                new ColumnCheck(SchoolContract.TableCabinets.COLUMN_CABINET_OFFSET_Y, ColumnCheck.TYPE_LONG, emptyCheck),
                                new ColumnCheck(SchoolContract.TableCabinets.COLUMN_NAME, ColumnCheck.TYPE_STRING, emptyCheck)
                        }),

                // описание таблицы парт
                new TableModel(
                        SchoolContract.TableDesks.NAME_TABLE_DESKS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableDesks.COLUMN_X, ColumnCheck.TYPE_LONG, emptyCheck),
                                new ColumnCheck(SchoolContract.TableDesks.COLUMN_Y, ColumnCheck.TYPE_LONG, emptyCheck),
                                new ColumnCheck(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES, ColumnCheck.TYPE_LONG, placesNumberCheck),
                                new ColumnCheck(SchoolContract.TableDesks.KEY_CABINET_ID, SchoolContract.TableCabinets.NAME_TABLE_CABINETS, notNullIdCheck)
                        }),

                // описание таблицы мест
                new TableModel(
                        SchoolContract.TablePlaces.NAME_TABLE_PLACES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TablePlaces.KEY_DESK_ID, SchoolContract.TableDesks.NAME_TABLE_DESKS, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TablePlaces.COLUMN_ORDINAL, ColumnCheck.TYPE_LONG, placesNumberCheck)
                        }),

                // описание таблицы слассов
                new TableModel(
                        SchoolContract.TableClasses.NAME_TABLE_CLASSES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableClasses.COLUMN_CLASS_NAME, ColumnCheck.TYPE_STRING, emptyCheck)
                        }),

                // описание таблицы учеников
                new TableModel(
                        SchoolContract.TableLearners.NAME_TABLE_LEARNERS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearners.COLUMN_FIRST_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearners.COLUMN_SECOND_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearners.COLUMN_COMMENT, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearners.KEY_CLASS_ID, SchoolContract.TableClasses.NAME_TABLE_CLASSES, notNullIdCheck)
                        }),

                // описание таблицы ученик-место
                new TableModel(
                        SchoolContract.TableLearnersOnPlaces.NAME_TABLE_LEARNERS_ON_PLACES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID, SchoolContract.TableLearners.NAME_TABLE_LEARNERS, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID, SchoolContract.TablePlaces.NAME_TABLE_PLACES, notNullIdCheck)
                        }),

                // описание таблицы оценок
                new TableModel(
                        SchoolContract.TableLearnersGrades.NAME_TABLE_LEARNERS_GRADES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMN_DATE, ColumnCheck.TYPE_STRING, dateCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMN_LESSON_NUMBER, ColumnCheck.TYPE_LONG, numberCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[0], ColumnCheck.TYPE_LONG, gradeCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[1], ColumnCheck.TYPE_LONG, gradeCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.COLUMNS_GRADE[2], ColumnCheck.TYPE_LONG, gradeCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[0], SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[1], SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEYS_GRADES_TITLES_ID[2], SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEY_ABSENT_TYPE_ID, SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES, nullRefCheck),// ссылка может быть пустой
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEY_SUBJECT_ID, SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGrades.KEY_LEARNER_ID, SchoolContract.TableLearners.NAME_TABLE_LEARNERS, notNullIdCheck)
                        }),

                // описание таблицы типов оценок
                new TableModel(
                        SchoolContract.TableLearnersGradesTitles.NAME_TABLE_LEARNERS_GRADES_TITLES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE, ColumnCheck.TYPE_STRING, emptyCheck)
                        }),

                // описание таблицы типов пропусков
                new TableModel(
                        SchoolContract.TableLearnersAbsentTypes.NAME_TABLE_LEARNERS_ABSENT_TYPES,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME, ColumnCheck.TYPE_STRING, emptyCheck)
                        }),

                // описание таблицы предметов
                new TableModel(
                        SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableSubjects.COLUMN_NAME, ColumnCheck.TYPE_STRING, emptyCheck),
                                new ColumnCheck(SchoolContract.TableSubjects.KEY_CLASS_ID, SchoolContract.TableClasses.NAME_TABLE_CLASSES, notNullIdCheck)
                        }),

                // описание таблицы уроков (предмет-время-кабинет)
                new TableModel(
                        SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID, SchoolContract.TableSubjects.NAME_TABLE_SUBJECTS, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID, SchoolContract.TableCabinets.NAME_TABLE_CABINETS, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_NUMBER, ColumnCheck.TYPE_LONG, numberCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_LESSON_DATE, ColumnCheck.TYPE_STRING, dateCheck),
                                new ColumnCheck(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_REPEAT, ColumnCheck.TYPE_LONG, (checkAble) -> {
                                    long number = (long) checkAble;
                                    return (SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER <= number &&
                                            number <= SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_WEEKLY);
                                })
                        }),

                // описание таблицы комментариев к уроку
                new TableModel(
                        SchoolContract.TableLessonComment.NAME_TABLE_LESSON_TEXT,
                        new ColumnCheck[]{
                                new ColumnCheck(BaseColumns._ID, ColumnCheck.TYPE_LONG, notNullIdCheck),
                                new ColumnCheck(SchoolContract.TableLessonComment.KEY_LESSON_ID, SchoolContract.TableSubjectAndTimeCabinetAttitude.NAME_TABLE, emptyCheck),
                                new ColumnCheck(SchoolContract.TableLessonComment.COLUMN_LESSON_DATE, ColumnCheck.TYPE_STRING, dateCheck),
                                new ColumnCheck(SchoolContract.TableLessonComment.COLUMN_LESSON_TEXT, ColumnCheck.TYPE_STRING, emptyCheck)
                        })
        };
    }


    // ------------------------------ экземпляры проверок ------------------------------


    // переменная проверки для всех id и не пустых ссылок
    private static final ImportDataBaseData.ColumnCheck.ConditionCheckable notNullIdCheck = (checkable) -> {
        long id = (long) checkable;
        return id >= 1;
    };
    // переменная проверки для всех пустых и не пустых ссылок
    private static final ImportDataBaseData.ColumnCheck.ConditionCheckable nullRefCheck = (checkable) -> {
        long id = (long) checkable;
        return (id >= 1 || id == -1);
    };

    // проверка для текстовых полей без проверки содержимого
    // todo не забудь про огрраничения в количество символов,
    //  (для премиум функций не надо, но для ограничений в принципе очень больших значений, надо)
    private static final ImportDataBaseData.ColumnCheck.ConditionCheckable emptyCheck = (checkAble) -> true;

    // проверка для дат
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final ImportDataBaseData.ColumnCheck.ConditionCheckable dateCheck = (checkAble) -> {
        try {
            dateFormat.parse((String) checkAble);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    };
    // проверка номера урока
    private static final ImportDataBaseData.ColumnCheck.ConditionCheckable numberCheck = (checkAble) -> {
        long number = (long) checkAble;
        return (number >= 1);
    };

    // проверка оценок
    private static final ImportDataBaseData.ColumnCheck.ConditionCheckable gradeCheck = (checkAble) -> {
        long number = (long) checkAble;
        return (number >= 0);
    };

    // стандартная проверка для количества мест за партой
    private static final ImportDataBaseData.ColumnCheck.ConditionCheckable placesNumberCheck = (checkAble) -> {
        long multiplier = (long) checkAble;
        return (multiplier >= 1);
    };
    

}
