/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.v2_0;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.MessageEnvelopeAggregator;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.v2_0.Delimiters;
import org.bdware.doip.codec.v2_0.DoipMessageCodec;
import org.bdware.doip.endpoint.client.NettyDoipClientChannel;
import org.bdware.doip.endpoint.client.NettyDoipClientHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class DOIPV2ClientChannel extends NettyDoipClientChannel {
    static Logger LOGGER = LogManager.getLogger(DOIPV2ClientChannel.class);

    final Bootstrap b = new Bootstrap();
    static EventLoopGroup group;
    private final TrustManager[] trustManagers;

    public DOIPV2ClientChannel(TrustManager[] trustManagers) {
        super();
        this.trustManagers = trustManagers;
    }

    @Override
    public void close() {
        try {
        	channel.unsafe().closeForcibly();
        }catch(Throwable e) {}
        
        if (handler != null) {
          try {
        	handler.close();
          }catch(Exception e) {}
        }
        isConnected = false;
        try {
        	//group.shutdownGracefully();
        }catch(Exception e) {}
    }

    @Override
    public void connect(String targetUrl) throws URISyntaxException, InterruptedException {
        URI uri = new URI(targetUrl);
        LOGGER.debug("[URI Parse]scheme:" + uri.getScheme() + "  host: " + uri.getHost() + "  port: " + uri.getPort());
        if (group == null) {
            group = new NioEventLoopGroup();
        }
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        b.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                new WriteBufferWaterMark(1024 * 1024, 20 * 1024 * 1024))
         .option(ChannelOption.SO_REUSEADDR, true)
         .option(ChannelOption.SO_LINGER, 0);
        b.group(group);
        handler = new NettyDoipClientHandler();
        int maxFrameLength = 5 * 1024 * 1024;
        b.channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline p = ch.pipeline();
                                SSLEngine engine = null;
                                try {
                                    SSLContext clientContext = SSLContext.getInstance("TLS");
                                    clientContext.init(null, trustManagers, null);
                                    engine = clientContext.createSSLEngine();
                                    engine.setUseClientMode(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                p.addFirst("ssl", new SslHandler(engine))
                                        .addLast(new DelimiterBasedFrameDecoder(maxFrameLength, Unpooled.copiedBuffer(Delimiters.EOF)))
                                        .addLast(new DoipMessageCodec())
                                        .addLast(handler);
                            }
                        });
        LOGGER.info("[DoipClient] Create TCP Client!");

        LOGGER.debug("[URI Parse]host: " + uri.getHost() + "  port: " + uri.getPort());
        channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.setChannel(channel);
        
        channel.config().setOption(ChannelOption.SO_LINGER, 0);
        isConnected = true;
    }

    protected SslContext getSSLContext(File chainFile, File keyFile) throws IllegalStateException {
        try {
            SslContext sslContext =
                    SslContextBuilder.forServer(chainFile, keyFile)
                            .ciphers(
                                    null,
                                    (ciphers, defaultCiphers, supportedCiphers) ->
                                            defaultCiphers.stream()
                                                    .filter(x -> null != x && !x.contains("RC4"))
                                                    .toArray(String[]::new))
                            .build();
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

    }

}
