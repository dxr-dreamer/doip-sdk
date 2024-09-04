/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.NaiveEnvelopeToDoMessage;
import org.bdware.doip.codec.WebSocketFrameToByteBufCodec;

public class NettyWSDoipListener extends NettyDoipListener {

    private final int port;
    private final String path;
    static Logger logger = LogManager.getLogger(NettyTCPDoipListener.class);
     private Channel ch;

    public NettyWSDoipListener(int port, String path, DoipListenerConfig listenerConfig) {
        this.port = port;
        this.path = path;
        this.listenerConfig = listenerConfig;
    }

    @Override
    public void start() {
        if (handler == null) {
            logger.error("Handler not init yet! set handler first");
            return;
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port);
            b.childHandler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(65536))
                                    .addLast(new WebSocketServerProtocolHandler(path,null, true))
                                    .addLast(new WebSocketFrameToByteBufCodec())
                                    .addLast(new LengthFieldBasedFrameDecoder(65536,
                                            20, 4, 0, 0))
                                    .addLast(new MessageEnvelopeCodec())
                                    .addLast(new NaiveEnvelopeToDoMessage());
                            listenerConfig.addExtraCodec(ch.pipeline());
                            ch.pipeline().addLast(handler);

                        }
                    });

            ch = b.bind().syncUninterruptibly().channel();
            logger.info("WS DOIP listener start at:" + port + path);
            if (startServerCallback != null)
                startServerCallback.onSuccess(port);
            ch.closeFuture().sync();
        } catch (Exception e) {
            if (startServerCallback != null)
                startServerCallback.onException(e);
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        ch.close();
    }
}
