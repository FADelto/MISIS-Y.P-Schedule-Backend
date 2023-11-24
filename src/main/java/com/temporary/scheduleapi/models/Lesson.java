package com.temporary.scheduleapi.models;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Getter
    private String name; // Название предмета

    @Getter
    private int subgroupNumber; // Номер подгруппы для английского

    @Getter
    private int timeStart; // Время начала занятия

    @Getter
    private int dayOfYear; // Номер дня в году

    @Getter
    @Enumerated(EnumType.ORDINAL)
    private LessonType lessonType; // Тип урока [Онлайн занятие, Лекция] Для технологий программирования

    public Lesson(String name, int subgroupNumber, int timeStart, int dayOfYear, LessonType lessonType) {
        this.name = name;
        this.subgroupNumber = subgroupNumber;
        this.timeStart = timeStart;
        this.dayOfYear = dayOfYear;
        this.lessonType = lessonType;
    }

    public Lesson() {

    }

}
