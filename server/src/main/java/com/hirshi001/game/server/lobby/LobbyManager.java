package com.hirshi001.game.server.lobby;

import com.badlogic.gdx.Gdx;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.connection.PlayerReference;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.lobby.Lobby;
import com.hirshi001.game.packets.lobby.PlayerUpdateLobbyPacket;
import com.hirshi001.game.server.ServerGameWorld;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packethandlercontext.PacketType;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager {

    Map<UUID, Lobby> lobbies = new ConcurrentHashMap<>();

    public boolean addPlayerToLobby(UUID playerId, UUID lobbyId) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            return false;
        }

        Player player = Player.getPlayerByUUID(playerId);
        if (player == null)
            return false;

        boolean result = lobby.addPlayer(playerId);
        PlayerReference newPlayer = new PlayerReference(playerId, player.name);

        if (result) {
            player.lobby = lobby;
            for (UUID uuid : lobby.getPlayers()) {
                if (uuid.equals(playerId))
                    continue;

                Player.getPlayerByUUID(uuid).channel.sendNow(
                        new PlayerUpdateLobbyPacket(
                                lobby.getLobbyData().getId(),
                                newPlayer,
                                true),
                        null, PacketType.TCP);
            }
        }

        return result;
    }

    public boolean removePlayerFromLobby(UUID playerId, UUID lobbyId) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null) {
            return false;
        }


        Player player = Player.getPlayerByUUID(playerId);
        if (player == null)
            return false;

        boolean result = lobby.removePlayer(playerId);
        if (result) {
            player.lobby = null;
            for (UUID uuid : lobby.getPlayers()) {
                if (uuid.equals(playerId))
                    continue;

                Player otherPlayer = Player.getPlayerByUUID(uuid);
                if (otherPlayer.channel == null || otherPlayer.channel.isClosed())
                    continue;
                try {
                    otherPlayer.channel.sendNow(
                            new PlayerUpdateLobbyPacket(
                                    lobby.getLobbyData().getId(),
                                    new PlayerReference(playerId, player.name),
                                    false),
                            null, PacketType.TCP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public Lobby getLobby(UUID lobbyId) {
        return lobbies.get(lobbyId);
    }

    public void update() {
        for (Lobby lobby : lobbies.values()) {
            if (lobby.isFull() && !lobby.started) {
                try {
                    startGame(lobby);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        while (lobbies.size() < 4) {
            Lobby lobby = new Lobby("A lobby name", 4);
            lobbies.put(lobby.getLobbyData().getId(), lobby);
        }

        for (Lobby lobby : lobbies.values()) {
            if (lobby.started && lobby.gameWorld != null) {
                try {
                    lobby.gameWorld.tick(Gdx.graphics.getDeltaTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        cleanup();
    }

    private void cleanup() {
        for (Map.Entry<UUID, Lobby> entry : lobbies.entrySet()) {
            Lobby lobby = entry.getValue();
            for (UUID playerId : lobby.getPlayers()) {
                Player player = Player.getPlayerByUUID(playerId);
                if (player == null
                        || player.channel == null
                        || player.channel.isClosed()
                        || player.lobby == null
                        || !player.lobby.getLobbyData().getId().equals(lobby.getLobbyData().getId())) {
                    lobby.removePlayer(playerId);
                    if(player !=null) player.lobby = null;
                }
            }
        }
    }

    public void startGame(Lobby lobby) {
        lobby.started = true;
        lobby.gameWorld = new ServerGameWorld(lobby.getPlayers());
    }

    public void onPlayerDisconnect(Player player) {
        if (player.lobby != null) {
            removePlayerFromLobby(player.getId(), player.lobby.getLobbyData().getId());
        }

        for (Lobby lobby : lobbies.values()) {
            lobby.removePlayer(player.getId());
        }
    }

    public Lobby.LobbyData[] getLobbyData() {
        Lobby.LobbyData[] data = new Lobby.LobbyData[lobbies.size()];
        int i = 0;
        for (Lobby lobby : lobbies.values()) {
            data[i] = lobby.getLobbyData();
            i++;
        }
        return data;
    }

}
