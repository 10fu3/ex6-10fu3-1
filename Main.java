import java.util.UUID;

public class Main {
    public static void main(String... args){
        //ラムダ式
        HttpServer.get("/route/:id",(ctx)->{
            String pathParam = ctx.req.getPathParams().getOrDefault("id","empty");
            ctx.res.setBody(pathParam);
        });
        HttpServer.get("/wait",(ctx)->{
            try {
                Thread.sleep(9500);
                ctx.res.setBody(UUID.randomUUID().toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        HttpServer.get("/nowait",(ctx)->{
            ctx.res.setBody("not_waited");
        });
        HttpServer.get().start(3000);
    }
}
