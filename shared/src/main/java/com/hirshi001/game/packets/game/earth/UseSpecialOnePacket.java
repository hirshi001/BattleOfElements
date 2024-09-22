package com.hirshi001.game.packets.game.earth;

import com.badlogic.gdx.math.Vector2;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.gameworld.controller.ButtonState;
import com.hirshi001.networking.packet.Packet;

public class UseSpecialOnePacket extends Packet {

    public Vector2 direction;
    public ButtonState state;


    public UseSpecialOnePacket(Vector2 direction, ButtonState state) {
        super();
        this.direction = direction;
        this.state = state;

    }

    public UseSpecialOnePacket() {
        super();
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeFloat(direction.x);
        out.writeFloat(direction.y);
        out.writeByte((byte) state.ordinal());

    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        direction = new Vector2(in.readFloat(), in.readFloat());
        state = ButtonState.values()[in.readByte()];

    }
}
