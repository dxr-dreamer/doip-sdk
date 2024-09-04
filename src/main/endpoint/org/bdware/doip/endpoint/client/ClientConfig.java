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

import io.netty.channel.ChannelPipeline;
import org.bdware.doip.codec.DOIPVersion;
import org.bdware.doip.codec.DoipMessagePrinter;
import org.bdware.doip.codec.EncryptionTransmissionCodec;
import org.bdware.doip.codec.SignerCodec;
import org.bdware.doip.codec.doipMessage.DoipMessageSigner;
import org.bdware.doip.endpoint.CryptoManager;

import java.net.URI;
import java.net.URISyntaxException;

public class ClientConfig {
    public String url;
    public String protocolVersion;

    public int timeoutSeconds= 5;
    private boolean debugPrint;
    private transient DoipMessageSigner signer;
    private transient CryptoManager cryptoManager;

    public ClientConfig(String url) {
        this(url, DOIPVersion.V2_1);
    }

    public ClientConfig(String url, String protocolVersion) {
        this.url = url;
        this.protocolVersion = protocolVersion;
    }

    public ClientConfig setTimeoutSeconds(int ts){
        this.timeoutSeconds = ts;
        return this;
    }

    public ClientConfig setSigner(DoipMessageSigner signer) {
        this.signer = signer;
        return this;
    }

    public ClientConfig setCryptoManager(CryptoManager manager) {
        assert signer != null;
        this.cryptoManager = manager;
        return this;
    }

    public ClientConfig setDebugPrint(boolean b) {
        debugPrint = b;
        return this;
    }

    public static ClientConfig fromUrl(String url) {
        return fromUrl(url, DOIPVersion.V2_1);
    }

    public static ClientConfig fromUrl(String url, String protocolVersion) {
        URI uri = null;
        try {
            uri = new URI(url);
            if (uri.getScheme().toLowerCase().equals("tls")) {
                ClientConfig ret = TLSClientConfig.getDefault(url);
                ret.protocolVersion = protocolVersion;
                return ret;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new ClientConfig(url, protocolVersion);
    }

    public void addSignerAndEncryptionTransmission(ChannelPipeline pipline) {
        if (debugPrint)
            pipline.addLast(new DoipMessagePrinter());
        if (signer != null) {
            pipline.addLast(new SignerCodec(signer));
        }
        if (cryptoManager != null) {
            pipline.addLast(new EncryptionTransmissionCodec<>(cryptoManager));
        }
        return;
    }
}
