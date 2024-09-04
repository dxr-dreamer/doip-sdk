// import "./doip/DOIPCodec";
// import "./doip/DOIPClient";
import "bootstrap/dist/css/bootstrap.min.css"
import React, {Component} from 'react';
import "./doip/DOIPClient";
import {Button, InputGroup, FormControl, Form, Container, Row, Col, Modal, Toast} from "react-bootstrap";
import DropdownButton from "react-bootstrap/DropdownButton";
import Dropdown from "react-bootstrap/Dropdown";
import {ListOperations, operation} from "./doip/DOIPClient";
import doType, {DigitalObject} from "./doip/DigitalObject";
import {DOElement} from "./doip/DOElement";
import {SearchParameter} from "./doip/SearchParameter";
import {Hello, Delete, ListOperation, Retrieve, Search, Create, Update} from "./ChildComponent";
import fetchJsonp from "fetch-jsonp";

let doipClient = require('./doip/DOIPClient');
let wsURL = '';
let ws = '';
let resolveURL="";
// let resolveURL="http://127.0.0.1:10001/resolve?identifier=";
let resolveRest="resolve?identifier="
let repoIdentifier='';
let registryIdentifier='';
let doIdentifier='';
// let repoID = "86.5000.470/doip.oWbxuXng3I_bdw";
// let registryID = "86.5000.470/doip.oWbxuXng3I_bdw";
// let doID = "86.5000.470/do.q6TpvldM6U_bdw";
// let newDO = new DigitalObject().createDigitalObject("86.5000.470/do.q6TpvldM6U_bdw", doType.DO);
// let elementID = "element001";
// let includeElementData = true;

class DOAClient extends React.Component{
    constructor(props) {
        super(props);
        this.display = this.display.bind(this);
        this.sendHello = this.sendHello.bind(this);
        this.sendlistOperations = this.sendlistOperations.bind(this);
        this.sendCreate = this.sendCreate.bind(this);
        this.sendUpdate = this.sendUpdate.bind(this);
        this.sendRetrieve = this.sendRetrieve.bind(this);
        this.sendDelete = this.sendDelete.bind(this);
        this.sendSearch = this.sendSearch.bind(this);
        this.state = {
            dropdownTitle: '选择DOIP操作码',
            helloBody: '',
            listOperationBody: '',
            retrieveBody: '',
            createBody: '',
            deleteBody: '',
            updateBody: '',
            searchBody: '',
            helloHeader: '',
            listOperationHeader: '',
            retrieveHeader: '',
            createHeader: '',
            deleteHeader: '',
            updateHeader: '',
            searchHeader: '',
            IRSAddress:'http://127.0.0.1:10001/'
        };
    }

    display(dataHeader,dataBody ,opera) {
        const d1 = dataHeader;
        const d2 = dataBody;
        if (opera == operation.Hello) {
            this.setState({
                helloHeader: d1,
            });
            this.setState({
                helloBody: d2,
            });
        } else if (opera == operation.ListOps) {
            this.setState({
                listOperationHeader: d1,
            });
            this.setState({
                listOperationBody: d2,
            });
        } else if (opera == operation.Create) {
            this.setState({
                createHeader: d1,
            });
            this.setState({
                createBody: d2,
            });
        } else if (opera == operation.Update) {
            this.setState({
                updateHeader: d1,
            });
            this.setState({
                updateBody: d2,
            });
        } else if (opera == operation.Retrieve) {
            this.setState({
                retrieveHeader: d1,
            });
            this.setState({
                retrieveBody: d2,
            });
        } else if (opera == operation.Search) {
            this.setState({
                searchHeader: d1,
            });
            this.setState({
                searchBody: d2,
            });
        } else if (opera == operation.Delete) {
            this.setState({
                deleteHeader: d1,
            });
            this.setState({
                deleteBody: d2,
            });
        }
    }

    sendHello(repoID) {
        resolveURL=this.state.IRSAddress;
        repoIdentifier=repoID;
        let urlString=resolveURL+resolveRest+repoIdentifier;
            fetch(urlString)
                .then(function (response) {
                    return response.text();
                })
                .then((result) => {
                    try {
                        eval('(' + JSON.parse(result).listeners + ')').forEach((arrItem) => {
                            if (arrItem.url.indexOf("ws") != -1) {
                                wsURL = arrItem.url;
                            }
                        });
                    }catch (e) {
                        console.log('error：' + '!' + e);
                        alert('输入的IRS的地址或者Repository的ID有误！');
                        return;
                    }
                    ws = doipClient.Connect(wsURL);
                    return ws;
                }).then((ws)=>{
                if (ws) {
                    doipClient.Hello(repoID, ws, this.display);
                } else {
                    return;
                }
            }).catch((error) => {
                    console.log('error is' + error);
                    alert('error is' + error);
                    return;
                }
            );
    }

