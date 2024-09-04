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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bdware.doip.codec.exception.DoDecodeException;
import org.bdware.doip.codec.utils.DoipGson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DigitalObject {
    public String id;
    public DoType type;
    public JsonObject attributes;
    public List<Element> elements;
    static final String UUID_KEY = "UUID";

    public DigitalObject(String id, DoType type) {
        this.id = id;
        this.type = type;
    }

    public void setAttributes(JsonObject data) {
        attributes = data;
    }

    /**
     * A convenience method that sets an attribute on the object.
     *
     * @param name the name of the attribute
     * @param att  the value to set as a String
     */
    public void addAttribute(String name, String att) {
        if (attributes == null) {
            attributes = new JsonObject();
        }
        attributes.addProperty(name, att);
    }

    public UUID getUUID() {
        if (attributes == null) return null;
        return attributes.has(UUID_KEY) ? UUID.fromString(attributes.get(UUID_KEY).getAsString()) : null;
    }

    public void setUUID(UUID uuid) {
        addAttribute(UUID_KEY, uuid.toString());
    }

    public void addAttribute(String name, Number att) {
        if (attributes == null) {
            attributes = new JsonObject();
        }
        attributes.addProperty(name, att);
    }

    public void addAttribute(String name, JsonElement att) {
        if (attributes == null) {
            attributes = new JsonObject();
        }
        attributes.add(name, att);
    }

    public void addElements(Element e) {
        if (this.elements == null) {
            this.elements = new ArrayList<>();
        }
        this.elements.add(e);
    }

    public boolean isSigned() {
        if (attributes != null && attributes.has("isSigned")) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public void needSign() {
        addAttribute("isSigned", "true");
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bo);
        Gson gson = DoipGson.getDoipGson();
        String jsonSegments = gson.toJson(this);
        try {
            // add length information
            out.writeInt(jsonSegments.getBytes(StandardCharsets.UTF_8).length);
            out.write(jsonSegments.getBytes(StandardCharsets.UTF_8));
            if (this.elements != null) {
//                digitalObject.elements = sortElement(digitalObject.elements);
                for (Element e : this.elements) {
                    if (e.getData() != null) {
                        assert e.length == e.getData().length;
                        out.write(e.getData());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bo.toByteArray();
    }

    public static DigitalObject fromByteArray(byte[] doBytes) throws DoDecodeException, IOException {
        if (doBytes == null|| doBytes.length==0) return null;
        DigitalObject d;
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(doBytes));

        // Json segment length
        int metaLen = input.readInt();
        if (metaLen > doBytes.length) {
            throw new DoDecodeException("invalid DO byte array");
        }
        byte[] content = new byte[metaLen];
        int eof = input.read(content);
//        logger.debug("digital Object: " + new String(content));
        Gson gson = DoipGson.getDoipGson();
        d = gson.fromJson(new String(content,StandardCharsets.UTF_8), DigitalObject.class);

        if (d.elements == null || eof == -1 || input.available() == 0) return d;

//        d.elements.sort(Comparator.comparing(o -> o.id));
        for (int i = 0; i < d.elements.size(); i++) {
            if (input.available() < d.elements.get(i).length) {
                throw new DoDecodeException("Unexpected element data length");
            }
            byte[] data = new byte[d.elements.get(i).length];
            input.read(data);
            d.elements.get(i).setData(data);
        }
        return d;
    }
}
