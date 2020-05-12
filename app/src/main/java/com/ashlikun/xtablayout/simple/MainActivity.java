package com.ashlikun.xtablayout.simple;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.ashlikun.xtablayout.XTabLayout;
import com.ashlikun.xtablayout.XTabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewPager();
    }

    private void initViewPager() {
        List<String> titles = new ArrayList<>();
        titles.add("全部");
        titles.add("待付款");
        titles.add("待使用");
        titles.add("待评价");
        titles.add("已完成");
        titles.add("已取消");
        titles.add("退款/售后");
        new XTabLayoutMediator(null, null, null);

        for (int i = 0; i < titles.size(); i++) {
            if (i % 2 == 0) {
                fragments.add(new Fragment2());
            } else {
                fragments.add(new Fragment1());
            }
        }
        FragmentAdapter adatper = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adatper);
        viewPager.setOffscreenPageLimit(4);
        //将TabLayout和ViewPager关联起来。
        final XTabLayout tabLayout = (XTabLayout) findViewById(R.id.tabLayout2);
        tabLayout.setupWithViewPager(viewPager);

    }
}
