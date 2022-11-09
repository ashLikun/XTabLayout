package com.ashlikun.xtablayout.simple;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ashlikun.xtablayout.DividerDrawable;

/**
 * @author　　: 李坤
 * 创建时间: 2018/11/8 10:37
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class Fragment1 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment1, container, false);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        DividerDrawable dividerDrawable = new DividerDrawable(getContext());
        dividerDrawable.setDividerSize(50, 2100);
        dividerDrawable.setColor(0xffffff00);
        dividerDrawable.setGravity(DividerDrawable.CENTER);
        view.setDividerDrawable(dividerDrawable);
        return view;
    }
}
