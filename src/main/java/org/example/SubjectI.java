package org.example;

public class SubjectI {
    private String name;
    private int[] range = new int[2];

    public SubjectI(String name, int[] range) {
        this.name = name;
        this.range = range;
    }

    public SubjectI() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void incrementEndRowCounter() {
        this.range[1]++;
    }

    public String getName() {
        return name;
    }

    public int[] getRange() {
        return range;
    }
    public boolean isEmpty(){
        return (name.isEmpty() && range.length == 0);
    }
}
