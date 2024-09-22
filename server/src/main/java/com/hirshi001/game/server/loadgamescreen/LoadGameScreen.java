package com.hirshi001.game.server.loadgamescreen;

import com.badlogic.gdx.ScreenAdapter;
import com.hirshi001.game.connection.Player;

import java.util.List;

public class LoadGameScreen extends ScreenAdapter {


    private List<Player> playerList;

    public LoadGameScreen(List<Player> playerList) {
        super();
        this.playerList = playerList;
    }

    @Override
    public void show() {
        //
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

}
