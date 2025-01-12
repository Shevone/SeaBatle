package com.example.seabattle.models.field.object;

import android.graphics.Color;

import java.util.List;

public class Mine extends FieldObject {

    public Mine(List<int[]> point){
        super(point);
        this.sign = 'M';
        this.color = Color.rgb(255, 165, 0);
    }
}
