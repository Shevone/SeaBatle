package com.example.seabattle.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.seabattle.R;
import com.example.seabattle.models.user.Player;
import com.example.seabattle.services.PlayerService;

public class NewUserActivity extends AppCompatActivity {

    private EditText newPlayerNameEditText;
    private PlayerService playerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        newPlayerNameEditText = findViewById(R.id.newPlayerNameEditText);
        Button createPlayerButton = findViewById(R.id.createPlayerButton);
        playerService = PlayerService.getInstance(this);

        createPlayerButton.setOnClickListener(v -> {
            String newPlayerName = newPlayerNameEditText.getText().toString().trim();
            if (newPlayerName.isEmpty()) {
                Toast.makeText(
                        this,
                        "Пожалуйста, введите имя.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            Player newPlayer = playerService.createPlayer(newPlayerName);
            playerService.setCurrentPlayer(newPlayer);
            Toast.makeText(this,
                    "Пользователь " + newPlayer.getName() + " создан!",
                    Toast.LENGTH_SHORT).show();
            finish(); // Возвращаемся в MainActivity
        });
    }
}