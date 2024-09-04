/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.server;

import com.google.gson.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.codec.utils.DoipGson;

import java.nio.charset.StandardCharsets;

public abstract class RepositoryHandlerBase implements RepositoryHandler {

    Logger logger = LogManager.getLogger(RepositoryHandlerBase.class);
    protected DoipServiceInfo serviceInfo;

    public RepositoryHandlerBase(DoipServiceInfo info) {
        serviceInfo = info;
    }

    @Override
    abstract public DoipMessage handleHello(DoipMessage request);

    @Override
    abstract public DoipMessage handleListOps(DoipMessage request);

    @Override
    abstract public DoipMessage handleCreate(DoipMessage request);

    @Override
    abstract public DoipMessage handleUpdate(DoipMessage request);

    @Override
    abstract public DoipMessage handleDelete(DoipMessage request);

    @Override
    abstract public DoipMessage handleRetrieve(DoipMessage request);

    @Op(op = BasicOperations.Unknown)
    public DoipMessage handleUnknown(DoipMessage request) {
        return replyStringWithStatus(request, "Unsupported Operation!", DoipResponseCode.Declined);
    }

    protected DoipMessage replyAllOperations(DoipMessage request) {
        JsonArray allBasicOps = new JsonArray();
        for (BasicOperations op : BasicOperations.values()) {
            allBasicOps.add(op.getName());
        }
        return replyString(request, DoipGson.getDoipGson().toJson(allBasicOps));
    }

    protected DoipMessage replyDoipServiceInfo(DoipMessage request) {
        DigitalObject respDO = serviceInfo.toDigitalObject();
        respDO.needSign();
        return replyDO(request, respDO);
    }

    protected DoipMessage replyDO(DoipMessage request, DigitalObject digitalObject) {
        DoipMessage response;
        response = new DoipMessageFactory.DoipMessageBuilder()
                .createResponse(DoipResponseCode.Success, request)
                .setBody(digitalObject)
                .create();
        logger.debug("[response message]" + DoipGson.getDoipGson().toJson(digitalObject.attributes));
        return response;
    }

    protected DoipMessage replyNull(DoipMessage inMsg) {
        DoipMessage response;
        response = new DoipMessageFactory.DoipMessageBuilder()
                .createResponse(DoipResponseCode.Success, inMsg)
                .create();
        return response;
    }

    protected DoipMessage replyString(DoipMessage inMsg, String retStr) {
        return replyStringWithStatus(inMsg, retStr, DoipResponseCode.Success);
    }

    protected DoipMessage replyBytes(DoipMessage inMsg, byte[] b) {
        DoipMessage response;
        response = new DoipMessageFactory.DoipMessageBuilder()
                .createResponse(DoipResponseCode.Success, inMsg)
                .setBody(b)
                .create();
        return response;
    }

    protected DoipMessage replyStringWithStatus(DoipMessage request, String str, DoipResponseCode resp) {
        DoipMessage response;
        response = new DoipMessageFactory.DoipMessageBuilder()
                .createResponse(resp, request)
                .setBody(str.getBytes(StandardCharsets.UTF_8))
                .create();
        return response;
    }
}
