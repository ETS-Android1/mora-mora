package com.example.hendrik.mianamalaga.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.hendrik.mianamalaga.dialogs.DialogHelp;
import com.example.hendrik.mianamalaga.utilities.Utils;
import com.example.hendrik.mianamalaga.adapter.AdapterLanguageChoice;
import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.dialogs.DialogAddLanguage;
import com.example.hendrik.mianamalaga.utilities.LayoutManagerCenterZoom;
import com.example.hendrik.mianamalaga.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.hendrik.mianamalaga.utilities.Utils.sortLocales;



public class ActivityLanguageChoice extends ActivityBase {

    private final String FILLER = "/unused";

    private ConstraintLayout mMainLayout;
    private ArrayList<Locale> mKnownLocalesArrayList;
    private RecyclerView mLanguageRecyclerView;
    private TextView mTitle;
    private Toolbar mToolbar;
    private LayoutManagerCenterZoom mLayoutManager;
    private Locale mLanguageToLearn;
    private Locale mKnownLanguageLocale;
    private FloatingActionButton mFabAddLanguage;
    private DrawerLayout mDrawerLayout;
    private Context mContext;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_choice);

        setupActionBar();
        setupFab();

        mContext = this;
        mTitle = findViewById(R.id.language_choice_title);
        mTitle.setText(R.string.SelectTheLanguageToLearn);
        mMainLayout = findViewById(R.id.activity_language_choice_main_layout);

        //LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.item_animation_slide_from_right);
        //mMainLayout.setLayoutAnimation(animationController);

        if ( !hasExternalStorageAccessPermission() ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        prepareBasicData();
        mPresentLocalesArrayList = getLanguagesFromDisk( mApplicationDirectory );
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
                    startActivityKnownLanguageSelection();
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
                    Toast.makeText(mContext , R.string.EnableEditModeToRemoveLanguages, Toast.LENGTH_LONG).show();
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
                .make(mMainLayout, mContext.getString( R.string.LanguageFolderIsDeleted ), Snackbar.LENGTH_LONG)
                .setAction("UNDO", view -> {
                    Snackbar snackBarUndo = Snackbar.make(mMainLayout,  R.string.LanguageFolderIsRestored, Snackbar.LENGTH_SHORT);
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




    private void clearTemporaryFolder() {

        if( hasExternalStorageAccessPermission() ) {

            if (!Utils.prepareFileStructure(mApplicationDirectory.toString())) {
                Toast.makeText(this, mContext.getString( R.string.FailedToCreateDirectories ), Toast.LENGTH_SHORT).show();
            }

            if (mTemporaryDirectory != null && mTemporaryDirectory.exists()) {
                Utils.deleteFolder(mTemporaryDirectory);
            }
        }
    }



    private void getLanguage(int position) {
        mLanguageToLearn = mPresentLocalesArrayList.get(position);
    }



    private void startActivityKnownLanguageSelection() {
        if(  hasExternalStorageAccessPermission() ){
            if( !mLanguageToLearn.getDisplayLanguage().isEmpty() ) {
                Intent intent = new Intent(this, ActivityKnownLanguageSelection.class);
                File languageToLearnDirectory = new File(mApplicationDirectory.toString() + File.separator + mLanguageToLearn.getLanguage());
                intent.putExtra(Constants.LanguageToLearnDirectoryName, languageToLearnDirectory.toString() );
                intent.putExtra(Constants.EditMode, mEditMode );
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            Toast.makeText(this, R.string.TheApplicationNeedsWritePermissions, Toast.LENGTH_SHORT).show();
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
        DialogAddLanguage dialogAddLanguage = DialogAddLanguage.newInstance( mApplicationDirectory.toString() );
        dialogAddLanguage.show(getSupportFragmentManager(), Constants.TAG);
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
                    copyLessonFilesFromAssetsFolder("de");
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
                    .make( mMainLayout, R.string.CantCopyBasicDataIHaveNoPermission, Snackbar.LENGTH_LONG);
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
