package com.example.irvingwang.magic_gallery;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class View_adapter extends FragmentStatePagerAdapter {

    private String[] tabTitleArray;
    public View_adapter(FragmentManager fm, Context context,
                        List<Fragment> fragmentList, String[] tabTitleArray) {
        super(fm);
        this.tabTitleArray = tabTitleArray;
    }

    /* 重写与TabLayout配合 */

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitleArray[position % tabTitleArray.length];
    }

    @Override
    public Fragment getItem(int i) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
