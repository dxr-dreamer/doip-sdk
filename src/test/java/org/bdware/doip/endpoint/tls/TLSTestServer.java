/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.tls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.endpoint.TestRepoHandler;
import org.bdware.doip.endpoint.server.DoipListenerConfig;
import org.bdware.doip.endpoint.server.DoipServerImpl;
import org.bdware.doip.endpoint.server.DoipServiceInfo;
import org.bdware.doip.endpoint.server.TLSListenerInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TLSTestServer {
    static Logger LOGGER = LogManager.getLogger(TLSTestServer.class);

    public static void main(String[] arg) throws InterruptedException {
        int port = 21042;
        if (arg != null && arg.length > 0) {
            port = Integer.valueOf(arg[0]);
        }
        run(port);
    }

    public static void run(int port) throws InterruptedException {
        List<DoipListenerConfig> infos = new ArrayList<>();
        try {
            File chain = new File("./input/chained.pem");
            File key = new File("./input/domain.pem");
            TLSListenerInfo tlsinfo = new TLSListenerInfo("tls://127.0.0.1:" + port, "2.1", chain, key);
            infos.add(tlsinfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DoipServiceInfo info = new DoipServiceInfo("aibd.govdata.tj/do.3f9c41e6-9f8e-48a0-9220-53f438d40e43", "ownerDEF", "gateRepo", infos);
        DoipServerImpl server = new DoipServerImpl(info);
        final AtomicInteger count = new AtomicInteger(0);
        TestRepoHandler handler = new TestRepoHandler();
        handler.count = count;
        server.setRepositoryHandler(handler);
        server.start();
        for (; ; ) {
            LOGGER.info("Count:" + count.get());
            Thread.sleep(10000);
        }
    }
}
