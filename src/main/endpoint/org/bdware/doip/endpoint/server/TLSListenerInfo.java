/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint.server;


import java.io.File;

public class TLSListenerInfo extends DoipListenerConfig {

    private transient final File chainKeyFile;
    private transient final File keyFile;

    public TLSListenerInfo(String url, String protocolVersion, File chainKeyFile, File keyFile) {
        super(url, protocolVersion);
        this.chainKeyFile = chainKeyFile;
        this.keyFile = keyFile;
    }

    public File getChainKeyFile() {
        return chainKeyFile;
    }

    public File getKeyFile() {
        return keyFile;
    }

}
