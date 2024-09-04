package org.bdware.doip.endpoint.server;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.MessageCredential;
import org.bdware.doip.endpoint.CryptoManager;

public class EncryptionTransmissionHandler<T, U> implements DoipRequestHandler {
    static Logger LOGGER = LogManager.getLogger(EncryptionTransmissionHandler.class);
    CryptoManager<T, U> cryptoManager;
    DoipRequestHandler wrapped;

    public EncryptionTransmissionHandler(CryptoManager<T, U> manager, DoipRequestHandler handler) {
        this.cryptoManager = manager;
        this.wrapped = handler;
    }

    @Override
    public DoipMessage onRequest(ChannelHandlerContext ctx, DoipMessage msg) {
        if (msg.header.isEncrypted()) {
            T mykey = cryptoManager.getOwnKeyPair(msg);
            String encryptedSymmetricKey = msg.credential.getAttriburte("encryptedSymmetricKey").getAsString();
            U symmetricKey = cryptoManager.decryptSymmetricKey(encryptedSymmetricKey, mykey);
            byte[] decyrpted = cryptoManager.decryptUseSymmetricKey(msg.body.getEncodedData(), symmetricKey);
            msg.body.encodedData = decyrpted;
        }
        DoipMessage result = wrapped.onRequest(ctx, msg);

        T pubkey = cryptoManager.getPubKeyFromCredential(msg);
        U encryptedKey = cryptoManager.getSymmetricKey(result);
        if (result.body.getEncodedData() != null && result.body.getEncodedData().length > 0) {
            byte[] encryptedBody = cryptoManager.encryptUseSymmetricKey(result.body.getEncodedData(), encryptedKey);
            result.body.encodedData = encryptedBody;
            String encryptedSymmetricKey = cryptoManager.encryptSymmetricKey(encryptedKey, pubkey);
            if (result.credential == null)
                result.credential = new MessageCredential((String) null);
            result.credential.setAttributes("encryptedSymmetricKey", encryptedSymmetricKey);
            result.header.setIsEncrypted(true);
        }

        return result;
    }
}
