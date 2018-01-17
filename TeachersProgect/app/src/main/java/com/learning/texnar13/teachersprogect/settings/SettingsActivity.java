package com.learning.texnar13.teachersprogect.settings;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle("Настройки");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar


        //изменение размера

        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        SeekBar sizeSeekBar = (SeekBar) findViewById(R.id.activity_settings_seekBar);
        LinearLayout sizeShowLayOut = (LinearLayout) findViewById(R.id.activity_settings_size_show_layout);

        final RelativeLayout room = new RelativeLayout(this);
        sizeShowLayOut.addView(room, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);


        if (db.getInterfaceSizeBySettingsProfileId(1) == -1){
            db.createNewSettingsProfileWithId1("default", 50);//TODO Skipped 49 frames!  The application may be doing too much work on its main thread.
        }
        sizeSeekBar.setProgress((int) db.getInterfaceSizeBySettingsProfileId(1));
        updateShowRoom(room, (int) db.getInterfaceSizeBySettingsProfileId(1));


        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//не когда касается, а когда начинает двигаться

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i != 0) {//избегаем деления на ноль
                    updateShowRoom(room, i);
                    db.setSettingsProfileParameters(1, "default", i);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        //удаление данных
        Button removeDataButton = (Button) findViewById(R.id.activity_settings_remove_data_button);
        removeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataBaseOpenHelper dbOpenHelper = new DataBaseOpenHelper(getApplicationContext());
                dbOpenHelper.restartTable();

                dbOpenHelper.createClass("1\"A\"");
                long classId = dbOpenHelper.createClass("2\"A\"");

                long lerner1Id = dbOpenHelper.createLearner("Зинченко", "Сократ", classId);
                long lerner2Id = dbOpenHelper.createLearner("Шумякин", "Феофан", classId);
                long lerner3Id = dbOpenHelper.createLearner("Рябец", "Валентин", classId);
                long lerner4Id = dbOpenHelper.createLearner("Гроша", "Любава", classId);
                long lerner5Id = dbOpenHelper.createLearner("Авдонина", "Алиса", classId);


                long cabinetId = dbOpenHelper.createCabinet("406");

                ArrayList<Long> places = new ArrayList<>();
                {
                    long desk1Id = dbOpenHelper.createDesk(2, 160, 200, cabinetId);//1
                    places.add(dbOpenHelper.createPlace(desk1Id, 1));
                    places.add(dbOpenHelper.createPlace(desk1Id, 2));
                }
                {
                    long desk2Id = dbOpenHelper.createDesk(2, 40, 200, cabinetId);//2
                    places.add(dbOpenHelper.createPlace(desk2Id, 1));
                    places.add(dbOpenHelper.createPlace(desk2Id, 2));
                }
                {
                    long desk3Id = dbOpenHelper.createDesk(2, 160, 120, cabinetId);//3
                    places.add(dbOpenHelper.createPlace(desk3Id, 1));
                    places.add(dbOpenHelper.createPlace(desk3Id, 2));
                }
                {
                    long desk4Id = dbOpenHelper.createDesk(2, 40, 120, cabinetId);//4
                    places.add(dbOpenHelper.createPlace(desk4Id, 1));
                    places.add(dbOpenHelper.createPlace(desk4Id, 2));
                }
                {
                    long desk5Id = dbOpenHelper.createDesk(2, 160, 40, cabinetId);//5
                    places.add(dbOpenHelper.createPlace(desk5Id, 1));
                    places.add(dbOpenHelper.createPlace(desk5Id, 2));
                }
                {
                    long desk6Id = dbOpenHelper.createDesk(2, 40, 40, cabinetId);//6
                    places.add(dbOpenHelper.createPlace(desk6Id, 1));
                    places.add(dbOpenHelper.createPlace(desk6Id, 2));
                }
                //   |6|  |5|   |    |  |  |  |
                //   |4|  |3|   |    | 4|  |  |
                //   |2|  |1|   |    |35|  |21|
                //       [-]


                long lessonId = dbOpenHelper.createSubject("физика", classId
                        //, cabinetId
                );
                Date startLessonTime = new GregorianCalendar(2017, 10, 17, 8, 30).getTime();//1502343000000 --10 августа
                Date endLessonTime = new GregorianCalendar(2017, 10, 17, 9, 15).getTime();//на 7 месяц  1502345700000
                dbOpenHelper.setLessonTimeAndCabinet(lessonId, cabinetId, startLessonTime, endLessonTime, SchoolContract.TableSubjectAndTimeCabinetAttitude.CONSTANT_REPEAT_NEVER);

                //создание настроек после удаления таблицы
                //db.createNewSettingsProfileWithId1("default", 50);

                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner1Id, places.get(1));
                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner2Id, places.get(0));
                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner3Id, places.get(2));
                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner4Id, places.get(7));
                dbOpenHelper.setLearnerOnPlace(//lessonId,
                        lerner5Id, places.get(3));
                dbOpenHelper.close();
            }
        });
    }

    private void updateShowRoom(RelativeLayout room, float multiplier) {
        room.removeAllViews();

        multiplier = multiplier / 1000;

        RelativeLayout[] tables = new RelativeLayout[4];
        RelativeLayout.LayoutParams[] tablesParams = new RelativeLayout.LayoutParams[4];

        for (int i = 0; i < 4; i++) {
            tables[i] = new RelativeLayout(this);
            tables[i].setBackgroundColor(Color.parseColor("#dad5de"));
            tablesParams[i] = new RelativeLayout.LayoutParams(
                    (int) pxFromDp(2000 * multiplier), (int) pxFromDp(1000 * multiplier));

            tablesParams[i].addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            tablesParams[i].addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            tablesParams[i].addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);


            room.addView(tables[i], tablesParams[i]);
        }
        // 00 11
        // 22 33
        tablesParams[0].setMargins((int) pxFromDp(1000 * multiplier), (int) pxFromDp(1000 * multiplier), 0, 0);
        tablesParams[1].setMargins((int) pxFromDp(4000 * multiplier), (int) pxFromDp(1000 * multiplier), 0, 0);
        tablesParams[2].setMargins((int) pxFromDp(1000 * multiplier), (int) pxFromDp(3000 * multiplier), 0, 0);
        tablesParams[3].setMargins((int) pxFromDp(4000 * multiplier), (int) pxFromDp(3000 * multiplier), 0, 0);

        //room.setLayoutParams(new LinearLayout.LayoutParams());
    }

    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
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
}
