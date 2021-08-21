package com.learning.texnar13.teachersprogect.gradesPeriods;

import androidx.recyclerview.widget.RecyclerView;

public class ScrollRecyclerTableManager extends RecyclerView.LayoutManager {

    // будет ли наш RecyclerView прокручиваться по вертикали?
    @Override
    public boolean canScrollVertically() {
        return true;// Конечно будет! возвращаем true
    }

    // будет ли наш RecyclerView прокручиваться горизонтально?
    @Override
    public boolean canScrollHorizontally() {
        return true;// Конечно будет! возвращаем true
    }


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return null;
    }


}
