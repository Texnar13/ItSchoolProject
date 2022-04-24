package com.learning.texnar13.teachersprogect.subjectsDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.learning.texnar13.teachersprogect.R;

import java.util.ArrayList;
import java.util.Arrays;

public class SubjectsDialogFragment extends DialogFragment {

    public static final String ARGS_LEARNERS_NAMES_STRING_ARRAY = "learnersArray";

    // список предметов
    private ArrayList<String> subjectsNames;


    // на каком окне сейчас находится диалог (для кнопки назад)
    int whatWindow = 0;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        /*// todo скрывать клавиатуру везде
         *
         * todo отладить
         *  создание предмета в редакторе урока при отсутствии предметов на втором классе
         *  а затем переключение обратно на первый
         *  java.lang.ArrayIndexOutOfBoundsException: length=1; index=-1
         *  at java.util.ArrayList.get(ArrayList.java:310)
         *  at com.learning.texnar13.teachersprogect.lessonRedactor.LessonRedactorDialogFragment.getAndOutSubjects(LessonRedactorDialogFragment.java:621)
         *  at com.learning.texnar13.teachersprogect.lessonRedactor.LessonRedactorDialogFragment$6.onItemSelected(LessonRedactorDialogFragment.java:551)
         *
         * */

        // начинаем строить диалог
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // ---- layout диалога ----
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setBackgroundColor(Color.TRANSPARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        builder.setView(linearLayout);


        // нажатие кнопки назад
        builder.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP &&
                    !event.isCanceled()) {
                if (whatWindow == 0) {
                    dialog.cancel();
                } else {
                    outMainMenu(linearLayout);
                }
                return true;
            }
            return false;
        });


        // получаем список предметов
        String[] names = getArguments().getStringArray(ARGS_LEARNERS_NAMES_STRING_ARRAY);
        if (names == null)
            names = new String[0];

        subjectsNames = new ArrayList<>(names.length);
        subjectsNames.addAll(Arrays.asList(names));


        // наконец создаем диалог и возвращаем его
        Dialog dialog = builder.create();

        // в начале выводим главное меню
        outMainMenu(linearLayout);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    // метод вывода главного меню
    void outMainMenu(LinearLayout rootContainer) {
        whatWindow = 0;

        View root = getLayoutInflater().inflate(R.layout.base_subjects_dialog_select, null);
        rootContainer.removeAllViews();
        rootContainer.addView(root, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // кнопка закрытия диалога
        root.findViewById(R.id.base_subjects_dialog_select_button_close).setOnClickListener(
                view -> dismiss());// закрываем диалог

        // кнопка изменить предметы
        root.findViewById(R.id.base_subjects_dialog_select_button_change).setOnClickListener(
                view -> outEditSubjectsMenu(rootContainer));

        // кнопка добавить предмет
        root.findViewById(R.id.base_subjects_dialog_select_button_add).setOnClickListener(
                view -> outCreateSubjectMenu(rootContainer));

        // вывод предметов
        LinearLayout subjectsContainer = root.findViewById(R.id.base_subjects_dialog_select_subjects_container);
        if (subjectsNames != null)
            for (int subjectI = 0; subjectI < subjectsNames.size(); subjectI++) {

                // создаем текстовое поле с названием предмета
                TextView subjectText = new TextView(getActivity());
                subjectText.setTypeface(ResourcesCompat.getFont(requireActivity(), R.font.montserrat_semibold));
                subjectText.setText(subjectsNames.get(subjectI));
                subjectText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
                subjectText.setTextColor(getResources().getColor(R.color.text_color_simple));

                // параметры текста
                LinearLayout.LayoutParams subjectTextParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                // выставляем отступ всем кроме первого
                subjectTextParams.setMargins(
                        0, (subjectI == 0) ? (0) : (getResources().getDimensionPixelSize(R.dimen.simple_margin)),
                        0, 0
                );
                subjectsContainer.addView(subjectText, subjectTextParams);

                // при нажатии на текстовое поле
                final int finalPosition = subjectI;
                subjectText.setOnClickListener(v -> {
                    // выделяем цветом выбранный текст
                    subjectText.setTextColor(getResources().getColor(R.color.base_blue));
                    // вызываем в активности метод по выбору предмета
                    ((SubjectsDialogInterface) getActivity()).setSubjectPosition(finalPosition);
                    // закрываем диалог
                    dismiss();
                });
            }
    }

    // метод вывода интерфейса создания предмета
    void outCreateSubjectMenu(LinearLayout rootContainer) {
        whatWindow = 1;

        // чистим флаги и разрешаем показывать клавиатуру
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        View root = getLayoutInflater().inflate(R.layout.base_subjects_dialog_create, null);
        rootContainer.removeAllViews();
        rootContainer.addView(root, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // кнопка назад
        root.findViewById(R.id.base_subjects_dialog_create_button_back).setOnClickListener(
                view -> outMainMenu(rootContainer));// возвращет опять главное меню

        // кнопка создать предмет
        root.findViewById(R.id.base_subjects_dialog_create_button_save).setOnClickListener(view -> {

            // получаем текст
            String input = ((TextView) root.findViewById(R.id.base_subjects_dialog_create_field_name)).getText().toString();

            // добавляем новый предмет в лист диалога и заодно ищем позицию на которую его нужно добавить
            int position = -1;
            for (int subjectI = 0; subjectI < subjectsNames.size(); subjectI++) {
                // если полученная строка лексикографически меньше текущей в списке то ставим полученную перед текущей
                if (input.compareTo(subjectsNames.get(subjectI)) < 0) {
                    subjectsNames.add(subjectI, input);
                    position = subjectI;
                    break;
                }
            }
            // если строка больше всех других строк, помещаем в конец списка
            if (position == -1) {
                position = subjectsNames.size();
                subjectsNames.add(input);
            }

            // вызываем в активности метод по созданию предмета передаем ему на какую позицию предмет был добавлен и само название
            // todo по моему это костыль с передачей позиции
            ((SubjectsDialogInterface) getActivity()).createSubject(input, position);

            // закрываем диалог
            dismiss();
        });
    }


    // метод вывода списка удаления и редактирования
    void outEditSubjectsMenu(LinearLayout rootContainer) {
        whatWindow = 2;

        // чистим флаги и разрешаем показывать клавиатуру
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        View root = getLayoutInflater().inflate(R.layout.base_subjects_dialog_edit, null);
        rootContainer.removeAllViews();
        rootContainer.addView(root, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // кнопка назад
        root.findViewById(R.id.base_subjects_dialog_edit_button_back).setOnClickListener(
                view -> outMainMenu(rootContainer));// возвращет опять главное меню

        // кнопка удалить в заголовке
        TextView deleteButton = root.findViewById(R.id.base_subjects_dialog_edit_button_remove);

        // контейнер предметов
        LinearLayout subjectsContainer = root.findViewById(R.id.base_subjects_dialog_edit_subjects_container);


        // сохраняем промежуточные данные
        // массив EditText из которых будем брать измененные предметы
        final EditText[] editSubjectsNames = new EditText[subjectsNames.size()];
        // массив отмеченных на удаление
        final boolean[] deleteList = new boolean[subjectsNames.size()];


        // вывод предметов
        for (int subjectI = 0; subjectI < subjectsNames.size(); subjectI++) {

            // раздуваем разметку одного пункта
            LinearLayout element = (LinearLayout) getLayoutInflater().inflate(R.layout.base_subjects_dialog_edit_list_element, null);
            LinearLayout.LayoutParams elementParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            elementParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.simple_margin);
            subjectsContainer.addView(element, elementParams);


            // кнопка чтобы отмечать предметы на удаление
            ImageView deleteImage = element.findViewById(R.id.base_subjects_dialog_edit_list_element_circle);
            // начальное значение выбора к удалению
            deleteList[subjectI] = false;
            // при нажатии на кнопку
            final int finalSubjectI = subjectI;
            deleteImage.setOnClickListener(v -> {
                // инвертируем состояние переменной
                deleteList[finalSubjectI] = !deleteList[finalSubjectI];
                // и иконки
                deleteImage.setImageResource(
                        deleteList[finalSubjectI] ? // стоит ли на удаление ?
                                R.drawable.learners_and_grades_activity_abs_checkbox_background_full :
                                R.drawable.learners_and_grades_activity_abs_checkbox_background_empty
                );
//                // состояние текста удаления
//                deleteButton.setTextColor(getResources().getColor(
//                        isActiveInDeleteList(deleteList) ?
//                                R.color.text_color_inverse :
//                                R.color.text_color_not_active
//                ));
            });


            // текстовое поле с названием предмета
            editSubjectsNames[subjectI] = element.findViewById(R.id.base_subjects_dialog_edit_list_element_text);
            editSubjectsNames[subjectI].setText(subjectsNames.get(subjectI));
        }


        // нажатие на кнопку удалить в заголовке
        deleteButton.setOnClickListener(v -> {

            // выбрано ли хотя бы что-то
            if (isActiveInDeleteList(deleteList)) {


                // чистим внутренний список
                for (int subjectsI = deleteList.length - 1; subjectsI >= 0; subjectsI--) {
                    if (deleteList[subjectsI]) {
                        subjectsNames.remove(subjectsI);
                    }
                }

                // вызываем в активности метод по удалению предметов // отправляем список на удаление
                ((SubjectsDialogInterface) getActivity()).deleteSubjects(deleteList);

                // выводим главное меню
                outMainMenu(rootContainer);
            }
        });


        // нажатие на кнопку сохранить
        root.findViewById(R.id.base_subjects_dialog_edit_button_save).setOnClickListener(v -> {

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
            outMainMenu(rootContainer);
        });
    }


    // проверить boolean массив на присутствие true
    boolean isActiveInDeleteList(boolean[] deleteList) {
        for (boolean e : deleteList) if (e) return true;
        return false;
    }
}
