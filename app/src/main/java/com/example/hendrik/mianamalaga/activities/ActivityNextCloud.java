package com.example.hendrik.mianamalaga.activities;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.hendrik.mianamalaga.utilities.Utils;
import com.example.hendrik.mianamalaga.tasks.RemoveFileAsyncTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;


import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dropbox.core.v2.users.FullAccount;
import com.example.hendrik.mianamalaga.adapter.AdapterCloudLanguage;
import com.example.hendrik.mianamalaga.adapter.AdapterTopic;
import com.example.hendrik.mianamalaga.BuildConfig;
import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.container.LanguageElement;
import com.example.hendrik.mianamalaga.R;
import com.example.hendrik.mianamalaga.tasks.AuthenticatorAsyncTask;
import com.example.hendrik.mianamalaga.tasks.CreateFolderAsyncTask;
import com.example.hendrik.mianamalaga.tasks.DownloadFileAsyncTask;
import com.example.hendrik.mianamalaga.tasks.ListFolderAsyncTask;
import com.example.hendrik.mianamalaga.tasks.UploadFileAsyncTask;
import com.example.hendrik.mianamalaga.container.Topic;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.model.RemoteFile;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


//TODO when you connect to cloud see if a special "important announcement file" is present. If yes show it to the user in a dialog ...
// an update should be able to be announced here

// TODO when the download of topic files ( picture and info ) is aborded - may due to internet connection failure - display the downloaded topics in listView


public class ActivityNextCloud extends AppCompatActivity {

    private String mAccessToken;
    private ArrayList<Topic> mTopicHomeArrayList;
    private ArrayList<Topic> mTopicCloudArrayList;
    private ArrayList<LanguageElement> mCloudFolderArrayList;
    private HashMap<String, File> mTopicDirectoriesToUpload = new HashMap<>();
    private HashMap<String, File> mTopicDirectoriesToDownload = new HashMap<>();
    private AdapterTopic mAdapterHome;
    private LinearLayoutManager mLayoutManager;
    private AdapterTopic mAdapterTopicCloud;
    private AdapterCloudLanguage mAdapterCloud;
    private ProgressDialog mProgressDialog;
    private long mTotalSize;
    private long mAccomplishedSize;
    private File mApplicationDirectory;
    private File mTemporaryDirectory;
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mMainLayout;
    private boolean mIsDownLoadMode;
    private RecyclerView mTopicCloudRecyclerView;
    private int mFilesToDownLoad;
    private int mFilesDownloaded;
    private FloatingActionButton mUpDownloadFab;

    private OwnCloudCredentials ownCloudCredentials;
    private AuthenticatorAsyncTask mAuthTask;
    private ListFolderAsyncTask mListFolderTask;
    private DownloadFileAsyncTask mDownloadFileTask;
    private UploadFileAsyncTask mUploadFileTask;
    private CreateFolderAsyncTask mCreateFolderTask;
    private RemoveFileAsyncTask mRemoveTask;

    private SharedPreferences mSharedPreferences;
    private String mServerUri;

    private enum Status{
        DISPLAY_UNKNOWN_LANGUAGE,
        DISPLAY_KNOWN_LANGUAGE,
        DISPLAY_TOPICS
    }

