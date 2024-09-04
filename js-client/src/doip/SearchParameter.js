export function SearchParameter() {
    this.query="";
    this.pageNum=0;
    this.pageSize=0;
    this.sortFields=null;
    this.sortFieldsSer="";
    this.type="full";

    this.createSearchParameter = function( query,  pageNum,  pageSize,  sortFields,  type) {
        this.query=query;
        this.pageNum=pageNum;
        this.pageSize=pageSize;
        this.sortFields=null;
        this.sortFieldsSer=sortFields;
        this.type=type;
        return this;
    }
   this.getDefaultParameter=function (){
        return new SearchParameter("", 0, 1000, "", "id");
    }

}