# 1. 概述
数字对象体系（Digital Object Architecture，下面简称为DOA）是图灵奖得主、互联网发明人Robert E. Kahn提出的一种实现异构信息系统互操作的新型计算架构。

在DOA架构中，将信息资源抽象表示为数字对象（Digital Object，下面简称DO），所有的DO都存储在DO Repository中，并且支持通过数字对象接口协议（Digital Object Interface Protocol，下面简称DOIP）来对外提供服务。每一种DO具有全球可解析的唯一标识，可以通过标识与解析协议（Identifier/Resolution Protocol，下面简称IRP）向Handle System进行注册与解析。同时每一个DO也具有对该DO进行描述的元信息，元信息存储在DO Registry（一种特殊的DO Repository）中，也支持通过DOIP来对外提供服务。

根据上面的描述，在DOA架构中，有三种构件和两种协议。三种构件分别是：Handle System，DO Repository和DO Registry。两种协议分别是：DOIP和IRP。北京大学提供了DOIP和IRP的代码实现，并提供了用于开发DO Repository，DO Registry以及Client的软件开发工具包（Software Development Kit，简称SDK）。服务提供者，可以基于该SDK实现DO Repository和DO Registry功能。同时，我们提供用于对接国内主流Handle System提供者的支持代码，比如中数、中科院网络中心等。服务提供者可以基于该代码开发用于提供标识与解析的服务。最终使用者，可以基于Client的SDK开发基于DOA的应用程序。

# 2. 快速上手
## 2.1 环境准备
相关的代码均采用Java编写，在使用相关代码前，请确保本地已经安装了JDK(>= Java 1.8)。

