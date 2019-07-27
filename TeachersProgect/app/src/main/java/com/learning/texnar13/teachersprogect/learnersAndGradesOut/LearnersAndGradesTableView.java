package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;


public class LearnersAndGradesTableView extends View {

    // кисть для отрисовки текста
    private Paint drawTextPaint;
    // кисть для отрисовки фона
    private Paint backgroundPaint;

    // размеры view
    private int viewWidth;
    private int viewHeight;
    // ширина отображаемой части полей с именами
    private int learnersShowedWidth = 500;
    // ширина границы клеток в пикселях
    private int cellBorderSize = 4;
    // свободное пространство в клетке вокруг текста
    private int cellFreeSpaceMargin = 40;
    // минимальная ширина текста не пустой клетки // todo ширина Н + cellFreeSpaceMargin+cellFreeSpaceMargin+cellBorderSize
    private int cellTextMinimumWidth = 80;
    // расстояние между оценками находящимися в одной клетке
    private int gradesSpaceMargin = 30;

    // растояние на которое смещены ученики по x
    private int learnersXOffset = 30;
    // растояние на которое смещены оценки по x
    private int gradesXOffset = 0;
    // растояние на которое смещены ученики и оценки по y
    private int learnersAndGradesYOffset = 0;

    // лист с учениками
    private ArrayList<LearnerAndHisGradesWithSize> learnersAndGradesDataAndSizes;
    // максимальная оценка для раскрашивания
    private long maxAnswersCount = 0;


    // конструкторы
    public LearnersAndGradesTableView(Context context) {
        super(context);
        // кисть для текста
        drawTextPaint = new Paint();
        drawTextPaint.setTextSize(getResources().getDimension(R.dimen.text_subtitle_size));
        // кисть для фона
        backgroundPaint = new Paint();
    }

