package com.learning.texnar13.teachersprogect.gradesPeriods;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.learning.texnar13.teachersprogect.R;

import java.util.Random;

public class ScrollRecyclerTableAdapter extends
        RecyclerView.Adapter<ScrollRecyclerTableAdapter.SimpleTextHolder> {

    // типы содержимого ячеек
    private static final int ELEMENT_TYPE_LEFT_TOP_CORNER = 0;
    private static final int ELEMENT_TYPE_LEFT_HEAD = 1;
    private static final int ELEMENT_TYPE_TOP_HEAD = 2;
    private static final int ELEMENT_TYPE_CONTENT = 3;


    // temp
    int columnsCount = 20;

    // Data data;
    Random r = new Random();
    private Context context;


    ScrollRecyclerTableAdapter(Context context) {
        this.context = context;
    }

    // определяем тип ячейки по ее позиции
    @Override
    public int getItemViewType(int position) {

        if (position == 0)
            return ELEMENT_TYPE_LEFT_TOP_CORNER;
        if (position < columnsCount)
            return ELEMENT_TYPE_TOP_HEAD;
        if (position % columnsCount == 0)
            return ELEMENT_TYPE_LEFT_HEAD;

        return ELEMENT_TYPE_CONTENT;
    }

    // создаем разметку, которая будет лежать в одном контейнере списка
    @NonNull
    @Override
    public SimpleTextHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // todo сейчас высота/ширина контейнеров в таблице расчитываепо первому элементу строки/столбца
        //  надо исправить на динамическую
        //  или несовсем.. там вообще какой-то странный принцип

        TextView text = new TextView(context);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.text_simple_size));
        text.setPadding(50, 50, 50, 50);
        text.setGravity(Gravity.CENTER);
        text.setBackgroundColor(context.getResources().getColor(
                (viewType == ELEMENT_TYPE_CONTENT) ? (R.color.baseBlue) : (R.color.baseOrange)
        ));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.setMargins(10, 10, 10, 10);

        LinearLayout container = new LinearLayout(context);
        container.addView(text, textParams);
        container.setBackgroundColor(Color.argb(
                255, r.nextInt(254), r.nextInt(254), r.nextInt(254)));


        return new SimpleTextHolder(container, text, viewType);
    }

    // заполняем разметку созданную в onCreateViewHolder данными
    @Override
    public void onBindViewHolder(@NonNull SimpleTextHolder holder, int position) {
        if (position / 20 % 2 != 0) {
            holder.textView.setText(position + "(" + holder.viewType + ")");
        } else {
            holder.textView.setText("---" + position + "---(" + holder.viewType + ")");

        }
    }

    // возвращает количество объектов в списке
    @Override
    public int getItemCount() {
        return 400;
    }


    public static class SimpleTextHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final int viewType;

        public SimpleTextHolder(View root, TextView text, int viewType) {
            super(root);
            textView = text;
            this.viewType = viewType;
        }
    }

}
