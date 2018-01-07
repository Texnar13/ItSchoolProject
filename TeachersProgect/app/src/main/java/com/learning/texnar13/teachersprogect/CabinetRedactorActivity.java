package com.learning.texnar13.teachersprogect;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class CabinetRedactorActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    public static final String EDITED_OBJECT_ID = "id";//ID редактируемого обьекта
    ArrayList<CabinetRedactorPoint> deskCoordinatesList = new ArrayList<>();
    float multiplier = 0;//множитель
    long checkedDeskId;
    ImageView instrumentalImage;
    RelativeLayout out;
    long cabinetId;
    TextView stateText;
//    //
//    RelativeLayout relativeLayout;
//    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_redactor);
        out = (RelativeLayout) findViewById(R.id.redactor_out);

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        //коэффициент размера интерфейса
        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;

        Log.i("TeachersApp", "" + multiplier);
        //кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO 2 это редактор кабинетов, пока понадобятся только двухместные парты,
//        /* выводим все парты находящиеся в этом кабинете
//        * при нажатии на картинку "плюс" в центре экрана появляется парта,
//        * которую перетаскиванием можно установить на нужное место
//        * парту можно удалить перетащив на картинку "корзина" или долгим нажатием, незнаю как будет удобнее реализовать
//        *    _________________________________
//        *   |                             сохр|
//        *   |_________________________________|
//        *   |   _                             |
//        *   |  |К|                       +    |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |             новая               |
//        *   |             _______             |
//        *   |            |   |   |            |
//        *   |            |___|___|            |
//        *   |                                 |
//        *   |                                 |
//        *   |    _______                      |
//        *   |   |   |   |                     |
//        *   |   |___|___|                     |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |_________________________________|
//        *
//        * в меню есть кнопка сохранить, если какие-то парты удалениы они удаляются из таблицы и добавленные парты
//        добавляются в таблицу + обязательно добавляются два новых места(если парта двухместная)
//        *
//        *при удалении парты каскадом удаляются все места ссылающиеся на неё и все зависимости ученик - место, вверх удаление не идёт
//        *
//        * -----------------------------------------------------------------------
//        */

        //какой view появился позже тот и отображаться будет выше

        cabinetId = getIntent().getLongExtra(EDITED_OBJECT_ID, 1);//получаем id кабинета
        Log.i("TeachersApp", "CabinetRedactorActivity - onCreate editedObjectId = " + cabinetId);

        Cursor cabinetCursor = db.getCabinets(cabinetId);
        cabinetCursor.moveToFirst();
        setTitle("редактирование кабинета \"" +
                cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)) +
                "\"");
        cabinetCursor.close();


        instrumentalImage = (ImageView) findViewById(R.id.activity_cabinet_redactor_instrumental_image);
        instrumentalImage.setOnClickListener(this);

        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);//курсор с партами
        while (desksCursor.moveToNext()) {//начальный вывод парт
            long deskId = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID));
            long deskX = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X));
            long deskY = desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y));
            final RelativeLayout deskLayout = new RelativeLayout(this);
            deskCoordinatesList.add(new CabinetRedactorPoint(deskId, deskLayout, deskX, deskY));
            RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(2000 * multiplier), (int) pxFromDp(1000 * multiplier));
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            deskLayoutParams.leftMargin = (int) pxFromDp(deskX * 25 * multiplier);
            deskLayoutParams.topMargin = (int) pxFromDp(deskY * 25 * multiplier);
            deskLayout.setLayoutParams(deskLayoutParams);
            deskLayout.setBackgroundColor(Color.parseColor("#f1bd7d"));
            out.addView(deskLayout);
        }
        desksCursor.close();

        //текст в центре
        stateText = (TextView) findViewById(R.id.cabinet_redactor_state_text);
        stateText.setText("Добавьте парту нажатием на '+', и поставьте ее так как она стоит в кабинете. Для удаления нажмите на парту и перетащите ее к появившемуся красному кресту");
        //если парт нет, то показываем текст
        if (deskCoordinatesList.size() == 0) {
            stateText.setText("Добавьте парту нажатием на '+', и поставьте ее так как она стоит в кабинете. Для удаления нажмите на парту и перетащите ее к появившемуся красному кресту");
        } else {
            stateText.setText("");
        }


//        //красная точка
//        relativeLayout = new RelativeLayout(this);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(8, 4);//фиксированный размер
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//        layoutParams.leftMargin = 50;
//        layoutParams.topMargin = 50;

        //relativeLayout.setLayoutParams(layoutParams);
        out.setOnTouchListener(this);
        //relativeLayout.setBackgroundColor(Color.RED);
        //out.addView(relativeLayout);

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //если парт нет, то показываем текст
        if (deskCoordinatesList.size() == 0) {
            stateText.setText("Добавьте парту нажатием на '+', и поставьте ее так как она стоит в кабинете. Для удаления нажмите на парту и перетащите ее к появившемуся красному кресту");
        } else {
            stateText.setText("");
        }