## 2.2 下载示例代码
* example-Repository [v1.0](https://gitee.com/BDWare/doip-compatibility-tool/blob/master/src/main/java/org/bdware/example/SimpleRepository.java)
* example-Registry [v1.0](https://gitee.com/BDWare/doip-compatibility-tool/blob/master/src/main/java/org/bdware/example/SimpleRegistry.java)
* example-Client [v1.0](https://gitee.com/BDWare/doip-compatibility-tool/blob/master/src/main/java/org/bdware/example/Client.java)
## 2.3 运行代码
下载回来的示例代码是一个压缩包，解压后包含一个jar包和一个conf文件夹（里面包含示例代码运行需要使用的配置文件）。
### 2.3.1 使用Client与Repository进行交互
1. 启动Respository
将Repository示例代码解压后，打开命令窗口，进入包含jar包的目录，此时目录内容如下：
```bash
ls
keys libs default_repo.json SimpleRepository-1.0.jar
```
执行如下命令，开启Repository：
```bash
java -jar SimpleRepository-1.0.jar
```
此时，命令行出现如下日志信息，表示Repository启动成功。
```bash
java -jar .\SimpleRepository-1.0.jar
[INFO ]16:34:22.248 load config from: default_repo.json (DoipServiceConfig.java:24)
[INFO ]16:34:23.623 DOIPServiceInfo: {"id":"86.5000.470/doip.localTcpRepo","serviceDescription":"test local Repository","publicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwUjtYUciJbdU5BYYFYykS33+1Wnm8KHf9+0/9EolcIWYro7xX8x9OHwF3nYu8xfhjjKo6Ma4WzI/ON5UO447eynCSKbvzJZV01rzuk03ED9rWfYQQyZrI6WyDtLAnwkG4ZJ+9ik75F+JF5YRHNb7bU0VU7FEtPS16uzF9twHi9JzZcfI8fwFKPO9Xzp6UagHzibIuRTLmU+Jl4jnzvU4MJsQlfcmyMM7VWM8oHCkd9NzC+XIr7GH/a3zcI8WEY9D1mLrwYR8UERd6rqSJ3W03O21s4+lLOGgGoRmtJERVqwK2TF789Fa7N9+9bZJJ3dAaP16iynHfOl5tb7Jmr4tLwIDAQAB","serviceName":"TestMultiTransProtocol","port":1718,"ipAddress":"127.0.0.1","protocol":"tls","protocolVersion":"2.0","listenerInfos":[{"url":"tls://127.0.0.1:1718","protocolVersion":"2.0","messageFormat":"delimiter"}],"owner":"86.5000.470/dou.SUPER"} (DoipServerImpl.java:34)
[INFO ]16:34:24.965 start at:1718 (NettyTLSDoipListener.java:63)
```

2. 修改客户端配置文件
将Client示例代码解压后，打开命令窗口，进入包含jar包的目录，此时目录内容如下：
```bash
ls
keys libs Client-1.0.jar default_client.json
```

配置文件default_client.json的默认内容为（默认情况下会连接步骤1中启动的Repository）：
```
{
  "repoID": "86.5000.470/doip.localTcpRepo",
  "repoURL": "tls://127.0.0.1:1718",
}
```

3. 使用Client发送操作请求
   Client是一个DOIP客户端命令行工具，支持DOIP的7种基本操作。不加参数直接执行，会显示工具的帮助信息，如下：
```
java -jar .\Client-1.0.jar
[INFO ]16:51:08.043 load config from: default_client.json (DOIPCMDClient.java:192)
usage: DOIPCMDClient [-c <arg>] [-d <arg>] [-h] [-i] [-l] [-r <arg>] [-s <arg>] [-u <arg>]
basic doip operations to illustrate usage of the protocol

 -c,--create <arg>     create do in doip repository/registry, e.g. create do: -c 86.5000.470/do.test DO message, create
                       meta: -c 86.5000.470/do.test Meta description
 -d,--delete <arg>     delete do in doip repository/registry, e.g. -d 86.5000.470/do.test
 -h                    display this help message
 -i,--hello            get doip service information, e.g. -i
 -l,--list             get repository/registry supported operations, e.g. -l
 -r,--retrieve <arg>   retrieve do in doip repository/registry, e.g. -r 86.5000.470/do.test
 -s,--search <arg>     search meta info in registry, e.g. -s key_word
 -u,--update <arg>     update do in doip repository/registry, e.g. -u 86.5000.470/do.test DO new_message

Please report issues at https://gitee.com/BDWare/doip-sdk.git
```
（1）发送hello操作请求
```
java -jar .\Client-1.0.jar -i
[INFO ]16:56:32.280 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:56:34.777 client sending message: {"requestId":"1701103136","targetId":"86.5000.470/doip.localTcpRepo","operationId":"0.DOIP/Op.Hello","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:56:35.353 client received message: {"requestId":"1701103136","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/doip.localTcpRepo","type":"0.TYPE/DO.DOIPServiceInfo","attributes":{"serviceName":"TestMultiTransProtocol","serviceDescription":"test local Repository","owner":"86.5000.470/dou.SUPER","publicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwUjtYUciJbdU5BYYFYykS33+1Wnm8KHf9+0/9EolcIWYro7xX8x9OHwF3nYu8xfhjjKo6Ma4WzI/ON5UO447eynCSKbvzJZV01rzuk03ED9rWfYQQyZrI6WyDtLAnwkG4ZJ+9ik75F+JF5YRHNb7bU0VU7FEtPS16uzF9twHi9JzZcfI8fwFKPO9Xzp6UagHzibIuRTLmU+Jl4jnzvU4MJsQlfcmyMM7VWM8oHCkd9NzC+XIr7GH/a3zcI8WEY9D1mLrwYR8UERd6rqSJ3W03O21s4+lLOGgGoRmtJERVqwK2TF789Fa7N9+9bZJJ3dAaP16iynHfOl5tb7Jmr4tLwIDAQAB","protocol":"tls","protocolVersion":"2.0","port":1718,"ipAddress":"127.0.0.1","listenerInfos":"[{\"url\":\"tls://127.0.0.1:1718\",\"protocolVersion\":\"2.0\",\"messageFormat\":\"delimiter\"}]","isSigned":"true"}}
#
{"bytesAlg":{"hashAlg":"SHA-256"},"signatures":{"payload":"eyJpZCI6Ijg2LjUwMDAuNDcwL2RvaXAubG9jYWxUY3BSZXBvIiwidHlwZSI6IjAuVFlQRS9ETy5ET0lQU2VydmljZUluZm8iLCJhdHRyaWJ1dGVzIjp7InNlcnZpY2VOYW1lIjoiVGVzdE11bHRpVHJhbnNQcm90b2NvbCIsInNlcnZpY2VEZXNjcmlwdGlvbiI6InRlc3QgbG9jYWwgUmVwb3NpdG9yeSIsIm93bmVyIjoiODYuNTAwMC40NzAvZG91LlNVUEVSIiwicHVibGljS2V5IjoiTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF3VWp0WVVjaUpiZFU1QllZRll5a1MzMysxV25tOEtIZjkrMC85RW9sY0lXWXJvN3hYOHg5T0h3RjNuWXU4eGZoampLbzZNYTRXekkvT041VU80NDdleW5DU0tidnpKWlYwMXJ6dWswM0VEOXJXZllRUXlackk2V3lEdExBbndrRzRaSis5aWs3NUYrSkY1WVJITmI3YlUwVlU3RkV0UFMxNnV6Rjl0d0hpOUp6WmNmSThmd0ZLUE85WHpwNlVhZ0h6aWJJdVJUTG1VK0psNGpuenZVNE1Kc1FsZmNteU1NN1ZXTThvSENrZDlOekMrWElyN0dIL2EzemNJOFdFWTlEMW1McndZUjhVRVJkNnJxU0ozVzAzTzIxczQrbExPR2dHb1JtdEpFUlZxd0syVEY3ODlGYTdOOSs5YlpKSjNkQWFQMTZpeW5IZk9sNXRiN0ptcjR0THdJREFRQUIiLCJwcm90b2NvbCI6InRscyIsInByb3RvY29sVmVyc2lvbiI6IjIuMCIsInBvcnQiOjE3MTgsImlwQWRkcmVzcyI6IjEyNy4wLjAuMSIsImxpc3RlbmVySW5mb3MiOiJbe1widXJsXCI6XCJ0bHM6Ly8xMjcuMC4wLjE6MTcxOFwiLFwicHJvdG9jb2xWZXJzaW9uXCI6XCIyLjBcIixcIm1lc3NhZ2VGb3JtYXRcIjpcImRlbGltaXRlclwifV0iLCJpc1NpZ25lZCI6InRydWUifX0","signatures":[{"protected":"eyJhbGciOiJSUzI1NiJ9","header":{"kid":"86.5000.470/dou.SUPER"},"signature":"fuoDyqZMElZ-8i5ptQ09TBz5AIp0wcVzoDVqECkmWPnjPMSVRCjpwaw6wqsRSqCMhKJbGPZn3GMmuFaybqa1S2daPq7WNw2-UpsdjqYE_uMNLrbtQNe-dz5PmBOK4gtmrNURLlofJqSWhw2vv6h0CnsIgqqN7r3IuuGglHU2QAdchywBf_FcgxGqJv3JuoGu_QGQpVufi0k1yD4ziVEs4_NN62_uZgkQXY7eSB-mzNKGv6c5WvF9-5Nm9Mej7KBHlFkNv30zx1SToYHCSc0xeXuTXfmyOzyO1W7piB1s6nZX47g5S5YysTytmdHm0OIlpcX6pqSVpSxx2oULl8oe8w"}]}} (DelimiterMessageClientCodec.java:85)
```
（2）发送listOperation操作请求
```
java -jar .\Client-1.0.jar -l
[INFO ]16:56:54.083 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:56:56.385 client sending message: {"requestId":"-1340345561","targetId":"86.5000.470/doip.localTcpRepo","operationId":"0.DOIP/Op.ListOperations","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:56:56.581 client received message: {"requestId":"-1340345561","status":"0.DOIP/Status.001","output":["0.DOIP/Op.Hello","0.DOIP/Op.ListOperations","0.DOIP/Op.Retrieve","0.DOIP/Op.Create","0.DOIP/Op.Update","0.DOIP/Op.Delete","0.DOIP/Op.Search","0.DOIP/Op.Extension","0.DOIP/Op.Unknown"]} (DelimiterMessageClientCodec.java:85)
```
（3）发送create操作请求
```
java -jar .\Client-1.0.jar -c 86.5000.470/do.test01 DO hello
[INFO ]16:58:59.062 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:59:01.354 client sending message: {"requestId":"1646813081","targetId":"86.5000.470/doip.localTcpRepo","operationId":"0.DOIP/Op.Create","authentication":{"username":"admin","password":"password"}}
#
{"id":"86.5000.470/do.test01","type":"0.TYPE/DO","attributes":{"content":{"create":"hello","name":"create","description":"this is an example for create"}}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:59:01.548 client received message: {"requestId":"1646813081","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/do.test01","type":"0.TYPE/DO","attributes":{"content":{"create":"hello","name":"create","description":"this is an example for create"}}} (DelimiterMessageClientCodec.java:85)
```
（4）发送update操作请求
```
java -jar .\Client-1.0.jar -u 86.5000.470/do.test01 DO hello_update
[INFO ]16:59:36.850 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:59:39.151 client sending message: {"requestId":"-1386601504","targetId":"86.5000.470/do.test01","operationId":"0.DOIP/Op.Update","authentication":{"username":"admin","password":"password"}}
#
{"id":"86.5000.470/do.test01","type":"0.TYPE/DO","attributes":{"content":{"update":"hello_update","name":"update","description":"this is an example for update"}}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:59:39.338 client received message: {"requestId":"-1386601504","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/do.test01","type":"0.TYPE/DO","attributes":{"content":{"update":"hello_update","name":"update","description":"this is an example for update"}}} (DelimiterMessageClientCodec.java:85)
```
（5）发送retrieve操作请求
```
java -jar .\Client-1.0.jar -r 86.5000.470/do.test01
[INFO ]17:00:05.143 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:00:07.485 client sending message: {"requestId":"1775903427","targetId":"86.5000.470/do.test01","operationId":"0.DOIP/Op.Retrieve","attributes":{"includeElementData":"true"},"authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:00:07.686 client received message: {"requestId":"1775903427","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/do.test01","type":"0.TYPE/DO","attributes":{"content":{"update":"hello_update","name":"update","description":"this is an example for update"}}} (DelimiterMessageClientCodec.java:85)
```
（6）发送delete操作请求
```
java -jar .\Client-1.0.jar -d 86.5000.470/do.test01
[INFO ]17:00:22.094 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:00:24.340 client sending message: {"requestId":"-762527615","targetId":"86.5000.470/do.test01","operationId":"0.DOIP/Op.Delete","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:00:24.491 client received message: {"requestId":"-762527615","status":"0.DOIP/Status.001"} (DelimiterMessageClientCodec.java:85)
```
（7）发送search操作请求
```
java -jar .\Client-1.0.jar -s "hello"
[INFO ]17:01:09.654 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:01:11.913 client sending message: {"requestId":"1513536436","targetId":"86.5000.470/doip.localTcpRepo","operationId":"0.DOIP/Op.Search","attributes":{"query":"hello","pageNum":0,"pageSize":10,"type":"full","sortFields":"id:ASC"},"authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:01:12.078 client received message: {"requestId":"1513536436","status":"0.DOIP/Status.200","output":{"message":"Unsupported Operation!"}} (DelimiterMessageClientCodec.java:85)
```
__注意__：根据DOIP标准，Repository不支持search操作。

### 2.3.2 使用Client与Registry进行交互
1. 启动Registry
将Registry示例代码解压后，打开命令窗口，进入包含jar包的目录，此时目录内容如下：
```bash
ls
keys libs default_regi.json SimpleRegistry-1.0.jar
```
执行如下命令，开启Registry：
```bash
java -jar SimpleRegistry-1.0.jar
```
此时，命令行出现如下日志信息，表示Registry启动成功。
```bash
java -jar .\SimpleRegistry-1.0.jar
[INFO ]16:32:10.663 load config from: default_regi.json (DoipServiceConfig.java:24)
[INFO ]16:32:13.127 DOIPServiceInfo: {"id":"86.5000.470/dou.TEST","serviceDescription":"test local TLS Registry","publicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgHes68FmNYTgj2UKMu0cAc6+1izOoAkOkcYu+wS3/utvtA98CutdjogZjPVnYP4lumA1TrKo9zY0xGyo/8hkMY3C7Bn9kK3+o1uT0Jv5qPmxcr8E1vZg6joe2k38pHKBkjIctqHDXVrzcoQG1EiUSNQwbhlT/EjNmdzt2+ykmkHf+jiocjwc6TAL0EmifHk4o6kmn4LHvChmTfaMeRUnT2Ol6Aral+dd5RfVhQ1lNsoRfTk7QVR9GETmgLOLDQTbWAkMGg/p6LgQml0eulTTg5SCuoFKY81wruPVeoQ6yfmfX11aKHS88XOEg5qUARGlwncaxMUFqTPpiJwKzzlJrwIDAQAB","serviceName":"TestTLSRegistry","port":1720,"ipAddress":"127.0.0.1","protocol":"tls","protocolVersion":"2.0","listenerInfos":[{"url":"tls://127.0.0.1:1720","protocolVersion":"2.0","messageFormat":"delimiter"}],"owner":"86.5000.470/dou.SUPER"} (DoipServerImpl.java:34)
[INFO ]16:32:14.658 start at:1720 (NettyTLSDoipListener.java:63)
```

2. 修改客户端配置文件
将Client示例代码解压后，打开命令窗口，进入包含jar包的目录，此时目录内容如下：
```bash
ls
keys libs Client-1.0.jar default_client.json
```

配置文件default_client.json的默认内容为：
```bash
{
  "repoID": "86.5000.470/doip.localTcpRepo",
  "repoURL": "tls://127.0.0.1:1718",
}
```
修改其中的repoID和repoURL为步骤1中启动的Registry。
```bash
{
  "repoID": "86.5000.470/dou.TEST",
  "repoURL": "tls://127.0.0.1:1720",
}
```

3. 使用Client发送操作请求
   Client的基本用法，请参考2.3.1中的步骤3。与Registry交互过程中，需要注意的事项事：1. 创建DO时制定的类型需要为“Meta”；2. Registry支持search操作。
（1）发送hello操作
```bash
java -jar .\Client-1.0.jar -i
[INFO ]17:10:54.658 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:10:56.929 client sending message: {"requestId":"1632407099","targetId":"86.5000.470/dou.TEST","operationId":"0.DOIP/Op.Hello","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:10:57.529 client received message: {"requestId":"1632407099","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/dou.TEST","type":"0.TYPE/DO.DOIPServiceInfo","attributes":{"serviceName":"TestTLSRegistry","serviceDescription":"test local TLS Registry","owner":"86.5000.470/dou.SUPER","publicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgHes68FmNYTgj2UKMu0cAc6+1izOoAkOkcYu+wS3/utvtA98CutdjogZjPVnYP4lumA1TrKo9zY0xGyo/8hkMY3C7Bn9kK3+o1uT0Jv5qPmxcr8E1vZg6joe2k38pHKBkjIctqHDXVrzcoQG1EiUSNQwbhlT/EjNmdzt2+ykmkHf+jiocjwc6TAL0EmifHk4o6kmn4LHvChmTfaMeRUnT2Ol6Aral+dd5RfVhQ1lNsoRfTk7QVR9GETmgLOLDQTbWAkMGg/p6LgQml0eulTTg5SCuoFKY81wruPVeoQ6yfmfX11aKHS88XOEg5qUARGlwncaxMUFqTPpiJwKzzlJrwIDAQAB","protocol":"tls","protocolVersion":"2.0","port":1720,"ipAddress":"127.0.0.1","listenerInfos":"[{\"url\":\"tls://127.0.0.1:1720\",\"protocolVersion\":\"2.0\",\"messageFormat\":\"delimiter\"}]","isSigned":"true"}}
#
{"bytesAlg":{"hashAlg":"SHA-256"},"signatures":{"payload":"eyJpZCI6Ijg2LjUwMDAuNDcwL2RvdS5URVNUIiwidHlwZSI6IjAuVFlQRS9ETy5ET0lQU2VydmljZUluZm8iLCJhdHRyaWJ1dGVzIjp7InNlcnZpY2VOYW1lIjoiVGVzdFRMU1JlZ2lzdHJ5Iiwic2VydmljZURlc2NyaXB0aW9uIjoidGVzdCBsb2NhbCBUTFMgUmVnaXN0cnkiLCJvd25lciI6Ijg2LjUwMDAuNDcwL2RvdS5TVVBFUiIsInB1YmxpY0tleSI6Ik1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBZ0hlczY4Rm1OWVRnajJVS011MGNBYzYrMWl6T29Ba09rY1l1K3dTMy91dHZ0QTk4Q3V0ZGpvZ1pqUFZuWVA0bHVtQTFUcktvOXpZMHhHeW8vOGhrTVkzQzdCbjlrSzMrbzF1VDBKdjVxUG14Y3I4RTF2Wmc2am9lMmszOHBIS0JrakljdHFIRFhWcnpjb1FHMUVpVVNOUXdiaGxUL0VqTm1kenQyK3lrbWtIZitqaW9jandjNlRBTDBFbWlmSGs0bzZrbW40TEh2Q2htVGZhTWVSVW5UMk9sNkFyYWwrZGQ1UmZWaFExbE5zb1JmVGs3UVZSOUdFVG1nTE9MRFFUYldBa01HZy9wNkxnUW1sMGV1bFRUZzVTQ3VvRktZODF3cnVQVmVvUTZ5Zm1mWDExYUtIUzg4WE9FZzVxVUFSR2x3bmNheE1VRnFUUHBpSndLenpsSnJ3SURBUUFCIiwicHJvdG9jb2wiOiJ0bHMiLCJwcm90b2NvbFZlcnNpb24iOiIyLjAiLCJwb3J0IjoxNzIwLCJpcEFkZHJlc3MiOiIxMjcuMC4wLjEiLCJsaXN0ZW5lckluZm9zIjoiW3tcInVybFwiOlwidGxzOi8vMTI3LjAuMC4xOjE3MjBcIixcInByb3RvY29sVmVyc2lvblwiOlwiMi4wXCIsXCJtZXNzYWdlRm9ybWF0XCI6XCJkZWxpbWl0ZXJcIn1dIiwiaXNTaWduZWQiOiJ0cnVlIn19","signatures":[{"protected":"eyJhbGciOiJSUzI1NiJ9","header":{"kid":"86.5000.470/dou.SUPER"},"signature":"XYJanYr50DDMRwwy9h8Ayy5JOAgvLnI6T__UgAxCjvxSBVPItMYpID5Yok4t6x9Jslna4o9P6ddf0O8fa2R3qv9XVzM-_6FehOeS-fKwb0J_HkncKk2NMVk1W7OSq8NvhsTn6jvREGWCSCnQH1Z3Lr6XyH0t68FkrGj186UNBagIk_Q3Vc_doYQfwlo3gJ3p_rjN26WcrllhjIWLGjxlWoN_XWoy3kn5Vr3_5pQu8Tn64qUFKdDFKvtomu-Tr-noYDf13AOlTaAAg8VsIgc-pbM4hEd2KdoBvGn9xANGFKE7ruoTSbOV79zM_cxFVZnQRPSAHKVHapBFWCCYdJmsKQ"}]}} (DelimiterMessageClientCodec.java:85)
```
（2）发送listOperation操作
```bash
java -jar .\Client-1.0.jar -l
[INFO ]17:11:38.824 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:11:41.068 client sending message: {"requestId":"453969307","targetId":"86.5000.470/dou.TEST","operationId":"0.DOIP/Op.ListOperations","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:11:41.258 client received message: {"requestId":"453969307","status":"0.DOIP/Status.001","output":["0.DOIP/Op.Hello","0.DOIP/Op.ListOperations","0.DOIP/Op.Retrieve","0.DOIP/Op.Create","0.DOIP/Op.Update","0.DOIP/Op.Delete","0.DOIP/Op.Search","0.DOIP/Op.Extension","0.DOIP/Op.Unknown"]} (DelimiterMessageClientCodec.java:85)
```
（3）发送create操作
```bash
java -jar .\Client-1.0.jar -c 86.5000.470/meta.test01 Meta metainfo
[INFO ]17:14:39.364 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:14:41.588 client sending message: {"requestId":"495617923","targetId":"86.5000.470/dou.TEST","operationId":"0.DOIP/Op.Create","authentication":{"username":"admin","password":"password"}}
#
{"id":"86.5000.470/meta.test01","type":"0.TYPE/DO.Metadata","attributes":{"content":{"create":"metainfo","name":"create","description":"this is an example for create"}}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:14:41.832 client received message: {"requestId":"495617923","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/meta.test01","type":"0.TYPE/DO.Metadata","attributes":{"content":{"create":"metainfo","name":"create","description":"this is an example for create"}}} (DelimiterMessageClientCodec.java:85)
```
（4）发送update操作
```bash
java -jar .\Client-1.0.jar -u 86.5000.470/meta.test01 Meta metainfo_update
[INFO ]17:15:04.887 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:15:07.150 client sending message: {"requestId":"1654592549","targetId":"86.5000.470/meta.test01","operationId":"0.DOIP/Op.Update","authentication":{"username":"admin","password":"password"}}
#
{"id":"86.5000.470/meta.test01","type":"0.TYPE/DO.Metadata","attributes":{"content":{"update":"metainfo_update","name":"update","description":"this is an example for update"}}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:15:07.454 client received message: {"requestId":"1654592549","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/meta.test01","type":"0.TYPE/DO.Metadata","attributes":{"content":{"update":"metainfo_update","name":"update","description":"this is an example for update"}}} (DelimiterMessageClientCodec.java:85)
```
（5）发送retrieve操作
```bash
java -jar .\Client-1.0.jar -r 86.5000.470/meta.test01
[INFO ]17:15:23.620 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:15:25.938 client sending message: {"requestId":"-1789595138","targetId":"86.5000.470/meta.test01","operationId":"0.DOIP/Op.Retrieve","attributes":{"includeElementData":"true"},"authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:15:26.108 client received message: {"requestId":"-1789595138","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/meta.test01","type":"0.TYPE/DO.Metadata","attributes":{"content":{"update":"metainfo_update","name":"update","description":"this is an example for update"}}} (DelimiterMessageClientCodec.java:85)
```
（6）发送search操作
```bash
java -jar .\Client-1.0.jar -s "metainfo_update"
[INFO ]17:16:00.188 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:16:02.441 client sending message: {"requestId":"2052262241","targetId":"86.5000.470/dou.TEST","operationId":"0.DOIP/Op.Search","attributes":{"query":"metainfo_update","pageNum":0,"pageSize":10,"type":"full","sortFields":"id:ASC"},"authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:16:02.726 client received message: {"requestId":"2052262241","status":"0.DOIP/Status.001","output":{"size":1,"results":[{"id":"86.5000.470/meta.test01","type":"0.TYPE/DO.Metadata","attributes":{"content":{"update":"metainfo_update","name":"update","description":"this is an example for update"}}}]}} (DelimiterMessageClientCodec.java:85)
```
（7）发送delete操作
```bash
java -jar .\Client-1.0.jar -d 86.5000.470/meta.test01
[INFO ]17:16:38.767 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]17:16:40.975 client sending message: {"requestId":"779456898","targetId":"86.5000.470/meta.test01","operationId":"0.DOIP/Op.Delete","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]17:16:41.160 client received message: {"requestId":"779456898","status":"0.DOIP/Status.001"} (DelimiterMessageClientCodec.java:85)
```

# 3. 与其他DOIP实现进行互操作
## 3.1 cordra
### 3.1.1  使用PKU实现的DOIP客户端访问cordra实现的DOIP服务
参考cordra提供的文档搭建cordra的DOIP服务，注册用户，并记录账户。使用PKU实现的DOIP客户端工具访问cordra的DOIP服务，使用已经注册的用户凭据来通过认证。这里以cordra2.1版本为例来进行说明。
1. 启动cordra的DOIP服务
将下载的cordra2.1代码进行解压，目录结构如下：
```bash
Mode                LastWriteTime         Length Name
----                -------------         ------ ----
d-----         2020/5/8     23:28                bin
d-----         2020/5/8     23:28                cordra-client-handle-storage
d-----        2021/1/17     14:00                data
d-----         2020/5/8     23:28                docker
d-----         2020/5/8     23:28                extensions
d-----         2020/8/6     23:26                src
d-----         2020/5/8     23:28                sw
-a----         2020/5/8     23:28          14152 cordra-client-LICENSE.txt
-a----        2020/7/27     10:49         948067 cordra-technical-manual-2.1.0.pdf
-a----         2020/5/8     23:28          66548 LICENSE.txt
-a----         2020/5/8     23:28           1503 README.txt
-a----         2020/5/8     23:28             45 shutdown
-a----         2020/5/8     23:28             61 shutdown.bat
-a----         2020/5/8     23:28             44 startup
-a----         2020/5/8     23:28             60 startup.bat
```
根据README.txt中的提示，修改admin的初始密码。然后通过启动脚本（Linux和MacOS中使用startup，Windows中使用startup.bat）开启DOIP服务。看到如下信息，表示启动完成。
```bash
data dir: D:\App\cordra-2.1.0\data
Initializing HTTP interface on port 8080
Initializing HTTPS interface on port 8443
Using existing keypair for HTTPS.
Storage: bdbje
Index: lucene
Initializing Handle TCP interface on port 2641
Initializing DOIP interface on port 9000
Startup complete.
```
2. 配置PKU DOIP客户端
当完成cordra服务端启动后，修改PKU DOIP客户端的配置文件。该配置文件与客户端的jar包放在一个目录，文件名为：default_client.json。修改内容如下，其中userPass为第一步配置的admin初始密码：
```json
{
  "repoID": "20.5000.123/service",
  "repoURL": "tls://127.0.0.1:9000",
  "userName": "admin",
  "userPass": "write_your_admin_password"
}
```
3. 使用DOIP客户端与cordra服务端进行交互
(1) 查看服务端信息：
```bash
java -jar .\DoipSDK-1.0.jar -i
[INFO ]16:37:25.727 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:37:27.743 client sending message: {"requestId":"-886484722","targetId":"20.5000.123/service","operationId":"0.DOIP/Op.Hello","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:37:27.979 client received message: {"requestId":"-886484722","status":"0.DOIP/Status.001","output":{"id":"20.5000.123/service","type":"0.TYPE/DOIPService","attributes":{"ipAddress":"127.0.0.1","port":9000,"protocol":"TCP","protocolVersion":"2.0","publicKey":{"kty":"RSA","n":"i8MxV1gugMrs_GdSNDRzxzoj87vJZ9tlUyDHFYJ6oHJDtmD2F2VK_hwqTLQgadmmKTs2RfHfzIIrkz1vWqvGLMMaTvvmYpqjZml64FDXEXP1yynAhV34ylJ7ChYENmbc1gEkv44wqG8lvQdyeysxM9tz6VSlYwT1AjfAute9QjtscU5Hpzr7kBTOpRRE7za3dCErPmBbNZKDy9ZUREIsOik0jwnlEs7uGJgh4AoQR--qczbTO-VLafDE1pmIfaeQU9WAPD2euXZ34vaLyV2MsZQ4BGJq4xYYbBoKdSXikdRQ3NMsGjPalajdYdxwWCt0yVWbP9MGObTiv3OlOToyuQ","e":"AQAB"}}}} (DelimiterMessageClientCodec.java:85)
```

(2) 查看服务端支持的操作：
```bash
java -jar .\DoipSDK-1.0.jar -l
[INFO ]16:40:23.876 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:40:25.922 client sending message: {"requestId":"-1201986326","targetId":"20.5000.123/service","operationId":"0.DOIP/Op.ListOperations","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:40:26.100 client received message: {"requestId":"-1201986326","status":"0.DOIP/Status.001","output":["0.DOIP/Op.Hello","0.DOIP/Op.ListOperations","0.DOIP/Op.Create","0.DOIP/Op.Search"]} (DelimiterMessageClientCodec.java:85)
```

(3) 创建数字对象：
```bash
java -jar .\DoipSDK-1.0.jar -c 86.5000.470/do.cordra.test0 DO hello
[INFO ]16:39:09.847 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:39:11.800 client sending message: {"requestId":"-1058388874","targetId":"20.5000.123/service","operationId":"0.DOIP/Op.Create","authentication":{"username":"admin","password":"password"}}
#
{"id":"86.5000.470/do.cordra.test0","type":"0.TYPE/DO","attributes":{"content":{"create":"hello","name":"create","description":"this is an example for create"}}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:39:12.019 client received message: {"requestId":"-1058388874","status":"0.DOIP/Status.001","output":{"id":"86.5000.470/do.cordra.test0","type":"0.TYPE/DO","attributes":{"content":{"name":"create","description":"this is an example for create","create":"hello"},"metadata":{"createdOn":1610872751960,"createdBy":"admin","modifiedOn":1610872751960,"modifiedBy":"admin","txnId":1610872751963003}},"elements":[]}} (DelimiterMessageClientCodec.java:85)
```

(4) 更新数字对象：
```bash
java -jar .\DoipSDK-1.0.jar -u 86.5000.470/do.cordra.test0 DO hello_update
[INFO ]16:42:05.815 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:42:07.871 client sending message: {"requestId":"-702244521","targetId":"86.5000.470/do.cordra.test0","operationId":"0.DOIP/Op.Update","authentication":{"username":"admin","password":"password"}}
#
{"id":"86.5000.470/do.cordra.test0","type":"0.TYPE/DO","attributes":{"content":{"update":"hello_update","name":"update","description":"this is an example for update"}}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:42:08.083 client received message: {"requestId":"-702244521","status":"0.DOIP/Status.001","output":{"id":"86.5000.470/do.cordra.test0","type":"0.TYPE/DO","attributes":{"content":{"name":"update","description":"this is an example for update","update":"hello_update"},"metadata":{"createdOn":1610872751960,"createdBy":"admin","modifiedOn":1610872928052,"modifiedBy":"admin","txnId":1610872928045004}},"elements":[]}} (DelimiterMessageClientCodec.java:85)
```

(5) 获取数字对象
```bash
java -jar .\DoipSDK-1.0.jar -r 86.5000.470/do.cordra.test0
[INFO ]16:45:24.997 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:45:27.069 client sending message: {"requestId":"1215397185","targetId":"86.5000.470/do.cordra.test0","operationId":"0.DOIP/Op.Retrieve","attributes":{"includeElementData":"true"},"authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:45:27.241 client received message: {"requestId":"1215397185","status":"0.DOIP/Status.001"}
#
{"id":"86.5000.470/do.cordra.test0","type":"0.TYPE/DO","attributes":{"content":{"name":"update","description":"this is an example for update","update":"hello_update"},"metadata":{"createdOn":1610872751960,"createdBy":"admin","modifiedOn":1610872928052,"modifiedBy":"admin","txnId":1610872928045004}},"elements":[]} (DelimiterMessageClientCodec.java:85)
```

(6) 检索数字对象：
```bash
java -jar .\DoipSDK-1.0.jar -s "+type:0.TYPE/DO +/name:update"
[INFO ]16:43:18.653 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:43:20.683 client sending message: {"requestId":"-328328880","targetId":"20.5000.123/service","operationId":"0.DOIP/Op.Search","attributes":{"query":"+type:0.TYPE/DO +/name:update","pageNum":0,"pageSize":10,"type":"full","sortFields":"id:ASC"},"authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:43:20.892 client received message: {"requestId":"-328328880","status":"0.DOIP/Status.001"}
#
{
  "size": 1,
  "results": [
    {
      "id": "86.5000.470/do.cordra.test0",
      "type": "0.TYPE/DO",
      "attributes": {
        "content": {
          "name": "update",
          "description": "this is an example for update",
          "update": "hello_update"
        },
        "metadata": {
          "createdOn": 1610872751960,
          "createdBy": "admin",
          "modifiedOn": 1610872928052,
          "modifiedBy": "admin",
          "txnId": 1610872928045004
        }
      },
      "elements": []
    }
  ]
} (DelimiterMessageClientCodec.java:85)
```

(7) 删除数字对象：
```bash
java -jar .\DoipSDK-1.0.jar -d 86.5000.470/do.cordra.test0
[INFO ]16:46:55.354 load config from: default_client.json (DOIPCMDClient.java:192)
[INFO ]16:46:57.436 client sending message: {"requestId":"-1326115326","targetId":"86.5000.470/do.cordra.test0","operationId":"0.DOIP/Op.Delete","authentication":{"username":"admin","password":"password"}}
#
#
 (DelimiterMessageClientCodec.java:35)
[INFO ]16:46:57.627 client received message: {"requestId":"-1326115326","status":"0.DOIP/Status.001"} (DelimiterMessageClientCodec.java:85)
```

### 3.1.2 使用cordra客户端访问PKU实现的DOIP服务
参考cordra提供的客户端开发代码，编写客户端程序，详细请参考cordra[相关文档](https://www.cordra.org/documentation/client/doip-java.html)。

__注意__：由于实现的差异，cordra客户端不能识别PKU DOIP服务针对hello操作返回的信息（包含签名），会报```Unexpected input segments```的错误。

# 4. 使用SDK编写自己的应用
## 4.1 运行环境准备
由于SDK代码采用Java编写，使用前，请准备好JDK环境（版本>=java1.8）。
## 4.2 下载SDK代码
* doip-java-sdk [v1.0](https://public.internetapi.cn/docs/doa/doa.html)

## 4.3 使用SDK
在源码目录下创建libs目录，将下载的SDK文件解压出来的jar包拷贝到libs目录中，如果您使用gradle，请在build.gradle中添加如下代码：
```groovy
repositories {
    maven { url 'http://maven.aliyun.com/nexus/content/groups/public' }
    flatDir{ dirs 'libs'}
}

dependencies {
    compile "bdware.doip.sdk:doip-java-sdk:0.1"
    compile "bdware.doip.codec:netty-codec:0.1"
    compile "bdware.doip.client:netty-doclient:0.1"
    compile "bdware.doip.server:netty-doserver:0.1"

    compile 'org.codehaus.groovy:groovy-all:2.3.11'
    compile 'net.sf.proguard:proguard-gradle:6.0.3'
    compile 'log4j:log4j:1.2.17'
    compile group: 'org.rocksdb', name: 'rocksdbjni', version: '6.4.6'
    compile group: 'org.apache.httpcomponents', name: 'httpclient', version: '4.5.11'
    compile group: 'com.google.code.gson', name: 'gson', version: '2.8.6'
    compile group: 'io.netty', name: 'netty-all', version: '4.1.29.Final'
    compile group: 'org.bouncycastle', name: 'bcpkix-jdk15on', version: '1.66'
}
```
## 4.4 编写业务代码
### 4.4.1 实现基本的Repository
```java
import bdware.doip.sdk.server.Config;
import bdware.doip.sdk.server.ServiceInfo;
import bdware.doip.sdk.server.RocksDBStore;
import bdware.doip.sdk.server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        String confPath = "default.conf";
        Config.LocalConfig conf = new Config(confPath).Parse();
        if (conf.storage.equals("rocksdb")) {
            new Server(new ServiceInfo(conf.repoID, conf.ownerID, conf.type, conf.listeners), new RocksDBStore());
        } else {
            System.out.println("unsupported database, exit!");
            System.exit(1);
        }
    }
}
```
### 4.4.2 实现基本的Registry
### 4.4.3 实现基本的Client
```java
import bdware.doip.codec.bean.DigitalObject;
import bdware.doip.codec.bean.DoResponse;
import bdware.doip.codec.bean.DoType;
import bdware.doip.codec.bean.Element;
import bdware.doip.codec.message.DoMessage;
import bdware.doip.sdk.client.Client;
import bdware.doip.sdk.client.Config;

public class Main {
    static String testConf = "default_client.conf";
    static String repoId = "86.5000.470/local.TLSRepository";
    static Config conf;

    static {
        try {
            conf = Config.fromFile(testConf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Client client = new Client(conf.serverUrl, conf.messageFormat, repoId);

    public static void main(String[] args) {
        String serverInfo = getServerInfo(repoId);
        System.out.println("server info:"+serverInfo);
        // pre-request: get do identifier from irp server

        if (put("86.5000.470/test.hello", "hello doa world".getBytes())) {
            System.out.println("create success");
        } else {
            System.out.println("create failed, exit");
        }

        System.out.println("contents for do:"+get("86.5000.470/test.hello"));
    }

    // hello
    public static String getServerInfo(String serverId) {
        DoMessage res = client.doipClient.hello(serverId);
        System.out.println("server info:"+new String(res.body));
        return new String(res.body);
    }

    // create
    public static boolean put(String doId, byte[] data) {
        String id = "1";
        String type = "text/plain";
        DigitalObject digitalObject = new DigitalObject(doId, DoType.DoString);
        Element element = new Element(id, type);
        element.setData(data);
        digitalObject.addElements(element);
        DoMessage res = client.doipClient.create(client.serverId, digitalObject);
        System.out.println("put res:"+new String(res.body));
        return res.parameters.response == DoResponse.Success;
    }

    // retrieve
    public static String get(String doId) {
        DoMessage doMessage = client.doipClient.retrieve(doId, "1", "true");
        return new String(doMessage.body);
    }

    // update
    public static boolean update(String doId, byte[] data) {
        DigitalObject digitalObject = new DigitalObject(doId, DoType.DoString);
        String id = "1";
        String type = "text/plain";
        Element element = new Element(id, type);
        element.setData(data);
        digitalObject.addElements(element);
        DoMessage res = client.doipClient.update(digitalObject);
        System.out.println("update res:"+new String(res.body));
        return res.parameters.response == DoResponse.Success;
    }

    //delete
    public static boolean delete(String doId) {
        DoMessage res = client.doipClient.delete(doId);
        System.out.println("delete res:"+new String(res.body));
        return res.parameters.response == DoResponse.Success;
    }
    // search, listOp
}

```
## 4.5 配置信息准备
### 4.5.1 生成证书
由于客户端与服务端采用了TLS通信协议，需要配置密钥。请确保环境中安装有keytools工具，然后使用如下命令，生成服务端使用的私钥和证书。（TODO:开发keytool）
* 为Repository生成证书
```
keytool -genkey -keyalg RSA -keysize 2048 -validity 365 -keypass 123456 -keystore doip_service_repository.keystore -storepass 123456 -dname "UID=86.5000.470/doip.RepositoryTLSService"
```
* 为Registry生成证书
```
keytool -genkey -keyalg RSA -keysize 2048 -validity 365 -keypass 123456 -keystore doip_service_registry.keystore -storepass 123456 -dname "UID=86.5000.470/doip.RegistryTLSService"
```
### 4.5.2  编写配置文件
1. Repository
创建名称为default_repository.conf的文件，写入如下内容。
```json
{
  "type": "Repository",
  "ownerID": "86.5000.470/dou.SUPER",
  "repoID": "86.5000.470/local.TLSRepository",
  "listeners": [
    {
      "url": "tls://127.0.0.1:1716",
      "protocolVersion": "2.0",
      "messageFormat": "delimiter"
    }
  ],
  "serviceDescription": "local tls repository",
  "serviceName": "DOA Repository",
  "keyFilePath": "./doip_service_repository.keystore",
  "keyPass": "123456",
  "storage": "rocksdb"
}
```
2. Registry
创建名称为default_registry.conf的文件，写入如下内容。
```json
{
  "type": "Registry",
  "ownerID": "86.5000.470/dou.SUPER",
  "repoID": "86.5000.470/local.TLSRegistry",
  "listeners": [
    {
      "url": "tls://127.0.0.1:1717",
      "protocolVersion": "2.0",
      "messageFormat": "delimiter"
    }
  ],
  "serviceDescription": "local tls registry",
  "serviceName": "DOA Registry",
  "keyFilePath": "./doip_service_registry.keystore",
  "keyPass": "123456",
  "storage": "rocksdb"
}
```
3. Client
创建名称为default_client.conf的文件，写入如下内容。
```json
{
  "repository": {
    "serverUrl": "tls://127.0.0.1:1716",
    "messageFormat": "delimiter",
    "keyFilePath": "./default_client_repository.keystore",
    "keyPass": "123456"
  },
  "registry": {
    "serverUrl": "tls://127.0.0.1:1717",
    "messageFormat": "delimiter",
    "keyFilePath": "./default_client_registry.keystore",
    "keyPass": "123456"
  }
}
```

## 4.6 部署程序
* 将编译好的Repository代码与default_server_repository.keystore，default_repository.conf放在同一目录，然后执行Repository。
* 将编译好的Registry代码与default_server_registry.keystore，default_registry.conf放在同一目录，然后执行Registry。
* 将编译好的Client代码与default_client_repository.keystore，default_client_registry.keystore，default_client.conf放在同一目录，然后执行Client。

# 5. 接口说明
详细的SDK接口说明文档
# 6. 查看源代码
* doip-java-sdk-src [v0.1](https://public.internetapi.cn/docs/doa/doa.html)
