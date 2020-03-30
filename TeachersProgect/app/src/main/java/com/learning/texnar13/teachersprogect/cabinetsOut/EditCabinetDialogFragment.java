package com.learning.texnar13.teachersprogect.cabinetsOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class EditCabinetDialogFragment extends DialogFragment {

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
        linearLayout.setLayoutParams(linearLayoutParams);
        builder.setView(linearLayout);




        // контейнер каритнки и кнопки назад
        RelativeLayout imageContainer = new RelativeLayout(getActivity());
        LinearLayout.LayoutParams imageContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        imageContainerParams.topMargin = (int) getResources().getDimension(R.dimen.double_margin);
        imageContainerParams.leftMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        imageContainerParams.rightMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        linearLayout.addView(
                imageContainer,
                imageContainerParams

        );

        // картинка
        ImageView imageBackground = new ImageView(getActivity());
        imageBackground.setImageResource(R.drawable.__background_add_cabinet);
        imageBackground.setAdjustViewBounds(true);
        RelativeLayout.LayoutParams imageBackgroundParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.cabinets_out_add_cabinet_image_width),
                (int) getResources().getDimension(R.dimen.cabinets_out_add_cabinet_image_height)
        );
        imageBackgroundParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageBackgroundParams.topMargin = (int) getResources().getDimension(R.dimen.double_margin);
        imageContainer.addView(imageBackground, imageBackgroundParams);

        // кнопка назад
        ImageView backImage = new ImageView(getActivity());
        backImage.setImageResource(R.drawable.__button_back_arrow_dark_gray);
        backImage.setAdjustViewBounds(true);
        RelativeLayout.LayoutParams backImageParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.my_icon_size),
                (int) getResources().getDimension(R.dimen.my_icon_size)
        );
        imageContainer.addView(backImage, backImageParams);
        //отмена
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // заголовок
        TextView title = new TextView(getActivity());
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_medium));
        title.setText(R.string.cabinets_out_activity_dialog_title_edit_cabinet);
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                0
        );

        linearLayout.addView(title, titleParams);


        LinearLayout editNameContainer = new LinearLayout(getActivity());
        LinearLayout.LayoutParams editNameContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editNameContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.forth_margin),
                0,
                (int) getResources().getDimension(R.dimen.forth_margin),
                0
        );
        linearLayout.addView(editNameContainer, editNameContainerParams);

        // текстовое поле имени
        final EditText editName = new EditText(getActivity());
        editName.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        editName.setTextColor(Color.BLACK);
        editName.setHint(R.string.cabinets_out_activity_dialog_hint_cabinet_name);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setHintTextColor(Color.GRAY);
        editNameContainer.addView(editName,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );
        try { // входные данные предыдущее имя
            editName.setText(getArguments().getString("name"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"name\"");
        }


        // кнопка удалить
        TextView negativeTextButton = new TextView(getActivity());
        negativeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        negativeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        negativeTextButton.setText(R.string.cabinets_out_activity_dialog_button_delete);
        negativeTextButton.setGravity(Gravity.CENTER);
        negativeTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        negativeTextButton.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
        negativeTextButton.setPaintFlags(negativeTextButton.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        // параметры кнопки
        LinearLayout.LayoutParams negativeTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        negativeTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        negativeTextButtonParams.gravity = Gravity.CENTER;
        linearLayout.addView(negativeTextButton, negativeTextButtonParams);


        // контейнер кнопки сохранить
        LinearLayout saveButtonContainer = new LinearLayout(getActivity());
        saveButtonContainer.setBackgroundResource(R.drawable._button_round_background_green);
        // параметры контейнера
        LinearLayout.LayoutParams saveButtonContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        saveButtonContainerParams.gravity = Gravity.CENTER;
        linearLayout.addView(saveButtonContainer, saveButtonContainerParams);

        // кнопка сохранить
        TextView saveTextButton = new TextView(getActivity());
        saveTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        saveTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        saveTextButton.setText(R.string.button_save);
        saveTextButton.setGravity(Gravity.CENTER);
        saveTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        saveTextButton.setTextColor(getResources().getColor(R.color.backgroundWhite));
        // параметры кнопки
        LinearLayout.LayoutParams saveTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        saveTextButtonParams.gravity = Gravity.CENTER;
        saveButtonContainer.addView(saveTextButton, saveTextButtonParams);



//при нажатии...
        //согласие
        saveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по созданию кабинета и передаем ей имя
                    ((EditCabinetDialogInterface) getActivity()).editCabinet(
                            editName.getText().toString(),
                            getArguments().getLong("cabinetId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id кабинета( Bungle putLong("cabinetId",cabinetId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must give cabinetId( Bungle putLong(\"cabinetId\",cabinetId) )"
                    );
                }
                dismiss();
            }
        });

        //удаление
        negativeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по далению кабинета и передаем id
                    ((EditCabinetDialogInterface) getActivity()).removeCabinet(
                            getArguments().getLong("cabinetId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id кабинета( Bungle putLong("cabinetId",cabinetId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must give cabinetId( Bungle putLong(\"cabinetId\",cabinetId) )"
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

interface EditCabinetDialogInterface {
    void editCabinet(String name, long cabinetId);
    void removeCabinet(long cabinetId);
}