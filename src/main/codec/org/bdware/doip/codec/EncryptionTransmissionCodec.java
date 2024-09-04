package org.bdware.doip.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.MessageCredential;
import org.bdware.doip.endpoint.CryptoManager;

import java.util.List;

/*
*
*
encode()
1.	get recipient’s identifier.For a client, it can get identifier of DOIP service through a web page or other way. For a DOIP service, it can get a client’s identifier from request message.
2.	resolve recipient’s identifier through Identifier/Resolution System (e.g. Handle System or another implementation) to get recipient’s public key (e.g. RSA2048)
3.	generate random symmetric key locally (e.g. AES256)
4.	encrypt message body using generated random symmetric key in step 3
5.	encrypt generated random symmetric key using recipient’s public key
6.	put encrypted message body into message body
7.	put encrypted symmetric key into message credential
8.	set encrypt flag (EC) to 1 in envelope
9.	sign message header and body using sender’s private key
10.	put signature into message credential
*
decode()
When received one encrypted DOIP message (EC flag is set to 1), follow these steps to process the message:
1.	get sender’s identifier from message credential
2.	resolve sender’s identifier to get sender’s public key
3.	verify signature using sender’s public key
4.	decrypt symmetric key using receiver’s private key
5.	decrypt message body using decrypted symmetric key
6.	process decrypted message body
*
*
* */
// signer should be lower than Encrypter
// This class is used in client side.
public class EncryptionTransmissionCodec<T, U> extends MessageToMessageCodec<DoipMessage, DoipMessage> {
    static Logger LOGGER = LogManager.getLogger(EncryptionTransmissionCodec.class);
    CryptoManager<T, U> keyRetriever;

    public EncryptionTransmissionCodec(CryptoManager<T, U> manager) {
        this.keyRetriever = manager;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DoipMessage msg, List<Object> out) throws Exception {
        try {
            T pubkey = keyRetriever.getPublicKey(msg);
            U encryptedKey = keyRetriever.getSymmetricKey(msg);
            if (msg.body.getEncodedData() != null && msg.body.getEncodedData().length > 0) {
                byte[] encryptedBody = keyRetriever.encryptUseSymmetricKey(msg.body.getEncodedData(), encryptedKey);
                msg.body.encodedData = encryptedBody;
                String encryptedSymmetricKey = keyRetriever.encryptSymmetricKey(encryptedKey, pubkey);
                if (msg.credential == null)
                    msg.credential = new MessageCredential((String) null);
                msg.credential.setAttributes("encryptedSymmetricKey", encryptedSymmetricKey);
                msg.header.setIsEncrypted(true);
            }
            keyRetriever.putPubKeyToCredential(msg, keyRetriever.getOwnKeyPair(msg));
            out.add(msg);
        } catch (Exception e) {
            LOGGER.debug("Ignore encrypted failed message:" + e.getMessage());
            e.printStackTrace();
        }
    }

    //client should decode the msg using its own pirvate key.
    @Override
    protected void decode(ChannelHandlerContext ctx, DoipMessage msg, List<Object> out) throws Exception {
        //decrypt
        try {
            if (!msg.header.isEncrypted()) {
                out.add(msg);
                return;
            }
            T mykey = keyRetriever.getOwnKeyPair(msg);
            String encryptedSymmetricKey = msg.credential.getAttriburte("encryptedSymmetricKey").getAsString();
            U symmetricKey = keyRetriever.decryptSymmetricKey(encryptedSymmetricKey, mykey);
            byte[] decyrpted = keyRetriever.decryptUseSymmetricKey(msg.body.getEncodedData(), symmetricKey);
            msg.body.encodedData = decyrpted;
            out.add(msg);
        } catch (Exception e) {
            LOGGER.debug("Ignore decrypt failed message:" + e.getMessage());
            e.printStackTrace();
        }
    }
}
