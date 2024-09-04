/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare doip sdk] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.doipMessage;

import org.bdware.doip.codec.exception.MessageCodecException;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

public class DoipMessage implements Cloneable {

    public transient int requestID = 0;

    private transient String recipientID = null;        //mainly user to encryption

    public MessageHeader header;
    public MessageBody body;
    public MessageCredential credential;

    public DoipMessage(String id, String opCode) {
        header = new MessageHeader(id, opCode);
        body = new MessageBody();
    }

    public DoipMessage(String id, String opCode, int flag) {
        header = new MessageHeader(id, opCode, flag);
        body = new MessageBody();
    }

    public boolean isRequest() {
        return header.isRequest();
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    transient InetSocketAddress sender = null;

    public InetSocketAddress getSender() {
        return sender;
    }

    public void setSender(InetSocketAddress sender) {
        this.sender = sender;
    }

    public  byte[] getDoipMessageHeaderBody() {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            header.parameterLength = header.parameters.length();
            header.bodyLength = body.getLength();
            //encode header
            oo.writeInt(header.getFlag());
            oo.writeInt(header.parameterLength);
            oo.writeInt(header.bodyLength);
            if (header.parameterLength != 0 && header.parameterLength != header.parameters.length())
                throw new MessageCodecException("invalid parameter length: " + header.parameterLength);
            oo.write(header.parameters.toByteArray());
            //encode body
            if (header.bodyLength != 0 && header.bodyLength != body.getLength())
                throw new MessageCodecException("invalid body length: " + header.parameterLength);
            oo.write(body.getEncodedData());
            oo.close();
            return bo.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public DoipMessage clone() {
        try {
            return (DoipMessage) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public DoipMessage withNewDOID(DoipMessage originalMessage, String newDOID) {
        DoipMessage clonedMsg = originalMessage.clone();
        clonedMsg.requestID = 0;
        clonedMsg.header = originalMessage.header.deepCopy();
        clonedMsg.header.parameters.id = newDOID;
        return clonedMsg;
    }
}
