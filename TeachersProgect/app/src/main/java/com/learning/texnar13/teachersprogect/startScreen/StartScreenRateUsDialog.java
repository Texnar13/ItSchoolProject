package com.learning.texnar13.teachersprogect.startScreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class StartScreenRateUsDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View root = requireActivity().getLayoutInflater().inflate(R.layout.start_screen_dialog_rate_us, null);
        builder.setView(root);

        // при нажатии на кнопку закрыть
        root.findViewById(R.id.start_screen_dialog_rate_us_button_close).setOnClickListener(v -> dismiss());

        // при нажатии на кнопку оценить
        root.findViewById(R.id.start_screen_dialog_rate_us_button_rate).setOnClickListener(view -> {
            //вызываем в активности метод по оценке
            ((RateInterface) requireActivity()).rate(0);
            dismiss();
        });

//        //отмена
//        negativeTextButton.setOnClickListener(view -> {
//            //вызываем в активности метод по оценке
//            ((RateInterface) getActivity()).rate(1);
//            dismiss();
//        });
//

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}

interface RateInterface {
    void rate(int rateId);
}

