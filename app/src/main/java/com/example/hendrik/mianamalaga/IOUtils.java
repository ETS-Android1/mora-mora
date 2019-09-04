package com.example.hendrik.mianamalaga;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class IOUtils {


    // Checks if external storage is available for read and write
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    // Checks if external storage is available to at least read
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }



    /*
    That function takes the content list created in the ActivityConversation where the conversation content
    and the file names for audio and video files were stored as an argument and writes an xml (content.xml) )file to
    the external storage (SD card or virtual SD card). The relativeResourceDir corresponds to the name of the
    conversation topic
     */

    public static void writeContentListToFile(File file , List<Content> contentList){
        if(isExternalStorageWritable()){

        if(!file.getParentFile().exists()){
            file.getParentFile().mkdir();
        }

            try {
                FileOutputStream stream = new FileOutputStream(file);
                String content = writeContentListToXmlString(contentList);
                stream.write(content.getBytes());
                stream.flush();
                stream.close();
            } catch (IOException e){
                Log.e(Constants.TAG, "IO exception while writing file!");
                Log.e(Constants.TAG, e.getMessage());
            }
        } else {
            if (isExternalStorageReadable()){
                Log.e(Constants.TAG, "External Storage is only readable!");
                //TODO throw Exception
            } else {
                Log.e(Constants.TAG, "External Storage is not available!");
            }
        }

    }

    public static void writeChatContentListToFile(File file , ArrayList<ChatContent> chatContentList){
        if(isExternalStorageWritable()){

            if(!file.getParentFile().exists()){
                file.getParentFile().mkdir();
            }

            ChatContent[] chatContentArray = chatContentList.toArray(new ChatContent[ chatContentList.size() ]);
            Gson gSon = new Gson();
            String target = gSon.toJson(chatContentArray);

            try {
                FileOutputStream stream = new FileOutputStream(file);
                stream.write(target.getBytes());
                stream.flush();
                stream.close();
            } catch (IOException e){
                Log.e(Constants.TAG, "IO exception while writing file!");
                Log.e(Constants.TAG, e.getMessage());
            }
        } else {
            if (isExternalStorageReadable()){
                Log.e(Constants.TAG, "External Storage is only readable!");
                //TODO throw Exception
            } else {
                Log.e(Constants.TAG, "External Storage is not available!");
            }
        }

    }

    public static String readJsonFile(File file){
        String string = "";
        try{
            string = getStringFromFile( file.toString() );
        } catch (Exception e){
            Log.e(Constants.TAG, "Could not read " + file.toString() );
            Log.e(Constants.TAG, e.getMessage() );
        }

        return string;
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }






    /*
    Function to build an xml String out of the data stored in the List<Content>
     */

    private static String writeContentListToXmlString(List<Content> messages){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "messages");
            serializer.attribute("", "number", String.valueOf(messages.size()));
            for (Content msg: messages){
                serializer.startTag("", "entry");
                serializer.startTag("","originalText");
                serializer.text(msg.getOriginalText());
                serializer.endTag("","originalText");
                serializer.startTag("","translationText");
                serializer.text(msg.getTranslationText());
                serializer.endTag("","translationText");
                serializer.startTag("","responseText");
                serializer.text(msg.getResponseText());
                serializer.endTag("","responseText");
                serializer.startTag("","responseTranslationText");
                serializer.text(msg.getResponseTranslationText());
                serializer.endTag("","responseTranslationText");
                serializer.startTag("","audioFile");
                serializer.text(msg.getAudioFile());
                serializer.endTag("","audioFile");
                serializer.startTag("","videoFile");
                serializer.text(msg.getVideoFileName());
                serializer.endTag("","videoFile");
                serializer.startTag("","jumpIndex");
                serializer.text(String.valueOf(msg.getJumpIndex()));
                serializer.endTag("","jumpIndex");
                serializer.endTag("", "entry");
            }
            serializer.endTag("", "messages");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.e(Constants.TAG,"Can't convert ArrayList to String! ");
            Log.e(Constants.TAG, e.getMessage());
            return null;
        }
    }


     /*
    That function takes an instance of Topic created in the ActivityTopicChoice where the topic short info
    and an image were stored as an argument and writes an xml (info.xml) )file to the external storage
    (SD card or virtual SD card). The file corresponds to the name of the topic filename
     */

    public static void writeTopicToFile(File file, Topic topic){
        if( isExternalStorageWritable() ){
             try {
                FileOutputStream stream = new FileOutputStream(file);
                String info = writeTopicToXmlString(topic);
                stream.write(info.getBytes());
                Log.e(Constants.TAG, "File created in " + file.getAbsolutePath());
                stream.flush();
                stream.close();
            } catch (IOException e){
                Log.e(Constants.TAG, "IO exception while writing file!");
                Log.e(Constants.TAG, e.getMessage());
            }
        } else {
            if (isExternalStorageReadable()){
                Log.e(Constants.TAG, "External Storage is only readable!");
                //TODO throw exception
            } else {
                Log.e(Constants.TAG, "External Storage is not available!");
            }
        }

    }

    public static void writeTopicToNewFile(File file, Topic topic){
        if( isExternalStorageWritable() ){
            try {
                FileOutputStream stream = new FileOutputStream(file);
                Gson gSon = new Gson();
                String info = gSon.toJson( topic );
                stream.write(info.getBytes());
                Log.e(Constants.TAG, "File created in " + file.getAbsolutePath());
                stream.flush();
                stream.close();
            } catch (IOException e){
                Log.e(Constants.TAG, "IO exception while writing file!");
                Log.e(Constants.TAG, e.getMessage());
            }
        } else {
            if (isExternalStorageReadable()){
                Log.e(Constants.TAG, "External Storage is only readable!");
                //TODO throw exception
            } else {
                Log.e(Constants.TAG, "External Storage is not available!");
            }
        }

    }



    /*
    Function to build an xml String out of the data stored in an Topic instance
     */

    private static String writeTopicToXmlString(Topic topic){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", Constants.InfoNameSpace);
            serializer.startTag("", Constants.InfoTopicNameTag);
            serializer.text(topic.getName());
            serializer.endTag("",Constants.InfoTopicNameTag);
            serializer.startTag("", Constants.InfoTopicImageTag);
            serializer.text(topic.getImageName());
            serializer.endTag("",Constants.InfoTopicImageTag);
            serializer.startTag("",Constants.InfoShortInfoTag);
            serializer.text(topic.getShortInfo());
            serializer.endTag("",Constants.InfoShortInfoTag);
            serializer.endTag("", Constants.InfoNameSpace);
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.e(Constants.TAG,"Can't convert Topic ArrayList to String! ");
            Log.e(Constants.TAG, e.getMessage());
            return null;
        }
    }




    /*
     Read all topic directories and info.xml file inside them, then extracts the topic name,
     the shortInfo and the image name and puts them into the topic list.
     */

    public static void getTopicListFromFiles(File baseDirectory, ArrayList<Topic> topicArrayList, boolean includeTemporaryFiles){

        Log.e(Constants.TAG,"getTopicListFromFiles() base directory: " + baseDirectory.getAbsolutePath() );
        if (topicArrayList != null){
            HashMap<String,Topic> topicHashMap = new HashMap<>();
            for (Topic topic : topicArrayList){
                topicHashMap.put(topic.getName(), topic);
            }


            File [] topicDirectories = baseDirectory.listFiles();
            if( topicDirectories != null ) {
                for (File topicDirectory : topicDirectories) {
                    if (topicDirectory.isDirectory()) {
                        if (!topicDirectory.getName().equals(Constants.TemporaryFolder)) {
                            File chatContentFile = new File(topicDirectory, Constants.ChatContentFileName);
                            if (chatContentFile.exists() || includeTemporaryFiles) {
                                File resourceFile = new File(topicDirectory, "/" + Constants.InfoFileName);
                                //TODO File resourceFile = new File(topicDirectory, "/" + Constants.InfoFileNameNew);
                                //String topicString = readJsonFile( resourceFile );
                                //Gson gSon = new Gson();
                                //Topic topic = gSon.fromJson( topicString, Topic.class);

                                Topic topic;
                                try {
                                    FileInputStream stream = new FileInputStream(resourceFile);
                                    ContentXmlParser parser = new ContentXmlParser();
                                    topic = parser.parseInfo(stream);
                                    String imageName = topic.getImageName();
                                    topic.setImageFileString(topicDirectory.getAbsolutePath() + File.separatorChar + imageName);
                                    topicHashMap.put(topic.getName(), topic);
                                } catch (IOException | XmlPullParserException e) {
                                    Log.e(Constants.TAG, "IO exception while writing file!");
                                    Log.e(Constants.TAG, e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            topicArrayList.clear();
            topicArrayList.addAll(topicHashMap.values());

        } else {
            Log.e(Constants.TAG, "topicArrayList was not initialized!");
        }
    }
/*
    public static void removeTopicsWithoutContent(ArrayList<Topic> topicArrayList, Context context){
        ArrayList<Topic> topicsToRemove = new ArrayList<>();
        for( Topic topic : topicArrayList){
            String imageFileString = topic.getImageFileString();
            File topicDirectory = new File(imageFileString).getParentFile();
            File contentFile = new File(topicDirectory, Constants.ChatContentFileName);
            if(!contentFile.exists()){
                topicsToRemove.add(topic);
            }

        }

        for (Topic topicToRemove : topicsToRemove){
            topicArrayList.remove(topicToRemove);
        }
    }
*/
    public static void deleteFolder(File folder){
        File [] files = folder.listFiles();
        for(File file : files){
            if (file.isDirectory())
                deleteFolder(file);
            file.delete();
        }
    }


    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        Log.e(Constants.TAG,"copyFile  Copying src:" + srcDir + " Dest: " + dstDir );
        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static boolean prepareFileStructure(String applicationDirectoryString){          //TODO that function should inlcude the temporaray directory as an argument
        File applicationDirectory = new File( applicationDirectoryString );

        if( !isExternalStorageWritable() ){
            Log.e(Constants.TAG, "External Storage is not writable! ");
            return false;
        }

        if( !applicationDirectory.exists() ){
            if( !applicationDirectory.mkdir() ){
                Log.e(Constants.TAG, "Failed to create application directory:  " + applicationDirectoryString);
                return false;
            }
        }
/*
        File temporaryDirectory = new File(applicationDirectory, Constants.TemporaryFolder);

        if( !temporaryDirectory.exists() ){
            if( !temporaryDirectory.mkdir() ){
                Log.e(Constants.TAG, "Failed to create temporary directory:  " + temporaryDirectory.toString());
                return false;
            }
        }
*/
        return true;
    }


    public static boolean isImageFile(String filePath){
        return (filePath.endsWith(".jpg")||filePath.endsWith(".jpeg"));
    }

    public static boolean isVideoFile(String filePath){
        return (filePath.endsWith(".webm")||filePath.endsWith(".mp4"));
    }

    public static boolean isAudioFile(String filePath){
        return (filePath.endsWith(".m4a")||filePath.endsWith(".mp3"));
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degree, boolean mirror) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        if(mirror)mtx.setScale(1,-1);
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public static String convertTopicName(String topicName){
        return topicName.trim().replaceAll("\\s", "_").toLowerCase();
    }

    public static void sortLocales(ArrayList<Locale> list){
        Collections.sort(list, (o1, o2) -> o1.getDisplayLanguage().compareToIgnoreCase(o2.getDisplayLanguage()));
    }

}

