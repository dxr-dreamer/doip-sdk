/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.client;

import com.google.gson.Gson;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.operations.BasicOperations;

import java.util.HashMap;
import java.util.Random;

public class StreamClientImpl extends DoipClientImpl{

    Random random = new Random();
    HashMap<Integer,DoipMessage> streamInProcess = new HashMap<>();

    public int retrieveByStream(String id,String element, DoipMessageCallback cb){
        DoipMessage msg =
                new DoipMessageFactory.DoipMessageBuilder()
                        .createRequest(id, BasicOperations.Retrieve.getName())
                        .create();
        if(element != null) msg.header.parameters.addAttribute("element", element);
        msg.header.parameters.addAttribute("action", "start");
        int requestID = random.nextInt();
        while(streamInProcess.containsKey(requestID)){
            requestID = random.nextInt();
        }
        msg.requestID = requestID;
        streamInProcess.put(requestID,msg);
        sendMessage(msg, cb);
        return msg.requestID;
    }

    public void stopAStream(int requestID, DoipMessageCallback cb){
        DoipMessage msg = streamInProcess.get(requestID);
        if(msg == null) return;
        msg.header.parameters.addAttribute("action", "stop");
        logger.debug("stop message: " + new Gson().toJson(msg.header.parameters.attributes));
        sendMessage(msg,cb);
    }

}
