/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.operations;

public enum  BasicOperations {
    Hello("0.DOIP/Op.Hello"),
    ListOps("0.DOIP/Op.ListOperations"),
    Retrieve("0.DOIP/Op.Retrieve"),
    Create("0.DOIP/Op.Create"),
    Update("0.DOIP/Op.Update"),
    Delete("0.DOIP/Op.Delete"),
    Search("0.DOIP/Op.Search"),
    Subscribe("0.DOIP/Op.Subscribe"),
    Publish("0.DOIP/Op.Publish"),
    Extension("0.DOIP/Op.Extension"),
    Unknown("0.DOIP/Op.Unknown");

    private final String name;

    BasicOperations(String displayName) {
        this.name = displayName;
    }

    public static BasicOperations getDoOp(String opStr) {
        for (BasicOperations op : BasicOperations.values()) {
            if (op.getName().equals(opStr)) {
                return op;
            }
        }
        return BasicOperations.Unknown;
    }

    public String getName() {
        return name;
    }
}
