package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import androidx.fragment.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
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

import androidx.core.content.res.ResourcesCompat;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;

//todo если будет какая-то суперсложная ошибка, то возможно это из-за того, что я не чищу ссылки в types на view после удаления view с экрана
public class EditGradesTypesDialogFragment extends DialogFragment {

    public static String ARGS_TYPES_ID_ARRAY_TAG = "typesId";
    public static String ARGS_TYPES_NAMES_ARRAY_TAG = "typesNames";

    // массив с текстами записей, id и view-компонентами
    ArrayList<GradesTypeRecord> types = new ArrayList<>();
    //linear в скролле
    LinearLayout listOut;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// ---- layout диалога ----
        View dialogLayout = getActivity().getLayoutInflater().inflate(R.layout.settings_dialog_edit_grades_types, null);
        builder.setView(dialogLayout);


// ---- LinearLayout в скролле для вывода списка ----
        listOut = dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_grades_types_list_out);
        listOut.setGravity(Gravity.CENTER);
        listOut.setOrientation(LinearLayout.VERTICAL);

// ---- получаем данные ----
        try {
            //переданные значения
            long[] typesId = getArguments().getLongArray(ARGS_TYPES_ID_ARRAY_TAG);
            String[] typesStrings = getArguments().getStringArray(ARGS_TYPES_NAMES_ARRAY_TAG);

            //массив с полями
            for (int i = 0; i < typesId.length; i++) {
                types.add(new GradesTypeRecord(typesId[i], typesStrings[i]));
            }
        } catch (java.lang.NullPointerException e) {
            //в диалог необходимо передать ( Bungle putLong("learnerId",learnerId) )
            e.printStackTrace();
            Log.i(
                    "TeachersApp",
                    "EditTimeDialogFragment: you must give time( Bungle putIntArray, putStringArray)"
            );
        }

// -- выводим поля в список --
        outTypesInLinearLayout(listOut);

// ---- кнопка добавления типа ----
        TextView addTextButton = dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_grades_types_text_button_add);

        addTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // ---- вызываем в активности метод по созданию нового типа ----
                    GradesTypeRecord newType = new GradesTypeRecord(
                            ((EditGradesTypeDialogFragmentInterface) getActivity()).createGradesType(getResources().getString(R.string.settings_activity_dialog_new_type_title)),
                            getResources().getString(R.string.settings_activity_dialog_new_type_title)
                    );
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

                    // ---- выводим содержимое в контейнер ----
                    outContentInTypeContainer(newType, false);

                } catch (java.lang.ClassCastException e) {
                    //в вызвающей активности должен быть имплементирован класс EditGradesTypeDialogFragmentInterface
                    e.printStackTrace();
                    Log.e(
                            "TeachersApp",
                            "EditGradesTypeDialogFragment: you must implements EditGradesTypeDialogFragmentInterface in your activity"
                    );
                }
            }
        });


// ---- кнопка отмены ----
        ImageView neutralButton = dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_grades_types_cancel_button);
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
        for (GradesTypeRecord type : types) {
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
            type.typeContainer = item;
            // ---- выводим контейнер в список ----
            layout.addView(item, itemParams);

            // ---- выводим содержимое в контейнер ----
            outContentInTypeContainer(type, false);
        }

    }

    // ----- метод вывода содержимого одного элемента списка -----
    void outContentInTypeContainer(final GradesTypeRecord typeRecord, boolean isContainerActive) {
        // удаляем все содержимое, если оно есть
        typeRecord.typeContainer.removeAllViews();

        if (isContainerActive) {// если контейнер активен выводим текстовое поле с кнопками
            // -- отключаем нажатие на контейнер --
            typeRecord.typeContainer.setOnClickListener(null);

            // -- удаляем текст --
            typeRecord.typeContainer.removeAllViews();
            // -- вставляем текстовое поле --
            final EditText editText = new EditText(getActivity());
            editText.setText(typeRecord.typeName);
            editText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.geometria_family));
            editText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getActivity().getResources().getDimension(R.dimen.text_subtitle_size)
            );
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextColor(getResources().getColor(R.color.backgroundDarkGray));
            editText.setSingleLine(true);//todo закрывать клавиатуру на открытии нового контейнера и закрытии этого

            // эта строка убирает флаги(были поставлены автоматически), которые недавали открывать
            // клавиатуру на программно созданных editText
            //http://qaru.site/questions/2321163/soft-keyboard-does-not-show-up-on-edittext-recyclerview-in-a-dialogfragment
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            editText.setGravity(Gravity.CENTER_VERTICAL);
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
            if (typeRecord.typeId != 1) {

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
                            isDeleted = ((EditGradesTypeDialogFragmentInterface) getActivity()).removeGradesType(typeRecord.typeId);
                        } catch (java.lang.ClassCastException e) {
                            //в вызвающей активности должен быть имплементирован класс EditGradesTypeDialogFragmentInterface
                            e.printStackTrace();
                            Log.e(
                                    "TeachersApp",
                                    "EditGradesTypeDialogFragment: you must implements EditGradesTypeDialogFragmentInterface in your activity"
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
                        //вызываем в активности метод по сохранению
                        isChanged = ((EditGradesTypeDialogFragmentInterface) getActivity()).editGradesType(typeRecord.typeId, editText.getText().toString());
                    } catch (java.lang.ClassCastException e) {
                        //в вызвающей активности должен быть имплементирован класс EditGradesTypeDialogFragmentInterface
                        e.printStackTrace();
                        Log.e(
                                "TeachersApp",
                                "EditGradesTypeDialogFragment: you must implements EditGradesTypeDialogFragmentInterface in your activity"
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
                        typeRecord.typeName = editText.getText().toString();

                        // выводим неактивный
                        outContentInTypeContainer(typeRecord, false);
                    }
                }
            });

        } else {// если контейнер не активен выводим просто текст

            // --- текстовое поле элемента ---
            TextView textView = new TextView(getActivity());
            textView.setText(typeRecord.typeName);
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
                    // закрываем все остальные контейнеры//TODO можно будет добавить переменную boolean отвечающую зато, открыт контейнер или нет, и лишний раз не перерисовывать его
                    for (GradesTypeRecord fType : types) {
                        if (fType.typeId != typeRecord.typeId) {
                            // выводим его не активным
                            outContentInTypeContainer(fType, false);
                        } else
                            // выводим его активным
                            outContentInTypeContainer(typeRecord, true);
                    }
                }
            });
        }
    }


    //---------форматы----------

    private float pxFromDp(float px) {
        return px * getActivity().getResources().getDisplayMetrics().density;
    }
}

class GradesTypeRecord {

    String typeName;
    long typeId;
    LinearLayout typeContainer;

    GradesTypeRecord(long id, String name) {
        this.typeId = id;
        this.typeName = name;
    }

}

interface EditGradesTypeDialogFragmentInterface {
    long createGradesType(String name);

    boolean editGradesType(long typeId, String newName);

    boolean removeGradesType(long typeId);

    // удалять с проверкой и возвратом boolean
}