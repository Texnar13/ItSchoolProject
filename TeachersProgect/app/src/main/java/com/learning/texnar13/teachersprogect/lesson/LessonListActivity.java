package com.learning.texnar13.teachersprogect.lesson;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.learning.texnar13.teachersprogect.R;
import com.learning.texnar13.teachersprogect.data.DataBaseOpenHelper;
import com.learning.texnar13.teachersprogect.data.SchoolContract;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class LessonListActivity extends AppCompatActivity implements GradesDialogInterface, EndLessonInterface {

    public static final String LESSON_TIME = "startTime";
    public static final String SUBJECT_ID = "subjectId";
    public static final String ARGS_STRING_ARRAY_LEARNERS_NAMES = "learnersNames";
    public static final String ARGS_INT_ARRAY_LEARNERS_ID = "learnersID";
    public static final String STRING_GRADES_TYPES_ARRAY = "gradesTypesArray";
    public static final String INT_GRADES_TYPES_ID_ARRAY = "gradesTypesIdArray";
    public static final String INT_MAX_GRADE = "maxGrade";

    public static final String FIRST_GRADES = "grades1";
    public static final String SECOND_GRADES = "grades2";
    public static final String THIRD_GRADES = "grades3";
    public static final String FIRST_GRADES_TYPES = "gradesTypes1";
    public static final String SECOND_GRADES_TYPES = "gradesTypes2";
    public static final String THIRD_GRADES_TYPES = "gradesTypes3";

    public static final String ADD_BANNER = "AdBanner";

//    public static final String LIST_ID = "listId";


    // номер выбранного ученика
    int chosenLearnerPoz;


    // тост выводящийся после сохранения оценок
    Toast toast;


    // ---------
    // массив учеников и их оценок
    //static LearnerAndHisGrades[] learnerAndHisGrades;

    // id предмета
    static long subjectId;

    // получаем учеников
    static long[] learnersId;
    static String[] learnersNames;

    // массив с TextView учеников
    static TextView[] learnersGradesTexts;

    // получаем массив предметов
    static long[] gradesTypesId;
    static String[] GradesTypesNames;


    // получаем размер максимальной оценки
    static int maxGrade;

    // получаем массивы оценок и выбранных типов
    static int[][] grades;
    static int[][] gradesTypesIndexes;


    // межстраничный баннер открывающийся при сохранении оценок
    //InterstitialAd lessonEndBanner;


    // ---------


    // инициализация меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lesson_list_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.lesson_list_menu_save).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.i("TeachersApp", "LessonListActivity - onPrepareOptionsMenu - save");

                DataBaseOpenHelper db = new DataBaseOpenHelper(getApplicationContext());

                // сохраняем оценки в бд
                for (int learnerI = 0; learnerI < learnersId.length; learnerI++) {

                    // н создаем только одну
                    if (grades[learnerI][0] == -2) {
                        db.createGrade(
                                learnersId[learnerI],
                                grades[learnerI][0],
                                gradesTypesId[0],
                                subjectId,
                                getIntent().getStringExtra(LESSON_TIME)
                        );
                    } else
                        for (int gradeI = 0; gradeI < 3; gradeI++) {
                            if (grades[learnerI][gradeI] != 0) {
                                db.createGrade(
                                        learnersId[learnerI],
                                        grades[learnerI][gradeI],
                                        gradesTypesId[gradesTypesIndexes[learnerI][gradeI]],
                                        subjectId,
                                        getIntent().getStringExtra(LESSON_TIME)
                                );
                            }
                        }
                    toast.show();
                }
                db.close();
                finish();


//                // выводим рекламу при закрытии активности конца урока
//                if (lessonEndBanner != null)
//                    if (lessonEndBanner.isLoaded()) {
//                        lessonEndBanner.show();
//                    }


                //очистка данных
                learnersId = null;
                return true;
            }
        });

        return true;
    }


    // создание активности
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        if (getIntent().getExtras() != null) {
//            // полуаем контейнер с данными
//            EndLessonIntentContainer intentContainer = (EndLessonIntentContainer) getIntent().getExtras().getSerializable(ADD_BANNER);
//            if (intentContainer != null) {
//                // полуаем рекламмный блок из контейнера
//                //lessonEndBanner = intentContainer.addBanner;
//            } else lessonEndBanner = null;
//        }


        // базовый скролящийся контейнер
        ScrollView scrollView = new ScrollView(this);
        setContentView(scrollView);

        // поле вывода данных
        LinearLayout table = new LinearLayout(this);
        table.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(table, ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);


        // перед получением данных убеждаемся, что еще не получили их
        if (learnersId == null) {

            // todo будет время рассортируй по классам :)
            // получаем данные из intent
            subjectId = getIntent().getLongExtra(SUBJECT_ID, -1);

            // получаем учеников
            learnersId = getIntent().getLongArrayExtra(ARGS_INT_ARRAY_LEARNERS_ID);
            learnersNames = getIntent().getStringArrayExtra(ARGS_STRING_ARRAY_LEARNERS_NAMES);
            // убираем выбор с ученика
            chosenLearnerPoz = -1;

            // массив с TextView учеников
            learnersGradesTexts = new TextView[learnersId.length];

            // получаем массив предметов
            gradesTypesId = getIntent().getLongArrayExtra(INT_GRADES_TYPES_ID_ARRAY);
            GradesTypesNames = getIntent().getStringArrayExtra(STRING_GRADES_TYPES_ARRAY);


            // получаем размер максимальной оценки
            maxGrade = getIntent().getIntExtra(INT_MAX_GRADE, -1);

            // получаем массивы оценок и выбранных типов
            grades = new int[learnersId.length][3];
            gradesTypesIndexes = new int[learnersId.length][3];
            {
                int[] grades1 = getIntent().getIntArrayExtra(FIRST_GRADES);
                int[] grades2 = getIntent().getIntArrayExtra(SECOND_GRADES);
                int[] grades3 = getIntent().getIntArrayExtra(THIRD_GRADES);

                int[] gradesTypes1 = getIntent().getIntArrayExtra(FIRST_GRADES_TYPES);
                int[] gradesTypes2 = getIntent().getIntArrayExtra(SECOND_GRADES_TYPES);
                int[] gradesTypes3 = getIntent().getIntArrayExtra(THIRD_GRADES_TYPES);

                for (int learnerI = 0; learnerI < learnersId.length; learnerI++) {
                    grades[learnerI][0] = grades1[learnerI];
                    grades[learnerI][1] = grades2[learnerI];
                    grades[learnerI][2] = grades3[learnerI];

                    gradesTypesIndexes[learnerI][0] = gradesTypes1[learnerI];
                    gradesTypesIndexes[learnerI][1] = gradesTypes2[learnerI];
                    gradesTypesIndexes[learnerI][2] = gradesTypes3[learnerI];
                }
            }
        }

        // выводим учеников и их оценки
        for (int learnersI = 0; learnersI < learnersId.length; learnersI++) {

            // контейнер ученика
            LinearLayout learnerRow = new LinearLayout(this);
            learnerRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams learnerRowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            learnerRowParams.leftMargin = pxFromDp(10);
            learnerRowParams.topMargin = pxFromDp(10);
            learnerRowParams.rightMargin = pxFromDp(10);
            learnerRowParams.bottomMargin = pxFromDp(10);
            table.addView(learnerRow, learnerRowParams);


            // текст имени ученика
            TextView learnerText = new TextView(this);
            learnerText.setSingleLine(true);
            learnerText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
            learnerText.setText(learnersNames[learnersI]);
            learnerText.setTextColor(Color.BLACK);
            learnerText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            LinearLayout.LayoutParams learnerTextParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    4F
            );
            learnerTextParams.rightMargin = pxFromDp(10);
            learnerRow.addView(learnerText, learnerTextParams);

            // текст с оценками
            TextView learnerGradesText = new TextView(this);
            learnersGradesTexts[learnersI] = learnerGradesText;
            learnerGradesText.setGravity(Gravity.CENTER);
            learnerGradesText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));

            // выводим оценки
            StringBuilder gradeText = new StringBuilder();
            if (grades[learnersI][0] == 0 && grades[learnersI][1] == 0 && grades[learnersI][2] == 0) {
                gradeText.append('-');
            } else if (grades[learnersI][0] == -2) {
                gradeText.append(getResources().getString(R.string.learners_and_grades_out_activity_title_grade_n));
            } else {
                if (grades[learnersI][0] != 0) {
                    gradeText.append(grades[learnersI][0]);
                    if (grades[learnersI][1] != 0 || grades[learnersI][2] != 0) {
                        gradeText.append(' ');
                    }
                }
                if (grades[learnersI][1] != 0) {
                    gradeText.append(grades[learnersI][1]);

                    if (grades[learnersI][2] != 0) {
                        gradeText.append(' ');
                    }
                }
                if (grades[learnersI][2] != 0) {
                    gradeText.append(grades[learnersI][2]);
                }
            }
            learnerGradesText.setText(gradeText.toString());
            learnerGradesText.setTextColor(Color.BLACK);
            learnerGradesText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
            LinearLayout.LayoutParams learnerGradesTextParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    2F
            );
            learnerRow.addView(learnerGradesText, learnerGradesTextParams);

            // при нажатии на контейнер
            final int finalLearnersI = learnersI;
            learnerRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // ставим этого ученика как выбранного
                    chosenLearnerPoz = finalLearnersI;

                    // вызываем диалог оценок
                    GradeDialogFragment gradeDialogFragment = new GradeDialogFragment();
                    // выводим аргументы
                    Bundle args = new Bundle();
                    args.putString(GradeDialogFragment.ARGS_LEARNER_NAME, learnersNames[finalLearnersI]);
                    args.putStringArray(GradeDialogFragment.ARGS_STRING_GRADES_TYPES_ARRAY, GradesTypesNames.clone());
                    args.putIntArray(GradeDialogFragment.ARGS_INT_GRADES_ARRAY, grades[finalLearnersI].clone());
                    args.putInt(GradeDialogFragment.ARGS_INT_MAX_GRADE, maxGrade);
                    args.putIntArray(GradeDialogFragment.ARGS_INT_GRADES_TYPES_CHOSEN_NUMBERS_ARRAY, gradesTypesIndexes[finalLearnersI].clone());
                    args.putInt(GradeDialogFragment.ARGS_INT_CHOSEN_GRADE_POSITION, -1);
                    gradeDialogFragment.setArguments(args);
                    // показываем диалог
                    gradeDialogFragment.show(getFragmentManager(), "gradeDialogFragment - Hello");
                }
            });
        }

        // подсказка в низу
        TextView helpText = new TextView(this);
        helpText.setTypeface(ResourcesCompat.getFont(this, R.font.geometria_family));
        helpText.setTextColor(getResources().getColor(R.color.backgroundGray));
        helpText.setGravity(Gravity.CENTER);
        helpText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_simple_size));
        helpText.setText(R.string.lesson_list_activity_text_help);
        //параметры
        LinearLayout.LayoutParams helpTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        helpTextParams.setMargins(pxFromDp(20), pxFromDp(10), pxFromDp(20), pxFromDp(10));
        table.addView(helpText, helpTextParams);

        // тост говорящий, что оценки сохранены
        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.lesson_list_activity_menu_toast_grades_saved), Toast.LENGTH_SHORT);


