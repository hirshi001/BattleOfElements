package com.hirshi001.game.gameworld.character;

import com.badlogic.gdx.math.Vector2;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.gameworld.controller.ButtonState;
import com.hirshi001.game.gameworld.fire.Fireball;
import com.hirshi001.game.gameworld.ground.EarthRock;
import com.hirshi001.game.packets.game.earth.UseSpecialOnePacket;
import com.hirshi001.networking.packethandlercontext.PacketType;

public class FireCharacter extends Character {


    static final float SPECIAL_ONE_COOLDOWN = 0.5F;
    float specialOneCooldown = 0;

    public FireCharacter() {
        super();
    }

    public FireCharacter(GameWorld world, float x, float y) {
        super(Nation.FIRE, world, x, y);
    }


    @Override
    public void tick(float delta) {
        if(specialOneCooldown > 0) {
            specialOneCooldown -= delta;
        }

        super.tick(delta);
    }


    @Override
    public void useSpecialOne(Vector2 direction, ButtonState state) {
        if(state == ButtonState.DOWN) {
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
                if(specialOneCooldown > 0) {
                    return;
                }
                specialOneCooldown = SPECIAL_ONE_COOLDOWN;

                Vector2 position = getBody().getPosition().cpy();
                position.mulAdd(direction, 2.5F);

                world.addEntity(new Fireball(world, getUUID(), position, direction));
            }
        }
    }

}
