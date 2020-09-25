package com.example.hendrik.mianamalaga.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hendrik.mianamalaga.BuildConfig;
import com.example.hendrik.mianamalaga.dialogs.DialogHelp;
import com.example.hendrik.mianamalaga.utilities.Utils;
import com.example.hendrik.mianamalaga.adapter.AdapterLanguageChoice;
import com.example.hendrik.mianamalaga.adapter.AdapterLanguageSpinner;
import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.dialogs.DialogAddLanguage;
import com.example.hendrik.mianamalaga.utilities.LayoutManagerCenterZoom;
import com.example.hendrik.mianamalaga.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.example.hendrik.mianamalaga.utilities.Utils.sortLocales;



public class ActivityLanguageChoice extends AppCompatActivity {

    private final String FILLER = "/unused";

    private ConstraintLayout mMainLayout;
    private Spinner mKnownLanguageSpinner;
    private ArrayList<Locale> mPresentLocalesArrayList;
    private ArrayList<Locale> mKnownLocalesArrayList;
    private RecyclerView mLanguageRecyclerView;
    private TextView mTitle;
    private  Toolbar mToolbar;
    private AdapterLanguageChoice mLanguageAdapter;
    private LayoutManagerCenterZoom mLayoutManager;
    private Locale mLanguageToLearn;
    //private String mKnownLanguage;
    private Locale mKnownLanguageLocale;
    private static File mApplicationDirectory = null;
    private static File mTemporaryDirectory;
    private boolean  mHasWriteExternalStoragePermission;
    private boolean mEditMode;
    private FloatingActionButton mFabAddLanguage;
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_INTERNET = 3;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_choice);

        //mApplicationDirectory = Environment.getExternalStoragePublicDirectory(Constants.MoraMora);
        mApplicationDirectory = this.getExternalFilesDir(Constants.MoraMora);
        mTemporaryDirectory = new File( mApplicationDirectory, Constants.TemporaryFolder);

        setupActionBar();
        setupFab();

        mContext = this;
        mTitle = findViewById(R.id.language_choice_title);
        mMainLayout = findViewById(R.id.activity_language_choice_main_layout);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.item_animation_slide_from_right);
        mMainLayout.setLayoutAnimation(animationController);

        if ( !hasExternalStorageAccessPermission() ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            mHasWriteExternalStoragePermission = true;
        }

        setupKnownLanguageSpinner();
        prepareBasicData();
        mPresentLocalesArrayList = getLanguagesFromDisk();
        addSomeEmptyRows( mPresentLocalesArrayList );

        setupLanguageRecyclerView();

        setupNavigationDrawer();

        clearTemporaryFolder();

        copyOldDataIntoNewFolderCompatFunction();                                                   // TODO Remove after update

    }

    private void copyOldDataIntoNewFolderCompatFunction(){
        if( Environment.getExternalStoragePublicDirectory(Constants.MoraMora).exists() ){
            File source = Environment.getExternalStoragePublicDirectory( Constants.MoraMora );
            File dest = mApplicationDirectory.getParentFile();
            Utils.copyFileOrDirectory( source.toString() , dest.toString() );

            Utils.deleteFolder(source);
            source.delete();
            recreate();
        }
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.activity_language_choice_drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {

                case R.id.toolbar_help:
                    DialogHelp helpDialog = DialogHelp.newInstance(getString( R.string.ActivityLanguageChoiceHelpText) );
                    helpDialog.show( getSupportFragmentManager(), Constants.TAG );
                    mDrawerLayout.closeDrawers();
                    return true;
/*
                case R.id.toolbar_login:
                    //checkPermissionAndOpenLoginActivity();
                    mDrawerLayout.closeDrawers();
                    Toast.makeText(mContext , "That functionality is not yet \n" +
                                                    "implemented. Choose a language \n" +
                                                    "and access cloud from the next \n" +
                                                    " screen (activity) !", Toast.LENGTH_LONG).show();
                    return true;
*/
                case R.id.toolbar_editor_mode:
                    if( menuItem.isChecked() ){
                        menuItem.setChecked(false);
                        mEditMode = false;
                        mFabAddLanguage.hide();

                    } else {
                        if( hasExternalStorageAccessPermission() ){
                            menuItem.setChecked(true);
                            mEditMode = true;
                            mFabAddLanguage.show();
                        }
                    }
                    mDrawerLayout.closeDrawers();
                    return true;

                case R.id.toolbar_dictionary:
                    Intent intent = new Intent(this, ActivityDictionary.class);
                    intent.putExtra("EditMode", mEditMode);
                    intent.putExtra( Constants.FullTemporaryDirectory, mTemporaryDirectory.toString() );
                    intent.putExtra( Constants.RelativeResourceDirectory, mKnownLanguageLocale.getLanguage() + FILLER );
                    intent.putExtra( Constants.MoraMora, mApplicationDirectory.toString() );
                    startActivity(intent);
                    return true;

                default:
                    return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_empty, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.toolbar_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.toolbar_settings:
                startActivity( new Intent(this, ActivitySettings.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setupLanguageRecyclerView(){
        mLanguageRecyclerView = findViewById(R.id.language_choice_recyclerView);                    // TODO that variable can be local
        mLayoutManager = new LayoutManagerCenterZoom(this);                                // TODO that variable can be local
        mLanguageRecyclerView.setLayoutManager(mLayoutManager);
        mLanguageRecyclerView.scrollToPosition(4);
        mLanguageAdapter = new AdapterLanguageChoice( mPresentLocalesArrayList, R.layout.list_element_language );
        mLanguageAdapter.setOnItemClickListener((position, viewHolder) -> {

            getLanguage(position);

            if( !mLanguageToLearn.getDisplayLanguage().isEmpty() ) {
                mLanguageRecyclerView.animate()
                        .translationX( -mLanguageRecyclerView.getWidth() )
                        .setDuration(400);

                new Handler().postDelayed(() -> {
                    startTopicChoiceActivity();
                }, 300);
            }

        });
        mLanguageRecyclerView.setAdapter(mLanguageAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if ( mEditMode ){
                    deleteLanguage(viewHolder);
                } else {
                    Toast.makeText(mContext , "Enable edit mode to remove languages!", Toast.LENGTH_LONG).show();
                    mLanguageAdapter.notifyDataSetChanged();
                }
            }
        });


        touchHelper.attachToRecyclerView( mLanguageRecyclerView );

        mLanguageAdapter.notifyDataSetChanged();
    }

    private void deleteLanguage(RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();

        Snackbar snackbar = Snackbar
                .make(mMainLayout, "Language folder is deleted!", Snackbar.LENGTH_LONG)
                .setAction("UNDO", view -> {
                    Snackbar snackBarUndo = Snackbar.make(mMainLayout, "Language folder is restored!", Snackbar.LENGTH_SHORT);
                    snackBarUndo.show();
                    mLanguageAdapter.notifyDataSetChanged();
                });

        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event == DISMISS_EVENT_TIMEOUT){
                    deleteDirectory(position);
                }
            }
        });

        snackbar.show();
    }

    private void deleteDirectory(int position){

        Locale locale = mPresentLocalesArrayList.get(position);
        mPresentLocalesArrayList.remove( locale );
        mLanguageAdapter.notifyItemRemoved( position );
        mLanguageAdapter.notifyDataSetChanged();

        File languageFullPathFile = new File( mApplicationDirectory, locale.getLanguage() );
        Utils.deleteFolder( languageFullPathFile );
        languageFullPathFile.delete();
    }

    private void setupKnownLanguageSpinner(){
        mKnownLanguageSpinner = findViewById(R.id.language_choice_spinner);
        mKnownLocalesArrayList = new ArrayList<>();                                                 // TODO this can be ArrayList<Locale> mKnownLocalesArrayList = new ArrayList<>();


        String[] languages = Locale.getISOLanguages();
        Map<String, Locale> localeMap = new HashMap<>(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            localeMap.put(locale.getLanguage(), locale);
        }

        for (Locale locale : localeMap.values()){
            mKnownLocalesArrayList.add( locale );
        }

        Utils.sortLocales(mKnownLocalesArrayList);
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);
        //String knownLanguageLocaleLanguage = sharedPrefs.getString(Constants.KnownLanguage, Locale.getDefault().getLanguage() );
        String knownLanguageLocaleLanguage = sharedPrefs.getString(Constants.KnownLanguage, "de" );     // TODO should be fr for base language maybe (files in assets folder)

        mKnownLanguageLocale = new Locale( knownLanguageLocaleLanguage );
        //mKnownLanguage = mKnownLanguageLocale.getDisplayLanguage();
        int defaultPosition = findLocalePosition( mKnownLanguageLocale, mKnownLocalesArrayList);

        AdapterLanguageSpinner spinnerAdapter = new AdapterLanguageSpinner(this, android.R.layout.simple_spinner_item, R.id.list_element_language_spinner_text_view, mKnownLocalesArrayList);
        mKnownLanguageSpinner.setAdapter(spinnerAdapter);
        mKnownLanguageSpinner.setSelection( defaultPosition );
        mKnownLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mKnownLanguageLocale = mKnownLocalesArrayList.get(position);
                SharedPreferences sharedPrefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString( Constants.KnownLanguage, mKnownLanguageLocale.getISO3Language() );
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private int findLocalePosition(Locale locale, ArrayList<Locale> localesArrayList) {

        if( mKnownLocalesArrayList != null){                                                        // TODO this can be localesArrayList
            for( int index = 0; index < localesArrayList.size(); index++ ){
                Locale localeOfList = localesArrayList.get(index);
                if( localeOfList.getDisplayLanguage().equals( locale.getDisplayLanguage() ))
                    return index;
            }
        }
        return -1;
    }

    /*
        private void getKnownLanguage() {
            if( mLanguageToLearn != null) {
                if (mLanguageToLearn.equals("Deutsch"))
                    mKnownLanguage = "Français";
                else
                    mKnownLanguage = "Deutsch";
            }
        }
    */
    private void addSomeEmptyRows(ArrayList<Locale> arrayList) {
        arrayList.add(0,new Locale(""));
        arrayList.add(0, new Locale(""));
        arrayList.add(new Locale(""));
        arrayList.add(new Locale(""));
    }


    private void clearTemporaryFolder() {

        if( hasExternalStorageAccessPermission() ) {

            if (!Utils.prepareFileStructure(mApplicationDirectory.toString())) {
                Toast.makeText(this, "Failed to create directories!", Toast.LENGTH_SHORT).show();
            }

            if (mTemporaryDirectory != null && mTemporaryDirectory.exists()) {
                Utils.deleteFolder(mTemporaryDirectory);
            }
        }
    }

    private void checkPermissionAndOpenLoginActivity() {
        if ( hasExternalStorageAccessPermission() ){
            if( hasInternetPermission() ){
                Intent intent = new Intent(this, ActivityLogIntoCloud.class);
                intent.putExtra(Constants.MoraMora, mApplicationDirectory.toString());
                intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString());
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_REQUEST_INTERNET);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private boolean hasExternalStorageAccessPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasInternetPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mHasWriteExternalStoragePermission = true;
                    recreate();
                } else {
                    Toast.makeText(this, "The application needs write permissions!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case PERMISSION_REQUEST_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    checkPermissionAndOpenLoginActivity();
                return;
            }
        }
    }

    private void getLanguage(int position) {
        mLanguageToLearn = mPresentLocalesArrayList.get(position);
    }



    private void startTopicChoiceActivity() {
        if(  mHasWriteExternalStoragePermission ){
            if( !mLanguageToLearn.getDisplayLanguage().isEmpty() ) {
                Intent intent = new Intent(this, ActivityTopicChoice.class);
                intent.putExtra(Constants.LanguageToLearn, mLanguageToLearn.getLanguage());
                intent.putExtra(Constants.MotherTongue, mKnownLanguageLocale.getLanguage() );
                intent.putExtra(Constants.MoraMora, mApplicationDirectory.toString());
                intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString());
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            Toast.makeText(this, "The application needs write permissions!", Toast.LENGTH_SHORT).show();
        }

    }


    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mToolbar	=	findViewById(R.id.toolbar_language_choice);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setIcon(R.drawable.ic_gecko_top_view_shape);
        }
    }

    private void setupFab() {
        mFabAddLanguage = findViewById(R.id.language_choice_add_language_fab);
        mFabAddLanguage.setOnClickListener(view -> fabAddLanguage());
        mFabAddLanguage.hide();
    }

    private void fabAddLanguage(){
        DialogAddLanguage dialogAddLanguage = DialogAddLanguage.newInstance();
        dialogAddLanguage.show(getSupportFragmentManager(), Constants.TAG);
    }

    public void onDialogAddLanguagePosClick(Locale locale){

        if( !locale.getDisplayLanguage().isEmpty() ){
            if( mHasWriteExternalStoragePermission ){
                String languageDirectory = locale.getLanguage();
                File newLanguageDirectory = new File(mApplicationDirectory, languageDirectory );
                if( !newLanguageDirectory.exists() ){
                    if( !newLanguageDirectory.mkdir() )
                        Log.e(Constants.TAG, "Failed to create language directory!");
                    else {
                        File motherTongueDirectory = new File( newLanguageDirectory, mKnownLanguageLocale.getLanguage() );
                        if( !motherTongueDirectory.exists() )
                            motherTongueDirectory.mkdir();

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

    private ArrayList<Locale> getLanguagesFromDisk(){
        if( mApplicationDirectory != null ){
            ArrayList<Locale> languageList = new ArrayList<>();
            File[] languageDirectories = mApplicationDirectory.listFiles();

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

    private void prepareBasicData(){
        if( hasExternalStorageAccessPermission() ) {

            if (mApplicationDirectory != null) {
                if( !mApplicationDirectory.exists() ){
                    mApplicationDirectory.mkdir();
                }

                if ( mApplicationDirectory.list().length == 0 ){
                    Log.d(Constants.TAG, "First run of the application. Directories will be freshly created! ");
                    copyLessonFilesFromAssetsFolder("fr");
                }

                /*
                String[] iso639Minus1LanguageCodes = {"mg","fr","de","en","it","pt","ru","zh"};
                for (String iso639Language : iso639Minus1LanguageCodes) {
                    File languageToLearnDirectory = new File(mApplicationDirectory, iso639Language);
                    if( !languageToLearnDirectory.exists() )
                        languageToLearnDirectory.mkdir();

                    File motherTongueDirectory = new File( languageToLearnDirectory, mKnownLanguageLocale.getLanguage() );
                    if( !motherTongueDirectory.exists() )
                        motherTongueDirectory.mkdir();
                }
*/
                if( mTemporaryDirectory != null ){
                    if( !mTemporaryDirectory.exists() )
                        mTemporaryDirectory.mkdir();
                } else {
                    Log.e(Constants.TAG," temporary directory file was not initialized!");
                }
            } else {
                Log.e(Constants.TAG, "ApplicationDirectory was not yet initialized!");
            }


        } else {
            Snackbar snackbar = Snackbar
                    .make( mMainLayout, "Can't copy basic data! I have no permission!", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }



    private void copyLessonFilesFromAssetsFolder(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFileFromAssetsFolder(path);
            } else {
                File dir = new File( mApplicationDirectory, path );

                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyLessonFilesFromAssetsFolder(path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e(Constants.TAG, "I/O Exception", ex);
        }
    }

    private void copyFileFromAssetsFolder(String filename) {
        AssetManager assetManager = this.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = mApplicationDirectory.toString() + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

}