//        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
//        // получаем зависимость
//        Cursor attitudeCursor = db.getSubjectAndTimeCabinetAttitudeById(attitudeId);
//        attitudeCursor.moveToFirst();
//        long subjectId = attitudeCursor.getLong(attitudeCursor.getColumnIndex(SchoolContract.TableSubjectAndTimeCabinetAttitude.KEY_SUBJECT_ID));
//        attitudeCursor.close();
//        // получаем предмет
//        Cursor subjectCursor = db.getSubjectById(subjectId);
//        subjectCursor.moveToFirst();
//        long classId = subjectCursor.getLong(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.KEY_CLASS_ID));
//        String subjectName = subjectCursor.getString(subjectCursor.getColumnIndex(SchoolContract.TableSubjects.COLUMN_NAME));
//        subjectCursor.close();
//
//        // получаем учеников
//        Cursor learnersCursor = db.getLearnersByClassId(classId);
//        learnersCursor.close();
//        db.close();


//        // проверка на заполнение
//        if () {
////----
//
//
////--массив с учениками и оценками--
//
//            //id учеников из базы
//            long[] learnersIdArray = getIntent().getLongArrayExtra(LIST_ID);
//
//            //инициализация
//            learnersGrades = new LearnerGradeUnit[learnersIdArray.length];
//
//            //оценки
//            long gradeArray0[] = getIntent().getLongArrayExtra(FIRST_LIST_GRADES);
//            long gradeArray1[] = getIntent().getLongArrayExtra(SECOND_LIST_GRADES);
//            long gradeArray2[] = getIntent().getLongArrayExtra(THIRD_LIST_GRADES);
//
//            for (int i = 0; i < learnersIdArray.length; i++) {
//                //помещаем id
//                learnersGrades[i] = new LearnerGradeUnit(learnersIdArray[i]);
//                //помещаем оценки
//                learnersGrades[i].grades = new long[3];
//                learnersGrades[i].grades[0] = gradeArray0[i];
//                learnersGrades[i].grades[1] = gradeArray1[i];
//                learnersGrades[i].grades[2] = gradeArray2[i];
//            }
//        }
    }

