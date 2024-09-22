package com.hirshi001.game.gameworld.controller;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

public class UserCharacterController extends InputAdapter {

    CharacterInterface character;

    Vector2 direction = new Vector2();
    boolean up, down, left, right;

    boolean updated;

    public UserCharacterController(CharacterInterface character) {
        this.character = character;
    }

    public boolean moving() {
        return up || down || left || right;
    }

    public boolean updated() {
        boolean temp = updated;
        updated = false;
        return temp;
    }

    void recalculateDirection() {
        direction.set(0, 0);
        if(up) {
            direction.y += 1;
        }
        if(down) {
            direction.y -= 1;
        }
        if(left) {
            direction.x -= 1;
        }
        if(right) {
            direction.x += 1;
        }
        direction.nor();
        if(!character.dead())
            character.direction(direction);
        updated = true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.W) {
            up = true;
            recalculateDirection();
            return true;
        }
        if(keycode == Input.Keys.S) {
            down = true;
            recalculateDirection();
            return true;
        }
        if(keycode == Input.Keys.A) {
            left = true;
            recalculateDirection();
            return true;
        }
        if(keycode == Input.Keys.D) {
            right = true;
            recalculateDirection();
            return true;
        }
        if(keycode == Input.Keys.O) {
            character.useSpecialOne(direction, ButtonState.DOWN);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.W) {
            up = false;
            recalculateDirection();
            return true;
        }
        if(keycode == Input.Keys.S) {
            down = false;
            recalculateDirection();
            return true;
        }
        if(keycode == Input.Keys.A) {
            left = false;
            recalculateDirection();
            return true;
        }
        if(keycode == Input.Keys.D) {
            right = false;
            recalculateDirection();
            return true;
        }
        if(keycode == Input.Keys.O) {
            character.useSpecialOne(direction, ButtonState.UP);
            return true;
        }
        return false;
    }

}
