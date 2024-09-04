import {BytesToEnvelope, BytesToMessage, DOToBytes, EnvelopeToBytes, MessageToBytes} from "./DOIPCodec";
import {DOIPMessage} from "./DOIPMessage";
import {DOIPEnvelope} from "./DOIPEnvelope";

let operation = {
    Hello: "0.DOIP/Op.Hello",
    ListOps: "0.DOIP/Op.ListOperations",
    Retrieve: "0.DOIP/Op.Retrieve",
    Create: "0.DOIP/Op.Create",
    Update: "0.DOIP/Op.Update",
    Delete: "0.DOIP/Op.Delete",
    Search: "0.DOIP/Op.Search",
    Extension: "0.DOIP/Op.Extension",
    Unknown: "0.DOIP/Op.Unknown"
};

//Hello操作
function Hello(repoID, ws, callback) {
    if (!ws) {
        console.log("WebSocket为空！");
        alert("WebSocket为空！");
        return;
    }
    if(!repoID) {
        console.log("RepositoryID为空！");
        alert("RepositoryID为空！");
        return;
    }
    /*接收*/
    ws.onmessage = (resp) => {
        //判断是否为Blob格式的数据
        if (resp.data instanceof Blob) {
            let blob = resp.data;
            //通过FileReader读取数据
            let reader = new FileReader();
            //以下这两种方式我都可以解析出来，因为Blob对象的数据可以按文本或二进制的格式进行读取
            //reader.readAsBinaryString(blob);
            // reader.readAsText(blob, 'utf8');
            reader.readAsArrayBuffer(blob);
            reader.onload = function () {
                let receive_data = this.result;//这个就是解析出来的数据
                //console.log(receive_data);
                //解析并展示数据
                let respEnv = BytesToEnvelope(receive_data);
                //console.log(respEnv.content);
                let doipMsg = BytesToMessage(respEnv.content, respEnv.requestId);
                //console.log("=====doip msg: " + doipMsg)
                const headerString=JSON.stringify(doipMsg.header);
                const bufBytes=Buffer.from(doipMsg.body).slice(4);
                const bodyString=bufBytes.toString();
                callback(headerString,bodyString, operation.Hello);
            }
        }
    }
    /*发送*/
    let req = new DOIPMessage().createDOIPMessage(repoID, operation.Hello);
    let env = new DOIPEnvelope();
    env.content = MessageToBytes(req);
    env.contentLength=env.content.length;
    // console.log(Buffer.byteLength(env));
    ws.addEventListener('open',function (){ ws.send(EnvelopeToBytes(env));})
}

//ListOperations操作
function ListOperations(doID, ws, callback) {
    if (!ws) {
        console.log("WebSocket为空！");
        alert("WebSocket为空！");
        return;
    }
    if(!doID) {
        console.log("doID为空！");
        alert("doID为空！");
        return;
    }
    /*接收*/
    ws.onmessage = (resp) => {
        //判断是否为Blob格式的数据
        if (resp.data instanceof Blob) {
            let blob = resp.data;
            //通过FileReader读取数据
            let reader = new FileReader();
            //以下这两种方式我都可以解析出来，因为Blob对象的数据可以按文本或二进制的格式进行读取
            //reader.readAsBinaryString(blob);
            // reader.readAsText(blob, 'utf8');
            reader.readAsArrayBuffer(blob);
            reader.onload = function () {
                let receive_data = this.result;//这个就是解析出来的数据
                //console.log(receive_data);
                //解析并展示数据
                let respEnv = BytesToEnvelope(receive_data);
                //console.log(respEnv.content);
                let doipMsg = BytesToMessage(respEnv.content, respEnv.requestId);
                //console.log("=====doip msg: " + doipMsg)
                const headerString=JSON.stringify(doipMsg.header);
                const bufBytes=Buffer.from(doipMsg.body);
                const bodyString=bufBytes.toString();
                callback(headerString,bodyString, operation.ListOps);
            }
        }
    }
    /*发送*/
    //根据ListOperations要求创建DOIPMessage对象，DOIPEnvelope对象
    let req = new DOIPMessage().createDOIPMessage(doID, operation.ListOps);
    let env = new DOIPEnvelope();
    env.content = MessageToBytes(req);
    env.contentLength=env.content.length;
    // console.log(Buffer.byteLength(env));
    ws.addEventListener('open',function (){ ws.send(EnvelopeToBytes(env));})
}

