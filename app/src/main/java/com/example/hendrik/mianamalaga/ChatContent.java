package com.example.hendrik.mianamalaga;

public class ChatContent {
    private String[] nativeMessages;
    private String[] translatedMessages;
    private String[] mediaFileNames;
    private String imageFileName;
    private boolean isUser;
    private int jumpIndex;

    ChatContent(){
        nativeMessages = new String[]{""};
        translatedMessages = new String[]{""};
        mediaFileNames = new String[]{""};
        imageFileName = "";
        jumpIndex = 1;
    }

    public String[] getNativeMessages() {
        return nativeMessages;
    }

    public void setNativeMessages(String[] nativeMessages) {
        this.nativeMessages = nativeMessages;
    }

    public String[] getTranslatedMessages() {
        return translatedMessages;
    }

    public void setTranslatedMessages(String[] translatedMessages) {
        this.translatedMessages = translatedMessages;
    }

    public String[] getMediaFileNames() {
        return mediaFileNames;
    }

    public void setMediaFileNames(String[] mediaFileNames) {
        this.mediaFileNames = mediaFileNames;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public int getJumpIndex() {
        return jumpIndex;
    }

    public void setJumpIndex(int jumpIndex) {
        this.jumpIndex = jumpIndex;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }
}
