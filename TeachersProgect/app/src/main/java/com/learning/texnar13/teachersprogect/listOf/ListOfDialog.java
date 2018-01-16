package com.learning.texnar13.teachersprogect.listOf;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class ListOfDialog extends DialogFragment {

    String objectParameter;
    long parentId;
    ArrayList<Long> objectsId = new ArrayList<>();

    public ListOfDialog() {//конструктор этого класса

    }

    //todo просто передавать параметр в инт что с ним делать

    @Override//создание диалога
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //инициализация
        this.objectParameter = getArguments().getString("objectParameter");
        this.parentId = getArguments().getLong("parentId");
        long[] array = getArguments().getLongArray("objectsId");
        for (int i = 0; i < array.length; i++) {
            objectsId.add(array[i]);
        }

        Log.i("TeachersApp", "ListOfDialog - onCreateDialog");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.;
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.new_list_of_dialog, null);

        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.new_list_of_dialog_layout);
        switch (objectParameter) {
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:
                Log.i("TeachersApp", "ListOfDialog - onCreateDialog - out classes");

                final EditText className = new EditText(getActivity().getApplicationContext());
                className.setTextColor(Color.BLACK);
                className.setHint("имя класса");
                className.setHintTextColor(Color.GRAY);
                linearLayout.addView(className, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                if (objectsId.size() == 0) {
                    builder.setTitle("создание класса");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.createClass(className.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getClasses();//получаем классы
                                ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<>();//создаём лист с классами
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)), SchoolContract.TableClasses.NAME_TABLE_CLASSES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter = new ListOfAdapter(getActivity(), listOfClasses, false, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                                ((ListOfActivity) getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
                                ((ListView) getActivity().findViewById(R.id.content_list_of_list_view)).setAdapter(adapter);
                            }
                            db.close();
                            dismiss();
                        }
                    });
                    builder.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
                } else {
                    builder.setTitle("редактирование класса");
                    final DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                    if (objectsId.size() == 1) {//если получаем на вход только один класс то ставим в строку его имя
                        Cursor cursor = db.getClasses(objectsId.get(0));
                        cursor.moveToFirst();
                        className.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
                        cursor.close();
                    }
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.setClassesNames(objectsId, className.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getClasses();//получаем классы
                                ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<>();//создаём лист с классами
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)), SchoolContract.TableClasses.NAME_TABLE_CLASSES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter = new ListOfAdapter(getActivity(), listOfClasses, false, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                                ((ListOfActivity) getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
                                ((ListView) getActivity().findViewById(R.id.content_list_of_list_view)).setAdapter(adapter);
                            }
                            db.close();
                            dismiss();
                        }
                    });
                    builder.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
                }
                break;
            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
                Log.i("TeachersApp", "ListOfDialog - onCreateDialog - out cabinets");

                final EditText cabinetName = new EditText(getActivity().getApplicationContext());
                cabinetName.setTextColor(Color.BLACK);
                cabinetName.setHint("название кабинета");
                cabinetName.setHintTextColor(Color.GRAY);

                linearLayout.addView(cabinetName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                final DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                if (objectsId.size() == 1) {//если получаем на вход только один кабинет то ставим в строку его имя
                    Cursor cursor = db.getCabinets(objectsId.get(0));
                    cursor.moveToFirst();
                    cabinetName.setText(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)));
                    cursor.close();
                }
                if (objectsId.size() == 0) {
                    builder.setTitle("создание кабинета");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            db.createCabinet(cabinetName.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getCabinets();//получаем кабинеты
                                ArrayList<ListOfAdapterObject> listOfCabinets = new ArrayList<>();//создаём лист с кабинетами
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfCabinets.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter = new ListOfAdapter(getActivity(), listOfCabinets, false, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
                                ((ListOfActivity) getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
                                ((ListView) getActivity().findViewById(R.id.content_list_of_list_view)).setAdapter(adapter);
                            }
                            db.close();
                            dismiss();
                        }
                    });
                    builder.setNegativeButton("отменить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
                } else {
                    builder.setTitle("редактирование кабинета");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.setCabinetName(objectsId, cabinetName.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getCabinets();//получаем классы
                                ArrayList<ListOfAdapterObject> listOfCabinets = new ArrayList<>();//создаём лист с классами
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfCabinets.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter = new ListOfAdapter(getActivity(), listOfCabinets, false, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
                                ((ListOfActivity) getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
                                ((ListView) getActivity().findViewById(R.id.content_list_of_list_view)).setAdapter(adapter);
                            }
                            db.close();
                            dismiss();
                        }
                    });
                    builder.setNegativeButton("отменить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
                }
                break;

        }
        builder.setView(dialogLayout);//view в центре диалога
        return builder.create();// Create the AlertDialog object and return it
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);


    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

    }
}