    sendlistOperations(doID) {
        resolveURL=this.state.IRSAddress;
        let urlStringTODO=resolveURL+resolveRest+doID;
        fetch(urlStringTODO)
            .then(function (response) {
                return response.text();
            })
            .then((result) => {
                try {
                    // wsURL=eval('(' + JSON.parse(result).listeners + ')').shift().url;
                    // if(JSON.parse(result))
                    let resultJson=JSON.parse(result);
                    if(resultJson.hasOwnProperty("repository")){
                        repoIdentifier=resultJson.repository;
                        if (repoIdentifier) {
                            let urlString = resolveURL +resolveRest+ repoIdentifier;
                            console.log(urlString);
                            fetch(urlString)
                                .then(function (response) {
                                    return response.text();
                                })
                                .then((result) => {
                                    try {
                                        eval('(' + JSON.parse(result).listeners + ')').forEach((arrItem) => {
                                            if (arrItem.url.indexOf("ws") != -1) {
                                                wsURL = arrItem.url;
                                            }
                                        });
                                        ws = doipClient.Connect(wsURL);
                                    } catch (e) {
                                        console.log('error：' + '!' + e);
                                        alert('输入的IRS的地址或者DO的ID有误！');
                                        return;
                                    }
                                    return ws;
                                }).then((ws) => {
                                doipClient.ListOperations(doID, ws, this.display);
                            });
                        }

                    }else{
                        console.log('输入的DO的ID有误或不存在此DO！');
                        alert('输入的DO的ID有误或不存在此DO！');
                        return ;
                    }
                }catch (e) {
                    console.log('error：' + '!' + e);
                    alert('输入的IRS的地址或者DO的ID有误！');
                    return ;
                }
            }).catch((error) => {
                console.log('error is' + error);
                alert('error is' + error);
                return;
            }
        );
    }

    sendCreate(repoID,doString) {
        /**
         * string解析
         * 往metadata中放数据
         * 往DO中放数据和metadata（element中data要先转成byte[]）
         */

        //doString格式问题抛出异常
        try {
            let doJson = JSON.parse(doString);
            if (typeof doJson == 'object' && doJson) {
                //doJson.type为空处理
                if (!doJson.type) {
                    alert("请补充DO的type！");
                    return;
                }
                let digitalObject = new DigitalObject().createDigitalObject("", doJson.type);
                digitalObject.setAttributes(doJson.attributes);
                /*        let metaJO = doJson.attributes.metadata;
                        digitalObject.addAttribute("metadata", metaJO);   //metadata 创建索引*/
                //增添elements
                let elements=new Array();
                if (doJson.elements) {
                    doJson.elements.forEach(function (element) {
                        //把element赋给DOElement
                        let doElement=new DOElement().createElement(element.id,element.type);
                        doElement.length=element.length;
                        doElement.dataString=element.dataString;
                        doElement.attributes= element.attributes;
                        const dataString = element.dataString;
                        let data = Buffer.from(dataString);
                        doElement.setData(data);
                        elements.push(doElement)
                    })
                }
                digitalObject.seElements(elements);

                resolveURL=this.state.IRSAddress;
                repoIdentifier=repoID;
                let urlString=resolveURL+resolveRest+repoIdentifier;
                fetch(urlString)
                    .then(function (response) {
                        return response.text();
                    })
                    .then((result) => {
                        try {
                            eval('(' + JSON.parse(result).listeners + ')').forEach((arrItem) => {
                                if (arrItem.url.indexOf("ws") != -1) {
                                    wsURL = arrItem.url;
                                }
                            });
                            ws = doipClient.Connect(wsURL);
                        }catch (e) {
                            console.log('error：' + '!' + e);
                            alert('输入的IRS的地址或者Repository的ID有误！');
                            return;
                        }
                        return ws;
                    }).then(
                        (ws)=>{
                            if (ws) {
                                doipClient.Create(repoID, ws, digitalObject, this.display);
                            } else {
                                return;
                            }
                        }
                ).catch((error) => {
                        console.log('error is' + error);
                        alert('error is' + error);
                        return;
                    }
                );
            } else {
                alert("DO的信息输入有误，请重新输入！");
                return;
            }
        } catch (e) {
            console.log('error：' + doString + '!!!' + e);
            alert("DO的信息输入有误，请重新输入！");
            return;
        }
    }

