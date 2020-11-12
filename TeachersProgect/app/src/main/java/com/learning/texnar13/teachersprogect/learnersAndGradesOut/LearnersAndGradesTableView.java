package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class LearnersAndGradesTableView extends View {

    //final String TAG = "TeachersApp";

    // кисть для отрисовки текста
    private Paint drawTextPaint;
    // кисть для отрисовки маленького текста
    private Paint drawSmallTextPaint;
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
    // высота строки дни недели в шапке
    private int titleWeekdaysHeight;
    // высота кнопки добавить ученика под таблицей
    private int addLearnerButtonHeight;

    // ширина границы клеток в пикселях
    private int cellBorderSize;
    // диаметр круга вокруг даты в шапке
    private int dateCircleRadius;
    // свободное пространство в клетке вокруг текста
    private int cellFreeSpaceMargin;
    // расстояние между оценками находящимися в одной клетке
    private int gradesSpaceMargin;
    // минимальная ширина текста не пустой клетки
    private int cellTextMinimumWidth;
    // высота строк таблицы
    private int cellMinimumHeight;
    // ширина таблицы оценок
    private int gradesTableWidth;
    // высота всей таблицы
    private int tableHeight;

    // высота текстов заголовка
    private int headTextsHeight;
    // высота маленьких текстов
    private int smallTextsHeight;
    // размеры текста "добавить ученика"
    private int addLearnerTextWidth;

    // растояние на которое смещены ученики по x
    private int learnersXOffset = 0;
    // растояние на которое смещены оценки по x
    private int gradesXOffset = 0;
    // растояние на которое смещены ученики и оценки по y
    private int learnersAndGradesYOffset = 0;


    // массив с учениками
    private LearnerAndHisGradesWithSize[] learnersDataAndSizes;
    // массив c оценками       [номер_ученика][номер_дня][номер_урока].grades[номер_оценки]
    GradeUnitWithSize[][][] learnersGrades;
    // максимальная оценка для раскрашивания
    private long maxAnswersCount = 5;
    // названия типов пропусков
    AbsentType[] absTypes;
    // наличие комментариев на датах и уроках
    boolean[][] isComment;

    // строка с текстом фио в шапке
    String headName;
    // строка с текстом добавить ученика
    private String addLearnerButtonText;

    // первый день недели
    private int firstMonthDayOfWeek = -1;
    // текущаяя дата (-1 - этот месяц не текущий) (нумерация с 0)
    private int currentDate = -1;
    // текущий урок
    private int currentLesson = 0;


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

        // получаем имя для фио
        headName = getResources().getString(R.string.learners_and_grades_out_activity_title_table_names);
        // текст кнопки добавить ученика
        addLearnerButtonText = getResources().getString(R.string.learners_and_grades_out_activity_text_add_learner);


        // --- кисть для текста ---
        drawTextPaint = new Paint();
        drawTextPaint.setTextSize(getResources().getDimension(R.dimen.text_simple_size));
        // семейство шрифтов
        drawTextPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.geometria));
        // сглаживание
        drawTextPaint.setAntiAlias(true);

        // - проставляем размеры -
        Rect rect = new Rect();
        drawTextPaint.getTextBounds("8", 0, 1, rect);

        // высота текста заголовка
        headTextsHeight = rect.bottom - rect.top;

        // ширина границы клеток и линий вокруг даты в пикселях
        cellBorderSize = (int) (getResources().getDisplayMetrics().density);// зависимые едницы в пиксели
        if (cellBorderSize < 1) cellBorderSize = 1;
        // свободное пространство в клетке вокруг текста
        cellFreeSpaceMargin = (int) getResources().getDimension(R.dimen.simple_margin);

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

        // ширина текста "добавить ученика"
        addLearnerTextWidth = (int) drawTextPaint.measureText(addLearnerButtonText);


        // --- кисть для маленького текста ---
        drawSmallTextPaint = new Paint();
        drawSmallTextPaint.setTextSize(getResources().getDimension(R.dimen.text_sub_simple_size));
        // семейство шрифтов
        drawSmallTextPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.geometria));
        // сглаживание
        drawSmallTextPaint.setAntiAlias(true);

        // - проставляем размеры -
        drawSmallTextPaint.getTextBounds("88", 0, 2, rect);
        // высота маленьких текстов
        smallTextsHeight = rect.bottom - rect.top;
        // высота строки дни недели в шапке
        titleWeekdaysHeight = smallTextsHeight + cellBorderSize * 4;

        // --- кисть для фона ---
        backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);

    }


    // здесь происходит определение размеров view, так же их можно задать жестко
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.e("TeachersApp", "LearnersAndGradesTableView: onMeasure");
        // и поставим view размеры
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //Log.e("TeachersApp", "LearnersAndGradesTableView: onLayout");
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
    void setData(DataObject tData, AbsentType[] absTypes/*, boolean isOutAllDays*/) {

        // копируем ссылку на названия пропусков
        this.absTypes = absTypes;


        // ---- получаем полную копию данных ----

        // запрещаем выводить графику пока заполняем данные
        canDraw = false;

        // копируем сюда список, тк его можно поменять извне
        // todo в период копирования переменную tData можно изменить/обнулить что приводит к ошибкам
        tData.isInCopyProcess = true;
        ArrayList<NewLearnerAndHisGrades> data = new ArrayList<>();
        for (int learnersI = 0; learnersI < tData.learnersAndHisGrades.size(); learnersI++) {
            data.add(
                    new NewLearnerAndHisGrades(tData.learnersAndHisGrades.get(learnersI))
            );
        }


        // работаем с переданной датой
        if (tData.yearAndMonth != null) {
            // дата отображаемого месяца и года
            GregorianCalendar viewYearAndMonthCalendar = new GregorianCalendar();
            viewYearAndMonthCalendar.setTime(tData.yearAndMonth);
            viewYearAndMonthCalendar.set(GregorianCalendar.DAY_OF_MONTH, 1);
            firstMonthDayOfWeek = (int) viewYearAndMonthCalendar.get(GregorianCalendar.DAY_OF_WEEK);
            // (Вс-Сб)->(Пн-Вс)
            if (firstMonthDayOfWeek == 1) {
                firstMonthDayOfWeek = 6;
            } else {
                firstMonthDayOfWeek -= 2;
            }

            // -- проставляем текущие дату и номер урока --
            // получаем текущую дату
            GregorianCalendar currentCalendar = new GregorianCalendar();
            currentCalendar.setTime(new Date());
            currentCalendar.setLenient(false);

            // сравниваем с отображаемым календарем текущую дату
            if (viewYearAndMonthCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    viewYearAndMonthCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
                // текущая дата
                currentDate = currentCalendar.get(Calendar.DAY_OF_MONTH) - 1;
                // текущий урок
                int[][] times = (new DataBaseOpenHelper(getContext())).getSettingsTime(1);// стандартное время уроков
                int lessonNumber = 0;// текущий урок
                for (int lessonI = 0; lessonI < times.length; lessonI++) {
                    if ((currentCalendar.get(Calendar.HOUR_OF_DAY) > times[lessonI][0] ||
                            (currentCalendar.get(Calendar.HOUR_OF_DAY) == times[lessonI][0] && currentCalendar.get(Calendar.MINUTE) >= times[lessonI][1])) &&
                            (currentCalendar.get(Calendar.HOUR_OF_DAY) < times[lessonI][2] || (currentCalendar.get(Calendar.HOUR_OF_DAY) == times[lessonI][2] && currentCalendar.get(Calendar.MINUTE) <= times[lessonI][3]))
                    ) {
                        lessonNumber = lessonI;
                    }
                }
                currentLesson = lessonNumber;
            }


            // смотрим где есть коммментарии к уроку
            boolean notNull = false;
            if (tData.lessonComments != null)
                if (tData.lessonComments.length > 0)
                    if (tData.lessonComments[0].length > 0)
                        notNull = true;

            if (notNull) {
                isComment = new boolean[tData.lessonComments.length][tData.lessonComments[0].length];
                for (int dayI = 0; dayI < isComment.length; dayI++) {
                    for (int lessonI = 0; lessonI < isComment[0].length; lessonI++) {
                        isComment[dayI][lessonI] = (tData.lessonComments[dayI][lessonI] != null);
                    }
                }
            } else {
                isComment = new boolean[viewYearAndMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][9];
            }
        }

        // назначаем размер листу во view чтобы потом скопировать в него данные
        learnersDataAndSizes = new LearnerAndHisGradesWithSize[data.size()];

        // данные полностью скопированы
        tData.isInCopyProcess = false;


        // ---- создаем адаптированный под графику массив ----


        // строка для промежуточных расчетов
        StringBuilder tempString = new StringBuilder();

        // чистим размеры таблицы
        tableHeight = 0;
        gradesTableWidth = 0;

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
            learnerRect.right = learnerRect.left + learnerRect.right + (cellBorderSize + cellFreeSpaceMargin) * 2;
            learnerRect.left = 0;
            learnerRect.top = 0;

            // расчитываем отступы сверху самой клетки ученика в таблице
            // если ученик не первый
            if (learnerI != 0) {
                // сравниваем его по ширине с предыдущим
                if (learnersDataAndSizes[learnerI - 1].cellWidth >= learnerRect.right) {
                    // и если предыдущий больше, то выставляем текущему размеры предыдущего
                    learnerRect.right = learnersDataAndSizes[learnerI - 1].cellWidth;
                } else {
                    // а если текущий больше, то выставляем ВСЕМ предыдущим размер текущего
                    for (int learnerSizeI = 0; learnerSizeI < learnerI; learnerSizeI++) {
                        learnersDataAndSizes[learnerSizeI].cellWidth = learnerRect.right;
                    }
                }

//                // задаем смещение по высоте
//                learnerRect.top = learnersAndGradesDataAndSizes[learnerI - 1].location.bottom;
//                learnerRect.bottom += learnerRect.top;
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

            // и наконец сохраняем все в ученика
            learnersDataAndSizes[learnerI] = new LearnerAndHisGradesWithSize(
                    data.get(learnerI).name,
                    data.get(learnerI).surname,
                    learnerRect.width(),
                    learnerRect.height(),
                    leftMargin,
                    bottomMargin
            );

            // считаем высоту всей таблицы
            tableHeight += learnersDataAndSizes[learnerI].cellHeight;
        }

        // если предали не пустой массив
        if (data.size() != 0 && data.get(0).learnerGrades.length != 0) {


//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
//todo ----------------------------------
            // (с оценками, с уроком, с заметкой, первый урок пустого дня)
            boolean paramShowColumnsWithGrades = true;
            boolean paramShowColumnsWithLessons = true;
            boolean paramShowColumnsWithNote = true;
            boolean paramShowColumnsWithEmptyDay = true;

            boolean[][] isLessonsActive = new boolean[data.get(0).learnerGrades.length][data.get(0).learnerGrades[0].length];


            // =========================== инициализируем массив оценок ============================
            learnersGrades = new GradeUnitWithSize[data.size()][data.get(0).learnerGrades.length][];

            // отображаемые дни
            boolean[][] isLessonShow = new boolean[data.get(0).learnerGrades.length][data.get(0).learnerGrades[0].length];

            // временные переменные
            boolean viewCurrentDay;
            int lessonsCount;

            for (int dayI = 0; dayI < data.get(0).learnerGrades.length; dayI++) {

                // считаем количество уроков в следующем дне
                lessonsCount = 0;
                for (int lessonI = 0; lessonI < data.get(0).learnerGrades[dayI].length; lessonI++) {

                    // отображается ли этот день
                    viewCurrentDay = false;
                    for (int learnerI = 0; learnerI < data.size(); learnerI++) {

                        // проверяем условия для отображения текущего дня

                        // если есть оценка / пропуск
                        if ((data.get(learnerI).learnerGrades[dayI][lessonI].grades[0] != 0 ||
                                data.get(learnerI).learnerGrades[dayI][lessonI].grades[1] != 0 ||
                                data.get(learnerI).learnerGrades[dayI][lessonI].grades[2] != 0 ||
                                data.get(learnerI).learnerGrades[dayI][lessonI].absTypePoz != -1
                        ) && paramShowColumnsWithGrades) {
                            viewCurrentDay = true;
                        } else if (isComment[dayI][lessonI] && paramShowColumnsWithNote) {// урок с замткой
                            viewCurrentDay = true;
                        }

                    }
                    // если этот урок есть в расписании
                    if (isLessonsActive[dayI][lessonI] && paramShowColumnsWithLessons) {
                        viewCurrentDay = true;
                    }

                    // если день отображается
                    if (viewCurrentDay) {
                        isLessonShow[dayI][lessonI] = true;
                        lessonsCount++;
                    }

                }

                // если уроков нет, добавляем первый пустой
                if (lessonsCount == 0 && paramShowColumnsWithEmptyDay) {
                    isLessonShow[dayI][0] = true;
                    lessonsCount++;
                }

                // выставляем количество уроков в массив
                for (int learnerI = 0; learnerI < learnersGrades.length; learnerI++) {
                    learnersGrades[learnerI][dayI] = new GradeUnitWithSize[lessonsCount];
                }
            }


            // ================== заполняем графический массив оценок данными ======================

            // счетчик дней в новом массиве
            int lessonArrayPoz;

            // ------- пробегаемся по дням -------
            for (int dayI = 0; dayI < data.get(0).learnerGrades.length; dayI++) {

                // ------ пробегаемся по урокам ------
                lessonArrayPoz = 0;
                for (int lessonI = 0; lessonI < data.get(0).learnerGrades[dayI].length; lessonI++) {

                    // выводится ли этот урок
                    if (isLessonShow[dayI][lessonI]) {

                        int cellWidth = 0;
                        // ---- пробегаемся по ученикам ----
                        for (int learnerI = 0; learnerI < data.size(); learnerI++) {


                            // временная переменная для расчета ширины текста
                            int tempTextWidth = 0;


                            // --- расчитываем отступы оценок ---
                            int[] leftMargins = new int[data.get(learnerI).learnerGrades[dayI][lessonI].grades.length];

                            // если пропуск
                            if (data.get(learnerI).learnerGrades[dayI][lessonI].absTypePoz != -1) {

                                // расчитываем отступы всех трех оценок
                                leftMargins[0] = cellFreeSpaceMargin;
                                tempTextWidth = (int) drawTextPaint.measureText(
                                        absTypes[data.get(learnerI).learnerGrades[dayI][lessonI].absTypePoz]
                                );

                                leftMargins[1] = leftMargins[0] + tempTextWidth;
                                leftMargins[2] = leftMargins[1];
                                // имитируем расчет последней оценки
                                tempTextWidth = 0;
                            } else {// если пропуска нет высчитываем все три оценки

                                // складываем оценки и вычисляем их общую длинну
                                for (int gradesI = 0; gradesI < data.get(learnerI).learnerGrades[dayI][lessonI].grades.length; gradesI++) {


                                    if (data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI] == 0) {
                                        // если оценка нулевая
                                        if (gradesI == 0) {
                                            // для первой оценки
                                            leftMargins[0] = 0;
                                        } else {
                                            leftMargins[gradesI] = leftMargins[gradesI - 1] + tempTextWidth;
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
                                                leftMargins[gradesI] = leftMargins[gradesI - 1] + tempTextWidth + gradesSpaceMargin;
                                            }
                                        }
                                    }

                                    // считаем ширину оценки для отступа следующей
                                    if (data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI] > 0) {
                                        // если это не нулевой балл
                                        tempTextWidth = (int) drawTextPaint.measureText(
                                                Integer.toString(data.get(learnerI).learnerGrades[dayI][lessonI].grades[gradesI]));
                                    } else {
                                        tempTextWidth = 0;
                                    }
                                    // -1 -> ошибка

                                }
                            }


                            // ----- расчитываем размеры самой ячейки -----
                            // считаем ширину всего прямоугольника
                            cellWidth = leftMargins[leftMargins.length - 1] + tempTextWidth;

                            // ширина ячейки не должна быть меньше заданной (это нужно для абсолютно пустых уроков)
                            if (cellWidth < cellTextMinimumWidth) {
                                cellWidth = cellTextMinimumWidth;
                                // прочерк
                            }
                            // место для рамки и отступов (нулевые остаются нулевыми)
                            cellWidth += (cellFreeSpaceMargin + cellBorderSize);


                            // ----- синхронизируем эту ячейку по ширине с предыдущими -----
                            if (learnerI != 0) {
                                // сравниваем размер с предыдущими
                                if (cellWidth > learnersGrades[learnerI - 1][dayI][lessonArrayPoz].cellWidth) {
                                    // если новая ячейка больше предыдущих
                                    for (int learnerSizeI = 0; learnerSizeI < learnerI; learnerSizeI++) {
                                        learnersGrades[learnerSizeI][dayI][lessonArrayPoz].cellWidth = cellWidth;
                                    }
                                } else {
                                    // если новая ячейка меньше или равна предыдущим ставим ей их размер
                                    cellWidth = learnersGrades[learnerI - 1][dayI][lessonArrayPoz].cellWidth;
                                }
                            }


                            // ----- сохраняем размеры оценок в массив ------
                            learnersGrades[learnerI][dayI][lessonArrayPoz] = new GradeUnitWithSize(
                                    data.get(learnerI).learnerGrades[dayI][lessonI].grades,
                                    data.get(learnerI).learnerGrades[dayI][lessonI].absTypePoz,
                                    lessonI,
                                    cellWidth,
                                    learnersDataAndSizes[learnerI].cellHeight,// высоту берем от ученика
                                    cellBorderSize + cellFreeSpaceMargin,// отступ снизу
                                    leftMargins
                            );
                        }

                        // считаем ширину таблицы оценок
                        gradesTableWidth += cellWidth;
                        // к следующему уроку
                        lessonArrayPoz++;
                    }
                }
            }
        } else {
            learnersGrades = new GradeUnitWithSize[data.size()][0][0];
        }

        // разрешаем выводить графику
        canDraw = true;
    }

    // - строки для промежуточных расчетов -
    private RectF headEllipseRect = new RectF();

    private Rect smallTextTempRect = new Rect();


    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {


        // переменная запрещающая вывод графики
        if (canDraw && learnersDataAndSizes != null) {

            //if (learnersGrades != null) {-----------


            // закрашиваем фон
            canvas.drawColor(getResources().getColor(R.color.backgroundWhite));

//            // выводим надпись загрузка на месте оценок
//            if (learnersDataAndSizes.length != 0) {
//                drawTextPaint.setColor(getResources().getColor(R.color.baseBlue));
//                canvas.drawText(
//                        getResources().getString(R.string.learners_and_grades_out_activity_load_text),
//                        learnersShowedWidth + cellFreeSpaceMargin,
//                        learnersAndGradesOffsetForTitle + cellMinimumHeight / 2F + cellFreeSpaceMargin,
//                        drawTextPaint
//                );
//            }


            // --- расчитываемое смещение клеток ---
            int currentGradesCellOffsetX = learnersShowedWidth + gradesXOffset;
            int currentCellOffsetY = learnersAndGradesOffsetForTitle + learnersAndGradesYOffset + titleWeekdaysHeight * 2;

            // определяем с какого ученика начать чтобы не выводить лишнее
            int startLearner = 0;

            if (learnersDataAndSizes.length != 0) {
                while (startLearner < learnersDataAndSizes.length - 1 &&
                        (learnersDataAndSizes[startLearner].cellHeight + currentCellOffsetY) < 0) {
                    // к следующему ученику
                    currentCellOffsetY = currentCellOffsetY + learnersDataAndSizes[startLearner].cellHeight;
                    startLearner++;
                }
            }


            // ----- определяем с какого дня начать чтобы не выводить лишнее -----
            int startDay = 0;
            int startLesson = 0;


            if (learnersGrades.length > 0)
                while ((startDay < learnersGrades[0].length - 1)) {
                    if ((startLesson >= learnersGrades[0][startDay].length - 1))
                        break;
                    if ((currentGradesCellOffsetX + learnersGrades[0][startDay][startLesson].cellWidth) >= learnersShowedWidth)
                        break;

                    while (
                            (startLesson < learnersGrades[0][startDay].length - 1) &&
                                    (currentGradesCellOffsetX + learnersGrades[0][startDay][startLesson].cellWidth) < learnersShowedWidth
                    ) {
                        // к следующему уроку
                        currentGradesCellOffsetX += learnersGrades[0][startDay][startLesson].cellWidth;
                        startLesson++;
                    }
                    // к следующему дню
                    startDay++;
                }


            // ======================== выводим таблицу (учеников и оценки) ========================
            // начальная координата x к которой будем возвращаться в цикле
            int startX = currentGradesCellOffsetX;

            // пробегаемся по ученикам                                    смотрим не выходит ли ученик за границы экрана
            for (int learnerI = startLearner; learnerI < learnersDataAndSizes.length && (currentCellOffsetY) <= viewHeight; learnerI++) {

                currentGradesCellOffsetX = startX;

                // ----- пробегаемся по дням -----                                         // и что клетки не выходят за границы экрана (иначе их бессмысленно выводить)
                for (int dayIterator = startDay; dayIterator < learnersGrades[learnerI].length && currentGradesCellOffsetX <= viewWidth; dayIterator++) {

                    // пробегаемся по урокам в дне
                    for (int lessonIterator = 0; lessonIterator < learnersGrades[learnerI][dayIterator].length && currentGradesCellOffsetX <= viewWidth; lessonIterator++) {

//                        // проверяем что ширина клеток больше нуля (иначе весь столбик будет нулевой)
//                        if (learnersGrades[learnerI][dayIterator][lessonIterator].location.width() != 0) {

                        // рисуем рамку
                        backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                        canvas.drawRect(
                                currentGradesCellOffsetX,
                                currentCellOffsetY,
                                currentGradesCellOffsetX + learnersGrades[learnerI][dayIterator][lessonIterator].cellWidth,
                                currentCellOffsetY + learnersGrades[learnerI][dayIterator][lessonIterator].cellHeight,
                                backgroundPaint
                        );

                        // рисуем внутреннюю часть клетки
                        // проверяем не нажата ли ячейка
                        if (learnerI == chosenCellPoz[0] && dayIterator == chosenCellPoz[1] && lessonIterator == chosenCellPoz[2]) {
                            backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                        } else {
                            backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                        }
                        canvas.drawRect(
                                currentGradesCellOffsetX,
                                currentCellOffsetY,
                                currentGradesCellOffsetX + learnersGrades[learnerI][dayIterator][lessonIterator].cellWidth - cellBorderSize,
                                currentCellOffsetY + learnersGrades[learnerI][dayIterator][lessonIterator].cellHeight - cellBorderSize,
                                backgroundPaint
                        );


                        // печатаем оценки
                        // если стоит пропуск
                        if (learnersGrades[learnerI][dayIterator][lessonIterator].absTypePoz != -1) {

                            // выбираем цвет текста, проверяя не нажата ли ячейка
                            if (learnerI == chosenCellPoz[0] && dayIterator == chosenCellPoz[1] && lessonIterator == chosenCellPoz[2]) {
                                drawTextPaint.setColor(Color.WHITE);
                            } else {
                                drawTextPaint.setColor(Color.BLACK);
                            }

                            // ---- выводим текст ----
                            canvas.drawText(
                                    absTypes[learnersGrades[learnerI][dayIterator][lessonIterator].absTypePoz],
                                    currentGradesCellOffsetX + learnersGrades[learnerI][dayIterator][lessonIterator].leftTextMargins[0],
                                    currentCellOffsetY + learnersGrades[learnerI][dayIterator][lessonIterator].cellHeight
                                            - learnersGrades[learnerI][dayIterator][lessonIterator].bottomTextMargin,
                                    drawTextPaint
                            );

                        } else {// если пропуска нет
                            // пробегаемся по оценкам
                            for (int gradeI = 0; gradeI < learnersGrades[learnerI][dayIterator][lessonIterator].grades.length; gradeI++) {
                                // печатаем оценку
                                if (learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] > 0) {
                                    // ------ если это не нулевой балл ------

                                    // ---- выбираем цвет текста  ----
                                    if (maxAnswersCount == -1) {
                                        drawTextPaint.setColor(getResources().getColor(R.color.backgroundDarkGray));
                                    } else
                                        //1
                                        if ((int) (((float) learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 20) {
                                            drawTextPaint.setColor(getResources().getColor(R.color.grade1));
                                        } else
                                            //2
                                            if ((int) (((float) learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 41) {
                                                drawTextPaint.setColor(getResources().getColor(R.color.grade2));
                                            } else
                                                //3
                                                if ((int) (((float) learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 60) {
                                                    drawTextPaint.setColor(getResources().getColor(R.color.grade3));
                                                } else
                                                    //4
                                                    if ((int) (((float) learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 80) {
                                                        drawTextPaint.setColor(getResources().getColor(R.color.grade4));
                                                    } else
                                                        //5
                                                        if ((int) (((float) learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100) <= 100) {
                                                            drawTextPaint.setColor(getResources().getColor(R.color.grade5));
                                                        } else
                                                            // > 5
                                                            if ((int) (((float) learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] / (float) maxAnswersCount) * 100F) > 100) {
                                                                drawTextPaint.setColor(Color.DKGRAY);
                                                            }
                                    // ---- выводим текст ----
                                    canvas.drawText(
                                            Integer.toString(learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI]),
                                            currentGradesCellOffsetX + learnersGrades[learnerI][dayIterator][lessonIterator].leftTextMargins[gradeI],
                                            currentCellOffsetY + learnersGrades[learnerI][dayIterator][lessonIterator].cellHeight
                                                    - learnersGrades[learnerI][dayIterator][lessonIterator].bottomTextMargin,
                                            drawTextPaint
                                    );

                                }
                            }
                        }

                        // к следующему уроку (колонке)
                        currentGradesCellOffsetX = currentGradesCellOffsetX + learnersGrades[learnerI][dayIterator][lessonIterator].cellWidth;
                    }
                }


                // -------- выводим текущего ученика --------
                // рисуем рамку ученика
                backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                canvas.drawRect(
                        learnersXOffset,
                        currentCellOffsetY,
                        learnersShowedWidth,
                        learnersDataAndSizes[learnerI].cellHeight + currentCellOffsetY,
                        backgroundPaint
                );

                // рисуем фон ученика
                backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                canvas.drawRect(
                        learnersXOffset + cellBorderSize,
                        currentCellOffsetY,
                        learnersShowedWidth - cellBorderSize,
                        learnersDataAndSizes[learnerI].cellHeight + currentCellOffsetY - cellBorderSize,
                        backgroundPaint
                );

                // ограничиваем область рисования clip-областью
                canvas.save();
                canvas.clipRect(
                        cellBorderSize,
                        currentCellOffsetY + cellBorderSize,
                        learnersShowedWidth - cellBorderSize,
                        currentCellOffsetY + learnersDataAndSizes[learnerI].cellHeight - cellBorderSize
                );
                // и пишем его текст

                // фамилия
                drawTextPaint.setColor(Color.BLACK);
                drawTextPaint.getTextBounds(learnersDataAndSizes[learnerI].surname, 0, learnersDataAndSizes[learnerI].surname.length(), smallTextTempRect);
                int surnameHeight = smallTextTempRect.bottom - smallTextTempRect.top;
                canvas.drawText(
                        learnersDataAndSizes[learnerI].surname,
                        learnersDataAndSizes[learnerI].leftTextMargin + learnersXOffset,
                        currentCellOffsetY + cellBorderSize*4 + surnameHeight,
                        drawTextPaint
                );
                // имя
                drawSmallTextPaint.setColor(Color.BLACK);
                drawSmallTextPaint.getTextBounds(learnersDataAndSizes[learnerI].name, 0, learnersDataAndSizes[learnerI].name.length(), smallTextTempRect);
                canvas.drawText(
                        learnersDataAndSizes[learnerI].name,
                        learnersDataAndSizes[learnerI].leftTextMargin + learnersXOffset,
                        currentCellOffsetY + surnameHeight + cellBorderSize * 8 + smallTextTempRect.bottom - smallTextTempRect.top,
                        drawSmallTextPaint
                );
                // убираем clip-область
                canvas.restore();


                // к следующему ученику (строке)
                currentCellOffsetY = currentCellOffsetY + learnersDataAndSizes[learnerI].cellHeight;
            }


            // выводим кнопку добавить ученика
            drawTextPaint.setColor(Color.BLACK);
            drawTextPaint.setUnderlineText(true);
            if (learnersDataAndSizes.length != 0) {
                canvas.drawText(
                        addLearnerButtonText,
                        viewWidth / 2F - addLearnerTextWidth / 2F,
                        currentCellOffsetY + addLearnerButtonHeight / 2F + headTextsHeight / 2F,
                        drawTextPaint
                );
            } else
                canvas.drawText(
                        addLearnerButtonText,
                        viewWidth / 2F - addLearnerTextWidth / 2F,
                        addLearnerButtonHeight / 2F + headTextsHeight / 2F + learnersAndGradesOffsetForTitle,
                        drawTextPaint
                );
            drawTextPaint.setUnderlineText(false);


            // =================================== выводим шапку ===================================

            if (learnersDataAndSizes.length != 0) {

                // смещение текущего дня по ширине
                int currentHeadCellOffsetX = learnersShowedWidth + gradesXOffset;
                // не выводим дни которые находятся за предеами экрана
                for (int dayIterator = 0; dayIterator < learnersGrades[0].length && currentHeadCellOffsetX <= viewWidth; dayIterator++) {


                    // один урок без украшательств
                    if (learnersGrades[0][dayIterator].length == 1) {

                        // рисуем фон
                        backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                        canvas.drawRect(
                                currentHeadCellOffsetX,
                                titleWeekdaysHeight * 2,
                                currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth,
                                learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2,
                                backgroundPaint
                        );


                        // если этот день сегодняшний, то выделяем его
                        if (dayIterator == currentDate && learnersGrades[0][dayIterator][0].lessonNumber == currentLesson) {
                            // рисуем круг
                            backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                            canvas.drawCircle(
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F,
                                    learnersAndGradesOffsetForTitle / 2F + titleWeekdaysHeight * 2,
                                    dateCircleRadius - cellBorderSize * 2,
                                    backgroundPaint
                            );
                        }

                        // считаем размеры текста даты
                        int dateTextWidth = (int) drawTextPaint.measureText(Integer.toString((dayIterator + 1)));


                        // рисуем круок комментария
                        if (isComment[dayIterator][0]) {
                            backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                            canvas.drawCircle(
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F - dateTextWidth / 2F - cellBorderSize * 5,
                                    learnersAndGradesOffsetForTitle / 2F - headTextsHeight / 2F - cellBorderSize * 5 + titleWeekdaysHeight * 2,
                                    cellBorderSize * 4,
                                    backgroundPaint
                            );
                        }

                        // рисуем текст даты
                        drawTextPaint.setColor(Color.BLACK);
                        canvas.drawText(Integer.toString(dayIterator + 1),
                                currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F - dateTextWidth / 2F,
                                learnersAndGradesOffsetForTitle / 2F + headTextsHeight / 2F + titleWeekdaysHeight * 2,
                                drawTextPaint
                        );

                        // рисуем маленький номер урока
                        canvas.drawText(Integer.toString(learnersGrades[0][dayIterator][0].lessonNumber + 1),
                                currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F + dateTextWidth / 2F,
                                learnersAndGradesOffsetForTitle / 2F - headTextsHeight / 2F + titleWeekdaysHeight * 2,
                                drawSmallTextPaint
                        );

                        // фон дня недели
                        backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                        canvas.drawRect(
                                currentHeadCellOffsetX,
                                titleWeekdaysHeight,
                                currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth,
                                titleWeekdaysHeight * 2,
                                backgroundPaint
                        );
                        // день недели
                        if (firstMonthDayOfWeek > -1) {

                            // получаем сам текст
                            String dateText = getResources().getStringArray(R.array.schedule_month_activity_week_days_short_array)[
                                    (firstMonthDayOfWeek + dayIterator) % 7
                                    ];
                            // ширина маленького текста
                            drawSmallTextPaint.getTextBounds(dateText, 0, dateText.length(), smallTextTempRect);
                            canvas.drawText(dateText,
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F - (smallTextTempRect.left + smallTextTempRect.right) / 2F,
                                    titleWeekdaysHeight * 1.5f + (smallTextTempRect.bottom - smallTextTempRect.top) / 2F,
                                    drawSmallTextPaint
                            );
                        }

                        // отступ к следующей ячейке
                        currentHeadCellOffsetX += learnersGrades[0][dayIterator][0].cellWidth;

                    } else {

                        // пробегаемся по всем урокам
                        for (int lessonI = 0; lessonI < learnersGrades[0][dayIterator].length; lessonI++) {


                            // рисуем фон
                            backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                            canvas.drawRect(
                                    currentHeadCellOffsetX,
                                    titleWeekdaysHeight * 2,
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth,
                                    learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2,
                                    backgroundPaint
                            );

                            // рисуем общий фон дня

                            // начальный полукруг и прямоугольник
                            if (lessonI == 0) {
                                // рисуем полукруг
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                                headEllipseRect.set(
                                        currentHeadCellOffsetX +
                                                learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - dateCircleRadius + cellBorderSize / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight * 2,
                                        currentHeadCellOffsetX +
                                                learnersGrades[0][dayIterator][lessonI].cellWidth / 2F + dateCircleRadius - cellBorderSize / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight * 2
                                );
                                canvas.drawArc(headEllipseRect, 90, 180, false, backgroundPaint);
                                backgroundPaint.setStyle(Paint.Style.FILL);

                                // рисуем прямоугольник
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                                canvas.drawRect(
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight * 2,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight * 2,
                                        backgroundPaint
                                );
                            } else if (lessonI == learnersGrades[0][dayIterator].length - 1) {// окончание и прямоугольник
                                // рисуем прямоугольник
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                                canvas.drawRect(
                                        currentHeadCellOffsetX,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight * 2,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight * 2,
                                        backgroundPaint
                                );

                                // рисуем полукруг
                                headEllipseRect.set(
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - dateCircleRadius + cellBorderSize / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight * 2,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F + dateCircleRadius - cellBorderSize / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight * 2
                                );
                                canvas.drawArc(headEllipseRect, -90, 180, false, backgroundPaint);
                                backgroundPaint.setStyle(Paint.Style.FILL);

                            } else {// урок посередине
                                // рисуем прямоугольник
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                                canvas.drawRect(
                                        currentHeadCellOffsetX,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight * 2,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight * 2,
                                        backgroundPaint
                                );
                            }


                            // если этот день сегодняшний, то выделяем его
                            if (dayIterator == currentDate && learnersGrades[0][dayIterator][lessonI].lessonNumber == currentLesson) {
                                // рисуем круг
                                backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                                canvas.drawCircle(
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + titleWeekdaysHeight * 2,
                                        dateCircleRadius - cellBorderSize * 2,
                                        backgroundPaint
                                );
                            }

                            // считаем размеры текста даты
                            int dateTextWidth = (int) drawTextPaint.measureText(Integer.toString((dayIterator + 1)));


                            // рисуем круок комментария
                            if (isComment[dayIterator][lessonI]) {
                                backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                                canvas.drawCircle(
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - dateTextWidth / 2F - cellBorderSize * 5,
                                        learnersAndGradesOffsetForTitle / 2F - headTextsHeight / 2F - cellBorderSize * 5 + titleWeekdaysHeight * 2,
                                        cellBorderSize * 4,
                                        backgroundPaint
                                );
                            }

                            // рисуем текст даты
                            drawTextPaint.setColor(Color.BLACK);
                            canvas.drawText(Integer.toString(dayIterator + 1),
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - dateTextWidth / 2F,
                                    learnersAndGradesOffsetForTitle / 2F + headTextsHeight / 2F + titleWeekdaysHeight * 2,
                                    drawTextPaint
                            );

                            // рисуем маленький номер урока
                            canvas.drawText(Integer.toString(learnersGrades[0][dayIterator][lessonI].lessonNumber + 1),
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F + dateTextWidth / 2F,
                                    learnersAndGradesOffsetForTitle / 2F - headTextsHeight / 2F + titleWeekdaysHeight * 2,
                                    drawSmallTextPaint
                            );

                            // фон дня недели
                            backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                            canvas.drawRect(
                                    currentHeadCellOffsetX,
                                    titleWeekdaysHeight,
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth,
                                    titleWeekdaysHeight * 2,
                                    backgroundPaint
                            );
                            // день недели
                            if (firstMonthDayOfWeek > -1) {
                                // получаем сам текст
                                String dateText = getResources().getStringArray(R.array.schedule_month_activity_week_days_short_array)[
                                        (firstMonthDayOfWeek + dayIterator) % 7
                                        ];

                                // ширина маленького текста
                                drawSmallTextPaint.getTextBounds(dateText, 0, dateText.length(), smallTextTempRect);

                                canvas.drawText(dateText,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - (smallTextTempRect.left + smallTextTempRect.right) / 2F,
                                        titleWeekdaysHeight * 1.5f + (smallTextTempRect.bottom - smallTextTempRect.top) / 2F,
                                        drawSmallTextPaint
                                );
                            }

                            // отступ к следующей ячейке
                            currentHeadCellOffsetX += learnersGrades[0][dayIterator][lessonI].cellWidth;
                        }
                    }
                }

                // ----- фио -----
                backgroundPaint.setColor(getResources().getColor(R.color.backgroundLiteGray));
                canvas.drawRect(0, 0, learnersShowedWidth, learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2, backgroundPaint);
                drawTextPaint.setColor(getResources().getColor(R.color.backgroundGray));
                canvas.drawText(headName,
                        0, // learnersShowedWidth / 2F;//- headTextRect.right / 2F;
                        learnersAndGradesOffsetForTitle / 2F + headTextsHeight / 2F + titleWeekdaysHeight * 2,
                        drawTextPaint
                );
            }
            //}
        }

        // покрасили один раз, обнуляем
        chosenCellPoz[0] = -1;
        chosenCellPoz[1] = -1;
        chosenCellPoz[2] = -1;

        super.onDraw(canvas);
    }


    // метод обрабатывающий нажатие на view
    int[] touch(PointF downPoint, boolean longClick) {

        if (learnersDataAndSizes.length != 0) {

            // если нажали область ниже таблицы
            if (downPoint.y > (tableHeight + learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2 + learnersAndGradesYOffset)) {
                // если нажата кнопка создать ученика
                if (downPoint.y > tableHeight + learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2 + learnersAndGradesYOffset &&
                        downPoint.y <= tableHeight + learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2 + learnersAndGradesYOffset + addLearnerButtonHeight) {
                    return new int[]{-2, -2, -2};
                } else // иначе не нажато ничего
                    return new int[]{-1, -1, -1};
            }

            if (downPoint.x > learnersShowedWidth) {
                // если нажатие в области оценок

                // шапка таблицы оценок
                int cellXOffset = learnersShowedWidth + gradesXOffset;
                for (int dayIterator = 0; dayIterator < learnersGrades[0].length; dayIterator++) {
                    for (int lessonIterator = 0; lessonIterator < learnersGrades[0][dayIterator].length; lessonIterator++) {
                        if (
                            // координаты касания относительно таблицы по Y
                                (downPoint.y > titleWeekdaysHeight * 2) &&
                                        (downPoint.y <= learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2) &&
                                        // координаты касания относительно таблицы по X
                                        (downPoint.x >= cellXOffset) &&
                                        (downPoint.x <= cellXOffset + learnersGrades[0][dayIterator][lessonIterator].cellWidth)
                        ) {

                            // нажата дата урока
                            chosenCellPoz[0] = -1;
                            chosenCellPoz[1] = dayIterator;
                            chosenCellPoz[2] = lessonIterator;
                            return new int[]{-1, dayIterator, learnersGrades[0][dayIterator][lessonIterator].lessonNumber};
                        }

                        cellXOffset += learnersGrades[0][dayIterator][lessonIterator].cellWidth;
                    }
                }
                // пробегаемся по содержимому таблицы оценок
                int cellYOffset = learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2 + learnersAndGradesYOffset;
                for (int learnerIterator = 0; learnerIterator < learnersDataAndSizes.length; learnerIterator++) {
                    cellXOffset = learnersShowedWidth + gradesXOffset;
                    for (int dayIterator = 0; dayIterator < learnersGrades[learnerIterator].length; dayIterator++) {
                        for (int lessonIterator = 0; lessonIterator < learnersGrades[learnerIterator][dayIterator].length; lessonIterator++) {
                            if ((downPoint.x >= cellXOffset) &&
                                    (downPoint.x <= cellXOffset + learnersGrades[learnerIterator][dayIterator][lessonIterator].cellWidth) &&
                                    (downPoint.y >= cellYOffset) &&
                                    (downPoint.y <= cellYOffset + learnersGrades[learnerIterator][dayIterator][lessonIterator].cellHeight)
                            ) {
                                // нажата ячейка оценок
                                chosenCellPoz[0] = learnerIterator;
                                chosenCellPoz[1] = dayIterator;
                                chosenCellPoz[2] = lessonIterator;
                                return new int[]{learnerIterator, dayIterator, learnersGrades[0][dayIterator][lessonIterator].lessonNumber};
                            }

                            cellXOffset += learnersGrades[learnerIterator][dayIterator][lessonIterator].cellWidth;
                        }
                    }
                    cellYOffset += learnersGrades[learnerIterator][0][0].cellHeight;
                }


            } else {
                // если нажатие в области учеников

                // пробегаемся по таблице
                int cellYOffset = learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2 + learnersAndGradesYOffset;
                for (int learnerIterator = 0; learnerIterator < learnersDataAndSizes.length; learnerIterator++) {
                    if ((downPoint.y >= cellYOffset) && (downPoint.y <= cellYOffset + learnersDataAndSizes[learnerIterator].cellHeight)) {
                        // нажат ученик
                        chosenCellPoz[0] = learnerIterator; // номер ученика по списку
                        chosenCellPoz[1] = -1;              // номер дня
                        chosenCellPoz[2] = -1;              // номер урока
                        return new int[]{learnerIterator, -1, -1};
                    }
                    cellYOffset += learnersDataAndSizes[learnerIterator].cellHeight;
                }
            }
        } else {
            // если нажата кнопка создать ученика
            if (downPoint.y > learnersAndGradesOffsetForTitle && downPoint.y <= learnersAndGradesOffsetForTitle + addLearnerButtonHeight) {
                return new int[]{-2, -2, -2};
            }
        }
        // если не нашли совпадений в таблице то не нажато ничего
        return new int[]{-1, -1, -1};
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
        if (learnersDataAndSizes.length != 0) {

            // ----- смещение по x -----
            // смтрим что двигалось ученики или оценки
            if (startPoint.x <= learnersShowedWidth) {
                // --- сдвигаем учеников ---
                // убираем старое смещение
                learnersXOffset -= dynamicLearnersXOffset;
                // вычисляем новое смещение
                dynamicLearnersXOffset = (int) (currX - startPoint.x);

                // по первому ученику смотрим, что ученик из-за смещения не уходит вправо создавая пустую облать
                if (learnersXOffset + dynamicLearnersXOffset <= 0
                        // и что ширина учеников больше чем ширина доступной для отображения части view
                        && learnersDataAndSizes[0].cellWidth > learnersShowedWidth) {

                    // не будет ли из-за смещения ученик уходить влево создавая пустую облать
                    if (learnersDataAndSizes[0].cellWidth + learnersXOffset + dynamicLearnersXOffset >= learnersShowedWidth) {
                        // смещаем
                        this.learnersXOffset += dynamicLearnersXOffset;
                    } else {
                        // иначе ставим минимально возможное значение
                        this.learnersXOffset = -learnersDataAndSizes[0].cellWidth + learnersShowedWidth;
                    }
                } else {
                    // иначе ставим максимально возможное значение
                    this.learnersXOffset = 0;
                }
            } else {
                // --- сдвигаем оценки ---
                // убираем старое смещение
                gradesXOffset -= dynamicGradesXOffset;

                if (learnersGrades[0].length != 0) {
                    // вычисляем новое смещение
                    dynamicGradesXOffset = (int) (currX - startPoint.x);

                    // по первому столбцу оценок смотрим, не будут ли из-за положительного смещения оценки уходить вправо создавая пустую облать
                    if (gradesXOffset + dynamicGradesXOffset <= 0 && viewWidth - learnersShowedWidth < gradesTableWidth) {
                        // не будут ли из-за отрицательного смещения оценки уходить влево создавая пустую облать
                        if (gradesTableWidth + gradesXOffset + dynamicGradesXOffset >= viewWidth - learnersShowedWidth) {
                            // смещаем
                            this.gradesXOffset += dynamicGradesXOffset;
                        } else {
                            // иначе ставим минимально возможное значение
                            this.gradesXOffset = viewWidth - learnersShowedWidth - gradesTableWidth;
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

            if (learnersAndGradesYOffset + dynamicLearnersAndGradesYOffset <= 0
                    // и проверяем не меньше ли высота учеников чем высота view
                    && tableHeight > viewHeight - learnersAndGradesOffsetForTitle - titleWeekdaysHeight * 2 - addLearnerButtonHeight) {
                // не будет ли из-за отрицательного смещения ученик уходить вверх создавая пустую облать
                if (tableHeight + learnersAndGradesYOffset + dynamicLearnersAndGradesYOffset >= viewHeight - learnersAndGradesOffsetForTitle + titleWeekdaysHeight * 2 - addLearnerButtonHeight) {
                    // смещаем
                    this.learnersAndGradesYOffset += dynamicLearnersAndGradesYOffset;

                } else {
                    // иначе ставим минимально возможное значение
                    this.learnersAndGradesYOffset = viewHeight - tableHeight - learnersAndGradesOffsetForTitle - titleWeekdaysHeight * 2 - addLearnerButtonHeight;

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


}


// оценки за один урок и размеры поля в котором они находятся
class GradeUnitWithSize {
    // массив оценок
    int[] grades;

    // тип пропуска
    int absTypePoz;

    // номер урока
    int lessonNumber;

    // размеры клетки
    int cellWidth;
    int cellHeight;
    //Rect location;
    // отступ текста от нижней границы
    int bottomTextMargin;
    //  отступ текста от левой границы
    int[] leftTextMargins;

    GradeUnitWithSize(int[] grades, int absTypePoz, int lessonNumber, int cellWidth, int cellHeight, int bottomTextMargin, int[] leftTextMargins) {
        this.grades = grades;
        this.absTypePoz = absTypePoz;
        this.lessonNumber = lessonNumber;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.bottomTextMargin = bottomTextMargin;
        this.leftTextMargins = leftTextMargins;
    }
}

// ученик размеры поля ученика и его оценки
class LearnerAndHisGradesWithSize {
    // ученик
    String name;
    String surname;

    // размеры его клетки
    int cellWidth;
    int cellHeight;
    //Rect location;
    //  отступ текста от нижней границы
    int bottomTextMargin;
    //  отступ текста от левой границы
    int leftTextMargin;


    LearnerAndHisGradesWithSize(String name, String surname, int cellWidth, int cellHeight, int leftTextMargin, int bottomTextMargin) {
        this.name = name;
        this.surname = surname;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.bottomTextMargin = bottomTextMargin;
        this.leftTextMargin = leftTextMargin;
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


// подчеркнутый текст
// negativeTextButton.setPaintFlags(negativeTextButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);