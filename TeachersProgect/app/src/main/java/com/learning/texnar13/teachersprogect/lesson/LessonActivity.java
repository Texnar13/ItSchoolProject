package com.learning.texnar13.teachersprogect.lesson;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
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

import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.SeatingRedactorActivity;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.lessonList.LessonListActivity;

//todo
    /*итак, надо полностью пределать урок,
    * onCreate(),
    * подгружаются все ученики, id кабинета, класса, предмета, зав. урок-время
    * создаются массивы с оценками(или подгружаются;)),
    *
    * эти поля должны быть статичными, и с проверкой на существование(для переворота)
    * при переходе удалить
    *
    * OnStart(),
    * выводятся парты и ученики
    *
    * */

public class LessonActivity extends AppCompatActivity {

    //----------------данные----------------
    //--константы--
    public static final String LESSON_ATTITUDE_ID = "lessonAttitudeId";
    //--переменные--
    float multiplier = 2;
    //--загружаемые из бд данные--
    //id
    static long lessonAttitudeId = -2;
    static long subjectId;
    static long learnersClassId;
    static long cabinetId;
    //текст
    static String lessonName = "";
    //массивы
    static LearnerAndGrade[] learnersAndGrades;


//    static ArrayList<LearnerAndGrade> gradeArrayList = new ArrayList<>();//массив с оценками за этот урок;

//    static long lessonAttitudeId;
//    static long lessonId;
//

//------------------------------------подготовка меню-----------------------------------------------

    //--раздуваем--
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_menu, menu);
        return true;
    }

    //--назначаем действия--
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //закончить урок
        menu.findItem(R.id.lesson_menu_end_lesson).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                long[] learnersId = new long[learnersAndGrades.length];
                long[] grades1 = new long[learnersAndGrades.length];
                long[] grades2 = new long[learnersAndGrades.length];
                long[] grades3 = new long[learnersAndGrades.length];
                for (int j = 0; j < learnersAndGrades.length; j++) {
                    learnersId[j] = learnersAndGrades[j].learnerId;
                    grades1[j] = learnersAndGrades[j].getRawGrade(0);
                    grades2[j] = learnersAndGrades[j].getRawGrade(1);
                    grades3[j] = learnersAndGrades[j].getRawGrade(2);
                }
                Intent intent = new Intent(getApplicationContext(), LessonListActivity.class);
                intent.putExtra(LessonListActivity.ATTITUDE_ID, lessonAttitudeId);
                intent.putExtra(LessonListActivity.SUBJECT_ID, subjectId);
                intent.putExtra(LessonListActivity.LIST_ID, learnersId);
                intent.putExtra(LessonListActivity.FIRST_LIST_GRADES, grades1);
                intent.putExtra(LessonListActivity.SECOND_LIST_GRADES, grades2);
                intent.putExtra(LessonListActivity.THIRD_LIST_GRADES, grades3);
                startActivity(intent);

                //обнуляем данные
                lessonAttitudeId = -2;
                subjectId = 0;
                learnersClassId = 0;
                cabinetId = 0;
                lessonName = null;
                learnersAndGrades = null;
                finish();
                return true;
            }
        });
        //посадить учеников
        menu.findItem(R.id.lesson_menu_edit_seating).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //намерение перехода на редактор рассадки
                Intent intent = new Intent(getApplicationContext(), SeatingRedactorActivity.class);
                //кладем id в intent
                intent.putExtra(SeatingRedactorActivity.CLASS_ID, learnersClassId);
                intent.putExtra(SeatingRedactorActivity.CABINET_ID, cabinetId);
                //переходим
                startActivity(intent);
                return true;
            }
        });
        //расставить парты
        menu.findItem(R.id.lesson_menu_edit_tables).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //намерение перехода на редактор кабинета
                Intent intent = new Intent(getApplicationContext(), CabinetRedactorActivity.class);
                //кладем id в intent
                intent.putExtra(CabinetRedactorActivity.EDITED_OBJECT_ID, cabinetId);
                //переходим
                startActivity(intent);
                return true;
            }
        });
        //подсказка
        menu.findItem(R.id.lesson_menu_help).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent = new Intent(getApplicationContext(), LessonHelp.class);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

