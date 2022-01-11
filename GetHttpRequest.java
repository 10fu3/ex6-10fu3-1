import java.util.Map;

public class GetHttpRequest extends HttpRequest implements IHttpRequest{

    public GetHttpRequest(HttpRequest req){
        super.route = req.route;
        super.body = req.body;
        super.cookie = req.cookie;
        super.method = req.method;
        super.pathParams = req.pathParams;
    }

    /**
     * フォームパラメーター
     *
     * @return [Key:Value]
     */
    @Override
    public Map<String, String> getFormParams() {
        throw new IllegalAccessHttpRequestField();
    }

    /**
     * リクエスト本体
     * @return リクエスト本文
     */
    @Override
    public String getBody() {
        throw new IllegalAccessHttpRequestField();
    }
}
