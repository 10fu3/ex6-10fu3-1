import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class HttpRequest implements IHttpRequest{

    HttpMethod method = HttpMethod.ERROR;
    Map<String,String> pathParams = new HashMap<>();
    Map<String,String> cookie = new HashMap<>();
    String body = "";
    String route = "";

    /**
     * フォームパラメーター
     *
     * @return [Key:Value]
     */
    @Override
    public Map<String, String> getFormParams() {
        return Arrays.stream(this.body.split("&"))
                .map(s->s.split("="))
                .filter(s->s.length == 2)
                .map(s->new Tuple<>(s[0],s[1]))
                .collect(Collectors.toMap(i->i.key,i->i.value));
    }

    /**
     * パスパラメーター
     *
     * @return [Key:Value]
     */
    @Override
    public Map<String, String> getPathParams() {
        return this.pathParams;
    }

    /**
     * Cookie
     *
     * @return cookie
     */
    @Override
    public Map<String, String> getCookie() {
        return this.cookie;
    }

    /**
     * リクエスト本体
     *
     * @return リクエスト本文
     */
    @Override
    public String getBody() {
        return this.body;
    }

    /**
     * リクエスト送信先
     *
     * @return リクエスト送信先
     */
    @Override
    public String getRoute() {
        return this.route;
    }

    /**
     * HTTPメソッドの種類
     *
     * @return HTTPメソッド
     */
    @Override
    public HttpMethod getMethod() {
        return null;
    }

    @Override
    public String toString() {
        return ParseJSON.parseObject(this);
    }
}
