package com.example.seabattle.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.seabattle.R;
import com.example.seabattle.models.user.Player;
import com.example.seabattle.services.PlayerService;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PlayerService playerService;
    private Button startButton;
    private Button newUserButton;
    private Button switchUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerService = PlayerService.getInstance(this);

        startButton = findViewById(R.id.startButton);
        newUserButton = findViewById(R.id.newUserButton);
        switchUserButton = findViewById(R.id.switchUserButton);


        startButton.setOnClickListener(v -> {
            Player currentPlayer = playerService.getCurrentPlayer();

            if (currentPlayer != null) {
                // Переход к GameActivity
                Intent intent = new Intent(this, SetupBoardActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(
                        this,
                        "Нет текущего пользователя. Создайте нового или выберите существующего.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        newUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewUserActivity.class);
            startActivity(intent);
        });

        switchUserButton.setOnClickListener(v -> {
            List<Player> players = playerService.getAllPlayers();
            if (players.isEmpty()) {
                Toast.makeText(this,
                        "Нет зарегистрированных пользователей",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, SwitchUserActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Player currentPlayer = playerService.getCurrentPlayer();
        if (currentPlayer != null) {
            Toast.makeText(this,
                    "Добро пожаловать, " + currentPlayer.getName(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}