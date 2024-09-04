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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;

public class Element {
    /**
     * The id of the element.
     */
    public String id;

    /**
     * The size of the data portion.  May be -1 when the size is unknown.
     */
    public int length;

    /**
     * The type of the element.
     */
    public String type;

    /**
     * The attributes of the element.
     * {"api":true,url:""}这种类型，标识Element的值来自于一个restful接口
     */
    public JsonObject attributes;

    /**
     * Element data in string format. Suggest only used for debug.
     */
    public String dataString;
    /**
     * The bytes of the element, as an {@code InputStream}.
     */
    private transient byte[] data;

    public Element(String id, String type){
        this.id = id;
        this.type = type;
        this.length = -1;
    }

    public void setData(byte[] data){
        if(data == null){
            this.data = null;
            this.length = 0;
            return;
        }
        this.data = data;
        this.length = data.length;
    }

    public void excludeData(){
        this.data = null;
    }

    public byte[] getData(){
        return data;
    }

    /**
     * A convenience method that sets an attribute on the object.
     *
     * @param name the name of the attribute
     * @param att the value to set as a String
     */
    public synchronized void setAttribute(String name, String att) {
        if (attributes == null) {
            attributes = new JsonObject();
        }
        attributes.addProperty(name, att);
    }

    public String toString(){
        dataString = new String(data, StandardCharsets.UTF_8);
        return new Gson().toJson(this);
    }
}
