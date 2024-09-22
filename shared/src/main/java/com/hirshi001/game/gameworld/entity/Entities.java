package com.hirshi001.game.gameworld.entity;

import com.hirshi001.game.gameworld.character.*;
import com.hirshi001.game.gameworld.character.Character;
import com.hirshi001.game.gameworld.fire.Fireball;
import com.hirshi001.game.gameworld.ground.EarthRock;
import com.hirshi001.game.gameworld.water.Water;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Entities {

    public static final Map<Integer, Supplier<Entity>> entityIdMap = new HashMap<>();
    public static final Map<Class<? extends Entity>, Integer> entityClassMap = new HashMap<>();

    public static void registerEntity(int id, Supplier<Entity> entity) {
        entityIdMap.put(id, entity);
        entityClassMap.put(entity.get().getClass(), id);
    }

    public static Entity createEntity(int id) {
        return entityIdMap.get(id).get();
    }

    public static int getEntityId(Entity entity) {
        return entityClassMap.get(entity.getClass());
    }

    static {
        registerEntity(0, AirCharacter::new);
        registerEntity(1, FireCharacter::new);
        registerEntity(2, WaterCharacter::new);
        registerEntity(3, EarthCharacter::new);
        registerEntity(4, EarthRock::new);
        registerEntity(5, Fireball::new);
        registerEntity(6, Water::new);
    }

}
