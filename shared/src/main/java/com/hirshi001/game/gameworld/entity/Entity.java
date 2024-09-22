package com.hirshi001.game.gameworld.entity;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.util.ByteBufSyncable;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packet.ByteBufSerializable;

public abstract class Entity implements ByteBufSyncable, ByteBufSerializable {

    public GameWorld world;
    protected Body body;
    protected UUID uuid;

    public float health = 100;
    boolean requiresUrgentSyc = false;

    public boolean dead = false;

    public Entity() {

    }

    public Entity(GameWorld world) {
        this.world = world;
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Body getBody() {
        return body;
    }

    public boolean requiresSync() {
        return true;
    }

    protected void urgentSync() {
        requiresUrgentSyc = true;
    }


    public boolean requiresUrgentSync() {
        boolean temp = requiresUrgentSyc;
        requiresUrgentSyc = false;
        return temp;
    }

    @Override
    public void writeSyncBytes(ByteBuffer out) {
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getAngle());
    }

    @Override
    public void readSyncBytes(ByteBuffer in) {
        body.setTransform(in.readFloat(), in.readFloat(), in.readFloat());
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
        buffer.writeFloat(body.getPosition().x);
        buffer.writeFloat(body.getPosition().y);
        buffer.writeFloat(body.getAngle());
    }

    @Override
    public void readBytes(ByteBuffer buffer) {
        uuid = new UUID(buffer.readLong(), buffer.readLong());
        body.setTransform(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public void damage(float amount) {
        health -= amount;
    }

    public void touched(Fixture other) {

    }

    public void tick(float delta) {
        if(world.isServer()) {
            if (health <= 0 || body.getPosition().y < -50) {
                world.removeEntity(this.getUUID());
            }
        }
    }

    public Nation getElement() {
        return null;
    }
}