    sendUpdate(doString) {
        //doString格式问题抛出异常
        try {
            let doJson = JSON.parse(doString);
            if(!doJson.id){
                alert("要更新的DO的id为空！");
                return;
            }
            if (typeof doJson == 'object' && doJson) {
                //doJson.type为空处理
                if (!doJson.type) {
                    alert("请补充DO的type！");
                    return;
                }
                let digitalObject = new DigitalObject().createDigitalObject(doJson.id, doJson.type);
                digitalObject.setAttributes(doJson.attributes);
                /*        let metaJO = doJson.attributes.metadata;
                        digitalObject.addAttribute("metadata", metaJO);   //metadata 创建索引*/
                //增添elements
                let elements=new Array();
                if (doJson.elements) {
                    doJson.elements.forEach(function (element) {
                        //把element赋给DOElement
                        let doElement=new DOElement().createElement(element.id,element.type);
                        doElement.length=element.length;
                        doElement.dataString=element.dataString;
                        doElement.attributes= element.attributes;
                        const dataString = element.dataString;
                        let data = Buffer.from(dataString);
                        doElement.setData(data);
                        elements.push(doElement)
                    })
                }
                digitalObject.seElements(elements);

                resolveURL=this.state.IRSAddress;
                let urlStringTODO=resolveURL+resolveRest+doJson.id;
                fetch(urlStringTODO)
                    .then(function (response) {
                        return response.text();
                    })
                    .then((result) => {
                        try {
                            // wsURL=eval('(' + JSON.parse(result).listeners + ')').shift().url;
                            // if(JSON.parse(result))
                            let resultJson=JSON.parse(result);
                            if(resultJson.hasOwnProperty("repository")){
                                repoIdentifier=resultJson.repository;
                                if (repoIdentifier) {
                                    let urlString = resolveURL+resolveRest + repoIdentifier;
                                    console.log(urlString);
                                    fetch(urlString)
                                        .then(function (response) {
                                            return response.text();
                                        })
                                        .then((result) => {
                                            try {
                                                eval('(' + JSON.parse(result).listeners + ')').forEach((arrItem) => {
                                                    if (arrItem.url.indexOf("ws") != -1) {
                                                        wsURL = arrItem.url;
                                                    }
                                                });
                                                ws = doipClient.Connect(wsURL);
                                            } catch (e) {
                                                console.log('error：' + '!' + e);
                                                alert('输入的DO的ID有误！');
                                                return;
                                            }
                                            return ws;
                                        }).then((ws) => {
                                        if (ws) {
                                            doipClient.Update(ws, digitalObject, this.display);
                                        } else {
                                            return;
                                        }
                                    });
                                }
                            }else{
                                console.log('输入的DO的ID有误或不存在此DO！');
                                alert('输入的DO的ID有误或不存在此DO！');
                                return ;
                            }
                        }catch (e) {
                            console.log('error：' + '!' + e);
                            alert('输入的IRS的地址或者DO的ID有误！');
                            return ;
                        }
                    }).catch((error) => {
                        console.log('error is' + error);
                        alert('error is' + error);
                        return;
                    }
                );
            } else {
                alert("DO的信息输入有误，请重新输入！");
                return;
            }
        } catch (e) {
            console.log('error：' + doString + '!!!' + e);
            alert("DO的信息输入有误，请重新输入！");
            return;
        }
    }

    sendRetrieve(doID,elementID,includeElementData) {
        resolveURL=this.state.IRSAddress;
        let urlStringTODO=resolveURL+resolveRest+doID;
        urlStringTODO = "http://localhost:3000/";
        // TODO 将urlStringTODO 替换为通过标识解析系统的解析接口拿到repo地址的接口。
        fetch(urlStringTODO)
            .then(function (response) {
                return response.text();
            })
            .then((result) => {
                try {
                    // wsURL=eval('(' + JSON.parse(result).listeners + ')').shift().url;
                    // if(JSON.parse(result))
                    // TODO 通过标识解析系统的解析接口拿到repo地址。

                    let resultJson = "{\"repository\":\"ws://127.0.0.1:21042/wsdoip\"}";
                    console.log("connect:"+resultJson) ;
                    resultJson=JSON.parse(resultJson);

                    if(resultJson.hasOwnProperty("repository")){
                        let wsUrl=resultJson.repository;
                        try {
                            console.log("connect:"+wsUrl) ;
                            ws = doipClient.Connect(wsUrl);
                        } catch (e) {
                            console.log('error：' + '!' + e);
                            alert('输入的DO的ID有误！');
                            return;
                        }
                        return ws;
                    }else{
                        console.log('输入的DO的ID有误或不存在此DO！');
                        alert('输入的DO的ID有误或不存在此DO！');
                        return ;
                    }
                }catch (e) {
                    console.log('error：' + '!' + e);
                    alert('输入的DO的ID有误！');
                    return ;
                }
            }).then((ws) => {
                        if (ws) {
                            doipClient.Retrieve(doID, ws, elementID, includeElementData, this.display);
                        } else {
                            return;
                        }
                        }
            ).catch((error) => {
                console.log('error is' + error);
                alert('error is' + error);
                return;
            }
        );
    }

