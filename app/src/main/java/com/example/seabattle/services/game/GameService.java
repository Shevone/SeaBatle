package com.example.seabattle.services.game;

import android.os.Handler;
import android.os.Looper;

import com.example.seabattle.models.field.object.FieldObject;
import com.example.seabattle.models.game.Game;
import com.example.seabattle.models.game.ShotResult;
import com.example.seabattle.models.game.ShotType;
import com.example.seabattle.models.user.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameService {

    /**
     * Игра
     */
    private final Game game = new Game();

    /**
     * Уже отсрелянные точки
     */
    private final Map<Integer, Boolean[][]> alreadyShootPoint = new HashMap<>();

    /**
     * Очередь хода
     */
    private final Map<Integer, Integer> step = new HashMap<>();

    private static GameService instance;
    private final List<GameObserver> observers = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private Player player1;
    private Player player2;

    /**
     * Регистрация подписчиков
     *
     * @param observer
     */
    public void registerObserver(GameObserver observer) {
        observers.add(observer);
    }

    /**
     * Отписка
     *
     * @param observer
     */
    public void unregisterObserver(GameObserver observer) {
        observers.remove(observer);
    }

    /**
     * Сообщить о событии
     *
     * @param result ShotResult
     */
    protected void notifyObservers(ShotResult result) {
        uiHandler.post(() -> {
            for (GameObserver observer : observers) {
                observer.onStepCompleted(result);
            }
        });
    }

    public static GameService getInstance() {
        if (instance == null) {
            instance = new GameService();
        }
        return instance;
    }

    /**
     * Установить первого игрока
     *
     * @param player Игрок
     */
    public void setPlayer1(Player player) {
        this.player1 = player;
        step.put(player.getID(), 1);
        alreadyShootPoint.put(player.getID(), new Boolean[10][10]);
        game.setPlayer(player);
    }

    /**
     * Установить второго игрока
     *
     * @param player Игрок
     */
    public void setPlayer2(Player player) {
        this.player2 = player;
        step.put(player.getID(), 0);
        alreadyShootPoint.put(player.getID(), new Boolean[10][10]);
        game.setPlayer(player);
    }


    /**
     * Добавить точку к списку отсреляных
     *
     * @param playerID int ID игрока стреляющего
     * @param row      int
     * @param col      int
     */
    public void addPointToShotList(int playerID, int row, int col) {
        Boolean[][] playerField = alreadyShootPoint.get(playerID);
        if (playerField == null) {
            return;
        }
        playerField[row][col] = true;
        alreadyShootPoint.put(playerID, playerField);
    }

    /**
     * @param playerID int ID стреляющего
     * @param row      X
     * @param col      Y
     * @return доступна ли для выстреал
     */
    public boolean isPointAvailableToShot(int playerID, int row, int col) {
        Boolean[][] playerField = alreadyShootPoint.get(playerID);
        if (playerField == null) {
            return false;
        }
        return playerField[row][col] == null;
    }

    /**
     * Возвращает последний уничтоженный корабль у бота
     *
     * @return FieldObject|null
     */
    public FieldObject getLastDestroyedObject(int playerID) {
        return game.getLastDestroyedObject(playerID);
    }

    /**
     * Выстрел.
     *
     * @param currentPlayerID ID стреляющего
     * @param row             X
     * @param col             Y
     */
    public void shot(int currentPlayerID, int row, int col) {
        executorService.execute(() -> {
            int opponentID = getOppID(currentPlayerID);
            ShotType res = game.shot(opponentID, row, col);
            if (res != ShotType.IMPOSSIBLE) {
                addPointToShotList(currentPlayerID, row, col);
            }
            calculateNexStep(currentPlayerID, opponentID, res);
            notifyObservers(new ShotResult(row, col, res, opponentID, currentPlayerID));
        });
    }

    /**
     * Вычисление след. хода
     *
     * @param currentPlayerID ID стреляющего
     * @param opponentID      ID игрока по которому стреляют
     * @param res             Тип попадания
     */
    private void calculateNexStep(int currentPlayerID, int opponentID, ShotType res) {
        switch (res) {
            case MISS:
                int currentStepCount = step.get(currentPlayerID) - 1;
                if (currentStepCount <= 0){
                    // Если у текущего игрока не оставлось ходов - то даем один ход противнику
                    step.put(currentPlayerID, 0);
                    step.put(opponentID, 1);
                } else {
                    step.put(currentPlayerID, currentStepCount);
                }
              break;
            case MINE_HIT:
                step.put(currentPlayerID, 0);
                step.put(opponentID, 2);
                break;
            case HIT:
                break;
            case DESTROYED:
                break;
            case IMPOSSIBLE:
                return;
        }
    }

    /**
     * Сейчас ход этого игрока?
     *
     * @param playerID ID игрока для проверки
     * @return boolean - его ход или нет
     */
    public boolean isTurn(int playerID) {
        return step.get(playerID) > 0;
    }

    /**
     * Проиграл ли игрок
     *
     * @param playerIDGotShot ID игрока для проверки
     * @return boolean проиграл или нет
     */
    public boolean isLose(int playerIDGotShot) {
        Player player;
        if (playerIDGotShot == player1.getID()) {
            player = player1;
        } else {
            player = player2;
        }
        return !player.isAlive();
    }

    /**
     * Получить ID оппонента
     *
     * @param currentPlayerID ID текущего игрока
     * @return int ID оппонентаж
     */
    public int getOppID(int currentPlayerID) {
        if (currentPlayerID == player1.getID()) {
            return player2.getID();
        }
        return player1.getID();
    }

    /**
     * Получить имя оппонента
     *
     * @param currentPlayerID ID текущего игрока
     * @return int ID оппонентаж
     */
    public String getOppName(int currentPlayerID) {
        if (currentPlayerID == player1.getID()) {
            return player2.getName();
        }
        return player1.getName();
    }
}

