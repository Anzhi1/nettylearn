package io.netty.learn.common;

import io.netty.buffer.ByteBuf;
import io.netty.learn.util.JsonUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class Message<T extends MessageBody>{

    private MessageHeader messageHeader;

    private T messageBody;


    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(MessageHeader messageHeader) {
        this.messageHeader = messageHeader;
    }

    public T getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(T messageBody) {
        this.messageBody = messageBody;
    }

    public void encode(ByteBuf byteBuf){
        byteBuf.writeInt(messageHeader.getVersion());
        byteBuf.writeLong(messageHeader.getStreamId());
        byteBuf.writeInt(messageHeader.getOpcode());
        byteBuf.writeBytes(JsonUtil.toJson(messageBody).getBytes());
    }

    public abstract Class<T> getMessageBodyDecodeClass(int opcode);

    public void decode(ByteBuf msg){
        int version = msg.readInt();
        long streamId = msg.readLong();
        int opCode = msg.readInt();
        MessageHeader messageHeader = new MessageHeader(version,opCode,streamId);
        this.setMessageHeader(messageHeader);

        Class<T> bodyClazz = getMessageBodyDecodeClass(opCode);
        T body = JsonUtil.fromJson(msg.toString(StandardCharsets.UTF_8),bodyClazz);
        this.setMessageBody(body);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messageBody=" + messageBody +
                '}';
    }
}
