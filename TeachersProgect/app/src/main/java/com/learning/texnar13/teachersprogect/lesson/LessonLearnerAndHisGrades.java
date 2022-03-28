package com.learning.texnar13.teachersprogect.lesson;

import android.content.Context;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;


// класс для хранения ученика и его оценок
class LessonLearnerAndHisGrades {

    // параметры ученика
    long learnerId;
    String shortName;
    String fullName;

    // id оценки
    long gradeId;
    // номер текущей оценки
    int chosenGradePosition = 0;
    // массив оценок
    LessonListActivity.LessonListLearnerAndGradesData.GradeUnit[] gradesUnits;
    // тип пропуска
    int absTypePozNumber;

    // View данные
    LearnerViewData viewData;

    LessonLearnerAndHisGrades(long learnerId, String shortName, String fullName,
                              long gradeId, LessonListActivity.LessonListLearnerAndGradesData.GradeUnit[] gradesUnits,
                              int absTypePozNumber
    ) {
        this.learnerId = learnerId;
        this.shortName = shortName;
        this.fullName = fullName;
        this.gradeId = gradeId;
        this.gradesUnits = gradesUnits;
        this.absTypePozNumber = absTypePozNumber;
        viewData = null;
    }


    // заготовка аргументов для диалога оценок
    int[] getGradesArray() {
        int[] result = new int[gradesUnits.length];
        for (int i = 0; i < result.length; i++) result[i] = gradesUnits[i].grade;
        return result;
    }

    int[] getGradesTypesArray() {
        int[] result = new int[gradesUnits.length];
        for (int i = 0; i < result.length; i++) result[i] = gradesUnits[i].gradeTypePoz;
        return result;
    }


    class LearnerViewData {


        // размер одноместной парты
        private static final int NO_ZOOMED_DESK_SIZE = 40;
        // ширина границы вокруг клетки ученика на парте
        private static final int NO_ZOOMED_LEARNER_BORDER_SIZE = NO_ZOOMED_DESK_SIZE / 20;

        private static final float SMALL_GRADE_SIZE = 7;
        private static final float MEDIUM_GRADE_SIZE = 7;
        private static final float LARGE_GRADE_SIZE = 10;

        private static final float SMALL_GRADE_SIZE_DOUBLE = 4;
        private static final float MEDIUM_GRADE_SIZE_DOUBLE = 7;
        private static final float LARGE_GRADE_SIZE_DOUBLE = 9;


        // контейнер места ученика
        RelativeLayout viewPlaceOut;
        // текст имени ученика
        TextView viewLearnerNameText;
        // текст главной оценки
        TextView centerGrade;
        // текст побочной оценки 1
        TextView leftGrade;
        // текст побочной оценки 2
        TextView rightGrade;
        // картинка ученика
        ImageView viewLearnerImage;

        // данные из активности
        GraduationSettings graduationSettings;
        Context context;

        LearnerViewData(RelativeLayout viewPlaceOut, TextView viewLearnerNameText,
                        TextView viewMainGradeText, TextView viewGrade1,
                        TextView viewGrade2, ImageView viewLearnerImage, GraduationSettings graduationSettings, Context context) {
            // контейнер места ученика
            this.viewPlaceOut = viewPlaceOut;
            // текст имени ученика
            this.viewLearnerNameText = viewLearnerNameText;
            // текст главной оценки
            this.centerGrade = viewMainGradeText;
            // текст побочной оценки 1
            this.leftGrade = viewGrade1;
            // текст побочной оценки 2
            this.rightGrade = viewGrade2;
            // картинка ученика
            this.viewLearnerImage = viewLearnerImage;

            // данные из активности
            this.graduationSettings = graduationSettings;
            this.context = context;
        }


        // ---- обновление размеров контейнеров и текста ----

        // обновление размеров контейнеров и текста для зума
        void updateSizesForZoom(float multiplier, int placeDeskPosition) {
            updateSizePlaceView(multiplier, placeDeskPosition);
            updateSizesGradesViews(multiplier);
            updateSizeLearnerNameView(multiplier);
        }

        // обновление размеров контейнера места
        void updateSizePlaceView(float multiplier, int placeDeskPosition) {
            // контейнер места ученика
            RelativeLayout.LayoutParams viewPlaceOutParams = (RelativeLayout.LayoutParams) viewPlaceOut.getLayoutParams();
            viewPlaceOutParams.leftMargin = pxFromDp((NO_ZOOMED_DESK_SIZE
                    * placeDeskPosition + NO_ZOOMED_LEARNER_BORDER_SIZE) * multiplier);
            viewPlaceOutParams.topMargin =
                    pxFromDp(NO_ZOOMED_LEARNER_BORDER_SIZE * multiplier);
            viewPlaceOutParams.width =
                    pxFromDp((NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_BORDER_SIZE * 2) * multiplier);
            viewPlaceOutParams.height = viewPlaceOutParams.width;
        }

