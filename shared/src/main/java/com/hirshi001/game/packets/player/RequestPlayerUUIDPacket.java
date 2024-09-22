package com.hirshi001.game.packets.player;

import com.hirshi001.networking.packet.Packet;
import com.hirshi001.networking.util.defaultpackets.primitivepackets.StringPacket;

public class RequestPlayerUUIDPacket extends StringPacket {

    public RequestPlayerUUIDPacket() {
        super();
    }

    public RequestPlayerUUIDPacket(String value) {
        super(value);
    }
}
