package com.hirshi001.game.gameworld.character;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.gameworld.controller.ButtonState;
import com.hirshi001.game.gameworld.water.Water;
import com.hirshi001.game.packets.game.earth.UseSpecialOnePacket;
import com.hirshi001.networking.packethandlercontext.PacketType;

import java.util.ArrayList;

public class WaterCharacter extends Character {


    public WaterCharacter() {
        super();
    }

    public WaterCharacter(GameWorld world, float x, float y) {
        super(Nation.WATER, world, x, y);
    }

    @Override
    public void useSpecialOne(Vector2 direction, ButtonState state) {
        super.useSpecialOne(direction, state);
        if (state == ButtonState.DOWN) {
            if (world.isClient()) {

                Vector2 direction2 = direction.cpy();
                if (direction2.isZero()) {
                    if (facingRight) {
                        direction2.set(1, 0);
                    } else {
                        direction2.set(-1, 0);
                    }
                }
                Player.getPlayerByUUID(this.uuid).channel.sendNow(new UseSpecialOnePacket(direction2, ButtonState.DOWN), null, PacketType.TCP);
                return;
            } else {
                float lowerY = getBody().getPosition().y - 5F;
                float upperY = getBody().getPosition().y + 5F;
                float lowerX, upperX;
                if (direction.x > 0) {
                    lowerX = getBody().getPosition().x - 10F;
                    upperX = getBody().getPosition().x + 2F;
                } else {
                    lowerX = getBody().getPosition().x - 2F;
                    upperX = getBody().getPosition().x + 10F;
                }

                world.world.QueryAABB(new QueryCallback() {
                    @Override
                    public boolean reportFixture(Fixture fixture) {
                        if (fixture.getBody().getUserData() instanceof Water) {
                            ((Water)fixture.getBody().getUserData()).control(getUUID(), direction);
                        }
                        return false;
                    }
                }, lowerX, lowerY, upperX, upperY);

            }
        }
    }
}
