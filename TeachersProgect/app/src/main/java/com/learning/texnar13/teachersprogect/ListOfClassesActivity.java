package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

public class ListOfClassesActivity extends AppCompatActivity {

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(this,Lear); String Name = getIntent().getStringExtra("name");
                //intent.putExtra();//TODO!!!!!!!

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final LinearLayout out = (LinearLayout) findViewById(R.id.content_list_of_classes_out);

        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        final Cursor cursor = db.getClasses();
        Log.i("ListOfClassesActivity", "out classes");
        while (cursor.moveToNext()) {
            final Button temp = new Button(this);
            temp.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
            temp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("ListOfClassesActivity", "out learners, classId: " + cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID)));
                    out.removeAllViews();
                    final Cursor learnersCursor = db.getLearnersByClassId(cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID)));
                    while (learnersCursor.moveToNext()) {
                        Button tempLearnerButton = new Button(ListOfClassesActivity.this);
                        temp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i("ListOfClassesActivity", "chose learner: " + learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
                            }
                        });
                        ViewGroup.LayoutParams tempParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        out.addView(tempLearnerButton, tempParams);
                    }
                    learnersCursor.close();
                }
            });
            ViewGroup.LayoutParams tempParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            out.addView(temp, tempParams);
        }
        cursor.close();
        db.close();//закрыли базу данных
    }

}
