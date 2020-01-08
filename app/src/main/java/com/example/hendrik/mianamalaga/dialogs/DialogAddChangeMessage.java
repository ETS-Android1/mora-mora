package com.example.hendrik.mianamalaga;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.hendrik.mianamalaga.activities.ActivityConversation;

import java.io.File;
import java.util.Objects;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class DialogAddChangeMessage extends DialogFragment {

    Activity mCallingActivity;
    int mPosition;
    String[] mNativeMessages;
    String[] mTranslatedMessages;
    EditText mChatMessageEditText;
    EditText mTranslatedMessageEditText;
    ImageButton mCamButton;
    ImageButton mCamAppButton;
    ImageButton mMicButton;
    ImageButton mPictureButton;
    ImageButton mFileButton;
    File mChosenFile;
    boolean isAudioRecording = false;


    public static DialogAddChangeMessage newInstance(String[] nativeMessages, String[] translatedMessages, int position) {
        DialogAddChangeMessage dialogAddChangeMessage = new DialogAddChangeMessage();
        Bundle args = new Bundle();
        args.putStringArray("native Messages", nativeMessages);
        args.putStringArray("translated Messages", translatedMessages);
        args.putInt("Position", position);
        dialogAddChangeMessage.setArguments(args);
        return dialogAddChangeMessage;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            mNativeMessages = getArguments().getStringArray("native Messages");
            mTranslatedMessages = getArguments().getStringArray("translated Messages");
            mPosition = getArguments().getInt("Position");
        }

        mCallingActivity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View baseView = inflater.inflate(R.layout.dialog_add_message, null);

        mChatMessageEditText = baseView.findViewById(R.id.dialog_add_change_message_message);
        mChatMessageEditText.setText(mNativeMessages[0]);

        mTranslatedMessageEditText = baseView.findViewById(R.id.dialog_add_change_message_message_translation);
        mTranslatedMessageEditText.setText(mTranslatedMessages[0]);

        mCamButton = baseView.findViewById(R.id.dialog_add_change_message_camera);
        mMicButton = baseView.findViewById(R.id.dialog_add_change_message_mic_on);
        mPictureButton = baseView.findViewById(R.id.dialog_add_change_message_picture);
        mFileButton = baseView.findViewById(R.id.dialog_add_change_message_file);
        mCamAppButton = baseView.findViewById(R.id.dialog_add_change_message_camera_app);

        mFileButton.setOnClickListener(v -> prepareFilePickerDialog());

        if (checkCameraHardware(mCallingActivity)) {
            mCamButton.setOnClickListener(v -> {
                getMessageAndSave();
                ((ActivityConversation) mCallingActivity).prepareCamera(mPosition, MEDIA_TYPE_VIDEO);
                dismiss();
            });

            mPictureButton.setOnClickListener(v -> {
                getMessageAndSave();
                ((ActivityConversation) mCallingActivity).prepareCamera(mPosition, MEDIA_TYPE_IMAGE);
                dismiss();
            });

            mCamAppButton.setOnClickListener(v -> {
                ((ActivityConversation) mCallingActivity).recordAudioStop();
                getMessageAndSave();
                ((ActivityConversation) mCallingActivity).dispatchTakeVideoIntent(mPosition);
                dismiss();
            });

            mPictureButton.setVisibility(View.VISIBLE);
            mCamButton.setVisibility(View.VISIBLE);
            mCamAppButton.setVisibility(View.VISIBLE);
        } else {
            mPictureButton.setVisibility(View.GONE);
            mCamButton.setVisibility(View.GONE);
            mCamAppButton.setVisibility(View.GONE);
        }

        mMicButton.setImageResource(R.drawable.ic_microphone);
        mMicButton.setOnClickListener(v -> {
            if (isAudioRecording) {
                mMicButton.setImageResource(R.drawable.ic_microphone);
                isAudioRecording = false;
                getMessageAndSave();
                Toast.makeText(getActivity(), "Recording stopped!", Toast.LENGTH_SHORT).show();
                ((ActivityConversation) mCallingActivity).recordAudioStop();
                dismiss();
            } else {
                mMicButton.setImageResource(R.drawable.ic_microphone_off);
                Toast.makeText(getActivity(), "Recording!", Toast.LENGTH_SHORT).show();
                ((ActivityConversation) mCallingActivity).recordAudio(mPosition);
                isAudioRecording = true;
            }

        });


        builder.setView(baseView);
        builder.setCancelable(true);

        builder.setPositiveButton("Apply", (dialog, which) -> getMessageAndSave());
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            ((ActivityConversation) mCallingActivity).recordAudioStop();
        });

        return builder.create();
    }

    private void getMessageAndSave() {
        ((ActivityConversation) mCallingActivity).recordAudioStop();
        String nativeMessage = mChatMessageEditText.getText().toString();
        String translatedMessage = mTranslatedMessageEditText.getText().toString();
        ((ActivityConversation) mCallingActivity).onDialogAddChangeMessagePosClick(nativeMessage, translatedMessage, mPosition, mChosenFile);
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    public void prepareFilePickerDialog() {
        DialogFilePicker filePicker = new DialogFilePicker(Objects.requireNonNull(getContext()));
        filePicker.setOnFileSelectedListener(this::onFileChosen);
        filePicker.show();
    }

    public void onFileChosen(File file) {
        mChosenFile = file;
    }

}