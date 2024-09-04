package org.bdware.doip.ddo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.digitalObject.DoType;
import org.bdware.doip.codec.digitalObject.Element;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.DoExample;
import org.bdware.doip.endpoint.server.DoipServer;
import org.bdware.doip.endpoint.server.DoipServerImpl;
import org.bdware.doip.endpoint.server.DoipServiceInfo;
import org.bdware.doip.endpoint.server.HttpRepositoryHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HttpRepositoryHandlerTest {

    private HttpRepositoryHandler handler;

    private String jsonConfig = "{"
            + "\"pyserverUrl\": \"http://127.0.0.1:5000\","
            + "\"id\": \"localTest/doip.tcp\","
            + "\"serviceDescription\": \"testTCPServer\","
            + "\"owner\": \"127.0.0.1/dou.TEST\","
            + "\"repoType\": \"repository\""
            + "}";

    private DoipServiceInfo serviceInfo = DoipServiceInfo.fromJson(jsonConfig);

    @BeforeEach
    void setUp() {
        // 假设 JSON 配置字符串正确包含了 Python 服务器的 URL

        handler = new HttpRepositoryHandler(serviceInfo);
    }

    @Test
    void testHandleHello() {
        // 创建测试请求
        DoipMessage request = new DoipMessage("id", "Hello");

        // 调用 handler 方法
        DoipMessage result = handler.handleHello(request);

        // 验证返回的 DoipMessage
        assertNotNull(result, "Result should not be null");
        assertEquals("Hello from Digital Object Repository!", result.body.getDataAsJsonString(), "Response message should match");
    }

    @Test
    void testHandleListOps() {
        // 创建测试请求
        DoipMessage request = new DoipMessage("id", "ListOps");

        // 调用方法
        DoipMessage result = handler.handleListOps(request);

        // 验证结果是否包含所有操作名称
        assertNotNull(result, "Result should not be null");
        String jsonResponse = result.body.getDataAsJsonString();
        System.out.println(jsonResponse);
        assertNotNull(jsonResponse, "JSON response should not be null");
        assertTrue(jsonResponse.contains("Hello"),
                "Should list 'Hello' operation");
        assertTrue(jsonResponse.contains("Create"),
                "Should list 'Create' operation");
        assertTrue(jsonResponse.contains("Retrieve"),
                "Should list 'Retrieve' operation");
        assertTrue(jsonResponse.contains("Update"),
                "Should list 'Update' operation");
        assertTrue(jsonResponse.contains("Delete"),
                "Should list 'Delete' operation");
        assertTrue(jsonResponse.contains("ListOps"),
                "Should list 'ListOps' operation");
    }

    @Test
    void testHandleCreateBasic() {
        // 构造测试请求，模拟 DoipMessage 包含 data 和 metadata
        String jsonBody = "{"
                + "\"data\": {\"content\": \"Example content\"},"
                + "\"metadata\": {\"description\": \"Test metadata\"}"
                + "}";
        DoipMessage request = new DoipMessage("id", "Create");
        request.body.encodedData = jsonBody.getBytes();

        // 调用 handleCreate 方法
        DoipMessage result = handler.handleCreate(request);

        // 验证返回的 DoipMessage
        assertNotNull(result, "Result should not be null");
        String jsonResponse = result.body.getDataAsJsonString();
        System.out.println(jsonResponse);
        assertTrue(jsonResponse.contains("Digital Object created"),
                "Response should confirm creation");
        assertTrue(jsonResponse.contains("doid"),
                "Response should contain a DOID");
    }

    @Test
    void testCreateAndUpdate() {
        // 创建测试请求，模拟 DoipMessage 包含 data 和 metadata 用于创建
        String jsonCreateBody = "{"
                + "\"data\": {\"content\": \"Example content\"},"
                + "\"metadata\": {\"description\": \"Test metadata\"}"
                + "}";
        DoipMessage createRequest = new DoipMessage("id", "Create");
        createRequest.body.encodedData = jsonCreateBody.getBytes(StandardCharsets.UTF_8);

        // 调用 handleCreate 方法
        DoipMessage createResult = handler.handleCreate(createRequest);

        // 从 createResult 提取 doid
        String doid = createResult.body.getDataAsJsonString(); // 假设我们从结果中解析得到 doid
        String extractedDOID = JsonParser.parseString(doid).getAsJsonObject().get("doid").getAsString();

        // 构造更新请求，使用刚才创建的 doid
        String jsonUpdateBody = "{"
                + "\"doid\": \"" + extractedDOID + "\","
                + "\"data\": {\"content\": \"Updated content\"},"
                + "\"metadata\": {\"description\": \"Updated metadata\"}"
                + "}";
        DoipMessage updateRequest = new DoipMessage("id", "Update");
        updateRequest.body.encodedData = jsonUpdateBody.getBytes(StandardCharsets.UTF_8);

        // 调用 handleUpdate 方法
        DoipMessage updateResult = handler.handleUpdate(updateRequest);

        // 验证更新结果
        assertNotNull(updateResult, "Update result should not be null");
        String updateResponse = updateResult.body.getDataAsJsonString();
        assertTrue(updateResponse.contains("Digital Object updated"),
                "Update response should confirm the update");
    }

    @Test
    void testCreateThenDelete() {
        // 创建一个 DO 对象
        String jsonBodyCreate = "{\"data\": {\"content\": \"Example content\"}, \"metadata\": {\"description\": \"Test metadata\"}}";
        DoipMessage requestCreate = new DoipMessage("id", "Create");
        requestCreate.body.encodedData = jsonBodyCreate.getBytes(StandardCharsets.UTF_8);

        DoipMessage createdResult = handler.handleCreate(requestCreate);
        assertNotNull(createdResult, "Creation result should not be null");

        String jsonResponseCreate = createdResult.body.getDataAsJsonString();
        assertTrue(jsonResponseCreate.contains("Digital Object created"), "Creation response should confirm creation");

        // 解析 DOID 并使用它进行删除操作
        JsonObject createResponseObject = JsonParser.parseString(jsonResponseCreate).getAsJsonObject();
        String doid = createResponseObject.get("doid").getAsString();

        String jsonBodyDelete = "{\"doid\": \"" + doid + "\"}";
        DoipMessage requestDelete = new DoipMessage("id", "Delete");
        requestDelete.body.encodedData = jsonBodyDelete.getBytes(StandardCharsets.UTF_8);

        DoipMessage deleteResult = handler.handleDelete(requestDelete);
        assertNotNull(deleteResult, "Deletion result should not be null");

        String jsonResponseDelete = deleteResult.body.getDataAsJsonString();
        assertTrue(jsonResponseDelete.contains("Digital Object deleted"), "Deletion response should confirm deletion");
    }

    @Test
    void testCreateAndRetrieveFlow() {
        try {
            // 构造测试请求，模拟 DoipMessage 包含 data 和 metadata
            String jsonCreateBody = "{"
                    + "\"data\": {\"content\": \"Example content\"},"
                    + "\"metadata\": {\"description\": \"Test metadata\"}"
                    + "}";
            DoipMessage createRequest = new DoipMessage("id", "Create");
            createRequest.body.encodedData = jsonCreateBody.getBytes(StandardCharsets.UTF_8);

            // 调用 handleCreate 方法
            DoipMessage createResult = handler.handleCreate(createRequest);
            String createResponse = createResult.body.getDataAsJsonString();
            System.out.println("Create Response: " + createResponse);
            String doid = extractDoid(createResponse);

            // 构造检索请求的 JSON 包含 doid
            String jsonRetrieveBody = "{"
                    + "\"doid\": \"" + doid + "\""
                    + "}";
            DoipMessage retrieveRequest = new DoipMessage("id", "Retrieve");
            retrieveRequest.body.encodedData = jsonRetrieveBody.getBytes(StandardCharsets.UTF_8);

            // 调用 handleRetrieve 方法
            DoipMessage retrieveResult = handler.handleRetrieve(retrieveRequest);

            // 从 retrieveResult 获取 DigitalObject
            DigitalObject retrievedDO = retrieveResult.body.getDataAsDigitalObject();
            assertNotNull(retrievedDO, "Retrieved digital object should not be null");
            assertNotNull(retrievedDO, "Retrieved digital object should not be null");
            assertNotNull(retrievedDO.id, "Digital object ID should not be null");
            // 打印和检查
            System.out.println("Retrieved DigitalObject ID: " + retrievedDO.id);
            assertEquals(doid, retrievedDO.id, "DOID should match the created one");
            // 打印和校验元素
            if (retrievedDO.elements != null && !retrievedDO.elements.isEmpty()) {
                Element element = retrievedDO.elements.get(0); // 假设只有一个元素
                assertNotNull(element.getData(), "Element data should not be null");
                System.out.println("Element Data: " + element.toString());
                assertEquals("ddolib", element.type, "Element type should match 'ddolib'");
                assertTrue(element.length > 0, "Element length should be greater than 0");
            } else {
                fail("No elements found in retrieved digital object");
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }


    private String extractDoid(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        return jsonObject.get("doid").getAsString();
    }

    @Test
    void testCreateAndRetrieveFlowPrint() {
        try {
            // 构造测试请求，模拟 DoipMessage 包含 data 和 metadata
            String jsonCreateBody = "{"
                    + "\"data\": {\"content\": \"Example content\"},"
                    + "\"metadata\": {\"description\": \"Test metadata\"}"
                    + "}";
            DoipMessage createRequest = new DoipMessage("id", "Create");
            createRequest.body.encodedData = jsonCreateBody.getBytes(StandardCharsets.UTF_8);

            // 调用 handleCreate 方法
            DoipMessage createResult = handler.handleCreate(createRequest);
            String createResponse = createResult.body.getDataAsJsonString();
            System.out.println("Create Response: " + createResponse);
            String doid = extractDoid(createResponse);

            // 构造检索请求的 JSON 包含 doid
            String jsonRetrieveBody = "{"
                    + "\"doid\": \"" + doid + "\""
                    + "}";
            DoipMessage retrieveRequest = new DoipMessage("id", "Retrieve");
            retrieveRequest.body.encodedData = jsonRetrieveBody.getBytes(StandardCharsets.UTF_8);

            // 调用 handleRetrieve 方法
            DoipMessage retrieveResult = handler.handleRetrieve(retrieveRequest);

            // 从 retrieveResult 获取 DigitalObject
            DigitalObject retrievedDO = retrieveResult.body.getDataAsDigitalObject();
            assertNotNull(retrievedDO, "Retrieved digital object should not be null");
            System.out.println("Retrieved Digital Object ID: " + retrievedDO.id);
            System.out.println("Retrieved Digital Object Type: " + retrievedDO.type);
            System.out.println("Retrieved Digital Object Attributes: " + retrievedDO.attributes);
            retrievedDO.elements.forEach(element ->
                    System.out.println("Element ID: " + element.id + ", Type: " + element.type + ", Length: " + element.length)
            );

        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed due to an exception: " + e.getMessage());
        }
    }

    @Test
    void testDoipMessageForCreate() {
        // 使用 DoExample 类生成一个 DigitalObject
        DigitalObject testDO = DoExample.large; // 使用预设的大型 DO 对象

        // 调用创建 DoipMessage 的方法
        DoipMessage createdMessage = handler.createDoipMessageForCreate(testDO);

        // 检查返回的 DoipMessage 是否非空
        assertNotNull(createdMessage, "DoipMessage should not be null");

        // 检查 body 是否包含正确的 JSON 字符串
        String bodyContent = new String(createdMessage.body.encodedData, StandardCharsets.UTF_8);
        assertTrue(bodyContent.contains("\"data\""), "Body should contain 'data' key");
        assertTrue(bodyContent.contains("\"metadata\""), "Body should contain 'metadata' key");

        // 打印输出消息内容，用于手动验证
        System.out.println("Generated DoipMessage Body: " + bodyContent);

        // 调用 handleCreate 方法
        DoipMessage result = handler.handleCreate(createdMessage);

        // 验证返回的 DoipMessage
        assertNotNull(result, "Result should not be null");
        String jsonResponse = result.body.getDataAsJsonString();
        System.out.println(jsonResponse);
        assertTrue(jsonResponse.contains("Digital Object created"),
                "Response should confirm creation");
        assertTrue(jsonResponse.contains("doid"),
                "Response should contain a DOID");
    }

    @Test
    void testServerStartup() {
        String serverjsonConfig = "{"
                + "\"pyserverUrl\": \"http://127.0.0.1:5000\","
                + "\"id\": \"localTest/doip.tcp\","
                + "\"serviceDescription\": \"testTCPServer\","
                + "\"publicKey\": \"{\\\"kty\\\":\\\"EC\\\",\\\"d\\\":\\\"VPvAXurYhEwCRbIuSCEPOaTyfUIbH6an4scA4BpdWCw\\\",\\\"use\\\":\\\"sig\\\",\\\"crv\\\":\\\"P-256\\\",\\\"kid\\\":\\\"86.5000.470/dou.TEST\\\",\\\"x\\\":\\\"IFVGcQ22vd7SEd1HsjcYuaLWUrfj4ochceom6YNCX4g\\\",\\\"y\\\":\\\"HSuB60fA_53vi4L30WiVQjouvAB0gSPAS8kf8Ny3RN0\\\"}\","
                + "\"serviceName\": \"testTCPServer\","
                + "\"listenerInfos\": ["
                + "    {"
                + "        \"url\": \"udp://127.0.0.1:8004\","
                + "        \"protocolVersion\": \"2.1\""
                + "    }"
                + "],"
                + "\"owner\": \"86.5000.470/dou.TEST\","
                + "\"repoType\": \"registry\""
                + "}";

        // Parse the JSON string to create a DoipServiceInfo object
        DoipServiceInfo serverserviceInfo = DoipServiceInfo.fromJson(serverjsonConfig);

        // Create server instance
        DoipServer server = new DoipServerImpl(serverserviceInfo);

        HttpRepositoryHandler shandler = new HttpRepositoryHandler(serverserviceInfo);
        // Set the repository handler
        server.setRepositoryHandler(shandler);

        // Start the server and catch any initialization errors
        assertDoesNotThrow(() -> {
            server.start();
        }, "Server should start without throwing an exception");

        server.start();
        // Optionally, we can stop the server after the test
        // server.stop();
    }




}
