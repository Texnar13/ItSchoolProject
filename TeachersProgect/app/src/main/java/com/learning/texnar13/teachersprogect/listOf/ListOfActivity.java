package com.learning.texnar13.teachersprogect.listOf;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

interface AbleToChangeTheEditMenu {
    void editIsEditMenuVisible(boolean isEditMenuVisible);
}

public class ListOfActivity extends AppCompatActivity implements AbleToChangeTheEditMenu {//todo0 закрыть все курсоры и базы данных

    public static final String LIST_PARAMETER = "listParameter";
    public static final String DOP_LIST_PARAMETER = "dopListParameter";

    FloatingActionButton fab;
    String listParameterValue;

    boolean isEditMenuVisible = false;
    ListOfAdapter adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_of_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.i("TeachersApp", "ListOfActivity - onPrepareOptionsMenu");
        menu.setGroupVisible(R.id.list_of_menu_group, isEditMenuVisible);
        menu.findItem(R.id.list_of_menu_delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //реализовываем в адаптере получение списка чёкнутых и скармливаем базе данных
                switch (getIntent().getStringExtra(LIST_PARAMETER)) {//todo0 -1 при удалении, и в диалог
                    case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.deleteClasses(adapter.getIdCheckedListOfAdapterObjects());
                        db.close();
                        break;
                    }
                    case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.deleteLearners(adapter.getIdCheckedListOfAdapterObjects());
                        db.close();
                        break;
                    }
                    case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.deleteCabinets(adapter.getIdCheckedListOfAdapterObjects());
                        db.close();
                        break;
                    }
                    case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES: {
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.deleteSchedules(adapter.getIdCheckedListOfAdapterObjects());
                        db.close();
                        break;
                    }
                    case SchoolContract.TableLessons.NAME_TABLE_LESSONS: {
                        DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                        db.deleteLessons(adapter.getIdCheckedListOfAdapterObjects());
                        db.close();
                        break;
                    }
                }
//                ListOfDialog dialog = new ListOfDialog();
//                dialog.objectParameter = SchoolContract.TableClasses.NAME_TABLE_CLASSES;
//                dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects();
//                dialog.show(getFragmentManager(), "dialogDeleteClass");
                return true;
            }
        });
        menu.findItem(R.id.list_of_menu_rename).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.i("TeachersApp", "ListOfActivity - onPrepareOptionsMenu renameId =" + adapter.getIdCheckedListOfAdapterObjects());
                ListOfDialog dialog = new ListOfDialog();
                dialog.objectParameter = getIntent().getStringExtra(LIST_PARAMETER);
                if (getIntent().getStringExtra(LIST_PARAMETER).equals(SchoolContract.TableLearners.NAME_TABLE_LEARNERS)) {
                    dialog.parentId = getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1);
                }
                if (getIntent().getStringExtra(LIST_PARAMETER).equals(SchoolContract.TableLessons.NAME_TABLE_LESSONS)) {
                    dialog.parentId = getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1);
                }
                dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects();
                dialog.show(getFragmentManager(), "dialogEdit");

