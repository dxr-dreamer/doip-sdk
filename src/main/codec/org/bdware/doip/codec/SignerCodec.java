package org.bdware.doip.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageSigner;

import java.util.List;

public class SignerCodec extends MessageToMessageCodec<DoipMessage, DoipMessage> {
    DoipMessageSigner signer;
    static Logger LOGGER = LogManager.getLogger(SignerCodec.class);

    public SignerCodec(DoipMessageSigner signer) {
        this.signer = signer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, DoipMessage msg, List<Object> out) throws Exception {
        signer.signMessage(msg);
        out.add(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DoipMessage msg, List<Object> out) throws Exception {
        if (!msg.header.isCertified()) {
            out.add(msg);
            return;
        }
        boolean verifyResult = signer.verifyMessage(msg);
        if (verifyResult) {
            out.add(msg);
        } else {
            LOGGER.error("verify Failed!! ignore msg!");
        }
    }
}