    sendSearch(registryID,query,type) {
        let sp = new SearchParameter().createSearchParameter(query, 0, 1000, "", type);
        resolveURL=this.state.IRSAddress;
        registryIdentifier=registryID;
        let urlString=resolveURL+resolveRest+registryIdentifier;
        fetch(urlString)
            .then(function (response) {
                return response.text();
            })
            .then((result) => {
                try {
                    eval('(' + JSON.parse(result).listeners + ')').forEach((arrItem) => {
                        if (arrItem.url.indexOf("ws") != -1) {
                            wsURL = arrItem.url;
                        }
                    });
                    ws = doipClient.Connect(wsURL);
                }catch (e) {
                    console.log('error：' + '!' + e);
                    alert('输入的Repository的ID有误！');
                    return;
                }
                return ws;
            }).then((ws) => {
            if (ws) {
                doipClient.Search(registryID, ws, sp, this.display);
            } else {
                return;
            }
        }).catch((error) => {
                console.log('error is' + error);
                alert('error is' + error);
                return;
            }
        );
    }

    sendDelete(doID) {
        resolveURL=this.state.IRSAddress;
        let urlStringTODO=resolveURL+resolveRest+doID;
        fetch(urlStringTODO)
            .then(function (response) {
                return response.text();
            })
            .then((result) => {
                try {
                    // wsURL=eval('(' + JSON.parse(result).listeners + ')').shift().url;
                    // if(JSON.parse(result))
                    let resultJson=JSON.parse(result);
                    if(resultJson.hasOwnProperty("repository")){
                        repoIdentifier=resultJson.repository;
                        if (repoIdentifier) {
                            let urlString = resolveURL +resolveRest+ repoIdentifier;
                            console.log(urlString);
                            fetch(urlString)
                                .then(function (response) {
                                    return response.text();
                                })
                                .then((result) => {
                                    try {
                                        eval('(' + JSON.parse(result).listeners + ')').forEach((arrItem) => {
                                            if (arrItem.url.indexOf("ws") != -1) {
                                                wsURL = arrItem.url;
                                            }
                                        });
                                        ws = doipClient.Connect(wsURL);
                                    } catch (e) {
                                        console.log('error：' + '!' + e);
                                        alert('输入的IRS地址或者DO的ID有误！');
                                        return;
                                    }
                                    return ws;
                                }).then((ws) => {
                                if (ws) {
                                    doipClient.Delete(doID, ws, this.display);
                                } else {
                                    return;
                                }
                            });
                        }

                    }else{
                        console.log('输入的DO的ID有误或不存在此DO！');
                        alert('输入的DO的ID有误或不存在此DO！');
                        return ;
                    }
                }catch (e) {
                    console.log('error：' + '!' + e);
                    alert('输入的IRS地址或者DO的ID有误！');
                    return ;
                }
            }).catch((error) => {
            console.log('error is' + error);
            alert('error is' + error);
            return;
        });
    }
    componentDidMount() {

    }

