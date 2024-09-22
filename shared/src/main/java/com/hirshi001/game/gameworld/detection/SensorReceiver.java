package com.hirshi001.game.gameworld.detection;

import com.badlogic.gdx.physics.box2d.Fixture;

public interface SensorReceiver {

    void senseBegin(Fixture other);

    void senseEnd(Fixture other);

}
