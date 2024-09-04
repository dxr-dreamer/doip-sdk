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

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.HeaderParameter;
import org.bdware.doip.codec.doipMessage.MessageCredential;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;
import org.bdware.doip.codec.exception.MessageCodecException;
import org.bdware.doip.codec.utils.DoipGson;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class NaiveEnvelopeToDoMessage extends MessageToMessageCodec<MessageEnvelope, DoipMessage> {
    //depend on tcp
    //envelope assembly and disassembly is not supported
    //suitable to transmit DOMessage < 5*1024*1024
    static Logger LOGGER = LogManager.getLogger(NaiveEnvelopeToDoMessage.class);

    public NaiveEnvelopeToDoMessage() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DoipMessage msg, List<Object> out) throws Exception {
        messageToEnvelopes(msg, out);
    }

    public void messageToByteBuf(DoipMessage msg, ByteBuf out) throws MessageCodecException {
        msg.header.parameterLength = msg.header.parameters.length();
        msg.header.bodyLength = msg.body.getLength();
        //encode header
        out.writeInt(msg.header.getFlag());
        out.writeInt(msg.header.parameterLength);
        out.writeInt(msg.header.bodyLength);
        if (msg.header.parameterLength != 0 && msg.header.parameterLength != msg.header.parameters.length())
            throw new MessageCodecException("invalid parameter length: " + msg.header.parameterLength);
        out.writeBytes(msg.header.parameters.toByteArray());
        //encode body
        if (msg.header.bodyLength != 0 && msg.header.bodyLength != msg.body.getLength())
            throw new MessageCodecException("invalid body length: " + msg.header.parameterLength);
        out.writeBytes(msg.body.getEncodedData());
        //encode credential
        if (msg.credential != null) {
            out.writeInt(msg.credential.attributeLength());
            out.writeBytes(DoipGson.getDoipGson().toJson(msg.credential.attributes).getBytes(StandardCharsets.UTF_8));
            out.writeInt(msg.credential.sigLength());
            out.writeBytes(msg.credential.getSignature());
        }
        return;
    }

    public void messageToEnvelopes(DoipMessage msg, List<Object> out) throws MessageCodecException {
        ByteBuf bf = Unpooled.directBuffer();
        try {
            messageToByteBuf(msg, bf);
            MessageEnvelope env = new MessageEnvelope();
            env.content = bf.retainedSlice(0, bf.writerIndex());
            env.setTruncated(false);
            env.setEncrypted(msg.header.isEncrypted());
            env.sequenceNumber = 0;
            env.totalNumber = 1;
            env.requestId = msg.requestID;
            env.contentLength = bf.writerIndex();
            env.cachedID = msg.header.parameters.id;
            out.add(env);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bf.release();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, MessageEnvelope msg, List<Object> out) throws Exception {
        assert msg.totalNumber == 1;
        out.add(byteBufToMessage(msg.content, msg.requestId));
        msg.content.release();
    }

    public DoipMessage byteBufToMessage(ByteBuf bf, int requestId) throws MessageCodecException {
        //decode header
        DoipMessage msg = new DoipMessage("", "");
        msg.requestID = requestId;
        msg.header.setFlag(bf.readInt());
        msg.header.parameterLength = bf.readInt();
        msg.header.bodyLength = bf.readInt();
        byte[] parameters = new byte[msg.header.parameterLength];
        bf.readBytes(parameters);
        msg.header.parameters = DoipGson.getDoipGson().fromJson(new String(parameters, StandardCharsets.UTF_8), HeaderParameter.class);
        //decode body
        if (msg.header.bodyLength > 0) {
            if (bf.readableBytes() < msg.header.bodyLength) throw new MessageCodecException("invalid body length");
            msg.body.encodedData = new byte[msg.header.bodyLength];
            bf.readBytes(msg.body.encodedData);
        }
        //decodeCredential
        if (bf.readableBytes() > 0) {
            byte[] attrBytes = readDataArray(bf);
            JsonObject attrJo = DoipGson.getDoipGson().fromJson(new String(attrBytes, StandardCharsets.UTF_8), JsonObject.class);
            byte[] signature = readDataArray(bf);
            msg.credential = new MessageCredential(attrJo);
            msg.credential.setSignature(signature);
        }
        return msg;
    }

    private static byte[] readDataArray(ByteBuf din) throws MessageCodecException {
        int dataLen = din.readInt();
        if (dataLen < 0 || dataLen > din.readableBytes()) throw new MessageCodecException("invalid credential length");
        byte[] data = new byte[dataLen];
        din.readBytes(data);
        return data;
    }
}
