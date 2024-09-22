package com.hirshi001.game.gameworld.ground;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.util.UUID;

import java.util.Objects;

public class EarthRock extends Entity {

    public static BodyDef bodyDef;
    public static FixtureDef fixtureDef;

    static {
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.friction = 1f;
        fixtureDef.shape = new PolygonShape();
        ((PolygonShape) fixtureDef.shape).setAsBox(0.5F, 0.5F);
    }

    Fixture mainFixture;
    float time = 0F;

    float targetY;
    public Vector2 direction = new Vector2(0, 0);

    UUID owner;
    public float scale = 1F;

    boolean shot = false;

    public EarthRock() {
    }

    public EarthRock(GameWorld world, UUID owner, Vector2 position, Vector2 direction) {
        super(world);
        this.owner = owner;
        health = 10;
        targetY = position.y;

        body = world.world.createBody(bodyDef);
        body.setGravityScale(0F);
        body.setUserData(this);
        body.setTransform(position.x, position.y - 1F, 0);
        mainFixture = body.createFixture(fixtureDef);
        mainFixture.setSensor(true);

        this.direction = direction;
    }

    @Override
    public void tick(float delta) {
        super.tick(delta);
        if(shot)
            return;

        this.health = 10 * scale;

        if(time < 0.5F) {
            time += delta;
            Vector2 currentPosition = body.getTransform().getPosition();
            currentPosition.y += 4 * delta;
            if(currentPosition.y > targetY)
                currentPosition.y = targetY;
            body.setTransform(currentPosition.x, currentPosition.y, 0);
        }else if(time >= 0.5F){
            scale += 0.5F * delta;
        }
    }

    public void fire(float scale) {
        if(world == null)
            return;

        if(world.isServer()) {
            urgentSync();
        }

        shot = true;
        this.scale = scale;

        Vector2 position = body.getTransform().getPosition();
        world.world.destroyBody(body);

        body = world.world.createBody(bodyDef);
        body.setUserData(this);
        body.setTransform(position.x, position.y, 0);

        FixtureDef newDef = new FixtureDef();
        newDef.friction = mainFixture.getFriction();
        newDef.density = mainFixture.getDensity();
        newDef.shape = new PolygonShape();
        ((PolygonShape) newDef.shape).setAsBox(0.25F * scale, 0.25F * scale);
        mainFixture = body.createFixture(newDef);

        body.setGravityScale(1F);
        body.setLinearVelocity(direction.cpy().setLength(40F));
        mainFixture.setSensor(false);
    }

    @Override
    public void touched(Fixture other) {
        if(!world.isServer())
            return;

        if(other.getBody().getUserData() instanceof Entity) {
            Entity entity = (Entity) other.getBody().getUserData();
            UUID otherUUID = entity.getUUID();
            if(Objects.equals(otherUUID, owner))
                return;

            if(entity.getElement() == Nation.EARTH) {
                entity.damage(10 * scale);
                this.damage(10);
            }
            if(entity.getElement() == Nation.WATER) {
                entity.damage(10 * scale);
                this.damage(5);
            }
            if(entity.getElement() == Nation.FIRE) {
                entity.damage(10 * scale);
                this.damage(5);
            }
            if(entity.getElement() == Nation.AIR) {
                entity.damage(15 * scale);
                this.damage(3);
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
        buffer.writeFloat(targetY);
        buffer.writeFloat(direction.x);
        buffer.writeFloat(direction.y);
    }

    @Override
    public void readBytes(ByteBuffer buffer) {
        health = 10;

        body = world.world.createBody(bodyDef);
        body.setGravityScale(0.8F);
        body.setUserData(this);
        mainFixture = body.createFixture(fixtureDef);
        mainFixture.setSensor(true);

        super.readBytes(buffer);
        owner = new UUID(buffer.readLong(), buffer.readLong());
        body.setLinearVelocity(buffer.readFloat(), buffer.readFloat());
        targetY = buffer.readFloat();

        direction = new Vector2(buffer.readFloat(), buffer.readFloat());
    }

    public float getSize() {
        return 1F * scale;
    }

    @Override
    public void writeSyncBytes(ByteBuffer out) {
        super.writeSyncBytes(out);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(scale);
        out.writeBoolean(shot);
        if(shot) {
            out.writeFloat(direction.x);
            out.writeFloat(direction.y);
        }
    }

    @Override
    public void readSyncBytes(ByteBuffer in) {
        super.readSyncBytes(in);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        scale = in.readFloat();
        boolean shot = in.readBoolean();
        if(shot) {
            direction = new Vector2(in.readFloat(), in.readFloat());
        }
        if(!this.shot && shot)
            fire(scale);

    }

    @Override
    public Nation getElement() {
        return Nation.EARTH;
    }
}
