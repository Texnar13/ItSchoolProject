package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;

public class EditGradesTypesDialogFragment extends DialogFragment {

    public static String ARGS_TYPES_ID_ARRAY_TAG = "typesId";
    public static String ARGS_TYPES_NAMES_ARRAY_TAG = "typesNames";

    // массив с текстами записей и id
    ArrayList<GradesTypeRecord> types = new ArrayList<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_settings_edit_time, null);
        builder.setView(dialogLayout);

        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_linear_layout);
        linearLayout.setBackgroundResource(R.color.colorBackGround);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
        linearLayout.setLayoutParams(linearLayoutParams);


//---------- базовый скролл ------
// ---- LinearLayout в скролле для вывода всего ----
// -- заголовок --
// -- вывод списка --
// -- кнопка отмены --


//---------- базовый скролл ------
        ScrollView scrollView = new ScrollView(getActivity());
        LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                2F
        );
        linearLayout.addView(scrollView, scrollViewParams);

//---- LinearLayout в скролле для вывода всего ----
        LinearLayout allOut = new LinearLayout(getActivity());
        allOut.setGravity(Gravity.CENTER);
        allOut.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(allOut);

// -- заголовок --
        TextView title = new TextView(getActivity());
        title.setText("title");//getResources().getString(R.string.settings_activity_dialog_edit_time_title));
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        title.setAllCaps(true);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

        allOut.addView(title, titleParams);

// -- вывод списка --
        LinearLayout typesOut = new LinearLayout(getActivity());
        typesOut.setGravity(Gravity.CENTER);
        typesOut.setOrientation(LinearLayout.VERTICAL);
        allOut.addView(typesOut);


        // получаем данные
        try {
            //переданные значения
            long[] typesId = getArguments().getLongArray(ARGS_TYPES_ID_ARRAY_TAG);
            String[] typesStrings = getArguments().getStringArray(ARGS_TYPES_NAMES_ARRAY_TAG);

            //массив с полями
            for (int i = 0; i < typesId.length; i++) {
                types.add(new GradesTypeRecord(typesId[i], typesStrings[i]));
            }
        } catch (java.lang.NullPointerException e) {
            //в диалог необходимо передать ( Bungle putLong("learnerId",learnerId) )
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "EditTimeDialogFragment: you must give time( Bungle putIntArray)"
            );
        }

        // выводим поля
        outTypesInLinearLayout(types, typesOut);

//-- кнопка отмены --
        //кнопка
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(getResources().getString(R.string.settings_activity_dialog_button_cancel));
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.my_buttons_height_size)
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));
        allOut.addView(neutralButton, neutralButtonParams);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return builder.create();
    }

    // ----- метод вывода всех типов в список -----
    void outTypesInLinearLayout(final ArrayList<GradesTypeRecord> list, final LinearLayout layout) {

        // ---- контейнер ----
        // --- текстовое поле ---
        // --- контейнер с кнопками ---
        // -- кнопка удаления --
        // -- кнопка переименования --

        // ---- чистка ----
        layout.removeAllViews();

        // ---- основные поля ----
        for (int i = 0; i < list.size(); i++) {
            // ---- контейнер ----
            final LinearLayout item = new LinearLayout(getActivity());
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setBackground(getActivity().getResources().getDrawable(R.drawable.button_light_gray));
            // параметры контейнера
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.bottomMargin = 8;
            itemParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));
            layout.addView(item, itemParams);

            // -- текстовое поле --
            TextView type = new TextView(getActivity());
            type.setText(list.get(i).typeName + " " + list.get(i).typeId);
            type.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.text_subtitle_size));
            type.setTextColor(Color.BLACK);
            type.setGravity(Gravity.CENTER_VERTICAL);
            // параметры текста
            final LinearLayout.LayoutParams typeTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size),
                    1F
            );
            typeTextParams.gravity = Gravity.CENTER_VERTICAL;
            typeTextParams.leftMargin = (int) pxFromDp(10);
            typeTextParams.rightMargin = (int) pxFromDp(10);
            item.addView(type, typeTextParams);

            // превращаем текстовое поле в текст
            //type.setEnabled(false);
            //type.setCursorVisible(false);
            //type.setBackgroundColor(Color.TRANSPARENT);
            //type.setKeyListener(null);
            //type.setOnClickListener(null);

            // - при нажатии на контейнер -
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // отключаем нажатие на контейнер
                    item.setOnClickListener(null);

                    // контейнер для кнопок изменения
                    LinearLayout container = new LinearLayout(getActivity());
                    container.setOrientation(LinearLayout.HORIZONTAL);
                    container.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1F
                    );
                    item.addView(container, containerParams);
                    // кнопка удалить
                    Button neutralButton = new Button(getActivity());
                    neutralButton.setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
                    neutralButton.setText(getResources().getString(R.string.settings_activity_dialog_button_remove));
                    neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                    neutralButton.setTextColor(Color.WHITE);
                    LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) getResources().getDimension(R.dimen.my_buttons_height_size)
                    );
                    neutralButtonParams.weight = 1;
                    neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));
                    container.addView(neutralButton, neutralButtonParams);
                    // кнопка сохранить
                    Button saveButton = new Button(getActivity());
                    saveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
                    saveButton.setText(getResources().getString(R.string.settings_activity_dialog_button_save));
                    saveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                    saveButton.setTextColor(Color.WHITE);
                    LinearLayout.LayoutParams saveButtonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) getResources().getDimension(R.dimen.my_buttons_height_size)
                    );
                    saveButtonParams.weight = 1;
                    saveButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));
                    container.addView(saveButton, saveButtonParams);

                    // при нажатии
                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            outTypesInLinearLayout(list, layout);
                        }
                    });
                }
            });
        }

        // -- вывод текстовой кнопки добавить --
        // контейнер
        LinearLayout lastItem = new LinearLayout(getActivity());
        lastItem.setGravity(Gravity.CENTER);
        lastItem.setOrientation(LinearLayout.HORIZONTAL);
        // параметры контейнера
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (int) getResources().getDimension(R.dimen.my_buttons_height_size)
        );
        layout.addView(lastItem, itemParams);

        // текст с названием
        TextView type = new TextView(getActivity());
        type.setText("+ Добавить тип оценок");
        type.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.text_simple_size));
        type.setTextColor(Color.DKGRAY);
        lastItem.addView(type);
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

