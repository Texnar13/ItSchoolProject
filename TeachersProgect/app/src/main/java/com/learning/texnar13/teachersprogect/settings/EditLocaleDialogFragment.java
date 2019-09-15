package com.learning.texnar13.teachersprogect.settings;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class EditLocaleDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //layout диалога
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
                (int) getResources().getDimension(R.dimen.simple_margin),
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
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
        title.setText(R.string.settings_activity_dialog_edit_locale_title);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                0,
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin));
        Log.e("TeachersApp", "outMainMenu: " + closeImageView.getId());
        titleParams.gravity = Gravity.CENTER_VERTICAL;
        headLayout.addView(title, titleParams);


//--список языков--
        //--------ставим диалогу список в виде view--------
        //названия и коды из констант
        String[] localeNames = getResources().getStringArray(R.array.locale_names);
        final String[] localeСodes = getResources().getStringArray(R.array.locale_code);
        String lastLocale = getArguments().getString("locale");
        //номер прошлой локали
        int lastLocaleNumber = 0;
        for (int i = 0; i < localeСodes.length; i++) {
            if (localeСodes[i].equals(lastLocale)) {
                lastLocaleNumber = i;
            }

        }


        //контейнеры для прокрутки
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.setBackgroundResource(R.drawable._dialog_bottom_background_white);
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F));

        LinearLayout linear = new LinearLayout(getActivity());
        linear.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linear);

        // проходимся по списку
        for (int i = 0; i < localeСodes.length; i++) {
            //контейнер
            LinearLayout item = new LinearLayout(getActivity());

            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.setMargins(
                    (int) getResources().getDimension(R.dimen.double_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin)
            );
            linear.addView(item, itemParams);
            //текст в нем
            TextView text = new TextView(getActivity());
            text.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            text.setText(localeNames[i]);
            if (lastLocaleNumber == i) {
                text.setTextColor(getResources().getColor(R.color.baseBlue));
            } else text.setTextColor(Color.BLACK);
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            item.addView(text);

            //нажатие на пункт списка
            final int number = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((EditLocaleDialogFragmentInterface) getActivity()).editLocale(localeСodes[number]);
                    dismiss();
                }
            });
        }


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    //---------форматы----------

    private int pxFromDp(float px) {
        return (int) (px * getActivity().getResources().getDisplayMetrics().density);
    }
}

interface EditLocaleDialogFragmentInterface {
    void editLocale(String newLocale);
}

