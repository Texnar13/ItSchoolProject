package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

import java.util.Random;

public class SettingsRemoveDataDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundResource(R.drawable._dialog_full_background_white);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
        linearLayout.setLayoutParams(linearLayoutParams);
        builder.setView(linearLayout);

//--заголовок--
        TextView title = new TextView(getActivity());
        title.setText(getResources().getString(R.string.settings_activity_dialog_delete_title));
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        title.setAllCaps(true);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                0
        );

        linearLayout.addView(title, titleParams);

//----защита от случайных удалений----

        Random random = new Random();
        final int n = random.nextInt(50);

        //текстовое поле с числом
        final TextView numberText = new TextView(getActivity());
        numberText.setGravity(Gravity.CENTER);
        numberText.setTextColor(getResources().getColor(R.color.colorBackGroundDark));
        numberText.setText(getResources().getString(R.string.settings_activity_dialog_delete_text_confirm_delete) + " " + n);
        numberText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams numberTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        numberTextParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                0,
                (int) getResources().getDimension(R.dimen.simple_margin),
                0
        );
        //добавляем текстовое поле
        linearLayout.addView(numberText, numberTextParams);

        //текстовое поле для числа
        final EditText editNumber = new EditText(getActivity());
        editNumber.setTextColor(Color.BLACK);
        editNumber.setGravity(Gravity.CENTER);
        editNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editNumber.setHint(getResources().getString(R.string.settings_activity_dialog_hint_number));
        editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        editNumber.setHintTextColor(Color.GRAY);
        LinearLayout.LayoutParams editNumberParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editNumberParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                0,
                (int) getResources().getDimension(R.dimen.simple_margin),
                0
        );
        //добавляем текстовое поле
        linearLayout.addView(editNumber, editNumberParams);

//----кнопки согласия/отмены----
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        //контейнер в диалог
        linearLayout.addView(container);
//--кнопка отмены--
        // контейнер кнопки
        LinearLayout cancelButtonContainer = new LinearLayout(getActivity());
        cancelButtonContainer.setGravity(Gravity.CENTER);
        cancelButtonContainer.setBackgroundResource(R.drawable.__background_round_simple_full_dark);
        LinearLayout.LayoutParams cancelButtonContainerParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        cancelButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        cancelButtonContainerParams.gravity = Gravity.CENTER;
        container.addView(cancelButtonContainer, cancelButtonContainerParams);

        TextView cancelText = new TextView(getActivity());
        cancelText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        cancelText.setText(R.string.settings_activity_dialog_button_cancel);
        cancelText.setTextColor(getResources().getColor(R.color.backgroundWhite));
        cancelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams cancelTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cancelTextParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        cancelButtonContainer.addView(cancelText, cancelTextParams);
        //при нажатии
        cancelButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // кнопка удалить
        // контейнер кнопки
        LinearLayout removeButtonContainer = new LinearLayout(getActivity());
        removeButtonContainer.setGravity(Gravity.CENTER);
        removeButtonContainer.setBackgroundResource(R.drawable.__background_round_simple_full_pink);
        LinearLayout.LayoutParams removeButtonContainerParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        removeButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        removeButtonContainerParams.gravity = Gravity.CENTER;
        container.addView(removeButtonContainer, removeButtonContainerParams);

        TextView removeText = new TextView(getActivity());
        removeText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        removeText.setText(R.string.settings_activity_dialog_button_remove);
        removeText.setTextColor(getResources().getColor(R.color.backgroundWhite));
        removeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams removeTextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        removeTextParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        removeButtonContainer.addView(removeText, removeTextParams);
        //при нажатии
        removeButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //если числа совпали вызываем в активности метод по удалению
                    if (editNumber.getText().toString().equals(Integer.toString(n))) {
                        ((com.learning.texnar13.teachersprogect.settings.SettingsRemoveInterface)
                                getActivity()).settingsRemove();
                    } else {
                        //пишем пользователю о его ошибке
                        Toast toast = Toast.makeText(getActivity().getApplicationContext()
                                , getResources().getString(R.string.settings_activity_toast_data_delete_falture), Toast.LENGTH_SHORT);
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


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
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