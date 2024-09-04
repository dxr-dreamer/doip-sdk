/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bdware.doip.codec.doipMessage.MessageEnvelope;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class BitTest {
    Logger logger = LogManager.getLogger(BitTest.class);
    @Test
    public void testEnvelope(){
        MessageEnvelope envelope = MessageEnvelope.createResendMessage(0,0);
//        logger.debug(Integer.toBinaryString(envelope.flag));
//        envelope.setResend();
//        logger.debug(Integer.toBinaryString(envelope.flag));
//        envelope.setTruncated();
//        logger.debug(Integer.toBinaryString(envelope.flag));

        logger.debug(envelope.isResend());
        logger.debug(envelope.isTruncated());
    }

    @Test
    public void testStringLength(){

        String a = "he我";
        logger.info(a.length());
        logger.info(a.getBytes(StandardCharsets.UTF_8).length);
    }

    @Test
    public void testShortInt(){
        int flag = 0;
        flag = (flag | 0x80000000);
        assert(flag<0);
        printIntAsBits(flag);

        int flag1 = 0;
        flag1 = (flag1 | 0x80000000);
        flag1 = (flag1 | 0x40000000);
        printIntAsBits(flag1);
        assert((flag1<< 1)<0);
        System.out.println(Integer.toBinaryString(flag1));

    }

    public void printIntAsBits(int t){
        for(int i=0;i<Integer.SIZE;i++){
            System.out.print(t<0?1:0);
            t = t<<1;
        }
        System.out.println();
    }
}

