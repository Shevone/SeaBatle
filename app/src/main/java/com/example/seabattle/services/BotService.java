package com.example.seabattle.services;

import com.example.seabattle.models.game.ShotResult;
import com.example.seabattle.models.game.ShotType;
import com.example.seabattle.models.user.Bot;
import com.example.seabattle.services.game.GameObserver;
import com.example.seabattle.services.game.GameService;

public class BotService implements GameObserver {
    private final Bot bot = new Bot();
    private final GameService gameService = GameService.getInstance();
    private static BotService instance;

    private BotService() {
        gameService.registerObserver(this);
    }

    public static BotService getInstance() {
        if (instance == null) {
            instance = new BotService();
        }
        return instance;
    }

    public Bot getBot() {
        return bot;
    }

    /**
     * @param result Результат хода
     */
    @Override
    public void onStepCompleted(ShotResult result) {
        int botID = bot.getID();
        // Если выстрел был выполнен ботом, то регистрируем ему
        if (result.getCurrentPlayerID() == botID) {
            int[] coordinates = result.getShotCoordinate();
            ShotType shotType = result.getResult();
            int shotResult;
            switch (shotType) {
                case HIT:
                    shotResult = 0;
                    break;
                case DESTROYED:
                    shotResult = 1;
                    break;
                case MINE_HIT:
                    shotResult = 2;
                    break;
                default:
                    shotResult = -1;
                    break;
            }
            bot.registerShot(coordinates[0], coordinates[1], shotResult);
        }

        boolean isBotTurn = gameService.isTurn(botID);
        if (!isBotTurn) {
            return;
        }
        int[] point = bot.makeShot();
        if (point == null) {
            return;
        }
        gameService.shot(botID, point[0], point[1]);
    }
}
