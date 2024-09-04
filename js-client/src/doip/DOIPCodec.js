import "bytearray"
import {DOIPEnvelope} from "./DOIPEnvelope";
import {DOIPMessage} from "./DOIPMessage";

const ba = require("bytearray");

function EnvelopeToBytes(envelope) {
    let bfEnvelopeTemp = Buffer.alloc(24);
    ba.writeByte(bfEnvelopeTemp, envelope.majorVersion);
    ba.writeByte(bfEnvelopeTemp, envelope.minVersion);
    ba.writeShort(bfEnvelopeTemp, envelope.flag)
    ba.writeInt(bfEnvelopeTemp, envelope.reserved);
    ba.writeInt(bfEnvelopeTemp, envelope.requestId);
    ba.writeInt(bfEnvelopeTemp, envelope.sequenceNumber);
    ba.writeInt(bfEnvelopeTemp, envelope.totalNumber);
    ba.writeInt(bfEnvelopeTemp, envelope.contentLength);
    const totalLength = bfEnvelopeTemp.length + envelope.content.length;
    const bfEnvelope = Buffer.concat([bfEnvelopeTemp, envelope.content], totalLength);
    return bfEnvelope;
}

function BytesToEnvelope(envelopeBytes) {
    let env = new DOIPEnvelope();
    const envelopeBuffer = Buffer.from(envelopeBytes, 'utf8');
    //console.log(envelopeBuffer);
    env.majorVersion = ba.readByte(envelopeBuffer, envelopeBuffer.position);
    env.minVersion = ba.readByte(envelopeBuffer, envelopeBuffer.position);
    env.flag = ba.readShort(envelopeBuffer, envelopeBuffer.position);
    env.reserved = ba.readInt(envelopeBuffer, envelopeBuffer.position);
    env.requestId = ba.readInt(envelopeBuffer, envelopeBuffer.position);
    env.sequenceNumber = ba.readInt(envelopeBuffer, envelopeBuffer.position);
    env.totalNumber = ba.readInt(envelopeBuffer, envelopeBuffer.position);
    env.contentLength = ba.readInt(envelopeBuffer, envelopeBuffer.position);
    let lengthRest=envelopeBuffer.length-24;
    if(env.contentLength != lengthRest){
        console.log("unequal content length: " + env.contentLength + ":" +lengthRest);
        alert("unequal content length: " + env.contentLength + ":" +lengthRest);
        return;
    }
    env.content = Buffer.from(envelopeBytes, 24, env.contentLength);
    //env.content =ba.readUTF(envelopeBuffer,envelopeBuffer.position);
    //env.content =ba.readUnsignedIntArray(envelopeBuffer,envelopeBuffer.position);
    return env;
}

function MessageToBytes(msg) {
    let bfMessageTemp = Buffer.alloc(65512);
    //parameters
    let paraLength = Buffer.from(JSON.stringify(msg.header.parameters)).length;
    msg.header.parameterLength = paraLength;
    //console.log("parameter string: " + JSON.stringify(msg.header.parameters))

    //credential暂时为空

    //写header
    ba.writeInt(bfMessageTemp, msg.header.flag);
    ba.writeInt(bfMessageTemp, msg.header.parameterLength);
    ba.writeInt(bfMessageTemp, msg.header.bodyLength);
    if(msg.header.parameterLength != 0 && msg.header.parameterLength != paraLength)
    {
        console.log("invalid parameter length: " + msg.header.parameterLength);
        alert("invalid parameter length: " + msg.header.parameterLength);
        return;
    }
    //字节数组
    ba.writeUTFBytes(bfMessageTemp, JSON.stringify(msg.header.parameters));
    //body咋办呢
    const length = bfMessageTemp.length - ba.getBytesAvailable(bfMessageTemp);
    let bfMessage = Buffer.from(bfMessageTemp.buffer, 0, length);
    const totalLength=length+msg.body.length;
    const bodyArray=Buffer.from(msg.body);
    if(msg.header.bodyLength != msg.body.length)
    {
        console.log("invalid body length: " + msg.header.bodyLength);
        alert("invalid body length:" + msg.header.bodyLength);
        return;
    }
    let finalbfMessage = Buffer.concat([bfMessage,bodyArray], totalLength);
    //console.log(bfMessage)
    return finalbfMessage;
}

function BytesToMessage(msgBytes, requestId) {
    //uint8Array 转成message
    let doipMSG = new DOIPMessage();
    //const msgBuffer=Buffer.from(msgBytes);
    //decode header
    doipMSG.header.requestID = requestId;
    doipMSG.header.flag = ba.readInt(msgBytes, msgBytes.position);
    doipMSG.header.parameterLength = ba.readInt(msgBytes, msgBytes.position);
    doipMSG.header.bodyLength = ba.readInt(msgBytes, msgBytes.position);
    let paraString=ba.readUTFBytes(msgBytes, doipMSG.header.parameterLength, msgBytes.position);
    doipMSG.header.parameters =JSON.parse(paraString);
    //decode body
    if (doipMSG.header.bodyLength > 0) {
        const restLength=msgBytes.length-msgBytes.position;
        if(restLength < doipMSG.header.bodyLength)
        {
            console.log("invalid body length");
            alert("invalid body length");
            return;
        }
        doipMSG.body  = msgBytes.subarray(msgBytes.position, msgBytes.length);
    }
    //DO转化为JSON格式的String，并将长度一起转为了Byte[]
    //ba.readInt(msgBytes, msgBytes.position);
   // doipMSG.body = ba.readUTFBytes(msgBytes, doipMSG.header.bodyLength-4, msgBytes.position);
    //const bodyString=byteToString(doipMSG.body);
    return doipMSG;
}

function DOToBytes(digitalObject) {
    let bfDO = Buffer.alloc(65512);
    //先写elements,每个element再转byte[]，放入到byte数组elementsTemp中
    //digitalObject.elements=sortElement(digitalObject.elements);
    let elementsTemp=new Array();
    let totalLength =0;
    if (digitalObject.elements) {
        digitalObject.elements.forEach(function (element) {
            if (element.data.length!=0 && element.length==element.data.length) {
                totalLength = elementsTemp.length + element.length;
                const bfelementsTemp = Buffer.from(elementsTemp);
                const bfelementData = Buffer.from(element.data);
                elementsTemp = Buffer.concat([bfelementsTemp, bfelementData], totalLength);
            }
        });
    }
    //DO转byte[]
    //现将DO的element中data[]变为空
    if (digitalObject.elements) {
        digitalObject.elements.forEach(function (element) {
            delete element.data;
        });
    }
    let doStr = JSON.stringify(digitalObject);
    //写DO
    ba.writeInt(bfDO, Buffer.byteLength(doStr));
    ba.writeUTFBytes(bfDO, doStr);
    let doLength = bfDO.length - ba.getBytesAvailable(bfDO);
    let doBuffer = Buffer.from(bfDO.buffer, 0, doLength);
    totalLength=totalLength+doLength;
    let finalDOBuffer = Buffer.concat([doBuffer, Buffer.from(elementsTemp)], totalLength);
    //console.log(bfMessage)
    return finalDOBuffer;
}

function sortElement(eles){
    for(let i=0;i<eles.length;i++)
    for(let j=i+1;j<eles.length;j++){
        if(eles[i].id>eles[j].id){
            //i与j交换位置
            let temp=eles[i];
            eles[i]=eles[j];
            eles[j]=temp;
        }
    }
    return eles;
}
export {EnvelopeToBytes, BytesToEnvelope, MessageToBytes, BytesToMessage,DOToBytes};


