package com.learning.texnar13.teachersprogect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class CabinetRedactorActivity extends AppCompatActivity {

    public static final String EDITED_OBJECT_TYPE = "editedObject";//тип редактируемого обьекта
    public static final String EDITED_OBJECT_ID = "id";//ID редактируемого обьекта

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cabinet_redactor);
        RelativeLayout out = (RelativeLayout) findViewById(R.id.redactor_out);
        //TODO 2 это редактор кабинетов, пока понадобятся только двухместные парты,
        /* выводим все парты находящиеся в этом кабинете
        * при нажатии на картинку "плюс" в центре экрана появляется парта,
        * которую перетаскиванием можно установить на нужное место
        * парту можно удалить перетащив на картинку "корзина" или долгим нажатием, незнаю как будет удобнее реализовать
        *    _________________________________
        *   |                             сохр|
        *   |_________________________________|
        *   |   _                             |
        *   |  |К|                       +    |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |             новая               |
        *   |             _______             |
        *   |            |   |   |            |
        *   |            |___|___|            |
        *   |                                 |
        *   |                                 |
        *   |    _______                      |
        *   |   |   |   |                     |
        *   |   |___|___|                     |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |                                 |
        *   |_________________________________|
        *
        * в меню есть кнопка сохранить, если какие-то парты удалениы они удаляются из таблицы и добавленные парты
        добавляются в таблицу + обязательно добавляются два новых места(если парта двухместная)
        *
        *при удалении парты каскадом удаляются все места ссылающиеся на неё и все зависимости ученик - место, вверх удаление не идёт
        *
        * -----------------------------------------------------------------------
        */
    }
}