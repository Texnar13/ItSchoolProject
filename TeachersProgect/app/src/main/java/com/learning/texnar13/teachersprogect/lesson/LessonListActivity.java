package com.learning.texnar13.teachersprogect.lesson;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.text.SimpleDateFormat;

public class LessonListActivity extends AppCompatActivity {

    public static final String ATTITUDE_ID = "attitudeId";
    public static final String LESSON_ID = "lessonId";
    public static final String LIST_ID = "listId";
    public static final String FIRST_LIST_GRADES = "listGrades1";
    public static final String SECOND_LIST_GRADES = "listGrades2";
    public static final String THIRD_LIST_GRADES = "listGrades3";

    long[] learnersIdArray;
    long[][] gradeArray = new long[3][];
    Toast toast;
    long attitudeId;
    long lessonId;

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
                Log.i("TeachersApp", "LessonListActivity - onPrepareOptionsMenu - onMenuItemClick");
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                //получаем id ласса
//                Cursor attitudeCursor = db.getLessonById(lessonId);
//                attitudeCursor.moveToFirst();
//                long lessonTime = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableLessonAndTimeWithCabinet.COLUMN_DATE_BEGIN));
//                attitudeCursor.close();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                for (int i = 0; i < learnersIdArray.length; i++) {
//                    if (gradeArray[0][i] != 0) {
//                        db.createGrade(learnersIdArray[i], gradeArray[0][i], lessonId, dateFormat.format(new Date(lessonTime)));//todo id урока
//                    }
//                    if (gradeArray[1][i] != 0) {
//                        db.createGrade(learnersIdArray[i], gradeArray[1][i], lessonId, dateFormat.format(new Date(lessonTime)));
//                    }
//                    if (gradeArray[2][i] != 0) {
//                        db.createGrade(learnersIdArray[i], gradeArray[2][i], lessonId, dateFormat.format(new Date(lessonTime)));
//                    }
//                    toast.show();
                }
                finish();
                return true;
            }
        });

        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        toast = Toast.makeText(this, "все оценки успешно сохранены!", Toast.LENGTH_LONG);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        setTitle("Итоги урока");

        LinearLayout list = (LinearLayout) findViewById(R.id.activity_lesson_list);

        attitudeId = getIntent().getLongExtra(ATTITUDE_ID,-1);
        lessonId = getIntent().getLongExtra(LESSON_ID,-1);
        learnersIdArray = getIntent().getLongArrayExtra(LIST_ID);
        gradeArray[0] = getIntent().getLongArrayExtra(FIRST_LIST_GRADES);
        gradeArray[1] = getIntent().getLongArrayExtra(SECOND_LIST_GRADES);
        gradeArray[2] = getIntent().getLongArrayExtra(THIRD_LIST_GRADES);

        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        boolean flag = false;
        for (int i = 0; i < gradeArray[0].length; i++) {//робегаясь по длинне побочных массивов вывожу из главных оценки
            LinearLayout tempLayout = new LinearLayout(getApplicationContext());//контейнер имя оценка

            //выводим ученика
            TextView textViewWithName = new TextView(getApplicationContext());//имя
            textViewWithName.setGravity(Gravity.CENTER_HORIZONTAL);
            textViewWithName.setTextColor(Color.GRAY);
            if (flag) {
                textViewWithName.setBackgroundColor(Color.parseColor("#fbe2e7"));
            }
            textViewWithName.setTextSize(30F);
            Cursor currentLearner = db.getLearner(learnersIdArray[i]);
            currentLearner.moveToFirst();
            textViewWithName.setText(currentLearner.getString(currentLearner.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME))
                    + " " + currentLearner.getString(currentLearner.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)));
            currentLearner.close();
            tempLayout.addView(textViewWithName, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F));

            for (int j = 0; j < gradeArray.length; j++) {

                TextView textViewWisGrade = new TextView(getApplicationContext());//оценка
                textViewWisGrade.setTextColor(Color.GRAY);
                if (!flag) {
                    textViewWisGrade.setBackgroundColor(Color.parseColor("#fbe2e7"));
                    flag = true;
                } else {
                    flag = false;
                }
                textViewWisGrade.setTextSize(30F);
                textViewWisGrade.setGravity(Gravity.CENTER_HORIZONTAL);
                if (gradeArray[j][i] == 0) {
                    textViewWisGrade.setText("-");
                } else {
                    if(gradeArray[j][i] != -2){
                        textViewWisGrade.setText(Long.toString(gradeArray[j][i]));
                    }else{
                        textViewWisGrade.setText("Н");
                    }

                }
                tempLayout.addView(textViewWisGrade, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4F));
            }
            list.addView(tempLayout);
        }
        db.close();
    }
}
