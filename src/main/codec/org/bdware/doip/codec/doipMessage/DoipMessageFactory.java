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

import com.google.gson.JsonElement;
import org.bdware.doip.codec.digitalObject.DigitalObject;

public class DoipMessageFactory {


    public static class DoipMessageBuilder {
        DoipMessage ret;

        public DoipMessageBuilder() {
        }

        public DoipMessageBuilder createRequest(String targetID, String operation) {
            ret = new DoipMessage(targetID, operation);
            ret.header.setIsRequest(true);
            return this;
        }

        public DoipMessageBuilder createResponse(DoipResponseCode responseCode, DoipMessage reqMsg) {
            if (reqMsg == null) {
                ret = new DoipMessage(null, null, 0);
                ret.header.parameters.response = responseCode;
                return this;
            }

            ret = new DoipMessage(reqMsg.header.parameters.id, reqMsg.header.parameters.operation);
            ret.header.setIsRequest(false);
            ret.header.parameters = reqMsg.header.parameters.deepCopy();
            ret.header.parameters.response = responseCode;
            ret.requestID = reqMsg.requestID;
            ret.header.setIsCertified(reqMsg.header.isCertified());
            ret.header.setIsEncrypted(reqMsg.header.isEncrypted());

            if (reqMsg.credential != null && reqMsg.credential.getSigner() != null) {
                ret.setRecipientID(reqMsg.credential.getSigner());
            }

            return this;
        }

        public DoipMessageBuilder addAttributes(String key, JsonElement value) {
            ret.header.parameters.addAttribute(key, value);
            return this;
        }

        public DoipMessageBuilder addAttributes(String key, long value) {
            ret.header.parameters.addAttribute(key, value);
            return this;
        }

        public DoipMessageBuilder addAttributes(String key, String value) {
            ret.header.parameters.addAttribute(key, value);
            return this;
        }

        public DoipMessageBuilder addAttributes(String key, int value) {
            ret.header.parameters.addAttribute(key, value);
            return this;
        }

        public DoipMessageBuilder addAttributes(String key, boolean value) {
            ret.header.parameters.addAttribute(key, value);
            return this;
        }

        public DoipMessageBuilder setBody(byte[] body) {
            ret.body.encodedData = body;
            ret.header.bodyLength = ret.body.getLength();
            return this;
        }

        public DoipMessageBuilder setBody(DigitalObject digitalObject) {
            ret.body.setDataAsDigitalObject(digitalObject);
            return this;
        }

        public DoipMessage create() {
            return ret;
        }

        public DoipMessageBuilder setRequestID(int requestID) {
            ret.requestID = requestID;
            return this;
        }

        public DoipMessageBuilder setFlag(int flag) {
            ret.header.setFlag(flag);
            return this;
        }

        public DoipMessageBuilder setIsCertified(boolean isCertified) {
            ret.header.setIsCertified(isCertified);
            return this;
        }

        public DoipMessageBuilder setIsPragmatics(boolean isPragmatics) {
            ret.header.setIsPragmatics(isPragmatics);
            return this;
        }
        public DoipMessageBuilder setIsCommand(boolean isCommand) {
            ret.header.setIsCommand(isCommand);
            return this;
        }
        public DoipMessageBuilder setIsEncrypted(boolean isEncrypted) {
            ret.header.setIsEncrypted(isEncrypted);
            return this;
        }

        public DoipMessageBuilder setDoipMessage(DoipMessage message) {
            ret = message;
            return this;
        }
    }

    public static DoipMessage createTimeoutResponse(int requestID, String msg) {
        DoipMessage ret = new DoipMessage(null, null);
        ret.header.IsRequest = true;
        ret.header.parameters.response = DoipResponseCode.UnKnownError;
        ret.header.parameters.addAttribute("timeout", "timeout");
        ret.header.parameters.addAttribute("msg", msg);
        ret.requestID = requestID;
        return ret;
    }

    public static boolean isLocalTimeoutMessage(DoipMessage msg) {
        if (msg == null || msg.header == null || msg.header.parameters == null || msg.header.parameters.response == null)
            return false;
        if (msg.header.parameters.attributes == null) return false;
        if (!msg.header.parameters.attributes.has("timeout")) return false;
        if (msg.header.parameters.response == DoipResponseCode.UnKnownError && msg.header.parameters.attributes.get("timeout").getAsString().equals("timeout")) {
            return true;
        }
        return false;
    }

    public static DoipMessage createConnectFailedResponse(int requestID) {
        DoipMessage ret = new DoipMessage(null, null);
        ret.header.IsRequest = true;
        ret.header.parameters.response = DoipResponseCode.UnKnownError;
        ret.header.parameters.addAttribute("failed", "connect target doip service failed");
        ret.requestID = requestID;
        return ret;
    }

}
