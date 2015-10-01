package mobi.tattu.utils.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Cristian on 11/6/2015.
 */
public class RetryQueue<T> {

//    private static final int MAX_RETRY_DELAY = 60 * 5 * 1000; // 5 minutes
 private static final int MAX_RETRY_DELAY = 30 * 1000; // 30 seconds
    private static final int INITIAL_RETRY_DELAY = 30 * 1000; // 30 seconds
    private int mCurrentRetryDelay = 0;

    private final String mQueueName;
    private final Action<T> mAction;
    private final Queue<T> mPendingMessages;
    private final boolean mPersistent;
    private final List<WeakReference<QueueListener<T>>> mListeners = new ArrayList<>();
    private Gson mGson;
    private SharedPreferences mPrefs;
    private Class<T> type;

    private Handler mHandler;



    public RetryQueue(Context ctx, String name, boolean persistent, Action<T> action, Class<T> type)  {
        mQueueName = name;
        mAction = action;
        mPersistent = persistent;
        mPendingMessages = new ConcurrentLinkedQueue<>();
        this.type = type;
        if (mPersistent) {
            mPrefs = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
            mGson = new GsonBuilder().create();
            restorePendingRequestsQueue();
        }
    }


    /**
     * Este metodo se tiene que invocar antes de llamar al metodo {@link RetryQueue#fireRequests()}
     *
     */
    public void init() {
        HandlerThread thread = new HandlerThread(mQueueName, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        start(thread.getLooper());
    }

    public void start(Looper looper) {
        stop();
        mHandler = new Handler(looper);
    }

    public void stop() {
        if (isStarted()) {
            mHandler.getLooper().quit();
            mHandler = null;
        }
    }

    public boolean isStarted() {
        return mHandler != null;
    }

    private void restorePendingRequestsQueue() {
        String pendingQueueString = mPrefs.getString(mQueueName, null);
        if (pendingQueueString != null) {

            List<T> pendingQueue = mGson.fromJson(pendingQueueString, new ListOfJson<T>(type));
            mPendingMessages.addAll(pendingQueue);
        }

    }


    private void persistPendingRequests() {

        List<T> requests = new ArrayList<>(mPendingMessages.size());
        for (T request : mPendingMessages) {
            requests.add(request);
        }
        if(requests.size() > 0){
            mPrefs.edit().putString(mQueueName, mGson.toJson(requests)).commit();
        }else{
            mPrefs.edit().clear().commit();
        }
    }

    public String getName() {
        return mQueueName;
    }

    public void add(T obj) {
        mCurrentRetryDelay = 0;
        mPendingMessages.add(obj);
        if (isPersistent()) {
            persistPendingRequests();
        }

    }

    /**
     * Lanza los requests
     */
    public void fireRequests() {

        fireRequests(0);
    }


    /**
     * Sends state update to the server for every pending update in the queue
     *
     * @param delay
     */
    private void fireRequests(long delay) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(mRetryTask, delay);
    }

    private Runnable mRetryTask = new Runnable() {
        @Override
        public void run() {
            if (!mPendingMessages.isEmpty()) {
                T request = mPendingMessages.peek();

                boolean removeFromQueue = false;
                boolean result = mAction.perform(request);
                if(result){
                    removeFromQueue = true;
                    mCurrentRetryDelay = 0;
                    broadcastRequestCompleted(request);
                }else{
                    // initially retry in INITIAL_RETRY_DELAY, the retry exponentially until a max retry delay of MAX_RETRY_DELAY
                    mCurrentRetryDelay = mCurrentRetryDelay >= MAX_RETRY_DELAY ? MAX_RETRY_DELAY : mCurrentRetryDelay > 0 ? mCurrentRetryDelay * 2
                            : INITIAL_RETRY_DELAY;
                    Log.w("RequestQueue_" + mQueueName, "Error sending request. Retrying in: " + mCurrentRetryDelay);
                }

                if (removeFromQueue) {
                    removeFromPendingQueue();
                }
                if (!mPendingMessages.isEmpty()) { // if more pending request, then re run task
                    fireRequests(mCurrentRetryDelay);
                }else {
                    stop();
                }
            }
        }

        private void removeFromPendingQueue() {
            mPendingMessages.clear();
            if (isPersistent()) {
                persistPendingRequests();
            }
        }

    };

    public boolean isPersistent() {
        return mPersistent;
    }

    public void registerListener(QueueListener<T> listener) {
        synchronized (mListeners) {
            mListeners.add(new WeakReference<>(listener));
        }
    }

    public void unregisterListener(QueueListener<T> target) {
        synchronized (mListeners) {
            for (Iterator<WeakReference<QueueListener<T>>> it = mListeners.iterator(); it.hasNext(); ) {
                QueueListener listener = it.next().get();
                if (listener == null || listener == target) {
                    it.remove();
                }
            }
        }
    }

    private void broadcastRequestCompleted(T request) {
        synchronized (mListeners) {
            for (Iterator<WeakReference<QueueListener<T>>> it = mListeners.iterator(); it.hasNext(); ) {
                QueueListener<T> listener = it.next().get();
                if (listener != null) {
                    listener.onSuccess(this, request);
                } else {
                    it.remove();
                }
            }
        }
    }

    public interface QueueListener<T> {
        void onSuccess(RetryQueue queue, T obj);
    }

    public interface Action<T> {
        /**
         *
         * @param obj
         * @return true indica que se tiene que terminar de ejecutar la tarea
         */
        boolean perform(T obj) ;
    }

    public static class ActionException extends Exception {
    }
    public class ListOfJson<T> implements ParameterizedType {

        private Class<?> wrapped;

        public ListOfJson(Class<T> wrapper) {
            this.wrapped = wrapper;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    public void removeAllRetryQueue(){
        mPendingMessages.clear();
        if(isPersistent()){
            persistPendingRequests();
        }
    }
}
