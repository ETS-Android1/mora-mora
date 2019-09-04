package com.example.hendrik.mianamalaga;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;


public class DialogAddTopic extends AppCompatDialogFragment {

    Activity mCallingActivity;
    ImageButton mCameraButton;
    ImageButton mFileButton;
    TextView mPictureSourceTextView;
    EditText mTopicNameEditText;
    EditText mShortInfoEditText;
    File mChosenFile;
    int mPosition;


    public static DialogAddTopic newInstance(Topic topic ){
        DialogAddTopic dialogAddTopic = new DialogAddTopic();
        Bundle args = new Bundle();
        args.putString("Topic Name",topic.getName());
        args.putString("Image Name",topic.getImageName());
        args.putString("Image Path",topic.getImageFileString());
        args.putString("Short Info",topic.getShortInfo());
        args.putInt("Position",topic.getPosition());
        dialogAddTopic.setArguments(args);
        return dialogAddTopic;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String topicName = "";
        String shortInfo = "";
        mPosition = 0;

        if(getArguments() != null){
            topicName = getArguments().getString("Topic Name");
            shortInfo = getArguments().getString("Short Info");
            mPosition = getArguments().getInt("Position");
        }

        mCallingActivity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = mCallingActivity.getLayoutInflater();
        View baseView = inflater.inflate(R.layout.dialog_change_topic, null);

        mTopicNameEditText = baseView.findViewById(R.id.dialog_add_topic_name);
        mShortInfoEditText = baseView.findViewById(R.id.dialog_add_topic_short_info);
        mPictureSourceTextView = baseView.findViewById(R.id.dialog_add_topic_picture_source_text_view);
        mCameraButton = baseView.findViewById(R.id.dialog_add_topic_camera_button);
        mFileButton = baseView.findViewById(R.id.dialog_add_topic_file_button);

        mTopicNameEditText.setText(topicName);
        mShortInfoEditText.setText(shortInfo);

        if( checkCameraHardware( mCallingActivity )){
            mCameraButton.setVisibility(View.VISIBLE);
        } else {
            mCameraButton.setVisibility(View.INVISIBLE);
        }

        mCameraButton.setOnClickListener(v -> prepareCamera());
        mFileButton.setOnClickListener(v -> prepareFilePickerDialog());

        builder.setView(baseView);
        builder.setCancelable(true);

        builder.setPositiveButton(R.string.Change, (dialog, which) -> {
            Topic topic = new Topic();
            if( !mTopicNameEditText.getText().toString().isEmpty() ){                           //TODO remove all special characters of string
                topic.setName(mTopicNameEditText.getText().toString());
                topic.setShortInfo(mShortInfoEditText.getText().toString());
                ((ActivityTopicChoice)mCallingActivity).onDialogAddTopicPosClick(topic, mPosition, mChosenFile);
            } else {
                Toast.makeText(getContext(), "Topic Name must not be empty!", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            ;
        });

        return builder.create();
    }

    public void prepareCamera(){
        Topic topic = new Topic();
        topic.setName(mTopicNameEditText.getText().toString());
        topic.setShortInfo(mShortInfoEditText.getText().toString());
        ((ActivityTopicChoice)mCallingActivity).onDialogAddTopicCameraClick(topic, mPosition);
        dismiss();
    }

    public void prepareFilePickerDialog(){
        DialogFilePicker filePicker = new DialogFilePicker( Objects.requireNonNull(getContext()) );
        filePicker.setOnFileSelectedListener( this::onFileChosen );
        filePicker.show();
    }

    public void onFileChosen(File file){
        mChosenFile = file;
        mPictureSourceTextView.setText(file.getName());
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }
}
