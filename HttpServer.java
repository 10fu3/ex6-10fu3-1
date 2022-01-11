import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import java.util.Map;

/**
 * ノンブロッキングHTTP Server
 */

public class HttpServer {

    //シングルトンオブジェクト
    private final static HttpServer hs = new HttpServer();

    private AsynchronousServerSocketChannel server;

    private final int TIMEOUT = 60;

//    private static final Queue<AsynchronousSocketChannel> works = new ConcurrentLinkedQueue<>();

    //private static final Thread[] workers = new Thread[2];
    private static ExecutorService pool = Executors.newFixedThreadPool(4);

    //シングルトンオブジェクトとラムダ式の併用
    public static final Map<HttpMethod,Map<String, Consumer<Context>>> handles =
            Map.of(HttpMethod.GET,new LinkedHashMap<>(),
                    HttpMethod.POST,new LinkedHashMap<>(),
                    HttpMethod.PUT,new LinkedHashMap<>(),
                    HttpMethod.UPDATE,new LinkedHashMap<>(),
                    HttpMethod.DELETE,new LinkedHashMap<>());

    public static HttpServer get(){
        return hs;
    }

    private void handleRequest(AsynchronousSocketChannel channel) {
        assert channel != null;
        pool.submit(()->{
            try (AsynchronousSocketChannel acceptedChannel = channel) {
                ByteBuffer buff = ByteBuffer.allocateDirect(8192);

                acceptedChannel.read(buff).get(TIMEOUT, TimeUnit.SECONDS);

                buff.flip();
                byte[] bytes = new byte[buff.limit()];
                buff.get(bytes);
                buff.compact();

                new HttpRequestHandle(buff,new Scanner(new String(bytes)));
                System.out.println(Thread.currentThread().getName());
                buff.flip();
                acceptedChannel.write(buff).get(TIMEOUT, TimeUnit.SECONDS);

            } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void get(String baseURL,Consumer<Context> ctx){
        assert ctx != null;
        handles.get(HttpMethod.GET).put(baseURL,ctx);
    }

    public static void post(String baseURL,Consumer<Context> ctx){
        assert ctx != null;
        handles.get(HttpMethod.POST).put(baseURL, ctx);
    }

    public static void put(String baseURL,Consumer<Context> ctx){
        assert ctx != null;
        handles.get(HttpMethod.PUT).put(baseURL, ctx);
    }

    public static void update(String baseURL,Consumer<Context> ctx){
        assert ctx != null;
        handles.get(HttpMethod.UPDATE).put(baseURL, ctx);
    }

    public static void delete(String baseURL,Consumer<Context> ctx){
        assert ctx != null;
        handles.get(HttpMethod.DELETE).put(baseURL, ctx);
    }

    public void start(int port){

        try {
            server = AsynchronousServerSocketChannel.open();
            server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            server.bind(new InetSocketAddress(port));

            while(true) {
                Future<AsynchronousSocketChannel> acceptFuture = server.accept();
                try {
                    handleRequest(acceptFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
