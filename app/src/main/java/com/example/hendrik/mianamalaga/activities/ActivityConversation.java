
package com.example.hendrik.mianamalaga.activities;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.hendrik.mianamalaga.BuildConfig;
import com.example.hendrik.mianamalaga.dialogs.DialogFilePicker;
import com.example.hendrik.mianamalaga.dialogs.DialogHelp;
import com.example.hendrik.mianamalaga.utilities.Utils;
import com.example.hendrik.mianamalaga.adapter.AdapterChatArray;
import com.example.hendrik.mianamalaga.adapter.AdapterPageFragment;
import com.example.hendrik.mianamalaga.container.ChatContent;
import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.dialogs.DialogAddChangeMessage;
import com.example.hendrik.mianamalaga.fragments.FragmentPageResponse;
import com.example.hendrik.mianamalaga.fragments.FragmentVideoCamera;
import com.example.hendrik.mianamalaga.interfaces.OnUpdateResponseInFragment;
import com.example.hendrik.mianamalaga.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

// TODO If we do have more than one original messages one after the other. The videos must also be played one ofter another ....
// TODO without user action. One should on the other hand prevent that multiple user messages follow one after another

public class ActivityConversation extends ActivityBase {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 2;

    private AdapterPageFragment mPageFragmentAdapter;
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
    private TextView mSolutionTextView;
    private ImageView mImageView;
    private Animator mCurrentAnimator;
    private ConstraintLayout mMainLayout;
    private FloatingActionButton mFabHelp;
    private Button mButtonA;
    private Button mButtonB;
    private Button mButtonC;
    private boolean mUseButtonChoice = true;

    private ArrayList<ChatContent> mChatContentArrayList;


