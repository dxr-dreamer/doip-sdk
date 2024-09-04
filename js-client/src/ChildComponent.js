import React, {Component} from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import {Button, Container, Form, FormControl, InputGroup, Table} from "react-bootstrap";

class Hello extends React.Component {
    constructor(props) {
        super(props);
        this.state = {repositoryID: ''}
    }

    render() {
        return (
            <Container fluid>
                <div style={{textAlign:"center"}}>
                    <h4 >Say Hello!</h4>
                    <Form.Group>
                        <Form.Label style={{fontSize:"1.5rem"}}>请输入Repository的ID：</Form.Label>
                        <Form.Control placeholder="identifier" onChange={(event) => this.setState({'repositoryID': event.target.value})}/>
                    </Form.Group>
                    <br/>
                    <Button variant="primary" block onClick={() => this.props.sendHello(this.state.repositoryID)}>确定</Button>
                </div>
            </Container>

        )
    }
}

class Delete extends React.Component {
    constructor(props) {
        super(props);
        this.state = {doID: ''}
    }

    render() {
        return (
            <Container fluid>
                <div style={{textAlign:"center"}}>
                    <Form>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入DO的ID：</Form.Label>
                            <Form.Control placeholder="identifier" onChange={(event) => this.setState({'doID': event.target.value})}/>
                        </Form.Group>
                        <br/>
                        <Button variant="primary" block onClick={() => this.props.sendDelete(this.state.doID)}>
                            确定
                        </Button>
                    </Form>
                </div>
            </Container>

        )
    }
}

class ListOperation extends React.Component {
    constructor(props) {
        super(props);
        this.state = {doID: ''}
    }

    render() {
        return (
            <Container fluid>
                <div style={{textAlign:"center"}}>
                    <Form>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入DO的ID：</Form.Label>
                            <Form.Control placeholder="identifier" onChange={(event) => this.setState({'doID': event.target.value})}/>
                        </Form.Group>
                        <br/>
                        <Button variant="primary" block onClick={() => this.props.sendlistOperations(this.state.doID)}>
                            确定
                        </Button>
                    </Form>
                </div>
            </Container>

        )
    }
}

class Retrieve extends React.Component {
    constructor(props) {
        super(props);
        this.state = {doID: '',elementID:'',includeElementData:"true"}
    }

    render() {
        return (
            <Container fluid>
                <div style={{textAlign:"center"}}>
                    <Form>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入DO的ID：</Form.Label>
                            <Form.Control placeholder="identifier" onChange={(event) => this.setState({'doID': event.target.value})}/>
                        </Form.Group>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入Element的ID：</Form.Label>
                            <Form.Control placeholder="identifier" onChange={(event) => this.setState({'elementID': event.target.value})}/>
                        </Form.Group>
                        <br/>
                        <Form.Group>
                            <Form.Label style={{fontSize: "1rem"}}>请选择includeElementData参数的值：</Form.Label>
                            <Form.Check
                                inline
                                label="True"
                                name="group1"
                                checked={
                                    this.state.includeElementData=='true'?true:false
                                }
                                value="true"
                                type="radio"
                                id={`1`}
                                onChange={(event) => this.setState({'includeElementData': event.target.value})}
                            />
                            <Form.Check
                                inline
                                label="False"
                                value="false"
                                checked={
                                    this.state.includeElementData=='false'?true:false
                                }
                                name="group1"
                                type="radio"
                                id={`2`}
                                onChange={(event) => this.setState({'includeElementData': event.target.value})}
                            /> </Form.Group> <br/>
                        <Button variant="primary" block onClick={() => this.props.sendRetrieve(this.state.doID,this.state.elementID,this.state.includeElementData)}>
                            确定
                        </Button>
                    </Form>
                </div>
            </Container>

        )
    }
}

class Search extends React.Component {
    constructor(props) {
        super(props);
        this.state = {registryID: '',query:'',type:"id"}
    }

    render() {
        return (
            <Container fluid>
                <div style={{textAlign:"center"}}>
                    <Form>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入Registry的ID：</Form.Label>
                            <Form.Control placeholder="identifier" onChange={(event) => this.setState({'registryID': event.target.value})}/>
                        </Form.Group>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入查询内容：</Form.Label>
                            <Form.Control placeholder="identifier" onChange={(event) => this.setState({'query': event.target.value})}/>
                        </Form.Group>
                        <br/>
                        <Form.Group>
                            <Form.Label style={{fontSize: "1rem"}}>请选择type参数的值：</Form.Label>
                            <Form.Check
                                inline
                                label="id"
                                name="group1"
                                value="id"
                                checked={
                                    this.state.type=='id'?true:false
                                }
                                type="radio"
                                id={`1`}
                                onChange={(event) => this.setState({'type': event.target.value})}
                            />
                            <Form.Check
                                inline
                                label="full"
                                value="full"
                                name="group1"
                                checked={
                                    this.state.type=='full'?true:false
                                }
                                type="radio"
                                id={`2`}
                                onChange={(event) => this.setState({'type': event.target.value})}
                            /> </Form.Group> <br/>
                        <Button variant="primary" block onClick={() => this.props.sendSearch(this.state.registryID,this.state.query,this.state.type)}>
                            确定
                        </Button>
                    </Form>
                </div>
            </Container>

        )
    }
}

class Create extends React.Component {
    constructor(props) {
        super(props);
        this.state = {repositoryID: '',doString:''}
    }

    render() {
        return (
            <Container fluid>
                <div style={{textAlign:"center"}}>
                    <Form>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入Repository的ID：</Form.Label>
                            <Form.Control placeholder="identifier" onChange={(event) => this.setState({'repositoryID': event.target.value})}/>
                        </Form.Group>
                        <br/>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入DO信息：</Form.Label>
                            <textarea className="form-control" placeholder="DO" style={{height:"160px",overflow:"auto"}} onChange={(event) => this.setState({'doString': event.target.value})}/>
                        </Form.Group>
                        <br/>
                        <Button variant="primary" block onClick={() => this.props.sendCreate(this.state.repositoryID,this.state.doString)}>
                            确定
                        </Button>
                    </Form>
                </div>
            </Container>

        )
    }
}

class Update extends React.Component {
    constructor(props) {
        super(props);
        this.state = {doString:''}
    }

    render() {
        return (
            <Container fluid>
                <div style={{textAlign:"center"}}>
                    <Form>
                        <Form.Group>
                            <Form.Label style={{fontSize:"1.5rem"}}>请输入要更新的DO信息：</Form.Label>
                            <br/>
                            <textarea className="form-control" style={{height:"160px",overflow:"auto"}}placeholder="DO" onChange={(event) => this.setState({'doString': event.target.value})}/>
                        </Form.Group>
                        <br/>
                        <Button variant="primary" block onClick={() => this.props.sendUpdate(this.state.doString)}>
                            确定
                        </Button>
                    </Form>
                </div>
            </Container>

        )
    }
}

export  {Hello,Delete,ListOperation,Retrieve,Search,Create,Update};