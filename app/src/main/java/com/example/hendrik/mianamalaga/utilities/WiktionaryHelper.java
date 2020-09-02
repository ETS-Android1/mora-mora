/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.hendrik.mianamalaga.utilities;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.container.DictionaryObject;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper methods to simplify talking with and parsing responses from a
 * lightweight Wiktionary API. Before making any requests, you should call
 * {@link #prepareUserAgent(Context)} to generate a User-Agent string based on
 * your application package name and version.
 */

public class WiktionaryHelper {


    /**
     * {@link StatusLine} HTTP status code when no server error has occurred.
     */
    private static final int HTTP_STATUS_OK = 200;
    /**
     * Shared buffer used by {@link #getUrlContent(String)} when reading results
     * from an API request.
     */
    private static byte[] sBuffer = new byte[512];
    /**
     * User-agent string to use when making requests. Should be filled using
     * {@link #prepareUserAgent(Context)} before making any other calls.
     */
    private static String sUserAgent = null;

    /**
     * Thrown when there were problems contacting the remote API server, either
     * because of a network error, or the server returned a bad status code.
     */
    public static class ApiException extends Exception {
        public ApiException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ApiException(String detailMessage) {
            super(detailMessage);
        }
    }

    /**
     * Thrown when there were problems parsing the response to an API call,
     * either because the response was empty, or it was malformed.
     */
    public static class ParseException extends Exception {
        public ParseException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }

    /**
     * Prepare the internal User-Agent string for use. This requires a
     * {@link Context} to pull the package name and version number for this
     * application.
     */
    public static void prepareUserAgent(Context context) {
        try {

            PackageManager manager = context.getPackageManager();                                   // Read package name and version number from manifest
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            sUserAgent = String.format("mora.mora@gmx.net-", info.packageName, info.versionName);   // TODO the two parameters are not yet inserted there should be two %s values in the String to format

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.TAG, "Couldn't find package information in PackageManager", e);
        }
    }

    /**
     * Read and return the content for a specific Wiktionary page. This makes a
     * lightweight API call, and trims out just the page content returned.
     * Because this call blocks until results are available, it should not be
     * run from a UI thread.
     *
     * @param title           The exact title of the Wiktionary page requested.
     * @return Exact content of page.
     * @throws ApiException   If any connection or server error occurs.
     * @throws ParseException If there are problems parsing the response.
     */
 /*   public static List getPageContent(String title )
            throws ApiException {
        String encodedTitle = Uri.encode(title);                                                    // Encode page title and expand templates if requested

        return getUrlContent( WIKTIONARY + encodedTitle );

    }*/

    /**
     * Pull the raw text content of the given URL. This call blocks until the
     * operation has completed, and is synchronized because it uses a shared
     * buffer {@link #sBuffer}.
     *
     * @param urlString The exact URL to request.
     * @return The raw content returned by the server.
     * @throws ApiException If any connection or server error occurs.
     */
    public static synchronized List getUrlContent(String urlString) throws ApiException {
        if (sUserAgent == null) {
            throw new ApiException("User-Agent string must be prepared");
        }

        URL url = null;
        System.out.println("Doing REST API request to " + urlString );

        try {
            url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", sUserAgent);
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Contact-Me", "mora.mora@gmx.net");


            if( connection.getResponseCode() == HTTP_STATUS_OK  ) {

                InputStream responseBody = connection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                JsonReader jsonReader = new JsonReader(responseBodyReader);

                List<DictionaryObject> dictionaryObjectList = new ArrayList<DictionaryObject>();
                dictionaryObjectList.add( new DictionaryObject("url", url.toString()));

                parseJsonStream( jsonReader, dictionaryObjectList );

                return dictionaryObjectList;

            } else {
                Log.e(Constants.TAG, "GET failed. Response Code :: " + connection.getResponseCode() );
            }

        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (ProtocolException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }


    private static void parseJsonStream( JsonReader reader, List list ){

        try {
            while (reader.peek() != JsonToken.END_DOCUMENT ) {
                JsonToken nextToken = reader.peek();

                switch( nextToken ){
                    case BEGIN_ARRAY:
                        reader.beginArray();
                        parseJsonStream( reader,list );
                        break;
                    case END_ARRAY:
                        reader.endArray();
                        break;
                    case BEGIN_OBJECT:
                        reader.beginObject();
                        parseJsonStream( reader,list );
                        break;
                    case END_OBJECT:
                        reader.endObject();
                        break;
                    case NAME:
                        String key = reader.nextName();
                        JsonToken tokenAfterNameToken = reader.peek();
                        if( tokenAfterNameToken == JsonToken.STRING ){
                            String string = reader.nextString();
                            list.add( new DictionaryObject( key, string ) );
                            //Log.e(Constants.TAG,"key -String: " + key + " - " + string );
                        }
                        break;
                    case STRING:
                        reader.nextString();
                        break;
                    default:
                        reader.skipValue();

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
