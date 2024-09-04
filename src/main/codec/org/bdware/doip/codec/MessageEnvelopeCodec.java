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
import io.netty.handler.codec.ByteToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;
import org.bdware.doip.codec.exception.MessageCodecException;

import java.util.List;

public class MessageEnvelopeCodec extends ByteToMessageCodec<MessageEnvelope> {
    static Logger LOGGER = LogManager.getLogger(MessageEnvelopeCodec.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageEnvelope msg, ByteBuf out) throws Exception {
        envelopeToBytes(msg, out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        MessageEnvelope envelope = new MessageEnvelope();
        byteBufToEnvelope(in, envelope);
        out.add(envelope);
    }

    public static void envelopeToBytes(MessageEnvelope envelope, ByteBuf bf) throws MessageCodecException {
        if (envelope.contentLength != envelope.content.readableBytes()) {
            throw new MessageCodecException("unequal content length");
        }
        bf.writeByte(envelope.majorVersion);
        bf.writeByte(envelope.minorVersion);
        bf.writeShort(envelope.getFlag());
        bf.writeInt(envelope.reserved);
        bf.writeInt(envelope.requestId);
        bf.writeInt(envelope.sequenceNumber);
        bf.writeInt(envelope.totalNumber);
        bf.writeInt(envelope.contentLength);
        bf.writeBytes(envelope.content);
        envelope.content.release();
        return;
    }

    public static void byteBufToEnvelope(ByteBuf bf, MessageEnvelope envelope) throws MessageCodecException {
        envelope.majorVersion = bf.readByte();
        envelope.minorVersion = bf.readByte();
        envelope.setFlag(bf.readShort());
        envelope.reserved = bf.readInt();
        envelope.requestId = bf.readInt();
        envelope.sequenceNumber = bf.readInt();
        envelope.totalNumber = bf.readInt();
        if (envelope.sequenceNumber > envelope.totalNumber)
            throw new MessageCodecException("invalid sequence number: " + envelope.sequenceNumber + ", total: " + envelope.totalNumber);
        envelope.contentLength = bf.readInt();
        if (envelope.contentLength != bf.readableBytes()) {
            throw new MessageCodecException("unequal content length: " + envelope.contentLength + ":" + bf.readableBytes());
        }
        envelope.content = bf.retainedSlice(bf.readerIndex(), bf.readableBytes());
        int readerIndex = bf.readerIndex() + bf.readableBytes();
        bf.setIndex(readerIndex, readerIndex);
        //   bf.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.debug("got exception: " + cause.getMessage());
        cause.printStackTrace();
        if (ctx.channel().isActive()) ctx.close();
    }
}
