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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

//класс с редактором рассадки учеников принимает на вход id урока
//todo если возникнет надобность - никаких изменений с бд до соранения лучше неделать, чтобы можно было отменить изменения
/*
* выводим уже рассаженных учеников
*    _________________________________
*   |                             сохр|
*   |_________________________________|
*   |                                 |
*   |    _______         _______      |
*   |   | + |имя|       |имя|имя|     |
*   |   |___|фам|       |фам|фам|     |
*   |    _______         _______      |
*   |   |имя|имя|       |имя| + |     |
*   |   |фам|фам|       |фам|___|     |
*   |    _______         _______      |
*   |   | + |имя|       | + | + |     |
*   |   |___|фам|       |___|___|     |
*   |    _______         _______      |
*   |   | + | + |       | + | + |     |
*   |   |___|___|       |___|___|     |
*   |                                 |
*   |                                 |
*   |                                 |
*   |                                 |
*   |_________________________________|
* по нажатию на + открывается список с нерассаженными учениками,
* из которого по нажатию выбираем ученика, которого хотим посадить на это место
*
* при сохранении добавляем / удаляем зависимости ученик-место
* */
public class SeatingRedactorActivity extends AppCompatActivity {

    final public static String LESSON_ID = "lessonId";
    long lessonId = 1;
    long cabinetId;
    long classId;
    int multiplier = 2;//todo зум

    static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seating_redactor);
        drawDesks();
    }

    private void drawDesks() {
        RelativeLayout room = (RelativeLayout) findViewById(R.id.seating_redactor_room);
        room.removeAllViews();

        int maxDeskX = 0;//максимальный отступ парты для расчёта размеров отображаемого layout
        int maxDeskY = 0;


        final DataBaseOpenHelper db = new DataBaseOpenHelper(this);

        lessonId = getIntent().getLongExtra(LESSON_ID, 1);//получаем id урока по умолчанию 1
        Cursor lessonCursor = db.getLessonById(lessonId);//курсор с уроком
        lessonCursor.moveToFirst();
        classId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableLessons.KEY_CLASS_ID));
        cabinetId = lessonCursor.getLong(lessonCursor.getColumnIndex(SchoolContract.TableLessons.KEY_CABINET_ID));

        //ставим заголовок имя урока
        getSupportActionBar().setTitle("редактирование урока \"" + lessonCursor.getString(
                lessonCursor.getColumnIndex(SchoolContract.TableLessons.COLUMN_NAME)) + "\"");

        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);//курсор с партами

        while (desksCursor.moveToNext()) {
            //создание парты
            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
            tempRelativeLayoutDesk.setBackgroundColor(Color.parseColor("#bce4af00"));

            RelativeLayout.LayoutParams tempRelativeLayoutDeskParams = new RelativeLayout.LayoutParams((int) dpFromPx(80 * multiplier), (int) dpFromPx(40 * multiplier));
            tempRelativeLayoutDeskParams.leftMargin = (int) dpFromPx(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)) * multiplier);
            tempRelativeLayoutDeskParams.topMargin = (int) dpFromPx(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)) * multiplier);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            Log.i("TeachersApp", "SeatingRedactorActivity - onCreate view desk:" + desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));

            //вычисляем максимальный отступ парты
            if (tempRelativeLayoutDeskParams.leftMargin > maxDeskX) {
                maxDeskX = tempRelativeLayoutDeskParams.leftMargin;
            }
            if (tempRelativeLayoutDeskParams.topMargin > maxDeskY) {
                maxDeskY = tempRelativeLayoutDeskParams.topMargin;
            }

            //проходим по местам на парте
            final Cursor placeCursor = db.getPlacesByDeskId(desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)));
            while (placeCursor.moveToNext()) {
                //создание места и ученика

                //id
                final long learnerId = db.getLearnerIdByLessonAndPlaceId(lessonId, placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));
                final long placeId = placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID));
                //создание места
                final LinearLayout tempPlaceLayout = new LinearLayout(this);
                tempPlaceLayout.setOrientation(LinearLayout.VERTICAL);
                tempPlaceLayout.setBackgroundColor(Color.parseColor("#bc8e6d02"));
                RelativeLayout.LayoutParams tempRelativeLayoutPlaceParams = new RelativeLayout.LayoutParams((int) dpFromPx((40 - 2) * multiplier), (int) dpFromPx((40 - 2) * multiplier));
                tempRelativeLayoutPlaceParams.leftMargin = (int) dpFromPx((1 + (40 * (placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.COLUMN_ORDINAL)) - 1))) * multiplier);
                tempRelativeLayoutPlaceParams.topMargin = (int) dpFromPx(multiplier);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                Log.i("TeachersApp", "SeatingRedactorActivity - onCreate view place:" + placeCursor.getLong(placeCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)));
