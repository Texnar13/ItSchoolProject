package com.learning.texnar13.teachersprogect.lesson;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class EndLessonDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
        linearLayout.setLayoutParams(linearLayoutParams);
        builder.setView(linearLayout);


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
        title.setText(R.string.lesson_activity_learner_dialog_out_text);
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
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin));
        Log.e("TeachersApp", "outMainMenu: " + closeImageView.getId());
        titleParams.gravity = Gravity.CENTER_VERTICAL;
        headLayout.addView(title, titleParams);


        // кнопки согласия/отмены
        // контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundResource(R.drawable._dialog_bottom_background_white);
        container.setGravity(Gravity.CENTER);
        linearLayout.addView(container);

        // кнопка отмены
        TextView neutralButton = new TextView(getActivity());
        neutralButton.setBackgroundResource(R.drawable._button_round_background_green);
        neutralButton.setGravity(Gravity.CENTER);
        neutralButton.setText(R.string.lesson_activity_learner_dialog_out_no);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.my_buttons_height_size)
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        container.addView(neutralButton, neutralButtonParams);
        // при нажатии
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // кнопка согласия
        TextView positiveButton = new TextView(getActivity());
        positiveButton.setBackgroundResource(R.drawable._button_round_background_pink);
        positiveButton.setPadding(
                (int) getResources().getDimension(R.dimen.double_margin),
                0,
                (int) getResources().getDimension(R.dimen.double_margin),
                0
        );
        positiveButton.setGravity(Gravity.CENTER);
        positiveButton.setText(R.string.lesson_activity_learner_dialog_out_yes);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.my_buttons_height_size)
        );
        positiveButtonParams.weight = 1;
        positiveButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        container.addView(positiveButton, positiveButtonParams);
        // при нажатии
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //вызываем в активности метод по созданию кабинета и передаем ей имя
                ((EndLessonInterface) getActivity()).endLesson();
                dismiss();
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    // преобразование зависимой величины в пиксели
    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface EndLessonInterface {
    void endLesson();
}
