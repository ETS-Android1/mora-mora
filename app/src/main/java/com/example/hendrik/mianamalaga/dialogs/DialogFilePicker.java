package com.example.hendrik.mianamalaga.dialogs;


import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

public class DialogFilePicker  {

    private AlertDialog mDialog;
    private DialogFilePickerRecyclerView mFileRecyclerView;
    private OnFileSelectedListener mOnFileSelectedListener;

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
    }

    DialogFilePicker(@NonNull Context context){
        mDialog = new AlertDialog.Builder( context ).create();
        mFileRecyclerView = new DialogFilePickerRecyclerView(context);
        mDialog.setView(mFileRecyclerView);
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Select", (dialog, which) -> {
            if (mOnFileSelectedListener != null){
                File selectedFile = mFileRecyclerView.getSelectedFile();
                mOnFileSelectedListener.onFileSelected( selectedFile );
            }
            dialog.dismiss();
        });

        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> dialog.dismiss());
    }

    public void show(){
        mFileRecyclerView.go();
        mDialog.show();
    }


    /**
     * Listener to know which file/directory was selected
     *
     * @param onFileSelectedListener Instance of the Listener
     */
    public void setOnFileSelectedListener(OnFileSelectedListener onFileSelectedListener) {
        this.mOnFileSelectedListener = onFileSelectedListener;
    }
}
