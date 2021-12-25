package com.example.hendrik.mianamalaga.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.R;
import com.example.hendrik.mianamalaga.adapter.AdapterLanguageChoice;
import com.example.hendrik.mianamalaga.dialogs.DialogAddLanguage;
import com.example.hendrik.mianamalaga.dialogs.DialogHelp;
import com.example.hendrik.mianamalaga.utilities.LayoutManagerCenterZoom;
import com.example.hendrik.mianamalaga.utilities.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;


import static com.example.hendrik.mianamalaga.utilities.Utils.sortLocales;

public class ActivityKnownLanguageSelection extends ActivityBase{

    private final String FILLER = "/unused";

    private ConstraintLayout mMainLayout;
    private RecyclerView mLanguageRecyclerView;
    private TextView mTitle;
    private LayoutManagerCenterZoom mLayoutManager;
    private Locale mKnownLanguageLocale;

    private FloatingActionButton mFabAddLanguage;
    private DrawerLayout mDrawerLayout;
    private Context mContext;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_choice);

        setupFab();

        mContext = this;
        mTitle = findViewById(R.id.language_choice_title);
        mTitle.setText(R.string.SelectTheAvailableTranslations);
        mMainLayout = findViewById(R.id.activity_language_choice_main_layout);

        if( mLanguageToLearnDirectory == null ){ finish();}
        mPresentLocalesArrayList = getLanguagesFromDisk( mLanguageToLearnDirectory );
        addSomeEmptyRows( mPresentLocalesArrayList );

        setupLanguageRecyclerView();

        setupNavigationDrawer();
        setupActionBar();
    }

    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Toolbar toolbar	=	findViewById(R.id.toolbar_language_choice);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setIcon(R.drawable.ic_gecko_top_view_shape);
            actionBar.setDisplayHomeAsUpEnabled(true);
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
                    intent.putExtra(Constants.EditMode, mEditMode );
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

        mLanguageRecyclerView = findViewById(R.id.language_choice_recyclerView);
        mLayoutManager = new LayoutManagerCenterZoom(this);
        mLanguageRecyclerView.setLayoutManager(mLayoutManager);
        //mLanguageRecyclerView.scrollToPosition(4);

        mLanguageAdapter = new AdapterLanguageChoice( mPresentLocalesArrayList, R.layout.list_element_language );
        mLanguageAdapter.setOnItemClickListener((position, viewHolder) -> {

            mKnownLanguageLocale = getLanguage(position);

            if( !mKnownLanguageLocale.getDisplayLanguage().isEmpty() ) {
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
                    Toast.makeText(mContext , mContext.getString( R.string.EnableEditModeToRemoveLanguages ), Toast.LENGTH_LONG).show();
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
                    Snackbar snackBarUndo = Snackbar.make(mMainLayout, R.string.LanguageFolderIsRestored, Snackbar.LENGTH_SHORT);
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



    private Locale getLanguage(int position) {
        return mPresentLocalesArrayList.get(position);
    }



    private void startTopicChoiceActivity() {
        if(  hasExternalStorageAccessPermission() ){
            if( !mKnownLanguageLocale.getDisplayLanguage().isEmpty() ) {
                Intent intent = new Intent(this, ActivityTopicChoice.class);
                intent.putExtra(Constants.LanguageToLearnDirectoryName, mLanguageToLearnDirectory.toString() );
                intent.putExtra(Constants.MotherTongue, mKnownLanguageLocale.getLanguage() );
                intent.putExtra(Constants.EditMode, mEditMode );
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            Toast.makeText(this, R.string.TheApplicationNeedsWritePermissions, Toast.LENGTH_SHORT).show();
        }

    }



    private void setupFab() {
        mFabAddLanguage = findViewById(R.id.language_choice_add_language_fab);
        mFabAddLanguage.setOnClickListener(view -> fabAddLanguage());
        if (mEditMode) {
            mFabAddLanguage.show();
        } else {
            mFabAddLanguage.hide();
        }
    }

    private void fabAddLanguage(){
        DialogAddLanguage dialogAddLanguage = DialogAddLanguage.newInstance( mLanguageToLearnDirectory.toString() );
        dialogAddLanguage.show(getSupportFragmentManager(), Constants.TAG);
    }






}
