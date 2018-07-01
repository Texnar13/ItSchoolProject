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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class EditTimeDialogFragment extends DialogFragment {

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

//----------скролл для вывода------
        ScrollView scrollView = new ScrollView(getActivity());
        LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                2F
        );
        linearLayout.addView(scrollView, scrollViewParams);

//----LinearLayout в скролле----
        LinearLayout timesOut = new LinearLayout(getActivity());
        timesOut.setGravity(Gravity.CENTER);
        timesOut.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(timesOut);

//--заголовок--
        TextView title = new TextView(getActivity());
        title.setText(getResources().getString(R.string.settings_activity_dialog_edit_time_title));
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        title.setAllCaps(true);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

        timesOut.addView(title, titleParams);

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
            timesOut.addView(item);

            //--цифра
            TextView number = new TextView(getActivity());
            number.setText("" + (i + 1) + ".");
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
            fields[i][0].setEms(2);
            fields[i][0].setGravity(Gravity.END);
            fields[i][0].setHint(getResources().getString(R.string.settings_activity_dialog_hint_hour));
            fields[i][0].setText("" + array[i][0]);
            fields[i][0].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            fields[i][0].setInputType(InputType.TYPE_CLASS_NUMBER);
            timeContainer.addView(fields[i][0]);

            //----:
            TextView beginPointer = new TextView(getActivity());
            beginPointer.setTextColor(Color.BLACK);
            beginPointer.setText(" : ");
            beginPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            timeContainer.addView(beginPointer);

            //----поле ввода beginMinute
            fields[i][1] = new EditText(getActivity());
            fields[i][1].setEms(2);
            fields[i][1].setGravity(Gravity.START);
            fields[i][1].setHint(getResources().getString(R.string.settings_activity_dialog_hint_minute));
            fields[i][1].setText("" + array[i][1]);
            fields[i][1].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            fields[i][1].setInputType(InputType.TYPE_CLASS_NUMBER);
            timeContainer.addView(fields[i][1]);

            //---- --
            TextView pointerMid = new TextView(getActivity());
            pointerMid.setTextColor(Color.BLACK);
            pointerMid.setText(" — ");
            pointerMid.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            timeContainer.addView(pointerMid);

            //----поле ввода endHour
            fields[i][2] = new EditText(getActivity());
            fields[i][2].setEms(2);
            fields[i][2].setGravity(Gravity.END);
            fields[i][2].setHint(getResources().getString(R.string.settings_activity_dialog_hint_hour));
            fields[i][2].setText("" + array[i][2]);
            fields[i][2].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            fields[i][2].setInputType(InputType.TYPE_CLASS_NUMBER);
            timeContainer.addView(fields[i][2]);

            //----:
            TextView endPointer = new TextView(getActivity());
            endPointer.setTextColor(Color.BLACK);
            endPointer.setText(" : ");
            endPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            timeContainer.addView(endPointer);

            //----поле ввода endMinute
            fields[i][3] = new EditText(getActivity());
            fields[i][3].setEms(2);
            fields[i][3].setGravity(Gravity.START);
            fields[i][3].setHint(getResources().getString(R.string.settings_activity_dialog_hint_minute));
            fields[i][3].setText("" + array[i][3]);
            fields[i][3].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            fields[i][3].setInputType(InputType.TYPE_CLASS_NUMBER);
            timeContainer.addView(fields[i][3]);

        }

//--кнопки согласия/отмены--

        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1F
        );
        //контейнер в диалог
        timesOut.addView(container, containerParams);

//кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(getResources().getString(R.string.settings_activity_dialog_button_cancel));
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));
        container.addView(neutralButton, neutralButtonParams);

//кнопка сохранения
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        positiveButton.setText(getResources().getString(R.string.settings_activity_dialog_button_save));
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        positiveButtonParams.weight = 1;
        positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));
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
                                fields[i][j].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
                            }
                        } else {
                            flag = false;
                            fields[i][j].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
                        }
                    }
                    //----проверяем сразу ряд----
                    if (flag) {
                        //если время начала больше времени конца
                        if (((Integer.parseInt(fields[i][0].getText().toString()) > Integer.parseInt(fields[i][2].getText().toString())) || ((Integer.parseInt(fields[i][0].getText().toString()) == Integer.parseInt(fields[i][2].getText().toString())) && (Integer.parseInt(fields[i][1].getText().toString()) >= Integer.parseInt(fields[i][3].getText().toString()))))) {
                            flag = false;
                            fields[i][0].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
                            fields[i][1].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
                            fields[i][2].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
                            fields[i][3].setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
                        }else {
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

        //отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return builder.create();
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface EditTimeDialogFragmentInterface {
    void editTime(int[][] time);
}
