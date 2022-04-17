package com.learning.texnar13.teachersprogect.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

import java.io.Serializable;
import java.util.ArrayList;

public class EditTimeDialogFragment extends DialogFragment {

    // массив с полями
    ArrayList<TimeViewLine> lines;

    // контейнер вывода времени
    LinearLayout outContainer;

    /* todo переделай эту жесть +
     *  косяк: не можем добавить урок после 23:59
     *  правим, создаем
     *  цифра все равно красная
     *
     *  todo нужна проверка и сохранение после каждого on focus changed.
     *   метод проверки по 3 ряда? (измененный ряд, один ряд сверху и один ряд снизу)
     */


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
        EditTimeDialogDataTransfer dataTransfer =
                ((EditTimeDialogDataTransfer) getArguments().getSerializable(EditTimeDialogDataTransfer.PARAM_DATA));
        if (dataTransfer == null) {
            dismiss();
            Log.i("TeachersApp",
                    "EditTimeDialogFragment: you must give time( Bungle putIntArray)"
            );
            Dialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            return dialog;
        }
        // распаковываем время
        int[][] rawDataArray = dataTransfer.lessonPeriods;


        // массив с полями
        lines = new ArrayList<>(rawDataArray.length);

        // выводим данные в поля
        // контейнер полей
        outContainer = scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_time_out);
        // выводим строки
        // создаем обьект строки
        for (int rowsI = 0; rowsI < rawDataArray.length; rowsI++) {
            lines.add(inflateNewLine(rawDataArray[rowsI], rowsI + 1));
        }


        // выводим крестик на последнем уроке
        setLastElementOnClickListener();


        // при нажатии на кнопку закрыть
        scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_button_close).setOnClickListener(v -> dismiss());

        // кнопка добавить урок
        scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_button_add).setOnClickListener(v -> {
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

                // сохраняем изменения в бд?

            } else {
                Toast.makeText(getActivity(), R.string.settings_activity_dialog_edit_time_toast_last_lesson_error, Toast.LENGTH_LONG).show();
                lastLine.timeFields[3].setBackgroundResource(R.drawable.base_background_edit_text_error);
            }
        });

        // кнопка сохранения времени из полей
        scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_button_save).setOnClickListener(view -> {

            // массив с передаваемым временем
            int[][] newTime = checkFields();
            if (newTime != null) {
                try {
                    //вызываем в активности метод по сохранению
                    ((EditTimeDialogFragmentInterface) getActivity()).editTime(newTime);
                } catch (ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditTimeDialogFragmentInterface
                    e.printStackTrace();
                    Log.i(
                            "TeachersApp",
                            "EditTimeDialogFragment: you must implements EditTimeDialogFragmentInterface in your activity"
                    );
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                dismiss();
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    TimeViewLine inflateNewLine(int[] startTextValues, int lessonPos) {
        // создаем обьект строки
        TimeViewLine line = new TimeViewLine();

        // раздуваем одну строку
        View rootOfElement = getActivity().getLayoutInflater().inflate(
                R.layout.settings_dialog_edit_time_template_single_time_line, null);
        outContainer.addView(rootOfElement);
        line.container = rootOfElement;

        // находим в ней элементы
        line.timeFields[0] = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_start_hour);
        line.timeFields[1] = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_start_minute);
        line.timeFields[2] = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_end_hour);
        line.timeFields[3] = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_end_minute);
        line.deleteImage = rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_delete_button);

        ((TextView) rootOfElement.findViewById(R.id.settings_dialog_edit_time_template_single_time_line_lesson_number))
                .setText(lessonPos + ".");

        // проставляем значения в 4 текстовых поля
        for (int textsI = 0; textsI < 4; textsI++) {
            line.timeFields[textsI].setText(getTwoSymbols(startTextValues[textsI]));
            line.timeFields[textsI].setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        }
        return line;
    }


    // выводим крестик на последнем уроке
    void setLastElementOnClickListener() {
        if (lines.size() > 1) {
            ImageView deleteImage = lines.get(lines.size() - 1).deleteImage;
            deleteImage.setImageResource(R.drawable.base_button_close_blue);
            deleteImage.setOnClickListener(v -> {
                // удаляем view урока
                outContainer.removeView(lines.get(lines.size() - 1).container);

                /*
                 удаляем из бд?
                 (нет, не удаляем пользовательские данные (уроки) из бд)
                  (но вот уроки неплохо было бы редачить на лету)
                */

                //удаляем из списка
                lines.remove(lines.size() - 1);

                // ставим те же параметры пункту выше
                setLastElementOnClickListener();
            });
        }
    }

    // проверка полей
    int[][] checkFields() {
        Log.e("tag", "test");
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

                // проверяем длинну строки
                if ((fieldText.length() <= 2) && (fieldText.length() != 0)) {

                    // прооверяем на лишние символы
                    try {
                        fieldsTime[linesI][fieldI] = Integer.parseInt(fieldText);
                    } catch (NumberFormatException e) {
                        currentCorrectFlag = false;
                        e.printStackTrace();
                    }

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
                    lines.get(linesI).timeFields[fieldI].setBackgroundResource(R.drawable.base_background_edit_text_selector);
                } else {
                    lines.get(linesI).timeFields[fieldI].setBackgroundResource(R.drawable.base_background_edit_text_error);
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
                    correctFlag = false;
                    for (int fieldI = 0; fieldI < 4; fieldI++)
                        lines.get(lessonI).timeFields[fieldI].setBackgroundResource(R.drawable.base_background_edit_text_error);

                } else
                    for (int fieldI = 0; fieldI < 4; fieldI++)
                        lines.get(lessonI).timeFields[fieldI].setBackgroundResource(R.drawable.base_background_edit_text_selector);
            }

            if (correctFlag) {
                for (int lessonI = 0; lessonI < fieldsTime.length - 1; lessonI++) {
                    // если время начала урока больше времени начала следующего урока,
                    if (fieldsTime[lessonI][0] > fieldsTime[lessonI + 1][0] || (
                            (fieldsTime[lessonI][0] == fieldsTime[lessonI + 1][0]) &&
                                    (fieldsTime[lessonI][1] >= fieldsTime[lessonI + 1][1])
                    )) {
                        correctFlag = false;
                        lines.get(lessonI).timeFields[0].setBackgroundResource(R.drawable.base_background_edit_text_error);
                        lines.get(lessonI).timeFields[1].setBackgroundResource(R.drawable.base_background_edit_text_error);
                        lines.get(lessonI + 1).timeFields[0].setBackgroundResource(R.drawable.base_background_edit_text_error);
                        lines.get(lessonI + 1).timeFields[1].setBackgroundResource(R.drawable.base_background_edit_text_error);
                    }

                    // или если время конца урока больше времени конца следующего
                    if (fieldsTime[lessonI][2] > fieldsTime[lessonI + 1][2] || (
                            (fieldsTime[lessonI][2] == fieldsTime[lessonI + 1][2]) &&
                                    (fieldsTime[lessonI][3] >= fieldsTime[lessonI + 1][3])
                    )) {
                        correctFlag = false;
                        lines.get(lessonI).timeFields[2].setBackgroundResource(R.drawable.base_background_edit_text_error);
                        lines.get(lessonI).timeFields[3].setBackgroundResource(R.drawable.base_background_edit_text_error);
                        lines.get(lessonI + 1).timeFields[2].setBackgroundResource(R.drawable.base_background_edit_text_error);
                        lines.get(lessonI + 1).timeFields[3].setBackgroundResource(R.drawable.base_background_edit_text_error);
                    }
                }
            }
        }

        return correctFlag ? fieldsTime : null;
    }


    // метод трансформации числа в текст с двумя позициями
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0) {
            return '0' + Integer.toString(number);
        } else {
            return Integer.toString(number);
        }
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
