import java.util.Map;

public class HttpRequestBuilder {
    private HttpRequest req = new HttpRequest();

    private HttpRequestBuilder setRequest(HttpRequest req){
        this.req = req;
        return this;
    }

    public static HttpRequestBuilder edit(HttpRequest req){
        return new HttpRequestBuilder().setRequest(req);
    }

    public Map<String, String> getFormParam(){
        return this.req.getFormParams();
        
    }

    public Map<String, String> getPathParam(){
        return this.req.pathParams;
    }

    public Map<String,String> getCookie(){
        return this.req.cookie;
    }

    public String getBody(){
        return this.req.body;
    }

    public String getRoute(){
        return this.req.route;
    }

    public HttpMethod getMethod(){
        return this.req.method;
    }

    public HttpRequestBuilder setPathParam(Map<String,String> map){
        this.req.pathParams = map;
        return this;
    }

    public HttpRequestBuilder setCookie(Map<String,String> cookie){
        this.req.cookie = cookie;
        return this;
    }

    public HttpRequestBuilder setBody(String body){
        this.req.body = body;
        return this;
    }

    public HttpRequestBuilder setRoute(String route){
        this.req.route = route;
        return this;
    }

    public HttpRequestBuilder setMethod(HttpMethod method){
        this.req.method = method;
        return this;
    }

    /**
     * リクエストを表現するクラスに変換する
     * @return HttpRequest
     */
    IHttpRequest toHttpRequest(){
        if(this.getMethod() == HttpMethod.GET){
            return new GetHttpRequest(this.req);
        }
        return this.req;
    }
}
