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


    final String TAG = "TeachersApp";
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int DRAW = 3;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    int widthOld = 1;
    int heightOld = 1;

    int xOld = 1;
    int yOld = 1;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                start.set(event.getX(), event.getY());//задаем точку начала касания
                mode = DRAG;
                Log.d(TAG, "mode=DRAG");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);//находим изначальное растояние между пальцами

                widthOld = myRectangle.getWidth();
                heightOld = myRectangle.getHeight();
                xOld = (int) myRectangle.getX();
                yOld = (int) myRectangle.getY();

                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    findMidPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:
//                if (mode == DRAW) {
//                    onTouchEvent(event);//срабатывает просто перемещение пальца
//                }
                if (mode == DRAG) {
                    ///code for draging..
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);//новое расстояние между пальцами
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 10f) {
                        float scale = newDist / oldDist;//находим коэффициент разницы между изначальным и новым расстоянием

                        if (scale > 0.1f) {
                            if ((scale < 1 && widthOld * scale > 10f && heightOld * scale > 10f) ||
                                    (scale > 1 && widthOld * scale < 750f && heightOld * scale < 750f)) {
                                rectParams.width = (int) (widthOld * scale);
                                rectParams.height = (int) (heightOld * scale);

                                myRectangle.setX(xOld * scale);
                                myRectangle.setY(yOld * scale);

                                myRectangle.setLayoutParams(rectParams);
                            }
                        } else {
                            //перемещение двух пальцев
                        }
                    }
                }
                break;
        }
        return true; // indicate event was handled
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
