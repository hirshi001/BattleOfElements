package com.hirshi001.game.server.packets;

import com.badlogic.gdx.math.Vector2;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.gameworld.character.Character;
import com.hirshi001.game.gameworld.ground.EarthRock;
import com.hirshi001.game.packets.game.PlayerMovePacket;
import com.hirshi001.game.packets.game.earth.UseSpecialOnePacket;
import com.hirshi001.game.packets.lobby.*;
import com.hirshi001.game.packets.player.RequestPlayerUUIDPacket;
import com.hirshi001.game.packets.util.UUIDPacket;
import com.hirshi001.game.server.ServerApplication;
import com.hirshi001.game.server.ServerGameWorld;
import com.hirshi001.game.server.lobby.LobbyManager;
import com.hirshi001.game.settings.GameSettings;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packethandlercontext.PacketHandlerContext;
import com.hirshi001.networking.packethandlercontext.PacketType;
import com.hirshi001.networking.util.defaultpackets.primitivepackets.BooleanPacket;

public class PacketHandlers {

    public static void handleRequestPlayerUUIDPacket(PacketHandlerContext<RequestPlayerUUIDPacket> packet) {
        GameSettings.runnablePoster.postRunnable(() -> {
            Player player = Player.getOrCreatePlayer(null, packet.channel, packet.packet.value);
            System.out.println(player.getId());

            packet.channel.attach(player);
            packet.channel.sendNow(new UUIDPacket(player.getId()).setResponsePacket(packet.packet), null, PacketType.TCP);
        });
    }

    public static void handleFetchLobbyPacket(PacketHandlerContext<FetchLobbyPacket> packet) {
        GameSettings.runnablePoster.postRunnable(() -> {
            LobbyManager lobbyManager = ServerApplication.instance().lobbyManager;

            LobbyBatchPacket batchPacket = new LobbyBatchPacket(lobbyManager.getLobbyData());
            packet.channel.sendNow(batchPacket.setResponsePacket(packet.packet), null, PacketType.TCP);
        });
    }


    public static void handleJoinLobbyPacket(PacketHandlerContext<JoinLobbyPacket> packet) {
        GameSettings.runnablePoster.postRunnable(() -> {
            Object attached = packet.channel.getAttachment();
            if (!(attached instanceof Player)) {
                packet.channel.sendNow(new BooleanPacket(false).setResponsePacket(packet.packet), null, PacketType.TCP);
                return;
            }
            Player player = (Player) attached;
            if (player.lobby != null) {
                packet.channel.sendNow(new BooleanPacket(false).setResponsePacket(packet.packet), null, PacketType.TCP);
                return;
            }

            LobbyManager lobbyManager = ServerApplication.instance().lobbyManager;

            UUID lobbyId = packet.packet.uuid;

            if (lobbyManager.addPlayerToLobby(player.getId(), lobbyId))
                packet.channel.sendNow(new LobbyPacket(lobbyManager.getLobby(lobbyId)).setResponsePacket(packet.packet), null, PacketType.TCP);
            else
                packet.channel.sendNow(new BooleanPacket(false).setResponsePacket(packet.packet), null, PacketType.TCP);
        });
    }

    public static void leaveLobbyPacket(PacketHandlerContext<LeaveLobbyPacket> packet) {
        GameSettings.runnablePoster.postRunnable(() -> {
            Object attached = packet.channel.getAttachment();
            if (!(attached instanceof Player)) {
                return;
            }

            Player player = (Player) attached;
            if (player.lobby == null)
                return;

            LobbyManager lobbyManager = ServerApplication.instance().lobbyManager;

            UUID lobbyId = player.lobby.getLobbyData().getId();

            lobbyManager.removePlayerFromLobby(player.getId(), lobbyId);
        });
    }

    public static void handlePlayerMovePacket(PacketHandlerContext<PlayerMovePacket> packet) {
        GameSettings.runnablePoster.postRunnable(() -> {
            Object attached = packet.channel.getAttachment();
            if (!(attached instanceof Player)) {
                return;
            }

            Player player = (Player) attached;
            if (player.lobby == null)
                return;

            ((ServerGameWorld) player.lobby.gameWorld).handlePlayerMove(player.getId(), packet.packet.newPosition, packet.packet.newVelocity);
        });
    }

    public static void handleUseSpecialOnePacket(PacketHandlerContext<UseSpecialOnePacket> packet) {
        GameSettings.runnablePoster.postRunnable(() -> {
            Object attached = packet.channel.getAttachment();
            if (!(attached instanceof Player)) {
                return;
            }

            Player player = (Player) attached;
            if (player.lobby == null)
                return;

            player.getCharacter().useSpecialOne(packet.packet.direction, packet.packet.state);

        });
    }



}
