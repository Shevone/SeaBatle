package com.example.seabattle.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.seabattle.R;
import com.example.seabattle.models.field.Field;
import com.example.seabattle.models.field.object.FieldObject;
import com.example.seabattle.models.user.Player;
import com.example.seabattle.services.PlayerService;

import java.util.ArrayList;
import java.util.List;


public class SetupBoardActivity extends AppCompatActivity {

    private final String NOT_READY_TEXT = "Поле еще не готово к битве!";
    private final String ERROR_CREATE_TEXT = "Ошибка при создании объекта на поле";


    private GridLayout gridLayout;
    private TextView[][] gridCells;

    private Button createShipButton;
    private Button createMineButton;

    private boolean readyToPlay = false;

    private final Field field = new Field();

    /**
     * Теущие выбранные координаты
     */
    private final List<int[]> currentSelection = new ArrayList<>();
    /**
     * Направление выбора
     * 0 - по Х
     * 1- по Y
     * -1 - нет
     */
    private int selectOrientation = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_board);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        PlayerService playerService = PlayerService.getInstance(this);
        Player currentPlayer = playerService.getCurrentPlayer();
        if (currentPlayer == null) {
            finish();
            return;
        }
        gridLayout = findViewById(R.id.grid_layout);
        gridCells = new TextView[10][10];

        TextView currentPlayerTextView = findViewById(R.id.currentPlayerNameTextView);
        currentPlayerTextView.setText(currentPlayer.getName());

        createShipButton = findViewById(R.id.createShipButton);
        createMineButton = findViewById(R.id.createMineButton);
        Button clearSelectionButton = findViewById(R.id.clearSelectionButton);
        Button playButton = findViewById(R.id.startGameButton);
        Button randomButton = findViewById(R.id.randomPlacementButton);
        Button exitButton = findViewById(R.id.returnToMenuButton);

        createGrid();

        updateButtonsVisibility();

        createShipButton.setOnClickListener(v -> {
            createShip();
        });

        createMineButton.setOnClickListener(v -> {
            createMine();
        });

        clearSelectionButton.setOnClickListener(v -> {
            clearSelection();
        });

        randomButton.setOnClickListener(v -> {
            randomPlacement();
        });

        exitButton.setOnClickListener(view -> {
            returnToMainMenu();
        });

        playButton.setOnClickListener(v -> {
            if (!this.readyToPlay) {
                Toast.makeText(
                        this,
                        this.NOT_READY_TEXT,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Записываем игроку созданное поле
                currentPlayer.setPlayerBoard(field.getBoard());
                playerService.setCurrentPlayer(currentPlayer);
                // переходим в активити игры
                Intent intent = new Intent(this, GameActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Случайное расположение кораблей
     */
    private void randomPlacement() {
        clearSelection();
        for (FieldObject ship : field.randomShips()) {
            drawFieldObject(ship);
        }

        for (FieldObject mine : field.randomMines()) {
            drawFieldObject(mine);
        }

        readyToPlay = field.isFieldReadyToPlay();
    }

    /**
     * Отрисовка объекта на поле
     *
     * @param fieldObject объект поля
     */
    private void drawFieldObject(FieldObject fieldObject) {
        for (int[] cell : fieldObject.getCoordinates()) {
            gridCells[cell[0]][cell[1]].setText(fieldObject.getSign());
            gridCells[cell[0]][cell[1]].setBackgroundColor(fieldObject.getColor());
        }
    }

    /**
     * Расчет размера соты
     *
     * @return int - размер
     */
    private int calculateCellSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        int numberOfColumns = 10;
        int numberOfRows = 10;
        int screenPadding = 16; // Отступ в dp

        int paddingPixels = (int) (screenPadding * getResources().getDisplayMetrics().density);

        int availableWidth = screenWidth - (2 * paddingPixels);
        int availableHeight = screenHeight - (2 * paddingPixels);

        int cellSizeWidth = availableWidth / numberOfColumns;
        int cellSizeHeight = availableHeight / numberOfRows;

        return Math.min(cellSizeWidth, cellSizeHeight);
    }

    /**
     * Создание сетки.
     */
    private void createGrid() {

        int cellSize = calculateCellSize();


        // Устанавливаем margin на GridLayout
        int marginSize = 2;

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                TextView cell = new TextView(this);
                cell.setText("");
                cell.setBackgroundColor(Color.LTGRAY);

                // Создание FrameLayout для добавления border
                FrameLayout cellFrame = new FrameLayout(this);
                cellFrame.addView(cell);

                // Устанавливаем layout params
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row),
                        GridLayout.spec(col)
                );

                params.width = cellSize;
                params.height = cellSize;

                params.setMargins(marginSize, marginSize, marginSize, marginSize);
                cellFrame.setLayoutParams(params);

                // Установка border (можно добавить xml drawable)
                cellFrame.setBackgroundColor(Color.BLACK);

                // Сохраняем ячейку в массиве
                gridCells[row][col] = cell;

                // Добавляем обработчик нажатий
                int finalRow = row;
                int finalCol = col;
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCellClick(cell, finalRow, finalCol);
                    }
                });
                gridLayout.addView(cellFrame);
            }
        }
    }


    /**
     * Обработчик нажаитя на "соты"
     *
     * @param cell TextView
     * @param row  int
     * @param col  int
     */
    private void onCellClick(TextView cell, int row, int col) {
        if (readyToPlay) {
            return;
        }

        boolean isSelectedNow = false;
        for (int[] coordinate : currentSelection) {
            if ((row == coordinate[0]) && (col == coordinate[1])) {
                isSelectedNow = true;
                break;
            }
        }

        if (isSelectedNow) {
            // Если уже выбрана
            if (!removeIfEdge(currentSelection, row, col)) {
                // Можем убирать только крайние точки
                if (!isNearby(currentSelection.get(0), row, col)) {
                    Toast.makeText(
                            this,
                            "Можно удалять только крайние точки",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // Когда остается одна клетка то убираем направление выбора
            if (currentSelection.size() == 1) {
                selectOrientation = -1;
            }
            cell.setText("");
            cell.setBackgroundColor(Color.LTGRAY);
        } else if (field.isValidCellForSelection(row, col)) {
            if (currentSelection.isEmpty()) {
                // Если первая то ставим
                currentSelection.add(new int[]{row, col});
            } else if (currentSelection.size() == 1) {
                // Если точка вторая, то проверям что она рядом
                if (!isNearby(currentSelection.get(0), row, col)) {
                    Toast.makeText(
                            this,
                            "Точка должна быть рядом",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                currentSelection.add(new int[]{row, col});
                determineShipOrientation();
            } else {
                if (currentSelection.size() >= 4) {
                    Toast.makeText(
                            this,
                            "Уже много точек",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                // Если точка третья и больше, то проверям что она по направлению
                // и рядом с какой либо из крайних
                if (!canExtend(currentSelection, row, col)) {
                    Toast.makeText(
                            this,
                            "Надо выбрать точку рядом",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                currentSelection.add(new int[]{row, col});
            }
            cell.setText("X");
            cell.setBackgroundColor(Color.RED);
        } else {
            Toast.makeText(
                    this,
                    "Невозможно выбрать точку",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        updateButtonsVisibility();
    }

    /**
     * Удаление точки из списка если она является краевой
     *
     * @param points
     * @param x
     * @param y
     * @return
     */
    public boolean removeIfEdge(List<int[]> points, int x, int y) {
        if (points == null || points.isEmpty()) {
            return false; // Нечего удалять, список пуст
        }

        int size = points.size();
        if (size == 1) {
            int[] firstPoint = points.get(0);
            if (firstPoint[0] == x && firstPoint[1] == y) {
                points.remove(0);
                return true;
            }
            return false;
        }


        for (int i = 0; i < points.size(); i++) {
            int[] point = points.get(i);
            if (point[0] == x && point[1] == y) {
                if (i == 0 || i == points.size() - 1) {
                    points.remove(i);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Можно ли этой координатой дополнить существующие
     *
     * @param existingPoints
     * @param x
     * @param y
     * @return
     */
    public boolean canExtend(List<int[]> existingPoints, int x, int y) {
        if (existingPoints == null || existingPoints.isEmpty() || existingPoints.size() > 2) {
            return true;
        }

        for (int[] existingPoint : existingPoints) {
            if (isNearAndAligned(existingPoint, x, y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Находится ли точка рядом по пути выбора.
     *
     * @param existingPoint
     * @param x2
     * @param y2
     * @return
     */
    private boolean isNearAndAligned(int[] existingPoint, int x2, int y2) {
        if (selectOrientation == 0) {
            return (x2 == existingPoint[0]) && (y2 == existingPoint[1] + 1 || y2 == existingPoint[1] - 1);
        } else {
            return (y2 == existingPoint[1]) && (x2 == existingPoint[0] + 1 || x2 == existingPoint[0] - 1);
        }
    }

    /**
     * Проверка что точка находится рядом
     *
     * @param existingPoint int[] точка для проверки
     * @param x2            Х новой точки
     * @param y2            У новой точки
     * @return boolean
     */
    public static boolean isNearby(int[] existingPoint, int x2, int y2) {
        int x1 = existingPoint[0];
        int y1 = existingPoint[1];
        return (x2 == x1 && (y2 == y1 + 1 || y2 == y1 - 1)) ||
                (y2 == y1 && (x2 == x1 + 1 || x2 == x1 - 1));
    }

    /**
     * Определение направленности нового корабля
     */
    private void determineShipOrientation() {
        if (currentSelection.size() < 2) {
            return;
        }

        int[] firstCell = currentSelection.get(0);
        int[] secondCell = currentSelection.get(1);

        if (firstCell[0] == secondCell[0]) {
            selectOrientation = 0;
        } else if (firstCell[1] == secondCell[1]) {
            selectOrientation = 1;
        }
    }

    /**
     * Отображение кнопок в зависимости от кол-ва выбранных клеток
     */
    private void updateButtonsVisibility() {
        int currentShipSize = currentSelection.size();
        if (currentShipSize == 0) {
            createShipButton.setVisibility(View.GONE);
            createMineButton.setVisibility(View.GONE);
        } else if (currentShipSize == 1 && field.canCreateMine()) {
            createShipButton.setVisibility(View.VISIBLE);
            createMineButton.setVisibility(View.VISIBLE);
        } else {
            createShipButton.setVisibility(View.VISIBLE);
            createMineButton.setVisibility(View.GONE);
        }
    }

    /**
     * Обработчик кнопки создать корабль
     */
    private void createShip() {
        FieldObject newShip = field.createShip(currentSelection);
        if (newShip == null) {
            Toast.makeText(
                    this,
                    this.ERROR_CREATE_TEXT,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        handleCreate(newShip);
    }

    /**
     * Обработчик кнопки создания мины
     */
    private void createMine() {
        if (currentSelection.size() > 1) {
            Toast.makeText(
                    this,
                    "Мина может быть только размер в 1 клетку",
                    Toast.LENGTH_SHORT).show();
        }

        //вызов метода сервиса
        FieldObject mine = field.createMine(currentSelection.get(0));
        if (mine == null) {
            Toast.makeText(
                    this,
                    this.ERROR_CREATE_TEXT,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        handleCreate(mine);
    }

    /**
     * Соббытие - создание нового объекта
     *
     * @param newObject FieldObject
     */
    private void handleCreate(FieldObject newObject) {
        drawFieldObject(newObject);
        currentSelection.clear();
        this.readyToPlay = field.isFieldReadyToPlay();
    }

    /**
     * Очистка выбора
     */
    private void clearSelection() {
        for (int[] cell : field.getSelectedCoordinates()) {
            gridCells[cell[0]][cell[1]].setText("");
            gridCells[cell[0]][cell[1]].setBackgroundColor(Color.LTGRAY);
        }
        for (int[] cell : currentSelection) {
            gridCells[cell[0]][cell[1]].setText("");
            gridCells[cell[0]][cell[1]].setBackgroundColor(Color.LTGRAY);
        }
        currentSelection.clear();
        field.clear();
        readyToPlay = false;
        updateButtonsVisibility();
    }

    /**
     * Возврат в главное меню
     */
    private void returnToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}