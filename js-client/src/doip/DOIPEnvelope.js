export function DOIPEnvelope() {
    this.majorVersion=3;
    this.minVersion=3;
    this.flag=0;
    this.reserved=0;
    this.requestId=0;
    this.sequenceNumber=0;
    this.totalNumber=1;
    this.contentLength=0;
    this.content = {};
}