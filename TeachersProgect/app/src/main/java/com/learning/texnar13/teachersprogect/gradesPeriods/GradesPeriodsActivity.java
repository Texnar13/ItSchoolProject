package com.learning.texnar13.teachersprogect.gradesPeriods;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.learning.texnar13.teachersprogect.R;

import java.util.LinkedList;

public class GradesPeriodsActivity extends AppCompatActivity {

    // todo точно не помню что за классы в этом пакете,
    //  но вроде как они создавались для того, чтобы сделать новую активность статистики
    //
    // todo или это для импорта данных... надо разобраться

    GradesPeriodsDataModel data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades_periods);

        // получаем данные из бд
        if (data == null) loadDataFromDB();

        RecyclerView recyclerTable = findViewById(R.id.activity_grades_periods_recycler);

        // о расположении контейнеров относительно друг друга
        RecyclerViewScrollGridLayoutManager layoutManager = new RecyclerViewScrollGridLayoutManager();
        layoutManager.setTotalColumnCount(20);
        recyclerTable.setLayoutManager(layoutManager);

        // о содержимом контейнеров
        recyclerTable.setAdapter(new ScrollRecyclerTableAdapter(this));


        recyclerTable.addItemDecoration(new ItemOffsetDecoration());

        //MergeAdapter

    }


    // получаем данные из бд
    void loadDataFromDB() {

        // получаем класс и предмет
        GradesPeriodsDataModel.SubjectAndClassUnit subjectAndClass =
                new GradesPeriodsDataModel.SubjectAndClassUnit("Algebra", 121, "7B", 14);

        // получаем учеников
        GradesPeriodsDataModel.LearnerUnit[] learners = new GradesPeriodsDataModel.LearnerUnit[15];
        {
            learners[0] = new GradesPeriodsDataModel.LearnerUnit("Иванов АА", 110);
            learners[1] = new GradesPeriodsDataModel.LearnerUnit("Петров АА", 111);
            learners[2] = new GradesPeriodsDataModel.LearnerUnit("Сидоров АА", 112);
            learners[3] = new GradesPeriodsDataModel.LearnerUnit("Григорьев АА", 113);
            learners[4] = new GradesPeriodsDataModel.LearnerUnit("Шпагин АА", 114);
            learners[5] = new GradesPeriodsDataModel.LearnerUnit("Терентьев АА", 115);
            learners[6] = new GradesPeriodsDataModel.LearnerUnit("Прокофьев АА", 116);
            learners[7] = new GradesPeriodsDataModel.LearnerUnit("Пологов АА", 117);
            learners[8] = new GradesPeriodsDataModel.LearnerUnit("Петрогова АА", 118);
            learners[9] = new GradesPeriodsDataModel.LearnerUnit("Городонова АА", 119);
            learners[10] = new GradesPeriodsDataModel.LearnerUnit("Гарговагардовна АА", 120);
            learners[11] = new GradesPeriodsDataModel.LearnerUnit("Пуприн АА", 121);
            learners[12] = new GradesPeriodsDataModel.LearnerUnit("Сузев АА", 122);
            learners[13] = new GradesPeriodsDataModel.LearnerUnit("Суховский АА", 123);
            learners[14] = new GradesPeriodsDataModel.LearnerUnit("Фроунов АА", 124);
        }

        // получаем периоды времени и считаем среднюю оценку
        LinkedList<GradesPeriodsDataModel.StudyPeriod> studyPeriods = new LinkedList<>();

        GradesPeriodsDataModel.GradeUnit[] gradeUnits = new GradesPeriodsDataModel.GradeUnit[15];
        gradeUnits[0] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits[1] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits[2] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits[3] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits[4] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        gradeUnits[5] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits[6] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits[7] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits[8] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits[9] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        gradeUnits[10] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits[11] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits[12] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits[13] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits[14] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        studyPeriods.add(new GradesPeriodsDataModel.StudyPeriod("01.07.2021", "20.07.2021", "1-я четверть", gradeUnits));
        GradesPeriodsDataModel.GradeUnit[] gradeUnits2 = new GradesPeriodsDataModel.GradeUnit[15];
        gradeUnits2[0] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits2[1] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits2[2] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits2[3] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits2[4] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        gradeUnits2[5] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits2[6] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits2[7] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits2[8] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits2[9] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        gradeUnits2[10] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits2[11] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits2[12] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits2[13] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits2[14] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        studyPeriods.add(new GradesPeriodsDataModel.StudyPeriod("02.07.2021", "20.08.2021", "2-я четверть", gradeUnits2));
        GradesPeriodsDataModel.GradeUnit[] gradeUnits3 = new GradesPeriodsDataModel.GradeUnit[15];
        gradeUnits3[0] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits3[1] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits3[2] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits3[3] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits3[4] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        gradeUnits3[5] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits3[6] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits3[7] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits3[8] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits3[9] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        gradeUnits3[10] = new GradesPeriodsDataModel.GradeUnit(1, 10, 2);
        gradeUnits3[11] = new GradesPeriodsDataModel.GradeUnit(5, 1, 2);
        gradeUnits3[12] = new GradesPeriodsDataModel.GradeUnit(4, 0, 3);
        gradeUnits3[13] = new GradesPeriodsDataModel.GradeUnit(2, 2, 5);
        gradeUnits3[14] = new GradesPeriodsDataModel.GradeUnit(3, 4, 4);
        studyPeriods.add(new GradesPeriodsDataModel.StudyPeriod("02.08.2021", "21.10.2021", "3-я четверть", gradeUnits3));

        data = new GradesPeriodsDataModel(subjectAndClass, learners, studyPeriods);
    }


    static class ItemOffsetDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            if (parent.getChildAdapterPosition(view) <= 5) {
                outRect.right = 10;
                outRect.left = 5;
                outRect.top = 15;
                outRect.bottom = 20;
            }
        }
    }
}

/*
* как делать разноцветныйтекст:
* TextView textView = (TextView) findViewById(R.id.test_id);
        Spannable mySpanString = new SpannableString("3 4 5");
        mySpanString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.baseBlue)),
                0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mySpanString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.baseOrange)),
                2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mySpanString.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.baseGreen)),
                4, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(mySpanString);
        // или через textView.append(mySpanString);
*
* */