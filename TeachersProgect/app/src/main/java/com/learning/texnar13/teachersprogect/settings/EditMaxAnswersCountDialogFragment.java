package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.learning.texnar13.teachersprogect.R;

public class EditMaxAnswersCountDialogFragment extends DialogFragment {

    public static final String ARGUMENT_LAST_MAX = "lastMax";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // раздуваем Layout
        View linearLayout = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_edit_maximum_grade, null);
        // -------------------------------- получаем текстовое поле --------------------------------
        final EditText editText = (EditText) linearLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_maximum_grade_edit_text);
        // ставим прошлое значение
        editText.setText("" + getArguments().getInt(ARGUMENT_LAST_MAX));
        // -------------------------------- кнопки сохранения/отмены --------------------------------
        // сохранение
        View buttonSave = linearLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_maximum_grade_button_save);
        buttonSave.setOnClickListener(v -> {
            // проверяем поля
            String enteredText = editText.getText().toString().trim();
            if (enteredText.equals("")) {
                editText.setText("1");
                Toast.makeText(getActivity(), getString(R.string.settings_activity_toast_grade_no_entered), Toast.LENGTH_SHORT).show();
            } else {
                if (enteredText.length() <= 6) {
                    if (Integer.valueOf(enteredText) > 100) {
                        editText.setText("100");
                        Toast.makeText(getActivity(), getString(R.string.settings_activity_toast_grade_too_match), Toast.LENGTH_SHORT).show();
                        //вызываем в активности метод по изменению максимума
                        ((EditMaxAnswersDialogInterface) getActivity()).editMaxAnswer(
                                Integer.parseInt(editText.getText().toString())
                        );
                    } else {
                        if (Integer.valueOf(enteredText) < 1) {
                            editText.setText("1");
                            Toast.makeText(getActivity(), getString(R.string.settings_activity_toast_grade_too_min), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.settings_activity_toast_grade_saved) + " " + enteredText, Toast.LENGTH_SHORT).show();

                        }
                        //вызываем в активности метод по изменению максимума
                        ((EditMaxAnswersDialogInterface) getActivity())
                                .editMaxAnswer(Integer.parseInt(enteredText));
                    }
                } else {
                    editText.setText("100");
                    Toast.makeText(getActivity(), getString(R.string.settings_activity_toast_grade_too_match), Toast.LENGTH_SHORT).show();
                    //вызываем в активности метод по изменению максимума
                    ((EditMaxAnswersDialogInterface) getActivity()).editMaxAnswer(
                            Integer.parseInt(editText.getText().toString())
                    );
                }
            }

            dismiss();
        });
        // отмена
        ImageView buttonCancel = linearLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_maximum_grade_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        // ------------------------------ получаем строитель диалогов ------------------------------
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        // отдаем Layout
        dialogBuilder.setView(linearLayout);

        // наконец создаем диалог и возвращаем его
        Dialog dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }
}

interface EditMaxAnswersDialogInterface {
    void editMaxAnswer(int max);
}