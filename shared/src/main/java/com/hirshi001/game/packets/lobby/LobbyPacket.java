package com.hirshi001.game.packets.lobby;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.buffer.util.ByteBufUtil;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.connection.PlayerReference;
import com.hirshi001.game.lobby.Lobby;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class LobbyPacket extends Packet {

    public String name;
    public UUID lobbyId;
    public int maxPlayers;
    public int playerCount;

    public List<PlayerReference> players = new ArrayList<>();

    public LobbyPacket() {
    }

    public LobbyPacket(Lobby lobby) {
        this.name = lobby.getLobbyData().getName();
        this.lobbyId = lobby.getLobbyData().getId();
        this.maxPlayers = lobby.getLobbyData().getMaxPlayers();
        this.playerCount = lobby.getLobbyData().getPlayerCount();
        for(UUID uuid : lobby.getPlayers()){
            PlayerReference playerReference = new PlayerReference(uuid, Player.getPlayerByUUID(uuid).name);
            players.add(playerReference);
        }
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        ByteBufUtil.writeStringToBuf(name, out);
        out.writeLong(lobbyId.getMostSignificantBits());
        out.writeLong(lobbyId.getLeastSignificantBits());
        out.writeInt(maxPlayers);
        out.writeInt(playerCount);

        for(PlayerReference uuid : players){
            ByteBufUtil.writeStringToBuf(uuid.name, out);
            out.writeLong(uuid.id.getMostSignificantBits());
            out.writeLong(uuid.id.getLeastSignificantBits());
        }
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);

        name = ByteBufUtil.readStringFromBuf(in);
        long mostSigBits = in.readLong();
        long leastSigBits = in.readLong();
        lobbyId = new UUID(mostSigBits, leastSigBits);
        maxPlayers = in.readInt();
        playerCount = in.readInt();

        for(int i = 0; i < playerCount; i++){
            String playerName = ByteBufUtil.readStringFromBuf(in);
            long playerMostSigBits = in.readLong();
            long playerLeastSigBits = in.readLong();
            UUID playerId = new UUID(playerMostSigBits, playerLeastSigBits);

            PlayerReference playerReference = new PlayerReference(playerId, playerName);
            players.add(playerReference);
        }
    }
}
