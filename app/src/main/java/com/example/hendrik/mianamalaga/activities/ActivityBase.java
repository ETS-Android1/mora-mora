package com.example.hendrik.mianamalaga.activities;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.R;
import com.example.hendrik.mianamalaga.adapter.AdapterLanguageChoice;
import com.example.hendrik.mianamalaga.utilities.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.hendrik.mianamalaga.utilities.Utils.sortLocales;

public class ActivityBase extends AppCompatActivity {

    protected static File mApplicationDirectory = null;
    protected static File mTemporaryDirectory;
    protected static File mLanguageToLearnDirectory;
    protected static File mFullTopicsDirectory;
    protected boolean mEditMode;

    protected ArrayList<Locale> mPresentLocalesArrayList;
    protected AdapterLanguageChoice mLanguageAdapter;

    protected static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    protected static final int PERMISSION_REQUEST_CAMERA = 2;
    protected static final int PERMISSION_REQUEST_INTERNET = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(android.R.style.Theme_Holo_Light);
        super.onCreate(savedInstanceState);

        mApplicationDirectory = this.getExternalFilesDir(Constants.MoraMora);
        mTemporaryDirectory = new File( mApplicationDirectory, Constants.TemporaryFolder);

        getIntents();

        if ( !hasExternalStorageAccessPermission() ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

    }



    private void getIntents() {
        if (getIntent().getExtras() != null){

            if( getIntent().hasExtra(Constants.EditMode)) {
                mEditMode = getIntent().getExtras().getBoolean(Constants.EditMode);
            }

            if( getIntent().hasExtra(Constants.LanguageToLearnDirectoryName)) {
                String languageToLearnDirectoryName = getIntent().getExtras().getString(Constants.LanguageToLearnDirectoryName);
                mLanguageToLearnDirectory = new File( languageToLearnDirectoryName );

                if( getIntent().hasExtra(Constants.MotherTongue)){
                    String motherTongueDirectoryName = getIntent().getExtras().getString(Constants.MotherTongue);
                    mFullTopicsDirectory = new File( mLanguageToLearnDirectory, motherTongueDirectoryName );
                }
            }

            if( !Utils.prepareFileStructure(mApplicationDirectory.toString()) ){
                finish();
            }
        }
    }

    protected ArrayList<Locale> getLanguagesFromDisk(File directory){

        if( directory != null ){
            ArrayList<Locale> languageList = new ArrayList<>();
            File[] languageDirectories = directory.listFiles();

            if( languageDirectories != null) {
                for (File languageDirectory : languageDirectories) {
                    if (!languageDirectory.getName().equals(Constants.TemporaryFolder)) {
                        Locale locale = new Locale(languageDirectory.getName());
                        languageList.add(locale);
                    }
                }

                sortLocales(languageList);
            }
            return languageList;

        } else {
            Log.e(Constants.TAG," Application directory was not yet initialized!");
        }

        return null;
    }

    protected boolean hasExternalStorageAccessPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED;
    }

    protected boolean hasInternetPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final Handler handler = new Handler();
                    handler.postDelayed((Runnable) () -> {
                        recreate();
                    }, 300);
                } else {
                    Toast.makeText(this, this.getString( R.string.TheApplicationNeedsWritePermissions ), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSION_REQUEST_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                   // checkPermissionAndOpenLoginActivity();
                return;
            }
            case  PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionAndOpenLoginActivity();
                }
                return;
            }
        }
    }

    protected void checkPermissionAndOpenLoginActivity() {
        if ( hasExternalStorageAccessPermission() ){
            if( hasInternetPermission() ){
                Intent intent = new Intent(this, ActivityLogIntoCloud.class);
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void onDialogAddLanguagePosClick(Locale locale, String fullDirectory){

        if( !locale.getDisplayLanguage().isEmpty() && fullDirectory != null ){
            if( hasExternalStorageAccessPermission() ){
                String languageDirectory = locale.getLanguage();
                File baseDirectory = new File( fullDirectory );
                File newLanguageDirectory = new File( baseDirectory, languageDirectory );
                if( !newLanguageDirectory.exists() ){
                    if( !newLanguageDirectory.mkdir() )
                        Log.e(Constants.TAG, "Failed to create language directory!");
                    else {

                        mPresentLocalesArrayList.add( locale );
                        removeEmptyRows(mPresentLocalesArrayList);
                        sortLocales( mPresentLocalesArrayList );
                        addSomeEmptyRows(mPresentLocalesArrayList);
                        mLanguageAdapter.notifyDataSetChanged();

                    }
                }

            }
        }
    }

    protected void addSomeEmptyRows(ArrayList<Locale> arrayList) {
        arrayList.add(0,new Locale(""));
        arrayList.add(0, new Locale(""));
        arrayList.add(new Locale(""));
        arrayList.add(new Locale(""));
    }

    private void removeEmptyRows(ArrayList<Locale> arrayList) {
        ArrayList<Locale> localesToRemove = new ArrayList<>();
        for( Locale locale : arrayList){
            if( locale.getLanguage().isEmpty() )
                localesToRemove.add(locale);
        }

        for( Locale locale : localesToRemove){
            arrayList.remove(locale);
        }
    }

}
