## 一、项目背景

#### 1.DOA

![](https://gitee.com/BDWare/BDWare/raw/master/_static/imgs/DOAArchitecture.png)

数字对象架构（Digital Object Architecture, DOA）是现有Internet体系结构的一个逻辑扩展，它支持不同系统之间的数据互操作需求，而不仅仅是将数字形式的信息从Internet的一个位置传送到另一个位置。数字对象体系结构中的基本元素是数字对象（Digital Object），是对任意信息系统数据资源的统一抽象。数字对象分为四个部分：标识、元数据、状态数据以及实体。其中，标识永久且唯一的指向一个数字对象，是数字对象永久不变的属性；元数据是数字对象的描述信息，用于数字对象的检索和发现；状态数据包含数字对象当前位置、访问入口、访问方式等信息，用于数字对象的定位和访问；实体则是数字对象所包含的实际内容，可以是任何比特序列或一系列比特序列的集合。标识和状态数据的分离也使得数字对象的标识不在与数字对象的访问入口紧耦合，使得无论是否在互联网环境中，数字对象都可以被共享、访问，进而实现普适环境下的信息系统互操作。

DO是对互联网资源的抽象，DO代表互联网中有意义或者是有价值的资源。DO的表现形式可以是一个比特序列，或者是多个比特序列的集合；也可以是一个对外提供接口的程序。就像互联网中每一个Host都有一个IP地址作为标识一样，在DOA中每个DO同样有一个标识，用于对这个DO进行唯一的识别，这个标识叫做DOID。

DOA中有两个基本协议：标识/解析协议IRP(Identifier/Resolution Protocol)和数字对象接口协议DOIP(Digital Object Interface Protocol)。DOA中有三个基本的组成部分，分别是标识/解析系统(Identifier/Resolution System)负责DO的标识和解析、仓库系统(Repository System)负责DO的存储和访问、以及注册系统负责DO元数据的注册和DO的搜索(Registry System)。

#### 2.DOIP
DOIP协议规定了客户端和数字对象进行交互的标准方式。数字对象服务，即DOIP服务，负责管理数字对象。同时，DOIP服务本身也可以视为一种数字对象，具有唯一的可解析的标识，并且可以基于DOIP服务访问其具体内容。DOIP协议使用IRP协议来解析和分配协议中所提到的各种标识。标识的最大长度可以随着时间变化，初始条件下，DOIP中标识的最大位数是4096位。

DOIP协议是应用层协议，规定了与数字对象管理、访问相关的语法和语义，其底层需要基于网络通讯协议来传递DOIP消息。DOIP 2.0 版本协议基于TLS协议传递DOIP消息，而在DOIP 2.1 版本中定义了一种新的消息格式，可以对消息的顺序性、完整性和安全性提供保障。因此，DOIP消息可以通过某些不可靠的通信协议（如UDP）进行传输。客户端可以根据DOIPServiceInfo中定义的字段来选择合适的通信协议。

在安全性保障方面，DOIP 2.0 版协议提供了一种基于PKI的安全性保障，通过CA证书验证客户端和服务端的身份信息，以及客户端和服务端的通讯加密。DOIP 2.1 版本协议则新增了一种基于标识解析系统的身份验证和安全性保障机制。每个用户均会被分配一个DOID作为身份标识，根据用户的DOID可以解析到用户的公钥信息，根据公钥信息即可验证用户的身份。默认情况下，公钥会被序列化为JWK的格式。

类型是每个DO的必要属性之一，类型的重要功能之一就是能使DOIP服务识别目标DO所支持的操作。DOIP中定义了一些基本类型，并支持对基本类型的扩展，或者创建新的类型。类型同样拥有一个DOID，并且可以通过IRP协议解析其对应的标识记录。扩展类型及新增类型由专业领域的组织或公司来负责在其领域中的类型创建，其标识记录中描述该类型的语义、结构以及其他的一些没有在DOIP中定义的细节。

## 二、项目结构

![](https://gitee.com/BDWare/BDWare/raw/master/_static/imgs/DOIPSDKModule.png)

本项目是DOIP协议的软件开发套件SDK，可以基于本SDK实现DOIP Client和DOIP Server（DO Repository/Registry）。
项目整体功能模块如图所示，主要包括核心层和端点层两部分。

#### 1.核心层
协议的编解码器，在src/main/codec目录：
负责对数字对象进行序列化编解码，以及对在通信协议中传输的DoipMessage进行编、解码操作。

#### 2.端点层
协议端点的开发框架，在src/main/endpoint目录：
提供服务器和客户端的开发接口，可以方便的实现基于不同传输协议下的服务器和客户端软件。
当前SDK支持使用 udp、tcp、tls、websocket 等传输协议，可参考：src/test/java/org.bdware.doip.endpoint包内的client与server的写法。

## 三、使用前的准备
#### 1.gradle.properties
项目编译前，需要在根目录下新建一个配置文件，文件名为gradle.properties，复制以下内容到文件内：
```gradle
NEXUS_USERNAME=abc
NEXUS_PASSWORD=def
signing.keyId=ghi
signing.password=jkl
signing.secretKeyRingFile=mno
```
#### 2.生成JWK密钥
DOIP Client和Server基于公私钥来进行身份验证和加密通讯，但并不限定公私钥的生成机制以及公钥的交换方式。
DOIP SDK默认采用JWX作为公私钥标准，JWK序列化密钥、JWS签名、JWE加解密。

有关JWX的相关介绍可以参考
- JWK格式简介 https://blog.csdn.net/JosephThatwho/article/details/114876345
- JWK在线生成 https://mkjwk.org/
- jwt官网 https://jwt.io/
- jwt反解 http://jwt.calebb.net/

通常情况下，用户的公私钥信息会作为用户的标识记录保存在标识解析系统中，在启用签名验证或加密时需要先将用户信息注册至IRS:
1. 运行irs/src/main/java/org.bdware.irs.mocked.IRSMockMain,默认启动端口为10001，启动IRS
2. 调用client/src/test/java/InternalIrpClientTest/updateUserHandleRecord 加载JWK并注册
3. 然后可以通过用户标识，解析用户公钥


#### 3.TLS加密证书（TLS通讯必须）
基于TLS协议的客户端与服务端，在启动时需要指定X509证书和私钥文件。相应证书、文件可以基于keytools工具生成。
请确保环境中安装有keytools工具，然后使用如下命令，生成服务端使用的私钥和证书。
* 为Repository生成加密证书
```  tls
keytool -genkey -keyalg RSA -keysize 2048 -validity 365 -keypass 123456 -keystore doip_service_repository.keystore -storepass 123456 -dname "UID=86.5000.470/doip.RepositoryTLSService"  
```  
* 为Registry生成加密证书
```  tls
keytool -genkey -keyalg RSA -keysize 2048 -validity 365 -keypass 123456 -keystore doip_service_registry.keystore -storepass 123456 -dname "UID=86.5000.470/doip.RegistryTLSService"  
```


## 四、基于SDK开发数字对象仓库，提供DOIP服务：

数字对象仓库包含了对数字对象存储、管理等能力，需要结合具体的场景具体处理。
因此，DOIP SDK本身并不包含一个完整实现的数字对象仓库，但提供了相应的接口，开发者可以便捷的开发出符合自己需求的数字对象仓库。

### 1.实现服务器Handler
Handler是服务器处理请求的具体实现，不同的服务器应用之间的主要区别就在于使用了不同的Handler实现方式。
Handler每个方法的输入为接受到的DOIP请求消息，返回值为对应的DOIP响应消息。

不同的方法被赋予了不同的注解，在接收到DOIP消息时，会根据DOIPMessage.MessageHeader.HeaderParameter.operation判断其操作码调用相应的操作。
项目中对该接口的实现参考BDRepositoryHandler。对于继承的基本DOIP操作BasicOperations，不需要在实例化的方法上增加注解，在RequestHandlerImpl初始化时会根据其集成的方法逐层向上查找注解，直至找到RepositoryHandler和RegistryHandler方法上的注解。

缺省情况下，仓库系统的RepositoryHandler应该包含协议规定的六种基本操作，在SDK中表现为RepositoryHandler接口：
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

注册系统的RegistryHandler至少还需实现Search操作：
```java
public interface RegistryHandler extends RepositoryHandler{  
    @Op(op = BasicOperations.Search)  
    DoipMessage handleSearch(DoipMessage request);  
}
```
SDK为开发者提供了RepositoryHandlerBase类和RegistryHandlerBase类做为开发仓库系统和注册系统Handler的基类，包含一些有助于开发的工具方法。
开发者可以通过继承基类来便捷的开发服务器的Handler。

### 2.配置服务器信息
服务器配置信息DoipServiceInfo主要包含以下参数：
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
其中 port、ipAddress、protocol、protocolVersion是为和DOIP v2.0协议兼容所设。DOIP v2.1协议使用ListenerInfo描述DOIP服务入口，包含url，protocolVersion两部分。其中url指明了服务入口所支持的协议以及地址。
```java
class DoipListenerConfig{
    public String url;
    public String protocolVersion;
}
```
下面给出一个配置信息（json文件）的示例代码，其中的listenerInfos参数中包含了多个监听端口，并且不同端口支持使用不同的通信协议，目前SDK中内置了TLS、TCP、UDP以及WebSocket四种协议：
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
可采用如下方法，直接将JSON格式的配置文件转换成DoipServiceInfo类型的配置信息：
```java
    Reader infoStrReader = new FileReader("./config/conf.json");  
    DoipServiceInfo serviceInfo = new Gson().fromJson(infoStrReader,new TypeToken<DoipServiceInfo>(){}.getType());
```

### 3.生成服务器实例并启动
SDK为开发者提供了DoipServerImpl类作为开发服务器的模板类，开发者只需要使用DoipServiceInfo类型的配置信息即可生成服务器：
```java
DoipServer server = new DoipServerImpl(DoipServiceInfo serviceInfo);
```
随后，通过DoipServer.setHandler方法，设置处理逻辑
```java
server.setRepositoryHandler(RepositoryHandler handler);
```
启动服务器：
```java
server.start();
```
服务器会根据DoipServiceInfo.listenerInfos，启动一个或多个监听，接受DOIP请求，请求会交由相应的Handler方法处理，并返回响应消息。

## 五、基于DOIP SDK开发DOIP客户端：
DOIP SDK提供了以及DoipClient接口以及缺省接口实现DoipClientImpl，访问目标仓库节点的DOIP服务。

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

#### 1.生成客户端配置信息
客户端配置信息ClientConfig主要包含以下参数：
```java
class ClientConfig{
    String url;                 //目标DOIP服务地址
    String protocolVersion;     //采用的DOIP协议版本
}
```

#### 2.连接目标DOIP服务
以下是生成一个客户端并配置客户端信息的示例代码，默认情况下协议版本为V2.1：
```java
        String targetDoipService = "udp://127.0.0.1:21042/";
        DoipClientImpl Client = new DoipClientImpl();
        Client.connect(ClientConfig.fromUrl(address));
```

#### 3.访问目标DOIP服务并通过回调函数获取响应结果
DOIPClient通过回调函数DoipMessageCallback异步处理返回的消息，开发者需要实现DoipMessageCallback中的onResult方法，具体处理响应消息：
```java
public interface DoipMessageCallback {
    void onResult(DoipMessage msg);
}
```
一个简单的访问示例入下所示,方法调用DoipClient.retrieve接口，向目标服务器发送了检索DO的请求（Retrieve）：
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

## 六、扩展DOIP SDK

### 1. 扩展DOIP操作
除了DOIP的7个基本操作之外，DOIP同样支持方法的扩展，但需要为扩展的方法增加注解以匹配新的操作码。扩展操作码的注解形式可参见如下代码：
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
上述例子中BDRegistryHandler实现了RegistryHandler接口，除了实现基础的7个DOIP操作外还扩展实现了Join操作。
以Join方法为例，通过为handleJoin方法添加注解@Op(op = BasicOperations.Extension,name = "Op.Join")
其中op代表此方法为扩展的方法，name即为需要匹配的操作码。

通过扩展方法，BDRegistryHandler可以处理DOIP消息中operation为Op.Join的消息，

### 2. 扩展新的通讯协议
DOIP协议底层可以基于任意的通讯协议传递消息，本项目基于Netty实现了TCP,UDP,TLS以及Websocket协议，开发人员也可以结合实际需求扩展其他通讯协议。

基于新通讯协议的DOIP需要实现DOIPListener和DOIPClientChannel两个接口，分别用于服务端（Repository/Registry）和客户端。
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

然后分别通过DoipListenerGenerator.addListener和DoipClientChannelGenerator.addClientChannel方法动态配置扩展通讯协议。
服务端和客户端在启动时会根据url的schema选择相应的Listener/ClientChannel连接。

一个扩展的基于Bluetooth协议的DOIP可参见[Android DOIP Repository](https://gitee.com/blessser/DoRepoAtPhone)，其中BlueToothDoipListener即为Bluetooth协议实现。

客户端实现参考仓库[Android DOIP Client](https://gitee.com/blessser/doip-android-client.git)，其中BlueToothDoipClient实现了DoipClientChannel方法。

## 七、相关协议
- [DOIPv2.1协议标准](http://doa-atsd.org/task.group.2/DOIP.recommendation.2022-2-24.pdf)