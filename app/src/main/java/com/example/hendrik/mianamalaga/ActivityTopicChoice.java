package com.example.hendrik.mianamalaga;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


//TODO - the app crashes when VideoFragment is running and one presses the back button

public class ActivityTopicChoice extends AppCompatActivity  {

    private ArrayList<Topic> mTopicArrayList;
    private AdapterTopic mAdapter;
    private FloatingActionButton mNewTopicFab;
    private FloatingActionButton mSaveFab;
    private boolean mEditMode;
    private Context mContext;
    private  RecyclerView mTopicRecyclerView;
    private static File mApplicationDirectory;
    private static File mTemporaryDirectory;
    private FrameLayout mCameraFrameLayout;
    private CardView mCameraCardView;
    private CoordinatorLayout mMainLayout;
    private RecyclerView.LayoutManager mLayoutManager;
    private DrawerLayout mDrawerLayout;

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 2;
    private static final int PERMISSION_REQUEST_INTERNET = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_choice);
        setupToolbar();
        getIntents();
        setupFabs();
        setupNavigationDrawer();

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

        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(this, R.anim.item_animation_slide_from_right);
        mMainLayout.setLayoutAnimation(animationController);


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
                    Toast.makeText(mContext, "Enable editor mode to remove topics!", Toast.LENGTH_LONG).show();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        touchHelper.attachToRecyclerView(mTopicRecyclerView);

        mAdapter.notifyDataSetChanged();

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
                    //showHelp();
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

        mAdapter.setOnItemClickListener((position, viewHolder) -> {
            Topic topic = mTopicArrayList.get(position);
            String resourceDirectory = File.separatorChar + IOUtils.convertTopicName( topic.getName() ) + File.separatorChar;
            startConversation(resourceDirectory);
        });

        mAdapter.setOnItemLongClickListener((position, viewHolder) -> {
            if (mEditMode){
                Topic topic = mTopicArrayList.get(position);
                topic.setPosition(position);
                DialogAddTopic dialogAddTopic = DialogAddTopic.newInstance(topic);
                dialogAddTopic.show(getSupportFragmentManager(),Constants.TAG);
            } else {
                Toast.makeText(mContext, "Enable editor mode to edit topics!", Toast.LENGTH_LONG).show();
            }
            return true;
        });
        mAdapter.notifyDataSetChanged();
    }

    private void getDataFromDisk() {
        if( hasExternalStorageAccessPermission() ){
            IOUtils.getTopicListFromFiles(mApplicationDirectory, mTopicArrayList, false );

            File[] temporaryLanguageToLearnDirectories = mTemporaryDirectory.listFiles();
            for( File temporaryLanguageToLeanDirectory : temporaryLanguageToLearnDirectories ){
                if( temporaryLanguageToLeanDirectory.isDirectory() ){
                    File[] knownLanguageDirectories = temporaryLanguageToLeanDirectory.listFiles();
                    for( File knownLanguageDirectory : knownLanguageDirectories ){
                        if( knownLanguageDirectory.isDirectory() ){
                            IOUtils.getTopicListFromFiles(knownLanguageDirectory, mTopicArrayList, false );
                        }
                    }
                }
            }


        } else {
            finish();
        }
    }

    private void getIntents() {
        if (getIntent().getExtras() != null){
            mEditMode = getIntent().getExtras().getBoolean(Constants.EditMode);
            String appDirectoryPathString = getIntent().getExtras().getString(Constants.MoraMora);
            String languageDirectoryName = getIntent().getExtras().getString(Constants.LanguageToLearn);
            String motherTongueDirectoryName = getIntent().getExtras().getString(Constants.MotherTongue);
            String fullDirectoryString = appDirectoryPathString + File.separator + languageDirectoryName + File.separator + motherTongueDirectoryName;
            String fullTemporaryDirectory = getIntent().getExtras().getString(Constants.FullTemporaryDirectory);
            mApplicationDirectory = new File(fullDirectoryString);
            mTemporaryDirectory = new File(fullTemporaryDirectory);

            if( !IOUtils.prepareFileStructure(mApplicationDirectory.toString()) ){
                finish();
            }
        }
    }

    private void deleteTopic(RecyclerView.ViewHolder viewHolder) {

        int position = viewHolder.getAdapterPosition();

        Snackbar snackbar = Snackbar
                .make(mMainLayout, "Topic is deleted!", Snackbar.LENGTH_LONG)
                .setAction("UNDO", view -> {
                    Snackbar snackBarUndo = Snackbar.make(mMainLayout, "Topic is restored!", Snackbar.LENGTH_SHORT);
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

        File topicFullPathFile = new File(mApplicationDirectory, IOUtils.convertTopicName( topic.getName() ) );
        File[] topicFiles = topicFullPathFile.listFiles();
        for (File file : topicFiles){
            file.delete();
        }
        topicFullPathFile.delete();
    }


    @Override
 public void onDestroy(){
    // clearTemporaryFolder();
     if( mApplicationDirectory.isDirectory() && mApplicationDirectory.listFiles().length == 0){
         mApplicationDirectory.delete();
     }

     super.onDestroy();
 }


 public void startConversation(String resourceDirectory) {
        Intent intent = new Intent(this, ActivityConversation.class);

        intent.putExtra(Constants.EditMode, mEditMode);
        intent.putExtra("ResourceDir",resourceDirectory);
        intent.putExtra(Constants.MoraMora, mApplicationDirectory.toString());
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
        }

        return super.onOptionsItemSelected(item);
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    checkPermissionAndOpenLoginActivity();
                return;
            }
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


    private void setupToolbar(){
        Toolbar	toolbar	= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.ic_gecko_top_view_shape);
        actionBar.setDisplayHomeAsUpEnabled(true);
    //    actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
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
        }

        //if( !isDirectoryEmpty(new File(mApplicationDirectory, Constants.TemporaryFolder)) )
        if( !isDirectoryEmpty( mTemporaryDirectory ) )
            mSaveFab.show();

        mNewTopicFab.setOnClickListener(view -> showChangeTopicDialog());
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

    private void showChangeTopicDialog() {
        Topic topic = new Topic();
        topic.setPosition(mTopicArrayList.size() + 1);
        DialogAddTopic dialogAddTopic = DialogAddTopic.newInstance(topic);
        dialogAddTopic.show(getSupportFragmentManager(),Constants.TAG);
    }

    private void saveTopicsToFilePressed() {

        Snackbar snackbar = Snackbar
                .make(mMainLayout, "Save all topics and clear temporary folder!", Snackbar.LENGTH_LONG)
                .setAction("UNDO", view -> {
                    Snackbar snackBarUndo = Snackbar.make(mMainLayout, "Action is undone!", Snackbar.LENGTH_SHORT);
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

    private void saveTopicsToFile(){                        //TODO I want to replace whitespaces and trim the string but the function is not called find out why!
        Log.e(Constants.TAG,"Entering saveTopicToFile()");
        File [] topicDirectories = mApplicationDirectory.listFiles();
        HashMap<String,File> fileHashMap = new HashMap<>();

        for (File topicDirectory : topicDirectories) {
            if (topicDirectory.isDirectory()) {
                fileHashMap.put(topicDirectory.getName(),topicDirectory);
            }
        }

        for (int index = 0; index < mTopicArrayList.size(); index++){
            Topic topic = mTopicArrayList.get(index);
            File topicDirectory = fileHashMap.get( IOUtils.convertTopicName( topic.getName() ) );

            if(topicDirectory == null){
                String topicDirectoryName = IOUtils.convertTopicName( topic.getName() );
                File newTopicDirectory = new File( mApplicationDirectory, topicDirectoryName);
                if (!newTopicDirectory.exists())
                    newTopicDirectory.mkdir();
            }
            File resourceFile = new File(topicDirectory, Constants.InfoFileName);                   //TODO remove that line if everything is ready
            IOUtils.writeTopicToFile( resourceFile, topic);                                         //TODO remove that line if everything is ready
            File topicInfoFile = new File(topicDirectory, Constants.InfoFileNameNew);
            IOUtils.writeTopicToNewFile( topicInfoFile, topic);
        }

        copyTemporaryFolder();
    }

    //Could not read /storage/emulated/0/MoraMora/mg/fr/à_la_plage/chatContent.txt

    private void copyTemporaryFolder() {

        File[]  languageToLearnDirectories = mTemporaryDirectory.listFiles();
        Log.e(Constants.TAG,"Entering copyTempFolder! "+ mTemporaryDirectory.toString() );

        for( File languageToLearnDirectory :  languageToLearnDirectories){
            File[] languagesKnownDirectories = languageToLearnDirectory.listFiles();
            for( File languagesKnownDirectory : languagesKnownDirectories) {
                File[] topicDirectories = languagesKnownDirectory.listFiles();
                for( File topicDirectory : topicDirectories ) {
                    if (new File(topicDirectory, Constants.ChatContentFileName).exists()) {
                        Log.e(Constants.TAG, "Copying: " + topicDirectory.toString());
                        IOUtils.copyFileOrDirectory(topicDirectory.toString(), mApplicationDirectory.toString());
                    }
                }
            }
        }

        clearTemporaryFolder();
    }

    private void clearTemporaryFolder() {
        //
        // 89Log.e(Constants.TAG,"Deleting temp folder!");
        IOUtils.deleteFolder(mTemporaryDirectory);
    }



    /*
    When the user finished the DialogAddTopic dialog and presses the OK button the following
    routine is executed.
     */
    public void onDialogAddTopicPosClick(Topic topic, int position, File imageFile){

        if (position <= mTopicArrayList.size()){

            Topic oldTopic = mTopicArrayList.get(position);
            if (!topic.getName().toLowerCase().equals(oldTopic.getName().toLowerCase() ) ){
                File oldFolder = new File( mApplicationDirectory, oldTopic.getName().trim().replaceAll("\\s", "_"));
                File newFolder = new File( mApplicationDirectory, IOUtils.convertTopicName( topic.getName() ) );
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
            File resourceDirectory = new File(mApplicationDirectory, IOUtils.convertTopicName( topic.getName() ) );
            File imagePath = new File(resourceDirectory, Constants.TopicPictureFileName);
            topic.setImageFileString(imagePath.toString());

            File chatContentFile = new File(resourceDirectory, Constants.ChatContentFileName);
            ArrayList<ChatContent> contentList = new ArrayList<>();
            contentList.add(new ChatContent());
            IOUtils.writeChatContentListToFile(chatContentFile, contentList);
            mAdapter.addItem(topic);
        }

        if( imageFile != null ){

            File resourceDirectory = new File( mApplicationDirectory, IOUtils.convertTopicName( topic.getName() ) );    //TODO compress image file if size is too big
            IOUtils.copyFileOrDirectory( imageFile.toString(), resourceDirectory.toString() );

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

        File relativeResourceFile = new File( IOUtils.convertTopicName( topic.getName() ), Constants.InfoFileName);
        File resourceFile = new File(mApplicationDirectory, relativeResourceFile.toString());
        IOUtils.writeTopicToFile( resourceFile, topic);

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

            File topicDirectory = new File(mApplicationDirectory, IOUtils.convertTopicName(topic.getName()));
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

/*
private void runLayoutAnimation(final RecyclerView recyclerView) {
    final Context context = recyclerView.getContext();
    final LayoutAnimationController controller =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);

    recyclerView.setLayoutAnimation(controller);
    recyclerView.getAdapter().notifyDataSetChanged();
    recyclerView.scheduleLayoutAnimation();
}
 */