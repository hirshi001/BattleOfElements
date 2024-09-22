package com.hirshi001.game.renderer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.hirshi001.game.gameworld.ground.EarthRock;

public class EarthRockRenderer extends EntityRenderer<EarthRock> {

    TextureRegion textureRegion;

    String textureName = "game/earth/Earth-Rock.png";

    public EarthRockRenderer(EarthRock entity, AssetManager assetManager) {
        super(entity, assetManager);
        assetManager.load(textureName, Texture.class);
        assetManager.finishLoading();

        textureRegion = new TextureRegion(assetManager.get(textureName, Texture.class));
    }

    @Override
    public void tick(float delta) {

    }

    @Override
    public void render(SpriteBatch batch) {
        float width = entity.getSize();
        float height = entity.getSize();
        float x = entity.getBody().getPosition().x - width / 2;
        float y = entity.getBody().getPosition().y - height / 2;
        float angle = entity.getBody().getAngle() * MathUtils.radiansToDegrees;

        batch.draw(textureRegion, x, y, width / 2, height / 2, width, height, 1, 1, angle);
    }
}
