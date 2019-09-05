package com.learning.texnar13.teachersprogect.learnersClassesOut;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class CreateLearnersClassDialogFragment extends DialogFragment {
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
        imageBackground.setBackgroundResource(R.drawable.__background_add_class);
        imageBackground.setAdjustViewBounds(true);
        RelativeLayout.LayoutParams imageBackgroundParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.classes_out_add_class_image_width),
                (int) getResources().getDimension(R.dimen.classes_out_add_class_image_height)
        );
        imageBackgroundParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageBackgroundParams.topMargin = (int) getResources().getDimension(R.dimen.double_margin);
        imageContainer.addView(imageBackground, imageBackgroundParams);

        // кнопка назад
        ImageView backImage = new ImageView(getActivity());
        backImage.setBackgroundResource(R.drawable.__button_back_arrow_dark_gray);
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
        title.setText(R.string.learners_classes_out_activity_dialog_title_create_class);
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
        editName.setHint(R.string.learners_classes_out_activity_dialog_hint_class_name);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setHintTextColor(Color.GRAY);
        editNameContainer.addView(editName,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );


        // контейнер кнопки создать
        LinearLayout createButtonContainer = new LinearLayout(getActivity());
        createButtonContainer.setBackgroundResource(R.drawable._button_round_background_green);
        // параметры контейнера
        LinearLayout.LayoutParams saveTextButtonContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        saveTextButtonContainerParams.gravity = Gravity.CENTER;
        linearLayout.addView(createButtonContainer, saveTextButtonContainerParams);

        // кнопка создать
        final TextView createTextButton = new TextView(getActivity());
        createTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        createTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        createTextButton.setText(R.string.button_save);
        createTextButton.setGravity(Gravity.CENTER);
        createTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        createTextButton.setTextColor(getResources().getColor(R.color.backgroundWhite));
        // параметры кнопки
        LinearLayout.LayoutParams createTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        createTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        createTextButtonParams.gravity = Gravity.CENTER;
        createButtonContainer.addView(createTextButton, createTextButtonParams);























//        //layout диалога
//        LinearLayout linearLayout = new LinearLayout(getActivity());
//        linearLayout.setBackgroundResource(R.color.colorBackGround);
//        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        linearLayout.setGravity(Gravity.CENTER);
//        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
//        linearLayout.setLayoutParams(linearLayoutParams);
//        builder.setView(linearLayout);
//
////--заголовок--
//        TextView title = new TextView(getActivity());
//        title.setText(R.string.learners_classes_out_activity_dialog_title_create_class);
//        title.setTextColor(Color.BLACK);
//        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
//        title.setAllCaps(true);
//        title.setGravity(Gravity.CENTER);
//
//        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//        titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);
//
//        linearLayout.addView(title, titleParams);
//
//
////--текстовое поле имени--
//        final EditText editName = new EditText(getActivity());
//        editName.setTextColor(Color.BLACK);
//        editName.setHint(R.string.learners_classes_out_activity_dialog_hint_class_name);
//        editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
//        editName.setInputType(InputType.TYPE_CLASS_TEXT);
//        editName.setHintTextColor(Color.GRAY);
//
//        LinearLayout editNameContainer = new LinearLayout(getActivity());
//        LinearLayout.LayoutParams editNameContainerParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//        editNameContainerParams.setMargins((int) pxFromDp(25), 0, (int) pxFromDp(25), 0);
//        //добавляем текстовое поле
//        editNameContainer.addView(
//                editName,
//                new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT
//                ));
//        linearLayout.addView(editNameContainer, editNameContainerParams);
//
////--кнопки согласия/отмены--
//        //контейнер для них
//        LinearLayout container = new LinearLayout(getActivity());
//        container.setOrientation(LinearLayout.HORIZONTAL);
//        container.setGravity(Gravity.CENTER);
//
//        //кнопка отмены
//        Button neutralButton = new Button(getActivity());
//        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
//        neutralButton.setText(R.string.learners_classes_out_activity_dialog_button_cancel);
//        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
//        neutralButton.setTextColor(Color.WHITE);
//        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                (int)getResources().getDimension(R.dimen.my_buttons_height_size)
//        );
//        neutralButtonParams.weight = 1;
//        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));
//
//        //кнопка согласия
//        Button positiveButton = new Button(getActivity());
//        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
//        positiveButton.setText(R.string.learners_classes_out_activity_dialog_button_add);
//        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
//        positiveButton.setTextColor(Color.WHITE);
//        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                (int)getResources().getDimension(R.dimen.my_buttons_height_size)
//        );
//        positiveButtonParams.weight = 1;
//        positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));
//
//
//        //кнопки в контейнер
//        container.addView(neutralButton, neutralButtonParams);
//        container.addView(positiveButton, positiveButtonParams);
//
//        //контейнер в диалог
//        linearLayout.addView(container);


        // при нажатии...
        // создание
        createTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по созданию класса и передаем ей имя
                    ((com.learning.texnar13.teachersprogect.learnersClassesOut.CreateLearnersClassDialogInterface) getActivity()).createLearnersClass(
                            editName.getText().toString()
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс CreateLearnersClassInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnersClassDialogFragment: you must implements CreateLearnersClassInterface in your activity"
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

interface CreateLearnersClassDialogInterface {
    void createLearnersClass(String name);
}
