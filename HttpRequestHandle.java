import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpRequestHandle {

    private ByteBuffer buff;

    private HttpMethod methodMatch(String method){
        HttpMethod[] methods = HttpMethod.values();
        for (int i = 0; i < methods.length; i++) {
            if(method.equalsIgnoreCase(methods[i].toString())){
                return methods[i];
            }
        }
        return HttpMethod.ERROR;
    }

    private void appendHeaderParameter(Map<String,String> headerMap,String header) {
        int idx = header.indexOf(":");
        if (idx == -1) {
            return;
        }
        String[] split = header.split(":");
        headerMap.put(split[0],split[1]);
    }

    private void appendMessageBody(List<String> lines,String bodyLine) {
        lines.add(bodyLine);
    }

    private Thread[] workers = new Thread[4];

    public HttpRequestHandle(ByteBuffer bb,Scanner sc){
        try{
            this.buff = bb;

            buff.flip();
            byte[] bytes = new byte[buff.limit()];
            buff.get(bytes);
            buff.compact();

            HttpRequestBuilder hrb = new HttpRequestBuilder();

            Map<String,String> headers = new HashMap<>();

            if(sc.hasNextLine()){
                String[] methodAndPath = sc.nextLine().split(" ");
                if(methodAndPath.length >= 3){
                    hrb.setMethod(methodMatch(methodAndPath[0]));
                    hrb.setRoute(methodAndPath[1]);
                }
            }

            while (sc.hasNextLine()) {
                String header = sc.nextLine();
                if("".equalsIgnoreCase(header)){
                    break;
                }
                appendHeaderParameter(headers,header);
            }

            while(sc.hasNextLine()){
                String bodyLine = sc.nextLine();
                hrb.setBody(hrb.getBody()+"\n"+bodyLine);
            }

            if(headers.containsKey("Cookie")){
                hrb.setCookie(
                        Arrays.stream(headers.get("Cookie").replaceFirst(" ","").split("; "))
                                .map(s->s.split("="))
                                .map(s->new Tuple<>(s[0],s[1]))
                                .collect(Collectors.toMap(i->i.key,i->i.value)));
            }



            hrb.setPathParam(new Path(HttpServer.handles.get(hrb.getMethod()).keySet()
                    .stream()
                    .filter(k->new Path(k).isMatch(hrb.getRoute()))
                    .findFirst()
                    .orElse(""))
                    .getMatchingPair(hrb.getRoute()));

            Context ctx = new Context(hrb.toHttpRequest());

            HttpServer.handles.get(hrb.getMethod()).keySet()
                    .stream()
                    .filter(k->new Path(k).isMatch(hrb.getRoute()))
                    .map(k-> HttpServer.handles.get(hrb.getMethod()).get(k))
                    .forEach(j->j.accept(ctx));



            buff.clear();
            buff.put(ctx.res.convertHTTPResponse().getBytes());

        }catch (NullPointerException e){
        }


//
//        System.out.println(hrb.toHttpRequest().toString());
    }
}
