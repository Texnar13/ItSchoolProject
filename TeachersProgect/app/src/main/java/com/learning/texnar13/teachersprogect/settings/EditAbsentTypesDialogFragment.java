package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;

public class EditAbsentTypesDialogFragment extends DialogFragment {

    public static String ARGS_TYPES_ID_ARRAY_TAG = "typesId";
    public static String ARGS_TYPES_NAMES_ARRAY_TAG = "typesNames";
    public static String ARGS_TYPES_LONG_NAMES_ARRAY_TAG = "typesLongNames";

    // массив с текстами записей, id и view-компонентами
    ArrayList<AbsentTypeRecord> types = new ArrayList<>();
    // базовый linear в скролле
    LinearLayout listOut;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // layout диалога
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_edit_absent_types, null);
        builder.setView(dialogLayout);

        // LinearLayout в скролле для вывода списка
        listOut = dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_absent_types_list_out);
        listOut.setGravity(Gravity.CENTER);
        listOut.setOrientation(LinearLayout.VERTICAL);


        // получаем список типов
        try {
            // переданные значения
            long[] typesId = getArguments().getLongArray(ARGS_TYPES_ID_ARRAY_TAG);
            String[] typesStrings = getArguments().getStringArray(ARGS_TYPES_NAMES_ARRAY_TAG);
            String[] typesLongStrings = getArguments().getStringArray(ARGS_TYPES_LONG_NAMES_ARRAY_TAG);

            // массив с полями
            for (int i = 0; i < typesId.length; i++) {
                types.add(new AbsentTypeRecord(typesId[i], typesStrings[i], typesLongStrings[i]));
            }
        } catch (java.lang.NullPointerException e) {
            // в диалог необходимо передать ( Bungle putLong("learnerId",learnerId) )
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "EditTimeDialogFragment: you must give time( Bungle putIntArray, putStringArray)"
            );
        }

        // выводим поля в список
        outTypesInLinearLayout(listOut);

        // кнопка добавления типа
        dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_absent_types_text_button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // ---- вызываем в активности метод по созданию нового типа ----
                    AbsentTypeRecord newType = new AbsentTypeRecord(-1, "", "");
                    // ---- создаем контейнер ----
                    LinearLayout newTypeContainer = new LinearLayout(getActivity());
                    newTypeContainer.setGravity(Gravity.CENTER_VERTICAL);
                    newTypeContainer.setOrientation(LinearLayout.VERTICAL);
                    newTypeContainer.setBackground(getActivity().getResources().getDrawable(R.drawable.base_dialog_background_dwhite_full_round));
                    // параметры контейнера
                    LinearLayout.LayoutParams newTypeContainerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    newTypeContainerParams.bottomMargin = 8;
                    newTypeContainerParams.setMargins(
                            (int) getResources().getDimension(R.dimen.half_margin),
                            (int) getResources().getDimension(R.dimen.simple_margin),
                            (int) getResources().getDimension(R.dimen.half_margin),
                            (int) getResources().getDimension(R.dimen.simple_margin));

                    // ---- добавляем ссылку на контейнер элементу списка ----
                    newType.typeContainer = newTypeContainer;

                    // ---- добавляем тип в лист ----
                    types.add(newType);
                    // ---- выводим контейнер в скролл ----
                    listOut.addView(newTypeContainer, newTypeContainerParams);

                    // ---- выводим содержимое в открытый закрывая все остальные контейнеры
                    for (int typeI = 0; typeI < types.size(); typeI++) {
                        if (types.get(typeI) != newType) {
                            if (types.get(typeI).typeId == -1) {// если запись новая, удаляем
                                listOut.removeView(types.get(typeI).typeContainer);
                                types.remove(typeI);
                                typeI--;
                            } else {// иначе выводим его не активным
                                outContentInTypeContainer(types.get(typeI), false);
                            }
                        } else
                            // выводим его активным
                            outContentInTypeContainer(newType, true);

                    }

                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditAbsentTypeDialogFragmentInterface
                    e.printStackTrace();
                    Log.e(
                            "TeachersApp",
                            "EditAbsentTypeDialogFragment: you must implements EditAbsentTypeDialogFragmentInterface in your activity"
                    );
                }
            }
        });


