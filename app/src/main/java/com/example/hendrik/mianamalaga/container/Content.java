package com.example.hendrik.mianamalaga.container;

import com.example.hendrik.mianamalaga.Constants;

import java.util.ArrayList;
import java.util.List;

public class Content {
    private String originalText;
    private String translationText;
    private String responseText;
    private String responseTranslationText;
    private String audioFile;
    private String videoFile;
    public int jumpIndex;
    private List<String> responseTextList;
    private List<String> responseTranslationTextList;

    public Content() {
        this.setOriginalText("");
        this.setTranslationText("");
        this.setResponseText("");
        this.setResponseTranslationText("");
        this.setAudioFile("");
        this.setVideoFileName("");
        this.jumpIndex = 1;
        this.responseTextList = new ArrayList<>();
        this.responseTranslationTextList = new ArrayList<>();
    }

    public Content(int lesson) {
        this.setOriginalText("");
        this.setTranslationText("");
        this.setResponseText("");
        this.setResponseTranslationText("");
        this.setAudioFile(Constants.AudioName + lesson + ".m4a");
        this.setVideoFileName(Constants.VideoName + lesson + ".mp4");
        this.jumpIndex = 1;
        this.responseTextList = new ArrayList<>();
        this.responseTranslationTextList = new ArrayList<>();
    }

    public String getResponseTranslationText() {
        return responseTranslationText;
    }

    public void setResponseTranslationText(String responseTranslationText) {
        this.responseTranslationText = responseTranslationText;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getTranslationText() {
        return translationText;
    }

    public void setTranslationText(String translationText) {
        this.translationText = translationText;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getVideoFileName() {
        return videoFile;
    }

    public void setVideoFileName(String videoFile) {
        this.videoFile = videoFile;
    }

    public int getJumpIndex(){return jumpIndex; }

    public void addResponse(String responseText, String responseTranslationText){
        responseTextList.add(responseText);
        responseTranslationTextList.add(responseTranslationText);
    }

    public List<String> getResponseTextList(){
        return responseTextList;
    }

    public List<String> getResponseTranslationTextList(){
        return responseTranslationTextList;
    }
}
