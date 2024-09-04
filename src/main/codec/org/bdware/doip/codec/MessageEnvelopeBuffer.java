/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;

import java.net.InetSocketAddress;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class MessageEnvelopeBuffer {
    private PriorityQueue<MessageEnvelope> queue;
    private Set<Integer> toReceive;
    private InetSocketAddress sender;
    private static Logger LOGGER = LogManager.getLogger(MessageEnvelope.class);
    private long lastUpdate;

    public MessageEnvelopeBuffer(int requestId) {
        queue = new PriorityQueue<MessageEnvelope>(new Comparator<MessageEnvelope>() {
            @Override
            public int compare(MessageEnvelope o1, MessageEnvelope o2) {
                return o1.sequenceNumber - o2.sequenceNumber;
            }
        });
    }

    public synchronized void add(MessageEnvelope msg) {
        lastUpdate = System.currentTimeMillis();
        if (toReceive == null) {
            toReceive = new HashSet<>();
            for (int i = 0; i < msg.totalNumber; i++)
                toReceive.add(i);
            sender = msg.getSender();
        }
        if (toReceive.contains(msg.sequenceNumber)) {
            queue.add(msg);
            toReceive.remove(msg.sequenceNumber);
        }
    }

    public ByteBuf toByteBuf() {
        CompositeByteBuf ret = Unpooled.compositeBuffer();
        int len = 0;
        for (; !queue.isEmpty(); ) {
            len += queue.peek().contentLength;
            ret.addComponent(Unpooled.wrappedBuffer(queue.poll().content));
        }
        ret.setIndex(0, len);
        return ret;
    }

    public boolean isComplete() {
        return (queue.size() > 0 && queue.peek().totalNumber == queue.size());
    }

    public InetSocketAddress getSender() {
        return sender;
    }
}
