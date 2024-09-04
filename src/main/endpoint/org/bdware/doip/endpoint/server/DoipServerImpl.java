/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.endpoint.EndpointFactory;

import java.util.ArrayList;
import java.util.List;

public class DoipServerImpl implements DoipServer {
    static Logger logger = LogManager.getLogger(DoipServerImpl.class);

    List<DoipListener> listeners;
    protected DoipServiceInfo serviceInfo;
    List<ListenerContainer> containers;
    DoipRequestHandler requestCallback;

    public DoipServerImpl(DoipServiceInfo info) {
        this.serviceInfo = info;
        listeners = new ArrayList<>();
        for (DoipListenerConfig lsnrInfo : info.listenerInfos) {
            DoipListener listener = EndpointFactory.createDoipListener(lsnrInfo);
            listeners.add(listener);
        }
        containers = new ArrayList<>();
    }

    public static DoipServer createDoipServer(DoipServiceInfo info) {
        DoipServerImpl server = new DoipServerImpl(info);
        return server;
    }

    @Override
    public void start() {
        start(null);
    }

    public void start(StartServerCallback cb) {
        logger.info("DOIPServiceInfo: " + serviceInfo.toJson());
        for (DoipListener listener : listeners) {
            listener.setRequestHandler(requestCallback);
            if (listener instanceof StartServerResultSender && cb != null) {
                ((StartServerResultSender) listener).setStartServerResultCallback(cb);
            }
            ListenerContainer container = new ListenerContainer(listener);
            containers.add(container);
            container.start();
        }
    }

    @Override
    public void stop() {
        logger.info("Try to stop listeners:");
        for (DoipListener listener : listeners) {
            listener.stop();
        }
        for (ListenerContainer c : containers) {
            c.interrupt();
        }
    }

    @Override
    public void setRepositoryHandler(RepositoryHandler handler) {
        requestCallback = new RequestHandlerImpl(handler);
    }

    public void setRequestCallback(DoipRequestHandler requestCallback) {
        this.requestCallback = requestCallback;
    }

    public DoipRequestHandler getRequestCallback() {
        return this.requestCallback;
    }


    public class ListenerContainer extends Thread {
        DoipListener listener;

        ListenerContainer(DoipListener l) {
            this.listener = l;
        }

        @Override
        public void run() {
            listener.start();
        }
    }

}
