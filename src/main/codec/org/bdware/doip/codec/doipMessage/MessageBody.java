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

import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.exception.DoDecodeException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MessageBody {

    //Body可能是Element，也可能是DigitalObject
    public byte[] encodedData;

    public MessageBody() {
        encodedData = new byte[0];
    }

    public int getLength() {
        return encodedData.length;
    }

    public void setDataAsDigitalObject(DigitalObject digitalObject) {
        this.encodedData = digitalObject.toByteArray();
    }

    public DigitalObject getDataAsDigitalObject() throws DoDecodeException, IOException {
        return DigitalObject.fromByteArray(this.encodedData);
    }

    public String getDataAsJsonString() {
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(encodedData));
        // Json segment length
        int metaLen = 0;
        try {
            metaLen = input.readInt();
            if (metaLen != input.available()) {
                input.reset();
                metaLen = input.available();
            }
            byte[] content = new byte[metaLen];
            input.read(content);
            return new String(content, StandardCharsets.UTF_8);
        } catch (Exception e) {
//            e.printStackTrace();
            return new String(encodedData, StandardCharsets.UTF_8);
        }
    }

    public byte[] getEncodedData() {
        return encodedData;
    }

    public byte[] getDataAsByteArray() {
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(encodedData));
        // Json segment length
        int metaLen = 0;
        try {
            metaLen = input.readInt();
            if (metaLen != input.available()) {
                input.reset();
                metaLen = input.available();
            }
            byte[] content = new byte[metaLen];
            input.read(content);
            return content;
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }
}