//    // пробуждение активности
//    @Override
//    protected void onStart() {
//
//        // вывод оценок
//
//        //чистим поле
//        list.removeAllViews();
//
//        DataBaseOpenHelper db = new DataBaseOpenHelper(this);
//        boolean flag = false;
////--------идем по ученикам--------
//        for (int i = 0; i < learnersGrades.length; i++) {
////----контейнер имя оценка----
//            LinearLayout tempLayout = new LinearLayout(getApplicationContext());
//
////----текстовое поле с именем ученика----
//            //создаем
//            TextView textViewWithName = new TextView(getApplicationContext());
//            textViewWithName.setGravity(Gravity.LEFT);
//            textViewWithName.setTextColor(Color.BLACK);
//
//            //раскрашиваем
//            if (flag) {
//                //textViewWithName.setBackgroundColor(Color.parseColor("#fbe2e7"));
//            }
//            textViewWithName.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
////---достаем имя ученика---
//            Cursor currentLearner = db.getLearner(learnersGrades[i].learnerId);
//            currentLearner.moveToFirst();
//            //ставим его
//            textViewWithName.setText(
//                    currentLearner.getString(currentLearner.getColumnIndex(
//                            SchoolContract.TableLearners.COLUMN_SECOND_NAME
//                    )) + " " + currentLearner.getString(currentLearner.getColumnIndex(
//                            SchoolContract.TableLearners.COLUMN_FIRST_NAME
//                    ))
//            );
//            currentLearner.close();
//            LinearLayout.LayoutParams textViewWithNameParams = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    1F
//            );
//            textViewWithNameParams.leftMargin = (int)pxFromDp(10);
////----помещаем текст с именем ученика в контейнер----
//            tempLayout.addView(
//                    textViewWithName,textViewWithNameParams
//
//            );
//
////--------пробегаемся по его оценкам--------
//            //обьявляем
//            learnersGrades[i].view = new View[learnersGrades[i].grades.length];
//            for (int j = 0; j < learnersGrades[i].grades.length; j++) {
//                //создаем текстовое поле с оценкой
//                TextView textViewWisGrade = new TextView(getApplicationContext());
//                textViewWisGrade.setTextColor(Color.BLACK);
//
//                //добавляем текстовое поле в массив
//                learnersGrades[i].view[j] = textViewWisGrade;
//
//                //раскрашиваем
//                if (!flag) {
//                    //textViewWisGrade.setBackgroundColor(Color.parseColor("#fbe2e7"));
//                    flag = true;
//                } else {
//                    flag = false;
//                }
//                textViewWisGrade.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
//                textViewWisGrade.setGravity(Gravity.CENTER_HORIZONTAL);
////----ставим в поле оценку----
//                if (learnersGrades[i].grades[j] == 0) {
//                    textViewWisGrade.setText("-");
//                } else {
//                    if (learnersGrades[i].grades[j] != -2) {
//                        textViewWisGrade.setText(Long.toString(learnersGrades[i].grades[j]));
//                    } else {
//                        textViewWisGrade.setText(getResources().getString(R.string.learners_and_grades_out_activity_title_grade_n));
//                    }
//                }
////-------задаем действие при нажатии на оценку-------
//                final int finalI = i;
//                final int finalJ = j;
//                textViewWisGrade.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //передаваемые данные
//                        Bundle bundle = new Bundle();
//                        bundle.putInt(LessonListEditDialogFragment.LEARNER_NUMBER, finalI);
//                        bundle.putInt(LessonListEditDialogFragment.GRADE_NUMBER, finalJ);
//                        bundle.putInt(LessonListEditDialogFragment.GRADE, (int) learnersGrades[finalI].grades[finalJ]);
//                        //диалог
//                        LessonListEditDialogFragment editDialog = new LessonListEditDialogFragment();
//                        editDialog.setArguments(bundle);
//                        editDialog.show(getFragmentManager(), "editGradeDialog");
//                    }
//                });
//
//
//
//
//
////-------помещаем текст с оценкой в контейнер-------
//                tempLayout.addView(
//                        textViewWisGrade,
//                        new LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                4F
//                        )
//                );
//            }
//
////-------помещаем контейнер в список-------
//            list.addView(tempLayout);
////---подчеркивание---
//            LinearLayout linearLayout = new LinearLayout(this);
//            linearLayout.setBackgroundResource(R.drawable.line_gray);
//
//            list.addView(linearLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)pxFromDp(2)));
//        }
//
////-------подсказка textView в низу-------
//        //текст
//        TextView textView = new TextView(this);
//        textView.setTextColor(Color.GRAY);
//        textView.setGravity(Gravity.CENTER);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_subtitle_size));
//        textView.setText(R.string.lesson_list_activity_text_help);
//        //параметры
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//        );
//        params.setMargins((int) pxFromDp(20), (int) pxFromDp(10), (int) pxFromDp(20), (int) pxFromDp(10));
//
//        list.addView(textView, params);
//
//
//        db.close();
//
//        super.onStart();
//    }

    // обратная связь от диалога оценок GradeDialogFragment
    @Override
    public void setGrades(int[] newGrades, int[] chosenTypesNumbers) {

        // меняем массивы
        grades[chosenLearnerPoz] = newGrades;
        gradesTypesIndexes[chosenLearnerPoz] = chosenTypesNumbers;

        // меняем текст оценки выбранного ученика
        StringBuilder gradeText = new StringBuilder();
        if (grades[chosenLearnerPoz][0] == 0 && grades[chosenLearnerPoz][1] == 0 && grades[chosenLearnerPoz][2] == 0) {
            gradeText.append('-');
        } else if (grades[chosenLearnerPoz][0] == -2) {
            gradeText.append(getResources().getString(R.string.learners_and_grades_out_activity_title_grade_n));
        } else {
            if (grades[chosenLearnerPoz][0] != 0) {
                gradeText.append(grades[chosenLearnerPoz][0]);
                if (grades[chosenLearnerPoz][1] != 0 || grades[chosenLearnerPoz][2] != 0) {
                    gradeText.append(' ');
                }
            }
            if (grades[chosenLearnerPoz][1] != 0) {
                gradeText.append(grades[chosenLearnerPoz][1]);

                if (grades[chosenLearnerPoz][2] != 0) {
                    gradeText.append(' ');
                }
            }
            if (grades[chosenLearnerPoz][2] != 0) {
                gradeText.append(grades[chosenLearnerPoz][2]);
            }
        }
        learnersGradesTexts[chosenLearnerPoz].setText(gradeText.toString());

        // убираем выбор с ученика
        chosenLearnerPoz = -1;
    }

    @Override
    public void onBackPressed() {
        // показываем диалог подтверждения выхода из активности
        EndLessonDialogFragment endLessonDialogFragment = new EndLessonDialogFragment();
        endLessonDialogFragment.show(getSupportFragmentManager(), "EndLesson - Hello");
    }

    // обратная связь от диалога EndLessonDialogFragment
    @Override
    public void endLesson() {

        // очистка данных
        learnersId = null;

        // выходим из активности
        finish();
    }

    private int pxFromDp(float px) {
        return (int) (px * getResources().getDisplayMetrics().density);
    }

    // класс для передачи данных в эту активность
    static class EndLessonIntentContainer implements Serializable {
        //InterstitialAd addBanner;
    }


//    // класс для хранения типов ответов
//    class AnswersType {
//        long id;
//        String typeName;
//
//        public AnswersType(long id, String typeName) {
//            this.id = id;
//            this.typeName = typeName;
//        }
//    }
//
//    // класс с учеником
//    class LearnerAndHisGrades {
//
//        // параметры ученика
//        long learnerId;
//
//        // массив оценок
//        int[] learnerGrades;
//        // массив номеров типов оценок
//        int[] learnerGradesTypes;
//
//        // текстовое поле с оценками ученика
//        TextView text;
//
//        public LearnerAndHisGrades(long learnerId, int[] learnerGrades, int[] learnerGradesTypes, TextView text) {
//            this.learnerId = learnerId;
//            this.learnerGrades = learnerGrades;
//            this.learnerGradesTypes = learnerGradesTypes;
//            this.text = text;
//        }
//    }

}