//----------------------------------создание экрана-------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_main);
        //кнопка назад в actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //проверка, есть ли уже данные, если нет, то достаем их
        if (lessonAttitudeId == -2) {

//-------------------------------загружаем статичные поля из бд-------------------------------------
            DataBaseOpenHelper db = new DataBaseOpenHelper(this);

            // ---заполняем---
            //id зависимости
            lessonAttitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -1);
            //если id не передано
            if (lessonAttitudeId == -1) {
                finish();
            }

            //курсор с зависимостью, из него все данные
            Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(lessonAttitudeId);
            attitudeCursor.moveToFirst();
            //id кабинета
            cabinetId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID
            ));
            //id предмета
            subjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(
                    SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID
            ));
            //закончили работать с зависимостью
            attitudeCursor.close();

            //курсор с предметом, из него все данные
            Cursor subjectCursor = db.getSubjectById(subjectId);
            subjectCursor.moveToFirst();
            //id класса
            learnersClassId = subjectCursor.getLong(subjectCursor.getColumnIndex(
                    SchoolContract.TableSubjects.KEY_CLASS_ID
            ));
            lessonName = subjectCursor.getString(subjectCursor.getColumnIndex(
                    SchoolContract.TableSubjects.COLUMN_NAME
            ));
            //закончили работать с предметом
            subjectCursor.close();

            //курсор с учениками, из него все данные
            Cursor learnersCursor = db.getLearnersByClassId(learnersClassId);
            //инициализируем массив нужной длинны
            learnersAndGrades = new LearnerAndGrade[learnersCursor.getCount()];
            //заполняем его
            for (int i = 0; i < learnersAndGrades.length; i++) {
                learnersCursor.moveToPosition(i);
                //создаем нового
                learnersAndGrades[i] = new LearnerAndGrade(
                        learnersCursor.getLong(learnersCursor.getColumnIndex(
                                SchoolContract.TableLearners.KEY_LEARNER_ID
                        ))
                );
            }
            //закончили работать с учениками
            learnersCursor.close();
        }
/*
    * подгружаются все ученики, id кабинета, класса, предмета, зав. урок-время
    * создаются массивы с оценками(или подгружаются;)),
    *
    * эти поля должны быть статичными, и с проверкой на существование(для переворота)
    * при переходе (окончании урока) удалить
    */

        //3 это класс урока
//        * вывод учеников, парт и мест, по нажатию на ученика открывается доп.меню*
//        где можно выбрать оценку, нет оценки/1/2/3/4/5
//        после окончания урока учитель нажимает закончить урок
//        выводим стастику за весь урок(2)
//        *
//        *
//        *
//        *    _________________________________
//        *   |название урока     закончить урок|
//        *   |_________________________________|
//        *   |                                 |
//        *   |                                 |
//        *   |    _______         _______      |
//        *   |   |имя|имя|       |имя|имя|     |
//        *   |   |фам|фам|       |фам|фам|     |
//        *   |    _______         _______      |
//        *   |   |имя|имя|       |имя|имя|     |
//        *   |   |фам|фам|       |фам|фам|     |
//        *   |    _______         _______      |
//        *   |   |имя|имя|       |имя|имя|     |
//        *   |   |фам|фам|       |фам|фам|     |
//        *   |    _______         _______      |
//        *   |   |имя|имя|       |имя|имя|     |
//        *   |   |фам|фам|       |фам|фам|     |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |                                 |
//        *   |_________________________________|
//        *
//        *
//        *
//        * (2)
//        *    _________________________________
//        *   |  имя урока            сохранить |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |   имя фамилия            оценка |
//        *   |_________________________________|
//        *   |                                 |
//        *   |_________________________________|
//        *
//        *
//        * нажимаем сохранить и сохраняем в таблицу ученик-оценка
//        *
//        *
//        *
//        *
//        *
//        *
    }

//------------------------------------запуск экрана-------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();
        //сразу ставим заголовок
        setTitle(lessonName);
