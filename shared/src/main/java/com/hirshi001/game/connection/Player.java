package com.hirshi001.game.connection;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.buffer.util.ByteBufUtil;
import com.hirshi001.game.gameworld.character.Character;
import com.hirshi001.game.lobby.Lobby;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.packet.ByteBufSerializable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Player {


    static Map<UUID, Player> idToPlayer;

    static {
        idToPlayer = new ConcurrentHashMap<>();
    }

    public static Player getPlayerByUUID(UUID id) {
        return idToPlayer.get(id);
    }

    /**
     * Get or create a player by their id/channel.
     *
     * @param id      the id of the player to search for
     * @param channel the channel of the player to search and use to create the player if not found
     * @param name    the name of the player to use to create the object
     * @return the player object
     */
    public static Player getOrCreatePlayer(UUID id, Channel channel, String name) {
        Object attached = channel.getAttachment();
        if (attached instanceof Player) {
            return (Player) attached;
        }

        if (id != null) {
            Player player = idToPlayer.get(id);
            if (player != null)
                return player;

            player = new Player(name, channel, id);
            idToPlayer.put(id, player);
            channel.attach(player);
            return player;
        }
        return createPlayer(channel, name);
    }

    private static Player createPlayer(Channel channel, String name) {
        UUID newUUID = UUID.randomUUID();
        while (idToPlayer.containsKey(newUUID)) {
            newUUID = UUID.randomUUID();
        }
        Player player = new Player(name, channel, newUUID);

        idToPlayer.put(newUUID, player);
        channel.attach(player);

        return player;
    }

    public static void removePlayer(UUID id) {
        if(id == null)
            return;
        idToPlayer.remove(id);
    }

    public static void cleanup() {
        idToPlayer.values().removeIf(player -> player.channel != null && player.channel.isClosed());
    }


    public String name;
    public Channel channel;
    private final UUID id;
    public Lobby lobby;

    private Player(String name, Channel channel, UUID id) {
        this.name = name;
        this.channel = channel;
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public Character getCharacter() {
        return (Character) lobby.gameWorld.gameObjects.get(id);
    }

}
