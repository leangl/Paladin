package mobi.tattu.utils;

import rx.Observable;

/**
 * Created by Leandro on 10/9/2015.
 */
public class RxUtils {

    public static <T> Observable<T> wrapError(F.Action0<? extends T> o) {
        try {
            return Observable.just(o.apply());
        } catch (Throwable throwable) {
            return Observable.error(throwable);
        }
    }

}