//-------------------------------загружаем не статичные поля----------------------------------------
//                      (обновляются при перезаходе на активность)
        //поле вывода, класс
        RelativeLayout room = (RelativeLayout) findViewById(R.id.room_layout);

        //размеры экрана по парте
        int maxX = 0;
        int maxY = 0;

        //база данных
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        //размер парт
        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;
        //курсор с партами
        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);

//--------------------------------вывод парт и учеников---------------------------------------------

        //чистим класс
        room.removeAllViews();
        //выводим парты
        for (int deskIterator = 0; deskIterator < desksCursor.getCount(); deskIterator++) {
            desksCursor.moveToPosition(deskIterator);
//-----------создание парты-----------
            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
            tempRelativeLayoutDesk.setBackgroundColor(Color.LTGRAY);

            //длина парты по количеству мест и фикс. ширина
            RelativeLayout.LayoutParams tempRelativeLayoutDeskParams =
                    new RelativeLayout.LayoutParams(
                            (int) pxFromDp(
                                    desksCursor.getLong(desksCursor.getColumnIndex(
                                            SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES
                                    )) * 1000 * multiplier),
                            (int) pxFromDp(1000 * multiplier));
            //координаты парт
            tempRelativeLayoutDeskParams.leftMargin = (int) pxFromDp(
                    desksCursor.getLong(desksCursor.getColumnIndex(
                            SchoolContract.TableDesks.COLUMN_X
                    )) * 25 * multiplier);
            tempRelativeLayoutDeskParams.topMargin = (int) pxFromDp(
                    desksCursor.getLong(desksCursor.getColumnIndex(
                            SchoolContract.TableDesks.COLUMN_Y
                    )) * 25 * multiplier);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_START);


