import java.lang.reflect.Field;
import java.util.*;

import static java.lang.reflect.Modifier.*;

public class ParseJSON {

    public static String listToString(List<Object> l){
        StringBuilder sss = new StringBuilder("[");

        for(Object i : l){
            if(i instanceof String || i.getClass().isEnum()){
                sss.append("\"");
                sss.append(i);
                sss.append("\"");
            }else if(ValueUtil.isPrimitive(i)){
                sss.append(i);
            }else{
                sss.append(parseObject(i));
            }
            sss.append(",");
        }

        return sss.length() == 1 ? "[]" : sss.substring(0, sss.length() - 1) + "]";
    }

    public static String parseMap(Map<String,Object> i){
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for (String s : i.keySet()) {
            sb.append(String.format("\"%s\":",s));

            if(i.get(s) instanceof List){
                sb.append(listToString((List<Object>) i.get(s)));
            }else if(i.get(s) instanceof Map){
                sb.append(parseMap((Map<String, Object>) i.get(s)));
            }else if(i.get(s) instanceof String || i.get(s).getClass().isEnum()){
                sb.append(String.format("\"%s\"",i.get(s)));
            }else if(ValueUtil.isPrimitive(i.get(s))){
                sb.append(i.get(s));
            }else{
                sb.append(parseObject(i.get(s)));
            }
            sb.append(",");
        }

        return sb.length() == 1 ? "{}" : sb.deleteCharAt(sb.length()-1).append("}").toString();
    }

    public static String parseObject(Object i){
        if(i instanceof List){
            return listToString((List<Object>)i);
        }else if(i instanceof Map){
            return parseMap((Map<String, Object>)i);
        }else{
            StringBuilder sss = new StringBuilder("{");
            for (Field field : i.getClass().getDeclaredFields()) {
                try {
                    if(!((field.getModifiers() & PUBLIC) != 0 || field.getModifiers() == 0)){
                        continue;
                    }
                    field.setAccessible(true);
                    Object v = field.get(i);
                    sss.append("\"").append(field.getName()).append("\":");

                    if(v instanceof String || v.getClass().isEnum()){
                        sss.append("\"");
                        sss.append(v);
                        sss.append("\"");
                    }else if(ValueUtil.isPrimitive(v)){
                        sss.append(v);
                    }else{
                        sss.append(parseObject(v));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                sss.append(",");
            }
            return sss.length() == 1 ? "{}" : sss.deleteCharAt(sss.length()-1).append("}").toString();
        }
    }

}
