package com.example.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.app.R;

import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {
    private ViewPager vp;
    private OrderViewModel mViewModel;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private SellFragment sellFragment;
    private FragmentAdapter mFragmentAdapter;
    private BuyFragment buyFragment;
    private TextView tv_Sell,tv_Buy;

    public static OrderFragment newInstance() {
        return new OrderFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.order_fragment, container, false);
        vp = view.findViewById(R.id.viewpager);
        tv_Sell = view.findViewById(R.id.tv_Sell);
        tv_Buy = view.findViewById(R.id.tv_Buy);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);
        // TODO: Use the ViewModel
        mFragments = new ArrayList<Fragment>();
        sellFragment = new SellFragment();
        buyFragment = new BuyFragment();

        mFragments.add(sellFragment);
        mFragments.add(buyFragment);
        FragmentManager fragmentManager = getFragmentManager();
        mFragmentAdapter = new FragmentAdapter(fragmentManager, mFragments);
//        vp.setOffscreenPageLimit(1);
        vp.setAdapter(mFragmentAdapter);
        vp.setCurrentItem(0);
        tv_Sell.setTextColor(Color.parseColor("#1ba0e1"));
        tv_Sell.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO 自动生成的方法存根
                vp.setCurrentItem(0, true);
            }
        });
        tv_Buy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO 自动生成的方法存根
                vp.setCurrentItem(1, true);
            }
        });
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // TODO 自动生成的方法存根
                ChangeTextColor(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO 自动生成的方法存根

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO 自动生成的方法存根

            }
        });
    }

    public class FragmentAdapter extends FragmentStatePagerAdapter {//继承这个防止viewpager只加载一次

        List<Fragment> fragments = new ArrayList<Fragment>();

        public FragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
            // TODO 自动生成的构造函数存根
        }

        @Override
        public Fragment getItem(int arg0) {
            // TODO 自动生成的方法存根
            return fragments.get(arg0);
        }

        @Override
        public int getCount() {
            // TODO 自动生成的方法存根
            return fragments.size();
        }


    }

    private void ChangeTextColor(int arg0) {
        if (arg0 == 0) {
            tv_Sell.setTextColor(Color.parseColor("#1E90FF"));
            tv_Buy.setTextColor(Color.parseColor("#000000"));
        }else if (arg0 == 1) {
            tv_Sell.setTextColor(Color.parseColor("#000000"));
            tv_Buy.setTextColor(Color.parseColor("#1E90FF"));
        }
    }
}