    public LearnersAndGradesTableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // кисть для текста
        drawTextPaint = new Paint();
        drawTextPaint.setTextSize(getResources().getDimension(R.dimen.text_subtitle_size));
        // кисть для фона
        backgroundPaint = new Paint();
    }

    public LearnersAndGradesTableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // кисть для текста
        drawTextPaint = new Paint();
        drawTextPaint.setTextSize(getResources().getDimension(R.dimen.text_subtitle_size));
        // кисть для фона
        backgroundPaint = new Paint();
    }


    // метод получения значений
    void setData(ArrayList<NewLearnerAndHisGrades> data, int maxAnswersCount) {
        // todo запрет на вывод onDraw пока заполняем данные

        //  максимальная оценка
        this.maxAnswersCount = maxAnswersCount;
        // назначаем размер листу во view чтобы потом скопировать в него данные
        learnersAndGradesDataAndSizes = new ArrayList<>(data.size());


        // строка для промежуточных расчетов
        StringBuilder tempString = new StringBuilder();

        // -------- пробегаемся по ученикам и выясняем их размеры --------
        for (int learnerI = 0; learnerI < data.size(); learnerI++) {
            // чистим строку
            tempString.delete(0, tempString.length());// todo проверить лог

            // считываем размеры текста
            Rect learnerRect = new Rect();
            drawTextPaint.getTextBounds(
                    tempString.append(data.get(learnerI).surname).append(" ").append(data.get(learnerI).name).toString(),
                    0,
                    tempString.length(),
                    learnerRect
            );

            // отступы текстового поля от границ
            int leftMargin = cellBorderSize + cellFreeSpaceMargin;
            int bottomMargin = cellBorderSize + cellFreeSpaceMargin;

            // расчитываем размер клетки этого ученика
            learnerRect.bottom = cellFreeSpaceMargin - learnerRect.top + cellFreeSpaceMargin + cellBorderSize;
            learnerRect.right = cellBorderSize + cellFreeSpaceMargin + learnerRect.left + learnerRect.right + cellBorderSize + cellFreeSpaceMargin;
            learnerRect.left = 0;
            learnerRect.top = 0;

            // расчитываем отступы сверху самой клетки ученика в таблице
            // если ученик не первый
            if (learnerI != 0) {
                // сравниваем его по ширине с предыдущим
                if (learnersAndGradesDataAndSizes.get(learnerI - 1).location.right >= learnerRect.right) {
                    // и если предыдущий больше, то выставляем текущему размеры предыдущего
                    learnerRect.right = learnersAndGradesDataAndSizes.get(learnerI - 1).location.right;
                } else {
                    // а если текущий больше, то выставляем ВСЕМ предыдущим размер текущего
                    for (int learnerSizeI = 0; learnerSizeI < learnerI; learnerSizeI++) {
                        learnersAndGradesDataAndSizes.get(learnerSizeI).location.right = learnerRect.right;
                    }
                }
//                // сравниваем его по высоте с предыдущим // todo или через Н
//                if (learnersAndGradesDataAndSizes.get(learnerI - 1).location.right >= learnerRect.right) {
//                    // и если предыдущий больше, то выставляем текущему размеры предыдущего
//                    learnerRect.bottom = learnersAndGradesDataAndSizes.get(learnerI - 1).location.bottom - ;
//                } else {
//                    // а если текущий больше, то выставляем ВСЕМ предыдущим размер текущего
//                    for (int learnerSizeI = 0; learnerSizeI < learnerI; learnerSizeI++) {
//                        learnersAndGradesDataAndSizes.get(learnerSizeI).location.right = learnerRect.right;
//                    }
//                }// todo  сравнить по ширине с минимальным размером ученика или нет минимальный размер ученика по фио

                // задаем смещение по высоте
                learnerRect.top = learnersAndGradesDataAndSizes.get(learnerI - 1).location.bottom;
                learnerRect.bottom += learnerRect.top;
            } else {
                // задаем смещение по высоте первому ученику                                        <-
                learnerRect.top = 20;
                learnerRect.bottom += 20;
            }

            // и наконец сохраняем все в ученика с пустыми оценками
            learnersAndGradesDataAndSizes.add(
                    new LearnerAndHisGradesWithSize(
                            data.get(learnerI).id,
                            data.get(learnerI).name,
                            data.get(learnerI).surname,
                            learnerRect,
                            leftMargin,
                            bottomMargin,
                            new GradeUnitWithSize[0][0]
                    )
            );// todo высоту можно взять по самому большому ученику или просто из drawTextPaint H

        }


// ---------------------- //todo по поводу базы данных, все данные должны храниться не по времени а по урокам и дате тк количество уроков везде фиксированно как и оценки!!!!!!!!!!!!!!!!!!!!!!!!!!


        if (data.size() != 0) {
            // -------------- инициализируем у учеников массивы оценок --------------
            for (int learnerI = 0; learnerI < data.size(); learnerI++) {
                learnersAndGradesDataAndSizes.get(learnerI).learnerGrades =
                        new GradeUnitWithSize[data.get(learnerI).learnerGrades.length][];

                for (int dayI = 0; dayI < data.get(learnerI).learnerGrades.length; dayI++) {
                    learnersAndGradesDataAndSizes.get(learnerI).learnerGrades[dayI] =
                            new GradeUnitWithSize[data.get(learnerI).learnerGrades[dayI].length];
                }
            }

            // ------- пробегаемся по дням -------
            for (int dayI = 0; dayI < data.get(0).learnerGrades.length; dayI++) {

                // ------ пробегаемся по урокам ------
                for (int lessonI = 0; lessonI < data.get(0).learnerGrades[dayI].length; lessonI++) {

                    // ---- пробегаемся по ученикам ----
                    for (int learnerI = 0; learnerI < data.size(); learnerI++) {

                        // создаем прямоугольник для хранения координат оценки
                        Rect gradeRect = new Rect();
                        // расчитываем отступы оценок от краёв прямоугольника
                        int bottomMargin = cellBorderSize + cellFreeSpaceMargin;
                        int[] leftMargins = new int[data.get(learnerI).learnerGrades[dayI][lessonI].grades.length];

                        // складываем оценки и вычисляем их общую длинну
                        for (int gradesI = 0; gradesI < data.get(learnerI).learnerGrades[dayI][lessonI].grades.length; gradesI++) {


                            if (data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI] == 0) {
                                // если оценка нулевая
                                if (gradesI == 0) {
                                    // для первой оценки
                                    leftMargins[0] = 0;
                                } else {
                                    leftMargins[gradesI] = leftMargins[gradesI - 1] + gradeRect.left + gradeRect.right;
                                }
                            } else {
                                // если оценка не нулевая
                                if (gradesI == 0) {
                                    // для первой оценки
                                    leftMargins[0] = cellFreeSpaceMargin;
                                } else {
                                    if (leftMargins[gradesI - 1] == 0) {
                                        leftMargins[gradesI] = cellBorderSize + cellFreeSpaceMargin;
                                    } else {
                                        leftMargins[gradesI] = leftMargins[gradesI - 1] + gradeRect.left + gradeRect.right + gradesSpaceMargin;
                                    }
                                }
                            }

                            Log.e("TeachersApp", "leftTextMargins[gradesI]=" + leftMargins[gradesI]);


                            // считаем ширину оценки для отступа следующей
                            if (data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI] > 0) {
                                // если это не нулевой балл
                                drawTextPaint.getTextBounds(
                                        Integer.toString(data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI]),
                                        0,
                                        Integer.toString(data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI]).length(),
                                        gradeRect
                                );

                            } else if (data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI] == -2) {
                                // -2 -> Abs
                                drawTextPaint.getTextBounds(
                                        getResources().getString(R.string.learners_and_grades_out_activity_title_grade_n),
                                        0,
                                        getResources().getString(R.string.learners_and_grades_out_activity_title_grade_n).length(),
                                        gradeRect
                                );

                            } else if (data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI] == 0) {
                                gradeRect.left = 0;
                                gradeRect.right = 0;
                            }
                            // -1 -> ошибка

                        }
                        Log.e("TeachersApp", "setData: 00 day=" + dayI + " lesson=" + lessonI + " learner=" + learnerI + " result" + gradeRect.right);


                        // ----- расчитываем размеры и положение самой ячейки -----
                        // считаем ширину всего прямоугольника
                        gradeRect.right = leftMargins[leftMargins.length - 1] + gradeRect.right + gradeRect.left;
                        gradeRect.left = 0;
                        // ширина текста должна быть не меньше заданной (если она не_нулевая или первая)
                        if (gradeRect.right > 0 || lessonI == 0) {
                            if (gradeRect.right < cellTextMinimumWidth) {
                                gradeRect.right = cellTextMinimumWidth;
                                // прочерк
                            }
                            // место для рамки и отступов (нулевые остаются нулевыми)
                            gradeRect.right += cellFreeSpaceMargin + cellBorderSize;
                        }

                        // выравниваем левый край относительно предыдущей колонки
                        if (lessonI == 0) {
                            if (dayI != 0) {
                                // первый урок -> последний урок в предыдущем дне
                                gradeRect.left = learnersAndGradesDataAndSizes.get(learnerI).learnerGrades[dayI - 1]
                                        [learnersAndGradesDataAndSizes.get(learnerI).learnerGrades[dayI - 1].length - 1]
                                        .location.right;
                                gradeRect.right += learnersAndGradesDataAndSizes.get(learnerI).learnerGrades[dayI - 1]
                                        [learnersAndGradesDataAndSizes.get(learnerI).learnerGrades[dayI - 1].length - 1]
                                        .location.right;
                            }
                            // самый первый -> начальный отступ пока 0

                        } else {
                            // урок посередине
                            gradeRect.left = learnersAndGradesDataAndSizes.get(learnerI).
                                    learnerGrades[dayI][lessonI - 1].location.right;
                            gradeRect.right += learnersAndGradesDataAndSizes.get(learnerI).
                                    learnerGrades[dayI][lessonI - 1].location.right;
                        }

                        // и выравниваем ширину ячеек сверху если нужно
                        if (learnerI != 0) {
                            // сравниваем размер с предыдущими
                            if (gradeRect.right > learnersAndGradesDataAndSizes.get(learnerI - 1).learnerGrades[dayI][lessonI].location.right) {
                                // если новая ячейка больше предыдущих
                                for (int learnerSizeI = 0; learnerSizeI < learnerI; learnerSizeI++) {
                                    learnersAndGradesDataAndSizes.get(learnerSizeI).learnerGrades[dayI][lessonI].location.right = gradeRect.right;
                                }
                            } else {
                                // если новая ячейка меньше или равна предыдущим ставим ей их размер
                                gradeRect.right = learnersAndGradesDataAndSizes.get(learnerI - 1).learnerGrades[dayI][lessonI].location.right;
                            }
                        }

                        // высоту берем от ученика
                        gradeRect.top = learnersAndGradesDataAndSizes.get(learnerI).location.top;
                        gradeRect.bottom = learnersAndGradesDataAndSizes.get(learnerI).location.bottom;


                        // ----- сохраняем размеры оценок текущего ученика в массив ------
                        learnersAndGradesDataAndSizes.get(learnerI).learnerGrades[dayI][lessonI] =
                                new GradeUnitWithSize(
                                        data.get(learnerI).learnerGrades[dayI][lessonI].grades,
                                        gradeRect,
                                        bottomMargin,
                                        leftMargins
                                );


                        // todo выравнивание высоты учеика по высоте оценки, если она больше? или наоборот упростить все для скорости?
                        // todo +1  чтобы не накладывались края?

                    }
                }
            }
        }
    }


    // здесь происходит определение размеров view, так же их можно задать жестко
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // считаем размеры доступного места
        this.viewWidth = widthMeasureSpec;
        this.viewHeight = heightMeasureSpec;
        //Log.e("TeachersApp", "onMeasure: "+widthMeasureSpec);
        // и поставим view размеры
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {// TODO: 2019-07-10 rect.intersect(Rect r);


        canvas.drawRGB(200, 255, 200);
        canvas.drawText("Hello world!\uD83D\uDE0D", 100, 100, drawTextPaint);


        //Rect tempRect = new Rect();
        //int tempGradeMargin;

        // пробегаемся по ученикам
        for (int i = 0; i < learnersAndGradesDataAndSizes.size(); i++) {
            //int tempA = 0;
            Log.e("TeachersApp", "onDraw: " + learnersAndGradesDataAndSizes.get(i).learnerGrades.length);

            // ---- выводим текущего ученика ----

            // рисуем рамку ученика
            backgroundPaint.setColor(getResources().getColor(R.color.colorBackGroundDark));
            canvas.drawRect(
                    learnersAndGradesDataAndSizes.get(i).location.left + learnersXOffset,
                    learnersAndGradesDataAndSizes.get(i).location.top,
                    learnersAndGradesDataAndSizes.get(i).location.right + learnersXOffset,
                    learnersAndGradesDataAndSizes.get(i).location.bottom,
                    backgroundPaint
            );

            // рисуем фон ученика
            backgroundPaint.setColor(getResources().getColor(R.color.colorBackGround));
            canvas.drawRect(
                    learnersAndGradesDataAndSizes.get(i).location.left + cellBorderSize + learnersXOffset,
                    learnersAndGradesDataAndSizes.get(i).location.top,
                    learnersAndGradesDataAndSizes.get(i).location.right - cellBorderSize + learnersXOffset,
                    learnersAndGradesDataAndSizes.get(i).location.bottom - cellBorderSize,
                    backgroundPaint
            );

            drawTextPaint.setColor(Color.BLACK);
            // пишем его текст
            canvas.drawText(
                    learnersAndGradesDataAndSizes.get(i).surname + " " + learnersAndGradesDataAndSizes.get(i).name,
                    learnersAndGradesDataAndSizes.get(i).location.left + learnersAndGradesDataAndSizes.get(i).leftTextMargin + learnersXOffset,
                    learnersAndGradesDataAndSizes.get(i).location.bottom - learnersAndGradesDataAndSizes.get(i).bottomTextMargin,
                    drawTextPaint
            );
            backgroundPaint.setColor(getResources().getColor(R.color.colorPrimaryOrange));


            // пробегаемся по дням
            for (int dayIterator = 0; dayIterator < learnersAndGradesDataAndSizes.get(i).learnerGrades.length; dayIterator++) {
                // пробегаемся по урокам в дне
                for (int lessonIterator = 0; lessonIterator < learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator].length; lessonIterator++) {

                    // не выводим клетки с нулевой шириной (весь столбик будет нулевой)
                    if (learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.width() != 0) {

                        // рисуем рамку
                        backgroundPaint.setColor(getResources().getColor(R.color.colorBackGroundDark));
                        canvas.drawRect(
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left + learnersShowedWidth,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.top,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.right + learnersShowedWidth,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom,
                                backgroundPaint
                        );
                        // рисуем внутреннюю часть клетки
                        backgroundPaint.setColor(getResources().getColor(R.color.colorBackGround));
                        canvas.drawRect(
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left + learnersShowedWidth,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.top,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.right + learnersShowedWidth - cellBorderSize,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom - cellBorderSize,
                                backgroundPaint
                        );

                        // выводим ли прочерк
                        boolean isZero = true;

                        // пробегаемся по оценкам
                        for (int gradeI = 0; gradeI < learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades.length; gradeI++) {

                            // печатаем оценки
                            if (learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] > 0) {
                                isZero = false;
                                // если это не нулевой балл

                                drawTextPaint.setColor(Color.BLACK);
                                canvas.drawText(
                                        Integer.toString(learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI]),
                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left
                                                + learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].leftTextMargins[gradeI] + learnersShowedWidth,
                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom
                                                - learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].bottomTextMargin,
                                        drawTextPaint
                                );

                            } else if (learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] == -2) {
                                isZero = false;
                                // -2 -> Abs

                                drawTextPaint.setColor(Color.BLACK);
                                canvas.drawText(
                                        getResources().getString(R.string.learners_and_grades_out_activity_title_grade_n),
                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left
                                                + learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].leftTextMargins[gradeI] + learnersShowedWidth,
                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom
                                                - learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].bottomTextMargin,
                                        drawTextPaint
                                );
                            }
                            //else if (
