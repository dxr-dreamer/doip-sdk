package org.bdware.doip.endpoint;

import org.bdware.doip.codec.doipMessage.DoipMessage;

public interface CryptoManager<T, U> {
    public U getSymmetricKey(DoipMessage message);

    public T getPublicKey(DoipMessage message);

    public T getOwnKeyPair(DoipMessage message);

    byte[] encryptUseSymmetricKey(byte[] encodedData, U symmetricKey);

    String encryptSymmetricKey(U symmetricKey, T publicKey);

    U decryptSymmetricKey(String encryptedSymmetricKey, T mykey);

    byte[] decryptUseSymmetricKey(byte[] encodedData, U symmetricKey);

    void putPubKeyToCredential(DoipMessage message, T publicKey);

    T getPubKeyFromCredential(DoipMessage message);
}
