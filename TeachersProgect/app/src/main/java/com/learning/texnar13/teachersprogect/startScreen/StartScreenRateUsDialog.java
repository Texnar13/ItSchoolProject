package com.learning.texnar13.teachersprogect.startScreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class StartScreenRateUsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_learner, null);
        builder.setView(dialogLayout);
        //--LinearLayout в layout файле--
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
        title.setText(R.string.start_screen_activity_dialog_title_rate);
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


        RatingBar ratingBarFive = new RatingBar(getActivity());
        ratingBarFive.setIsIndicator(true);
        ratingBarFive.setNumStars(5);
        ratingBarFive.setRating(5);
        linearLayout.addView(ratingBarFive, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        RatingBar ratingBarThree = new RatingBar(getActivity());
        ratingBarThree.setIsIndicator(true);
        ratingBarThree.setNumStars(5);
        ratingBarThree.setRating(2.5F);
        linearLayout.addView(ratingBarThree, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

//--кнопки согласия/отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
        neutralButton.setText(R.string.start_screen_activity_dialog_button_later);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка согласия
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        positiveButton.setText(R.string.start_screen_activity_dialog_button_rate_now);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
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
        neutralButton.setOnClickListener(new View.OnClickListener() {
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







//        //кнопка оценить
//        builder.setPositiveButton("оценить!", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                try {
//                    //вызываем в активности метод по оценке
//                    ((RateInterface) getActivity()).rate(
//                            0
//                    );
//                } catch (java.lang.ClassCastException e) {
//                    //в вызвающей активности должен быть имплементирован класс RateInterface
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "StartScreenRateUsDialog: you must implements RateInterface in your activity"
//                    );
//                }
//            }
//        });
//        //перенести на потом
//        builder.setNegativeButton("позже", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                try {
//                    //вызываем в активности метод по оценке
//                    ((RateInterface) getActivity()).rate(
//                            1
//                    );
//                } catch (java.lang.ClassCastException e) {
//                    //в вызвающей активности должен быть имплементирован класс RateInterface
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "StartScreenRateUsDialog: you must implements RateInterface in your activity"
//                    );
//                }
//            }
//        });

        return builder.create();
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

