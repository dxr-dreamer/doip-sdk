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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<DoipMessage> {
    static Logger logger = LogManager.getLogger(NettyServerHandler.class);
    protected DoipRequestHandler requestHandler;
    HashMap<Integer, PushFileStream> clientMap = new HashMap<>();
    private int streamPushInterval = 500;

    public NettyServerHandler(DoipRequestHandler handler, DoipListenerConfig listenerConfig) {
        this.requestHandler = listenerConfig.wrapEncHandlerIfNeeded(handler);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DoipMessage msg) {
        //logger.debug("receive a message: " + new Gson().toJson(msg.header));
        if (msg.header.parameters == null || msg.header.parameters.operation == null) {
//            logger.info("Invalid message, header: ");
            replyStringWithStatus(ctx, msg, "invalid request", DoipResponseCode.Invalid);
            return;
        }
        if (msg.header.parameters.attributes != null && msg.header.parameters.attributes.get("action") != null) {
            if (msg.header.parameters.attributes.get("action").getAsString().equals("start")) {
                logger.info("send DO stream");
                sendResponseUsingStream(ctx, msg);
            } else {
                logger.info("stop stream");
                stopStream(ctx, msg);
            }
        } else {
            DoipMessage response = requestHandler.onRequest(ctx, msg);
            if (response != null)
                sendResponse(ctx, msg, response);
            else
                defaultHandler(ctx, msg);
        }
    }

    private void sendResponseUsingStream(ChannelHandlerContext ctx, DoipMessage request) {
        PushFileStream pfs = new PushFileStream(ctx, request);
        pfs.start();
        clientMap.put(request.requestID, pfs);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        logger.debug("channel active");

    }

    public void defaultHandler(ChannelHandlerContext ctx, DoipMessage request) {
        replyStringWithStatus(ctx, request, "Unsupported Operation!", DoipResponseCode.Declined);
    }

    protected void replyStringWithStatus(ChannelHandlerContext ctx, DoipMessage request, String str, DoipResponseCode resp) {
        DoipMessage response;
        response = new DoipMessageFactory.DoipMessageBuilder()
                .createResponse(resp, request)
                .setBody(str.getBytes(StandardCharsets.UTF_8))
                .create();
        sendResponse(ctx, request, response);
    }

    private void sendResponse(ChannelHandlerContext ctx, DoipMessage request, DoipMessage response) {
        logger.debug("body length: " + response.body.getLength());
        if (request.getSender() != null) {
            response.setSender(request.getSender());
        }
//        if (!ctx.channel().isWritable()) {
//            Thread.yield();
//            logger.info("network busy, yeild");
//        }
        ctx.writeAndFlush(response);
    }

    private class PushFileStream extends Thread {
        ChannelHandlerContext ctx;
        DoipMessage request;
        boolean running = true;

        PushFileStream(ChannelHandlerContext ctx, DoipMessage msg) {
            this.ctx = ctx;
            this.request = msg;
        }

        @Override
        public void run() {
            while (running) {
                DoipMessage resp = requestHandler.onRequest(ctx, request);
                if (!ctx.channel().isActive()) {
                    logger.warn("channel inactive");
                    return;
                }
                sendResponse(ctx, request, resp);
                try {
                    sleep(streamPushInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void tryToStop() {
            running = false;
        }
    }

    private void stopStream(ChannelHandlerContext ctx, DoipMessage msg) {
        logger.info("stop stream: ");
        clientMap.get(msg.requestID).tryToStop();
        clientMap.remove(msg.requestID);
        replyStringWithStatus(ctx, msg, "stopped", DoipResponseCode.Success);
    }
}
