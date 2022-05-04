package com.learning.texnar13.teachersprogect.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.SharedPrefsContract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class EditTimeDialogFragment extends DialogFragment {

    // статус подписки
    boolean isSubscribe;

    // массив с полями
    ArrayList<TimeViewLine> lines;

    // контейнер вывода времени
    LinearLayout outContainer;


    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ScrollView scrollLayout = (ScrollView) getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_edit_time, null);
        builder.setView(scrollLayout);


        // получаем пачку данных
        EditTimeDialogDataTransfer dataTransfer = Objects.requireNonNull(((EditTimeDialogDataTransfer)
                requireArguments().getSerializable(EditTimeDialogDataTransfer.PARAM_DATA)
        ));
        // распаковываем время
        int[][] rawDataArray = dataTransfer.lessonPeriods;


        // статус подписки
        isSubscribe = PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext())
                .getBoolean(SharedPrefsContract.PREFS_BOOLEAN_PREMIUM_STATE, false);


        // выводим данные в поля

        // контейнер полей
        outContainer = scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_time_out);
        // массив с полями
        lines = new ArrayList<>(rawDataArray.length);
        // выводим строки
        // создаем обьект строки
        for (int rowsI = 0; rowsI < rawDataArray.length; rowsI++) {
            lines.add(inflateNewLine(rawDataArray[rowsI], rowsI + 1));
        }
        // выводим крестик на последнем уроке
        setLastElementOnClickListener();


        // кнопка добавить урок
        scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_button_add)
                .setOnClickListener(v -> {
                    if (isSubscribe || lines.size() < SharedPrefsContract.PREMIUM_PARAM_MAX_LESSONS_COUNT) {
                        addNewLesson();
                    } else {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.settings_activity_toast_time_subscribe,
                                        SharedPrefsContract.PREMIUM_PARAM_MAX_LESSONS_COUNT),
                                Toast.LENGTH_SHORT).show();
                    }
                });


        // при нажатии на кнопку закрыть
        scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_button_close).setOnClickListener(v -> {
            // проверяем все поля
            int[][] newTime = checkFields();
            if (newTime != null) {
                // вызываем dismiss, а сохранение будет уже в нем
                dismiss();
            } else
                // если пользователь пытается нажать кнопку назад, с неправильными полями
                Toast.makeText(getActivity(), R.string.settings_activity_toast_time_no_saved, Toast.LENGTH_SHORT).show();
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        // проверяем все поля
        int[][] newTime = checkFields();
        if (newTime != null) {
            // вызываем в активности метод по сохранению и передаем время из полей
            ((EditTimeDialogFragmentInterface) requireActivity()).editTime(newTime);
        } else
            // если пользователь вышел из диалога, оставив неправильные поля
            Toast.makeText(getActivity(), R.string.settings_activity_toast_time_no_saved, Toast.LENGTH_SHORT).show();
    }

    // по кнопке добавить урок
    void addNewLesson() {
        // проверяем последнее поле
        TimeViewLine lastLine = lines.get(lines.size() - 1);
        int prevHour = Integer.parseInt(lastLine.timeFields[2].getText().toString());
        int prevMinute = Integer.parseInt(lastLine.timeFields[3].getText().toString());

        // если после него есть еще две минуты
        if (prevHour != 23 || prevMinute <= 57) {
            // убираем кнопку удалить у последнего элемента
            lastLine.deleteImage.setImageResource(R.color.transparent);
            lastLine.deleteImage.setOnClickListener(null);


            // добавляем новый элемент
            {
                // время начала нового урока по prev
                int startHour;
                int startMinute;
                if (prevMinute == 59) {
                    startHour = prevHour + 1;
                    startMinute = 0;
                } else {
                    startHour = prevHour;
                    startMinute = prevMinute + 1;
                }
                // время конца нового урока по start
                int endHour;
                int endMinute;
                if (startMinute == 59) {
                    endHour = startHour + 1;
                    endMinute = 0;
                } else {
                    endHour = startHour;
                    endMinute = startMinute + 1;
                }

                // создаем обьект строки
                lines.add(inflateNewLine(
                        new int[]{startHour, startMinute, endHour, endMinute}, lines.size() + 1));
            }

            // выводим крестик на последнем уроке
            setLastElementOnClickListener();

            // чистка полей от красного шрифта
            checkFields();

        } else {
            Toast.makeText(getActivity(), R.string.settings_activity_dialog_edit_time_toast_last_lesson_error, Toast.LENGTH_LONG).show();
            lastLine.timeFields[3].setTextColor(getResources().getColor(R.color.signalRed));
        }
    }

    // создание строки современем из разметки
    TimeViewLine inflateNewLine(int[] startTextValues, int lessonPos) {
        // создаем обьект строки
        TimeViewLine line = new TimeViewLine();

        // раздуваем одну строку
        View rootOfElement = requireActivity().getLayoutInflater().inflate(
                R.layout.settings_dialog_edit_time_template_single_time_line, null);
        LinearLayout.LayoutParams rootOfElementParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        outContainer.addView(rootOfElement, rootOfElementParams);
        line.container = rootOfElement;

        // находим в ней элементы
        line.timeFields[0] = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_start_hour);
        line.timeFields[1] = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_start_minute);
        line.timeFields[2] = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_end_hour);
        line.timeFields[3] = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_end_minute);
        line.deleteImage = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_delete_button);

        // если нет подписки и урок больше чем PREMIUM_PARAM_MAX_LESSONS_COUNT, перекрашиваем его в серый
        if(!isSubscribe && lessonPos > SharedPrefsContract.PREMIUM_PARAM_MAX_LESSONS_COUNT){
            int textColor = getResources().getColor(R.color.text_color_not_active);
            line.timeFields[0].setTextColor(textColor);
            line.timeFields[1].setTextColor(textColor);
            line.timeFields[2].setTextColor(textColor);
            line.timeFields[3].setTextColor(textColor);
            ((TextView)rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_divider_start_dots)).setTextColor(textColor);
            ((TextView)rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_divider_separator)).setTextColor(textColor);
            ((TextView)rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_divider_end_dots)).setTextColor(textColor);
        }

        // проставляем значение в номер урока
        ((TextView) rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_lesson_number))
                .setText(lessonPos + ".");

        // проставляем значения в 4 текстовых поля
        for (int textsI = 0; textsI < 4; textsI++) {
            line.timeFields[textsI].setText(getTwoSymbols(startTextValues[textsI]));
            line.timeFields[textsI].setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
            line.timeFields[textsI].setOnFocusChangeListener((view, bool) -> checkFields());
        }
        return line;
    }


    // выводим крестик на последнем уроке
    void setLastElementOnClickListener() {
        if (lines.size() > 1) {
            ImageView deleteImage = lines.get(lines.size() - 1).deleteImage;
            deleteImage.setImageResource(R.drawable.base_button_close);
            deleteImage.setOnClickListener(v -> {
                // удаляем view урока
                outContainer.removeView(lines.get(lines.size() - 1).container);

                // удаляем из списка
                lines.remove(lines.size() - 1);

                // ставим те же параметры пункту выше
                setLastElementOnClickListener();
            });
        }
    }

    // проверка полей
    int[][] checkFields() {
        // флаг правильности всех полей
        boolean correctFlag = true;
        // флаг правильности текущего поля
        boolean currentCorrectFlag;

        // массив с временем из полей
        int[][] fieldsTime = new int[lines.size()][4];

        // проверяем формат чисел
        for (int linesI = 0; linesI < fieldsTime.length; linesI++) {
            for (int fieldI = 0; fieldI < 4; fieldI++) {

                // получаем текст ячейки
                String fieldText = lines.get(linesI).timeFields[fieldI].getText().toString().trim();
                // эта ячейка до проверки правильная
                currentCorrectFlag = true;

                // записываем из текстовых полей в массив
                if (fieldText.length() == 0) {
                    fieldsTime[linesI][fieldI] = 0;
                } else
                    fieldsTime[linesI][fieldI] = Integer.parseInt(fieldText);

                if ((fieldText.length() != 0)) {
                    // численный размер
                    if ((fieldsTime[linesI][fieldI] > 23 && fieldI % 2 == 0) ||
                            (fieldsTime[linesI][fieldI] > 59 && fieldI % 2 == 1) ||
                            (fieldsTime[linesI][fieldI] < 0)
                    ) currentCorrectFlag = false;
                } else {
                    currentCorrectFlag = false;
                }

                // соответствующе закрашиваем ячейку
                if (currentCorrectFlag) {
                    // получаем основной цвет текста в соответствии с подпиской
                    int textColor = getResources().getColor(
                            (isSubscribe || linesI < SharedPrefsContract.PREMIUM_PARAM_MAX_LESSONS_COUNT)?
                                    R.color.text_color_simple:
                                    R.color.text_color_not_active
                    );
                    lines.get(linesI).timeFields[fieldI].setTextColor(textColor);
                } else {
                    lines.get(linesI).timeFields[fieldI].setTextColor(getResources().getColor(R.color.signalRed));
                    correctFlag = false;
                }
            }
        }

        // если все поля корректны по формату, проверяем время в newTime
        if (correctFlag) {

            for (int lessonI = 0; lessonI < fieldsTime.length; lessonI++) {

                // если время начала урока больше времени конца
                if (fieldsTime[lessonI][0] > fieldsTime[lessonI][2] || (
                        (fieldsTime[lessonI][0] == fieldsTime[lessonI][2]) &&
                                (fieldsTime[lessonI][1] >= fieldsTime[lessonI][3])
                )) {
                    // перекрашиваем уже покрашенные ячейки в красный
                    correctFlag = false;
                    for (int fieldI = 0; fieldI < 4; fieldI++)
                        lines.get(lessonI).timeFields[fieldI].setTextColor(getResources().getColor(R.color.signalRed));
                }
            }

            if (correctFlag) {
                for (int lessonI = 0; lessonI < fieldsTime.length - 1; lessonI++) {
                    // если время начала урока больше времени начала следующего урока,
                    if (fieldsTime[lessonI][0] > fieldsTime[lessonI + 1][0] || (
                            (fieldsTime[lessonI][0] == fieldsTime[lessonI + 1][0]) &&
                                    (fieldsTime[lessonI][1] >= fieldsTime[lessonI + 1][1])
                    )) {
                        correctFlag = false;
                        lines.get(lessonI).timeFields[0].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI).timeFields[1].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI + 1).timeFields[0].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI + 1).timeFields[1].setTextColor(getResources().getColor(R.color.signalRed));
                    }

                    // или если время конца урока больше времени конца следующего
                    if (fieldsTime[lessonI][2] > fieldsTime[lessonI + 1][2] || (
                            (fieldsTime[lessonI][2] == fieldsTime[lessonI + 1][2]) &&
                                    (fieldsTime[lessonI][3] >= fieldsTime[lessonI + 1][3])
                    )) {
                        correctFlag = false;
                        lines.get(lessonI).timeFields[2].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI).timeFields[3].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI + 1).timeFields[2].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI + 1).timeFields[3].setTextColor(getResources().getColor(R.color.signalRed));
                    }
                }
            }
        }

        // если поля не прошли проверку возвращаем null
        return correctFlag ? fieldsTime : null;
    }


    // метод трансформации числа в текст с двумя позициями
    String getTwoSymbols(int number) {
        return (number < 10 ?
                '0' + Integer.toString(number) :
                Integer.toString(number)
        );
    }


    // класс для предачи данных в диалог от активити
    static class EditTimeDialogDataTransfer implements Serializable {
        public static final String PARAM_DATA = "EditTimeData";

        int[][] lessonPeriods;

        public EditTimeDialogDataTransfer(int[][] lessonPeriods) {
            this.lessonPeriods = lessonPeriods;
        }
    }

}

class TimeViewLine {
    // контейнер нужен, чтобы можно было удалять view
    View container;
    TextView[] timeFields;
    ImageView deleteImage;

    TimeViewLine() {
        timeFields = new TextView[4];
    }
}

interface EditTimeDialogFragmentInterface {
    void editTime(int[][] time);
}
