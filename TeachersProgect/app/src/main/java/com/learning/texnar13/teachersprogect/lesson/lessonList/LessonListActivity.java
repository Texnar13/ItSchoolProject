package com.learning.texnar13.teachersprogect.lesson.lessonList;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.LearnersAndGradesOut.EditLearnerDialogFragment;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;
import com.learning.texnar13.teachersprogect.lesson.LessonActivity;

import java.text.SimpleDateFormat;

public class LessonListActivity extends AppCompatActivity implements EditGradeDialogInterface {

    public static final String ATTITUDE_ID = "attitudeId";
    public static final String SUBJECT_ID = "subjectId";
    public static final String LIST_ID = "listId";
    public static final String FIRST_LIST_GRADES = "listGrades1";
    public static final String SECOND_LIST_GRADES = "listGrades2";
    public static final String THIRD_LIST_GRADES = "listGrades3";

    //---полученные---
    //массив учеников и оценок
    static LearnerGradeUnit[] learnersGrades;
    //id зависимости
    static long attitudeId = -1;
    //id предмета
    static long lessonId;

    //---созданные---
    //поле вывода
    LinearLayout list;

    //тост выводящийся после сохранения оценок
    Toast toast;

    //id учеников
//    long[] learnersIdArray;
//    //массив оценок: номер оценки, оценка
//    long[][] gradeArray = new long[3][];


//--------------------------------------методы диалога----------------------------------------------

    @Override
    public void editGrade(int learnerNumber, int gradeNumber, int grade) {
        //меняем массив
        learnersGrades[learnerNumber].grades[gradeNumber] = grade;
        //ставим оценку
        if (grade == 0) {
            ((TextView) learnersGrades[learnerNumber].view[gradeNumber]).setText("-");
        } else {
            if (grade != -2) {
                ((TextView) learnersGrades[learnerNumber].view[gradeNumber]).setText("" + grade);
            } else {
                ((TextView) learnersGrades[learnerNumber].view[gradeNumber]).setText("Н");
            }
        }
    }


//----------------------------------------меню сверху-----------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_list_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.lesson_list_menu_save).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.i("TeachersApp", "LessonListActivity - onPrepareOptionsMenu - save");
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                //получаем id класса
                Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(attitudeId);
                //если не найдена зависимость
                if (attitudeCursor.getCount() == 0) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Не передан урок. Оценки не сохранены", Toast.LENGTH_LONG);
                    toast.show();
                    Log.e("TeachersApp", " attitudeId = " + attitudeId + "; attitudeCursor is empty");
                    finish();
                    return false;
                }
                attitudeCursor.moveToFirst();
                String lessonTime = attitudeCursor.getString(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.COLUMN_DATE_BEGIN));
                attitudeCursor.close();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                //сохранить оценки
                for (int i = 0; i < learnersGrades.length; i++) {
                    if (learnersGrades[i].grades[0] != 0) {
                        db.createGrade(learnersGrades[i].learnerId, learnersGrades[i].grades[0], lessonId, getIntent().getStringExtra(LessonActivity.LESSON_TIME));
                    }
                    if (learnersGrades[i].grades[1] != 0) {
                        db.createGrade(learnersGrades[i].learnerId, learnersGrades[i].grades[1], lessonId, getIntent().getStringExtra(LessonActivity.LESSON_TIME));
                    }
                    if (learnersGrades[i].grades[2] != 0) {
                        db.createGrade(learnersGrades[i].learnerId, learnersGrades[i].grades[2], lessonId, getIntent().getStringExtra(LessonActivity.LESSON_TIME));
                    }
                    toast.show();
                }
                db.close();
                finish();
                //очистка данных
                attitudeId = -1;
                return true;
            }
        });

        return true;
    }

