package com.learning.texnar13.teachersprogect.lesson;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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

public class LessonOutView extends View {

    // ---------- константы ----------
    // размер одноместной парты
    static final int NO_ZOOMED_DESK_SIZE = 40;
    static final int NO_ZOOMED_LEARNER_SIZE = NO_ZOOMED_DESK_SIZE / 2;
    static final int NO_ZOOMED_DESK_BORDER = 3;

    // ----- Переменные назначенные при инициализации -----
    // цвета коэффициентЭкрана(screenDensity)
    // Набор кистей для отрисовки

    // кисть для фона парты
    Paint deskFillPaint;
    // кисть для отрисовки имени(внизу клетки)
    private TextPaint drawPaintName;

    // цвета
    // фон
    int cabinetColor;
    // простая парта
    int simpleDeskColor;
    // цвета оценок
    int[] gradesColors;
    // цвет обычного текста
    int simpleTextColor;
    // цвет отсутствия
    int absentColor;


    // плотность экрана нужна для расчета размеров парт
    static float screenDensity;
    // максимальная оценка
    int maxAnswersCount;

    // ----- Отрисовываемые данные -----
    float scale;
    PointF coordinateCenter;
    DrawableDesk[] desks;


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
        screenDensity = context.getResources().getDisplayMetrics().density;


        // цвета
        Resources r = context.getResources();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Resources.Theme theme = context.getTheme();

            // фон
            cabinetColor = r.getColor(R.color.backgroundDarkGray, theme);

            // простая парта
            simpleDeskColor = r.getColor(R.color.backgroundLiteGray, theme);

