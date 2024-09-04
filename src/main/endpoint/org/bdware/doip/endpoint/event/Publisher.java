package org.bdware.doip.endpoint.event;

import com.google.gson.JsonArray;
import org.bdware.doip.codec.doipMessage.DoipMessage;

//发布者, 处理以下几类消息：
//订阅消息
//取消订阅
//请求发送第s,e条消息
//请求发送[k,l,m]条消息
//请求校验第s,e的merkelTree
//请求发送第s,e的merkelTree
public interface Publisher {
    //------start: 处理来自订阅者的消息-------
    DoipMessage subscribe(String topicId, String subscriberId, boolean needReplay, DoipMessage request);

    DoipMessage unsubscribe(String topicId, String subscriberId, DoipMessage request);

    // count==-1时，表示发当前所有消息
    DoipMessage sendDataInRange(String topicId, String subscriberId, long offset, long count, DoipMessage request);

    DoipMessage sendDataInList(String topicId, String subscriberId, JsonArray indexList, DoipMessage request);

    DoipMessage verifyMerkelInRange(String topicId, String subscriberId, DoipMessage request);

    DoipMessage sendMerkelInRange(String topicId, String subscriberId, DoipMessage request);
    //------end: 处理来自订阅者的消息-------

    // 触发广播某个Topic的 DoipMessage Event。
    void publish(String topicId, DoipMessage request);
}
