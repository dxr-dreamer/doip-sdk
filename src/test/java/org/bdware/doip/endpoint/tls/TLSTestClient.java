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
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.DoExample;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientImpl;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TLSTestClient {
    static Logger LOGGER = LogManager.getLogger(TLSTestClient.class);

    @Test
    public void request() throws Exception {
        String address = "tls://127.0.0.1:21042/";
        DoipClientImpl doipClient = new DoipClientImpl();
        doipClient.connect(ClientConfig.fromUrl(address));
        long start = System.currentTimeMillis();
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger correct = new AtomicInteger(0);
        DigitalObject digitalObject = DoExample.large;
        digitalObject.id = "bdware.test/large";
        int totalCount = 10;
        for (int i = 0; i < totalCount; i++) {
            doipClient.update(digitalObject, new DoipMessageCallback() {
                @Override
                public void onResult(DoipMessage msg) {
                    try {
                        DigitalObject digitalObject = DigitalObject.fromByteArray(msg.body.encodedData);
                        String str = new String(digitalObject.elements.get(0).getData());
                        if (str.startsWith("cold"))
                            correct.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    total.incrementAndGet();

                }
            });
        }
        int circle = 0;
        for (; total.get() < totalCount; ) {
            if (++circle % 100 == 0)
                LOGGER.info(String.format("%d/%d", correct.get(), total.get()));
            Thread.sleep(10);
        }
        LOGGER.info(String.format("%d/%d", correct.get(), total.get()));
    }
}
