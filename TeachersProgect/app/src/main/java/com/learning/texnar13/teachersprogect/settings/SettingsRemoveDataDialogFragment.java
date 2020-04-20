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
import android.widget.ImageView;
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
                (int) getResources().getDimension(R.dimen.double_margin),
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
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_medium));
        title.setText(R.string.settings_activity_dialog_delete_title);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
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


//----защита от случайных удалений----

        Random random = new Random();
        final int n = random.nextInt(40) + 10;

        //текстовое поле с числом
        final TextView numberText = new TextView(getActivity());
        numberText.setGravity(Gravity.CENTER);
        numberText.setTextColor(getResources().getColor(R.color.backgroundGray));
        numberText.setText(getResources().getString(R.string.settings_activity_dialog_delete_text_confirm_delete) + ": " + n);
        numberText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        LinearLayout.LayoutParams numberTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        numberTextParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        //добавляем текстовое поле
        linearLayout.addView(numberText, numberTextParams);


        // контейнер текстового поля числа
        LinearLayout numberContainer = new LinearLayout(getActivity());
        LinearLayout.LayoutParams numberContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        numberContainer.setBackgroundColor(getResources().getColor(R.color.backgroundLiteGray));
        numberContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        linearLayout.addView(numberContainer, numberContainerParams);

        //текстовое поле для числа
        final EditText editNumber = new EditText(getActivity());
        editNumber.setTextColor(Color.BLACK);
        editNumber.setGravity(Gravity.CENTER);
        editNumber.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editNumber.setHint(getResources().getString(R.string.settings_activity_dialog_hint_number));
        editNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        editNumber.setHintTextColor(Color.GRAY);
        editNumber.setBackgroundColor(getResources().getColor(R.color.backgroundLiteGray));// убираем подчеркивание
        LinearLayout.LayoutParams editNumberParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editNumberParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin), 0,
                (int) getResources().getDimension(R.dimen.simple_margin), 0
        );
        // добавляем текстовое поле в контейнер
        numberContainer.addView(editNumber, editNumberParams);

//----кнопки согласия/отмены----
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.topMargin = (int) getResources().getDimension(R.dimen.half_margin);
        //контейнер в диалог
        linearLayout.addView(container, containerParams);
//--кнопка отмены--

        // кнопка удалить
        // контейнер кнопки
        LinearLayout removeButtonContainer = new LinearLayout(getActivity());
        removeButtonContainer.setGravity(Gravity.CENTER);
        removeButtonContainer.setBackgroundResource(R.drawable.__background_round_simple_full_signal_red);
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