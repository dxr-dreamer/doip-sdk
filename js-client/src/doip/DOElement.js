export function DOElement() {
    this.id="";
    this.length=-1;
    this.type="";
    this.dataString='';
    this.attributes= {};
    //data 为byte[]
    this.data=new Array();

    this.createElement = function (id, type) {
        this.id = id;
        this.type = type;
        this.length = -1;
        return this;
    }

    this.setData = function (data) {
        if (data == null) {
            this.data = null;
            this.length = 0;
            return;
        }
        this.data = data;
        this.length = data.length;
    }
    this.getData = function () {
        return this.data;
    }
}