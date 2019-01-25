package com.kantek.chatsdk.listeners;

import android.text.Editable;
import android.text.TextWatcher;

import org.jivesoftware.smackx.chatstates.ChatState;

import java.util.Timer;
import java.util.TimerTask;

public abstract class OnNotifyTypingListener implements TextWatcher {
    private Timer mTimer;
    private ChatState mChatState = ChatState.active;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 0) {
            mChatState = ChatState.active;
            composing(mChatState.toString());
            return;
        }
        if (mChatState != ChatState.composing) {
            synchronized (this) {
                mChatState = ChatState.composing;
            }
            composing(mChatState.toString());
        }
        if (mTimer != null) mTimer.cancel();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (OnNotifyTypingListener.this) {
                    if (mChatState == ChatState.active) return;
                    if (mChatState == ChatState.composing) {
                        mChatState = ChatState.paused;
                        composing(mChatState.toString());
                    }
                }
            }
        }, 2000);
    }

    protected abstract void composing(String chatState);

    @Override
    public void afterTextChanged(Editable s) {

    }
}
