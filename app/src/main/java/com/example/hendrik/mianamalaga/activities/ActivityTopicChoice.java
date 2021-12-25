package com.example.hendrik.mianamalaga.activities;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.example.hendrik.mianamalaga.dialogs.DialogHelp;
import com.example.hendrik.mianamalaga.utilities.Utils;
import com.example.hendrik.mianamalaga.adapter.AdapterTopic;
import com.example.hendrik.mianamalaga.container.ChatContent;
import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.dialogs.DialogAddTopic;
import com.example.hendrik.mianamalaga.fragments.FragmentVideoCamera;
import com.example.hendrik.mianamalaga.R;
import com.example.hendrik.mianamalaga.container.Topic;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


//TODO - the app crashes when VideoFragment is running and one presses the back button
// TODO - when creating a topic add getUserId String to topic to be able to block creator in case of abuse

// TODO - make it possible that a user can like or dislike a topic once - when the topic is going to be uploaded ...
//  download latest topic file and compare both to prevent abusive actions (difference in like or dislike is only one, ...)



public class ActivityTopicChoice extends ActivityBase  {

    private ArrayList<Topic> mTopicArrayList;
    private AdapterTopic mAdapter;
    private FloatingActionButton mNewTopicFab;
    private FloatingActionButton mSaveFab;
    private Context mContext;
    private  RecyclerView mTopicRecyclerView;
    private FrameLayout mCameraFrameLayout;
    private CardView mCameraCardView;
    private CoordinatorLayout mMainLayout;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout mDrawerLayout;

 //   private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
 //   private static final int PERMISSION_REQUEST_CAMERA = 2;
 //   private static final int PERMISSION_REQUEST_INTERNET = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_choice);
        setupToolbar();
        setupFabs();
        setupNavigationDrawer();
        setupNavigationDrawerComponents();

        mMainLayout = findViewById(R.id.activity_topic_choice_coordinator_layout);
        mCameraFrameLayout = findViewById(R.id.activity_topic_choice_camera_container);
        mCameraCardView = findViewById(R.id.activity_topic_choice_camera_card_view);

        mContext = this;
        mTopicArrayList = new ArrayList<>();

        copyTemporaryFolder();                                                                      //TODO this function should not be called here as it overwrites old topics
        getDataFromDisk();

        mTopicRecyclerView = findViewById(R.id.topic_choice_recyclerView);
        mTopicRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mTopicRecyclerView.setLayoutManager(mLayoutManager);

        mTopicRecyclerView.setAlpha(0);



