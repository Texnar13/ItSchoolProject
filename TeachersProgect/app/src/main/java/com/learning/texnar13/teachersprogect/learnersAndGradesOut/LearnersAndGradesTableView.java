package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;


public class LearnersAndGradesTableView extends View {

    // кисть для отрисовки всего
    Paint drawPaint;

    // размеры view
    int viewWidth;
    int viewHeight;
    // лист с учениками
    ArrayList<LearnerAndHisGradesWithSize> learnersAndGradesDataAndSizes;


    // конструкторы
    public LearnersAndGradesTableView(Context context) {
        super(context);
    }

    public LearnersAndGradesTableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LearnersAndGradesTableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    // метод получения значений
    void setData(ArrayList<LearnerAndHisGrades> data) {
        // назначаем размер листу во view чтобы потом скопировать в него данные
        learnersAndGradesDataAndSizes = new ArrayList<>(data.size());
        // переменные для промежуточных расчетов
        Rect tempRect = new Rect();
        String tempS;

        // -------- максимальная длинна поля ученика --------
        int learnerMaxSize = 500; // минимальный размер поля = 500 px                               <-
        // размер текста учеников
        drawPaint.setTextSize(getResources().getDimension(R.dimen.text_subtitle_size));
        // ищем самый большой прямоугольник
        for (int learnerI = 0; learnerI < data.size(); learnerI++) {
            tempS = data.get(learnerI).surname + " " + data.get(learnerI).name;
            // вычисляем размер текущего прямоугольника
            drawPaint.getTextBounds(tempS, 0, tempS.length(), tempRect);
            if (learnerMaxSize < tempRect.right) learnerMaxSize = tempRect.right;
        }


        // -------- пробегаемся по ученикам --------
        for (int learnerI = 0; learnerI < data.size(); learnerI++) {

            // ------ вычисляем высоту для каждой строки с учеником ------
            tempS = data.get(learnerI).surname + " " + data.get(learnerI).name;
            drawPaint.getTextBounds(tempS, 0, tempS.length(), tempRect);
            if (learnerI != 0) {
                // располагаем этого ученика под предыдущим
                tempRect.top = learnersAndGradesDataAndSizes.get(learnerI - 1).location.bottom;
                tempRect.bottom += learnersAndGradesDataAndSizes.get(learnerI - 1).location.bottom;
            } else {
                // отступ сверху для первого ученика                                                <-
                tempRect.top = 100;
                tempRect.bottom += 100;
            }
            tempRect.right = learnerMaxSize;


            // ------ разбираемся с оценками ученика ------
            // инициализируем массив по дням
            GradeUnitWithSize[][] gradeUnits = new GradeUnitWithSize
                    [data.get(learnerI).learnerGrades.length][];
            // пробегаемся по дням
            Rect tempGradeRect;
            for (int dayI = 0; dayI < gradeUnits.length; dayI++) {
                // инициализируем массив по урокам
                gradeUnits[dayI] = new GradeUnitWithSize[data.get(learnerI).learnerGrades[dayI].length];
                // пробегаемся по урокам
                for (int lessonI = 0; lessonI < gradeUnits[dayI].length; lessonI++) {
                    // прямоугольник ограничивающий текст оценок
                    tempGradeRect = new Rect(0, 0, 0, 0); // todo               <<<<<<<- кстати не забудь в базе дланных прописать код для свежеустановленного приложения

                    // копируем данные и задаем положение
                    gradeUnits[dayI][lessonI] = new GradeUnitWithSize(
                            data.get(learnerI).learnerGrades[dayI][lessonI].grades,
                            tempGradeRect
                    );
                }
            }


            // ------ и наконец сохраняем все в ученика ------
            learnersAndGradesDataAndSizes.set(learnerI,
                    new LearnerAndHisGradesWithSize(
                            data.get(learnerI).id,
                            data.get(learnerI).name,
                            data.get(learnerI).surname,
                            tempRect,
                            gradeUnits
                    )
            );
        }
    }


    // здесь происходит определение размеров view, так же их можно задать жестко
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // считаем размеры доступного места
        this.viewWidth = widthMeasureSpec;
        this.viewHeight = heightMeasureSpec;
        // и поставим view размеры
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();

        canvas.drawRGB(255, 255, 255);

