package com.example.hendrik.mianamalaga;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class AdapterLanguageChoice extends RecyclerView.Adapter<AdapterLanguageChoice.LanguageViewHolder> {

    public interface OnItemClickListener{
        void onItemClicked(int position, LanguageViewHolder viewHolder);
    }

    private ArrayList<Locale> mLanguageLocaleArrayList;
    public OnItemClickListener mOnItemClickListener;
    private int mListElementResourceId;

    public void setOnItemClickListener(AdapterLanguageChoice.OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View baseView = LayoutInflater.from(viewGroup.getContext()).inflate( mListElementResourceId , viewGroup, false);
        return  new LanguageViewHolder(baseView);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder languageViewHolder, int position) {
        String languageString = mLanguageLocaleArrayList.get(position).getDisplayLanguage();
        languageViewHolder.mLanguageTextView.setText(languageString);

        languageViewHolder.mBaseView.setOnClickListener(v -> mOnItemClickListener.onItemClicked(position, languageViewHolder));
    }

    @Override
    public int getItemCount() {
        return mLanguageLocaleArrayList == null ? 0 : mLanguageLocaleArrayList.size();
    }

    public class LanguageViewHolder extends RecyclerView.ViewHolder{

        View mBaseView;
        TextView mLanguageTextView;

        LanguageViewHolder(View baseView){
            super(baseView);
            mLanguageTextView = baseView.findViewById(R.id.language_listView_element_textView);
            mBaseView = baseView;
        }

    }

    public AdapterLanguageChoice( ArrayList<Locale> languageLocaleArrayList, int listElementResourceId) {
        mLanguageLocaleArrayList = languageLocaleArrayList;
        mListElementResourceId = listElementResourceId;
    }



}
