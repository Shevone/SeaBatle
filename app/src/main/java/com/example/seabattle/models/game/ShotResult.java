package com.example.seabattle.models.game;

public class ShotResult {

    private final ShotType result;

    private final int[] shotCoordinate;

    private final int gotShotPlayerID;

    private final int currentPlayerID;

    public ShotResult(int row, int col, ShotType result,int gotShotPlayerID, int currentPlayerID) {
        this.result = result;
        this.shotCoordinate = new int[]{row, col};
        this.gotShotPlayerID = gotShotPlayerID;
        this.currentPlayerID = currentPlayerID;
    }

    public ShotType getResult() {
        return result;
    }

    public int[] getShotCoordinate() {
        return shotCoordinate;
    }

    public int getGotShotPlayerID() {
        return gotShotPlayerID;
    }

    public int getCurrentPlayerID() {
        return currentPlayerID;
    }
}
