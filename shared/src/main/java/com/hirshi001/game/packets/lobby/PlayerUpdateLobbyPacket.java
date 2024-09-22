package com.hirshi001.game.packets.lobby;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.buffer.util.ByteBufUtil;
import com.hirshi001.game.connection.PlayerReference;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.Packet;

public class PlayerUpdateLobbyPacket extends Packet {


    public UUID lobbyUUID;
    public PlayerReference playerReference;
    public boolean add;

    public PlayerUpdateLobbyPacket() {
    }

    public PlayerUpdateLobbyPacket(UUID lobbyUUID, PlayerReference playerReference, boolean add) {
        this.lobbyUUID = lobbyUUID;
        this.playerReference = playerReference;
        this.add = add;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeLong(lobbyUUID.getMostSignificantBits());
        out.writeLong(lobbyUUID.getLeastSignificantBits());

        out.writeBoolean(add);

        ByteBufUtil.writeStringToBuf(playerReference.name, out);
        out.writeLong(playerReference.id.getMostSignificantBits());
        out.writeLong(playerReference.id.getLeastSignificantBits());
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        lobbyUUID = new UUID(in.readLong(), in.readLong());

        add = in.readBoolean();

        String name = ByteBufUtil.readStringFromBuf(in);
        playerReference = new PlayerReference(new UUID(in.readLong(), in.readLong()), name);
    }
}
