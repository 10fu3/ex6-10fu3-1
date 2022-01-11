public class Context {
    public final IHttpRequest req;
    public final IHttpResponse res;
    public Context(IHttpRequest req){
        this.req = req;
        this.res = new HttpResponse();
    }
}
