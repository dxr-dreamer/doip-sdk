package org.bdware.doip.codec.doipMessage;

public interface DoipMessageSigner {
    public boolean verifyMessage(DoipMessage doipMessage);

    public void signMessage(DoipMessage doipMessage);
}