//-----считаем максимальную парту-----
            if (desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * 100 * multiplier > maxX) {
                maxX = (int) (desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * 100 * multiplier);
            }
            if (desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * 100 * multiplier > maxY) {
                maxY = (int) (desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * 100 * multiplier);
            }


            //выводим места на парте
            Cursor placeCursor = db.getPlacesByDeskId(
                    desksCursor.getLong(desksCursor.getColumnIndex(
                            SchoolContract.TableDesks.KEY_DESK_ID
                    ))
            );
            for (int placeIterator = 0; placeIterator < placeCursor.getCount(); placeIterator++) {
                placeCursor.moveToPosition(placeIterator);
//-----------создание места-----------
//-место
//---контейнер
//-----оценки
//-----контейнер ученика
//-------имя ученика
//-------картинка ученика


//-----контейнер-----
                //создаем layout с контейнером ученика и оценками
                RelativeLayout placeOut = new RelativeLayout(this);
                placeOut.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                //ставим размеры
                RelativeLayout.LayoutParams placeParams = new RelativeLayout.LayoutParams(
                        (int) pxFromDp((1000 - 50) * multiplier),
                        (int) pxFromDp((1000 - 50) * multiplier)
                );
                // и отступы
                placeParams.leftMargin = (int) pxFromDp(
                        (25 + (1000 * (
                                placeCursor.getLong(placeCursor.getColumnIndex(
                                        SchoolContract.TablePlaces.COLUMN_ORDINAL
                                )) - 1))) * multiplier);
                placeParams.topMargin = (int) pxFromDp(25 * multiplier);
                placeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                placeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                placeParams.addRule(RelativeLayout.ALIGN_PARENT_START);

//-----текст оценок-----
                //текст с оценками
                final TextView grade1Text = new TextView(this);
                grade1Text.setTextColor(Color.WHITE);
                grade1Text.setTextSize(325 * multiplier);
                grade1Text.setText("");

                final TextView grade2Text = new TextView(this);
                grade2Text.setTextColor(Color.WHITE);
                grade2Text.setTextSize(325 * multiplier);
                grade2Text.setText("");

                //параметры текста
                RelativeLayout.LayoutParams grade1TextParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                grade1TextParams.setMargins((int) pxFromDp(25 * multiplier), 0, 0, 0);

                RelativeLayout.LayoutParams grade2TextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                grade2TextParams.setMargins(0, 0, (int) pxFromDp(25 * multiplier), 0);

                //выводим текст в контейнер
                placeOut.addView(grade1Text, grade1TextParams);
                placeOut.addView(grade2Text, grade2TextParams);

//------создание ученика на месте------

//-----контейнер ученика----
//(с именем и картинкой)
                //контейнер
                LinearLayout learnerConteiner = new LinearLayout(this);
                learnerConteiner.setOrientation(LinearLayout.VERTICAL);
                //параметры контейнера
                LinearLayout.LayoutParams learnerConteinerParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );

                //получаем ученика, который сидит на этом месте
                long learnerId = db.getLearnerIdByClassIdAndPlaceId(
                        learnersClassId,
                        placeCursor.getLong(placeCursor.getColumnIndex(
                                SchoolContract.TablePlaces.KEY_PLACE_ID
                        ))
                );
                //если ученик есть
                if (learnerId != -1) {

                    //ищем ученика в массиве с оценками
                    int temp = -1;
                    for (int i = 0; i < learnersAndGrades.length; i++) {
                        if (learnersAndGrades[i].learnerId == learnerId) {
                            temp = i;
                            break;
                        }
                    }
                    final int learnerPozition = temp;

                    //получаем ученика
                    Cursor learnerCursor = db.getLearner(learnerId);
                    learnerCursor.moveToFirst();

                    //старые данные сохраненных оценок
                    if (learnersAndGrades[learnerPozition].getGradesCount() == 2) {
                        grade2Text.setText("" + learnersAndGrades[learnerPozition].getRawGrade(1));
                    }
                    if (learnersAndGrades[learnerPozition].getGradesCount() >= 1) {
                        grade1Text.setText("" + learnersAndGrades[learnerPozition].getRawGrade(0));
                    }

//------------картинка ученика------------
                    //создаем картинку
                    final ImageView tempLernerImage = new ImageView(this);
                    //ставим ей изображение по оценке(из памяти)
                    switch ((int) learnersAndGrades[learnerPozition].getGrade()) {
                        case -2:
                            tempLernerImage.setImageResource(R.drawable.learner_white);
                            break;
                        case 0:
                            tempLernerImage.setImageResource(R.drawable.learner_gray);
                            break;
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
                    //параметры картинки
                    LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            1F
                    );

                    //добавляем картинку в контейнер
                    learnerConteiner.addView(tempLernerImage, tempLernerImageParams);
//---------------текст ученика---------------
                    //создание текста
                    TextView tempLearnerText = new TextView(this);
                    tempLearnerText.setTextSize(200 * multiplier);
                    tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
                    tempLearnerText.setTextColor(Color.WHITE);
                    tempLearnerText.setText(
                            learnerCursor.getString(learnerCursor.getColumnIndex(
                                    SchoolContract.TableLearners.COLUMN_SECOND_NAME
                            ))
                    );

                    //параметры текста
                    LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            3F
                    );

                    //добавляем текст в контейнер
                    learnerConteiner.addView(tempLearnerText, tempLearnerTextParams);

