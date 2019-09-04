package com.example.hendrik.mianamalaga;

public class Topic {
    private String mName;
    private String mShortInfo;
    private String mImageFileString;
    private String mImageName;
    private int mPosition;

    public Topic(String name, String shortInfo, String imagePath){
        mName = name;
        mShortInfo = shortInfo;
        mImageFileString = imagePath;
        mImageName = Constants.TopicPictureFileName;
        mPosition = 0;
    }

    public Topic(){
        mName = "";
        mShortInfo = "";
        mImageFileString = "";
        mImageName = Constants.TopicPictureFileName;
        mPosition = 0;
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
}
