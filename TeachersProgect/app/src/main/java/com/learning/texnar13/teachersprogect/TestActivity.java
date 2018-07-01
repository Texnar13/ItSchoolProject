package com.learning.texnar13.teachersprogect;

import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class TestActivity extends AppCompatActivity implements View.OnTouchListener {

    RelativeLayout flat;
    LinearLayout myRectangle;
    RelativeLayout.LayoutParams rectParams;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        flat = (RelativeLayout) findViewById(R.id.test_activity_layout_flat);
        flat.setOnTouchListener(this);
        myRectangle = (LinearLayout) findViewById(R.id.test_activity_layout_flat_rect);
        rectParams = new RelativeLayout.LayoutParams(myRectangle.getWidth(), myRectangle.getHeight());
    }


    static final int NONE = 0;
    static final int ZOOM = 2;
    int mode = NONE;
    //середина касания пальцев
    PointF startMid = new PointF();
    //текущая позиия
    PointF nowMid = new PointF();
    //изначальное растояние между пальцам
    float oldDist = 1f;
    //начальные параметры обьекта
    int widthOld = 1;
    int heightOld = 1;
    int xOld = 1;
    int yOld = 1;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                //если поставлен второй палец,назначаем новые координаты
                if (event.getPointerCount() == 2) {
                    //начальные размеры обьекта
                    widthOld = myRectangle.getWidth();
                    heightOld = myRectangle.getHeight();
                    //начальные координаты обьекта
                    xOld = (int) myRectangle.getX();
                    yOld = (int) myRectangle.getY();
                    //находим изначальное растояние между пальцами
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        findMidPoint(startMid, event);
                        findMidPoint(nowMid, event);
                        mode = ZOOM;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    //новое расстояние между пальцами
                    float newDist = spacing(event);
                    //находим коэффициент разницы между изначальным и новым расстоянием
                    float scale = newDist / oldDist;

                    if (newDist > 10f) {//слишком маленькое расстояние между пальцами
                        if (scale > 0.01f &&//слишком маленький коэффициент
                                (widthOld * scale > 10f && heightOld * scale > 10f) &&//слишком маленький размер
                                (widthOld * scale < 1500f && heightOld * scale < 1500f)//слишком большой размер
                                ) {
                            //-----трансформация размера-----
                            rectParams.width = (int) (widthOld * scale);
                            rectParams.height = (int) (heightOld * scale);
                            myRectangle.setLayoutParams(rectParams);

                            //-----трансформация координаты-----
                            //текущая середина пальцев
                            findMidPoint(nowMid, event);
                            //-перемещение обьекта-
                            // относительно центра зуммирования и перемещение пальцев в процессе зума
                            //ставим обьекту координаты
                            myRectangle.setX(((xOld - startMid.x) * scale) + nowMid.x);
                            myRectangle.setY(((yOld - startMid.y) * scale) + nowMid.y);
                        } else {
                            //если не можем использовать изменение размера,
                            // тогда просто перемещаем
                            //берем прошлую середину
                            float lastX = nowMid.x;
                            float lastY = nowMid.y;
                            // и текущую
                            findMidPoint(nowMid, event);
                            //и сравниваем их
                            myRectangle.setX(myRectangle.getX() + nowMid.x - lastX);
                            myRectangle.setY(myRectangle.getY() + nowMid.y - lastY);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                //больше двух - ничего не меняется
                //если пальцев осталось два переназначаем начальные координаты
//                if (event.getPointerCount() - 1 == 2) {
//                    //начальные размеры обьекта
//                    widthOld = myRectangle.getWidth();
//                    heightOld = myRectangle.getHeight();
//                    //начальные координаты обьекта
//                    xOld = (int) myRectangle.getX();
//                    yOld = (int) myRectangle.getY();
//                    //находим изначальное растояние между пальцами
//                    oldDist = spacing(event);
//                    if (oldDist > 10f) {
//                        findMidPoint(startMid, event);
//                        mode = ZOOM;
//                    }
//                }
                //один палец - ничего
                if (event.getPointerCount() - 1 < 2) {
                    mode = NONE;
                }
                break;
        }
        return true;
    }

    //******************* Расстояние между первым и вторым пальцами из event
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    //************* координата середины между первым и вторым пальцами из event
    private void findMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}

