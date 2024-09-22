package com.hirshi001.game.renderer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hirshi001.game.gameworld.entity.Entity;

public abstract class EntityRenderer<T extends Entity> {

    public T entity;

    public EntityRenderer(T entity, AssetManager assetManager) {
        this.entity = entity;
    }

    public abstract void tick(float delta);
    public abstract void render(SpriteBatch batch);



}