//------------при нажатии на контейнер ученика------------
                    learnerConteiner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (learnersAndGrades[learnerPozition].grade[0] != -2)
                                if (learnersAndGrades[learnerPozition].getGrade() != 5) {
                                    learnersAndGrades[learnerPozition].setGrade(
                                            (byte) (1 + learnersAndGrades[learnerPozition].getGrade())
                                    );
                                } else {
                                    learnersAndGrades[learnerPozition].setGrade((byte) 1);
                                }

                            switch ((int) learnersAndGrades[learnerPozition].getGrade()) {
                                case -2:
                                    tempLernerImage.setImageResource(R.drawable.learner_white);
                                    break;
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
                    });

                    learnerConteiner.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                        @Override
                        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

                            if (learnersAndGrades[learnerPozition].getGrade() != -2 && learnersAndGrades[learnerPozition].getGradesCount() < 1) {
                                contextMenu.add(0, -2, 0, "отсутствует").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_white);
                                        learnersAndGrades[learnerPozition].setGrade((byte) -2);
                                        return true;
                                    }
                                });
                            }
                            if (learnersAndGrades[learnerPozition].getGrade() != 0) {
                                contextMenu.add(0, 0, 0, "нет оценки").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_gray);
                                        learnersAndGrades[learnerPozition].setGrade((byte) 0);
                                        return true;
                                    }
                                });
                            }
                            if (learnersAndGrades[learnerPozition].getGrade() != 1) {
                                contextMenu.add(0, 1, 0, "1").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_red);
                                        learnersAndGrades[learnerPozition].setGrade((byte) 1);
                                        return true;
                                    }
                                });
                            }
                            if (learnersAndGrades[learnerPozition].getGrade() != 2) {
                                contextMenu.add(0, 2, 0, "2").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_orange);
                                        learnersAndGrades[learnerPozition].setGrade((byte) 2);
                                        return true;
                                    }
                                });
                            }
                            if (learnersAndGrades[learnerPozition].getGrade() != 3) {
                                contextMenu.add(0, 3, 0, "3").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_yellow);
                                        learnersAndGrades[learnerPozition].setGrade((byte) 3);
                                        return true;
                                    }
                                });
                            }
                            if (learnersAndGrades[learnerPozition].getGrade() != 4) {
                                contextMenu.add(0, 4, 0, "4").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_lime);
                                        learnersAndGrades[learnerPozition].setGrade((byte) 4);
                                        return true;
                                    }
                                });
                            }
                            if (learnersAndGrades[learnerPozition].getGrade() != 5) {
                                contextMenu.add(0, 5, 0, "5").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_green);
                                        learnersAndGrades[learnerPozition].setGrade((byte) 5);
                                        return true;
                                    }
                                });
                            }
                            if (learnersAndGrades[learnerPozition].getGradesCount() != 2 && learnersAndGrades[learnerPozition].getGrade() != 0 && learnersAndGrades[learnerPozition].getGrade() != -2) {
                                contextMenu.add(0, 6, 0, "новая оценка").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        tempLernerImage.setImageResource(R.drawable.learner_gray);
                                        switch (learnersAndGrades[learnerPozition].getGradesCount()) {
                                            case 0:
                                                grade1Text.setText("" + learnersAndGrades[learnerPozition].getGrade());
                                                break;
                                            case 1:
                                                grade2Text.setText("" + learnersAndGrades[learnerPozition].getGrade());
                                                break;
                                        }
                                        learnersAndGrades[learnerPozition].nextGrade();
                                        return true;
                                    }
                                });
                            }
                        }
                    });

                    learnerCursor.close();
                }
                //добавляем контейнер с текстом и картинкой в основной контейнер
                placeOut.addView(learnerConteiner, learnerConteinerParams);
                tempRelativeLayoutDesk.addView(placeOut, placeParams);
            }
            placeCursor.close();
            room.addView(tempRelativeLayoutDesk, tempRelativeLayoutDeskParams);
        }
        //размеры комнаты по самой дальней парте
        room.setLayoutParams(new LinearLayout.LayoutParams(
                (maxX + (int) dpFromPx(3000 * multiplier)),
                (maxY + (int) dpFromPx(2250 * multiplier))
        ));
        desksCursor.close();
        db.close();

//-------------------------------получение данных-----------------------------------
//        //база данных
//        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
//        //размер парт
//        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f;
//        ////заголовок
//        //setTitle("Урок");
//        //кнопка назад в actionBar
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        //поле вывода, класс
//        RelativeLayout room = (RelativeLayout) findViewById(R.id.room_layout);
//
//
//        long classId;
//        long cabinetId;
//
//        Cursor lessonCursor;//курсор с текущим уроком
//        Cursor desksCursor;//курсор с партами
//        int maxX = 0;//размеры экрана по парте
//        int maxY = 0;
//
//
//        if (getIntent().getLongExtra(LESSON_ATTITUDE_ID, -1) == -1) {//-1 ошибка, 1> использовать переданные
//            Toast toast = Toast.makeText(this, "ошибка при получении урока!", Toast.LENGTH_LONG);
//            toast.show();
//            Log.i("TeachersApp", "LessonActivity - error with intent attitudeId == 1");
//            finish();
//            return;
//        } else {
//            lessonAttitudeId = getIntent().getLongExtra(LESSON_ATTITUDE_ID, -1);
//        }
//
//        //получаем все данные о классе
//        //курсор с зависимостью
//        Cursor lessonAttitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(lessonAttitudeId);
//        lessonAttitudeCursor.moveToFirst();
//
//        //курсор с уроком
//        lessonCursor = db.getSubjectById(lessonAttitudeCursor.getLong(lessonAttitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID)));
//        lessonCursor.moveToFirst();
//
//        lessonId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_SUBJECT_ID));
//        classId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
//        cabinetId = lessonAttitudeCursor.getLong(lessonAttitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_CABINET_ID));
//
//        lessonCursor.close();
//        lessonAttitudeCursor.close();
//        //курсор с партами
//        desksCursor = db.getDesksByCabinetId(cabinetId);
//        //learnersCursor = db.getLearnersByClassId(classId);
//        //seatingCursor = db.getAttitudesByLessonId(lessonId);