    private HashMap<Integer, OnUpdateResponseInFragment> mInterfaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_conversation);
        setupActionBar();
        getIntents();

        mActivity = this;
        mMainLayout = findViewById(R.id.conversation_main_layout);
        mVideoFrameLayout = findViewById(R.id.container);
        mVideoView = findViewById(R.id.conversation_video_view);
        mVideoCardView = findViewById(R.id.video_view_card_view);
        mImageView = findViewById(R.id.conversation_video_image_view);
        mResponsePagerCardView = findViewById(R.id.pager_card_view);
        mSolutionTextView = findViewById(R.id.solution_textView);

        File resourceDirectory = getResourceDirectory(mResourceDir);
        File resourceChatContentFile = new File(resourceDirectory, Constants.ChatContentFileName);
        mChatContentArrayList = readResourceContentFile(resourceChatContentFile);

        setupNavigationDrawer();
        setupConversationListView();
        setupViewPager();
        setupFabs();
        setupAnswerButtons();
        setupUserInputTextView();
        //  setupUserHelpAudio();

        mInterfaces = new HashMap<Integer, OnUpdateResponseInFragment>();

        File topicDirectory = new File(mFullTopicsDirectory, mResourceDir);
        File backgroundOutputFile = new File( topicDirectory,Constants.BackgroundImageName );
        if ( backgroundOutputFile.exists() ){
            mMainLayout.setBackground( Drawable.createFromPath( backgroundOutputFile.toString() ));
        }

        if (mEditMode) {
            prepareEditMode();
        } else {
            mVideoCardView.post(() -> {                                                             // This is a work around to get the dimensions of the VideoCardView
                if (mChatContentArrayList.size() > 0) {                                             // The measurement is taken after the drawing phase is finished..

                    SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
                    int lastItemCount = sharedPref.getInt(Constants.ConversationListCount + mResourceDir, 0);
                    mAdapter.setItemCount(lastItemCount);

                    prepare_section();
                } else {
                    prepareEditMode();
                }
            });

        }

    }

    private ArrayList<ChatContent> readResourceContentFile(File resourceFile) {  //TODO This is not working when file is in temporary folder . it look at the false place
                                                                                 //TODO the copy from temp folder to ap folder seems not to work either
        String chatContentString = Utils.readJsonFile(resourceFile);
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
                    checkPermissionAndOpenLoginActivity();
                    return true;
                case R.id.toolbar_help:
                    showHelp();
                    return true;
                case R.id.toolbar_editor_mode:
                    if (menuItem.isChecked()) {
                        menuItem.setChecked(false);
                        mEditMode = false;
                        setupFabs();
                        mButtonA.setVisibility(View.VISIBLE);
                        mButtonB.setVisibility(View.VISIBLE);
                        mButtonC.setVisibility(View.VISIBLE);
                        mSolutionTextView.setVisibility(View.VISIBLE);
                    } else {
                        menuItem.setChecked(true);
                        mEditMode = true;
                        setupFabs();
                        prepareEditMode();
                    }
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.toolbar_restart:
                    mDrawerLayout.closeDrawers();
                    mAdapter.setItemCount(0);
                    prepare_section();

                    return true;

                case R.id.toolbar_dictionary:
                    Toast.makeText(this, this.getString( R.string.ThatFunctionalityIsUnderDevelopment ),Toast.LENGTH_SHORT).show();
                    /*
                    Intent intent = new Intent(this, ActivityDictionary.class);
                    intent.putExtra("EditMode", mEditMode);
                    intent.putExtra( Constants.FullTemporaryDirectory, mTemporaryDirectory.toString() );
                    intent.putExtra( Constants.RelativeResourceDirectory, mResourceDir );
                    intent.putExtra( Constants.MoraMora, mApplicationDirectory.toString() );
                    startActivity(intent);*/
                    return true;

                case R.id.toolbar_background_image:
                    mDrawerLayout.closeDrawers();
                    openFilePickerToChooseBackgroundImageFile();
                    return true;

                default:
                    return false;
            }
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        if( !mEditMode ) {
            SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            int lastItemCount = (mAdapter.getItemCount() > 0) ? mAdapter.getItemCount() - 1 : 0;
            editor.putInt(Constants.ConversationListCount + mResourceDir, lastItemCount);
            editor.apply();
        }
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

    private void getIntents() {
        if (getIntent().getExtras() != null) {
            //mEditMode = getIntent().getExtras().getBoolean( Constants.EditMode );
            mResourceDir = getIntent().getExtras().getString( Constants.RelativeResourceDirectory).toLowerCase();
            //String AppDirectoryPathString = getIntent().getExtras().getString(Constants.MoraMora);
            //mApplicationDirectory = new File(AppDirectoryPathString);
            //mTemporaryDirectory = new File(getIntent().getExtras().getString(Constants.FullTemporaryDirectory));

            if (!Utils.prepareFileStructure(mFullTopicsDirectory.toString())) {
                finish();
            }
        }
    }

    private void prepareEditMode() {
        mAdapter.setItemCount(mChatContentArrayList.size());
        mAdapter.notifyDataSetChanged();
        mButtonA.setVisibility(View.GONE);
        mButtonB.setVisibility(View.GONE);
        mButtonC.setVisibility(View.GONE);
        mSolutionTextView.setVisibility(View.GONE);
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
                String response = mResponseText.getText().toString().toLowerCase().replaceAll("\\W", "");
                response( response );
            }
            return false;
        });
    }


    /*
    The floating action button is normally invisible but is used in editor mode to create a new
    question-answer combination for the conversation.
     */

    private void setupFabs() {
        mFabHelp = findViewById(R.id.fab_help);
        FloatingActionButton fabAddOriginalMessage = findViewById(R.id.conversation_fab_add);
        FloatingActionButton fabRemoveMessage = findViewById(R.id.conversation_fab_remove);
        FloatingActionButton fabAddUserMessage = findViewById(R.id.conversation_fab_add_user_message);

        if (mEditMode) {
            fabAddOriginalMessage.show();
            fabRemoveMessage.show();
            fabAddUserMessage.show();
            mFabHelp.setOnClickListener(view -> showHelpEditMode());
            //mFabHelp.setImageResource(R.drawable.ic_file_save);
        } else {
            fabAddOriginalMessage.hide();
            fabRemoveMessage.hide();
            fabAddUserMessage.hide();
            mFabHelp.setOnClickListener(view -> help());
            mFabHelp.setImageResource(R.drawable.ic_help);
        }

        fabAddOriginalMessage.setOnClickListener(view -> addChatMessage(false));
        fabAddUserMessage.setOnClickListener(view -> addChatMessage(true));
        fabRemoveMessage.setOnClickListener(view -> removeLastChatMessage());
    }

    private void addChatMessage(boolean isUser) {
        ChatContent chatContent = new ChatContent();
        if (isUser) {
            if (mChatContentArrayList.get(mChatContentArrayList.size() - 1).isUser()) {
                Toast.makeText(this, this.getString( R.string.youAlreadyHaveAddedAUserMessage ), Toast.LENGTH_SHORT).show();
                return;
            }

            chatContent.setUser(true);
        } else
            chatContent.setUser(false);

        mChatContentArrayList.add(chatContent);
        mAdapter.notifyDataSetChanged();
        prepareEditMode();
        Toast.makeText(this, this.getString( R.string.ContentAdded ), Toast.LENGTH_SHORT).show();
        smoothScrollToPosition( mAdapter.getItemCount() );
    }

    private void saveConversationToFile() {

        File resourceDirectory = getResourceDirectory(mResourceDir);                              //TODO change that to show a snackbar
        File chatContentFile = new File(resourceDirectory, Constants.ChatContentFileName);
        Utils.writeChatContentListToFile(chatContentFile, mChatContentArrayList);
        //Toast.makeText(this, this.getString( R.string.ConversationIsSaved ), Toast.LENGTH_SHORT).show();

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

        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
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
        intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString());         //TODO is that really working? Does the intent is really send ?
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

        if (mAdapter.getItemCount() < mChatContentArrayList.size()) {

            mAdapter.stepForward();
            playMedia(false);

            int currentListPosition = mAdapter.getItemCount() - 1;
            int nextListPosition = currentListPosition + 1;

            mResponseText.setText("");

            if (nextListPosition < mChatContentArrayList.size()) {
                String response = mChatContentArrayList.get(nextListPosition).getTranslatedMessage();
                mSolutionTextView.setText( response.length() > 2 ? response.substring(2) : response );

                String [] possibleAnswers = mChatContentArrayList.get(nextListPosition).getNativeMessages();
                if( possibleAnswers.length == 3) {
                    mButtonA.setText(possibleAnswers[0]);
                    mButtonB.setText(possibleAnswers[1]);
                    mButtonC.setText(possibleAnswers[2]);
                }
/*
                while (responseArray.length 1 > mPageFragmentAdapter.getCount()) {
                    mPageFragmentAdapter.addFragment(new FragmentPageResponse(), " ");
                    mPageFragmentAdapter.notifyDataSetChanged();
                }


                for (int position = 0; position < responseArray.length-1; position++) {
                    String responseString = mChatContentArrayList.get(nextListPosition).getTranslatedMessage();[position];
                    if (mInterfaces.get(0) != null) {
                        mInterfaces.get(position).updateResponses(responseString);
                    } else {
                        mResponseCacheList.add(responseString);
                    }
                }
*/
                smoothScrollToPosition( currentListPosition );
            }

        } else {
            Toast.makeText(this, R.string.CongratulationYouDidIt, Toast.LENGTH_LONG);
        }

    }

    private void smoothScrollToPosition(int position){
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

        smoothScroller.setTargetPosition( position );
        mLayoutManager.startSmoothScroll(smoothScroller);
    }

    private void setupAnswerButtons(){
        mButtonA = findViewById(R.id.buttonA);
        mButtonB = findViewById(R.id.buttonB);
        mButtonC = findViewById(R.id.buttonC);

        mButtonA.setOnClickListener(view -> {
            response("A");
        });

        mButtonB.setOnClickListener(view -> {
            response("B");
        });

        mButtonC.setOnClickListener(view -> {
            response("C");
        });
    }

    /*
        private void convertOldToNew(Content content){
             content.addResponse(content.getResponseText(), content.getResponseTranslationText());
             content.addResponse("Veloma!", "Auf Wiedersehen!");
        }
    */
    private void playMedia(boolean isHelp) {
        String[] mediaNames = mChatContentArrayList.get(mAdapter.getItemCount() - 1).getMediaFileNames();
        if( !mediaNames[0].isEmpty() ) onPlayMediaButtonPressed(mediaNames[0], isHelp);
    }

    private File getResourceDirectory(String ResourceDir) {
        return new File( mFullTopicsDirectory, ResourceDir );
    }


    public void response( String response ) {
        int nextListPosition = mAdapter.getItemCount();

        if (nextListPosition < mChatContentArrayList.size()) {

            boolean foundCorrectResponse = false;

            if( mUseButtonChoice ){
                if( mChatContentArrayList.get(nextListPosition).getTranslatedMessage().startsWith(response) ) foundCorrectResponse = true;
            } else {
                String[] responseArray = mChatContentArrayList.get(nextListPosition).getNativeMessages();
                String rightResponse = responseArray[0].toLowerCase().replaceAll("\\W", "");
                if (rightResponse.equals( response )) foundCorrectResponse = true;
            }

            if ( foundCorrectResponse == true ){
                mAdapter.stepForward();

                if (mAdapter.getItemCount() < mChatContentArrayList.size()) {
                    prepare_section();
                    Toast.makeText(this, this.getString( R.string.Bravo ), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, this.getString( R.string.BravoBravoBravoYouGotIt ), Toast.LENGTH_LONG).show();
                    mFabHelp.hide();
                    Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.firework);
                    mVideoView.setVideoURI(videoUri);
                    startMediaAnimation("something.mp4", false);
                }
            } else {
                Toast.makeText(this, this.getString( R.string.Ups ), Toast.LENGTH_LONG).show();
            }



/*
            for (int index = 0; index < responseArray.length; index++) {
                String rightResponse = responseArray[index].toLowerCase().replaceAll("\\W", "");
                if (rightResponse.equals(response)) {
                    mAdapter.stepForward();
                    foundCorrectResponse = true;
                    if (mAdapter.getItemCount() < mChatContentArrayList.size()) {
                        mResponseText.setText("");
                        prepare_section();
                        Toast.makeText(this, "Bravo!!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Bravo Bravo Bravo You got it!!", Toast.LENGTH_LONG).show();
                        mFabHelp.hide();
                        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.firework);
                        mVideoView.setVideoURI(videoUri);
                        startMediaAnimation("something.mp4", false);
                    }
                }
            }
            if (!foundCorrectResponse)
                Toast.makeText(this, "Ups!!", Toast.LENGTH_LONG).show();*/
        }
    }


    public void help() {

        Log.e(Constants.TAG, " help start itemcount: " + mAdapter.getItemCount());
        if (mInterfaces.get(0) != null) {
            if (mAdapter.getItemCount() < mChatContentArrayList.size()) {
                String[] responseStringArray = mChatContentArrayList.get(mAdapter.getItemCount()).getNativeMessages();
                for (int pos = 0; pos < responseStringArray.length; pos++) {
                    String responseString = responseStringArray[pos];
                    mInterfaces.get(pos).updateResponses(responseString);
                }
            }
        } else {
            Log.e(Constants.TAG, "Interface to fragment is null: Fragment is not yet created");
        }

        mConversationListView.animate().alpha(0f).setDuration(100);

        final Handler handler = new Handler();    // TODO this not elegant. There should be an animation completion listener
        handler.postDelayed((Runnable) () -> {
            mAdapter.stepForward();
            playMedia(true);
        }, 300);

    }


    protected void showInputDialog(final int position) {

        String[] nativeMessages = mChatContentArrayList.get(position).getNativeMessages();
        String translatedMessage = mChatContentArrayList.get(position).getTranslatedMessage();
        String infoText = mChatContentArrayList.get(position).getSupplementalInfoText();
        boolean isUser = mChatContentArrayList.get(position).isUser();
        DialogAddChangeMessage dialog = DialogAddChangeMessage.newInstance(nativeMessages, translatedMessage, infoText, position, isUser);
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



    public void prepareCamera(int position, int mediaType) {
        mVideoFrameLayout.setVisibility(View.VISIBLE);
        File topicDirectory = new File(mFullTopicsDirectory, mResourceDir);

        File mediaFile;

        if (mediaType == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(topicDirectory, Constants.VideoName + position + ".mp4");
            mChatContentArrayList.get(position).setMediaFileNames(new String[]{mediaFile.getName()});

            File audioFileToRemove = new File(topicDirectory, Constants.AudioName + position + ".m4a");
            if (audioFileToRemove.exists())
                audioFileToRemove.delete();

            File imageFileToRemove = new File(topicDirectory, Constants.ImageName + position + ".jpg");
            if (imageFileToRemove.exists())
                imageFileToRemove.delete();

        } else if (mediaType == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(topicDirectory, Constants.ImageName + position + ".jpg");
            mChatContentArrayList.get(position).setImageFileName(mediaFile.getName());

            File videoFileToRemove = new File(topicDirectory, Constants.VideoName + position + ".mp4");
            if (videoFileToRemove.exists())
                videoFileToRemove.delete();

        } else {
            Log.e(Constants.TAG, "Prepare Camera was called with in invalid media type!");
            return;
        }


        getFragmentManager().beginTransaction()
                .replace(R.id.container, FragmentVideoCamera.newInstance(mediaFile.toString()))
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

        File topicDirectory = new File(mFullTopicsDirectory, mResourceDir);

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
        if (videoFileToRemove.exists())
            videoFileToRemove.delete();
    }

    public void recordAudioStop() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void onDialogAddChangeMessagePosClick(String[] nativeMessages, String translatedMessage, String infoText, File mediaFile, int position) {

        mChatContentArrayList.get(position).setNativeMessages( nativeMessages);
        mChatContentArrayList.get(position).setTranslatedMessages( translatedMessage );
        mChatContentArrayList.get(position).setSupplementalInfoText( infoText );

        if (mediaFile != null) {
            File topicDirectory = getResourceDirectory( mResourceDir );                             //TODO compress file if image if too large - if video tell user that file size to large!

            Utils.copyFileOrDirectory(mediaFile.toString(), mTemporaryDirectory.toString() );
            File temporaryMediaFile = new File(mTemporaryDirectory, mediaFile.getName());
            File renamedTemporaryMediaFile = new File( mTemporaryDirectory, "tempFile" + mediaFile.getName().substring( mediaFile.getName().lastIndexOf(".")) );
            try {
                temporaryMediaFile.renameTo( renamedTemporaryMediaFile );
            } catch (NullPointerException e) {
                Log.e(Constants.TAG, getString(R.string.could_not_rename));
            }

            //Utils.copyFileOrDirectory(mediaFile.toString(), topicDirectory.toString());
            Utils.copyFileOrDirectory(renamedTemporaryMediaFile.toString(), topicDirectory.toString());

            //File fullMediaFile = new File(topicDirectory, mediaFile.getName());
            File fullMediaFile = new File(topicDirectory, renamedTemporaryMediaFile.getName());
            File renamedMediaFile = null;
            if (Utils.isImageFile(mediaFile.toString())) {
                renamedMediaFile = new File(topicDirectory, Constants.ImageName + position + ".jpg");
                mChatContentArrayList.get(position).setImageFileName(renamedMediaFile.getName());
            } else if (Utils.isVideoFile(mediaFile.toString())) {
                renamedMediaFile = new File(topicDirectory, Constants.VideoName + position + ".mp4");
                mChatContentArrayList.get(position).setMediaFileNames(new String[]{renamedMediaFile.getName()});
                mChatContentArrayList.get(position).setImageFileName("");
            }

            if (  renamedMediaFile != null ) {

                if (renamedMediaFile.exists()) {
                    renamedMediaFile.delete();
                }
                try {
                    fullMediaFile.renameTo(renamedMediaFile);
                } catch (NullPointerException e) {
                    Log.e(Constants.TAG, getString(R.string.could_not_rename));
                }
            }
        }

        mAdapter.notifyItemChanged(position);
        mAdapter.notifyDataSetChanged();
        mConversationListView.requestFocus();
        mContentChanged = true;

        saveConversationToFile();
    }

    public void onPlayMediaButtonPressed(String mediaName, boolean isHelp) {


        File resourceDirectory = getResourceDirectory(mResourceDir);
        File mediaPathFile = new File(resourceDirectory, mediaName);

        if (Utils.isAudioFile(mediaName)) {
            String reducedMediaName = mediaName.substring(0, mediaName.indexOf("."));               //TODO this is not a very elegant solution maybe
            String positionString = reducedMediaName.replaceAll("[^0-9]", "");
            int position = Integer.parseInt(positionString);

            if (position < mChatContentArrayList.size()) {

                String imageFileName = mChatContentArrayList.get(position).getImageFileName();
                File imagePathFile = new File(resourceDirectory, imageFileName);

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                        .skipMemoryCache(true)
                        .override(600, 600);

                Glide.with(this)
                        .load(Uri.fromFile(imagePathFile))
                        .apply(requestOptions)
                        .into(mImageView);


            } else {
                mVideoView.setBackgroundResource(R.drawable.lemur);
            }

        } else
            mVideoView.setBackgroundResource(0);

        mVideoView.setVideoPath(mediaPathFile.toString());
        startMediaAnimation(mediaName, isHelp);
    }

    public void startMediaAnimation(String mediaName, boolean isHelp) {

        float startScale;
        Rect finalBounds = new Rect();
        Rect startBounds = new Rect(0, 0, 0, 0);
        Point offset = new Point();

        mVideoCardView.getGlobalVisibleRect(finalBounds, offset);

        startScale = (float) startBounds.width() / finalBounds.width();

        mVideoCardView.setVisibility(View.VISIBLE);
        mVideoCardView.setAlpha(0.4f);

        if (Utils.isAudioFile(mediaName)) {
            mImageView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setAlpha(0f);
        } else {
            mVideoView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
            mVideoView.seekTo(1);
        }


        Point startPoint = new Point();
        startPoint.x = (int) (startBounds.left - finalBounds.width() * startScale / 2);
        startPoint.y = (int) (startBounds.top - finalBounds.height() * startScale / 2 - this.getResources().getDimension(R.dimen.my_app_bar_height));

        mVideoCardView.setX(startPoint.x);
        mVideoCardView.setY(startPoint.y);

        int centerX = this.getResources().getDisplayMetrics().widthPixels / 2;
        int centerY = this.getResources().getDisplayMetrics().heightPixels / 2;

        float marginLeft = centerX - mVideoCardView.getWidth() / 2;
        float marginTop = centerY - 1.4f * mVideoCardView.getWidth() / 2;


        AnimatorSet startSet = new AnimatorSet();
        startSet
                .play(ObjectAnimator.ofFloat(mVideoCardView, View.X, marginLeft))
                .with(ObjectAnimator.ofFloat(mVideoCardView, View.Y, marginTop))
                .with(ObjectAnimator.ofFloat(mVideoCardView, View.SCALE_X, 0f, 1f))
                .with(ObjectAnimator.ofFloat(mVideoCardView, View.SCALE_Y, 0f, 1f))
                .with(ObjectAnimator.ofFloat(mVideoCardView, View.ALPHA, 1))
                .with(ObjectAnimator.ofFloat(mConversationListView, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mResponsePagerCardView, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mButtonA, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mButtonB, View.ALPHA, 0f))
                .with(ObjectAnimator.ofFloat(mButtonC, View.ALPHA, 0f));


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
                    mButtonA.animate().alpha(1f).setDuration(500);
                    mButtonB.animate().alpha(1f).setDuration(500);
                    mButtonC.animate().alpha(1f).setDuration(500);
                    mCurrentAnimator = null;

                    if (isHelp)
                        mAdapter.setItemCount(mAdapter.getItemCount() - 1);

                    int nextMessageItemNumber = mAdapter.getItemCount();

                    if( nextMessageItemNumber < mChatContentArrayList.size() ){
                        if( !mChatContentArrayList.get( nextMessageItemNumber  ).isUser() ){
                            prepare_section();
                        }
                    }
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

    static final int REQUEST_VIDEO_CAPTURE = 1;

    public void dispatchTakeVideoIntent(int position) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            File topicDirectory = getResourceDirectory(mResourceDir);
            File videoFile = new File(topicDirectory, Constants.VideoName + position + ".mp4");

            mChatContentArrayList.get(position).setMediaFileNames(new String[]{videoFile.getName()});
            mChatContentArrayList.get(position).setImageFileName("");

            File oldMediaFile = new File(topicDirectory, Constants.ImageName + position + ".jpg");
            if (oldMediaFile.exists()) {
                oldMediaFile.delete();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                Context context = ActivityConversation.this;
                Uri tempVideoFileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider",  videoFile );
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempVideoFileUri);

                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities( takeVideoIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, tempVideoFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

            } else {

                Uri videoFileUri = Uri.fromFile(videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri);
            }
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            /*
            Uri videoUri = intent.getData();
            File videoFile = new File(videoUri.getEncodedPath());

            if( videoFile.getParent().endsWith( BuildConfig.TEMPORARY_FOLDER_NAME )){
                File destinationDirectory = new File( mApplicationDirectory, mResourceDir);
                File destinationFile = new File( destinationDirectory, videoFile.getName());
                Log.e(Constants.TAG,"Copying: " + videoFile.toString() + " into: " + destinationFile.toString() );
                Utils.copyFileOrDirectory( videoFile.toString(), destinationFile.toString() );
            }
             */
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void openFilePickerToChooseBackgroundImageFile(){
        DialogFilePicker filePicker = new DialogFilePicker( Objects.requireNonNull( ActivityConversation.this ) );
        filePicker.setOnFileSelectedListener( this::onFileChosen );
        filePicker.show();
    }

    private void onFileChosen(File file) {

        if( !Utils.isImageFile( file.toString() )){
            Toast.makeText(getApplicationContext(), this.getString( R.string.ChooseAnImageFileInstead ),Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap realImage = BitmapFactory.decodeFile( file.toString() );
        Bitmap bitmap = Utils.rotateBitmap(realImage, 90, false );

        File topicDirectory = new File(mFullTopicsDirectory, mResourceDir);
        File backgroundOutputFile = new File( topicDirectory,Constants.BackgroundImageName );

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream( backgroundOutputFile );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);

        mMainLayout.setBackground( Drawable.createFromPath( backgroundOutputFile.toString() ));
    }


    public void onSupplementalInfoButtonPressed(String supplementalInfoText) {
        DialogHelp helpDialog = DialogHelp.newInstance( supplementalInfoText );
        helpDialog.show( getSupportFragmentManager(), Constants.TAG );
    }

    void showHelp(){
        DialogHelp helpDialog = DialogHelp.newInstance( getString(R.string.HelpConversation) ); // TODO explain more and extract string resource
        helpDialog.show( getSupportFragmentManager(), Constants.TAG );
    }

    void showHelpEditMode(){
        DialogHelp helpDialog = DialogHelp.newInstance( getString(R.string.UsingThePlusButtonsYouCanAdd) ); // TODO explain more and extract string resource
        helpDialog.show( getSupportFragmentManager(), Constants.TAG );
    }
}




