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
                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());
                switch (getIntent().getStringExtra(LIST_PARAMETER)) {//todo -1 при удалении, и в диалог
                    case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
                        db.deleteClasses(adapter.getIdCheckedListOfAdapterObjects());
                        break;
                    }
                    case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
                        db.deleteLearners(adapter.getIdCheckedListOfAdapterObjects());
                        break;
                    }
                    case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
                        db.deleteCabinets(adapter.getIdCheckedListOfAdapterObjects());
                        break;
                    }
                    case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES: {
                        db.deleteSchedules(adapter.getIdCheckedListOfAdapterObjects());
                        break;
                    }
                    case SchoolContract.TableLessons.NAME_TABLE_LESSONS: {
                        db.deleteLessons(adapter.getIdCheckedListOfAdapterObjects());
                        break;
                    }
                }
                db.close();

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

        ListView listView = (ListView) findViewById(R.id.content_list_of_list_view);
        //будущий курсор с обьектами вывода;
        switch (listParameterValue) {//вибираем тип содержимого списка
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
                getSupportActionBar().setTitle("мои классы");//ставим заголовок
                ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<>();//получаем классы из базы данных и заносим их в arrayList
                {
                    Cursor cursor = db.getClasses();
                    while (cursor.moveToNext()) {
                        listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)),//имя
                                SchoolContract.TableClasses.NAME_TABLE_CLASSES,//тип
                                cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));//id
                    }
                    cursor.close();
                }
                this.adapter = new ListOfAdapter(this, listOfClasses, false, SchoolContract.TableClasses.NAME_TABLE_CLASSES);//создаём адаптер
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp", "ListOfActivity - out classes");

            }
            break;
            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
                {//ставим заголовок
                    Cursor tempCursor = db.getClasses(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
                    tempCursor.moveToFirst();
                    getSupportActionBar().setTitle("ученики в классе " + tempCursor.getString(tempCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
                    tempCursor.close();
                }
                ArrayList<ListOfAdapterObject> listOfLearners = new ArrayList<>();//создаём лист с классами
                {
                    Cursor cursor = db.getLearnersByClassId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));//получаем учеников в курсоре по id класса (по умолчанию по первому классу)
                    while (cursor.moveToNext()) {//курсор в лист
                        listOfLearners.add(new ListOfAdapterObject(cursor.getString(
                                cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) +
                                " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
                                SchoolContract.TableLearners.NAME_TABLE_LEARNERS,
                                cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID))));
                    }
                    cursor.close();
                }
                this.adapter = new ListOfAdapter(this, listOfLearners, false, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//создаём адаптер со списком учеников
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp ", "ListOfActivity - out learners");
                break;
            }
            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
                getSupportActionBar().setTitle("мои кабинеты");//ставим заголовок
                ArrayList<ListOfAdapterObject> listOfCabinets = new ArrayList<>();//создаём лист с классами
                {
                    Cursor cursor = db.getCabinets();//получаем кабинеты
                    while (cursor.moveToNext()) {//курсор в лист
                        listOfCabinets.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
                    }
                    cursor.close();
                }
                this.adapter = new ListOfAdapter(this, listOfCabinets, false, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);//создаём адаптер
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp", "ListOfActivity - out cabinets");
                break;
            }
            case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES: {
                getSupportActionBar().setTitle("мои расписания");//ставим заголовок
                ArrayList<ListOfAdapterObject> listOfSchedules = new ArrayList<>();//создаём лист с расписаниями
                {
                    Cursor cursor = db.getSchedules();//получаем расписания
                    while (cursor.moveToNext()) {//курсор в лист
                        listOfSchedules.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableSchedules.COLUMN_NAME)), SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSchedules.KEY_SCHEDULE_ID))));
                    }
                    cursor.close();
                }
                this.adapter = new ListOfAdapter(this, listOfSchedules, false, SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES);//создаём адаптер
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp", "ListOfActivity - out Schedules");
                break;
            }
            case SchoolContract.TableLessons.NAME_TABLE_LESSONS: {
                {
                    Cursor tempCursor = db.getSchedules(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
                    tempCursor.moveToFirst();
                    getSupportActionBar().setTitle("расписание \"" + tempCursor.getString(tempCursor.getColumnIndex(SchoolContract.TableSchedules.COLUMN_NAME)) + "\"");//ставим заголовок
                    tempCursor.close();
                }
                ArrayList<ListOfAdapterObject> listOfLessons = new ArrayList<ListOfAdapterObject>();//создаём лист с уроками
                {
                    Cursor cursor = db.getLessonsByScheduleId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));//получаем уроки по расписанию
                    while (cursor.moveToNext()) {//курсор в лист todo передавать в список не только имя урока но и его параметры
                        listOfLessons.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)), SchoolContract.TableLessons.NAME_TABLE_LESSONS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessons.KEY_LESSON_ID))));
                    }
                    cursor.close();
                }
                this.adapter = new ListOfAdapter(this, listOfLessons, false, SchoolContract.TableLessons.NAME_TABLE_LESSONS);//создаём адаптер
                listView.setAdapter(this.adapter);//ставим адаптер
                Log.i("TeachersApp", "ListOfActivity - out Lessons");
                break;
            }
            default:
                Log.wtf("TeachersApp", "ListOfActivity - in out, listParameterValue is default!");
                break;
        }
        db.close();//закрыли базу данных
    }

    @Override
    public void onBackPressed() {
        if (isEditMenuVisible) {
            ArrayList<ListOfAdapterObject> list = new ArrayList<>();//создаём лист с обьектами
            switch (listParameterValue) {//исходя из типа заполняем лист
                case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
                        Cursor cursor = new DataBaseOpenHelper(this).getClasses();
                        while (cursor.moveToNext()) {//курсор в лист
                            list.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)), SchoolContract.TableClasses.NAME_TABLE_CLASSES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));
                        }
                        cursor.close();
                    break;
                }
                case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
                    Cursor cursor = new DataBaseOpenHelper(this).getLearnersByClassId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
                    while (cursor.moveToNext()) {//курсор в лист
                        list.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)), SchoolContract.TableLearners.NAME_TABLE_LEARNERS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID))));
                    }
                    cursor.close();
                    break;
                }
                case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
                    Cursor cursor = new DataBaseOpenHelper(this).getCabinets();
                    while (cursor.moveToNext()) {//курсор в лист
                        list.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
                    }
                    cursor.close();
                    break;
                }
                case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES: {
                    Cursor cursor = new DataBaseOpenHelper(this).getSchedules();
                    while (cursor.moveToNext()) {//курсор в лист
                        list.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableSchedules.COLUMN_NAME)), SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSchedules.KEY_SCHEDULE_ID))));
                    }
                    cursor.close();
                    break;
                }
                case SchoolContract.TableLessons.NAME_TABLE_LESSONS: {
                    Cursor cursor = new DataBaseOpenHelper(this).getLessonsByScheduleId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
                    while (cursor.moveToNext()) {//курсор в лист
                        list.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)), SchoolContract.TableLessons.NAME_TABLE_LESSONS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessons.KEY_LESSON_ID))));
                    }
                    cursor.close();
                    break;
                }
            }
            this.adapter = new ListOfAdapter(this, list, false, listParameterValue);
            ((ListView) findViewById(R.id.content_list_of_list_view)).setAdapter(this.adapter);
            isEditMenuVisible = false;
        } else {
            super.onBackPressed();
        }
    }
}