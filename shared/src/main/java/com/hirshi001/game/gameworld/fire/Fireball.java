package com.hirshi001.game.gameworld.fire;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.util.UUID;

import java.util.Objects;

public class Fireball extends Entity {

    public static BodyDef bodyDef;
    public static FixtureDef fixtureDef;

    static {
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.friction = 1f;
        fixtureDef.shape = new PolygonShape();
        ((PolygonShape) fixtureDef.shape).setAsBox(0.25F, 0.25F);
    }

    Fixture mainFixture;
    UUID owner;


    public Fireball() {
    }

    public Fireball(GameWorld world, UUID owner, Vector2 position, Vector2 direction) {
        super(world);
        this.owner = owner;
        health = 10;

        body = world.world.createBody(bodyDef);
        body.setGravityScale(0.1F);
        body.setUserData(this);
        body.setTransform(position.x, position.y, 0);
        body.setLinearVelocity(direction.cpy().setLength(40F));
        mainFixture = body.createFixture(fixtureDef);
    }


    @Override
    public void touched(Fixture other) {
        if(!world.isServer())
            return;

        if(other.getBody().getUserData() instanceof Entity) {
            Entity entity = (Entity) other.getBody().getUserData();

            if(entity == this)
                return;

            UUID otherId = entity.getUUID();
            if(Objects.equals(otherId, owner))
                return;

            if(entity.getElement() == Nation.EARTH) {
                entity.damage(5);
                this.damage(15);
            }
            if(entity.getElement() == Nation.WATER) {
                entity.damage(4);
                this.damage(15);
            }
            if(entity.getElement() == Nation.FIRE) {
                entity.damage(4);
                this.damage(4);
            }
            if(entity.getElement() == Nation.AIR) {
                entity.damage(8);
                this.damage(2);
            }
        }
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        super.writeBytes(buffer);
        buffer.writeLong(owner.getMostSignificantBits());
        buffer.writeLong(owner.getLeastSignificantBits());
        buffer.writeFloat(body.getLinearVelocity().x);
        buffer.writeFloat(body.getLinearVelocity().y);
    }

    @Override
    public void readBytes(ByteBuffer buffer) {
        health = 10;

        body = world.world.createBody(bodyDef);
        body.setGravityScale(0.5F);
        body.setUserData(this);
        mainFixture = body.createFixture(fixtureDef);
        mainFixture.setSensor(true);

        super.readBytes(buffer);
        owner = new UUID(buffer.readLong(), buffer.readLong());
        body.setLinearVelocity(buffer.readFloat(), buffer.readFloat());
    }

    public float getSize() {
        return 1F;
    }

    @Override
    public void writeSyncBytes(ByteBuffer out) {
        super.writeSyncBytes(out);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
    }

    @Override
    public void readSyncBytes(ByteBuffer in) {
        super.readSyncBytes(in);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
    }

    @Override
    public Nation getElement() {
        return Nation.EARTH;
    }


}