//        //
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(8, 4);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//        //

        RelativeLayout.LayoutParams deskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(2000 * multiplier), (int) pxFromDp(1000 * multiplier));
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        deskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                //
//                layoutParams.leftMargin = (int) (motionEvent.getX());
//                layoutParams.topMargin = (int) (motionEvent.getY());
//                relativeLayout.setLayoutParams(layoutParams);
//                //
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if ((motionEvent.getX() >= pxFromDp(deskCoordinatesList.get(i).x * 25 * multiplier)) &&
                            (motionEvent.getX() <= pxFromDp((deskCoordinatesList.get(i).x * 25 + 2000) * multiplier)) &&
                            (motionEvent.getY() >= pxFromDp(deskCoordinatesList.get(i).y * 25 * multiplier)) &&
                            (motionEvent.getY() <= pxFromDp((deskCoordinatesList.get(i).y * 25 + 1000) * multiplier))) {

                        instrumentalImage.setImageResource(R.drawable.ic_delete);

                        checkedDeskId = deskCoordinatesList.get(i).deskId;
                        //совмещаем точку нажатия и центр(-40;-20) парты
                        deskLayoutParams.leftMargin = (int) (motionEvent.getX() - pxFromDp(1000 * multiplier));
                        deskLayoutParams.topMargin = (int) (motionEvent.getY() - pxFromDp(500 * multiplier));
                        deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE://старые + новая - x нажатия
//                //
//                layoutParams.leftMargin = (int) (motionEvent.getX());
//                layoutParams.topMargin = (int) (motionEvent.getY());
//                relativeLayout.setLayoutParams(layoutParams);
//                //
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if (deskCoordinatesList.get(i).deskId == checkedDeskId) {
                        //если палец находится в пределах крестика то удаляем парту
                        if ((motionEvent.getX() >= out.getWidth() / 2 - 25 * getApplicationContext().getResources().getDisplayMetrics().density) &&
                                (motionEvent.getX() <= out.getWidth() / 2 + 25 * getApplicationContext().getResources().getDisplayMetrics().density) &&
                                (motionEvent.getY() >= out.getHeight() - 50 * getApplicationContext().getResources().getDisplayMetrics().density) &&
                                (motionEvent.getY() <= out.getHeight())) {
                            Log.i("TeachersApp", "yay!");
                            //удаляем парту
                            DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                            db.deleteDesk(deskCoordinatesList.get(i).deskId);
                            out.removeView(deskCoordinatesList.get(i).desk);
                            deskCoordinatesList.remove(i);
                            instrumentalImage.setImageResource(R.drawable.ic_input_add);
                        } else {

                            deskLayoutParams.leftMargin = (int) (motionEvent.getX() - pxFromDp(1000 * multiplier));
                            deskLayoutParams.topMargin = (int) (motionEvent.getY() - pxFromDp(500 * multiplier));
                            deskCoordinatesList.get(i).desk.setLayoutParams(deskLayoutParams);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (int i = 0; i < deskCoordinatesList.size(); i++) {
                    if (deskCoordinatesList.get(i).deskId == checkedDeskId) {

                        instrumentalImage.setImageResource(R.drawable.ic_input_add);

                        deskCoordinatesList.get(i).x = (long) ((motionEvent.getX() - pxFromDp(1000 * multiplier)) / pxFromDp(25 * multiplier));
                        deskCoordinatesList.get(i).y = (long) ((motionEvent.getY() - pxFromDp(500 * multiplier)) / pxFromDp(25 * multiplier));
                        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
                        db.setDeskCoordinates(deskCoordinatesList.get(i).deskId,
                                deskCoordinatesList.get(i).x,
                                deskCoordinatesList.get(i).y);
                    }
                }
                Log.i("TeachersApp", "X = " + motionEvent.getX() +
                        " ; Y = " + motionEvent.getY() + " ;");
                checkedDeskId = -1;
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        //todo места к парте
        // узнаем размеры экрана из класса Display
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        long deskId = db.createDesk(2, (int) (dpFromPx(metricsB.widthPixels / 2) / (25 * multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2) / (25 * multiplier)), cabinetId);

        db.createPlace(deskId, 1);
        db.createPlace(deskId, 2);

        RelativeLayout newDeskLayout = new RelativeLayout(this);

        RelativeLayout.LayoutParams newDeskLayoutParams = new RelativeLayout.LayoutParams((int) pxFromDp(2000 * multiplier), (int) pxFromDp(1000 * multiplier));
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newDeskLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        newDeskLayoutParams.leftMargin = (int) (metricsB.widthPixels / 2 - pxFromDp(1000 * multiplier));
        newDeskLayoutParams.topMargin = (int) (metricsB.heightPixels / 2 - pxFromDp(500 * multiplier));
        newDeskLayout.setLayoutParams(newDeskLayoutParams);
        newDeskLayout.setBackgroundColor(Color.parseColor("#f1bd7d"));

        deskCoordinatesList.add(new CabinetRedactorPoint(deskId, newDeskLayout, (int) (dpFromPx(metricsB.widthPixels / 2 - pxFromDp(1000 * multiplier)) / (25 * multiplier)), (int) (dpFromPx(metricsB.heightPixels / 2 - pxFromDp(500 * multiplier)) / (25 * multiplier))));

        out.addView(newDeskLayout);
        //появилась парта, удаляем текст
        stateText.setText("");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://кнопка назад в actionBar
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private float pxFromDp(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px) {
        return px / getApplicationContext().getResources().getDisplayMetrics().density;
    }

}

class CabinetRedactorPoint {
    long deskId;
    RelativeLayout desk;
    long x;
    long y;

    CabinetRedactorPoint(long id, RelativeLayout relativeDesk, long x, long y) {
        this.deskId = id;
        this.desk = relativeDesk;
        this.x = x;
        this.y = y;
    }
}