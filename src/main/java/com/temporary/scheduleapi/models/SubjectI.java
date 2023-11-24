package com.temporary.scheduleapi.models;

import lombok.Getter;

@Getter
public class SubjectI {
    private String name;
    private int subgroupNumber;
    private int[] range = new int[2];

    public SubjectI(String name, int[] range) {
        this.name = name;
        this.range = range;
    }

    public SubjectI(String name, int subgroupNumber, int[] range) {
        this.name = name;
        this.subgroupNumber = subgroupNumber;
        this.range = range;
    }

    public SubjectI() {
    }

    public void incrementEndRowCounter() {
        this.range[1]++;
    }

    public boolean isEmpty() {
        return (name.isEmpty() && range.length == 0);
    }
}

