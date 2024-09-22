package com.hirshi001.game.joinroom;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.hirshi001.game.GameApp;
import com.hirshi001.game.connection.PlayerReference;
import com.hirshi001.game.loadingscreens.AssetedScreen;
import com.hirshi001.game.lobby.Lobby;
import com.hirshi001.game.lobbyscreen.LobbyScreen;
import com.hirshi001.game.menu.MenuScreen;
import com.hirshi001.game.packets.lobby.FetchLobbyPacket;
import com.hirshi001.game.packets.lobby.JoinLobbyPacket;
import com.hirshi001.game.packets.lobby.LobbyBatchPacket;
import com.hirshi001.game.packets.lobby.LobbyPacket;
import com.hirshi001.game.util.Pair;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.packet.Packet;
import com.hirshi001.networking.packethandlercontext.PacketHandlerContext;
import com.hirshi001.networking.packethandlercontext.PacketType;
import com.hirshi001.networking.util.defaultpackets.primitivepackets.BooleanPacket;
import com.hirshi001.restapi.RestFuture;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JoinRoomScreen extends AssetedScreen {

    TextureRegion background;
    Skin skin;

    Stage stage;
    Table lobbyInfoTable;


    @Override
    public void render(float delta) {
        super.render(delta);
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
        PlayerReference.clear();

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
                GameApp.getInstance().setScreen(new MenuScreen());
            }
        });
        table.add(backButton).fillX().expandX().top().left();

        TextButton reloadButton = new TextButton("Reload", skin);
        reloadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                fetchLobby();
            }
        });
        table.add(reloadButton).fillX().expandX().top().right();

        table.row();

        lobbyInfoTable = new Table();
        lobbyInfoTable.setFillParent(true);
        table.add(lobbyInfoTable).fill().expand();

        table.validate();

        fetchLobby();
    }

    private void fetchLobby() {
        Channel channel = GameApp.getInstance().client.getChannel();
        channel.sendTCPWithResponse(new FetchLobbyPacket(), null, TimeUnit.SECONDS.toMillis(10))
                .then((response) -> {
                    if (!(response.packet instanceof LobbyBatchPacket)) {
                        throw new IllegalArgumentException("Expected ByteArrayPacket but got " + response.packet.getClass().getSimpleName());
                    }
                })
                .map((response) -> (LobbyBatchPacket) response.packet)
                .then((data) -> {

                    lobbyInfoTable.clear();

                    Label hostLabel = new Label("Host", skin);
                    lobbyInfoTable.add(hostLabel).fill();

                    Label playerCount = new Label("Player Count", skin);
                    lobbyInfoTable.add(playerCount).fill();

                    lobbyInfoTable.row();

                    for (Lobby.LobbyData lobbyData : data.lobbies) {
                        TextButton joinButton = new TextButton(lobbyData.getName(), skin);
                        joinButton.addListener(new ClickListener() {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                GameApp.getInstance().client.getChannel()
                                        .sendWithResponse(new JoinLobbyPacket(lobbyData.getId()), null, PacketType.TCP, TimeUnit.SECONDS.toMillis(30))
                                        .then((response) -> {
                                            if (response.packet instanceof BooleanPacket) {
                                                assert !((BooleanPacket) response.packet).value;
                                                System.out.println("Failed to join lobby " + lobbyData.getName());
                                                return;
                                            }

                                            assert response.packet instanceof LobbyPacket;

                                            Lobby.LobbyData lobbyData = getLobbyData((LobbyPacket)response.packet);

                                            Lobby lobby = new Lobby(lobbyData);

                                            for(PlayerReference playerReference : ((LobbyPacket) response.packet).players){
                                                PlayerReference.addPlayerReference(playerReference);
                                                lobby.addPlayer(playerReference.id);
                                            }

                                            GameApp.getInstance().setScreen(new LobbyScreen(lobby));
                                        })
                                        .performAsync();
                            }
                        });
                        lobbyInfoTable.add(joinButton).fill();

                        Label playerCountValue = new Label(lobbyData.getPlayerCount() + "/" + lobbyData.getMaxPlayers(), skin);
                        lobbyInfoTable.add(playerCountValue).fill();

                        lobbyInfoTable.row();
                    }
                })
                .onFailure((e) -> {
                    if (e instanceof IllegalArgumentException) {
                        e.printStackTrace();
                    }
                }).performAsync();
    }

    @NotNull
    private static Lobby.LobbyData getLobbyData(LobbyPacket lobbyPacket) {
        return new Lobby.LobbyData(
                        lobbyPacket.name,
                        lobbyPacket.playerCount,
                        lobbyPacket.maxPlayers,
                        lobbyPacket.lobbyId
                );
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    protected void loadDependencies(AssetManager assetManager) {
        assetManager.load("JoinRoomPage/JoinPageGui.json", Skin.class);
        assetManager.load("MenuPage/BattleOfElements.png", Texture.class);
    }
}
