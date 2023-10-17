package org.example;

import java.util.ArrayList;
import java.util.List;

public class Lesson {
    private final String name; // название
    private final List<Integer> timeStart; // время начала занятия
    private final List<Integer> DayOfYear;
    private final List<LessonType> lessonType;

    public Lesson(String name, List<Integer> timeStart, List<Integer> dayOfYear) {
        this.name = name;
        this.timeStart = timeStart;
        DayOfYear = dayOfYear;
        this.lessonType = new ArrayList<>();
    }

//    public Lesson(String name, List<Integer> timeStart, List<Integer> dayOfYear) {
//        this.name = name;
//        this.timeStart = timeStart;
//        DayOfYear = dayOfYear;
//    }

    public List<LessonType> getLessonType() {
        return lessonType;
    }

//    public void setLessonType(LessonType lessonType) {
//        this.lessonType = lessonType;
//    }
    public void addLessonType(LessonType Type) {
        lessonType.add(Type);
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