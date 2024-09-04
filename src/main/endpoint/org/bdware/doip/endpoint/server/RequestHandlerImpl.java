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

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.operations.BasicOperations;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestHandlerImpl implements DoipRequestHandler {

    Map<String, Method> handlers;
    static Logger logger = LogManager.getLogger(NettyServerHandler.class);
    protected RepositoryHandler doipHandler;

    public RequestHandlerImpl(RepositoryHandler doipHandler) {
        handlers = new HashMap<>();
        this.doipHandler = doipHandler;
        Class handlerClass = doipHandler.getClass();

        while (handlerClass != Object.class) {
            putDoipHandlerMethod(handlerClass);
            Class[] interfaces = handlerClass.getInterfaces();
            for (Class clz : interfaces) {
                putDoipHandlerMethod(clz);
            }
            handlerClass = handlerClass.getSuperclass();
        }
    }

    private void putDoipHandlerMethod(Class handlerClass) {
        Method[] methods = handlerClass.getDeclaredMethods();
        for (Method m : methods) {
            Op a = m.getAnnotation(Op.class);
            if (a != null) {
//                logger.debug("method annotation: " + a.op().getName());
//                logger.debug("method annotation: " + a.name());
                if (a.op() != BasicOperations.Extension) {
                    putHandler(a.op().getName(), m);
                } else {
                    putHandler(a.name(), m);
                }
            }
        }
    }

    private void putHandler(String name, Method m) {
        if (handlers.containsKey(name)) {
//            logger.error("Duplicated operation handler:" + name + " methodName:" + m.getName());
            return;
        }
        logger.debug("[Register operation] name: " + name);
        m.setAccessible(true);
        handlers.put(name, m);
    }

    @Override
    public DoipMessage onRequest(ChannelHandlerContext ctx, DoipMessage msg) {
        String str = msg.header.parameters.operation;
        logger.debug("[Call operation] name: " + str);
        if (str != null) {
            Method m;
            m = handlers.get(str);
            if (m == null) m = handlers.get(BasicOperations.Unknown.getName());
            if (m != null) {
                try {
                    return (DoipMessage) m.invoke(doipHandler, msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
