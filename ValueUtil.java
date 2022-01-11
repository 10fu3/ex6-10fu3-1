public class ValueUtil {
    public static boolean isPrimitive(Object i){
        return (i instanceof Integer || i instanceof Long || i instanceof Float || i instanceof Double || i instanceof Character || i instanceof Boolean);
    }
}
