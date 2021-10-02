package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class EditLocaleDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_edit_locale, null);
        builder.setView(dialogLayout);

        // LinearLayout в скролле для вывода списка
        LinearLayout listOut = dialogLayout.findViewById(R.id.settings_dialog_edit_locale_out);
        listOut.setOrientation(LinearLayout.VERTICAL);

        // при нажатии на кнопку закрыть
        dialogLayout.findViewById(R.id.settings_dialog_edit_locale_cancel_button).setOnClickListener(v -> dismiss());


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


        // проходимся по списку
        for (int i = 0; i < localeСodes.length; i++) {
            //контейнер
//            LinearLayout item = new LinearLayout(getActivity());
//
//            item.setOrientation(LinearLayout.HORIZONTAL);
//            item.setGravity(Gravity.CENTER_VERTICAL);
//            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//            );
//            itemParams.setMargins(
//                    getResources().getDimensionPixelOffset(R.dimen.double_margin),
//                    getResources().getDimensionPixelOffset(R.dimen.simple_margin),
//                    getResources().getDimensionPixelOffset(R.dimen.simple_margin),
//                    getResources().getDimensionPixelOffset(R.dimen.simple_margin)
//            );
//            linear.addView(item, itemParams);
            // текст
            TextView text = new TextView(getActivity());
            text.setText(localeNames[i]);
            text.setGravity(Gravity.CENTER_VERTICAL);
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.simple_buttons_text_size));
            if (lastLocaleNumber == i) {
                text.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_semibold));
                text.setTextColor(getResources().getColor(R.color.baseBlue));
            } else {
                text.setTextColor(Color.BLACK);
            }

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.settings_activity_edit_locale_buttons_height)
            );
            listOut.addView(text, itemParams);

            //нажатие на пункт списка
            final int number = i;
            text.setOnClickListener(view -> {
                ((EditLocaleDialogFragmentInterface) getActivity()).editLocale(localeСodes[number]);
                dismiss();
            });
        }


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
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

