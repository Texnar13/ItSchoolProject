package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
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

    // переменная разрешающая вывод графики
    private boolean canDraw = true;
    // позиция нажатой клетки (клетка закрашивается другим цветом)
    private int[] chosenCellPoz = new int[]{-1, -1, -1};

    // размеры view
    private int viewWidth;
    private int viewHeight;
    // ширина отображаемой части полей с именами
    private int learnersShowedWidth = 0;
    // высота шапки таблицы
    private int learnersAndGradesOffsetForTitle;
    // диаметр круга вокруг даты в шапке
    private int dateCircleRadius;
    // высота кнопки добавить ученика под таблицей
    private int addLearnerButtonHeight;

    // ширина границы клеток в пикселях
    private int cellBorderSize;
    // свободное пространство в клетке вокруг текста
    private int cellFreeSpaceMargin;
    // расстояние между оценками находящимися в одной клетке
    private int gradesSpaceMargin;
    // минимальная ширина текста не пустой клетки
    private int cellTextMinimumWidth;
    // высота строк таблицы
    private int cellMinimumHeight;

    // растояние на которое смещены ученики по x
    private int learnersXOffset = 0;
    // растояние на которое смещены оценки по x
    private int gradesXOffset = 0;
    // растояние на которое смещены ученики и оценки по y
    private int learnersAndGradesYOffset = 0;


    // лист с учениками
    private ArrayList<LearnerAndHisGradesWithSize> learnersAndGradesDataAndSizes;
    // максимальная оценка для раскрашивания
    private long maxAnswersCount = 5;
    // текущаяя дата (-1 - этот месяц не текущий) (нумерация с 0)
    int currentDate = -1;


    // конструкторы
    public LearnersAndGradesTableView(Context context) {
        super(context);
        myInit();
    }

    public LearnersAndGradesTableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        myInit();
    }

    public LearnersAndGradesTableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        myInit();
    }

    private void myInit() {

        // --- кисть для текста ---
        drawTextPaint = new Paint();
        drawTextPaint.setTextSize(getResources().getDimension(R.dimen.text_subtitle_size));
        // семейство шрифтов
        drawTextPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.geometria_family));
        // сглаживание
        drawTextPaint.setAntiAlias(true);

        // --- кисть для фона ---
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);

        // --- проставляем размеры ---
        Rect rect = new Rect();
        drawTextPaint.getTextBounds("88", 0, 2, rect);

        // ширина границы клеток и линий вокруг даты в пикселях
        cellBorderSize = (int) pxFromDp(1);
        if (cellBorderSize < 1) cellBorderSize = 1;
        // свободное пространство в клетке вокруг текста
        cellFreeSpaceMargin = (int) pxFromDp(15);

        // минимальная ширина текста не пустой клетки
        cellTextMinimumWidth = (rect.left + rect.right) / 2 + cellFreeSpaceMargin * 2 + cellBorderSize;
        // расстояние между оценками находящимися в одной клетке
        gradesSpaceMargin = (rect.left + rect.right) / 2;
        // высота строк таблицы
        cellMinimumHeight = rect.bottom - rect.top + cellFreeSpaceMargin * 2 + cellBorderSize;

        // радиус круга вокруг даты в шапке
        dateCircleRadius = (int) ((rect.bottom - rect.top) * 1.5);
        // высота шапки таблицы
        learnersAndGradesOffsetForTitle = dateCircleRadius * 2 + cellBorderSize * 4;
        // высота кнопки добавить ученика под таблицей
        addLearnerButtonHeight = cellMinimumHeight;
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
        // считаем размеры этого view
        this.viewWidth = right - left;
        this.viewHeight = bottom - top;
        // если ширина отображаемой части учеников еще не задана
        if (this.learnersShowedWidth <= 0)
            this.learnersShowedWidth = this.viewWidth / 3;
    }

    // максимальная оценка
    void setMaxAnswersCount(int maxAnswersCount) {
        this.maxAnswersCount = maxAnswersCount;
    }

    // метод получения значений
    void setData(ArrayList<NewLearnerAndHisGrades> tData) {
        Log.e("TeachersApp", "LearnersAndGradesTableView: setData");

        // запрещаем выводить графику пока заполняем данные
        canDraw = false;

        // копируем сюда список, тк его можно поменять извне
        ArrayList<NewLearnerAndHisGrades> data = (ArrayList<NewLearnerAndHisGrades>) tData.clone();


        // назначаем размер листу во view чтобы потом скопировать в него данные
        learnersAndGradesDataAndSizes = new ArrayList<>(data.size());


        // строка для промежуточных расчетов
        StringBuilder tempString = new StringBuilder();

        // -------- пробегаемся по ученикам и выясняем их размеры --------
        for (int learnerI = 0; learnerI < data.size(); learnerI++) {
            // чистим строку
            tempString.delete(0, tempString.length());

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

                // задаем смещение по высоте
                learnerRect.top = learnersAndGradesDataAndSizes.get(learnerI - 1).location.bottom;
                learnerRect.bottom += learnerRect.top;
            } else {
                // задаем минимальный размер первому ученику
                if (learnerRect.right < learnersShowedWidth) {
                    learnerRect.right = learnersShowedWidth;
                }
            }

            // если высота меньше минимально заданной
            if (learnerRect.bottom < cellMinimumHeight) {
                learnerRect.bottom = cellMinimumHeight;
            }

            // и наконец сохраняем все в ученика с пустыми оценками
            learnersAndGradesDataAndSizes.add(
                    new LearnerAndHisGradesWithSize(
                            data.get(learnerI).name,
                            data.get(learnerI).surname,
                            learnerRect,
                            leftMargin,
                            bottomMargin,
                            new GradeUnitWithSize[0][0]
                    )
            );
        }
        // todo по поводу базы данных, все данные должны храниться не по времени а по урокам и дате тк количество уроков везде фиксированно как и оценки!!!!!!!!!!!!!!!!!!!!!!!!!!

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


                        // проверяем не пустые ли оценки у учеников
                        if (data.get(learnerI).learnerGrades.length == data.get(0).learnerGrades.length)
                            if (data.get(learnerI).learnerGrades[dayI].length == data.get(0).learnerGrades[dayI].length) {


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
                            }
                    }
                }
            }
        }

        // разрешаем выводить графику
        canDraw = true;
    }


    // строки для промежуточных расчетов
    private RectF headEllipseRect = new RectF();
    private Rect headTextRect = new Rect();

    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {
        Log.e("TeachersApp", "LearnersAndGradesTableView: onDraw canDraw=" + canDraw);

        // переменная запрещающая вывод графики
        if (canDraw && learnersAndGradesDataAndSizes != null) {

            // закрашиваем фон
            canvas.drawColor(getResources().getColor(R.color.backgroundWhite));

            // выводим надпись загрузка на месте оценок
            if (learnersAndGradesDataAndSizes.size() != 0) {
                drawTextPaint.setColor(getResources().getColor(R.color.baseBlue));
                canvas.drawText(
                        getResources().getString(R.string.learners_and_grades_out_activity_load_text),
                        learnersShowedWidth + cellFreeSpaceMargin,
                        learnersAndGradesOffsetForTitle +
                                learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom / 2F
                                + cellFreeSpaceMargin,
                        drawTextPaint
                );
            }


            // ---------- выводим таблицу ----------

            // определяем с какого дня и урока начать чтобы не выводить лишнее
            int startLearner = 0;
            if (learnersAndGradesDataAndSizes.size() != 0) {
                while (startLearner < learnersAndGradesDataAndSizes.size() - 1
                        && (learnersAndGradesDataAndSizes.get(startLearner).location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset) < 0) {
                    startLearner++;
                }

                // заодно проверяем не выходит ли смещение таблицы с оценками за границы экрана
                if (learnersAndGradesDataAndSizes.get(0).learnerGrades.length != 0) {
                    if (learnersAndGradesDataAndSizes.get(0).
                            learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1].length != 0) {
                        if (learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1][learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1].length - 1].location.right + learnersShowedWidth + gradesXOffset <= viewWidth) {
                            // иначе ставим минимально возможное значение
                            this.gradesXOffset = viewWidth - learnersShowedWidth - learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1][learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1].length - 1].location.right;
                        }
                    }
                }
            }

            // пробегаемся по ученикам                                               смотрим не выходит ли ученик за границы экрана
            for (int i = startLearner; i < learnersAndGradesDataAndSizes.size() && (learnersAndGradesDataAndSizes.get(i).location.top + learnersAndGradesYOffset) <= viewHeight - learnersAndGradesOffsetForTitle; i++) {
                //Log.e("TeachersApp", "onDraw: " + learnersAndGradesDataAndSizes.get(i).learnerGrades.length);

                // -------- выводим оценки текущего ученика --------

                // ----- определяем с какого дня начать чтобы не выводить лишнее -----
                int startDay = 0;
                while (startDay < learnersAndGradesDataAndSizes.get(i).learnerGrades.length - 1
                        && (learnersAndGradesDataAndSizes.get(i).learnerGrades[startDay][0].location.right + learnersShowedWidth + gradesXOffset) < learnersShowedWidth) {
                    startDay++;
                }
                if (startDay != 0)
                    startDay--; // выводим один день слева (тк этот механизм не проходится по урокам а только по дням)

                // ----- пробегаемся по дням -----
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
                            backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                            canvas.drawRect(
                                    learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left + learnersShowedWidth + gradesXOffset,
                                    learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.top + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                    learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.right + learnersShowedWidth + gradesXOffset,
                                    learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                                    backgroundPaint
                            );

                            // рисуем внутреннюю часть клетки
                            // проверяем не нажата ли ячейка
                            if (i == chosenCellPoz[0] && dayIterator == chosenCellPoz[1] && lessonIterator == chosenCellPoz[2]) {
                                backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                            } else {
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                            }
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
                                        drawTextPaint.setColor(getResources().getColor(R.color.grade1));
                                    } else
                                        //2
                                        if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 41) {
                                            drawTextPaint.setColor(getResources().getColor(R.color.grade2));
                                        } else
                                            //3
                                            if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 60) {
                                                drawTextPaint.setColor(getResources().getColor(R.color.grade3));
                                            } else
                                                //4
                                                if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 80) {
                                                    drawTextPaint.setColor(getResources().getColor(R.color.grade4));
                                                } else
                                                    //5
                                                    if ((int) (((float) learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 100) {
                                                        drawTextPaint.setColor(getResources().getColor(R.color.grade5));
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

                                    // проверяем не нажата ли ячейка
                                    if (i == chosenCellPoz[0] && dayIterator == chosenCellPoz[1] && lessonIterator == chosenCellPoz[2]) {
                                        drawTextPaint.setColor(Color.WHITE);
                                    } else {
                                        drawTextPaint.setColor(Color.BLACK);
                                    }

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

                                // проверяем не нажата ли ячейка
                                if (i == chosenCellPoz[0] && dayIterator == chosenCellPoz[1] && lessonIterator == chosenCellPoz[2]) {
                                    drawTextPaint.setColor(Color.WHITE);
                                } else {
                                    drawTextPaint.setColor(Color.GRAY);
                                }

//                                canvas.drawText(
//                                        "-",
//                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.left
//                                                + cellFreeSpaceMargin + learnersShowedWidth + gradesXOffset,
//                                        learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].location.bottom
//                                                - learnersAndGradesDataAndSizes.get(i).learnerGrades[dayIterator][lessonIterator].bottomTextMargin
//                                                + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
//                                        drawTextPaint
//                                );
                            }
                        }
                    }
                }

                // -------- выводим текущего ученика --------

                // рисуем рамку ученика
                backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                canvas.drawRect(
                        learnersAndGradesDataAndSizes.get(i).location.left + learnersXOffset,
                        learnersAndGradesDataAndSizes.get(i).location.top + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                        learnersShowedWidth,
                        learnersAndGradesDataAndSizes.get(i).location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset,
                        backgroundPaint
                );

                // рисуем фон ученика
                backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
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

            // выводим кнопку добавить ученика
            String addButtonText = "*+Добавить ученика*";
            drawTextPaint.setColor(Color.BLACK);
            drawTextPaint.getTextBounds(addButtonText, 0, addButtonText.length(), headTextRect);
            if (learnersAndGradesDataAndSizes.size() != 0) {
                canvas.drawText(
                        addButtonText,
                        viewWidth / 2F - headTextRect.right / 2F,
                        addLearnerButtonHeight / 2F + (headTextRect.bottom - headTextRect.top) / 2F +
                                learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset
                        ,
                        drawTextPaint
                );
            } else
                canvas.drawText(
                        addButtonText,
                        viewWidth / 2F - headTextRect.right / 2F,
                        addLearnerButtonHeight / 2F + (headTextRect.bottom - headTextRect.top) / 2F + learnersAndGradesOffsetForTitle,
                        drawTextPaint
                );

            // ---------- выводим шапку ----------
            if (learnersAndGradesDataAndSizes.size() != 0) {

                // получаем имя для фио
                String name = getResources().getString(R.string.learners_and_grades_out_activity_title_table_names);
                drawTextPaint.getTextBounds(name, 0, name.length(), headTextRect);
                // получаем отступ с низу для текста и всех дат
                float bottomTitleTextOffset = learnersAndGradesOffsetForTitle / 2F - headTextRect.top / 2F;
                // и отступ с лева для заголовка имени
                float nameTitleLeftOffset = learnersShowedWidth / 2F - headTextRect.right / 2F;


                // ----- даты -----
                // по дням
                for (int dayIterator = 0; dayIterator < learnersAndGradesDataAndSizes.get(0).learnerGrades.length; dayIterator++) {
                    if (learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator].length != 0) {

                        // не выводим дни которые находятся за предеами экрана
                        if ((learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.left + learnersShowedWidth + gradesXOffset) > viewWidth)
                            break;

                        // рисуем фон первого урока
                        backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                        canvas.drawRect(
                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.left + learnersShowedWidth + gradesXOffset,
                                0,
                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.right + learnersShowedWidth + gradesXOffset,
                                learnersAndGradesOffsetForTitle,
                                backgroundPaint
                        );


                        // заранее пробегаемся по урокам в дне и ищем последний ненулевой по ширине урок
                        int lastLesson = 0;
                        for (int lessonIterator = 0; lessonIterator < learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator].length; lessonIterator++) {
                            if (learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.width() != 0)
                                lastLesson = lessonIterator;
                        }

                        // в этот день есть уроки кроме первого?
                        String dateText;
                        if (lastLesson != 0) {
                            // рисуем круг
                            backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                            canvas.drawCircle(
                                    learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.left +
                                            learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.width() / 2F +
                                            learnersShowedWidth + gradesXOffset,
                                    learnersAndGradesOffsetForTitle / 2F,
                                    dateCircleRadius,
                                    backgroundPaint
                            );

                            // выводим остальные уроки
                            for (int lessonIterator = 1; lessonIterator < learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator].length; lessonIterator++) {
                                // но только не пустые
                                if (learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.width() != 0) {
                                    // рисуем фон
                                    backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                                    canvas.drawRect(
                                            learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.left + learnersShowedWidth + gradesXOffset,
                                            0,
                                            learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.right + learnersShowedWidth + gradesXOffset,
                                            learnersAndGradesOffsetForTitle,
                                            backgroundPaint
                                    );

                                    // окончание
                                    if (lessonIterator == lastLesson) {
                                        // рисуем линии
                                        backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                                        canvas.drawRect(
                                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.left +
                                                        learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.width() / 2F +
                                                        learnersShowedWidth + gradesXOffset,
                                                learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize,
                                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.right -
                                                        learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.width() / 2F +
                                                        learnersShowedWidth + gradesXOffset,
                                                learnersAndGradesOffsetForTitle / 2F + dateCircleRadius,
                                                backgroundPaint
                                        );
                                        canvas.drawRect(
                                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.left +
                                                        learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.width() / 2F +
                                                        learnersShowedWidth + gradesXOffset,
                                                learnersAndGradesOffsetForTitle / 2F - dateCircleRadius,
                                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.right -
                                                        learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.width() / 2F +
                                                        learnersShowedWidth + gradesXOffset,
                                                learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize,
                                                backgroundPaint
                                        );

                                        // рисуем полукруг
                                        backgroundPaint.setStyle(Paint.Style.STROKE);
                                        backgroundPaint.setStrokeWidth(cellBorderSize);
                                        headEllipseRect.set(
                                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.left +
                                                        learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.width() / 2F - dateCircleRadius + cellBorderSize / 2F +
                                                        learnersShowedWidth + gradesXOffset,
                                                learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F,
                                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.left +
                                                        learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.width() / 2F + dateCircleRadius - cellBorderSize / 2F +
                                                        learnersShowedWidth + gradesXOffset,
                                                learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F
                                        );
                                        canvas.drawArc(headEllipseRect, -90, 180, false, backgroundPaint);

                                        backgroundPaint.setStyle(Paint.Style.FILL);

                                    }

                                    // рисуем текст
                                    drawTextPaint.setColor(Color.BLACK);
                                    dateText = "" + (dayIterator + 1);// todo или все-таки выводить номер урока lessonIterator
                                    drawTextPaint.getTextBounds(dateText, 0, dateText.length(), headTextRect);
                                    canvas.drawText(dateText,
                                            learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.left +
                                                    learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][lessonIterator].location.width() / 2F - headTextRect.right / 2F +
                                                    learnersShowedWidth + gradesXOffset,
                                            bottomTitleTextOffset,
                                            drawTextPaint
                                    );
                                }
                            }
                        }

                        // если этот день сегодняшний, то выделяем его
                        if (dayIterator == currentDate) {
                            // рисуем круг
                            backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                            canvas.drawCircle(
                                    learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.left +
                                            learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.width() / 2F +
                                            learnersShowedWidth + gradesXOffset,
                                    learnersAndGradesOffsetForTitle / 2F,
                                    dateCircleRadius - cellBorderSize * 2,
                                    backgroundPaint
                            );
                        }

                        // рисуем дату
                        drawTextPaint.setColor(Color.BLACK);
                        dateText = "" + (dayIterator + 1);
                        drawTextPaint.getTextBounds(dateText, 0, dateText.length(), headTextRect);
                        canvas.drawText(dateText,
                                learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.left +
                                        learnersAndGradesDataAndSizes.get(0).learnerGrades[dayIterator][0].location.width() / 2F - headTextRect.right / 2F +
                                        learnersShowedWidth + gradesXOffset,
                                bottomTitleTextOffset,
                                drawTextPaint
                        );
                    }
                }

                // ----- фио -----
                backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                canvas.drawRect(0, 0, learnersShowedWidth, learnersAndGradesOffsetForTitle, backgroundPaint);
                drawTextPaint.setColor(getResources().getColor(R.color.backgroundGray));
                canvas.drawText(name,
                        nameTitleLeftOffset,
                        bottomTitleTextOffset,
                        drawTextPaint
                );
            }
        }

        // покрасили один раз, обнуляем
        chosenCellPoz[0] = -1;
        chosenCellPoz[1] = -1;
        chosenCellPoz[2] = -1;

        super.onDraw(canvas);
    }


    // метод обрабатывающий нажатие на view
    int[] touch(PointF downPoint, boolean longClick) {
        Log.e(TAG, "LearnersAndGradesTableView: touch(" + downPoint + ", " + longClick + ")");

        if (learnersAndGradesDataAndSizes.size() != 0) {

            // если нажали область ниже таблицы
            if (downPoint.y > (learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset)) {
                // если нажата кнопка создать ученика
                if (downPoint.y > learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset &&
                        downPoint.y <= learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom + learnersAndGradesOffsetForTitle + learnersAndGradesYOffset + addLearnerButtonHeight) {
                    return new int[]{-2, -2, -2};
                } else // иначе не нажато ничего
                    return new int[]{-1, -1, -1};
            }

            if (downPoint.x > learnersShowedWidth) {
                // если нажатие в области оценок

                // пробегаемся по таблице
                for (int learnerIterator = 0; learnerIterator < learnersAndGradesDataAndSizes.size(); learnerIterator++) {
                    for (int dayIterator = 0; dayIterator < learnersAndGradesDataAndSizes.get(learnerIterator).learnerGrades.length; dayIterator++) {
                        for (int lessonIterator = 0; lessonIterator < learnersAndGradesDataAndSizes.get(learnerIterator).learnerGrades[dayIterator].length; lessonIterator++) {
                            if (learnersAndGradesDataAndSizes.get(learnerIterator).learnerGrades[dayIterator][lessonIterator].location.contains(
                                    (int) downPoint.x - learnersShowedWidth - gradesXOffset,
                                    (int) downPoint.y - learnersAndGradesOffsetForTitle - learnersAndGradesYOffset
                            )) {
                                // нажата ячейка оценок
                                chosenCellPoz[0] = learnerIterator;
                                chosenCellPoz[1] = dayIterator;
                                chosenCellPoz[2] = lessonIterator;
                                return new int[]{learnerIterator, dayIterator, lessonIterator};
                            }
                        }
                    }
                }

                // если не нашли совпадений в таблице то не нажато ничего
                return new int[]{-1, -1, -1};

            } else {
                // если нажатие в области учеников

                // про бегаемся по таблице
                for (int learnerIterator = 0; learnerIterator < learnersAndGradesDataAndSizes.size(); learnerIterator++) {
                    if (learnersAndGradesDataAndSizes.get(learnerIterator).location.contains(
                            (int) downPoint.x,
                            (int) downPoint.y - learnersAndGradesOffsetForTitle - learnersAndGradesYOffset
                    )) {

                        // нажат ученик
                        chosenCellPoz[0] = learnerIterator; // номер ученика по списку
                        chosenCellPoz[1] = -1;              // номер дня
                        chosenCellPoz[2] = -1;              // номер урока
                        return new int[]{learnerIterator, -1, -1};
                    }
                }

                // не нажато ничего
                return new int[]{-1, -1, -1};

            }
        } else {
            // если нажата кнопка создать ученика
            if (downPoint.y > learnersAndGradesOffsetForTitle && downPoint.y <= learnersAndGradesOffsetForTitle + addLearnerButtonHeight) {
                return new int[]{-2, -2, -2};
            } else // иначе не нажато ничего
                return new int[]{-1, -1, -1};
        }
    }


    // переременные для хранения предыдущего смещения
    // растояние на которое смещены ученики по x
    private int dynamicLearnersXOffset = 0;
    // растояние на которое смещены оценки по x
    private int dynamicGradesXOffset = 0;
    // растояние на которое смещены ученики и оценки по y
    private int dynamicLearnersAndGradesYOffset = 0;

    // метод обрабатывающий перемещение пальца по view
    void setMove(PointF startPoint, float currX, float currY) {
        if (learnersAndGradesDataAndSizes.size() != 0) {

            // ----- смещение по x -----
            // смтрим что двигалось ученики или оценки
            if (startPoint.x <= learnersShowedWidth) {
                // --- сдвигаем учеников ---
                // убираем старое смещение
                learnersXOffset -= dynamicLearnersXOffset;
                // вычисляем новое смещение
                dynamicLearnersXOffset = (int) (currX - startPoint.x);

                // по первому ученику смотрим, что ученик из-за смещения не уходит вправо создавая пустую облать
                if (learnersAndGradesDataAndSizes.get(0).location.left + learnersXOffset + dynamicLearnersXOffset <= 0
                        // и что ширина учеников больше чем ширина доступной для отображения части view
                        && learnersAndGradesDataAndSizes.get(0).location.right > learnersShowedWidth) {

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
                        if (learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1][learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1].length - 1].location.right + gradesXOffset + dynamicGradesXOffset >= viewWidth - learnersShowedWidth) {
                            // смещаем
                            this.gradesXOffset += dynamicGradesXOffset;
                        } else {
                            // иначе ставим минимально возможное значение
                            this.gradesXOffset = viewWidth - learnersShowedWidth - learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1][learnersAndGradesDataAndSizes.get(0).learnerGrades[learnersAndGradesDataAndSizes.get(0).learnerGrades.length - 1].length - 1].location.right;
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
            if (learnersAndGradesDataAndSizes.get(0).location.top + learnersAndGradesYOffset + dynamicLearnersAndGradesYOffset <= 0
                    // и проверяем не меньше ли высота учеников чем высота view
                    && learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom >= viewHeight - learnersAndGradesOffsetForTitle) {
                // не будет ли из-за отрицательного смещения ученик уходить вверх создавая пустую облать
                if (learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom + learnersAndGradesYOffset + dynamicLearnersAndGradesYOffset >= viewHeight - learnersAndGradesOffsetForTitle - addLearnerButtonHeight) {
                    // смещаем
                    this.learnersAndGradesYOffset += dynamicLearnersAndGradesYOffset;

                } else {
                    // иначе ставим минимально возможное значение
                    this.learnersAndGradesYOffset = -learnersAndGradesDataAndSizes.get(learnersAndGradesDataAndSizes.size() - 1).location.bottom + viewHeight - learnersAndGradesOffsetForTitle - addLearnerButtonHeight;

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
    void endMove() {
        // сохраняем текущее смещение
        dynamicLearnersXOffset = 0;
        dynamicGradesXOffset = 0;
        dynamicLearnersAndGradesYOffset = 0;
    }

    // зависимые едницы в пиксели
    private float pxFromDp(float dp) {
        return dp * getResources().getDisplayMetrics().density;
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


    LearnerAndHisGradesWithSize(String name, String surname, Rect location, int leftTextMargin, int bottomTextMargin, GradeUnitWithSize[][] learnerGrades) {
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
