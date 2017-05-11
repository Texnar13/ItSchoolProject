package com.learning.texnar13.teachersprogect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.learning.texnar13.teachersprogect.data.SchoolContract;

public class RedactorActivity extends AppCompatActivity {

    public static final String EDITED_OBJECT = "editedObject";//тип редактируемого обьекта
    public static final String EDITED_OBJECT_ID = "id";//ID редактируемого обьекта
    public static final String OBJECT_NEW = "new";//обьект новый, константа используется в качестве ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redactor);
        RelativeLayout out = (RelativeLayout) findViewById(R.id.redactor_out);


        switch (getIntent().getStringExtra(EDITED_OBJECT)){
            case SchoolContract.TableClasses.NAME_TABLE_CLASSES:
                if(getIntent().getStringExtra(EDITED_OBJECT_ID).equals(OBJECT_NEW)){//если условие истинно, то мы хотим создать новый класс иначе достаём его по id


                   // out.addView();
                }else{

                }

                break;
            //case:

                //break;
            //case:

                //break;
        }

    }
}
