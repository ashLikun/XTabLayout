package com.ashlikun.xtablayout.simple;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.ashlikun.xtablayout.XTabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements XTabLayout.OnTabSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initViewPager();
    }

    private void initViewPager() {
        List<String> titles = new ArrayList<>();
        titles.add("第一");
        titles.add("第二第二");
        titles.add("第三");
        final XTabLayout tabLayout = findViewById(R.id.tablayout);
        final XTabLayout tabLayout2 = findViewById(R.id.tabLayout2);
        tabLayout.addOnTabSelectedListener(this);
        //将TabLayout和ViewPager关联起来。
        tabLayout2.addTab(tabLayout2.newTab().setText("商品"));
        tabLayout2.addTab(tabLayout2.newTab().setText("详情"));
        tabLayout2.addTab(tabLayout2.newTab().setText("评价"));
        for (int i = 0; i < 3; i++) {
            tabLayout.addTab(tabLayout.newTab().setText("我是第" + i));
        }
       // tabLayout.setCurrentTabPosition(6);
    }

    @Override
    public void onTabSelected(XTabLayout.Tab tab) {
        Log.e("aaaa", "onTabSelected" + tab.getPosition());
    }

    @Override
    public void onTabUnselected(XTabLayout.Tab tab) {
        Log.e("aaaa", "onTabUnselected" + tab.getPosition());
    }

    @Override
    public void onTabReselected(XTabLayout.Tab tab) {
        Log.e("aaaa", "onTabReselected" + tab.getPosition());
    }
}
