package com.learning.texnar13.teachersprogect.data;


import android.database.Cursor;

// класс призванный сократить код
public class SCursor {

    Cursor cursor;

    public SCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public long getInt(String name) {
        return cursor.getInt(cursor.getColumnIndex(name));
    }

    public long getLong(String name) {
        return cursor.getLong(cursor.getColumnIndex(name));
    }

    public boolean moveToNext() {
        return cursor.moveToNext();
    }

    public void close() {
        cursor.close();
    }

    public int getCount(){ return cursor.getCount(); }
}
