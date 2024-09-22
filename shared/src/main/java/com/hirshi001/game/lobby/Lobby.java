package com.hirshi001.game.lobby;

import com.hirshi001.game.connection.Player;
import com.hirshi001.game.connection.PlayerReference;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.util.UUID;

import java.util.ArrayList;
import java.util.List;

public class Lobby {


    public static class LobbyData {
        private String name;
        private int playerCount;
        private int maxPlayers;
        private UUID id;

        public LobbyData() {
        }

        public LobbyData(String name, int playerCount, int maxPlayers, UUID id) {
            this.name = name;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public UUID getId() {
            return id;
        }
    }

    private final LobbyData lobbyData;

    private final ArrayList<UUID> players = new ArrayList<>();

    public boolean started;

    public GameWorld gameWorld;


    public Lobby(UUID uuid, String name, int limit) {
        this.lobbyData = new LobbyData();
        this.lobbyData.id = uuid;
        this.lobbyData.name = name;
        this.lobbyData.maxPlayers = limit;
    }

    public Lobby(LobbyData lobbyData) {
        this.lobbyData = lobbyData;
    }

    public Lobby(String name, int limit) {
        this(UUID.randomUUID(), name, limit);
    }

    public LobbyData getLobbyData() {
        return lobbyData;
    }

    public boolean addPlayer(UUID player) {

        if (players.size() == lobbyData.maxPlayers) {
            return false;
        }
        boolean result = players.add(player);
        lobbyData.playerCount = players.size();

        return result;
    }

    public boolean removePlayer(UUID player) {
        boolean result = players.remove(player);
        lobbyData.playerCount = players.size();
        // gameWorld.removePlayer(player);
        return result;
    }

    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }

    public boolean isFull() {
        return lobbyData.playerCount == lobbyData.maxPlayers;
    }
}