    render() {
        return (
            <Container fluid>
                <div style={{backgroundColor: "#3a81ea", marginTop: "1.5%"}}><h1 className="text-center"
                                                                                 style={{color: "#fff"}}>北大数瑞DOA客户端</h1>
                </div>
                <div className="row" style={{borderBottom: "3px inset", paddingTop: "1%", paddingBottom: "1%"}}>
                    <InputGroup className="mb-3">
                        <InputGroup.Text>请先选择操作:</InputGroup.Text>
                        <DropdownButton
                            variant="outline-secondary"
                            title={this.state.dropdownTitle}
                            id="Operation"
                            onSelect={(selectedKey) => {
                                this.setState({'dropdownTitle': selectedKey});
                                this.setState ( {
                                    'helloBody': '',
                                    'listOperationBody': '',
                                    'retrieveBody': '',
                                    'createBody': '',
                                    'deleteBody': '',
                                    'updateBody': '',
                                    'searchBody': '',
                                    'helloHeader': '',
                                    'listOperationHeader': '',
                                    'retrieveHeader': '',
                                    'createHeader': '',
                                    'deleteHeader': '',
                                    'updateHeader': '',
                                    'searchHeader': ''
                                });
                            }}
                        >
                            <Dropdown.Item eventKey="Hello">0.DOIP/Op.Hello</Dropdown.Item>
                            <Dropdown.Item eventKey="ListOperation">0.DOIP/Op.ListOperation</Dropdown.Item>
                            <Dropdown.Item eventKey="Retrieve">0.DOIP/Op.Retrieve</Dropdown.Item>
                            <Dropdown.Item eventKey="Create">0.DOIP/Op.Create</Dropdown.Item>
                            <Dropdown.Item eventKey="Delete">0.DOIP/Op.Delete</Dropdown.Item>
                            <Dropdown.Item eventKey="Update">0.DOIP/Op.Update</Dropdown.Item>
                            <Dropdown.Item eventKey="Search">0.DOIP/Op.Search</Dropdown.Item>
                        </DropdownButton>
                        <Col sm md lg xl="2"></Col>
                        <InputGroup.Text className="textAlign" style={{textAlign:"right"}}>请输入IRS的地址：</InputGroup.Text>
                        <Form.Control placeholder="http://127.0.0.1:10001/" value={this.state.IRSAddress}
                                      onChange={(event) =>
                                          this.setState({'IRSAddress': event.target.value})
                                      }/>
                    </InputGroup>
                </div>
                <div className="row" style={{borderBottom: "3px inset"}}>

                    <Col sm md lg xl="6" className="justify-content-center"
                         style={{borderRight: "3px inset", paddingTop: "4%", paddingBottom: "15%"}}>
                        {
                            this.state.dropdownTitle === 'Hello' ?
                                <Hello sendHello={this.sendHello}/>
                                : this.state.dropdownTitle === 'ListOperation' ?
                                <ListOperation sendlistOperations={this.sendlistOperations}/>
                                : this.state.dropdownTitle === 'Retrieve' ?
                                    <Retrieve sendRetrieve={this.sendRetrieve}/>
                                    : this.state.dropdownTitle === 'Create' ?
                                        <Create sendCreate={this.sendCreate}/>
                                        : this.state.dropdownTitle === 'Delete' ?
                                            <Delete sendDelete={this.sendDelete}/>
                                            : this.state.dropdownTitle === 'Update' ?
                                                <Update sendUpdate={this.sendUpdate}/>
                                                : this.state.dropdownTitle === 'Search' ?
                                                    <Search sendSearch={this.sendSearch}/>
                                                    :""
                        }
                    </Col>
                    <Col sm md lg xl="6">
                        <h3 className="text-center">响应结果</h3>
                        <br/>
                        <h5>Header:</h5>
                        <p style={{wordBreak:"break-all", wordWrap:"break-all"}}>
                            {
                                this.state.dropdownTitle === 'Hello' ?
                                    this.state.helloHeader
                                    : this.state.dropdownTitle === 'ListOperation' ?
                                    this.state.listOperationHeader
                                    : this.state.dropdownTitle === 'Retrieve' ?
                                        this.state.retrieveHeader
                                        : this.state.dropdownTitle === 'Create' ?
                                            this.state.createHeader
                                            : this.state.dropdownTitle === 'Delete' ?
                                                this.state.deleteHeader
                                                : this.state.dropdownTitle === 'Update' ?
                                                    this.state.updateHeader
                                                    : this.state.dropdownTitle === 'Search' ?
                                                        this.state.searchHeader
                                                        : ""
                            }
                        </p>
                        <br/>
                        <h5>Body:</h5>
                        <p style={{wordBreak:"break-all", wordWrap:"break-all"}}>
                            {
                                this.state.dropdownTitle === 'Hello' ?
                                    this.state.helloBody
                                    : this.state.dropdownTitle === 'ListOperation' ?
                                    this.state.listOperationBody
                                    : this.state.dropdownTitle === 'Retrieve' ?
                                        this.state.retrieveBody
                                        : this.state.dropdownTitle === 'Create' ?
                                            this.state.createBody
                                            : this.state.dropdownTitle === 'Delete' ?
                                                this.state.deleteBody
                                                : this.state.dropdownTitle === 'Update' ?
                                                    this.state.updateBody
                                                    : this.state.dropdownTitle === 'Search' ?
                                                        this.state.searchBody
                                                        : ""
                            }
                        </p>
                    </Col>
                </div>
            </Container>
        );
    }
}
export {ws,DOAClient} ;




