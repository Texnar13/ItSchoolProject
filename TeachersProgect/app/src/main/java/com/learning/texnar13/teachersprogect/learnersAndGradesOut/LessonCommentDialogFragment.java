package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class LessonCommentDialogFragment extends DialogFragment {

    public static final String ARGS_PREVIOUS_TEXT = "noteText";


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // ---- layout диалога ----
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundResource(R.drawable._dialog_full_background_white);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        // текст заголовка
        TextView title = new TextView(getActivity());
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_medium));
        title.setText(getResources().getString(R.string.learners_and_grades_out_activity_dialog_title_lesson_comment));
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                getResources().getDimensionPixelOffset(R.dimen.double_margin),
                getResources().getDimensionPixelOffset(R.dimen.double_margin),
                getResources().getDimensionPixelOffset(R.dimen.double_margin),
                getResources().getDimensionPixelOffset(R.dimen.simple_margin)
        );
        titleParams.gravity = Gravity.CENTER_VERTICAL;
        linearLayout.addView(title, titleParams);

        // получаем старый текст
        String text = "";
        Bundle args = getArguments();
        if (args != null) {
            text = getArguments().getString(ARGS_PREVIOUS_TEXT);
            if (text == null) {
                text = "";
            }
        }

        // поле для текста
        final EditText edit = new EditText(getActivity());
        edit.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_medium));
        edit.setHint(getResources().getString(R.string.learners_and_grades_out_activity_dialog_hint_lesson_comment));
        edit.setText(text);
        edit.setGravity(Gravity.CENTER_VERTICAL);
        edit.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
        edit.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        // ставим ограничение в 3 строки
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(70)});
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        editParams.setMargins(
                getResources().getDimensionPixelOffset(R.dimen.double_margin),
                0,
                getResources().getDimensionPixelOffset(R.dimen.double_margin),
                0
        );
        editParams.gravity = Gravity.CENTER_VERTICAL;
        linearLayout.addView(edit, editParams);

        // контейнер кнопки создать
        LinearLayout createButtonContainer = new LinearLayout(getActivity());
        createButtonContainer.setBackgroundResource(R.drawable._button_round_background_green);
        // параметры контейнера
        LinearLayout.LayoutParams saveTextButtonContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextButtonContainerParams.setMargins(
                getResources().getDimensionPixelOffset(R.dimen.simple_margin),
                getResources().getDimensionPixelOffset(R.dimen.simple_margin),
                getResources().getDimensionPixelOffset(R.dimen.simple_margin),
                getResources().getDimensionPixelOffset(R.dimen.half_more_margin)
        );
        saveTextButtonContainerParams.gravity = Gravity.CENTER;
        linearLayout.addView(createButtonContainer, saveTextButtonContainerParams);

        // кнопка создать
        final TextView createTextButton = new TextView(getActivity());
        createTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        createTextButton.setText(getResources().getString(R.string.learners_and_grades_out_activity_dialog_button_save));
        createTextButton.setGravity(Gravity.CENTER);
        createTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        createTextButton.setTextColor(getResources().getColor(R.color.backgroundWhite));
        // параметры кнопки
        LinearLayout.LayoutParams createTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        createTextButtonParams.setMargins(
                getResources().getDimensionPixelOffset(R.dimen.forth_margin),
                getResources().getDimensionPixelOffset(R.dimen.simple_margin),
                getResources().getDimensionPixelOffset(R.dimen.forth_margin),
                getResources().getDimensionPixelOffset(R.dimen.simple_margin)
        );
        createTextButtonParams.gravity = Gravity.CENTER;
        createButtonContainer.addView(createTextButton, createTextButtonParams);
        // при нажатии на кнопку
        createButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // передаем название нового предмета активности
                try {
                    // вызываем в активности метод по созданию предмета
                    ((LessonCommentDialogInterface) getActivity()).setCommentText(edit.getText().toString());
                } catch (java.lang.ClassCastException e) {
                    e.printStackTrace();
                    Log.e("TeachersApp",
                            "SubjectsDialogFragment: you must implements LessonCommentDialogInterface in your activity"
                    );
                }

                // закрываем диалог
                dismiss();
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        //вызываем в активности метод разрешения изменения оценок
        ((AllowEditGradesInterface) getActivity()).allowUserEditGrades();
    }
}


interface LessonCommentDialogInterface {
    // установить текст
    void setCommentText(String text);
}