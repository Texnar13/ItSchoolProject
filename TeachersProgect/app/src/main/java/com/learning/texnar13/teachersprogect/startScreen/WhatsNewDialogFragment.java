package com.learning.texnar13.teachersprogect.startScreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class WhatsNewDialogFragment extends DialogFragment {

//    public static final String ARGUMENT_SHOWN_TEXT = "shownText";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //layout диалога
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_start_screen_whats_new, null);
        builder.setView(dialogView);
//---контейнер---
        LinearLayout dialogLinearLayout = (LinearLayout) dialogView.findViewById(R.id.dialog_fragment_layout_start_screen_whats_new_linear_layout);
// --- scroll ---
        ScrollView scrollView = new ScrollView(getActivity());
        dialogLinearLayout.addView(scrollView);
// --- контейнер в нем ---
        LinearLayout scrollLayout = new LinearLayout(getActivity());
        scrollLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(scrollLayout);
//---заголовок---
        TextView title = new TextView(getActivity());
        title.setTextColor(Color.BLACK);
        title.setText(R.string.start_screen_activity_dialog_whats_new_title);
        title.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.text_title_size)
        );
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins((int)pxFromDp(15),(int)pxFromDp(15),(int)pxFromDp(15),(int)pxFromDp(0));
        scrollLayout.addView(title,titleParams);
//---текст---
        TextView text = new TextView(getActivity());
        text.setTextColor(Color.BLACK);
        text.setText(R.string.start_screen_activity_dialog_whats_new_text);
        text.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.text_subtitle_size)
        );
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins((int)pxFromDp(30),(int)pxFromDp(15),(int)pxFromDp(30),(int)pxFromDp(15));
        scrollLayout.addView(text,textParams);
//---кнопка выхода---
        //контейнер для нее
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);
        //сама кнопка
        Button button = new Button(getActivity());
        button.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        button.setText(R.string.start_screen_activity_dialog_whats_new_button_ok);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        button.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        buttonParams.weight = 1;
        buttonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));
        //кнопки в контейнер
        container.addView(button, buttonParams);
        //контейнер в диалог
        scrollLayout.addView(container);
        //при нажатии...
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

//        String text;
//
//        try {
//            text = getArguments().getString(ARGUMENT_SHOWN_TEXT);
//        } catch (NullPointerException e) {
//            Log.e("TeachersApp", "WhatsNewDialogFragment - You must give bundle argument ARGUMENT_SHOWN_TEXT (String)");
//            e.printStackTrace();
//        }

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}
