package com.learning.texnar13.teachersprogect.seatingRedactor;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.ScheduleDayActivity;

import java.util.ArrayList;

public class ChooseLearnerDialogFragment extends DialogFragment {

    public static final String ARGS_LEARNERS_NAMES_ARRAY = "learnersNames";
    public static final String ARGS_LEARNERS_INDEXES_ARRAY = "learnersIndexes";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // ---- layout диалога ----
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundColor(Color.TRANSPARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );
        builder.setView(linearLayout);

        // выводим заголовок
        LinearLayout titleLayout = new LinearLayout(getActivity());
        titleLayout.setBackgroundResource(R.drawable._dialog_head_background_blue);
        linearLayout.addView(titleLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // кнопка закрыть
        ImageView closeImageView = new ImageView(getActivity());
        closeImageView.setImageResource(R.drawable.__button_close);
        LinearLayout.LayoutParams closeImageViewParams = new LinearLayout.LayoutParams(pxFromDp(40), pxFromDp(40));
        closeImageViewParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
        titleLayout.addView(closeImageView, closeImageViewParams);
        // при нажатии на кнопку закрыть
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // закрываем диалог
                dismiss();
            }
        });

        // текст заголовка
        TextView title = new TextView(getActivity());
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_medium));
        title.setText(R.string.lesson_redactor_activity_text_choose_learner);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, pxFromDp(10), pxFromDp(10), pxFromDp(10));
        titleParams.gravity = Gravity.CENTER_VERTICAL;
        titleLayout.addView(title, titleParams);


        // создаем скролл тела диалога
        ScrollView bodyLayoutScroll = new ScrollView(getActivity());
        bodyLayoutScroll.setBackgroundResource(R.drawable._dialog_bottom_background_dark);
        linearLayout.addView(bodyLayoutScroll,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // создаем контейнер тела диалога
        LinearLayout bodyLayoutContainer = new LinearLayout(getActivity());
        bodyLayoutContainer.setOrientation(LinearLayout.VERTICAL);
        ScrollView.LayoutParams bodyLayoutContainerParams = new ScrollView.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bodyLayoutContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                0,
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        bodyLayoutScroll.addView(bodyLayoutContainer, bodyLayoutContainerParams);

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
                bodyLayoutContainer.addView(textContainer, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                TextView learnerText = new TextView(getActivity());
                learnerText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria));
                learnerText.setText(learnersNames.get(learnerI));
                learnerText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                learnerText.setTextColor(Color.BLACK);
                learnerText.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                learnerTextParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
                textContainer.addView(learnerText, learnerTextParams);

                // при нажатии
                final int finalLearnerI = learnerI;
                textContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // передаем в активность номер выбранного ученика
                        ((ChooseLearnerDialogInterface) getActivity()).chooseLearner(learnersIndexes.get(finalLearnerI));
                        dismiss();
                    }
                });

            }


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }


    // преобразование зависимой величины в пиксели
    private int pxFromDp(float dp) {
        return (int) (dp * getActivity().getResources().getDisplayMetrics().density);
    }
}


interface ChooseLearnerDialogInterface {

    // установить предмет стоящий на этой позиции как выбранный
    void chooseLearner(int learnerPosition);

}