package com.example.hendrik.mianamalaga;

public class Topic {

    private String mName;
    private String mShortInfo;
    private String mImageFileString;
    private String mImageName;
    private String mAuthor;
    private String mSignature;
    private int mPosition;
    private int mDifficulty;
    private int mLikes;
    private int mDislikes;
    private long mFolderSize;



    public Topic(String name, String shortInfo, String imagePath){
        mName = name;
        mShortInfo = shortInfo;
        mImageFileString = imagePath;
        mImageName = Constants.TopicPictureFileName;
        mPosition = 0;
        mAuthor = "";
        mSignature = "";
        mLikes = 0;
        mDislikes = 0;
        mFolderSize = 0;
    }

    public Topic(){
        mName = "";
        mShortInfo = "";
        mImageFileString = "";
        mImageName = Constants.TopicPictureFileName;
        mPosition = 0;
        mDifficulty = 0;
        mAuthor = "";
        mSignature = "";
        mLikes = 0;
        mDislikes = 0;
        mFolderSize = 0;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getShortInfo() {
        return mShortInfo;
    }

    public void setShortInfo(String shortInfo) {
        mShortInfo = shortInfo;
    }

    public String getImageFileString() {
        return mImageFileString;
    }

    public void setImageFileString(String imageFileString) {
        mImageFileString = imageFileString;
    }

    public String getImageName() {
        return mImageName;
    }

    public void setImageName(String mImageName) {
        this.mImageName = mImageName;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }



    public int getDifficulty() { return mDifficulty;  }

    public void setDifficulty(int difficulty) {  this.mDifficulty = difficulty;  }

    public String getAuthor() {  return mAuthor;  }

    public void setAuthor(String author) {  this.mAuthor = author;  }

    public String getSignature() { return mSignature; }

    public void setSignature(String signature) { this.mSignature = signature; }

    public int getLikes() { return mLikes; }

    public void setLikes(int likes) { this.mLikes = likes; }

    public int getDislikes() { return mDislikes; }

    public void setDislikes(int dislikes) { this.mDislikes = dislikes; }

    public long getFolderSize() { return mFolderSize; }

    public void setFolderSize(long folderSize) { this.mFolderSize = folderSize; }
}
