package com.hirshi001.game.packets.util;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.Packet;


public class UUIDPacket extends Packet {

    public UUID uuid;

    public UUIDPacket() {
        super();
    }


    public UUIDPacket(UUID uuid) {
        super();
        this.uuid = uuid;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        long mostSigBits = in.readLong();
        long leastSigBits = in.readLong();
        uuid = new UUID(mostSigBits, leastSigBits);
    }
}
