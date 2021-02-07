package com.learning.texnar13.teachersprogect.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

import java.io.Serializable;
import java.util.ArrayList;

public class EditTimeDialogFragment extends DialogFragment {

    // массив с полями
    ArrayList<TimeViewLine> lines;

    // контейнер вывода времени
    LinearLayout outContainer;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // настраиваем программный вывод векторных изображений
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

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
        // выводим поля
        for (int lineI = 0; lineI < rawDataArray.length; lineI++) {

            // создаем обьект строки
            TimeViewLine line = new TimeViewLine();
            lines.add(line);
            // контейнер строки
            line.container = new LinearLayout(getActivity());
            line.container.setGravity(Gravity.CENTER_VERTICAL);
            line.container.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            containerParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.simple_margin);
            outContainer.addView(line.container, containerParams);


            // параметры текстовых полей
            line.timeFields = new EditText[4];
            LinearLayout.LayoutParams[] lineNumberFieldsParams = new LinearLayout.LayoutParams[4];
            // инициализируем все 4 текстовых поля
            for (int textsI = 0; textsI < lineNumberFieldsParams.length; textsI++) {
                line.timeFields[textsI] = new EditText(getActivity());
                line.timeFields[textsI].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                line.timeFields[textsI].setGravity(Gravity.CENTER);
                line.timeFields[textsI].setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                line.timeFields[textsI].setBackgroundResource(R.drawable.statistic_activity_date_background);
                line.timeFields[textsI].setPadding(0, 0, 0, 0);
                line.timeFields[textsI].setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                line.timeFields[textsI].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria));
                // тексты минут пишем двумя числами
                if (textsI % 2 != 0) {
                    line.timeFields[textsI].setText(getTwoSymbols(rawDataArray[lineI][textsI]));
                } else {
                    line.timeFields[textsI].setText(Integer.toString(rawDataArray[lineI][textsI]));
                }
                lineNumberFieldsParams[textsI] = new LinearLayout.LayoutParams(
                        getResources().getDimensionPixelOffset(R.dimen.text_field_two_simple_symbols_width),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
            }

            // текст номера
            TextView lineNumberText = new TextView(getActivity());
            lineNumberText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            lineNumberText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            lineNumberText.setText(Integer.toString(lineI + 1) + '.');
