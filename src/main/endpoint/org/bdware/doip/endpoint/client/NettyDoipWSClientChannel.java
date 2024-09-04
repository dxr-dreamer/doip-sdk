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
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.NaiveEnvelopeToDoMessage;
import org.bdware.doip.codec.WebSocketFrameToByteBufCodec;

import java.net.URI;
import java.net.URISyntaxException;

public class NettyDoipWSClientChannel extends NettyDoipClientChannel {

    final Bootstrap b = new Bootstrap();
    static EventLoopGroup group;

    public NettyDoipWSClientChannel() {
        super();
    }

    @Override
    public void close() {
        if (handler != null) handler.close();
        isConnected = false;
    }

    @Override
    public void connect(String targetUrl) throws URISyntaxException, InterruptedException {
        URI uri = new URI(targetUrl);
        logger.debug("[URI Parse]scheme:" + uri.getScheme() + "  host: " + uri.getHost() + "  port: " + uri.getPort());
        if (group == null) {
            group = new NioEventLoopGroup();
        }
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        b.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                new WriteBufferWaterMark(1024 * 1024, 20 * 1024 * 1024));

        b.group(group);
        handler = new NettyDoipClientHandler();

        b.channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                WebSocketClientProtocolHandler wsClientHandler = new WebSocketClientProtocolHandler(
                                        WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));
                                p.addLast(new HttpClientCodec())
                                        .addLast(new HttpObjectAggregator(65536))
                                        .addLast(wsClientHandler)
                                        .addLast(new WebSocketFrameToByteBufCodec())
                                        .addLast(new LengthFieldBasedFrameDecoder(65536,
                                                20, 4, 0, 0))
                                        .addLast(new MessageEnvelopeCodec())
                                        .addLast(new NaiveEnvelopeToDoMessage())
                                        .addLast(handler);
                            }
                        });
        logger.info("[DoipClient] Create TCP Client!");
        logger.debug("[URI Parse]host: " + uri.getHost() + "  port: " + uri.getPort());
        channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.setChannel(channel);

        isConnected = true;
    }

}
