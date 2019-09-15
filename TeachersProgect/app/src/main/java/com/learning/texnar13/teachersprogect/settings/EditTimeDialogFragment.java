package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class EditTimeDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //layout диалога
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
        linearLayout.setLayoutParams(linearLayoutParams);
        builder.setView(linearLayout);

        // layout заголовка
        LinearLayout headLayout = new LinearLayout(getActivity());
        headLayout.setOrientation(LinearLayout.HORIZONTAL);
        headLayout.setBackgroundResource(R.drawable._dialog_head_background_dark);
        headLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayout.addView(headLayout, headLayoutParams);

        // кнопка закрыть
        ImageView closeImageView = new ImageView(getActivity());
        closeImageView.setImageResource(R.drawable.__button_close);
        LinearLayout.LayoutParams closeImageViewParams = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.my_icon_size),
                (int) getResources().getDimension(R.dimen.my_icon_size));
        closeImageViewParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin));
        headLayout.addView(closeImageView, closeImageViewParams);
        // при нажатии на кнопку закрыть
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // закрываем диалог
                dismiss();
            }
        });

        // текст заголовка
        TextView title = new TextView(getActivity());
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        title.setText(R.string.settings_activity_dialog_edit_time_title);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                0,
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin));
        Log.e("TeachersApp", "outMainMenu: " + closeImageView.getId());
        titleParams.gravity = Gravity.CENTER_VERTICAL;
        headLayout.addView(title, titleParams);


        // скролл для вывода
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setBackgroundResource(R.drawable._dialog_bottom_background_white);
        LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                2F
        );
        linearLayout.addView(scrollView, scrollViewParams);

        // LinearLayout в скролле
        LinearLayout timesOut = new LinearLayout(getActivity());
        timesOut.setGravity(Gravity.CENTER);
        timesOut.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(timesOut);


//--поля времени--
        int[][] array = new int[9][4];
        try {
            //переданные значения
            array[0] = getArguments().getIntArray("arr0");
            array[1] = getArguments().getIntArray("arr1");
            array[2] = getArguments().getIntArray("arr2");
            array[3] = getArguments().getIntArray("arr3");
            array[4] = getArguments().getIntArray("arr4");
            array[5] = getArguments().getIntArray("arr5");
            array[6] = getArguments().getIntArray("arr6");
            array[7] = getArguments().getIntArray("arr7");
            array[8] = getArguments().getIntArray("arr8");
        } catch (java.lang.NullPointerException e) {
            //в диалог необходимо передать ( Bungle putLong("learnerId",learnerId) )
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "EditTimeDialogFragment: you must give time( Bungle putIntArray)"
            );
        }

        //массив с полями
        final EditText[][] fields = new EditText[array.length][array[0].length];

//------выводим поля------
        for (int i = 0; i < array.length; i++) {
            //контейнер
            //--цифра
            //--контейнер времени
            //----поле ввода
            //----:
            //----поле ввода
            //---- --
            //----поле ввода
            //----:
            //----поле ввода

            //контейнер
            LinearLayout item = new LinearLayout(getActivity());
            item.setGravity(Gravity.CENTER);
            item.setOrientation(LinearLayout.HORIZONTAL);
            // параметры контейнера
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.bottomMargin = 8;
            timesOut.addView(item,itemParams);

            //--цифра
            TextView number = new TextView(getActivity());
            number.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            number.setText((i + 1) + ".");
            number.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.text_subtitle_size));
            number.setTextColor(Color.BLACK);
            item.addView(number);

            //--контейнер времени
            LinearLayout timeContainer = new LinearLayout(getActivity());
            timeContainer.setGravity(Gravity.CENTER);
            timeContainer.setOrientation(LinearLayout.HORIZONTAL);
            item.addView(timeContainer);

            //----поле ввода beginHour
            fields[i][0] = new EditText(getActivity());
            fields[i][0].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            fields[i][0].setGravity(Gravity.CENTER);
            fields[i][0].setHint(getResources().getString(R.string.settings_activity_dialog_hint_hour));
            fields[i][0].setText("" + array[i][0]);
            fields[i][0].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            fields[i][0].setInputType(InputType.TYPE_CLASS_NUMBER);
            fields[i][0].setBackgroundResource(R.drawable._underlined_black);
            LinearLayout.LayoutParams fieldOneParams = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.text_field_two_simple_symbols_width),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            fieldOneParams.leftMargin = (int) getResources().getDimension(R.dimen.double_margin);
            timeContainer.addView(fields[i][0], fieldOneParams);

            //----:
            TextView beginPointer = new TextView(getActivity());
            beginPointer.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            beginPointer.setTextColor(Color.BLACK);
            beginPointer.setText(R.string.settings_activity_dialog_text_time_colon);
            beginPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            timeContainer.addView(beginPointer);

            //----поле ввода beginMinute
            fields[i][1] = new EditText(getActivity());
            fields[i][1].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            fields[i][1].setGravity(Gravity.CENTER);
            fields[i][1].setHint(getResources().getString(R.string.settings_activity_dialog_hint_minute));
            fields[i][1].setText("" + getTwoSymbols(array[i][1]));
            fields[i][1].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            fields[i][1].setInputType(InputType.TYPE_CLASS_NUMBER);
            fields[i][1].setBackgroundResource(R.drawable._underlined_black);
            timeContainer.addView(fields[i][1], (int) getResources().getDimension(R.dimen.text_field_two_simple_symbols_width), LinearLayout.LayoutParams.WRAP_CONTENT);

            //---- --
            TextView pointerMid = new TextView(getActivity());
            beginPointer.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            pointerMid.setTextColor(Color.BLACK);
            pointerMid.setText(R.string.settings_activity_dialog_text_time_dash);
            pointerMid.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            LinearLayout.LayoutParams pointerMidParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            pointerMidParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            pointerMidParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
            timeContainer.addView(pointerMid,pointerMidParams);

            //----поле ввода endHour
            fields[i][2] = new EditText(getActivity());
            fields[i][2].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            fields[i][2].setGravity(Gravity.CENTER);
            fields[i][2].setHint(getResources().getString(R.string.settings_activity_dialog_hint_hour));
            fields[i][2].setText("" + array[i][2]);
            fields[i][2].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            fields[i][2].setInputType(InputType.TYPE_CLASS_NUMBER);
            fields[i][2].setBackgroundResource(R.drawable._underlined_black);
            timeContainer.addView(fields[i][2], (int) getResources().getDimension(R.dimen.text_field_two_simple_symbols_width), LinearLayout.LayoutParams.WRAP_CONTENT);

            //----:
            TextView endPointer = new TextView(getActivity());
            endPointer.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            endPointer.setTextColor(Color.BLACK);
            endPointer.setText(R.string.settings_activity_dialog_text_time_colon);
            endPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            timeContainer.addView(endPointer);

            //----поле ввода endMinute
            fields[i][3] = new EditText(getActivity());
            fields[i][3].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            fields[i][3].setGravity(Gravity.CENTER);
            fields[i][3].setHint(getResources().getString(R.string.settings_activity_dialog_hint_minute));
            fields[i][3].setText("" + getTwoSymbols(array[i][3]));
            fields[i][3].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            fields[i][3].setInputType(InputType.TYPE_CLASS_NUMBER);
            fields[i][3].setBackgroundResource(R.drawable._underlined_black);
            timeContainer.addView(fields[i][3], (int) getResources().getDimension(R.dimen.text_field_two_simple_symbols_width), LinearLayout.LayoutParams.WRAP_CONTENT);

        }



        //контейнер для кнопки
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setBackgroundResource(R.drawable._button_round_background_green);
        container.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin));
        //контейнер в диалог
        timesOut.addView(container, containerParams);


        //кнопка сохранения
        TextView positiveButton = new TextView(getActivity());
        positiveButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        positiveButton.setGravity(Gravity.CENTER);
        positiveButton.setText(getResources().getString(R.string.button_save));
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT
        );
        positiveButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.half_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.half_margin));
        container.addView(positiveButton, positiveButtonParams);


