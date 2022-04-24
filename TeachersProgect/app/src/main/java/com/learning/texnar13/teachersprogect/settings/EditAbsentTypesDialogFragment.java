package com.learning.texnar13.teachersprogect.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;
import java.util.Objects;

public class EditAbsentTypesDialogFragment extends DialogFragment {

    // передаваемые параметры
    public static String ARGS_TYPES_ID_ARRAY_TAG = "typesId";
    public static String ARGS_TYPES_NAMES_ARRAY_TAG = "typesNames";
    public static String ARGS_TYPES_LONG_NAMES_ARRAY_TAG = "typesLongNames";
    // Платное огграничение на все типы
    public static String ARGS_TYPES_MAX_COUNT = "maxCount";

    // массив с текстами записей, id и view-компонентами
    ArrayList<AbsentTypeRecord> types = new ArrayList<>();
    // базовый контейнер в скролле для вывода всего списка
    LinearLayout listOut;
    // максимальное количество типов (для премиума) (-1 - без ограничений)
    int maxTypesCount;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = requireArguments();


        // получаем список типов
        long[] typesId = Objects.requireNonNull(arguments.getLongArray(ARGS_TYPES_ID_ARRAY_TAG));
        String[] typesStrings = Objects.requireNonNull(arguments.getStringArray(ARGS_TYPES_NAMES_ARRAY_TAG));
        String[] typesLongStrings = Objects.requireNonNull(arguments.getStringArray(ARGS_TYPES_LONG_NAMES_ARRAY_TAG));
        maxTypesCount = arguments.getInt(ARGS_TYPES_MAX_COUNT);
        // массив с полями
        for (int i = 0; i < typesId.length; i++) {
            types.add(new AbsentTypeRecord(typesId[i], typesStrings[i], typesLongStrings[i], null));
            // (создание View происходит в outTypesInLinearLayout)
        }


        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // layout диалога
        View dialogLayout = requireActivity().getLayoutInflater().inflate(R.layout.settings_dialog_edit_types_absent, null);
        builder.setView(dialogLayout);
        // кнопка отмены
        View neutralButton = dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_absent_types_cancel_button);
        neutralButton.setOnClickListener(view -> dismiss());

        // LinearLayout в скролле для вывода списка
        listOut = dialogLayout.findViewById(R.id.dialog_fragment_layout_settings_edit_absent_types_list_out);
        // выводим поля в список
        outTypesInLinearLayout(listOut);

        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    // создаем контейнер типа
    LinearLayout addNewContainerInList() {
        // создаем контейнер
        LinearLayout newTypeContainer = new LinearLayout(getActivity());
        newTypeContainer.setGravity(Gravity.CENTER_VERTICAL);
        newTypeContainer.setOrientation(LinearLayout.VERTICAL);
        newTypeContainer.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.base_background_button_round_gray, null));
        // параметры контейнера
        LinearLayout.LayoutParams newTypeContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        newTypeContainerParams.bottomMargin =
                getResources().getDimensionPixelSize(R.dimen.double_margin);
        // выводим контейнер в скролл
        listOut.addView(newTypeContainer, newTypeContainerParams);
        // возвращаем ссылку на контейнер
        return newTypeContainer;
    }

    // метод вывода всех типов в список С созданием под них контейнеров
    void outTypesInLinearLayout(LinearLayout layout) {
        // чистка
        layout.removeAllViews();

        // основные поля
        for (int typeI = 0; typeI < types.size(); typeI++) {
            // создаем контейнер для одного элемента и
            // добавляем ссылку на контейнер элементу списка
            types.get(typeI).typeContainer = addNewContainerInList();
            // выводим в этот контейнер наполнение
            outContentInTypeContainer(types.get(typeI), false, (maxTypesCount != -1) && (typeI + 1 > maxTypesCount));
        }

        // вывод кнопки добавить тип
        outAddTypeButton();
    }

    // вывод кнопки добавить тип
    void outAddTypeButton() {
        // вывод контейнера с кнопкой добавить
        LinearLayout addNewTypeButtonContainer = addNewContainerInList();
        // раздуваем разметку содержимого контейнера
        getLayoutInflater().inflate(R.layout.settings_dialog_edit_types_simple_container, addNewTypeButtonContainer);
        // заголовок кнопки
        TextView addNewTypeButton =
                addNewTypeButtonContainer.findViewById(R.id.settings_dialog_edit_types_simple_container_text);
        addNewTypeButton
                .setText(R.string.settings_activity_dialog_add_absent_type_title);
        // при нажатии на контейнер
        addNewTypeButtonContainer.setOnClickListener(v -> addNewType(v));
    }


    // добавление типа (по кнопке)
    void addNewType(View button) {

        // ограничение на количество типов
        if (maxTypesCount != -1 && types.size() >= maxTypesCount) {
            Toast.makeText(
                    getActivity(),
                    getResources().getString(
                            R.string.settings_activity_dialog_new_absent_type_toast_subscrybe,
                            maxTypesCount),
                    Toast.LENGTH_SHORT
            ).show();
        } else {

            // удаляем кнопку добавить тип
            listOut.removeView(button);

            // вызываем в активности метод по созданию нового типа и
            // добавляем ссылку на контейнер элементу списка
            AbsentTypeRecord newType = new AbsentTypeRecord(-1, "", "", addNewContainerInList());

            // добавляем тип в лист
            types.add(newType);

            // выводим содержимое в открытый закрывая все остальные контейнеры
            updateAllContainersContent(newType);
        }
    }

    // обновить содержимое всех контейнеров списка (один активный может быть нулевым)
    void updateAllContainersContent(@Nullable AbsentTypeRecord activeRecord) {
        for (int typeI = 0; typeI < types.size(); typeI++) {
            if (types.get(typeI) != activeRecord) {

                // удаляем новую запись без id (видимо пользователь не захотел сохранять её)
                if (types.get(typeI).typeId == -1) {
                    listOut.removeView(types.get(typeI).typeContainer);
                    types.remove(typeI);
                    typeI--;

                    // добавляем кнопку добавить тип
                    outAddTypeButton();
                } else
                    // закрываем все остальные контейнеры
                    outContentInTypeContainer(types.get(typeI), false, (maxTypesCount != -1) && (typeI + 1 > maxTypesCount));
            } else if (activeRecord != null)
                // выводим активный контейнер
                outContentInTypeContainer(activeRecord, true, (maxTypesCount != -1) && (typeI + 1 > maxTypesCount));
        }
    }

    // метод вывода содержимого одного элемента списка
    void outContentInTypeContainer(AbsentTypeRecord typeRecord, boolean isContainerActive, boolean isContainerBlocked) {
        // удаляем все содержимое, если оно есть
        typeRecord.typeContainer.removeAllViews();

        if (isContainerActive) {
            // ставим высоту контейнера по содержимомоу
            typeRecord.typeContainer.getLayoutParams().height =
                    ViewGroup.LayoutParams.WRAP_CONTENT;

            // если контейнер активен выводим текстовое поле с кнопками
            outContentActive(typeRecord);
        } else {

            // ставим стандартную высоту контейнера
            typeRecord.typeContainer.getLayoutParams().height =
                    getResources().getDimensionPixelSize(R.dimen.simple_buttons_height);

            // если контейнер не активен выводим просто текст
            outContentNotActive(typeRecord, isContainerBlocked);
        }
    }


    // если контейнер не активен выводим просто текст
    void outContentNotActive(AbsentTypeRecord typeRecord, boolean isContainerBlocked) {
        // -- удаляем предыдущее наполнение --
        typeRecord.typeContainer.removeAllViews();

        // раздуваем разметку содержимого контейнера
        getLayoutInflater().inflate(R.layout.settings_dialog_edit_types_simple_container, typeRecord.typeContainer);

        // заголовок кнопки
        TextView title = typeRecord.typeContainer.findViewById(R.id.settings_dialog_edit_types_simple_container_text);
        title.setText(typeRecord.longName + ": " + typeRecord.typeName);


        // можно ли нажать на этот контейнер или он заблокировани подпиской
        if (isContainerBlocked) {
            title.setTextColor(getResources().getColor(R.color.text_color_not_active));
            // при нажатии на контейнер
            typeRecord.typeContainer.setOnClickListener(v -> Toast.makeText(
                    getActivity(),
                    getResources().getString(
                            R.string.settings_activity_dialog_new_absent_type_toast_subscrybe,
                            maxTypesCount),
                    Toast.LENGTH_SHORT
            ).show());
        } else {
            // при нажатии на контейнер
            typeRecord.typeContainer.setOnClickListener(v -> {
                // закрываем все остальные контейнеры
                updateAllContainersContent(typeRecord);
            });
        }
    }


    // если контейнер активен выводим текстовое поле с кнопками
    void outContentActive(AbsentTypeRecord typeRecord) {
        // -- отключаем нажатие на контейнер --
        typeRecord.typeContainer.setOnClickListener(null);

        // -- удаляем предыдущее наполнение --
        typeRecord.typeContainer.removeAllViews();

        // раздуваем разметку содержимого контейнера
        getLayoutInflater().inflate(R.layout.settings_dialog_edit_types_absent_active_container, typeRecord.typeContainer);


        // текстовое поле для длинного текста
        final EditText longEditText = typeRecord.typeContainer.findViewById(R.id.settings_dialog_edit_types_absent_active_container_longedittext);
        longEditText.setText(typeRecord.longName);
        // ставим ограничение в 20 символов
        longEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});


        // текстовое поле для короткого текста
        final EditText editText = typeRecord.typeContainer.findViewById(R.id.settings_dialog_edit_types_absent_active_container_edittext);
        editText.setText(typeRecord.typeName);
        // ставим ограничение в 4 символа
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});


        // кнопка удалить
        if (typeRecord.typeId != -1 && typeRecord.typeId != 1) {

            // нажатие на кнопку удалить
            typeRecord.typeContainer
                    .findViewById(R.id.settings_dialog_edit_types_absent_active_container_remove_button)
                    .setOnClickListener(v -> {

                        // вызываем в активности метод по удалению
                        boolean isDeleted = ((EditAbsentTypeDialogFragmentInterface) requireActivity()).removeAbsentType(typeRecord.typeId);
                        if (isDeleted) {
                            // удаляем переменную в скролле
                            listOut.removeView(typeRecord.typeContainer);
                            // и в списке
                            types.remove(typeRecord);

                            // обновляем все остальные контейнеры (нужно чтобы исчез подписочный серый текст)
                            updateAllContainersContent(null);
                        }
                    });
        } else {
            // убираем кнопку удалить
            ((LinearLayout) typeRecord.typeContainer
                    .findViewById(R.id.settings_dialog_edit_types_absent_active_container_buttons_container))
                    .removeView(
                            typeRecord.typeContainer
                                    .findViewById(R.id.settings_dialog_edit_types_absent_active_container_remove_button)
                    );
        }

        // нажатие на кнопку сохранить
        typeRecord.typeContainer.findViewById(R.id.settings_dialog_edit_types_absent_active_container_save_button)
                .setOnClickListener(v -> saveActiveTypeChanges(
                        typeRecord,
                        longEditText.getText().toString().trim(),
                        editText.getText().toString().trim()
                ));


        // эта строка убирает флаги(были поставлены автоматически), которые недавали открывать
        // клавиатуру на программно созданных editText
        //http://qaru.site/questions/2321163/soft-keyboard-does-not-show-up-on-edittext-recyclerview-in-a-dialogfragment
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        // показываем клавиатуру
        longEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

    }


    // нажатие кнопки сохранить в типе
    void saveActiveTypeChanges(AbsentTypeRecord typeRecord, String longName, String name) {
        // вызываем метод по сохранению названия
        boolean isChanged = false;
        // проверка на пустые поля
        if (longName.length() == 0) {
            Toast.makeText(
                    getActivity(),
                    R.string.settings_activity_dialog_new_absent_type_toast_empty,
                    Toast.LENGTH_SHORT
            ).show();
        } else if (name.length() == 0) {
            Toast.makeText(
                    getActivity(),
                    R.string.settings_activity_dialog_new_absent_type_toast_empty_short,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // если запись новая
            if (typeRecord.typeId == -1) {
                // создаем ее в бд в активности
                typeRecord.typeId = ((EditAbsentTypeDialogFragmentInterface) requireActivity()).createAbsentType(
                        name, longName
                );
                isChanged = true;

                // тип новый и все-таки создан, добавляем кнопку добавить тип внизу
                outAddTypeButton();
            } else {
                // изменяем существующую запись
                isChanged = ((EditAbsentTypeDialogFragmentInterface) requireActivity()).editAbsentType(
                        typeRecord.typeId, name, longName
                );
            }
        }

        // перевыводим содержимое
        if (isChanged) {
            // меняем переменную в списке
            typeRecord.typeName = name;
            typeRecord.longName = longName;

            // выводим текущий контейнер как неактивный
            outContentInTypeContainer(typeRecord, false, false);

            // прячем клавиатуру
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getDialog().getWindow().getDecorView().getWindowToken(), 0);
        }
    }

}

class AbsentTypeRecord {

    String typeName;
    String longName;
    long typeId;
    LinearLayout typeContainer;

    AbsentTypeRecord(long id, String name, String longName, LinearLayout typeContainer) {
        this.typeId = id;
        this.typeName = name;
        this.longName = longName;
        this.typeContainer = typeContainer;
    }

}

interface EditAbsentTypeDialogFragmentInterface {
    long createAbsentType(String name, String longName);

    boolean editAbsentType(long typeId, String newName, String newLongName);

    boolean removeAbsentType(long typeId);

    // удалять с проверкой и возвратом boolean
}