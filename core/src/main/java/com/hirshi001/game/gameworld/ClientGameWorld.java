package com.hirshi001.game.gameworld;

import com.badlogic.gdx.InputProcessor;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.GameApp;
import com.hirshi001.game.gameworld.character.Character;
import com.hirshi001.game.gameworld.controller.UserCharacterController;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.packets.game.InitGamePacket;
import com.hirshi001.game.packets.game.PlayerMovePacket;
import com.hirshi001.game.packets.game.SyncPacket;
import com.hirshi001.game.settings.GameSettings;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packethandlercontext.PacketType;

import java.util.Arrays;

public class ClientGameWorld extends GameWorld {

    UserCharacterController controller;

    public Character clientCharacter;

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public boolean isClient() {
        return true;
    }

    public void initGame(InitGamePacket packet) {
        players = packet.playerUUIDs;
        System.out.println(Arrays.toString(players));
        System.out.println(GameApp.getInstance().player.getId());
        for(int i = 0; i < players.length; i++) {
            if(players[i].equals(GameApp.getInstance().player.getId())) {
                clientCharacter = getCharacters()[i];
            }

            characters[i].setUUID(players[i]);
            gameObjects.put(characters[i].getUUID(), characters[i]);
        }

        controller = new UserCharacterController(clientCharacter);
        clientCharacter.isClientControlled = true;
    }

    @Override
    public void tick(float delta) {
        super.tick(delta);

        if(clientCharacter != null && (controller.moving() || controller.updated())) {
            GameApp.getInstance().player.channel.sendUDP(new PlayerMovePacket(clientCharacter), null).perform();
        }
    }

    public void sync(SyncPacket packet) {
        ByteBuffer syncData = GameSettings.bufferFactory.buffer(packet.data);

        System.out.println("Received sync packet");
        UUID uuid = packet.entityId;
        Entity entity = gameObjects.get(uuid);
        if(entity == null) {
            System.out.println("Null entity when syncing on Client: " + uuid);
            return;
        }
        entity.readSyncBytes(syncData);
        System.out.println("Synced " + uuid + " : " + entity);

    }

    public InputProcessor getInputProcessor() {
        return controller;
    }

}
