package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
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

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

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

        addTextButton.setOnClickListener(view -> {
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
                newTypeContainer.setBackground(getActivity().getResources().getDrawable(R.drawable.base_background_button_round_gray));
                // параметры контейнера
                LinearLayout.LayoutParams newTypeContainerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                newTypeContainerParams.bottomMargin = 8;
                newTypeContainerParams.setMargins(
                        getResources().getDimensionPixelOffset(R.dimen.half_margin),
                        getResources().getDimensionPixelOffset(R.dimen.simple_margin),
                        getResources().getDimensionPixelOffset(R.dimen.half_margin),
                        getResources().getDimensionPixelOffset(R.dimen.simple_margin));

                // ---- добавляем ссылку на контейнер элементу списка ----
                newType.typeContainer = newTypeContainer;

                // ---- добавляем тип в лист ----
                types.add(newType);
                // ---- выводим контейнер в скролл ----
                listOut.addView(newTypeContainer, newTypeContainerParams);

                // ---- выводим содержимое в контейнер ----
                outContentInTypeContainer(newType, false);

            } catch (ClassCastException e) {
                //в вызвающей активности должен быть имплементирован класс EditGradesTypeDialogFragmentInterface
                e.printStackTrace();
                Log.e(
                        "TeachersApp",
                        "EditGradesTypeDialogFragment: you must implements EditGradesTypeDialogFragmentInterface in your activity"
                );
            }
        });