////-------------------------вывод парт и учеников-------------------------------
//
//        int i = -1;//щётчик учеников
//        //чистим класс
//        room.removeAllViews();
//        while (desksCursor.moveToNext()) {
////-----------создание парты-------------
//            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
//            tempRelativeLayoutDesk.setBackgroundColor(Color.LTGRAY);
//
//            //длина парты по количеству мест
//            RelativeLayout.LayoutParams tempRelativeLayoutDeskParams =
//                    new RelativeLayout.LayoutParams((int) pxFromDp(
//                            desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES)) *
//                                    1000 * multiplier), (int) pxFromDp(1000 * multiplier));
//            tempRelativeLayoutDeskParams.leftMargin = (int) pxFromDp(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * 25 * multiplier);
//            tempRelativeLayoutDeskParams.topMargin = (int) pxFromDp(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * 25 * multiplier);
//            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//            Log.i("TeachersApp", "LessonActivity - onCreate view desk:" + desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
//
////-----------------считаем максимальную парту----------------
//            if (desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * 100 * multiplier > maxX) {
//                maxX = (int) (desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * 100 * multiplier);
//            }
//            if (desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * 100 * multiplier > maxY) {
//                maxY = (int) (desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * 100 * multiplier);
//            }
//
//
////----------------создание места-------------
//            Cursor placeCursor = db.getPlacesByDeskId(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
//            while (placeCursor.moveToNext()) {
//
////------layout с учеником и оценками------
//                RelativeLayout gradeLearnerPlaceOut = new RelativeLayout(this);
//                gradeLearnerPlaceOut.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//
//                RelativeLayout.LayoutParams tempRelativeLayoutPlaceParams = new RelativeLayout.LayoutParams((int) pxFromDp((1000 - 50) * multiplier), (int) pxFromDp((1000 - 50) * multiplier));
//                tempRelativeLayoutPlaceParams.leftMargin = (int) pxFromDp((25 + (1000 * (placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.COLUMN_ORDINAL)) - 1))) * multiplier);
//                tempRelativeLayoutPlaceParams.topMargin = (int) pxFromDp(25 * multiplier);
//                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//
//                final TextView grade1Text = new TextView(this);
//                grade1Text.setTextColor(Color.WHITE);
//                grade1Text.setTextSize(325 * multiplier);
//                grade1Text.setText("");
//
//
//                final TextView grade2Text = new TextView(this);
//                grade2Text.setTextColor(Color.WHITE);
//                grade2Text.setTextSize(325 * multiplier);
//                grade2Text.setText("");
//
//                RelativeLayout.LayoutParams grade1TextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//                grade1TextParams.addRule(RelativeLayout.ALIGN_PARENT_START);
//                grade1TextParams.setMargins((int) pxFromDp(25 * multiplier), 0, 0, 0);
//
//                RelativeLayout.LayoutParams grade2TextParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                grade2TextParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//                grade2TextParams.setMargins(0, 0, (int) pxFromDp(25 * multiplier), 0);
//
//                gradeLearnerPlaceOut.addView(grade1Text, grade1TextParams);
//                gradeLearnerPlaceOut.addView(grade2Text, grade2TextParams);
//
//
////---------------layout с учеником и текстом--------------
//                LinearLayout tempPlaceLayout = new LinearLayout(this);
//                tempPlaceLayout.setOrientation(LinearLayout.VERTICAL);
//
//                LinearLayout.LayoutParams tempPlaceLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//
//                Log.i("TeachersApp", "LessonActivity - onCreate view place:" + placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));
//
////------------создание ученика-----------
//                long learnerId = db.getLearnerIdByClassIdAndPlaceId(classId, placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));
//                if (learnerId != -1) {
//                    //оценки
//                    i++;
//                    final int tempGradeId = i;
//                    gradeArrayList.add(i, new LearnerAndGrade(learnerId));
//
//
//                    Cursor learnerCursor = db.getLearner(learnerId);//получаем ученика
//                    learnerCursor.moveToFirst();
        Log.i("", "");
