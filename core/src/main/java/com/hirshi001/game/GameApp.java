package com.hirshi001.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Disposable;
import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.game.connection.Player;
import com.hirshi001.game.loadingscreens.AssetLoadingScreen;
import com.hirshi001.game.loadingscreens.AssetedScreen;
import com.hirshi001.game.menu.MenuScreen;
import com.hirshi001.game.packets.game.InitGamePacket;
import com.hirshi001.game.packets.game.PlayerMovePacket;
import com.hirshi001.game.packets.game.SyncPacket;
import com.hirshi001.game.packets.game.earth.UseSpecialOnePacket;
import com.hirshi001.game.packets.game.entity.AddEntityPacket;
import com.hirshi001.game.packets.game.entity.DeleteEntityPacket;
import com.hirshi001.game.packets.lobby.*;
import com.hirshi001.game.packets.player.RequestPlayerUUIDPacket;
import com.hirshi001.game.packets.util.MaintainConnectionPacket;
import com.hirshi001.game.packets.util.UUIDPacket;
import com.hirshi001.game.settings.GameSettings;
import com.hirshi001.networking.network.NetworkFactory;
import com.hirshi001.networking.network.channel.AbstractChannelListener;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.network.channel.ChannelInitializer;
import com.hirshi001.networking.network.channel.ChannelOption;
import com.hirshi001.networking.network.client.Client;
import com.hirshi001.networking.network.client.ClientOption;
import com.hirshi001.networking.networkdata.DefaultNetworkData;
import com.hirshi001.networking.networkdata.NetworkData;
import com.hirshi001.networking.packetdecoderencoder.SimplePacketEncoderDecoder;
import com.hirshi001.networking.packethandlercontext.PacketType;
import com.hirshi001.networking.packetregistrycontainer.PacketRegistryContainer;
import com.hirshi001.networking.packetregistrycontainer.SinglePacketRegistryContainer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class GameApp extends Game {

    private static GameApp instance;

    public static GameApp getInstance() {
        return instance;
    }

    public final Disposable disposeWhenClose;
    public final BufferFactory bufferFactory;
    public final NetworkFactory networkFactory;

    public Client client;

    public Player player;

    final int port;

    static String[] namePrefixes = {"Bob", "Alice", "Charlie", "David", "Eve", "Frank", "Grace", "Heidi", "Ivan", "Judy", "Mallory", "Oscar", "Peggy", "Trent", "Victor", "Walter", "Zoe"};
    static String[] nameSuffixes = {"Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson"};

    final String name = namePrefixes[(int) (Math.random() * namePrefixes.length)] + " " + nameSuffixes[(int) (Math.random() * nameSuffixes.length)];

    public GameApp(Disposable disposeWhenClose, BufferFactory bufferFactory, NetworkFactory networkFactory, int port) {
        this.disposeWhenClose = disposeWhenClose;
        this.bufferFactory = bufferFactory;
        this.networkFactory = networkFactory;
        this.port = port;
        GameSettings.bufferFactory = bufferFactory;
    }

    @Override
    public void create() {
        instance = this;

        PacketRegistryContainer packetRegistryContainer = new SinglePacketRegistryContainer();

        packetRegistryContainer.getDefaultRegistry()
                .registerDefaultPrimitivePackets()
                .register(MaintainConnectionPacket::new, null, MaintainConnectionPacket.class, 0)
                .register(RequestPlayerUUIDPacket::new, null, RequestPlayerUUIDPacket.class, 1)
                .register(FetchLobbyPacket::new, null, FetchLobbyPacket.class, 2)
                .register(JoinLobbyPacket::new, null, JoinLobbyPacket.class, 3)
                .register(LobbyBatchPacket::new, null, LobbyBatchPacket.class, 4)
                .register(UUIDPacket::new, null, UUIDPacket.class, 5)
                .register(LobbyPacket::new, null, LobbyPacket.class, 6)
                .register(PlayerUpdateLobbyPacket::new, PacketHandlers::handlePlayerUpdateLobbyPacket, PlayerUpdateLobbyPacket.class, 7)
                .register(LeaveLobbyPacket::new, null, LeaveLobbyPacket.class, 8)
                .register(InitGamePacket::new, PacketHandlers::handleInitGamePacket, InitGamePacket.class, 9)
                .register(PlayerMovePacket::new, null, PlayerMovePacket.class, 10)
                .register(SyncPacket::new, PacketHandlers::handleSyncPacket, SyncPacket.class, 11)
                .register(DeleteEntityPacket::new, PacketHandlers::handleDeleteEntityPacket, DeleteEntityPacket.class, 12)
                .register(AddEntityPacket::new, PacketHandlers::handleAddEntityPacket, AddEntityPacket.class, 13)
                .register(UseSpecialOnePacket::new, null, UseSpecialOnePacket.class, 14);



        NetworkData networkData = new DefaultNetworkData(new SimplePacketEncoderDecoder(), packetRegistryContainer);
        try {
            client = networkFactory.createClient(networkData, bufferFactory, "localhost", port);
            client.setClientOption(ClientOption.TCP_PACKET_CHECK_INTERVAL, -1);
            client.setClientOption(ClientOption.UDP_PACKET_CHECK_INTERVAL, -1);

            client.setChannelInitializer(new ChannelInitializer() {
                @Override
                public void initChannel(Channel channel) {
                    channel.setChannelOption(ChannelOption.DEFAULT_SWITCH_PROTOCOL, true);
                }
            });

            client.addClientListeners(new AbstractChannelListener() {
                @Override
                public void onTCPConnect(Channel channel) {
                    channel.sendWithResponse(
                                    new RequestPlayerUUIDPacket(name),
                                    null, PacketType.TCP,
                                    TimeUnit.SECONDS.toMillis(30))
                            .then(p -> {
                                System.out.println("Setting player object");
                                player = Player.getOrCreatePlayer(((UUIDPacket) p.packet).uuid, p.channel, name);
                                System.out.println(player.getId());
                            }).perform();

                    super.onTCPConnect(channel);
                }
            });

            if (client.supportsTCP()) client.startTCP().perform().get();
            if (client.supportsUDP()) client.startUDP().perform().get();

            Thread.sleep(10000);

        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        setScreen(new MenuScreen());
    }

    @Override
    public void setScreen(Screen screen) {
        if (screen == null) {
            throw new IllegalArgumentException("Screen cannot be null");
        }
        if (screen instanceof AssetedScreen) {
            AssetedScreen assetedScreen = (AssetedScreen) screen;
            if (assetedScreen.getAssetManager() == null)
                setScreen(new AssetLoadingScreen(assetedScreen));
            else
                super.setScreen(screen);
        } else
            super.setScreen(screen);
    }


    float time = 0;

    @Override
    public void render() {
        time += Gdx.graphics.getDeltaTime();
        if (time > 5) {
            time = 0;
            client.getChannel().sendTCP(new MaintainConnectionPacket(), null).perform();
        }
        if (client.getChannel() != null) {
            if(client.supportsTCP()) {
                client.getChannel().checkTCPPackets();
                client.getChannel().flushTCP();
            }
            if(client.supportsUDP()) {
                client.getChannel().checkUDPPackets();
                client.getChannel().flushUDP();
            }
        }
        super.render();

    }

    @Override
    public void dispose() {
        super.dispose();
        if (disposeWhenClose != null) disposeWhenClose.dispose();
    }
}