// ---- кнопка отмены ----
        ImageView neutralButton = dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_absent_types_cancel_button);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    // ----- метод вывода всех типов в список -----
    void outTypesInLinearLayout(final LinearLayout layout) {

        // ---- контейнер ----
        // --- текстовое поле ---
        // --- контейнер с кнопками ---
        // -- кнопка удаления --
        // -- кнопка переименования --

        // ---- чистка ----
        layout.removeAllViews();

        // ---- основные поля ----
        for (int typeI = 0; typeI < types.size(); typeI++) {
            // проверка id = -1
            if (types.get(typeI).typeId == -1) {
                listOut.removeView(types.get(typeI).typeContainer);
                types.remove(typeI);
                typeI--;
            } else {

                // ---- создаем контейнер для одного элемента ----
                LinearLayout item = new LinearLayout(getActivity());
                item.setGravity(Gravity.CENTER_VERTICAL);
                item.setOrientation(LinearLayout.VERTICAL);
                item.setBackground(getActivity().getResources().getDrawable(R.drawable.base_dialog_background_dwhite_full_round));
                // параметры контейнера
                LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                itemParams.bottomMargin = 8;
                itemParams.setMargins(
                        (int) getResources().getDimension(R.dimen.half_margin),
                        (int) getResources().getDimension(R.dimen.simple_margin),
                        (int) getResources().getDimension(R.dimen.half_margin),
                        (int) getResources().getDimension(R.dimen.simple_margin));
                // ---- добавляем ссылку на контейнер элементу списка ----
                types.get(typeI).typeContainer = item;
                // ---- выводим контейнер в список ----
                layout.addView(item, itemParams);

                // ---- выводим содержимое в контейнер ----
                outContentInTypeContainer(types.get(typeI), false);
            }
        }

    }

    // ----- метод вывода содержимого одного элемента списка -----
    void outContentInTypeContainer(final AbsentTypeRecord typeRecord, boolean isContainerActive) {
        // удаляем все содержимое, если оно есть
        typeRecord.typeContainer.removeAllViews();

        if (isContainerActive) {// если контейнер активен выводим текстовое поле с кнопками
            // -- отключаем нажатие на контейнер --
            typeRecord.typeContainer.setOnClickListener(null);

            // -- удаляем текст --
            typeRecord.typeContainer.removeAllViews();

            // -- вставляем текстовое поле для длинного текста --
            final EditText longEditText = new EditText(getActivity());
            longEditText.setText(typeRecord.longName);
            longEditText.setHint(R.string.settings_activity_dialog_new_absent_type_long_title_hint);
            longEditText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            longEditText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getActivity().getResources().getDimension(R.dimen.text_subtitle_size)
            );
            longEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            longEditText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            longEditText.setSingleLine(true);
            longEditText.setGravity(Gravity.CENTER_VERTICAL);
            // ставим ограничение в 20 символов
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(20);
            longEditText.setFilters(filterArray);
            // параметры текста
            final LinearLayout.LayoutParams editLongTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size),
                    1F
            );
            editLongTextParams.gravity = Gravity.CENTER_VERTICAL;
            editLongTextParams.leftMargin = (int) getResources().getDimension(R.dimen.double_margin);
            editLongTextParams.rightMargin = (int) getResources().getDimension(R.dimen.double_margin);
            typeRecord.typeContainer.addView(longEditText, editLongTextParams);


            // -- вставляем текстовое поле --
            final EditText editText = new EditText(getActivity());
            editText.setText(typeRecord.typeName);
            editText.setHint(R.string.settings_activity_dialog_new_absent_type_title_hint);
            editText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            editText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getActivity().getResources().getDimension(R.dimen.text_subtitle_size)
            );
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            editText.setSingleLine(true);
            editText.setGravity(Gravity.CENTER_VERTICAL);
            // ставим ограничение в 3 символа
            InputFilter[] longFilterArray = new InputFilter[1];
            longFilterArray[0] = new InputFilter.LengthFilter(3);
            editText.setFilters(longFilterArray);
            // параметры текста
            final LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size),
                    1F
            );
            editTextParams.gravity = Gravity.CENTER_VERTICAL;
            editTextParams.leftMargin = (int) getResources().getDimension(R.dimen.double_margin);
            editTextParams.rightMargin = (int) getResources().getDimension(R.dimen.double_margin);
            typeRecord.typeContainer.addView(editText, editTextParams);

            // эта строка убирает флаги(были поставлены автоматически), которые недавали открывать
            // клавиатуру на программно созданных editText
            //http://qaru.site/questions/2321163/soft-keyboard-does-not-show-up-on-edittext-recyclerview-in-a-dialogfragment
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


            // -- и контейнер для кнопок изменения --
            LinearLayout buttonsContainer = new LinearLayout(getActivity());
            buttonsContainer.setOrientation(LinearLayout.HORIZONTAL);
            buttonsContainer.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1F
            );
            typeRecord.typeContainer.addView(buttonsContainer, containerParams);

            // кнопка удалить
            if (typeRecord.typeId != 1 && typeRecord.typeId != -1) {

                // контейнер кнопки
                LinearLayout removeButtonContainer = new LinearLayout(getActivity());
                removeButtonContainer.setGravity(Gravity.CENTER);
                removeButtonContainer.setBackgroundResource(R.drawable.base_dialog_background_gray_full_round);
                LinearLayout.LayoutParams removeButtonContainerParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
                removeButtonContainerParams.setMargins(
                        (int) getResources().getDimension(R.dimen.simple_margin),
                        (int) getResources().getDimension(R.dimen.simple_margin),
                        0,
                        (int) getResources().getDimension(R.dimen.simple_margin)
                );
                removeButtonContainerParams.gravity = Gravity.CENTER;
                buttonsContainer.addView(removeButtonContainer, removeButtonContainerParams);

                TextView removeText = new TextView(getActivity());
                removeText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
                removeText.setText(R.string.settings_activity_dialog_button_remove);
                removeText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
                removeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                LinearLayout.LayoutParams removeTextParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                removeTextParams.setMargins(
                        (int) getResources().getDimension(R.dimen.simple_margin),
                        (int) getResources().getDimension(R.dimen.simple_margin),
                        (int) getResources().getDimension(R.dimen.simple_margin),
                        (int) getResources().getDimension(R.dimen.simple_margin)
                );
                removeButtonContainer.addView(removeText, removeTextParams);
                removeButtonContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isDeleted = false;
                        try {
                            //вызываем в активности метод по удалению
                            isDeleted = ((EditAbsentTypeDialogFragmentInterface) getActivity()).removeAbsentType(typeRecord.typeId);
                        } catch (java.lang.ClassCastException e) {
                            //в вызвающей активности должен быть имплементирован класс EditAbsentTypeDialogFragmentInterface
                            e.printStackTrace();
                            Log.e(
                                    "TeachersApp",
                                    "EditAbsentTypeDialogFragment: you must implements EditAbsentTypeDialogFragmentInterface in your activity"
                            );
                        }

                        if (isDeleted) {
                            // удаляем переменную в скролле
                            listOut.removeView(typeRecord.typeContainer);
                            // и в списке
                            types.remove(typeRecord);
                        }
                    }
                });
            }


            // контейнер кнопки
            LinearLayout saveButtonContainer = new LinearLayout(getActivity());
            saveButtonContainer.setGravity(Gravity.CENTER);
            saveButtonContainer.setBackgroundResource(R.drawable.base_dialog_background_gray_full_round);
            LinearLayout.LayoutParams saveButtonContainerParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            );
            saveButtonContainerParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin)
            );
            saveButtonContainerParams.gravity = Gravity.CENTER;
            buttonsContainer.addView(saveButtonContainer, saveButtonContainerParams);

            TextView saveText = new TextView(getActivity());
            saveText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            saveText.setText(R.string.settings_activity_dialog_button_save);
            saveText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            saveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
            LinearLayout.LayoutParams saveTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            saveTextParams.setMargins(
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin),
                    (int) getResources().getDimension(R.dimen.simple_margin)
            );
            saveButtonContainer.addView(saveText, saveTextParams);
            saveButtonContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // вызываем метод по сохранению названия
                    boolean isChanged = false;
                    try {
                        // проверка на пустые поля
                        if (editText.getText().toString().trim().length() == 0 ||
                                longEditText.getText().toString().trim().length() == 0) {
                            Toast.makeText(
                                    getActivity(),
                                    R.string.settings_activity_dialog_new_absent_type_toast_empty,
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {


                            // если запись новая
                            if (typeRecord.typeId == -1) {
                                // создаем ее в бд
                                typeRecord.typeId = ((EditAbsentTypeDialogFragmentInterface) getActivity()).createAbsentType(
                                        editText.getText().toString().trim(),
                                        longEditText.getText().toString().trim()
                                );
                                isChanged = true;
                            } else {
                                // изменяем существующую запись
                                isChanged = ((EditAbsentTypeDialogFragmentInterface) getActivity()).editAbsentType(
                                        typeRecord.typeId,
                                        editText.getText().toString().trim(),
                                        longEditText.getText().toString().trim()
                                );
                            }
                        }
                    } catch (java.lang.ClassCastException e) {
                        //в вызвающей активности должен быть имплементирован класс EditAbsentTypeDialogFragmentInterface
                        e.printStackTrace();
                        Log.e(
                                "TeachersApp",
                                "EditAbsentTypeDialogFragment: you must implements EditAbsentTypeDialogFragmentInterface in your activity"
                        );
                    }
                    // прячем клавиатуру
                    try {
                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    } catch (java.lang.NullPointerException e) {
                        e.printStackTrace();
                        Log.e(
                                "TeachersApp",
                                "cannot hide keyboard"
                        );
                    }

                    // перевыводим содержимое
                    if (isChanged) {
                        // меняем переменную в списке
                        typeRecord.typeName = editText.getText().toString().trim();
                        typeRecord.longName = longEditText.getText().toString().trim();

                        // выводим неактивный
                        outContentInTypeContainer(typeRecord, false);
                    }
                }
            });

        } else {// если контейнер не активен выводим просто текст

            // --- текстовое поле элемента ---
            TextView textView = new TextView(getActivity());
            textView.setText(typeRecord.longName);
            textView.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.text_subtitle_size));
            textView.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            // параметры текста
            final LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.my_buttons_height_size),
                    1F
            );
            textViewParams.gravity = Gravity.CENTER_VERTICAL;
            textViewParams.leftMargin = (int) getResources().getDimension(R.dimen.double_margin);
            textViewParams.rightMargin = (int) getResources().getDimension(R.dimen.double_margin);
            typeRecord.typeContainer.addView(textView, textViewParams);

            // --- при нажатии на контейнер ---
            typeRecord.typeContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // закрываем все остальные контейнеры
                    for (int typeI = 0; typeI < types.size(); typeI++) {
                        if (types.get(typeI) != typeRecord) {
                            if (types.get(typeI).typeId == -1) {// если запись новая, удаляем
                                listOut.removeView(types.get(typeI).typeContainer);
                                types.remove(typeI);
                                typeI--;
                            } else {// иначе выводим его не активным
                                outContentInTypeContainer(types.get(typeI), false);
                            }
                        } else
                            // выводим его активным
                            outContentInTypeContainer(typeRecord, true);
                    }
                }
            });
        }
    }


    // метод вывода содержимого нового не сохраненного элемента списка (с id = -1)
    // при сохранении он сановится обычным элементом
    // если пользователь нажмет во вне контейнера, то запись стирается проверками в других методах (тк id = -1).
    void outNewContainer(final AbsentTypeRecord typeRecord) {

    }
}

class AbsentTypeRecord {

    String typeName;
    String longName;
    long typeId;
    LinearLayout typeContainer;

    AbsentTypeRecord(long id, String name, String longName) {
        this.typeId = id;
        this.typeName = name;
        this.longName = longName;
    }

}

interface EditAbsentTypeDialogFragmentInterface {
    long createAbsentType(String name, String longName);

    boolean editAbsentType(long typeId, String newName, String newLongName);

    boolean removeAbsentType(long typeId);

    // удалять с проверкой и возвратом boolean
}