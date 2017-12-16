package com.learning.texnar13.teachersprogect.LearnersAndGrades;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.learning.texnar13.teachersprogect.R;

public class CreateLearnerDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //заголовок
        builder.setTitle("Добавление ученика");

        //layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.create_learner_dialog_fragment_layout, null);
        builder.setView(dialogLayout);
        //LinearLayout в layout файле
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.create_learner_dialog_fragment_linear_layout);

        //текстовое поле фамилии
        final EditText editLastName = new EditText(getActivity());
        editLastName.setTextColor(Color.BLACK);
        editLastName.setHint("ФАМИЛИЯ");
        editLastName.setHintTextColor(Color.GRAY);
        //добавляем текстовое поле
        linearLayout.addView(editLastName);
        //текстовое поле имени
        final EditText editName = new EditText(getActivity());
        editName.setTextColor(Color.BLACK);
        editName.setHint("ИМЯ");
        editName.setHintTextColor(Color.GRAY);
        //добавляем текстовое поле
        linearLayout.addView(editName);

        //кнопки согласия/отмены
        builder.setPositiveButton("добавление", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    //вызываем в активности метод по созданию ученика и передаем ей имя и фамилию
                    ((CreateLearnerInterface) getActivity()).createLearner(
                            editLastName.getText().toString(),
                            editName.getText().toString(),
                            getArguments().getLong("classId")
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс CreateLearnerInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnerDialogFragment: you must implements CreateLearnerInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e){
                    //в диалог необходимо передать id класса( Bungle putLong("classId",classId) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "CreateLearnerDialogFragment: you must give classId( Bungle putLong(\"classId\",classId) )"
                    );
                }
            }
        });
        builder.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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

interface CreateLearnerInterface {
    void createLearner(String lastName, String name, long classId);
}
