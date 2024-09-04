/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.DOIPVersion;
import org.bdware.doip.endpoint.client.*;
import org.bdware.doip.endpoint.server.*;
import org.bdware.doip.endpoint.v2_0.DOIPV2ClientChannel;
import org.bdware.doip.endpoint.v2_0.DOIPV2DoipListener;

import java.net.URI;
import java.util.HashMap;

public class EndpointFactory {
    static Logger LOGGER = LogManager.getLogger(NettyDoipListener.class);
    static HashMap<String, DoipListener> adhocListeners = new HashMap<>();
    static HashMap<String, DoipClientChannel> adhocClient = new HashMap<>();

    static class V2_1 {
        public static DoipListener createDoipListener(DoipListenerConfig listenerInfo) {
            try {
                URI uri = new URI(listenerInfo.url);
                LOGGER.debug("[URI Parse]scheme:" + uri.getScheme() + "; host: " + uri.getHost() + "  port: " + uri.getPort());
                switch (uri.getScheme().toLowerCase()) {
                    case "udp":
                        return new NettyUDPDoipListener(uri.getPort(), listenerInfo);
                    case "tcp":
                        return new NettyTCPDoipListener(uri.getPort(), listenerInfo);
                    case "tls":
                        TLSListenerInfo tlinfo = (TLSListenerInfo) listenerInfo;
                        return new NettyTLSDoipListener(uri.getPort(), tlinfo);
                    case "ws":
                        return new NettyWSDoipListener(uri.getPort(), uri.getPath(), listenerInfo);
                    default:
                        if (adhocListeners.get(uri.getScheme()) != null) return adhocListeners.get(uri.getScheme());
                        LOGGER.error("[Create repository error] Unsupported Listener: " + new Gson().toJson(listenerInfo));
                        return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static DoipClientChannel createDoipClientChannel(ClientConfig config) {
            try {
                URI uri = new URI(config.url);
                LOGGER.debug("[URI Parse]scheme:" + uri.getScheme() + "host: " + uri.getHost() + "  port: " + uri.getPort());
                switch (uri.getScheme().toLowerCase()) {
                    case "udp":
                        return new NettyDoipUDPClientChannel();
                    case "tcp":
                        return new NettyDoipTCPClientChannel(config);
                    case "tls":
                        TLSClientConfig tlsClientConfig = (TLSClientConfig) config;
                        return new NettyDoipTLSClientChannel(tlsClientConfig.managers);
                    case "ws":
                        return new NettyDoipWSClientChannel();
                    default:
                        if (adhocClient.get(uri.getScheme()) != null) {
                            return adhocClient.get(uri.getScheme());
                        }
                        LOGGER.error("[Create repository error] Unsupported URL: " + config.url);
                        return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    static class V2_0 {
        public static DoipListener createDoipListener(DoipListenerConfig listenerInfo) {
            try {
                URI uri = new URI(listenerInfo.url);
//                LOGGER.debug("[URI Parse]scheme:" + uri.getScheme() + "; host: " + uri.getHost() + "  port: " + uri.getPort());
                switch (uri.getScheme()) {
                    case "tls":
                    case "TLS":
                        TLSListenerInfo tlinfo = (TLSListenerInfo) listenerInfo;
                        return new DOIPV2DoipListener(uri.getPort(),tlinfo);
                    default:
                        if (adhocListeners.get(uri.getScheme()) != null) return adhocListeners.get(uri.getScheme());
                        LOGGER.error("[Create repository error] Unsupported Listener: " + new Gson().toJson(listenerInfo));
                        return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public static DoipClientChannel createDoipClientChannel(ClientConfig config) {
            try {
                URI uri = new URI(config.url);
                LOGGER.debug("[URI Parse]scheme:" + uri.getScheme() + "host: " + uri.getHost() + "  port: " + uri.getPort());
                switch (uri.getScheme()) {
                    case "tls":
                    case "TLS":
                        TLSClientConfig tlsClientConfig = (TLSClientConfig) config;
                        return new DOIPV2ClientChannel(tlsClientConfig.managers);
                    default:
                        if (adhocClient.get(uri.getScheme()) != null) {
                            return adhocClient.get(uri.getScheme());
                        }
                        LOGGER.error("[Create repository error] Unsupported URL: " + config.url);
                        return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static DoipListener createDoipListener(DoipListenerConfig listenerInfo) {
        switch (listenerInfo.protocolVersion) {
            case DOIPVersion.V2_1:
                return V2_1.createDoipListener(listenerInfo);
            case DOIPVersion.V2_0:
                return V2_0.createDoipListener(listenerInfo);
            default:
                LOGGER.error("[Create repository error] Unsupported version: " + listenerInfo.protocolVersion);
                return null;
        }
    }

    public static DoipClientChannel createDoipClientChannel(ClientConfig config) {
        switch (config.protocolVersion) {
            case DOIPVersion.V2_1:
                return V2_1.createDoipClientChannel(config);
            case DOIPVersion.V2_0:
                return V2_0.createDoipClientChannel(config);
            default:
                LOGGER.error("[Create client error] Unsupported version: " + config.protocolVersion);
                return null;
        }
    }

    public static void addDoipListener(String schema, DoipListener listener) {
        adhocListeners.put(schema, listener);
    }


    public static void addClientChannel(String schema, DoipClientChannel channel) {
        adhocClient.put(schema, channel);
    }
}