//                //создание картинки ученика
//                final ImageView tempLernerImage = new ImageView(this);
//                final LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
//                tempLernerImage.setImageResource(R.drawable.learner_gray);//по умолчанию серая картинка
//
//                //создание текста ученика
//                final TextView tempLearnerText = new TextView(this);
//                final LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2F);
//                tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
//                tempLearnerText.setTextColor(Color.WHITE);
//
//                //создание кнопки добавить ученика
//                final ImageView tempImageAdd = new ImageView(getApplicationContext());
//                final LinearLayout.LayoutParams tempImageAddParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                tempImageAdd.setImageResource(R.drawable.ic_menu_add);
//                tempImageAdd.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        tempPlaceLayout.removeAllViews();
//                        ChooseLearnerDialogFragment dialogFragment = new ChooseLearnerDialogFragment(lessonId);
//                        dialogFragment.show(getFragmentManager(), "chooseLearners");
//                        handler = new Handler() {
//                            public void handleMessage(android.os.Message msg) {
//                                //todo сделать возврат -1 если ничего не выбрано и отменить вывод кнопок если список пуст
//                                //добавляем запись по id ученика и урока
//                                db.setLearnerOnPlace(lessonId, msg.what, placeId);
//                                //Cursor chooseCursor = db.getLearner(msg.what);
//                                //chooseCursor.moveToFirst();
//                                //обновляем TextView
//                                //tempLearnerText.setText(chooseCursor.getString(chooseCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
//                                //tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);
//                                //tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
//                                //chooseCursor.close();
//                                drawDesks();
//                            }
//                        };
//                    }
//                });
                if (learnerId != -1) {//если id ученика не равно -1 то выводим ученика иначе кнопку добавить ученика

                    final Cursor learnerCursor = db.getLearner(learnerId);//получаем ученика
                    learnerCursor.moveToFirst();

                    //создание картинки ученика
                    final ImageView tempLernerImage = new ImageView(this);
                    final LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
                    tempLernerImage.setImageResource(R.drawable.learner_gray);//по умолчанию серая картинка

                    //создание текста ученика
                    final TextView tempLearnerText = new TextView(this);
                    final LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 2F);
                    tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
                    tempLearnerText.setTextColor(Color.WHITE);

                    //картинка ученика
                    tempLernerImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //tempPlaceLayout.removeAllViews();
                            db.deleteAttitudeByLessonIdAndLearnerId(lessonId, learnerId);//удаляем запись по id ученика и урока
                            drawDesks();
                            //tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);
                        }
                    });
                    tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);

                    //текст ученика
                    tempLearnerText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //tempPlaceLayout.removeAllViews();
                            db.deleteAttitudeByLessonIdAndLearnerId(lessonId, learnerId);//удаляем запись по id ученика и урока
                            drawDesks();
                            //tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);

                        }
                    });
                    tempLearnerText.setText(learnerCursor.getString(learnerCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)));
                    tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
                    learnerCursor.close();
                } else {
                    //создание кнопки добавить ученика
                    final ImageView tempImageAdd = new ImageView(getApplicationContext());
                    final LinearLayout.LayoutParams tempImageAddParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    tempImageAdd.setImageResource(R.drawable.ic_menu_add);
                    tempImageAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tempPlaceLayout.removeAllViews();
                            ChooseLearnerDialogFragment dialogFragment = new ChooseLearnerDialogFragment(lessonId);
                            dialogFragment.show(getFragmentManager(), "chooseLearners");
                            handler = new Handler() {
                                public void handleMessage(android.os.Message msg) {
                                    //возврат -1 если ничего не выбрано иначе id ученика
                                    if (msg.what != -1) {
                                        //добавляем запись по id ученика и урока
                                        db.setLearnerOnPlace(lessonId, msg.what, placeId);
                                    }
                                    drawDesks();
                                }
                            };
                        }
                    });

                    if (db.getNotPutLearnersIdByLessonId(lessonId).size() != 0)
                        tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);
                }


                //добавление места в парту
                tempRelativeLayoutDesk.addView(tempPlaceLayout, tempRelativeLayoutPlaceParams);
            }
            placeCursor.close();

            //добавление парты в комнату
            room.addView(tempRelativeLayoutDesk, tempRelativeLayoutDeskParams);
        }