    private Status status = Status.DISPLAY_UNKNOWN_LANGUAGE;
    private final int UNUSED_POSITION = 0;
    private final int UNUSED_FILE_NUMBER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        mIsDownLoadMode = true;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );

        setupUi();
        setupToolbar();
        getIntents();
        setupNavigationDrawer();

        if (!tokenExists()) {                                                                            //No token Back to LoginActivity
            Intent intent = new Intent(ActivityNextCloud.this, ActivityLogIntoCloud.class);
            intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString());
            intent.putExtra(Constants.MoraMora, mApplicationDirectory.toString());
            startActivity(intent);
        }


        mAccessToken = retrieveAccessToken();
        setupFabs();

        ownCloudCredentials = buildCredentials(mAccessToken);
        getUserAccount();
        status = Status.DISPLAY_UNKNOWN_LANGUAGE;

        if (!mIsDownLoadMode)
            switchToUploadMode();

    }

    private void setupUi() {
        mMainLayout = findViewById(R.id.activity_cloud_main_layout);
        mTopicCloudRecyclerView = findViewById(R.id.cloud_cloud_recyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mTopicCloudRecyclerView.setLayoutManager(mLayoutManager);
        mTopicCloudRecyclerView.setHasFixedSize(true);
    }

    private void setupFabs() {
        //FloatingActionButton uploadFab = findViewById(R.id.cloud_fab_upload);
        //uploadFab.setOnClickListener(view -> uploadSelectedTopics());
        mUpDownloadFab = findViewById(R.id.cloud_fab_download);
        mUpDownloadFab.setOnClickListener(view -> {
            if ( mIsDownLoadMode )
                downloadSelectedTopics();
            else
                uploadSelectedTopics();
        });

    }

    private void getIntents() {
        if (getIntent().getExtras() != null) {
            String AppDirectoryPathString = getIntent().getExtras().getString(Constants.MoraMora);
            mApplicationDirectory = new File(AppDirectoryPathString);
            String temporaryDirectoryPath = getIntent().getExtras().getString(Constants.FullTemporaryDirectory);
            mTemporaryDirectory = new File(temporaryDirectoryPath);

            if (!Utils.prepareFileStructure(AppDirectoryPathString)) {
                finish();
            }
        }
    }


    private void addOrRemoveTopicToUploadList(AdapterTopic.TopicViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        Topic topic = mTopicHomeArrayList.get(position);

        if (viewHolder.mIconImageView.getVisibility() == View.INVISIBLE) {
            viewHolder.mIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_file_upload));
            viewHolder.mIconImageView.setVisibility(View.VISIBLE);
            viewHolder.mBaseView.setBackgroundColor(Color.LTGRAY);
            mTopicDirectoriesToUpload.put(topic.getName(), new File(new File(mApplicationDirectory, Utils.convertTopicName(topic.getName()) ).toString()));
        } else {

            viewHolder.mIconImageView.setVisibility(View.INVISIBLE);
            viewHolder.mBaseView.setBackgroundColor(Color.TRANSPARENT);
            mTopicDirectoriesToUpload.remove(topic.getName());
        }

        mAdapterHome.notifyItemChanged(position);
    }


    private void downloadSelectedTopics() {
        if (mAccessToken == null)
            return;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setMessage("Downloading files. Please wait...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        mTotalSize = 0;

        for (File relativeTopicDirectory : mTopicDirectoriesToDownload.values()) {

            mListFolderTask = new ListFolderAsyncTask(this, result -> {
                mListFolderTask = null;
                if ( result.isSuccess() ){
                    List<RemoteFile> files = new ArrayList<>();
                    List<Object> resultList = result.getData();
                    resultList.remove(0);
                    for (Object object : resultList) {
                        files.add((RemoteFile) object);
                    }
                    if (files != null) {
                        downloadCompleteFolder( files );
                    }
                } else {
                    mProgressDialog.dismiss();
                }
            });
            Object[] params = { mServerUri, ownCloudCredentials, relativeTopicDirectory.toString().toLowerCase() };
            mListFolderTask.execute(params);
        }
    }

    void downloadCompleteFolder(List<RemoteFile> remoteFiles) {

        mAccomplishedSize = 0;

        if (remoteFiles != null) {

            for (RemoteFile remoteFile : remoteFiles) {
                String remoteFileName = Utils.getNameFromPath(remoteFile.getRemotePath());
                if (!remoteFileName.equals(Constants.InfoFileName) && !remoteFileName.equals(Constants.TopicPictureFileName))
                    mTotalSize += remoteFile.getSize();
            }

            for (RemoteFile remoteFile : remoteFiles) {

                String remoteFileName = Utils.getNameFromPath(remoteFile.getRemotePath());
                if (!remoteFileName.equals(Constants.InfoFileName) && !remoteFileName.equals(Constants.TopicPictureFileName)) {

                    mDownloadFileTask = new DownloadFileAsyncTask(this, UNUSED_FILE_NUMBER, (result, fileNumber, fileSize) -> {
                        mDownloadFileTask = null;
                        if (result.isSuccess()) {
                            publishProgress(fileSize);
                        } else {
                            mProgressDialog.dismiss();
                            handleError(result, "An Error occurred during download!");
                        }
                    });
                    Object[] params = { mServerUri, ownCloudCredentials, remoteFile, mTemporaryDirectory};
                    mDownloadFileTask.execute(params);
                }
            }
        }
    }


    void publishProgress(long downloadedBytes) {

        int progress = 0;
        mAccomplishedSize += downloadedBytes;
        if (mTotalSize > 0) {
            progress = (int) (100 * mAccomplishedSize / mTotalSize);
        }

        mProgressDialog.setProgress(progress);
        if (progress >= 100) {
            mTotalSize = 0;
            mProgressDialog.dismiss();
            Toast.makeText(ActivityNextCloud.this,
                    "Transaction complete!",
                    Toast.LENGTH_SHORT)
                    .show();

            if( mIsDownLoadMode ) {
                startTopicChoiceActivity();
            }
        }
    }

    private void startTopicChoiceActivity(){

        File[] downloadedLanguageToLearnDirectory = mTemporaryDirectory.listFiles();
        File[] downloadedLanguageKnownDirectory = downloadedLanguageToLearnDirectory[0].listFiles();

        String languageDirectoryName = downloadedLanguageToLearnDirectory[0].getName();
        String motherTongueDirectoryName = downloadedLanguageKnownDirectory[0].getName();

        File languageToLearnDirectory = mApplicationDirectory.getParentFile();
        mApplicationDirectory = languageToLearnDirectory.getParentFile();


        Intent intent = new Intent(ActivityNextCloud.this, ActivityTopicChoice.class);
        intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString());
        intent.putExtra(Constants.MoraMora, mApplicationDirectory.toString());
        intent.putExtra(Constants.MotherTongue, motherTongueDirectoryName);
        intent.putExtra(Constants.LanguageToLearn, languageDirectoryName);
        Log.e(Constants.TAG," Starting topic choice!");
        startActivity(intent);
        finish();
    }


    private boolean tokenExists() {
        SharedPreferences prefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(Constants.OldCloudAccesTokenName, null);
        return accessToken != null;
    }

    private String retrieveAccessToken() {
        SharedPreferences prefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);              //check if ACCESS_TOKEN is stored on previous app launches
        String accessToken = prefs.getString(Constants.OldCloudAccesTokenName, null);
        if (accessToken == null) {
            Log.d(Constants.TAG, "No token found");
            return null;
        } else {
            Log.d(Constants.TAG, "Token exists");                                                               //accessToken already exists
            return accessToken;
        }
    }


    protected void getUserAccount() {

        if (mAccessToken == null)
            return;

            mAuthTask = new AuthenticatorAsyncTask(this, result -> updateNextCloudUI(result));
            Object[] params = { mServerUri, ownCloudCredentials};
            mAuthTask.execute(params);

    }

    private void updateNextCloudUI(RemoteOperationResult result) {
        mAuthTask = null;

        if( result.isSuccess() ){
            if( result.getLastPermanentLocation() != null ){
                Log.d(Constants.TAG,"NextCloud server has redirected to: " +  result.getLastPermanentLocation() );
            }

            ArrayList<Object> authResultData = result.getData();
            if (authResultData == null || authResultData.size() == 0) {
                Log.d(Constants.TAG, "Could not read user data!");
            } else {
                UserInfo userInfo = (UserInfo) authResultData.get(0);
                TextView accountNameTextView = findViewById(R.id.cloud_account_name_textView);
                accountNameTextView.setText(userInfo.getDisplayName());
                loadAvailableLanguages("");
            }

        } else {
            Log.e(Constants.TAG, "Error receiving NextCloud account details.");                 // TODO ask if a new connect should be triggered
        }
    }

    public OwnCloudCredentials buildCredentials(String credentialsString){

        Boolean isDefaultCLoud = mSharedPreferences.getBoolean("useDefaultCloud", true);

	//TODO try with Nextcloud credentials as they permit the use of access token
	// The output of that function must be NextCloudCredentials but it should be compatible with OwnCloudClient
	/*
	// Set basic credentials
  	client.setCredentials(
      	NextcloudCredentialsFactory.newBasicCredentials(username, password)
  	);
  	// Set bearer access token
  	String nextCloudAccessToken = BuildConfig.NEXTCLOUD_ACCES_TOKEN;
  	client.setCredentials(
      	NextcloudCredentialsFactory.newBearerCredentials( nextCloudAccessToken )
  	);
  	// Set SAML2 session token
  	client.setCredentials(
      	NextcloudCredentialsFactory.newSamlSsoCredentials(cookie)
  	);
	*/


        if ( isDefaultCLoud ) {
            mServerUri = BuildConfig.NEXTCLOUD_SERVER_URI;
            String[] credentialStringArray = credentialsString.split(Constants.Separator);
            String username = credentialStringArray[0];
            String password = credentialStringArray[1];
            return OwnCloudCredentialsFactory.newBasicCredentials(username, password);
        } else {
            mServerUri = mSharedPreferences.getString( getResources().getString( R.string.cloudUrlCustom ), "");
            String username = mSharedPreferences.getString( getResources().getString(R.string.cloudUserNameCustom ), "");
            String password = mSharedPreferences.getString( getResources().getString( R.string.cloudPasswordCustom ), "");
            return OwnCloudCredentialsFactory.newBasicCredentials(username, password);
        }
    }



    private void updateUI(FullAccount account) {
        ImageView profileImageView = findViewById(R.id.imageView);
        TextView accountNameTextView = findViewById(R.id.cloud_account_name_textView);

        accountNameTextView.setText(account.getName().getDisplayName());
        Glide.with(this)
                .load(account.getProfilePhotoUrl())
                .apply(new RequestOptions().override(600, 600))
                .into(profileImageView);

    }


    private void uploadSelectedTopics() {
        if (mAccessToken == null)
            return;

        Boolean isDefaultCLoud = mSharedPreferences.getBoolean("useDefaultCloud", true);

        if( !isDefaultCLoud ) {


            mTotalSize = calculateTotalFileSize(mTopicDirectoriesToUpload);

            if (mTotalSize > 0) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Uploading files. Please wait...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();
            } else {
                //TODO undefined dialog
            }
            mAccomplishedSize = 0;

            for (File topicDirectory : mTopicDirectoriesToUpload.values()) {
                if (topicDirectory != null) {
                    mCreateFolderTask = new CreateFolderAsyncTask(this, topicDirectory, (result, folder) -> {
                        mCreateFolderTask = null;
                        if (result.isSuccess() || result.getCode().equals(RemoteOperationResult.ResultCode.FOLDER_ALREADY_EXISTS)) {
                            File[] topicFiles = folder.listFiles();
                            if (topicFiles != null) {
                                for (File file : topicFiles) {
                                    if (file != null) {

                                        Log.d(Constants.TAG, "Uploading file: " + file.getAbsolutePath());

                                        mUploadFileTask = new UploadFileAsyncTask(this, (uploadResult, fileSize) -> {
                                            if (uploadResult.isSuccess()) {
                                                publishProgress(fileSize);
                                            } else {
                                                handleError(uploadResult, "Failed to upload file!");
                                                mProgressDialog.dismiss();
                                            }
                                        });
                                        Object[] params = {mServerUri, ownCloudCredentials, file};
                                        mUploadFileTask.execute(params);
                                    }
                                }
                            }
                        } else {
                            handleError(result, "Failed to create folder!");
                        }
                    });
                    Object[] params = {mServerUri, ownCloudCredentials};
                    mCreateFolderTask.execute(params);
                }
            }

        } else {
            Snackbar snackbar = Snackbar
                    .make(mMainLayout, "For uploads use a custom cloud!", Snackbar.LENGTH_LONG)
                    .setAction("Change cloud?", view -> {
                        startActivity( new Intent(this, ActivitySettings.class));
                    });
            snackbar.show();
        }

    }

    private long calculateTotalFileSize(HashMap<String, File> fileList) {
        long totalSize = 0;

        for (File topicDirectory : fileList.values()) {
            if (topicDirectory != null) {
                File[] topicFiles = topicDirectory.listFiles();
                if (topicFiles != null) {
                    for (File file : topicFiles) {
                        if (file != null) {
                            try{
                                totalSize += file.length();
                            } catch (SecurityException exception ){
                                Log.e(Constants.TAG," Can't get length of file!");
                                Log.e(Constants.TAG, exception.getMessage() );
                                return 0;
                            }
                        }
                    }
                }
            }
        }
        return totalSize;
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_cloud);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_gecko_top_view_shape);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.activity_cloud_drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout.closeDrawers();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.toolbar_login:
                    mDrawerLayout.closeDrawers();
                    recreate();
                    return true;
                case R.id.toolbar_help:
                    //showHelp();
                    return true;
                case R.id.switch_mode:
                    if (mIsDownLoadMode) {
                        mIsDownLoadMode = false;
                        menuItem.setIcon(R.drawable.ic_cloud_download);
                        menuItem.setTitle("Switch to download mode");
                        switchToUploadMode();
                    } else {
                        menuItem.setIcon(R.drawable.ic_cloud_upload);
                        menuItem.setTitle("Switch to upload mode");
                        mIsDownLoadMode = true;
                        switchToDownLoadMode();
                    }
                    mDrawerLayout.closeDrawers();
                    return true;

                default:
                    return false;
            }
        });
    }

    private void switchToDownLoadMode() {
        loadAvailableLanguages("");
        mUpDownloadFab.setImageResource(R.drawable.ic_cloud_download);
    }

    private void switchToUploadMode() {
        mTopicHomeArrayList = new ArrayList<>();
        Utils.getTopicListFromFiles(mApplicationDirectory, mTopicHomeArrayList, false);


        mAdapterHome = new AdapterTopic(this, mTopicHomeArrayList);
        mAdapterHome.setOnItemClickListener((position, viewHolder) -> {
            addOrRemoveTopicToUploadList(viewHolder);
        });
        mAdapterHome.setOnItemLongClickListener((position, viewHolder) -> true);

        mTopicCloudRecyclerView.removeAllViews();
        mTopicCloudRecyclerView.setAdapter(mAdapterHome);
        mAdapterHome.notifyDataSetChanged();

        mUpDownloadFab.setImageResource(R.drawable.ic_cloud_upload);
    }

    private void loadAvailableLanguages(String path) {

                mListFolderTask = new ListFolderAsyncTask(this, result -> {
                    mListFolderTask = null;

                    if( result.isSuccess() ) {
                        List<RemoteFile> files = new ArrayList<>();
                        List<Object> resultList = result.getData();
                        resultList.remove(0);
                        for (Object object : resultList ) {
                            files.add((RemoteFile) object);
                        }
                        if (files != null) {
                            handleLoadedData( files, UNUSED_POSITION );
                        }
                    } else {
                        handleError( result, "Failed to list folder!");
                    }
                });
                Object[] params = { mServerUri, ownCloudCredentials, path};
                mListFolderTask.execute(params);


    }



    private void diveIntoTopicFolderAndDownloadNecessaryFiles(String pathLower) {


        mListFolderTask = new ListFolderAsyncTask(this, result -> {
            mListFolderTask = null;
            if( result.isSuccess() ) {
                List<RemoteFile> files = new ArrayList<>();
                List<Object> resultList = result.getData();
                resultList.remove(0);
                for (Object object : resultList ) {
                    files.add((RemoteFile) object);
                }
                if (files != null) {
                    for (int topicNumber = 0; topicNumber < files.size(); topicNumber++) {
                        RemoteFile remoteFile = files.get(topicNumber);

                        if (remoteFile != null) {
                            String remoteFileName = Utils.getNameFromPath( remoteFile.getRemotePath() );
                            Log.e(Constants.TAG," New Download Task for : " + remoteFileName );
                            if ( remoteFileName.equals(Constants.InfoFileName) || remoteFileName.equals(Constants.InfoFileNameNew) || remoteFileName.equals(Constants.TopicPictureFileName)) {
                                File destinationDirectory = mTemporaryDirectory;
                                mDownloadFileTask = new DownloadFileAsyncTask(this, topicNumber, (result1, fileNumber, fileSize) -> {
                                    if (result.isSuccess()){
                                       mDownloadFileTask = null;
                                        mFilesDownloaded++;
                                        mProgressDialog.setProgress(mFilesDownloaded);
                                        if (mFilesDownloaded == mFilesToDownLoad)
                                            mProgressDialog.dismiss();
                                        Log.d(Constants.TAG, "Download finished :" + mFilesDownloaded + " of " + mFilesToDownLoad);
                                        if (mFilesDownloaded == mFilesToDownLoad) {
                                            setupTopicCloudRecyclerView();
                                        }
                                    } else {
                                        handleError(result, "An error occurred!");
                                    }

                                });
                                Object[] params = { mServerUri, ownCloudCredentials, remoteFile, destinationDirectory };
                                mDownloadFileTask.execute(params);
                            }
                        }
                    }

                }
            } else {
                handleError(result, "An error occurred!");
            }
        });
        Object[] params = { mServerUri, ownCloudCredentials, pathLower };
        mListFolderTask.execute(params);


    }

    private void setupTopicCloudRecyclerView() {

        if (mTopicCloudArrayList == null) {
            mTopicCloudArrayList = new ArrayList<>();
        }

        File[] languageToLearnDirectories = mTemporaryDirectory.listFiles();
        for (File languageToLearnDirectory : languageToLearnDirectories) {
            if (languageToLearnDirectory.isDirectory()) {
                File[] knownLanguageDirectories = languageToLearnDirectory.listFiles();
                for (File knownLanguageDirectory : knownLanguageDirectories) {
                    Utils.getTopicListFromFiles(knownLanguageDirectory, mTopicCloudArrayList, true);
                }
            }
        }


        mAdapterTopicCloud = new AdapterTopic(this, mTopicCloudArrayList);
        mAdapterTopicCloud.setOnItemClickListener((position, viewHolder) -> {
            addOrRemoveTopicToDownloadList(viewHolder);
        });

        mAdapterTopicCloud.setOnItemLongClickListener((position, viewHolder) -> {
            if( mSharedPreferences.getBoolean("useDefaultCloud", true) ) {
                Snackbar snackbar = Snackbar.make(mMainLayout, "You can only remove topics from your own cloud!", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                removeTopicFromCloud(viewHolder);
            }
            return false;
        });

        mTopicCloudRecyclerView.removeAllViews();
        mTopicCloudRecyclerView.setAdapter(mAdapterTopicCloud);
        mAdapterTopicCloud.notifyDataSetChanged();

    }


    private void setupRecyclerView(){

        mCloudFolderArrayList = new ArrayList<>();
        mAdapterCloud = new AdapterCloudLanguage(this, mCloudFolderArrayList);

        mAdapterCloud.setOnItemClickListener((position, viewHolder) -> {
            LanguageElement languageElement = mCloudFolderArrayList.get(position);

                    String remotePath = languageElement.getRemoteFile().getRemotePath();
                    mListFolderTask = new ListFolderAsyncTask(this, result -> {
                        mListFolderTask = null;
                        if( result.isSuccess() ) {
                            List<RemoteFile> files = new ArrayList<>();
                            List<Object> resultList = result.getData();
                            resultList.remove(0);                                               // First element is parent folder - remove it from list
                            for (Object object : resultList ) {
                                files.add((RemoteFile) object);
                            }
                            if (files != null) {
                                handleLoadedData( files, position + 1 );
                            }
                        } else {
                            Log.e(Constants.TAG, "Failed to list folder " + remotePath );
                            Snackbar snackbar = Snackbar
                                    .make(mMainLayout, "An error occurred !", Snackbar.LENGTH_LONG)
                                    .setAction("Retry", view -> {
                                        recreate();
                                    });
                            snackbar.show();
                        }
                    });
                    Object[] params = { mServerUri, ownCloudCredentials, remotePath};
                    mListFolderTask.execute(params);

        });


        mTopicCloudRecyclerView.setAdapter(mAdapterCloud);
        mAdapterCloud.notifyDataSetChanged();
    }

    private void handleLoadedData(List resultList, int position) {
        List<RemoteFile> remoteList = new ArrayList<>(resultList);


        switch( status ){
            case DISPLAY_UNKNOWN_LANGUAGE:

                setupRecyclerView();
                mCloudFolderArrayList.clear();
                for( RemoteFile remoteFile : remoteList ){
                    String languageName = Utils.getNameFromPath( remoteFile.getRemotePath() );
                    LanguageElement languageElement = new LanguageElement( remoteFile, languageName , true );
                    mCloudFolderArrayList.add( languageElement );
                }
                status = Status.DISPLAY_KNOWN_LANGUAGE;
                break;

            case DISPLAY_KNOWN_LANGUAGE:

                mCloudFolderArrayList.clear();
                for( RemoteFile remoteFile : remoteList ){
                    String languageName = Utils.getNameFromPath( remoteFile.getRemotePath() );
                    LanguageElement languageElement = new LanguageElement( remoteFile, languageName , false );
                    mCloudFolderArrayList.add( languageElement );
                }
                mTopicCloudRecyclerView.removeAllViews();
                mAdapterCloud.notifyItemChanged(position);
                mAdapterCloud.notifyDataSetChanged();
                status = Status.DISPLAY_TOPICS;
                break;

            case DISPLAY_TOPICS:

                Utils.deleteFolder(mTemporaryDirectory);

                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading topics. Please wait...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);
                mProgressDialog.show();

                mFilesToDownLoad = remoteList.size()*2;
                mProgressDialog.setMax(mFilesToDownLoad);
                for (int topicNumber = 0; topicNumber < remoteList.size(); topicNumber++) {
                    RemoteFile remoteFile = remoteList.get(topicNumber);
                    diveIntoTopicFolderAndDownloadNecessaryFiles( remoteFile.getRemotePath() );
                }
                break;
        }

    }



    private void addOrRemoveTopicToDownloadList(AdapterTopic.TopicViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        Topic topic = mTopicCloudArrayList.get(position);

        if (viewHolder.mIconImageView.getVisibility() == View.INVISIBLE) {
            viewHolder.mIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_file_save));
            viewHolder.mIconImageView.setVisibility(View.VISIBLE);
            viewHolder.mBaseView.setBackgroundColor(Color.LTGRAY);
            File topicFullPathFile = new File(topic.getImageFileString().toLowerCase()).getParentFile();
            String topicCloudPath = topicFullPathFile.toString().replaceAll(".+/" + Constants.TemporaryFolder, "");
            mTopicDirectoriesToDownload.put(topic.getName(), new File(topicCloudPath));

        } else {

            viewHolder.mIconImageView.setVisibility(View.INVISIBLE);
            viewHolder.mBaseView.setBackgroundColor(Color.TRANSPARENT);
            mTopicDirectoriesToDownload.remove(topic.getName());
        }

        mAdapterCloud.notifyItemChanged(position);
    }

    private void removeTopicFromCloud(AdapterTopic.TopicViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        Topic topic = mTopicCloudArrayList.get(position);


        if (viewHolder.mIconImageView.getVisibility() == View.INVISIBLE) {

            mUpDownloadFab.setImageResource( R.drawable.ic_delete_forever );

            viewHolder.mIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_forever));
            viewHolder.mIconImageView.setVisibility(View.VISIBLE);
            viewHolder.mBaseView.setBackgroundColor(Color.LTGRAY);

            File topicFullPathFile = new File(topic.getImageFileString().toLowerCase()).getParentFile();
            String topicCloudPath = topicFullPathFile.toString().replaceAll(".+/" + Constants.TemporaryFolder, "");

            Date systemDate = Calendar.getInstance().getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

            SharedPreferences prefs = getSharedPreferences(Constants.SharedPreference, Context.MODE_PRIVATE);
            String timeStampOfLastRemoval = prefs.getString(Constants.TimeStampOfLastRemove, null);


            int hoursSinceLastTopicRemoval = 0;
            try {
                Date dateOfLastTopicRemoval = simpleDateFormat.parse( timeStampOfLastRemoval );
                long milliSeconds = Math.abs( systemDate.getTime() - dateOfLastTopicRemoval.getTime() );
                hoursSinceLastTopicRemoval = (int) (milliSeconds /(1000 * 60 * 60));
            } catch (Exception exception ){
                Log.e(Constants.TAG," Exception when parsing time of last removal : " + exception.getMessage() );
            }

            if ( hoursSinceLastTopicRemoval >= 24 || timeStampOfLastRemoval == null ){
                Snackbar snackbar = Snackbar
                        .make(mMainLayout, "Do you really want to remove that topic from cloud storage?", Snackbar.LENGTH_LONG)
                        .setAction("Remove", view -> {
                            mRemoveTask = new RemoveFileAsyncTask(this, result -> {
                                mRemoveTask = null;
                                if( result.isSuccess() ){

                                    mAdapterTopicCloud.remove( position );
                                    String systemDateString = simpleDateFormat.format( systemDate );
                                    SharedPreferences.Editor prefEditor = prefs.edit();
                                    prefEditor.putString(Constants.TimeStampOfLastRemove, systemDateString );
                                    prefEditor.apply();
                                } else {
                                    handleError(result, "Failed to remove topic!");
                                }
                                mUpDownloadFab.setImageResource(R.drawable.ic_cloud_upload);
                            });
                            Object[] params = { mServerUri, ownCloudCredentials, new File( topicCloudPath )};
                            mRemoveTask.execute(params);
                        });

                snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if (event == DISMISS_EVENT_TIMEOUT){
                            mUpDownloadFab.setImageResource(R.drawable.ic_cloud_upload);
                        }
                    }
                });
                snackbar.show();
            } else {
                int hoursToWait = 24 - hoursSinceLastTopicRemoval;
                Snackbar.make(mMainLayout, "You have to wait " + hoursToWait + " hour until you can remove that topic!", Snackbar.LENGTH_LONG ).show();
                viewHolder.mIconImageView.setVisibility(View.INVISIBLE);
                viewHolder.mBaseView.setBackgroundColor(Color.TRANSPARENT);
                mUpDownloadFab.setImageResource(R.drawable.ic_cloud_upload);
            }


        } else {

            viewHolder.mIconImageView.setVisibility(View.INVISIBLE);
            viewHolder.mBaseView.setBackgroundColor(Color.TRANSPARENT);
            mUpDownloadFab.setImageResource(R.drawable.ic_cloud_upload);
        }

        mAdapterTopicCloud.notifyItemChanged(position);
        mAdapterTopicCloud.notifyDataSetChanged();

    }


    private void onSetupCloudRecyclerViewError() {
        if (mTopicCloudArrayList == null) {
            mTopicCloudArrayList = new ArrayList<>();
            mTopicCloudArrayList.add(new Topic(getString(R.string.CloudNotAvaiable), getString(R.string.InternetConnectionOn), ""));
        }
    }

    private void handleError(RemoteOperationResult result, String message ){
        Log.e(Constants.TAG, message + result.toString() );
        Snackbar snackbar = Snackbar
                .make(mMainLayout, message, Snackbar.LENGTH_LONG)
                .setAction("Retry", view -> {
                    recreate();
                });
        snackbar.show();
    }




}
