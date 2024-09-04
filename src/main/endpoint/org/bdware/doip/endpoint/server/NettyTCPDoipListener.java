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
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;

public class NettyTCPDoipListener extends NettyDoipListener {

    private final int port;
    static Logger logger = LogManager.getLogger(NettyTCPDoipListener.class);
    private Channel ch;

    public NettyTCPDoipListener(int port, DoipListenerConfig listenerConfig) {
        this.port = port;
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
        int maxFrame = 5 * 1024 * 1024;
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                    new WriteBufferWaterMark(2 * maxFrame, 10 * maxFrame));
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(port);
            b.childHandler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipline = ch.pipeline();

                            pipline.addLast(new LengthFieldBasedFrameDecoder(maxFrame,
                                            20, 4, 0, 0))
                                    .addLast(new MessageEnvelopeCodec())
                                    .addLast(new MessageEnvelopeAggregator(maxFrame - MessageEnvelope.ENVELOPE_LENGTH));
                            listenerConfig.addExtraCodec(pipline);
                            pipline.addLast(handler);
                        }
                    });

            ch = b.bind().syncUninterruptibly().channel();
            logger.info("TCP DOIP listener start at:" + port);
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
