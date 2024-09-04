/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint;

import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.doipMessage.DoipMessageFactory;
import org.bdware.doip.codec.doipMessage.DoipResponseCode;
import org.bdware.doip.endpoint.server.RepositoryHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class TestRepoHandler implements RepositoryHandler {
    public AtomicInteger count = new AtomicInteger(0);

    @Override
    public DoipMessage handleHello(DoipMessage request) {
        return null;
    }

    @Override
    public DoipMessage handleListOps(DoipMessage request) {
        return null;
    }

    @Override
    public DoipMessage handleCreate(DoipMessage request) {
         return handle(request);
    }

    @Override
    public DoipMessage handleUpdate(DoipMessage request) {
        return handle(request);
    }

    @Override
    public DoipMessage handleDelete(DoipMessage request) {
        return null;
    }

    @Override
    public DoipMessage handleRetrieve(DoipMessage request) {
        return handle(request);
    }

    private DoipMessage handle(DoipMessage request) {
        DoipMessageFactory.DoipMessageBuilder builder = new DoipMessageFactory.DoipMessageBuilder();
        DoipMessageFactory.DoipMessageBuilder resp = builder.createResponse(DoipResponseCode.Success, request);

        resp.setRequestID(request.requestID);
        if (request.header.parameters.id.contains("small")) {
            resp.setBody(DoExample.small);
        } else {
            resp.setBody(DoExample.large);
        }
        
        count.incrementAndGet();
        DoipMessage msg = resp.create();
        assert msg.requestID == request.requestID;
        return msg;
    }
}