package com.hirshi001.game.gameworld.water;

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

public class Water extends Entity {

    public static BodyDef bodyDef;
    public static FixtureDef fixtureDef;

    static {
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.friction = 1f;
        fixtureDef.shape = new PolygonShape();
        ((PolygonShape) fixtureDef.shape).setAsBox(0.2F, 0.2F);
    }

    Fixture mainFixture;
    boolean controlled;

    UUID owner;

    public Water() {
    }

    public float getSize() {
        return 0.4F;
    }

    public Water(GameWorld world, Vector2 position, Vector2 direction) {
        super(world);
        health = 10;

        body = world.world.createBody(bodyDef);
        body.setUserData(this);
        body.setTransform(position.x, position.y, 0);
        mainFixture = body.createFixture(fixtureDef);

        body.setLinearVelocity(direction.cpy().setLength(5F));
        controlled = false;
    }

    @Override
    public void tick(float delta) {
        super.tick(delta);
        if(world.isServer()) {
            if(body.getPosition().y < -50) {
                world.removeEntity(getUUID());
            }
        }
    }

    public void control(UUID owner, Vector2 direction) {
        controlled = true;
        this.owner = owner;
        body.setGravityScale(0.5F);
        body.setLinearVelocity(direction.cpy().setLength(40F));
        urgentSync();
    }

    @Override
    public void touched(Fixture other) {
        if(!world.isServer())
            return;

        if(other.getBody().getUserData() instanceof Entity) {
            Entity entity = (Entity) other.getBody().getUserData();
            UUID otherUUID = entity.getUUID();
            if(Objects.equals(otherUUID, getUUID()))
                return;

            if(entity.getElement() == Nation.EARTH) {
                entity.damage(10);
                this.damage(10);
            }
            if(entity.getElement() == Nation.WATER && !(entity instanceof Water)) {
                entity.damage(3);
                this.damage(3);
            }
            if(entity.getElement() == Nation.FIRE) {
                entity.damage(10);
                this.damage(5);
            }
            if(entity.getElement() == Nation.AIR) {
                entity.damage(15);
                this.damage(3);
            }
        }

    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        super.writeBytes(buffer);
        buffer.writeFloat(body.getLinearVelocity().x);
        buffer.writeFloat(body.getLinearVelocity().y);
    }

    @Override
    public void readBytes(ByteBuffer buffer) {
        health = 10;

        body = world.world.createBody(bodyDef);
        body.setGravityScale(1F);
        body.setUserData(this);
        mainFixture = body.createFixture(fixtureDef);

        super.readBytes(buffer);
        body.setLinearVelocity(buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public boolean requiresSync() {
        return controlled;
    }

    @Override
    public void writeSyncBytes(ByteBuffer out) {
        super.writeSyncBytes(out);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeBoolean(controlled);
        if(controlled) {
            out.writeLong(owner.getMostSignificantBits());
            out.writeLong(owner.getLeastSignificantBits());
        }
    }

    @Override
    public void readSyncBytes(ByteBuffer in) {
        super.readSyncBytes(in);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        controlled = in.readBoolean();
        if(controlled) {
            owner = new UUID(in.readLong(), in.readLong());
        }
        body.setGravityScale(0.5F);
    }

    @Override
    public Nation getElement() {
        return Nation.WATER;
    }


}
