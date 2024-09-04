package org.bdware.doip.endpoint.tcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadDoipClientTest {
    static Logger LOGGER = LogManager.getLogger(MultiThreadDoipClientTest.class);
    static int threadCount = 1000;
    static int reqCount = 30;
    static String serverAddr = "tcp://127.0.0.1:21042";
    static Executor executor = Executors.newFixedThreadPool(threadCount);

    public static void main(String[] args) {
        if (args.length > 0) {
            serverAddr = args[0];
            threadCount = Integer.valueOf(args[1]);
            reqCount = Integer.valueOf(args[2]);
        }
        new MultiThreadDoipClientTest().run();
    }

    @Test
    public void testOnce() {
        main(new String[]{
                "tcp://39.104.200.95:21042",
                "1000",
                "30000"
        });
    }

    @Test
    public void run() {
        DoipClientTest.TestResult total = new DoipClientTest.TestResult();
        AtomicInteger counter = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    DoipClientTest test = new DoipClientTest();
                    try {
                        DoipClientTest.TestResult result = test.testRetrive(serverAddr, reqCount, "bdware.test/small");
                        //  DoipClientTest.TestResult result = test.testRetriveWithClose(test.serverAddr, reqCount, "bdware.test/small");
                        total.merge(result);
                    } catch (InterruptedException e) {

                    }
                    counter.getAndIncrement();

                }
            });
        }

        for (; counter.get() < threadCount; ) {
            Thread.yield();
        }
        total.dur = System.currentTimeMillis() - start;
        LOGGER.info(total.getResultStr());
        for (; ; ) ;
    }
}