        paint.setColor(getResources().getColor(R.color.colorPrimaryBlue));
        canvas.drawRect(10, 10, 400, 200, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(getResources().getDimension(R.dimen.text_subtitle_size));
        canvas.drawText("Hello world!\uD83D\uDE0D", 100, 100, paint);

        Rect textRect = new Rect();
        String s = "Hello world!\uD83D\uDE0D";
        paint.getTextBounds(s, 0, s.length(), textRect);
        paint.setColor(getResources().getColor(R.color.colorPrimaryOrange));
        textRect.top += 200;
        textRect.bottom += 200;
        canvas.drawRect(textRect, paint);


//        // пробегаемся по ученикам (3)
//        paint.setColor(getResources().getColor(R.color.colorPrimaryBlue));
//        for (int i = 0; i < learnersAndGradesDataAndSizes.size(); i++) {
//            // выводим текущего ученика
//            canvas.drawRect(learnersAndGradesDataAndSizes.get(i).location, paint);
//            // пробегаемся по дням
//            for (int dayIterator = 0; dayIterator < learnersAndGradesDataAndSizes.get(i).learnerGrades.length; dayIterator++) {
//                // пробегаемся по урокам в дне
//                for (int lessonIterator = 0; lessonIterator < learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator].length; lessonIterator++) {
//                    canvas.drawRect(
//                            learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location,
//                            paint
//                    );
//                }
//            }
//        }
//
//
//        // или другой вариант , который подразумевает, что что количество дней и уроков везде одинаково
//        // и они все инициализированы хотябы нулевыми значениями
//        // todo похоже на опасное место из старой модели, мложет лучше сделать так, чтобы оценки учеников были в нутри в виде списка?
//        // или проверка на null?
//        //
//        // третий вариант, проставить правильно размеры всех полей и выводить построчно
//
//
//        // пробегаемся по ученикам (2)
//        paint.setColor(getResources().getColor(R.color.colorPrimaryBlue));
//        for (int i = 0; i < learnersAndGradesDataAndSizes.size(); i++) {
//            // выводим текущего ученика
//            canvas.drawRect(learnersAndGradesDataAndSizes.get(i).location, paint);
//        }
//        // пробегаемся по дням
//        for (int dayIterator = 0; dayIterator < learnersAndGradesDataAndSizes.get(0).learnerGrades.length; dayIterator++) {
//            // пробегаемся по урокам в дне
//            for (int lessonIterator = 0; lessonIterator < learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator].length; lessonIterator++) {
//                // пробегаемся по ученикам в столбик
//                for (int learnersIterator = 0; learnersIterator < learnersAndGradesDataAndSizes.size(); learnersIterator++) {
//                    // выводим оценки конкретного ученика в этот день на этом уроке
//                    canvas.drawRect(
//                            learnersAndGradesDataAndSizes.get(learnersIterator).learnerGrades[dayIterator][lessonIterator].location,
//                            paint
//                    );
//                }
//            }
//        }


        super.onDraw(canvas);
    }
}

// нужны массивы в которых хранятся просто данные
// и массивы в которых есть размеры этих данных при выводе на экран


// оценки за один урок и размеры поля в котором они находятся
class GradeUnitWithSize {
    int[] grades;
    Rect location;

    GradeUnitWithSize(int[] grades, Rect location) {
        this.grades = grades;
        this.location = location;
    }
}

// ученик размеры поля ученика и его оценки
class LearnerAndHisGradesWithSize {
    // ученик
    long id;
    String name;
    String surname;
    // размеры его поля
    Rect location;
    // его оценки        ( [номер дня][номер урока] ).grades[номер оценки]
    GradeUnitWithSize[][] learnerGrades;

    LearnerAndHisGradesWithSize(long id, String name, String surname, Rect location, GradeUnitWithSize[][] learnerGrades) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.location = location;
        this.learnerGrades = learnerGrades;
    }
}






/*
 *
 * день
 *   урок
 *     ученик
 *       оценка(3)
 * */



/*
 *
 * здесь хранятся прямоугольники с местоположениями,
 * из главной активности получаем координаты нажатия и сразу передаем их сюда,
 * здесь пробегаемся по прямоугольникам с проверкой координат,
 * и если все совпадает возвращаем id и другую информацию
 *
 * кстати числа тогда выводим в конце поверх нарисованного исходя из размера одного из элементов таблицы
 *
 *
 * Длинна
 *
 * заливается в начале и может удаляться добавляться при этом происходит их сортировка - количество учеников
 * перезаливается при полной смене данных и в начале - количество дней
 * перезаливается при полной смене данных и в начале может быть у всех разным но не больше n - количество уроков
 * всегда три - количество оценок
 *
 * */
