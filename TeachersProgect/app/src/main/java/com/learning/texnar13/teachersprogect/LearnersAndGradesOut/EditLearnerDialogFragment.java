package com.learning.texnar13.teachersprogect.LearnersAndGradesOut;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class EditLearnerDialogFragment extends DialogFragment {//входные данные предыдущее имя, фамилия, id

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        //заголовок
//        builder.setTitle("Редактирование ученика");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_learner, null);
        builder.setView(dialogLayout);


        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_learner_dialog_fragment_linear_layout);
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
        title.setText(R.string.learners_and_grades_out_activity_dialog_title_edit_learner);
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        title.setAllCaps(true);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), 0);

        linearLayout.addView(title, titleParams);


//--текстовое поле имени--
        final EditText editName = new EditText(getActivity());
        editName.setTextColor(Color.BLACK);
        editName.setHint(R.string.learners_and_grades_out_activity_dialog_hint_learner_name);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        //editName.setSingleLine(true);
        editName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editName.setHintTextColor(Color.GRAY);
        try {//входные данные предыдущее имя
            editName.setText(getArguments().getString("name"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp","you must give bundle argument \"name\"");
        }

        LinearLayout editNameContainer = new LinearLayout(getActivity());
        LinearLayout.LayoutParams editNameContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editNameContainerParams.setMargins((int) pxFromDp(25), 0, (int) pxFromDp(25), 0);
        //добавляем текстовое поле
        editNameContainer.addView(
                editName,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));
        linearLayout.addView(editNameContainer, editNameContainerParams);


//--текстовое поле фамилии--
        final EditText editLastName = new EditText(getActivity());
        editLastName.setTextColor(Color.BLACK);
        editLastName.setHint(R.string.learners_and_grades_out_activity_dialog_hint_learner_second_name);
        editLastName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        //editName.setSingleLine(true);
        editLastName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editLastName.setHintTextColor(Color.GRAY);
        try {//входные данные предыдущая фамилия
            editLastName.setText(getArguments().getString("lastName"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"lastName\"");
        }

        LinearLayout editLastNameContainer = new LinearLayout(getActivity());
        LinearLayout.LayoutParams editLastNameContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editLastNameContainerParams.setMargins((int) pxFromDp(25), 0, (int) pxFromDp(25), 0);
        //добавляем текстовое поле
        editLastNameContainer.addView(
                editLastName,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));
        linearLayout.addView(editLastNameContainer, editLastNameContainerParams);


//--кнопки согласия/отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.learners_and_grades_out_activity_dialog_button_cancel);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка удалить
        Button negativeButton = new Button(getActivity());
        negativeButton.setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
        negativeButton.setText(R.string.learners_and_grades_out_activity_dialog_button_delete);
        negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        negativeButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams negativeButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        negativeButtonParams.weight = 1;
        negativeButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка сохранения
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        positiveButton.setText(R.string.learners_and_grades_out_activity_dialog_button_save);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        positiveButtonParams.weight = 1;
        positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));

        //кнопки в контейнер
        container.addView(neutralButton, neutralButtonParams);
        container.addView(negativeButton, negativeButtonParams);
        container.addView(positiveButton, positiveButtonParams);

        //контейнер в диалог
        linearLayout.addView(container);

        //при нажатии...
        //сохранение
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по созданию ученика и передаем ей имя и фамилию
                    ((EditLearnerDialogInterface) getActivity()).editLearner(
                            editLastName.getText().toString(),
                            editName.getText().toString(),
                            getArguments().getLong("learnerId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditLearnerInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditLearnerDialogFragment: you must implements EditLearnerInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id ученика( Bungle putLong("learnerId",learnerId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditLearnerDialogFragment: you must give learnerId( Bungle putLong(\"learnerId\",learnerId) )"
                    );
                }
                dismiss();
            }
        });

        //отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Log.i("TeachersApp", "EditLearnerDialogFragment - dismiss(neutralButton)");
                try {
                    //вызываем в активности метод по обновлению таблицы
                    ((UpdateTableInterface) getActivity()).updateAll();
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

        //удаление
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по далению ученика и передаем id
                    ((EditLearnerDialogInterface) getActivity()).removeLearner(
                            getArguments().getLong("learnerId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditLearnerInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditLearnerDialogFragment: you must implements EditLearnerInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id ученика( Bungle putLong("learnerId",learnerId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditLearnerDialogFragment: you must give learnerId( Bungle putLong(\"learnerId\",learnerId) )"
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
        Log.i("TeachersApp", "EditLearnerDialogFragment - onCancel");
        try {
            //вызываем в активности метод по обновлению таблицы
            ((UpdateTableInterface) getActivity()).updateAll();
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
    void editLearner(String lastName, String name, long learnerId);
    void removeLearner(long learnerId);
}

