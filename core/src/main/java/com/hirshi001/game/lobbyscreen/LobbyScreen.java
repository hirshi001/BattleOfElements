package com.hirshi001.game.lobbyscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hirshi001.game.GameApp;
import com.hirshi001.game.connection.PlayerReference;
import com.hirshi001.game.joinroom.JoinRoomScreen;
import com.hirshi001.game.loadingscreens.AssetedScreen;
import com.hirshi001.game.lobby.Lobby;
import com.hirshi001.game.packets.lobby.LeaveLobbyPacket;
import com.hirshi001.game.util.UUID;
import com.hirshi001.networking.packethandlercontext.PacketType;

public class LobbyScreen extends AssetedScreen {

    TextureRegion background;
    Skin skin;

    Stage stage;
    Table lobbyInfoTable;

    public LobbyScreen(Lobby lobby) {
        GameApp.getInstance().player.lobby = lobby;
    }


    @Override
    public void render(float delta) {
        super.render(delta);
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        super.show();
        background = new TextureRegion(getAssetManager().get("MenuPage/BattleOfElements.png", Texture.class));
        skin = getAssetManager().get("JoinRoomPage/JoinPageGui.json", Skin.class);

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                GameApp.getInstance().player.lobby = null;
                GameApp.getInstance().client.getChannel().sendNow(new LeaveLobbyPacket(), null, PacketType.TCP);
                GameApp.getInstance().setScreen(new JoinRoomScreen());
            }
        });
        table.add(backButton).fillX().expandX().top().left();


        Lobby lobby = GameApp.getInstance().player.lobby;
        Label title = new Label("Lobby: " + lobby.getLobbyData().getName(), skin);
        table.add(title).fillX().expandX().top().center();

        Label lobbyStatus = new Label("Status: Waiting for players " + lobby.getLobbyData().getPlayerCount() + "/" + lobby.getLobbyData().getMaxPlayers(), skin);
        table.add(lobbyStatus).fillX().expandX().top().right();

        table.row();

        lobbyInfoTable = new Table();
        lobbyInfoTable.setFillParent(true);
        table.add(lobbyInfoTable).fill().expand();

        updateLobby();
    }

    public void updateLobby(){
        Lobby lobby = GameApp.getInstance().player.lobby;
        lobbyInfoTable.clear();
        for(UUID uuid : lobby.getPlayers()){
            PlayerReference playerReference = PlayerReference.getPlayerReference(uuid);
            Label playerLabel = new Label(playerReference.name, skin);
            lobbyInfoTable.add(playerLabel).fillX().expandX().top().center();
            lobbyInfoTable.row();
        }
        lobbyInfoTable.validate();
    }

    @Override
    protected void loadDependencies(AssetManager assetManager) {
        assetManager.load("JoinRoomPage/JoinPageGui.json", Skin.class);
        assetManager.load("MenuPage/BattleOfElements.png", Texture.class);
    }
}
