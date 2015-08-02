package mobi.tattu.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Leandro on 30/05/2015.
 */
public class ArrayUtils {

    public static <T> String join(String joint, T... elems) {
        return join(joint, Arrays.asList(elems));
    }

    public static <T> String join(String joint, List<T> elems) {
        StringBuilder sb = new StringBuilder();
        for (T elem : elems) {
            if (sb.length() > 0) {
                sb.append(joint);
            }
            sb.append(elem);
        }
        return sb.toString();
    }


//    public static <T> List<T> filter(Collection<T> list, Filter<T> f) {
//        List<T> result = new ArrayList<>();
//        for (T t : list) if (f.filter(t)) result.add(t);
//        return result;
//    }
//
//    public interface Filter<T> {
//        boolean filter(T t);
//    }

    public static <T> List<T> concat(List<T> l, T... items) {
        return concat(l, Arrays.asList(items));
    }

    public static <T> List<T> concat(List<T> l1, List<T> l2) {
        List<T> result = new ArrayList<>(l1);
        result.addAll(l2);
        return result;
    }

}
