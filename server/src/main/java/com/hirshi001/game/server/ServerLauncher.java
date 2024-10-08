package com.hirshi001.game.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import java.io.IOException;
import java.security.Security;
import java.util.Arrays;

/** Launches the server application. */
public class ServerLauncher {

    public static void main(String[] args) throws IOException {

        String password = null;
        String path = null;
        int websocketPort = 3000, javaPort = 4000;

        if(args.length >= 1) {
            password = args[0];
        }
        if(args.length >= 2) {
            path = args[1];
        }
        if(args.length >= 3) {
            websocketPort = Integer.parseInt(args[2]);
        }
        if(args.length >= 4) {
            javaPort = Integer.parseInt(args[3]);
        }

        createApplication(password, path, websocketPort, javaPort);

    }


    private static Application createApplication(String password, String path, int websocketPort, int javaPort) {
        // Note: you can use a custom ApplicationListener implementation for the headless project instead of GameApp.
        return new HeadlessApplication(new ServerApplication(password, path, websocketPort, javaPort), getDefaultConfiguration());
    }

    private static HeadlessApplicationConfiguration getDefaultConfiguration() {
        HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        configuration.updatesPerSecond = 16; // When this value is negative, GameApp#render() is never called.
        //// If the above line doesn't compile, it is probably because the project libGDX version is older.
        //// In that case, uncomment and use the below line.
        //configuration.renderInterval = -1f; // When this value is negative, GameApp#render() is never called.
        return configuration;
    }
}
