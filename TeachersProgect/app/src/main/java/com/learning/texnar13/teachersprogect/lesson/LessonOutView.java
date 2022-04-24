package com.learning.texnar13.teachersprogect.lesson;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;

public class LessonOutView extends View {

    // ---------- константы ----------
    // размер одноместной парты
    private static final int NO_ZOOMED_DESK_SIZE = 40;
    private static final int NO_ZOOMED_LEARNER_SIZE = NO_ZOOMED_DESK_SIZE / 2;
    private static final int NO_ZOOMED_DESK_BORDER = 3;
    private static final int NO_ZOOMED_DESK_RADIUS = 5;

    // ----- Переменные назначенные при инициализации -----
    // цвета коэффициентЭкрана(screenDensity)
    // Набор кистей для отрисовки

    // кисть для фона парты
    private Paint deskFillPaint;
    // кисть для отрисовки имени(внизу клетки)
    private TextPaint textPaintName;
    // кисть для отрисовки большой оценки
    private TextPaint textPaintMainGrade;
    // кисть для отрисовки маленькой оценки
    private TextPaint textPaintSmallGrade;

    // цвета
    // фон
    private int cabinetColor;
    // простая парта
    private int simpleDeskColor;
    // цвета оценок
    private int[] gradesColors;
    // цвет обычного текста
    private int simpleTextColor;
    // цвет обычного текста если есть оценочный фон
    private int gradedTextColor;
    // цвет отсутствия
    private int absentColor;


    // плотность экрана нужна для расчета размеров парт
    private static float screenDensity;
    // максимальная оценка
    private int maxAnswersCount;

    // ----- Отрисовываемые данные -----
    private float scale;
    private PointF coordinateCenter;
    private DrawableDesk[] desks;


    // ----- Переменные назначенные при инициализации, меняющиеся в процессе работы view -----


    // ---------------------------------------------------------------------------------------------
    // ------ Инициализация
    // ---------------------------------------------------------------------------------------------


    // конструкторы
    public LessonOutView(Context context) {
        super(context);
        myInit(context);
    }

