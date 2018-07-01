package com.learning.texnar13.teachersprogect.settings;

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
import android.widget.ScrollView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class EditLocaleDialogFragment extends DialogFragment {
    /*
    *

    private SharedPreferences preferences;
    private Locale locale;
    private String lang;

    @Override
    public void onCreate() {
        //получаем предыдущие данные
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //извлекаем язык
        lang = preferences.getString("lang", "default");//здесь просто получение строки из диалога ..default..en..ru..
        //по умолчанию?
        if (lang.equals("default")) {lang=getResources().getConfiguration().locale.getCountry();}
        //новая локализация
        locale = new Locale(lang);
        Locale.setDefault(locale);
        //новые настройки
        Configuration config = new Configuration();
        config.locale = locale;
        //сохраняем новые настройки
        getBaseContext().getResources().updateConfiguration(config, null);

        super.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }

    */

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //начинаем строить диалог
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());

        //layout диалога
        LinearLayout out = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_lesson_redactor_dialog_lesson_name, null);
        builder.setView(out);

        //--LinearLayout в layout файле--
        LinearLayout linearLayout = (LinearLayout) out.findViewById(R.id.create_lesson_dialog_fragment_linear_layout);
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
        title.setText(R.string.settings_activity_dialog_edit_locale_title);
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
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F));
        LinearLayout linear = new LinearLayout(getActivity());
        linear.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linear);


        for (int i = 0; i < localeСodes.length; i++) {
//--------пункт списка--------
            //контейнер
            LinearLayout item = new LinearLayout(getActivity());
            if (lastLocaleNumber == i) {
                item.setBackgroundColor(getResources().getColor(R.color.colorBackGroundDark));
            }
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams itemParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) (pxFromDp(40) * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))
                    );
            itemParams.setMargins(
                    (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                    (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                    (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                    (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier)))
            );
            linear.addView(item, itemParams);
            //текст в нем
            TextView text = new TextView(getActivity());
            text.setText(localeNames[i]);
            text.setTextColor(Color.BLACK);
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

//--кнопка отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.lesson_redactor_activity_dialog_button_cancel);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));
        //кнопки в контейнер
        container.addView(neutralButton, neutralButtonParams);
        //контейнер в диалог
        linearLayout.addView(container);
        //отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return builder.create();
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

interface EditLocaleDialogFragmentInterface {
    void editLocale(String newLocale);
}

