package com.learning.texnar13.teachersprogect.listOf;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

interface AbleToChangeTheEditMenu {
    void editIsEditMenuVisible(boolean isEditMenuVisible);
}

public class ListOfActivity extends AppCompatActivity implements AbleToChangeTheEditMenu {//todo0 закрыть все курсоры и базы данных
    //todo сделать общий метод обновления адаптера

    public static final String LIST_PARAMETER = "listParameter";
    public static final String DOP_LIST_PARAMETER = "dopListParameter";

    FloatingActionButton fab;
    String listParameterValue;

    boolean isEditMenuVisible = false;
    AppCompatActivity listOfActivity = this;
    ListOfAdapter adapter;//ссылка на адаптер

    //текст в центре экрана
    public static TextView stateText;

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
                ArrayList<ListOfAdapterObject> list = new ArrayList<>();//создаём лист с классами
                Cursor cursor;
                switch (getIntent().getStringExtra(LIST_PARAMETER)) {//todo -1 при удалении, и в диалог
                    case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
                        db.deleteClasses(adapter.getIdCheckedListOfAdapterObjects());
                        cursor = db.getClasses();//получаем классы
                        break;
                    }
//                    case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
//                        db.deleteLearners(adapter.getIdCheckedListOfAdapterObjects());
//                        cursor = db.getLearnersByClassId(getIntent().getLongExtra(DOP_LIST_PARAMETER, 0));//получаем учеников
//                        break;
//                    }
                    case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
                        db.deleteCabinets(adapter.getIdCheckedListOfAdapterObjects());
                        cursor = db.getCabinets();//получаем кабинеты
                        break;
                    }
                    default:
                        throw new RuntimeException("notDefaultListParameter(" + getIntent().getStringExtra(LIST_PARAMETER) + ")");
                }
                while (cursor.moveToNext()) {//курсор в лист
                    list.add(
                            new ListOfAdapterObject(
                                    cursor.getString(
                                            cursor.getColumnIndex(
                                                    SchoolContract.TableClasses.COLUMN_CLASS_NAME)),
                                    SchoolContract.TableClasses.NAME_TABLE_CLASSES,
                                    cursor.getLong(
                                            cursor.getColumnIndex(
                                                    SchoolContract.TableClasses.KEY_CLASS_ID))));
                }
                cursor.close();

                /*
                01-08 18:22:37.422 4755-4755/com.learning.texnar13.teachersprogect E/CursorWindow: Failed to read row 0, column -1 from a CursorWindow which has 1 rows, 2 columns.
01-08 18:22:37.422 4755-4755/com.learning.texnar13.teachersprogect D/AndroidRuntime: Shutting down VM


                                                                                     --------- beginning of crash
01-08 18:22:37.423 4755-4755/com.learning.texnar13.teachersprogect E/AndroidRuntime: FATAL EXCEPTION: main
                                                                                     Process: com.learning.texnar13.teachersprogect, PID: 4755
                                                                                     java.lang.IllegalStateException: Couldn't read row 0, col -1 from CursorWindow.  Make sure the Cursor is initialized correctly before accessing data from it.
                                                                                         at android.database.CursorWindow.nativeGetString(Native Method)
                                                                                         at android.database.CursorWindow.getString(CursorWindow.java:438)
                                                                                         at android.database.AbstractWindowedCursor.getString(AbstractWindowedCursor.java:51)
                                                                                         at com.learning.texnar13.teachersprogect.listOf.ListOfActivity$1.onMenuItemClick(ListOfActivity.java:83)
                                                                                         at android.support.v7.view.menu.MenuItemImpl.invoke(MenuItemImpl.java:152)
                                                                                         at android.support.v7.view.menu.MenuBuilder.performItemAction(MenuBuilder.java:969)
                                                                                         at android.support.v7.view.menu.MenuBuilder.performItemAction(MenuBuilder.java:959)
                                                                                         at android.support.v7.widget.ActionMenuView.invokeItem(ActionMenuView.java:623)
                                                                                         at android.support.v7.view.menu.ActionMenuItemView.onClick(ActionMenuItemView.java:154)
                                                                                         at android.view.View.performClick(View.java:4780)
                                                                                         at android.view.View$PerformClick.run(View.java:19866)
                                                                                         at android.os.Handler.handleCallback(Handler.java:739)
                                                                                         at android.os.Handler.dispatchMessage(Handler.java:95)
                                                                                         at android.os.Looper.loop(Looper.java:135)
                                                                                         at android.app.ActivityThread.main(ActivityThread.java:5254)
                                                                                         at java.lang.reflect.Method.invoke(Native Method)
                                                                                         at java.lang.reflect.Method.invoke(Method.java:372)
                                                                                         at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:903)
                                                                                         at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:698)
                                                                                         */


                ListOfAdapter newAdapter = new ListOfAdapter(listOfActivity, list, false, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                adapter = newAdapter;
                ((ListView) findViewById(R.id.content_list_of_list_view)).setAdapter(newAdapter);
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
                Log.i("TeachersApp", "ListOfActivity - onPrepareOptionsMenu renameId =" +
                        adapter.getIdCheckedListOfAdapterObjects());
                //параметры для диалога
                Bundle bundle = new Bundle();
                bundle.putString("objectParameter",getIntent().getStringExtra(LIST_PARAMETER));
                bundle.putLong("parentId",getIntent().getLongExtra(DOP_LIST_PARAMETER, -1));
                {
                    ArrayList<Long> idCheckedListOfAdapterObjects = adapter.getIdCheckedListOfAdapterObjects();
                    long[] idObjects = new long[idCheckedListOfAdapterObjects.size()];
                    for (int i = 0; i < idCheckedListOfAdapterObjects.size(); i++) {
                        idObjects[i]=idCheckedListOfAdapterObjects.get(i);
                    }
                    bundle.putLongArray("objectsId", idObjects);
                }
                ListOfDialog dialog = new ListOfDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "dialogEdit");
                //вызываем диалог, ставим имя и обновляем из диалога
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        //находим ссылку на текст в центре экрана
        stateText = (TextView) findViewById(R.id.list_of_state_text);

        listParameterValue = getIntent().getStringExtra(LIST_PARAMETER);
        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("TeachersApp", "ListOfActivity - newUnit(fab.OnClickListener)");
                Bundle bundle = new Bundle();
                bundle.putString("objectParameter",listParameterValue);
                bundle.putLong("parentId",getIntent().getLongExtra(DOP_LIST_PARAMETER, -1));
                bundle.putLongArray("objectsId",new long[0]);
                ListOfDialog dialog = new ListOfDialog();
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "dialogNewUnit");
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        LinearLayout room = (LinearLayout) findViewById(R.id.content_list_of_content_room);
        ListView listView = (ListView) findViewById(R.id.content_list_of_list_view);

        //будущий курсор с обьектами вывода;
        switch (listParameterValue) {//вибираем тип содержимого списка
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES: {
                //ставим центральный текст по умолчанию(для классов)
                stateText.setText("Здесь выводятся созданные вами классы с учениками. Чтобы создать новый класс нажмите кнопку '+', для редактирования, удерживайте класс, затем в меню выберите 'карандаш', чтобы переименовать, или 'корзину', чтобы удалить класс. Для просмотра учеников в классе нажмите на него в списке. ");
                //дизайн
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#bed7e9")));
                room.setBackgroundColor(Color.WHITE);//parseColor("#f4e6d3")
                //ставим заголовок
                getSupportActionBar().setTitle("Мои классы");
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
//            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
//                //дизайн
//                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#bed7e9")));
//                room.setBackgroundColor(Color.WHITE);//parseColor("#f4e6d3")
//                {//ставим заголовок
//                    Cursor tempCursor = db.getClasses(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
//                    tempCursor.moveToFirst();
//                    getSupportActionBar().setTitle("Ученики в классе " + tempCursor.getString(tempCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
//                    tempCursor.close();
//                }
//                ArrayList<ListOfAdapterObject> listOfLearners = new ArrayList<>();//создаём лист с классами
//                {
//                    Cursor cursor = db.getLearnersByClassId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));//получаем учеников в курсоре по id класса (по умолчанию по первому классу)
//                    while (cursor.moveToNext()) {//курсор в лист
//                        listOfLearners.add(new ListOfAdapterObject(cursor.getString(
//                                cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) +
//                                " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
//                                SchoolContract.TableLearners.NAME_TABLE_LEARNERS,
//                                cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID))));
//                    }
//                    cursor.close();
//                }
//                this.adapter = new ListOfAdapter(this, listOfLearners, false, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//создаём адаптер со списком учеников
//                listView.setAdapter(this.adapter);//ставим адаптер
//                Log.i("TeachersApp ", "ListOfActivity - out learners");
//                break;
//            }
            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
                //ставим центральный текст по умолчанию(для кабинетов)
                stateText.setText("Для создания нового кабинета нажмите кнопку '+' внизу экрана, затем введите его номер или название. Для расстановки парт войдите в кабинет нажав на него в списке. Для редактирования нажмите и удерживайте кабинет, затем в меню сверху выберите карандаш, чтобы переименовать его или корзину, чтобы его удалить(если не хотите изменять кабинет, нажмите кнопку назад чтобы выйти из меню).");
                //дизайн
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#f5ce9d")));
                room.setBackgroundColor(Color.WHITE);//parseColor("#f4e6d3")

                getSupportActionBar().setTitle("Мои кабинеты");//ставим заголовок
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
            default:
                Log.wtf("TeachersApp", "ListOfActivity - in out, listParameterValue is default!");
                break;
        }
        db.close();//закрыли базу данных
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
//                        case SchoolContract.TableLearners.NAME_TABLE_LEARNERS: {
//                            Cursor cursor = new DataBaseOpenHelper(this).getLearnersByClassId(getIntent().getLongExtra(ListOfActivity.DOP_LIST_PARAMETER, 1));
//                            while (cursor.moveToNext()) {//курсор в лист
//                                list.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)), SchoolContract.TableLearners.NAME_TABLE_LEARNERS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID))));
//                            }
//                            cursor.close();
//                            break;
//                        }
                        case SchoolContract.TableCabinets.NAME_TABLE_CABINETS: {
                            Cursor cursor = new DataBaseOpenHelper(this).getCabinets();
                            while (cursor.moveToNext()) {//курсор в лист
                                list.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
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