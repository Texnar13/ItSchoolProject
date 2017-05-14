package com.learning.texnar13.teachersprogect.listOf;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class ListOfDialog extends DialogFragment {

//TODO!!! интересно, почему-то сохраняются id первого переименования
    String objectParameter;
    long parentId;
    ArrayList<Long> objectsId = new ArrayList<>();

    LinearLayout content;
    TextView title;
    //Context activityContext = getActivity().getApplicationContext();

    @Override//конструктор диалога
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.new_list_of_dialog, null);

        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.new_list_of_dialog_layout);
        switch (objectParameter) {
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:

                final EditText name = new EditText(getActivity().getApplicationContext());
                name.setHint("имя класса");
                linearLayout.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                if (objectsId.size() == 0) {
                    builder.setTitle("создание класса");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.createClass(name.getText().toString());
                            ListView listView = (ListView) getActivity().findViewById(R.id.content_list_of_list_view);
                            listView.setAdapter(new ListOfAdapter(getActivity(), db.getClasses(), false));//получаем классы
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
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.setClassesNames(objectsId, name.getText().toString());
                            ListView listView = (ListView) getActivity().findViewById(R.id.content_list_of_list_view);
                            listView.setAdapter(new ListOfAdapter(getActivity(), db.getClasses(), false));
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


            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS:
                if (objectsId.size() == 0) {
                    builder.setTitle("создание ученика");
                    builder.setPositiveButton("имя позитивной кнопки", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.setNegativeButton("имя негативной кнопки", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                } else {
                    builder.setTitle("редактирование ученика");
                    builder.setPositiveButton("имя позитивной кнопки", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.setNegativeButton("имя негативной кнопки", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                }
                break;
            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
                if (objectsId.size() == 0) {
                    builder.setTitle("создание кабинета");
                    builder.setPositiveButton("имя позитивной кнопки", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.setNegativeButton("имя негативной кнопки", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                } else {
                    builder.setTitle("редактирование кабинета");
                    builder.setPositiveButton("имя позитивной кнопки", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.setNegativeButton("имя негативной кнопки", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                }
                break;
        }
        builder.setView(dialogLayout);//view в центре диалога
        return builder.create();// Create the AlertDialog object and return it
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        getDialog().setTitle("редактор");
//
//        View v = inflater.inflate(R.layout.list_of_dialog, null);
//        content = (LinearLayout) v.findViewById(R.id.list_of_dialog_content);
//        title = (TextView) v.findViewById(R.id.list_of_dialog_title);
//
//        switch (objectParameter) {
//            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:
//                if (objectsId.size() == 0) {
//                    title.setText("создание класса");
//                    final EditText name = new EditText(activityContext);
//                    name.setHint("имя класса");
//                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
//                            db.createClass(name.getText().toString());
//                            ListView listView = (ListView) getActivity().findViewById(R.id.content_list_of_list_view);
//                            listView.setAdapter(new ListOfAdapter(getActivity(), db.getClasses(), false));//получаем классы
//                            db.close();
//                            dismiss();
//                        }
//                    });
//                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            dismiss();
//                        }
//                    });
//                } else {
//                    title.setText("ред. классы");
//                    final EditText name = new EditText(activityContext);
//                    name.setHint("новое имя класса");
//                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//
//                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
//                            db.setClassesNames(objectsId, name.getText().toString());
//                            ListView listView = (ListView) getActivity().findViewById(R.id.content_list_of_list_view);
//                            listView.setAdapter(new ListOfAdapter(getActivity(), db.getClasses(), false));
//                            db.close();
////                            Intent intent;
////                            intent = new Intent(activityContext, ListOfActivity.class);
////                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
////                            startActivity(intent);
//                            dismiss();//todo попробовать сделать в главной активити метод вывода информации и её отображение заново
//                        }
//                    });
//                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            dismiss();
//                        }
//                    });
//                }
//                break;
//            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS:
//                if (objectsId.size() == 0) {
//                    title.setText("создание ученика");
//                    final EditText name = new EditText(activityContext);
//                    name.setHint("имя");
//                    final EditText lastName = new EditText(activityContext);
//                    lastName.setHint("фамилия");
//                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                    content.addView(lastName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//
//                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
//                            db.createLearner(lastName.getText().toString(), name.getText().toString(), parentId);
//                            db.close();
//                            Intent intent;
//                            intent = new Intent(activityContext, ListOfActivity.class);
//                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);
//                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, parentId);
//                            startActivity(intent);
//                            dismiss();
//                        }
//                    });
//                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            dismiss();
//                        }
//                    });
////                } else {
////                    title.setText("ред. ученика");
////                    final EditText name = new EditText(activityContext);
////                    name.setHint("имя");
////                    final EditText lastName = new EditText(activityContext);
////                    lastName.setHint("фамилия");
////                    {
////                        Cursor learnerCursor = new DataBaseOpenHelper(activityContext).getLearner(objectsId);
////                        learnerCursor.moveToFirst();
////                        name.setText(learnerCursor.getString(learnerCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)));
////                        lastName.setText(learnerCursor.getString(learnerCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
////                        learnerCursor.close();
////                    }
////                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
////                    content.addView(lastName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
////
////                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
////                            db.setLearnerNameAndLastName(new long[]{objectsId}, name.getText().toString(), lastName.getText().toString());
////                            db.close();
////                            Intent intent;
////                            intent = new Intent(activityContext, ListOfActivity.class);
////                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);
////                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, parentId);
////                            startActivity(intent);
////                            dismiss();//todo попробовать сделать в главной активити метод вывода информации и её отображение заново
////                        }
////                    });
////                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            dismiss();
////                        }
////                    });
//                }
//                break;
//            case SchoolContract.TableCabinets.NAME_TABLE_CABINETS:
//                if (objectsId.size() == 0) {
//                    title.setText("создание кабинета");
//                    final EditText name = new EditText(activityContext);
//                    name.setHint("название кабинета");
//                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
//                            db.createCabinet(name.getText().toString());
//                            db.close();
//                            Intent intent;
//                            intent = new Intent(activityContext, ListOfActivity.class);
//                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
//                            startActivity(intent);
//                            dismiss();
//                        }
//                    });
//                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            dismiss();
//                        }
//                    });
////                } else {
////                    title.setText("ред. имя кабинета");
////                    final EditText name = new EditText(activityContext);
////                    name.setHint("название");
////                    {
////                        Cursor cabinetNameCursor = new DataBaseOpenHelper(activityContext).getCabinets(objectsId);
////                        cabinetNameCursor.moveToFirst();
////                        name.setText(cabinetNameCursor.getString(cabinetNameCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)));
////                        cabinetNameCursor.close();
////                    }
////                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
////
////                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
////                            db.setCabinetName(new long[]{objectsId}, name.getText().toString());
////                            db.close();
////                            Intent intent;
////                            intent = new Intent(activityContext, ListOfActivity.class);
////                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
////                            startActivity(intent);
////                            dismiss();//todo попробовать сделать в главной активити метод вывода информации и её отображение заново
////                        }
////                    });
////                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View view) {
////                            dismiss();
////                        }
////                    });
//                }
//                break;
//        }
//        return v;
//    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);


    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

    }
}
