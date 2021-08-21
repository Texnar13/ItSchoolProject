package com.learning.texnar13.teachersprogect.gradesPeriods;

import java.util.LinkedList;

// все загруженные данные
class GradesPeriodsDataModel {

    // данные о переданном уроке и предмете
    SubjectAndClassUnit currentSubjectAndClass;
    // не изменяемые данные об учениках
    LearnerUnit[] learners;
    // изменяемые периоды обучения
    LinkedList<StudyPeriod> studyPeriods;

    public GradesPeriodsDataModel(SubjectAndClassUnit currentSubjectAndClass,
                                  LearnerUnit[] learners, LinkedList<StudyPeriod> studyPeriods) {
        this.currentSubjectAndClass = currentSubjectAndClass;
        this.learners = learners;
        this.studyPeriods = studyPeriods;
    }

    // класс и предмет
    static class SubjectAndClassUnit {
        String subjectName;// subject name
        long dbSubjectId;

        String className;// class name
        long dbClassId;

        public SubjectAndClassUnit(String subjectName, long dbSubjectId, String className, long dbClassId) {
            this.subjectName = subjectName;
            this.dbSubjectId = dbSubjectId;
            this.className = className;
            this.dbClassId = dbClassId;
        }
    }

    // ученик
    static class LearnerUnit {
        String name;// фио
        long dbId;

        public LearnerUnit(String name, long dbId) {
            this.name = name;
            this.dbId = dbId;
        }
    }

    // учебный период
    static class StudyPeriod {

        // даты
        String startDate;
        String endDate;
        // название
        String name;


        // массив оценок
        GradeUnit[] gradeUnits;


        public StudyPeriod(String startDate, String endDate, String name, GradeUnit[] gradeUnits) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.name = name;
            this.gradeUnits = gradeUnits;
        }
    }

    // оценки
    static class GradeUnit {
        // средняя оценка
        int averageGrade;
        // число пропусков
        int absentTimes;
        // итоговая оценка
        int finalGrade;

        public GradeUnit(int averageGrade, int absentTimes, int finalGrade) {
            this.averageGrade = averageGrade;
            this.absentTimes = absentTimes;
            this.finalGrade = finalGrade;
        }
    }
}
