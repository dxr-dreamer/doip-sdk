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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class TLSClientConfig extends ClientConfig {
    public final TrustManager[] managers;
    static Logger LOGGER = LogManager.getLogger(TLSClientConfig.class);

    public TLSClientConfig(String url, TrustManager[] managers) {
        super(url);
        this.managers = managers;
    }

    public static ClientConfig getDefault(String url) {
        TrustManager DUMMY_TRUST_MANAGER = new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                LOGGER.debug(
                        "[TLSClientConfig] UNKNOWN CLIENT CERTIFICATE: " + chain[0].getSubjectDN());
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                LOGGER.debug("UNKNOWN SERVER CERTIFICATE: " + chain[0].getSubjectDN());
            }
        };
        TLSClientConfig ret = new TLSClientConfig(url, new TrustManager[]{DUMMY_TRUST_MANAGER});
        return ret;
    }
}