//                                    gradeI == learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades.length - 1
//                                            && tempGradeMargin == 0
//                            ) {
//                                //если уже последний заход а ширина поля все еще 0 выводим прочерк
//                                canvas.drawText(
//                                        "-",
//                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left,
//                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom,
//                                        drawTextPaint
//                                );
//
//                            }
                            // -1 -> ошибка


                            // ---- выбираем цвет оценки ----

                            // > 5
//                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) > 100) {
//                                //        Color.DKGRAY
//                            }
//                            //5
//                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 100) {
//                                //        getResources().getColor(R.color.grade5Color)
//                            }
//                            //4
//                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 80) {
//                                //        getResources().getColor(R.color.grade4Color)
//                            }
//                            //3
//                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 60) {
//                                //        getResources().getColor(R.color.grade3Color)
//                            }
//                            //2
//                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 41) {
//                                //       getResources().getColor(R.color.grade2Color)
//
//                            }
//                            //1
//                            if ((int) (((float) grades[i] / (float) maxAnswersCount) * 100F) <= 20) {
//                                //        getResources().getColor(R.color.grade1Color)
//                            }
                        }

                        if (isZero) {
                            // выводим прочерк
                            drawTextPaint.setColor(Color.GRAY);
                            canvas.drawText(
                                    "-",
                                    learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left
                                            + cellFreeSpaceMargin + learnersShowedWidth,
                                    learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom
                                            - learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].bottomTextMargin,
                                    drawTextPaint
                            );
                        }
                    }
                }
            }
        }

        super.onDraw(canvas);
    }
}


