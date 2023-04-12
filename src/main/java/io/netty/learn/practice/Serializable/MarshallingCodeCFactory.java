package io.netty.learn.practice.Serializable;

import io.netty.handler.codec.marshalling.*;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
public class MarshallingCodeCFactory {

    /**
     * 创建Jboss Marshalling解码器MarshallingDecoder
     *
     * @return
     */
    public static MarshallingDecoder buildMarshallingDecoder() {
        //"serial"表示Java序列化工厂对象
        final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        UnmarshallerProvider provider = new DefaultUnmarshallerProvider(marshallerFactory, configuration);
        //1024表示单个消息序列化的最大长度
        return new MarshallingDecoder(provider, 1024);
    }

    /**
     * 创建Jboss Marshalling编码器MarshallingEncoder
     *
     * @return
     */
    public static MarshallingEncoder buildMarshallingEncoder() {
       final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("serial");
       final MarshallingConfiguration configuration = new MarshallingConfiguration();
       configuration.setVersion(5);
       MarshallerProvider provider = new DefaultMarshallerProvider(marshallerFactory,configuration);
       //encoder用于将是按了序列化接口的POJO对象序列化为二进制数组
        return new MarshallingEncoder(provider);
    }
}
