package org.bdware.doip.endpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RecvCounter extends Thread {
    static Logger LOGGER = LogManager.getLogger(RecvCounter.class);
    AtomicLong i;
    String tag;


    public RecvCounter(String tag) {
        this.tag = tag;
        i = new AtomicLong(0);
    }

    @Override
    public void run() {
        for (; ; ) {
            long pre = System.currentTimeMillis();
            long preCount = i.get();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            LOGGER.info(tag + " RECV:" + i.get() + " deltaTPS:" + (i.get() - preCount) * 1000L / (System.currentTimeMillis() - pre));
        }
    }

    public void inc() {
        i.incrementAndGet();
    }

    public void set(int size) {
        i.set(size);
    }
}
