package com.learning.texnar13.teachersprogect.cabinetsOut;

import android.app.AlertDialog;
import android.app.Dialog;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import com.learning.texnar13.teachersprogect.R;

public class EditCabinetDialogFragment extends DialogFragment {

    public static final String ARG_CABINET_NAME = "name";
    public static final String ARG_CABINET_ID = "cabinetId";
    public static final String ARG_ARRAY_CLASSES_ID = "classesId";
    public static final String ARG_ARRAY_CLASSES_NAMES = "classesNames";


    // быда ли нажата кнопка удалить
    boolean isDelete = false;

    // название кабинета
    String startName;
    // id кабинета
    long cabinetId;
    //
    long[] classesIds;
    //
    String[] classesNames;

    // текстовое поле имени
    EditText editName;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.cabinets_out_dialog_edit_cabinet, null);
        builder.setView(dialogLayout);


        // получаем входные данные
        // предыдущее имя
        startName = getArguments().getString(ARG_CABINET_NAME);
        if (startName == null) {
            startName = "";
            Log.i("TeachersApp", "you must give bundle argument \"" + ARG_CABINET_NAME + "\"");
        }
        // id кабинета
        cabinetId = getArguments().getLong(ARG_CABINET_ID);
        classesIds = getArguments().getLongArray(ARG_ARRAY_CLASSES_ID);
        if (classesIds == null) {
            classesIds = new long[0];
            Log.i("TeachersApp", "you must give bundle argument \"" + ARG_ARRAY_CLASSES_ID + "\"");
        }
        classesNames = getArguments().getStringArray(ARG_ARRAY_CLASSES_NAMES);
        if (classesNames == null) {
            classesNames = new String[0];
            Log.i("TeachersApp", "you must give bundle argument \"" + ARG_ARRAY_CLASSES_NAMES + "\"");
        }


        // вывод списка классов
        LinearLayout classesOut = dialogLayout.findViewById(R.id.dialog_fragment_layout_cabinet_out_edit_cabinet_classes_out);
        for (int classI = 0; classI < classesIds.length; classI++) {

            // создаем кнопку с классом
            TextView classText = new TextView(getActivity());
            classText.setBackgroundResource(R.drawable._dialog_full_background_white);
            classText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_medium));
            classText.setGravity(Gravity.CENTER);
            classText.setPadding(
                    getResources().getDimensionPixelOffset(R.dimen.half_more_margin),
                    getResources().getDimensionPixelOffset(R.dimen.half_more_margin),
                    getResources().getDimensionPixelOffset(R.dimen.half_more_margin),
                    getResources().getDimensionPixelOffset(R.dimen.half_more_margin)
            );
            classText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            classText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.text_subtitle_size));
            classText.setText(classesNames[classI]);
            LinearLayout.LayoutParams classTextParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            classTextParams.gravity = Gravity.CENTER;
            classTextParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.double_margin);

            // нажатие
            final int finalClassI = classI;
            classText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //вызываем в активности метод по далению кабинета и передаем id
                        ((EditCabinetDialogInterface) getActivity()).arrangeLearnersInCabinet(
                                cabinetId,
                                classesIds[finalClassI]
                        );
                    } catch (java.lang.ClassCastException e) {
                        //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
                        e.printStackTrace();
                        Log.i(
                                "TeachersApp",
                                "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
                        );
                    }
                }
            });
            classesOut.addView(classText, classTextParams);
        }

        // текстовое поле имени
        editName = dialogLayout.findViewById(R.id.dialog_fragment_layout_cabinet_out_edit_cabinet_name_field);
        editName.setText(startName);

        // кнопка отмена
        dialogLayout.findViewById(R.id.dialog_fragment_layout_cabinet_out_edit_cabinet_cancel_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });

        // кнопка расставить парты
        dialogLayout.findViewById(R.id.dialog_fragment_layout_cabinet_out_edit_cabinet_arrange_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            //вызываем в активности метод по далению кабинета и передаем id
                            ((EditCabinetDialogInterface) getActivity()).arrangeCabinetDesks(
                                    cabinetId
                            );
                        } catch (java.lang.ClassCastException e) {
                            //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
                            e.printStackTrace();
                            Log.i(
                                    "TeachersApp",
                                    "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
                            );
                        }
                    }
                });


        // кнопка удаление
        dialogLayout.findViewById(R.id.dialog_fragment_layout_cabinet_out_edit_cabinet_delete_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            isDelete = true;
                            //вызываем в активности метод по далению кабинета и передаем id
                            ((EditCabinetDialogInterface) getActivity()).removeCabinet(
                                    cabinetId
                            );
                        } catch (java.lang.ClassCastException e) {
                            //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
                            e.printStackTrace();
                            Log.i(
                                    "TeachersApp",
                                    "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
                            );
                        }
                        dismiss();
                    }
                });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // сохраняем изменения
        Log.e("TAG", "onDismiss: 1");
        if (!isDelete && !editName.getText().toString().equals(startName)) {
            try {
                Log.e("TAG", "onDismiss: 2");
                //вызываем в активности метод по созданию кабинета и передаем ей имя
                ((EditCabinetDialogInterface) getActivity()).editCabinetName(
                        cabinetId,
                        editName.getText().toString()
                );
            } catch (java.lang.ClassCastException e) {
                //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
                e.printStackTrace();
                Log.i(
                        "TeachersApp",
                        "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
                );
            }
        }
    }

}

interface EditCabinetDialogInterface {
    void editCabinetName(long cabinetId, String name);

    void arrangeCabinetDesks(long cabinetId);

    void arrangeLearnersInCabinet(long cabinetId, long classId);

    void removeCabinet(long cabinetId);
}