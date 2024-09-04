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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.digitalObject.DoType;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DoipServiceInfo {
    public String id;
    public String serviceDescription;
    public String publicKey;
    public String serviceName;
    public int port;
    public String ipAddress;
    public String protocol;
    public String protocolVersion;
    public List<DoipListenerConfig> listenerInfos;
    public String owner;
    public String repoType;
    // 新增链接python server
    public String pyserverUrl;

    public DoipServiceInfo(String id, String owner, String repoType, List<DoipListenerConfig> listenerInfos){
        this.id = id;
        this.owner = owner;
        this.repoType = repoType;
        this.listenerInfos = listenerInfos;
        String url = listenerInfos.get(0).url;
        URI uri = URI.create(url);
        protocol = uri.getScheme();
        ipAddress = uri.getHost();
        port = uri.getPort();
        protocolVersion = listenerInfos.get(0).protocolVersion;
    }

    public static DoipServiceInfo fromJson(String str) {
        return new Gson().fromJson(str, DoipServiceInfo.class);
    }

    public String toJson(){
        return new Gson().toJson(this);
    }

    public byte[] toBytes(){
        return this.toJson().getBytes(StandardCharsets.UTF_8);
    }

    public DigitalObject toDigitalObject(){

        DigitalObject dObj = new DigitalObject(this.id, DoType.DOIPServiceInfo);
        dObj.addAttribute("serviceName", serviceName);
        dObj.addAttribute("serviceDescription",serviceDescription);
        dObj.addAttribute("owner",owner);
        dObj.addAttribute("repoType",repoType);
        dObj.addAttribute("publicKey",publicKey);
        dObj.addAttribute("protocol",protocol);
        dObj.addAttribute("protocolVersion",protocolVersion);
        dObj.addAttribute("port",port);
        dObj.addAttribute("ipAddress",ipAddress);
        dObj.addAttribute("listenerInfos",new Gson().toJson(listenerInfos));
        return dObj;
    }

    public static DoipServiceInfo fromDigitalObject(DigitalObject dObj) {
        List<DoipListenerConfig> ss = new Gson().fromJson(dObj.attributes.get("listenerInfos").getAsString(),
                new TypeToken<List<DoipListenerConfig>>(){}.getType());
        DoipServiceInfo serviceInfo = new DoipServiceInfo(
                dObj.id,
                dObj.attributes.get("owner").getAsString(),
                dObj.attributes.get("repoType").getAsString(),
                ss
        );
        serviceInfo.serviceName = dObj.attributes.get("serviceName") == null?"":dObj.attributes.get("serviceName").getAsString();
        serviceInfo.publicKey = dObj.attributes.get("publicKey") == null?"":dObj.attributes.get("publicKey").getAsString();
        serviceInfo.serviceDescription = dObj.attributes.get("serviceDescription") == null?"":dObj.attributes.get("serviceDescription").getAsString();
        serviceInfo.repoType = dObj.attributes.get("repoType") == null?"":dObj.attributes.get("repoType").getAsString();

        return serviceInfo;
    }

}
