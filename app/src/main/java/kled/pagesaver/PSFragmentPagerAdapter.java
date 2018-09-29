package kled.pagesaver;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Danielle on 2/24/17.
 */

public class PSFragmentPagerAdapter extends FragmentPagerAdapter{
    private ArrayList<Fragment> mFragmentList;

    public PSFragmentPagerAdapter(FragmentManager fragManager, ArrayList<Fragment> fragments) {
        super(fragManager);

        mFragmentList = fragments;
    }

    @Override
    public Fragment getItem(int pos){
        return mFragmentList.get(pos);
    }

    @Override
    public int getCount(){
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
        if(position == 0)
            return "Current Books";
        else if(position == 1)
            return "Previous Books";
        else if(position == 2)
            return "Search";
        else
            return null;
    }
}
