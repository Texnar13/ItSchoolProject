package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class LearnersAndGradesTableView extends View {

    // кисть для отрисовки дней недели и индексов
    private TextPaint drawPaintWeekDaysAndIndexes;
    // кисть для отрисовки оценок (цвет меняется динамически)
    private TextPaint drawPaintGrade;
    // кисть для отрисовки имени(внизу клетки)
    private TextPaint drawPaintName;
    // кисть для отрисовки фамилии(вверху клетки) и дат
    private TextPaint drawPaintSecondNameAndDate;
    // кисть для отрисовки жирных дат
    private TextPaint drawPaintFatDate;
    // кисть для отрисовки предмета и месяца
    private TextPaint drawPaintTitles;

    // кисть для отрисовки фона
    private Paint backgroundPaint;


    // переменная разрешающая вывод графики
    private boolean canDraw = true;
    // позиция нажатой клетки (клетка закрашивается другим цветом)
    private final int[] chosenCellPoz = new int[]{-1, -1, -1};
    // выводим ли мы все дни
    public boolean isAllDaysShowed = true;

    // размеры view
    private int viewWidth;
    private int viewHeight;
    // ширина отображаемой части полей с именами
    private int learnersShowedWidth = 0;
    // высота шапки таблицы
    private int learnersAndGradesOffsetForTitle;
    // высота строки дни недели в шапке
    private int titleWeekdaysHeight;
    // отступ снизу текста дней недели
    private int textBottomMarginWeekDays;
    // высота кнопки добавить ученика под таблицей
    private int addLearnerButtonHeight;
    // отступ кнопки добавить ученика от таблицы
    private int addLearnerButtonTopMargin;// todo!!! область нажатия

    // ширина границы клеток в пикселях
    private int cellBorderSize;
    // диаметр круга вокруг даты в шапке
    private int dateCircleRadius;
    // свободное пространство в клетке вокруг текста
    private int cellFreeSpaceMargin;
    // отступ имен учеников слева
    private int learnersLeftTextMargin;
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
    private GradeUnitWithSize[][][] learnersGrades;
    // максимальная оценка для раскрашивания
    private long maxAnswersCount = 5;
    // названия типов пропусков
    private AbsentType[] absTypes;
    // названия дней недели
    private String[] weekDaysNames;
    // наличие комментариев на датах и уроках
    private boolean[][] isLesson;

    // строка с текстом добавить ученика
    private String addLearnerButtonText;

    // первый день недели
    private int firstMonthDayOfWeek = -1;
    // текущаяя дата (-1 - этот месяц не текущий) (нумерация с 0)
    private int currentDate = -1;
    // текущий урок
    private int currentLesson = 0;


    // выводятся ли дата и предмет в заголовке
    public boolean isSubjectAndDateInTable = true;

    public String currentSubjectTitle = "";

    public String currentDateTitle = "";


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

        // получаем текст кнопки добавить ученика
        addLearnerButtonText = getResources().getString(R.string.learners_and_grades_out_activity_text_add_learner);

        // названия дней недели
        weekDaysNames = getResources().getStringArray(R.array.schedule_month_activity_week_days_short_array);


        // --- кисть для отрисовки дней недели и индексов ---
        drawPaintWeekDaysAndIndexes = new TextPaint();
        drawPaintWeekDaysAndIndexes.setTextSize(getResources().getDimension(R.dimen.learners_and_grades_activity_index_text_size));
        drawPaintWeekDaysAndIndexes.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_semibold));
        drawPaintWeekDaysAndIndexes.setColor(Color.BLACK);
        drawPaintWeekDaysAndIndexes.setAntiAlias(true);// сглаживание

        // --- кисть для отрисовки оценок (цвет меняется динамически) ---
        drawPaintGrade = new TextPaint();
        drawPaintGrade.setTextSize(getResources().getDimension(R.dimen.learners_and_grades_activity_content_text_size));
        drawPaintGrade.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_medium));
        drawPaintGrade.setAntiAlias(true);// сглаживание

        // --- кисть для отрисовки имени(внизу клетки) ---
        drawPaintName = new TextPaint();
        drawPaintName.setTextSize(getResources().getDimension(R.dimen.learners_and_grades_activity_content_text_size));
        drawPaintName.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_medium));
        drawPaintName.setColor(Color.BLACK);
        drawPaintName.setAntiAlias(true);// сглаживание

        // --- кисть для отрисовки фамилии(вверху клетки) и дат ---
        drawPaintSecondNameAndDate = new TextPaint();
        drawPaintSecondNameAndDate.setTextSize(getResources().getDimension(R.dimen.learners_and_grades_activity_content_text_size));
        drawPaintSecondNameAndDate.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_semibold));
        drawPaintSecondNameAndDate.setColor(Color.BLACK);
        drawPaintSecondNameAndDate.setAntiAlias(true);// сглаживание

        // --- кисть для отрисовки жирных дат ---
        drawPaintFatDate = new TextPaint();
        drawPaintFatDate.setTextSize(getResources().getDimension(R.dimen.learners_and_grades_activity_content_text_size));
        drawPaintFatDate.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_extrabold));
        drawPaintFatDate.setColor(Color.BLACK);
        drawPaintFatDate.setAntiAlias(true);// сглаживание

        // --- кисть для отрисовки предмета и месяца ---
        drawPaintTitles = new TextPaint();
        drawPaintTitles.setTextSize(getResources().getDimension(R.dimen.learners_and_grades_activity_subject_and_month_text_size));
        drawPaintTitles.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_semibold));
        drawPaintTitles.setColor(getResources().getColor(R.color.backgroundWhite));
        drawPaintTitles.setAntiAlias(true);// сглаживание


        // - проставляем размеры -
        Rect rect = new Rect();
        drawPaintSecondNameAndDate.getTextBounds("8", 0, 1, rect);

        // высота текста заголовка
        headTextsHeight = rect.bottom - rect.top;

        // ширина границы клеток и линий вокруг даты в пикселях
        cellBorderSize = (int) (getResources().getDisplayMetrics().density);// зависимые едницы в пиксели
        if (cellBorderSize < 1) cellBorderSize = 1;
        // свободное пространство в клетке вокруг текста
        cellFreeSpaceMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        // отступ имен учеников слева
        learnersLeftTextMargin = cellBorderSize + cellFreeSpaceMargin;

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
        // отступ кнопки добавить ученика от таблицы
        addLearnerButtonTopMargin = cellFreeSpaceMargin;

        // ширина текста "добавить ученика"
        addLearnerTextWidth = (int) drawPaintGrade.measureText(addLearnerButtonText);


        // - проставляем размеры -
        drawPaintWeekDaysAndIndexes.getTextBounds("88", 0, 2, rect);
        // высота строки дни недели в шапке
        titleWeekdaysHeight = (rect.bottom - rect.top + cellBorderSize * 4) * 2;

        // считаем отступ снизу для дней недели
        drawPaintWeekDaysAndIndexes.getTextBounds("m", 0, 1, rect);
        textBottomMarginWeekDays = (int) (titleWeekdaysHeight / 2f - (rect.bottom - rect.top) / 2f);

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
    void setData(DataObject tData, AbsentType[] absTypes /*, boolean isOutAllDays*/) {

        // копируем ссылку на названия пропусков
        this.absTypes = absTypes;

        // ---- получаем полную копию данных ----

        // запрещаем выводить графику пока заполняем данные
        canDraw = false;

        // копируем сюда список, тк его можно поменять извне
        // todo в период копирования переменную tData можно изменить/обнулить что приводит к ошибкам
        tData.isInCopyProcess = true;
        NewLearnerAndHisGrades[] data = new NewLearnerAndHisGrades[tData.learnersAndHisGrades.length];

        for (int learnersI = 0; learnersI < tData.learnersAndHisGrades.length; learnersI++) {
            data[learnersI] = new NewLearnerAndHisGrades(tData.learnersAndHisGrades[learnersI]);
        }


        // работаем с переданной датой
        if (tData.yearAndMonth != null) {
            // дата отображаемого месяца и года
            GregorianCalendar viewYearAndMonthCalendar = new GregorianCalendar();
            viewYearAndMonthCalendar.setTime(tData.yearAndMonth);
            viewYearAndMonthCalendar.set(GregorianCalendar.DAY_OF_MONTH, 1);

            // (Вс-Сб)->(Пн-Вс)
            firstMonthDayOfWeek = (viewYearAndMonthCalendar.get(GregorianCalendar.DAY_OF_WEEK) == Calendar.SUNDAY) ? 6 : viewYearAndMonthCalendar.get(GregorianCalendar.DAY_OF_WEEK) - 2;

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


            // смотрим где есть уроки
            if (tData.lessonsUnits != null) {
                isLesson = new boolean[tData.lessonsUnits.length][tData.lessonsUnits[0].length];
                for (int dayI = 0; dayI < isLesson.length; dayI++) {
                    for (int lessonI = 0; lessonI < isLesson[0].length; lessonI++) {
                        isLesson[dayI][lessonI] = (tData.lessonsUnits[dayI][lessonI] != null);
                    }
                }
            } else {
                isLesson = new boolean[viewYearAndMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)][(new DataBaseOpenHelper(getContext())).getSettingsTime(1).length];//todo оптимизировать
            }
        }

        // назначаем размер листу во view чтобы потом скопировать в него данные
        learnersDataAndSizes = new LearnerAndHisGradesWithSize[data.length];

        // данные полностью скопированы
        tData.isInCopyProcess = false;


        // ---- создаем адаптированный под графику массив ----


        // строка для промежуточных расчетов
        StringBuilder tempString = new StringBuilder();

        // чистим размеры таблицы
        tableHeight = 0;
        gradesTableWidth = 0;

        // -------- пробегаемся по ученикам и выясняем их размеры --------
        for (int learnerI = 0; learnerI < data.length; learnerI++) {
            // чистим строку
            tempString.delete(0, tempString.length());

            // расчитываем размер клетки этого ученика
            float cellHeight;
            float cellWidth;

            {// считываем размеры клетки с именем и фамилией ученика
                // фамилия
                Rect tempRect = new Rect();
                drawPaintSecondNameAndDate.getTextBounds(data[learnerI].surname, 0, data[learnerI].surname.length(), tempRect);
                float learnerSurnameWidth = tempRect.left + tempRect.right;
                float learnerSurnameHeight = tempRect.bottom - tempRect.top;
                // имя
                drawPaintName.getTextBounds(data[learnerI].name, 0, data[learnerI].name.length(), tempRect);
                float learnerNameWidth = tempRect.left + tempRect.right;
                float learnerNameHeight = tempRect.bottom - tempRect.top;

                cellWidth = Math.max(learnerSurnameWidth, learnerNameWidth) + (cellBorderSize + cellFreeSpaceMargin) * 2;
                // высота верхнего + высота нижнего + граница снизу + по половинке свободного пространства + отступ между именем и фамилией
                cellHeight = learnerSurnameHeight + learnerNameHeight + cellBorderSize + cellFreeSpaceMargin + cellBorderSize * 4;

            }


            // подстраиваем ширину клетки под остальные
            if (learnerI != 0) {
                // сравниваем его по ширине с предыдущим
                if (learnersDataAndSizes[learnerI - 1].cellWidth >= cellWidth) {
                    // и если предыдущий больше, то выставляем текущему размеры предыдущего
                    cellWidth = learnersDataAndSizes[learnerI - 1].cellWidth;
                } else {
                    // а если текущий больше, то выставляем ВСЕМ предыдущим размер текущего
                    for (int learnerSizeI = 0; learnerSizeI < learnerI; learnerSizeI++) {
                        learnersDataAndSizes[learnerSizeI].cellWidth = (int) cellWidth;
                    }
                }
            } else {
                // задаем минимальный размер первому ученику
                if (cellWidth < learnersShowedWidth) {
                    cellWidth = learnersShowedWidth;
                }
            }

            // если высота меньше минимально заданной
            if (cellHeight < cellMinimumHeight) {
                cellHeight = cellMinimumHeight;
            }

            // и наконец сохраняем все в ученика
            learnersDataAndSizes[learnerI] = new LearnerAndHisGradesWithSize(
                    data[learnerI].name,
                    data[learnerI].surname,
                    (int) cellWidth,
                    (int) cellHeight,
                    cellBorderSize + cellFreeSpaceMargin
            );

            // считаем высоту всей таблицы
            tableHeight += learnersDataAndSizes[learnerI].cellHeight;
        }

        // если предали не пустой массив
        if (data.length != 0 && data[0].learnerGrades.length != 0) {

            // (с оценками, с уроком, первый урок пустого дня)
            boolean paramShowColumnsWithGrades = true; // отключение оценок в уроке кроме самой начальной
            boolean paramShowColumnsWithLesson = true; // уроки кроме первого отображаются?
            boolean paramShowColumnsWithEmptyDay = isAllDaysShowed; // пустые дни отображаются?

            // =========================== инициализируем массив оценок ============================
            learnersGrades = new GradeUnitWithSize[data.length][data[0].learnerGrades.length][];

            // отображаемые дни
            boolean[][] isLessonShow = new boolean[data[0].learnerGrades.length][data[0].learnerGrades[0].length];

            // временные переменные
            boolean viewCurrentDay;
            int lessonsCount;
            boolean isGrades;

            for (int dayI = 0; dayI < data[0].learnerGrades.length; dayI++) {

                // считаем количество уроков в следующем дне
                lessonsCount = 0;
                for (int lessonI = 0; lessonI < data[0].learnerGrades[dayI].length; lessonI++) {

                    // отображается ли этот день
                    viewCurrentDay = false;
                    for (NewLearnerAndHisGrades datum : data) {

                        // проверяем условия для отображения текущего дня

                        // если есть оценка / пропуск
                        isGrades = false;
                        if (datum.learnerGrades[dayI][lessonI] != null) {
                            if (datum.learnerGrades[dayI][lessonI].grades[0] != 0 ||
                                    datum.learnerGrades[dayI][lessonI].grades[1] != 0 ||
                                    datum.learnerGrades[dayI][lessonI].grades[2] != 0 ||
                                    datum.learnerGrades[dayI][lessonI].absTypePoz != -1) {
                                isGrades = true;
                            }
                        }


                        if (isGrades && paramShowColumnsWithGrades) {
                            viewCurrentDay = true;
                        } else if (isLesson[dayI][lessonI] && paramShowColumnsWithLesson) {// урок с заметкой
                            viewCurrentDay = true;
                        }

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
            // переменная хранящая текущую клетку с оценками
            GradeUnit currentGrades;
            // временная переменная для расчета ширины текста
            int tempTextWidth = 0;


            // ------- пробегаемся по дням -------
            for (int dayI = 0; dayI < data[0].learnerGrades.length; dayI++) {

                // ------ пробегаемся по урокам ------
                lessonArrayPoz = 0;
                for (int lessonI = 0; lessonI < data[0].learnerGrades[dayI].length; lessonI++) {

                    // выводится ли этот урок
                    if (isLessonShow[dayI][lessonI]) {

                        int cellWidth = 0;
                        // ---- пробегаемся по ученикам ----
                        for (int learnerI = 0; learnerI < data.length; learnerI++) {


                            // достаем массив текущих оценок
                            if (data[learnerI].learnerGrades[dayI][lessonI] == null) {
                                currentGrades = new GradeUnit(new int[3], -1, new int[3], -1);
                            } else {
                                currentGrades = data[learnerI].learnerGrades[dayI][lessonI];
                            }


                            // --- расчитываем отступы оценок ---
                            int[] leftMargins = new int[currentGrades.grades.length];

                            // если пропуск
                            if (currentGrades.absTypePoz != -1) {

                                // расчитываем отступы всех трех оценок
                                leftMargins[0] = cellFreeSpaceMargin;
                                tempTextWidth = (int) drawPaintGrade.measureText(
                                        absTypes[currentGrades.absTypePoz].absTypeName
                                );

                                leftMargins[1] = leftMargins[0] + tempTextWidth;
                                leftMargins[2] = leftMargins[1];
                                // имитируем расчет последней оценки
                                tempTextWidth = 0;
                            } else {// если пропуска нет высчитываем все три оценки

                                // складываем оценки и вычисляем их общую длинну
                                for (int gradesI = 0; gradesI < currentGrades.grades.length; gradesI++) {


                                    if (currentGrades.grades[gradesI] == 0) {
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
                                    if (currentGrades.grades[gradesI] > 0) {
                                        // если это не нулевой балл
                                        tempTextWidth = (int) drawPaintGrade.measureText(
                                                Integer.toString(currentGrades.grades[gradesI]));
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
                                    currentGrades.grades,
                                    currentGrades.absTypePoz,
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
            learnersGrades = new GradeUnitWithSize[data.length][0][0];
        }

        // разрешаем выводить графику
        canDraw = true;
    }

    // - строки для промежуточных расчетов -
    private final RectF headEllipseRect = new RectF();

    private final Rect smallTextTempRect = new Rect();


    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {

        // переменная запрещающая вывод графики
        if (canDraw && learnersDataAndSizes != null) {

            // при выполненных расчетах проверяем смещение
            checkOffset();


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
            int currentCellOffsetY = learnersAndGradesOffsetForTitle + learnersAndGradesYOffset + titleWeekdaysHeight;

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
            for (int learnerI = startLearner; learnerI < learnersDataAndSizes.length &&
                    (currentCellOffsetY) <= viewHeight; learnerI++) {

                currentGradesCellOffsetX = startX;

                // ----- пробегаемся по дням -----                                         // и что клетки не выходят за границы экрана (иначе их бессмысленно выводить)
                for (int dayIterator = startDay; dayIterator < learnersGrades[learnerI].length &&
                        currentGradesCellOffsetX <= viewWidth; dayIterator++) {

                    // пробегаемся по урокам в дне
                    for (int lessonIterator = 0; lessonIterator < learnersGrades[learnerI][dayIterator].length
                            && currentGradesCellOffsetX <= viewWidth; lessonIterator++) {

                        // рисуем рамку
                        backgroundPaint.setColor(getResources().getColor((learnerI % 2 == 0) ?
                                R.color.backgroundWhite : R.color.backgroundDarkWhite
                        ));
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
                        } else
                            backgroundPaint.setColor(getResources().getColor((learnerI % 2 == 1) ?
                                    R.color.backgroundWhite : R.color.backgroundDarkWhite
                            ));
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
                                drawPaintGrade.setColor(Color.WHITE);
                            } else {
                                drawPaintGrade.setColor(Color.BLACK);
                            }

                            // ---- выводим текст ----
                            canvas.drawText(
                                    absTypes[learnersGrades[learnerI][dayIterator][lessonIterator].absTypePoz].absTypeName,
                                    currentGradesCellOffsetX + learnersGrades[learnerI][dayIterator][lessonIterator].leftTextMargins[0],
                                    currentCellOffsetY + learnersGrades[learnerI][dayIterator][lessonIterator].cellHeight - cellBorderSize
                                            - learnersGrades[learnerI][dayIterator][lessonIterator].bottomTextMargin,
                                    drawPaintGrade
                            );

                        } else {// если пропуска нет
                            // пробегаемся по оценкам
                            for (int gradeI = 0; gradeI < learnersGrades[learnerI][dayIterator][lessonIterator].grades.length; gradeI++) {
                                // печатаем оценку
                                if (learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] > 0) {
                                    // ------ если это не нулевой балл ------

                                    // ---- выбираем цвет текста  ----
                                    int gradeColor = getResources().getColor(R.color.backgroundDarkGray);// это цвет любых значений вне пределов, а также Н
                                    if (maxAnswersCount != -1) {
                                        int percentGrade = (int) (
                                                learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI] * 100F / maxAnswersCount);

                                        if (percentGrade <= 20) {// 1
                                            gradeColor = getResources().getColor(R.color.grade1);
                                        } else if (percentGrade <= 41) {// 2
                                            gradeColor = getResources().getColor(R.color.grade2);
                                        } else if (percentGrade <= 60) {// 3
                                            gradeColor = getResources().getColor(R.color.grade3);
                                        } else if (percentGrade <= 80) {// 4
                                            gradeColor = getResources().getColor(R.color.grade4);
                                        } else if (percentGrade <= 100) {// 5
                                            gradeColor = getResources().getColor(R.color.grade5);
                                        }
                                    }
                                    drawPaintGrade.setColor(gradeColor);

                                    // ---- выводим текст ----
                                    canvas.drawText(
                                            Integer.toString(learnersGrades[learnerI][dayIterator][lessonIterator].grades[gradeI]),
                                            currentGradesCellOffsetX + learnersGrades[learnerI][dayIterator][lessonIterator].leftTextMargins[gradeI],
                                            currentCellOffsetY + learnersGrades[learnerI][dayIterator][lessonIterator].cellHeight
                                                    - learnersGrades[learnerI][dayIterator][lessonIterator].bottomTextMargin,
                                            drawPaintGrade
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

                backgroundPaint.setColor(getResources().getColor((learnerI % 2 == 0) ?
                        R.color.backgroundWhite : R.color.backgroundDarkWhite
                ));
                canvas.drawRect(
                        learnersXOffset,
                        currentCellOffsetY,
                        learnersShowedWidth,
                        learnersDataAndSizes[learnerI].cellHeight + currentCellOffsetY,
                        backgroundPaint
                );

                // рисуем фон ученика

                backgroundPaint.setColor(getResources().getColor((learnerI % 2 == 1) ?
                        R.color.backgroundWhite : R.color.backgroundDarkWhite
                ));
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
                        currentCellOffsetY,
                        learnersShowedWidth - cellBorderSize,
                        currentCellOffsetY + learnersDataAndSizes[learnerI].cellHeight - cellBorderSize
                );
                // и пишем его текст

                // фамилия
                drawPaintSecondNameAndDate.getTextBounds(learnersDataAndSizes[learnerI].surname, 0, learnersDataAndSizes[learnerI].surname.length(), smallTextTempRect);

                canvas.drawText(
                        learnersDataAndSizes[learnerI].surname,
                        learnersLeftTextMargin + learnersXOffset,
                        currentCellOffsetY - cellBorderSize * 2 + (learnersDataAndSizes[learnerI].cellHeight - cellBorderSize) / 2F,
                        drawPaintSecondNameAndDate
                );
                // имя
                drawPaintName.getTextBounds(learnersDataAndSizes[learnerI].name, 0, learnersDataAndSizes[learnerI].name.length(), smallTextTempRect);
                int nameHeight = smallTextTempRect.bottom - smallTextTempRect.top;
                canvas.drawText(
                        learnersDataAndSizes[learnerI].name,
                        learnersLeftTextMargin + learnersXOffset,
                        currentCellOffsetY + cellBorderSize * 2 + (learnersDataAndSizes[learnerI].cellHeight - cellBorderSize) / 2F + nameHeight,
                        drawPaintName
                );
                // убираем clip-область
                canvas.restore();


                // к следующему ученику (строке)
                currentCellOffsetY = currentCellOffsetY + learnersDataAndSizes[learnerI].cellHeight;
            }


            // выводим кнопку добавить ученика

            drawPaintGrade.setUnderlineText(true);
            drawPaintGrade.setColor(Color.BLACK);
            if (learnersDataAndSizes.length != 0) {
//                // ее фон
//                backgroundPaint.setColor(getResources().getColor(R.color.baseOrange));
//                headEllipseRect.set(viewWidth / 2F - addLearnerTextWidth / 2F,
//                        currentCellOffsetY + headTextsHeight / 2F+addLearnerButtonTopMargin,
//                        viewWidth / 2F + addLearnerTextWidth / 2F,
//                        currentCellOffsetY  + headTextsHeight / 2F + addLearnerButtonHeight + addLearnerButtonTopMargin);
//                canvas.drawRoundRect(headEllipseRect, 50, 50, backgroundPaint);//hhjghj

                // ее текст
                canvas.drawText(
                        addLearnerButtonText,
                        viewWidth / 2F - addLearnerTextWidth / 2F,
                        currentCellOffsetY + addLearnerButtonHeight / 2F + headTextsHeight / 2F,
                        drawPaintGrade
                );
            } else
                canvas.drawText(
                        addLearnerButtonText,
                        viewWidth / 2F - addLearnerTextWidth / 2F,
                        addLearnerButtonHeight / 2F + headTextsHeight / 2F + learnersAndGradesOffsetForTitle,
                        drawPaintGrade
                );
            drawPaintGrade.setUnderlineText(false);


            // =================================== выводим шапку ===================================

            if (learnersDataAndSizes.length != 0) {

                // смещение текущего дня по ширине
                int currentHeadCellOffsetX = learnersShowedWidth + gradesXOffset;
                // не выводим дни которые находятся за предеами экрана
                for (int dayIterator = 0; dayIterator < learnersGrades[0].length &&
                        currentHeadCellOffsetX <= viewWidth; dayIterator++) {


                    // один урок без украшательств
                    if (learnersGrades[0][dayIterator].length == 1) {

                        // рисуем фон
                        backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                        canvas.drawRect(
                                currentHeadCellOffsetX,
                                titleWeekdaysHeight,
                                currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth,
                                learnersAndGradesOffsetForTitle + titleWeekdaysHeight,
                                backgroundPaint
                        );


                        // если этот день сегодняшний, то выделяем его
                        if (dayIterator == currentDate && learnersGrades[0][dayIterator][0].lessonNumber == currentLesson) {
                            // рисуем круг
                            backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                            canvas.drawCircle(
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F,
                                    learnersAndGradesOffsetForTitle / 2F + titleWeekdaysHeight,
                                    dateCircleRadius - cellBorderSize * 2,
                                    backgroundPaint
                            );
                        }

                        // считаем размеры текста даты
                        int dateTextWidth = (int) drawPaintSecondNameAndDate.measureText(Integer.toString((dayIterator + 1)));


                        // есть ли на этой дате урок
                        if (isLesson[dayIterator][learnersGrades[0][dayIterator][0].lessonNumber]) {
                            // рисуем текст даты
                            canvas.drawText(Integer.toString(dayIterator + 1),
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F - dateTextWidth / 2F,
                                    learnersAndGradesOffsetForTitle / 2F + headTextsHeight / 2F + titleWeekdaysHeight,
                                    drawPaintFatDate
                            );

                            // рисуем маленький номер урока
                            canvas.drawText(Integer.toString(learnersGrades[0][dayIterator][0].lessonNumber + 1),
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F + dateTextWidth / 2F,
                                    learnersAndGradesOffsetForTitle / 2F - headTextsHeight / 2F + titleWeekdaysHeight,
                                    drawPaintWeekDaysAndIndexes
                            );
                        } else {
                            // рисуем текст даты
                            canvas.drawText(Integer.toString(dayIterator + 1),
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F - dateTextWidth / 2F,
                                    learnersAndGradesOffsetForTitle / 2F + headTextsHeight / 2F + titleWeekdaysHeight,
                                    drawPaintSecondNameAndDate
                            );

                            // самый простой первый урок
                            if (learnersGrades[0][dayIterator][0].lessonNumber != 0) {
                                // рисуем маленький номер урока
                                canvas.drawText(Integer.toString(learnersGrades[0][dayIterator][0].lessonNumber + 1),
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F + dateTextWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - headTextsHeight / 2F + titleWeekdaysHeight,
                                        drawPaintWeekDaysAndIndexes
                                );
                            }
                        }

                        // рисуем фон полоски месяца
                        backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                        canvas.drawRect(
                                currentHeadCellOffsetX,
                                0,
                                currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth,
                                titleWeekdaysHeight,
                                backgroundPaint
                        );

                        // фон дня недели
                        backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                        canvas.drawRect(
                                currentHeadCellOffsetX,
                                0,
                                currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth,
                                titleWeekdaysHeight,
                                backgroundPaint
                        );
                        // день недели
                        if (firstMonthDayOfWeek > -1) {

                            // получаем сам текст
                            String dateText = weekDaysNames[(firstMonthDayOfWeek + dayIterator) % 7];
                            // ширина маленького текста
                            drawPaintWeekDaysAndIndexes.getTextBounds(dateText, 0, dateText.length(), smallTextTempRect);
                            canvas.drawText(dateText,
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][0].cellWidth / 2F - (smallTextTempRect.left + smallTextTempRect.right) / 2F,
                                    titleWeekdaysHeight - textBottomMarginWeekDays,
                                    drawPaintWeekDaysAndIndexes
                            );
                        }

                        // отступ к следующей ячейке
                        currentHeadCellOffsetX += learnersGrades[0][dayIterator][0].cellWidth;

                    } else {

                        // пробегаемся по всем урокам
                        for (int lessonI = 0; lessonI < learnersGrades[0][dayIterator].length; lessonI++) {


                            // рисуем фон
                            backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                            canvas.drawRect(
                                    currentHeadCellOffsetX,
                                    titleWeekdaysHeight,
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth,
                                    learnersAndGradesOffsetForTitle + titleWeekdaysHeight,
                                    backgroundPaint
                            );

                            // рисуем общий фон дня

                            // начальный полукруг и прямоугольник
                            if (lessonI == 0) {
                                // рисуем полукруг
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                                headEllipseRect.set(
                                        currentHeadCellOffsetX +
                                                learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - dateCircleRadius + cellBorderSize / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight,
                                        currentHeadCellOffsetX +
                                                learnersGrades[0][dayIterator][lessonI].cellWidth / 2F + dateCircleRadius - cellBorderSize / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight
                                );
                                canvas.drawArc(headEllipseRect, 90, 180, false, backgroundPaint);
                                backgroundPaint.setStyle(Paint.Style.FILL);

                                // рисуем прямоугольник
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                                canvas.drawRect(
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight,
                                        backgroundPaint
                                );
                            } else if (lessonI == learnersGrades[0][dayIterator].length - 1) {// окончание и прямоугольник
                                // рисуем прямоугольник
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                                canvas.drawRect(
                                        currentHeadCellOffsetX,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight,
                                        backgroundPaint
                                );

                                // рисуем полукруг
                                headEllipseRect.set(
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - dateCircleRadius + cellBorderSize / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F + dateCircleRadius - cellBorderSize / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight
                                );
                                canvas.drawArc(headEllipseRect, -90, 180, false, backgroundPaint);
                                backgroundPaint.setStyle(Paint.Style.FILL);

                            } else {// урок посередине
                                // рисуем прямоугольник
                                backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                                canvas.drawRect(
                                        currentHeadCellOffsetX,
                                        learnersAndGradesOffsetForTitle / 2F - dateCircleRadius + cellBorderSize / 2F + titleWeekdaysHeight,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth,
                                        learnersAndGradesOffsetForTitle / 2F + dateCircleRadius - cellBorderSize / 2F + titleWeekdaysHeight,
                                        backgroundPaint
                                );
                            }


                            // если этот день сегодняшний, то выделяем его
                            if (dayIterator == currentDate && learnersGrades[0][dayIterator][lessonI].lessonNumber == currentLesson) {
                                // рисуем круг
                                backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                                canvas.drawCircle(
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + titleWeekdaysHeight,
                                        dateCircleRadius - cellBorderSize * 2,
                                        backgroundPaint
                                );
                            }

                            // считаем размеры текста даты
                            int dateTextWidth = (int) drawPaintSecondNameAndDate.measureText(Integer.toString((dayIterator + 1)));

                            // есть ли на этой дате урок
                            if (isLesson[dayIterator][learnersGrades[0][dayIterator][lessonI].lessonNumber]) {
                                // рисуем текст даты
                                canvas.drawText(Integer.toString(dayIterator + 1),
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - dateTextWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + headTextsHeight / 2F + titleWeekdaysHeight,
                                        drawPaintFatDate
                                );

                                // рисуем маленький номер урока
                                canvas.drawText(Integer.toString(learnersGrades[0][dayIterator][lessonI].lessonNumber + 1),
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F + dateTextWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - headTextsHeight / 2F + titleWeekdaysHeight,
                                        drawPaintWeekDaysAndIndexes
                                );
                            } else {
                                // рисуем текст даты
                                canvas.drawText(Integer.toString(dayIterator + 1),
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - dateTextWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F + headTextsHeight / 2F + titleWeekdaysHeight,
                                        drawPaintSecondNameAndDate
                                );

                                // рисуем маленький номер урока
                                canvas.drawText(Integer.toString(learnersGrades[0][dayIterator][lessonI].lessonNumber + 1),
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F + dateTextWidth / 2F,
                                        learnersAndGradesOffsetForTitle / 2F - headTextsHeight / 2F + titleWeekdaysHeight,
                                        drawPaintWeekDaysAndIndexes
                                );
                            }

                            // рисуем фон полоски месяца
                            backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                            canvas.drawRect(currentHeadCellOffsetX, 0, currentHeadCellOffsetX +
                                            learnersGrades[0][dayIterator][lessonI].cellWidth,
                                    titleWeekdaysHeight, backgroundPaint);

                            // фон дня недели
                            backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                            canvas.drawRect(
                                    currentHeadCellOffsetX,
                                    0,
                                    currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth,
                                    titleWeekdaysHeight,
                                    backgroundPaint
                            );
                            // день недели
                            if (firstMonthDayOfWeek > -1) {
                                // получаем сам текст
                                String dateText = weekDaysNames[(firstMonthDayOfWeek + dayIterator) % 7];

                                // ширина маленького текста
                                drawPaintWeekDaysAndIndexes.getTextBounds(dateText, 0, dateText.length(), smallTextTempRect);
                                canvas.drawText(dateText,
                                        currentHeadCellOffsetX + learnersGrades[0][dayIterator][lessonI].cellWidth / 2F - (smallTextTempRect.left + smallTextTempRect.right) / 2F,
                                        titleWeekdaysHeight - textBottomMarginWeekDays,
                                        drawPaintWeekDaysAndIndexes
                                );
                            }

                            // отступ к следующей ячейке
                            currentHeadCellOffsetX += learnersGrades[0][dayIterator][lessonI].cellWidth;
                        }
                    }
                }

//                // рисуем текст месяца
//                // ширина текста
//                drawSmallTextPaint.getTextBounds(currentDateTitle, 0, currentDateTitle.length(), smallTextTempRect);
//                canvas.drawText(currentDateTitle,
//                        learnersShowedWidth + gradesXOffset + cellBorderSize,
//                        titleWeekdaysHeight / 2f - smallTextTempRect.top / 2F,
//                        drawSmallTextPaint
//                );


                // ----- уголок в заголовке -----
                if (isSubjectAndDateInTable) {
                    float headSize = (learnersAndGradesOffsetForTitle + titleWeekdaysHeight);

                    // рисуем фон
                    backgroundPaint.setColor(getResources().getColor(R.color.baseBlue));
                    canvas.drawRect(0, 0, learnersShowedWidth, learnersAndGradesOffsetForTitle + titleWeekdaysHeight, backgroundPaint);

                    // рисуем текст предмета
                    String shortSubjectText = TextUtils.ellipsize(
                            currentSubjectTitle,
                            drawPaintTitles,
                            learnersShowedWidth - learnersLeftTextMargin,
                            TextUtils.TruncateAt.END
                    ).toString();
                    canvas.drawText(
                            shortSubjectText,
                            (learnersShowedWidth - drawPaintTitles.measureText(shortSubjectText)) / 2F,
                            headSize / 4F + headTextsHeight / 2F,
                            drawPaintTitles
                    );

                    int imageSize = (int) (headSize / 2f * 0.66);

                    // рисуем картинки
                    Bitmap arrowLeft = getBitmapFromVectorDrawable(
                            getContext(),
                            R.drawable.base_button_arrow_back_inverse,
                            imageSize,
                            imageSize
                    );
                    canvas.drawBitmap(
                            arrowLeft,
                            headSize / 4F - imageSize / 2F,
                            headSize * 0.75F - imageSize / 2F,
                            backgroundPaint
                    );

                    Bitmap arrowRight = getBitmapFromVectorDrawable(
                            getContext(),
                            R.drawable.base_button_arrow_forvard_inverse,
                            imageSize,
                            imageSize
                    );
                    canvas.drawBitmap(
                            arrowRight,
                            learnersShowedWidth - headSize * 0.25F - imageSize / 2F,
                            headSize * 0.75F - imageSize / 2F,
                            backgroundPaint
                    );

                    // рисуем текст даты
                    String shortDateText = TextUtils.ellipsize(
                            currentDateTitle,
                            drawPaintTitles,
                            learnersShowedWidth - headSize - learnersLeftTextMargin * 2,
                            TextUtils.TruncateAt.END
                    ).toString();
                    canvas.drawText(shortDateText,
                            (learnersShowedWidth - drawPaintTitles.measureText(shortDateText)) / 2F,
                            headSize * 0.75F + headTextsHeight / 2F,
                            drawPaintTitles
                    );
                } else {
                    // если уголок серый
                    // рамка уголка
                    backgroundPaint.setColor(getResources().getColor(R.color.backgroundWhite));
                    canvas.drawRect(0, 0, learnersShowedWidth, learnersAndGradesOffsetForTitle +
                            titleWeekdaysHeight, backgroundPaint);
                    // фон уголка
                    backgroundPaint.setColor(getResources().getColor(R.color.backgroundDarkWhite));
                    canvas.drawRect(0, 0, learnersShowedWidth - cellBorderSize,
                            learnersAndGradesOffsetForTitle + titleWeekdaysHeight - cellBorderSize, backgroundPaint);
                }
            }
        }

        // покрасили один раз, обнуляем
        chosenCellPoz[0] = -1;
        chosenCellPoz[1] = -1;
        chosenCellPoz[2] = -1;

        super.onDraw(canvas);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId, int bitmapWidth, int bitmapHeight) {
        // создаем drawable из векторной картинки
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        assert drawable != null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        // перерисовываем drawable в bitmap
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, false);
    }


    // метод обрабатывающий нажатие на view
    int[] touch(PointF downPoint, boolean longClick) {
        /* {место в таблице, ученик, день, урок}
           0 - мимо
           1 - ученик
           2 - дата
           3 - оценка
           4 - добавить ученика
           5 - предмет
           6 - дата назад
           7 - дата вперед
        */

        if (learnersDataAndSizes.length != 0) {

            // если нажали область ниже таблицы
            if (downPoint.y > (tableHeight + learnersAndGradesOffsetForTitle + titleWeekdaysHeight + learnersAndGradesYOffset)) {
                // если нажата кнопка создать ученика
                if (downPoint.y > tableHeight + learnersAndGradesOffsetForTitle + titleWeekdaysHeight + learnersAndGradesYOffset &&
                        downPoint.y <= tableHeight + learnersAndGradesOffsetForTitle + titleWeekdaysHeight + learnersAndGradesYOffset + addLearnerButtonHeight) {
                    return new int[]{4, -1, -1, -1};
                } else // иначе не нажато ничего
                    return new int[]{0, -1, -1, -1};
            }

            if (downPoint.x > learnersShowedWidth) {
                // если нажатие в области оценок

                // шапка таблицы оценок
                int cellXOffset = learnersShowedWidth + gradesXOffset;
                for (int dayIterator = 0; dayIterator < learnersGrades[0].length; dayIterator++) {
                    for (int lessonIterator = 0; lessonIterator < learnersGrades[0][dayIterator].length; lessonIterator++) {
                        if (
                            // координаты касания относительно таблицы по Y
                                (downPoint.y > titleWeekdaysHeight) &&
                                        (downPoint.y <= learnersAndGradesOffsetForTitle + titleWeekdaysHeight) &&
                                        // координаты касания относительно таблицы по X
                                        (downPoint.x >= cellXOffset) &&
                                        (downPoint.x <= cellXOffset + learnersGrades[0][dayIterator][lessonIterator].cellWidth)
                        ) {

                            // нажата дата урока
                            chosenCellPoz[0] = -1;
                            chosenCellPoz[1] = dayIterator;
                            chosenCellPoz[2] = lessonIterator;
                            return new int[]{2, -1, dayIterator, learnersGrades[0][dayIterator][lessonIterator].lessonNumber};
                        }

                        cellXOffset += learnersGrades[0][dayIterator][lessonIterator].cellWidth;
                    }
                }
                // пробегаемся по содержимому таблицы оценок
                // cellYOffset -> переменная, через которую считается смещение рядов
                int cellYOffset = learnersAndGradesOffsetForTitle + titleWeekdaysHeight + learnersAndGradesYOffset;
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
                                return new int[]{3, learnerIterator, dayIterator, learnersGrades[0][dayIterator][lessonIterator].lessonNumber};
                            }

                            cellXOffset += learnersGrades[learnerIterator][dayIterator][lessonIterator].cellWidth;
                        }
                    }
                    // исходя из выоты имени ученика считаем смещение для следующей строки
                    cellYOffset += learnersDataAndSizes[learnerIterator].cellHeight;
                }

            } else {
                // нажатие в угол сверху
                if (downPoint.y <= learnersAndGradesOffsetForTitle + titleWeekdaysHeight &&
                        isSubjectAndDateInTable) {

                    float headSize = (learnersAndGradesOffsetForTitle + titleWeekdaysHeight);

                    if (downPoint.y <= headSize / 2) {// если нажат предмет
                        return new int[]{5, -1, -1, -1};
                    } else {// если нажата дата
                        if (downPoint.x <= headSize / 2) {// дата назад
                            return new int[]{6, -1, -1, -1};
                        } else if (downPoint.x >= learnersShowedWidth - headSize / 2)// дата вперед
                            return new int[]{7, -1, -1, -1};
                    }

                } else {// если нажатие в области учеников

                    // пробегаемся по таблице
                    int cellYOffset = learnersAndGradesOffsetForTitle + titleWeekdaysHeight + learnersAndGradesYOffset;
                    for (int learnerIterator = 0; learnerIterator < learnersDataAndSizes.length; learnerIterator++) {
                        if ((downPoint.y >= cellYOffset) && (downPoint.y <= cellYOffset + learnersDataAndSizes[learnerIterator].cellHeight)) {
                            // нажат ученик
                            chosenCellPoz[0] = learnerIterator; // номер ученика по списку
                            chosenCellPoz[1] = -1;              // номер дня
                            chosenCellPoz[2] = -1;              // номер урока
                            return new int[]{1, learnerIterator, -1, -1};
                        }
                        cellYOffset += learnersDataAndSizes[learnerIterator].cellHeight;
                    }
                }
            }
        } else {
            // если нажата кнопка создать ученика
            if (downPoint.y > learnersAndGradesOffsetForTitle && downPoint.y <= learnersAndGradesOffsetForTitle + addLearnerButtonHeight) {
                return new int[]{4, -2, -2, -2};
            }
        }
        // если не нашли совпадений в таблице то не нажато ничего
        return new int[]{0, -1, -1, -1};
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
                    && tableHeight > viewHeight - learnersAndGradesOffsetForTitle - titleWeekdaysHeight - addLearnerButtonHeight) {
                // не будет ли из-за отрицательного смещения ученик уходить вверх создавая пустую облать
                if (tableHeight + learnersAndGradesYOffset + dynamicLearnersAndGradesYOffset >= viewHeight - learnersAndGradesOffsetForTitle - titleWeekdaysHeight - addLearnerButtonHeight) {
                    // смещаем
                    this.learnersAndGradesYOffset += dynamicLearnersAndGradesYOffset;
                } else {
                    // иначе ставим минимально возможное значение
                    this.learnersAndGradesYOffset = viewHeight - tableHeight - learnersAndGradesOffsetForTitle - titleWeekdaysHeight - addLearnerButtonHeight;
                }
            } else {
                // иначе ставим максимально возможное значение
                this.learnersAndGradesYOffset = 0;
            }

            // ----- вызываем перерисовку -----
            invalidate();
        }
    }


    // метод обрабатывающий перемещение пальца по view
    void checkOffset() {
        if (learnersDataAndSizes.length != 0) {

            // ----- смещение по x -----
            // --- проверяем сдвиг учеников ---
            // по первому ученику смотрим, что ученик из-за смещения не уходит вправо создавая пустую облать
            if (learnersXOffset <= 0
                    // и что ширина учеников больше чем ширина доступной для отображения части view
                    && learnersDataAndSizes[0].cellWidth > learnersShowedWidth) {

                // будет ли из-за смещения ученик уходить влево создавая пустую облать
                if (learnersDataAndSizes[0].cellWidth + learnersXOffset < learnersShowedWidth) {
                    // ставим минимально возможное значение
                    this.learnersXOffset = -learnersDataAndSizes[0].cellWidth + learnersShowedWidth;
                }
            } else {
                // иначе ставим максимально возможное значение
                this.learnersXOffset = 0;
            }
            // --- проверяем сдвиг оценок ---
            if (learnersGrades[0].length != 0) {
                int gradesTableShowedWidth = viewWidth - learnersShowedWidth;
                // по первому столбцу оценок смотрим, не будут ли из-за положительного смещения оценки уходить вправо создавая пустую облать
                if (gradesXOffset <= 0 && gradesTableShowedWidth < gradesTableWidth) {
                    // будут ли из-за отрицательного смещения оценки уходить влево создавая пустую облать
                    if (gradesTableWidth + gradesXOffset < gradesTableShowedWidth) {
                        // ставим минимально возможное значение
                        this.gradesXOffset = gradesTableShowedWidth - gradesTableWidth;
                    }
                } else// иначе ставим максимально возможное значение
                    this.gradesXOffset = 0;

            }

            // ----- проверяем сдвиг по y -----
            // по первому ученику смотрим, не будет ли из-за положительного смещения ученик уходить вниз создавая пустую облать
            if (learnersAndGradesYOffset <= 0
                    // и проверяем не меньше ли высота учеников чем высота view
                    && tableHeight > viewHeight - learnersAndGradesOffsetForTitle - titleWeekdaysHeight - addLearnerButtonHeight) {
                // будет ли из-за отрицательного смещения ученик уходить вверх создавая пустую облать
                if (tableHeight + learnersAndGradesYOffset < viewHeight - learnersAndGradesOffsetForTitle - titleWeekdaysHeight - addLearnerButtonHeight)
                    // ставим минимально возможное значение
                    this.learnersAndGradesYOffset = viewHeight - tableHeight - learnersAndGradesOffsetForTitle - titleWeekdaysHeight - addLearnerButtonHeight;
            } else// иначе ставим максимально возможное значение
                this.learnersAndGradesYOffset = 0;
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


    LearnerAndHisGradesWithSize(String name, String surname, int cellWidth, int cellHeight, int bottomTextMargin) {
        this.name = name;
        this.surname = surname;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.bottomTextMargin = bottomTextMargin;
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
//
////  вродебы так можно выбрать жирность текста, но по моему, это более тяжелый способ
////        if (Build.VERSION.SDK_INT >= 28) {
////            Typeface typefaceA = ResourcesCompat.getFont(this, R.font.montserrat_family);
////            drawPaintFatDate.setTypeface(Typeface.create(typefaceA, 700, false));
////        } else {
////            drawPaintFatDate.setTypeface(ResourcesCompat.getFont(this, R.font.montserrat_extrabold));
////        }
//