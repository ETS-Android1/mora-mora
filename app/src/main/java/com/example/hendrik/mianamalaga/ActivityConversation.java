package com.example.hendrik.mianamalaga;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


public class ActivityConversation extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 2;

    private AdapterPageFragment mPageFragmentAdapter;
    private static File mApplicationDirectory;
    private static File mTemporaryDirectory;
    private FrameLayout mVideoFrameLayout;
    private EditText mResponseText;
    private RecyclerView mConversationListView;
    private LinearLayoutManager mLayoutManager;
    private AdapterChatArray mAdapter;
    private boolean mEditMode;
    private String mResourceDir;
    private List<String> mResponseCacheList = new ArrayList<>();                         // Cache to save String if fragment is not yet created
    private boolean mContentChanged = false;
    private MediaRecorder mMediaRecorder;
    private DrawerLayout mDrawerLayout;
    private Activity mActivity;
    private VideoView mVideoView;
    private CardView mVideoCardView;
    private CardView mResponsePagerCardView;
    private ImageView mImageView;
    private Animator mCurrentAnimator;

    private ArrayList<ChatContent> mChatContentArrayList;


    private HashMap<Integer, OnUpdateResponseInFragment> mInterfaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_conversation);
        setupActionBar();
        getIntents();

        mActivity = this;
        mVideoFrameLayout = findViewById(R.id.container);
        mVideoView = findViewById(R.id.conversation_video_view);
        mVideoCardView = findViewById(R.id.video_view_card_view);
        mImageView = findViewById(R.id.conversation_video_image_view);
        mResponsePagerCardView = findViewById(R.id.pager_card_view);

        File resourceDirectory = getResourceDirectory(mResourceDir);
        File resourceChatContentFile = new File(resourceDirectory, Constants.ChatContentFileName);
        mChatContentArrayList = readResourceContentFile(resourceChatContentFile);


        setupNavigationDrawer();
        setupConversationListView();
        setupViewPager();
        setupFabs();
        setupUserInputTextView();
      //  setupUserHelpAudio();


        mInterfaces = new HashMap<Integer, OnUpdateResponseInFragment>();

        if (mEditMode) {
            prepareEditMode();
        } else {
            if (mChatContentArrayList.size() > 0) {
                prepare_section();
            } else {
                prepareEditMode();
            }

        }

    }

    private ArrayList<ChatContent> readResourceContentFile(File resourceFile) {  //TODO This is not working when file is in temporary folder . it look at the false place
                                                                                    //TODO the copy from temp folder to ap folder seems not to work either
        String chatContentString = IOUtils.readJsonFile(resourceFile);
        Gson gSon = new Gson();
        ChatContent[] chatContentArray = gSon.fromJson(chatContentString, ChatContent[].class);

        return new ArrayList<>(Arrays.asList(chatContentArray));

    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.activity_conversation_drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.toolbar_login:
                    //checkPermissionAndOpenLoginActivity();
                    return true;
                case R.id.toolbar_help:
                    //showHelp();
                    return true;
                case R.id.toolbar_editor_mode:
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mEditMode = false;
                    } else {
                        menuItem.setChecked(true);
                        mEditMode = true;
                        setupFabs();
                        prepareEditMode();
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

        switch (item.getItemId()) {
            case R.id.toolbar_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getIntents() {
        if (getIntent().getExtras() != null) {
            mEditMode = getIntent().getExtras().getBoolean(Constants.EditMode);
            mResourceDir = getIntent().getExtras().getString("ResourceDir").toLowerCase();
            String AppDirectoryPathString = getIntent().getExtras().getString(Constants.MoraMora);
            mApplicationDirectory = new File(AppDirectoryPathString);
            mTemporaryDirectory = new File( getIntent().getExtras().getString(Constants.FullTemporaryDirectory) );

            if (!IOUtils.prepareFileStructure(mApplicationDirectory.toString())) {
                finish();
            }
        }
    }

    private void prepareEditMode() {
        mAdapter.setItemCount(mChatContentArrayList.size());
        mAdapter.notifyDataSetChanged();
    }

    /*
    The user can press the help button (floating action button fab) to listen to the audio file
    of the right answer. That function sets up the audio infrastructure and configures the call
    to the function playSound() when the sound file is loaded and ready to be played.
     */
/*
    private void setupUserHelpAudio() {
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            mSoundIsLoaded = true;
            playSound();
        });
    }
*/

    /*
    The user interacts with the application via that EditTextView. Its used to give responses or
    ask questions to the video speaker. After hitting the DONE button the keyboard disappears and
    the input is processed in the function response().
     */

    private void setupUserInputTextView() {
        mResponseText = findViewById(R.id.response_editText);
        mResponseText.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))) {
                response();
            }
            return false;
        });
    }


    /*
    The floating action button is normally invisible but is used in editor mode to create a new
    question-answer combination for the conversation.
     */

    private void setupFabs() {
        FloatingActionButton fabAddMessage = findViewById(R.id.conversation_fab_add);
        FloatingActionButton fabHelp = findViewById(R.id.fab_help);
        FloatingActionButton fabRemoveMessage = findViewById(R.id.conversation_fab_remove);
        FloatingActionButton fabSave = findViewById(R.id.conversation_fab_save);

        if (!mEditMode) {
            fabAddMessage.hide();
            fabRemoveMessage.hide();
            fabSave.hide();
        } else {
            fabAddMessage.show();
            fabRemoveMessage.show();
            fabSave.show();
        }

        fabAddMessage.setOnClickListener(view -> addChatMessage());
        fabHelp.setOnClickListener(view -> help());
        fabSave.setOnClickListener(view -> saveConversationToFile());
        fabRemoveMessage.setOnClickListener(view -> removeLastChatMessage());
    }

    private void addChatMessage() {
        ChatContent chatContent = new ChatContent();
        if (mChatContentArrayList.get(mChatContentArrayList.size() - 1).isUser())
            chatContent.setUser(false);
        else
            chatContent.setUser(true);

        mChatContentArrayList.add(chatContent);
        mAdapter.notifyDataSetChanged();
        prepareEditMode();
        Toast.makeText(this, "Content added!", Toast.LENGTH_SHORT).show();
    }

    private void saveConversationToFile() {

        File resourceDirectory = getResourceDirectory(mResourceDir);                              //TODO change that to show a snackbar
        File chatContentFile = new File(resourceDirectory, Constants.ChatContentFileName);
        IOUtils.writeChatContentListToFile(chatContentFile, mChatContentArrayList);
        Toast.makeText(this, "Conversation is saved!", Toast.LENGTH_SHORT).show();

    }

    private void removeLastChatMessage() {
        int position = mChatContentArrayList.size() - 1;
        mAdapter.remove(position);
    }

    /*
    The conversation between the user and the video speaker is represented in a listView. Each list
    item is a sentence pronounced by either the video speaker or the user.
     */


    private void setupConversationListView() {

        mAdapter = new AdapterChatArray(this, mChatContentArrayList, getResourceDirectory(mResourceDir).toString());

        mConversationListView = findViewById(R.id.conversation_listView);
        mConversationListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mConversationListView.setLayoutManager(mLayoutManager);
        mConversationListView.setAdapter(mAdapter);


        mAdapter.setOnItemLongClickListener((position, viewHolder) -> {
            if (mEditMode) {
                if (!hasMicrophoneAccess()) {
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
                } else if (!hasCameraAccess()) {
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                } else {
                    showInputDialog(position);
                }
            }
            return true;
        });
    }

    private boolean hasCameraAccess() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasMicrophoneAccess() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }


    /*
    During the conversation the user has multiple choices which answer to give. These answers are
    proposed by a viewPager (vertical swipe view). This function is responsible for its setup
     */

    private void setupViewPager() {
        mPageFragmentAdapter = new AdapterPageFragment(getSupportFragmentManager());
        mPageFragmentAdapter.addFragment(new FragmentPageResponse(), " ");
        mPageFragmentAdapter.notifyDataSetChanged();

        ViewPager ViewPager = findViewById(R.id.pager);
        ViewPager.setAdapter(mPageFragmentAdapter);
    }


    @Override
    public void onBackPressed() {
        if (mContentChanged) {
            mContentChanged = false;
        }
        Intent intent = new Intent(this, ActivityTopicChoice.class);
        intent.putExtra("EditMode", mEditMode);
        intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString() );         //TODO is that really working? Does the intent is really send ?
        super.onBackPressed();
    }


    public void setOnUpdateResponsesInterface(OnUpdateResponseInFragment fragment, int position) {        // It's called from within the pager fragment to send its address
        OnUpdateResponseInFragment interfaceInstance = new FragmentPageResponse();
        interfaceInstance = fragment;
        mInterfaces.put(position, interfaceInstance);

        if (!mResponseCacheList.isEmpty()) {
            fragment.updateResponses(mResponseCacheList.get(position));
            if (mResponseCacheList.size() == position) {
                mResponseCacheList.clear();
            }
        }

    }


    private void prepare_section() {

        if ( mAdapter.getItemCount() < mChatContentArrayList.size()) {

            mAdapter.stepForward();
            playMedia( false );

            int currentListPosition = mAdapter.getItemCount() - 1;
            int nextListPosition = currentListPosition + 1;

            mResponseText.setText("");

            if( nextListPosition < mChatContentArrayList.size() ) {
                String[] responseArray = mChatContentArrayList.get(nextListPosition).getTranslatedMessages();

                while (responseArray.length > mPageFragmentAdapter.getCount()) {
                    mPageFragmentAdapter.addFragment(new FragmentPageResponse(), " ");
                    mPageFragmentAdapter.notifyDataSetChanged();
                }


                for (int position = 0; position < responseArray.length; position++) {
                    String responseString = mChatContentArrayList.get(nextListPosition).getTranslatedMessages()[position];
                    if (mInterfaces.get(0) != null) {
                        mInterfaces.get(position).updateResponses(responseString);
                    } else {
                        mResponseCacheList.add(responseString);
                    }
                }

                RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_END;
                    }

                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return 4 / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, displayMetrics);       // The first number (4) defines the scrolling speed
                    }
                };

                smoothScroller.setTargetPosition(currentListPosition);
                mLayoutManager.startSmoothScroll(smoothScroller);
            }

        } else {
            Toast.makeText(this, "Congratulation!! You did it!", Toast.LENGTH_LONG);
        }

    }

    /*
        private void convertOldToNew(Content content){
             content.addResponse(content.getResponseText(), content.getResponseTranslationText());
             content.addResponse("Veloma!", "Auf Wiedersehen!");
        }
    */
    private void playMedia(boolean isHelp) {
        String[] mediaNames = mChatContentArrayList.get(mAdapter.getItemCount() - 1).getMediaFileNames();
        onPlayMediaButtonPressed(mediaNames[0], new Rect(0,0,0,0), isHelp);
    }

    private File getResourceDirectory(String ResourceDir) {
        return new File(mApplicationDirectory, ResourceDir);
    }


    public void response() {
        int nextListPosition = mAdapter.getItemCount();

        String response = mResponseText.getText().toString().toLowerCase().replaceAll("\\W", "");

        String[] responseArray = mChatContentArrayList.get( nextListPosition ).getNativeMessages();
        boolean foundCorrectResponse = false;
        for (int index = 0; index < responseArray.length; index++) {
            String rightResponse = responseArray[index].toLowerCase().replaceAll("\\W", "");
            if (rightResponse.equals(response)) {
                foundCorrectResponse = true;
                mAdapter.stepForward();
                mResponseText.setText("");
                prepare_section();
                Toast.makeText(this, "Bravo!!", Toast.LENGTH_LONG).show();
            }
        }
        if (!foundCorrectResponse)
            Toast.makeText(this, "Ups!!", Toast.LENGTH_LONG).show();
    }


    public void help() {

        Log.e(Constants.TAG," help start itemcount: " + mAdapter.getItemCount());
        if (mInterfaces.get(0) != null) {
            String[] responseStringArray = mChatContentArrayList.get( mAdapter.getItemCount() ).getNativeMessages();
            for (int pos = 0; pos < responseStringArray.length; pos++) {
                String responseString = responseStringArray[pos];
                mInterfaces.get(pos).updateResponses(responseString);
            }
        } else {
            Log.e(Constants.TAG, "Interface to fragment is null: Fragment is not yet created");
        }

        mAdapter.stepForward();
        playMedia(true);


    }



    protected void showInputDialog(final int position) {

        String[] nativeMessages = mChatContentArrayList.get(position).getNativeMessages();          //TODO this can be multiple messages and should all be passed to the dialog wich must be able to handle that
        String[] translatedMessages = mChatContentArrayList.get(position).getTranslatedMessages();
        DialogAddChangeMessage dialog = DialogAddChangeMessage.newInstance(nativeMessages, translatedMessages, position);
        dialog.show(getSupportFragmentManager(), Constants.TAG);
    }


    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Toolbar toolbar = findViewById(R.id.toolbar_conversation);
            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(R.drawable.ic_gecko_top_view_shape);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);                                  // Show the Up button in the action bar.
        }
    }
