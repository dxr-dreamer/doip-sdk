/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.client;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ResponseWait {
    static Logger LOGGER = LogManager.getLogger(ResponseWait.class);
    public final static HashedWheelTimer HASHED_WHEEL_TIMER = new HashedWheelTimer(r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    }, 5, TimeUnit.MILLISECONDS, 2);
    // RecvCounter counter = new RecvCounter("(ResponseWait.java:84)");

    public ResponseWait() {
        //   counter.start();
    }

    // use static map to ensure requestid is !UNIC!
    // in a client(With multiple connection)
    final static Map<Integer, Pair<DoipMessageCallback, Timeout>> waitObj = new ConcurrentHashMap<>();

    public static class Pair<T, U> {
        final T first;
        final U second;

        Pair(T t, U u) {
            this.first = t;
            this.second = u;
        }
    }

    public void wakeUpAndRemove(int requestID, DoipMessage result) {
        Pair<DoipMessageCallback, Timeout> ob = getAndRemove(requestID);
        wakeup(ob, result);
    }

    //DO NOT use synchronized like "private synchronized ..."
    //Because the waitObj is static
    private Pair<DoipMessageCallback, Timeout> getAndRemove(int requestID) {
        Pair<DoipMessageCallback, Timeout> ob = waitObj.remove(requestID);
        if (ob != null && ob.second != null) {
            ob.second.cancel();
            return ob;
        }
        return null;
    }

    public boolean waitResponseFor5Secs(final int requestID, DoipMessageCallback cb) {
        return waitResponse(requestID, cb, 5);
    }

    public boolean waitResponse(int requestID, DoipMessageCallback cb, int seconds) {
        TimerTask tt = timeout -> {
            DoipMessage timeOutMessage = DoipMessageFactory.createTimeoutResponse(requestID, "[ResponseWait.waitResponse]");
            wakeUpAndRemove(requestID, timeOutMessage);
            // TODO: 修复潜在的bug
            // 如果requestID重复，可能会移除未处理的回调。
        };
        Timeout timeout = HASHED_WHEEL_TIMER.newTimeout(tt, seconds, TimeUnit.SECONDS);
        Pair<DoipMessageCallback, Timeout> value = new Pair<>(cb, timeout);
        Pair<DoipMessageCallback, Timeout> oldValue = waitObj.putIfAbsent(requestID, value);
        if (oldValue == null) {
            return true;
        } else {
            LOGGER.debug("Response Wait 返回false，再次尝试！");
            timeout.cancel();
            return false;
        }
    }

    public void wakeup(int requestID, DoipMessage result) {
        Pair<DoipMessageCallback, Timeout> ob = waitObj.get(requestID);
        wakeup(ob, result);
    }

    public void wakeup(Pair<DoipMessageCallback, Timeout> ob, DoipMessage result) {
        if (ob != null) {
            ob.second.cancel();
            ob.first.onResult(result);
        } else {
//            LOGGER.info(total.incrementAndGet() + "Try to wakeup empty callback, maybe return too late:" + new Gson().toJson(result));
        }
    }

    static AtomicInteger total = new AtomicInteger(0);
}
