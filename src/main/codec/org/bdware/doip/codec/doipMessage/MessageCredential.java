/*
 * Copyright (c) [2019] [Peking University]
 * [BDWare-DOIP] is licensed under the Mulan PSL v1.
 * You can use this software according to the terms and conditions of the Mulan PSL v1.
 * You may obtain a copy of Mulan PSL v1 at:
 *
 *   http://license.coscl.org.cn/MulanPSL
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 * PURPOSE.
 * See the Mulan PSL v1 for more details.
 */
package org.bdware.doip.codec.doipMessage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bdware.doip.codec.utils.DoipGson;

import java.nio.charset.StandardCharsets;

public class MessageCredential {

    public JsonObject attributes;
    int sigLength = 0;
    byte[] signature;

    public MessageCredential(String signer, byte[] sig) {
        this.attributes = new JsonObject();
        setSignature(sig);
        setSigner(signer);
    }

    public MessageCredential(String signer) {
        this.attributes = new JsonObject();
        setSigner(signer);
    }

    public MessageCredential(JsonObject attributes) {
        this.attributes = attributes;
    }

    public int attributeLength() {
        return DoipGson.getDoipGson().toJson(attributes).getBytes(StandardCharsets.UTF_8).length;
    }

    public int sigLength() {
        return sigLength;
    }

    public int getTotalLength() {
        return attributeLength() + sigLength + 4 + 4;
    }

    public String getSigner() {
        if (attributes.has("signer")) return attributes.get("signer").getAsString();
        return null;
    }

    public void setSigner(String signer) {
        attributes.addProperty("signer", signer);
    }

    public void setAttributes(String key, String value) {
        this.attributes.addProperty(key, value);
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] sig) {
        sigLength = sig.length;
        signature = sig;
    }


    public JsonElement getAttriburte(String key) {
        return attributes.get(key);
    }
}
