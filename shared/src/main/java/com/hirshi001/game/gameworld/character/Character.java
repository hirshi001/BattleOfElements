package com.hirshi001.game.gameworld.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.hirshi001.buffer.buffers.ByteBuffer;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.gameworld.controller.ButtonState;
import com.hirshi001.game.gameworld.controller.CharacterInterface;
import com.hirshi001.game.gameworld.detection.SensorReceiver;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.gameworld.fire.Fireball;
import com.hirshi001.game.gameworld.ground.EarthRock;
import com.hirshi001.game.packets.game.earth.UseSpecialOnePacket;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packethandlercontext.PacketHandlerContext;
import com.hirshi001.networking.packethandlercontext.PacketType;

public abstract class Character extends Entity implements CharacterInterface {

    public static BodyDef bodyDef;
    public static FixtureDef fixtureDef;

    static {
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;

        fixtureDef = new FixtureDef();
        fixtureDef.density = 1;
        fixtureDef.friction = 1f;
        fixtureDef.shape = new PolygonShape();
        ((PolygonShape) fixtureDef.shape).setAsBox(1F, 2F);
    }

    public static final float MAX_HORIZONTAL_VELOCITY = 10;
    public static final float JUMP_SPEED = 13;

    public Nation nation;
    public Fixture mainFixture;
    public Fixture groundSensor;
    public Vector2 direction = new Vector2();
    public int groundTouches = 0;

    public boolean isClientControlled = false;
    public boolean doubleJumped = false;

    boolean facingRight = true;

    public Character() {
        super();

    }

    public Character(Nation nation, GameWorld world, float x, float y) {
        super(world);
        this.nation = nation;

        body = world.world.createBody(bodyDef);
        body.setUserData(this);
        body.setTransform(x, y, 0);
        mainFixture = body.createFixture(fixtureDef);

        FixtureDef sensorFixtureDef = new FixtureDef();
        sensorFixtureDef.isSensor = true;
        sensorFixtureDef.shape = new PolygonShape();
        ((PolygonShape) sensorFixtureDef.shape).setAsBox(1F, 0.2F, new Vector2(0, -2), 0);
        groundSensor = body.createFixture(sensorFixtureDef);

        groundSensor.setUserData(new SensorReceiver() {
            @Override
            public void senseBegin(Fixture other) {
                doubleJumped = false;
                groundTouches++;
            }

            @Override
            public void senseEnd(Fixture other) {
                groundTouches--;
            }
        });
    }

    public boolean isGrounded() {
        return groundTouches > 0;
    }

    public boolean canMove() {
        return true;
    }

    @Override
    public void touched(Fixture other) {
        if(!world.isServer())
            return;
        if(!(other.getBody().getUserData() instanceof Entity))
            return;

        if(other.getBody().getUserData() instanceof Character)
            return;

        Entity otherEntity = (Entity) other.getBody().getUserData();
        otherEntity.damage(15);
    }

    @Override
    public void tick(float delta) {
        super.tick(delta);

        if(world.isServer())
            return;


        Vector2 direction = new Vector2(this.direction);
        if(direction.isZero()) {
            body.setLinearVelocity(MathUtils.clamp(body.getLinearVelocity().x, -2F, 2F), body.getLinearVelocity().y);
            return;
        }

        if(!canMove())
            return;

        Vector2 currentVelocity = body.getLinearVelocity();
        if(direction.x != 0) {
            currentVelocity.x = direction.x * MAX_HORIZONTAL_VELOCITY;
            facingRight = direction.x > 0;
        }

        if((isGrounded() || (nation == Nation.AIR && !doubleJumped)) && direction.y > 0.1){
            currentVelocity.y = JUMP_SPEED;
            if(nation == Nation.AIR && !isGrounded()) {
                doubleJumped = true;
            }
        }

        if(!isGrounded() && direction.y < 0) {
            if(direction.y < 0.1) {
                body.applyForceToCenter(0, -3, true);
            }
            if(direction.x == 0) {
                currentVelocity.x = 0;
            }
        }

        body.setLinearVelocity(currentVelocity);
    }


    @Override
    public void direction(Vector2 direction) {
        this.direction.set(direction);
    }

    @Override
    public void useSpecialOne(Vector2 direction, ButtonState state) {

    }

    @Override
    public boolean dead() {
        return dead;
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        super.writeBytes(buffer);
    }

    @Override
    public void readBytes(ByteBuffer buffer) {
        body = world.world.createBody(bodyDef);
        body.setUserData(this);
        mainFixture = body.createFixture(fixtureDef);

        FixtureDef sensorFixtureDef = new FixtureDef();
        sensorFixtureDef.isSensor = true;
        sensorFixtureDef.shape = new PolygonShape();
        ((PolygonShape) sensorFixtureDef.shape).setAsBox(1F, 0.2F, new Vector2(0, -2), 0);
        groundSensor = body.createFixture(sensorFixtureDef);

        groundSensor.setUserData(new SensorReceiver() {
            @Override
            public void senseBegin(Fixture other) {
                doubleJumped = false;
                groundTouches++;
            }

            @Override
            public void senseEnd(Fixture other) {
                groundTouches--;
            }
        });
        super.readBytes(buffer);
    }

    @Override
    public void writeSyncBytes(ByteBuffer out) {
        super.writeSyncBytes(out);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
    }

    @Override
    public void readSyncBytes(ByteBuffer in) {
        if(!isClientControlled) {
            super.readSyncBytes(in);
            body.setLinearVelocity(in.readFloat(), in.readFloat());
        }
        else {
            Vector2 position = new Vector2(in.readFloat(), in.readFloat());
            float angle = in.readFloat();
            if(position.dst2(body.getPosition()) > 0.5F) {
                body.setTransform(position.add(body.getPosition()).scl(0.5F), angle);
            }

            in.readFloat();
            in.readFloat();
        }
    }

    @Override
    public Nation getElement() {
        return nation;
    }
}
