package com.learning.texnar13.teachersprogect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

public class SeatingRedactorActivity extends AppCompatActivity {

    final public static String LESSON_ID = "lessonId";
    long lessonId = 1;
    long cabinetId;
    long classId;
    int multiplicator = 2;

    static Handler handler;


    final ArrayList<RedactorLearnerAndGrade> gradeArrayList = new ArrayList<>();//массив с оценками за этот урок;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seating_redactor);

        RelativeLayout room = (RelativeLayout) findViewById(R.id.seating_redactor_room);

        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);


        lessonId = getIntent().getLongExtra(LESSON_ID, 1);
        Cursor lessonCursor = db.getLessonById(lessonId);//курсор с уроком
        lessonCursor.moveToFirst();
        classId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableLessons.KEY_CLASS_ID));
        cabinetId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableLessons.KEY_CABINET_ID));

        getSupportActionBar().setTitle("редактирование урока " + lessonCursor.getString(lessonCursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)));

        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);//курсор с партами

        int i = -1;//щётчик учеников

        //todo0 берём макс значение парты по X и по y прибавляем отступ минимальных и размер мах парты получаем размер layout
        while (desksCursor.moveToNext()) {
            //создание парты
            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
            tempRelativeLayoutDesk.setBackgroundColor(Color.parseColor("#bce4af00"));

            RelativeLayout.LayoutParams tempRelativeLayoutDeskParams = new RelativeLayout.LayoutParams((int) dpFromPx(80 * multiplicator), (int) dpFromPx(40 * multiplicator));
            tempRelativeLayoutDeskParams.leftMargin = (int) dpFromPx(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * multiplicator);
            tempRelativeLayoutDeskParams.topMargin = (int) dpFromPx(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * multiplicator);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            Log.i("TeachersApp", "SeatingRedactorActivity - onCreate view desk:" + desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));

            //создание места
            final Cursor placeCursor = db.getPlacesByDeskId(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
            while (placeCursor.moveToNext()) {

                final LinearLayout tempPlaceLayout = new LinearLayout(this);
                tempPlaceLayout.setOrientation(LinearLayout.VERTICAL);
                tempPlaceLayout.setBackgroundColor(Color.parseColor("#bc8e6d02"));

                RelativeLayout.LayoutParams tempRelativeLayoutPlaceParams = new RelativeLayout.LayoutParams((int) dpFromPx((40 - 2) * multiplicator), (int) dpFromPx((40 - 2) * multiplicator));
                tempRelativeLayoutPlaceParams.leftMargin = (int) dpFromPx((1 + (40 * (placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.COLUMN_ORDINAL)) - 1))) * multiplicator);
                tempRelativeLayoutPlaceParams.topMargin = (int) dpFromPx(1);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                Log.i("TeachersApp", "SeatingRedactorActivity - onCreate view place:" + placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));

                //создание ученика
                final long learnerId = db.getLearnerIdByLessonAndPlaceId(lessonId, placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));


                //создание view на парте
                final long placeId = placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID));
                //картинка ученика
                final ImageView tempLernerImage = new ImageView(this);
                final LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
                tempLernerImage.setImageResource(R.drawable.learner_gray);//по умолчанию серая картинка
                //текст ученика
                final TextView tempLearnerText = new TextView(this);
                final LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2F);
                tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
                tempLearnerText.setTextColor(Color.WHITE);
                //кнопка добавить ученика
                final ImageView tempImageAdd = new ImageView(getApplicationContext());
                final LinearLayout.LayoutParams tempImageAddParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                tempImageAdd.setImageResource(R.drawable.ic_menu_add);
                tempImageAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tempPlaceLayout.removeAllViews();
                        ChooseLearnerDialogFragment dialogFragment = new ChooseLearnerDialogFragment(classId);
                        dialogFragment.show(getFragmentManager(), "chooseLearners");
                        handler = new Handler() {
                            public void handleMessage(android.os.Message msg) {
                                //добавляем запись по id ученика и урока
                                db.setLearnerOnPlace(lessonId, msg.what, placeId);
                                Cursor chooseCursor = db.getLearner(msg.what);
                                chooseCursor.moveToFirst();
                                //обновляем TextView
                                tempLearnerText.setText(chooseCursor.getString(chooseCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
                                tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);
                                tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
                            }
                        };
                    }
                });

                if (learnerId != -1) {
                    //оценки
                    i++;
                    final int tempGradeId = i;
                    gradeArrayList.add(i, new RedactorLearnerAndGrade(learnerId, placeId));

                    final Cursor learnerCursor = db.getLearner(learnerId);//получаем ученика
                    learnerCursor.moveToFirst();

                    //картинка ученика
                    tempLernerImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tempPlaceLayout.removeAllViews();
                            db.deleteAttitudeByLessonIdAndLearnerId(lessonId, learnerId);//удаляем запись по id ученика и урока
                            tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);

                        }
                    });
