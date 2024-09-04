/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.tcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.endpoint.DoExample;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientImpl;
import org.bdware.doip.endpoint.client.DoipMessageCallback;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DoipClientTest {
    static Logger LOGGER = LogManager.getLogger(DoipClientTest.class);

    //[INFO ]20:59:10.205 Final Result:10000/10000 dur:1843 (DoipClientTest.java:47)
    //[INFO ]00:23:01.932 Final Result:10000/10000 dur:3962 (DoipClientTest.java:47)
    //io.netty.channel.AbstractChannel$AnnotatedSocketException: Can't assign requested address: /127.0.0.1:1717
    @Test
    public void testRetrieveReconnect() throws InterruptedException {
        long start = System.currentTimeMillis();
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger correct = new AtomicInteger(0);
        int totalCount = 10000;
        for (int i = 0; i < totalCount; i++) {
            final DoipClientImpl doipClient = new DoipClientImpl();
            doipClient.connect(ClientConfig.fromUrl("tcp://127.0.0.1:1717"));
            doipClient.retrieve("aibd/do.e626924a-3b1c-492f-9a41-59179bfe0361", null, true, new DoipMessageCallback() {
                @Override
                public void onResult(DoipMessage msg) {
                    String str = new String(msg.body.encodedData);
                    //LOGGER.info("Retrieved:" + str
                    //+ " respCode:" + msg.header.parameters.response);
                    total.incrementAndGet();
                    if (str.contains("aaa"))
                        correct.incrementAndGet();
                    if (doipClient != null) doipClient.close();
                }
            });
        }
        int circle = 0;
        for (; total.get() < totalCount; ) {
            if (++circle % 100 == 0)
                LOGGER.info(String.format("%d/%d", correct.get(), total.get()));
            Thread.sleep(10);
        }
        int dur = (int) (System.currentTimeMillis() - start);
        LOGGER.info(String.format("Final Result:%d/%d dur:%d", correct.get(), total.get(), dur));
    }

    //[INFO ]20:59:10.205 Final Result:10000/10000 dur:1843 (DoipClientTest.java:47)
    //[INFO ]00:22:41.480 Final Result:10000/10000 dur:1585 (DoipClientTest.java:79)
    //[INFO ]00:20:02.158 Final Result:10000/10000 dur:1260 (DoipClientTest.java:78)

    //[INFO ]1MB 阿里云12:34:40.126 Final Result:1000/1000 dur:145297 (DoipClientTest.java:90)
    // [INFO ] <100bytes 12:53:44.621 Final Result:100000/100000 dur:44302 (DoipClientTest.java:90)
    //String serverAddr = "tcp://39.104.208.148:21042";
    String serverAddr = "tcp://127.0.0.1:22042";
    //String serverAddr = "tcp://39.104.208.148:21043";

    //[INFO ]13:58:36.514 tcp://127.0.0.1:21042 Final Result:100000/100000 dur:6422 rps:15571.47 (DoipClientTest.java:152)
    //[INFO ]14:07:43.841 tcp://39.104.208.148:21042 Final Result:100000/100000 dur:23539 rps:4248.27 (DoipClientTest.java:163)
    //[INFO ]18:00:19.110 tcp://39.104.208.148:21042 Final Result:100000/100000 dur:19008 rps:5260.94 (DoipClientTest.java:164)
    //=========From Client To Ali============
    //========GOServer======
    //[INFO ]22:44:53.320 tcp://39.104.208.148:21043 Final Result:10000/10000 dur:5184 rps:1929.01
    //[INFO ]22:45:13.564 tcp://39.104.208.148:21043 Final Result:10000/10000 dur:6595 rps:1516.30 (DoipClientTest.java:173)
    //========JavaServer======
    //[INFO ]22:35:36.773 tcp://39.104.208.148:21042 Final Result:100000/100000 dur:56747 rps:1762.21
    //[INFO ]22:36:25.928 tcp://39.104.208.148:21042 Final Result:10000/10000 dur:5316 rps:1881.11 (DoipClientTest.java:166)
    //=========From Ali To Ali============
    //========GOServer======
    //[INFO ]23:23:33.827 tcp://39.104.208.148:21043 Final Result:10000/10000 dur:994 rps:10060.36
    //[INFO ]23:23:09.093 tcp://39.104.208.148:21043 Final Result:100000/100000 dur:9982 rps:10018.03
    //========JavaServer======
    //[INFO ]23:21:31.202 tcp://39.104.208.148:21043 Final Result:10000/10000 dur:815 rps:12269.94
    //[INFO ]23:22:07.044 tcp://39.104.208.148:21043 Final Result:100000/100000 dur:5804 rps:17229.50

    @Test
    public void retrieveSmallReqSmallResp() throws InterruptedException {
        testRetrive(serverAddr, 100000, "bdware.test/small");
    }

    //[INFO ]14:04:06.583 Final Result:1000/1000 dur:1624 rps:615.76 (DoipClientTest.java:122)
    //[INFO ]15:04:44.567 tcp://39.104.208.148:21042 Final Result:1000/1000 dur:1664213 rps:0.60 (DoipClientTest.java:164)
    //[INFO ]18:04:34.049 tcp://39.104.208.148:21042 Final Result:100/100 dur:147864 rps:0.68 (DoipClientTest.java:166)

    //=========From Client To Ali============
    //========GOServer======
    // [INFO ]22:47:57.138 tcp://39.104.208.148:21043 Final Result:100/100 dur:143820 rps:0.70
    //========JavaServer======
    //[INFO ]22:39:13.940 tcp://39.104.208.148:21042 Final Result:100/100 dur:143744 rps:0.70
    //=========From Ali To Ali============
    //========GOServer======
    //[INFO ]23:24:31.354 tcp://39.104.208.148:21043 Final Result:1000/1000 dur:6272 rps:159.44
    //[INFO ]23:24:47.992 tcp://39.104.208.148:21043 Final Result:1000/1000 dur:6224 rps:160.67
    //========JavaServer======
    //[INFO ]23:20:44.496 tcp://39.104.208.148:21043 Final Result:1000/1000 dur:8119 rps:123.17
    //[INFO ]23:21:09.055 tcp://39.104.208.148:21043 Final Result:1000/1000 dur:7979 rps:125.33

    @Test
    public void retrieveSmallReqLargeResp() throws InterruptedException {
        testRetrive(serverAddr, 1, "bdware.test/large");
    }

    //[INFO ]14:04:38.661 Final Result:1000/1000 dur:1504 rps:664.89 (DoipClientTest.java:123)
    //[INFO ]18:05:16.192 Final Result:100/100 dur:22211 rps:4.50 (DoipClientTest.java:129)
    //=========From Client To Ali============
    //========GOServer======
    //[INFO ]22:51:05.949 Final Result:100/100 dur:23531 rps:4.25
    //========JavaServer======
    //[INFO ]22:39:55.479 Final Result:100/100 dur:22953 rps:4.36
    //=========From Ali To Ali============
    //========GOServer======
    //[INFO ]23:25:19.908 Final Result:1000/1000 dur:6220 rps:160.77
    //[INFO ]23:25:44.038 Final Result:1000/1000 dur:6032 rps:165.78
    //========JavaServer======
    //[INFO ]23:19:59.843 Final Result:1000/1000 dur:6074 rps:164.64
    //[INFO ]23:20:20.670 Final Result:1000/1000 dur:6122 rps:163.35
    @Test
    public void updateLargeReqSmallResp() throws InterruptedException {
        String id = "bdware.test/small";
        DigitalObject digitalObject = DoExample.large;
        digitalObject.id = id;
        testUpdate(serverAddr, 1000, digitalObject);
    }

    //[INFO ]14:05:38.304 Final Result:1000/1000 dur:3643 rps:274.50 (DoipClientTest.java:124)
    //[INFO ]18:08:24.324 Final Result:100/100 dur:151355 rps:0.66 (DoipClientTest.java:129)
    //=========From Client To Ali============
    //========GOServer======
    //[INFO ]22:54:11.173 Final Result:100/100 dur:146644 rps:0.68
    //========JavaServer======
    //[INFO ]22:43:17.428 Final Result:100/100 dur:147570 rps:0.68
    //=========From Ali To Ali============
    //========GOServer======
    //[INFO ]23:26:09.188 Final Result:1000/1000 dur:6702 rps:149.21
    //[INFO ]23:26:37.987 Final Result:1000/1000 dur:6873 rps:145.50
    //========JavaServer======
    //[INFO ]23:19:01.484 Final Result:1000/1000 dur:6870 rps:145.56
    //[INFO ]23:19:37.614 Final Result:1000/1000 dur:6672 rps:149.88
    @Test
    public void updateLargeReqLargeResp() throws InterruptedException {
        String id = "bdware.test/large";
        DigitalObject digitalObject = DoExample.large;
        digitalObject.id = id;
        testUpdate(serverAddr, 1000, digitalObject);
    }

    public void testUpdate(String addr, int totalCount, DigitalObject digitalObject) throws InterruptedException {
        DoipClientImpl doipClient = new DoipClientImpl();
        doipClient.connect(ClientConfig.fromUrl(addr));
        long start = System.currentTimeMillis();
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger correct = new AtomicInteger(0);
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
        int dur = (int) (System.currentTimeMillis() - start);
        LOGGER.info(String.format("Final Result:%d/%d dur:%d rps:%.2f", correct.get(), total.get(),
                dur, (correct.get() + 0.0D) * 1000.0D / (dur)));
    }

    static class TestResult {
        long dur;
        double rps;
        int total;
        int correct;

        public synchronized void merge(TestResult result) {
            rps += result.rps;
            total += result.total;
            correct += result.correct;
        }

        public String getResultStr() {
            return String.format("Final Result:%d/%d dur:%d rps:%.2f", correct, total,
                    dur, rps);
        }
    }

    public TestResult testRetrive(String addr, int totalCount, String id) throws InterruptedException {
        DoipClientImpl doipClient = new DoipClientImpl();
        doipClient.connect(ClientConfig.fromUrl(addr));
        doipClient.waitForConnected();
        long start = System.currentTimeMillis();
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger correct = new AtomicInteger(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < totalCount; i++) {
                    doipClient.retrieve(id, null, true, new DoipMessageCallback() {
                        @Override
                        public void onResult(DoipMessage msg) {
                            try {
                                DigitalObject digitalObject = DigitalObject.fromByteArray(msg.body.encodedData);
                                if (digitalObject != null) {
                                    String str = new String(digitalObject.elements.get(0).getData());
                                    //  String str = new String(msg.body.encodedData);
                                    if (str.startsWith("cold"))
                                        correct.incrementAndGet();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            total.incrementAndGet();
                        }
                    });
                }
            }
        }).start();

        int circle = 0;
        for (; total.get() < totalCount; ) {
            if (++circle % 100 == 0)
                LOGGER.info(String.format("%d/%d -> totalCount:%d", correct.get(), total.get(), totalCount));
            Thread.sleep(10);
        }
        int dur = (int) (System.currentTimeMillis() - start);

        TestResult result = new TestResult();
        result.correct = correct.get();
        result.total = total.get();
        result.rps = (correct.get() + 0.0D) * 1000.0D / (dur);
        result.dur = dur;
        LOGGER.info(addr + " " + result.getResultStr());
        return result;
    }

    public TestResult testRetriveWithClose(String addr, int totalCount, String id) throws InterruptedException {
        DoipClientImpl doipClient;
        long start = System.currentTimeMillis();
        final AtomicInteger total = new AtomicInteger(0);
        final AtomicInteger correct = new AtomicInteger(0);
        for (int i = 0; i < totalCount; i++) {
            doipClient = new DoipClientImpl();
            doipClient.connect(ClientConfig.fromUrl(addr));
            doipClient.waitForConnected();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            doipClient.retrieve(id, null, true, new DoipMessageCallback() {
                @Override
                public void onResult(DoipMessage msg) {
                    try {
                        DigitalObject digitalObject = DigitalObject.fromByteArray(msg.body.encodedData);
                        String str = new String(digitalObject.elements.get(0).getData());
                        //  String str = new String(msg.body.encodedData);
                        if (str.startsWith("cold"))
                            correct.incrementAndGet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    countDownLatch.countDown();
                    total.incrementAndGet();
                }
            });
            countDownLatch.await(2, TimeUnit.SECONDS);
            doipClient.close();
        }
        int circle = 0;
        for (; total.get() < totalCount; ) {
            if (++circle % 100 == 0)
                LOGGER.info(String.format("%d/%d", correct.get(), total.get()));
            Thread.sleep(10);
        }
        int dur = (int) (System.currentTimeMillis() - start);
        LOGGER.info(String.format("%s Final Result:%d/%d dur:%d rps:%.2f", serverAddr, correct.get(), total.get(),
                dur, (correct.get() + 0.0D) * 1000.0D / (dur)));
        TestResult result = new TestResult();
        result.correct = correct.get();
        result.total = total.get();
        result.rps = (correct.get() + 0.0D) * 1000.0D / (dur);
        result.dur = dur;
        return result;
    }
}
