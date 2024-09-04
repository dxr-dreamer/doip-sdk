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


import io.netty.channel.ChannelPipeline;
import org.bdware.doip.codec.DoipMessagePrinter;
import org.bdware.doip.codec.EncryptionTransmissionCodec;
import org.bdware.doip.codec.SignerCodec;
import org.bdware.doip.codec.doipMessage.DoipMessageSigner;
import org.bdware.doip.endpoint.CryptoManager;

public class DoipListenerConfig {
    public String url;
    public String protocolVersion;
    private transient DoipMessageSigner signer;
    private transient CryptoManager cryptoManager;
    private boolean debugPrint;

    public DoipListenerConfig(String url, String protocolVersion) {
        this.url = url;
        this.protocolVersion = protocolVersion;
    }

    public DoipListenerConfig setSigner(DoipMessageSigner signer) {
        assert signer != null;
        this.signer = signer;
        return this;
    }

    public DoipListenerConfig setDebugPrint(boolean b) {
        debugPrint = b;
        return this;
    }

    //Should set Signer before set cryptoManager
    public DoipListenerConfig setCryptoManager(CryptoManager cryptoManager) {
        assert signer != null;
        assert cryptoManager != null;
        this.cryptoManager = cryptoManager;
        return this;
    }

    public void addExtraCodec(ChannelPipeline pipline) {
        if (debugPrint)
            pipline.addLast(new DoipMessagePrinter());
        if (signer != null) {
            pipline.addLast(new SignerCodec(signer));
        }
    }

    public DoipRequestHandler wrapEncHandlerIfNeeded(DoipRequestHandler handler) {
        if (cryptoManager != null)
            return new EncryptionTransmissionHandler<>(cryptoManager, handler);
        else return handler;
    }
}