//package com.learning.texnar13.teachersprogect;
//
//import android.graphics.PointF;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//
//public class TestActivity extends AppCompatActivity implements View.OnTouchListener {
//
//    RelativeLayout flat;
//    LinearLayout myRectangle;
//    RelativeLayout.LayoutParams rectParams;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_test);
//
//        flat = (RelativeLayout) findViewById(R.id.test_activity_layout_flat);
//        flat.setOnTouchListener(this);
//        myRectangle = (LinearLayout) findViewById(R.id.test_activity_layout_flat_rect);
//        rectParams = new RelativeLayout.LayoutParams(myRectangle.getWidth(), myRectangle.getHeight());
//    }
//
//
//    final String TAG = "TeachersApp";
//    // We can be in one of these 3 states
//    static final int NONE = 0;
//    static final int DRAG = 1;
//    static final int ZOOM = 2;
//    static final int DRAW = 3;
//    int mode = NONE;
//
//    // Remember some things for zooming
//    PointF start = new PointF();
//    PointF startMid = new PointF();
//    PointF nextEndMid = new PointF();
//    float oldDist = 1f;
//
//    int widthOld = 1;
//    int heightOld = 1;
//
//    int xOld = 1;
//    int yOld = 1;
//
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//
//        switch (event.getAction() & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN:
//                start.set(event.getX(), event.getY());//задаем точку начала касания
//                mode = DRAG;
//                Log.d(TAG, "mode=DRAG");
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                oldDist = spacing(event);//находим изначальное растояние между пальцами
//
//                widthOld = myRectangle.getWidth();
//                heightOld = myRectangle.getHeight();
//                xOld = (int) myRectangle.getX();
//                yOld = (int) myRectangle.getY();
//
//                Log.d(TAG, "oldDist=" + oldDist);
//                if (oldDist > 10f) {
//                    findMidPoint(startMid, event);
//                    mode = ZOOM;
//                    Log.d(TAG, "mode=ZOOM");
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
////                if (mode == DRAW) {
////                    onTouchEvent(event);//срабатывает просто перемещение пальца
////                }
//                if (mode == DRAG) {
//                    //code for draging..
//
//                } else if (mode == ZOOM) {
//                    //новое расстояние между пальцами
//                    float newDist = spacing(event);
//                    if (newDist > 10f) {//находим коэффициент разницы между изначальным и новым расстоянием
//                        float scale = newDist / oldDist;
//                        if (scale > 0.01f) {
//                            if ((scale < 1 && widthOld * scale > 10f && heightOld * scale > 10f)
//                                    || (scale > 1// && widthOld * scale < 750f && heightOld * scale < 750f
//                            )
//                                    ) {
//                                //трансформация размера
//                                rectParams.width = (int) (widthOld * scale);
//                                rectParams.height = (int) (heightOld * scale);
//
//                                //трансформация координаты относительно центра
//                                myRectangle.setX(startMid.x + ((xOld - startMid.x) * scale));
//                                myRectangle.setY(startMid.y + ((yOld - startMid.y) * scale));
//
//                            }
//                        } else {
//                            //перемещение двух пальцев
//                        }
//                    }
//                    //перемещение пальцев в процессе зума
//                    findMidPoint(nextEndMid, event);
//                    myRectangle.setX(myRectangle.getX() + (nextEndMid.x - startMid.x));
//                    myRectangle.setY(myRectangle.getY() + (nextEndMid.y - startMid.y));
//                    myRectangle.setLayoutParams(rectParams);
//
//                    //findMidPoint(startMid, event);
//                }
//
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_POINTER_UP:
//                mode = NONE;
//                Log.d(TAG, "mode=NONE");
//                break;
//        }
//        return true; // indicate event was handled
//    }
//
//    //******************* Расстояние между первым и вторым пальцами из event
//    private float spacing(MotionEvent event) {
//        float x = event.getX(0) - event.getX(1);
//        float y = event.getY(0) - event.getY(1);
//        return (float) Math.sqrt(x * x + y * y);
//    }
//
//    //************* координата середины между первым и вторым пальцами из event
//    private void findMidPoint(PointF point, MotionEvent event) {
//        float x = event.getX(0) + event.getX(1);
//        float y = event.getY(0) + event.getY(1);
//        point.set(x / 2, y / 2);
//    }
//
//}
