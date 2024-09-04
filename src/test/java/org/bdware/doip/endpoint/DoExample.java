/*
 *    Copyright (c) [2021] [Peking University]
 *    [BDWare DOIP SDK] is licensed under Mulan PSL v2.
 *    You can use this software according to the terms and conditions of the Mulan PSL v2.
 *    You may obtain a copy of Mulan PSL v2 at:
 *             http://license.coscl.org.cn/MulanPSL2
 *    THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *    See the Mulan PSL v2 for more details.
 */

package org.bdware.doip.endpoint;

import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.digitalObject.DoType;
import org.bdware.doip.codec.digitalObject.Element;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DoExample {
    public static DigitalObject small = getDo("bdware.test/small", 1, 20, true);
    public static DigitalObject large = getDo("bdware.test/large", 200, 5000, true);
    public static DigitalObject noData = getDo("bdware.test/small", 1, 20, false);
    public static DigitalObject b2000 = getDo("bdware.test/large", 2, 1000, true);
    public static DigitalObject superLarge = getDo("bdware.test/large", 2000, 1000, true);

    private static DigitalObject getDo(String doid, int num, int dataSize, boolean withData) {
        DigitalObject digitalObject = new DigitalObject("doid", DoType.DO);
        digitalObject.addAttribute("random", "attr");
        digitalObject.addAttribute("another", 52);
        if (withData)
            makeTestElements(num, dataSize).forEach(digitalObject::addElements);
        return digitalObject;
    }

    static List<Element> makeTestElementsWithoutData(List<Element> es) {
        List<Element> es2 = new ArrayList<Element>();
        Random rand = new Random();
        es.forEach((e) -> {
            Element e2 = new Element(e.id, e.type);
            e2.length = e.length;
            e2.attributes = e.attributes;
            es2.add(e2);
        });
        return es2;
    }

    static List<Element> makeTestElements(int num, int dataSize) {
        List<Element> es = new ArrayList<Element>();
        Random rand = new Random();
        for (int i = 0; i < num; i++) {
            Element e = makeTestElement(dataSize);
            es.add(e);
        }
        return es;
    }

    static Element makeTestElement(int dataSize) {
        Element e = new Element("file", "application/pdf");
        byte[] data = new byte[dataSize];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataSize; i++)
            sb.append(Math.abs(new Random().nextInt()) % 10);
        System.arraycopy(sb.toString().getBytes(StandardCharsets.UTF_8), 0, data, 0, dataSize);

        System.arraycopy("cold ".getBytes(StandardCharsets.UTF_8), 0, data, 0,
                "cold ".getBytes(StandardCharsets.UTF_8).length
        );
        e.setData(data);
        e.setAttribute("random", "attr");
        e.setAttribute("size", String.valueOf(dataSize));
        return e;
    }
}
