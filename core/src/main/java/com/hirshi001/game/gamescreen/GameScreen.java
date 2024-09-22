package com.hirshi001.game.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.hirshi001.game.GameApp;
import com.hirshi001.game.gameworld.ClientGameWorld;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.character.Character;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.loadingscreens.AssetedScreen;
import com.hirshi001.game.renderer.CharacterRenderer;
import com.hirshi001.game.renderer.EntityRenderer;
import com.hirshi001.game.renderer.GameWorldRenderer;

import java.util.HashMap;
import java.util.Map;

public class GameScreen extends AssetedScreen {

    ClientGameWorld gameWorld;
    GameWorldRenderer gameWorldRenderer;

    Stage stage;


    @Override
    public void show() {

        gameWorld = (ClientGameWorld) GameApp.getInstance().player.lobby.gameWorld;
        gameWorldRenderer = new GameWorldRenderer(gameWorld);

        stage = new Stage();
        Gdx.input.setInputProcessor(new InputMultiplexer(gameWorld.getInputProcessor(), gameWorldRenderer));
    }

    @Override
    public void render(float delta) {
        gameWorld.tick(delta);
        gameWorldRenderer.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        gameWorldRenderer.resize(width, height);
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

    @Override
    protected void loadDependencies(AssetManager assetManager) {

    }
}
