package com.example.seabattle.models.game;

import com.example.seabattle.models.field.object.FieldObject;
import com.example.seabattle.models.field.object.Mine;
import com.example.seabattle.models.field.object.Ship;
import com.example.seabattle.models.user.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Game {

    private final Map<Integer, Player> playerMap = new HashMap<>();

    private final Map<Integer, List<FieldObject>> destroyedObjects = new HashMap<>();

    /**
     * Установить игрока
     *
     * @param player Игрок
     */
    public void setPlayer(Player player) {
        if (playerMap.keySet().toArray().length == 2) {
            return;
        }
        playerMap.put(player.getID(), player);
        destroyedObjects.put(player.getID(), new ArrayList<>());
    }

    /**
     * Выстрел по точке
     *
     * @param row             X
     * @param col             Y
     * @param playerIDGotShot int ID игрока по которому стреляют
     * @return int
     */
    public ShotType shot(int playerIDGotShot, int row, int col) {

        Player player = this.playerMap.get(playerIDGotShot);
        if (player == null) {
            return ShotType.IMPOSSIBLE;
        }
        FieldObject[][] targetBoard = player.getPlayerBoard();

        FieldObject targetObject = targetBoard[row][col];
        if (targetObject == null) {
            return ShotType.MISS;
        }

        if (targetObject instanceof Mine) {
            targetObject.setHit();
            addToLastDestroy(playerIDGotShot, targetObject);
            return ShotType.MINE_HIT;
        } else if (targetObject instanceof Ship) {
            targetObject.setHit();
            if (targetObject.isDestroyed()) {
                addToLastDestroy(playerIDGotShot, targetObject);
                player.decrementHealth();
                return ShotType.DESTROYED;
            }
            return ShotType.HIT;
        }
        return ShotType.MISS;
    }

    /**
     * Добавление объекта к списку уничтоженных
     *
     * @param playerID    int
     * @param fieldObject FieldObject
     */
    private void addToLastDestroy(int playerID, FieldObject fieldObject) {
        if (destroyedObjects.containsKey(playerID)) {
            destroyedObjects.get(playerID).add(fieldObject);
        } else {
            List<FieldObject> newList = new ArrayList<>();
            newList.add(fieldObject);
            destroyedObjects.put(playerID, newList);
        }
    }

    /**
     * Возвращает последний уничтоженный объект игрока
     *
     * @param key ID игрока
     * @return объект/null
     */
    public FieldObject getLastDestroyedObject(int key) {
        if (destroyedObjects.containsKey(key)) {
            List<FieldObject> list = destroyedObjects.get(key);
            if (list != null && !list.isEmpty()) {
                return list.get(list.size() - 1);
            }
        }
        return null;
    }
}
