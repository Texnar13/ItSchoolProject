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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.util.ArrayList;

//класс с редактором рассадки учеников принимает на вход id урока
//todo если возникнет надобность - никаких изменений с бд до сохранения лучше не делать, чтобы можно было отменить изменения
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

    //константы
    //final public static String SUBJECT_ID = "lessonId";
    final public static String CLASS_ID = "classId";
    final public static String CABINET_ID = "cabinetId";

    //технические переменные
    static Handler handler;
    int maxDeskX = 0;
    int maxDeskY = 0;
    float multiplier;

    //главные переменные
    long cabinetId;
    long classId;

    //массивы с данными из бд
    ArrayList<DeskUnit> desksList = new ArrayList<>();
    ArrayList<LearnerUnit> learnersList = new ArrayList<>();
    ArrayList<AttitudeUnit> attitudesList = new ArrayList<>();
    //long []  = { };

    //параметры view
    RelativeLayout.LayoutParams tempRelativeLayoutDeskParams;
    RelativeLayout.LayoutParams tempRelativeLayoutPlaceParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seating_redactor);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//кнопка назад в actionBar

        //----инициализация переменных----
        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
        multiplier = db.getInterfaceSizeBySettingsProfileId(1) / 1000f * getResources().getInteger(R.integer.desks_screen_multiplier);
        classId = getIntent().getLongExtra(CLASS_ID, -1);
        cabinetId = getIntent().getLongExtra(CABINET_ID, -1);
        if (classId == -1 || cabinetId == -1) {
            Log.e("TeachersApp", "SeatingRedactorActivity - not  intent: classId = " + classId + " cabinetId = " + cabinetId);
            finish();
            Toast toast = Toast.makeText(this, "не выбран класс или кабинет для редактирования", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        //----расставляем постоянные данные----
        // ставим заголовок имя урока
        Cursor classCursor = db.getClasses(classId);
        classCursor.moveToFirst();
        Cursor cabinetCursor = db.getCabinets(cabinetId);
        cabinetCursor.moveToFirst();
        setTitle(getResources().getString(R.string.title_activity_seating_redactor_first) + " " +
                classCursor.getString(classCursor.getColumnIndex(SchoolContract.TableClasses.COLUMN_CLASS_NAME)) +
                " " + getResources().getString(R.string.title_activity_seating_redactor_second) + " " +
                cabinetCursor.getString(cabinetCursor.getColumnIndex(SchoolContract.TableCabinets.COLUMN_NAME))
        );
        classCursor.close();
        cabinetCursor.close();

        // выводим учеников из базы данных
        Cursor learnersCursor = db.getLearnersByClassId(classId);
        while (learnersCursor.moveToNext()) {
            learnersList.add(new LearnerUnit(
                    learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)),
                    learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME)),
                    learnersCursor.getString(learnersCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)),
                    learnersCursor.getLong(learnersCursor.getColumnIndex(SchoolContract.TableLearners.KEY_CLASS_ID))
            ));
        }
        learnersCursor.close();

        // выводим парты из базы данных
        Cursor desksCursor = db.getDesksByCabinetId(cabinetId);//курсор с партами
        while (desksCursor.moveToNext()) {
            //все параметры самой парты
            DeskUnit deskUnit = new DeskUnit(
                    desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.KEY_DESK_ID)),
                    desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_X)),
                    desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_Y)),
                    desksCursor.getLong(desksCursor.getColumnIndex(SchoolContract.TableDesks.COLUMN_NUMBER_OF_PLACES))
            );
            desksList.add(deskUnit);
            //вычисляем максимальный отступ парты
            if (deskUnit.x * 100 * multiplier > maxDeskX) {//максимальный отступ парты для расчёта размеров отображаемого layout
                maxDeskX = (int) (deskUnit.x * 100 * multiplier);
            }
            if (deskUnit.y * 100 * multiplier > maxDeskY) {
                maxDeskY = (int) (deskUnit.y * 100 * multiplier);
            }
            //выводим места на партах из базы данных
            Cursor placesCursor = db.getPlacesByDeskId(deskUnit.id);
            while (placesCursor.moveToNext()) {
                PlaceUnit placeUnit = new PlaceUnit(
                        placesCursor.getLong(placesCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_PLACE_ID)),
                        placesCursor.getLong(placesCursor.getColumnIndex(SchoolContract.TablePlaces.KEY_DESK_ID)),
                        placesCursor.getLong(placesCursor.getColumnIndex(SchoolContract.TablePlaces.COLUMN_ORDINAL)));
                deskUnit.placesList.add(placeUnit);
                // выводим зависимости из базы данных
                for (int i = 0; i < learnersList.size(); i++) {//пробегаемся по ученикам, ищем совпадения с этим местом
                    Cursor attitudesCursor = db.getAttitudeByLearnerIdAndPlaceId(learnersList.get(i).id, placeUnit.id);
                    while (attitudesCursor.moveToNext()) {
                        attitudesList.add(new AttitudeUnit(
                                attitudesCursor.getLong(attitudesCursor.getColumnIndex(SchoolContract.TableLearnersOnPlaces.KEY_ATTITUDES_ID)),
                                attitudesCursor.getLong(attitudesCursor.getColumnIndex(SchoolContract.TableLearnersOnPlaces.KEY_LEARNER_ID)),
                                attitudesCursor.getLong(attitudesCursor.getColumnIndex(SchoolContract.TableLearnersOnPlaces.KEY_PLACE_ID))
                        ));
                    }
                    attitudesCursor.close();
                }
            }
            placesCursor.close();
        }
        desksCursor.close();

        //----параметры view----
        //параметры для всех парт
        //параметры для всех мест


        //----выводим всё----
        drawDesks(db);
    }


    //------------вывести парты------------
    private void drawDesks(final DataBaseOpenHelper db) {
        RelativeLayout room = (RelativeLayout) findViewById(R.id.seating_redactor_room);
        room.removeAllViews();

        for (DeskUnit deskUnit : desksList) {//пробегаемся по выгруженным партам

            //создание парты
            RelativeLayout tempRelativeLayoutDesk = new RelativeLayout(this);
            tempRelativeLayoutDesk.setBackgroundColor(Color.LTGRAY);
            tempRelativeLayoutDesk.setBackgroundResource(R.drawable.start_screen_3_3_green_spot);
            //настраиваем параметры под конкретную парту
            tempRelativeLayoutDeskParams = new RelativeLayout.LayoutParams(
                    (int) pxFromDp(1000 * deskUnit.countOfPlaces * multiplier),
                    (int) pxFromDp(1000 * multiplier));//размеры проставляются далее индивидуально
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tempRelativeLayoutDeskParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            tempRelativeLayoutDeskParams.leftMargin =
                    (int) pxFromDp(deskUnit.x * 25 * multiplier);
            tempRelativeLayoutDeskParams.topMargin =
                    (int) pxFromDp(deskUnit.y * 25 * multiplier);
            Log.i("TeachersApp", "SeatingRedactorActivity - draw - view desk:" + deskUnit.id);

            for (final PlaceUnit placeUnit : deskUnit.placesList) {//проходим по местам на парте
                //создание места и ученика

                //получаем id ученика
                long learnerId = -1;
                int attitudeIndex = -1;
                for (int i = 0; i < attitudesList.size(); i++) {
                    if (attitudesList.get(i).placeId == placeUnit.id) {
                        learnerId = attitudesList.get(i).learnerId;
                        attitudeIndex = i;
                        break;
                    }
                }
                //создание места
                final LinearLayout tempPlaceLayout = new LinearLayout(this);
                tempPlaceLayout.setOrientation(LinearLayout.VERTICAL);
                //tempPlaceLayout.setBackgroundColor(Color.parseColor("#e4ea7e"));
                //настраиваем параметры под конкретное место
                tempRelativeLayoutPlaceParams = new RelativeLayout.LayoutParams(
                        (int) pxFromDp((1000 - 50) * multiplier),
                        (int) pxFromDp((1000 - 50) * multiplier));
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                tempRelativeLayoutPlaceParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                tempRelativeLayoutPlaceParams.leftMargin = (int) pxFromDp((25 + (1000 * (placeUnit.ordinalNumber - 1))) * multiplier);
                tempRelativeLayoutPlaceParams.topMargin = (int) pxFromDp(25 * multiplier);
                Log.i("TeachersApp", "SeatingRedactorActivity - draw view place:" + placeUnit.id);
                //садим ученика на место
                if (learnerId != -1) {//если id ученика не равно -1 то выводим ученика иначе кнопку добавить ученика
                    //final переменная
                    final long finalLearnerId = learnerId;
                    final int finalAttitudeIndex = attitudeIndex;
                    //создание картинки ученика
                    final ImageView tempLernerImage = new ImageView(this);
                    final LinearLayout.LayoutParams tempLernerImageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1F);
                    tempLernerImage.setImageResource(R.drawable.lesson_learner_0_gray);//по умолчанию серая картинка

                    //создание текста ученика
                    final TextView tempLearnerText = new TextView(this);
                    tempLearnerText.setTextSize(200 * multiplier);
                    final LinearLayout.LayoutParams tempLearnerTextParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 3F);
                    tempLearnerText.setGravity(Gravity.CENTER_HORIZONTAL);
                    tempLearnerText.setTextColor(Color.GRAY);
                    String learnerLastName = "";
                    //получаем текст ученика
                    for (int i = 0; i < learnersList.size(); i++) {
                        if (learnersList.get(i).id == learnerId) {
                            learnerLastName = learnersList.get(i).lastName;
                            break;
                        }
                    }
                    tempLearnerText.setText(learnerLastName);
                    //картинка ученика
                    tempLernerImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            db.deleteAttitudeByLearnerIdAndPlaceId(finalLearnerId, placeUnit.id);//сразу удаляем запись по id ученика и урока
                            attitudesList.remove(finalAttitudeIndex);
                            drawDesks(db);
                        }
                    });
                    tempPlaceLayout.addView(tempLernerImage, tempLernerImageParams);

                    //текст ученика
                    tempLearnerText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            db.deleteAttitudeByLearnerIdAndPlaceId(finalLearnerId, placeUnit.id);//сразу удаляем запись по id ученика и урока
                            attitudesList.remove(finalAttitudeIndex);
                            drawDesks(db);

                        }
                    });
                    tempPlaceLayout.addView(tempLearnerText, tempLearnerTextParams);
                } else {
                    //создание кнопки добавить ученика
                    final ImageView tempImageAdd = new ImageView(getApplicationContext());
                    final LinearLayout.LayoutParams tempImageAddParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    tempImageAdd.setImageResource(R.drawable.ic_menu_add_standart);
                    tempImageAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tempPlaceLayout.removeAllViews();
                            Bundle bundle = new Bundle();
                            bundle.putLong("cabinetId", cabinetId);
                            bundle.putLong("classId", classId);
                            ChooseLearnerDialogFragment dialogFragment = new ChooseLearnerDialogFragment();
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(getFragmentManager(), "chooseLearners");
                            handler = new Handler() {
                                public void handleMessage(android.os.Message msg) {
                                    //возврат -1 если ничего не выбрано иначе id ученика
                                    if (msg.what != -1) {
                                        //добавляем запись по id ученика и урока

                                        attitudesList.add(//добавляем зависимость в локальный массив
                                                new AttitudeUnit(
                                                        db.setLearnerOnPlace(msg.what, placeUnit.id),//добавляем зависимость в базу получаем id
                                                        msg.what,//id ученика
                                                        placeUnit.id//d места
                                                )
                                        );
                                    }
                                    drawDesks(db);
                                }
                            };
                        }
                    });
                    if (learnersList.size() != attitudesList.size())
                        tempPlaceLayout.addView(tempImageAdd, tempImageAddParams);
                }


                //добавление места в парту
                tempRelativeLayoutDesk.addView(tempPlaceLayout, tempRelativeLayoutPlaceParams);
            }

            //добавление парты в комнату
            room.addView(tempRelativeLayoutDesk, tempRelativeLayoutDeskParams);
        }

