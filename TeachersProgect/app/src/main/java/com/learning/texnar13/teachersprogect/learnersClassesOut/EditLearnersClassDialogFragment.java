package com.learning.texnar13.teachersprogect.learnersClassesOut;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class EditLearnersClassDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // layout диалога
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundResource(R.drawable.base_background_dialog_full_round_white);
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
        imageBackground.setImageResource(R.drawable.__background_add_class);
        imageBackground.setAdjustViewBounds(true);
        RelativeLayout.LayoutParams imageBackgroundParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.classes_out_add_class_image_width),
                (int) getResources().getDimension(R.dimen.classes_out_add_class_image_height)
        );
        imageBackgroundParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        imageContainer.addView(imageBackground, imageBackgroundParams);

        // кнопка назад
        ImageView backImage = new ImageView(getActivity());
        backImage.setImageResource(R.drawable.base_button_arrow_back_blue);
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
        title.setText(R.string.learners_classes_out_activity_dialog_title_edit_class);
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.half_margin),
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
        editName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
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
        try { // входные данные предыдущее название
            editName.setText(getArguments().getString("name"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"name\"");
        }


        // кнопка удалить
        TextView negativeTextButton = new TextView(getActivity());
        negativeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_medium));
        negativeTextButton.setText(R.string.button_save);
        negativeTextButton.setGravity(Gravity.CENTER);
        negativeTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        negativeTextButton.setTextColor(getResources().getColor(R.color.signalRed));
        negativeTextButton.setPaintFlags(negativeTextButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        // параметры кнопки
        LinearLayout.LayoutParams negativeTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        negativeTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                0
        );
        negativeTextButtonParams.gravity = Gravity.CENTER;
        linearLayout.addView(negativeTextButton, negativeTextButtonParams);


        // контейнер кнопки сохранить
        LinearLayout saveButtonContainer = new LinearLayout(getActivity());
        saveButtonContainer.setBackgroundResource(R.drawable.base_background_button_circle_orange);
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
                (int) getResources().getDimension(R.dimen.forth_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.forth_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        saveTextButtonParams.gravity = Gravity.CENTER;
        saveButtonContainer.addView(saveTextButton, saveTextButtonParams);

        //при нажатии...
        // согласие
        saveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editName.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), R.string.learners_classes_out_activity_toast_empty_name, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        //вызываем в активности метод по созданию класса и передаем ей имя
                        ((com.learning.texnar13.teachersprogect.learnersClassesOut.EditLearnersClassDialogInterface) getActivity()).editLearnersClass(
                                editName.getText().toString().trim(),
                                getArguments().getLong("classId")
                        );
                    } catch (java.lang.ClassCastException e) {
                        //в вызвающей активности должен быть имплементирован интерфейс EditLearnersClassDialogInterface
                        e.printStackTrace();
                        Log.i(
                                "TeachersApp",
                                "EditLearnersClassDialogFragment: you must implements EditLearnersClassDialogInterface in your activity"
                        );
                    } catch (java.lang.NullPointerException e) {
                        //в диалог необходимо передать id класса( Bungle putLong("classId",classId) )
                        e.printStackTrace();
                        Log.i(
                                "TeachersApp",
                                "EditLearnersClassDialogFragment: you must give classId( Bungle putLong(\"classId\",classId) )"
                        );
                    }
                    dismiss();
                }
            }
        });

        //удаление
        negativeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по далению класса и передаем id
                    ((com.learning.texnar13.teachersprogect.learnersClassesOut.EditLearnersClassDialogInterface) getActivity()).removeLearnersClass(
                            getArguments().getLong("classId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован интерфейс EditLearnersClassDialogInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditLearnersClassDialogFragment: you must implements EditLearnersClassDialogInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id класса( Bungle putLong("classId",classId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditLearnersClassDialogFragment: you must give classId( Bungle putLong(\"classId\",classId) )"
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

interface EditLearnersClassDialogInterface {
    void editLearnersClass(String name, long classId);

    void removeLearnersClass(long classId);
}
