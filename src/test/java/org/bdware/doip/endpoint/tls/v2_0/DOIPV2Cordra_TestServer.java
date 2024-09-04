package org.bdware.doip.endpoint.tls.v2_0;


import net.dona.doip.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class DOIPV2Cordra_TestServer {
    static Logger LOGGER = LogManager.getLogger(DOIPV2Cordra_TestServer.class);

    public static void main(String[] args) throws Exception {
        int port = 21043;
        if (args != null && args.length > 0) {
            port = Integer.valueOf(args[0]);
        }
        run(port);
    }

    public static void run(int port) throws Exception {
        DoipServerConfig config = new DoipServerConfig();
        DoipProcessor doipProcessor = new DoipProcessor() {
            @Override
            public void process(DoipServerRequest req, DoipServerResponse resp) throws IOException {
                //ToDo 对req和resp进行处理
            }
        };
        DoipServer doipServer = new DoipServer(config, doipProcessor);
        doipServer.init();
        for( ; ;) {
            Thread.sleep(10000);
        }
    }
}
