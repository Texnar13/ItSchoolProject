package com.learning.texnar13.teachersprogect.seatingRedactor;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;

public class ChooseLearnerDialogFragment extends DialogFragment {

    public static final String ARGS_LEARNERS_NAMES_ARRAY = "learnersNames";
    public static final String ARGS_LEARNERS_INDEXES_ARRAY = "learnersIndexes";


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        // начинаем строить диалог
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        View baseContainer = getActivity().getLayoutInflater().inflate(R.layout.lesson_redactor_dialog_choose_learner, null);
        builder.setView(baseContainer);

        // при нажатии на кнопку закрыть
        baseContainer.findViewById(R.id.lesson_redactor_dialog_choose_learner_button_close).setOnClickListener(v -> dismiss());

        // контейнер вывода
        LinearLayout out = baseContainer.findViewById(R.id.lesson_redactor_dialog_choose_learner_out);

        // получаем  из intent имена учеников
        ArrayList<String> learnersNames = getArguments().getStringArrayList(ARGS_LEARNERS_NAMES_ARRAY);
        // и их индексы
        final ArrayList<Integer> learnersIndexes = getArguments().getIntegerArrayList(ARGS_LEARNERS_INDEXES_ARRAY);

        // и выводим их
        if (learnersNames != null)
            for (int learnerI = 0; learnerI < learnersNames.size(); learnerI++) {

                // создаем кнопку
                LinearLayout textContainer = new LinearLayout(getActivity());
                textContainer.setOrientation(LinearLayout.VERTICAL);
                out.addView(textContainer, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                TextView learnerText = new TextView(getActivity());
                learnerText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_semibold));
                learnerText.setText(learnersNames.get(learnerI));
                learnerText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                learnerText.setTextColor(Color.BLACK);
                learnerText.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                learnerTextParams.setMargins(0, 0, 0,
                        getResources().getDimensionPixelOffset(R.dimen.double_margin));
                textContainer.addView(learnerText, learnerTextParams);

                // при нажатии
                final int finalLearnerI = learnerI;
                textContainer.setOnClickListener(v -> {
                    // передаем в активность номер выбранного ученика
                    ((ChooseLearnerDialogInterface) getActivity()).chooseLearner(learnersIndexes.get(finalLearnerI));
                    dismiss();
                });

            }


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }


}


interface ChooseLearnerDialogInterface {
    // установить предмет стоящий на этой позиции как выбранный
    void chooseLearner(int learnerPosition);
}