/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.event;

import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.endpoint.server.Op;

public interface TopicHandler {

    @Op(op = BasicOperations.Publish)
    DoipMessage handlePublish(DoipMessage request);

    @Op(op = BasicOperations.Subscribe)
    DoipMessage handleSubscribe(DoipMessage request);

}
