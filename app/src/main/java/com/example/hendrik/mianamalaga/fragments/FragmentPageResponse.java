package com.example.hendrik.mianamalaga;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hendrik.mianamalaga.activities.ActivityConversation;


public class FragmentPageResponse extends Fragment implements OnUpdateResponseInFragment {

   public static final String ARG_OBJECT = "object";
   private View mRootView;
   private int mPosition;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate){
       mRootView = inflater.inflate(R.layout.bubble, container, false);
       Bundle args = getArguments();
       mPosition = args.getInt(ARG_OBJECT);
       String key = Integer.toString(mPosition);
       ((TextView)mRootView.findViewById(R.id.bubble_text_view)).setText(args.getString(key));

       Activity mainActivity = getActivity();
       ((ActivityConversation)mainActivity).setOnUpdateResponsesInterface(this, mPosition);

       return mRootView;
   }


    @Override
    public void updateResponses(String response) {
        ((TextView)mRootView.findViewById(R.id.bubble_text_view)).setText(response);
    }
}