        // обновление размеров текста оценок
        void updateSizesGradesViews(float multiplier) {
            // текст побочной оценки 1
            leftGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                    ((gradesUnits[getLeftGradeArrayPos()].grade < 10) ?
                            (SMALL_GRADE_SIZE) : (SMALL_GRADE_SIZE_DOUBLE)) * multiplier);
            ((RelativeLayout.LayoutParams) leftGrade.getLayoutParams()).leftMargin = (int) (10 * multiplier);

            // текст побочной оценки 2
            rightGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                    ((gradesUnits[getRightGradeArrayPos()].grade < 10) ?
                            (SMALL_GRADE_SIZE) : (SMALL_GRADE_SIZE_DOUBLE)) * multiplier);
            ((RelativeLayout.LayoutParams) rightGrade.getLayoutParams()).rightMargin = (int) (10 * multiplier);

            // текст главной оценки
            if (gradesUnits[getLeftGradeArrayPos()].grade == 0 && gradesUnits[getRightGradeArrayPos()].grade == 0) {
                centerGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                        ((gradesUnits[chosenGradePosition].grade < 10) ?
                                (LARGE_GRADE_SIZE) : (LARGE_GRADE_SIZE_DOUBLE)) * multiplier);
            } else
                centerGrade.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                        ((gradesUnits[chosenGradePosition].grade < 10) ?
                                (MEDIUM_GRADE_SIZE) : (MEDIUM_GRADE_SIZE_DOUBLE)) * multiplier);
        }

        // выставляем в текстовое поле имя ученика
        void updateSizeLearnerNameView(float multiplier) {
            viewLearnerNameText.setTextSize(7 * multiplier);
        }


        // ---- выставляем данные из полей во view ----
        void updateGradesTexts() {

            // текст главной оценки
            setViewGradeText(centerGrade, gradesUnits[chosenGradePosition].grade);
            // текст побочной оценки 1
            setViewGradeText(leftGrade, gradesUnits[getLeftGradeArrayPos()].grade);
            // текст побочной оценки 2
            setViewGradeText(rightGrade, gradesUnits[getRightGradeArrayPos()].grade);

            // меняем изображение на учненике в соответствии с оценкой
            updateLearnerBackground();
        }

        // выставляем в текстовое поле имя ученика
        void updateLearnerNameText() {
            viewLearnerNameText.setText(shortName);
        }

        // ставим в tempLernerImage изображение по оценке
        void updateLearnerBackground() {
            // находим последнюю поставленную оценку

            if (absTypePozNumber != -1) {// пропуск
                viewLearnerImage.setImageResource(R.drawable.lesson_activity_learner_icon_abs);
            } else if (gradesUnits[chosenGradePosition].grade == 0) {
                if (gradesUnits[getLeftGradeArrayPos()].grade == 0 && gradesUnits[getRightGradeArrayPos()].grade == 0) {
                    viewLearnerImage.setImageResource(R.drawable.lesson_activity_learner_icon_gray_0);
                } else {
                    viewLearnerImage.setImageResource(R.drawable.lesson_activity_learner_icon_base);
                }
            } else {
                float currentGrade = (float) gradesUnits[chosenGradePosition].grade / graduationSettings.maxAnswersCount;
                if (currentGrade <= 0.2F) {
                    //1
                    viewLearnerImage.setImageResource(R.drawable.lesson_activity_learner_icon_1);
                } else if (currentGrade <= 0.41F) {
                    //2
                    viewLearnerImage.setImageResource(R.drawable.lesson_activity_learner_icon_2);
                } else if (currentGrade <= 0.60F) {
                    //3
                    viewLearnerImage.setImageResource(R.drawable.lesson_activity_learner_icon_3);
                } else if (currentGrade <= 0.80F) {
                    //4
                    viewLearnerImage.setImageResource(R.drawable.lesson_activity_learner_icon_4);
                } else if (currentGrade <= 1F) {
                    //5
                    viewLearnerImage.setImageResource(R.drawable.lesson_activity_learner_icon_5);
                }
            }
        }

        // ---- внутренние вспомогательные методы ----

        // выставление текста в оценку
        private void setViewGradeText(TextView gradeView, int grade) {
            gradeView.setText((grade > 0) ? ("" + grade) : (""));
        }

        // позиции оценок в массиве в зависимости от главной оценки
        private int getLeftGradeArrayPos() {
            return (chosenGradePosition == 0) ? (1) : (0);
        }

        private int getRightGradeArrayPos() {
            return (chosenGradePosition == 2) ? (1) : (2);
        }


        private int pxFromDp(float dp) {
            return (int) (dp * context.getResources().getDisplayMetrics().density);
        }

    }

}
