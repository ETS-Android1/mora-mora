package com.example.hendrik.mianamalaga;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;


public class AdapterChatArray extends RecyclerView.Adapter<AdapterChatArray.ChatMessageViewHolder> {

    public interface OnMediaPlaybackCompleteListener {
        void onMediaPlaybackComplete();
    }


    public interface OnItemLongClickListener {
        boolean onItemLongClicked(int position, ChatMessageViewHolder viewHolder);
    }

    private ArrayList<ChatContent> mChatContentArrayList;
    private Context mContext;
    private String mResourceDirectory;
    private int mChatContentCount;
    public OnMediaPlaybackCompleteListener mOnMediaPlaybackCompleteListener;
    public OnItemLongClickListener mOnItemCLongClickListener;

    public void setOnMediaPlaybackCompleteListener(OnMediaPlaybackCompleteListener listener) {
        this.mOnMediaPlaybackCompleteListener = listener;
    }

    public void removeOnMediaPlaybackCompleteListener(OnMediaPlaybackCompleteListener listener) {
        this.mOnMediaPlaybackCompleteListener = null;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemCLongClickListener = listener;
    }



    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {

        View mBaseView;
        private TextView mAgentTextView;
        private TextView mUserTextView;
        CardView mAgentCardView;
        CardView mUserCardView;
        ImageView mAgentImageView;
        ImageView mUserImageView;

        ChatMessageViewHolder(View baseView) {
            super(baseView);
            mAgentTextView = baseView.findViewById(R.id.text_message_agent);
            mUserTextView = baseView.findViewById(R.id.text_message_user);
            mAgentCardView = baseView.findViewById(R.id.agent_card_view);
            mUserCardView = baseView.findViewById(R.id.user_card_view);
            mAgentImageView = baseView.findViewById(R.id.agent_image_view);
            mUserImageView = baseView.findViewById(R.id.user_image_view);
            mBaseView = baseView;
        }

    }

    public AdapterChatArray(Context context, ArrayList<ChatContent> arrayList, String resourceDirectoryPath) {
        mChatContentArrayList = arrayList;
        mContext = context;
        mResourceDirectory = resourceDirectoryPath;
        mChatContentCount = 0;
    }