//---------------при нажатии...---------------
        //сохранение
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //флаг проверки все ли поля соответствуют
                boolean flag = true;

                //массив с передаваемым временем
                int time[][] = new int[fields.length][fields[0].length];

                for (int i = 0; i < time.length; i++) {
                    for (int j = 0; j < time[0].length; j++) {
                        //----проверяем отдельную клетку----
                        if (!(fields[i][j].getText().toString().length() > 2) && //не слишком большое по длинне
                                !(fields[i][j].getText().toString().length() == 0)//не слишком маленькое по длинне
                                ) {
                            if (((Integer.parseInt(fields[i][j].getText().toString()) > 23) && (j % 2 == 0)) ||//не слишком большое численно
                                    ((Integer.parseInt(fields[i][j].getText().toString()) > 59) && (j % 2 == 1)) ||
                                    (Integer.parseInt(fields[i][j].getText().toString()) < 0)//не слишком маленькое численно
                                    ) {
                                flag = false;
                                fields[i][j].setBackgroundResource(R.drawable._underlined_black_pink);
                            }
                        } else {
                            flag = false;
                            fields[i][j].setBackgroundResource(R.drawable._underlined_black_pink);
                        }
                    }
                    //----проверяем сразу ряд----
                    if (flag) {
                        //если время начала больше времени конца
                        if (((Integer.parseInt(fields[i][0].getText().toString()) > Integer.parseInt(fields[i][2].getText().toString())) || ((Integer.parseInt(fields[i][0].getText().toString()) == Integer.parseInt(fields[i][2].getText().toString())) && (Integer.parseInt(fields[i][1].getText().toString()) >= Integer.parseInt(fields[i][3].getText().toString()))))) {
                            flag = false;
                            fields[i][0].setBackgroundResource(R.drawable._underlined_black_pink);
                            fields[i][1].setBackgroundResource(R.drawable._underlined_black_pink);
                            fields[i][2].setBackgroundResource(R.drawable._underlined_black_pink);
                            fields[i][3].setBackgroundResource(R.drawable._underlined_black_pink);
                        } else {
                            time[i][0] = Integer.parseInt(fields[i][0].getText().toString());
                            time[i][1] = Integer.parseInt(fields[i][1].getText().toString());
                            time[i][2] = Integer.parseInt(fields[i][2].getText().toString());
                            time[i][3] = Integer.parseInt(fields[i][3].getText().toString());
                        }
                    }
                }
                if (flag) {
                    try {
                        //вызываем в активности метод по сохранению
                        ((EditTimeDialogFragmentInterface) getActivity()).editTime(time);
                    } catch (java.lang.ClassCastException e) {
                        //в вызвающей активности должен быть имплементирован класс EditTimeDialogFragmentInterface
                        e.printStackTrace();
                        Log.i(
                                "TeachersApp",
                                "EditTimeDialogFragment: you must implements EditTimeDialogFragmentInterface in your activity"
                        );
                    }
                    dismiss();
                }
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }

    // -- метод трансформации числа в текст с двумя позициями --
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }
}

interface EditTimeDialogFragmentInterface {
    void editTime(int[][] time);
}
