package com.example.hendrik.mianamalaga.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hendrik.mianamalaga.R;

import java.util.ArrayList;


public class AdapterDictionaryTranslation extends RecyclerView.Adapter<AdapterDictionaryTranslation.TranslationsViewHolder> {


    private ArrayList<String> mTranslationsArrayList;


    @NonNull
    @Override
    public AdapterDictionaryTranslation.TranslationsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View baseView = LayoutInflater.from(viewGroup.getContext()).inflate( R.layout.list_element_dict_translation , viewGroup, false);
        return  new AdapterDictionaryTranslation.TranslationsViewHolder(baseView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDictionaryTranslation.TranslationsViewHolder languageViewHolder, int position) {
        String translationString = mTranslationsArrayList.get(position);
        languageViewHolder.mLanguageTextView.setText( translationString );

    }

    @Override
    public int getItemCount() {
        return mTranslationsArrayList == null ? 0 : mTranslationsArrayList.size();
    }

    public class TranslationsViewHolder extends RecyclerView.ViewHolder{

        View mBaseView;
        TextView mLanguageTextView;

        TranslationsViewHolder(View baseView){
            super(baseView);
            mLanguageTextView = baseView.findViewById(R.id.dictionary_listView_element_textView);
            mBaseView = baseView;
        }

    }

    /**
     * Public constructor of the adapter used to store and get the translations
     * @param translationsArrayList List of Strings - Translations
     * @param listElementResourceId
     */

    public AdapterDictionaryTranslation( ArrayList<String> translationsArrayList ) {
        mTranslationsArrayList = translationsArrayList;
    }



}