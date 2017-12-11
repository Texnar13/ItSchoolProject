package com.learning.texnar13.teachersprogect.listOf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.CabinetRedactorActivity;
import com.learning.texnar13.teachersprogect.LearnersAndGrades.LearnersAndGradesActivity;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;


class ListOfAdapter extends BaseAdapter {//todo задача адаптера принимать список, отправлять обратно выбранный элемент
    private Activity activity;
    private Context context;
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
        View view;
        //view = convertView;
        //if (view == null) {
        //    view = inflater.inflate(R.layout.list_of_adapter_element, parent, false);
        //}
        view = inflater.inflate(R.layout.list_of_adapter_element, parent, false);

        LinearLayout flat = (LinearLayout) view.findViewById(R.id.list_of_adapter_element_out);//контейнер элемента списка
        TextView title = new Button(context);//элемент списка, пока кнопка
//
//        title.setTextColor(Color.BLACK);
//        title.setBackgroundColor(Color.parseColor("#c9c9c9"));
        switch (type) {
            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
                title.setBackgroundColor(Color.parseColor("#f5ce9d"));
                title.setTextSize(20);
                title.setTextColor(Color.parseColor("#88591d"));//parseColor("#5c3a0d")
                break;
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:
                title.setBackgroundColor(Color.parseColor("#bed7e9"));
                title.setTextSize(20);
                title.setTextColor(Color.parseColor("#1f5b85"));
                break;
            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS:
                title.setBackgroundColor(Color.TRANSPARENT);
                title.setClickable(false);
                title.setTextSize(20);
                title.setTextColor(Color.parseColor("#1f5b85"));
                break;
            default:
                throw new  RuntimeException();
        }
        title.setText(((ListOfAdapterObject) getItem(position)).getObjName());//ставим имя
        Log.i("TeachersApp", "ListOfAdapter - getView isChecked = " + ((ListOfAdapterObject) getItem(position)).isChecked() + " position = " + position);
        if (showCheckBoxes) {//выбираем будем ли помещать в контейнер checkBox и назначаем checkBox-у действия

            try {
                ((AbleToChangeTheEditMenu) activity).editIsEditMenuVisible(true);//меню с действиями к выбранным heckBox-ам
            } catch (java.lang.ClassCastException err) {//активити должно имплементировать вывод меню
                Log.e("TeachersApp", "you must implements AbleToChangeTheEditMenu in your class");
                err.printStackTrace();
                activity.finish();
            }
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
            try {
                ((AbleToChangeTheEditMenu) activity).editIsEditMenuVisible(false);//
            } catch (java.lang.ClassCastException err) {
                Log.e("TeachersApp", "you must implements AbleToChangeTheEditMenu in your class");
                err.printStackTrace();
                activity.finish();
            }
            Log.i("TeachersApp", "ListOfAdapter - add new element");
            final long objId = ((ListOfAdapterObject) getItem(position)).getObjId();//получаем id обьекта
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("TeachersApp", "ListOfAdapter - classes onClick, id = " + objId);
                    Intent intent;//намерение для запуска ледующего активити
                    switch (type) {//тип !вызывающего! обьекта
                        case SchoolContract.TableClasses.NAME_TABLE_CLASSES://запуск этого активити заново
//                            intent = new Intent(context, ListOfActivity.class);
//                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//с параметром ученики
//                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, objId);//передаём id выбранного класса
//                            activity.startActivity(intent);
//                            break;

                        //todo статистика оценок ученика
//                        case SchoolContract.TableLearners.NAME_TABLE_LEARNERS://todo0 будем переходить к статистике оценок ученика

                            intent = new Intent(context, LearnersAndGradesActivity.class);
                            //intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//с параметром
                            intent.putExtra(LearnersAndGradesActivity.CLASS_ID, objId);//передаём id выбранного ученика
                            activity.startActivity(intent);
                            break;
                        case SchoolContract.TableCabinets.NAME_TABLE_CABINETS://запуск редактора кабинета
                            intent = new Intent(context, CabinetRedactorActivity.class);
                            intent.putExtra(CabinetRedactorActivity.EDITED_OBJECT_ID, objId);//передаём id выбранного бьекта
                            activity.startActivity(intent);
                            break;
                        default:
                    }
                }
            });
            title.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
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
        flat.addView(title, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    ArrayList<Long> getIdCheckedListOfAdapterObjects() {
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