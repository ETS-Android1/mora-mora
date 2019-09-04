package com.example.hendrik.mianamalaga;
import com.dropbox.core.v2.files.Metadata;

public class LanguageElement {
    private String language;
    private Metadata metadata;
    private boolean isLanguageToLearn;

    public LanguageElement(String language, boolean isLanguageToLearn){
        this.language = language;
        this.isLanguageToLearn = isLanguageToLearn;
    }

    public LanguageElement(Metadata metadata, String language, boolean isLanguageToLearn){
        this.metadata = metadata;
        this.language = language;
        this.isLanguageToLearn = isLanguageToLearn;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public boolean isLanguageToLearn() {
        return isLanguageToLearn;
    }

    public void setLanguageToLearn(boolean languageToLearn) {
        isLanguageToLearn = languageToLearn;
    }

}
