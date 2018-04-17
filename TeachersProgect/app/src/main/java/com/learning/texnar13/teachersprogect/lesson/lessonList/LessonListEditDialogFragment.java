package com.learning.texnar13.teachersprogect.lesson.lessonList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.LoginFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;

public class LessonListEditDialogFragment extends DialogFragment {//входные данные позиция в столбце, позиция в строке

    //передаваемые данные
    public static final String LEARNER_NUMBER = "learnerNumber";
    public static final String GRADE_NUMBER = "gradeNumber";
    public static final String GRADE = "grade";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i("TeachersApp", "LessonListEditDialogFragment - onCreateDialog");
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

//---layout диалога---
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_learner, null);
        builder.setView(dialogLayout);

//--LinearLayout в layout файле--
        LinearLayout linearLayout = (LinearLayout) dialogLayout.findViewById(R.id.edit_learner_dialog_fragment_linear_layout);
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
        title.setText(R.string.lesson_list_activity_dialog_title);
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

//--spinner с выбором оценки--
        //массив с текстами оценок
        DataBaseOpenHelper db = new DataBaseOpenHelper(getActivity());
        String gradesText[] = new String[db.getSettingsMaxGrade(1)+2];
        db.close();
        gradesText[0] = getString(R.string.learners_and_grades_out_activity_title_grade_n);
        gradesText[1] = getString(R.string.learners_and_grades_out_activity_title_grade_no_answers);
        for (int i = 2; i < gradesText.length; i++) {
            gradesText[i] = ""+(i-1);
        }
        //адаптер
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getActivity().getApplicationContext(),
                R.layout.spiner_dropdown_element_learners_and_grades_subjects,
                gradesText
        );
        //спинер
        final Spinner spinner = new Spinner(getActivity().getApplicationContext());
        //ставим адаптер
        spinner.setAdapter(arrayAdapter);
        //выводим спиннер
        linearLayout.addView(
                spinner,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        try {//входные данные выбранная оценка
            switch (getArguments().getInt(GRADE)) {
                case -2:
                    spinner.setSelection(0, false);
                    break;
                case 0:
                    spinner.setSelection(1, false);
                    break;
                case 1:
                    spinner.setSelection(2, false);
                    break;
                case 2:
                    spinner.setSelection(3, false);
                    break;
                case 3:
                    spinner.setSelection(4, false);
                    break;
                case 4:
                    spinner.setSelection(5, false);
                    break;
                case 5:
                    spinner.setSelection(6, false);
                    break;
                default:
                    Log.i("TeachersApp", "yourGradeIsDefault");
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i("TeachersApp", "you must give bundle argument \"" + GRADE + "\"");
        }


//--кнопки согласия/отмены--
        //контейнер для них
        LinearLayout container = new LinearLayout(getActivity());
        container.setOrientation(LinearLayout.HORIZONTAL);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.lesson_list_activity_dialog_button_cancel);
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
        positiveButton.setText(R.string.lesson_list_activity_dialog_button_save);
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
                    int tempI;
                    switch (spinner.getSelectedItemPosition()) {
                        case 0:
                            tempI = -2;
                            break;
                        case 1:
                            tempI = 0;
                            break;
//                        case 2:
//                            tempI = 1;
//                            break;
//                        case 3:
//                            tempI = 2;
//                            break;
//                        case 4:
//                            tempI = 3;
//                            break;
//                        case 5:
//                            tempI = 4;
//                            break;
//                        case 6:
//                            tempI = 5;
//                            break;
                        default:
                            tempI = spinner.getSelectedItemPosition()-1;
                            break;
                    }

                    //вызываем в активности метод по изменению оценки и передаем выбранную оценку
                    ((com.learning.texnar13.teachersprogect.lesson.lessonList.EditGradeDialogInterface) getActivity()).editGrade(
                            getArguments().getInt(LEARNER_NUMBER),
                            getArguments().getInt(GRADE_NUMBER),
                            tempI);
                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditGradeDialogInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "LessonListEditDialogFragment: you must implements EditGradeDialogInterface in your activity"
                    );
                } catch (java.lang.NullPointerException e) {
                    //в диалог необходимо передать оценку и позиции( Bungle putLong("grade",grade) )
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "LessonListEditDialogFragment: you must give grade or position( Bungle putLong(\""+GRADE+"\",grade) )"
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

////----кнопка сохранения изменений----
//        builder.setPositiveButton("сохранить", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//            }
//        });
//        //просто выход из диалога
//        builder.setNegativeButton("отмена", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
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

interface EditGradeDialogInterface {
    void editGrade(int learnerNumber, int gradeNumber, int grade);
}