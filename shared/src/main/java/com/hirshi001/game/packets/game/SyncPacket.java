package com.hirshi001.game.packets.game;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.Packet;

public class SyncPacket extends Packet {


    public UUID entityId;
    public byte[] data;

    public SyncPacket() {
        super();
    }


    public SyncPacket(UUID entityId, ByteBuffer buffer) {
        super();
        this.entityId = entityId;
        data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeLong(entityId.getMostSignificantBits());
        out.writeLong(entityId.getLeastSignificantBits());
        out.writeInt(data.length);
        out.writeBytes(data);
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        entityId = new UUID(in.readLong(), in.readLong());
        data = new byte[in.readInt()];
        in.readBytes(data);
    }
}
