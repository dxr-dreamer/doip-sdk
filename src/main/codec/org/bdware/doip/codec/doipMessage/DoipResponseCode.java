/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare doip sdk] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.doipMessage;

public enum DoipResponseCode {
    Success("0.DOIP/Status.001"),
    Invalid("0.DOIP/Status.101"),
    UnAuth_Client("0.DOIP/Status.102"),
    UnAuth_Op("0.DOIP/Status.103"),
    DoNotFound("0.DOIP/Status.104"),
    DoAlreadyExist("0.DOIP/Status.105"),
    Declined("0.DOIP/Status.200"),
    TLSRequired("0.DOIP/Status.201"),
    DelegateRequired("0.DOIP/Status.303"),

    MoreThanOneErrors("0.DOIP/Status.500"),
    UnKnownError("0.DOIP/Status.999");
    private final String name;

    DoipResponseCode(String displayName) {
        this.name = displayName;
    }

    public static DoipResponseCode getDoResponse(String respStr){
        for (DoipResponseCode resp : DoipResponseCode.values()) {
            if (resp.getName().equals(respStr)) {
                return resp;
            }
        }
        return DoipResponseCode.UnKnownError;
    }

    public String getName() {
        return name;
    }
}