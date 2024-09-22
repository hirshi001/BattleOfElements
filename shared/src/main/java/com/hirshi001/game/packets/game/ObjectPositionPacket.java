package com.hirshi001.game.packets.game;

import com.badlogic.gdx.math.Vector2;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.Packet;

public class ObjectPositionPacket extends Packet {

    public Vector2 newPosition;
    public UUID objectUUID;

    public ObjectPositionPacket() {
        super();
    }

    public ObjectPositionPacket(UUID objectUUID, Vector2 newPosition) {
        this.objectUUID = objectUUID;
        this.newPosition = newPosition;
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeFloat(newPosition.x);
        out.writeFloat(newPosition.y);
        out.writeLong(objectUUID.getMostSignificantBits());
        out.writeLong(objectUUID.getLeastSignificantBits());

    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        newPosition = new Vector2(in.readFloat(), in.readFloat());
        objectUUID = new UUID(in.readLong(), in.readLong());
    }
}