//---------------------------------------создание активности----------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);
        //поле вывода данных
        list = (LinearLayout) findViewById(R.id.activity_lesson_list);


        toast = Toast.makeText(getApplicationContext(), "все оценки успешно сохранены!", Toast.LENGTH_SHORT);


        // проверка на заполнение
        if (attitudeId == -1) {
//--получение данных--
            attitudeId = getIntent().getLongExtra(ATTITUDE_ID, -1);
            lessonId = getIntent().getLongExtra(SUBJECT_ID, -1);

//--массив с учениками и оценками--

            //id учеников из базы
            long[] learnersIdArray = getIntent().getLongArrayExtra(LIST_ID);

            //инициализация
            learnersGrades = new LearnerGradeUnit[learnersIdArray.length];

            //оценки
            long gradeArray0[] = getIntent().getLongArrayExtra(FIRST_LIST_GRADES);
            long gradeArray1[] = getIntent().getLongArrayExtra(SECOND_LIST_GRADES);
            long gradeArray2[] = getIntent().getLongArrayExtra(THIRD_LIST_GRADES);

            for (int i = 0; i < learnersIdArray.length; i++) {
                //помещаем id
                learnersGrades[i] = new LearnerGradeUnit(learnersIdArray[i]);
                //помещаем оценки
                learnersGrades[i].grades = new long[3];
                learnersGrades[i].grades[0] = gradeArray0[i];
                learnersGrades[i].grades[1] = gradeArray1[i];
                learnersGrades[i].grades[2] = gradeArray2[i];
            }
        }
//---------------------------------------вывод оценок-----------------------------------------------

//
//        //куда выводим все
//        LinearLayout list = (LinearLayout) findViewById(R.id.activity_lesson_list);
//
//        //--получение данных--
//        attitudeId = getIntent().getLongExtra(ATTITUDE_ID, -1);
//        lessonId = getIntent().getLongExtra(SUBJECT_ID, -1);
//        //массивы
//        learnersIdArray = getIntent().getLongArrayExtra(LIST_ID);
//        gradeArray[0] = getIntent().getLongArrayExtra(FIRST_LIST_GRADES);
//        gradeArray[1] = getIntent().getLongArrayExtra(SECOND_LIST_GRADES);
//        gradeArray[2] = getIntent().getLongArrayExtra(THIRD_LIST_GRADES);
//
//        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
//        boolean flag = false;
//        for (int i = 0; i < gradeArray[0].length; i++) {//пробегаясь по длинне побочных массивов вывожу из главных оценки
//            //контейнер имя оценка
//            LinearLayout tempLayout = new LinearLayout(getApplicationContext());
//
//            //выводим ученика
////--------текстовое поле с именем ученика----------
//            //создаем
//            TextView textViewWithName = new TextView(getApplicationContext());
//            textViewWithName.setGravity(Gravity.CENTER_HORIZONTAL);
//            textViewWithName.setTextColor(Color.GRAY);
//            //раскрашиваем
//            if (flag) {
//                textViewWithName.setBackgroundColor(Color.parseColor("#fbe2e7"));
//            }
//            textViewWithName.setTextSize(30F);
//            //достаем имя ученика
//            Cursor currentLearner = db.getLearner(learnersIdArray[i]);
//            currentLearner.moveToFirst();
//            //ставим его
//            textViewWithName.setText(currentLearner.getString(currentLearner.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME))
//                    + " " + currentLearner.getString(currentLearner.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)));
//            currentLearner.close();
//            //помещаем имя ученика в контейнер
//            tempLayout.addView(textViewWithName, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F));
//            //пробегаемся по его оценкам
//            for (int j = 0; j < gradeArray.length; j++) {
//                //создаем текстовое поле с оценкой
//                TextView textViewWisGrade = new TextView(getApplicationContext());
//                textViewWisGrade.setTextColor(Color.GRAY);
//                //раскрашиваем
//                if (!flag) {
//                    textViewWisGrade.setBackgroundColor(Color.parseColor("#fbe2e7"));
//                    flag = true;
//                } else {
//                    flag = false;
//                }
//                textViewWisGrade.setTextSize(30F);
//                textViewWisGrade.setGravity(Gravity.CENTER_HORIZONTAL);
//                //ставим в поле оценку
//                if (gradeArray[j][i] == 0) {
//                    textViewWisGrade.setText("-");
//                } else {
//                    if (gradeArray[j][i] != -2) {
//                        textViewWisGrade.setText(Long.toString(gradeArray[j][i]));
//                    } else {
//                        textViewWisGrade.setText("Н");
//                    }
//                }
//                //помещаем текст с оценкой в контейнер
//                tempLayout.addView(textViewWisGrade, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4F));
//            }
//            //помещаем контейнер в список
//            list.addView(tempLayout);
//        }
//        db.close();
    }

