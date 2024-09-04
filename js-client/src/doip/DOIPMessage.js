export function DOIPMessage() {
    this.header = {
        "flag":0,
        "requestID":0,
        "parameterLength":4,
        "bodyLength":0,
        "parameters":{"id":"","operation":"","response":"","attributes":{}}
    };

    //给body赋值,digitalObject为byte[]
    this.setBody=function (digitalObject) {
        this.body = digitalObject;
        this.header.bodyLength=digitalObject.length;
    }
    this.body = new Array();
    this.credential = null;
    // const codec = require('DOIPCodec');

    this.createDOIPMessage = function(paramID,operation) {
        this.header.parameters.id = paramID;
        this.header.parameters.operation = operation;
        this.header.requestID = 1;
        return this;
    }

    this.addAttributes=function (attrkey,value) {
        this.header.parameters.attributes[attrkey] = value;
    }
}