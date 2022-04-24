package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class EditDarkModeDialogFragment extends DialogFragment {


    // передаваемые параметры
    public static String ARGS_CURRENT_DAY_NIGHT_MODE= "currentMode";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_edit_locale, null);
        builder.setView(dialogLayout);

        // меняем заголовок
        ((TextView)dialogLayout.findViewById(R.id.settings_dialog_edit_locale_title)).setText(
                R.string.settings_activity_dialog_edit_theme_title
        );


        // LinearLayout в скролле для вывода списка
        LinearLayout listOut = dialogLayout.findViewById(R.id.settings_dialog_edit_locale_out);
        listOut.setOrientation(LinearLayout.VERTICAL);

        // при нажатии на кнопку закрыть
        dialogLayout.findViewById(R.id.settings_dialog_edit_locale_cancel_button).setOnClickListener(v -> dismiss());


        //--------ставим диалогу список в виде view--------
        // названия и коды из констант
        String[] dayNightNames = getResources().getStringArray(R.array.day_night);
        // номер прошлой темы
        int lastThemePoz = getArguments().getInt(ARGS_CURRENT_DAY_NIGHT_MODE);


        // проходимся по списку
        for (int i = 0; i < dayNightNames.length; i++) {
            // текст
            TextView text = new TextView(getActivity());
            text.setText(dayNightNames[i]);
            text.setGravity(Gravity.CENTER_VERTICAL);
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.simple_buttons_text_size));
            text.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_semibold));
            if (lastThemePoz == i) {
                text.setTextColor(getResources().getColor(R.color.base_blue));
            } else {
                text.setTextColor(getResources().getColor(R.color.text_color_simple));
            }

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.simple_buttons_height)
            );
            listOut.addView(text, itemParams);

            //нажатие на пункт списка
            final int number = i;
            text.setOnClickListener(view -> {
                ((EditDarkModeDialogFragmentInterface) getActivity()).editDarkMode(number);
                dismiss();
            });
        }

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        return dialog;
    }
}

interface EditDarkModeDialogFragmentInterface {
    void editDarkMode(int newModePos);
}

