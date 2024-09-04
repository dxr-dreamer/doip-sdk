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

public class DOWithOwner extends DigitalObject{

    public static final String DO_OWNER = "doOwner";

    public static DOWithOwner fromDO(DigitalObject digitalObject){
        if(digitalObject.attributes == null || digitalObject.attributes.get(DO_OWNER) == null){
            return null;
        }
        return (DOWithOwner)digitalObject;
    }

    public DOWithOwner(String id, DoType type) {
        super(id, type);
    }

    public void setDoOwner(String ownerID){
        addAttribute(DO_OWNER,ownerID);
    }

    public String getDoOwner(){
        if(attributes.get(DO_OWNER) == null) return null;
        return attributes.get(DO_OWNER).getAsString();
    }
}
