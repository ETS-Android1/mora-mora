package com.example.hendrik.mianamalaga;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;

import java.util.ArrayList;
import java.util.Locale;

public class AdapterCloudLanguage extends RecyclerView.Adapter<AdapterCloudLanguage.LanguageViewHolder>{

    public interface OnItemClickListener{
        void onItemClicked(int position, AdapterCloudLanguage.LanguageViewHolder viewHolder);
    }

    private ArrayList<LanguageElement> mLanguageElementArrayList;
    private Context mContext;
    public AdapterCloudLanguage.OnItemClickListener mOnItemClickListener;


    public void setOnItemClickListener(AdapterCloudLanguage.OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }



    public class LanguageViewHolder extends RecyclerView.ViewHolder{
        TextView mLanguageTextView;
        CardView mLanguageCardView;
        ImageView mImageView;
        View mBaseView;

        LanguageViewHolder(View baseView){
            super(baseView);
            mLanguageTextView = baseView.findViewById(R.id.language_cloud_listView_element_textView);
            mLanguageCardView = baseView.findViewById(R.id.language_cloud_listView_element_cardView);
            mImageView = baseView.findViewById(R.id.language_cloud_image_view);
            mBaseView = baseView;
        }

    }

    public AdapterCloudLanguage(Context context, ArrayList<LanguageElement> arrayList){
        mLanguageElementArrayList = arrayList;
        mContext = context;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View baseView = LayoutInflater.from( viewGroup.getContext()).inflate(R.layout.list_element_language_cloud, viewGroup, false);
        LanguageViewHolder viewHolder = new LanguageViewHolder( baseView );
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder viewHolder, int position) {
        LanguageElement languageElement = mLanguageElementArrayList.get(position);

        Locale languageLocale = new Locale( languageElement.getLanguage() );
        viewHolder.mLanguageTextView.setText( languageLocale.getDisplayName() );

        if( languageElement.isLanguageToLearn() ){
            viewHolder.mImageView.setImageResource(R.drawable.language_choice_background);
        } else {
            viewHolder.mImageView.setImageResource(R.drawable.taxi);
        }


        viewHolder.mBaseView.setOnClickListener(v -> mOnItemClickListener.onItemClicked(position, viewHolder ));
    }

    @Override
    public int getItemCount(){
        return mLanguageElementArrayList == null ? 0 : mLanguageElementArrayList.size();
    }



}
