package com.example.seabattle.models.field;

import com.example.seabattle.models.field.object.FieldObject;
import com.example.seabattle.models.field.object.Mine;
import com.example.seabattle.models.field.object.Ship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Field {

    /**
     * Ключ для мины в мапе
     */
    private final Integer MINE_KEY = 0;

    /**
     * Список выбранных координат
     */
    private final List<int[]> selectedCells = new ArrayList<>();

    /**
     * Конфиг колличества объектов
     */
    public final Map<Integer, Integer> shipCounts = new HashMap<>();

    /**
     * Доска с объектами поля
     */
    private FieldObject[][] board = new FieldObject[10][10];

    /**
     * 0 - горизонтальный,
     * 1 - вертикальный,
     * -1 - не задан
     */
    private int shipOrientation = -1;

    public Field() {
        setDefaultConfig();
    }

    /**
     * Случайно располагает корабли
     *
     * @return Список кораблей
     */
    public List<FieldObject> randomShips() {
        List<FieldObject> ships = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : this.shipCounts.entrySet()) {
            Integer size = entry.getKey();
            Integer count = entry.getValue();
            if (size == 0) {
                // Пропускаем мину
                continue;
            }
            for (int i = 0; i < count; i++) {
                FieldObject result = placeRandomShip(size);
                if (result != null) {
                    ships.add(result);
                }
            }
        }
        return ships;
    }

    /**
     * Располагает мины в случайном порядке
     *
     * @return созданные мины
     */
    public List<FieldObject> randomMines() {
        List<FieldObject> mines = new ArrayList<>();
        Integer mineCnt = shipCounts.get(MINE_KEY);
        if (mineCnt == null) {
            return mines;
        }
        for (int i = 0; i < mineCnt; i++) {
            FieldObject result = placeRandomMine();
            if (result != null) {
                mines.add(result);
            }
        }
        return mines;
    }

    public FieldObject[][] getBoard() {
        return this.board;
    }

    /**
     * Возвращает координаты занятых точек
     *
     * @return List<int [ ]>
     */
    public List<int[]> getSelectedCoordinates() {
        return this.selectedCells;
    }


    /**
     * Можо ли занимать данную точку.
     * Не является ли она уже выбранной
     * И
     * Нет ли вокруг этой точки других
     *
     * @param row int
     * @param col int
     * @return boolean
     */
    public boolean isValidCellForSelection(int row, int col) {
        if (board[row][col] != null) {
            return false;
        }

        for (int i = row - 1; i <= row + 1; i++) {
            if (i < 0 || i > 9) {
                continue;
            }
            for (int j = col - 1; j < col + 1; j++) {
                if (j < 0 || j > 9) {
                    continue;
                }
                if (board[i][j] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Проверка на наличие соседей
     *
     * @param row int
     * @param col int
     * @return boolean
     */
    private boolean isCellValidForPlacement(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Пропускаем саму ячейку
                int newRow = row + i;
                int newCol = col + j;

                if (newRow >= 0 && newRow < 10 && newCol >= 0 && newCol < 10) {
                    if (isCellOccupied(newRow, newCol)) {
                        return false;
                    }
                }
            }
        }
        return true; // Все соседние ячейки свободны
    }

    /**
     * Является ли "сота" выбранной занятой
     *
     * @param row int
     * @param col int
     * @return boolean
     */
    private boolean isCellOccupied(int row, int col) {
        for (int[] cell : selectedCells) {
            if (cell[0] == row && cell[1] == col) {
                return true;
            }
        }
        return false;
    }

    /**
     * Очистка поля
     */
    public void clear() {
        board = new FieldObject[10][10];
        selectedCells.clear();
        shipOrientation = -1;

        setDefaultConfig();
    }

    /**
     * Готово ли поле к игре
     *
     * @return boolean
     */
    public boolean isFieldReadyToPlay() {
        for (Map.Entry<Integer, Integer> entry : this.shipCounts.entrySet()) {
            Integer count = entry.getValue();
            if (count != 0) {
                return false;
            }
        }
        return true;
    }


    /**
     * Есть ли возможость создать мину
     *
     * @return boolean
     */
    public boolean canCreateMine() {
        return canCreateShip(this.MINE_KEY);
    }

    /**
     * Есть ли возможность создать корабль определенного размера
     *
     * @param len Integer
     * @return boolean
     */
    public boolean canCreateShip(Integer len) {
        Integer count = this.shipCounts.get(len);
        if (count == null) {
            return false;
        }
        return count != 0;
    }

    /**
     * Создание нового корабля по координтам
     *
     * @param points Координаты корабля
     * @return FieldObject новый корабл
     */
    public FieldObject createShip(List<int[]> points) {
        if (points.size() > 4) {
            return null;
        }
        if (!canCreateShip(points.size())) {
            return null;
        }
        if (!recalculateShipsCount(points.size())) {
            return null;
        }

        selectedCells.addAll(points);
        Ship newShip = new Ship(points);
        for (int[] point : points) {
            this.board[point[0]][point[1]] = newShip;
        }
        return newShip;
    }

    /**
     * Создание мины по переданной координате
     *
     * @param coordinates int[2] координаты мины
     * @return Новая мина
     */
    public FieldObject createMine(int[] coordinates) {

        if (!canCreateMine()) {
            return null;
        }

        int row = coordinates[0];
        int col = coordinates[1];
        List<int[]> points = new ArrayList<>();
        points.add(coordinates);
        Mine mine = new Mine(points);
        board[row][col] = mine;
        selectedCells.add(new int[]{row, col});
        recalculateShipsCount(this.MINE_KEY);
        return mine;
    }


    /**
     * Создание рандомного корабля на поле.
     *
     * @param shipSize Размер корабля
     * @return Созданный корабль|null
     */
    private FieldObject placeRandomShip(int shipSize) {
        int maxTries = 1000;
        int tries = 0;
        Random random = new Random();

        while (tries < maxTries) {
            int row = random.nextInt(10);
            int col = random.nextInt(10);
            int orientation = random.nextInt(2);

            List<int[]> potentialShip = new ArrayList<>();

            if (orientation == 0) {
                if (col + shipSize > 10) {
                    tries++;
                    continue;
                }

                for (int i = 0; i < shipSize; i++) {
                    potentialShip.add(new int[]{row, col + i});
                }
            } else { // vertical
                if (row + shipSize > 10) {
                    tries++;
                    continue;
                }

                for (int i = 0; i < shipSize; i++) {
                    potentialShip.add(new int[]{row + i, col});
                }
            }

            // Проверям, на заянты ли точки
            boolean validPlacement = true;
            for (int[] cell : potentialShip) {
                if (isCellOccupied(cell[0], cell[1]) || !isCellValidForPlacement(cell[0], cell[1])) {
                    validPlacement = false;
                    break;
                }
            }

            // Если расоположение доупстимо, то создаем корабль
            if (validPlacement) {
                return createShip(potentialShip);
            }
            tries++;
        }
        return null;
    }

    /**
     * Рандомно распологает мину на поле
     *
     * @return Созданная мина|null
     */
    private FieldObject placeRandomMine() {
        boolean placed = false;
        int maxTries = 1000;
        int tries = 0;


        Random random = new Random();
        while (tries < maxTries) {
            int row = random.nextInt(10);
            int col = random.nextInt(10);

            if (isCellOccupied(row, col) || !isCellValidForPlacement(row, col)) {
                tries++;
                continue;
            }
            FieldObject newMine = createMine(new int[]{row, col});
            if (newMine != null) {
                return newMine;
            }
        }
        return null;
    }

    /**
     * Пересчет кол-ва кораблей в зависимости от размера нового корабля
     *
     * @param shipSize Integer
     * @return boolean
     */
    private boolean recalculateShipsCount(Integer shipSize) {
        if (!shipCounts.containsKey(shipSize)) {
            return false;
        }
        Integer currentCount = shipCounts.get(shipSize);
        if (currentCount == null || currentCount < 0) {
            return false;
        }
        shipCounts.put(shipSize, currentCount - 1);
        return true;
    }

    /**
     * Установка стандартной конфигурации кол-ва объектов
     */
    private void setDefaultConfig() {
        shipCounts.put(4, 1);
        shipCounts.put(3, 2);
        shipCounts.put(2, 3);
        shipCounts.put(1, 2);
        shipCounts.put(this.MINE_KEY, 2);
    }
}
