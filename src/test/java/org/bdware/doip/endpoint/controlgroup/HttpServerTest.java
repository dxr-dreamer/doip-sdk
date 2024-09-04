/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.controlgroup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpServerTest {
    static Logger LOGGER = LogManager.getLogger(HttpServerTest.class);

    public static void main(String[] args) {
        new HttpServerTest().run();
    }

    //[INFO ]20:42:10.386 Final Result:10000/10000 dur:3992 (HttpServerTest.java:100)
    //[INFO ]00:21:40.049 Final Result:10000/10000 dur:2927 (HttpServerTest.java:104)
    //[INFO ]00:08:52.531 Final Result:99970/100000 dur:211947 (HttpServerTest.java:100)
    @Test
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b1 = new ServerBootstrap();
            b1.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            HttpHandler h = new HttpHandler();
            b1.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(21044)
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel arg0) {
                                    arg0.pipeline()
                                            .addLast(new HttpServerCodec())
                                            .addLast(new HttpObjectAggregator(10 * 1024 * 1024))
                                            .addLast(h);
                                }
                            });
            final Channel ch = b1.bind(21044).sync().channel();
            LOGGER.debug("[CMHttpServer] listen master port at:" + 21044);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (; ; ) ;
    }

    //=========From Client To Ali============
    //[INFO ]17:51:00.700 http://127.0.0.1:21044/ Final Result:10000/10000 dur:2139 rps:4675.08  (HttpServerTest.java:135)
    //[INFO ]23:58:36.152 http://39.104.208.148:21044/ Final Result:1000/1000 dur:8964 rps:111.56
    //=========From Ali To Ali============
    //[INFO ]00:06:06.550 http://39.104.208.148:21043/ Final Result:10000/10000 dur:1373 zdl:7283.32
    //[INFO ]00:07:07.120 http://39.104.208.148:21043/ Final Result:10000/10000 dur:1298 zdl:7704.16
    @Test
    public void smallReqSmallResp() throws Exception {
        testClient(serverAddr, 1000, "bdware.test/small", "");
    }

    //[INFO ]17:51:36.493 http://127.0.0.1:21044/ Final Result:1000/1000 dur:21229 rps:47.11  (HttpServerTest.java:135)
    //[INFO ]23:59:21.120 http://39.104.208.148:21044/ Final Result:100/100 dur:29175 rps:3.43
    //=========From Ali To Ali============
    //[INFO ]00:07:54.345 http://39.104.208.148:21043/ Final Result:1000/1000 dur:8412 zdl:118.88
    //[INFO ]00:08:17.069 http://39.104.208.148:21043/ Final Result:1000/1000 dur:8438 zdl:118.51
    @Test
    public void smallReqLargeResp() throws Exception {
        testClient(serverAddr, 100, "bdware.test/large", "");
    }

    // [INFO]17:55:02.653 http://127.0.0.1:21044/ Final Result:1000/1000 dur:1401 rps:713.78  (HttpServerTest.java:135)
    //[INFO ]00:00:00.780 http://39.104.208.148:21044/ Final Result:100/100 dur:22476 rps:4.45

    //=========From Ali To Ali============
    //[INFO ]00:08:37.819 http://39.104.208.148:21043/ Final Result:1000/1000 dur:6242 zdl:160.21
    //[INFO ]00:09:00.098 http://39.104.208.148:21043/ Final Result:1000/1000 dur:6174 zdl:161.97
    // Optimized by Zero-Copy
    @Test
    public void largeReqSmallResp() throws Exception {
        testClient(serverAddr, 100, "bdware.test/small", longStr);
    }

    //[INFO ]17:56:18.769 http://127.0.0.1:21044/ Final Result:1000/1000 dur:20680 rps:48.36  (HttpServerTest.java:136)
    //[INFO ]00:01:13.328 http://39.104.208.148:21044/ Final Result:100/100 dur:36070 rps:2.77
    //=========From Ali To Ali============
    //[INFO ]00:09:29.003 http://39.104.208.148:21043/ Final Result:1000/1000 dur:10122 zdl:98.79
    //[INFO ]00:09:47.582 http://39.104.208.148:21043/ Final Result:1000/1000 dur:9664 zdl:103.48
    @Test
    public void largeReqLargeResp() throws Exception {
        testClient(serverAddr, 100, "bdware.test/large", longStr);
    }


    String serverAddr = "http://39.104.208.148:21044/";

    public void testClient(String urlStr, int totalCount, String doid, String body) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger correct = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        for (int i = 0; i < totalCount; i++)
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(urlStr);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(30000);
                        connection.setReadTimeout(30000);
                        connection.addRequestProperty("doid", doid);
                        connection.setDoOutput(true);
                        connection.connect();
                        OutputStream out = connection.getOutputStream();
                        out.write(body.getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        out.close();
                        InputStream input = connection.getInputStream();
                        Scanner sc = new Scanner(input);
                        StringBuilder sb = new StringBuilder();
                        while (sc.hasNextLine()) {
                            sb.append(sc.nextLine());
                        }
                        sc.close();
                        String content = sb.toString();
                        if (content.startsWith("cold")) {
                            correct.incrementAndGet();
                        }
                        input.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        total.incrementAndGet();
                    }
                }
            });
        int circle = 0;
        for (; total.get() < totalCount; ) {
            if (++circle % 100 == 0)
                LOGGER.info(String.format("%d/%d", correct.get(), total.get()));
            Thread.sleep(10);
        }
        int dur = (int) (System.currentTimeMillis() - start);
        LOGGER.info(String.format("%s Final Result:%d/%d dur:%d rps:%.2f ", serverAddr, correct.get(), total.get(),
                dur, (correct.get() + 0.0D) * 1000.0D / (dur)));

    }

    static class HttpClientInboundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
            //LOGGER.info("receive:" + msg.content().toString(CharsetUtil.UTF_8));
        }
    }

    static String longStr = getLongStr();

    private static String getLongStr() {
        StringBuilder sb = new StringBuilder("cold");
        for (int i = 0; i < 1024 * 1024; i++)
            sb.append("a");
        return sb.toString();
    }

    @ChannelHandler.Sharable
    static class HttpHandler extends SimpleChannelInboundHandler<HttpObject> {
        static AtomicInteger counter = new AtomicInteger(0);

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            int i = counter.incrementAndGet();
            try {
                FullHttpMessage hm = (FullHttpMessage) msg;
                String doid = ((FullHttpMessage) msg).headers().get("doid");
                DefaultFullHttpResponse response;
                if (doid != null && doid.contains("small"))
                    response =
                            new DefaultFullHttpResponse(
                                    HttpVersion.HTTP_1_1, OK, Unpooled.wrappedBuffer("cold".getBytes(StandardCharsets.UTF_8)));
                else
                    response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1, OK, Unpooled.wrappedBuffer(longStr
                            .getBytes(StandardCharsets.UTF_8)));

                ctx.writeAndFlush(response);
                ctx.close();
            } catch (Throwable t) {

            } finally {
                counter.decrementAndGet();
            }
        }
    }
}
