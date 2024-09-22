package com.hirshi001.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.hirshi001.game.connection.PlayerReference;
import com.hirshi001.game.gamescreen.GameScreen;
import com.hirshi001.game.gameworld.ClientGameWorld;
import com.hirshi001.game.gameworld.entity.Entities;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.joinroom.JoinRoomScreen;
import com.hirshi001.game.lobby.Lobby;
import com.hirshi001.game.lobbyscreen.LobbyScreen;
import com.hirshi001.game.packets.game.InitGamePacket;
import com.hirshi001.game.packets.game.SyncPacket;
import com.hirshi001.game.packets.game.entity.AddEntityPacket;
import com.hirshi001.game.packets.game.entity.DeleteEntityPacket;
import com.hirshi001.game.packets.lobby.PlayerUpdateLobbyPacket;
import com.hirshi001.game.settings.GameSettings;
import com.hirshi001.networking.packethandlercontext.PacketHandlerContext;

public class PacketHandlers {

    public static void handlePlayerUpdateLobbyPacket(PacketHandlerContext<PlayerUpdateLobbyPacket> packet) {
        Gdx.app.postRunnable(() -> {


            Lobby lobby = GameApp.getInstance().player.lobby;
            if (lobby == null)
                return;

            PlayerReference playerReference = packet.packet.playerReference;

            assert packet.packet.lobbyUUID.equals(lobby.getLobbyData().getId());
            if (packet.packet.add) {
                PlayerReference.addPlayerReference(playerReference);
                lobby.addPlayer(playerReference.id);
            } else {

                PlayerReference.removePlayerReference(playerReference.id);
                lobby.removePlayer(playerReference.id);

                if (playerReference.id.equals(GameApp.getInstance().player.getId()))
                    GameApp.getInstance().setScreen(new JoinRoomScreen());
            }

            Screen screen = GameApp.getInstance().getScreen();
            if (screen instanceof LobbyScreen) {
                ((LobbyScreen) screen).updateLobby();
            }
        });
    }

    public static void handleInitGamePacket(PacketHandlerContext<InitGamePacket> packet) {
        Gdx.app.postRunnable(() -> {
            GameApp.getInstance().player.lobby.gameWorld = new ClientGameWorld();

            GameApp.getInstance().setScreen(new GameScreen());
            ClientGameWorld gameWorld = (ClientGameWorld) GameApp.getInstance().player.lobby.gameWorld;
            gameWorld.initGame(packet.packet);
        });
    }

    public static void handleSyncPacket(PacketHandlerContext<SyncPacket> packet) {
        Gdx.app.postRunnable(() -> {
            ((ClientGameWorld)GameApp.getInstance().player.lobby.gameWorld).sync(packet.packet);
        });
    }

    public static void handleDeleteEntityPacket(PacketHandlerContext<DeleteEntityPacket> packet) {
        Gdx.app.postRunnable(() -> {
            GameApp.getInstance().player.lobby.gameWorld.removeEntity(packet.packet.uuid);
        });
    }

    public static void handleAddEntityPacket(PacketHandlerContext<AddEntityPacket> packet) {
        Gdx.app.postRunnable(() -> {
            Entity entity = Entities.createEntity(packet.packet.id);
            entity.world = GameApp.getInstance().player.lobby.gameWorld;
            entity.readBytes(GameSettings.bufferFactory.wrap(packet.packet.data));
            GameApp.getInstance().player.lobby.gameWorld.addEntity(entity);
        });
    }

}
