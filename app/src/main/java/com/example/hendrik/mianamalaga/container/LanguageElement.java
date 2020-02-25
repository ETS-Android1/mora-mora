package com.example.hendrik.mianamalaga.container;
import com.dropbox.core.v2.files.Metadata;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

public class LanguageElement {
    private String language;
    private Metadata metadata;
    private RemoteFile remoteFile;
    private boolean isLanguageToLearn;

    public LanguageElement(String language, boolean isLanguageToLearn){
        this.language = language;
        this.isLanguageToLearn = isLanguageToLearn;
    }

    public LanguageElement(Metadata metadata, String language, boolean isLanguageToLearn){
        this.metadata = metadata;
        this.language = language;
        this.isLanguageToLearn = isLanguageToLearn;
        this.remoteFile = null;
    }

    public LanguageElement(RemoteFile remoteFile, String language, boolean isLanguageToLearn){
        this.metadata = null;
        this.language = language;
        this.isLanguageToLearn = isLanguageToLearn;
        this.remoteFile = remoteFile;
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

    public RemoteFile getRemoteFile() {
        return remoteFile;
    }

    public void setRemoteFile(RemoteFile remoteFile) {
        this.remoteFile = remoteFile;
    }
}
