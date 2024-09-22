package com.hirshi001.game.loadingscreens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hirshi001.game.GameApp;

public class AssetLoadingScreen implements Screen {

    private final AssetManager assetManager = new AssetManager();

    AssetedScreen nextScreen;

    public AssetLoadingScreen(AssetedScreen nextScreen) {
        this.nextScreen = nextScreen;
    }


    @Override
    public void show() {
        nextScreen.loadDependencies(assetManager);
    }

    @Override
    public final void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        assetManager.update();
        if (assetManager.isFinished()) {
            nextScreen.setAssetManager(assetManager);
            Gdx.app.postRunnable(() -> {
                GameApp.getInstance().setScreen(nextScreen);
            });
        }
    }



    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
