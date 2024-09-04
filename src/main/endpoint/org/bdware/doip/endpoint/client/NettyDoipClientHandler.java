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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ChannelHandler.Sharable
public class NettyDoipClientHandler extends SimpleChannelInboundHandler<DoipMessage> {
    static Logger logger = LogManager.getLogger(NettyDoipClientHandler.class);
    public Channel channel;
    ResponseWait sync = new ResponseWait();
    Random random = new Random();

    public void sendMessage(DoipMessage request, DoipMessageCallback callback) {
        sendMessage(request, callback, 30);
    }

    public void sendMessage(DoipMessage request, DoipMessageCallback callback, int timeoutSeconds) {
        if (callback == null) {
            logger.error("DoipMessageCallback is null, please check!");
            return;
        }
//        if (!channel.isWritable()) {
//            Thread.yield();
//            logger.info("network busy, yeild");
//        }

        int retryCount = 0;

        if (request.requestID == 0) {
            request.requestID = random.nextInt();
        }

        int MAX_RETRY_COUNT = 5;
        while (retryCount < MAX_RETRY_COUNT && !sync.waitResponse(request.requestID, callback, timeoutSeconds)) {
            request.requestID = random.nextInt();
            Thread.yield();
            retryCount++;
        }
        // logger.debug("writeAndFlush: " + new String(request.header.parameters.toByteArray()));
        // logger.debug("channel status: " + channel.isActive());

        if (retryCount >= MAX_RETRY_COUNT) {
            logger.error("waitObj.size() is too large! Could not get response: " + request.requestID);
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createResponse(DoipResponseCode.MoreThanOneErrors, request);
            builder.addAttributes("msg", "waitObj.size too large!");
            callback.onResult(builder.create());
        } else {
            channel.writeAndFlush(request);
        }
    }


    public DoipMessage sendMessageSync(DoipMessage request, int timeoutSeconds) {
        List<DoipMessage> ret = new ArrayList<>();
        final DoipMessageCallback cb = new DoipMessageCallback() {
            @Override
            public void onResult(DoipMessage msg) {
                ret.add(msg);
                synchronized (this) {
                    this.notify();
                }
            }
        };
        sendMessage(request, cb, timeoutSeconds);
        try {
            cb.wait(timeoutSeconds * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ret.size() > 0)
            return ret.get(0);
        return DoipMessageFactory.createTimeoutResponse(request.requestID,"[NettyDoipClientHandler.sendMessageSync]");
    }

    public void close() {
        channel.close();
    }

    public void setChannel(Channel c) {
        this.channel = c;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DoipMessage msg) {
        logger.debug("channelRead0 receive a message");
        if (msg.header.parameters.attributes != null && msg.header.parameters.attributes.get("action") != null) {
            if (msg.header.parameters.attributes.get("action").getAsString().equals("start")) {
                sync.wakeup(msg.requestID, msg);
            } else {
                logger.debug("stop stream");
                sync.wakeUpAndRemove(msg.requestID, msg);
            }
        } else {
            sync.wakeUpAndRemove(msg.requestID, msg);
        }
    }


}
