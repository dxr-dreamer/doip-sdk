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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Future;
import org.bdware.doip.codec.DatagramPacketToMessageEnvelopeCodec;
import org.bdware.doip.codec.SenderInjector;
import org.bdware.doip.codec.MessageEnvelopePrinter;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class NettyDoipUDPClientChannel extends NettyDoipClientChannel {

    int clientPort;
    EventLoopGroup group;
    final Bootstrap b = new Bootstrap();

    public NettyDoipUDPClientChannel() throws URISyntaxException {
        this(0);
    }

    public NettyDoipUDPClientChannel(int cp) throws URISyntaxException {
        clientPort = cp;
    }

    @Override
    public void close() {
        remoteAddress = null;
        isConnected = false;
        if (handler != null) handler.close();
        if (group != null) {
            Future<?> future = group.shutdownGracefully();
            future.syncUninterruptibly();
        }
    }

    @Override
    public void connect(String targetUrl) throws URISyntaxException, InterruptedException {
        URI uri = new URI(targetUrl);
        if (group == null) {
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            b.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                    new WriteBufferWaterMark(10, 100));
            group = new NioEventLoopGroup(10);
            b.group(group);
            super.handler = new NettyDoipClientHandler();
            b.channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(
                            new ChannelInitializer<DatagramChannel>() {
                                @Override
                                protected void initChannel(DatagramChannel ch) {
                                    ch.pipeline().addLast(new DatagramPacketToMessageEnvelopeCodec(uri.getHost(), uri.getPort()))
                                            .addLast(new SenderInjector(uri.getHost(), uri.getPort()))
                                            .addLast(new MessageEnvelopePrinter())
                                            .addLast(new MessageEnvelopeAggregator(MessageEnvelopeAggregator.MTU_802_3 - MessageEnvelope.ENVELOPE_LENGTH))
                                            .addLast(handler);
                                }
                            });
        }
        remoteAddress = new InetSocketAddress(uri.getHost(), uri.getPort());
        super.channel = b.bind(clientPort).sync().channel();
        handler.setChannel(channel);
        isConnected = true;
    }
}
