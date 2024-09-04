let doType = {
    DO: "0.TYPE/DO",
    DOList: "0.TYPE/DOList",
    Metadata: "0.TYPE/DO.Metadata",
    DOIPServiceInfo: "0.TYPE/DO.DOIPServiceInfo",
    DOIPOperation: "0.TYPE/DO.DOIPOperation",
    UnKnown: "0.TYPE/UnKnow"
};

export function DigitalObject() {
    this.id="";
    this.type="";
    this.attributes= {};
    this.elements=new Array();
    this.createDigitalObject = function(id,type) {
        this.id = id;
        this.type = type;
        return this;
    }
    //给属性赋值,data为JsonObject
    this.setAttributes=function (data) {
        this.attributes = data;
    }

    this.addAttribute=function( name, value) {
        this.attributes[name]=value;
    }
    //给Elements赋值，data为element数组
    this.seElements=function (data) {
        this.elements = data;
    }
    //给Elements添加元素
   this.addElement=function(element){
    this.elements.push(element);
}
}
export default doType;