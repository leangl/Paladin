package mobi.tattu.utils.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Leandro on 11/6/2015.
 */
public class RetryQueue<T> {

    private static final int MAX_RETRY_DELAY = 60 * 5 * 1000; // 5 minutes
    private static final int INITIAL_RETRY_DELAY = 30 * 1000; // 30 seconds
    private int mCurrentRetryDelay = 0;

    private final String mQueueName;
    private final Action<T> mAction;
    private final Queue<T> mPendingMessages;
    private final boolean mPersistent;
    private final List<WeakReference<QueueListener<T>>> mListeners = new ArrayList<>();
    private Gson mGson;
    private SharedPreferences mPrefs;

    private Handler mHandler;

    public RetryQueue(Context ctx, String name, Action<T> action) {
        this(ctx, name, false, action);
    }

    public RetryQueue(Context ctx, String name, boolean persistent, Action<T> action) {
        mQueueName = name;
        mAction = action;
        mPersistent = persistent;
        if (mPersistent) {
            mPrefs = ctx.getSharedPreferences(name, Context.MODE_PRIVATE);
            mGson = new GsonBuilder().create();
            restorePendingRequestsQueue();
        }
        mPendingMessages = new ConcurrentLinkedQueue<>();
    }

    public void start() {
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
            Type listType = new TypeToken<ArrayList<T>>() {
            }.getType();
            List<T> pendingQueue = mGson.fromJson(pendingQueueString, listType);
            mPendingMessages.addAll(pendingQueue);
        }
        if (!mPendingMessages.isEmpty()) {
            fireRequests();
        }
    }

    private void persistPendingRequests() {
        List<T> requests = new ArrayList<>(mPendingMessages.size());
        for (T request : mPendingMessages) {
            requests.add(request);
        }
        mPrefs.edit().putString(mQueueName, mGson.toJson(requests)).apply();
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
        fireRequests();
    }

    private void fireRequests() {
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
                try {
                    mAction.perform(request);
                    removeFromQueue = true;
                    mCurrentRetryDelay = 0;
                    broadcastRequestCompleted(request);
                } catch (ActionException e) { // if callbacks returns error then retry
                    // initially retry in INITIAL_RETRY_DELAY, the retry exponentially until a max retry delay of MAX_RETRY_DELAY
                    mCurrentRetryDelay = mCurrentRetryDelay >= MAX_RETRY_DELAY ? MAX_RETRY_DELAY : mCurrentRetryDelay > 0 ? mCurrentRetryDelay * 2
                            : INITIAL_RETRY_DELAY;
                    Log.w("RequestQueue_" + mQueueName, "Error sending request. Retrying in: " + mCurrentRetryDelay, e);
                }
                if (removeFromQueue) {
                    removeFromPendingQueue();
                }
                if (!mPendingMessages.isEmpty()) { // if more pending request, then re run task
                    fireRequests(mCurrentRetryDelay);
                }
            }
        }

        private void removeFromPendingQueue() {
            mPendingMessages.poll();
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
        void perform(T obj) throws ActionException;
    }

    public static class ActionException extends Exception {
    }

}
