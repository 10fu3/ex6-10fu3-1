import java.util.HashMap;
import java.util.Map;

public class Path {
    private final String[] rawPath;

    public Path(String path){
        this.rawPath = path.split("/");
    }

    private static String[] splitSlash(String url){
        return url.split("/");
    }

    public boolean isMatch(String requestURL){
        String[] request = splitSlash(requestURL);
        if(this.rawPath.length != request.length){
            return false;
        }
        for (int i = 0; i < request.length; i++) {
            if(this.rawPath[i].length() == 0){
                continue;
            }
            if(this.rawPath[i].charAt(0) == ':'){
                continue;
            }
            if(!request[i].equalsIgnoreCase(this.rawPath[i])){
                return false;
            }
        }
        return true;
    }

    public Map<String,String> getMatchingPair(String requestURL){
        if(!isMatch(requestURL)){
            return new HashMap<>();
        }
        String[] request = requestURL.split("/");
        Map<String,String> pair = new HashMap<>();
        for (int i = 0; i < request.length; i++) {
            if(this.rawPath[i].length() == 0){
                continue;
            }
            if(this.rawPath[i].charAt(0) == ':'){
                pair.put(this.rawPath[i].replaceFirst(":",""),request[i]);
                continue;
            }
        }
        return pair;
    }
}