////------------картинка ученика------------
//                    final ImageView tempLernerImage = new ImageView(this);
//                    tempLernerImage.setImageResource(R.drawable.learner_gray);//по умолчанию серая картинка
//                    LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1F);

//                    View.OnClickListener onClickListener = new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if (gradeArrayList.get(tempGradeId).getGrade() != -2)
//                                if (gradeArrayList.get(tempGradeId).getGrade() != 5) {
//                                    gradeArrayList.get(tempGradeId).setGrade((byte) (1 + gradeArrayList.get(tempGradeId).getGrade()));
//                                } else {
//                                    gradeArrayList.get(tempGradeId).setGrade((byte) 1);
//                                }
//
//                            switch ((int) gradeArrayList.get(tempGradeId).getGrade()) {
//                                case -2:
//                                    tempLernerImage.setImageResource(R.drawable.learner_white);
//                                    break;
////                                case 0:
////                                    tempLernerImage.setImageResource(R.drawable.learner_gray);
////                                    break;
//                                case 1:
//                                    tempLernerImage.setImageResource(R.drawable.learner_red);
//                                    break;
//                                case 2:
//                                    tempLernerImage.setImageResource(R.drawable.learner_orange);
//                                    break;
//                                case 3:
//                                    tempLernerImage.setImageResource(R.drawable.learner_yellow);
//                                    break;
//                                case 4:
//                                    tempLernerImage.setImageResource(R.drawable.learner_lime);
//                                    break;
//                                case 5:
//                                    tempLernerImage.setImageResource(R.drawable.learner_green);
//                                    break;
//                            }
//                        }
//                    };
//                    tempLernerImage.setOnClickListener(onClickListener);
//                    View.OnCreateContextMenuListener onCreateContextMenuListener = new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//
//                            if (gradeArrayList.get(tempGradeId).getGrade() != -2 && gradeArrayList.get(tempGradeId).getGradesCount() < 1) {
//                                contextMenu.add(0, -2, 0, "отсутствует").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//                                        tempLernerImage.setImageResource(R.drawable.learner_white);
//                                        gradeArrayList.get(tempGradeId).setGrade((byte) -2);
//                                        return true;
//                                    }
//                                });
//                            }
//                            if (gradeArrayList.get(tempGradeId).getGrade() != 0) {
//                                contextMenu.add(0, 0, 0, "нет оценки").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//                                        tempLernerImage.setImageResource(R.drawable.learner_gray);
//                                        gradeArrayList.get(tempGradeId).setGrade((byte) 0);
//                                        return true;
//                                    }
//                                });
//                            }
//                            if (gradeArrayList.get(tempGradeId).getGrade() != 1) {
//                                contextMenu.add(0, 1, 0, "1").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//                                        tempLernerImage.setImageResource(R.drawable.learner_red);
//                                        gradeArrayList.get(tempGradeId).setGrade((byte) 1);
//                                        return true;
//                                    }
//                                });
//                            }
//                            if (gradeArrayList.get(tempGradeId).getGrade() != 2) {
//                                contextMenu.add(0, 2, 0, "2").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//                                        tempLernerImage.setImageResource(R.drawable.learner_orange);
//                                        gradeArrayList.get(tempGradeId).setGrade((byte) 2);
//                                        return true;
//                                    }
//                                });
//                            }
//                            if (gradeArrayList.get(tempGradeId).getGrade() != 3) {
//                                contextMenu.add(0, 3, 0, "3").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//                                        tempLernerImage.setImageResource(R.drawable.learner_yellow);
//                                        gradeArrayList.get(tempGradeId).setGrade((byte) 3);
//                                        return true;
//                                    }
//                                });
//                            }
//                            if (gradeArrayList.get(tempGradeId).getGrade() != 4) {
//                                contextMenu.add(0, 4, 0, "4").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//                                        tempLernerImage.setImageResource(R.drawable.learner_lime);
//                                        gradeArrayList.get(tempGradeId).setGrade((byte) 4);
//                                        return true;
//                                    }
//                                });
//                            }
//                            if (gradeArrayList.get(tempGradeId).getGrade() != 5) {
//                                contextMenu.add(0, 5, 0, "5").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//                                        tempLernerImage.setImageResource(R.drawable.learner_green);
//                                        gradeArrayList.get(tempGradeId).setGrade((byte) 5);
//                                        return true;
//                                    }
//                                });
//                            }
//                            if (gradeArrayList.get(tempGradeId).getGradesCount() != 2 && gradeArrayList.get(tempGradeId).getGrade() != 0 && gradeArrayList.get(tempGradeId).getGrade() != -2) {
//                                contextMenu.add(0, 6, 0, "новая оценка").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                    @Override
//                                    public boolean onMenuItemClick(MenuItem menuItem) {
//                                        tempLernerImage.setImageResource(R.drawable.learner_gray);
//                                        switch (gradeArrayList.get(tempGradeId).getGradesCount()) {
//                                            case 0:
//                                                grade1Text.setText("" + gradeArrayList.get(tempGradeId).getGrade());
//                                                break;
//                                            case 1:
//                                                grade2Text.setText("" + gradeArrayList.get(tempGradeId).getGrade());
//                                                break;
//                                        }
//                                        gradeArrayList.get(tempGradeId).nextGrade();
//                                        return true;
//                                    }
//                                });
//                            }
//                        }
//                    };
//                    tempLernerImage.setOnCreateContextMenuListener(onCreateContextMenuListener);
//                    tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);
//
        Log.i("", "");
