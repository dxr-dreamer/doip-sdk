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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.v2_0.Delimiters;
import org.bdware.doip.codec.v2_0.DoipMessageCodec;
import org.bdware.doip.endpoint.server.NettyDoipListener;
import org.bdware.doip.endpoint.server.NettyTLSDoipListener;
import org.bdware.doip.endpoint.server.TLSListenerInfo;

import javax.net.ssl.SSLEngine;
import java.io.File;

public class DOIPV2DoipListener extends NettyDoipListener {
    private final int port;
    static Logger LOGGER = LogManager.getLogger(NettyTLSDoipListener.class);
    private Channel ch;
    SslContext sslContext;


    public DOIPV2DoipListener(int port, TLSListenerInfo conf) throws Exception {
        this.port = port;
        super.listenerConfig = conf;
        if (!conf.getChainKeyFile().exists() || !conf.getKeyFile().exists())
            throw new IllegalStateException("missing key file or key chain file");
        sslContext = getSSLContext(conf.getChainKeyFile(), conf.getKeyFile());
    }

    @Override
    public void start() {
        if (handler == null) {
            LOGGER.error("Handler not init yet! set handler first");
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
                                    .addLast(new DelimiterBasedFrameDecoder(5 * 1024 * 1024, Unpooled.wrappedBuffer(Delimiters.EOF)))
                                    .addLast(new DoipMessageCodec())
                                    .addLast(handler);
                        }
                    });
            ch = b.bind().syncUninterruptibly().channel();
            LOGGER.info("TLS DOIP listener start at:" + port);
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