class GradesTypeRecord {

    String typeName;
    long typeId;

    GradesTypeRecord(long id, String name) {
        this.typeId = id;
        this.typeName = name;
    }

}

interface EditGradesTypeDialogFragmentInterface {
    void editTime(int[][] time);
    // удалять с проверкой и возвратом boolean
}



//---------------при нажатии...---------------
//сохранение
//        positiveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //флаг проверки все ли поля соответствуют
//                boolean flag = true;
//
//                //массив с передаваемым временем
//                int time[][] = new int[fields.length][fields[0].length];
//
//                for (int i = 0; i < time.length; i++) {
//                    for (int j = 0; j < time[0].length; j++) {
//                        //----проверяем отдельную клетку----
//                        if (!(fields[i][j].getText().toString().length() > 2) && //не слишком большое по длинне
//                                !(fields[i][j].getText().toString().length() == 0)//не слишком маленькое по длинне
//                        ) {
//                            if (((Integer.parseInt(fields[i][j].getText().toString()) > 23) && (j % 2 == 0)) ||//не слишком большое численно
//                                    ((Integer.parseInt(fields[i][j].getText().toString()) > 59) && (j % 2 == 1)) ||
//                                    (Integer.parseInt(fields[i][j].getText().toString()) < 0)//не слишком маленькое численно
//                            ) {
//                                flag = false;
//                                fields[i][j].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
//                            }
//                        } else {
//                            flag = false;
//                            fields[i][j].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
//                        }
//                    }
//                    //----проверяем сразу ряд----
//                    if (flag) {
//                        //если время начала больше времени конца
//                        if (((Integer.parseInt(fields[i][0].getText().toString()) > Integer.parseInt(fields[i][2].getText().toString())) || ((Integer.parseInt(fields[i][0].getText().toString()) == Integer.parseInt(fields[i][2].getText().toString())) && (Integer.parseInt(fields[i][1].getText().toString()) >= Integer.parseInt(fields[i][3].getText().toString()))))) {
//                            flag = false;
//                            fields[i][0].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
//                            fields[i][1].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
//                            fields[i][2].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
//                            fields[i][3].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
//                        } else {
//                            time[i][0] = Integer.parseInt(fields[i][0].getText().toString());
//                            time[i][1] = Integer.parseInt(fields[i][1].getText().toString());
//                            time[i][2] = Integer.parseInt(fields[i][2].getText().toString());
//                            time[i][3] = Integer.parseInt(fields[i][3].getText().toString());
//                        }
//                    }
//                }
//                if (flag) {
//                    try {
//                        //вызываем в активности метод по сохранению
//                        ((EditTimeDialogFragmentInterface) getActivity()).editTime(time);
//                    } catch (java.lang.ClassCastException e) {
//                        //в вызвающей активности должен быть имплементирован класс EditTimeDialogFragmentInterface
//                        e.printStackTrace();
//                        Log.i(
//                                "TeachersApp",
//                                "EditTimeDialogFragment: you must implements EditTimeDialogFragmentInterface in your activity"
//                        );
//                    }
//                    dismiss();
//                }
//            }
//        });

//отмена