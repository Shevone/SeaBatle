package com.example.seabattle.ui;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.seabattle.R;
import com.example.seabattle.models.user.Player;
import com.example.seabattle.services.PlayerService;

import java.util.List;

public class SwitchUserActivity extends AppCompatActivity {

    private PlayerService playerService;
    private LinearLayout usersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_user);

        usersContainer = findViewById(R.id.usersContainer);
        playerService = PlayerService.getInstance(this);
        List<Player> players = playerService.getAllPlayers();

        for (Player player : players) {
            TextView userTextView = getTextView(player);
            usersContainer.addView(userTextView);
        }
    }

    private @NonNull TextView getTextView(Player player) {
        TextView userTextView = new TextView(this);
        userTextView.setText(player.getName());
        userTextView.setTextSize(16);
        userTextView.setPadding(16, 16, 16, 16);
        userTextView.setOnClickListener(v -> {
            playerService.setCurrentPlayer(player);
            Toast.makeText(
                    this,
                    "Выбран пользователь: " + player.getName(),
                    Toast.LENGTH_SHORT).show();
            finish();
        });
        return userTextView;
    }
}