//            lineNumberText.setGravity(Gravity.RIGHT);
            lineNumberText.setGravity(Gravity.END);
            LinearLayout.LayoutParams lineNumberTextParams = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelOffset(R.dimen.text_field_two_simple_symbols_width),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            line.container.addView(lineNumberText, lineNumberTextParams);

            // текст первого часа
            line.container.addView(line.timeFields[0], lineNumberFieldsParams[0]);

            // текст ':'
            TextView beginPointer = new TextView(getActivity());
            beginPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            beginPointer.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            beginPointer.setText(getResources().getString(R.string.settings_activity_dialog_text_time_colon));
            LinearLayout.LayoutParams beginPointerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            line.container.addView(beginPointer, beginPointerParams);

            // текст первых минут
            line.container.addView(line.timeFields[1], lineNumberFieldsParams[1]);

            // текст '--'
            TextView midPointer = new TextView(getActivity());
            midPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            midPointer.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            midPointer.setText(getResources().getString(R.string.settings_activity_dialog_text_time_dash));
            LinearLayout.LayoutParams midPointerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            line.container.addView(midPointer, midPointerParams);

            // текст второго часа
            line.container.addView(line.timeFields[2], lineNumberFieldsParams[2]);

            // текст ':'
            TextView endPointer = new TextView(getActivity());
            endPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            endPointer.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            endPointer.setText(getResources().getString(R.string.settings_activity_dialog_text_time_colon));
            LinearLayout.LayoutParams endPointerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            line.container.addView(endPointer, endPointerParams);

            // текст вторых минут
            line.container.addView(line.timeFields[3], lineNumberFieldsParams[3]);

            // изображение кнопки
            line.deleteImage = new ImageView(getActivity());
            line.deleteImage.setImageResource(R.color.transparent);
            LinearLayout.LayoutParams closeImageParams = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelOffset(R.dimen.my_icon_small_size),
                    getResources().getDimensionPixelOffset(R.dimen.my_icon_small_size)
            );
            closeImageParams.leftMargin = getResources().getDimensionPixelOffset(R.dimen.simple_margin);
            closeImageParams.rightMargin = getResources().getDimensionPixelOffset(R.dimen.simple_margin);
            line.container.addView(line.deleteImage, closeImageParams);
        }

        // выводим крестик на последнем уроке
        setLastElementOnClickListener();


        // при нажатии на кнопку закрыть
        scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // закрываем диалог
                dismiss();
            }
        });

        // кнопка добавить урок
        scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        TimeViewLine line = new TimeViewLine();
                        lines.add(line);
                        // контейнер строки
                        line.container = new LinearLayout(getActivity());
                        line.container.setGravity(Gravity.CENTER_VERTICAL);
                        line.container.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        containerParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.simple_margin);
                        outContainer.addView(line.container, containerParams);


                        // параметры текстовых полей
                        line.timeFields = new EditText[4];
                        LinearLayout.LayoutParams[] lineNumberFieldsParams = new LinearLayout.LayoutParams[4];
                        // инициализируем все 4 текстовых поля
                        for (int textsI = 0; textsI < lineNumberFieldsParams.length; textsI++) {
                            line.timeFields[textsI] = new EditText(getActivity());
                            line.timeFields[textsI].setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                            line.timeFields[textsI].setGravity(Gravity.CENTER);
                            line.timeFields[textsI].setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                            line.timeFields[textsI].setBackgroundResource(R.drawable.statistic_activity_date_background);
                            line.timeFields[textsI].setPadding(0, 0, 0, 0);
                            line.timeFields[textsI].setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                            line.timeFields[textsI].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria));
                            lineNumberFieldsParams[textsI] = new LinearLayout.LayoutParams(
                                    getResources().getDimensionPixelOffset(R.dimen.text_field_two_simple_symbols_width),
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                        }
                        line.timeFields[0].setText(Integer.toString(startHour));
                        line.timeFields[1].setText(getTwoSymbols(startMinute));
                        line.timeFields[2].setText(Integer.toString(endHour));
                        line.timeFields[3].setText(getTwoSymbols(endMinute));


                        // текст номера
                        TextView lineNumberText = new TextView(getActivity());
                        lineNumberText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                        lineNumberText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                        lineNumberText.setText(Integer.toString(lines.size()) + '.');
                        lineNumberText.setGravity(Gravity.END);
                        LinearLayout.LayoutParams lineNumberTextParams = new LinearLayout.LayoutParams(
                                getResources().getDimensionPixelOffset(R.dimen.text_field_two_simple_symbols_width),
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        line.container.addView(lineNumberText, lineNumberTextParams);

                        // текст первого часа
                        line.container.addView(line.timeFields[0], lineNumberFieldsParams[0]);

                        // текст ':'
                        TextView beginPointer = new TextView(getActivity());
                        beginPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                        beginPointer.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                        beginPointer.setText(getResources().getString(R.string.settings_activity_dialog_text_time_colon));
                        LinearLayout.LayoutParams beginPointerParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        line.container.addView(beginPointer, beginPointerParams);

                        // текст первых минут
                        line.container.addView(line.timeFields[1], lineNumberFieldsParams[1]);

                        // текст '--'
                        TextView midPointer = new TextView(getActivity());
                        midPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                        midPointer.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                        midPointer.setText(getResources().getString(R.string.settings_activity_dialog_text_time_dash));
                        LinearLayout.LayoutParams midPointerParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        line.container.addView(midPointer, midPointerParams);

                        // текст второго часа
                        line.container.addView(line.timeFields[2], lineNumberFieldsParams[2]);

                        // текст ':'
                        TextView endPointer = new TextView(getActivity());
                        endPointer.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                        endPointer.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                        endPointer.setText(getResources().getString(R.string.settings_activity_dialog_text_time_colon));
                        LinearLayout.LayoutParams endPointerParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        line.container.addView(endPointer, endPointerParams);

                        // текст вторых минут
                        line.container.addView(line.timeFields[3], lineNumberFieldsParams[3]);

                        // изображение кнопки
                        line.deleteImage = new ImageView(getActivity());
                        line.deleteImage.setImageResource(R.color.transparent);
                        LinearLayout.LayoutParams closeImageParams = new LinearLayout.LayoutParams(
                                getResources().getDimensionPixelOffset(R.dimen.my_icon_small_size),
                                getResources().getDimensionPixelOffset(R.dimen.my_icon_small_size)
                        );
                        closeImageParams.leftMargin = getResources().getDimensionPixelOffset(R.dimen.simple_margin);
                        closeImageParams.rightMargin = getResources().getDimensionPixelOffset(R.dimen.simple_margin);
                        line.container.addView(line.deleteImage, closeImageParams);
                    }

                    // выводим крестик на последнем уроке
                    setLastElementOnClickListener();

                    // сохраняем изменения в бд?

                } else {
                    Toast.makeText(getActivity(), "#Вы не можете создать урок после 23:58", Toast.LENGTH_LONG).show();// todo перевод
                    lastLine.timeFields[3].setTextColor(getResources().getColor(R.color.signalRed));
                    lastLine.timeFields[3].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                    //lastLine.timeFields[3].set(getResources().getColor(R.color.signalRed));
                    //setBackgroundResource(R.drawable.statistic_activity_date_background_alert);
                }
            }
        });

        // кнопка сохранения времени из полей
        scrollLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_time_button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // массив с передаваемым временем
                int[][] newTime = checkFields();
                if (newTime != null) {
                    try {
                        //вызываем в активности метод по сохранению
                        ((EditTimeDialogFragmentInterface) getActivity()).editTime(newTime);
                    } catch (java.lang.ClassCastException e) {
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
            }
        });


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }


    // выводим крестик на последнем уроке
    void setLastElementOnClickListener() {
        if (lines.size() > 1) {
            ImageView deleteImage = lines.get(lines.size() - 1).deleteImage;
            deleteImage.setImageResource(R.drawable.base_button_close_background_round);
            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // удаляем view урока
                    outContainer.removeView(lines.get(lines.size() - 1).container);

                    // удаляем из бд?
                    //

                    // удаляем из списка
                    lines.remove(lines.size() - 1);

                    // ставим те же параметры пункту выше
                    setLastElementOnClickListener();
                }
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
                    lines.get(linesI).timeFields[fieldI].setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                    lines.get(linesI).timeFields[fieldI].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria));
                } else{
                    lines.get(linesI).timeFields[fieldI].setTextColor(getResources().getColor(R.color.signalRed));
                    lines.get(linesI).timeFields[fieldI].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
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
                    for (int fieldI = 0; fieldI < 4; fieldI++) {
                        lines.get(lessonI).timeFields[fieldI].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI).timeFields[fieldI].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                    }
                } else {
                    for (int fieldI = 0; fieldI < 4; fieldI++) {
                        lines.get(lessonI).timeFields[fieldI].setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                        lines.get(lessonI).timeFields[fieldI].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria));
                    }
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
                        lines.get(lessonI).timeFields[0].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                        lines.get(lessonI).timeFields[1].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI).timeFields[1].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                        lines.get(lessonI + 1).timeFields[0].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI + 1).timeFields[0].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                        lines.get(lessonI + 1).timeFields[1].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI + 1).timeFields[1].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                    }

                    // или если время конца урока больше времени конца следующего
                    if (fieldsTime[lessonI][2] > fieldsTime[lessonI + 1][2] || (
                            (fieldsTime[lessonI][2] == fieldsTime[lessonI + 1][2]) &&
                                    (fieldsTime[lessonI][3] >= fieldsTime[lessonI + 1][3])
                    )) {
                        correctFlag = false;
                        lines.get(lessonI).timeFields[2].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI).timeFields[2].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                        lines.get(lessonI).timeFields[3].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI).timeFields[3].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                        lines.get(lessonI + 1).timeFields[2].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI + 1).timeFields[2].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                        lines.get(lessonI + 1).timeFields[3].setTextColor(getResources().getColor(R.color.signalRed));
                        lines.get(lessonI + 1).timeFields[3].setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_bold));
                    }
                }
            }
        }

        if (correctFlag)
            return fieldsTime;
        else
            return null;
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
    LinearLayout container;
    TextView[] timeFields;
    ImageView deleteImage;
}

interface EditTimeDialogFragmentInterface {
    void editTime(int[][] time);
}