//        // Узнаем размеры экрана из ресурсов
//        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
//
//        // узнаем размеры экрана из класса Display
//        Display display = getWindowManager().getDefaultDisplay();
//        DisplayMetrics metricsB = new DisplayMetrics();
//        display.getMetrics(metricsB);

        room.setLayoutParams(new FrameLayout.LayoutParams((maxDeskX + (int) dpFromPx(80 + 40) * multiplier), (maxDeskY + (int) dpFromPx(40 + 40) * multiplier)));//(w, h)320*7 = 2240
        //room.setLayoutParams(new FrameLayout.LayoutParams(1120, 1120));//(w, h)320*7 = 2240
        Log.i("TeachersProject", "" + (maxDeskX + (80 + 40) * multiplier) + "" + (maxDeskY + (40 + 40) * multiplier));
        //todo под размер экрана может всё таки  scroll view
    }

    private float dpFromPx(float px) {
        return px * getApplicationContext().getResources().getDisplayMetrics().density;

    }
//    @Override
//    public void chooseLearnerDialogFragmentInterfaceMethod(long methodItem) {
//        choseLearnerId = methodItem;
//    }
}

class ChooseLearnerDialogFragment extends DialogFragment {//диалог по выбору не распределенного ученика

    long lessonId;

    public ChooseLearnerDialogFragment(long lessonId) {
        this.lessonId = lessonId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//билдер диалога
        final DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());//база
        final ArrayList<Long> learnersId = db.getNotPutLearnersIdByLessonId(lessonId);//лист с id нераспределенных по местам учеников
        String[] learnersNames = new String[learnersId.size()];//массив с именами учеников(пустой)
        for (int i = 0; i < learnersNames.length; i++) {//заполняем
            Cursor learnerTempCursor = db.getLearner(learnersId.get(i));//получаем ученика
            learnerTempCursor.moveToFirst();
            //получаем имя
            learnersNames[i] = learnerTempCursor.getString(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + learnerTempCursor.getString(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME));
            learnerTempCursor.close();
        }
        builder.setItems(learnersNames, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {//лист с именами
                Cursor learnerTempCursor = db.getLearner(learnersId.get(which));//получаем выбранного ученика
                learnerTempCursor.moveToFirst();
                //посылаем id выбранного ученика
                SeatingRedactorActivity.handler.sendEmptyMessage((int)
                        learnerTempCursor.getLong(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
                dismiss();
            }
            // The 'which' argument contains the index position
            // of the selected item
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.i("teachersApp", "SeatingRedactorActivity/ChooseLearnerDialogFragment/onDismiss");
        super.onDismiss(dialog);
        SeatingRedactorActivity.handler.sendEmptyMessage(-1);
    }
}

