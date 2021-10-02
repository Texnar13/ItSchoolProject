package com.learning.texnar13.teachersprogect.learnersClassesOut;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class CreateLearnersClassDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.learners_classes_out_dialog_create_class, null);
        builder.setView(dialogLayout);

        // отмена
        dialogLayout.findViewById(R.id.learners_classes_out_dialog_create_class_button_cancel).setOnClickListener(v -> dismiss());

        // сохранить
        dialogLayout.findViewById(R.id.learners_classes_out_dialog_create_class_button_save).setOnClickListener(view -> {
            EditText editName = dialogLayout.findViewById(R.id.learners_classes_out_dialog_create_class_name_input);
            if (editName.getText().toString().trim().length() == 0) {
                Toast.makeText(getActivity(), R.string.learners_classes_out_activity_toast_empty_name, Toast.LENGTH_SHORT).show();
            } else {
                //вызываем в активности метод по созданию класса и передаем ей имя
                ((CreateLearnersClassDialogInterface) getActivity()).createLearnersClass(
                        editName.getText().toString().trim()
                );
                dismiss();
            }
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}

interface CreateLearnersClassDialogInterface {
    void createLearnersClass(String name);
}
