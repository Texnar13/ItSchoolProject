package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;
import java.util.Arrays;

public class SubjectsDialogFragment extends DialogFragment {

    public static final String ARGS_LEARNERS_NAMES_STRING_ARRAY = "learnersArray";
    public static final String ARGS_CHOSEN_SUBJECT_NUMBER = "chosenSubjectNumber";

    // список предметов
    ArrayList<String> subjectsNames;

    // контейнеры содержимого диалога
    RelativeLayout titleLayout;
    LinearLayout bodyLayout;
    LinearLayout bottomLayout;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // ---- layout диалога ----
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundResource(R.color.colorBackGround);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );
        builder.setView(linearLayout);

        // ---- контейнер для шапки ----
        titleLayout = new RelativeLayout(getActivity());
        linearLayout.addView(titleLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // ---- контейнер для тела диалога ----
        ScrollView bodyScroll = new ScrollView(getActivity());
        bodyScroll.setMinimumHeight(pxFromDp(20));
        linearLayout.addView(bodyScroll,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // контейнер в скролле
        bodyLayout = new LinearLayout(getActivity());
        bodyLayout.setOrientation(LinearLayout.VERTICAL);
        bodyScroll.addView(bodyLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // ---- контейнер для кнопок снизу ----
        bottomLayout = new LinearLayout(getActivity());
        bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
        bottomLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(bottomLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        // получаем список предметов
        String[] names = getArguments().getStringArray(ARGS_LEARNERS_NAMES_STRING_ARRAY);
        if (names == null)
            names = new String[0];

        subjectsNames = new ArrayList<>(names.length);
        subjectsNames.addAll(Arrays.asList(names));


        // в начале выводим главное меню
        outMainMenu();

        // наконец создаем диалог и возвращаем его
        return builder.create();
    }

    // метод вывода главного меню
    void outMainMenu() {

        // выставляем цвета диалога
        titleLayout.setBackgroundResource(R.color.colorPrimaryBlue);
        bodyLayout.setBackgroundResource(R.color.colorBackGround);
        bottomLayout.setBackgroundResource(R.color.colorBackGroundDark);

        // затираем то что было выведено до этого
        titleLayout.removeAllViews();
        bodyLayout.removeAllViews();
        bottomLayout.removeAllViews();


        // кнопка закрыть
        LinearLayout closeImageView = new LinearLayout(getActivity());
        closeImageView.setBackgroundResource(R.drawable.start_screen_3_3_green_spot);


        RelativeLayout.LayoutParams closeImageViewParams = new RelativeLayout.LayoutParams(pxFromDp(40), pxFromDp(40));
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
        title.setText(R.string.learners_and_grades_out_activity_dialog_title_choose_subject);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, pxFromDp(10), pxFromDp(10), pxFromDp(10));
        titleParams.addRule(RelativeLayout.RIGHT_OF, closeImageView.getId());
        titleParams.addRule(RelativeLayout.CENTER_VERTICAL);
        titleLayout.addView(title, titleParams);


        // выводим предметы
        if (subjectsNames != null)
            for (int subjectI = 0; subjectI < subjectsNames.size(); subjectI++) {
                // создаем текстовое поле с названием предмета
                final TextView subjectText = new TextView(getActivity());
                subjectText.setText(subjectsNames.get(subjectI));
                subjectText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                subjectText.setTextColor(Color.BLACK);

                // параметры текста
                LinearLayout.LayoutParams subjectTextParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                subjectTextParams.setMargins(pxFromDp(10), pxFromDp(5), pxFromDp(10), pxFromDp(5));
                bodyLayout.addView(subjectText, subjectTextParams);

                // при нажатии на текстовое поле
                final int finalPosition = subjectI;
                subjectText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("TeachersApp", "onClick: " + finalPosition);

                        // выделяем цветом выбранный текст
                        subjectText.setTextColor(getResources().getColor(R.color.colorPrimaryGreen));

                        try {
                            // вызываем в активности метод по выбору предмета
                            ((SubjectsDialogInterface) getActivity()).setSubjectPosition(finalPosition);
                        } catch (java.lang.ClassCastException e) {
                            e.printStackTrace();
                            Log.i("TeachersApp",
                                    "SubjectsDialogFragment: you must implements SubjectsDialogInterface in your activity"
                            );
                        }
                        // закрываем диалог
                        dismiss();
                    }
                });
            }


        // ---- кнопки внизу диалога ----

        // кнопка изменить
        final TextView changeTextButton = new TextView(getActivity());
        changeTextButton.setText("*Изменить*");
        changeTextButton.setGravity(Gravity.CENTER);
        changeTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        changeTextButton.setTextColor(Color.BLACK);
        // параметры кнопки
        LinearLayout.LayoutParams changeTextButtonParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        changeTextButtonParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(5), pxFromDp(10));
        bottomLayout.addView(changeTextButton, changeTextButtonParams);
        // при нажатии на кнопку
        changeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выделяем цветом выбранный текст
                changeTextButton.setTextColor(getResources().getColor(R.color.colorPrimaryGreen));
                // выводим меню редактирования
                outEditSubjectsMenu();
            }
        });

        // кнопка добавить
        final TextView addTextButton = new TextView(getActivity());
        addTextButton.setText("*Добавить*");
        addTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        addTextButton.setGravity(Gravity.CENTER);
        addTextButton.setTextColor(Color.BLACK);
        // параметры кнопки
        LinearLayout.LayoutParams addTextButtonParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        addTextButtonParams.setMargins(pxFromDp(5), pxFromDp(10), pxFromDp(10), pxFromDp(10));
        bottomLayout.addView(addTextButton, addTextButtonParams);
        // при нажатии на кнопку
        addTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выделяем цветом выбранный текст
                addTextButton.setTextColor(getResources().getColor(R.color.colorPrimaryGreen));
                // выводим меню создания предмета
                outCreateSubjectMenu();
            }
        });
    }

    // метод вывода интерфейса создания предмета
    void outCreateSubjectMenu() {
        // выставляем цвета диалога
        titleLayout.setBackgroundResource(R.color.colorBackGroundDark);
        bodyLayout.setBackgroundResource(R.color.colorBackGround);
        bottomLayout.setBackgroundResource(R.color.colorBackGround);

        // затираем то что было выведено до этого
        titleLayout.removeAllViews();
        bodyLayout.removeAllViews();
        bottomLayout.removeAllViews();


        // чистим флаги и разрешаем показывать клавиатуру
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        // кнопка назад
        ImageView closeImageView = new ImageView(getActivity());
        closeImageView.setBackgroundResource(R.drawable.calendar_left_arrow);

        RelativeLayout.LayoutParams closeImageViewParams = new RelativeLayout.LayoutParams(pxFromDp(40), pxFromDp(40));
        closeImageViewParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
        titleLayout.addView(closeImageView, closeImageViewParams);
        // при нажатии на кнопку закрыть
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo скрываем клавиатуру

                // возвращаем опять главное меню
                outMainMenu();
            }
        });

        // текст заголовка
        TextView title = new TextView(getActivity());
        title.setText("Введите название предмета:");
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, pxFromDp(10), pxFromDp(10), pxFromDp(10));
        titleParams.addRule(RelativeLayout.RIGHT_OF, closeImageView.getId());
        titleParams.addRule(RelativeLayout.CENTER_VERTICAL);
        titleLayout.addView(title, titleParams);


        // текстовое поле для названия предмета
        final EditText subjectNameField = new EditText(getActivity());
        subjectNameField.setHint("*Название предмета*");
        subjectNameField.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        subjectNameField.setTextColor(Color.BLACK);
        // параметры текста
        LinearLayout.LayoutParams subjectNameFieldParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subjectNameFieldParams.setMargins(pxFromDp(5), 0, pxFromDp(5), 0);
        bodyLayout.addView(subjectNameField, subjectNameFieldParams);


        // контейнер кнопки создать
        LinearLayout createButtonContainer = new LinearLayout(getActivity());
        createButtonContainer.setBackgroundResource(R.drawable.start_screen_3_3_green_spot);
        // параметры контейнера
        LinearLayout.LayoutParams saveTextButtonContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextButtonContainerParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
        saveTextButtonContainerParams.gravity = Gravity.CENTER;
        bottomLayout.addView(createButtonContainer, saveTextButtonContainerParams);

        // кнопка создать
        final TextView createTextButton = new TextView(getActivity());
        createTextButton.setText("*Создать*");
        createTextButton.setGravity(Gravity.CENTER);
        createTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        createTextButton.setTextColor(Color.BLACK);
        // параметры кнопки
        LinearLayout.LayoutParams createTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        createTextButtonParams.setMargins(pxFromDp(5), pxFromDp(5), pxFromDp(5), pxFromDp(5));
        createTextButtonParams.gravity = Gravity.CENTER;
        createButtonContainer.addView(createTextButton, createTextButtonParams);
        // при нажатии на кнопку
        createButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выделяем цветом выбранный текст
                createTextButton.setTextColor(getResources().getColor(R.color.colorPrimaryGreen));
                // todo скрываем клавиатуру

                // сортируя при этом
                int position = -1;
                for (int subjectI = 0; subjectI < subjectsNames.size(); subjectI++) {
                    // если полученная строка лексикографически меньше текущей в списке то ставим ее перед ней
                    if (subjectNameField.getText().toString().compareTo(subjectsNames.get(subjectI)) < 0) {
                        subjectsNames.add(subjectI, subjectNameField.getText().toString());
                        position = subjectI;
                        break;
                    }
                }
                // если строка больше всех других строк, помещаем в конец списка
                if (position == -1) {
                    position = subjectsNames.size();
                    subjectsNames.add(subjectNameField.getText().toString());
                }

                // передаем название нового предмета активности
                try {
                    // вызываем в активности метод по созданию предмета
                    ((SubjectsDialogInterface) getActivity()).createSubject(subjectNameField.getText().toString(), position);
                } catch (java.lang.ClassCastException e) {
                    e.printStackTrace();
                    Log.i("TeachersApp",
                            "SubjectsDialogFragment: you must implements SubjectsDialogInterface in your activity"
                    );
                }

                // закрываем диалог
                dismiss();
            }
        });

    }

    // метод вывода списка удаления и редактирования
    void outEditSubjectsMenu() {
        // выставляем цвета диалога
        titleLayout.setBackgroundResource(R.color.colorBackGroundDark);
        bodyLayout.setBackgroundResource(R.color.colorBackGround);
        bottomLayout.setBackgroundResource(R.color.colorBackGround);

        // затираем то что было выведено до этого
        titleLayout.removeAllViews();
        bodyLayout.removeAllViews();
        bottomLayout.removeAllViews();

        // чистим флаги и разрешаем показывать клавиатуру
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        // кнопка назад
        ImageView closeImageView = new ImageView(getActivity());
        closeImageView.setBackgroundResource(R.drawable.calendar_left_arrow);

        RelativeLayout.LayoutParams closeImageViewParams = new RelativeLayout.LayoutParams(pxFromDp(40), pxFromDp(40));
        closeImageViewParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
        titleLayout.addView(closeImageView, closeImageViewParams);
        // при нажатии на кнопку закрыть
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo скрываем клавиатуру

                // возвращаем опять главное меню
                outMainMenu();
            }
        });

        // кнопка удалить в заголовке
        final TextView title = new TextView(getActivity());
        title.setText("*Удалить*");
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.RED);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        titleParams.addRule(RelativeLayout.CENTER_VERTICAL);
        titleParams.setMargins(0, pxFromDp(10), pxFromDp(10), pxFromDp(10));


        // массив EditText из которых будем брать измененные предметы
        final EditText[] editSubjectsNames = new EditText[subjectsNames.size()];
        // массив отмеченных на удаление
        final boolean[] deleteList = new boolean[subjectsNames.size()];

        // выводим предметы
        for (int subjectI = 0; subjectI < subjectsNames.size(); subjectI++) {

            // контейнер текстового поля
            LinearLayout subjectContainer = new LinearLayout(getActivity());
            // параметры контейнера
            LinearLayout.LayoutParams subjectContainerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            subjectContainerParams.setMargins(pxFromDp(10), pxFromDp(5), pxFromDp(10), pxFromDp(5));
            subjectContainerParams.gravity = Gravity.CENTER_VERTICAL;
            bodyLayout.addView(subjectContainer, subjectContainerParams);


            // кнопка чтобы отмечать предметы на удаление
            final ImageView deleteImage = new ImageView(getActivity());
            deleteImage.setBackgroundResource(R.drawable.button_lite_blue);
            // параметры кнопки
            LinearLayout.LayoutParams deleteImageParams = new LinearLayout.LayoutParams(
                    pxFromDp(30), pxFromDp(30)
            );
            deleteImageParams.setMargins(pxFromDp(5), 0, pxFromDp(5), 0);
            deleteImageParams.gravity = Gravity.CENTER;
            subjectContainer.addView(deleteImage, deleteImageParams);
            // инициализируем начальные начения
            deleteList[subjectI] = false;
            // при нажатии на кнопку
            final int finalSubjectI = subjectI;
            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // инвертируем состояние кнопки
                    if (deleteList[finalSubjectI]) {
                        deleteImage.setBackgroundResource(R.drawable.button_lite_blue);
                    } else {
                        deleteImage.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
                    }
                    // и переменной
                    deleteList[finalSubjectI] = !deleteList[finalSubjectI];
                }
            });


            // создаем текстовое поле с названием предмета
            editSubjectsNames[subjectI] = new EditText(getActivity());
            editSubjectsNames[subjectI].setText(subjectsNames.get(subjectI));
            editSubjectsNames[subjectI].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            editSubjectsNames[subjectI].setTextColor(Color.BLACK);
            editSubjectsNames[subjectI].setBackgroundResource(R.drawable.underlined_shape);
            editSubjectsNames[subjectI].setHint("*Название предмета*");
            // параметры текста
            LinearLayout.LayoutParams subjectTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            subjectTextParams.setMargins(pxFromDp(5), 0, pxFromDp(5), 0);
            subjectContainer.addView(editSubjectsNames[subjectI], subjectTextParams);
        }


        // нажатие на кнопку удалить
        titleLayout.addView(title, titleParams);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title.setTextColor(Color.BLACK);
                // todo скрываем клавиатуру

                // чистим внутренний список
                for (int subjectsI = deleteList.length - 1; subjectsI >= 0; subjectsI--) {
                    if (deleteList[subjectsI]) {
                        subjectsNames.remove(subjectsI);
                    }
                }

                // отправляем список на удаление в активность
                try {
                    // вызываем в активности метод по удалению предметов
                    ((SubjectsDialogInterface) getActivity()).deleteSubjects(deleteList);
                } catch (java.lang.ClassCastException e) {
                    e.printStackTrace();
                    Log.i("TeachersApp",
                            "SubjectsDialogFragment: you must implements SubjectsDialogInterface in your activity"
                    );
                }

                // выводим главное меню
                outMainMenu();
            }
        });


        // контейнер кнопки сохранить
        LinearLayout saveButtonContainer = new LinearLayout(getActivity());
        saveButtonContainer.setBackgroundResource(R.drawable.start_screen_3_3_green_spot);
        // параметры контейнера
        LinearLayout.LayoutParams saveTextButtonContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextButtonContainerParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
        saveTextButtonContainerParams.gravity = Gravity.CENTER;
        bottomLayout.addView(saveButtonContainer, saveTextButtonContainerParams);

        // кнопка сохранить
        final TextView saveTextButton = new TextView(getActivity());
        saveTextButton.setText("*Сохранить*");
        saveTextButton.setGravity(Gravity.CENTER);
        saveTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        saveTextButton.setTextColor(Color.BLACK);
        // параметры кнопки
        LinearLayout.LayoutParams saveTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextButtonParams.setMargins(pxFromDp(5), pxFromDp(5), pxFromDp(5), pxFromDp(5));
        saveTextButtonParams.gravity = Gravity.CENTER;
        saveButtonContainer.addView(saveTextButton, saveTextButtonParams);
        // при нажатии на кнопку
        saveTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выделяем цветом выбранный текст
                saveTextButton.setTextColor(getResources().getColor(R.color.colorPrimaryGreen));
                // todo скрываем клавиатуру

                // сохраняем имена из текстовых полей в массив
                for (int subjectsI = 0; subjectsI < subjectsNames.size(); subjectsI++) {
                    subjectsNames.set(subjectsI, editSubjectsNames[subjectsI].getText().toString());
                }

                // передаем массив активности
                try {
                    // вызываем в активности метод по переименованию предметов
                    ((SubjectsDialogInterface) getActivity()).renameSubjects((String[]) subjectsNames.toArray());
                } catch (java.lang.ClassCastException e) {
                    e.printStackTrace();
                    Log.i("TeachersApp",
                            "SubjectsDialogFragment: you must implements SubjectsDialogInterface in your activity"
                    );
                }

                // сортируем внутренний массив
                String[] tempArray = new String[subjectsNames.size()];
                subjectsNames.toArray(tempArray);
                Arrays.sort(tempArray);
                subjectsNames.clear();
                subjectsNames.addAll(Arrays.asList(tempArray));


                // выводим главное меню
                outMainMenu();
            }
        });
    }


    // преобразование зависимой величины в пиксели
    private int pxFromDp(float dp) {
        return (int) (dp * getActivity().getResources().getDisplayMetrics().density);
    }
}


