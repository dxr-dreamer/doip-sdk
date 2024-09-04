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


public class MessageHeader {

    public boolean IsRequest;

    private int flag;
    public int parameterLength;
    public int bodyLength;

    public HeaderParameter parameters;

    private transient boolean isRequest = false;
    private transient boolean isCertified = false;
    // !IMPORTANT!
    // isEncrypted is defined in message envelop!
    private transient boolean isEncrypted = false;
    private transient boolean isPragmatics = false;
    private transient boolean isCommand = false;

    public MessageHeader(String id, String opCode) {
        flag = 0;
        parameters = new HeaderParameter(id, opCode);
        parameterLength = parameters.length();
    }

    public MessageHeader(String id, String opCode, int f) {
        this.setFlag(f);
        parameters = new HeaderParameter(id, opCode);
        parameterLength = parameters.length();
    }

    public MessageHeader() {
    }

    public MessageHeader deepCopy() {
        MessageHeader deepCopyMsgHeader = new MessageHeader();
        deepCopyMsgHeader.isRequest = this.isRequest;
        deepCopyMsgHeader.flag = this.flag;
        deepCopyMsgHeader.parameterLength = this.parameterLength;
        deepCopyMsgHeader.bodyLength = this.bodyLength;
        deepCopyMsgHeader.parameters = this.parameters.deepCopy();
        deepCopyMsgHeader.isRequest = this.isRequest;
        deepCopyMsgHeader.isCertified = this.isCertified;
        deepCopyMsgHeader.isEncrypted = this.isEncrypted;
        deepCopyMsgHeader.isCommand = this.isCommand;
        return deepCopyMsgHeader;
    }

    private boolean isRequest0() {
        return (this.flag & 0x80000000) != 0;
    }

    private boolean isCertified0() {
        return (this.flag & 0x40000000) != 0;
    }

    private boolean isPragmatics0() {
        return (this.flag & 0x20000000) != 0;
    }

    private boolean isSwitch0() {
        return (this.flag & 0x10000000) != 0;
    }

    public boolean isRequest() {
        return isRequest;
    }

    public boolean isCertified() {
        return isCertified;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public boolean isPragmatics() {
        return isPragmatics;
    }

    public boolean isCommand() { return isCommand; }

    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
        updateFlag();
    }

    public void setIsPragmatics(boolean isPragmatics) {
        this.isPragmatics = isPragmatics;
        updateFlag();
    }

    public void setIsEncrypted(boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
        updateFlag();
    }

    public void setIsCertified(boolean isCertified) {
        this.isCertified = isCertified;
        updateFlag();
    }

    public void setIsCommand(boolean isCommand) {
        this.isCommand = isCommand;
        updateFlag();
    }

    public void setFlag(int f) {
        this.flag = f;
        if (isRequest0()) isRequest = true;
        if (isCertified0()) isCertified = true;
        if (isPragmatics0()) isPragmatics = true;
        if (isSwitch0()) isCommand = true;
    }

    public int getFlag() {
        return this.flag;
    }

    private void updateFlag() {
        this.flag = 0;
        if (isRequest) this.flag |= 0x80000000;
        if (isCertified) this.flag |= 0x40000000;
        if (isPragmatics) this.flag |= 0x20000000;
        if (isCommand) this.flag |= 0x10000000;
    }
}
