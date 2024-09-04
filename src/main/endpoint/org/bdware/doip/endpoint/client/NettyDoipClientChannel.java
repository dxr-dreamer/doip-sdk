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

import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;

public abstract class NettyDoipClientChannel implements DoipClientChannel {
    static Logger logger = LogManager.getLogger(NettyDoipClientChannel.class);
    protected NettyDoipClientHandler handler;
    protected InetSocketAddress remoteAddress;
    protected boolean isConnected = false;
    protected Channel channel;

    protected int timeoutSecond = 5;


    @Override
    public void sendMessage(DoipMessage message, DoipMessageCallback cb) {
        if (handler == null) {
            logger.error("client handler not set yet");
            return;
        }
        if (!isConnected) {
            logger.error("client not connected, connect first!");
        }
        logger.debug("channel send message");
        handler.sendMessage(message, cb, timeoutSecond);
    }

    @Override
    abstract public void connect(String url) throws URISyntaxException, InterruptedException;

    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isActive();
    }

    @Override
    public void setTimeoutSecond(int ts){
        this.timeoutSecond = ts;
    }

}