interface SubjectsDialogInterface {

    // установить предмет стоящий на этой позиции как выбранный
    void setSubjectPosition(int position);

    // создать предмет
    void createSubject(String name, int position);

    // удалить предмет
    void deleteSubjects(boolean[] deleteList);

    // переименовать предметы
    void renameSubjects(String[] newSubjectsNames);


}


//


//


//


// void createSubjectDialogMethod(int code, int position, String classNameText);
// void RemoveSubjectDialogMethod(int code, int position, ArrayList<Long> deleteList);



/*
*


package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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

public class SubjectRemoveDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.i("TeachersApp", "RemoveLearnerSubjectDialogFragment-onCreateDialog");

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        LinearLayout out = new LinearLayout(getActivity());
        builder.setView(out);

        //--LinearLayout в layout файле--
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundResource(R.color.colorBackGround);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams.setMargins((int) pxFromDp(10), (int) pxFromDp(15), (int) pxFromDp(10), (int) pxFromDp(15));
        linearLayout.setLayoutParams(linearLayoutParams);
        out.addView(linearLayout);--

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
            LinearLayout.LayoutParams itemParams =
                    new LinearLayout.LayoutParams(
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
        container.setGravity(Gravity.CENTER);

        //кнопка отмены
        Button neutralButton = new Button(getActivity());
        neutralButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        neutralButton.setText(R.string.lesson_redactor_activity_dialog_button_cancel);
        neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        neutralButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams neutralButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.my_buttons_height_size)
        );
        neutralButtonParams.weight = 1;
        neutralButtonParams.setMargins((int) pxFromDp(10), (int) pxFromDp(10), (int) pxFromDp(5), (int) pxFromDp(10));

        //кнопка согласия
        Button positiveButton = new Button(getActivity());
        positiveButton.setBackgroundResource(R.drawable.start_screen_3_1_blue_spot);
        positiveButton.setText(R.string.lesson_redactor_activity_dialog_button_remove);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        positiveButton.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams positiveButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.my_buttons_height_size)
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
                long[] lessonsId = getArguments().getLongArray("lessonsId");
                ArrayList<Long> deleteList = new ArrayList<>(mSelectedItems.size());
                for (int itemPoz : mSelectedItems) {
                    deleteList.add(lessonsId[itemPoz]);
                }
                //возвращаем все в активность
                ((SubjectRemoveLearnersDialogInterface) getActivity())
                        .RemoveSubjectDialogMethod(
                                0,
                                0,
                                deleteList
                        );

                dismiss();
            }
        });

        //отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //возвращаем все в активность
                ((SubjectRemoveLearnersDialogInterface) getActivity())
                        .RemoveSubjectDialogMethod(
                                1,
                                getArguments().getInt("position"),
                                new ArrayList<Long>()
                        );
                dismiss();
            }
        });
        return builder.create();

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.i("TeachersApp", "RemoveLearnerSubjectDialogFragment-onCancel");
        super.onCancel(dialog);
        ((SubjectRemoveLearnersDialogInterface) getActivity())
                .RemoveSubjectDialogMethod(
                        1,
                        getArguments().getInt("position"),
                        new ArrayList<Long>()
                );
    }

    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}



*
* */