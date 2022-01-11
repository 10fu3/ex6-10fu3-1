public interface IHttpResponse {

    IHttpResponse setLocation(String path);

    IHttpResponse setCookie(String k,String v);

    IHttpResponse setHeader(String k,String v);

    IHttpResponse setBody(String body);

    IHttpResponse setJSON(Object obj);

    IHttpResponse setStatusCode(StatusCode status);

    /**
     * HTTPレスポンスに変換
     * @return 変換後の文字列
     */
    String convertHTTPResponse();
}