// ---- кнопка отмены ----
        ImageView neutralButton = dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_grades_types_cancel_button);
        neutralButton.setOnClickListener(view -> dismiss());

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
            item.setBackground(getActivity().getResources().getDrawable(R.drawable.base_background_button_round_gray));
            // параметры контейнера
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            itemParams.bottomMargin = 8;
            itemParams.setMargins(
                    getResources().getDimensionPixelOffset(R.dimen.half_margin),
                    getResources().getDimensionPixelOffset(R.dimen.simple_margin),
                    getResources().getDimensionPixelOffset(R.dimen.half_margin),
                    getResources().getDimensionPixelOffset(R.dimen.simple_margin));
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

        if (isContainerActive) {
            // если контейнер активен выводим текстовое поле с кнопками
            // -- отключаем нажатие на контейнер --
            typeRecord.typeContainer.setOnClickListener(null);

            // тавим высоту контейнера по содержимомоу
            typeRecord.typeContainer.getLayoutParams().height =
                    ViewGroup.LayoutParams.WRAP_CONTENT;

            // -- удаляем текст --
            typeRecord.typeContainer.removeAllViews();
            // -- вставляем текстовое поле -- (назначая ему стиль)
            final EditText editText = new EditText(getActivity());
            editText.setText(typeRecord.typeName);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            editText.setSingleLine(true);//todo закрывать клавиатуру на открытии нового контейнера и закрытии этого
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
            editText.setGravity(Gravity.CENTER_VERTICAL);

            // эта строка убирает флаги(были поставлены автоматически), которые недавали открывать
            // клавиатуру на программно созданных editText
            //http://qaru.site/questions/2321163/soft-keyboard-does-not-show-up-on-edittext-recyclerview-in-a-dialogfragment
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            // параметры текста
            final LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.my_buttons_height_size),
                    1F
            );
            editTextParams.gravity = Gravity.CENTER_VERTICAL;
            editTextParams.leftMargin = getResources().getDimensionPixelOffset(R.dimen.double_margin);
            editTextParams.rightMargin = getResources().getDimensionPixelOffset(R.dimen.double_margin);
            editTextParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.double_margin);
            typeRecord.typeContainer.addView(editText, editTextParams);


            // -- и контейнер для кнопок изменения --
            LinearLayout buttonsContainer = new LinearLayout(getActivity());
            buttonsContainer.setOrientation(LinearLayout.HORIZONTAL);
            buttonsContainer.setBackground(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.base_background_button_bottom_round_blue, null));
            buttonsContainer.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.simple_buttons_height),
                    1F
            );
            typeRecord.typeContainer.addView(buttonsContainer, containerParams);

            // кнопка удалить
            if (typeRecord.typeId != 1) {
                TextView removeText = new TextView(getActivity());
                removeText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
                removeText.setText(R.string.settings_activity_dialog_button_remove);
                removeText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_semibold));
                removeText.setTextColor(getResources().getColor(R.color.backgroundWhite));
                removeText.setGravity(Gravity.CENTER);
                removeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.simple_buttons_text_size));
                removeText.setPadding(
                        getResources().getDimensionPixelOffset(R.dimen.double_margin),
                        getResources().getDimensionPixelOffset(R.dimen.double_margin),
                        getResources().getDimensionPixelOffset(R.dimen.double_margin),
                        getResources().getDimensionPixelOffset(R.dimen.double_margin)
                );

                LinearLayout.LayoutParams removeTextParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1
                );
                buttonsContainer.addView(removeText, removeTextParams);
                removeText.setOnClickListener(v -> {
                    boolean isDeleted = false;
                    try {
                        //вызываем в активности метод по удалению
                        isDeleted = ((EditGradesTypeDialogFragmentInterface) getActivity()).removeGradesType(typeRecord.typeId);
                    } catch (ClassCastException e) {
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
                });
            }


            TextView saveText = new TextView(getActivity());
            saveText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
            saveText.setText(R.string.button_save);
            saveText.setTextColor(getResources().getColor(R.color.backgroundWhite));
            saveText.setGravity(Gravity.CENTER);
            saveText.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_semibold));
            saveText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.simple_buttons_text_size));
            saveText.setPadding(
                    getResources().getDimensionPixelOffset(R.dimen.double_margin),
                    getResources().getDimensionPixelOffset(R.dimen.double_margin),
                    getResources().getDimensionPixelOffset(R.dimen.double_margin),
                    getResources().getDimensionPixelOffset(R.dimen.double_margin)
            );

            LinearLayout.LayoutParams saveTextParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1
            );
            buttonsContainer.addView(saveText, saveTextParams);
            saveText.setOnClickListener(v -> {
                // вызываем метод по сохранению названия
                boolean isChanged = false;
                try {
                    //вызываем в активности метод по сохранению
                    isChanged = ((EditGradesTypeDialogFragmentInterface) getActivity()).editGradesType(typeRecord.typeId, editText.getText().toString());
                } catch (ClassCastException e) {
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
                } catch (NullPointerException e) {
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
            });

        } else {// если контейнер не активен выводим просто текст

            // тавим стандартную высоту контейнера
            typeRecord.typeContainer.getLayoutParams().height =
                    getResources().getDimensionPixelSize(R.dimen.simple_buttons_height);

            // --- текстовое поле элемента ---
            TextView textView = new TextView(getActivity());
            textView.setText(typeRecord.typeName);
            textView.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.montserrat_medium));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.simple_buttons_text_size));
            textView.setTextColor(Color.BLACK);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            // параметры текста
            final LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelOffset(R.dimen.my_buttons_height_size),
                    1F
            );
            textViewParams.gravity = Gravity.CENTER_VERTICAL;
            textViewParams.leftMargin = getResources().getDimensionPixelOffset(R.dimen.double_margin);
            textViewParams.rightMargin = getResources().getDimensionPixelOffset(R.dimen.double_margin);
            typeRecord.typeContainer.addView(textView, textViewParams);

            // --- при нажатии на контейнер ---
            typeRecord.typeContainer.setOnClickListener(v -> {
                // закрываем все остальные контейнеры//TODO можно будет добавить переменную boolean отвечающую зато, открыт контейнер или нет, и лишний раз не перерисовывать его
                for (GradesTypeRecord fType : types) {
                    if (fType.typeId != typeRecord.typeId) {
                        // выводим его не активным
                        outContentInTypeContainer(fType, false);
                    } else
                        // выводим его активным
                        outContentInTypeContainer(typeRecord, true);
                }
            });
        }
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