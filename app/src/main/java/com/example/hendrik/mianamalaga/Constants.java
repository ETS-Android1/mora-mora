package com.example.hendrik.mianamalaga;

import androidx.annotation.NonNull;

import com.example.hendrik.mianamalaga.activities.ActivityConversation;

public class Constants {
    /*
    The following is the log tag used to debug the application
     */
    public static final String TAG = ActivityConversation.class.getSimpleName();

    /*
    Naming conventions for shared preference
     */
    public static final String KnownLanguage = "KnownLanguage";
    public static final String ConversationListCount = "ConversationListCount";

    /*
    Naming conventions for intents
     */
    public static final String LanguageToLearn = "LanguageToLearn";
    public static final String MotherTongue = "MotherTongue";
    public static final String FullTemporaryDirectory = "FullTemporaryDirectory";
    public static final String RelativeResourceDirectory = "RelativeResourceDirectory";

    /*
    File name conventions. Special file names are used to store specific data
     */
    public static final String InfoFileName = "info.xml";
    public static final String InfoFileNameNew = "info.txt";
    public static final String ChatContentFileName = "chatContent.txt";
    public static final String TopicPictureFileName = "topic_picture.jpg";
    public static final String TemporaryFolder = BuildConfig.TEMPORARY_FOLDER_NAME;
    public static final String VideoName = "video_";
    public static final String AudioName = "audio_";
    public static final String ImageName = "image_";
    public static final String BackgroundImageName = "BackgroundImage.jpeg";
    public static final String MoraMora = BuildConfig.APPLICATION_NAME;

    /*
    These three constants are xml tags used inside of the info.xml file (one in each conversation
    folder ) where a short description and an image file name is stored.
     */
    public static final String InfoNameSpace = "topicInfo";
    public static final String InfoTopicNameTag = "topicName";
    public static final String InfoTopicImageTag = "topicImage";
    public static final String InfoShortInfoTag = "info";

    /*
    Define which cloud provide is to choose
     */
    public enum CloudProvider{
        DROPBOX, NEXTCLOUD
    }

    @NonNull public static CloudProvider cloudProvider = CloudProvider.NEXTCLOUD;                            // TODO This should not be here but fixed inside of Preference Activity

    /*
    Dropbox access keys
     */

    public static final String OldCloudAccesTokenName = "access-token";
    public static final String SharedPreference = "com.example.hendrik.mianamalaga";

    /*
    NextCloud acces keys
     */
    public static final String Separator = ";";
    public static final String Root_path = "/";

    public static final String TimeStampOfLastRemove = "TimeStampOfLastRemove";

    /*
    Intent variables
     */
    public static final String EditMode = "EditMode";

    /*
    Database related stuff
     */
    public static final String DatabaseName = "Mora-Mora-Dictionary.db";
}