//--------------------------------------пробуждение активности--------------------------------------

    @Override
    protected void onStart() {
        //заголовок
        setTitle("Итоги урока");

//---------------------------------------вывод оценок-----------------------------------------------

        //чистим поле
        list.removeAllViews();

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        boolean flag = false;
//--------идем по ученикам--------
        for (int i = 0; i < learnersGrades.length; i++) {
//----контейнер имя оценка----
            LinearLayout tempLayout = new LinearLayout(getApplicationContext());

//----текстовое поле с именем ученика----
            //создаем
            TextView textViewWithName = new TextView(getApplicationContext());
            textViewWithName.setGravity(Gravity.CENTER_HORIZONTAL);
            textViewWithName.setTextColor(Color.GRAY);

            //раскрашиваем
            if (flag) {
                textViewWithName.setBackgroundColor(Color.parseColor("#fbe2e7"));
            }
            textViewWithName.setTextSize(30F);
//---достаем имя ученика---
            Cursor currentLearner = db.getLearner(learnersGrades[i].learnerId);
            currentLearner.moveToFirst();
            //ставим его
            textViewWithName.setText(
                    currentLearner.getString(currentLearner.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_SECOND_NAME
                    )) + " " + currentLearner.getString(currentLearner.getColumnIndex(
                            SchoolContract.TableLearners.COLUMN_FIRST_NAME
                    ))
            );
            currentLearner.close();
//----помещаем текст с именем ученика в контейнер----
            tempLayout.addView(
                    textViewWithName,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1F
                    )
            );

//--------пробегаемся по его оценкам--------
            //обьявляем
            learnersGrades[i].view = new View[learnersGrades[i].grades.length];
            for (int j = 0; j < learnersGrades[i].grades.length; j++) {
                //создаем текстовое поле с оценкой
                TextView textViewWisGrade = new TextView(getApplicationContext());
                textViewWisGrade.setTextColor(Color.GRAY);

                //добавляем текстовое поле в массив
                learnersGrades[i].view[j] = textViewWisGrade;

                //раскрашиваем
                if (!flag) {
                    textViewWisGrade.setBackgroundColor(Color.parseColor("#fbe2e7"));
                    flag = true;
                } else {
                    flag = false;
                }
                textViewWisGrade.setTextSize(30F);
                textViewWisGrade.setGravity(Gravity.CENTER_HORIZONTAL);
//----ставим в поле оценку----
                if (learnersGrades[i].grades[j] == 0) {
                    textViewWisGrade.setText("-");
                } else {
                    if (learnersGrades[i].grades[j] != -2) {
                        textViewWisGrade.setText(Long.toString(learnersGrades[i].grades[j]));
                    } else {
                        textViewWisGrade.setText("Н");
                    }
                }
//-------задаем действие при нажатии на оценку-------
                final int finalI = i;
                final int finalJ = j;
                textViewWisGrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //передаваемые данные
                        Bundle bundle = new Bundle();
                        bundle.putInt(LessonListEditDialogFragment.LEARNER_NUMBER, finalI);
                        bundle.putInt(LessonListEditDialogFragment.GRADE_NUMBER, finalJ);
                        bundle.putInt(LessonListEditDialogFragment.GRADE, (int) learnersGrades[finalI].grades[finalJ]);
                        //диалог
                        LessonListEditDialogFragment editDialog = new LessonListEditDialogFragment();
                        editDialog.setArguments(bundle);
                        editDialog.show(getFragmentManager(), "editGradeDialog");
                    }
                });


//-------помещаем текст с оценкой в контейнер-------
                tempLayout.addView(
                        textViewWisGrade,
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                4F
                        )
                );
            }
//-------помещаем контейнер в список-------
            list.addView(tempLayout);
        }

//-------подсказка textView в низу-------
        //текст
        TextView textView = new TextView(this);
        textView.setTextColor(Color.GRAY);
        textView.setTextSize(20);
        textView.setText("Здесь выводятся результаты урока. Чтобы изменить оценку нажмите на нее. Для сохранения оценок нажмите кнопку сохранить вверху. Если не хотите сохранять оценки нажмите кнопку назад.");
        //параметры
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins((int) pxFromDp(20), (int) pxFromDp(10), (int) pxFromDp(20), (int) pxFromDp(10));

        list.addView(textView, params);


        db.close();

        super.onStart();
    }

//--------перед выходом---------

    @Override
    public void onBackPressed() {
        //очистка данных
        attitudeId = -1;
        super.onBackPressed();
    }

//---------форматы----------

    private float pxFromDp(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px) {
        return px / getApplicationContext().getResources().getDisplayMetrics().density;
    }
}
