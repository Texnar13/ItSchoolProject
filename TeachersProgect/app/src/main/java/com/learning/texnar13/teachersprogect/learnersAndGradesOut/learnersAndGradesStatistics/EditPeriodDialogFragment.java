package com.learning.texnar13.teachersprogect.learnersAndGradesOut.learnersAndGradesStatistics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.learning.texnar13.teachersprogect.R;

import java.util.GregorianCalendar;

public class EditPeriodDialogFragment extends DialogFragment {

    public static final String ARGUMENT_ID = "_id";
    public static final String ARGUMENT_NAME = "name";
    public static final String ARGUMENT_DATE_ARRAY = "dateArray";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_layout_edit_period, null);
        builder.setView(dialogLayout);


// -- текстовое поле названия --
        final EditText editName = (EditText) dialogLayout.findViewById(R.id.edit_period_dialog_fragment_edit_name);
        // достаем переданные данные
        editName.setText(getArguments().getString(ARGUMENT_NAME));

// --- выбор даты ---
        // получаем предыдущие данные
        int[] date = getArguments().getIntArray(ARGUMENT_DATE_ARRAY);// y,m,d,y,m,d
        // -- дата начала --
        // получаем поля
        final EditText editStartDay = (EditText) dialogLayout.findViewById(R.id.create_period_dialog_fragment_edit_start_day);
        final EditText editStartMonth = (EditText) dialogLayout.findViewById(R.id.create_period_dialog_fragment_edit_start_month);
        final EditText editStartYear = (EditText) dialogLayout.findViewById(R.id.create_period_dialog_fragment_edit_start_year);
        // выводим дату
        editStartDay.setHint("" + date[2]);
        editStartMonth.setHint("" + date[1]);
        editStartYear.setHint("" + date[0]);

        // -- дата конца --
        // получаем поля
        final EditText editEndDay = (EditText) dialogLayout.findViewById(R.id.create_period_dialog_fragment_edit_end_day);
        final EditText editEndMonth = (EditText) dialogLayout.findViewById(R.id.create_period_dialog_fragment_edit_end_month);
        final EditText editEndYear = (EditText) dialogLayout.findViewById(R.id.create_period_dialog_fragment_edit_end_year);
        // выводим дату
        editEndDay.setHint("" + date[5]);
        editEndMonth.setHint("" + date[4]);
        editEndYear.setHint("" + date[3]);

