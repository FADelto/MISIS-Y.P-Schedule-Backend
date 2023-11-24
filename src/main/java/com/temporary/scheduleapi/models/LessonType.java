package com.temporary.scheduleapi.models;

import com.google.api.services.sheets.v4.model.Color;

import java.util.Collection;
import java.util.Collections;

public enum LessonType {
    ONLINE(new Color().setRed(0.8509804f).setGreen(0.91764706f).setBlue(0.827451f)),
    LECTURE(new Color().setRed(1f).setGreen(0.8980392f).setBlue(0.6f)),
    GROUP1(new Color().setRed(0.7882353f).setGreen(0.85490197f).setBlue(0.972549f)),
    GROUP2(new Color().setRed(0.91764706f).setGreen(0.81960785f).setBlue(0.8627451f));

    private final Color color;

    LessonType(Color color) {
        this.color = color;
    }

    public Collection<? extends Color> getColor() {
        return Collections.singleton(color);
    }
}
