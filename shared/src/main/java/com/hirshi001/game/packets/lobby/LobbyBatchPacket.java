package com.hirshi001.game.packets.lobby;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.buffer.util.ByteBufUtil;
import com.hirshi001.game.lobby.Lobby;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.Packet;


public class LobbyBatchPacket extends Packet {


    public Lobby.LobbyData[] lobbies;

    public LobbyBatchPacket() {
        super();
    }

    public LobbyBatchPacket(Lobby.LobbyData[] lobbies) {
        super();
        this.lobbies = lobbies;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeInt(lobbies.length);
        for (Lobby.LobbyData lobby : lobbies) {
            ByteBufUtil.writeStringToBuf(lobby.getName(), out);
            out.writeInt(lobby.getPlayerCount());
            out.writeInt(lobby.getMaxPlayers());
            out.writeLong(lobby.getId().getMostSignificantBits());
            out.writeLong(lobby.getId().getLeastSignificantBits());
        }
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        int length = in.readInt();
        lobbies = new Lobby.LobbyData[length];
        for (int i = 0; i < length; i++) {
            String name = ByteBufUtil.readStringFromBuf(in);
            int playerCount = in.readInt();
            int maxPlayers = in.readInt();
            long mostSigBits = in.readLong();
            long leastSigBits = in.readLong();
            UUID id = new UUID(mostSigBits, leastSigBits);
            Lobby.LobbyData lobby = new Lobby.LobbyData(name, playerCount, maxPlayers, id);
            lobbies[i] = lobby;
        }
    }
}
