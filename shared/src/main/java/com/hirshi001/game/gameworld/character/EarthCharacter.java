package com.hirshi001.game.gameworld.character;

import com.badlogic.gdx.math.Vector2;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.gameworld.controller.ButtonState;
import com.hirshi001.game.gameworld.ground.EarthRock;
import com.hirshi001.game.packets.game.earth.UseSpecialOnePacket;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packethandlercontext.PacketType;

public class EarthCharacter extends Character {


    static final float SPECIAL_ONE_COOLDOWN = 0.5F;
    float specialOneCooldown = 0;

    boolean usingSpecialOne = false;

    // server only
    EarthRock earthRock;

    public EarthCharacter() {
        super();
    }

    public EarthCharacter(GameWorld world, float x, float y) {
        super(Nation.EARTH, world, x, y);
    }

    @Override
    public void tick(float delta) {
        if(specialOneCooldown > 0 && !usingSpecialOne) {
            specialOneCooldown -= delta;
        }

        super.tick(delta);
    }

    @Override
    public void useSpecialOne(Vector2 direction, ButtonState state) {
        if(state == ButtonState.DOWN) {
            usingSpecialOne = true;

            if(world.isClient()) {
                Vector2 direction2 = direction.cpy();
                if (direction2.isZero()) {
                    if (facingRight) {
                        direction2.set(1, 0);
                    } else {
                        direction2.set(-1, 0);
                    }
                }
                Player.getPlayerByUUID(this.uuid).channel.sendNow(new UseSpecialOnePacket(direction2, ButtonState.DOWN), null, PacketType.TCP);
            } else {
                if(specialOneCooldown > 0 || !isGrounded()) {
                    return;
                }
                Vector2 position = getBody().getPosition().cpy();
                position.mulAdd(direction, 3F);
                this.earthRock = new EarthRock(world, getUUID(), position, direction);
                world.addEntity(earthRock);
            }
        }
        else if(state == ButtonState.UP) {
            if(world.isClient()) {
                usingSpecialOne = false;
                Player.getPlayerByUUID(this.uuid).channel.sendNow(new UseSpecialOnePacket(direction, ButtonState.UP), null, PacketType.TCP);
            } else {
                if(!usingSpecialOne || specialOneCooldown > 0 || earthRock == null) {
                    usingSpecialOne = false;
                    return;
                }
                usingSpecialOne = false;

                specialOneCooldown = SPECIAL_ONE_COOLDOWN;

                if(!direction.isZero()) {
                    if(!(direction.x > 0 && earthRock.direction.x < 0) && !(direction.x < 0 && earthRock.direction.x > 0))
                        this.earthRock.direction = direction;
                }
                this.earthRock.fire(this.earthRock.scale);
            }

        }



    }

    @Override
    public boolean canMove() {
        return !usingSpecialOne;
    }
}
