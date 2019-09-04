package com.example.hendrik.mianamalaga;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;

public class DialogFilePickerRecyclerView extends RecyclerView {
    private DialogFilePickerAdapter mAdapter;

    DialogFilePickerRecyclerView(Context context){
        super(context);
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new DialogFilePickerAdapter(this);
    }

    public File getSelectedFile(){
        return mAdapter.getSelectedFile();
    }

    public void go(){
        setAdapter(mAdapter);
        mAdapter.go();
    }
}
