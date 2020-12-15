package com.example.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/4 12:54
 * 内容   :启动页面
 * 版本: 1.0
 */
public class LauncherActivity extends AppCompatActivity {
    private ViewPager viewpager;
    private LinearLayout ll_point_group;
    private Button bt_launcher;
    private ArrayList<ImageView> imageViews;
    private static final String Tag = "轮播图";//
    private int preposition = 0;//上一次高亮现实的位置
    //准备数据
    private final int[] imageIds = {
            R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
        String isAutoLogin = sp.getString("phonenumber", null);
        if (isAutoLogin != null) {//如果登陆成功过直接进主页
            Intent intent = new Intent(LauncherActivity.this, indexActivity.class);
            startActivity(intent);
            this.finish();
        }
        initView();
    }

    public void initView() {
        viewpager = findViewById(R.id.viewpager_launcher);//实例化ViewPager
        ll_point_group = findViewById(R.id.ll_point_group_launcher);//实例化LinearLayout
        bt_launcher = findViewById(R.id.bt_launcher);//实例化LinearLayout
        bt_launcher.setVisibility(View.GONE);//一开始就隐藏
        imageViews = new ArrayList<>();
        for (int i = 0; i < imageIds.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setBackgroundResource(imageIds[i]);
            imageViews.add(imageView);//将图片全部添加进ArrayList中
            ImageView point = new ImageView(this);//创建圆点图片
            point.setBackgroundResource(R.drawable.point_selector);//将shape画好的圆点图片添加进去
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);//设置shape的高和宽
            if (i == 0) {
                point.setEnabled(true);//显示红色
            } else {
                point.setEnabled(false);//显示灰色
                params.leftMargin = 20;//设置shape的左边距
            }
            point.setLayoutParams(params);
            ll_point_group.addView(point);
        }
        viewpager.setAdapter(new mPagerAdapter());
        viewpager.addOnPageChangeListener(new mPageChangeListener());//给viewpager设置监听事件
        bt_launcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    /**
     * Viewpager适配器
     */
    class mPagerAdapter extends PagerAdapter {

        /**
         * @return 返回个数
         */
        @Override
        public int getCount() {//得到图片的总数
            return imageViews.size();
        }

        /**
         * 比较我们的view和object同一个实例
         *
         * @param view
         * @param object
         * @return
         */
        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            if (view == object) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * 相当于ListView的getView方法
         *
         * @param container viewPage本身
         * @param position  当前实例化位置
         * @return
         */
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            ImageView imageView = imageViews.get(position);
            container.addView(imageView);//添加到viewpage中
            return imageView;
        }

        /**
         * 销毁
         *
         * @param container
         * @param position
         * @param object
         */
        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            // super.destroyItem(container, position, object);
            container.removeView((View) object);
        }
    }

    /**
     * 监听事件
     */
    class mPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        /**
         * @param position 选中了哪一个
         */
        @Override
        public void onPageSelected(int position) {
            int realPosition = position % imageViews.size();//取模后的值
            ll_point_group.getChildAt(preposition).setEnabled(false);//把上一个高亮的设置默认-灰色
            ll_point_group.getChildAt(realPosition).setEnabled(true);//当前的设置为高亮-红色
            preposition = realPosition;
            if (position == imageIds.length - 1) {//数组下标是从0开始的
                bt_launcher.setVisibility(View.VISIBLE);//如果到了最后一张图就显示按钮
            } else {
                bt_launcher.setVisibility(View.GONE);//隐藏按钮，gone：不显示,且不保留所占的空间，也为了防止用户往回滑后就显示了
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}