//检索DO的element
function Retrieve(doID,ws,elementID,includeElementData,callback) {
    if (!ws) {
        console.log("WebSocket为空！");
        alert("WebSocket为空！");
        return;
    }
    if(!doID) {
        console.log("doID为空！");
        alert("doID为空！");
        return;
    }
    /*接收*/
    ws.onmessage = (resp) => {
        //判断是否为Blob格式的数据
        if (resp.data instanceof Blob) {
            let blob = resp.data;
            //通过FileReader读取数据
            let reader = new FileReader();
            //以下这两种方式我都可以解析出来，因为Blob对象的数据可以按文本或二进制的格式进行读取
            //reader.readAsBinaryString(blob);
            // reader.readAsText(blob, 'utf8');
            reader.readAsArrayBuffer(blob);
            reader.onload = function () {
                let receive_data = this.result;//这个就是解析出来的数据
                //console.log(receive_data);
                //解析并展示数据
                let respEnv = BytesToEnvelope(receive_data);
                //console.log(respEnv.content);
                let doipMsg = BytesToMessage(respEnv.content, respEnv.requestId);
                //console.log("=====doip msg: " + doipMsg)
                const headerString=JSON.stringify(doipMsg.header);
                let bufBytes="";
                if (doipMsg.header.parameters.response == "0.DOIP/Status.001" && !elementID) {
                    bufBytes = Buffer.from(doipMsg.body).slice(4);
                } else {
                    bufBytes = Buffer.from(doipMsg.body);
                }
                const bodyString=bufBytes.toString();
                callback(headerString,bodyString, operation.Retrieve);
            }
        }
    }
    /*发送*/
    //根据ListOperations要求创建DOIPMessage对象，DOIPEnvelope对象
    let req = new DOIPMessage().createDOIPMessage(doID, operation.Retrieve);
    //将elementID和includeElementData两个参数，放入到message的header的attributes
    if(elementID) req.addAttributes("element",elementID);
    if(includeElementData) req.addAttributes("includeElementData",includeElementData);
    let env = new DOIPEnvelope();
    env.content = MessageToBytes(req);
    env.contentLength=env.content.length;
    // console.log(Buffer.byteLength(env));
    ws.addEventListener('open',function (){ ws.send(EnvelopeToBytes(env));})
}

//创建DO
function Create(repoID, ws, digitalObject, callback) {
    if (!ws) {
        console.log("WebSocket为空！");
        alert("WebSocket为空！");
        return;
    }
    if(!repoID) {
        console.log("RepositoryID为空！");
        alert("RepositoryID为空！");
        return;
    }
    /*接收*/
    ws.onmessage = (resp) => {
        //判断是否为Blob格式的数据
        if (resp.data instanceof Blob) {
            let blob = resp.data;
            //通过FileReader读取数据
            let reader = new FileReader();
            //以下这两种方式我都可以解析出来，因为Blob对象的数据可以按文本或二进制的格式进行读取
            //reader.readAsBinaryString(blob);
            // reader.readAsText(blob, 'utf8');
            reader.readAsArrayBuffer(blob);
            reader.onload = function () {
                let receive_data = this.result;//这个就是解析出来的数据
                //console.log(receive_data);
                //解析并展示数据
                let respEnv = BytesToEnvelope(receive_data);
                //console.log(respEnv.content);
                let doipMsg = BytesToMessage(respEnv.content, respEnv.requestId);
                //console.log("=====doip msg: " + doipMsg)
                const headerString=JSON.stringify(doipMsg.header);
                let bufBytes="";
                if (doipMsg.header.parameters.response == "0.DOIP/Status.001") {
                    bufBytes = Buffer.from(doipMsg.body).slice(4);
                } else {
                    bufBytes = Buffer.from(doipMsg.body);
                }
                const bodyString=bufBytes.toString();
                callback(headerString,bodyString, operation.Create);
            }
        }
    }
    /*发送*/
    //创建message，需要将DO放入body中。注意DO放入时的类型【dotobyte[]】
    let req = new DOIPMessage().createDOIPMessage(repoID, operation.Create);
    //DO转byte[],并赋值给message的body
    req.setBody(DOToBytes(digitalObject));
    let env = new DOIPEnvelope();
    env.content = MessageToBytes(req);
    env.contentLength=env.content.length;
    // console.log(Buffer.byteLength(env));
    ws.addEventListener('open',function (){ ws.send(EnvelopeToBytes(env));})
}

function Delete(doID, ws, callback) {
    if (!ws) {
        console.log("WebSocket为空！");
        alert("WebSocket为空！");
        return;
    }
    if(!doID) {
        console.log("doID为空！");
        alert("doID为空！");
        return;
    }
    /*接收*/
    ws.onmessage = (resp) => {
        //判断是否为Blob格式的数据
        if (resp.data instanceof Blob) {
            let blob = resp.data;
            //通过FileReader读取数据
            let reader = new FileReader();
            //以下这两种方式我都可以解析出来，因为Blob对象的数据可以按文本或二进制的格式进行读取
            //reader.readAsBinaryString(blob);
            // reader.readAsText(blob, 'utf8');
            reader.readAsArrayBuffer(blob);
            reader.onload = function () {
                let receive_data = this.result;//这个就是解析出来的数据
                //console.log(receive_data);
                //解析并展示数据
                let respEnv = BytesToEnvelope(receive_data);
                //console.log(respEnv.content);
                let doipMsg = BytesToMessage(respEnv.content, respEnv.requestId);
                //console.log("=====doip msg: " + doipMsg)
                const headerString=JSON.stringify(doipMsg.header);
                const bufBytes=Buffer.from(doipMsg.body);
                const bodyString=bufBytes.toString();
                callback(headerString,bodyString, operation.Delete);
            }
        }
    }
    /*发送*/
    //根据ListOperations要求创建DOIPMessage对象，DOIPEnvelope对象
    let req = new DOIPMessage().createDOIPMessage(doID, operation.Delete);
    let env = new DOIPEnvelope();
    env.content = MessageToBytes(req);
    env.contentLength=env.content.length;
    // console.log(Buffer.byteLength(env));
    ws.addEventListener('open',function (){ ws.send(EnvelopeToBytes(env));})
}