//                new DataBaseOpenHelper(getApplicationContext()).setClassesNames(adapter.getIdCheckedListOfAdapterObjects(), "2z");


                //вызываем диалог, ставим имя и обновляем из диалога
                //добавить обновление списка скорее всего через общий метод, хотя не факт возможно это мне пришло в голову лишь для удобства
                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void editIsEditMenuVisible(boolean isEditMenuVisible) {
        this.isEditMenuVisible = isEditMenuVisible;
        Log.i("TeachersApp", "ListOfActivity - editIsEditMenuVisible=" + isEditMenuVisible);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listParameterValue = getIntent().getStringExtra(LIST_PARAMETER);
        //final LinearLayout out = (LinearLayout) findViewById(R.id.content_list_of_classes_out);
        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListOfDialog dialog = new ListOfDialog();
                dialog.objectParameter = listParameterValue;
                switch (listParameterValue) {
                    case SchoolContract.TableClasses.NAME_TABLE_CLASSES://создание классов
                        dialog.show(getFragmentManager(), "dialogNewClass");
                        break;
                    case SchoolContract.TableLearners.NAME_TABLE_LEARNERS://создание учеников
                        dialog.parentId = getIntent().getLongExtra(DOP_LIST_PARAMETER, -1);
                        dialog.show(getFragmentManager(), "dialogNewLearner");
                        break;
                    case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
                        dialog.show(getFragmentManager(), "dialogNewCabinet");
                        break;
                    case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES:
                        dialog.show(getFragmentManager(), "dialogNewSchedule");
                        break;
                    case SchoolContract.TableLessons.NAME_TABLE_LESSONS:
                        dialog.parentId = getIntent().getLongExtra(DOP_LIST_PARAMETER, -1);
                        dialog.show(getFragmentManager(), "dialogNewLesson");
                        break;
                    default:
                        Log.wtf("TeachersApp", "ListOfActivity - in fab, listParameterValue is " + listParameterValue);
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        //Button tempButtonForList;
        //ViewGroup.LayoutParams tempParamsForListButton = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//параметры кнопки
        ListView listView = (ListView) findViewById(R.id.content_list_of_list_view);
        Cursor cursor;//будущий курсор с обьектами вывода;
        switch (listParameterValue) {//вибираем содержимое списка
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
                getSupportActionBar().setTitle("мои классы");//ставим заголовок
                cursor = db.getClasses();//получаем классы
                ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<ListOfAdapterObject>();//создаём лист с классами
                while (cursor.moveToNext()) {//курсор в лист
                    listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)), SchoolContract.TableClasses.NAME_TABLE_CLASSES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));
                }
                this.adapter = new ListOfAdapter(this, listOfClasses, false, SchoolContract.TableClasses.NAME_TABLE_CLASSES);//создаём адаптер
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp", "ListOfActivity - out classes");
//                while (cursor.moveToNext()) {//проходимся по классам
//                    tempButtonForList = new Button(this);//кнопка которая будет вставляться в список
//                    final long classId = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID));// мы не можем использовать эту переменную в onClick, та как курсор закрывается а на кнопку мы нажимаем уже потом
//
//                    //tempButtonForList.setBackgroundResource(R.style.Widget_AppCompat_Button_Borderless);  style="?android:attr/borderlessButtonStyle"
//                    tempButtonForList.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));//устанавливаем на кнопку имя класса
//                    tempButtonForList.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//
//                            Intent intent;
//                            intent = new Intent(ListOfActivity.this, ListOfActivity.class);//запуск этого активити заново
//                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//но с учениками
//                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, classId);//передаём id выбранного класса
//                            startActivity(intent);
//
//                        }
//                    });
//                    tempButtonForList.setOnLongClickListener(new View.OnLongClickListener() {
//                        @Override
//                        public boolean onLongClick(View view) {
//
//                            ListOfDialog dialog = new ListOfDialog();
//                            dialog.objectParameter = listParameterValue;
//                            dialog.objectsId = classId;
//                            dialog.show(getFragmentManager(), "dialogEditClass");
//                            return true;
//                        }
//                    });
//
//                    out.addView(tempButtonForList, tempParamsForListButton);//добавляем эту кнопку
//                    Log.i("ListOfActivity", "Classes: add button " + classId);
//                }
                cursor.close();
            }
            break;
            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
                //ставим заголовок
                Cursor tempCursor = db.getClasses(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
                tempCursor.moveToFirst();
                getSupportActionBar().setTitle("ученики в классе " + tempCursor.getString(tempCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
                tempCursor.close();
                cursor = db.getLearnersByClassId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));//получаем учеников в курсоре по id класса (по умолчанию по первому классу)
                ArrayList<ListOfAdapterObject> listOfLearners = new ArrayList<ListOfAdapterObject>();//создаём лист с классами
                while (cursor.moveToNext()) {//курсор в лист
                    listOfLearners.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)), SchoolContract.TableLearners.NAME_TABLE_LEARNERS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID))));
                }
                this.adapter = new ListOfAdapter(this, listOfLearners, false, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//создаём адаптер со списком учеников
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp ", "ListOfActivity - out learners");
//            while (cursor.moveToNext()) {//проходимся по ученикам
//                tempButtonForList = new Button(this);//кнопка которая будет вставляться в список
//                final long learnerId = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID));
//
//                tempButtonForList.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)) + " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));//устанавливаем на кнопку имя ученика
//                tempButtonForList.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Intent intent;
//                        intent = new Intent(ListOfActivity.this, CabinetRedactorActivity.class);//запуск редактора
//                        intent.putExtra(CabinetRedactorActivity.EDITED_OBJECT_TYPE, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//редактируем ученика
//                        intent.putExtra(CabinetRedactorActivity.EDITED_OBJECT_ID, learnerId);//передаём id выбранного ученика
//                        startActivity(intent);
//                    }
//                });
//                out.addView(tempButtonForList, tempParamsForListButton);//добавляем эту кнопку
//                Log.i("TeachersApp", "ListOfActivity - Learners: add button");
//            }
                cursor.close();
                break;
            }
            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
                getSupportActionBar().setTitle("мои кабинеты");//ставим заголовок
                cursor = db.getCabinets();//получаем кабинеты
                ArrayList<ListOfAdapterObject> listOfCabinets = new ArrayList<ListOfAdapterObject>();//создаём лист с классами
                while (cursor.moveToNext()) {//курсор в лист
                    listOfCabinets.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
                }
                this.adapter = new ListOfAdapter(this, listOfCabinets, false, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);//создаём адаптер
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp", "ListOfActivity - out cabinets");
//                while (cursor.moveToNext()) {//проходимся по кабинетам
//                    tempButtonForList = new Button(this);//кнопка которая будет вставляться в список
//                    final long cabinetId = cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID));
//
//                    tempButtonForList.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)));//устанавливаем на кнопку имя кабинета
//                    tempButtonForList.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Intent intent;
//                            intent = new Intent(ListOfActivity.this, CabinetRedactorActivity.class);//редактируем класс
//                            intent.putExtra(CabinetRedactorActivity.EDITED_OBJECT_TYPE, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);//редактируем кабинет
//                            intent.putExtra(CabinetRedactorActivity.EDITED_OBJECT_ID, cabinetId);//передаём id выбранного ученика
//                            startActivity(intent);
//                        }
//                    });
//                    out.addView(tempButtonForList, tempParamsForListButton);//добавляем эту кнопку
//                    Log.i("TeachersApp", "ListOfActivity - cabinets: add button");
//                }
                cursor.close();
                break;
            case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES:
                getSupportActionBar().setTitle("мои расписания");//ставим заголовок
                cursor = db.getSchedules();//получаем расписания
                ArrayList<ListOfAdapterObject> listOfSchedules = new ArrayList<ListOfAdapterObject>();//создаём лист с расписаниями
                while (cursor.moveToNext()) {//курсор в лист
                    listOfSchedules.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableSchedules.COLUMN_NAME)), SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSchedules.KEY_SCHEDULE_ID))));
                }
                this.adapter = new ListOfAdapter(this, listOfSchedules, false, SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES);//создаём адаптер
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp", "ListOfActivity - out Schedules");
                cursor.close();
                break;
            case SchoolContract.TableLessons.NAME_TABLE_LESSONS:
                Cursor tempCursor = db.getSchedules(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
                tempCursor.moveToFirst();
                getSupportActionBar().setTitle("расписание " + tempCursor.getString(tempCursor.getColumnIndex(SchoolContract.TableSchedules.COLUMN_NAME)));//ставим заголовок
                tempCursor.close();
                cursor = db.getLessonsByScheduleId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));//получаем уроки по расписанию
                ArrayList<ListOfAdapterObject> listOfLessons = new ArrayList<ListOfAdapterObject>();//создаём лист с уроками
                while (cursor.moveToNext()) {//курсор в лист todo передавать в список не только имя урока но и его параметры
                    listOfLessons.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)), SchoolContract.TableLessons.NAME_TABLE_LESSONS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessons.KEY_LESSON_ID))));
                }
                this.adapter = new ListOfAdapter(this, listOfLessons, false, SchoolContract.TableLessons.NAME_TABLE_LESSONS);//создаём адаптер
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp", "ListOfActivity - out Lessons");
                cursor.close();
                break;
            default:
                Log.wtf("TeachersApp", "ListOfActivity - in out, listParameterValue is default!");
                break;
        }
        db.close();//закрыли базу данных
    }

    @Override
    public void onBackPressed() {
        if (isEditMenuVisible) {
            switch (listParameterValue) {//исходя из содержимого списка
                case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
                    Cursor cursor = new DataBaseOpenHelper(this).getClasses();
                    ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<ListOfAdapterObject>();//создаём лист с классами
                    while (cursor.moveToNext()) {//курсор в лист
                        listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)), SchoolContract.TableClasses.NAME_TABLE_CLASSES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));
                    }
                    cursor.close();
                    this.adapter = new ListOfAdapter(this, listOfClasses, false, listParameterValue);
                    ((ListView) findViewById(R.id.content_list_of_list_view)).setAdapter(this.adapter);
                    break;
                }
                case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
                    Cursor cursor = new DataBaseOpenHelper(this).getLearnersByClassId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
                    ArrayList<ListOfAdapterObject> listOfLearners = new ArrayList<ListOfAdapterObject>();//создаём лист с учениками
                    while (cursor.moveToNext()) {//курсор в лист
                        listOfLearners.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)), SchoolContract.TableLearners.NAME_TABLE_LEARNERS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID))));
                    }
                    cursor.close();
                    this.adapter = new ListOfAdapter(this, listOfLearners, false, listParameterValue);
                    ((ListView) findViewById(R.id.content_list_of_list_view)).setAdapter(this.adapter);
                    break;
                }
                case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
                    Cursor cursor = new DataBaseOpenHelper(this).getCabinets();
                    ArrayList<ListOfAdapterObject> listOfCabinets = new ArrayList<ListOfAdapterObject>();//создаём лист с кабинетами
                    while (cursor.moveToNext()) {//курсор в лист
                        listOfCabinets.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
                    }
                    cursor.close();
                    this.adapter = new ListOfAdapter(this, listOfCabinets, false, listParameterValue);
                    ((ListView) findViewById(R.id.content_list_of_list_view)).setAdapter(this.adapter);
                    break;
                }
                case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES: {
                    Cursor cursor = new DataBaseOpenHelper(this).getSchedules();
                    ArrayList<ListOfAdapterObject> listOfSchedules = new ArrayList<ListOfAdapterObject>();//создаём лист с расписаниями
                    while (cursor.moveToNext()) {//курсор в лист
                        listOfSchedules.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableSchedules.COLUMN_NAME)), SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSchedules.KEY_SCHEDULE_ID))));
                    }
                    cursor.close();
                    this.adapter = new ListOfAdapter(this, listOfSchedules, false, listParameterValue);
                    ((ListView) findViewById(R.id.content_list_of_list_view)).setAdapter(this.adapter);
                    break;
                }
                case SchoolContract.TableLessons.NAME_TABLE_LESSONS:{
                    Cursor cursor = new DataBaseOpenHelper(this).getLessonsByScheduleId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
                    ArrayList<ListOfAdapterObject> listOfLessons = new ArrayList<ListOfAdapterObject>();//создаём лист с уроками
                    while (cursor.moveToNext()) {//курсор в лист
                        listOfLessons.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)), SchoolContract.TableLessons.NAME_TABLE_LESSONS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessons.KEY_LESSON_ID))));
                    }
                    cursor.close();
                    this.adapter = new ListOfAdapter(this, listOfLessons, false, listParameterValue);
                    ((ListView) findViewById(R.id.content_list_of_list_view)).setAdapter(this.adapter);
                    break;
                }
            }
            isEditMenuVisible = false;
        } else {
            super.onBackPressed();
        }
    }

    //    @Override
//    public void onBackPressed() {
//        Intent intent;
//        switch (listParameterValue) {
//            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:
//                intent = new Intent(ListOfActivity.this, StartScreenActivity.class);//домой
//                startActivity(intent);
//                Log.i("ListOfActivity", "back on classes");
//                break;
//            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS:
//                intent = new Intent(ListOfActivity.this, ListOfActivity.class);
//                intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
//                startActivity(intent);
//                Log.i("ListOfActivity", "back on learners");
//                break;
//            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
//                intent = new Intent(ListOfActivity.this, StartScreenActivity.class);//домой
//                startActivity(intent);
//                Log.i("ListOfActivity", "back on cabinets");
//                break;
//        }
//    }
}