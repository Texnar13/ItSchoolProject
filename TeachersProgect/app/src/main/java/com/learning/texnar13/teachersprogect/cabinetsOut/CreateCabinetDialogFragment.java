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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

public class CreateCabinetDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.cabinets_out_dialog_create_cabinet, null);
        builder.setView(dialogLayout);

        // отмена
        dialogLayout.findViewById(R.id.cabinets_out_dialog_create_cabinet_button_cancel).setOnClickListener(v -> dismiss());

        // сохранить
        dialogLayout.findViewById(R.id.cabinets_out_dialog_create_cabinet_button_save).setOnClickListener(view -> {
            // вызываем в активности метод по созданию кабинета и передаем ей имя
            ((CreateCabinetInterface) getActivity()).createCabinet(
                    ((EditText) dialogLayout.findViewById(R.id.cabinets_out_dialog_create_cabinet_name_input))
                            .getText().toString()
            );
            dismiss();
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}

interface CreateCabinetInterface {
    void createCabinet(String name);
}
