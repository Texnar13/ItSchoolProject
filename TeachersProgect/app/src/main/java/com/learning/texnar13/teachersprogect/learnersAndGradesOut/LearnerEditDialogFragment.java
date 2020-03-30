package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

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
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class LearnerEditDialogFragment extends DialogFragment {//входные данные предыдущее имя, фамилия, id

    // константы по которым в диалог передаются аргументы
    public static final String ARGS_LEARNER_NAME = "name";
    public static final String ARGS_LEARNER_LAST_NAME = "lastName";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // layout диалога
        LinearLayout linear = new LinearLayout(getActivity());
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linear.setLayoutParams(linearParams);
        builder.setView(linear);


        // базовый скролл
        ScrollView scrollView = new ScrollView(getActivity());
        LinearLayout.LayoutParams scrollViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linear.addView(scrollView, scrollViewParams);

        // layout в скролле
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundResource(R.drawable._dialog_full_background_white);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        scrollView.addView(linearLayout, linearLayoutParams);


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
        imageBackground.setImageResource(R.drawable.__background_add_learner_image);
        imageBackground.setAdjustViewBounds(true);
        RelativeLayout.LayoutParams imageBackgroundParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.learners_and_grades_add_learner_image_width),
                (int) getResources().getDimension(R.dimen.learners_and_grades_add_learner_image_height)
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
                try {
                    //вызываем в активности метод по обновлению таблицы
                    ((UpdateTableInterface) getActivity()).allowUserEditLearners();
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс UpdateTableInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnerDialogFragment: you must implements UpdateTableInterface in your activity"
                    );
                }
            }
        });

        // заголовок
        TextView title = new TextView(getActivity());
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_medium));
        title.setText(R.string.learners_and_grades_out_activity_dialog_title_edit_learner);
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
        editNameContainer.setOrientation(LinearLayout.VERTICAL);
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

        // текстовое поле фамилии
        final EditText editSurname = new EditText(getActivity());
        editSurname.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        editSurname.setTextColor(Color.BLACK);
        editSurname.setHint(R.string.learners_and_grades_out_activity_dialog_hint_learner_second_name);
        editSurname.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editSurname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editSurname.setHintTextColor(Color.GRAY);
        editNameContainer.addView(editSurname,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
        try {//входные данные предыдущая фамилия
            editSurname.setText(getArguments().getString(ARGS_LEARNER_LAST_NAME));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"" + ARGS_LEARNER_LAST_NAME + "\"");
        }

        // текстовое поле имени
        final EditText editName = new EditText(getActivity());
        editName.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        editName.setTextColor(Color.BLACK);
        editName.setHint(R.string.learners_and_grades_out_activity_dialog_hint_learner_name);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editName.setHintTextColor(Color.GRAY);
        editNameContainer.addView(editName,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
        try {//входные данные предыдущее имя
            editName.setText(getArguments().getString(ARGS_LEARNER_NAME));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"" + ARGS_LEARNER_NAME + "\"");
        }

        // кнопка удалить
        TextView negativeTextButton = new TextView(getActivity());
        negativeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        negativeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        negativeTextButton.setText(R.string.learners_and_grades_out_activity_dialog_button_delete);
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


        //при нажатии...
        //сохранение
        createButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по созданию ученика и передаем ей имя и фамилию
                    ((EditLearnerDialogInterface) getActivity()).editLearner(
                            editSurname.getText().toString(),
                            editName.getText().toString()
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditLearnerInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "LearnerEditDialogFragment: you must implements EditLearnerInterface in your activity"
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
                    //вызываем в активности метод по далению ученика и передаем id
                    ((EditLearnerDialogInterface) getActivity()).removeLearner();
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditLearnerInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "LearnerEditDialogFragment: you must implements EditLearnerInterface in your activity"
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
        Log.i("TeachersApp", "LearnerEditDialogFragment - onCancel");
        try {
            //вызываем в активности метод по обновлению таблицы
            ((UpdateTableInterface) getActivity()).allowUserEditLearners();
        } catch (java.lang.ClassCastException e) {
            //в вызвающей активности должен быть имплементирован класс UpdateTableInterface
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "CreateLearnerDialogFragment: you must implements UpdateTableInterface in your activity"
            );
        }
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface EditLearnerDialogInterface {
    void editLearner(String lastName, String name);

    void removeLearner();
}

