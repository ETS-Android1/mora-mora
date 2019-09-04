package com.example.hendrik.mianamalaga;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContentXmlParser {
    private static final String namespace = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    public Topic parseInfo(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readInfo(parser);
        } finally {
            in.close();
        }
    }

    List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List contentList = new ArrayList();

        parser.require(XmlPullParser.START_TAG,namespace,"messages");
        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = parser.getName();
            if(name.equals("entry")){
                contentList.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return contentList;
    }

    Topic readInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        Topic topic = new Topic();

        parser.require(XmlPullParser.START_TAG,namespace,Constants.InfoNameSpace);
        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = parser.getName();
            if(name.equals(Constants.InfoTopicNameTag)) {
                parser.require(XmlPullParser.START_TAG, namespace, Constants.InfoTopicNameTag);
                topic.setName(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, Constants.InfoTopicNameTag);

            } else if(name.equals(Constants.InfoTopicImageTag)){
                parser.require(XmlPullParser.START_TAG,namespace,Constants.InfoTopicImageTag);
                topic.setImageName(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, Constants.InfoTopicImageTag);

            } else if(name.equals( Constants.InfoShortInfoTag)){
                parser.require(XmlPullParser.START_TAG,namespace,Constants.InfoShortInfoTag);
                topic.setShortInfo(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, Constants.InfoShortInfoTag);
            }
        }
        return topic;
    }

    private Content readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        Content content = new Content();

        parser.require(XmlPullParser.START_TAG,namespace,"entry");
        while (parser.next() != XmlPullParser.END_TAG){
            if(parser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            String name = parser.getName();
            if(name.equals("originalText")){
                parser.require(XmlPullParser.START_TAG,namespace,"originalText");
                content.setOriginalText(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, "originalText");
            } else if (name.equals("translationText")){
                parser.require(XmlPullParser.START_TAG,namespace,"translationText");
                content.setTranslationText(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, "translationText");
            } else if (name.equals("responseText")){
                parser.require(XmlPullParser.START_TAG,namespace,"responseText");
                content.setResponseText(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, "responseText");
            } else if (name.equals("responseTranslationText")){
                parser.require(XmlPullParser.START_TAG,namespace,"responseTranslationText");
                content.setResponseTranslationText(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, "responseTranslationText");
            } else if (name.equals("audioFile")){
                parser.require(XmlPullParser.START_TAG,namespace,"audioFile");
                content.setAudioFile(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, "audioFile");
            } else if (name.equals("videoFile")){
                parser.require(XmlPullParser.START_TAG, namespace, "videoFile");
                content.setVideoFileName(readText(parser));
                parser.require(XmlPullParser.END_TAG, namespace, "videoFile");
            } else if (name.equals("jumpIndex")){
                parser.require(XmlPullParser.START_TAG, namespace, "jumpIndex");
                String string = readText(parser);
                try {
                    content.jumpIndex = Integer.parseInt(string);
                } catch (NumberFormatException e){
                    content.jumpIndex = 1;
                }
                parser.require(XmlPullParser.END_TAG, namespace, "jumpIndex");
            }
        }
        return content;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