function Update(ws, newDO, callback) {
    if (!ws) {
        console.log("WebSocket为空！");
        alert("WebSocket为空！");
        return;
    }
    if (!newDO.id) {
        console.log("DigitalObject的id为空！");
        alert("DigitalObject的id为空！");
        return;
    }
    /*接收*/
    ws.onmessage = (resp) => {
        //判断是否为Blob格式的数据
        if (resp.data instanceof Blob) {
            let blob = resp.data;
            //通过FileReader读取数据
            let reader = new FileReader();
            //以下这两种方式我都可以解析出来，因为Blob对象的数据可以按文本或二进制的格式进行读取
            //reader.readAsBinaryString(blob);
            // reader.readAsText(blob, 'utf8');
            reader.readAsArrayBuffer(blob);
            reader.onload = function () {
                let receive_data = this.result;//这个就是解析出来的数据
                console.log(receive_data);
                //解析并展示数据
                let respEnv = BytesToEnvelope(receive_data);
                console.log(respEnv.content);
                let doipMsg = BytesToMessage(respEnv.content, respEnv.requestId);
                console.log("=====doip msg: " + doipMsg)
                const headerString=JSON.stringify(doipMsg.header);
                const bufBytes=Buffer.from(doipMsg.body);
                const bodyString=bufBytes.toString();
                callback(headerString,bodyString, operation.Update);
            }
        }
    }
    /*发送*/
    //创建message，需要将DO放入body中。注意DO放入时的类型【dotobyte[]】
    let req = new DOIPMessage().createDOIPMessage(newDO.id, operation.Update);
    //DO转byte[],并赋值给message的body
    req.setBody(DOToBytes(newDO));
    let env = new DOIPEnvelope();
    env.content = MessageToBytes(req);
    env.contentLength=env.content.length;
    // console.log(Buffer.byteLength(env));
    ws.addEventListener('open',function (){ ws.send(EnvelopeToBytes(env));})
}

function Search(registryID, ws, sp, callback) {
    if (!ws) {
        console.log("WebSocket为空！");
        alert("WebSocket为空！");
        return;
    }
    if(!registryID) {
        console.log("registryID为空！");
        alert("registryID为空！");
        return;
    }
    /*接收*/
    ws.onmessage = (resp) => {
        //判断是否为Blob格式的数据
        if (resp.data instanceof Blob) {
            let blob = resp.data;
            //通过FileReader读取数据
            let reader = new FileReader();
            //以下这两种方式我都可以解析出来，因为Blob对象的数据可以按文本或二进制的格式进行读取
            //reader.readAsBinaryString(blob);
            // reader.readAsText(blob, 'utf8');
            reader.readAsArrayBuffer(blob);
            reader.onload = function () {
                let receive_data = this.result;//这个就是解析出来的数据
                //console.log(receive_data);
                //解析并展示数据
                let respEnv = BytesToEnvelope(receive_data);
                //console.log(respEnv.content);
                let doipMsg = BytesToMessage(respEnv.content, respEnv.requestId);
                //console.log("=====doip msg: " + doipMsg)
                const headerString=JSON.stringify(doipMsg.header);
                const bufBytes=Buffer.from(doipMsg.body);
                const bodyString=bufBytes.toString();
                callback(headerString,bodyString, operation.Search);
            }
        }
    }
    /*发送*/
    //根据ListOperations要求创建DOIPMessage对象，DOIPEnvelope对象
    let req = new DOIPMessage().createDOIPMessage(registryID, operation.Search);
    req.addAttributes("query",sp.query);
    req.addAttributes("pageNum",sp.pageNum);
    req.addAttributes("pageSize",sp.pageSize);
    req.addAttributes("type",sp.type);
    req.addAttributes("sortFields",sp.sortFieldsSer);
    let env = new DOIPEnvelope();
    env.content = MessageToBytes(req);
    env.contentLength=env.content.length;
    // console.log(Buffer.byteLength(env));
    ws.addEventListener('open',function (){ ws.send(EnvelopeToBytes(env));})
}

function Connect(wsURL) {
    if (!wsURL) {
        alert("链接为空！");
        console.log("链接为空！");
        return ;
    }
        let ws = new WebSocket(wsURL);
        ws.onerror= function (e) {
            console.log(e);
            alert("连接失败！");
            return;

        //ws = new WebSocket(wsServer); location.reload();
    };
  /*  await new Promise((resolve) => {
        ws.onopen = function (e) {
            resolve(e.data);
        };
    }).then((data)=>{return ws;});*/
    return ws;
}

export {Hello, ListOperations, Retrieve, Create, Delete, Update, Search, Connect, operation};