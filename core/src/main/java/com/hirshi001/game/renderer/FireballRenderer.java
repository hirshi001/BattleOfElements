package com.hirshi001.game.renderer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hirshi001.game.gameworld.fire.Fireball;

public class FireballRenderer extends EntityRenderer<Fireball> {


    Animation<TextureRegion> animation;

    static String[] textureNames = new String[]{
            "game/fire/Fireball-1.png",
            "game/fire/Fireball-2.png",
    };

    public FireballRenderer(Fireball entity, AssetManager assetManager) {
        super(entity, assetManager);
        for (String textureName : textureNames) {
            assetManager.load(textureName, Texture.class);
        }
        assetManager.finishLoading();

        TextureRegion[] textureRegions = new TextureRegion[textureNames.length];
        for (int i = 0; i < textureNames.length; i++) {
            textureRegions[i] = new TextureRegion(assetManager.get(textureNames[i], Texture.class));
        }

        animation = new Animation<>(0.1f, textureRegions);
    }

    float stateTime = 0;

    @Override
    public void tick(float delta) {
        stateTime += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        float width = entity.getSize();
        float height = entity.getSize();
        float x = entity.getBody().getPosition().x - width / 2;
        float y = entity.getBody().getPosition().y - height / 2;
        float angle = entity.getBody().getAngle() * 180 / (float) Math.PI;

        batch.draw(animation.getKeyFrame(stateTime, true), x, y, 0, 0, width, height, 1, 1, angle);
    }
}
