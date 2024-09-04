package org.bdware.doip.endpoint.event;

import com.google.gson.JsonObject;
import org.bdware.doip.codec.doipMessage.DoipMessage;

//订阅者要处理以下消息：
//receiveData
//receiveHash
//receiveDataAndHash
public interface Subscriber {
    //------start: 处理来自发布者的消息-------
    void receiveMerkelConfiguration(String topicId, JsonObject configuration, DoipMessage data);

    DoipMessage receiveData(String topicId, String publisherId, DoipMessage data);

    DoipMessage receiveHash(String topicId, String publisherId, DoipMessage data);

    DoipMessage receiveDataAndHash(String topicId, String publisherId, DoipMessage data);

    //------end: 处理来自发布者的消息-------

    //Commit了一条消息会触发这个回调
    void onReceiveData(String topicId, String publisherId, DoipMessage request);

}
