## Project Background

#### 1.DOA

![](https://gitee.com/BDWare/BDWare/raw/master/_static/imgs/DOAArchitecture.png)

Digital Object Architecture (DOA) is a logical extension of the existing Internet architecture. It supports data interoperability requirements between different systems, rather than just transferring digital information from one location on the Internet to another. The basic element of digital object architecture is digital object, which is a unified abstraction of any information system data resources. Digital objects are divided into four parts: identification, metadata, status data and entity. Wherein, the identifier permanently and uniquely points to a digital object, which is a permanent attribute of the digital object; Metadata is the description information of digital objects, which is used for the retrieval and discovery of digital objects; The status data includes the current position, access entry, access mode and other information of the digital object, which is used for positioning and accessing the digital object; An entity is the actual content of a digital object, which can be any bit sequence or a collection of a series of bit sequences. The separation of identification and status data also makes the identification of digital objects not tightly coupled with the access portal of digital objects, so that digital objects can be shared and accessed regardless of whether they are in the Internet environment, thus realizing the interoperability of information systems in a pervasive environment.

DO is an abstraction of Internet resources. DO represents meaningful or valuable resources in the Internet. The representation of DO can be a bit sequence or a collection of multiple bit sequences; It can also be a program that provides external interfaces. Just as each host in the Internet has an IP address as an identifier, each DO in the DOA also has an identifier, which is used to uniquely identify the DO. This identifier is called DOID.

There are two basic protocols in DOA: Identifier/Resolution Protocol (IRP) and Digital Object Interface Protocol (DOIP). There are three basic components in DOA, the Identifier/Resolution System is responsible for the identification and resolution of DO, the Repository System is responsible for the storage and access of DO, and the Registry System is responsible for the registry of DO metadata and the search of DO.

#### 2.DOIP

The DOIP protocol specifies a standard way for clients to interact with digital objects. Digital object service, or DOIP service, is responsible for managing digital objects. At the same time, the DOIP service itself can also be regarded as a digital object with a unique and resolvable identifier, and its specific content can be accessed based on the DOIP service. The DOIP protocol uses the IRP protocol to parse and assign various identifiers mentioned in the protocol. The maximum length of the identifier can change over time. Under initial conditions, the maximum number of identifiers in DOIP is 4096.

DOIP protocol is an application layer protocol, which specifies the syntax and semantics related to digital object management and access. Its bottom layer needs to transfer DOIP messages based on network communication protocol. The DOIP 2.0 protocol transmits DOIP messages based on TLS protocol, while a new message format is defined in DOIP 2.1, which can guarantee the sequence, integrity and security of messages. Therefore, DOIP messages can be transmitted through some unreliable communication protocols, such as UDP. The client can select the appropriate communication protocol according to the fields defined in DOIPServiceInfo.

In terms of security assurance, DOIP version 2.0 protocol provides a PKI based security assurance, which verifies the identity information of client and server through CA certificate, and encrypts the communication between client and server. The DOIP 2.1 protocol adds an authentication and security guarantee mechanism based on the identifier resolution system. Each user will be assigned a DOID as an identity. The user's public key information can be resolved according to the user's DOID, and the user's identity can be verified according to the public key information. By default, the public key is serialized to JWK format.

Type is one of the necessary attributes of each DO. One of the important functions of type is to enable DOIP services to identify the operations supported by the target DO. DOIP defines some basic types, and supports the extension of basic types, or the creation of new types. Type also has a DOID, and its corresponding identifier record can be resolved through IRP protocol. Extension types and new types are created by organizations or companies in their fields. Their identifier records describe the semantics, structure and other details of the types that are not defined in DOIP.

## Project Structure

![](https://gitee.com/BDWare/BDWare/raw/master/_static/imgs/DOIPSDKModule.png)

This project is a software development kit of DOIP protocol, which can be used to implement DOIP Client and DOIP Server (DO Repository/Registry).

The overall functional module of the project is shown in the figure, mainly including the core layer and endpoint layer.

#### 1. Core layer

The codec of the protocol is located in the src/main/codec directory:

Be responsible for serializing, encoding and decoding digital objects, and encoding or decoding the DoipMessage transmitted in the communication protocol.

#### 2. Endpoint layer

The protocol development API is located in the src/main/endpoint directory:

It provides the development interface of the server and the client, and can easily implement the server and client software based on different transmission protocols.

Currently, the SDK supports the use of udp, tcp, tls, websocket and other transport protocols. Please refer to the method of client and server in the src/test/java/org.bdware.doip.endpoint package.

## Prepare for Using
#### 1.gradle.properties
Before compiling the project, you need to create a new configuration file named gradle.properties in the root directory, and copy the following contents into the file:
```gradle
NEXUS_USERNAME=abc
NEXUS_PASSWORD=def
signing.keyId=ghi
signing.password=jkl
signing.secretKeyRingFile=mno
```
#### 2.Generate JWK key

DOIP Client and Server perform authentication and encrypted communication based on public and private keys, but do not limit the generation mechanism of public and private keys and the exchange method of public keys.

The DOIP SDK uses JWX as the public and private key standard by default, and JWK serialized key, JWS signature, and JWE encryption and decryption.

Relevant introduction of JWX can be referred to

- Introduction to JWK format https://blog.csdn.net/JosephThatwho/article/details/114876345
- JWK online generation https://mkjwk.org/
- Jwt official website https://jwt.io/
- Jwt inverse solution http://jwt.calebb.net/

Generally, the user's public and private key information will be saved in the identifier resolution system as the user's identifier record. When signature verification or encryption is enabled, the user information needs to be registered with the IRS:

1. Run irs/src/main/java/org. bdware. irs.mocked.IRSMockMain, the default startup port is 10001, and start IRS
2. Call client/src/test/java/InternalIrpClientTest/updateUserHandleRecord to load JWK and register
3. Then the user's public key can be resolved through the user ID

#### 3.TLS encryption certificate (required for TLS communication)

For TLS based clients and servers, you need to specify X509 certificates and private key files when starting. The corresponding certificates and files can be generated based on the keytools tool.

Please ensure that the keytools tool is installed in the environment, and then use the following command to generate the private key and certificate used by the server.

* Generate encryption certificate for Repository
```  tls
keytool -genkey -keyalg RSA -keysize 2048 -validity 365 -keypass 123456 -keystore doip_service_repository.keystore -storepass 123456 -dname "UID=86.5000.470/doip.RepositoryTLSService"  
```  
* Generate encryption certificate for Registry
```  tls
keytool -genkey -keyalg RSA -keysize 2048 -validity 365 -keypass 123456 -keystore doip_service_registry.keystore -storepass 123456 -dname "UID=86.5000.470/doip.RegistryTLSService"  
```


## Develop DO repository based on DOIP SDK and provide DOIP services：

Digital object repository includes the ability to store and manage digital objects, which needs to be handled in combination with specific scenarios.

Therefore, the DOIP SDK itself does not contain a fully implemented digital object repository, but it provides corresponding interfaces so that developers can easily develop a digital object repository that meets their needs.

### 1. Implement Server Handler

Handler is the specific implementation of server processing requests. The main difference between different server applications is that they use different Handler.

The input of each handler method is the received DOIP request message, and the return value is the corresponding DOIP response message.

Different methods are given different annotations. When receiving a DOIP message, it will judge its operation code to call the corresponding operation according to the DOIPMessage. MessageHeader. HeaderParameter. operation.

Refer to BDRepositoryHandler for the implementation of this interface in the project. For the inherited basic DOIP operation BasicOperations, it is not necessary to add annotations to the instantiated method. When RequestHandlerImpl is initialized, it will search for annotations layer by layer according to its integrated method until it is found  annotations on RepositoryHandler and RegistryHandler methods.

By default, the RepositoryHandler of the repository system should contain six basic operations specified in the protocol, which are represented as the RepositoryHandler interface in the SDK:
```java
public interface RepositoryHandler {
    @Op(op = BasicOperations.Hello)
    DoipMessage handleHello(DoipMessage request);

    @Op(op = BasicOperations.ListOps)
    DoipMessage handleListOps(DoipMessage request);

    @Op(op = BasicOperations.Create)
    DoipMessage handleCreate(DoipMessage request);

    @Op(op = BasicOperations.Update)
    DoipMessage handleUpdate(DoipMessage request);

    @Op(op = BasicOperations.Delete)
    DoipMessage handleDelete(DoipMessage request);

    @Op(op = BasicOperations.Retrieve)
    DoipMessage handleRetrieve(DoipMessage request);
}
```

The RegistryHandler of the registry system must at least implement the search operation:
```java
public interface RegistryHandler extends RepositoryHandler{  
    @Op(op = BasicOperations.Search)  
    DoipMessage handleSearch(DoipMessage request);  
}
```
The SDK provides the developers with RepositoryHandlerBase class and RegistryHandlerBase class as the base classes of the handler of the repository system and the registry system, including some tools and methods that are helpful for development.

Developers can conveniently develop server handlers by inheriting base classes.

### 2. Configure server information

Server configuration information DoipServiceInfo mainly includes the following parameters:
```java
class DoipServiceInfo{
    String id;
    String serviceDescription;
    String publicKey;
    String serviceName;
    int port;
    String ipAddress;
    String protocol;
    String protocolVersion;
    List<DoipListenerConfig> listenerInfos;
    String owner;
    String repoType;
}
```
The port, ipAddress, protocol, and protocolVersion are set for compatibility with the DOIP v2.0 protocol. The DOIP v2.1 protocol uses ListenerInfo to describe the DOIP service entry, including url and protocolVersion. The url indicates the protocol and address supported by the service portal.
```java
class DoipListenerConfig{
    public String url;
    public String protocolVersion;
}
```
The following is a sample code of configuration information (json file). The listenerInfos parameter contains multiple listening ports, and different ports support different communication protocols. Currently, the SDK has four built-in protocols: TLS, TCP, UDP, and WebSocket:
```json
{
  "id": "localTest/doip.tcp",
  "serviceDescription": "testTCPServer",
  "publicKey": "{\"kty\":\"EC\",\"d\":\"VPvAXurYhEwCRbIuSCEPOaTyfUIbH6an4scA4BpdWCw\",\"use\":\"sig\",\"crv\":\"P-256\",\"kid\":\"86.5000.470\\/dou.TEST\",\"x\":\"IFVGcQ22vd7SEd1HsjcYuaLWUrfj4ochceom6YNCX4g\",\"y\":\"HSuB60fA_53vi4L30WiVQjouvAB0gSPAS8kf8Ny3RN0\"}",
  "serviceName": "testTCPServer",
  "listenerInfos": [
    {
      "url": "tcp://127.0.0.1:8001",
      "protocolVersion": "2.1"
    },
    {
      "url": "udp://127.0.0.1:8002",
      "protocolVersion": "2.1"
    }
  ],
  "owner": "86.5000.470/dou.TEST",
  "repoType": "registry"
}
```
The following methods can be used to directly convert the configuration file in JSON format to the configuration information of DoipServiceInfo type:
```java
    Reader infoStrReader = new FileReader("./config/conf.json");  
    DoipServiceInfo serviceInfo = new Gson().fromJson(infoStrReader,new TypeToken<DoipServiceInfo>(){}.getType());
```

### 3. Generate a server instance and start it

The SDK provides the developer with the DoipServerImpl class as the template class of the development server. The developer only needs to use the configuration information of the DoipServiceInfo type to generate the server:
```java
DoipServer server = new DoipServerImpl(DoipServiceInfo serviceInfo);
```
Then, set the processing logic through the DoipServer.setHandler method
```java
server.setRepositoryHandler(RepositoryHandler handler);
```
Start server:
```java
server.start();
```
The server will be based on the DoipServiceInfo.listenerInfos, start one or more listeners to accept DOIP requests, which will be processed by the corresponding Handler method and return a response message.

## Develop DOIP client based on DOIP SDK：
The DOIP SDK provides the DoipClient interface and the default interface to implement DoipClientImpl and access the DOIP service of the target repository node.

```java
public interface DoipClient {
    @Op(op = BasicOperations.Hello)
    void hello(String id, DoipMessageCallback cb);

    @Op(op = BasicOperations.ListOps)
    void listOperations(String id, DoipMessageCallback cb);

    @Op(op = BasicOperations.Retrieve)
    void retrieve(String id, String element, boolean includeElementData, DoipMessageCallback cb);

    @Op(op = BasicOperations.Create)
    void create(String targetDoipService, DigitalObject digitalObject, DoipMessageCallback cb);

    @Op(op = BasicOperations.Update)
    void update(DigitalObject digitalObject, DoipMessageCallback cb);

    @Op(op = BasicOperations.Delete)
    void delete(String id, DoipMessageCallback cb);

    @Op(op = BasicOperations.Search)
    void search(String id, SearchParameter sp, DoipMessageCallback cb);
}
```

#### 1. Generate client configuration information

Client configuration information ClientConfig mainly includes the following parameters:
```java
class ClientConfig{
    String url;                 //目标DOIP服务地址
    String protocolVersion;     //采用的DOIP协议版本
}
```

#### 2. Connect to the target DOIP service

The following is an example code for generating a client and configuring client information. By default, the protocol version is V2.1:
```java
        String targetDoipService = "udp://127.0.0.1:21042/";
        DoipClientImpl Client = new DoipClientImpl();
        Client.connect(ClientConfig.fromUrl(address));
```

#### 3. Access the target DOIP service and obtain the response result through the callback function

The DOIPClient processes the returned message asynchronously through the callback function DoipMessageCallback. The developer needs to implement the onResult method in DoipMessageCallback to process the response message specifically:
```java
public interface DoipMessageCallback {
    void onResult(DoipMessage msg);
}
```
A simple access example is shown below. The method calls the DoipClient.retrieve interface and sends a request to the target server to retrieve DOs:
```java
public static void retrieve(String DOID) throws InterruptedException {
    String targetDoipService = "udp://127.0.0.1:21042/";
    DoipClientImpl Client = new DoipClientImpl();
    Client.connect(ClientConfig.fromUrl(address));
    boolean flag = true;
    client.retrieve(DOID,"",false, msg -> {
        try {
            flag = false;
            DigitalObject  ret_DO = msg.body.getDataAsDigitalObject();
            System.out.println(new Gson().toJson(msg.body.getDataAsDigitalObject()));
        } catch (DoDecodeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
    while(flag){
        System.out.println("Wait For Response");
        Thread.sleep(5000);
    }
}
```

## Expand DOIP SDK

### 1. Expand DOIP operation

In addition to the seven basic operations of DOIP, DOIP also supports method extension, but annotations need to be added to the extended method to match the new operation code. For the annotation form of the extension operation code, see the following code:
```java
public class BDRegistryHandler extends RegistryHandlerBase {
    /*
        .....
    */
    @Op(op = BasicOperations.Extension,name = "Op.Join")
    public DoipMessage handleJoin(DoipMessage request){
            if(request.header.parameters.attributes == null ||request.header.parameters.attributes.get("repoID")== null){
                logger.info("invalid join request: repoID not found");
                return replyStringWithStatus(request, "invalid join request: repoID not found.", DoipResponseCode.Invalid);
            }
            String repoID = request.header.parameters.attributes.get("repoID").getAsString();
            String repoUrl = request.header.parameters.attributes.get("repoUrl").getAsString();
            inRegs.put(repoID,repoUrl);
            saveRegFederation();
            return replyString(request,"success");
        }
    /*
        .....
    */
}
```
In the above example, BDRegistryHandler implements the RegistryHandler interface. In addition to the seven basic DOIP operations, it also extends the Join operation.

Taking the Join method as an example, add the annotation @ Op (op=BasicOperations. Extension, name="Op. Join") to the handleJoin method

Where op represents that this method is an extended method, and name represents the operation code to be matched.

With the extension method, BDRegistryHandler can process the message with the operation of Op.Join in the DOIP message,

### 2. Expand the new communication protocol

The bottom layer of DOIP protocol can transfer messages based on any communication protocol. This project implements TCP, UDP, TLS and Websocket protocols based on Netty. Developers can also expand other communication protocols according to actual needs.

DOIP based on the new communication protocol needs to implement two interfaces: DOIPListener and DOIPClientChannel, which are respectively used for the server (Repository/Registry) and the client.
```java
public interface DoipListener {
    void start();
    void stop();
    void setRequestHandler(DoipRequestHandler handler);
}
```

```java
public interface DoipClientChannel {
    void sendMessage(DoipMessage request, DoipMessageCallback callback);

    void close();

    void connect(String url) throws URISyntaxException, InterruptedException;

    boolean isConnected();
}
```

Then dynamically configure the extended communication protocol through the methods of DoipListenerGenerator.addListener and DoipClientChannelGenerator.addClientChannel respectively.

The server and client will select the corresponding Listener/ClientChannel connection according to the schema of the url when starting.

For an extended DOIP based on Bluetooth protocol, see [Android DOIP Repository]（ https://gitee.com/blessser/DoRepoAtPhone ）, where BlueToothDoipListener is the Bluetooth protocol implementation.

Client implementation reference repository [Android DOIP Client]（ https://gitee.com/blessser/doip-android-client.git ）, where BlueToothDoipClient implements the DoipClientChannel method.

## Related Protocol
- [DOIPv2.1 protocol standard](http://doa-atsd.org/task.group.2/DOIP.recommendation.2022-2-24.pdf)