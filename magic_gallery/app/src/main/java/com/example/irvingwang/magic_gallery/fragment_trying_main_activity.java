package com.example.irvingwang.magic_gallery;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class fragment_trying_main_activity extends AppCompatActivity {


    private ArrayList<Fragment> fragmentList;
    private String[] tabtitle;
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_trying_main_activity);
        View_adapter adapter= new View_adapter(getSupportFragmentManager(),this,
                fragmentList,tabtitle);
        viewPager= (ViewPager)findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout=(TabLayout)findViewById(R.id.view_tab);
        tabLayout.setupWithViewPager(viewPager);
    }
}
