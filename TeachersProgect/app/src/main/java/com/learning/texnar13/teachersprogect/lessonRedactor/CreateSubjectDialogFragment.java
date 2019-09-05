package com.learning.texnar13.teachersprogect.lessonRedactor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

//------------диалог создания пердмета------------
public class CreateSubjectDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //layout диалога
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundResource(R.color.colorBackGround);
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
        title.setText(R.string.lesson_redactor_activity_dialog_title_add_subject);
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
        editName.setHint(R.string.lesson_redactor_activity_dialog_hint_subject_name);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setHintTextColor(Color.GRAY);

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

//--кнопки согласия/отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.lesson_redactor_activity_dialog_button_cancel);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int)getResources().getDimension(R.dimen.my_buttons_height_size)
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка согласия
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        positiveButton.setText(R.string.lesson_redactor_activity_dialog_button_add);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int)getResources().getDimension(R.dimen.my_buttons_height_size)
        );
        positiveButtonParams.weight = 1;
        positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));


        //кнопки в контейнер
        container.addView(neutralButton, neutralButtonParams);
        container.addView(positiveButton, positiveButtonParams);

        //контейнер в диалог
        linearLayout.addView(container);


        //при нажатии...
        //согласие
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SubjectNameDialogInterface) getActivity())
                        .lessonNameDialogMethod(
                                1,
                                getArguments().getStringArray("stringLessons").length - 2,
                                editName.getText().toString()
                        );

                dismiss();
            }
        });

        //отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SubjectNameDialogInterface) getActivity())
                        .lessonNameDialogMethod(
                                0,
                                0,
                                ""
                        );
                dismiss();
            }
        });
        return builder.create();
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

//обратная связь от диалога
interface SubjectNameDialogInterface {
    void lessonNameDialogMethod(int code, int position, String classNameText);
}
