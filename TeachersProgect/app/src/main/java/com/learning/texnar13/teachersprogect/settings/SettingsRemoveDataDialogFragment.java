package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;

import java.util.Random;

public class SettingsRemoveDataDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//----layout диалога----
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_settings_remove, null);
        builder.setView(dialogLayout);
//----LinearLayout в layout файле----
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.settings_remove_dialog_fragment_linear_layout);
        linearLayout.setBackgroundResource(R.color.colorBackGround);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
        linearLayout.setLayoutParams(linearLayoutParams);
//--заголовок--
        TextView title = new TextView(getActivity());
        title.setText(getResources().getString(R.string.settings_activity_dialog_title));
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        title.setAllCaps(true);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

        linearLayout.addView(title, titleParams);

//----защита от случайных удалений----

        Random random = new Random();
        final int n = random.nextInt(50);

        //текстовое поле с числом
        final TextView numberText = new TextView(getActivity());
        numberText.setGravity(Gravity.CENTER);
        numberText.setTextColor(getResources().getColor(R.color.colorBackGroundDark));
        numberText.setText(getResources().getString(R.string.settings_activity_dialog_text)+" " + n);
        numberText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        //добавляем текстовое поле
        linearLayout.addView(numberText);

        //текстовое поле для числа
        final EditText editNumber = new EditText(getActivity());
        editNumber.setTextColor(Color.BLACK);
        editNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editNumber.setHint(getResources().getString(R.string.settings_activity_dialog_hint_number));
        editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        editNumber.setHintTextColor(Color.GRAY);
        //добавляем текстовое поле
        linearLayout.addView(editNumber);

//----кнопки согласия/отмены----
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);
        //контейнер в диалог
        linearLayout.addView(container);
//--кнопка отмены--
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.settings_activity_dialog_button_cancel);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));
        //кнопку в контейнер
        container.addView(neutralButton, neutralButtonParams);
        //при нажатии
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

//--кнопка удаления--
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
        positiveButton.setText(R.string.settings_activity_dialog_button_remove);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        positiveButtonParams.weight = 1;
        positiveButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));
        //кнопку в контейнер
        container.addView(positiveButton, positiveButtonParams);
        //при нажатии
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //если числа совпали вызываем в активности метод по удалению
                    if(editNumber.getText().toString().equals(Integer.toString(n))) {
                        ((com.learning.texnar13.teachersprogect.settings.SettingsRemoveInterface)
                                getActivity()).settingsRemove();
                    }else{
                        //пишем пользователю о его ошибке
                        Toast toast = Toast.makeText(getActivity().getApplicationContext()
                                ,getResources().getString(R.string.settings_activity_toast_data_delete_falture),Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс CreateCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "SettingsRemoveDataDialogFragment: you must implements SettingsRemoveInterface in your activity"
                    );
                }
                dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface SettingsRemoveInterface {
    void settingsRemove();
}