////---------------текст ученика---------------
//                    TextView tempLearnerText = new TextView(this);
//                    tempLearnerText.setOnClickListener(onClickListener);
//                    tempLearnerText.setTextSize(200 * multiplier);
//                    tempLearnerText.setOnCreateContextMenuListener(onCreateContextMenuListener);
//                    tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
//                    tempLearnerText.setTextColor(Color.WHITE);
//                    tempLearnerText.setText(learnerCursor.getString(learnerCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
//                    LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 3F);
//                    tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
//
//                    learnerCursor.close();
//                }
//                gradeLearnerPlaceOut.addView(tempPlaceLayout, tempPlaceLayoutParams);
////-----------добавление места в парту-----------
//                tempRelativeLayoutDesk.addView(gradeLearnerPlaceOut, tempRelativeLayoutPlaceParams);
//            }
//            placeCursor.close();
//
////------------добавление парты в комнату---------------
//            room.addView(tempRelativeLayoutDesk, tempRelativeLayoutDeskParams);
//        }
//        room.setLayoutParams(new LinearLayout.LayoutParams(
//                (maxX + (int) dpFromPx(3000 * multiplier)),
//                (maxY + (int) dpFromPx(2250 * multiplier))
//        ));
//        desksCursor.close();
//        db.close();
    }

//----------------------------------------функциональные кнопки-------------------------------------

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //кнопка назад в actionBar
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //обнуляем данные
        lessonAttitudeId = -2;
        subjectId = 0;
        learnersClassId = 0;
        cabinetId = 0;
        lessonName = null;
        LearnerAndGrade[] learnersAndGrades = null;

        super.onBackPressed();
    }


    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;

    }

}

//--------------------------------класс для хранения данных-----------------------------------------
class LearnerAndGrade {
    long learnerId;
    private byte gradesCount = 0;//здесь количество с нуля!
    byte[] grade = new byte[3];

    LearnerAndGrade(long learnerId) {
        this.learnerId = learnerId;

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
