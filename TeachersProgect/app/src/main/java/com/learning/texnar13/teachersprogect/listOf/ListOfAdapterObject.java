package com.learning.texnar13.teachersprogect.listOf;



public class ListOfAdapterObject {
    private String objName;
    private String objType;
    private long objId;
    private boolean isChecked;



    public ListOfAdapterObject(String name, String type, long id) {
        this.objName = name;
        this.objType = type;
        this.objId = id;
        isChecked = false;

    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getobjName() {
        return objName;
    }

    public void setobjName(String name) {
        this.objName = name;
    }

    public String getobjType() {
        return objType;
    }

    public void setobjType(String type) {
        this.objType = type;
    }

    public long getobjId() {
        return objId;
    }

    public void setobjId(long id) {
        this.objId = id;
    }
}
