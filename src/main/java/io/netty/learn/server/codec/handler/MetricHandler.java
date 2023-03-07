package io.netty.learn.server.codec.handler;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@ChannelHandler.Sharable
public class MetricHandler extends ChannelDuplexHandler {
    private final AtomicLong totalConnectNum = new AtomicLong();
    {
        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.register("totalConnectNum", (Gauge<Long>) totalConnectNum::longValue);

        //在Console中输出，每5s一次
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metricRegistry).build();
        consoleReporter.start(20, TimeUnit.SECONDS);

        //需要使用jmc来查看
/*        JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
        jmxReporter.start();*/
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        totalConnectNum.incrementAndGet();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        totalConnectNum.decrementAndGet();
        super.channelInactive(ctx);
    }
}
