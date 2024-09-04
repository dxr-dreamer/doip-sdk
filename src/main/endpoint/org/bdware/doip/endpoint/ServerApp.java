package org.bdware.doip.endpoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bdware.doip.endpoint.server.DoipServer;
import org.bdware.doip.endpoint.server.DoipServerImpl;
import org.bdware.doip.endpoint.server.DoipServiceInfo;
import org.bdware.doip.endpoint.server.HttpRepositoryHandler;

import java.io.IOException;

public class ServerApp {
    public static void main(String[] args) {
        // Define the JSON configuration string
        String serverJsonConfig = "{"
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

        // Parse the JSON configuration to create a DoipServiceInfo object
        DoipServiceInfo serviceInfo = new Gson().fromJson(serverJsonConfig, new TypeToken<DoipServiceInfo>(){}.getType());

        // Create the server instance
        DoipServer server = new DoipServerImpl(serviceInfo);
        HttpRepositoryHandler handler = new HttpRepositoryHandler(serviceInfo);

        // Set the repository handler
        server.setRepositoryHandler(handler);

        // Try starting the server
        try {
            server.start();
            System.out.println("Server started successfully.");
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }

        // Keep the server running or perform further actions
    }
}
