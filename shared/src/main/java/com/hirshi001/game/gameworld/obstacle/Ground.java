package com.hirshi001.game.gameworld.obstacle;

import com.badlogic.gdx.physics.box2d.*;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.entity.Entity;

public class Ground extends Obstacle {

    Fixture fixture;

    public Ground(GameWorld world, float x, float y, float halfWidth, float halfHeight) {
        super(world);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.StaticBody;

        body = world.world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfWidth, halfHeight);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0;
        fixtureDef.friction = 0.5f;

        fixture = body.createFixture(fixtureDef);

        shape.dispose();
    }

}
