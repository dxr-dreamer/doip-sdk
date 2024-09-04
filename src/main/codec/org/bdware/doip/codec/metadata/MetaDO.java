/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.codec.metadata;

import org.bdware.doip.codec.digitalObject.DOWithMetadata;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.digitalObject.DoType;

public class MetaDO extends DOWithMetadata {

    public MetaDO(String id){
        super(id,DoType.Metadata);
    }

    public static MetaDO fromDO(DigitalObject originalDO){
        MetaDO meta = new MetaDO(originalDO.id);
        if(originalDO.attributes == null || originalDO.attributes.get(METADATA) == null){
            return null;
        }
        meta.addAttribute(METADATA,originalDO.attributes.get(METADATA));
        return meta;
    }

    public static MetaDO fromDOWithMetadata(DOWithMetadata doWithMetadata){
        MetaDO meta = new MetaDO(doWithMetadata.id);
        meta.addAttribute(METADATA,doWithMetadata.getMetadata());
        return meta;
    }
}
