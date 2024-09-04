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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.List;

public class WebSocketFrameToByteBufCodec extends MessageToMessageCodec<BinaryWebSocketFrame, ByteBuf> {
    static Logger LOGGER = LogManager.getLogger(WebSocketFrameToByteBufCodec.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        BinaryWebSocketFrame frame = new BinaryWebSocketFrame(msg.retainedDuplicate());
        out.add(frame);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) {
        out.add(msg.content().retainedDuplicate());
    }
}
