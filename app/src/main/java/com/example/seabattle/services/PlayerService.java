package com.example.seabattle.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.seabattle.models.user.Bot;
import com.example.seabattle.models.user.Player;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerService {
    private static final String PLAYERS_FILE_NAME = "players.json";
    private static final AtomicInteger playerIdCounter = new AtomicInteger(1);
    private final Context context;
    private Player currentPlayer;
    private final Gson gson;
    private List<Player> players = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    private static PlayerService instance;

    public static synchronized PlayerService getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerService(context.getApplicationContext());
        }
        return instance;
    }


    private PlayerService(Context context) {
        this.context = context;
        this.gson = new Gson();
        loadPlayers();
        if (!players.isEmpty()) {
            if (currentPlayer == null) {
                currentPlayer = players.get(0);
            }
        }
    }

    /**
     * Создает нового игрока и сохраняет его в файл.
     *
     * @param name Имя игрока.
     * @return Новый объект Player.
     */
    public Player createPlayer(String name) {
        int id = playerIdCounter.getAndIncrement();
        Player newPlayer = new Player(id, name);
        players.add(newPlayer);
        savePlayers();
        return newPlayer;
    }

    /**
     * Возвращает текущего игрока.
     *
     * @return Текущий игрок.
     */
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * Устанавливает текущего игрока.
     *
     * @param player Объект игрока.
     */
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    /**
     * Возвращает всех игроков из файла.
     *
     * @return Список всех игроков.
     */
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players);
    }

    private void savePlayers() {
        File file = new File(context.getFilesDir(), PLAYERS_FILE_NAME);
        try (FileWriter writer = new FileWriter(file)) {
            String json = gson.toJson(players);
            writer.write(json);
        } catch (IOException e) {
            Log.e("PlayerService", "Error saving players: " + e.getMessage());
        }
    }

    private void loadPlayers() {
        File file = new File(context.getFilesDir(), PLAYERS_FILE_NAME);
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Player>>() {
            }.getType();
            players = gson.fromJson(reader, listType);
            if (players == null) {
                players = new ArrayList<>();
            } else {
                if (!players.isEmpty()) {
                    playerIdCounter.set(players.get(players.size() - 1).getID() + 1);
                }
            }
        } catch (IOException e) {
            Log.e("PlayerService", "Error load players: " + e.getMessage());
        }
    }
}