//--------размеры комнаты по самой дальней парте-------
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                (maxDeskX + (int) pxFromDp(3000 * multiplier)),
                (maxDeskY + (int) pxFromDp(3000 * multiplier))
        );
        //получаем размеры заголовка активности
//        Rect rectangle = new Rect();
//        Window window = getWindow();
//        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
//        int statusBarHeight = rectangle.top;
//        int contentViewTop =
//                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
//        int titleBarHeight = contentViewTop - statusBarHeight;
//
//        Log.e("*** Elenasys :: ", "StatusBar Height= " + statusBarHeight + " , TitleBar Height = " + titleBarHeight);
//
//        Log.e("REM", "maxDeskY = " + (maxDeskY + (int) pxFromDp(3000 * multiplier)) + " getResources= " + (getResources().getDisplayMetrics().heightPixels)); //- (int) pxFromDp(72)));
        //если ширина экрана всетаки больше
        if (getResources().getDisplayMetrics().widthPixels >= maxDeskX + (int) (pxFromDp(3000) * multiplier)) {
            layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        }
        //если высота экрана всетаки больше
        if (getResources().getDisplayMetrics().heightPixels - (int) pxFromDp(72) >= maxDeskY + (int) (pxFromDp(3000) * multiplier)) {
            layoutParams.height = getResources().getDisplayMetrics().heightPixels - (int) pxFromDp(72);
        }
        room.setLayoutParams(layoutParams);


        //room.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));//(w, h)320*7 = 2240
        //room.setLayoutParams(new FrameLayout.LayoutParams(1000, 2000));//(w, h)320*7 = 2240
        //room.setLayoutParams(new FrameLayout.LayoutParams(1120, 1120));//(w, h)320*7 = 2240
        Log.i("TeachersProject", "" + (maxDeskX + (int) dpFromPx((2000 + 1000) * multiplier)) + "" + (maxDeskY + (int) dpFromPx((2000 + 1000) * multiplier)));
    }

    //---------------системные методы---------------

    private float pxFromDp(float dp) {
        return dp * getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px) {
        return px / getApplicationContext().getResources().getDisplayMetrics().density;

    }

    @Override
    public void onBackPressed() {
        //Intent backIntent = new Intent();
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://кнопка назад в actionBar
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

//----------------диалог выбора ученика-----------------

    public static class ChooseLearnerDialogFragment extends DialogFragment {//диалог по выбору не распределенного ученика

        long cabinetId;
        long classId;

        public ChooseLearnerDialogFragment() {

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //
            // инициализация переменных
            this.cabinetId = getArguments().getLong("cabinetId");
            this.classId = getArguments().getLong("classId");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//билдер диалога
            final DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity().getApplicationContext());//база
            final ArrayList<Long> learnersId = db.getNotPutLearnersIdByCabinetIdAndClassId(cabinetId, classId);//лист с id нераспределенных по местам учеников
            String[] learnersNames = new String[learnersId.size()];//массив с именами учеников(пустой)
            for (int i = 0; i < learnersNames.length; i++) {//заполняем
                Cursor learnerTempCursor = db.getLearner(learnersId.get(i));//получаем ученика
                learnerTempCursor.moveToFirst();
                //получаем имя
                learnersNames[i] = learnerTempCursor.getString(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_SECOND_NAME)) + " " + learnerTempCursor.getString(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.COLUMN_FIRST_NAME));
                learnerTempCursor.close();
            }
            //--------ставим диалогу список в виде view--------
            ScrollView scroll = new ScrollView(getActivity());

            //контейнер список
            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setBackgroundColor(getResources().getColor(R.color.colorBackGround));
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            scroll.addView(linearLayout);

            for (int i = 0; i < learnersNames.length; i++) {
                //пункт списка
                LinearLayout item = new LinearLayout(getActivity());
                item.setOrientation(LinearLayout.VERTICAL);
                item.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams itemParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (int) (pxFromDp(40) * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))
                        );
                itemParams.setMargins(
                        (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                        (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier)))
                );
                linearLayout.addView(item, itemParams);
                //текст в нем
                TextView text = new TextView(getActivity());
                text.setText(learnersNames[i]);
                text.setTextColor(Color.BLACK);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                item.addView(text);

                //нажатие на пункт списка
                final int number = i;
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Cursor learnerTempCursor = db.getLearner(learnersId.get(number));//получаем выбранного ученика
                        learnerTempCursor.moveToFirst();
                        //посылаем id выбранного ученика
                        SeatingRedactorActivity.handler.sendEmptyMessage((int)
                                learnerTempCursor.getLong(learnerTempCursor.getColumnIndex(SchoolContract.TableLearners.KEY_LEARNER_ID)));
                        learnerTempCursor.close();
                        dismiss();
                    }
                });
            }
            builder.setView(scroll);
