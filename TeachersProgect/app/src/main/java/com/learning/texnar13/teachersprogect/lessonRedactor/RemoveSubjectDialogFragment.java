package com.learning.texnar13.teachersprogect.lessonRedactor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;

// ------------- диалог удаления предметов -------------
public class RemoveSubjectDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //layout диалога
        LinearLayout out = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.activity_lesson_redactor_dialog_lesson_name, null);
        builder.setView(out);

        //--LinearLayout в layout файле--
        LinearLayout linearLayout = (LinearLayout) out.findViewById(R.id.create_lesson_dialog_fragment_linear_layout);
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
        title.setText(R.string.lesson_redactor_activity_dialog_title_remove_subjects);
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

//--список выбранных предметов--
        final ArrayList<Integer> mSelectedItems = new ArrayList<>();
        //--------ставим диалогу список в виде view--------
        //список названий
        String[] subjectNames = getArguments().getStringArray("stringOnlyLessons");

        //контейнеры для прокрутки
        ScrollView scrollView = new ScrollView(getActivity());
        linearLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1F));
        LinearLayout linear = new LinearLayout(getActivity());
        linear.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linear);


        for (int i = 0; i < subjectNames.length; i++) {
//--------пункт списка--------
            //контейнер
            LinearLayout item = new LinearLayout(getActivity());
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.LEFT);
            item.setGravity(Gravity.START);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (int) (pxFromDp(40) * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))
            );
            itemParams.setMargins(
                    (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                    (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                    (int) (pxFromDp(20 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier))),
                    (int) (pxFromDp(10 * getActivity().getResources().getInteger(R.integer.desks_screen_multiplier)))
            );
            linear.addView(item, itemParams);

            //чекбокс
            final CheckBox checkBox = new CheckBox(getActivity());
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            checkBox.setChecked(false);
            item.addView(checkBox);

            //текст в нем
            TextView text = new TextView(getActivity());
            text.setText(subjectNames[i]);
            text.setTextColor(Color.BLACK);
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            item.addView(text);

            //нажатие на пункт списка
            final int number = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                        if (mSelectedItems.contains(number)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedItems.remove(Integer.valueOf(number));
                        }
                    } else {
                        // If the user checked the item, add it to the selected items
                        checkBox.setChecked(true);
                        mSelectedItems.add(number);
                    }
                }
            });
        }

//--кнопки согласия/отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.lesson_redactor_activity_dialog_button_cancel);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка согласия
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        positiveButton.setText(R.string.lesson_redactor_activity_dialog_button_remove);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        positiveButtonParams.weight = 1;
        positiveButtonParams.setMargins((int) pxFromDp(5), (int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(10));


        //кнопки в контейнер
        container.addView(neutralButton, neutralButtonParams);
        container.addView(positiveButton, positiveButtonParams);

        //контейнер в диалог
        linearLayout.addView(container);


        //при нажатии...
        //согласие
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //список для удаления
                    long[] lessonsId = getArguments().getLongArray("lessonsId");
                    ArrayList<Long> deleteList = new ArrayList<>(mSelectedItems.size());
                    for (int itemPoz : mSelectedItems) {
                        deleteList.add(lessonsId[itemPoz]);
                    }
                    //вызываем в активности метод по удалению предмета
                    ((RemoveSubjectDialogFragmentInterface) getActivity()).removeSubjects(
                            0, deleteList
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс RemoveSubjectDialogFragmentInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "RemoveSubjectDialogFragment: you must implements RemoveSubjectDialogFragmentInterface in your activity"
                    );
                } catch (NullPointerException e) {
                    // вызвающая активность должна передать аргумент
                    e.printStackTrace();
                    Log.i("TeachersApp", "you must give bundle argument \"lessonsId\"");
                }
                dismiss();
            }
        });

        //отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //список для удаления(пустой)
                    ArrayList<Long> deleteList = new ArrayList<>();
                    //вызываем в активности метод по удалению предмета
                    ((RemoveSubjectDialogFragmentInterface) getActivity()).removeSubjects(
                            1, deleteList
                    );
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс RemoveSubjectDialogFragmentInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "RemoveSubjectDialogFragment: you must implements RemoveSubjectDialogFragmentInterface in your activity"
                    );
                }
                dismiss();
            }
        });
        return builder.create();
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface RemoveSubjectDialogFragmentInterface {
    void removeSubjects(int message, ArrayList<Long> deleteList);
}