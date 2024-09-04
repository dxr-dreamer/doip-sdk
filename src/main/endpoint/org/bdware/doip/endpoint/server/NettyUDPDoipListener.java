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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.DatagramPacketToMessageEnvelopeCodec;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.MessageEnvelopePrinter;

public class NettyUDPDoipListener extends NettyDoipListener {
    private final int port;
    static Logger logger = LogManager.getLogger(NettyUDPDoipListener.class);
    private Channel ch;

    public NettyUDPDoipListener(int port, DoipListenerConfig config) throws Exception {
        this.port = port;
        this.listenerConfig = config;
    }

    @Override
    public void start() {
        if (handler == null) {
            logger.error("Handler not init yet! set handler first");
            return;
        }
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .localAddress(port)
                    .option(ChannelOption.SO_BROADCAST, true);
            b.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                    new WriteBufferWaterMark(0, 100));
            b.handler(
                    new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            logger.info("establish a channel:" + port);

                            ch.pipeline()
                                    .addLast(new DatagramPacketToMessageEnvelopeCodec())
                                    .addLast(new MessageEnvelopePrinter())
                                    .addLast(new MessageEnvelopeAggregator(MessageEnvelopeAggregator.MTU_802_3 - 24));
                            listenerConfig.addExtraCodec(ch.pipeline());
                            ch.pipeline().addLast(handler);
                        }
                    });
            ch = b.bind().syncUninterruptibly().channel();
            logger.info("UDP DOIP listener start at:" + port);
            if (startServerCallback != null)
                startServerCallback.onSuccess(port);
            ch.closeFuture().sync();
        } catch (Exception e) {
            if (startServerCallback != null)
                startServerCallback.onException(e);
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        ch.close();
    }

}
