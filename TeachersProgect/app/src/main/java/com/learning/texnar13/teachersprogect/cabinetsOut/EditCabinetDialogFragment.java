package com.learning.texnar13.teachersprogect.cabinetsOut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

public class EditCabinetDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        //builder.setTitle("Редактирование кабинета");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_cabinet, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_cabinet_dialog_fragment_linear_layout);
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
        title.setText(R.string.cabinets_out_activity_dialog_title_edit_cabinet);
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


//--текстовое поле имени--
        final EditText editName = new EditText(getActivity());
        editName.setTextColor(Color.BLACK);
        editName.setHint(R.string.cabinets_out_activity_dialog_hint_cabinet_name);
        editName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        editName.setInputType(InputType.TYPE_CLASS_TEXT);
        editName.setHintTextColor(Color.GRAY);
        try {//входные данные предыдущее имя
            editName.setText(getArguments().getString("name"));
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"name\"");
        }

        LinearLayout editNameContainer = new LinearLayout(getActivity());
        LinearLayout.LayoutParams editNameContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editNameContainerParams.setMargins((int) pxFromDp(25), 0, (int) pxFromDp(25), 0);
        //добавляем текстовое поле
        editNameContainer.addView(
                editName,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                ));
        linearLayout.addView(editNameContainer, editNameContainerParams);

//--кнопки согласия/отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_2_yellow_spot);
        neutralButton.setText(R.string.cabinets_out_activity_dialog_button_cancel);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка удалить
        Button negativeButton = new Button(getActivity());
        negativeButton.setBackgroundResource(R.drawable.start_screen_3_4_pink_spot);
        negativeButton.setText(R.string.cabinets_out_activity_dialog_button_delete);
        negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        negativeButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams negativeButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        negativeButtonParams.weight = 1;
        negativeButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка сохранения
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_2_yellow_spot);
        positiveButton.setText(R.string.cabinets_out_activity_dialog_button_save);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        positiveButtonParams.weight = 1;
        positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));

        //кнопки в контейнер
        container.addView(neutralButton, neutralButtonParams);
        container.addView(negativeButton, negativeButtonParams);
        container.addView(positiveButton, positiveButtonParams);

        //контейнер в диалог
        linearLayout.addView(container);


//при нажатии...
        //согласие
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по созданию кабинета и передаем ей имя
                    ((EditCabinetDialogInterface) getActivity()).editCabinet(
                            editName.getText().toString(),
                            getArguments().getLong("cabinetId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id кабинета( Bungle putLong("cabinetId",cabinetId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must give cabinetId( Bungle putLong(\"cabinetId\",cabinetId) )"
                    );
                }
                dismiss();
            }
        });

        //отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //удаление
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //вызываем в активности метод по далению кабинета и передаем id
                    ((EditCabinetDialogInterface) getActivity()).removeCabinet(
                            getArguments().getLong("cabinetId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать id кабинета( Bungle putLong("cabinetId",cabinetId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditCabinetDialogFragment: you must give cabinetId( Bungle putLong(\"cabinetId\",cabinetId) )"
                    );
                }
                dismiss();
            }
        });

//
////кнопка сохранения изменений
//        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                try {
//                    //вызываем в активности метод по созданию кабинета и передаем ей имя
//                    ((EditCabinetDialogInterface) getActivity()).editCabinet(
//                            editName.getText().toString(),
//                            getArguments().getLong("cabinetId")
//                    );
//                } catch (java.lang.ClassCastException e) {
//                    //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
//                    );
//                } catch (java.lang.NullPointerException e) {
//                    //в диалог необходимо передать id кабинета( Bungle putLong("cabinetId",cabinetId) )
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditCabinetDialogFragment: you must give cabinetId( Bungle putLong(\"cabinetId\",cabinetId) )"
//                    );
//                }
//            }
//        });
////просто выход из диалога
//        builder.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//            }
//        });
////удаление кабинета
//        builder.setNeutralButton("удалить", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                try {
//                    //вызываем в активности метод по далению кабинета и передаем id
//                    ((EditCabinetDialogInterface) getActivity()).removeCabinet(
//                            getArguments().getLong("cabinetId")
//                    );
//                } catch (java.lang.ClassCastException e) {
//                    //в вызвающей активности должен быть имплементирован интерфейс EditCabinetInterface
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditCabinetDialogFragment: you must implements EditCabinetDialogInterface in your activity"
//                    );
//                } catch (java.lang.NullPointerException e) {
//                    //в диалог необходимо передать id кабинета( Bungle putLong("cabinetId",cabinetId) )
//                    e.printStackTrace();
//                    Log.i(
//                            "TeachersApp",
//                            "EditCabinetDialogFragment: you must give cabinetId( Bungle putLong(\"cabinetId\",cabinetId) )"
//                    );
//                }
//            }
//        });

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

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface EditCabinetDialogInterface {
    void editCabinet(String name, long cabinetId);
    void removeCabinet(long cabinetId);
}