//                    tempLernerImage.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//
//                        }
//                    });
                    tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);

                    //текст ученика
                    tempLearnerText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tempPlaceLayout.removeAllViews();
                            db.deleteAttitudeByLessonIdAndLearnerId(lessonId, learnerId);//удаляем запись по id ученика и урока
                            tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);

                        }
                    });
//                    tempLearnerText.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
//                        @Override
//                        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
//                            contextMenu.add(0, 0, 0, "нет оценки").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                                @Override
//                                public boolean onMenuItemClick(MenuItem menuItem) {
//                                    tempLernerImage.setImageResource(R.drawable.learner_gray);
//                                    gradeArrayList.get(tempGradeId).setPlaceId(0);
//                                    return true;
//                                }
//                            });
//                        }
//                    });
                    tempLearnerText.setText(learnerCursor.getString(learnerCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
                    tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
                } else {
                    tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);
                }
                Log.i("TeachersApp", "SeatingRedactorActivity!!! " + learnerId);

                //добавление места в парту
                tempRelativeLayoutDesk.addView(tempPlaceLayout, tempRelativeLayoutPlaceParams);
            }
            placeCursor.close();

            //добавление парты в комнату
            room.addView(tempRelativeLayoutDesk, tempRelativeLayoutDeskParams);
        }
    }

    private float dpFromPx(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;

    }

//    @Override
//    public void chooseLearnerDialogFragmentInterfaceMethod(long methodItem) {
//        choseLearnerId = methodItem;
//    }
}

class RedactorLearnerAndGrade {
    private long learnerId;
    private long placeId;

    public RedactorLearnerAndGrade(long learnerId, long placeId) {
        this.learnerId = learnerId;
        this.placeId = placeId;
    }

    public long getLearnerId() {
        return learnerId;
    }

    public void setLearnerId(long learnerId) {
        this.learnerId = learnerId;
    }

    public long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(long placeId) {
        this.placeId = placeId;
    }
}

class ChooseLearnerDialogFragment extends DialogFragment {

    long classId;

    public ChooseLearnerDialogFragment(long classId) {
        this.classId = classId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//билдер диалога
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());//база
        final Cursor learnersCursor = db.getLearnersByClassId(classId);//курсор с учениками переданного класса
        String[] learnersNames = new String[learnersCursor.getCount()];//Cursor в String[]
        for (int i = 0; i < learnersNames.length; i++) {
            learnersCursor.moveToNext();
            learnersNames[i] = learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME));
        }
        builder.setItems(learnersNames, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                learnersCursor.moveToFirst();
                learnersCursor.move(which);
                SeatingRedactorActivity.handler.sendEmptyMessage((int)
                        learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
                dismiss();
            }
            // The 'which' argument contains the index position
            // of the selected item
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

interface chooseLearnerDialogFragmentInterface {
    void chooseLearnerDialogFragmentInterfaceMethod(long methodItem);
}

