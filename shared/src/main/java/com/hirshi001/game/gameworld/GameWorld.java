package com.hirshi001.game.gameworld;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.hirshi001.game.gameworld.character.*;
import com.hirshi001.game.gameworld.character.Character;
import com.hirshi001.game.gameworld.detection.SensorReceiver;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.gameworld.obstacle.Ground;
import com.hirshi001.game.gameworld.obstacle.Obstacle;
import com.hirshi001.game.gameworld.water.Water;
import com.hirshi001.game.util.UUID;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GameWorld {

    static final Vector2[] spawnPoints = {
            new Vector2(-25, 0),
            new Vector2(-8, 0),
            new Vector2(8, 0),
            new Vector2(25, 0)
    };


    public World world;

    public Character[] characters;
    public UUID[] players;

    public Map<UUID, Entity> gameObjects = new HashMap<>();

    List<Entity> toRemove = new ArrayList<>();
    List<Entity> toAdd = new ArrayList<>();

    ContactFilter contactFilter = new ContactFilter() {
        @Override
        public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
            if(fixtureA.getBody().getUserData() instanceof Entity && fixtureB.getBody().getUserData() instanceof Entity) {
                Entity a = (Entity) fixtureA.getBody().getUserData();
                Entity b = (Entity) fixtureB.getBody().getUserData();
                return !(a.getElement() == Nation.WATER && b.getElement() == Nation.WATER);
            }
            return true;
        }
    };

    ContactListener contactListener =new ContactListener() {
        @Override
        public void beginContact(Contact contact) {
            if(contact.getFixtureA().isSensor() && contact.getFixtureA().getUserData() instanceof SensorReceiver) {
                SensorReceiver sensorReceiver = (SensorReceiver) contact.getFixtureA().getUserData();
                sensorReceiver.senseBegin(contact.getFixtureB());
            }
            if(contact.getFixtureB().isSensor() && contact.getFixtureB().getUserData() instanceof SensorReceiver) {
                SensorReceiver sensorReceiver = (SensorReceiver) contact.getFixtureB().getUserData();
                sensorReceiver.senseBegin(contact.getFixtureA());
            }

            if(contact.getFixtureA().getBody().getUserData() instanceof Entity) {
                Entity entity = (Entity) contact.getFixtureA().getBody().getUserData();
                entity.touched(contact.getFixtureB());
            }

            if(contact.getFixtureB().getBody().getUserData() instanceof Entity) {
                Entity entity = (Entity) contact.getFixtureB().getBody().getUserData();
                entity.touched(contact.getFixtureA());
            }
        }

        @Override
        public void endContact(Contact contact) {
            if(contact.getFixtureA().isSensor() && contact.getFixtureA().getUserData() instanceof SensorReceiver) {
                SensorReceiver sensorReceiver = (SensorReceiver) contact.getFixtureA().getUserData();
                sensorReceiver.senseEnd(contact.getFixtureB());
            }
            if(contact.getFixtureB().isSensor() && contact.getFixtureB().getUserData() instanceof SensorReceiver) {
                SensorReceiver sensorReceiver = (SensorReceiver) contact.getFixtureB().getUserData();
                sensorReceiver.senseEnd(contact.getFixtureA());
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }

    };

    public GameWorld() {
        this.characters = new Character[4];
        world = new World(new Vector2(0, -9.8F), false);

        characters[0] = new EarthCharacter(this, spawnPoints[0].x, spawnPoints[0].y);
        characters[1] = new FireCharacter(this, spawnPoints[1].x, spawnPoints[1].y);
        characters[2] = new WaterCharacter(this, spawnPoints[2].x, spawnPoints[2].y);
        characters[3] = new AirCharacter(this, spawnPoints[3].x, spawnPoints[3].y);

        new Ground(this, 0, -20, 30, 10);

        world.setContactFilter(contactFilter);
        world.setContactListener(contactListener);
    }

    public final void addEntity(Entity entity) {
        toAdd.add(entity);
    }

    @MustBeInvokedByOverriders
    protected void addEntity0(Entity entity) {
        if(entity.getUUID() == null) {
            while(true) {
                UUID uuid = UUID.randomUUID();
                if(!gameObjects.containsKey(uuid)) {
                    entity.setUUID(uuid);
                    break;
                }
            }
        }
        assert !gameObjects.containsKey(entity.getUUID());

        entity.world = this;
        gameObjects.put(entity.getUUID(), entity);
        // System.out.println("Added entity: " + entity.getUUID() + " : " + entity);
    }

    public final Entity removeEntity(UUID uuid) {
        Entity entity = gameObjects.get(uuid);
        if(entity != null) {
            toRemove.add(entity);
        }
        return entity;
    }

    protected Entity removeEntity0(UUID uuid) {
        Entity entity = gameObjects.remove(uuid);
        if(entity != null) {
            entity.world = null;
            entity.dead = true;
            world.destroyBody(entity.getBody());
        }
        return entity;
    }

    public abstract boolean isServer();
    public abstract boolean isClient();

    /**
     * Returns an unmodifiable list of characters in the game world.
     * @return an unmodifiable list of characters in the game world.
     */
    public Character[] getCharacters() {
        return characters;
    }

    public void tick(float delta) {

        removeAddInTick();
        for(Entity entity : gameObjects.values()) {
            entity.tick(delta);
        }

        removeAddInTick();
        world.step(delta, 5, 40);

    }

    private void removeAddInTick() {

        List<Entity> toAdd = new ArrayList<>(this.toAdd);
        List<Entity> toRemove = new ArrayList<>(this.toRemove);

        this.toAdd.clear();
        this.toRemove.clear();

        for(Entity entity : toAdd) {
            addEntity0(entity);
        }

        for(Entity entity : toRemove) {
            removeEntity0(entity.getUUID());
        }
    }


}
