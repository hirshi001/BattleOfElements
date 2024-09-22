package com.hirshi001.game.packets.game.entity;

import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.gameworld.entity.Entities;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.settings.GameSettings;
import com.hirshi001.networking.packet.Packet;

public class AddEntityPacket extends Packet {

    public int id;
    public byte[] data;

    public AddEntityPacket() {
        super();
    }

    public AddEntityPacket(Entity entity) {
        super();
        this.id = Entities.getEntityId(entity);

        ByteBuffer buffer = GameSettings.bufferFactory.buffer();
        entity.writeBytes(buffer);
        this.data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeInt(id);
        out.writeInt(data.length);
        out.writeBytes(data);
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        id = in.readInt();
        int length = in.readInt();
        data = new byte[length];
        in.readBytes(data);
    }
}
