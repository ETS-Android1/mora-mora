package com.example.hendrik.mianamalaga;

import android.support.annotation.NonNull;

public class Constants {
    /*
    The following is the log tag used to debug the application
     */
    public static final String TAG = ActivityConversation.class.getSimpleName();

    /*
    Naming conventions for intents
     */
    public static final String LanguageToLearn = "LanguageToLearn";
    public static final String MotherTongue = "MotherTongue";
    public static final String FullTemporaryDirectory = "FullTemporaryDirectory";

    /*
    File name conventions. Special file names are used to store specific data
     */
    public static final String InfoFileName = "info.xml";
    public static final String InfoFileNameNew = "info.txt";
    public static final String ChatContentFileName = "chatContent.txt";
    public static final String TopicPictureFileName = "topic_picture.jpg";
    public static final String TemporaryFolder = "temporary";
    public static final String VideoName = "video_";
    public static final String AudioName = "audio_";
    public static final String ImageName = "image_";
    public static final String MoraMora = "MoraMora";

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

    public static final String CloudAccesTokenName = "access-token";
    public static final String CloudAccessToken = "8Muxz4-eeQAAAAAAAAAAGwznwnI4lX9TmSgASCO53mjqcBpDF-bVQN4wwBWB-4DS";
    public static final String SharedPreference = "com.example.hendrik.mianamalaga";

    /*
    NextCloud acces keys
     */
    public static final String UserName = "hendric";
    public static final String Separator = ";";

    /*
    Intent variables
     */
    public static final String EditMode = "EditMode";
}
