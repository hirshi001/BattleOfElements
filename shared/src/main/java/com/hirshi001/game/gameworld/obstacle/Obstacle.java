package com.hirshi001.game.gameworld.obstacle;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.entity.Entity;

public abstract class Obstacle extends Entity {

    public Obstacle(GameWorld world) {
        super(world);
    }

    @Override
    public boolean requiresSync() {
        return false;
    }
}