        configureAdapter();




        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = viewHolder1.getAdapterPosition();

                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(mTopicArrayList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(mTopicArrayList, i, i - 1);
                    }
                }
                Topic movedTopic = mTopicArrayList.get(fromPosition);
                mTopicArrayList.remove(fromPosition);
                mTopicArrayList.add(toPosition,movedTopic);

                mAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if (mEditMode){
                    deleteTopic(viewHolder);
                } else {
                    Toast.makeText(mContext, mContext.getString( R.string.EnableEditorModeToRemoveTopics ), Toast.LENGTH_LONG).show();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        touchHelper.attachToRecyclerView(mTopicRecyclerView);

        mAdapter.notifyDataSetChanged();

    }

    private void setupNavigationDrawerComponents() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

        if (header != null ) {
            TextView sortByTextView = header.findViewById(R.id.navigation_drawer_label_text_view);
            TextView appNameTextView = header.findViewById(R.id.navigation_drawer_label_app_name);
            SearchView searchView = header.findViewById(R.id.navigation_drawer_search_view);
            Spinner searchOrderSpinner = header.findViewById(R.id.navigation_drawer_sort_order_spinner);

            if(appNameTextView != null )
                appNameTextView.setVisibility(View.GONE);

            if ( sortByTextView != null )
                sortByTextView.setVisibility(View.VISIBLE);

            if( searchView != null ){
                searchView.setVisibility(View.VISIBLE);
                searchView.setQueryHint(getString(R.string.SearchTopics));
                searchView.setSubmitButtonEnabled( true );
                searchView.setQueryRefinementEnabled( true );
            }


            if( searchOrderSpinner != null ) {
                searchOrderSpinner.setVisibility(View.VISIBLE);

                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                        R.array.sort_order_spinner_array, R.layout.list_element_sort_spinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                searchOrderSpinner.setAdapter(adapter);

                searchOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String item = parent.getItemAtPosition(position).toString();
                        switch (item) {

                            case "Author":
                                Utils.sortTopicsByAuthor(mTopicArrayList);
                                break;

                            case "Difficulty":
                                Utils.sortTopicsByDifficulty(mTopicArrayList);
                                break;

                            case "Name":
                                Utils.sortTopicsByName(mTopicArrayList);
                                break;

                            case "Popularity":
                                Utils.sortTopicsByPopularity(mTopicArrayList);
                                break;

                            case "Size":
                                Utils.sortTopicsBySize(mTopicArrayList);
                                break;

                        }


                        mTopicRecyclerView.animate().alpha(0).setDuration(10).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) { }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mAdapter.notifyDataSetChanged();
                                mTopicRecyclerView.animate().alpha(1f).setDuration(500).setListener( null );
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) { }

                            @Override
                            public void onAnimationRepeat(Animator animation) { }
                        });

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }

    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.activity_topic_choice_drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.toolbar_login:
                    checkPermissionAndOpenLoginActivity();
                    return true;
                case R.id.toolbar_help:
                    DialogHelp helpDialog = DialogHelp.newInstance(getString( R.string.ActivityTopicChoiceHelpText) );
                    helpDialog.show( getSupportFragmentManager(), Constants.TAG );
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.toolbar_editor_mode:
                    if(menuItem.isChecked()){
                        menuItem.setChecked(false);
                        mEditMode = false;
                        mNewTopicFab.hide();
                        mSaveFab.hide();
                    } else {
                        if( hasExternalStorageAccessPermission() ){
                            menuItem.setChecked(true);
                            mEditMode = true;
                            mNewTopicFab.show();
                            mSaveFab.show();
                        }
                    }
                    mDrawerLayout.closeDrawers();
                    return true;
                default:
                    return false;
            }
        });
    }

    private void configureAdapter() {

        mAdapter = new AdapterTopic(this, mTopicArrayList);
        mTopicRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((position, viewHolder) -> {             // TODO
            Topic topic = mTopicArrayList.get(position);
            String resourceDirectory = File.separatorChar + Utils.convertTopicName( topic.getName() ) + File.separatorChar;
            startConversation(resourceDirectory);
        });

        mAdapter.setOnItemLongClickListener((position, viewHolder) -> {
            if (mEditMode){
                Topic topic = mTopicArrayList.get(position);
                topic.setPosition(position);
                DialogAddTopic dialogAddTopic = DialogAddTopic.newInstance(topic);
                dialogAddTopic.show(getSupportFragmentManager(), Constants.TAG);
            } else {
                Toast.makeText(mContext, mContext.getString( R.string.EnableEditorModeToEditTopics ), Toast.LENGTH_LONG).show();
            }
            return true;
        });
        mAdapter.notifyDataSetChanged();
    }

    private void getDataFromDisk() {
        if( hasExternalStorageAccessPermission() ){
            Utils.getTopicListFromFiles(mFullTopicsDirectory, mTopicArrayList, false );

            File[] temporaryLanguageToLearnDirectories = mTemporaryDirectory.listFiles();
            for( File temporaryLanguageToLeanDirectory : temporaryLanguageToLearnDirectories ){
                if( temporaryLanguageToLeanDirectory.isDirectory() ){
                    File[] knownLanguageDirectories = temporaryLanguageToLeanDirectory.listFiles();
                    for( File knownLanguageDirectory : knownLanguageDirectories ){
                        if( knownLanguageDirectory.isDirectory() ){
                            Utils.getTopicListFromFiles(knownLanguageDirectory, mTopicArrayList, false );
                        }
                    }
                }
            }


        } else {
            finish();
        }
    }

    private void deleteTopic(RecyclerView.ViewHolder viewHolder) {

        int position = viewHolder.getAdapterPosition();

        Snackbar snackbar = Snackbar
                .make(mMainLayout, mContext.getString( R.string.TopicIsDeleted ), Snackbar.LENGTH_LONG)
                .setAction("UNDO", view -> {
                    Snackbar snackBarUndo = Snackbar.make(mMainLayout, R.string.TopicIsRestored, Snackbar.LENGTH_SHORT);
                    snackBarUndo.show();
                    mAdapter.notifyDataSetChanged();
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

        Topic topic = mTopicArrayList.get(position);
        mAdapter.remove(position);
        mAdapter.notifyItemRemoved(position);
        mAdapter.notifyDataSetChanged();

        File topicFullPathFile = new File(mFullTopicsDirectory, Utils.convertTopicName( topic.getName() ) );
        File[] topicFiles = topicFullPathFile.listFiles();
        for (File file : topicFiles){
            file.delete();
        }
        topicFullPathFile.delete();
    }


    @Override
 public void onDestroy(){
    // clearTemporaryFolder();
     if( mFullTopicsDirectory.isDirectory() && mFullTopicsDirectory.listFiles().length == 0){
         mFullTopicsDirectory.delete();
     }

     super.onDestroy();
 }


 public void startConversation(String resourceDirectory) {
        Intent intent = new Intent(this, ActivityConversation.class);

        intent.putExtra(Constants.EditMode, mEditMode);
        intent.putExtra(Constants.RelativeResourceDirectory,resourceDirectory);
        intent.putExtra(Constants.MoraMora, mFullTopicsDirectory.toString());
        intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString());
        startActivity(intent);
    }


    /*
    The following two functions define the behaviour of the toolbar menu such as jumps
    to other activities for the log into cloud, help and others
    */

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


/*
    private boolean hasExternalStorageAccessPermission(){
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasInternetPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
            case PERMISSION_REQUEST_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    checkPermissionAndOpenLoginActivity();
                return;
            }
            case  PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar snackbar = Snackbar
                            .make(mMainLayout, "You now have camera access. Please try again to create a topic!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                return;
            }
        }
    }
*/

    private void setupToolbar(){
        Toolbar	toolbar	= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_gecko_top_view_shape);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


     /*
    The floating action button is normally invisible but is used in editor mode to create a new
    question-answer combination for the conversation.
     */

    private void setupFabs() {
        mNewTopicFab = findViewById(R.id.topic_choice_new_fab);
        mSaveFab = findViewById(R.id.topic_choice_save_fab);

        if(!mEditMode){
            mNewTopicFab.hide();
            mSaveFab.hide();
        } else {
            mNewTopicFab.show();
            mSaveFab.show();
        }

        if( !isDirectoryEmpty( mTemporaryDirectory ) )
            mSaveFab.show();

        mNewTopicFab.setOnClickListener(view -> showAddTopicDialog());
        mSaveFab.setOnClickListener(view -> saveTopicsToFilePressed());
    }

    private boolean isDirectoryEmpty(File directory){
        File[] fileArray = directory.listFiles();
        if (fileArray == null){
            return true;
        }

        else if(fileArray.length == 0)
            return true;

        return false;
    }

    private void showAddTopicDialog() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        String userName = sharedPreferences.getString(getResources().getString(R.string.lessonUserName), "anonymous");

        Topic topic = new Topic();
        topic.setPosition(mTopicArrayList.size() + 1);
        topic.setAuthor( userName );
        DialogAddTopic dialogAddTopic = DialogAddTopic.newInstance(topic);
        dialogAddTopic.show(getSupportFragmentManager(),Constants.TAG);
    }

    private void saveTopicsToFilePressed() {

        Snackbar snackbar = Snackbar
                .make(mMainLayout, mContext.getString( R.string.SaveAllTopicsAndClearTemporaryolder ), Snackbar.LENGTH_LONG)
                .setAction("UNDO", view -> {
                    Snackbar snackBarUndo = Snackbar.make(mMainLayout, R.string.ActionIsUndone, Snackbar.LENGTH_SHORT);
                    snackBarUndo.show();
                });

        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (event == DISMISS_EVENT_TIMEOUT){
                    saveTopicsToFile();
                }
            }
        });

        snackbar.show();

    }

    private void saveTopicsToFile(){

        File [] topicDirectories = mFullTopicsDirectory.listFiles();
        HashMap<String,File> fileHashMap = new HashMap<>();

        for (File topicDirectory : topicDirectories) {
            if (topicDirectory.isDirectory()) {
                fileHashMap.put( Utils.convertTopicName( topicDirectory.getName() ),topicDirectory );
            }
        }

        for (int index = 0; index < mTopicArrayList.size(); index++){
            Topic topic = mTopicArrayList.get(index);
            File topicDirectory = fileHashMap.get( Utils.convertTopicName( topic.getName() ) );

            if( topicDirectory == null ){
                String topicDirectoryName = Utils.convertTopicName( topic.getName() );
                File newTopicDirectory = new File( mFullTopicsDirectory, topicDirectoryName);
                if (!newTopicDirectory.exists())
                    newTopicDirectory.mkdir();
            }

            File topicInfoFile = new File(topicDirectory, Constants.InfoFileNameNew);
            Utils.writeTopicToNewFile( topicInfoFile, topic);
        }

        copyTemporaryFolder();
    }


    private void copyTemporaryFolder() {

        File[]  languageToLearnDirectories = mTemporaryDirectory.listFiles();

        if( languageToLearnDirectories != null  ) {
            for (File languageToLearnDirectory : languageToLearnDirectories) {
                File[] languagesKnownDirectories = languageToLearnDirectory.listFiles();
                if (languagesKnownDirectories != null) {
                    for (File languagesKnownDirectory : languagesKnownDirectories) {
                        File[] topicDirectories = languagesKnownDirectory.listFiles();
                        if (topicDirectories != null) {
                            for (File topicDirectory : topicDirectories) {
                                if (new File(topicDirectory, Constants.ChatContentFileName).exists()) {
                                    Log.d(Constants.TAG, "Copying: " + topicDirectory.toString());
                                    Utils.copyFileOrDirectory(topicDirectory.toString(), mFullTopicsDirectory.toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        clearTemporaryFolder();
    }

    private void clearTemporaryFolder() {
        Utils.deleteFolder(mTemporaryDirectory);
    }



    /*
    When the user finished the DialogAddTopic dialog and presses the OK button the following
    routine is executed.
     */
    public void onDialogAddTopicPosClick(Topic topic, int position, File imageFile){

        if (position <= mTopicArrayList.size()){

            Topic oldTopic = mTopicArrayList.get(position);
            if (!topic.getName().toLowerCase().equals(oldTopic.getName().toLowerCase() ) ){
                File oldFolder = new File( mFullTopicsDirectory, oldTopic.getName().trim().replaceAll("\\s", "_"));
                File newFolder = new File( mFullTopicsDirectory, Utils.convertTopicName( topic.getName() ) );
                try {
                    oldFolder.renameTo(newFolder);
                } catch (NullPointerException e){
                    showText(getString(R.string.could_not_rename));
                    Log.e(Constants.TAG, getString(R.string.could_not_rename));
                }
            }
            topic.setImageFileString( oldTopic.getImageFileString() );
            mAdapter.set(position,topic);
            mAdapter.notifyDataSetChanged();

        } else {
            File resourceDirectory = new File(mFullTopicsDirectory, Utils.convertTopicName( topic.getName() ) );

            if( resourceDirectory.exists() ){                                                       // TODO That modification has not yet been tested
                Snackbar snackbar = Snackbar
                        .make(mMainLayout, R.string.ATopicWithThatNameAlreadyxists, Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }

            File imagePath = new File(resourceDirectory, Constants.TopicPictureFileName);
            topic.setImageFileString(imagePath.toString());

            File chatContentFile = new File(resourceDirectory, Constants.ChatContentFileName);
            ArrayList<ChatContent> contentList = new ArrayList<>();
            contentList.add(new ChatContent());
            Utils.writeChatContentListToFile(chatContentFile, contentList);
            mAdapter.addItem(topic);
        }

        if( imageFile != null ){

            File resourceDirectory = new File( mFullTopicsDirectory, Utils.convertTopicName( topic.getName() ) );    //TODO compress image file if size is too big
            Utils.copyFileOrDirectory( imageFile.toString(), resourceDirectory.toString() );

            File newTopicPictureFile = new File(resourceDirectory, imageFile.getName() );
            File finalTopicPictureFile = new File(resourceDirectory, Constants.TopicPictureFileName);

            if( finalTopicPictureFile.exists() ){
                finalTopicPictureFile.delete();
            }
            try {
                newTopicPictureFile.renameTo( finalTopicPictureFile );
            } catch (NullPointerException e){
                Log.e(Constants.TAG, getString(R.string.could_not_rename));
            }
        }

        File relativeResourceFile = new File( Utils.convertTopicName( topic.getName() ), Constants.InfoFileNameNew);
        File resourceFile = new File(mFullTopicsDirectory, relativeResourceFile.toString());

        Utils.writeTopicToNewFile( resourceFile, topic);

        mAdapter.notifyItemChanged(position);
    }


    public void onDialogAddTopicCameraClick(Topic topic, int position){

        if ( !hasCameraAccess() ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        } else if ( !hasExternalStorageAccessPermission() ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {

            onDialogAddTopicPosClick(topic, position, null);
            mCameraCardView.setVisibility(View.VISIBLE);
            mCameraFrameLayout.setVisibility(View.VISIBLE);

            File topicDirectory = new File(mFullTopicsDirectory, Utils.convertTopicName(topic.getName()));
            File pictureFile = new File(topicDirectory, Constants.TopicPictureFileName);
            getFragmentManager().beginTransaction()
                    .replace(R.id.activity_topic_choice_camera_container, FragmentVideoCamera.newInstance(pictureFile.toString()))
                    .commit();
        }
    }

    public void onPictureTaken(){
        mCameraFrameLayout.setVisibility(View.GONE);
        mCameraCardView.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
    }

    private boolean hasCameraAccess() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }


    public void showText(String text){
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }


}

