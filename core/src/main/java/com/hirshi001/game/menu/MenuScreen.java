package com.hirshi001.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;
import com.hirshi001.game.GameApp;
import com.hirshi001.game.gamescreen.GameScreen;
import com.hirshi001.game.joinroom.JoinRoomScreen;
import com.hirshi001.game.loadingscreens.AssetedScreen;

public class MenuScreen extends AssetedScreen {

    SpriteBatch batch;

    TextureRegion background;
    Viewport backgroundViewport;


    Skin skin;
    Stage stage;




    public MenuScreen() {
    }

    @Override
    public void show() {
        AssetManager assets = getAssetManager();

        background = new TextureRegion(assets.get("MenuPage/BattleOfElements.png", Texture.class));
        skin = assets.get("MenuPage/MenuGui.json", Skin.class);

        backgroundViewport = new FillViewport(background.getRegionWidth(), background.getRegionHeight());
        batch = new SpriteBatch();

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton playButton = new TextButton("Join Room", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Join Room Button Clicked");
                Gdx.app.postRunnable(() -> {
                    GameApp.getInstance().setScreen(new JoinRoomScreen());
                });
            }
        });

        table.add(playButton).fillX().uniformX();

        table.row().pad(10, 0, 10, 0);

        TextButton optionsButton = new TextButton("Options", skin);
        table.add(optionsButton).fillX().uniformX();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        backgroundViewport.apply(true);
        batch.setProjectionMatrix(backgroundViewport.getCamera().combined);
        batch.begin();
        batch.draw(background, 0, 0);
        batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        backgroundViewport.update(width, height);
        stage.getViewport().update(width, height, true);
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
        super.dispose();
        stage.dispose();
        batch.dispose();
    }

    @Override
    protected void loadDependencies(AssetManager assetManager) {
        assetManager.load("MenuPage/MenuGui.json", Skin.class);
        assetManager.load("MenuPage/BattleOfElements.png", Texture.class);
    }

}
