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

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.digitalObject.Element;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.exception.DoipConnectException;
import org.bdware.doip.codec.metadata.SearchParameter;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.endpoint.EndpointFactory;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class DoipClientImpl implements DoipClient {

    DoipClientChannel doipChannel;
    String recipientID;
    String serverURL = null;
    Logger logger = LogManager.getLogger(DoipClientImpl.class);

    int timeoutSeconds = 5;

    @Override
    public void hello(String id, DoipMessageCallback cb) {
        DoipMessage msg =
                new DoipMessageFactory.DoipMessageBuilder()
                        .createRequest(id, BasicOperations.Hello.getName())
                        .create();
        sendMessage(msg, cb);
    }

    @Override
    public void listOperations(String id, DoipMessageCallback cb) {
        DoipMessage msg =
                new DoipMessageFactory.DoipMessageBuilder()
                        .createRequest(id, BasicOperations.ListOps.getName())
                        .create();
        sendMessage(msg, cb);
    }

    @Override
    public void retrieve(String id, String element, boolean includeElementData, DoipMessageCallback cb) {
        // 构造检索请求的 JSON 包含 doid
        String jsonRetrieveBody = "{"
                + "\"doid\": \"" + id + "\""
                + "}";
        DoipMessage msg = new DoipMessage(id, BasicOperations.Retrieve.getName());
        msg.body.encodedData = jsonRetrieveBody.getBytes(StandardCharsets.UTF_8);
        // 以下功能作用不明，未实现
        if (element != null) msg.header.parameters.addAttribute("element", element);
        if (includeElementData) msg.header.parameters.addAttribute("includeElementData", "true");
        sendMessage(msg, cb);
    }

    // 都是简单粗暴的直接传do
    @Override
    public void create(String targetDOIPServiceID, DigitalObject digitalObject, DoipMessageCallback cb) {
        DoipMessage msg = createDoipMessageFromDO(digitalObject,"create");
        sendMessage(msg, cb);
    }

    @Override
    public void update(DigitalObject digitalObject, DoipMessageCallback cb) {
        DoipMessage msg = createDoipMessageFromDO(digitalObject);
        sendMessage(msg, cb);
    }

    // 似乎真的用header id
    @Override
    public void delete(String id, DoipMessageCallback cb) {

        String jsonBodyDelete = "{\"doid\": \"" + id + "\"}";
        DoipMessage msg = new DoipMessage(id, BasicOperations.Delete.getName());
        msg.header.setIsRequest(true);
        msg.body.encodedData = jsonBodyDelete.getBytes(StandardCharsets.UTF_8);

        sendMessage(msg, cb);
    }

    @Override
    public void search(String id, SearchParameter sp, DoipMessageCallback cb) {
        DoipMessage msg =
                new DoipMessageFactory.DoipMessageBuilder()
                        .createRequest(id, BasicOperations.Search.getName())
                        .addAttributes("query", sp.query)
                        .addAttributes("pageNum", sp.pageNum)
                        .addAttributes("pageSize", sp.pageSize)
                        .addAttributes("type", sp.type)
                        .create();
        sendMessage(msg, cb);
    }

    @Override
    public void sendRawMessage(DoipMessage msg, DoipMessageCallback cb) {
        sendMessage(msg, cb);
    }

    @Override
    public void close() {
        doipChannel.close();
        doipChannel = null;
    }


    @Override
    public void connect(ClientConfig config) {
        try {
            serverURL = config.url;
            doipChannel = EndpointFactory.createDoipClientChannel(config);
            if (doipChannel == null) return;
            doipChannel.connect(serverURL);
            doipChannel.setTimeoutSecond(config.timeoutSeconds);
        } catch (URISyntaxException e) {
            logger.error("UUUUUURISyntaxException Exception!");
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("IIIIInterruped Exception!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reconnect() throws DoipConnectException {
        if (serverURL == null) throw (new DoipConnectException("target URL not set, use .connect(url) first"));
        ClientConfig clientConfig = ClientConfig.fromUrl(serverURL);
        if (doipChannel == null) doipChannel = EndpointFactory.createDoipClientChannel(clientConfig);
        if (doipChannel == null) return;
        try {
            doipChannel.connect(serverURL);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void reconnect(String url) throws DoipConnectException {
        serverURL = url;
        if (serverURL == null) throw (new DoipConnectException("target URL not set, use .connect(url) first"));
        ClientConfig clientConfig = ClientConfig.fromUrl(serverURL);
        if (doipChannel == null) doipChannel = EndpointFactory.createDoipClientChannel(clientConfig);
        if (doipChannel == null) return;
        try {
            doipChannel.connect(serverURL);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getRepoUrl() {
        return serverURL;
    }

    public void setRepoUrl(String serverURL) {
        this.serverURL = serverURL;
    }

    @Override
    public String getRecipientID() {
        return recipientID;
    }

    @Override
    public void setRecipientID(String id) {
        recipientID = id;
    }

    @Override
    public boolean isConnected() {
        return doipChannel != null && doipChannel.isConnected();
    }

    public void waitForConnected() {
        for (int i = 0; i < 100; i++) {
            if (doipChannel == null || !doipChannel.isConnected()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }
            break;
        }
    }

    public void sendMessage(DoipMessage msg, DoipMessageCallback cb) {
        if (!isConnected()) {
            if (!tryReconnect()) {
                logger.warn("channel not connect yet! " + (doipChannel == null) + " --> serverUrl:" + serverURL);
                DoipMessage resp = DoipMessageFactory.createConnectFailedResponse(msg.requestID);
                cb.onResult(resp);
                return;
            }
        }
        msg.setRecipientID(recipientID);
        doipChannel.sendMessage(msg, cb);
    }

    private boolean tryReconnect() {
        try {
            reconnect();
        } catch (DoipConnectException e) {
            throw new RuntimeException(e);
        }
        return isConnected();
    }

    public DoipMessage createDoipMessageFromDO(DigitalObject digitalObject) {
        // update
        DoipMessage message = new DoipMessage(digitalObject.id, BasicOperations.Update.getName());
        message.header.setIsRequest(true);

        // 创建 JSON 对象来存储 data 和 metadata
        JsonObject jsonRequest = new JsonObject();
        JsonObject jsonData = new JsonObject();
        JsonObject jsonMetadata = digitalObject.attributes; // 直接使用 DigitalObject 的 attributes 作为 metadata

        // 假设 DigitalObject 的第一个元素用于 data
        if (digitalObject.elements != null && !digitalObject.elements.isEmpty()) {
            Element firstElement = digitalObject.elements.get(0);
            if (firstElement.getData() != null) {
                jsonData.addProperty("content", new String(firstElement.getData(), StandardCharsets.UTF_8)); // 假设这是元素的数据表示
            }
        }

        // 构造请求 JSON
        jsonRequest.add("data", jsonData);
        jsonRequest.add("metadata", jsonMetadata);
        jsonRequest.addProperty("doid", digitalObject.id); // 添加 DO 的 ID 到 JSON 请求中

        // 将 JSON 对象转为字符串并设置到 DoipMessage 的 body 中
        try {
            String jsonString = jsonRequest.toString();
            message.body.encodedData = jsonString.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }

    public DoipMessage createDoipMessageFromDO(DigitalObject digitalObject,String targetServiceId) {
        // create
        DoipMessage message = new DoipMessage(targetServiceId, BasicOperations.Create.getName());
        message.header.setIsRequest(true);

        // 创建 JSON 对象来存储 data 和 metadata
        JsonObject jsonRequest = new JsonObject();
        JsonObject jsonData = new JsonObject();
        JsonObject jsonMetadata = digitalObject.attributes; // 直接使用 DigitalObject 的 attributes 作为 metadata

        // 假设 DigitalObject 的第一个元素用于 data
        if (digitalObject.elements != null && !digitalObject.elements.isEmpty()) {
            Element firstElement = digitalObject.elements.get(0);
            if (firstElement.getData() != null) {
                jsonData.addProperty("content", new String(firstElement.getData(), StandardCharsets.UTF_8)); // 假设这是元素的数据表示
            }
        }

        // 构造请求 JSON
        jsonRequest.add("data", jsonData);
        jsonRequest.add("metadata", jsonMetadata);

        // 将 JSON 对象转为字符串并设置到 DoipMessage 的 body 中
        try {
            String jsonString = jsonRequest.toString();
            message.body.encodedData = jsonString.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return message;
    }
    ;
}
