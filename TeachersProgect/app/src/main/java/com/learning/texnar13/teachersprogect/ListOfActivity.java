package com.learning.texnar13.teachersprogect;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

public class ListOfActivity extends AppCompatActivity {

    public static final String LIST_PARAMETER = "listParameter";
    public static final String DOP_LIST_PARAMETER = "dopListParameter";

    FloatingActionButton fab;
    String listParameterValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listParameterValue = getIntent().getStringExtra(LIST_PARAMETER);
        final LinearLayout out = (LinearLayout) findViewById(R.id.content_list_of_classes_out);
        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                intent = new Intent(ListOfActivity.this, RedactorActivity.class); //переход на редактор
                switch (listParameterValue) {
                    case SchoolContract.TableClasses.NAME_TABLE_CLASSES://создание классов
                        intent.putExtra(RedactorActivity.EDITED_OBJECT, SchoolContract.TableClasses.NAME_TABLE_CLASSES);//говорим редактору тип обьекта
                        intent.putExtra(RedactorActivity.EDITED_OBJECT_ID, RedactorActivity.OBJECT_NEW);//говорим редактору что обьект новый
                        break;
                    case SchoolContract.TableLearners.NAME_TABLE_LEARNERS://создание учеников
                        intent.putExtra(RedactorActivity.EDITED_OBJECT, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//говорим редактору тип обьекта
                        intent.putExtra(RedactorActivity.EDITED_OBJECT_ID, RedactorActivity.OBJECT_NEW);//говорим редактору что обьект новый

                        break;
                    case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
                        intent.putExtra(RedactorActivity.EDITED_OBJECT, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);//говорим редактору тип обьекта
                        intent.putExtra(RedactorActivity.EDITED_OBJECT_ID, RedactorActivity.OBJECT_NEW);//говорим редактору что обьект новый

                    default:
                        Log.wtf("ListOfActivity", "in fab, listParameterValue is default!");
                }
                startActivity(intent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        Button tempButtonForList;
        ViewGroup.LayoutParams tempParamsForButtonForList = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//параметры кнопки
        final Cursor cursor;//будущий курсор с обьектами вывод;
        switch (listParameterValue) {//вибираем содержимое списка
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:
                cursor = db.getClasses();//получаем классы

                Log.i("ListOfActivity", "out classes");
                while (cursor.moveToNext()) {//проходимся по классам
                    tempButtonForList = new Button(this);//кнопка которая будет вставляться в список
                    final long classId = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));// мы не можем использовать эту переменную в onClick, та как курсор закрывается а на кнопку мы нажимаем уже потом

                    tempButtonForList.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));//устанавливаем на кнопку имя класса
                    tempButtonForList.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent;
                            intent = new Intent(ListOfActivity.this, ListOfActivity.class);//запуск этого активити заново
                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//но с учениками
                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER,classId);//передаём id выбранного класса
                            startActivity(intent);
                        }
                    });
                    out.addView(tempButtonForList, tempParamsForButtonForList);//добавляем эту кнопку
                    Log.i("ListOfActivity", "Classes: add button "+classId);
                }
                cursor.close();
                break;
            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS:
                cursor = db.getLearnersByClassId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));//получаем учеников по умолчанию по первому классу
                Log.i("ListOfActivity", "out learners");
                while (cursor.moveToNext()) {//проходимся по ученикам
                    tempButtonForList = new Button(this);//кнопка которая будет вставляться в список
                    final long learnerId = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID));

                    tempButtonForList.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)) + " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));//устанавливаем на кнопку имя ученика
                    tempButtonForList.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent;
                            intent = new Intent(ListOfActivity.this, RedactorActivity.class);//запуск этого активити заново
                            intent.putExtra(RedactorActivity.EDITED_OBJECT, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//редактируем ученика
                            intent.putExtra(RedactorActivity.EDITED_OBJECT_ID, learnerId);//передаём id выбранного ученика
                            startActivity(intent);
                        }
                    });
                    out.addView(tempButtonForList, tempParamsForButtonForList);//добавляем эту кнопку
                    Log.i("ListOfActivity", "Learners: add button");
                }
                cursor.close();
                break;
            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
                cursor = db.getCabinets();//получаем кабинеты
                Log.i("ListOfActivity", "out cabinets");
                while (cursor.moveToNext()) {//проходимся по кабинетам
                    tempButtonForList = new Button(this);//кнопка которая будет вставляться в список
                    final long cabinetId = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID));

                    tempButtonForList.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)));//устанавливаем на кнопку имя кабинета
                    tempButtonForList.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent;
                            intent = new Intent(ListOfActivity.this, RedactorActivity.class);//запуск этого активити заново
                            intent.putExtra(RedactorActivity.EDITED_OBJECT, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);//редактируем кабинет
                            intent.putExtra(RedactorActivity.EDITED_OBJECT_ID, cabinetId);//передаём id выбранного ученика
                            startActivity(intent);
                        }
                    });
                    out.addView(tempButtonForList, tempParamsForButtonForList);//добавляем эту кнопку
                    Log.i("ListOfActivity", "cabinets: add button");
                }
                cursor.close();
                break;
            default:
                Log.wtf("ListOfActivity", "in out, listParameterValue is default!");
                break;
        }
        db.close();//закрыли базу данных
    }

}

/*
* Log.i("ListOfActivity", "out learners, classId: " + cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID)));
                    out.removeAllViews();
                    final Cursor learnersCursor = db.getLearnersByClassId(cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID)));
                    while (learnersCursor.moveToNext()) {
                        Button tempLearnerButton = new Button(ListOfActivity.this);
                        temp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i("ListOfActivity", "chose learner: " + learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
                            }
                        });
                        ViewGroup.LayoutParams tempParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        out.addView(tempLearnerButton, tempParams);
                    }
                    learnersCursor.close();
*
 *
  *
  *
  */

