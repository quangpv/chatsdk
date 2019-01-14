package ps.billyphan.chatsdk.models;

import android.arch.lifecycle.Observer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import ps.billyphan.chatsdk.ChatExecutors;

@SuppressWarnings("unchecked")
public class Observable implements Serializable {
    private transient Set<Observer<Observable>> mObservers = new HashSet<>();

    public void notifyChanged() {
        for (Observer<Observable> observer : mObservers) {
            ChatExecutors.onMainThread(() -> observer.onChanged(this));
        }
    }

    public <T extends Observable> void addObserver(Observer<T> observer) {
        mObservers.add((Observer<Observable>) observer);
    }

    public <T extends Observable> void removeObserver(Observer<T> observer) {
        mObservers.add((Observer<Observable>) observer);
    }

    public void removeObservers() {
        mObservers.clear();
    }
}
