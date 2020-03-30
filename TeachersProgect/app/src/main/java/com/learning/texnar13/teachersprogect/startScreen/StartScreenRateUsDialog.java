package com.learning.texnar13.teachersprogect.startScreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class StartScreenRateUsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //layout диалога
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
        TextView title = new TextView(getActivity());
        title.setText(R.string.start_screen_activity_dialog_title_rate);
        title.setTextColor(Color.BLACK);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_title_size));
        title.setAllCaps(true);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins((int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(15), (int) pxFromDp(10));
        linearLayout.addView(title, titleParams);


        RatingBar ratingBarFive = new RatingBar(getActivity());
        ratingBarFive.setIsIndicator(true);
        ratingBarFive.setNumStars(5);
        ratingBarFive.setRating(5);
        linearLayout.addView(ratingBarFive, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

//--кнопки согласия/отмены--

        // контейнер кнопки оценить
        LinearLayout positiveButtonContainer = new LinearLayout(getActivity());
        positiveButtonContainer.setBackgroundResource(R.drawable._button_round_background_green);
        // параметры контейнера
        LinearLayout.LayoutParams positiveButtonContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        positiveButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.half_margin)
        );
        positiveButtonContainerParams.gravity = Gravity.CENTER;
        linearLayout.addView(positiveButtonContainer, positiveButtonContainerParams);

        // кнопка сохранить
        TextView positiveTextButton = new TextView(getActivity());
        positiveTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        positiveTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        positiveTextButton.setText(R.string.start_screen_activity_dialog_button_rate_now);
        positiveTextButton.setGravity(Gravity.CENTER);
        positiveTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        positiveTextButton.setTextColor(getResources().getColor(R.color.backgroundWhite));
        // параметры кнопки
        LinearLayout.LayoutParams positiveTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        positiveTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.forth_margin)+(int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.forth_margin)+(int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        positiveTextButtonParams.gravity = Gravity.CENTER;
        positiveButtonContainer.addView(positiveTextButton, positiveTextButtonParams);


        // кнопка отмены
        TextView negativeTextButton = new TextView(getActivity());
        negativeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        negativeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        negativeTextButton.setText(R.string.start_screen_activity_dialog_button_later);
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
                (int) getResources().getDimension(R.dimen.half_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        negativeTextButtonParams.gravity = Gravity.CENTER;
        linearLayout.addView(negativeTextButton, negativeTextButtonParams);


        //при нажатии...
        //согласие
        positiveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по оценке
                    ((RateInterface) getActivity()).rate(
                            0
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс RateInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "StartScreenRateUsDialog: you must implements RateInterface in your activity"
                    );
                }
                dismiss();
            }
        });

        //отмена
        negativeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по оценке
                    ((RateInterface) getActivity()).rate(
                            1
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс RateInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "StartScreenRateUsDialog: you must implements RateInterface in your activity"
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

interface RateInterface {
    void rate(int rateId);
}

