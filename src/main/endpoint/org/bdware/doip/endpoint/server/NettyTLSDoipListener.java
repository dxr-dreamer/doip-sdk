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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.MessageEnvelopeCodec;
import org.bdware.doip.codec.NaiveEnvelopeToDoMessage;

import javax.net.ssl.SSLEngine;
import java.io.File;

public class NettyTLSDoipListener extends NettyDoipListener {
    private final int port;
    static Logger logger = LogManager.getLogger(NettyTLSDoipListener.class);
    private Channel ch;
    SslContext sslContext;

    public NettyTLSDoipListener(int port, TLSListenerInfo listenerInfo) {
        this.port = port;
        super.listenerConfig = listenerInfo;
        if (!listenerInfo.getKeyFile().exists() || !listenerInfo.getChainKeyFile().exists())
            throw new IllegalStateException("missing key file or key chain file");
        sslContext = getSSLContext(listenerInfo.getChainKeyFile(), listenerInfo.getKeyFile());
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
            b.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                    new WriteBufferWaterMark(2 * 5 * 1024 * 1024, 10 * 5 * 1024 * 1024));
            b.childHandler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            SSLEngine engine = sslContext.newEngine(ch.alloc());
                            engine.setUseClientMode(false);
                            engine.setNeedClientAuth(false);
                            ch.pipeline()
                                    .addFirst("ssl", new SslHandler(engine))
                                    .addLast(new LengthFieldBasedFrameDecoder(5 * 1024 * 1024,
                                            20, 4, 0, 0))
                                    .addLast(new MessageEnvelopeCodec())
                                    .addLast(new NaiveEnvelopeToDoMessage())
                                    .addLast(handler);
                        }
                    });
            ch = b.bind().syncUninterruptibly().channel();
            logger.info("TLS DOIP listener start at:" + port);
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