//TODO the play video and audio methods must fins out if its a video or an audio - hide - videoview it audio


    public void prepareCamera(int position, int mediaType) {
        mVideoFrameLayout.setVisibility(View.VISIBLE);
        File topicDirectory = new File(mApplicationDirectory, mResourceDir);

        File mediaFile;

        if( mediaType == MEDIA_TYPE_VIDEO){
            mediaFile = new File(topicDirectory, Constants.VideoName + position + ".mp4");
            mChatContentArrayList.get(position).setMediaFileNames(new String[]{mediaFile.getName()});

            File audioFileToRemove = new File(topicDirectory, Constants.AudioName + position + ".m4a");
            if( audioFileToRemove.exists() )
                audioFileToRemove.delete();

            File imageFileToRemove = new File(topicDirectory, Constants.ImageName + position + ".jpg");
            if( imageFileToRemove.exists() )
                imageFileToRemove.delete();

        } else if( mediaType == MEDIA_TYPE_IMAGE){
            mediaFile = new File(topicDirectory, Constants.ImageName + position + ".jpg");
            mChatContentArrayList.get(position).setImageFileName( mediaFile.getName() );

            File videoFileToRemove = new File(topicDirectory, Constants.VideoName + position + ".mp4");
            if( videoFileToRemove.exists() )
                videoFileToRemove.delete();

        } else {
            Log.e(Constants.TAG,"Prepare Camera was called with in invalid media type!");
            return;
        }



        getFragmentManager().beginTransaction()
                .replace(R.id.container, FragmentVideoCamera.newInstance( mediaFile.toString() ))
                .commit();
    }

    public void finishCamera() {
        mVideoFrameLayout.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
    }


    public void recordAudio(int position) {

        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        File topicDirectory = new File(mApplicationDirectory, mResourceDir);

        File audioFile = new File(topicDirectory, Constants.AudioName + position + ".m4a");
        mChatContentArrayList.get(position).setMediaFileNames(new String[]{audioFile.getName()});

        if (audioFile.exists()) {
            audioFile.delete();
        }

        mMediaRecorder.setOutputFile(audioFile.getAbsolutePath());

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Io problems while preparing recording!" +
                    audioFile.getAbsolutePath() + "]: " + e.getMessage());
        }

        File videoFileToRemove = new File(topicDirectory, Constants.VideoName + position + ".mp4");
        if( videoFileToRemove.exists() )
            videoFileToRemove.delete();
    }

    public void recordAudioStop() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void onDialogAddChangeMessagePosClick(String nativeMessage, String translatedMessage, int position, File mediaFile) {

        mChatContentArrayList.get(position).setNativeMessages(new String[]{nativeMessage});
        mChatContentArrayList.get(position).setTranslatedMessages(new String[]{translatedMessage});

        if( mediaFile != null ){
            File topicDirectory = getResourceDirectory( mResourceDir );                             //TODO compress file if image if too large - if video tell user that file size to large!
            IOUtils.copyFileOrDirectory( mediaFile.toString(), topicDirectory.toString() );

            File fullMediaFile = new File( topicDirectory, mediaFile.getName() );
            File renamedMediaFile = null;
            if( IOUtils.isImageFile( mediaFile.toString() )){
                renamedMediaFile = new File(topicDirectory, Constants.ImageName + position + ".jpg");
                mChatContentArrayList.get(position).setImageFileName( renamedMediaFile.getName() );
            } else if ( IOUtils.isVideoFile( mediaFile.toString() )){
                renamedMediaFile = new File(topicDirectory, Constants.VideoName + position + ".mp4");
                mChatContentArrayList.get(position).setMediaFileNames( new String[]{ renamedMediaFile.getName() } );
                mChatContentArrayList.get(position).setImageFileName( "" );
            }


            if( renamedMediaFile.exists() ){
                renamedMediaFile.delete();
            }
            try {
                fullMediaFile.renameTo( renamedMediaFile );
            } catch (NullPointerException e){
                Log.e(Constants.TAG, getString(R.string.could_not_rename));
            }
        }

        mAdapter.notifyItemChanged(position);
        mAdapter.notifyDataSetChanged();
        mConversationListView.requestFocus();
        mContentChanged = true;
    }

    public void onPlayMediaButtonPressed(String mediaName, Rect startBounds, boolean isHelp ){

        float startScale;
        Rect finalBounds = new Rect();
        Point offset = new Point();

        File resourceDirectory = getResourceDirectory(mResourceDir);
        File mediaPathFile = new File(resourceDirectory, mediaName );

        if( IOUtils.isAudioFile( mediaName) ){
            String reducedMediaName = mediaName.substring(0, mediaName.indexOf("."));               //TODO this is not a very elegant solution maybe
            String positionString = reducedMediaName.replaceAll("[^0-9]", "");
            int position = Integer.parseInt(positionString);

            if( position < mChatContentArrayList.size() ){

                String imageFileName = mChatContentArrayList.get( position ).getImageFileName();
                File imagePathFile = new File( resourceDirectory, imageFileName );

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                        .skipMemoryCache(true)
                        .override(600, 600);

                Glide.with(this)
                        .load(Uri.fromFile( imagePathFile ))
                        .apply(requestOptions)
                        .into(mImageView);


            } else {
                mVideoView.setBackgroundResource(R.drawable.lemur);
            }

        }
        else
            mVideoView.setBackgroundResource(0);

        mVideoView.setVideoPath( mediaPathFile.toString() );


        mVideoCardView.getGlobalVisibleRect(finalBounds, offset);

        startScale = (float) startBounds.width() / finalBounds.width();

        mVideoCardView.setVisibility(View.VISIBLE);
        mVideoCardView.setAlpha(0.4f);

        if( IOUtils.isAudioFile( mediaName )){
            mImageView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setAlpha(0f);
        } else {
            mVideoView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
            mVideoView.seekTo(1);
        }


        Point startPoint = new Point();
        startPoint.x = (int)(  startBounds.left - finalBounds.width()*startScale/2 );
        startPoint.y =  (int) ( startBounds.top - finalBounds.height()*startScale/2 - this.getResources().getDimension(R.dimen.my_app_bar_height) );

        mVideoCardView.setX(startPoint.x);
        mVideoCardView.setY(startPoint.y);

        int centerX = this.getResources().getDisplayMetrics().widthPixels/2;
        int centerY = this.getResources().getDisplayMetrics().heightPixels/2;


        Log.e(Constants.TAG,"mVideoCardView.getWidth/2:  " + mVideoCardView.getWidth()/2 );
        float marginLeft = centerX - 112;                                                           // The value 112 is returned by mVideoCardView.getWidth()/2
        float marginTop = centerY - 1.4f*112;                                                       // mVideoCardView.getHeight()/2 but the method returns a different value at the first call



        AnimatorSet startSet = new AnimatorSet();
        startSet
                .play(ObjectAnimator.ofFloat(mVideoCardView, View.X, marginLeft))
                .with(ObjectAnimator.ofFloat(mVideoCardView, View.Y, marginTop))
                .with(ObjectAnimator.ofFloat(mVideoCardView, View.SCALE_X, 0f, 1f))
                .with(ObjectAnimator.ofFloat(mVideoCardView, View.SCALE_Y, 0f, 1f))
                .with(ObjectAnimator.ofFloat(mVideoCardView, View.ALPHA, 1))
                .with(ObjectAnimator.ofFloat(mConversationListView, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mResponsePagerCardView, View.ALPHA, 0f));


        startSet.setDuration(500);
        startSet.setInterpolator(new DecelerateInterpolator());
        startSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mVideoView.start();
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        startSet.start();
        mCurrentAnimator = startSet;


        mVideoView.setOnCompletionListener(mp -> {

            AnimatorSet endSet = new AnimatorSet();
            endSet
                    .play(ObjectAnimator.ofFloat(mVideoCardView, View.X, startPoint.x))
                    .with(ObjectAnimator.ofFloat(mVideoCardView, View.Y, startPoint.y))
                    .with(ObjectAnimator.ofFloat(mVideoCardView, View.SCALE_X, 1f, 0f))
                    .with(ObjectAnimator.ofFloat(mVideoCardView, View.SCALE_Y, 1f, 0f))
                    .with(ObjectAnimator.ofFloat(mVideoCardView, View.ALPHA, 0.2f));

            endSet.setDuration(500);
            endSet.setInterpolator(new DecelerateInterpolator());
            endSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mVideoCardView.setVisibility(View.INVISIBLE);
                    mVideoView.setAlpha(1f);
                    mVideoView.setVisibility(View.INVISIBLE);
                    mImageView.setVisibility(View.INVISIBLE);
                    mConversationListView.animate().alpha(1f).setDuration(500);
                    mResponsePagerCardView.animate().alpha(1f).setDuration(500);
                    mCurrentAnimator = null;

                    if( isHelp )
                        mAdapter.setItemCount( mAdapter.getItemCount()-1 );

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCurrentAnimator = null;
                }
            });
            endSet.start();
            mCurrentAnimator = endSet;
        });
    }

}





