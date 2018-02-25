package com.learning.texnar13.teachersprogect.startScreen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.learning.texnar13.teachersprogect.R;

public class StartScreenRateUsDialog extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Пожалуйста оцените наше приложение)");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_learner, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_learner_dialog_fragment_linear_layout);




        //кнопка оценить
        builder.setPositiveButton("оценить!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
            }
        });
        //перенести на потом
        builder.setNegativeButton("позже", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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
            }
        });
        //не оценивать
        builder.setNeutralButton("я не хочу ставить оценку", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //вызываем в активности метод по оценке
                    ((RateInterface) getActivity()).rate(
                            2
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс RateInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "StartScreenRateUsDialog: you must implements RateInterface in your activity"
                    );
                }
            }
        });

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
}

interface RateInterface {
    void rate(int rateId);
}

