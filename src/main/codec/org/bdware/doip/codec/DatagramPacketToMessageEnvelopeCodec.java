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
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;

import java.net.InetSocketAddress;
import java.util.List;

public class DatagramPacketToMessageEnvelopeCodec extends MessageToMessageCodec<DatagramPacket, MessageEnvelope> {
    static Logger LOGGER = LogManager.getLogger(DatagramPacketToMessageEnvelopeCodec.class);
    InetSocketAddress address = null;

    public DatagramPacketToMessageEnvelopeCodec() {

    }

    public DatagramPacketToMessageEnvelopeCodec(String host, int port) {
        address = new InetSocketAddress(host, port);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageEnvelope msg, List<Object> out) throws Exception {
        try {
            //assert msg.contentLength <= MessageEnvelopeAggregator.MUT_802.3 - 24;
            ByteBuf buf = ctx.alloc().directBuffer();
            MessageEnvelopeCodec.envelopeToBytes(msg, buf);
            DatagramPacket packet = new DatagramPacket(buf, msg.getSender());
            out.add(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
        ByteBuf buf = msg.content();
        MessageEnvelope messageEnvelope = new MessageEnvelope(msg.sender());
        MessageEnvelopeCodec.byteBufToEnvelope(buf, messageEnvelope);
        out.add(messageEnvelope);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        LOGGER.debug("got exception: " + cause.getMessage());
        cause.printStackTrace();
        if(ctx.channel().isActive()) ctx.close();
    }
}
