/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;

import java.util.List;

public class MessageEnvelopePrinter extends MessageToMessageCodec<MessageEnvelope, MessageEnvelope> {
    static Logger LOGGER = LogManager.getLogger(MessageEnvelopePrinter.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageEnvelope msg, List<Object> out) throws Exception {
        LOGGER.info("[SEND] " + String.format("seq:%d total:%d req:%d contentLen:%d buf:%d", msg.sequenceNumber, msg.totalNumber, msg.requestId, msg.contentLength, msg.content.readableBytes()));
        out.add(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, MessageEnvelope msg, List<Object> out) throws Exception {
        LOGGER.info("[RECV] " + String.format("seq:%d total:%d req:%d contentLen:%d", msg.sequenceNumber, msg.totalNumber, msg.requestId, msg.contentLength));
        out.add(msg);
    }
}