// оценки за один урок и размеры поля в котором они находятся
class GradeUnitWithSize {
    // массив оценок
    int[] grades;

    // размеры клетки
    Rect location;
    // отступ текста от нижней границы
    int bottomTextMargin;
    //  отступ текста от левой границы
    int[] leftTextMargins;

    GradeUnitWithSize(int[] grades, Rect location, int bottomTextMargin, int[] leftTextMargins) {
        this.grades = grades;
        this.location = location;
        this.bottomTextMargin = bottomTextMargin;
        this.leftTextMargins = leftTextMargins;
    }
}

// ученик размеры поля ученика и его оценки
class LearnerAndHisGradesWithSize {
    // ученик
    long id;
    String name;
    String surname;
    // его оценки        ( [номер дня][номер урока] ).grades[номер оценки]
    GradeUnitWithSize[][] learnerGrades;

    // размеры его клетки
    Rect location;
    //  отступ текста от нижней границы
    int bottomTextMargin;
    //  отступ текста от левой границы
    int leftTextMargin;


    LearnerAndHisGradesWithSize(long id, String name, String surname, Rect location, int leftTextMargin, int bottomTextMargin, GradeUnitWithSize[][] learnerGrades) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.location = location;
        this.bottomTextMargin = bottomTextMargin;
        this.leftTextMargin = leftTextMargin;
        this.learnerGrades = learnerGrades;
    }
}



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
