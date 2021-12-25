package com.example.hendrik.mianamalaga.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.R;
import com.example.hendrik.mianamalaga.adapter.AdapterDictionaryTranslation;
import com.example.hendrik.mianamalaga.adapter.AdapterLanguageSpinner;
import com.example.hendrik.mianamalaga.container.DictionaryObject;
import com.example.hendrik.mianamalaga.tasks.DownloadFileFromUrlAsyncTask;
import com.example.hendrik.mianamalaga.tasks.WiktionaryLookupTask;
import com.example.hendrik.mianamalaga.utilities.DictDatabaseHelper;
import com.example.hendrik.mianamalaga.utilities.Utils;
import com.example.hendrik.mianamalaga.utilities.WiktionaryHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//TODO It is possible to find more than one translation - neige (deutsch, französisch). There must be multiple sound files. Use the correct one
// TODO take language in spinner into consideration  - adapt the WIKTIONARY constants String.format(WIKTIONARY, language) - https://%s.wiktionary.org...... mKnownLanguagelocale.getLanguage()


public class ActivityDictionary extends AppCompatActivity {

    /**
     * Some status definitions to manage the database saving process
     */

    private enum Status {
        DOWNLOAD_AUDIO_FILE,
        SAVE_WORDS
    }

    /**
     * Partial URL to use when requesting the detailed entry for a specific
     * Wiktionary page. Use {@link String#format(String, Object...)} to insert
     * the desired page title after escaping it as needed.
     */

    private static final String WIKTIONARY = "https://de.wiktionary.org/w/api.php?action=parse&format=json&prop=text|revid|displaytitle&page=";
    private static final String WIKTIONARY_GENERIC = "https://%s.wiktionary.org/w/api.php?action=parse&format=json&prop=text|revid|displaytitle&page=";
    private static final String WIKTIONARY_MEANING_GENERIC = "https://%s.wiktionary.org/w/api.php?action=parse&prop=wikitext&section=3&format=json&page=";
    private static final String WIKTIONARY_REST_API_PAGE = "https://en.wiktionary.org/api/rest_v1/page/definition/";

    /**
     * Partial URL to use when requesting the detailed entry for a specific
     * Wiktionary page. Use {@link String#format(String, Object...)} to insert
     * the desired page title after escaping it as needed.
     */

    static final String MEANING_PATTERN = "\\[\\[.+?\\]\\]";            // everything between [[ ]]
    static final String FILE_PATTERN = "//upload\\S+.ogg";                //non-whitespace character \S , + one or multiple times


    private Context mContext;
    public ProgressBar mProgressBar;
    private ConstraintLayout mMainLayout;
    private AdapterDictionaryTranslation mTranslationsAdapter;
    private ArrayList<String> mTranslationsArrayList;
    private ArrayList<DictionaryObject> mWordArrayList;
    private boolean mEditMode;
    private String mKnownLanguageDirString;
    private File mApplicationDirectoryFile;
    private File mTemporaryDirectory;
    private Locale mKnownLanguageLocale;
    private DictDatabaseHelper mDatabaseHelper;

    private EditText mDictWordEditText;
    private WebView mWebView;
    private FloatingActionButton mSoundFab;
    private FloatingActionButton mSaveToDatabaseFab;

