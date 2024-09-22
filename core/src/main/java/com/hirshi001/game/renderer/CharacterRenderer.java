package com.hirshi001.game.renderer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.hirshi001.game.gameworld.Nation;
import com.hirshi001.game.gameworld.character.Character;

public class CharacterRenderer<T extends Character> extends EntityRenderer<T>{

    public static void RequestAssets(AssetManager assetManager) {
        for(String path : idlePaths) {
            assetManager.load(path, Texture.class);
        }
        for(String path : runPaths) {
            assetManager.load(path, Texture.class);
        }
        for (String path : jumpPaths) {
            assetManager.load(path, Texture.class);
        }
    }



    Vector2 displayPosition = new Vector2();

    static String[] idlePaths = new String[] {
        "game/character/Character-Idle-1.png",
        "game/character/Character-Idle-2.png"
    };

    static String[] runPaths = new String[] {
        "game/character/Character-Run-1.png",
        "game/character/Character-Run-2.png",
        "game/character/Character-Run-3.png",
        "game/character/Character-Run-4.png",
        "game/character/Character-Run-5.png",
        "game/character/Character-Run-5.png"
    };

    static String[] jumpPaths = new String[] {
            "game/character/Character-Jump-1.png"
    };


    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> runAnimation;
    Animation<TextureRegion> jumpAnimation;

    public CharacterRenderer(T entity, AssetManager assetManager) {
        super(entity, assetManager);
        TextureRegion[] idleRegions = new TextureRegion[idlePaths.length];
        for(int i = 0; i < idlePaths.length; i++) {
            idleRegions[i] = new TextureRegion(assetManager.get(idlePaths[i], Texture.class));
        }
        idleAnimation = new Animation<TextureRegion>(1f, idleRegions);

        TextureRegion[] runRegions = new TextureRegion[runPaths.length];
        for(int i = 0; i < runPaths.length; i++) {
            runRegions[i] = new TextureRegion(assetManager.get(runPaths[i], Texture.class));
        }
        runAnimation = new Animation<TextureRegion>(0.1F, runRegions);

        TextureRegion[] jumpRegions = new TextureRegion[jumpPaths.length];
        for(int i = 0; i < jumpPaths.length; i++) {
            jumpRegions[i] = new TextureRegion(assetManager.get(jumpPaths[i], Texture.class));
        }
        jumpAnimation = new Animation<TextureRegion>(0.1f, jumpRegions);

        displayPosition = new Vector2();
        displayPosition.set(entity.getBody().getPosition());
    }

    float time = 0;
    @Override
    public void tick(float delta) {
        time += delta;
        displayPosition.interpolate(entity.getBody().getPosition(), 0.2F, Interpolation.linear);
    }

    @Override
    public void render(SpriteBatch batch) {

        Animation<TextureRegion> currentAnimation;
        if(Math.abs(entity.getBody().getLinearVelocity().x) > 0.1F && entity.isGrounded()) {
            currentAnimation = runAnimation;
        } else if (!entity.isGrounded()){
            currentAnimation = jumpAnimation;
        } else {
            currentAnimation = idleAnimation;
        }

        TextureRegion region = currentAnimation.getKeyFrame(time, true);

        boolean facingRight = entity.getBody().getLinearVelocity().x > 0;

        Color original = batch.getColor();
        Color color = original;
        switch (entity.nation) {
            case FIRE:
                color = Color.RED;
                break;
            case WATER:
                color = Color.BLUE;
                break;
            case EARTH:
                color = Color.GREEN;
                break;
            case AIR:
                color = Color.YELLOW;
                break;
        }

        batch.setColor(color);
        if(facingRight) {
            batch.draw(region, displayPosition.x - 2F, displayPosition.y - 2F, 4, 4);
        }else{
            batch.draw(region, displayPosition.x + 2F, displayPosition.y - 2F, -4, 4);
        }
        batch.setColor(original);

    }


}
