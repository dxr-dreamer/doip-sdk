package org.bdware.doip.endpoint.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import com.google.gson.Gson;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.digitalObject.DoType;
import org.bdware.doip.codec.digitalObject.Element;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.endpoint.server.RepositoryHandlerBase;

import java.nio.charset.StandardCharsets;

public class HttpRepositoryHandler extends RepositoryHandlerBase {

    public HttpRepositoryHandler(DoipServiceInfo info) {
        super(info);
    }

    @Override
    public DoipMessage handleHello(DoipMessage request) {
        // 获取配置的Python服务器URL
        String url = serviceInfo.pyserverUrl + "/hello";

        // 使用 CloseableHttpClient 发送 HTTP GET 请求
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            org.apache.http.HttpResponse response = client.execute(httpGet);

            // 读取响应内容并转换为字符串
            String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            // 解析 JSON 响应
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String messageFromServer = jsonObject.get("message").getAsString();

            // 使用从 Python 服务器返回的消息构造 DOIP 消息并返回
            return replyString(request, messageFromServer);
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常时返回错误消息
            return replyString(request, "Failed to connect to Python server");
        }
    }

    @Override
    public DoipMessage handleListOps(DoipMessage request) {
        // 获取配置的Python服务器URL
        String url = serviceInfo.pyserverUrl + "/listops";

        // 使用 CloseableHttpClient 发送 HTTP GET 请求
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            org.apache.http.HttpResponse response = client.execute(httpGet);

            // 读取响应内容并转换为字符串
            String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            // 解析 JSON 响应并构造回复消息
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray operations = jsonObject.getAsJsonArray("operations");
            return replyString(request, operations.toString());
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常时返回错误消息
            return replyString(request, "Failed to connect to Python server");
        }
    }

    public DoipMessage handleCreate(DigitalObject digitalObject){
        return handleCreate(createDoipMessageForCreate(digitalObject));
    }
    // 只适用于请求体为规定格式的json
    // 由do实例构造请求实例的方法需要更多测试
    // request.body转化为json后，应包含data(any)与metadata(dict)字段
    public DoipMessage handleCreate(DoipMessage request) {
        // 获取配置的Python服务器URL
        String url = serviceInfo.pyserverUrl + "/create";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // 构建 POST 请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 解析 JSON 字符串来获取 data 和 metadata
            String jsonData = request.body.getDataAsJsonString(); // 获取 JSON 数据字符串
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();

            JsonObject jsonRequest = new JsonObject();
            if (jsonObject.has("data") && jsonObject.has("metadata")) {
                jsonRequest.add("data", jsonObject.get("data"));
                jsonRequest.add("metadata", jsonObject.get("metadata"));
            } else {
                return replyString(request, "Invalid data or metadata in request");
            }

            // 设置请求体
            StringEntity entity = new StringEntity(jsonRequest.toString());
            httpPost.setEntity(entity);

            // 发送请求并获取响应
            HttpResponse response = client.execute(httpPost);
            String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            // 根据响应构造 DOIP 消息返回
            return replyString(request, jsonResponse); // 直接将 JSON 响应作为字符串回复
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常时返回错误消息
            return replyString(request, "Failed to connect to Python server or error in processing the request");
        }
    }

    // request.body转化为json后，应包含doid(str),data(any)与metadata(dict)字段
    @Override
    public DoipMessage handleUpdate(DoipMessage request) {
        // 从 DoipMessage 获取 JSON 字符串
        String jsonData = request.body.getDataAsJsonString();
        JsonObject jsonRequest = JsonParser.parseString(jsonData).getAsJsonObject();

        // 提取 doid、data 和 metadata
        if (!jsonRequest.has("doid") || !jsonRequest.has("data") || !jsonRequest.has("metadata")) {
            return replyString(request, "Invalid request: Missing 'doid', 'data', or 'metadata'.");
        }
        String doid = jsonRequest.get("doid").getAsString();
        JsonObject data = jsonRequest.getAsJsonObject("data");
        JsonObject metadata = jsonRequest.getAsJsonObject("metadata");

        // 构建 URL
        String url = serviceInfo.pyserverUrl + "/update/" + doid;

        // 使用 CloseableHttpClient 发送 HTTP PUT 请求
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");

            // 重新构建包含 data 和 metadata 的 JSON 对象作为请求体
            JsonObject requestBody = new JsonObject();
            requestBody.add("data", data);
            requestBody.add("metadata", metadata);

            StringEntity entity = new StringEntity(requestBody.toString());
            httpPut.setEntity(entity);

            // 发送请求并获取响应
            org.apache.http.HttpResponse response = client.execute(httpPut);
            String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            // 根据响应构造 DOIP 消息返回
            return replyString(request, jsonResponse); // 直接将 JSON 响应作为字符串回复
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常时返回错误消息
            return replyString(request, "Failed to connect to Python server or error in processing the request");
        }
    }

    // request.body转化为json后，应包含doid(str)字段
    @Override
    public DoipMessage handleDelete(DoipMessage request) {
        // 从 request 的 body 获取 doid
        String jsonBody = request.body.getDataAsJsonString();
        JsonObject jsonObject = JsonParser.parseString(jsonBody).getAsJsonObject();
        String doid = jsonObject.get("doid").getAsString();

        // 构造和发送 HTTP DELETE 请求
        String url = serviceInfo.pyserverUrl + "/delete/" + doid;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(url);
            org.apache.http.HttpResponse response = client.execute(httpDelete);

            // 读取响应内容并转换为字符串
            String jsonResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            // 根据 HTTP 响应构造 DOIP 消息并返回
            JsonObject responseJson = JsonParser.parseString(jsonResponse).getAsJsonObject();
            String messageFromServer = responseJson.has("message") ? responseJson.get("message").getAsString() :
                    responseJson.get("error").getAsString();

            return replyString(request, messageFromServer);
        } catch (Exception e) {
            e.printStackTrace();
            // 出现异常时返回错误消息
            return replyString(request, "Failed to connect to Python server");
        }
    }

    @Override
    public DoipMessage handleRetrieve(DoipMessage request) {
        // 从 request 获取 doid
        String jsonBody = request.body.getDataAsJsonString();
        JsonObject bodyObj = JsonParser.parseString(jsonBody).getAsJsonObject();
        String doid = bodyObj.get("doid").getAsString();
        String url = serviceInfo.pyserverUrl + "/retrieve/" + doid;

        // 发送 HTTP GET 请求
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            org.apache.http.HttpResponse response = client.execute(httpGet);

            // 检查 HTTP 响应状态码
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 404) {
                return replyString(request, "Digital Object not found");
            }

            // 解析响应 JSON
            String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            DigitalObject digitalObject = buildDigitalObjectFromJson(jsonObject);

            // 使用构建的 DigitalObject 返回 DOIP 响应
            return replyDO(request, digitalObject);
        } catch (Exception e) {
            e.printStackTrace();
            return replyString(request, "Failed to retrieve Digital Object due to an error");
        }
    }

    private DigitalObject buildDigitalObjectFromJson(JsonObject jsonObject) {
        String doid = jsonObject.get("doid").getAsString();
        JsonObject metadata = jsonObject.getAsJsonObject("metadata");
        JsonObject data = jsonObject.getAsJsonObject("data");

        DigitalObject digitalObject = new DigitalObject(doid, DoType.DO);
        digitalObject.setAttributes(metadata);

        // 假设 data 是单一元素的 JSON 对象
        Element element = new Element("data", "ddolib");
        element.setData(data.toString().getBytes(StandardCharsets.UTF_8)); // 转换 JSON 数据为 byte[]
        digitalObject.addElements(element);

        return digitalObject;
    }

    public DoipMessage createDoipMessageForCreate(DigitalObject digitalObject) {
        String doid = "id";
        if (digitalObject.id != null){
            doid = digitalObject.id;
        }
        DoipMessage message = new DoipMessage(doid, BasicOperations.Create.getName());
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



    // 实现其他方法...
}

