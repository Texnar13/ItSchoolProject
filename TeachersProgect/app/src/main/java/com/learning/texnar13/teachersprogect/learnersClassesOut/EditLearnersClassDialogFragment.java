package com.learning.texnar13.teachersprogect.learnersClassesOut;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class EditLearnersClassDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


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


        // кнопка назад
        ImageView backImage = new ImageView(getActivity());
        backImage.setImageResource(R.drawable._base_button_arrow_back_blue);
        backImage.setAdjustViewBounds(true);
        RelativeLayout.LayoutParams backImageParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.my_icon_size),
                (int) getResources().getDimension(R.dimen.my_icon_size)
        );
        imageContainer.addView(backImage, backImageParams);
        //отмена
        backImage.setOnClickListener(view -> dismiss());

        // заголовок
        TextView title = new TextView(getActivity());
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
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
        editName.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
        editName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editName.setTextColor(Color.BLACK);
        editName.setHint(R.string.learners_classes_out_activity_dialog_hint_class_name);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setHintTextColor(Color.GRAY);
        editNameContainer.addView(editName, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));
        try { // входные данные предыдущее название
            editName.setText(getArguments().getString("name"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"name\"");
        }


        // кнопка удалить
        TextView negativeTextButton = new TextView(getActivity());
        negativeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
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
        saveTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
        saveTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
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
        saveButtonContainer.setOnClickListener(view -> {

            if (editName.getText().toString().trim().length() == 0) {
                Toast.makeText(getActivity(), R.string.learners_classes_out_activity_toast_empty_name, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    //вызываем в активности метод по созданию класса и передаем ей имя
                    ((EditLearnersClassDialogInterface) getActivity()).editLearnersClass(
                            editName.getText().toString().trim()
                    );
                } catch (NullPointerException e) {
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



        final AppCompatActivity context = (AppCompatActivity)getActivity();

        //удаление
        negativeTextButton.setOnClickListener(view -> {
            try {
                //вызываем в активности метод по далению класса и передаем id
                ((EditLearnersClassDialogInterface) getActivity()).removeLearnersClass();
            } catch (NullPointerException e) {
                //в диалог необходимо передать id класса( Bungle putLong("classId",classId) )
                e.printStackTrace();
                Log.i(
                        "TeachersApp",
                        "EditLearnersClassDialogFragment: you must give classId( Bungle putLong(\"classId\",classId) )"
                );
            }
            dismiss();
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}

interface EditLearnersClassDialogInterface {
    void editLearnersClass(String name);

    void removeLearnersClass();
}
