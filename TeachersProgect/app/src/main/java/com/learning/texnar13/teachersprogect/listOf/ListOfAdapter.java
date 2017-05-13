package com.learning.texnar13.teachersprogect.listOf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

    /*Создаете в onCreate обработчик onClickListener с onClick в нем switch с определением ид элемента на который нажали и вызовом методов clickChkVisibleGroup и  clickChkVisibleItem. Далее цепляете в этом методе этот onClickListener к элементам чекбоксам.
Создаете глобальную переменную Menu myMenu; в событии onCreateOptionsMenu загоняете в эту переменную ссылку на меню myMenu = menu.
    */

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
        if (showCheckBoxes) {


            final CheckBox checkBox = new CheckBox(context);
            if(position == idPressedCheckBox){
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

            Log.i("ListOfAdapter", "add new element");
            final long classId = listOfAdapterObject.getobjId();
            flat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ошибка в getLong
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

                    ListView listView = (ListView) activity.findViewById(R.id.content_list_of_list_view);
                    listView.setAdapter(new ListOfAdapter(activity, content, true, (long) position));//todo cursor пустой
                    Log.i("ListOfActivity", "out classes wis checkboxes");
//                    ListOfDialog dialog = new ListOfDialog();
//                            dialog.objectParameter = listParameterValue;
//                            dialog.objectId = classId;
//                            dialog.show(getFragmentManager(), "dialogEditClass");
//                            return true;
                    return true;
                }
            });
        }
        flat.addView(title, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private ListOfAdapterObject getListOfAdapterObject(int position) {
        return (ListOfAdapterObject) getItem(position);
    }

}
