package com.example.hendrik.mianamalaga.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hendrik.mianamalaga.R;
import com.example.hendrik.mianamalaga.activities.ActivityConversation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Objects;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class DialogAddChangeMessage extends DialogFragment {

    Activity mCallingActivity;
    int mPosition;
    String[] mNativeMessages;
    String mTranslatedMessage;
    String mInfoText;
    EditText mChatMessageEditText;
    EditText mTranslatedMessageEditText;
    EditText mInfoTextEditText;
    ImageButton mCamButton;
    ImageButton mCamAppButton;
    ImageButton mMicButton;
    ImageButton mPictureButton;
    ImageButton mFileButton;
    TextView mCorrectAnswerTextView;
    FloatingActionButton mButtonA;
    FloatingActionButton mButtonB;
    FloatingActionButton mButtonC;
    RadioGroup mCorrectAnswerRadioGroup;
    File mChosenFile;
    boolean isAudioRecording = false;
    boolean mIsUser;


    public static DialogAddChangeMessage newInstance(String[] nativeMessages, String translatedMessage, String info, int position, boolean isUser) {
        DialogAddChangeMessage dialogAddChangeMessage = new DialogAddChangeMessage();
        Bundle args = new Bundle();
        args.putStringArray("native Messages", nativeMessages);
        args.putString("translated Message", translatedMessage);
        args.putString("Optional info",info);
        args.putInt("Position", position);
        args.putBoolean("isUser", isUser);
        dialogAddChangeMessage.setArguments(args);
        return dialogAddChangeMessage;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getArguments() != null) {
            mNativeMessages = getArguments().getStringArray("native Messages");
            mTranslatedMessage = getArguments().getString("translated Message");
            mInfoText = getArguments().getString("Optional info");
            mPosition = getArguments().getInt("Position");
            mIsUser = getArguments().getBoolean("isUser");
        }

        mCallingActivity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View baseView = inflater.inflate(R.layout.dialog_add_message, null);

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
                Toast.makeText(getActivity(), R.string.RecordingStopped, Toast.LENGTH_SHORT).show();
                ((ActivityConversation) mCallingActivity).recordAudioStop();
                dismiss();
            } else {
                mMicButton.setImageResource(R.drawable.ic_microphone_off);
                Toast.makeText(getActivity(), R.string.Recording, Toast.LENGTH_SHORT).show();
                ((ActivityConversation) mCallingActivity).recordAudio(mPosition);
                isAudioRecording = true;
            }

        });

        mButtonA = baseView.findViewById(R.id.dialog_add_change_message_fab_A);
        mButtonB = baseView.findViewById(R.id.dialog_add_change_message_fab_B);
        mButtonC = baseView.findViewById(R.id.dialog_add_change_message_fab_C);
        mCorrectAnswerRadioGroup = baseView.findViewById(R.id.dialog_add_change_message_radio_group_correct_answer);
        mCorrectAnswerTextView = baseView.findViewById(R.id.dialog_add_change_message_correct_answer_textview);

        if( mIsUser ) {

            if (mTranslatedMessage.startsWith("A")) mCorrectAnswerRadioGroup.check(R.id.radio_a);
            if (mTranslatedMessage.startsWith("B")) mCorrectAnswerRadioGroup.check(R.id.radio_b);
            if (mTranslatedMessage.startsWith("C")) mCorrectAnswerRadioGroup.check(R.id.radio_c);
            if (mTranslatedMessage.length() > 2)
                mTranslatedMessage = mTranslatedMessage.substring(2);

            mButtonA.setTag("ACTIVE");
            mButtonB.setTag("INACTIVE");
            mButtonC.setTag("INACTIVE");

            mCorrectAnswerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    mTranslatedMessageEditText.setVisibility(View.GONE);
                    switch (i) {
                        case R.id.radio_a:
                            //if (mButtonA.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) {
                            if( mButtonA.getTag().equals("ACTIVE") ){
                                mTranslatedMessageEditText.setVisibility(View.VISIBLE);
                            }
                            break;
                        case R.id.radio_b:
                            //if (mButtonB.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) {
                            if( mButtonB.getTag().equals("ACTIVE") ){
                                mTranslatedMessageEditText.setVisibility(View.VISIBLE);
                            }
                            break;
                        case R.id.radio_c:
                            //if (mButtonC.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) {
                            if( mButtonC.getTag().equals("ACTIVE") ){
                                mTranslatedMessageEditText.setVisibility(View.VISIBLE);
                            }
                            break;
                    }

                }
            });


            mButtonA.setOnClickListener(v -> {

                if (mCorrectAnswerRadioGroup.getCheckedRadioButtonId() == R.id.radio_a) {
                    mTranslatedMessageEditText.setVisibility(View.VISIBLE);
                } else {
                    mTranslatedMessageEditText.setVisibility(View.GONE);
                }

                if (mNativeMessages.length > 1) {
                    if (mButtonB.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) {
                        mNativeMessages[1] = mChatMessageEditText.getText().toString();
                    } else {
                        mNativeMessages[2] = mChatMessageEditText.getText().toString();
                    }
                    mTranslatedMessage = mTranslatedMessageEditText.getText().toString();
                    mChatMessageEditText.setText(mNativeMessages[0]);
                    mTranslatedMessageEditText.setText(mTranslatedMessage);
                }

                ViewCompat.setBackgroundTintList(mButtonA, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryColor));
                ViewCompat.setBackgroundTintList(mButtonB, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryLightColor));
                ViewCompat.setBackgroundTintList(mButtonC, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryLightColor));

                mButtonA.setTag("ACTIVE");
                mButtonB.setTag("INACTIVE");
                mButtonC.setTag("INACTIVE");
            });

            mButtonB.setOnClickListener(v -> {

                if (mCorrectAnswerRadioGroup.getCheckedRadioButtonId() == R.id.radio_b) {
                    mTranslatedMessageEditText.setVisibility(View.VISIBLE);
                } else {
                    mTranslatedMessageEditText.setVisibility(View.GONE);
                }

                if (mNativeMessages.length > 1) {
                    if (mButtonA.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) {
                        mNativeMessages[0] = mChatMessageEditText.getText().toString();
                    } else {
                        mNativeMessages[2] = mChatMessageEditText.getText().toString();
                    }
                    mTranslatedMessage = mTranslatedMessageEditText.getText().toString();
                    mChatMessageEditText.setText(mNativeMessages[1]);
                    mTranslatedMessageEditText.setText(mTranslatedMessage);
                }

                ViewCompat.setBackgroundTintList(mButtonA, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryLightColor));
                ViewCompat.setBackgroundTintList(mButtonB, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryColor));
                ViewCompat.setBackgroundTintList(mButtonC, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryLightColor));

                mButtonA.setTag("INACTIVE");
                mButtonB.setTag("ACTIVE");
                mButtonC.setTag("INACTIVE");

            });

            mButtonC.setOnClickListener(v -> {

                if (mCorrectAnswerRadioGroup.getCheckedRadioButtonId() == R.id.radio_c) {
                    mTranslatedMessageEditText.setVisibility(View.VISIBLE);
                } else {
                    mTranslatedMessageEditText.setVisibility(View.GONE);
                }

                if (mNativeMessages.length > 1) {
                    if (mButtonA.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) {
                        mNativeMessages[0] = mChatMessageEditText.getText().toString();
                    } else {
                        mNativeMessages[1] = mChatMessageEditText.getText().toString();
                    }
                    mTranslatedMessage = mTranslatedMessageEditText.getText().toString();
                    mChatMessageEditText.setText(mNativeMessages[2]);
                    mTranslatedMessageEditText.setText(mTranslatedMessage);
                }
                ViewCompat.setBackgroundTintList(mButtonA, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryLightColor));
                ViewCompat.setBackgroundTintList(mButtonB, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryLightColor));
                ViewCompat.setBackgroundTintList(mButtonC, ContextCompat.getColorStateList(mCallingActivity, R.color.secondaryColor));

                mButtonA.setTag("INACTIVE");
                mButtonB.setTag("INACTIVE");
                mButtonC.setTag("ACTIVE");

            });
        } else {
            mCorrectAnswerRadioGroup.setVisibility( View.GONE );
            mCorrectAnswerTextView.setVisibility( View.GONE );
            mButtonA.hide();
            mButtonB.hide();
            mButtonC.hide();
        }

        mChatMessageEditText = baseView.findViewById(R.id.dialog_add_change_message_message);
        mChatMessageEditText.setText(mNativeMessages[0]);

        mTranslatedMessageEditText = baseView.findViewById(R.id.dialog_add_change_message_message_translation);
        mTranslatedMessageEditText.setText(mTranslatedMessage);

        mInfoTextEditText = baseView.findViewById(R.id.dialog_add_change_message_editText_info);
        mInfoTextEditText.setText( mInfoText );

        if ( mIsUser && !(mCorrectAnswerRadioGroup.getCheckedRadioButtonId() == R.id.radio_a) ) mTranslatedMessageEditText.setVisibility(View.GONE);

        builder.setView(baseView);
        builder.setCancelable(true);

        builder.setPositiveButton(R.string.Apply, (dialog, which) -> getMessageAndSave());
        builder.setNegativeButton(R.string.CancelButton, (dialog, which) -> {
            ((ActivityConversation) mCallingActivity).recordAudioStop();
        });

        return builder.create();
    }

    private void getMessageAndSave() {
        ((ActivityConversation) mCallingActivity).recordAudioStop();
        mTranslatedMessage = mTranslatedMessageEditText.getText().toString();

        switch ( mCorrectAnswerRadioGroup.getCheckedRadioButtonId() ){
            case R.id.radio_a:
                mTranslatedMessage = "A:" + mTranslatedMessage;
                break;
            case R.id.radio_b:
                mTranslatedMessage = "B:" + mTranslatedMessage;
                break;
            case R.id.radio_c:
                mTranslatedMessage = "C:" + mTranslatedMessage;
                break;
        }

        if( mIsUser ){
            String nativeMessage = mChatMessageEditText.getText().toString();
            if (mButtonA.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) mNativeMessages[0] = nativeMessage;
            if (mButtonB.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) mNativeMessages[1] = nativeMessage;
            if (mButtonC.getBackgroundTintList().getDefaultColor() == getResources().getColor(R.color.secondaryColor)) mNativeMessages[2] = nativeMessage;
        } else {
            mNativeMessages[0] = mChatMessageEditText.getText().toString();
        }

        String infoText = mInfoTextEditText.getText().toString();
        ((ActivityConversation) mCallingActivity).onDialogAddChangeMessagePosClick( mNativeMessages, mTranslatedMessage, infoText, mChosenFile, mPosition);
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