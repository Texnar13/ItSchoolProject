package com.learning.texnar13.teachersprogect.lesson;

import android.database.Cursor;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

// класс для получения настроек приложения
final class GraduationSettings {
    // размер максимальной оценки
    int maxAnswersCount;
    // массив типов оценок
    AnswersType[] answersTypes;
    // массив типов пропусков
    AbsentType[] absentTypes;


    // -------------- фабрика обьектов --------------
    public static GraduationSettings newInstance(DataBaseOpenHelper db) {

        // макс. оценка, типы пропусков и оценок
        GraduationSettings answer = new GraduationSettings();
        // максимальная оценка
        answer.maxAnswersCount = db.getSettingsMaxGrade(1);
        // названия типов ответов
        Cursor typesCursor = db.getGradesTypes();
        answer.answersTypes = new AnswersType[typesCursor.getCount()];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < typesCursor.getCount(); typeI++) {
            typesCursor.moveToNext();
            // добавляем новый тип во внутренний список
            answer.answersTypes[typeI] = new AnswersType(
                    typesCursor.getLong(typesCursor.getColumnIndex(
                            SchoolContract.TableLearnersGradesTitles.KEY_ROW_ID)),
                    typesCursor.getString(typesCursor.getColumnIndex(
                            SchoolContract.TableLearnersGradesTitles.COLUMN_LEARNERS_GRADES_TITLE))
            );
        }
        typesCursor.close();
        // названия типов пропусков
        Cursor typesAbsCursor = db.getAbsentTypes();
        answer.absentTypes = new AbsentType[typesAbsCursor.getCount()];
        // извлекаем данные из курсора
        for (int typeI = 0; typeI < typesAbsCursor.getCount(); typeI++) {
            typesAbsCursor.moveToNext();
            // добавляем новый тип во внутренний список
            answer.absentTypes[typeI] = new AbsentType(
                    typesAbsCursor.getLong(typesAbsCursor.getColumnIndex(
                            SchoolContract.TableLearnersAbsentTypes.KEY_ROW_ID)),
                    typesAbsCursor.getString(typesAbsCursor.getColumnIndex(
                            SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_NAME)),
                    typesAbsCursor.getString(typesAbsCursor.getColumnIndex(
                            SchoolContract.TableLearnersAbsentTypes.COLUMN_LEARNERS_ABSENT_TYPE_LONG_NAME))
            );
        }
        typesAbsCursor.close();

        return answer;

    }

    private GraduationSettings() {
    }


    // -------------- заготовка аргументов для диалога оценок --------------
    public String[] getAnswersTypesArray() {
        String[] result = new String[answersTypes.length];
        for (int i = 0; i < result.length; i++) result[i] = answersTypes[i].typeName;
        return result;
    }

    public String[] getAbsentTypesLongNames() {
        String[] result = new String[absentTypes.length];
        for (int i = 0; i < result.length; i++) result[i] = absentTypes[i].typeAbsLongName;
        return result;
    }


    // -------------- классы для хранения --------------

    // класс для хранения типов ответов
    public static class AnswersType {
        long id;
        String typeName;

        AnswersType(long id, String typeName) {
            this.id = id;
            this.typeName = typeName;
        }
    }

    // класс для хранения типов пропусков
    public static class AbsentType {
        long id;
        String typeAbsName;
        String typeAbsLongName;

        AbsentType(long id, String typeAbsName, String typeAbsLongName) {
            this.id = id;
            this.typeAbsName = typeAbsName;
            this.typeAbsLongName = typeAbsLongName;
        }
    }
}