    public LessonOutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        myInit(context);
    }

    public LessonOutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        myInit(context);
    }

    private void myInit(Context context) {

        // сразу один раз загружается плотность экрана
        screenDensity = context.getResources().getDisplayMetrics().density;

        // цвета
        Resources r = context.getResources();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Resources.Theme theme = context.getTheme();

            // фон
            cabinetColor = r.getColor(R.color.base_background_color, theme);

            // простая парта
            simpleDeskColor = r.getColor(R.color.desks_color, theme);

            // цвета оценок
            gradesColors = new int[]{
                    r.getColor(R.color.grade1, theme),
                    r.getColor(R.color.grade2, theme),
                    r.getColor(R.color.grade3, theme),
                    r.getColor(R.color.grade4, theme),
                    r.getColor(R.color.grade5, theme)
            };
            // цвет обычного текста
            simpleTextColor = r.getColor(R.color.text_color_simple, theme);
            // цвет обычного текста если есть оценочный фон
            gradedTextColor = r.getColor(R.color.lesson_text_color, theme);
            // цвет отсутствия
            absentColor = r.getColor(R.color.absent_text_color, theme);
        } else {
            // фон
            cabinetColor = r.getColor(R.color.base_background_color);
            // простая парта
            simpleDeskColor = r.getColor(R.color.desks_color);

            // цвета оценок
            gradesColors = new int[]{
                    r.getColor(R.color.grade1),
                    r.getColor(R.color.grade2),
                    r.getColor(R.color.grade3),
                    r.getColor(R.color.grade4),
                    r.getColor(R.color.grade5)
            };
            // цвет обычного текста
            simpleTextColor = r.getColor(R.color.text_color_simple);
            // цвет обычного текста если есть оценочный фон
            gradedTextColor = r.getColor(R.color.lesson_text_color);
            // цвет отсутствия
            absentColor = r.getColor(R.color.absent_text_color);
        }

        // кисть фонов
        deskFillPaint = new Paint();
        deskFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // кисть текстов ученика
        textPaintName = new TextPaint();
        textPaintName.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_semibold));
        textPaintName.setAntiAlias(true);// сглаживание


        // кисть для отрисовки большой оценки
        textPaintMainGrade = new TextPaint();
        textPaintMainGrade.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_semibold));
        textPaintMainGrade.setAntiAlias(true);

        // кисть для отрисовки маленькой оценки
        textPaintSmallGrade = new TextPaint();
        textPaintSmallGrade.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_semibold));
        textPaintSmallGrade.setAntiAlias(true);

    }

    // ---------------------------------------------------------------------------------------------
    // ------ назначение размеров
    // ---------------------------------------------------------------------------------------------

    // здесь происходит определение размеров view, так же их можно задать жестко
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // считаем размеры этого view
        // this.viewWidth = right - left;
        // this.viewHeight = bottom - top;
    }

    // ---------------------------------------------------------------------------------------------
    // ------ Передача данных
    // ---------------------------------------------------------------------------------------------

    // Передача данных без отрисовки (отрисовка есть в setNewScaleParams)
    public void setData(
            int maxAnswersCount,
            LessonActivity.LessonLearnerAndHisGrades[] learnersAndTheirGrades,
            ArrayList<LessonActivity.DeskUnit> desksList) {

        // максимальное количество ответов
        this.maxAnswersCount = maxAnswersCount;

        // обработка массива парт
        desks = new DrawableDesk[desksList.size()];

        int deskI = 0;
        for (LessonActivity.DeskUnit importDesk : desksList) {

            // сначала создается массив учеников
            DrawableLearner[] learners = new DrawableLearner[importDesk.seatingLearnerNumber.length];
            for (int learnerI = 0; learnerI < learners.length; learnerI++) {

                // когда ученика на парте нет
                if (importDesk.seatingLearnerNumber[learnerI] == -1) {
                    learners[learnerI] = null;
                } else {
                    learners[learnerI] = new DrawableLearner(
                            importDesk.seatingLearnerNumber[learnerI],
                            learnersAndTheirGrades[importDesk.seatingLearnerNumber[learnerI]].firstName,
                            learnersAndTheirGrades[importDesk.seatingLearnerNumber[learnerI]].secondName,
                            learnersAndTheirGrades[importDesk.seatingLearnerNumber[learnerI]].getGradesArray(),
                            learnersAndTheirGrades[importDesk.seatingLearnerNumber[learnerI]].chosenGradePosition,// может быть -1 (все проверки есть)
                            (learnersAndTheirGrades[importDesk.seatingLearnerNumber[learnerI]].absTypePozNumber != -1)
                    );
                }
            }

            // создаем саму парту и помещаем в неё учеников
            desks[deskI] = new DrawableDesk(
                    new PointF(importDesk.startNoZoomedDeskX, importDesk.startNoZoomedDeskY),
                    learners
            );
            deskI++;
        }
    }


    // метод обновляющий разметку по изменениям размеров кабинета (Изменение размеров)
    public void setNewScaleParams(float newScale, PointF newCoordinateCenter) {

        // записываем переданные размеры
        scale = newScale;
        coordinateCenter = newCoordinateCenter;

        // считаем стандартные размеры парты
        DrawableDesk.deskSquareSize = pxScaledFromDp(NO_ZOOMED_DESK_SIZE);
        DrawableDesk.cornersRadius = pxScaledFromDp(NO_ZOOMED_DESK_RADIUS);

        // ----- вызываем перерисовку onDraw -----
        invalidate();
    }


    // метод обновления информации об одном ученике (без вывода графики)
    void updateLearner(int pressedLearnerListNumber, int[] grades, int chosenGradePosition, int chosenAbsPoz) {
        // поиск нужного ученика в партах
        for (DrawableDesk desk : desks) {
            for (int learnerI = 0; learnerI < desk.learners.length; learnerI++) {
                // если не пустой ученик с нужным id (номером в массиве) найден
                if (desk.learners[learnerI] != null)
                    if (desk.learners[learnerI].learnerArrayPoz == pressedLearnerListNumber) {
                        // меняем его параметры
                        desk.learners[learnerI].grades = grades;
                        desk.learners[learnerI].mainGradePos = chosenGradePosition;
                        desk.learners[learnerI].absent = chosenAbsPoz != -1;
                        return;
                    }
            }
        }
    }


    int getPressedLearnerNumber(float pressX, float pressY) {

        for (DrawableDesk desk : desks) {
            // если координата в парте по y
            if (pxScaledAndOffsetByYFromDp(desk.deskPosition.y) <= pressY &&
                    pressY <= pxScaledAndOffsetByYFromDp(desk.deskPosition.y) + pxScaledFromDp(NO_ZOOMED_DESK_SIZE)) {

                // нажатие на первую половину парты c не пустым учеником
                if (pxScaledAndOffsetByXFromDp(desk.deskPosition.x) <= pressX &&
                        pressX <= pxScaledAndOffsetByXFromDp(desk.deskPosition.x) + pxScaledFromDp(NO_ZOOMED_DESK_SIZE)) {
                    if (desk.learners[0] != null) {

                        // возвращаем количество учеников
                        return desk.learners[0].learnerArrayPoz;
                    }
                    // нажатие было на конкретную парту, но на ней нет ученика, дальше проверять смысла нет
                    return -1;
                } else if (desk.learners.length > 1)
                    if (pxScaledAndOffsetByXFromDp(desk.deskPosition.x) <= pressX &&
                            pressX <= pxScaledAndOffsetByXFromDp(desk.deskPosition.x) + pxScaledFromDp(NO_ZOOMED_DESK_SIZE * 2)) {
                        if (desk.learners[1] != null) {
                            // нажатие на вторую половину парты c не пустым учеником
                            return desk.learners[1].learnerArrayPoz;
                        }
                        // нажатие было на конкретную парту, но на ней нет ученика, дальше проверять смысла нет
                        return -1;
                    }
            }
        }
        // нажатие мимо
        return -1;
    }

    // ---------------------------------------------------------------------------------------------
    // ------ Отрисовка кадра
    // ---------------------------------------------------------------------------------------------

    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {

        // запрет вывода графики, пока данные не подготовлены
        if (desks != null && coordinateCenter != null) {

            // назначение элементам размеров, заданных по scale
            // (сделано это дабы не назначать одно и то же по сто раз)
            // получаем картинку ученика из вектора для отрисовки
            Bitmap learnerIcon = getBitmapFromVectorDrawable(R.drawable.lesson_activity_learner,
                    (int) pxFromDp(NO_ZOOMED_LEARNER_SIZE * scale),
                    (int) pxFromDp(NO_ZOOMED_LEARNER_SIZE * scale)
            );
            // получаем картинку отсутствующего ученика из вектора для отрисовки
            Bitmap absentIcon = getBitmapFromVectorDrawable(R.drawable.lesson_activity_learner_abs_ic,
                    (int) pxFromDp(NO_ZOOMED_LEARNER_SIZE * scale),
                    (int) pxFromDp(NO_ZOOMED_LEARNER_SIZE * scale)
            );
            //  назначение размера текста
            // текст имени
            textPaintName.setTextSize(getResources().getDimension(R.dimen.lesson_activity_learner_name_text_size) * 0.65f * scale);
            // текст главной оценки
            textPaintMainGrade.setTextSize(getResources().getDimension(R.dimen.lesson_activity_learner_main_grade_text_size) * 0.65f * scale);
            // текст побочной оценки
            textPaintSmallGrade.setTextSize(getResources().getDimension(R.dimen.lesson_activity_learner_small_grade_text_size) * 0.65f * scale);// todo перенести размеры шрифтов в поля класса, чтобы не искать их здесь каждый раз


            // очищаем фон
            canvas.drawColor(cabinetColor);
            // выыводим парты и учеников
            for (DrawableDesk desk : desks) {
                drawDeskAndItsLearners(desk, canvas, learnerIcon, absentIcon);
            }
        }
        super.onDraw(canvas);
    }

    // метод отрисовки парты
    private void drawDeskAndItsLearners(DrawableDesk desk, Canvas canvas, Bitmap learnerIcon, Bitmap absentIcon) {
        if (desk.learners.length == 0) {// одноместная парта
            throw new RuntimeException("desk.learners.length == 0");
        } else if (desk.learners.length == 1) {// одноместная парта

            drawLearnerOnDesk(desk.learners[0], desk.deskPosition, RECT_MODE_SQUARE, canvas, learnerIcon, absentIcon);
        } else {// двуместная парта
            drawLearnerOnDesk(desk.learners[0], desk.deskPosition, RECT_MODE_START, canvas, learnerIcon, absentIcon);
            drawLearnerOnDesk(desk.learners[1], desk.deskPosition, RECT_MODE_END, canvas, learnerIcon, absentIcon);
        }
    }


    //todo не хватает описания функции со всеми параметрами
    private void drawLearnerOnDesk(DrawableLearner learner, PointF deskPosition, int mode, Canvas canvas, Bitmap learnerIcon, Bitmap absentIcon) {

        if (learner == null) {
            // выбираем цвет фона
            deskFillPaint.setColor(simpleDeskColor);
            // Если ученика на месте нет, рисуем фон парты (место)
            drawRoundedRect(canvas, deskFillPaint,
                    pxFromDp(deskPosition.x * scale) + coordinateCenter.x,
                    pxFromDp(deskPosition.y * scale) + coordinateCenter.y,
                    DrawableDesk.cornersRadius, mode);
        } else {
            // если ученик на парте есть

            // выбираем цвет фона а также цвет текста оценок и имени
            if (learner.absent) {// пропуск
                deskFillPaint.setColor(simpleDeskColor);
                textPaintName.setColor(absentColor);
                textPaintMainGrade.setColor(absentColor);
                textPaintSmallGrade.setColor(absentColor);
            } else if (learner.mainGradePos == -1) {
                deskFillPaint.setColor(simpleDeskColor);
                textPaintName.setColor(simpleTextColor);
                textPaintMainGrade.setColor(simpleTextColor);
                textPaintSmallGrade.setColor(simpleTextColor);
            } else if (learner.grades[learner.mainGradePos] == 0) {
                deskFillPaint.setColor(simpleDeskColor);
                textPaintName.setColor(simpleTextColor);
                textPaintMainGrade.setColor(simpleTextColor);
                textPaintSmallGrade.setColor(simpleTextColor);
            } else {
                float currentGrade = (float) learner.grades[learner.mainGradePos] / maxAnswersCount;
                if (currentGrade <= 0.2F) {
                    deskFillPaint.setColor(gradesColors[0]);
                } else if (currentGrade <= 0.41F) {
                    deskFillPaint.setColor(gradesColors[1]);
                } else if (currentGrade <= 0.60F) {
                    deskFillPaint.setColor(gradesColors[2]);
                } else if (currentGrade <= 0.80F) {
                    deskFillPaint.setColor(gradesColors[3]);
                } else if (currentGrade <= 1F) {
                    deskFillPaint.setColor(gradesColors[4]);
                } else {// оценка вне диапазона
                    deskFillPaint.setColor(simpleDeskColor);
                }
                textPaintName.setColor(gradedTextColor);
                textPaintMainGrade.setColor(gradedTextColor);
                textPaintSmallGrade.setColor(gradedTextColor);
            }
            // рисуем фон парты (место)
            drawRoundedRect(canvas, deskFillPaint,
                    pxFromDp(deskPosition.x * scale) + coordinateCenter.x,
                    pxFromDp(deskPosition.y * scale) + coordinateCenter.y,
                    DrawableDesk.cornersRadius, mode);


            // выбор - рисовать картинку отсутствия, пустую картинку или оценки
            if (learner.absent) {
                // рисуем картинку отсутствующего ученика из Bitmap
                canvas.drawBitmap(
                        absentIcon,
                        pxFromDp((
                                deskPosition.x + NO_ZOOMED_DESK_SIZE / 2F - NO_ZOOMED_LEARNER_SIZE / 2f + ((mode == RECT_MODE_END) ? (NO_ZOOMED_DESK_SIZE) : (0))
                        ) * scale) + coordinateCenter.x,
                        pxFromDp((NO_ZOOMED_DESK_BORDER + deskPosition.y) * scale) + coordinateCenter.y,
                        deskFillPaint
                );
            } else if (learner.grades[0] == 0 && learner.grades[1] == 0 && learner.grades[2] == 0) {
                // рисуем картинку пустого ученика из Bitmap если оценок нет
                canvas.drawBitmap(
                        learnerIcon,
                        pxFromDp((
                                deskPosition.x + NO_ZOOMED_DESK_SIZE / 2F - NO_ZOOMED_LEARNER_SIZE / 2f + ((mode == RECT_MODE_END) ? (NO_ZOOMED_DESK_SIZE) : (0))
                        ) * scale) + coordinateCenter.x,
                        pxFromDp((NO_ZOOMED_DESK_BORDER + deskPosition.y) * scale) + coordinateCenter.y,
                        deskFillPaint
                );
            } else {
                // рисуем текст оценок
                drawLearnerGrades(learner, deskPosition, mode, canvas);
            }

            // рисуем текст имени
            drawLearnerTexts(learner, deskPosition, mode, canvas);
        }
    }


    private static final int RECT_MODE_START = 0;
    private static final int RECT_MODE_END = 1;
    private static final int RECT_MODE_SQUARE = 2;

    // метод позволяющий рисовать левую и правую половинки парт (от левого верхнего угла)
    private void drawRoundedRect(Canvas canvas, Paint paint, float startX, float startY, float radius, int mode) {
        // на всякий случай проверка на слишком большое скругление
        //if (radius > DrawableDesk.deskSquareSize / 2) radius = DrawableDesk.deskSquareSize / 2;

        // рисуем одну из фигур
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        switch (mode) {
            case 0:// левая половина парты
                path.moveTo(startX, startY + radius);
                path.rQuadTo(0, -radius, radius, -radius);
                path.lineTo(startX + DrawableDesk.deskSquareSize, startY);
                path.lineTo(startX + DrawableDesk.deskSquareSize, startY + DrawableDesk.deskSquareSize);
                path.lineTo(startX + radius, startY + DrawableDesk.deskSquareSize);
                path.rQuadTo(-radius, 0, -radius, -radius);
                break;
            case 1:// правая половина парты
                startX += DrawableDesk.deskSquareSize;
                path.moveTo(startX - 1, startY);
                path.lineTo(startX + DrawableDesk.deskSquareSize - radius, startY);
                path.rQuadTo(radius, 0, radius, radius);
                path.lineTo(startX + DrawableDesk.deskSquareSize, startY + DrawableDesk.deskSquareSize - radius);
                path.rQuadTo(0, radius, -radius, radius);
                path.lineTo(startX - 1, startY + DrawableDesk.deskSquareSize);
                break;
            case 2:// одноместная парта
                path.moveTo(startX, startY + radius);
                path.rQuadTo(0, -radius, radius, -radius);
                path.lineTo(startX + DrawableDesk.deskSquareSize - radius, startY);
                path.rQuadTo(radius, 0, radius, radius);
                path.lineTo(startX + DrawableDesk.deskSquareSize, startY + DrawableDesk.deskSquareSize - radius);
                path.rQuadTo(0, radius, -radius, radius);
                path.lineTo(startX + radius, startY + DrawableDesk.deskSquareSize);
                path.rQuadTo(-radius, 0, -radius, -radius);
                break;
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    // метод рисования текста имени ученика
    private Rect tempRect = new Rect();

    private void drawLearnerTexts(DrawableLearner learner, PointF deskPosition, int mode, Canvas canvas) {

        // вычисление смещения парты по X для левой и правой половинок парт
        float deskPosX = deskPosition.x + ((mode == RECT_MODE_END) ? (NO_ZOOMED_DESK_SIZE) : (0));

        // ограничиваем область рисования clip-областью
        canvas.save();
        canvas.clipRect(
                pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_BORDER),
                pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_BORDER + NO_ZOOMED_LEARNER_SIZE),
                pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_SIZE - NO_ZOOMED_DESK_BORDER),
                pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_SIZE - NO_ZOOMED_DESK_BORDER)
        );

        // вывод фамилии
        textPaintName.getTextBounds(learner.secondName, 0, learner.secondName.length(), tempRect);
        float learnerNameWidth = tempRect.left + tempRect.right;

        // проверка влезает ли текст в поле, если влезает, то его надо центрировать
        float textX = (learnerNameWidth < pxScaledFromDp(NO_ZOOMED_DESK_SIZE - 2 * NO_ZOOMED_DESK_BORDER)) ?
                (pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_SIZE / 2F) - learnerNameWidth / 2f) :
                (pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_BORDER));
        float textY = pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_BORDER + NO_ZOOMED_LEARNER_SIZE)
                - tempRect.top;

        canvas.drawText(learner.secondName, textX, textY, textPaintName);


        // вывод имени
        textPaintName.getTextBounds(learner.firstName, 0, learner.firstName.length(), tempRect);
        learnerNameWidth = tempRect.left + tempRect.right;

        // проверка влезает ли текст в поле, если влезает, то его надо центрировать
        textX = (learnerNameWidth < pxScaledFromDp(NO_ZOOMED_DESK_SIZE - 2 * NO_ZOOMED_DESK_BORDER)) ?
                (pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_SIZE / 2F) - learnerNameWidth / 2f) :
                (pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_BORDER));
        textY = pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_SIZE - NO_ZOOMED_DESK_BORDER)
                - tempRect.bottom;

        canvas.drawText(learner.firstName, textX, textY, textPaintName);

        // убираем clip-область
        canvas.restore();
    }


    // метод рисования текста оценок ученика
    private void drawLearnerGrades(DrawableLearner learner, PointF deskPosition, int mode, Canvas canvas) {

        // вычисление смещения парты по X для левой и правой половинок парт
        float deskPosX = deskPosition.x + ((mode == RECT_MODE_END) ? (NO_ZOOMED_DESK_SIZE) : (0));


        // ограничиваем область рисования clip-областью
        canvas.save();
        canvas.clipRect(
                pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_BORDER),
                pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_BORDER),
                pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_SIZE - NO_ZOOMED_DESK_BORDER),
                pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_SIZE - NO_ZOOMED_LEARNER_SIZE + 1)
        );


        // середина по которой выравниваются оценки
        float xGradesMid = pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_SIZE / 2F);
        float yGradesMid = pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_BORDER + NO_ZOOMED_LEARNER_SIZE / 2F);


        if (learner.grades[0] != 0 && learner.grades[1] == 0 && learner.grades[2] == 0) {
            // если нулевая оценка единственная, то она выводится по середине

            // выбираем размер текста для оценки
            TextPaint textPaint = (learner.mainGradePos == 0) ? textPaintMainGrade : textPaintSmallGrade;
            // расчет размеров оценки
            String grade = Integer.toString(learner.grades[0]);
            textPaint.getTextBounds(grade, 0, grade.length(), tempRect);
            float centerGradeWidth = tempRect.left + tempRect.right;
            // расчет по размерам положения
            float centerGradeTextX = xGradesMid - centerGradeWidth / 2f;
            float centerGradeTextY = yGradesMid + (tempRect.bottom - tempRect.top) / 2f;
            // отрисовка текста
            canvas.drawText(grade, centerGradeTextX, centerGradeTextY, textPaint);
        } else if (learner.grades[0] == 0 && learner.grades[1] == 0 && learner.grades[2] != 0) {
            // если вторая оценка единственная, то она выводится по середине

            // выбираем размер текста для оценки
            TextPaint textPaint = (learner.mainGradePos == 2) ? textPaintMainGrade : textPaintSmallGrade;
            // расчет размеров оценки
            String grade = Integer.toString(learner.grades[2]);
            textPaint.getTextBounds(grade, 0, grade.length(), tempRect);
            float centerGradeWidth = tempRect.left + tempRect.right;
            // расчет по размерам положения
            float centerGradeTextX = xGradesMid - centerGradeWidth / 2f;
            float centerGradeTextY = yGradesMid + (tempRect.bottom - tempRect.top) / 2f;
            // отрисовка текста
            canvas.drawText(grade, centerGradeTextX, centerGradeTextY, textPaint);
        } else {

            // вывод средней оценки
            // выбираем размер текста для оценки
            TextPaint textPaint = (learner.mainGradePos == 1) ? textPaintMainGrade : textPaintSmallGrade;
            // расчет размеров оценки
            String grade = Integer.toString(learner.grades[1]);
            textPaint.getTextBounds(grade, 0, grade.length(), tempRect);
            float centerGradeWidth = tempRect.left + tempRect.right;
            // расчет по размерам положения
            float centerGradeTextX = xGradesMid - centerGradeWidth / 2f;
            float centerGradeTextY = yGradesMid + (tempRect.bottom - tempRect.top) / 2f;
            // отрисовка текста
            if (learner.grades[1] != 0)
                canvas.drawText(grade, centerGradeTextX, centerGradeTextY, textPaint);


            // вывод левой оценки
            if (learner.grades[0] != 0) {
                // выбираем размер текста для оценки
                textPaint = (learner.mainGradePos == 0) ? textPaintMainGrade : textPaintSmallGrade;
                // расчет размеров оценки
                grade = Integer.toString(learner.grades[0]);
                textPaint.getTextBounds(grade, 0, grade.length(), tempRect);
                // расчет по размерам положения
                float textX = centerGradeTextX - (tempRect.left + tempRect.right);
                float textY = yGradesMid + (tempRect.bottom - tempRect.top) / 2f;
                // отрисовка текста
                canvas.drawText(grade, textX, textY, textPaint);
            }

            // вывод правой оценки
            if (learner.grades[2] != 0) {
                // выбираем размер текста для оценки
                textPaint = (learner.mainGradePos == 2) ? textPaintMainGrade : textPaintSmallGrade;
                // расчет размеров оценки
                grade = Integer.toString(learner.grades[2]);
                textPaint.getTextBounds(grade, 0, grade.length(), tempRect);
                // расчет по размерам положения
                float textX = centerGradeTextX + centerGradeWidth;
                float textY = yGradesMid + (tempRect.bottom - tempRect.top) / 2f;
                // отрисовка текста
                canvas.drawText(grade, textX, textY, textPaint);
            }
        }

        // убираем clip-область
        canvas.restore();
    }


    // метод получения из векторных изображений растра нужного размера
    private Bitmap getBitmapFromVectorDrawable(int drawableId, int bitmapWidth, int bitmapHeight) {
        // создаем drawable из векторной картинки
        Drawable drawable = ContextCompat.getDrawable(this.getContext(), drawableId);
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

    private static float pxFromDp(float dp) {
        return dp * screenDensity;
    }

    private float pxScaledFromDp(float dp) {
        return dp * screenDensity * scale;
    }

    private float pxScaledAndOffsetByXFromDp(float dp) {
        return dp * screenDensity * scale + coordinateCenter.x;
    }

    private float pxScaledAndOffsetByYFromDp(float dp) {
        return dp * screenDensity * scale + coordinateCenter.y;
    }

    // ---------------------------------------------------------------------------------------------
    // ------ Классы данных
    // ---------------------------------------------------------------------------------------------


    private static class DrawableDesk {

        // -- Переменные посчитанные для всех парт назначенные при инициализации, меняющиеся в процессе зума
        // ширина одного квадрата парты
        static float deskSquareSize;
        // радиус скругления углов
        static float cornersRadius;


        // -- Локальные переменные парты --
        // позиция парты
        PointF deskPosition;
        // ученики сидящие за партой (null - место пустое)
        DrawableLearner[] learners;

        public DrawableDesk(PointF deskPosition, DrawableLearner[] learners) {
            this.deskPosition = deskPosition;
            this.learners = learners;
        }
    }

    private static class DrawableLearner {
        // id для ередачи нажатия
        int learnerArrayPoz;
        // имя
        String firstName;
        String secondName;

        // оценки
        int[] grades;
        // номер главной оценки
        int mainGradePos;
        // присутствие отсутствия
        boolean absent;

        public DrawableLearner(int learnerArrayPoz, String firstName, String secondName, int[] grades, int mainGradePos, boolean absent) {
            this.learnerArrayPoz = learnerArrayPoz;
            this.firstName = firstName;
            this.secondName = secondName;
            this.grades = grades;
            this.mainGradePos = mainGradePos;
            this.absent = absent;
        }
    }

}
