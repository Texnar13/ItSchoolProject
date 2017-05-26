package com.learning.texnar13.teachersprogect.lesson;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

public class LessonListActivity extends AppCompatActivity {

    public static final String LIST_ID = "listId";
    public static final String LIST_GRADES = "listGrades";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        getSupportActionBar().setTitle("итоги урока");

        LinearLayout list = (LinearLayout) findViewById(R.id.activity_lesson_list);

        long[] learnersIdArray = getIntent().getLongArrayExtra(LIST_ID);
        long[] gradeArray = getIntent().getLongArrayExtra(LIST_GRADES);

        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
        boolean flag = false;
        for (int j = 0; j < gradeArray.length; j++) {
            LinearLayout tempLayout = new LinearLayout(getApplicationContext());//контейнер имя оценка

            TextView textViewWisName = new TextView(getApplicationContext());//имя
            textViewWisName.setGravity(Gravity.CENTER_HORIZONTAL);
            textViewWisName.setTextColor(Color.GRAY);
            if (flag) {
                textViewWisName.setBackgroundColor(Color.parseColor("#d8d5ff"));
            }
            textViewWisName.setTextSize(30F);
            Cursor currentLearner = db.getLearner(learnersIdArray[j]);
            currentLearner.moveToFirst();
            textViewWisName.setText(currentLearner.getString(currentLearner.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME))
                    + " " + currentLearner.getString(currentLearner.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)));
            currentLearner.close();
            tempLayout.addView(textViewWisName, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F));

            TextView textViewWisGrade = new TextView(getApplicationContext());//оценка
            textViewWisGrade.setTextColor(Color.GRAY);
            if (!flag) {
                textViewWisGrade.setBackgroundColor(Color.parseColor("#d8d5ff"));
                flag = true;
            }else{
                flag = false;
            }
            textViewWisGrade.setTextSize(30F);
            textViewWisGrade.setGravity(Gravity.CENTER_HORIZONTAL);
            textViewWisGrade.setText(Long.toString(gradeArray[j]));
            tempLayout.addView(textViewWisGrade, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4F));


            list.addView(tempLayout);
        }
    }
}
