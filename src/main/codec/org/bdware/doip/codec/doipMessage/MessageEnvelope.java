/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */
package org.bdware.doip.codec.doipMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;

public class MessageEnvelope {

    public byte majorVersion;
    public byte minorVersion;
    public String cachedID;
    private short flag;
    public int reserved;
    public int requestId;
    public int sequenceNumber;  //&& -1 && resend = true means all packet has been received by client
    public int totalNumber;
    public int contentLength;

    //generate from flag
    private transient boolean isResend = false;
    private transient boolean isTruncated = false;
    private transient boolean isEncrypted = false;
    private transient InetSocketAddress sender;
    /**
     * message envelope length，defined in DOIP protocol
     */
    public static final int ENVELOPE_LENGTH = 24;

    public transient ByteBuf content;

    public MessageEnvelope() {
        isResend = isTruncated = isEncrypted = false;
        flag = 0;
        sequenceNumber = 0;
        totalNumber = 1;
        reserved = 0;
        majorVersion = 2;
        minorVersion = 1;
    }

    public MessageEnvelope(InetSocketAddress sender) {
        this();
        this.sender = sender;
    }

    public MessageEnvelope(short flag, int requestId, int sequenceNumber, int totalNumber) {
        this.flag = flag;
        if (isResend0()) isResend = true;
        if (isTruncated0()) isTruncated = true;
        this.requestId = requestId;
        this.sequenceNumber = sequenceNumber;
        this.totalNumber = totalNumber;
        this.reserved = 0;
        majorVersion = 2;
        minorVersion = 1;
    }


    public static MessageEnvelope createResendMessage(int reqId, int order) {
        MessageEnvelope msg = new MessageEnvelope((short) -0x8000, reqId, order, 0);
        msg.requestId = reqId;
        msg.sequenceNumber = order;
        msg.contentLength = 0;
        msg.content = Unpooled.wrappedBuffer(new byte[0]);

        return msg;
    }

    public void setContent(byte[] content) {
        this.content = Unpooled.wrappedBuffer(content);
    }

    public boolean isResend() {
        return isResend;
    }

    public boolean isTruncated() {
        return isTruncated;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    private boolean isResend0() {
        return this.flag < 0;
    }

    private boolean isTruncated0() {
        return (short) (this.flag << 1) < 0;
    }

    private boolean isEncrypted0() {
        return (short) (this.flag << 2) < 0;
    }

    public void setFlag(Short flag) {
        this.flag = flag;
        isResend = isResend0();
        isTruncated = isTruncated0();
        isEncrypted = isEncrypted0();
    }

    public short getFlag() {
        updateFlag();
        return flag;
    }

    public void setResend(boolean isResend) {
        this.isResend = isResend;
    }

    public void setTruncated(boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    public void setEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }


    private void updateFlag() {
        this.flag = 0;
        if (isResend) this.flag = (short) (this.flag | -0x8000);
        if (isTruncated) this.flag = (short) (this.flag | 0x4000);
        if (isEncrypted) this.flag = (short) (this.flag | 0x2000);
    }

    public InetSocketAddress getSender() {
        return sender;
    }

    public void setSender(InetSocketAddress address) {
        sender = address;
    }
}
