package com.example.seabattle.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.seabattle.R;
import com.example.seabattle.helpers.CustomAlertDialog;
import com.example.seabattle.helpers.Timer;
import com.example.seabattle.models.field.object.FieldObject;
import com.example.seabattle.models.game.ShotResult;
import com.example.seabattle.models.user.Player;
import com.example.seabattle.services.BotService;
import com.example.seabattle.services.PlayerService;
import com.example.seabattle.services.VibrationService;
import com.example.seabattle.services.game.GameObserver;
import com.example.seabattle.services.game.GameService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity implements GameObserver {

    private final Map<Integer, TextView[][]> cellsByPlayer = new HashMap<>();
    private int maxCellWidth = 0;

    private VibrationService vibrationService;

    private GameService gameService;

    private Player player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        vibrationService = new VibrationService(this);
        // Инициализация сервиса игры

        gameService = GameService.getInstance();
        gameService.registerObserver(this);
        // Получение пользователей
        PlayerService playerService = PlayerService.getInstance(this);
        this.player = playerService.getCurrentPlayer();

        BotService botService = BotService.getInstance();
        Player bot = botService.getBot();

        gameService.setPlayer1(player);
        gameService.setPlayer2(bot);

        // Инициализация разметки
        GridLayout player1Grid = findViewById(R.id.player1_grid);
        GridLayout player2Grid = findViewById(R.id.player2_grid);

        TextView[][] currentPlayerCells = this.createCell(this.player.getID());
        TextView[][] oppCells = this.createCell(bot.getID());


        createGrid(player1Grid, currentPlayerCells, false);
        createGrid(player2Grid, oppCells, true);


        ViewTreeObserver viewTreeObserver = player1Grid.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                player1Grid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int cellWidth;
                cellWidth = currentPlayerCells[0][0].getWidth();
                if (cellWidth > maxCellWidth) maxCellWidth = cellWidth;

                cellWidth = oppCells[0][0].getWidth();
                if (cellWidth > maxCellWidth) maxCellWidth = cellWidth;

                for (int row = 0; row < 10; row++) {
                    for (int col = 0; col < 10; col++) {
                        currentPlayerCells[row][col].setWidth(maxCellWidth);
                        oppCells[row][col].setWidth(maxCellWidth);
                    }
                }
            }
        });

        this.drawBoard(currentPlayerCells, player.getPlayerBoard());
        this.drawBoard(oppCells, bot.getPlayerBoard());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameService.unregisterObserver(this);
    }

    /**
     * Отрисовка объектов на сетке
     *
     * @param cells сетка
     * @param board сетка в виде массива
     */
    private void drawBoard(TextView[][] cells, FieldObject[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int m = 0; m < board[i].length; m++) {
                FieldObject obj = board[i][m];
                if (obj == null) {
                    continue;
                }
                cells[i][m].setText(obj.getSign());
                cells[i][m].setBackgroundColor(obj.getColor());
            }
        }
    }

    /**
     * Создание сетки с привязкой к playerID
     *
     * @param playerID ID игрока
     * @return TextView[][]
     */
    private TextView[][] createCell(int playerID) {
        TextView[][] cells = new TextView[10][10];
        cellsByPlayer.put(
                playerID,
                cells
        );
        return cells;
    }

    /**
     * Остановка активити
     */
    @Override
    protected void onStop() {
        super.onStop();
//        timer.stop();
    }

    /**
     * Объявление кастомного меню
     *
     * @param menu Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    /**
     * На нажаите элемента меню
     *
     * @param item MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обрабатываем нажатие на кнопку меню
        if (item.getItemId() == R.id.action_surrender) {
            // Здесь код который должен вызываться при нажатии кнопки "Сдаться"
            makeToast("Вы сдались!");
            finish();// Завершаем игру
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Обработчик нажаитя на "соты"
     *
     * @param row  int
     * @param col  int
     */
    private void onCellClick(int row, int col) {
        // Сейчас ход текущего игрока?
        int currentPlayerID = player.getID();
        if (!gameService.isTurn(currentPlayerID)) {
            makeToast("Сейчас не ваш ход!");
            return;
        }

        if (!gameService.isPointAvailableToShot(currentPlayerID, row, col)) {
            makeToast("Точка не доступна для хода");
            return;
        }

        gameService.shot(currentPlayerID, row, col);
    }

    /**
     * Соббытие - уничтожение объекта
     * Воспроизводит длинную вибриацию и рисует границу вокруг объекта
     *
     * @param playerIDGotShot ID игрока по которому стреляют
     * @param playerID        ID стреляющего игрока
     */
    private void handleDestroy(int playerIDGotShot, int playerID) {
        vibrationService.vibrateLong();
        FieldObject lastDestroyed = gameService.getLastDestroyedObject(playerIDGotShot);
        List<int[]> points = new ArrayList<>();
        for (int[] coordinate : lastDestroyed.getCoordinates()) {
            int x = coordinate[0];
            int y = coordinate[1];
            for (int i = x - 1; i <= x + 1; i++) {
                if (i >= 0 && i < 10) {
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (j >= 0 && j < 10) {
                            points.add(new int[]{i, j});
                            gameService.addPointToShotList(playerID, i, j);
                        }
                    }
                }
            }
        }

        drawCells(
                cellsByPlayer.get(playerIDGotShot),
                points,
                "x",
                Color.BLUE
        );

        drawCells(
                cellsByPlayer.get(playerIDGotShot),
                lastDestroyed.getCoordinates(),
                lastDestroyed.getSign(),
                Color.RED
        );

        if (gameService.isLose(playerIDGotShot)) {
            CustomAlertDialog alertDialog = new CustomAlertDialog(this);
            alertDialog.showAlertDialog("Результат",
                    "Победил: " + gameService.getOppName(playerIDGotShot),
                    "Понял!",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            returnToMainMenu();
                        }
                    }
            );
        }
    }

    /**
     * Создание сетки поля
     *
     * @param gridLayout  GridLayout
     * @param gridCells   TextView[][]
     * @param isClickable boolean - клибалельно ли поле.
     */
    private void createGrid(GridLayout gridLayout, TextView[][] gridCells, boolean isClickable) {
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
                int marginSize = 2; // Размер отступа
                params.setMargins(marginSize, marginSize, marginSize, marginSize);
                cellFrame.setLayoutParams(params);

                cellFrame.setBackgroundColor(Color.BLACK);

                gridCells[row][col] = cell;
                if (isClickable) {
                    int finalRow = row;
                    int finalCol = col;
                    cell.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onCellClick(finalRow, finalCol);
                        }
                    });
                }
                gridLayout.addView(cellFrame);

            }
        }
    }

    /**
     * Отрисовка точек по переданных координатам
     *
     * @param gridCells   сетка
     * @param coordinates координаты для отрисовки
     * @param cellText    текст помещаемый внутрь "соты"
     * @param cellColor   цвет "соты"
     */
    private void drawCells(
            TextView[][] gridCells,
            List<int[]> coordinates,
            String cellText,
            int cellColor) {
        for (int[] cell : coordinates) {
            gridCells[cell[0]][cell[1]].setText(cellText);
            gridCells[cell[0]][cell[1]].setBackgroundColor(cellColor);
        }
    }

    /**
     * Возврат в главное меню
     */
    private void returnToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Метод вызывается после соверщения хода.
     *
     * @param result ShotResult Результат хода
     */
    @Override
    public void onStepCompleted(ShotResult result) {
        int playerIDGotShot = result.getGotShotPlayerID();
        TextView[][] cells = cellsByPlayer.get(playerIDGotShot);
        assert cells != null;

        int[] shootCoordinate = result.getShotCoordinate();
        TextView cell = cells[shootCoordinate[0]][shootCoordinate[1]];

        switch (result.getResult()) {
            case MISS:
                cell.setBackgroundColor(Color.BLUE);
                break;
            case HIT:
                cell.setBackgroundColor(Color.RED);
                vibrationService.vibrateShort();
                break;
            case MINE_HIT:
                cell.setBackgroundColor(Color.YELLOW);
                handleDestroy(playerIDGotShot, result.getCurrentPlayerID());
                break;
            case DESTROYED:
                cell.setBackgroundColor(Color.RED);
                handleDestroy(playerIDGotShot, result.getCurrentPlayerID());
                break;
            case IMPOSSIBLE:
                makeToast("Ошибка!");
                break;
        }
    }

    /**
     * Показывает тост в актитивти
     *
     * @param text Текст тоста
     */
    private void makeToast(String text) {
        Toast.makeText(
                this,
                text,
                Toast.LENGTH_SHORT).show();
    }
}