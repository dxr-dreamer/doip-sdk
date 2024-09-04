/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.tls.v2_0;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.DOIPVersion;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.DoExample;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientImpl;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class DOIPV2_TLSTestClient {
    static Logger LOGGER = LogManager.getLogger(DOIPV2_TLSTestClient.class);
    static boolean flag = true;
    @Test
    public void hello() throws Exception{
        //21042
        String address = "tls://127.0.0.1:21042/";
        DoipClientImpl doipClient = new DoipClientImpl();
        doipClient.connect(ClientConfig.fromUrl(address, DOIPVersion.V2_0));
        doipClient.hello("aibd.govdata.tj/do.3f9c41e6-9f8e-48a0-9220-53f438d40e43", new DoipMessageCallback() {
            @Override
            public void onResult(DoipMessage msg) {
                LOGGER.info(msg.header);
                flag = false;
            }
        });

        while(flag){
            Thread.sleep(500);
        }
    }

    @Test
    public void update() throws Exception {
        String address = "tls://127.0.0.1:21042/";
        DoipClientImpl doipClient = new DoipClientImpl();
        doipClient.connect(ClientConfig.fromUrl(address, DOIPVersion.V2_0));
        long start = System.currentTimeMillis();
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger correct = new AtomicInteger(0);
        DigitalObject digitalObject = DoExample.small;
        digitalObject.id = "bdware.test/small";
        int totalCount = 1;
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
    @Test
    public void retrieve() throws Exception {
        String address = "tls://127.0.0.1:21042/";
        DoipClientImpl doipClient = new DoipClientImpl();
        doipClient.connect(ClientConfig.fromUrl(address, DOIPVersion.V2_0));
        long start = System.currentTimeMillis();
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger correct = new AtomicInteger(0);
        DigitalObject digitalObject = DoExample.small;
        digitalObject.id = "bdware.test/small";
        int totalCount = 1;
        for (int i = 0; i < totalCount; i++) {
            doipClient.retrieve("bdware.test/small","",true, new DoipMessageCallback() {
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