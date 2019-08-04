package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;


public class LearnersAndGradesTableView extends View {

    final String TAG = "TeachersApp";

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
    private int learnersXOffset = -10;
    // растояние на которое смещены оценки по x
    private int gradesXOffset = -50;
    // растояние на которое смещены ученики и оценки по y
    private int learnersAndGradesYOffset = -10;
    // высота шапки таблицы
    private int learnersAndGradesOffsetForTitle = 80;

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


    // здесь происходит определение размеров view, так же их можно задать жестко
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e("TeachersApp", "LearnersAndGradesTableView: onMeasure");
        // и поставим view размеры
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.e("TeachersApp", "LearnersAndGradesTableView: onLayout");
        super.onLayout(changed, left, top, right, bottom);
        // считаем размеры этого view места
        this.viewWidth = right - left;
        this.viewHeight = bottom - top;
        // если ширина отображаемой части учеников еще не задана
        if (this.learnersShowedWidth <= 0)
            this.learnersShowedWidth = this.viewWidth / 3;
    }


    // метод получения значений
    void setData(ArrayList<NewLearnerAndHisGrades> data, int maxAnswersCount) {
        Log.e("TeachersApp", "LearnersAndGradesTableView: setData");
        // todo запрет на вывод onDraw пока заполняем данные (и на движение тк  если размеры по длинне допустим старые а новые размеры меньше то часть таблицы сьедет если перемещать по старым)

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
                // задаем минимальный размер первому ученику
                if (learnerRect.right < learnersShowedWidth) {
                    learnerRect.right = learnersShowedWidth;
                }
//                // задаем смещение по высоте первому ученику                                        <-
//                learnerRect.top = 20;
//                learnerRect.bottom += 20;
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


    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {// TODO: 2019-07-10 rect.intersect(Rect r);
        Log.e("TeachersApp", "LearnersAndGradesTableView: onDraw");


        canvas.drawRGB(200, 255, 200);
        canvas.drawText("Hello world!\uD83D\uDE0D", 100, 100, drawTextPaint);

        // определяем с какого дня и урока начать чтобы не выводить лишнее
        int startLearner = 0;
        while (startLearner < learnersAndGradesDataAndSizes.size() - 1
                && (learnersAndGradesDataAndSizes.get(startLearner).location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset) < 0 ) {
            startLearner++;
        }

        // пробегаемся по ученикам
        for (int i = startLearner; i < learnersAndGradesDataAndSizes.size() &&
                // смотрим не выходит ли ученик за границы экрана
                (learnersAndGradesDataAndSizes.get(i).location.top + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset) <= viewHeight;
             i++) {
            Log.e("TeachersApp", "onDraw: " + learnersAndGradesDataAndSizes.get(i).learnerGrades.length);

            // -------- выводим оценки текущего ученика --------

            // определяем с какого дня и урока начать чтобы не выводить лишнее
            int startDay = 0;
            while (startDay < learnersAndGradesDataAndSizes.get(i).learnerGrades.length - 1
                    && (learnersAndGradesDataAndSizes.get(i).learnerGrades[startDay][0].location.right + learnersShowedWidth + gradesXOffset) < learnersShowedWidth) {
                startDay++;
            }
            // выводим один день слева (тк этот механизм не проходится по урокам а только по дням)
            if (startDay != 0) startDay--;

            // пробегаемся по дням
            for (int dayIterator = startDay; dayIterator < learnersAndGradesDataAndSizes.get(i).learnerGrades.length; dayIterator++) {

                // и что клетки не выходят за границы экрана (иначе их бессмысленно выводить)
                if (learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator].length != 0)
                    if ((learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][0].location.left + learnersShowedWidth + gradesXOffset) > viewWidth)
                        break;

                // пробегаемся по урокам в дне
                for (int lessonIterator = 0; lessonIterator < learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator].length; lessonIterator++) {

                    // проверяем что ширина клеток больше нуля (иначе весь столбик будет нулевой)
                    if (learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.width() != 0) {

                        // рисуем рамку
                        backgroundPaint.setColor(getResources().getColor(R.color.colorBackGroundDark));
                        canvas.drawRect(
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left + learnersShowedWidth + gradesXOffset,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.top + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.right + learnersShowedWidth + gradesXOffset,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                backgroundPaint
                        );
                        // рисуем внутреннюю часть клетки
                        backgroundPaint.setColor(getResources().getColor(R.color.colorBackGround));
                        canvas.drawRect(
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left + learnersShowedWidth + gradesXOffset,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.top + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.right - cellBorderSize + learnersShowedWidth + gradesXOffset,
                                learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom - cellBorderSize + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                backgroundPaint
                        );

                        // выводим ли прочерк
                        boolean isZero = true;

                        // пробегаемся по оценкам
                        for (int gradeI = 0; gradeI < learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades.length; gradeI++) {

                            // печатаем оценки
                            if (learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] > 0) {
                                isZero = false;
                                // ------ если это не нулевой балл ------

                                // ---- выбираем цвет текста  ----
                                //1
                                if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 20) {
                                    drawTextPaint.setColor(getResources().getColor(R.color.grade1Color));
                                } else
                                    //2
                                    if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 41) {
                                        drawTextPaint.setColor(getResources().getColor(R.color.grade2Color));
                                    } else
                                        //3
                                        if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 60) {
                                            drawTextPaint.setColor(getResources().getColor(R.color.grade3Color));
                                        } else
                                            //4
                                            if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 80) {
                                                drawTextPaint.setColor(getResources().getColor(R.color.grade4Color));
                                            } else
                                                //5
                                                if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 100) {
                                                    drawTextPaint.setColor(getResources().getColor(R.color.grade5Color));
                                                } else
                                                    // > 5
                                                    if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100F) > 100) {
                                                        drawTextPaint.setColor(Color.DKGRAY);
                                                    }
                                // ---- выводим текст ----
                                canvas.drawText(
                                        Integer.toString(learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI]),
                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left
                                                + learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].leftTextMargins[gradeI]
                                                + learnersShowedWidth + gradesXOffset,
                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom
                                                - learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].bottomTextMargin
                                                + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                        drawTextPaint
                                );

                            } else if (learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] == -2) {
                                isZero = false;
                                // ------ -2 -> Abs ------
                                // ---- выбираем цвет текста ----
                                drawTextPaint.setColor(Color.BLACK);
                                // ---- выводим текст ----
                                canvas.drawText(
                                        getResources().getString(R.string.learners_and_grades_out_activity_title_grade_n),
                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left
                                                + learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].leftTextMargins[gradeI]
                                                + learnersShowedWidth + gradesXOffset,
                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom
                                                - learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].bottomTextMargin
                                                + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                        drawTextPaint
                                );
                            }
                        }

                        if (isZero) {
                            // ----- выводим прочерк -----
                            drawTextPaint.setColor(Color.GRAY);
                            canvas.drawText(
                                    "-",
                                    learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left
                                            + cellFreeSpaceMargin + learnersShowedWidth + gradesXOffset,
                                    learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom
                                            - learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].bottomTextMargin
                                            + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                    drawTextPaint
                            );
                        }
                    }
                }
            }


            // -------- выводим текущего ученика --------

            // расчитываем на сколько сокращается длинна ученика в зависимости от его смещения по горизонтали
            //int off = learnersShowedWidth - learnersAndGradesDataAndSizes.get(i).location.right + learnersXOffset;

            // рисуем рамку ученика
            backgroundPaint.setColor(getResources().getColor(R.color.colorBackGroundDark));
            canvas.drawRect(
                    learnersAndGradesDataAndSizes.get(i).location.left + learnersXOffset,
                    learnersAndGradesDataAndSizes.get(i).location.top + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                    learnersShowedWidth,
                    learnersAndGradesDataAndSizes.get(i).location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                    backgroundPaint
            );

            // рисуем фон ученика
            backgroundPaint.setColor(getResources().getColor(R.color.colorBackGround));
            canvas.drawRect(
                    learnersAndGradesDataAndSizes.get(i).location.left + cellBorderSize + learnersXOffset,
                    learnersAndGradesDataAndSizes.get(i).location.top + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                    learnersShowedWidth - cellBorderSize,
                    learnersAndGradesDataAndSizes.get(i).location.bottom - cellBorderSize + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                    backgroundPaint
            );

            // ограничиваем область рисования clip-областью
            canvas.save();
            canvas.clipRect(
                    learnersAndGradesDataAndSizes.get(i).location.left + cellBorderSize,
                    learnersAndGradesDataAndSizes.get(i).location.top + cellBorderSize + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                    learnersShowedWidth - cellBorderSize,
                    learnersAndGradesDataAndSizes.get(i).location.bottom - cellBorderSize + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset
            );
            // и пишем его текст
            drawTextPaint.setColor(Color.BLACK);
            canvas.drawText(
                    learnersAndGradesDataAndSizes.get(i).surname + " " + learnersAndGradesDataAndSizes.get(i).name,
                    learnersAndGradesDataAndSizes.get(i).location.left + learnersAndGradesDataAndSizes.get(i).leftTextMargin + learnersXOffset,
                    learnersAndGradesDataAndSizes.get(i).location.bottom - learnersAndGradesDataAndSizes.get(i).bottomTextMargin + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                    drawTextPaint
            );
            // убираем clip-область
            canvas.restore();
        }
        super.onDraw(canvas);
    }


    // метод обрабатывающий нажатие на view
    public void touch(PointF startPoint, boolean longClick) {
        Log.e(TAG, "LearnersAndGradesTableView: touch(" + startPoint + ", " + longClick + ")");
    }


    // переременные для хранения предыдущего смещения
    // растояние на которое смещены ученики по x
    private int dynamicLearnersXOffset = 0;
    // растояние на которое смещены оценки по x
    private int dynamicGradesXOffset = 0;
    // растояние на которое смещены ученики и оценки по y
    private int dynamicLearnersAndGradesYOffset = 0;

    // метод обрабатывающий перемещение пальца по view
    public void setMove(PointF startPoint, float currX, float currY) {
        if (learnersAndGradesDataAndSizes.size() != 0) {
            // ----- смещение по x -----
            if (startPoint.x <= learnersShowedWidth) {
                // --- сдвигаем учеников ---
                // убираем старое смещение
                learnersXOffset -= dynamicLearnersXOffset;
                // вычисляем новое смещение
                dynamicLearnersXOffset = (int) (currX - startPoint.x);

                // по первому ученику смотрим, не будет ли из-за смещения ученик уходить вправо создавая пустую облать
                if (learnersAndGradesDataAndSizes.get(0).location.left + learnersXOffset + dynamicLearnersXOffset <= 0) {
                    // не будет ли из-за смещения ученик уходить влево создавая пустую облать
                    if (learnersAndGradesDataAndSizes.get(0).location.right + learnersXOffset + dynamicLearnersXOffset >= learnersShowedWidth) {
                        // смещаем
                        this.learnersXOffset += dynamicLearnersXOffset;
                    } else {
                        // иначе ставим минимально возможное значение
                        this.learnersXOffset = -learnersAndGradesDataAndSizes.get(0).location.right + learnersShowedWidth;
                    }
                } else {
                    // иначе ставим максимально возможное значение
                    this.learnersXOffset = 0;
                }
            } else {
                // --- сдвигаем оценки ---
                // убираем старое смещение
                gradesXOffset -= dynamicGradesXOffset;

                if (learnersAndGradesDataAndSizes.get(0).learnerGrades.length != 0) {
                    // вычисляем новое смещение
                    dynamicGradesXOffset = (int) (currX - startPoint.x);

                    // по первому столбцу оценок смотрим, не будут ли из-за положительного смещения оценки уходить вправо создавая пустую облать
                    if (learnersAndGradesDataAndSizes.get(0).learnerGrades[0][0].location.left + gradesXOffset + dynamicGradesXOffset <= 0) {
                        // не будут ли из-за отрицательного смещения оценки уходить влево создавая пустую облать
                        if (learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1][0].location.right + gradesXOffset + dynamicGradesXOffset >= viewWidth - learnersShowedWidth) {
                            // смещаем
                            this.gradesXOffset += dynamicGradesXOffset;
                        } else {
                            // иначе ставим минимально возможное значение
                            this.gradesXOffset = viewWidth - learnersShowedWidth - learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1][0].location.right;
                        }
                    } else {
                        // иначе ставим максимально возможное значение
                        this.gradesXOffset = 0;
                    }
                }
            }

            // ----- смещение по y -----
            // убираем старое смещение
            learnersAndGradesYOffset -= dynamicLearnersAndGradesYOffset;
            // вычисляем новое смещение
            dynamicLearnersAndGradesYOffset = (int) (currY - startPoint.y);
            // по первому ученику смотрим, не будет ли из-за положительного смещения ученик уходить вниз создавая пустую облать
            if (learnersAndGradesDataAndSizes.get(0).location.top + learnersAndGradesYOffset + dynamicLearnersAndGradesYOffset <= 0) {
                // не будет ли из-за отрицательного смещения ученик уходить вверх создавая пустую облать
                if (learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom + learnersAndGradesYOffset + dynamicLearnersAndGradesYOffset >= viewHeight - learnersAndGradesOffsetForTitle) {
                    // смещаем
                    this.learnersAndGradesYOffset += dynamicLearnersAndGradesYOffset;

                } else {
                    // иначе ставим минимально возможное значение
                    this.learnersAndGradesYOffset = -learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom + viewHeight - learnersAndGradesOffsetForTitle;

                }
            } else {
                // иначе ставим максимально возможное значение
                this.learnersAndGradesYOffset = 0;
            }

            // ----- вызываем перерисовку -----
            invalidate();
        }
    }

    // метод сохраняющий перемещение пальца по view
    public void endMove() {
        // сохраняем текущее смещение
        dynamicLearnersXOffset = 0;
        dynamicGradesXOffset = 0;
        dynamicLearnersAndGradesYOffset = 0;
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
