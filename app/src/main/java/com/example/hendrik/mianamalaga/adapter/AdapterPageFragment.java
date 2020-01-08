package com.example.hendrik.mianamalaga;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class AdapterPageFragment extends FragmentPagerAdapter{

    private final List<FragmentPageResponse> mFragmentList;
    private final List<String> mResponseStringList;
    private final FragmentManager mFragmentManager;



        public AdapterPageFragment(FragmentManager fm) {
            super(fm);
            this.mFragmentManager = fm;
            this.mFragmentList = new ArrayList<>();
            this.mResponseStringList = new ArrayList<>();

        }

        @Override
        public Fragment getItem(int position){

            FragmentPageResponse fragment = mFragmentList.get(position);
            Bundle args = new Bundle();
            args.putInt(FragmentPageResponse.ARG_OBJECT, position);
            args.putString(Integer.toString(position), mResponseStringList.get(position));
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount(){
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position){
            return "OBJECT" + (position + 1);
        }


        public void addFragment(FragmentPageResponse fragment, String response) {
            mFragmentList.add(fragment);
            mResponseStringList.add(response);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            super.destroyItem(container, position, object);
            // remove destroyed Fragment from FragmentManager
            if(position <= getCount()) {
                FragmentTransaction trans = mFragmentManager.beginTransaction();
                trans.remove((FragmentPageResponse) object);
                trans.commit();
            }
        }

        public void destroyAllItems(ViewPager pager) {
            int mPosition = pager.getCurrentItem();
            int mPositionMax = pager.getCurrentItem()+1;
            if (mFragmentList.size() > 0 && mPosition < mFragmentList.size()) {
                     if (mPosition > 0) {
                         mPosition--;
                     }

                    for (int i = mPosition; i < mPositionMax; i++) {
                    //for (int i = 0; i < getCount(); i++) {
                        try {
                            Object objectobject = this.instantiateItem( pager, i);
                            destroyItem(pager, i, objectobject);
                        } catch (Exception e) {
                            Log.i("mianamal", "no more Fragment in FragmentPagerAdapter");
                        }
                    }
            }
        }

}
