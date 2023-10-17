package org.example;

import java.util.ArrayList;
import java.util.List;

public class Lesson {
    private final String name; // Название предмета
    private final List<Integer> timeStart; // Время начала занятия
    private final List<Integer> DayOfYear; // Номер дня в году
    private final List<LessonType> lessonType; // Тип урока [Онлайн занятие, Лекция, Занятия для 1 группы, Занятия для 2 группы]

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