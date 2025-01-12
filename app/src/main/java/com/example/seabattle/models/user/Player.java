package com.example.seabattle.models.user;

import com.example.seabattle.models.field.object.FieldObject;

public class Player {

    private final int ID;
    private final String name;

    protected FieldObject[][] playerBoard;

    private int health = 8;

    public Player(int ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    public void setPlayerBoard(FieldObject[][] playerBoard) {
        this.playerBoard = playerBoard;
        this.health = 8;
    }

    public void decrementHealth() {
        health--;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public FieldObject[][] getPlayerBoard() {
        return playerBoard;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }
}
