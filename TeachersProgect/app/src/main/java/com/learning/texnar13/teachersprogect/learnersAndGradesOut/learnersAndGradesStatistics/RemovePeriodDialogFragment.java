package com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;

public class RemovePeriodDialogFragment extends DialogFragment {

    public static final String PROFILES_ID_INTENT = "profilesId";
    public static final String PROFILES_NAMES_INTENT = "profilesNames";

    // полученные данные
    long[] profilesId;
    String[] profilesNames;
    // данные созданные в процессе работы
    boolean[] checkedProfiles;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_delete_period, null);
        builder.setView(dialogLayout);
// -- список в диалоге --
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.delete_period_dialog_fragment_periods_out_layout);

// --- получаем список профилей ---
        profilesId = getArguments().getLongArray(PROFILES_ID_INTENT);
        profilesNames = getArguments().getStringArray(PROFILES_NAMES_INTENT);
        checkedProfiles = new boolean[profilesId.length];

// --- выводим список с чекбоксами
        for (int i = 0; i < profilesId.length; i++) {
            // контейнер
            // -текст
            // -чекбокс

            // контейнер
            final RelativeLayout itemContainer = new RelativeLayout(getActivity());

            linearLayout.addView(itemContainer, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            // текст
            TextView itemText = new TextView(getActivity());
            itemText.setTextColor(Color.BLACK);
            itemText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            itemText.setText(profilesNames[i]);
            RelativeLayout.LayoutParams itemTextParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            itemTextParams.leftMargin = (int) pxFromDp(10);
            itemTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            itemTextParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            itemContainer.addView(itemText, itemTextParams);
            // чекбокс
            final CheckBox itemCheckBox = new CheckBox(getActivity());
            RelativeLayout.LayoutParams itemCheckBoxParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            itemCheckBoxParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            itemCheckBoxParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            itemContainer.addView(itemCheckBox, itemCheckBoxParams);
            // для текста
            itemTextParams.addRule(RelativeLayout.START_OF, itemCheckBox.getId());
            itemTextParams.addRule(RelativeLayout.LEFT_OF, itemCheckBox.getId());

            // при нажатии
            final int finalI = i;
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkedProfiles[finalI]) {// если чекбокс уже выбран
                        // убираем цвет
                        itemContainer.setBackgroundColor(Color.TRANSPARENT);
                        // убираем отметку
                        itemCheckBox.setChecked(false);
                        // убираем из массива
                        checkedProfiles[finalI] = false;
                    } else {// если чекбокс еще не выбран
                        //закрашиваем
                        itemContainer.setBackgroundColor(getResources().getColor(R.color.colorBackGroundDark));
                        // отмечаем
                        itemCheckBox.setChecked(true);
                        // сохраняем в массив
                        checkedProfiles[finalI] = true;
                    }
                }
            };
            itemContainer.setOnClickListener(clickListener);
            itemCheckBox.setOnClickListener(clickListener);
        }

// -- кнопки согласия/отмены --
        // кнопка отмены
        Button neutralButton = (Button) dialogLayout.findViewById(R.id.delete_period_dialog_fragment_button_cancel);
        // кнопка сохранения
        Button positiveButton = (Button) dialogLayout.findViewById(R.id.delete_period_dialog_fragment_button_remove);
        // при нажатии...
        // сохранение
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ArrayList<Long> checkedId = new ArrayList<>();
                    for (int i = 0; i < profilesId.length; i++) {
                        if (checkedProfiles[i]) {
                            checkedId.add(profilesId[i]);
                        }
                    }

                    //вызываем в активности метод по удалению промежутков статистики
                    ((RemoveStatisticDialogInterface) getActivity()).removeStatistic(
                            checkedId
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс RemoveStatisticDialogInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "RemovePeriodDialogFragment: you must implements RemoveStatisticDialogInterface in your activity"
                    );
                }
                dismiss();
            }
        });

        // отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по обновлению спиннера
                    ((RemoveStatisticDialogInterface) getActivity()).onlyUpdate();
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс RemoveStatisticDialogInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "RemovePeriodDialogFragment: you must implements RemoveStatisticDialogInterface in your activity"
                    );
                }
                dismiss();
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
        try {
            //вызываем в активности метод по обновлению спиннера
            ((RemoveStatisticDialogInterface) getActivity()).onlyUpdate();
        } catch (java.lang.ClassCastException e) {
            //в вызвающей активности должен быть имплементирован класс RemoveStatisticDialogInterface
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "RemovePeriodDialogFragment: you must implements RemoveStatisticDialogInterface in your activity"
            );
        }
        super.onCancel(dialog);
        Log.i("TeachersApp", "RemoveStatisticDialogInterface - onCancel");
    }

// --------- форматы ----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface RemoveStatisticDialogInterface {
    void removeStatistic(ArrayList<Long> chosenProfilesIDArray);

    void onlyUpdate();
}

