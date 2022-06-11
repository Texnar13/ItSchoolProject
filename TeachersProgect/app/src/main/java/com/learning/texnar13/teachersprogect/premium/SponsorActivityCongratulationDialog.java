package com.learning.texnar13.teachersprogect.premium;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class SponsorActivityCongratulationDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // начинаем строить диалог
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        View root = requireActivity().getLayoutInflater().inflate(R.layout.sponsor_activity_dialog_congratulations, null);
        builder.setView(root);

        // при нажатии на кнопку закрыть
        root.findViewById(R.id.sponsor_activity_dialog_congratulations_close_button).setOnClickListener(v -> dismiss());
        root.findViewById(R.id.sponsor_activity_dialog_congratulations_start_button).setOnClickListener(v -> dismiss());

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        return dialog;
    }
}