            // цвета оценок
            gradesColors = new int[]{
                    r.getColor(R.color.grade1, theme),
                    r.getColor(R.color.grade2, theme),
                    r.getColor(R.color.grade3, theme),
                    r.getColor(R.color.grade4, theme),
                    r.getColor(R.color.grade5, theme)
            };
            // цвет обычного текста
            simpleTextColor = r.getColor(R.color.simple_text_color, theme);
            // цвет отсутствия
            absentColor = r.getColor(R.color.absent_text_color, theme);
        } else {
            // фон
            cabinetColor = r.getColor(R.color.backgroundDarkGray);
            // простая парта
            simpleDeskColor = r.getColor(R.color.backgroundLiteGray);

            // цвета оценок
            gradesColors = new int[]{
                    r.getColor(R.color.grade1),
                    r.getColor(R.color.grade2),
                    r.getColor(R.color.grade3),
                    r.getColor(R.color.grade4),
                    r.getColor(R.color.grade5)
            };
            // цвет обычного текста
            simpleTextColor = r.getColor(R.color.simple_text_color);
            // цвет отсутствия
            absentColor = r.getColor(R.color.absent_text_color);
        }


        deskFillPaint = new Paint();
        deskFillPaint.setColor(Color.parseColor("#00ffff"));
        deskFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        drawPaintName = new TextPaint();
        drawPaintName.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_semibold));
        drawPaintName.setColor(Color.BLACK);
        drawPaintName.setAntiAlias(true);// сглаживание

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

    // (Изменение размеров)
    public void setData(int maxAnswersCount) {

        // считаем начальный размер стандартного квадрата парты
        DrawableDesk.deskSquareSize = 200;
        DrawableDesk.cornersRadius = 40;//todo записывается она сюда уже в пикселях, посчитанная с density

        // максимальное количество ответов
        this.maxAnswersCount = maxAnswersCount;

        desks = new DrawableDesk[4];
        desks[0] = new DrawableDesk(new PointF(10, 10), new DrawableLearner[2]);
        desks[1] = new DrawableDesk(new PointF(40, 20), new DrawableLearner[2]);
        desks[2] = new DrawableDesk(new PointF(1, 40), new DrawableLearner[1]);

        DrawableLearner[] learners = new DrawableLearner[2];
        learners[0] = new DrawableLearner(
                -1,
                "Ivan",
                "Ivanov",
                new int[]{3, 4, 5},
                1,
                false
        );


        learners[1] = new DrawableLearner(
                -1,
                "Iva",
                "IvanyБov",
                new int[]{3, 4, 5},
                2,
                true
        );

        desks[3] = new DrawableDesk(new PointF(50, 30), learners);

        invalidate();
    }

    // метод обновления информации об одном ученике
    void updateLearner() {// todo

    }


    // метод обновляющий разметку по изменениям размеров кабинета (Изменение размеров)
    public void setNewScaleParams(float newScale, PointF newCoordinateCenter) {

        // записываем переданные размеры
        scale = newScale;
        coordinateCenter = newCoordinateCenter;

        // считаем стандартные размеры парты
        DrawableDesk.deskSquareSize = pxFromDp(NO_ZOOMED_DESK_SIZE * scale);
        DrawableDesk.cornersRadius = pxFromDp(7) * scale;

        // ----- вызываем перерисовку onDraw -----
        invalidate();
    }
    // ---------------------------------------------------------------------------------------------
    // ------ Отрисовка кадра
    // ---------------------------------------------------------------------------------------------

    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {

        // запрет вывода графики, пока данные не подготовлены
        if (desks != null && coordinateCenter != null) {//canDraw &&

            // очищаем фон
            canvas.drawColor(cabinetColor);

            // выыводим парты и учеников
            for (DrawableDesk desk : desks) {
                drawDeskAndItsLearners(desk, canvas);
            }
        }

        super.onDraw(canvas);
    }

    // метод отрисовки парты
    private void drawDeskAndItsLearners(DrawableDesk desk, Canvas canvas) {
        if (desk.learners.length == 0) {// одноместная парта
            throw new RuntimeException("desk.learners.length == 0");
        } else if (desk.learners.length == 1) {// одноместная парта

            drawLearnerOnDesk(desk.learners[0], desk.deskPosition, RECT_MODE_SQUARE, canvas);
        } else {// двуместная парта
            drawLearnerOnDesk(desk.learners[0], desk.deskPosition, RECT_MODE_START, canvas);
            drawLearnerOnDesk(desk.learners[1], desk.deskPosition, RECT_MODE_END, canvas);
        }
    }


    //todo не хватает описания функции со всеми параметрами
    private void drawLearnerOnDesk(DrawableLearner learner, PointF deskPosition, int mode, Canvas canvas) {

        if (learner == null) {
            deskFillPaint.setColor(simpleDeskColor);
            // Если ученика на месте нет, рисуем фон парты (место)
            drawRoundedRect(canvas, deskFillPaint,
                    pxFromDp(deskPosition.x * scale) + coordinateCenter.x,
                    pxFromDp(deskPosition.y * scale) + coordinateCenter.y,
                    DrawableDesk.cornersRadius, mode);
        } else {

            // если ученик на парте есть
            // выбираем цвет фона
            if (learner.absent) {// пропуск
                deskFillPaint.setColor(simpleDeskColor);
            } else if (learner.grades[learner.mainGradePos] == 0) {
                deskFillPaint.setColor(simpleDeskColor);
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
            }

            drawRoundedRect(canvas, deskFillPaint,
                    pxFromDp(deskPosition.x * scale) + coordinateCenter.x,
                    pxFromDp(deskPosition.y * scale) + coordinateCenter.y,
                    DrawableDesk.cornersRadius, mode);


            // получаем иконку ученика из вектора todo перенести конкретно получение иконки выше, так чтобы оно вызывалось только один раз
            Bitmap learnerIcon = getBitmapFromVectorDrawable(
                    R.drawable.lesson_activity_learner_ic,
                    pxFromDp(NO_ZOOMED_LEARNER_SIZE * scale),
                    pxFromDp(NO_ZOOMED_LEARNER_SIZE * scale)
            );
            canvas.drawBitmap(
                    learnerIcon,
                    pxFromDp((
                            deskPosition.x + NO_ZOOMED_DESK_SIZE / 2F - NO_ZOOMED_LEARNER_SIZE / 2f + ((mode == RECT_MODE_END) ? (NO_ZOOMED_DESK_SIZE) : (0))
                    ) * scale) + coordinateCenter.x,
                    pxFromDp((NO_ZOOMED_DESK_BORDER + deskPosition.y) * scale) + coordinateCenter.y,
                    deskFillPaint
            );

            // рисуем текст

            // todo перенести конкретно назначение размера выше, так чтобы оно вызывалось только один раз
            drawPaintName.setTextSize(getResources().getDimension(R.dimen.lesson_activity_learner_name_text_size) * 0.65f * scale);

            drawLearnerTexts(learner, deskPosition, mode, canvas);

        }
// todo остановился здесь

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
    Rect tempRect = new Rect();

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
        drawPaintName.getTextBounds(learner.secondName, 0, learner.secondName.length(), tempRect);
        float learnerNameWidth = tempRect.left + tempRect.right;

        // проверка влезает ли текст в поле, если влезает, то его надо центрировать
        float textX = (learnerNameWidth < pxScaleFromDp(NO_ZOOMED_DESK_SIZE - 2 * NO_ZOOMED_DESK_BORDER)) ?
                (pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_SIZE / 2F) - learnerNameWidth / 2f) :
                (pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_BORDER));
        float textY = pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_BORDER + NO_ZOOMED_LEARNER_SIZE)
                - tempRect.top;

        canvas.drawText(learner.secondName, textX, textY, drawPaintName);


        // вывод имени
        drawPaintName.getTextBounds(learner.firstName, 0, learner.firstName.length(), tempRect);
        learnerNameWidth = tempRect.left + tempRect.right;

        // проверка влезает ли текст в поле, если влезает, то его надо центрировать
        textX = (learnerNameWidth < pxScaleFromDp(NO_ZOOMED_DESK_SIZE - 2 * NO_ZOOMED_DESK_BORDER)) ?
                (pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_SIZE / 2F) - learnerNameWidth / 2f) :
                (pxScaledAndOffsetByXFromDp(deskPosX + NO_ZOOMED_DESK_BORDER));
        textY = pxScaledAndOffsetByYFromDp(deskPosition.y + NO_ZOOMED_DESK_SIZE - NO_ZOOMED_DESK_BORDER)
                - tempRect.bottom;

        canvas.drawText(learner.firstName, textX, textY, drawPaintName);


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

    private static int pxFromDp(float dp) {
        return (int) (dp * screenDensity);
    }// todo пусть возвращает float

    private int pxScaleFromDp(float dp) {
        return (int) (dp * screenDensity * scale);
    }// todo пусть возвращает float

    private int pxScaledAndOffsetByXFromDp(float dp) {
        return (int) (dp * screenDensity * scale + coordinateCenter.x);// todo пусть возвращает float
    }

    private int pxScaledAndOffsetByYFromDp(float dp) {
        return (int) (dp * screenDensity * scale + coordinateCenter.y);// todo пусть возвращает float
    }

    // ---------------------------------------------------------------------------------------------
    // ------ Классы данных
    // ---------------------------------------------------------------------------------------------


    private static class DrawableDesk {

        // -- Переменные посчитанные для всех парт назначенные при инициализации, меняющиеся в процессе зума --todo записывается она сюда уже в пикселях, посчитанная с density
        // ширина одного квадрата парты
        static float deskSquareSize;
        // радиус скругления углов
        static float cornersRadius;


        // -- Локальные переменные парты --
        // позиция парты todo записывается она сюда уже в пикселях, посчитанная с density
        PointF deskPosition;
        // ученики сидящие за партой (null - место пустое)
        DrawableLearner[] learners;

        public DrawableDesk(PointF deskPosition, DrawableLearner[] learners) {
            this.deskPosition = deskPosition;
            this.learners = learners;
        }
    }

    private static class DrawableLearner {
        // id длпередачи нажатия
        long learnerId;
        // имя
        String firstName;
        String secondName;

        // оценки
        int[] grades;
        // номер главной оценки
        int mainGradePos;
        // присутствие отсутствия
        boolean absent;

        public DrawableLearner(long learnerId, String firstName, String secondName, int[] grades, int mainGradePos, boolean absent) {
            this.learnerId = learnerId;
            this.firstName = firstName;
            this.secondName = secondName;
            this.grades = grades;
            this.mainGradePos = mainGradePos;
            this.absent = absent;
        }
    }

}
