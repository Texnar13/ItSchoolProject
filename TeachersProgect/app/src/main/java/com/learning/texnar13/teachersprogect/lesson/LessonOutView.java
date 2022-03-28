package com.learning.texnar13.teachersprogect.lesson;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class LessonOutView extends View {

    // ---------- константы ----------
    // размер одноместной парты
    static final int NO_ZOOMED_DESK_SIZE = 40;

    // ----- Переменные назначенные при инициализации -----
    // цвета коэффициентЭкрана(screenDensity)
    // Набор кистей для отрисовки
    Paint noGradeDeskFillPaint;
    // плотность экрана нужна для расчета размеров парт
    static float screenDensity;

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
        noGradeDeskFillPaint = new Paint();
        noGradeDeskFillPaint.setColor(Color.parseColor("#00ffff"));
        noGradeDeskFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        // Это временно
        setData();
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

    public void setData() {

        // считаем начальный размер стандартного квадрата парты
        DrawableDesk.deskSquareSize = 200;
        DrawableDesk.cornersRadius = 40;//todo записывается она сюда уже в пикселях, посчитанная с density


        desks = new DrawableDesk[4];
        desks[0] = new DrawableDesk(new PointF(100, 100), new DrawableLearner[2]);
        desks[1] = new DrawableDesk(new PointF(400, 200), new DrawableLearner[2]);
        desks[2] = new DrawableDesk(new PointF(10, 400), new DrawableLearner[1]);
        desks[3] = new DrawableDesk(new PointF(500, 300), new DrawableLearner[2]);
    }


    // метод обновляющий разметку по изменениям размеров кабинета
    public void setNewScaleParams(float newScale, /*PointF oldCoordinateCenter,*/ PointF newCoordinateCenter) {

        if (desks.length != 0) {

            // записываем переданные размеры
            scale = newScale;
            coordinateCenter = newCoordinateCenter;

            // считаем стандартные размеры парты
            DrawableDesk.deskSquareSize = pxFromDp(NO_ZOOMED_DESK_SIZE * scale);
            DrawableDesk.cornersRadius = pxFromDp(7) * scale;

            // ----- вызываем перерисовку onDraw -----
            invalidate();
        }
    }
    // ---------------------------------------------------------------------------------------------
    // ------ Отрисовка кадра
    // ---------------------------------------------------------------------------------------------

    // отрисовка вызываемая через invalidate();
    @Override
    protected void onDraw(Canvas canvas) {

        // запрет вывода графики, пока данные не подготовлены
        if (desks != null) {//canDraw &&

            // очищаем фон
            canvas.drawColor(Color.MAGENTA);

            // выыводим парты и учеников
            for (DrawableDesk desk : desks) {
                drawDeskAndItsLearners(desk, canvas);
            }
        }

        super.onDraw(canvas);
    }

    // метод отрисовки парты
    void drawDeskAndItsLearners(DrawableDesk desk, Canvas canvas) {
        if (desk.learners.length == 0) {// одноместная парта
            throw new RuntimeException("desk.learners.length == 0");
        } else if (desk.learners.length == 1) {// одноместная парта
            drawLearnerOnDesk(desk.learners[0], desk.deskPosition, RECT_MODE_SQUARE, canvas);
        } else {// двуместная парта
            drawLearnerOnDesk(desk.learners[0], desk.deskPosition, RECT_MODE_START, canvas);
            drawLearnerOnDesk(desk.learners[1], desk.deskPosition, RECT_MODE_END, canvas);
        }
    }

    void drawLearnerOnDesk(DrawableLearner learner, PointF deskPosition, int mode, Canvas canvas) {

        if (learner == null) {
            // Если ученика на месте нет, рисуем фон парты (место)
            drawRoundedRect(canvas, noGradeDeskFillPaint, deskPosition.x, deskPosition.y, DrawableDesk.cornersRadius, mode);
        } else {

            // если ученик на парте есть
            drawRoundedRect(canvas, noGradeDeskFillPaint, desks[0].deskPosition.x, desks[0].deskPosition.y, DrawableDesk.cornersRadius, mode);

            // todo остановился здесь

        }
    }


    private static final int RECT_MODE_START = 0;
    private static final int RECT_MODE_END = 1;
    private static final int RECT_MODE_SQUARE = 2;

    // метод позволяющий рисовать левую и правую половинки парт (от левого верхнего угла)
    void drawRoundedRect(Canvas canvas, Paint paint, float startX, float startY, float radius,
                         int mode) {
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
                path.moveTo(startX, startY);
                path.lineTo(startX + DrawableDesk.deskSquareSize - radius, startY);
                path.rQuadTo(radius, 0, radius, radius);
                path.lineTo(startX + DrawableDesk.deskSquareSize, startY + DrawableDesk.deskSquareSize - radius);
                path.rQuadTo(0, radius, -radius, radius);
                path.lineTo(startX, startY + DrawableDesk.deskSquareSize);
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

    // метод получения из векторных изображений растра нужного размера
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

    private static int pxFromDp(float dp) {
        return (int) (dp * screenDensity);
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

    }

}
