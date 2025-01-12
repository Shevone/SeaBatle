package com.example.seabattle.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.seabattle.R;
import com.example.seabattle.models.field.Field;
import com.example.seabattle.models.field.object.FieldObject;
import com.example.seabattle.models.user.Player;
import com.example.seabattle.services.PlayerService;


public class SetupBoardActivity extends AppCompatActivity {

    private final String NOT_READY_TEXT = "Поле еще не готово к битве!";
    private final String ERROR_CREATE_TEXT = "Ошибка при создании объекта на поле";

    private GridLayout gridLayout;
    private TextView[][] gridCells;

    private Button createShipButton;
    private Button createMineButton;

    private boolean readyToPlay = false;

    private final Field field = new Field();

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
     * Создание сетки.
     */
    private void createGrid() {
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

                // Устанавливаем ширину и высоту
                int cellSize = 100; // Размер ячейки (можно настроить)
                params.width = cellSize;
                params.height = cellSize;

                // Устанавливаем отступы вокруг ячеек
                int marginSize = 2;
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
        boolean isSelected = field.isCellSelected(row, col);

        if (isSelected) {
            field.removeCellFromSelection(row, col);
            cell.setText("");
            cell.setBackgroundColor(Color.LTGRAY);
        } else if (field.isValidCellForSelection(row, col)) {
            field.addCellToSelection(row, col);
            cell.setText("X");
            cell.setBackgroundColor(Color.RED);
        }

        updateButtonsVisibility();
    }

    /**
     * Отображение кнопок в зависимости от кол-ва выбранных клеток
     */
    private void updateButtonsVisibility() {
        int currentShipSize = field.getCurrentShipSize();
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
        FieldObject newShip = field.createShipFromCurrentCoordinates();
        if (newShip == null) {
            Toast.makeText(
                    this,
                    this.ERROR_CREATE_TEXT,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        drawFieldObject(newShip);
        this.readyToPlay = field.isFieldReadyToPlay();
    }

    /**
     * Обработчик кнопки создания мины
     */
    private void createMine() {
        //вызов метода сервиса
        FieldObject mine = field.createMineFromCurrentCoordinates();
        if (mine == null) {
            Toast.makeText(
                    this,
                    this.ERROR_CREATE_TEXT,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        drawFieldObject(mine);
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
        for (int[] cell : field.getCurrentShipCoordinates()) {
            gridCells[cell[0]][cell[1]].setText("");
            gridCells[cell[0]][cell[1]].setBackgroundColor(Color.LTGRAY);
        }
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