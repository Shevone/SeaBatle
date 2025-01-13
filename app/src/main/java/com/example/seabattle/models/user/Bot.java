package com.example.seabattle.models.user;

import com.example.seabattle.models.field.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bot extends Player {

    private final Random random = new Random();

    /**
     * Уже использованные клетки
     */
    private final Integer[][] shotsMade = new Integer[10][10];

    /**
     * Попадания
     */
    private final List<int[]> hits = new ArrayList<>();

    /**
     * -1: нет направления,
     * 0: вправо,
     * 1: влево,
     * 2: вниз,
     * 3: вверх
     */
    private int shotDirection = -1;

    /**
     * Результат последнейго попадания
     * -1 - мимо
     * 0 - ранил
     * 1 - уничтожил
     */
    private int lastShotResult = -1;

    public Bot() {
        super(228, "Bot");
        createRandomField();
    }

    /**
     * Создание рандомного поля для бота
     */
    private void createRandomField() {
        Field field = new Field();

        field.randomShips();
        field.randomMines();

        this.playerBoard = field.getBoard();
    }

    /**
     * Выстрел
     *
     * @return int[]|null - если пропускает выстрел
     */
    public int[] makeShot() {
        if (hits.isEmpty()) {
            return getRandomShot();
        }
        int[] res = getSmartShot();

        if (shotsMade[res[0]][res[1]] != null) {
            clear();
            return getRandomShot();
        }
        return res;
    }

    /**
     * Регистрация результата выстрела
     *
     * @param row        int
     * @param col        int
     * @param shotResult -1 мимо, 0 - попал, 1 - уничтожил, 2 - мина
     */
    public void registerShot(int row, int col, int shotResult) {
        shotsMade[row][col] = shotResult;
        this.lastShotResult = shotResult;
        switch (shotResult) {
            case 1:
                // Уничтожен
                hits.add(new int[]{row, col});
                handleDestroy();
                break;
            case 0:
                // Попадание
                hits.add(new int[]{row, col});
                // Сохраняем shotDirection
                break;
            case 2:
                // мина
                hits.add(new int[]{row, col});
                handleDestroy();
                break;
            case -1:
                // Мимо
                break;
        }
    }


    /**
     * Уничтожение корабля у противника
     * - записываем точки вокруг уничтоженного корабля как использованные
     * - очищаем координаты, которые запоминали
     */
    private void handleDestroy() {
        for (int[] coordinate : hits) {
            int x = coordinate[0];
            int y = coordinate[1];
            for (int i = x - 1; i <= x + 1; i++) {
                if (i >= 0 && i < 10) {
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (j >= 0 && j < 10) {
                            shotsMade[i][j] = -1;
                        }
                    }
                }
            }
        }
        clear();
    }

    /**
     * Очистка результатов стрельбы
     */
    private void clear() {
        shotDirection = -1;
        lastShotResult = -1;
        hits.clear();
    }

    /**
     * Случайный ход от бота
     *
     * @return int [] Координтаы
     */
    private int[] getRandomShot() {
        clear();
        int row, col;
        do {
            row = random.nextInt(10);
            col = random.nextInt(10);
        } while (shotsMade[row][col] != null);
        return new int[]{row, col};
    }

    /**
     * Умный ход в исполнении бота
     *
     * @return int[]
     */
    private int[] getSmartShot() {
        int[] lastHit = hits.get(hits.size() - 1);

        int[] result = new int[2];
        if (shotDirection == -1
                || (lastShotResult == -1 && hits.size() == 1)) {
            // Первое попадание - Выбираем случайную сторону для выстрела
            int randDirection;
            do {
                randDirection = random.nextInt(4);
                result = getNextShot(lastHit, randDirection);
            } while (shotsMade[result[0]][result[1]] != null);
            shotDirection = randDirection;
        } else {
            // Если мы определили направление стрельбы
            switch (lastShotResult) {
                case 0:
                    result = getNextShot(lastHit, shotDirection);
                    break;
                case 1:
                    // Если уничтожен то очищаем все и генерируем рандомный ход
                    clear();
                    result = getRandomShot();
                    break;
                case -1:
                    // Промах - начинаем идти в противополодную сторону
                    result = reverseDirectionShot();
                    break;
            }
        }
        return result;
    }

    /**
     * Генерация выстрела в другом направлении
     *
     * @return int[] выстрел в другом направлении
     */
    private int[] reverseDirectionShot() {
        int[] firstShot = hits.get(0);
        int direction;

        switch (shotDirection) {
            case (0):
                direction = 1;
                break;
            case (1):
                direction = 0;
                break;
            case (2):
                direction = 3;
                break;
            case (3):
                direction = 2;
                break;
            default:
                direction = random.nextInt(3);
                break;
        }
        return getNextShot(firstShot, direction);
    }

    /**
     * Получаем следующий выстрел по направлению
     *
     * @param lastShot  int[] координты предыдущего
     * @param direction int направление выстерал
     * @return int[] координаты нового выстрела
     */
    private int[] getNextShot(int[] lastShot, int direction) {
        int[] result = null;
        switch (direction) {
            case (0):
                // вниз
                if (lastShot[0] + 1 <= 9) {
                    result = new int[]{lastShot[0] + 1, lastShot[1]};
                }
                break;
            case (1):
                // вверх
                if (lastShot[0] - 1 >= 0) {
                    result = new int[]{lastShot[0] - 1, lastShot[1]};
                }
                break;
            case (2):
                // влево
                if (lastShot[1] - 1 >= 0) {
                    result = new int[]{lastShot[0], lastShot[1] - 1};
                }
                break;
            case (3):
                // вправо
                if (lastShot[1] + 1 <= 9) {
                    result = new int[]{lastShot[0], lastShot[1] + 1};
                }
                break;
            default:
                result = getRandomShot();
                break;
        }
        if (result == null){
            result = getRandomShot();
        }
        shotDirection = direction;
        return result;
    }
}
