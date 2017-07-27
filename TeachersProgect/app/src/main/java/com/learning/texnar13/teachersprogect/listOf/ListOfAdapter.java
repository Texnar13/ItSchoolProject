package com.learning.texnar13.teachersprogect.listOf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;


public class ListOfAdapter extends BaseAdapter {//todo задача адаптера принимать список, отправлять обратно выбранный элемент
    //может нужна переменная был ли этот вызов чекбоксов первым  после отработки метода сделать её false
    //сохраняется массив, который создавался ври первых изменениях значения в следующем меняются, но используется первый
    //он должен обновляться при закрытии диалога возможно при закрытии мы передаём ему старый массив
    private Activity activity;
    private Context context;
    //private Cursor cursor;
    private LayoutInflater inflater;
    private ArrayList<ListOfAdapterObject> content;//содержит все данные об обьектах списка
    private boolean showCheckBoxes;
    private String type;

    ListOfAdapter(Activity activity, ArrayList<ListOfAdapterObject> content, boolean showCheckBoxes, String type) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.content = content;//отображаемые обьекты
        this.showCheckBoxes = showCheckBoxes;//есть ли чекбоксы
        this.type = type;//тип отображаемых обьектов
        //this.idPressedCheckBox = idPressedCheckBox;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public Object getItem(int i) {
        return content.get(i);
    }

    @Override
    public long getItemId(int i) {
        return content.get(i).getObjId();
    }

    //пункт списка
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        //if (view == null) {
        //    view = inflater.inflate(R.layout.list_of_adapter_element, parent, false);
        //}
        view = inflater.inflate(R.layout.list_of_adapter_element, parent, false);

        LinearLayout flat = (LinearLayout) view.findViewById(R.id.list_of_adapter_element_out);//контейнер элемента списка
        Button title = new Button(context);//элемент списка, пока кнопка
        title.setText(((ListOfAdapterObject) getItem(position)).getObjName());//ставим имя
        Log.i("TeachersApp", "ListOfAdapter - getView isChecked = " + ((ListOfAdapterObject) getItem(position)).isChecked()+" position = " + position);
        if (showCheckBoxes) {//выбираем будем ли помещать в контейнер checkBox и назначаем checkBox-у действия

            ((AbleToChangeTheEditMenu) activity).editIsEditMenuVisible(true);//меню с действиями к выбранным heckBox-ам

            final CheckBox checkBox = new CheckBox(context);//метки
            checkBox.setChecked(((ListOfAdapterObject) getItem(position)).isChecked());//----------------------------------
//            if (position == idPressedCheckBox) {
//                checkBox.setChecked(true);
//            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.i("TeachersApp", "ListOfAdapter - getVieW - onCheckedChanged position =" + position + " " + isChecked);
                    checkBox.setChecked(isChecked);
                    ((ListOfAdapterObject) getItem(position)).setChecked(isChecked);
                }
            });
            flat.addView(checkBox);
        } else {
            ((AbleToChangeTheEditMenu) activity).editIsEditMenuVisible(false);//
            Log.i("TeachersApp", "ListOfAdapter - add new element");
            final long objId = ((ListOfAdapterObject) getItem(position)).getObjId();//получаем id обьекта
            flat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("TeachersApp", "ListOfAdapter - classes onClick, id = " + objId);
                    Intent intent;//намерение для запуска ледующего активити
                    switch (type) {//тип вызывающего обьекта//// TODO: 21.07.2017 !!!!!!!!!!!!!!!!!!!сделать редактор рассадки открывающимся из определённого меню
                        case SchoolContract.TableClasses.NAME_TABLE_CLASSES://запуск этого активити заново
                            intent = new Intent(context, ListOfActivity.class);
                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//с параметром ученики
                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, objId);//передаём id выбранного класса
                            activity.startActivity(intent);
                            break;
//                        case SchoolContract.TableLearners.NAME_TABLE_LEARNERS://todo0 будем переходить к статистике оценок ученика
//                            intent = new Intent(context, ListOfActivity.class);
//                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//с параметром
//                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, objId);//передаём id выбранного ученика
//                            activity.startActivity(intent);
//                            break;
                        case SchoolContract.TableCabinets.NAME_TABLE_CABINETS://запуск редактора кабинета
                            intent = new Intent(context, CabinetRedactorActivity.class);
                            intent.putExtra(CabinetRedactorActivity.EDITED_OBJECT_ID, objId);//передаём id выбранного бьекта
                            activity.startActivity(intent);
                            break;
                        case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES://запуск этого активити заново
                            intent = new Intent(context, ListOfActivity.class);
                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLessons.NAME_TABLE_LESSONS);//с параметром уроки
                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, objId);//передаём id выбранного расписания
                            activity.startActivity(intent);
                            break;
//                        case SchoolContract.TableLessons.NAME_TABLE_LESSONS://todo запуск урока
//                            intent = new Intent(context, LessonActivity.class);
//                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLessons.NAME_TABLE_LESSONS);//с параметром уроки
//                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, objId);//передаём id выбранного расписания
//                            activity.startActivity(intent);
//                            break;
                        default:
                    }
                }
            });
            flat.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {//todo я в setOnLongClickListener не делаю что-то что есть в onBackPressed может content просрочен
                    Log.i("TeachersApp", "ListOfAdapter - onLongClick, id = " + objId);
                    ((ListOfAdapterObject) getItem(position)).setChecked(true);
                    Log.i("TeachersApp", "ListOfAdapter - ");
                    ListView listView = (ListView) activity.findViewById(R.id.content_list_of_list_view);
                    listView.setAdapter(new ListOfAdapter(activity, content, true, type));//передаю всё тот же лист, но в них только один чёкнут
                    return true;
                }
            });
        }
        activity.invalidateOptionsMenu();//обновляем меню
        flat.addView(title, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    ArrayList<Long> getIdCheckedListOfAdapterObjects() {//todo запоминает первое переименование
        Log.i("TeachersApp", "ListOfAdapter - getIdCheckedListOfAdapterObjects number = " + content.size() + " content = " + content);
        ArrayList<Long> idCheckedListOfAdapterObjects = new ArrayList<>();
        for (int i = 0; i < content.size(); i++) {
            if (content.get(i).isChecked()) {
                idCheckedListOfAdapterObjects.add(content.get(i).getObjId());
            }
        }
        return idCheckedListOfAdapterObjects;
    }

}

class ListOfAdapterObject {
    private String objName;
    private String objType;
    private long objId;
    private boolean isChecked;


    @Override
    public String toString() {
        return "objId =" + objId + " isChecked =" + isChecked();
    }

    ListOfAdapterObject(String name, String type, long id) {
        this.objName = name;
        this.objType = type;
        this.objId = id;
        isChecked = false;
        Log.i("TeachersApp", "createListOfAdapterObject - name =" + name + " type =" + type + " objId =" + objId + " isChecked =" + isChecked);
    }

    boolean isChecked() {
        return isChecked;
    }

    void setChecked(boolean checked) {
        Log.i("TeachersApp", "setChecked - objId =" + objId + " checked =" + checked);
        isChecked = checked;
    }

    String getObjName() {
        return objName;
    }

    public void setobjName(String name) {
        this.objName = name;
    }

    String getobjType() {
        return objType;
    }

    void setobjType(String type) {
        this.objType = type;
    }

    long getObjId() {
        return objId;
    }

    void setobjId(long id) {
        this.objId = id;
    }
}