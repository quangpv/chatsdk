package ps.billyphan.chatsdk;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.Consumer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ps.billyphan.chatsdk.listeners.Supplier;

public class ChatExecutors {

    private static ChatExecutors sInstance = new ChatExecutors();
    private Executor networkIO = Executors.newFixedThreadPool(3);
    private Handler mainThread = new Handler(Looper.getMainLooper());

    public static void inBackground(Runnable runnable) {
        sInstance.networkIO.execute(runnable);
    }

    public static <T> Loader<T> loadInBackground(Supplier<T> runnable) {
        return new Loader<>(runnable);
    }

    public static void onMainThread(Runnable runnable) {
        sInstance.mainThread.post(runnable);
    }

    public static class Loader<T> {
        private final Supplier<T> mBackgroundCallback;

        public Loader(Supplier<T> runnable) {
            mBackgroundCallback = runnable;
        }

        @SuppressWarnings("unchecked")
        public void onMainThread(Consumer<T> consumer) {
            sInstance.networkIO.execute(() -> {
                T value = (T) mBackgroundCallback.get();
                sInstance.mainThread.post(() -> consumer.accept(value));
            });
        }
    }
}
