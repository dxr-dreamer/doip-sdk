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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bdware.doip.codec.utils.DoipGson;

import java.nio.charset.StandardCharsets;

public class HeaderParameter {
    public String id;
    public String operation;
    public DoipResponseCode response;
    public JsonObject attributes;

    public HeaderParameter(String targetID, String operation) {
        this.id = targetID;
        this.operation = operation;
    }

    public HeaderParameter deepCopy() {
        HeaderParameter ret = new HeaderParameter(this.id, this.operation);
        if (attributes != null) ret.attributes = attributes.deepCopy();
        ret.response = this.response;
        return ret;
    }

    public int length() {
        return this.toByteArray().length;
    }

    public byte[] toByteArray() {
        return DoipGson.getDoipGson().toJson(this).getBytes(StandardCharsets.UTF_8);
    }

    public void setResponse(DoipResponseCode response) {
        this.response = response;
    }

    public static HeaderParameter fromJson(String str) {
        return DoipGson.getDoipGson().fromJson(str, HeaderParameter.class);
    }

    public void addAttribute(String key, String value) {
        if (this.attributes == null) {
            this.attributes = new JsonObject();
        }
        this.attributes.addProperty(key, value);
    }

    public void addAttribute(String key, long value) {
        if (this.attributes == null) {
            this.attributes = new JsonObject();
        }
        this.attributes.addProperty(key, value);
    }

    public void addAttribute(String key, int value) {
        if (this.attributes == null) {
            this.attributes = new JsonObject();
        }
        this.attributes.addProperty(key, value);
    }

    public void addAttribute(String key, boolean value) {
        if (this.attributes == null) {
            this.attributes = new JsonObject();
        }
        this.attributes.addProperty(key, value);
    }

    public void addAttribute(String key, JsonElement value) {
        if (this.attributes == null) {
            this.attributes = new JsonObject();
        }
        this.attributes.add(key, value);
    }

}
