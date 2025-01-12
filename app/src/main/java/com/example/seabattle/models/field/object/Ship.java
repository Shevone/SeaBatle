package com.example.seabattle.models.field.object;

import java.util.List;

public class Ship extends FieldObject {

    private Integer health;

    public Ship(List<int[]> point) {
        super(point);
        this.sign = String.valueOf(point.size()).charAt(0);
        this.health = point.size();
    }

    @Override
    public void setHit() {
        this.health--;
    }

    @Override
    public Boolean isDestroyed() {
        return this.health == 0;
    }
}
