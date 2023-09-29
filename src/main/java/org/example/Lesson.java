package org.example;

import java.util.List;

public class Lesson {
    private final String name; // название
    private List<Integer> timeStart; // время начала занятия
    private List<Integer> DayOfYear;

    public Lesson(String name, List<Integer> timeStart, List<Integer> dayOfYear) {
        this.name = name;
        this.timeStart = timeStart;
        DayOfYear = dayOfYear;
    }

    public Lesson(String name) {
        this.name = name;
    }

    public void addDayOfYear(int day) {
        DayOfYear.add(day);
    }

    public void addTimeStart(int Time) {
        timeStart.add(Time);
    }

    public List<Integer> getDayOfYear() {
        return DayOfYear;
    }

    public String getName() {
        return this.name;
    }

    public List<Integer> getTimeStart() {
        return timeStart;
    }
}