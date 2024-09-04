/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.v2_0;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.digitalObject.Element;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;
import org.bdware.doip.codec.exception.DoDecodeException;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.codec.utils.DoipGson;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DoipMessageCodec extends ByteToMessageCodec<DoipMessage> {
    static Logger LOGGER = LogManager.getLogger(DoipMessageCodec.class);
    private final byte[] SEGMENT_TERMINATOR = Delimiters.SEGMENT_TERMINATOR;
    Random rdm = new Random();
    private final Map<Integer, String> requestIDMap = new ConcurrentHashMap<>();

    public ArrayList<byte[]> toSegments(DigitalObject digitalObject) {
        ArrayList<byte[]> segStrings = new ArrayList<>();
        if (digitalObject == null) return segStrings;
        segStrings.add(DoipGson.getDoipGson().toJson(digitalObject).getBytes(StandardCharsets.UTF_8));
        if (digitalObject.elements != null) {
            for (Element e : digitalObject.elements) {
                if (e.getData() == null) continue;
                JsonObject eid = new JsonObject();
                eid.addProperty("id", e.id);
                segStrings.add(DoipGson.getDoipGson().toJson(eid).getBytes(StandardCharsets.UTF_8));
                segStrings.add(e.getData() == null ? "".getBytes(StandardCharsets.UTF_8) :
                        ("@\n" + e.getData().length + "\n" + new String(e.getData(),StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8));
            }
        }
        //TODO sign here
        /*
        *
         if (digitalObject.isSigned()) {
            ByteBuf signPayload = Unpooled.directBuffer();
            for (byte[] seg : segStrings) {
                signPayload.writeBytes(seg);
            }
            byte[] payloadBytes = new byte[signPayload.readableBytes()];
            signPayload.readBytes(payloadBytes);
            try {
                segStrings.add(Objects.requireNonNull(CertUtils.Sign(payloadBytes, GlobalCertifications.getGlobalJWK())).getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        *
        * */
        return segStrings;
    }

    public DigitalObject segmentsToDo(ArrayList<byte[]> segStrings) throws DoDecodeException {
        if (segStrings == null || segStrings.size() < 1) {
            LOGGER.error("input segment string is empty");
            return null;
        }
        Iterator<byte[]> segIt = segStrings.iterator();
        String jsonSegment = new String(segIt.next(),StandardCharsets.UTF_8);
        LOGGER.debug("DO json segments: " + jsonSegment);
        DigitalObject digitalObject;
        try {
            digitalObject = DoipGson.getDoipGson().fromJson(jsonSegment, DigitalObject.class);
            if (digitalObject.id == null) throw new DoDecodeException("not digital object segments");
        } catch (Exception e) {
            throw new DoDecodeException("not digital object segments");
        }
        HashMap<String, byte[]> elementDataMap = new HashMap<>();
        int index = 0;
        while (segIt.hasNext() && digitalObject.elements != null && index < digitalObject.elements.size()) {
            String elementIDSeg = new String(segIt.next(),StandardCharsets.UTF_8);
            JsonObject eid = new Gson().fromJson(elementIDSeg, JsonObject.class);
            String elementID = eid.get("id").getAsString();
            if (elementID == null) {
                LOGGER.error("element id segments error: " + elementIDSeg);
                break;
            }
            if (!segIt.hasNext()) {
                LOGGER.error("element data segments not found!");
                break;
            }
            byte[] elementDataSeg = segIt.next();
            BufferedReader br = new BufferedReader(new StringReader(new String(elementDataSeg,StandardCharsets.UTF_8)));
            String elementData = "";
            while (true) {
                try {
                    String line = br.readLine();
                    if (line == null || line.length() == 0) break;
                    if (line.charAt(0) == '@') {
                        int chunkLength = Integer.parseInt(br.readLine());
                        char[] chunk = new char[chunkLength];
                        br.read(chunk, 0, chunkLength);
                        elementData += new String(chunk);
                    } else {
                        if (elementData.length() > 0) {
                            LOGGER.error("invalid chunks");
                        }
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (elementData.length() > 0) {
                elementDataMap.put(elementID, elementData.getBytes(StandardCharsets.UTF_8));
            } else {
                elementDataMap.put(elementID, elementDataSeg);
            }
        }
//TODO VerifyHere
//        if (digitalObject.isSigned() && segIt.hasNext()) {
//            String signatureSeg = new String(segIt.next());
//
//            ByteBuf signPayload = Unpooled.directBuffer();
//            for (int i = 0; i < segStrings.size() - 1; i++) {
//                signPayload.writeBytes(segStrings.get(i));
//            }
//            byte[] payloadBytes = new byte[signPayload.readableBytes()];
//            signPayload.readBytes(payloadBytes);
//
//            try {
//                if (!CertUtils.verify(
//                        payloadBytes,
//                        signatureSeg,
//                        JWK.parse(digitalObject.attributes.get("publicKey").getAsString())))
//                    logger.warn("Verify signature error!");
//            } catch (Exception e) {
//                logger.warn("Verify signature error!");
//                e.printStackTrace();
//                return digitalObject;
//            }
//        }

        if (segIt.hasNext()) LOGGER.warn("unexcepted segment");

        if (digitalObject.elements != null)
            for (Element e : digitalObject.elements) {
                e.setData(elementDataMap.get(e.id));
            }
        return digitalObject;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, DoipMessage response, ByteBuf out) throws Exception {
        DelimiterResponse resp = new DelimiterResponse();
        if (requestIDMap.get(response.requestID) == null)
            resp.requestId = response.requestID + "";
        else {
            resp.requestId = requestIDMap.get(response.requestID);
            requestIDMap.remove(response.requestID);
        }
        if (response.header.parameters.response != null)
            resp.status = response.header.parameters.response.getName();
        resp.targetId = response.header.parameters.id;
        resp.operationId = response.header.parameters.operation;
        resp.attributes = response.header.parameters.attributes;

        LOGGER.debug("response body: " + new String(response.body.getEncodedData(),StandardCharsets.UTF_8));
        try {
            DigitalObject outDO = response.body.getDataAsDigitalObject();
            String jsonSegments = DoipGson.getDoipGson().toJson(resp);
            LOGGER.debug("response json segs: " + jsonSegments);
            out.writeBytes(jsonSegments.getBytes(StandardCharsets.UTF_8));
            ArrayList<byte[]> doSegments = toSegments(outDO);
            for (int i = 0; i < doSegments.size() - 1; i++) {
                out.writeBytes(SEGMENT_TERMINATOR);
                out.writeBytes(doSegments.get(i));
            }
            if (doSegments.size() > 0) {
                out.writeBytes(SEGMENT_TERMINATOR);
                out.writeBytes(doSegments.get(doSegments.size() - 1));
            }
            out.writeBytes("\n".getBytes(StandardCharsets.UTF_8));
            out.writeBytes(Delimiters.EOF);
        } catch (DoDecodeException de) {
            LOGGER.debug("not do segment: " + new String(response.body.encodedData),StandardCharsets.UTF_8);
            String elementDataSeg = null;
            if (response.header.parameters != null && response.header.parameters.attributes != null && response.header.parameters.attributes.has("element")) {
                elementDataSeg = ("@\n" + response.body.getLength() + "\n" + new String(response.body.getEncodedData(),StandardCharsets.UTF_8));
            } else if (response.body.getLength() > 0) {
                try {
                    resp.output = JsonParser.parseString(new String(response.body.encodedData,StandardCharsets.UTF_8)).getAsJsonObject();
                } catch (Exception parseJOError) {
                    try {
                        resp.output = JsonParser.parseString(new String(response.body.encodedData,StandardCharsets.UTF_8)).getAsJsonArray();
                    } catch (Exception parseJAError) {
                        JsonObject tempJo = new JsonObject();
                        tempJo.addProperty("message", new String(response.body.encodedData,StandardCharsets.UTF_8));
                        resp.output = tempJo;
                    }
                }
            }
            String jsonSegments = new Gson().toJson(resp);
            LOGGER.debug("response: " + jsonSegments);
            out.writeBytes(jsonSegments.getBytes(StandardCharsets.UTF_8));

            if (elementDataSeg != null) {
                out.writeBytes(SEGMENT_TERMINATOR);
                out.writeBytes(elementDataSeg.getBytes(StandardCharsets.UTF_8));

            }
            out.writeBytes("\n".getBytes(StandardCharsets.UTF_8));
            out.writeBytes(Delimiters.EOF);
        } catch (EOFException eofException) {
            String jsonSegments = new Gson().toJson(resp);
            LOGGER.debug("response: " + jsonSegments);
            out.writeBytes(jsonSegments.getBytes(StandardCharsets.UTF_8));
            out.writeBytes("\n".getBytes(StandardCharsets.UTF_8));
            out.writeBytes(Delimiters.EOF);
        } catch (Exception e) {
            e.printStackTrace();
            out.writeBytes(Delimiters.EOF);
        }

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        DoipMessage msg = bufToDoipMessage(in);
        out.add(msg);
    }

    //TODO optimize the buf to doip message
    private DoipMessage bufToDoipMessage(ByteBuf in) {
        DelimiterResponse resp;
        byte[] contentB = new byte[in.readableBytes()];
        in.readBytes(contentB);
        String content = new String(contentB, StandardCharsets.UTF_8);
        String[] msgSegments = content.split(new String(SEGMENT_TERMINATOR,StandardCharsets.UTF_8));
        assert msgSegments.length > 0;
        LOGGER.debug("decode response message: " + content);

        resp = new Gson().fromJson(msgSegments[0], DelimiterResponse.class);
        DoipMessage inMsg = new DoipMessage(null, null);
        inMsg.header.IsRequest = false;
        try {
            inMsg.requestID = Integer.parseInt(resp.requestId);
        } catch (Exception e) {
            inMsg.requestID = resp.hashCode();
        }

        System.out.println("inMsg.requestID:" + inMsg.requestID + "   resp.requestId: " + resp.requestId);
        requestIDMap.put(inMsg.requestID, resp.requestId);
        //TODO support full format of doip2.1

        inMsg.header.parameters.id = resp.targetId;
        inMsg.header.parameters.operation = BasicOperations.getDoOp(resp.operationId).getName();
        inMsg.header.parameters.response = DoipResponseCode.getDoResponse(resp.status);
        inMsg.header.parameters.attributes = resp.attributes;

        if (resp.output != null) {
            String outputStr = new Gson().toJson(resp.output);
            int a = outputStr.getBytes(StandardCharsets.UTF_8).length;
            ByteBuf bodyReader = Unpooled.buffer();
            bodyReader.writeInt(a);
            bodyReader.writeBytes(outputStr.getBytes(StandardCharsets.UTF_8));
            inMsg.body.encodedData = new byte[bodyReader.readableBytes()];
            bodyReader.readBytes(inMsg.body.encodedData);
        } else {
            if (msgSegments.length > 1) {
                ArrayList<byte[]> doSegs = new ArrayList<>();
                for (int i = 1; i < msgSegments.length; i++) {
                    doSegs.add(msgSegments[i].getBytes(StandardCharsets.UTF_8));
                }
                try {
                    DigitalObject inDO = segmentsToDo(doSegs);
                    inMsg.body.setDataAsDigitalObject(inDO);
                } catch (DoDecodeException e) {
                    if (doSegs.size() == 1) {
                        inMsg.body.encodedData = doSegs.get(0);
                    } else {
                        LOGGER.warn("unexpected segments size" + doSegs.size());
                    }
                }
            } else {
                LOGGER.debug("empty output");
            }
        }
        inMsg.header.bodyLength = inMsg.body == null ? 0 : inMsg.body.getLength();
        return inMsg;
    }
}
