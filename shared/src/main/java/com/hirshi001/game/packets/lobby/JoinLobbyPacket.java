package com.hirshi001.game.packets.lobby;

import com.hirshi001.game.packets.util.UUIDPacket;
import com.hirshi001.game.util.UUID;

public class JoinLobbyPacket extends UUIDPacket {

    public JoinLobbyPacket() {
        super();
    }

    public JoinLobbyPacket(UUID uuid) {
        super(uuid);
    }
}
