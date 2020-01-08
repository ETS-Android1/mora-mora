package com.example.hendrik.mianamalaga;

import com.example.hendrik.mianamalaga.fragments.FragmentPageResponse;

public class ResponseChoicePagerItem {

    private String mResponseString;
    private FragmentPageResponse mPageFragment;

    public ResponseChoicePagerItem(String responses, FragmentPageResponse fragment){
        this.mResponseString = responses;
        this.mPageFragment = fragment;
    }

    public void setResponse(String  responses){
        this.mResponseString = responses;
    }

    public String getResponseString(){
        return this.mResponseString;
    }

    public void setPageFragment(FragmentPageResponse fragment){
        this.mPageFragment = fragment;
    }

    FragmentPageResponse getFragment(){
        return this.mPageFragment;
    }

}
