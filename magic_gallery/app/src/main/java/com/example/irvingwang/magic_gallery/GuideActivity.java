package com.example.irvingwang.magic_gallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity {
    private ViewPager viewPager;//需要ViewPaeger
    private PagerAdapter mAdapter;//需要PagerAdapter适配器
    private List<View> mViews=new ArrayList<>();//准备数据源
    private Button bt_home;//在ViewPager的最后一个页面设置一个按钮，用于点击跳转到MainActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_guide);
        initView();//调用
    }

    private void initView() {
        viewPager= (ViewPager) findViewById(R.id.view_pager);

        LayoutInflater inflater=LayoutInflater.from(this);//将每个xml文件转化为View
        View guideOne=inflater.inflate(R.layout.guidance01, null);//每个xml中就放置一个imageView
        View guideTwo=inflater.inflate(R.layout.guidance02,null);
        View guideThree=inflater.inflate(R.layout.guidance03,null);

        mViews.add(guideOne);//将view加入到list中
        mViews.add(guideTwo);
        mViews.add(guideThree);

        mAdapter=new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view=mViews.get(position);//初始化适配器，将view加到container中
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View view=mViews.get(position);
                container.removeView(view);//将view从container中移除
            }

            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view==object;//判断当前的view是我们需要的对象
            }
        };

        viewPager.setAdapter(mAdapter);

        bt_home= (Button) guideThree.findViewById(R.id.to_Main);
        bt_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GuideActivity.this,camera.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