// -- кнопки согласия/отмены --
        // кнопка отмены
        Button neutralButton = (Button) dialogLayout.findViewById(R.id.create_period_dialog_fragment_button_cancel);
        // кнопка сохранения
        Button positiveButton = (Button) dialogLayout.findViewById(R.id.create_period_dialog_fragment_button_save);
        // при нажатии...
        // сохранение
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDateGood(editStartDay,
                        editStartMonth,
                        editStartYear,
                        editEndDay,
                        editEndMonth,
                        editEndYear)
                        ) {
                    try {
                        //вызываем в активности метод по созданию промежутка статистики
                        ((EditStatisticDialogInterface) getActivity()).editStatistic(
                                getArguments().getLong(ARGUMENT_ID),
                                editName.getText().toString(),
                                "" + editStartYear.getText().toString() + "-" +
                                        getTwoSymbols(Integer.parseInt(editStartMonth.getText().toString())) + "-" +
                                        getTwoSymbols(Integer.parseInt(editStartDay.getText().toString())) + " 00:00:00",
                                "" + editEndYear.getText().toString() + "-" +
                                        getTwoSymbols(Integer.parseInt(editEndMonth.getText().toString())) + "-" +
                                        getTwoSymbols(Integer.parseInt(editEndDay.getText().toString())) + " 23:59:59"
                        );
                    } catch (java.lang.ClassCastException e) {
                        //в вызвающей активности должен быть имплементирован класс CreateStatisticDialogInterface
                        e.printStackTrace();
                        Log.i(
                                "TeachersApp",
                                "EditStatisticDialogInterface: you must implements EditStatisticDialogInterface in your activity"
                        );
                    }
                    dismiss();
                }
            }
        });

        // отмена
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.i("TeachersApp", "EditStatisticDialogInterface - onCancel");
        super.onCancel(dialog);
    }

    //проверка введенных дат
    boolean isDateGood(EditText editStartDay, EditText editStartMonth, EditText editStartYear, EditText editEndDay, EditText editEndMonth, EditText editEndYear) {
        //переменная отвечающая за то подходят данные или нет
        boolean isGood = true;

        // - дата начала -

        // календарь для проверки даты
        GregorianCalendar startCalendar = new GregorianCalendar(0, 0, 1, 0, 0);

        // размеры текста
        if (editStartYear.getText().toString().length() != 4) {
            editStartYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            //убираем красный
            editStartYear.setBackground(null);
            // диапазон чисел
            if (Integer.parseInt(editStartYear.getText().toString()) < 1000 || Integer.parseInt(editStartYear.getText().toString()) > 9999) {
                editStartYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editStartYear.setBackground(null);
                // год в календарь
                startCalendar.set(GregorianCalendar.YEAR, Integer.parseInt(editStartYear.getText().toString()));

            }
        }

        // размеры текста
        if (editStartMonth.getText().toString().length() <= 0 || editStartMonth.getText().toString().length() > 2) {
            editStartMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            //убираем красный
            editStartMonth.setBackground(null);
            // диапазон чисел
            if (Integer.parseInt(editStartMonth.getText().toString()) < 1 || Integer.parseInt(editStartMonth.getText().toString()) > 12) {
                editStartMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editStartMonth.setBackground(null);
                // месяц в календарь
                startCalendar.set(GregorianCalendar.MONTH, Integer.parseInt(editStartMonth.getText().toString()) - 1);
            }
        }


        // размеры текста
        if (editStartDay.getText().toString().length() <= 0 || editStartDay.getText().toString().length() > 2) {
            editStartDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            //убираем красный
            editStartDay.setBackground(null);
            // диапазон чисел
            if (Integer.parseInt(editStartDay.getText().toString()) < 1 || Integer.parseInt(editStartDay.getText().toString()) > startCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)) {
                editStartDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editStartDay.setBackground(null);
                // день в календарь
                startCalendar.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(editStartDay.getText().toString()));
            }
        }


        // - дата конца -

        // календарь для проверки даты
        GregorianCalendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(startCalendar.getTime());

        // размеры текста
        if (editEndYear.getText().toString().length() != 4) {
            editEndYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            //убираем красный
            editEndYear.setBackground(null);
            // диапазон чисел
            if (Integer.parseInt(editEndYear.getText().toString()) < 1000 || Integer.parseInt(editEndYear.getText().toString()) > 9999) {
                editEndYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editEndYear.setBackground(null);
                // год в календарь
                endCalendar.set(GregorianCalendar.YEAR, Integer.parseInt(editEndYear.getText().toString()));
            }
        }

        // размеры текста
        if (editEndMonth.getText().toString().length() <= 0 || editEndMonth.getText().toString().length() > 2) {
            editEndMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            //убираем красный
            editEndMonth.setBackground(null);
            // диапазон чисел
            if (Integer.parseInt(editEndMonth.getText().toString()) < 1 || Integer.parseInt(editEndMonth.getText().toString()) > 12) {
                editEndMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editEndMonth.setBackground(null);
                // месяц в календарь
                endCalendar.set(GregorianCalendar.MONTH, Integer.parseInt(editEndMonth.getText().toString()) - 1);
            }
        }


        // размеры текста
        if (editEndDay.getText().toString().length() <= 0 || editEndDay.getText().toString().length() > 2) {
            editEndDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
            isGood = false;
        } else {
            //убираем красный
            editEndDay.setBackground(null);
            // диапазон чисел
            if (Integer.parseInt(editEndDay.getText().toString()) < 1 || Integer.parseInt(editEndDay.getText().toString()) > endCalendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)) {
                editEndDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                //убираем красный
                editEndDay.setBackground(null);
                // день в календарь
                endCalendar.set(GregorianCalendar.DAY_OF_MONTH, Integer.parseInt(editEndDay.getText().toString()));
            }
        }


        if (isGood) {
            // проверка по времени
            if (startCalendar.getTime().getTime() > endCalendar.getTime().getTime()) {
                Log.i("TeachersApp", "isDateGood: start:" + startCalendar.getTime().getTime() + " end:" + endCalendar.getTime().getTime());
                Log.i("TeachersApp", "isDateGood: start: year-" + startCalendar.get(GregorianCalendar.YEAR) +
                        " month-" + startCalendar.get(GregorianCalendar.MONTH) +
                        " day-" + startCalendar.get(GregorianCalendar.DAY_OF_MONTH) +
                        " end year-" + endCalendar.get(GregorianCalendar.YEAR) +
                        " month-" + endCalendar.get(GregorianCalendar.MONTH) +
                        " day-" + endCalendar.get(GregorianCalendar.DAY_OF_MONTH)
                );
                editStartDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editStartMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editStartYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editEndDay.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editEndMonth.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                editEndYear.setBackground(getResources().getDrawable(R.drawable.start_screen_3_4_pink_spot));
                isGood = false;
            } else {
                editStartDay.setBackground(null);
                editStartMonth.setBackground(null);
                editStartYear.setBackground(null);
                editEndDay.setBackground(null);
                editEndMonth.setBackground(null);
                editEndYear.setBackground(null);
            }
        }

        // все в порядке возвращаем истину
        return isGood;
    }

    // -- метод трансформации числа в текст с двумя позициями --
    String getTwoSymbols(int number) {
        if (number < 10 && number >= 0) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    // --------- форматы ----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

interface EditStatisticDialogInterface {
    void editStatistic(long id, String name, String startDate, String endDate);
}