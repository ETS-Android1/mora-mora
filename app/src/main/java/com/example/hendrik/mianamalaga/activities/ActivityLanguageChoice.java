package com.example.hendrik.mianamalaga;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hendrik.mianamalaga.activities.ActivitySettings;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.example.hendrik.mianamalaga.IOUtils.sortLocales;



public class ActivityLanguageChoice extends AppCompatActivity {

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
    private String mKnownLanguage;
    private Locale mKnownLanguageLocale;
    private static File mApplicationDirectory;
    private static File mTemporaryDirectory;
    private boolean  mHasWriteExternalStoragePermission;
    private boolean mEditMode;
    private FloatingActionButton mFabAddLanguage;
    private DrawerLayout mDrawerLayout;
    private Context mContext;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_choice);
        setupActionBar();
        setupFab();

        mContext = this;
        mTitle = findViewById(R.id.language_choice_title);
        mMainLayout = findViewById(R.id.activity_language_choice_main_layout);
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.item_animation_slide_from_right);
        mMainLayout.setLayoutAnimation(animationController);

        setupKnownLanguageSpinner();
        mApplicationDirectory = Environment.getExternalStoragePublicDirectory(Constants.MoraMora);
        mTemporaryDirectory = new File( mApplicationDirectory, Constants.TemporaryFolder);
        prepareBasicData();
        mPresentLocalesArrayList = getLanguagesFromDisk();
        addSomeEmptyRows( mPresentLocalesArrayList );

        setupLanguageRecyclerView();


        if ( !hasExternalStorageAccess() ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            mHasWriteExternalStoragePermission = true;
        }

        setupNavigationDrawer();

        clearTemporaryFolder();

    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.activity_language_choice_drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {

                case R.id.toolbar_help:
                    //showHelp();
                    return true;
                case R.id.toolbar_editor_mode:
                    if( menuItem.isChecked() ){
                        menuItem.setChecked(false);
                        mEditMode = false;
                        mFabAddLanguage.hide();

                    } else {
                        if( hasExternalStorageAccess() ){
                            menuItem.setChecked(true);
                            mEditMode = true;
                            mFabAddLanguage.show();
                        }
                    }
                    mDrawerLayout.closeDrawers();
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
        mLanguageRecyclerView = findViewById(R.id.language_choice_recyclerView);
        mLayoutManager = new LayoutManagerCenterZoom(this);
        mLanguageRecyclerView.setLayoutManager(mLayoutManager);
        mLanguageRecyclerView.scrollToPosition(4);
        mLanguageAdapter = new AdapterLanguageChoice( mPresentLocalesArrayList, R.layout.list_element_language );
        mLanguageAdapter.setOnItemClickListener((position, viewHolder) -> {

            mLanguageRecyclerView.animate()
                    .translationX( -mLanguageRecyclerView.getWidth() )
                    .setDuration(400);

            getLanguage(position);

            new Handler().postDelayed(() -> {
                startTopicChoiceActivity();
            }, 300);


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
        IOUtils.deleteFolder( languageFullPathFile );
        languageFullPathFile.delete();
    }

    private void setupKnownLanguageSpinner(){
        mKnownLanguageSpinner = findViewById(R.id.language_choice_spinner);
        mKnownLocalesArrayList = new ArrayList<>();


        String[] languages = Locale.getISOLanguages();
        Map<String, Locale> localeMap = new HashMap<>(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            localeMap.put(locale.getLanguage(), locale);
        }

        for (Locale locale : localeMap.values()){
            mKnownLocalesArrayList.add( locale );
        }

        IOUtils.sortLocales(mKnownLocalesArrayList);
        SharedPreferences sharedPrefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);
        String knownLanguageLocaleLanguage = sharedPrefs.getString(Constants.KnownLanguage, Locale.getDefault().getLanguage() );

        mKnownLanguageLocale = new Locale( knownLanguageLocaleLanguage );
        mKnownLanguage = mKnownLanguageLocale.getDisplayLanguage();
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

        if( mKnownLocalesArrayList != null){
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

        if( !IOUtils.prepareFileStructure(mApplicationDirectory.toString()) ){
            Toast.makeText(this, "Failed to create directories!", Toast.LENGTH_SHORT).show();
        }

        if( mTemporaryDirectory != null && mTemporaryDirectory.exists()) {
            IOUtils.deleteFolder(mTemporaryDirectory);
        }
    }

    private boolean hasExternalStorageAccess(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED;
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
        if( hasExternalStorageAccess() ) {
            if (mApplicationDirectory != null) {
                String[] iso639Minus1LanguageCodes = {"mg","fr","de","en","it","pt","ru","zh"};
                for (String iso639Language : iso639Minus1LanguageCodes) {
                    File languageToLearnDirectory = new File(mApplicationDirectory, iso639Language);
                    if( !languageToLearnDirectory.exists() )
                        languageToLearnDirectory.mkdir();

                    File motherTongueDirectory = new File( languageToLearnDirectory, mKnownLanguageLocale.getLanguage() );
                    if( !motherTongueDirectory.exists() )
                        motherTongueDirectory.mkdir();
                }

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

}
