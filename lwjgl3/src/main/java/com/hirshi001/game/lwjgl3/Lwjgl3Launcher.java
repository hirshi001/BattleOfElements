package com.hirshi001.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.utils.Disposable;
import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.buffer.bufferfactory.DefaultBufferFactory;
import com.hirshi001.game.GameApp;
import com.hirshi001.javanetworking.JavaNetworkFactory;
import com.hirshi001.javarestapi.JavaRestFutureFactory;
import com.hirshi001.networking.network.NetworkFactory;
import com.hirshi001.restapi.RestAPI;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.

        createApplication();
    }

    private static Lwjgl3Application createApplication() {

        RestAPI.setFactory(new JavaRestFutureFactory());
        BufferFactory bufferFactory = new DefaultBufferFactory();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
        NetworkFactory networkFactory = new JavaNetworkFactory(executorService);

        Disposable disposable = executorService::shutdownNow;

        GameApp gameApp = new GameApp(disposable, bufferFactory, networkFactory, 4000);

        return new Lwjgl3Application(gameApp, getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("BattleOfElements");
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        configuration.setWindowedMode(640, 480);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
