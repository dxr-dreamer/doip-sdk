/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.digitalObject;

public enum DoType {
    DO("0.TYPE/DO"),
    DOList("0.TYPE/DOList"),
    Metadata("0.TYPE/DO.Metadata"),
    DOIPServiceInfo("0.TYPE/DO.DOIPServiceInfo"),
    DOIPOperation("0.TYPE/DO.DOIPOperation"),
    UnKnown("0.TYPE/UnKnow");

    private final String name;

    DoType(String displayName) {
        this.name = displayName;
    }

    public static DoType getDoType(String typeStr){
        for (DoType type : DoType.values()) {
            if (type.getName().equals(typeStr)) {
                return type;
            }
        }
        return DoType.UnKnown;
    }

    public String getName() {
        return name;
    }
}