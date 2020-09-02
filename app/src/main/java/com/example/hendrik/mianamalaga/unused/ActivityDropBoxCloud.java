package com.example.hendrik.mianamalaga.unused;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.hendrik.mianamalaga.utilities.Utils;
import com.example.hendrik.mianamalaga.adapter.AdapterCloudLanguage;
import com.example.hendrik.mianamalaga.adapter.AdapterTopic;
import com.example.hendrik.mianamalaga.Constants;
import com.example.hendrik.mianamalaga.container.LanguageElement;
import com.example.hendrik.mianamalaga.R;
import com.example.hendrik.mianamalaga.container.Topic;
import com.example.hendrik.mianamalaga.activities.ActivityLogIntoCloud;
import com.example.hendrik.mianamalaga.activities.ActivityTopicChoice;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
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
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


//TODO all topics were downloaded instead of only the selected one
//TODO it must be possible to delete topics too

public class ActivityDropBoxCloud extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);
        mIsDownLoadMode = true;
        mMainLayout = findViewById(R.id.activity_cloud_main_layout);
        setupToolbar();
        getIntents();
        setupNavigationDrawer();

        if (!tokenExists()) {                                                                            //No token Back to LoginActivity
            Intent intent = new Intent(ActivityDropBoxCloud.this, ActivityLogIntoCloud.class);
            intent.putExtra(Constants.FullTemporaryDirectory, mTemporaryDirectory.toString());
            intent.putExtra(Constants.MoraMora, mApplicationDirectory.toString());
            startActivity(intent);
        }

        mAccessToken = retrieveAccessToken();
        getUserAccount();
        setupFabs();

        if (!mIsDownLoadMode)
            switchToUploadMode();

        loadAvailableLanguages("");
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
        mProgressDialog.setMessage("Downloading files. Please wait...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        for (File topicDirectory : mTopicDirectoriesToDownload.values()) {

            new TaskListFolder(DropBoxClient.getClient(mAccessToken), new TaskListFolder.Callback() {
                @Override
                public void onDataLoaded(ListFolderResult result) {
                    downloadCompleteFolder(result);
                }

                @Override
                public void onError(Exception e) {
                    mProgressDialog.dismiss();
                }
            }).execute(topicDirectory.toString().toLowerCase());
        }
    }

    void downloadCompleteFolder(ListFolderResult result) {
        List<Metadata> list = result.getEntries();
        mTotalSize = 0;
        mAccomplishedSize = 0;

        for (Metadata listEntry : list) {
            if (listEntry instanceof FileMetadata) {
                if (!listEntry.getName().equals(Constants.InfoFileNameNew) && !listEntry.getName().equals(Constants.TopicPictureFileName))
                    mTotalSize += ((FileMetadata) listEntry).getSize();
            }
        }

        for (Metadata listEntry : list) {
            FileMetadata listFile = null;

            if (listEntry instanceof FileMetadata) {
                listFile = (FileMetadata) listEntry;
            }

            if (listFile != null) {
                if (!listFile.getName().equals(Constants.InfoFileNameNew) && !listFile.getName().equals(Constants.TopicPictureFileName)) {

                    new TaskDownloadFile(DropBoxClient.getClient(mAccessToken), mTemporaryDirectory, 0, new TaskDownloadFile.Callback() {  //fileNumber is not used - file length is used instead
                        @Override
                        public void onDownloadComplete(File result, int fileNumber) {
                            publishProgress(result.length());
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(ActivityDropBoxCloud.this,
                                    "An error occurred during download: " + listEntry.getName(),
                                    Toast.LENGTH_SHORT)
                                    .show();
                            mProgressDialog.dismiss();
                        }
                    }).execute(listFile);
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
        if (progress >= 99) {
            mTotalSize = 0;
            mProgressDialog.dismiss();
            Toast.makeText(ActivityDropBoxCloud.this,
                    "Download complete!",
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


        Intent intent = new Intent(ActivityDropBoxCloud.this, ActivityTopicChoice.class);
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
        new TaskUserAccount(DropBoxClient.getClient(mAccessToken), new TaskUserAccount.TaskDelegate() {
            @Override
            public void onAccountReceived(FullAccount account) {
                Log.d("User", account.getEmail());                                             //Print account's info
                Log.d("User", account.getName().getDisplayName());
                Log.d("User", account.getAccountType().name());
                updateUI(account);
            }

            @Override
            public void onError(Exception error) {
                Log.d(Constants.TAG, "Error receiving account details.");
            }
        }).execute();
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

        mTotalSize = calculateTotalFileSize( mTopicDirectoriesToUpload );

        if( mTotalSize > 0 ){
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
                File[] topicFiles = topicDirectory.listFiles();
                if (topicFiles != null) {
                    for (File file : topicFiles) {
                        if (file != null) {
                            try {
                            } catch ( SecurityException exception ){
                                Log.e(Constants.TAG,"Can't access file size!");
                            }

                            Log.e(Constants.TAG, "Uploading file: " + file.getAbsolutePath());
                            new TaskUpLoad(DropBoxClient.getClient(mAccessToken), file, getApplicationContext(), result -> {
                                long fileSize =  result.length();
                                publishProgress( fileSize );
                            }).execute();
                        }
                    }
                }
            }
        }

        //TODO make a refresh of the cloud topic list!

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
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.activity_cloud_drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.toolbar_login:
                    recreate();
                    mDrawerLayout.closeDrawers();
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

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mTopicCloudRecyclerView.setLayoutManager(mLayoutManager);

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
        new TaskListFolder(DropBoxClient.getClient(mAccessToken), new TaskListFolder.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                List<Metadata> fileList = result.getEntries();
                setupCloudRecyclerView(fileList);
            }

            @Override
            public void onError(Exception e) {
                Log.e(Constants.TAG, "Failed to list folder " + path + " \n Exception: ", e);
                Snackbar snackbar = Snackbar
                        .make(mMainLayout, "An error occurred !", Snackbar.LENGTH_LONG)
                        .setAction("Retry", view -> {
                            recreate();
                        });
                snackbar.show();
            }
        }).execute(path);
    }


    protected void loadTopicFilesFromCloudFolder(String path) {
        Utils.deleteFolder(mTemporaryDirectory);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Downloading topics. Please wait...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();

        Log.e(Constants.TAG, "Download path: " + path);
        new TaskListFolder(DropBoxClient.getClient(mAccessToken), new TaskListFolder.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                List<Metadata> list = result.getEntries();
                mFilesToDownLoad = list.size() * 2;
                mProgressDialog.setMax(mFilesToDownLoad);
                for (int topicNumber = 0; topicNumber < list.size(); topicNumber++) {
                    Metadata listEntry = list.get(topicNumber);
                    if (listEntry instanceof FolderMetadata) {
                        diveIntoTopicFolderAndDownloadNecessaryFiles(listEntry.getPathLower());
                    }
                }

            }

            @Override
            public void onError(Exception e) {
                mProgressDialog.dismiss();
                Log.e(Constants.TAG, "Failed to list folder.", e);
                Snackbar snackbar = Snackbar
                        .make(mMainLayout, "An error occurred !", Snackbar.LENGTH_LONG)
                        .setAction("Retry", view -> {
                            recreate();
                        });
                snackbar.show();
                onSetupCloudRecyclerViewError();
            }
        }).execute(path);
    }

    private void diveIntoTopicFolderAndDownloadNecessaryFiles(String pathLower) {

        new TaskListFolder(DropBoxClient.getClient(mAccessToken), new TaskListFolder.Callback() {
            @Override
            public void onDataLoaded(ListFolderResult result) {
                List<Metadata> list = result.getEntries();
                for (int topicNumber = 0; topicNumber < list.size(); topicNumber++) {
                    Metadata listEntry = list.get(topicNumber);
                    FileMetadata listFile = null;
                    if (listEntry instanceof FileMetadata) {
                        listFile = (FileMetadata) listEntry;
                    }

                    if (listFile != null) {
                        if (listFile.getName().equals(Constants.InfoFileNameNew) || listFile.getName().equals(Constants.TopicPictureFileName)) {
                            File destinationDirectory = mTemporaryDirectory;
                            new TaskDownloadFile(DropBoxClient.getClient(mAccessToken), destinationDirectory, topicNumber, new TaskDownloadFile.Callback() {
                                @Override
                                public void onDownloadComplete(File result, int fileNumber) {
                                    mFilesDownloaded++;
                                    mProgressDialog.setProgress(mFilesDownloaded);
                                    if (mFilesDownloaded == mFilesToDownLoad)
                                        mProgressDialog.dismiss();
                                    Log.e(Constants.TAG, "Download finished :" + mFilesDownloaded + " of " + mFilesToDownLoad);
                                    if (mFilesDownloaded == mFilesToDownLoad) {
                                        setupTopicCloudRecyclerView();
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    onSetupCloudRecyclerViewError();
                                }
                            }).execute(listFile);
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {

            }
        }).execute(pathLower);
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

        mTopicCloudRecyclerView.removeAllViews();
        mTopicCloudRecyclerView.setAdapter(mAdapterTopicCloud);
        mAdapterTopicCloud.notifyDataSetChanged();

    }


    private void setupCloudRecyclerView(List<Metadata> fileList) {
        mCloudFolderArrayList = new ArrayList<>();

        for (Metadata metadata : fileList) {
            mCloudFolderArrayList.add(new LanguageElement(metadata, metadata.getName(), true));
        }


        mLayoutManager = new LinearLayoutManager(this);
        mTopicCloudRecyclerView = findViewById(R.id.cloud_cloud_recyclerView);
        mTopicCloudRecyclerView.setLayoutManager(mLayoutManager);
        mTopicCloudRecyclerView.setHasFixedSize(true);

        mAdapterCloud = new AdapterCloudLanguage(this, mCloudFolderArrayList);
        mAdapterCloud.setOnItemClickListener((position, viewHolder) -> {
            LanguageElement languageElement = mCloudFolderArrayList.get(position);
            if (languageElement.getMetadata() instanceof FolderMetadata) {

                new TaskListFolder(DropBoxClient.getClient(mAccessToken), new TaskListFolder.Callback() {
                    @Override
                    public void onDataLoaded(ListFolderResult result) {
                        updateCloudRecyclerView(result.getEntries(), position + 1);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(Constants.TAG, "Failed to list folder " + languageElement.getMetadata().getPathLower() + " \n Exception: ", e);
                        Snackbar snackbar = Snackbar
                                .make(mMainLayout, "An error occurred !", Snackbar.LENGTH_LONG)
                                .setAction("Retry", view -> {
                                    recreate();
                                });
                        snackbar.show();
                    }
                }).execute(languageElement.getMetadata().getPathLower());
            }
        });


        mTopicCloudRecyclerView.setAdapter(mAdapterCloud);
        mAdapterCloud.notifyDataSetChanged();
    }

    private void updateCloudRecyclerView(List<Metadata> fileList, int position) {

        mCloudFolderArrayList.clear();
        mTopicCloudRecyclerView.removeAllViews();

        mAdapterCloud.notifyDataSetChanged();

        for (Metadata metadata : fileList) {
            mCloudFolderArrayList.add(new LanguageElement(metadata, metadata.getName(), false));
            mAdapterCloud.notifyItemChanged(position);
        }

        mAdapterCloud.notifyDataSetChanged();
        mAdapterCloud.setOnItemClickListener((position1, viewHolder) -> {
            LanguageElement languageElement = mCloudFolderArrayList.get(position1);
            if (languageElement.getMetadata() instanceof FolderMetadata) {
                loadTopicFilesFromCloudFolder(languageElement.getMetadata().getPathLower());
            }

        });
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


    private void onSetupCloudRecyclerViewError() {
        if (mTopicCloudArrayList == null) {
            mTopicCloudArrayList = new ArrayList<>();
            mTopicCloudArrayList.add(new Topic(getString(R.string.CloudNotAvaiable), getString(R.string.InternetConnectionOn), ""));
        }
    }




}