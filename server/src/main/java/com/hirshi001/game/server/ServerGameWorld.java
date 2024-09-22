package com.hirshi001.game.server;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.gameworld.water.Water;
import com.hirshi001.game.packets.game.InitGamePacket;
import com.hirshi001.game.packets.game.SyncPacket;
import com.hirshi001.game.packets.game.entity.AddEntityPacket;
import com.hirshi001.game.packets.game.entity.DeleteEntityPacket;
import com.hirshi001.game.settings.GameSettings;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packethandlercontext.PacketType;

import java.util.List;

public class ServerGameWorld extends GameWorld {


    public ServerGameWorld(List<UUID> players) {
        super();
        this.players = new UUID[players.size()];
        players.toArray(this.players);

        for(int i = 0; i < players.size(); i++) {
            Player player = Player.getPlayerByUUID(players.get(i));
            player.lobby.gameWorld = this;
            player.channel.sendNow(new InitGamePacket(this.players), null, PacketType.TCP);

            characters[i].setUUID(player.getId());
            gameObjects.put(characters[i].getUUID(), characters[i]);
        }
    }

    @Override
    public Entity removeEntity0(UUID uuid) {
        Entity entity = super.removeEntity0(uuid);
        if(entity == null)
            return null;

        for(UUID id : players) {
            Player player = Player.getPlayerByUUID(id);
            if(player == null || player.channel == null || player.channel.isClosed())
                continue;
            try {
                player.channel.sendNow(new DeleteEntityPacket(entity.getUUID()), null, PacketType.TCP);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return entity;
    }

    @Override
    public void addEntity0(Entity entity) {
        super.addEntity0(entity);
        for(UUID player : players) {
            try {
                Player.getPlayerByUUID(player).channel.sendNow(new AddEntityPacket(entity), null, PacketType.TCP);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public void tick(float delta) {

        addEntity(new Water(this, new Vector2(-50, 15), new Vector2(1, 0).rotateDeg(MathUtils.random(-20F, 20F))));

        addEntity(new Water(this, new Vector2(50, 15), new Vector2(-1, 0).rotateDeg(MathUtils.random(-20F, 20F))));

        super.tick(delta); // performs the update

        sendSyncPackets(delta);



    }

    float time = 0;

    private void sendSyncPackets(float delta) {
        time += delta;
        boolean sync = false;
        if(time > 0.1F) {
            time = 0;
            sync = true;
        }


        // System.out.println("Sending sync packets");
        // send sync packets
        ByteBuffer syncBuffer = GameSettings.bufferFactory.buffer();
        for(Entity entity : gameObjects.values()) {
            if(!((entity.requiresSync() && sync) || entity.requiresUrgentSync()))
                continue;

            int readIndex = syncBuffer.readerIndex();
            entity.writeSyncBytes(syncBuffer);
            for(UUID player : players) {
                // System.out.println("Sending sync packet to " + player);
                syncBuffer.readerIndex(readIndex);
                try {
                    Player.getPlayerByUUID(player).channel.sendUDP(new SyncPacket(entity.getUUID(), syncBuffer), null).perform();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    public void handlePlayerMove(UUID player, Vector2 position, Vector2 newVelocity) {
        for(int i = 0; i < players.length; i++) {
            if(players[i].equals(player)) {
                characters[i].getBody().setTransform(position, 0);
                characters[i].getBody().setLinearVelocity(newVelocity);
            }
        }
    }
}
