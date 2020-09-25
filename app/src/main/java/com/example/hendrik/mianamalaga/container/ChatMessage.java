package com.example.hendrik.mianamalaga.container;

import android.os.Environment;

import com.example.hendrik.mianamalaga.Constants;

public class ChatMessage {
    private String mChatMessage;
    private boolean mUser;
    private boolean mOriginal;
    private int mLesson;
    //private String mMediaAbsolutePath;

    public ChatMessage(String chatMessage, boolean user, boolean original, int mLesson){
        this.mChatMessage = chatMessage;
        this.mUser = user;
        this.mOriginal = original;
        this.mLesson = mLesson;
        //this.mMediaAbsolutePath = Environment.getExternalStoragePublicDirectory( Constants.MoraMora ).getAbsolutePath();
    }

    public String getMessage(){
        return mChatMessage;
    }

    public void setMessage(String message){
        mChatMessage = message;
    }

    public boolean isUser(){
        return mUser;
    }

    public boolean isOriginal(){
        return mOriginal;
    }

    public void setOriginal(boolean original){
        mOriginal = original;
    }

    public int getLesson() {
        return mLesson;
    }

    /*
    public void setMediaAbsolutePath(String absolutPath){
        this.mMediaAbsolutePath = absolutPath;
    }

    public String getMediaAbsolutePath(){
        return mMediaAbsolutePath;
    }
     */
}


