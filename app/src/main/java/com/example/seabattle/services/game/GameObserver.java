package com.example.seabattle.services.game;

import com.example.seabattle.models.game.ShotResult;

public interface GameObserver {
    void onStepCompleted(ShotResult result);
}
