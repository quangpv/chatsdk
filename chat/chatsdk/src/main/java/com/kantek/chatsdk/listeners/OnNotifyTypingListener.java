package com.kantek.chatsdk.listeners;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.Timer;
import java.util.TimerTask;

public abstract class OnNotifyTypingListener implements TextWatcher {
    private Timer mTimer;
    private boolean mPause = true;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 0) return;
        if (mPause) {
            synchronized (this) {
                mPause = false;
            }
            composing(true);
        }
        if (mTimer != null) mTimer.cancel();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (OnNotifyTypingListener.this) {
                    if (!mPause) {
                        mPause = true;
                        composing(false);
                    }
                }
            }
        }, 2000);
    }

    protected abstract void composing(boolean isComposing);

    @Override
    public void afterTextChanged(Editable s) {

    }
}
