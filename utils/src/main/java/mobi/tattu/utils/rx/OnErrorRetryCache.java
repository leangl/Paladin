package mobi.tattu.utils.rx;

import android.util.Log;

import java.util.concurrent.Semaphore;

import roboguice.util.Ln;
import rx.Observable;

public class OnErrorRetryCache<T> {

    public static <T> Observable<T> from(Observable<T> source) {
        return new OnErrorRetryCache<>(source).deferred;
    }

    private final Observable<T> deferred;
    private final Semaphore singlePermit = new Semaphore(1);

    private Observable<T> cache = null;
    private Observable<T> inProgress = null;

    private OnErrorRetryCache(Observable<T> source) {
        deferred = Observable.defer(() -> createWhenObserverSubscribes(source));
    }

    private Observable<T> createWhenObserverSubscribes(Observable<T> source) {
        singlePermit.acquireUninterruptibly();

        Observable<T> cached = cache;
        if (cached != null) {
            singlePermit.release();
            return cached;
        }

        inProgress = source
                .doOnCompleted(this::onSuccess)
                .doOnError(this::onError)
                .doOnTerminate(this::onTermination)
                .replay()
                .autoConnect();

        return inProgress;
    }

    private void onSuccess() {
        cache = inProgress;
    }

    private void onError(Throwable error) {
        Ln.e(error)
;    }

    private void onTermination() {
        inProgress = null;
        singlePermit.release();
    }
}