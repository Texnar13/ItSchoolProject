package com.learning.texnar13.teachersprogect.lesson;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.SettingsActivity;
import com.learning.texnar13.teachersprogect.StartScreenActivity;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class LessonActivity extends AppCompatActivity {

    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    final ArrayList<LearnerAndGrade> gradeArrayList = new ArrayList<>();//массив с оценками за этот урок;
    float multiplier = 2;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.main_menu_end_lesson).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                long[] learnersId = new long[gradeArrayList.size()];
                long[] grades1 = new long[gradeArrayList.size()];
                long[] grades2 = new long[gradeArrayList.size()];
                long[] grades3 = new long[gradeArrayList.size()];
                for (int j = 0; j < gradeArrayList.size(); j++) {
                    learnersId[j] = gradeArrayList.get(j).getLearnerId();
                    grades1[j] = gradeArrayList.get(j).getRawGrade(0);
                    grades2[j] = gradeArrayList.get(j).getRawGrade(1);
                    grades3[j] = gradeArrayList.get(j).getRawGrade(2);
                }
                Intent intent = new Intent(getApplicationContext(), LessonListActivity.class);
                intent.putExtra(LessonListActivity.LIST_ID, learnersId);
                intent.putExtra(LessonListActivity.FIRST_LIST_GRADES, grades1);
                intent.putExtra(LessonListActivity.SECOND_LIST_GRADES, grades2);
                intent.putExtra(LessonListActivity.THIRD_LIST_GRADES, grades3);
                startActivity(intent);
                finish();
                return true;
            }
        });
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        //3 это класс урока
        /*
        * вывод учеников, парт и мест, по нажатию на ученика открывается доп.меню*
        где можно выбрать оценку, нет оценки/1/2/3/4/5
        после окончания урока учитель нажимает закончить урок
        выводим стастику за весь урок(2)
        *
        *
        *
        *    _________________________________
        *   |название урока     закончить урок|
        *   |_________________________________|
        *   |                                 |
        *   |                                 |
        *   |    _______         _______      |
        *   |   |имя|имя|       |имя|имя|     |
        *   |   |фам|фам|       |фам|фам|     |
        *   |    _______         _______      |
        *   |   |имя|имя|       |имя|имя|     |
        *   |   |фам|фам|       |фам|фам|     |
        *   |    _______         _______      |
        *   |   |имя|имя|       |имя|имя|     |
        *   |   |фам|фам|       |фам|фам|     |
        *   |    _______         _______      |
        *   |   |имя|имя|       |имя|имя|     |
        *   |   |фам|фам|       |фам|фам|     |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |_________________________________|
        *
        *
        *
        * (2)
        *    _________________________________
        *   |  имя урока            сохранить |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |   имя фамилия            оценка |
        *   |_________________________________|
        *   |                                 |
        *   |_________________________________|
        *
        *
        * нажимаем сохранить и сохраняем в таблицу ученик-оценка
        *
        *
        *
        *
        *
        *
        * */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_main);

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;

        setTitle("Урок");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        RelativeLayout room = (RelativeLayout) findViewById(R.id.room_layout);


        long lessonAttitudeId;
        long classId;
        long cabinetId;
        Cursor lessonCursor;//курсор с текущим уроком
        Cursor desksCursor;//курсор с партами


        if (getIntent().getLongExtra(LESSON_ATTITUDE_ID, -1) == -1) {//-1 ошибка, 1> использовать переданные
            Toast toast = Toast.makeText(this, "ошибка при получении урока!", Toast.LENGTH_LONG);
            toast.show();
            Log.i("TeachersApp", "LessonActivity - error with intent attitudeId == 1");
            finish();
            return;
        } else {
            lessonAttitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -1);
        }

        //получаем все данные о классе
        //курсор с зависимостью
        Cursor lessonAttitudeCursor = db.getLessonAttitudeById(lessonAttitudeId);
        lessonAttitudeCursor.moveToFirst();
        //курсор с уроком
        lessonCursor = db.getLessonById(lessonAttitudeCursor.getLong(lessonAttitudeCursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_LESSON_ID)));
        lessonCursor.moveToFirst();

        classId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableLessons.KEY_CLASS_ID));
        cabinetId = lessonAttitudeCursor.getLong(lessonAttitudeCursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.KEY_CABINET_ID));

        lessonCursor.close();
        lessonAttitudeCursor.close();
        //курсор с партами
        desksCursor = db.getDesksByCabinetId(cabinetId);
        //learnersCursor = db.getLearnersByClassId(classId);
        //seatingCursor = db.getAttitudesByLessonId(lessonId);

        int i = -1;//щётчик учеников

        //todo берём макс значение парты по X и по y прибавляем отступ минимальных и размер мах парты получаем размер layout
        while (desksCursor.moveToNext()) {
            //создание парты
            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
            tempRelativeLayoutDesk.setBackgroundColor(Color.LTGRAY);

            //длина парты по количеству мест
            RelativeLayout.LayoutParams tempRelativeLayoutDeskParams =
                    new RelativeLayout.LayoutParams((int) pxFromDp(
                            desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES)) *
                                    1000 * multiplier), (int) pxFromDp(1000 * multiplier));
            tempRelativeLayoutDeskParams.leftMargin = (int) pxFromDp(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * 25 * multiplier);
            tempRelativeLayoutDeskParams.topMargin = (int) pxFromDp(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * 25 * multiplier);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            Log.i("TeachersApp", "LessonActivity - onCreate view desk:" + desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));

            //создание места
            Cursor placeCursor = db.getPlacesByDeskId(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
            while (placeCursor.moveToNext()) {

                // layout с учеником и оценками
                RelativeLayout gradeLearnerPlaceOut = new RelativeLayout(this);
                gradeLearnerPlaceOut.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                RelativeLayout.LayoutParams tempRelativeLayoutPlaceParams = new RelativeLayout.LayoutParams((int) pxFromDp((1000 - 50) * multiplier), (int) pxFromDp((1000 - 50) * multiplier));
                tempRelativeLayoutPlaceParams.leftMargin = (int) pxFromDp((25 + (1000 * (placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.COLUMN_ORDINAL)) - 1))) * multiplier);
                tempRelativeLayoutPlaceParams.topMargin = (int) pxFromDp(25 * multiplier);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_START);

                final TextView grade1Text = new TextView(this);
                grade1Text.setTextColor(Color.WHITE);
                grade1Text.setTextSize(325 * multiplier);
                grade1Text.setText("");


                final TextView grade2Text = new TextView(this);
                grade2Text.setTextColor(Color.WHITE);
                grade2Text.setTextSize(325 * multiplier);
                grade2Text.setText("");

                RelativeLayout.LayoutParams grade1TextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                grade1TextParams.setMargins((int) pxFromDp(25 * multiplier), 0, 0, 0);
                //grade1TextParams.setMarginStart(multiplier);

                RelativeLayout.LayoutParams grade2TextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                grade2TextParams.setMargins(0, 0, (int) pxFromDp(25 * multiplier), 0);

                gradeLearnerPlaceOut.addView(grade1Text, grade1TextParams);
                gradeLearnerPlaceOut.addView(grade2Text, grade2TextParams);


                // layout с учеником и текстом
                LinearLayout tempPlaceLayout = new LinearLayout(this);
                tempPlaceLayout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout.LayoutParams tempPlaceLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


                Log.i("TeachersApp", "LessonActivity - onCreate view place:" + placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));

                //создание ученика
                long learnerId = db.getLearnerIdByClassIdAndPlaceId(classId, placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));
                if (learnerId != -1) {
                    //оценки
                    i++;
                    final int tempGradeId = i;
                    gradeArrayList.add(i, new LearnerAndGrade(learnerId));


                    Cursor learnerCursor = db.getLearner(learnerId);//получаем ученика
                    learnerCursor.moveToFirst();
                    //картинка ученика
                    final ImageView tempLernerImage = new ImageView(this);
                    tempLernerImage.setImageResource(R.drawable.learner_gray);//по умолчанию серая картинка
                    LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1F);
                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (gradeArrayList.get(tempGradeId).getGrade() != 5) {
                                gradeArrayList.get(tempGradeId).setGrade((byte) (1 + gradeArrayList.get(tempGradeId).getGrade()));
                            } else {
                                gradeArrayList.get(tempGradeId).setGrade((byte) 1);
                            }

                            switch ((int) gradeArrayList.get(tempGradeId).getGrade()) {
//                                case 0:
//                                    tempLernerImage.setImageResource(R.drawable.learner_gray);
//                                    break;
                                case 1:
                                    tempLernerImage.setImageResource(R.drawable.learner_red);
                                    break;
                                case 2:
                                    tempLernerImage.setImageResource(R.drawable.learner_orange);
                                    break;
                                case 3:
                                    tempLernerImage.setImageResource(R.drawable.learner_yellow);
                                    break;
                                case 4:
                                    tempLernerImage.setImageResource(R.drawable.learner_lime);
                                    break;
                                case 5:
                                    tempLernerImage.setImageResource(R.drawable.learner_green);
                                    break;
                            }
                        }
                    };
                    tempLernerImage.setOnClickListener(onClickListener);
                    View.OnCreateContextMenuListener onCreateContextMenuListener = new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

                            if (gradeArrayList.get(tempGradeId).getGrade() != 0) {
                                contextMenu.add(0, 0, 0, "нет оценки").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_gray);
                                        gradeArrayList.get(tempGradeId).setGrade((byte) 0);
                                        return true;
                                    }
                                });
                            }
                            if (gradeArrayList.get(tempGradeId).getGrade() != 1) {
                                contextMenu.add(0, 1, 0, "1").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_red);
                                        gradeArrayList.get(tempGradeId).setGrade((byte) 1);
                                        return true;
                                    }
                                });
                            }
                            if (gradeArrayList.get(tempGradeId).getGrade() != 2) {
                                contextMenu.add(0, 2, 0, "2").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_orange);
                                        gradeArrayList.get(tempGradeId).setGrade((byte) 2);
                                        return true;
                                    }
                                });
                            }
                            if (gradeArrayList.get(tempGradeId).getGrade() != 3) {
                                contextMenu.add(0, 3, 0, "3").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_yellow);
                                        gradeArrayList.get(tempGradeId).setGrade((byte) 3);
                                        return true;
                                    }
                                });
                            }
                            if (gradeArrayList.get(tempGradeId).getGrade() != 4) {
                                contextMenu.add(0, 4, 0, "4").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_lime);
                                        gradeArrayList.get(tempGradeId).setGrade((byte) 4);
                                        return true;
                                    }
                                });
                            }
                            if (gradeArrayList.get(tempGradeId).getGrade() != 5) {
                                contextMenu.add(0, 5, 0, "5").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_green);
                                        gradeArrayList.get(tempGradeId).setGrade((byte) 5);
                                        return true;
                                    }
                                });
                            }
                            if (gradeArrayList.get(tempGradeId).getGradesCount() != 2 && gradeArrayList.get(tempGradeId).getGrade() != 0) {
                                contextMenu.add(0, 6, 0, "новая оценка").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_gray);
                                        switch (gradeArrayList.get(tempGradeId).getGradesCount()) {
                                            case 0:
                                                grade1Text.setText("" + gradeArrayList.get(tempGradeId).getGrade());
                                                break;
                                            case 1:
                                                grade2Text.setText("" + gradeArrayList.get(tempGradeId).getGrade());
                                                break;
                                        }
                                        gradeArrayList.get(tempGradeId).nextGrade();
                                        return true;
                                    }
                                });
                            }
                        }
                    };
                    tempLernerImage.setOnCreateContextMenuListener(onCreateContextMenuListener);
                    tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);

                    //текст ученика
                    TextView tempLearnerText = new TextView(this);
                    tempLearnerText.setOnClickListener(onClickListener);
                    tempLearnerText.setTextSize(200 * multiplier);
                    tempLearnerText.setOnCreateContextMenuListener(onCreateContextMenuListener);
                    tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
                    tempLearnerText.setTextColor(Color.WHITE);
                    tempLearnerText.setText(learnerCursor.getString(learnerCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
                    LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 3F);
                    tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
                }
                Log.i("TeachersApp", "LessonActivity!!! " + learnerId);


                gradeLearnerPlaceOut.addView(tempPlaceLayout, tempPlaceLayoutParams);
                //добавление места в парту
                tempRelativeLayoutDesk.addView(gradeLearnerPlaceOut, tempRelativeLayoutPlaceParams);
            }
            placeCursor.close();

            //добавление парты в комнату
            room.addView(tempRelativeLayoutDesk, tempRelativeLayoutDeskParams);
        }
        desksCursor.close();
        db.close();
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

