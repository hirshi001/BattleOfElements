package com.hirshi001.game.packets.game;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.Packet;

public class InitGamePacket extends Packet {

    public UUID[] playerUUIDs;

    public InitGamePacket() {
        super();
    }

    public InitGamePacket(UUID[] playerUUIDs) {
        super();
        this.playerUUIDs = playerUUIDs;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeInt(playerUUIDs.length);
        for(UUID uuid : playerUUIDs) {
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        }

    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        int length = in.readInt();
        playerUUIDs = new UUID[length];
        for(int i = 0; i < length; i++) {
            playerUUIDs[i] = new UUID(in.readLong(), in.readLong());
        }
    }
}
