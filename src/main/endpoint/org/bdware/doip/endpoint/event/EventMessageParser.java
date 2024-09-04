/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.event;

import com.google.gson.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class EventMessageParser implements TopicHandler {
    private static final Logger LOGGER = LogManager.getLogger(EventMessageParser.class);
    Publisher publisher;
    Subscriber subscriber;

    public EventMessageParser(Publisher publisher, Subscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    @Override
    public DoipMessage handlePublish(DoipMessage request) {
        try {
            PublishMessageType publishType = PublishMessageType.valueOf(request.header.parameters.attributes.get("publishType").getAsString());
            String topicId = request.header.parameters.attributes.get("topicId").getAsString();
            String publisherId = request.header.parameters.attributes.get("publisherId").getAsString();
            if (publishType == null) throw new IllegalArgumentException("missing argument: publishType");
            if (request.header.parameters.attributes.has("merkelConfiguration")) {
                subscriber.receiveMerkelConfiguration(topicId, request.header.parameters.attributes.get("merkelConfiguration").getAsJsonObject(), request);
            }
            switch (publishType) {
                case Data:
                    return subscriber.receiveData(topicId, publisherId, request);
                case Hash:
                    return subscriber.receiveHash(topicId, publisherId, request);
                case DataAndHash:
                    return subscriber.receiveDataAndHash(topicId, publisherId, request);
                default:
                    throw new IllegalArgumentException("illegal argument: publishType");
            }
        } catch (Exception e) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(bo));
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createResponse(DoipResponseCode.UnKnownError, request);
            builder.setBody(bo.toByteArray());
            return builder.create();
        }
    }

    @Override
    public DoipMessage handleSubscribe(DoipMessage request) {
        try {
            if (!request.header.parameters.attributes.has("topicId") || !request.header.parameters.attributes.has("subscriberId")) {
                throw new IllegalArgumentException("missing argument: topicId or subscriberId");
            }
            SubscribeMessageType subscribeType = SubscribeMessageType.valueOf(request.header.parameters.attributes.get("subscribeType").getAsString());
            if (subscribeType == null) throw new IllegalArgumentException("missing argument: subscribeType");
            String topicId = request.header.parameters.attributes.get("topicId").getAsString();
            String subscriberId = request.header.parameters.attributes.get("subscriberId").getAsString();
            long offset = 0;
            long count = -1;
            switch (subscribeType) {
                case Subscribe:
                    boolean needReplay = request.header.parameters.attributes.has("needReplay") &&
                            request.header.parameters.attributes.get("needReplay").getAsBoolean();
                    return publisher.subscribe(topicId, subscriberId, needReplay, request);
                case Unsubscribe:
                    return publisher.unsubscribe(topicId, subscriberId, request);
                case DataInList:
                    JsonArray indexList = request.header.parameters.attributes.get("dataList").getAsJsonArray();
                    return publisher.sendDataInList(topicId, subscriberId, indexList, request);
                case DataInRange:
                    if (request.header.parameters.attributes.has("offset"))
                        offset = request.header.parameters.attributes.get("offset").getAsLong();
                    if (request.header.parameters.attributes.has("count"))
                        count = request.header.parameters.attributes.get("count").getAsLong();
                    return publisher.sendDataInRange(topicId, subscriberId, offset, count, request);
                case VerifyMerkel:
                    return publisher.verifyMerkelInRange(topicId, subscriberId, request);
                case RequestMerkel:
                    return publisher.sendMerkelInRange(topicId, subscriberId, request);
                default:
                    throw new IllegalArgumentException("illegal argument: subscribeType");
            }
        } catch (Exception e) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(bo));
            DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
            builder.createResponse(DoipResponseCode.UnKnownError, request);
            builder.setBody(bo.toByteArray());
            return builder.create();
        }
    }
}