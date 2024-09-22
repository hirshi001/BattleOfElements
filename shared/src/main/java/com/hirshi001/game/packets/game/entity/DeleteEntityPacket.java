package com.hirshi001.game.packets.game.entity;

import com.hirshi001.game.packets.util.UUIDPacket;
import com.hirshi001.game.util.UUID;

public class DeleteEntityPacket extends UUIDPacket {

    public DeleteEntityPacket() {
        super();
    }

    public DeleteEntityPacket(UUID uuid) {
        super(uuid);
    }
}
