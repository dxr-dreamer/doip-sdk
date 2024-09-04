package org.bdware.doip.endpoint.tls.v2_0;

import com.google.gson.JsonObject;
import net.dona.doip.InDoipSegment;
import net.dona.doip.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DOIPV2Cordra_TestClient {
    static Logger LOGGER = LogManager.getLogger(DOIPV2Cordra_TestClient.class);

    //    public static PrivateKey getPrivateKey() {
//        KeyPairGenerator keyPairGenerator;
//        try {
//            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//            keyPairGenerator.initialize(2048);
//            KeyPair keyPair = keyPairGenerator.generateKeyPair();
//            //公钥
//            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//            System.out.println("公钥：" + new BASE64Encoder().encode(publicKey.getEncoded()));
//            System.out.println("-----------------------------------------------------------------------");
//            //私钥
//            PrivateKey privateKey = keyPair.getPrivate();
//            return privateKey;
//        } catch (Exception e) {
//
//        }
//        return null;
//    }
    /*
     * 。。。。。
     * #
     * #
     * -
     *
     * 8888888
     * #
     * 777777
     * #
     * #
     * */
    public static String consume(InDoipSegment in) {

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int i = 0; (i = in.getInputStream().read(buf)) > 0; ) {
                bo.write(buf, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String ret = new String(bo.toByteArray());
        LOGGER.info("[Consume] " + ret);
        return ret;
    }

    public static void main(String[] args) {

        try {
            DoipClient client = new DoipClient();
//            PrivateKey pk = getPrivateKey();
//            AuthenticationInfo authInfo = new PrivateKeyAuthenticationInfo("abc",pk);

//            new PasswordAuthenticationInfo("admin", "wyq");
            AuthenticationInfo authInfo = new PasswordAuthenticationInfo("admin", "wyq");
            DigitalObject dobj = new DigitalObject();
            dobj.id = "20.5000.1148/123";
            dobj.type = "Document";
            JsonObject content = new JsonObject();
            content.addProperty("name", "example");
            dobj.setAttribute("content", content);
            Element el = new Element();
            el.id = "file";
            el.in = Files.newInputStream(Paths.get("./data/test.txt"));
            dobj.elements = new ArrayList<>();
            dobj.elements.add(el);

            ServiceInfo serviceInfo = new ServiceInfo("*.node.internetapi.cn", "127.0.0.1", 21042);
//            --------retrieve  failed------------
//           -----------------------------------------------------------------------------------------------------
//           |          method                                                                  status          |
//           |      retrieve(targetId,authInfo)                                                 failed          |
//           |      retrieve(targetId, includeElementData, authInfo)                            failed          |
//           |      retrieve(targetId, authInfo, serviceInfo)                                   success         |
//           |      retrieve(targetId, includeElementData, authInfo, serviceInfo)               success         |
//           -----------------------------------------------------------------------------------------------------
//            DigitalObject result = client.retrieve("abc.efg/small", authInfo);
//            DigitalObject result = client.retrieve("abc.efg/small", false, authInfo);
//            DigitalObject result = client.retrieve("abc.efg/small", authInfo, serviceInfo);
//            DigitalObject result = client.retrieve("abc.efg/small", false, authInfo, serviceInfo);
//            -------retrieveElement-------
//            InputStream result = client.retrieveElement("abc.efg/small", "", authInfo);
            InputStream result = client.retrieveElement("abc.efg/small", "", authInfo, serviceInfo);
//            -------retrievePartialElement-------
//            InputStream result = client.retrievePartialElement("abc.efg/small", "", null, null, authInfo, serviceInfo);
//            -------create-------
//            DigitalObject result = client.create(dobj, authInfo, serviceInfo);
//            -------hello-------
//            DigitalObject result = client.hello("20.5000.1148/123", authInfo);
//            DigitalObject result = client.hello("20.5000.1148/123", authInfo,serviceInfo);
//            -------update------
//            DigitalObject result = client.update(dobj,authInfo);
//            DigitalObject result = client.update(dobj,authInfo,serviceInfo);
//            -------listOperations--------
//            DigitalObject result = client.listOperations("20.5000.1148/123", authInfo);
//            DigitalObject result = client.listOperations("20.5000.1148/123", authInfo, serviceInfo);
//            -------delete--------
//            client.delete("20.5000.1148/123", authInfo);
//            client.delete("20.5000.1148/123", authInfo, serviceInfo);
//            -------search--------
//            SearchResults<DigitalObject> result = client.search("20.5000.1148/123", "aaa", null, authInfo);
//            SearchResults<DigitalObject> result = client.search("20.5000.1148/123", "aaa", null, authInfo, serviceInfo);
//            -------searchIds-------
//            SearchResults<String> result = client.searchIds("abc.efg/small", "", null, authInfo);
//            SearchResults<String> result = client.searchIds("abc.efg/small", "", null, authInfo, serviceInfo);
//            -------performOperation--------
//            DoipClientResponse result = client.performOperation(null, null);
//            DoipClientResponse result = client.performOperation(null, null, serviceInfo);
//            DoipClientResponse result = client.performOperation("abc.efg/small", "retrieve", authInfo, null);
//            DoipClientResponse result = client.performOperation("abc.efg/small", "retrieve", authInfo, null, new JsonObject());
//            DoipClientResponse result = client.performOperation("abc.efg/small", "retrieve", authInfo, null, new InDoipMessage() {
//                @Override
//                public Stream<InDoipSegment> stream() {
//                    return null;
//                }
//
//                @Override
//                public void close() {
//
//                }
//
//                @Override
//                public Iterator<InDoipSegment> iterator() {
//                    return null;
//                }
//            });
//            DoipClientResponse result = client.performOperation("abc.efg/small", "retrieve", authInfo, null, serviceInfo);
//            DoipClientResponse result = client.performOperation("abc.efg/small", "retrieve", authInfo, null, new JsonObject(), serviceInfo);
//            DoipClientResponse result = client.performOperation("abc.efg/small", "retrieve", authInfo, null, new InDoipMessage() {
//                @Override
//                public Stream<InDoipSegment> stream() {
//                    return null;
//                }
//
//                @Override
//                public void close() {
//
//                }
//
//                @Override
//                public Iterator<InDoipSegment> iterator() {
//                    return null;
//                }
//            }, serviceInfo);
            LOGGER.info(result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DoipException e) {
            e.printStackTrace();
        }


    }
}
