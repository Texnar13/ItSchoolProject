package com.learning.texnar13.teachersprogect;


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
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

public class ListOfDialog extends DialogFragment {

    final int newObject = -1;

    Context activityContext;
    String objectParameter;
    String dopParameter;
    long objectId = newObject;


    LinearLayout content;
    TextView title;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("редактор");

        View v = inflater.inflate(R.layout.list_of_dialog, null);
        content = (LinearLayout) v.findViewById(R.id.list_of_dialog_content);
        title = (TextView) v.findViewById(R.id.list_of_dialog_title);

        switch (objectParameter) {
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:
                if (objectId == newObject) {
                    title.setText("создание класса");
                    final EditText name = new EditText(activityContext);
                    name.setHint("имя класса");
                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
                            db.createClass(name.getText().toString());
                            db.close();
                            Intent intent;
                            intent = new Intent(activityContext, ListOfActivity.class);
                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                            startActivity(intent);
                            dismiss();
                        }
                    });
                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dismiss();
                        }
                    });
                } else {
                    title.setText("ред. класс");
                    final EditText name = new EditText(activityContext);
                    name.setHint("имя класса");
                    {
                        Cursor classNameCursor = new DataBaseOpenHelper(activityContext).getClasses(objectId);
                        classNameCursor.moveToFirst();
                        name.setText(classNameCursor.getString(classNameCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
                        classNameCursor.close();
                    }
                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
                            db.setClassName(objectId, name.getText().toString());
                            db.close();
                            Intent intent;
                            intent = new Intent(activityContext, ListOfActivity.class);
                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                            startActivity(intent);
                            dismiss();//todo попробовать сделать в главной активити метод вывода информации и её отображение заново
                        }
                    });
                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dismiss();
                        }
                    });
                }
                break;
            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS:
                if (objectId == newObject) {
                    title.setText("создание ученика");
                    final EditText name = new EditText(activityContext);
                    name.setHint("имя");
                    final EditText lastName = new EditText(activityContext);
                    name.setHint("фамилия");
                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    content.addView(lastName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
                            db.createLearner(lastName.getText().toString(),name.getText().toString(),Long.parseLong(dopParameter));
                            db.close();
                            Intent intent;
                            intent = new Intent(activityContext, ListOfActivity.class);
                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);
                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, Long.parseLong(dopParameter));
                            startActivity(intent);
                            dismiss();
                        }
                    });
                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dismiss();
                        }
                    });
                } else {
                    title.setText("ред. ученика");
                    final EditText name = new EditText(activityContext);
                    name.setHint("имя");
                    final EditText lastName = new EditText(activityContext);
                    name.setHint("фамилия");
                    {
                        Cursor learnerCursor = new DataBaseOpenHelper(activityContext).getLearner(objectId);
                        learnerCursor.moveToFirst();
                        name.setText(learnerCursor.getString(learnerCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)));
                        lastName.setText(learnerCursor.getString(learnerCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
                        learnerCursor.close();
                    }
                    content.addView(name, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    content.addView(lastName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    v.findViewById(R.id.list_of_dialog_save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(activityContext);
                            db.setLearnerNameAndLastName(objectId, name.getText().toString(),lastName.getText().toString());
                            db.close();
                            Intent intent;
                            intent = new Intent(activityContext, ListOfActivity.class);
                            intent.putExtra(ListOfActivity.LIST_PARAMETER, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);
                            intent.putExtra(ListOfActivity.DOP_LIST_PARAMETER, Long.parseLong(dopParameter));
                            startActivity(intent);
                            dismiss();//todo попробовать сделать в главной активити метод вывода информации и её отображение заново
                        }
                    });
                    v.findViewById(R.id.list_of_dialog_repeal).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dismiss();
                        }
                    });
                }
                break;
        }
        return v;
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