//            builder.setItems(learnersNames, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {//лист с именами
//
//                }
//                // The 'which' argument contains the index position
//                // of the selected item
//            });
            // Create the AlertDialog object and return it
            return builder.create();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            Log.i("teachersApp", "SeatingRedactorActivity/ChooseLearnerDialogFragment/onDismiss");
            super.onDismiss(dialog);
            SeatingRedactorActivity.handler.sendEmptyMessage(-1);
        }

        //---------------системные методы(здесь свои)---------------

        private float pxFromDp(float dp) {
            return dp * getActivity().getResources().getDisplayMetrics().density;
        }


    }

}


class DeskUnit {//хранит в себе одну парту
    long id;
    long x;
    long y;
    long countOfPlaces;
    ArrayList<PlaceUnit> placesList;

    public DeskUnit(long id, long x, long y, long countOfPlaces) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.countOfPlaces = countOfPlaces;
        this.placesList = new ArrayList<>();
    }
}

class PlaceUnit {//хранит в себе одно место
    long id;
    long deskId;
    long ordinalNumber;

    public PlaceUnit(long id, long deskId, long ordinalNumber) {
        this.id = id;
        this.deskId = deskId;
        this.ordinalNumber = ordinalNumber;
    }
}

class LearnerUnit {//хранит в себе одного ученика
    long id;
    String name;
    String lastName;
    long classId;

    public LearnerUnit(long id, String name, String lastName, long classId) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.classId = classId;
    }
}


class AttitudeUnit {//хранит в себе зависимость ученик место
    long id;
    long learnerId;
    long placeId;

    public AttitudeUnit(long id, long learnerId, long placeId) {
        this.id = id;
        this.learnerId = learnerId;
        this.placeId = placeId;
    }
}


