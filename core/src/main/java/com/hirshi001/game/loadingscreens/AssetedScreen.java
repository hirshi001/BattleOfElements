package com.hirshi001.game.loadingscreens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;

public abstract class AssetedScreen extends ScreenAdapter {

    private AssetManager assetManager = null;

    public AssetedScreen() {
    }

    @Override
    public void dispose() {
        super.dispose();
        assetManager.dispose();
    }

    protected abstract void loadDependencies(AssetManager assetManager);

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

}
