package com.learning.texnar13.teachersprogect.learnersAndGradesOut;

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
    LinearLayout titleLayout;
    LinearLayout bodyLayout;
    LinearLayout bottomLayout;

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

        // ---- контейнер для шапки ----
        titleLayout = new LinearLayout(getActivity());
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
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    // метод вывода главного меню
    void outMainMenu() {

        // выставляем цвета диалога
        titleLayout.setBackgroundResource(R.drawable._dialog_head_background_blue);
        bodyLayout.setBackgroundResource(R.color.backgroundWhite);
        bottomLayout.setBackgroundResource(R.drawable._dialog_bottom_background_dark);

        // затираем то что было выведено до этого
        titleLayout.removeAllViews();
        bodyLayout.removeAllViews();
        bottomLayout.removeAllViews();


        // кнопка закрыть
        LinearLayout closeImageView = new LinearLayout(getActivity());
        closeImageView.setBackgroundResource(R.drawable._button_close);
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
        title.setText(R.string.learners_and_grades_out_activity_dialog_title_choose_subject);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, pxFromDp(10), pxFromDp(10), pxFromDp(10));
        Log.e("TeachersApp", "outMainMenu: " + closeImageView.getId());
        titleParams.gravity = Gravity.CENTER_VERTICAL;
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
                        subjectText.setTextColor(getResources().getColor(R.color.baseGreen));

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
                changeTextButton.setTextColor(getResources().getColor(R.color.baseGreen));
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
                addTextButton.setTextColor(getResources().getColor(R.color.baseGreen));
                // выводим меню создания предмета
                outCreateSubjectMenu();
            }
        });
    }

    // метод вывода интерфейса создания предмета
    void outCreateSubjectMenu() {
        // выставляем цвета диалога
        titleLayout.setBackgroundResource(R.drawable._dialog_head_background_dark);
        bodyLayout.setBackgroundResource(R.color.backgroundWhite);
        bottomLayout.setBackgroundResource(R.drawable._dialog_bottom_background_white);

        // затираем то что было выведено до этого
        titleLayout.removeAllViews();
        bodyLayout.removeAllViews();
        bottomLayout.removeAllViews();


        // чистим флаги и разрешаем показывать клавиатуру
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        // кнопка назад
        ImageView closeImageView = new ImageView(getActivity());
        closeImageView.setBackgroundResource(R.drawable.calendar_left_arrow);

        LinearLayout.LayoutParams closeImageViewParams = new LinearLayout.LayoutParams(pxFromDp(40), pxFromDp(40));
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

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, pxFromDp(10), pxFromDp(10), pxFromDp(10));
        titleParams.gravity = Gravity.CENTER_VERTICAL;
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
        createButtonContainer.setBackgroundResource(R.drawable._button_round_background_green);
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
                createTextButton.setTextColor(getResources().getColor(R.color.baseGreen));
                // todo скрываем клавиатуру

                // сортируя при этом
                int position = -1;
                for (int subjectI = 0; subjectI < subjectsNames.size(); subjectI++) {
                    // если полученная строка лексикографически меньше текущей в списке то ставим полученную перед текущей
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
        titleLayout.setBackgroundResource(R.drawable._dialog_head_background_dark);
        bodyLayout.setBackgroundResource(R.color.backgroundWhite);
        bottomLayout.setBackgroundResource(R.drawable._dialog_bottom_background_white);

        // затираем то что было выведено до этого
        titleLayout.removeAllViews();
        bodyLayout.removeAllViews();
        bottomLayout.removeAllViews();

        // чистим флаги и разрешаем показывать клавиатуру
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        // relative контейнер для заголовка
        RelativeLayout relativeHeadContainer = new RelativeLayout(getActivity());
        titleLayout.addView(relativeHeadContainer,
                LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        // кнопка назад
        ImageView closeImageView = new ImageView(getActivity());
        closeImageView.setBackgroundResource(R.drawable.calendar_left_arrow);

        RelativeLayout.LayoutParams closeImageViewParams = new RelativeLayout.LayoutParams(pxFromDp(40), pxFromDp(40));
        closeImageViewParams.setMargins(pxFromDp(10), pxFromDp(10), pxFromDp(10), pxFromDp(10));
        relativeHeadContainer.addView(closeImageView, closeImageViewParams);
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
        relativeHeadContainer.addView(title, titleParams);


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
            deleteImage.setBackgroundResource(R.drawable._checkbox_empty);
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
                        deleteImage.setBackgroundResource(R.drawable._checkbox_empty);
                    } else {
                        deleteImage.setBackgroundResource(R.drawable._checkbox_full);
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
            editSubjectsNames[subjectI].setBackgroundResource(R.drawable._underlined_blue);
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
        saveButtonContainer.setBackgroundResource(R.drawable._button_round_background_green);
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
                saveTextButton.setTextColor(getResources().getColor(R.color.baseGreen));
                // todo скрываем клавиатуру

                // сохраняем имена из текстовых полей в массив
                // и параллельно создаем массив для предачи в активность
                String[] arrForActivity = new String[subjectsNames.size()];
                for (int subjectsI = 0; subjectsI < subjectsNames.size(); subjectsI++) {
                    subjectsNames.set(subjectsI, editSubjectsNames[subjectsI].getText().toString());

                    arrForActivity[subjectsI] = subjectsNames.get(subjectsI);
                }
                // вызываем в активности метод по переименованию предметов
                ((SubjectsDialogInterface) getActivity()).renameSubjects(arrForActivity);

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

/*
todo пригодится
* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        }
*
*
* */