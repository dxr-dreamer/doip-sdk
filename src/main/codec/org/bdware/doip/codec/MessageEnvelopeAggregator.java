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
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.HeaderParameter;
import org.bdware.doip.codec.doipMessage.MessageCredential;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;
import org.bdware.doip.codec.exception.MessageCodecException;
import org.bdware.doip.codec.utils.DoipGson;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageEnvelopeAggregator extends MessageToMessageCodec<MessageEnvelope, DoipMessage> {
    public static final int MTU_ethernet = 1500;
    public static final int MTU_802_3 = 1492;
    final int mtu;

    public MessageEnvelopeAggregator() {
        this(MTU_802_3);
    }

    public MessageEnvelopeAggregator(int mtu) {
        this.mtu = mtu;
    }

    private int getMTU() {
        return this.mtu;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DoipMessage msg, List<Object> out) throws Exception {
        messageToEnvelopes(msg, out);
    }

    public void messageToEnvelopes(DoipMessage msg, List<Object> out) throws MessageCodecException {
        ByteBuf bf = Unpooled.directBuffer();
        try {
            msg.header.parameterLength = msg.header.parameters.length();
            msg.header.bodyLength = msg.body.getLength();
            //encode header
            bf.writeInt(msg.header.getFlag());
            bf.writeInt(msg.header.parameterLength);
            bf.writeInt(msg.header.bodyLength);
            if (msg.header.parameterLength != 0 && msg.header.parameterLength != msg.header.parameters.length())
                throw new MessageCodecException("invalid parameter length: " + msg.header.parameterLength);
            bf.writeBytes(msg.header.parameters.toByteArray());
            //encode body
            if (msg.header.bodyLength != 0 && msg.header.bodyLength != msg.body.getLength())
                throw new MessageCodecException("invalid body length: " + msg.header.parameterLength);
            bf.writeBytes(msg.body.getEncodedData());
            //encode credential
            if (msg.credential != null) {
                bf.writeInt(msg.credential.attributeLength());
                bf.writeBytes(DoipGson.getDoipGson().toJson(msg.credential.attributes).getBytes(StandardCharsets.UTF_8));
                bf.writeInt(msg.credential.sigLength());
                bf.writeBytes(msg.credential.getSignature());
            }
            int encodedLength = bf.readableBytes();
            int totalNumber = (encodedLength - 1) / mtu + 1;
            InetSocketAddress sender = msg.getSender();
            while (bf.isReadable()) {
                MessageEnvelope env = new MessageEnvelope(sender);
                env.cachedID = msg.header.parameters.id;
                if (bf.readableBytes() <= mtu) {
                    int readindex = bf.readerIndex();
                    int toread = bf.readableBytes();
                    env.content = bf.retainedSlice(readindex, toread);
                    bf.readerIndex(readindex + toread);
                    env.setTruncated(totalNumber > 1);
                    env.setEncrypted(msg.header.isEncrypted());
                    env.sequenceNumber = out.size();
                    env.totalNumber = totalNumber;
                    env.requestId = msg.requestID;
                    env.contentLength = env.content.readableBytes();
                    out.add(env);
                    break;
                } else {
                    int readindex = bf.readerIndex();
                    env.content = bf.retainedSlice(readindex, mtu);
                    bf.readerIndex(readindex + getMTU());
                    env.setTruncated(totalNumber > 1);
                    env.setEncrypted(msg.header.isEncrypted());
                    env.sequenceNumber = out.size();
                    env.totalNumber = totalNumber;
                    env.requestId = msg.requestID;
                    env.contentLength = env.content.readableBytes();
                    out.add(env);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bf.release();
        }
    }

    Map<Integer, MessageEnvelopeBuffer> messageEnvelopeBufferMap = new ConcurrentHashMap<>();

    @Override
    protected void decode(ChannelHandlerContext ctx, MessageEnvelope msg, List<Object> out) throws Exception {
        if (!msg.isResend()) {
            if (msg.isTruncated()) {
                MessageEnvelopeBuffer buff;
                if (messageEnvelopeBufferMap.containsKey(msg.requestId)) {
                    buff = messageEnvelopeBufferMap.get(msg.requestId);
                } else {
                    buff = new MessageEnvelopeBuffer(msg.requestId);
                    messageEnvelopeBufferMap.put(msg.requestId, buff);
                }
                buff.add(msg);
                if (buff.isComplete()) {
                    DoipMessage doipMessage = byteBufToMessage(buff.toByteBuf(), msg.requestId);
                    doipMessage.setSender(buff.getSender());
                    doipMessage.header.setIsEncrypted(msg.isEncrypted());
                    out.add(doipMessage);
                    messageEnvelopeBufferMap.remove(msg.requestId);
                }
            } else {
                DoipMessage doipMessage = byteBufToMessage(msg.content, msg.requestId);
                doipMessage.setSender(msg.getSender());
                doipMessage.header.setIsEncrypted(msg.isEncrypted());
                out.add(doipMessage);
            }
        }
    }

    public static DoipMessage byteBufToMessage(ByteBuf bf, int requestId) throws MessageCodecException {
        //decode header
        DoipMessage msg = new DoipMessage("", "");
        msg.requestID = requestId;
        msg.header.setFlag(bf.readInt());
        msg.header.parameterLength = bf.readInt();
        msg.header.bodyLength = bf.readInt();
        byte[] parameters = new byte[msg.header.parameterLength];
        bf.readBytes(parameters);
        msg.header.parameters = DoipGson.getDoipGson().fromJson(new String(parameters,StandardCharsets.UTF_8), HeaderParameter.class);
        //decode body
        if (msg.header.bodyLength > 0) {
            if (bf.readableBytes() < msg.header.bodyLength) throw new MessageCodecException("invalid body length");
            msg.body.encodedData = new byte[msg.header.bodyLength];
            bf.readBytes(msg.body.encodedData);
        }
        //decodeCredential
        if (bf.readableBytes() > 0) {
            byte[] attrBytes = readDataArray(bf);
            JsonObject attrJo = DoipGson.getDoipGson().fromJson(new String(attrBytes,StandardCharsets.UTF_8), JsonObject.class);
            byte[] signature = readDataArray(bf);
            msg.credential = new MessageCredential(attrJo);
            msg.credential.setSignature(signature);
        }
        bf.release();
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