class LearnerAndGrade {
    private long learnerId;
    private byte gradesCount = 0;
    private byte[] grade = new byte[3];


    LearnerAndGrade(long learnerId) {
        this.learnerId = learnerId;
    }

    long getLearnerId() {
        return learnerId;
    }

    byte getGrade() {
        return grade[gradesCount];
    }

    byte getRawGrade(int i) {
        if (i >= 0 && i <= 2) {
            return grade[i];
        }
        return grade[0];
    }

    void setGrade(byte grade) {
        this.grade[gradesCount] = grade;
    }

    void nextGrade() {
        if (gradesCount != 2) {
            gradesCount++;
        }
    }

    byte getGradesCount() {
        return gradesCount;
    }
}

/*
 ArrayList<SchoolClass> NAME_TABLE_CLASSES;
        {
            ArrayList<Learner> a1 = new ArrayList<>();
            Зинченко Сократ
            Шумякин Феофан
            Рябец Валентин
            Гроша Любава
            Авдонина Алиса

            ArrayList<Learner> a2 = new ArrayList<>();
            a2.add(new Learner("Воробьев Прокофий"));//4
            a2.add(new Learner("Родзянко Леондий"));//5
            a2.add(new Learner("Таттар Ульян"));//6
            a2.add(new Learner("Завразина Ксения"));//23
            a2.add(new Learner("Чепурина Валентина"));//24

            ArrayList<Learner> a3 = new ArrayList<>();
            a3.add(new Learner("Юсупов Серафим"));//7
            a3.add(new Learner("Кайназаров Демьян"));//8
            a3.add(new Learner("Малыхин Гаврила"));//9
            a3.add(new Learner("Лутугина Аза"));//25
            a3.add(new Learner("Лаврентьева Ирина"));//26

            ArrayList<Learner> a4 = new ArrayList<>();
            a4.add(new Learner("Дегтярев Василий"));//10
            a4.add(new Learner("Шастин Афанасий"));//11
            a4.add(new Learner("Челомцев Евграф"));//12
            a4.add(new Learner("Краснокутская Варвара"));//27
            a4.add(new Learner("Живкова Антонина"));//28

            ArrayList<Learner> b1 = new ArrayList<>();
            b1.add(new Learner("Русанов Владлен"));//13
            b1.add(new Learner("Виноградов Радион"));//14
            b1.add(new Learner("Фукин Лукьян"));//15
            b1.add(new Learner("Бессонова Ольга"));//29
            b1.add(new Learner("Витковская Эвелина"));//30

            ArrayList<Learner> b2 = new ArrayList<>();
            b2.add(new Learner("Новомейский Казимир"));//16
            b2.add(new Learner("Шайнюк Александр"));//17
            b2.add(new Learner("Кошляк Николай"));//18
            b2.add(new Learner("Артёмова Каролина"));//31
            b2.add(new Learner("Косыгина Ева"));//32

            ArrayList<Learner> c1 = new ArrayList<>();
            c1.add(new Learner("Сальков Архип"));//19
            c1.add(new Learner("Шаньгин Игорь"));//20
            c1.add(new Learner("Ясинская Анна"));//33
            c1.add(new Learner("Ярцева Лариса"));//34
            c1.add(new Learner("Турбина Александра"));//35

            NAME_TABLE_CLASSES = new ArrayList<>();
            NAME_TABLE_CLASSES.add(new SchoolClass("a1", a1));
            NAME_TABLE_CLASSES.add(new SchoolClass("a2", a2));
            NAME_TABLE_CLASSES.add(new SchoolClass("a3", a3));
            NAME_TABLE_CLASSES.add(new SchoolClass("a4", a4));
            NAME_TABLE_CLASSES.add(new SchoolClass("b1", b1));
            NAME_TABLE_CLASSES.add(new SchoolClass("b2", b2));
            NAME_TABLE_CLASSES.add(new SchoolClass("c1", c1));
        }

        Cabinet cabinet1;
        {
            ArrayList<Desk> NAME_TABLE_DESKS = new ArrayList<Desk>();
            NAME_TABLE_DESKS.add(new Desk(1, 160, 200, 2));//1
            NAME_TABLE_DESKS.add(new Desk(2, 40, 200, 2));//2
            NAME_TABLE_DESKS.add(new Desk(3, 160, 120, 2));//3
            NAME_TABLE_DESKS.add(new Desk(4, 40, 120, 2));//4
            NAME_TABLE_DESKS.add(new Desk(5, 160, 40, 2));//5
            NAME_TABLE_DESKS.add(new Desk(6, 40, 40, 2));//6
            //   |6|  |5|
            //   |4|  |3|
            //   |2|  |1|
            //       [-]

            cabinet1 = new Cabinet("cab1", NAME_TABLE_DESKS, 0);
        }

        RelativeLayout room = (RelativeLayout) findViewById(R.id.room_layout);
        for (int i = 0; i < cabinet1.NAME_TABLE_DESKS.size(); i++) {
            RelativeLayout tempRelativeLayout = new RelativeLayout(this);
            tempRelativeLayout.setBackgroundColor(Color.parseColor("#bce4af00"));

            RelativeLayout.LayoutParams tempLayoutParams = new RelativeLayout.LayoutParams((int) dpFromPx(80), (int) dpFromPx(40));
            tempLayoutParams.leftMargin = (int) dpFromPx(cabinet1.NAME_TABLE_DESKS.get(i).getCoordinateX());
            tempLayoutParams.topMargin = (int) dpFromPx(cabinet1.NAME_TABLE_DESKS.get(i).getCoordinateY());
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);

            room.addView(tempRelativeLayout, tempLayoutParams);
        }
 */
