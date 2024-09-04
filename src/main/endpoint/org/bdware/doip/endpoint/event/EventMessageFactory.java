package org.bdware.doip.endpoint.event;

import com.google.gson.JsonArray;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;

public class EventMessageFactory {
    public static DoipMessage subscribeTopic(String targetDoId, String topicId, String subscriberId, boolean needReplay) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest(targetDoId, BasicOperations.Subscribe.getName());
        builder.addAttributes("topicId", topicId);
        builder.addAttributes("subscriberId", subscriberId);
        builder.addAttributes("needReplay", needReplay);
        builder.addAttributes("subscribeType", SubscribeMessageType.Subscribe.name());
        return builder.create();
    }

    public static DoipMessage unsubscribeTopic(String targetDoId, String topicId, String subscriberId) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest(targetDoId, BasicOperations.Subscribe.getName());
        builder.addAttributes("topicId", topicId);
        builder.addAttributes("subscriberId", subscriberId);
        builder.addAttributes("subscribeType", SubscribeMessageType.Unsubscribe.name());
        return builder.create();
    }

    public static DoipMessage requestDataInList(String targetDoId, String topicId, String subscriberId, JsonArray list) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest(targetDoId, BasicOperations.Subscribe.getName());
        builder.addAttributes("topicId", topicId);
        builder.addAttributes("subscriberId", subscriberId);
        builder.addAttributes("subscribeType", SubscribeMessageType.DataInRange.name());
        builder.addAttributes("dataList", list);
        return builder.create();
    }

    public static DoipMessage requestDataInRange(String targetDoId, String topicId, String subscriberId, long offset, long count) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest(targetDoId, BasicOperations.Subscribe.getName());
        builder.addAttributes("topicId", topicId);
        builder.addAttributes("subscriberId", subscriberId);
        builder.addAttributes("subscribeType", SubscribeMessageType.DataInRange.name());
        builder.addAttributes("offset", offset);
        builder.addAttributes("count", count);
        return builder.create();
    }

    public static DoipMessage verifyMerkel(String targetDoId, String topicId, String subscriberId) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest(targetDoId, BasicOperations.Subscribe.getName());
        builder.addAttributes("topicId", topicId);
        builder.addAttributes("subscriberId", subscriberId);
        builder.addAttributes("subscribeType", SubscribeMessageType.VerifyMerkel.name());
        return builder.create();
    }

    public static DoipMessage requestMerkel(String targetDoId, String topicId, String subscriberId) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest(targetDoId, BasicOperations.Subscribe.getName());
        builder.addAttributes("topicId", topicId);
        builder.addAttributes("subscriberId", subscriberId);
        builder.addAttributes("subscribeType", SubscribeMessageType.RequestMerkel.name());
        return builder.create();
    }

    public static DoipMessage publish(String targetId, String publisherId, String topicId, PublishMessageType type, byte[] data) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        builder.createRequest(targetId, BasicOperations.Publish.getName());
        builder.addAttributes("topicId", topicId);
        builder.addAttributes("publisherId", publisherId);
        builder.addAttributes("publishType", type.name());
        builder.setBody(data);
        return builder.create();
    }
}
