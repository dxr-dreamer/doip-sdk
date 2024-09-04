# doip-sdk

Doip-SDK includes the following two parts: 1. Protocol codec, in directory "src/main/codec" 2. Protocol endpoint development framework, in "src/main/endpoint".

## Usage

The current SDK supports udp, TCP, TLS, Websocket and other transport protocols. For details, see the client and server notation for "org.bdware.doip.endpoint" in the "src/test/java" directory.

| doip vesion | transport protocols | reference implementation |  Applicable scene  |
| ---- |  ----  | ----  | ----  |
| 2.1 | udp  | The envelope mtu is 1492 (according to 802.3)，No packet loss retransmission strategy, you need to implement retransmission yourself | Scenarios with high response requirements and small amount of data |
| 2.1 | tcp  | The envelope mtu is 5\*1024\*1024 (5MB), TCP based, so no packet loss retransmission strategy | Stable transmission |
| 2.1 | tls | The envelope mtu is 5\*1024\*1024 (5MB), Authentication to the server can be achieved by customizing the TrustManager | Encrypted Transmission Scenario |
| 2.1 | ws  | The envelope mtu is the 65536-websockt header | Works through the browser, no cross-domain issues |
| 2.0 | tls  | Delimiter based transmission | Compatible with the existing doip2.0 protocol, the automatic signature and verification of Do's Element has not yet been implemented |

Currently tcp can support 10MB level of data volume (single DO).
For large data volume (100MB) transmission, such as large file create scenarios, streaming MessageCodec needs to be written.

## org.bdwdare.doip.codec

The following codecs are implemented:

1.MessageEnvelop<->DoMessage

    1) NaiveEnvelopToDoMessage: Only support one DoMessage corresponds to one MessageEnvelop. This codec works with reliable transport protocols.
    2) MessageEnvelopAggregator: The MTU of MessageEnvelope can be customized, and there is no packet loss retransmission logic.

2.MessageEnvelop<->MessageEnvelop

    1) Can be used when debugging.
    2) SenderInjector: For DoipUDPClient, write the destination address to MessageEnvelop

3.MessageEnvelopCodec, for encode MessageEnvelop into a byte stream.

4.DoMessagePrinter, DoMessage that can print input and output, can be used when debugging.

5.DatagramMessageEnvelopeCodec, Used for encoding and decoding of UDP packets. This class needs to be used in conjunction with MessageEnvelopeAggregator to split each MessageEnvelope into smaller transmission units and then send them.

6.WebSocketFrameToByteBufCodec, for implement doip over websocket.

## Development environment configuration

If the build reports an error, you can copy the gradle.properties.template in the "scripts" directory to the "./" directory and rename it to gradle.properties.

## Examples

Examples of DOIP Client and Repository are located in src/test/java/. 
Users can develop own Repositories refer to these examples.