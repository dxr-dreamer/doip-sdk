/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.controlgroup;

public class HttpTestClient {
    public static void main(String[] args) throws Exception {
        // tcp://39.104.208.148:21042 1000 "bdware.test/small"
        if (args.length < 3) {
            System.out.println("Usage:\n tcp://39.104.208.148:21042 1000 bdware.test/small largeorempty");
        }
        if (args.length >= 4 && args[3].startsWith("large")) {
            new HttpServerTest().testClient(args[0], Integer.valueOf(args[1]), args[2], HttpServerTest.longStr);
            System.out.println("Request: Large, Response:" + args[2]);
        } else {
            new HttpServerTest().testClient(args[0], Integer.valueOf(args[1]), args[2], "");
            System.out.println("Request: Small, Response:" + args[2]);
        }
    }
}
