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

public abstract class NettyDoipListener implements DoipListener, StartServerResultSender {
    protected NettyServerHandler handler;
    protected DoipListenerConfig listenerConfig;
    protected StartServerCallback startServerCallback;

    @Override
    public void setRequestHandler(DoipRequestHandler handler) {
        this.handler = new NettyServerHandler(handler, listenerConfig);
    }

    public void setStartServerResultCallback(StartServerCallback cb) {
        this.startServerCallback = cb;
    }


    public abstract void start();

    public abstract void stop();

}

