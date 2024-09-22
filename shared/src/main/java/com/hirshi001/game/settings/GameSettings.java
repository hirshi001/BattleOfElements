package com.hirshi001.game.settings;

import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.networking.network.NetworkFactory;

public class GameSettings {

    @FunctionalInterface
    public interface RunnablePoster
    {
        void postRunnable (Runnable var1);
    }

    public static BufferFactory bufferFactory;

    public static RunnablePoster runnablePoster;


}