    @Override
    public AdapterChatArray.ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {             // Create new views (invoked by the layout manager)
        View baseView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_element_conversation, parent, false);
        return new ChatMessageViewHolder(baseView);
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder viewHolder, int position) {

        ChatContent chatContent = mChatContentArrayList.get(position);

        if (chatContent.isUser()) {
            viewHolder.mAgentTextView.setVisibility(View.GONE);
            viewHolder.mAgentCardView.setVisibility(View.GONE);
            viewHolder.mUserCardView.setVisibility(View.VISIBLE);
            viewHolder.mUserTextView.setVisibility(View.VISIBLE);
            viewHolder.mUserTextView.setText(chatContent.getNativeMessages()[0]);

            String mediaPath = new File(mResourceDirectory, chatContent.getMediaFileNames()[0]).getPath();
   //         viewHolder.mUserVideoView.setVideoPath(mediaPath);
            viewHolder.mBaseView.setTag(chatContent.getMediaFileNames());

            if (IOUtils.isVideoFile(mediaPath)) {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mediaPath,
                        MediaStore.Images.Thumbnails.MINI_KIND);

                Glide.with(mContext)
                        .load(thumb)
                        .apply(new RequestOptions().override(300, 300))
                        .into(viewHolder.mUserImageView);
            } else if (IOUtils.isImageFile( chatContent.getImageFileName() )){

                File imageFile = new File( mResourceDirectory, chatContent.getImageFileName());

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                        .skipMemoryCache(true)
                        .override(300, 300);

                Glide.with(mContext)
                        .load(Uri.fromFile( imageFile ))
                        .apply(requestOptions)
                        .into(viewHolder.mUserImageView);
            }


        } else {
            viewHolder.mUserTextView.setVisibility(View.GONE);
            viewHolder.mUserCardView.setVisibility(View.GONE);
            viewHolder.mAgentCardView.setVisibility(View.VISIBLE);
            viewHolder.mAgentTextView.setVisibility(View.VISIBLE);
            viewHolder.mAgentTextView.setText(chatContent.getNativeMessages()[0]);

            String mediaPath = new File(mResourceDirectory, chatContent.getMediaFileNames()[0]).getPath();
            viewHolder.mBaseView.setTag(chatContent.getMediaFileNames());

            if (IOUtils.isVideoFile( mediaPath )) {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mediaPath,
                        MediaStore.Images.Thumbnails.MINI_KIND);


                Glide.with(mContext)
                        .load(thumb)
                        .apply(new RequestOptions().override(300, 300))
                        .into(viewHolder.mAgentImageView);

            } else if (IOUtils.isImageFile( chatContent.getImageFileName() )){

                File imageFile = new File( mResourceDirectory, chatContent.getImageFileName());

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                        .skipMemoryCache(true)
                        .override(300, 300);

                Glide.with(mContext)
                        .load(Uri.fromFile( imageFile ))
                        .apply(requestOptions)
                        .into(viewHolder.mAgentImageView);
            }

        }

        viewHolder.mAgentTextView.setText(chatContent.getNativeMessages()[0]);
        viewHolder.mAgentCardView.setOnClickListener(v -> {
            String mediaPlath = ((String[])viewHolder.mBaseView.getTag())[0];
            Rect startBounds = new Rect();
            viewHolder.mAgentImageView.getGlobalVisibleRect(startBounds);
            ((ActivityConversation)mContext).onPlayMediaButtonPressed( mediaPlath, startBounds, false );
        });

        viewHolder.mAgentTextView.setOnClickListener(v -> {
            String messageInBubble = viewHolder.mAgentTextView.getText().toString();
            String[] translatedMessages = mChatContentArrayList.get(position).getTranslatedMessages();
            String[] nativeMessages = mChatContentArrayList.get(position).getNativeMessages();

            for (int index = 0; index < translatedMessages.length; index++) {
                if (translatedMessages[index].equals(messageInBubble))
                    viewHolder.mAgentTextView.setText(nativeMessages[index]);
                else if (nativeMessages[index].equals(messageInBubble))
                    viewHolder.mAgentTextView.setText(translatedMessages[index]);
            }

        });

        viewHolder.mUserCardView.setOnClickListener(v -> {
            String mediaPlath = ((String[])viewHolder.mBaseView.getTag())[0];
            Rect startBounds = new Rect();
            viewHolder.mUserImageView.getGlobalVisibleRect(startBounds);
            ((ActivityConversation)mContext).onPlayMediaButtonPressed( mediaPlath, startBounds, false );
        });

        viewHolder.mUserTextView.setOnClickListener(v -> {
            String messageInBubble = viewHolder.mUserTextView.getText().toString();
            String[] translatedMessages = mChatContentArrayList.get(position).getTranslatedMessages();
            String[] nativeMessages = mChatContentArrayList.get(position).getNativeMessages();

            for (int index = 0; index < translatedMessages.length; index++) {
                if (translatedMessages[index].equals(messageInBubble))
                    viewHolder.mUserTextView.setText(nativeMessages[index]);
                else if (nativeMessages[index].equals(messageInBubble))
                    viewHolder.mUserTextView.setText(translatedMessages[index]);
            }

        });


        viewHolder.mAgentTextView.setOnLongClickListener(v -> {
            mOnItemCLongClickListener.onItemLongClicked(position, viewHolder);
            return true;
        });

        viewHolder.mUserTextView.setOnLongClickListener(v -> {
            mOnItemCLongClickListener.onItemLongClicked(position, viewHolder);
            return true;
        });


    }

    @Override
    public int getItemCount() {
        if (mChatContentArrayList.size() == 0)
            return mChatContentArrayList.size();
        else
            return mChatContentCount;
    }

    public void stepForward() {
        if (mChatContentCount < mChatContentArrayList.size()) {
            mChatContentCount++;
            notifyDataSetChanged();
        }
    }

    public void setItemCount(int itemCount) {
        if (itemCount < mChatContentArrayList.size())
            mChatContentCount = itemCount;
        else
            mChatContentCount = mChatContentArrayList.size();

        notifyDataSetChanged();
    }


    public void remove(int position) {
        mChatContentCount--;
        mChatContentArrayList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(ChatContent chatContent) {
        mChatContentArrayList.add(chatContent);
        notifyDataSetChanged();
    }


}




