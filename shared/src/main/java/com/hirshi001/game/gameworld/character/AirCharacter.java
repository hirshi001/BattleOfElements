package com.hirshi001.game.gameworld.character;

import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.Nation;

public class AirCharacter extends Character {

    public AirCharacter() {
        super();
    }

    public AirCharacter(GameWorld world, float x, float y) {
        super(Nation.AIR, world, x, y);
    }

}
