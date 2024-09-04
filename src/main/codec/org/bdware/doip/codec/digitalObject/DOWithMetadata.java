/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.digitalObject;

import com.google.gson.JsonObject;

public class DOWithMetadata extends DigitalObject{

    public static final String METADATA = "metadata";

    public static DOWithMetadata fromDO(DigitalObject digitalObject){
        if(digitalObject.attributes == null || digitalObject.attributes.get(METADATA) == null){
            return null;
        }
        return (DOWithMetadata)digitalObject;
    }

    public DOWithMetadata(String id, DoType type) {
        super(id, type);
    }

    public void addMetadata(String key, String value){
        if(attributes==null || attributes.get(METADATA) == null){
            addAttribute(METADATA, new JsonObject());
        }
        attributes.get(METADATA).getAsJsonObject().addProperty(key,value);
    }

    public JsonObject getMetadata(){
        if(attributes.get(METADATA) == null) return null;
        return attributes.get(METADATA).getAsJsonObject();
    }

    public String getMetadata(String key){
        if(attributes.get(METADATA) == null) return null;
        return attributes.get(METADATA).getAsJsonObject().get(key).getAsString();
    }

    public void setMetadata(JsonObject jo){
        this.addAttribute(METADATA,jo);
    }

}
