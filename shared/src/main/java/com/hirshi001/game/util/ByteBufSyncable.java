package com.hirshi001.game.util;

import com.hirshi001.buffer.buffers.ByteBuffer;

public interface ByteBufSyncable {

    void writeSyncBytes(ByteBuffer out);

    void readSyncBytes(ByteBuffer in);

}
