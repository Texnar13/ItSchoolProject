package com.learning.texnar13.teachersprogect.listOf;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class ListOfDialog extends DialogFragment {

    //todo0 интересно, почему-то сохраняются id первого переименования
    String objectParameter;
    long parentId;
    ArrayList<Long> objectsId = new ArrayList<>();

//    LinearLayout content;
//    TextView title;
//    Context activityContext = getActivity().getApplicationContext();

    @Override//конструктор диалога
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
                                ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<ListOfAdapterObject>();//создаём лист с классами
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)), SchoolContract.TableClasses.NAME_TABLE_CLASSES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter =new ListOfAdapter(getActivity(), listOfClasses, false, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                                ((ListOfActivity)getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
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
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.setClassesNames(objectsId, className.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getClasses();//получаем классы
                                ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<ListOfAdapterObject>();//создаём лист с классами
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)), SchoolContract.TableClasses.NAME_TABLE_CLASSES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter =new ListOfAdapter(getActivity(), listOfClasses, false, SchoolContract.TableClasses.NAME_TABLE_CLASSES);
                                ((ListOfActivity)getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
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
            case SchoolContract.TableLearners.NAME_TABLE_LEARNERS:
                Log.i("TeachersApp", "ListOfDialog - onCreateDialog - out learners");

                final EditText firstName = new EditText(getActivity().getApplicationContext());
                firstName.setTextColor(Color.BLACK);
                firstName.setHint("имя");
                firstName.setHintTextColor(Color.GRAY);
                final EditText lastName = new EditText(getActivity().getApplicationContext());
                lastName.setTextColor(Color.BLACK);
                lastName.setHint("фамилия");
                lastName.setHintTextColor(Color.GRAY);

                linearLayout.addView(firstName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(lastName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


                if (objectsId.size() == 0) {
                    builder.setTitle("создание ученика");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.createLearner(lastName.getText().toString(), firstName.getText().toString(), parentId);
                            {//ставим адаптер
                                Cursor cursor = db.getLearnersByClassId(parentId);//получаем учеников
                                ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<ListOfAdapterObject>();//создаём лист с учениками
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)), SchoolContract.TableLearners.NAME_TABLE_LEARNERS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter = new ListOfAdapter(getActivity(), listOfClasses, false, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);
                                ((ListOfActivity)getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
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
                    builder.setTitle("редактирование ученика");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.setLearnerNameAndLastName(objectsId, lastName.getText().toString(), firstName.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getLearnersByClassId(parentId);//получаем учеников
                                ArrayList<ListOfAdapterObject> listOfClasses = new ArrayList<ListOfAdapterObject>();//создаём лист с учениками
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfClasses.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + cursor.getString(cursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)), SchoolContract.TableLearners.NAME_TABLE_LEARNERS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter =new ListOfAdapter(getActivity(), listOfClasses, false, SchoolContract.TableLearners.NAME_TABLE_LEARNERS);
                                ((ListOfActivity)getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
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

                if (objectsId.size() == 0) {
                    builder.setTitle("создание кабинета");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.createCabinet(cabinetName.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getCabinets();//получаем кабинеты
                                ArrayList<ListOfAdapterObject> listOfCabinets = new ArrayList<ListOfAdapterObject>();//создаём лист с кабинетами
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfCabinets.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter =new ListOfAdapter(getActivity(), listOfCabinets, false, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
                                ((ListOfActivity)getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
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
                                ArrayList<ListOfAdapterObject> listOfCabinets = new ArrayList<ListOfAdapterObject>();//создаём лист с классами
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfCabinets.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)), SchoolContract.TableCabinets.NAME_TABLE_CABINETS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter =new ListOfAdapter(getActivity(), listOfCabinets, false, SchoolContract.TableCabinets.NAME_TABLE_CABINETS);
                                ((ListOfActivity)getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
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
            case SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES:
                Log.i("TeachersApp", "ListOfDialog - onCreateDialog - out schedules");

                final EditText schedulesName = new EditText(getActivity().getApplicationContext());
                schedulesName.setTextColor(Color.BLACK);
                schedulesName.setHint("название расписания");
                schedulesName.setHintTextColor(Color.GRAY);
                linearLayout.addView(schedulesName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                if (objectsId.size() == 0) {
                    builder.setTitle("создание расписания");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.createSchedule(schedulesName.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getSchedules();//получаем расписания
                                ArrayList<ListOfAdapterObject> listOfSchedules = new ArrayList<ListOfAdapterObject>();//создаём лист с расписаниями
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfSchedules.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableSchedules.COLUMN_NAME)), SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSchedules.KEY_SCHEDULE_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter =new ListOfAdapter(getActivity(), listOfSchedules, false, SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES);
                                ((ListOfActivity)getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
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
                    builder.setTitle("редактирование расписания");
                    builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                            db.setSchedulesName(objectsId, schedulesName.getText().toString());
                            {//ставим адаптер
                                Cursor cursor = db.getSchedules();//получаем расписания
                                ArrayList<ListOfAdapterObject> listOfSchedules = new ArrayList<ListOfAdapterObject>();//создаём лист с расписаниями
                                while (cursor.moveToNext()) {//курсор в лист
                                    listOfSchedules.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableSchedules.COLUMN_NAME)), SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableSchedules.KEY_SCHEDULE_ID))));
                                }
                                cursor.close();
                                ListOfAdapter adapter =new ListOfAdapter(getActivity(), listOfSchedules, false, SchoolContract.TableSchedules.NAME_TABLE_SCHEDULES);
                                ((ListOfActivity)getActivity()).adapter = adapter;//обновляем адаптер в activity чтобы метод   dialog.objectsId = adapter.getIdCheckedListOfAdapterObjects(); имел новый список
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
            case SchoolContract.TableLessons.NAME_TABLE_LESSONS:
                Log.i("TeachersApp", "ListOfDialog - onCreateDialog - out lessons " + objectsId);

                final EditText lessonName = new EditText(getActivity().getApplicationContext());
                lessonName.setTextColor(Color.BLACK);
                lessonName.setHint("название урока");
                lessonName.setHintTextColor(Color.GRAY);
                linearLayout.addView(lessonName, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                final EditText dateBegin = new EditText(getActivity().getApplicationContext());
                final EditText dateEnd = new EditText(getActivity().getApplicationContext());

                //выпадающие списки
                final Spinner classesSpinner = new Spinner(getActivity().getApplicationContext());
                final Spinner cabinetsSpinner = new Spinner(getActivity().getApplicationContext());
                final DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                final Cursor cursorWisClasses = db.getClasses();
                final Cursor cursorWisCabinets = db.getCabinets();
                final ArrayList<Long> classesId = new ArrayList<>();
                final ArrayList<Long> cabinetId = new ArrayList<>();

                if (objectsId.size() < 2) {

                    ArrayList<String> classesNames = new ArrayList<>();

                    while (cursorWisClasses.moveToNext()) {
                        classesId.add(cursorWisClasses.getLong(cursorWisClasses.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID)));
                        classesNames.add(cursorWisClasses.getString(cursorWisClasses.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)));
                    }
                    classesSpinner.setAdapter(new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.list_of_dialog_spiner, R.id.list_of_dialog_spiner_text_view, classesNames.toArray(new String[classesNames.size()])));
                    linearLayout.addView(classesSpinner, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    ArrayList<String> cabinetsNames = new ArrayList<>();
                    while (cursorWisCabinets.moveToNext()) {
                        cabinetId.add(cursorWisCabinets.getLong(cursorWisCabinets.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID)));
                        cabinetsNames.add(cursorWisCabinets.getString(cursorWisCabinets.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME)));
                    }
                    cabinetsSpinner.setAdapter(new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.list_of_dialog_spiner, R.id.list_of_dialog_spiner_text_view, cabinetsNames.toArray(new String[cabinetsNames.size()])));
                    linearLayout.addView(cabinetsSpinner, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


                    dateBegin.setTextColor(Color.BLACK);
                    dateBegin.setHint("дата начала");
                    dateBegin.setHintTextColor(Color.GRAY);
                    linearLayout.addView(dateBegin, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    dateEnd.setTextColor(Color.BLACK);
                    dateEnd.setHint("дата окончания");
                    dateEnd.setHintTextColor(Color.GRAY);
                    linearLayout.addView(dateEnd, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                }
                switch (objectsId.size()) {
                    case 0:

                        builder.setTitle("создание урока");
                        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                db.createLesson(lessonName.getText().toString(),
                                        parentId,
                                        Long.parseLong(dateBegin.getText().toString()),
                                        Long.parseLong(dateEnd.getText().toString()),
                                        classesId.get(classesSpinner.getSelectedItemPosition()),
                                        cabinetId.get(cabinetsSpinner.getSelectedItemPosition()));
                                {//ставим адаптер
                                    Cursor cursor = db.getLessonsByScheduleId(parentId);//получаем уроки по id расписания
                                    ArrayList<ListOfAdapterObject> listOfLessons = new ArrayList<ListOfAdapterObject>();//создаём лист с уроками
                                    while (cursor.moveToNext()) {//курсор в лист
                                        listOfLessons.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)), SchoolContract.TableLessons.NAME_TABLE_LESSONS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessons.KEY_LESSON_ID))));
                                    }
                                    cursor.close();
                                    ((ListView) getActivity().findViewById(R.id.content_list_of_list_view)).setAdapter(new ListOfAdapter(getActivity(), listOfLessons, false, SchoolContract.TableLessons.NAME_TABLE_LESSONS));
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
                        break;
                    case 1:
                        builder.setTitle("редактирование урока");
                        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cursorWisClasses.move(classesSpinner.getSelectedItemPosition() - 1);
                                cursorWisCabinets.move(cabinetsSpinner.getSelectedItemPosition() - 1);

                                db.setLessonParameters(objectsId.get(0), lessonName.getText().toString(), parentId, Long.parseLong(dateBegin.getText().toString()), Long.parseLong(dateEnd.getText().toString()), cursorWisClasses.getLong(cursorWisClasses.getColumnIndex(SchoolContract.TableClasses.KEY_CLASS_ID)), cursorWisCabinets.getLong(cursorWisCabinets.getColumnIndex(SchoolContract.TableCabinets.KEY_CABINET_ID)));
                                {//ставим адаптер
                                    Cursor cursor = db.getLessonsByScheduleId(parentId);//получаем уроки по id расписания
                                    ArrayList<ListOfAdapterObject> listOfLessons = new ArrayList<ListOfAdapterObject>();//создаём лист с уроками
                                    while (cursor.moveToNext()) {//курсор в лист
                                        listOfLessons.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)), SchoolContract.TableLessons.NAME_TABLE_LESSONS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessons.KEY_LESSON_ID))));
                                    }
                                    cursor.close();
                                    ((ListView) getActivity().findViewById(R.id.content_list_of_list_view)).setAdapter(new ListOfAdapter(getActivity(), listOfLessons, false, SchoolContract.TableLessons.NAME_TABLE_LESSONS));
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
                        break;
                    default:
                        builder.setTitle("редактирование уроков");
                        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());
                                db.setLessonsNames(objectsId, lessonName.getText().toString());
                                {//ставим адаптер
                                    Cursor cursor = db.getLessonsByScheduleId(parentId);//получаем уроки по id расписания
                                    ArrayList<ListOfAdapterObject> listOfLessons = new ArrayList<ListOfAdapterObject>();//создаём лист с уроками
                                    while (cursor.moveToNext()) {//курсор в лист
                                        listOfLessons.add(new ListOfAdapterObject(cursor.getString(cursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)), SchoolContract.TableLessons.NAME_TABLE_LESSONS, cursor.getLong(cursor.getColumnIndex(SchoolContract.TableLessons.KEY_LESSON_ID))));
                                    }
                                    cursor.close();
                                    ((ListView) getActivity().findViewById(R.id.content_list_of_list_view)).setAdapter(new ListOfAdapter(getActivity(), listOfLessons, false, SchoolContract.TableLessons.NAME_TABLE_LESSONS));
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
                        break;
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
//                            dismiss();
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
////                            dismiss();
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
////                            dismiss();
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
