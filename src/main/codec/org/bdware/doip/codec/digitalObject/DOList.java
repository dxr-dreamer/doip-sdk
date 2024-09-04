package org.bdware.doip.codec.digitalObject;

import org.bdware.doip.codec.exception.DoDecodeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DOList extends DigitalObject {


    public DOList(String id) {
        super(id, DoType.DOList);
    }

    public void addDO(DigitalObject digitalObject){
        Element e = new Element(digitalObject.id,DoType.DO.getName());
        e.setData(digitalObject.toByteArray());
        addElements(e);
    }

    public boolean deleteDO(String doi){
        for (int i=0;i<elements.size();i++) {
            if(elements.get(i).id.equals(doi)){
                elements.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<DigitalObject> getDOList() throws IOException,DoDecodeException {
        ArrayList<DigitalObject> doList = new ArrayList<>();
        for (Element e: this.elements) {
            DigitalObject t = DigitalObject.fromByteArray(e.getData());
            doList.add(t);
        }
        return doList;
    }

    public static DOList fromDigitalObject(DigitalObject digitalObject){
        DOList doList = new DOList(digitalObject.id);
        doList.elements = digitalObject.elements;
        doList.attributes = digitalObject.attributes;
        return doList;
    }
}

