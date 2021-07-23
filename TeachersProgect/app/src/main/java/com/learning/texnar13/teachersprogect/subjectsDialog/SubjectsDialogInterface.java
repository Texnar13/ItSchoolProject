package com.learning.texnar13.teachersprogect.subjectsDialog;

public interface SubjectsDialogInterface {

    // установить предмет стоящий на этой позиции как выбранный
    void setSubjectPosition(int position);

    // создать предмет
    void createSubject(String name, int position);

    // удалить предмет
    void deleteSubjects(boolean[] deleteList);

    // переименовать предметы
    void renameSubjects(String[] newSubjectsNames);


}
