/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NettyDoipTCPClientChannel extends NettyDoipClientChannel {

    Bootstrap b;
    static EventLoopGroup group;

    boolean splitEnvelop;
    int maxFrameLength;

    public NettyDoipTCPClientChannel(boolean splitEnvelop, int maxFrameLength, ClientConfig config) {
        super();
        this.splitEnvelop = splitEnvelop;
        this.maxFrameLength = maxFrameLength;
        handler = new NettyDoipClientHandler();
        synchronized (NettyDoipTCPClientChannel.class) {
            if (group == null)
                group = new NioEventLoopGroup(new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = Executors.defaultThreadFactory().newThread(r);
                        thread.setDaemon(true);
                        return thread;
                    }
                });
        }
        b = new Bootstrap();
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_LINGER, 0);
        b.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                new WriteBufferWaterMark(2 * maxFrameLength, 10 * maxFrameLength));
        b.group(group);
        b.channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                p.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength,
                                                20, 4, 0, 0))
                                        .addLast(new MessageEnvelopeCodec());
//                                if (splitEnvelop) {
//                                    p.addLast(new NaiveEnvelopToDoMessage());
//                                } else
                                {
                                    p.addLast(new MessageEnvelopeAggregator(maxFrameLength - MessageEnvelope.ENVELOPE_LENGTH));
                                }
                                config.addSignerAndEncryptionTransmission(p);
                                p.addLast(handler);
                            }
                        });

    }

    public NettyDoipTCPClientChannel(ClientConfig config) {
        this(false, 5 * 1024 * 1024, config);
    }

    @Override
    public void close() {
        try {
            channel.unsafe().closeForcibly();
        } catch (Throwable e) {
        }

        if (handler != null) {
            try {
                handler.close();
            } catch (Throwable e) {
            }
        }
        isConnected = false;
    }

    @Override
    public void connect(String targetUrl) throws URISyntaxException, InterruptedException {
        URI uri = new URI(targetUrl);
        logger.debug("[URI Parse]scheme:" + uri.getScheme() + "  host: " + uri.getHost() + "  port: " + uri.getPort());
        logger.debug("[URI Parse]host: " + uri.getHost() + "  port: " + uri.getPort());
        channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.setChannel(channel);
        channel.config().setOption(ChannelOption.SO_LINGER, 0);
        isConnected = true;
    }

}
