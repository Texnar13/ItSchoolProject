package com.learning.texnar13.teachersprogect.learnersClassesOut;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

public class EditLearnersClassDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // начинаем строить диалог
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        // layout диалога
        View dialogLayout = requireActivity().getLayoutInflater().inflate(R.layout.learners_classes_out_dialog_edit_class, null);
        builder.setView(dialogLayout);
        // кнопка отмены
        View neutralButton = dialogLayout.findViewById(R.id.learners_classes_out_dialog_edit_class_button_cancel);
        neutralButton.setOnClickListener(view -> dismiss());


        // текстовое поле имени
        final EditText editName = dialogLayout.findViewById(R.id.learners_classes_out_dialog_edit_class_name_input);
        // входные данные предыдущее название
        editName.setText(requireArguments().getString("name"));


        //при нажатии...
        // согласие
        dialogLayout.findViewById(R.id.learners_classes_out_dialog_edit_class_button_save).setOnClickListener(view -> {

            if (editName.getText().toString().trim().length() == 0) {
                Toast.makeText(getActivity(), R.string.learners_classes_out_activity_toast_empty_name, Toast.LENGTH_SHORT).show();
            } else {
                //вызываем в активности метод по созданию класса и передаем ей имя
                ((EditLearnersClassDialogInterface) getActivity()).editLearnersClass(
                        editName.getText().toString().trim()
                );
                dismiss();
            }
        });


        final AppCompatActivity context = (AppCompatActivity) getActivity();

        //удаление
        dialogLayout.findViewById(R.id.learners_classes_out_dialog_edit_class_button_remove).setOnClickListener(view -> {
            //вызываем в активности метод по далению класса и передаем id
            ((EditLearnersClassDialogInterface) getActivity()).removeLearnersClass();
            dismiss();
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}

interface EditLearnersClassDialogInterface {
    void editLearnersClass(String name);

    void removeLearnersClass();
}