    private String mAudioFileUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dictionary);

        mContext = this;
        mProgressBar = findViewById(R.id.dict_progressBar);
        mMainLayout = findViewById(R.id.activity_language_choice_main_layout);

        getIntents();
        setupActionBar();
        setupUserInputTextView();
  //      setupKnownLanguageSpinner();
        setupTranslationsRecyclerView();


        setupFabs();


    }


    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, ActivityConversation.class);                // TODO is this necessary? Why not startActivity(intent);
        intent.putExtra("EditMode", mEditMode);                                              // TODO This is not working properly
        intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString());
        intent.putExtra(Constants.RelativeResourceDirectory, mKnownLanguageDirString);
        intent.putExtra(Constants.MoraMora, mApplicationDirectoryFile.toString());
        super.onBackPressed();
    }

    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Toolbar toolbar = findViewById(R.id.toolbar_language_choice);
            setSupportActionBar(toolbar);
            //getSupportActionBar().setIcon(R.drawable.ic_wiktionary_background);
        }
    }

    private void getIntents() {
        if (getIntent().getExtras() != null) {
            mEditMode = getIntent().getExtras().getBoolean(Constants.EditMode);
            String relResourceDirectoryString = getIntent().getExtras().getString(Constants.RelativeResourceDirectory).toLowerCase();
            mKnownLanguageDirString = new File(relResourceDirectoryString).getParentFile().toString();
            mApplicationDirectoryFile = new File(getIntent().getExtras().getString(Constants.MoraMora));
            mTemporaryDirectory = new File(getIntent().getExtras().getString(Constants.FullTemporaryDirectory));

            if (!Utils.prepareFileStructure(mApplicationDirectoryFile.toString())) {
                finish();
            }
        }
    }

    private void setupUserInputTextView() {
        mDictWordEditText = findViewById(R.id.dict_word_editText);
        mDictWordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                String wordToLookup = mDictWordEditText.getText().toString();
                String encodedTitle = Uri.encode(wordToLookup);

                //String urlToLookUp = WIKTIONARY + encodedTitle;
                String urlToLookUp = String.format( WIKTIONARY_GENERIC, mKnownLanguageLocale.getLanguage() ) + encodedTitle;

                WiktionaryHelper.prepareUserAgent(this);
                mProgressBar.setVisibility(View.VISIBLE);
                new WiktionaryLookupTask(this, result -> processRestApiResult(result)).execute(urlToLookUp);
            }
            return false;
        });
    }


    private void processRestApiResult(List<DictionaryObject> result) {

        if( result == null ){
            showSnackbar("Nothing found! Are you connected to the internet?");
            return;
        }

        StringBuilder builder = new StringBuilder();
        String baseUrl = null;
        String content = null;

        mProgressBar.setVisibility(View.GONE);

        for (DictionaryObject dictionaryObject : result) {
            String name = dictionaryObject.getName();

            switch (name) {
                case "definition":
                case "language":
                case "example":
                case "translation":
                case "*":
                    content = dictionaryObject.getValue();
                    builder.append(dictionaryObject.getValue());
                    builder.append("<br>");
                    builder.append("<br>");
                    break;
                case "url":
                    baseUrl = dictionaryObject.getValue();
                default:
                    break;
            }
        }


        Pattern FilePattern = Pattern.compile(FILE_PATTERN);
        Matcher matcher = FilePattern.matcher(content);

        if (matcher.find()) {
            String matchedString = matcher.group(0);
            mAudioFileUrl = "https:" + matchedString;
            mSoundFab.show();
            mSaveToDatabaseFab.show();
            System.out.println("\n" + "Audio file: " + mAudioFileUrl);
        } else {
            System.out.println("Found no audio file url!");
            mSoundFab.hide();
        }


        mWebView = findViewById(R.id.dict_webView);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.loadDataWithBaseURL(baseUrl, builder.toString(), "text/html", "UTF-8", null);         //loadData makes problems with special characters e.g. ä,ü...
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });

        String wordToLookup = mDictWordEditText.getText().toString();
        String encodedTitle = Uri.encode(wordToLookup);
        String urlToLookUp = String.format( WIKTIONARY_MEANING_GENERIC, mKnownLanguageLocale.getLanguage() ) + encodedTitle;

        WiktionaryHelper.prepareUserAgent(this);
        mProgressBar.setVisibility(View.VISIBLE);
        new WiktionaryLookupTask(this, translations -> processTranslationResults(translations)).execute(urlToLookUp);
    }

    private void processTranslationResults(List<DictionaryObject> result) {
        mProgressBar.setVisibility(View.GONE);
        String content = null;

        for (DictionaryObject dictionaryObject : result) {
            String name = dictionaryObject.getName();

            switch (name) {
                case "*":
                    content = dictionaryObject.getValue();
                    break;
                default:
                    break;
            }
        }


        Pattern MeaningPattern = Pattern.compile(MEANING_PATTERN);
        Matcher matcher = MeaningPattern.matcher(content);

        mTranslationsArrayList.clear();
        mWordArrayList.clear();
        while (matcher.find()) {
            String translations = matcher.group(0);
            String trans = translations.replace("[[", "").replace("]]", "");

            mTranslationsArrayList.add(trans);    // TODO this is applied on a null object reference find out why
            String wordToLookUp = mDictWordEditText.getText().toString();
            mWordArrayList.add( new DictionaryObject(wordToLookUp, trans ));
            //Log.e( Constants.TAG, "Meaning: " + trans );

        }
        mTranslationsAdapter.notifyDataSetChanged();

    }

    private void setupFabs() {
        mSoundFab = findViewById(R.id.dict_sound_button);
        mSoundFab.hide();
        mSoundFab.setOnClickListener(v -> {

            if (mAudioFileUrl != null) {
                try {
                    Uri uri = Uri.parse(mAudioFileUrl);
                    MediaPlayer player = new MediaPlayer();
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource(mContext, uri);
                    player.prepare();
                    player.start();
                    player.setOnCompletionListener(mp -> {
                        mp.stop();
                        mp.reset();
                    });
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            } else {
                String wordToLookup = mDictWordEditText.getText().toString();
                String encodedTitle = Uri.encode(wordToLookup);
                String urlToLookUp = WIKTIONARY + encodedTitle;

                WiktionaryHelper.prepareUserAgent(this);
                mProgressBar.setVisibility(View.VISIBLE);
                new WiktionaryLookupTask(this, result -> processRestApiResult(result)).execute(urlToLookUp);
            }
        });

        mSaveToDatabaseFab = findViewById(R.id.dict_save_button);
        mSaveToDatabaseFab.hide();
        mSaveToDatabaseFab.setOnClickListener(v -> {
            if( mAudioFileUrl != null ){
                saveDataToDatabase( Status.DOWNLOAD_AUDIO_FILE, null);
            } else {
                saveDataToDatabase( Status.SAVE_WORDS, null );
            }
        });

    }
/*
    private void setupKnownLanguageSpinner() {

        Spinner knownLanguageSpinner = findViewById(R.id.dictionary_language_spinner);
        ArrayList<Locale> knownLocalesArrayList = new ArrayList<>();

        String[] languages = Locale.getISOLanguages();
        Map<String, Locale> localeMap = new HashMap<>(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            localeMap.put(locale.getLanguage(), locale);
        }

        for (Locale locale : localeMap.values()) {
            knownLocalesArrayList.add(locale);
        }

        Utils.sortLocales(knownLocalesArrayList);
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);
        String knownLanguageLocaleLanguage = sharedPrefs.getString(Constants.KnownLanguage, Locale.getDefault().getLanguage());

        mKnownLanguageLocale = new Locale(knownLanguageLocaleLanguage);
        int defaultPosition = findLocalePosition(mKnownLanguageLocale, knownLocalesArrayList);

        AdapterLanguageSpinner spinnerAdapter = new AdapterLanguageSpinner(this, android.R.layout.simple_spinner_item,
                R.id.list_element_language_spinner_text_view, knownLocalesArrayList);

        knownLanguageSpinner.setAdapter(spinnerAdapter);
        knownLanguageSpinner.setSelection(defaultPosition);
        knownLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mKnownLanguageLocale = knownLocalesArrayList.get(position);
                SharedPreferences sharedPrefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Constants.KnownLanguage, mKnownLanguageLocale.getISO3Language());
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
*/
    /**
     * Finds the position of a specific locale inside a List of locales
     * @param locale the locale which position is wanted
     * @param localesArrayList the ArrayList where the locale can be found
     * @return the position of the locale inside the list
     */

    private int findLocalePosition(Locale locale, ArrayList<Locale> localesArrayList) {

        if (localesArrayList != null) {
            for (int index = 0; index < localesArrayList.size(); index++) {
                Locale localeOfList = localesArrayList.get(index);
                if (localeOfList.getDisplayLanguage().equals(locale.getDisplayLanguage()))
                    return index;
            }
        }
        return -1;
    }


    /**
     * Sets up the Recyclerview where the translation results are displayed
     */

    private void setupTranslationsRecyclerView() {

        RecyclerView translationsRecyclerView = findViewById(R.id.dictionary_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        translationsRecyclerView.setLayoutManager(layoutManager);
        mTranslationsArrayList = new ArrayList<>();
        mWordArrayList = new ArrayList<>();
        mTranslationsAdapter = new AdapterDictionaryTranslation( mTranslationsArrayList );

        translationsRecyclerView.setAdapter(mTranslationsAdapter);
        mTranslationsAdapter.notifyDataSetChanged();
    }






    private void saveDataToDatabase(Status status, String audioFileLocation) {

        switch( status ){

            case DOWNLOAD_AUDIO_FILE:

                Object[] params = {mAudioFileUrl, mTemporaryDirectory};
                new DownloadFileFromUrlAsyncTask( mProgressBar, fileLocation -> {
                    saveDataToDatabase(Status.SAVE_WORDS,  fileLocation);
                    return null;
                }).execute(params);
                break;

            case SAVE_WORDS:

                byte[] firstAudioByteArray = null;
                File databaseFile = new File( mApplicationDirectoryFile, mKnownLanguageDirString );
                mDatabaseHelper = new DictDatabaseHelper( databaseFile.toString() );

                if( audioFileLocation != null ){
                    File testAudioFile = new File( audioFileLocation );

                    try {
                        firstAudioByteArray = Utils.convertFileToByteArray( testAudioFile.toString() );
                    } catch (IOException exception) {
                        Log.e(Constants.TAG, "Unable to convert audio file to byte array!");
                    }
                }

                if( mWordArrayList == null )
                    return;

                for ( DictionaryObject object : mWordArrayList ){
                    mDatabaseHelper.insertContact(object.getName(), object.getValue(), firstAudioByteArray );
                }
                break;
        }
        return;
    }



    private void getDataFromDatabase(String word){

        byte[] returnArray = null;
        Cursor cursor = mDatabaseHelper.getData("Hallo");

        if (cursor != null) {
            cursor.moveToFirst();

            String result = "";

            while (cursor.moveToNext()) {
                String FirstLanguage = cursor.getString(cursor.getColumnIndex(DictDatabaseHelper.DICT_COLUMN_FIRST_LANGUAGE));
                String secondLanguage = cursor.getString(cursor.getColumnIndex(DictDatabaseHelper.DICT_COLUMN_SECOND_LANGUAGE));

                if ( cursor.getBlob( cursor.getColumnIndex( DictDatabaseHelper.DICT_COLUMN_FIRST_AUDIO )) != null)
                {
                    returnArray = cursor.getBlob( cursor.getColumnIndex( DictDatabaseHelper.DICT_COLUMN_FIRST_AUDIO ) );
                }
                // TODO put results in ArrayList
                result = result + "\n" + FirstLanguage + " - " + secondLanguage;
            }

            // TODO make media player read byte array or convert byte array to mp3

            if (!cursor.isClosed())
                cursor.close();
        } else {
            Log.e(Constants.TAG, "Cursor object is null!");
        }

    }

    private void showSnackbar(String message){
        Snackbar snackbar = Snackbar.make(mMainLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }


}
