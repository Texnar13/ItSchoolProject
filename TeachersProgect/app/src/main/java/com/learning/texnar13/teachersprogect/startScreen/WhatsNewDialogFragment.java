package com.learning.texnar13.teachersprogect.startScreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class WhatsNewDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        LinearLayout dialogLinearLayout = new LinearLayout(getActivity());
        dialogLinearLayout.setBackgroundResource(R.drawable.base_background_dialog_full_round_wite);
        dialogLinearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(dialogLinearLayout);

        // scroll
        ScrollView scrollView = new ScrollView(getActivity());
        dialogLinearLayout.addView(scrollView);
        // контейнер в нем
        LinearLayout scrollLayout = new LinearLayout(getActivity());
        scrollLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(scrollLayout);

        // заголовок
        TextView title = new TextView(getActivity());
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria));
        title.setTextColor(Color.BLACK);
        title.setText(R.string.start_screen_activity_dialog_whats_new_title);
        title.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.text_title_size)
        );
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                0
        );
        scrollLayout.addView(title, titleParams);

        // текст
        TextView text = new TextView(getActivity());
        text.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria));
        text.setTextColor(Color.BLACK);
        text.setText(R.string.start_screen_activity_dialog_whats_new_text);
        text.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.text_subtitle_size)
        );
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        scrollLayout.addView(text, textParams);


        // кнопка выхода

        //контейнер для нее
        RelativeLayout container = new RelativeLayout(getActivity());
        container.setBackgroundResource(R.drawable.base_background_button_circle_orange);
        container.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.CENTER;
        containerParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        //контейнер в диалог
        scrollLayout.addView(container, containerParams);

        //сама кнопка
        TextView button = new TextView(getActivity());
        button.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria));
        button.setText(R.string.start_screen_activity_dialog_whats_new_button_ok);
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        button.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        //кнопки в контейнер
        container.addView(button, buttonParams);


        // при нажатии кнопки выхода
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
