package com.example.seabattle.models.field.object;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public abstract class FieldObject {

    protected Character sign = ' ';

    protected int color = Color.GREEN;

    private final List<int[]> coordinates;

    private Boolean isDestroyed = false;

    FieldObject(List<int[]> points) {
        this.coordinates = points;
    }

    /**
     * Возвращает цвет объекта на поле
     *
     * @return int - rgb
     */
    public int getColor() {
        return this.color;
    }

    public String getSign() {
        return this.sign.toString();
    }


    public List<int[]> getCoordinates() {
        return coordinates;
    }

    public void setHit() {
        this.isDestroyed = true;
    }

    public Boolean isDestroyed() {
        return isDestroyed;
    }
}
