package com.hirshi001.game.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.hirshi001.buffer.bufferfactory.BufferFactory;
import com.hirshi001.buffer.bufferfactory.DefaultBufferFactory;
import com.hirshi001.game.connection.Player;
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
import com.hirshi001.game.server.lobby.LobbyManager;
import com.hirshi001.game.server.packets.PacketHandlers;
import com.hirshi001.game.settings.GameSettings;
import com.hirshi001.game.settings.Network;
import com.hirshi001.javanetworking.JavaNetworkFactory;
import com.hirshi001.javarestapi.JavaRestFutureFactory;
import com.hirshi001.networking.network.NetworkFactory;
import com.hirshi001.networking.network.channel.Channel;
import com.hirshi001.networking.network.channel.ChannelInitializer;
import com.hirshi001.networking.network.channel.ChannelOption;
import com.hirshi001.networking.network.server.AbstractServerListener;
import com.hirshi001.networking.network.server.Server;
import com.hirshi001.networking.network.server.ServerListener;
import com.hirshi001.networking.network.server.ServerOption;
import com.hirshi001.networking.networkdata.DefaultNetworkData;
import com.hirshi001.networking.networkdata.NetworkData;
import com.hirshi001.networking.packethandlercontext.PacketHandlerContext;
import com.hirshi001.networking.packetregistrycontainer.PacketRegistryContainer;
import com.hirshi001.networking.packetregistrycontainer.SinglePacketRegistryContainer;
import com.hirshi001.restapi.RestAPI;
import com.hirshi001.websocketnetworkingserver.WebsocketServer;
import logger.ConsoleColors;
import logger.DateStringFunction;
import logger.Logger;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerApplication extends ApplicationAdapter {

    public static NetworkFactory networkFactory;
    public static BufferFactory bufferFactory;
    public static ScheduledExecutorService executorService;

    public static Logger logger;
    public static PrintStream sysout, syserr;

    private final int websocketPort;
    private final int javaPort;

    private static ServerApplication instance;

    public LobbyManager lobbyManager = new LobbyManager();
    private Server javaServer, websocketServer;

    private String password;

    public static ServerApplication instance() {
        return instance;
    }

    public ServerApplication(String password, int websocketPort, int javaPort) {
        super();
        this.password = password;
        this.websocketPort = websocketPort;
        this.javaPort = javaPort;
        instance = this;
    }

    @Override
    public void create() {

        sysout = System.out;
        syserr = System.err;
        logger = new Logger(System.out, System.err,
                new DateStringFunction(ConsoleColors.CYAN, "[", "]")
        );
        logger.debug();
        logger.debugShort(true);

        System.setOut(logger);
        System.out.println("Starting server...");

        RestAPI.setFactory(new JavaRestFutureFactory());
        executorService = Executors.newScheduledThreadPool(3);
        networkFactory = new JavaNetworkFactory(executorService);
        bufferFactory = new DefaultBufferFactory();
        GameSettings.bufferFactory = bufferFactory;
        try {
            startServer(websocketPort, javaPort);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public void startServer(int websocketPort, int javaPort) throws IOException, ExecutionException, InterruptedException {

        GameSettings.runnablePoster = Gdx.app::postRunnable;

        PacketRegistryContainer registryContainer = new SinglePacketRegistryContainer();

        registryContainer.getDefaultRegistry()
                .registerDefaultPrimitivePackets()
                .register(MaintainConnectionPacket::new, null, MaintainConnectionPacket.class, 0)
                .register(RequestPlayerUUIDPacket::new, PacketHandlers::handleRequestPlayerUUIDPacket, RequestPlayerUUIDPacket.class, 1)
                .register(FetchLobbyPacket::new, PacketHandlers::handleFetchLobbyPacket, FetchLobbyPacket.class, 2)
                .register(JoinLobbyPacket::new, PacketHandlers::handleJoinLobbyPacket, JoinLobbyPacket.class, 3)
                .register(LobbyBatchPacket::new, null, LobbyBatchPacket.class, 4)
                .register(UUIDPacket::new, null, UUIDPacket.class, 5)
                .register(LobbyPacket::new, null, LobbyPacket.class, 6)
                .register(PlayerUpdateLobbyPacket::new, null, PlayerUpdateLobbyPacket.class, 7)
                .register(LeaveLobbyPacket::new, PacketHandlers::leaveLobbyPacket, LeaveLobbyPacket.class, 8)
                .register(InitGamePacket::new, null, InitGamePacket.class, 9)
                .register(PlayerMovePacket::new, PacketHandlers::handlePlayerMovePacket, PlayerMovePacket.class, 10)
                .register(SyncPacket::new, null, SyncPacket.class, 11)
                .register(DeleteEntityPacket::new, null, DeleteEntityPacket.class, 12)
                .register(AddEntityPacket::new, null, AddEntityPacket.class, 13)
                .register(UseSpecialOnePacket::new, PacketHandlers::handleUseSpecialOnePacket, UseSpecialOnePacket.class, 14);

        NetworkData networkData = new DefaultNetworkData(Network.PACKET_ENCODER_DECODER, registryContainer);
        ChannelInitializer channelInitializer = channel -> {
            channel.setChannelOption(ChannelOption.TCP_AUTO_FLUSH, true);
            channel.setChannelOption(ChannelOption.UDP_AUTO_FLUSH, true);
            channel.setChannelOption(ChannelOption.PACKET_TIMEOUT, TimeUnit.SECONDS.toNanos(30));
            channel.setChannelOption(ChannelOption.DEFAULT_SWITCH_PROTOCOL, true);
        };

        ServerListener serverListener = new AbstractServerListener() {
            @Override
            public void onClientConnect(Server server, Channel channel) {
                System.out.println("Client connected " + System.identityHashCode(channel) + " : " + Arrays.toString(channel.getAddress()) + " : " + channel.getPort());

            }

            @Override
            public void onClientDisconnect(Server server, Channel channel) {
                System.out.println("Client disconnected " + System.identityHashCode(channel) + " : " + Arrays.toString(channel.getAddress()) + " : " + channel.getPort());
                Gdx.app.postRunnable(() -> {
                    Object attachment = channel.getAttachment();
                    if(attachment instanceof Player) {
                        Player player = (Player) attachment;
                        lobbyManager.onPlayerDisconnect(player);
                        Player.removePlayer(player.getId());
                    }
                });
            }

            @Override
            public void onReceived(PacketHandlerContext<?> context) {
                // System.out.println("Received packet: " + context.packet.getClass());
            }

            @Override
            public void onSent(PacketHandlerContext<?> context) {
            }
        };

        // start websocket server
        websocketServer = new WebsocketServer(RestAPI.getDefaultExecutor(), networkData, bufferFactory, websocketPort); // networkFactory.createServer(networkData, bufferFactory, port);
        // TODO: Update the WebsocketServer code sot hat it has packet timeout functionality and potentially other things it is currently missing
        websocketServer.setChannelInitializer(channelInitializer);
        websocketServer.setServerOption(ServerOption.TCP_PACKET_CHECK_INTERVAL, -1);
        websocketServer.addServerListener(serverListener);
        setSSL((WebsocketServer) websocketServer, password); // TODO: Set Keystore password
        websocketServer.startTCP().onFailure(Throwable::printStackTrace).perform().get();
        System.out.println("WebsocketServer started on " + websocketServer.getPort());

        // start java server
        javaServer = networkFactory.createServer(networkData, bufferFactory, javaPort);
        javaServer.setChannelInitializer(channelInitializer);
        javaServer.setServerOption(ServerOption.TCP_PACKET_CHECK_INTERVAL, -1);
        javaServer.setServerOption(ServerOption.UDP_PACKET_CHECK_INTERVAL, -1);
        javaServer.addServerListener(serverListener);
        javaServer.startTCP().perform().get();
        javaServer.startUDP().perform().get();
        System.out.println("JavaServer started on " + javaServer.getPort());

    }

    private void setSSL(WebsocketServer websocketServer, String keystorePassword) {
        if (keystorePassword == null) {
            System.out.println("No keystore password provided, not setting SSL");
            return;
        }
        try {
            final String password = keystorePassword;
            final char[] passwordChars = password.toCharArray();

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream("cert.pkcs12"), passwordChars);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, passwordChars);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);


            websocketServer.setWebsocketSocketServerFactory(new DefaultSSLWebSocketServerFactory(sslContext));
            System.out.println("SSL Set");
        } catch (Exception e) {
            System.err.println("Failed to set SSL");
            e.printStackTrace();
        }
    }

    @Override
    public void render() {
        try {

            lobbyManager.update();
            Player.cleanup();

            javaServer.checkTCPPackets();
            javaServer.checkUDPPackets();
            websocketServer.checkTCPPackets();
            websocketServer.checkUDPPackets();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        javaServer.close();
        websocketServer.close();

        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            e.printStackTrace();
        }
    }
}
