package com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
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

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;
import java.util.Arrays;

public class PeriodDialogFragment extends DialogFragment {

    public static final String ARGS_PERIODS_STRING_ARRAY = "periodArray";

    // список предметов
    private ArrayList<String> periodsNames;

    // контейнеры содержимого диалога
    private LinearLayout titleLayout;
    private LinearLayout bodyLayout;
    private LinearLayout bottomLayout;

    // вынес для фона
    private ScrollView bodyScroll;

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
        bodyScroll = new ScrollView(getActivity());
        bodyScroll.setMinimumHeight((int) getResources().getDimension(R.dimen.forth_margin));
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

        // получаем список периодов
        String[] names = getArguments().getStringArray(ARGS_PERIODS_STRING_ARRAY);
        if (names == null)
            names = new String[0];

        periodsNames = new ArrayList<>(names.length);
        periodsNames.addAll(Arrays.asList(names));


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
        bodyScroll.setBackgroundResource(R.color.backgroundWhite);
        bottomLayout.setBackgroundResource(R.drawable._dialog_bottom_background_dark);

        // затираем то что было выведено до этого
        titleLayout.removeAllViews();
        bodyLayout.removeAllViews();
        bottomLayout.removeAllViews();


        // кнопка закрыть
        LinearLayout closeImageView = new LinearLayout(getActivity());
        closeImageView.setBackgroundResource(R.drawable.__button_close);
        LinearLayout.LayoutParams closeImageViewParams = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.my_icon_size),
                (int) getResources().getDimension(R.dimen.my_icon_size)
        );
        closeImageViewParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
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
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_light));
        title.setText(R.string.learners_and_grades_statistics_activity_dialog_title_choose_period);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        titleParams.gravity = Gravity.CENTER_VERTICAL;
        titleLayout.addView(title, titleParams);


        LinearLayout periodsContainer = new LinearLayout(getActivity());
        periodsContainer.setOrientation(LinearLayout.VERTICAL);
        // параметры текста
        LinearLayout.LayoutParams periodsContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        periodsContainerParams.topMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        periodsContainerParams.bottomMargin = (int) getResources().getDimension(R.dimen.simple_margin);
        bodyLayout.addView(
                periodsContainer,
                periodsContainerParams
        );

        // выводим предметы
        if (periodsNames != null)
            for (int periodI = 0; periodI < periodsNames.size(); periodI++) {
                // создаем текстовое поле с названием предмета
                final TextView periodText = new TextView(getActivity());
                periodText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_light));
                periodText.setText(periodsNames.get(periodI));
                periodText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
                periodText.setTextColor(Color.BLACK);

                // параметры текста
                LinearLayout.LayoutParams periodTextParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                periodTextParams.setMargins(
                        (int) getResources().getDimension(R.dimen.forth_margin),
                        (int) getResources().getDimension(R.dimen.simple_margin),
                        (int) getResources().getDimension(R.dimen.forth_margin),
                        (int) getResources().getDimension(R.dimen.simple_margin)
                );
                periodsContainer.addView(periodText, periodTextParams);

                // при нажатии на текстовое поле
                final int finalPosition = periodI;
                periodText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("TeachersApp", "onClick: " + finalPosition);

                        // выделяем цветом выбранный текст
                        periodText.setTextColor(getResources().getColor(R.color.baseGreen));


                        // вызываем в активности метод по выбору предмета
                        ((PeriodsDialogInterface) getActivity()).setPeriodPosition(finalPosition);

                        // закрываем диалог
                        dismiss();
                    }
                });
            }


        // ---- кнопки внизу диалога ----

        // кнопка изменить
        final TextView changeTextButton = new TextView(getActivity());
        changeTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_light));
        changeTextButton.setText(getResources().getString(R.string.learners_and_grades_out_activity_dialog_button_change));
        changeTextButton.setGravity(Gravity.CENTER);
        changeTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        changeTextButton.setTextColor(Color.BLACK);
        // параметры кнопки
        LinearLayout.LayoutParams changeTextButtonParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        changeTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        bottomLayout.addView(changeTextButton, changeTextButtonParams);
        // при нажатии на кнопку
        changeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выделяем цветом выбранный текст
                changeTextButton.setTextColor(getResources().getColor(R.color.baseGreen));
                // выводим меню редактирования
                outEditPeriodsMenu();
            }
        });

        // кнопка добавить
        final TextView addTextButton = new TextView(getActivity());
        addTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_light));
        addTextButton.setText(getResources().getString(R.string.learners_and_grades_out_activity_dialog_button_add));
        addTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        addTextButton.setGravity(Gravity.CENTER);
        addTextButton.setTextColor(Color.BLACK);
        // параметры кнопки
        LinearLayout.LayoutParams addTextButtonParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        addTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        bottomLayout.addView(addTextButton, addTextButtonParams);
        // при нажатии на кнопку
        addTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выделяем цветом выбранный текст
                addTextButton.setTextColor(getResources().getColor(R.color.baseGreen));
                // выводим меню создания предмета
                outCreatePeriodMenu();
            }
        });
    }

    // метод вывода интерфейса создания предмета
    void outCreatePeriodMenu() {
        // выставляем цвета диалога
        titleLayout.setBackgroundResource(R.drawable._dialog_head_background_dark);
        bodyScroll.setBackgroundResource(R.color.backgroundWhite);
        bottomLayout.setBackgroundResource(R.drawable._dialog_bottom_background_white);

        // затираем то что было выведено до этого
        titleLayout.removeAllViews();
        bodyLayout.removeAllViews();
        bottomLayout.removeAllViews();


        // чистим флаги и разрешаем показывать клавиатуру
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        // кнопка назад
        ImageView closeImageView = new ImageView(getActivity());
        closeImageView.setBackgroundResource(R.drawable.__button_back_arrow_blue);

        LinearLayout.LayoutParams closeImageViewParams = new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.my_icon_size),
                (int) getResources().getDimension(R.dimen.my_icon_size)
        );
        closeImageViewParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
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
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_light));
        title.setText(R.string.learners_and_grades_statistics_activity_dialog_title_add_statistic);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                0,
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        titleParams.gravity = Gravity.CENTER_VERTICAL;
        titleLayout.addView(title, titleParams);


        // текстовое поле для названия предмета
        final EditText periodNameField = new EditText(getActivity());
        periodNameField.setBackgroundResource(R.drawable._underlined_black);
        periodNameField.setHint(R.string.learners_and_grades_statistics_activity_dialog_hint_profile_name);
        periodNameField.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        periodNameField.setTextColor(Color.BLACK);
        // параметры текста
        LinearLayout.LayoutParams periodNameFieldParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        periodNameFieldParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin));
        bodyLayout.addView(periodNameField, periodNameFieldParams);


        // контейнер кнопки создать
        LinearLayout createButtonContainer = new LinearLayout(getActivity());
        createButtonContainer.setBackgroundResource(R.drawable._button_round_background_green);
        // параметры контейнера
        LinearLayout.LayoutParams saveTextButtonContainerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        saveTextButtonContainerParams.gravity = Gravity.CENTER;
        bottomLayout.addView(createButtonContainer, saveTextButtonContainerParams);

        // кнопка создать
        final TextView createTextButton = new TextView(getActivity());
        createTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_light));
        createTextButton.setText(getResources().getString(R.string.learners_and_grades_out_activity_dialog_button_create));
        createTextButton.setGravity(Gravity.CENTER);
        createTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        createTextButton.setTextColor(getResources().getColor(R.color.backgroundWhite));
        // параметры кнопки
        LinearLayout.LayoutParams createTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        createTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        createTextButtonParams.gravity = Gravity.CENTER;
        createButtonContainer.addView(createTextButton, createTextButtonParams);
        // при нажатии на кнопку
        createButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // выделяем цветом выбранный текст
                createTextButton.setTextColor(getResources().getColor(R.color.baseGreen));
                // todo скрываем клавиатуру


                // помещаем в конец списка
                periodsNames.add(periodNameField.getText().toString());


                // передаем название нового предмета активности
                try {
                    // вызываем в активности метод по созданию предмета
                    ((PeriodsDialogInterface) getActivity()).createPeriod(periodNameField.getText().toString());
                } catch (java.lang.ClassCastException e) {
                    e.printStackTrace();
                    Log.i("TeachersApp",
                            "PeriodsDialogFragment: you must implements PeriodsDialogInterface in your activity"
                    );
                }

                // закрываем диалог
                dismiss();
            }
        });

    }

    // метод вывода списка удаления и редактирования
    void outEditPeriodsMenu() {
        // выставляем цвета диалога
        titleLayout.setBackgroundResource(R.drawable._dialog_head_background_dark);
        bodyScroll.setBackgroundResource(R.color.backgroundWhite);
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
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // кнопка назад
        ImageView closeImageView = new ImageView(getActivity());
        closeImageView.setBackgroundResource(R.drawable.__button_back_arrow_blue);

        RelativeLayout.LayoutParams closeImageViewParams = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.my_icon_size), (int) getResources().getDimension(R.dimen.my_icon_size));
        closeImageViewParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
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
        title.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_light));
        title.setText(getResources().getString(R.string.learners_and_grades_out_activity_dialog_button_delete));
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setTextColor(Color.RED);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));

        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        titleParams.addRule(RelativeLayout.CENTER_VERTICAL);
        titleParams.setMargins(
                0,
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
        relativeHeadContainer.addView(title, titleParams);


        // массив EditText из которых будем брать измененные предметы
        final EditText[] editPeriodsNames = new EditText[periodsNames.size()];
        // массив отмеченных на удаление
        final boolean[] deleteList = new boolean[periodsNames.size()];

        // выводим предметы
        for (int periodI = 0; periodI < periodsNames.size(); periodI++) {

            // контейнер текстового поля
            LinearLayout periodContainer = new LinearLayout(getActivity());
            // параметры контейнера
            LinearLayout.LayoutParams periodContainerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            periodContainerParams.setMargins(
                    (int) getResources().getDimension(R.dimen.forth_margin),
                    (int) getResources().getDimension(R.dimen.half_margin),
                    (int) getResources().getDimension(R.dimen.forth_margin),
                    (int) getResources().getDimension(R.dimen.half_margin)
            );
            periodContainerParams.gravity = Gravity.CENTER_VERTICAL;
            bodyLayout.addView(periodContainer, periodContainerParams);


            // кнопка чтобы отмечать предметы на удаление
            final ImageView deleteImage = new ImageView(getActivity());
            deleteImage.setBackgroundResource(R.drawable.__checkbox_empty);
            // параметры кнопки
            LinearLayout.LayoutParams deleteImageParams = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.my_icon_small_size), (int) getResources().getDimension(R.dimen.my_icon_small_size)
            );
            deleteImageParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.half_margin)
            );
            deleteImageParams.gravity = Gravity.CENTER;
            periodContainer.addView(deleteImage, deleteImageParams);
            // инициализируем начальные начения
            deleteList[periodI] = false;
            // при нажатии на кнопку
            final int finalPeriodI = periodI;
            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // инвертируем состояние кнопки
                    if (deleteList[finalPeriodI]) {
                        deleteImage.setBackgroundResource(R.drawable.__checkbox_empty);
                    } else {
                        deleteImage.setBackgroundResource(R.drawable.__checkbox_full);
                    }
                    // и переменной
                    deleteList[finalPeriodI] = !deleteList[finalPeriodI];
                }
            });


            // создаем текстовое поле с названием предмета
            editPeriodsNames[periodI] = new EditText(getActivity());
            editPeriodsNames[periodI].setBackgroundResource(R.drawable._underlined_black);
            editPeriodsNames[periodI].setText(periodsNames.get(periodI));
            editPeriodsNames[periodI].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            editPeriodsNames[periodI].setTextColor(Color.BLACK);
            editPeriodsNames[periodI].setHint(R.string.learners_and_grades_statistics_activity_dialog_hint_profile_name);
            // параметры текста
            LinearLayout.LayoutParams periodTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size)
            );
            periodTextParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    0,
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    0);
            periodContainer.addView(editPeriodsNames[periodI], periodTextParams);
        }


        // нажатие на кнопку удалить
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title.setTextColor(Color.BLACK);
                // todo скрываем клавиатуру

                // чистим внутренний список
                for (int periodsI = deleteList.length - 1; periodsI >= 0; periodsI--) {
                    if (deleteList[periodsI]) {
                        periodsNames.remove(periodsI);
                    }
                }

                // отправляем список на удаление в активность
                try {
                    // вызываем в активности метод по удалению предметов
                    ((PeriodsDialogInterface) getActivity()).deletePeriods(deleteList);
                } catch (java.lang.ClassCastException e) {
                    e.printStackTrace();
                    Log.i("TeachersApp",
                            "PeriodsDialogFragment: you must implements PeriodsDialogInterface in your activity"
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
        saveTextButtonContainerParams.setMargins(
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin)
        );
        saveTextButtonContainerParams.gravity = Gravity.CENTER;
        bottomLayout.addView(saveButtonContainer, saveTextButtonContainerParams);

        // кнопка сохранить
        final TextView saveTextButton = new TextView(getActivity());
        saveTextButton.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_light));
        saveTextButton.setText(getResources().getString(R.string.learners_and_grades_out_activity_dialog_button_save));
        saveTextButton.setGravity(Gravity.CENTER);
        saveTextButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
        saveTextButton.setTextColor(getResources().getColor(R.color.backgroundWhite));
        // параметры кнопки
        LinearLayout.LayoutParams saveTextButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        saveTextButtonParams.setMargins(
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin),
                (int) getResources().getDimension(R.dimen.double_margin),
                (int) getResources().getDimension(R.dimen.simple_margin)
        );
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
                String[] arrForActivity = new String[periodsNames.size()];
                for (int periodsI = 0; periodsI < periodsNames.size(); periodsI++) {
                    periodsNames.set(periodsI, editPeriodsNames[periodsI].getText().toString());

                    arrForActivity[periodsI] = periodsNames.get(periodsI);
                }
                // вызываем в активности метод по переименованию предметов
                ((PeriodsDialogInterface) getActivity()).renamePeriods(arrForActivity);

                // сортируем внутренний массив
                String[] tempArray = new String[periodsNames.size()];
                periodsNames.toArray(tempArray);
                Arrays.sort(tempArray);
                periodsNames.clear();
                periodsNames.addAll(Arrays.asList(tempArray));


                // выводим главное меню
                outMainMenu();
            }
        });
    }
}


interface PeriodsDialogInterface {

    // установить предмет стоящий на этой позиции как выбранный
    void setPeriodPosition(int position);

    // создать предмет
    void createPeriod(String name);

    // удалить предмет
    void deletePeriods(boolean[] deleteList);

    // переименовать предметы
    void renamePeriods(String[] newPeriodsNames);
}