package com.hirshi001.game.gameworld.controller;

import com.badlogic.gdx.math.Vector2;

public interface CharacterInterface {

    void direction(Vector2 direction);

    void useSpecialOne(Vector2 direction, ButtonState state);

    boolean dead();

}
