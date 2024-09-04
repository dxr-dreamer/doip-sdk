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

import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.doipMessage.DoipMessage;
import org.bdware.doip.codec.exception.DoipConnectException;
import org.bdware.doip.codec.metadata.SearchParameter;
import org.bdware.doip.codec.operations.BasicOperations;
import org.bdware.doip.endpoint.server.Op;

public interface DoipClient {

    @Op(op = BasicOperations.Hello)
    void hello(String id, DoipMessageCallback cb);

    @Op(op = BasicOperations.ListOps)
    void listOperations(String id, DoipMessageCallback cb);

    @Op(op = BasicOperations.Retrieve)
    void retrieve(String id, String element, boolean includeElementData, DoipMessageCallback cb);

    @Op(op = BasicOperations.Create)
    void create(String targetDoipService, DigitalObject digitalObject, DoipMessageCallback cb);

    @Op(op = BasicOperations.Update)
    void update(DigitalObject digitalObject, DoipMessageCallback cb);

    @Op(op = BasicOperations.Delete)
    void delete(String id, DoipMessageCallback cb);

    @Op(op = BasicOperations.Search)
    void search(String id, SearchParameter sp, DoipMessageCallback cb);

    void sendRawMessage(DoipMessage msg, DoipMessageCallback cb);

    void close();

    void connect(ClientConfig url);

    void reconnect() throws DoipConnectException;

    String getRepoUrl();

    /*
        return the identifier of the repository/registry owner
        should be a id of DO user
     */
    String getRecipientID();

    void setRecipientID(String recipientID);

    boolean isConnected();

}
