package com.hirshi001.game.packets.game;

import com.badlogic.gdx.math.Vector2;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.gameworld.character.Character;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.Packet;

public class PlayerMovePacket extends Packet {

    public Vector2 newPosition;
    public Vector2 newVelocity;
    public Vector2 newDirection;

    public PlayerMovePacket(Vector2 newPosition, Vector2 newVelocity, Vector2 newDirection) {
        this.newPosition = newPosition;
        this.newVelocity = newVelocity;
        this.newDirection = newDirection;
    }


    public PlayerMovePacket(Character character) {
        this.newPosition = character.getBody().getPosition().cpy();
        this.newVelocity = character.getBody().getLinearVelocity().cpy();
        this.newDirection = character.direction.cpy();
    }


    public PlayerMovePacket() {
        super();
    }

    @Override
    public void writeBytes(ByteBuffer out) {
        super.writeBytes(out);
        out.writeFloat(newPosition.x);
        out.writeFloat(newPosition.y);
        out.writeFloat(newVelocity.x);
        out.writeFloat(newVelocity.y);
        out.writeFloat(newDirection.x);
        out.writeFloat(newDirection.y);
    }

    @Override
    public void readBytes(ByteBuffer in) {
        super.readBytes(in);
        newPosition = new Vector2(in.readFloat(), in.readFloat());
        newVelocity = new Vector2(in.readFloat(), in.readFloat());
        newDirection = new Vector2(in.readFloat(), in.readFloat());
    }
}
