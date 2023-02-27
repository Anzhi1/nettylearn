package io.netty.learn.common;

public class MessageHeader {
    private int version =1 ;
    private int opcode;
    private long streamId;


    public MessageHeader() {
    }

    public MessageHeader(int version, int opcode, long streamId) {
        this.version = version;
        this.opcode = opcode;
        this.streamId = streamId;
    }

    public int getVersion() {
        return version;
    }

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public long getStreamId() {
        return streamId;
    }

    public void setStreamId(long streamId) {
        this.streamId = streamId;
    }

    @Override
    public String toString() {
        return "MessageHeader{" +
                "version=" + version +
                ", opcode=" + opcode +
                ", streamId=" + streamId +
                '}';
    }
}


