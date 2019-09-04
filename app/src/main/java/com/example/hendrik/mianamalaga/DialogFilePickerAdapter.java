package com.example.hendrik.mianamalaga;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class DialogFilePickerAdapter extends RecyclerView.Adapter<DialogFilePickerAdapter.FilePickerItemViewHolder> {

    private List<File> mFileList;
    private File mSelectedFile;
    private File mRootDirectoy;
    private Context mContext;
    private DialogFilePickerRecyclerView mFilePickerRecyclerView;

    DialogFilePickerAdapter(DialogFilePickerRecyclerView view){
        mFilePickerRecyclerView = view;
        mContext = view.getContext();
        mRootDirectoy = Environment.getExternalStoragePublicDirectory("");
        mFileList = new LinkedList<>();
    }

    class FilePickerItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView mImageView;
        TextView mFileName;
        RelativeLayout mBaseView;

        FilePickerItemViewHolder(View baseView){
            super(baseView);
            mImageView = baseView.findViewById(R.id.dialog_file_picker_element_icon);
            mFileName = baseView.findViewById(R.id.dialog_file_picker_element_file_name);
            mBaseView = baseView.findViewById(R.id.dialog_file_picker_element_layout);
            mBaseView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int position = getPosition();
            if( mFileList.get( position ) != null ){
                File file = mFileList.get(getPosition());

                if (file.isDirectory())
                    listFiles(file);
                else{
                    mBaseView.setBackgroundColor(Color.LTGRAY);
                    mSelectedFile = file;
                }

            }
        }
    }

    public void go(){
        listFiles(mRootDirectoy);
    }

    private void listFiles(File directory){

        File[] files = directory.listFiles();
        if (files != null) {
            mFileList = new LinkedList<>(Arrays.asList(files));
            Collections.sort(mFileList);

            if( directory.getParentFile() != mRootDirectoy)
                mFileList.add(0, directory.getParentFile());
        }
        notifyDataSetChanged();
        mFilePickerRecyclerView.scrollToPosition(0);
    }

    @NonNull
    @Override
    public FilePickerItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View baseView = LayoutInflater.from( viewGroup.getContext() ).inflate(R.layout.dialog_file_picker_recycler_view_element, viewGroup, false);
        return new FilePickerItemViewHolder(baseView);
    }

    @Override
    public void onBindViewHolder(@NonNull FilePickerItemViewHolder filePickerItemViewHolder, int position) {
        File file = mFileList.get(position);
        if (file != null){
            if(position == 0  && file != mRootDirectoy ){
                filePickerItemViewHolder.mFileName.setText("..");
                filePickerItemViewHolder.mImageView.setImageResource(R.drawable.ic_folder);         //TODO this could be a folder with an arrow or something else more clarifying
            } else {
                filePickerItemViewHolder.mFileName.setText(file.getName());
                if ( file.isFile() ) {

                    if (IOUtils.isImageFile(file.toString())) {

                        RequestOptions requestOptions = new RequestOptions()
                                .override(300, 300);

                        Glide.with(mContext)
                                .load(Uri.fromFile(file))
                                .apply(requestOptions)
                                .into(filePickerItemViewHolder.mImageView);

                    } else if (IOUtils.isVideoFile(file.toString())) {

                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(file.toString(),
                                MediaStore.Images.Thumbnails.MINI_KIND);


                        Glide.with(mContext)
                                .load(thumb)
                                .apply(new RequestOptions().override(300, 300))
                                .into(filePickerItemViewHolder.mImageView);

                    } else {
                        filePickerItemViewHolder.mImageView.setImageResource(R.drawable.ic_do_not_disturb);
                    }
                } else {
                    filePickerItemViewHolder.mImageView.setImageResource(R.drawable.ic_folder);
                }
            }
        }
    }


    @Override
    public int getItemCount(){
        return mFileList == null ? 0 : mFileList.size();
    }

    public File getSelectedFile(){
        return mSelectedFile;
    }


}


/*
File[] jpgfiles = f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file)
            {
                return (file.getPath().endsWith(".jpg")||file.getPath().endsWith(".jpeg"));
            }
});
 */