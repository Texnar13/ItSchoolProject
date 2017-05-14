package com.learning.texnar13.teachersprogect.listOf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;


public class ListOfAdapter extends BaseAdapter {

    private Activity activity;
    private Context context;
    //private Cursor cursor;
    private LayoutInflater inflater;
    private ArrayList<ListOfAdapterObject> content;
    private boolean showCheckBoxes;
    private long idPressedCheckBox = -1;

    ListOfAdapter(Activity activity, ArrayList<ListOfAdapterObject> content, boolean showCheckBoxes, long idPressedCheckBox) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.content = content;
        this.showCheckBoxes = showCheckBoxes;
        this.idPressedCheckBox = idPressedCheckBox;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    ListOfAdapter(Activity activity, Cursor cursor, boolean showCheckBoxes) {
        ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<ListOfAdapterObject>();
        while (cursor.moveToNext()) {
            listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)), SchoolContract.TableClasses.NAME_TABLE_CLASSES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));
        }
        //this.cursor = cursor;

        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.content = listOfClasses;
        this.showCheckBoxes = showCheckBoxes;
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
        return content.get(i).getobjId();
    }

    //пункт списка
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.i("ListOfAdapter", "getView position = " + position);
        // используем созданные, но не используемые view
        View view = convertView;
        //if (view == null) {
        //    view = inflater.inflate(R.layout.list_of_adapter_element, parent, false);
        //}
        view = inflater.inflate(R.layout.list_of_adapter_element, parent, false);
        final ListOfAdapterObject listOfAdapterObject = getListOfAdapterObject(position);


        LinearLayout flat = (LinearLayout) view.findViewById(R.id.list_of_adapter_element_out);
        Button title = new Button(context);
        title.setText(listOfAdapterObject.getobjName());
        //выводим чекбоксы
        Log.i("ListOfAdapter", "getView showCheckBoxes = " + showCheckBoxes);
        if (showCheckBoxes) {

            ((AbleToChangeTheEditMenu) activity).editIsEditMenuVisible(true);

            final CheckBox checkBox = new CheckBox(context);
            if (position == idPressedCheckBox) {
                checkBox.setChecked(true);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkBox.setChecked(isChecked);
                    listOfAdapterObject.setChecked(isChecked);
                }
            });
            flat.addView(checkBox);
        } else {
            ((AbleToChangeTheEditMenu) activity).editIsEditMenuVisible(false);
            Log.i("ListOfAdapter", "add new element");
            final long classId = listOfAdapterObject.getobjId();
            flat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("ListOfAdapter", "classes onClick, id = " + classId);
                    Intent intent;
                    intent = new Intent(context, ListOfActivity.class);//запуск этого активити заново
                    intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);//но с учениками
                    intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, classId);//передаём id выбранного класса
                    activity.startActivity(intent);
                }
            });
            flat.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.i("ListOfAdapter", "classes onLongClick, id = " + classId);
                    listOfAdapterObject.setChecked(true);
                    ListView listView = (ListView) activity.findViewById(R.id.content_list_of_list_view);
                    listView.setAdapter(new ListOfAdapter(activity, content, true, (long) position));
                    return true;
                }
            });
        }
        activity.invalidateOptionsMenu();//обновляем меню
        flat.addView(title, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private ListOfAdapterObject getListOfAdapterObject(int position) {
        return (ListOfAdapterObject) getItem(position);
    }

    ArrayList<Long> getIdCheckedListOfAdapterObjects() {//когда создаю новый при помощи fab их на 1 меньше
        Log.i("ListOfAdapter", "getIdCheckedListOfAdapterObjects number = " + content.size() + " content = " + content);
        ArrayList<Long> idCheckedListOfAdapterObjects = new ArrayList<>();
        for (int i = 0; i < content.size(); i++) {
            if (content.get(i).isChecked()) {
                idCheckedListOfAdapterObjects.add(content.get(i).getobjId());
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


    ListOfAdapterObject(String name, String type, long id) {
        this.objName = name;
        this.objType = type;
        this.objId = id;
        isChecked = false;

    }

    boolean isChecked() {
        return isChecked;
    }

    void setChecked(boolean checked) {
        isChecked = checked;
    }

    String getobjName() {
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

    long getobjId() {
        return objId;
    }

    void setobjId(long id) {
        this.objId = id;
    }
}

