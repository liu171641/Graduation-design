package com.example.app.Fragment;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.example.app.ProductDetailsActivity;
import com.example.app.R;
import com.example.app.Sql.DBHelper;
import com.example.app.Tools.NetworkChangeReceiver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :商品Fragment
 * 版本: 1.0
 */
public class HomeFragment extends Fragment {
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    adapter = new SimpleAdapter(getActivity(), list, R.layout.listviewstyle,
                            new String[]{"url", "title", "price"},
                            new int[]{R.id.iv_Shopping_Icon, R.id.tv_Shopping_title, R.id.tv_Shopping_Price});
                    adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                        public boolean setViewValue(View view, Object data,
                                                    String textRepresentation) {
                            if (view instanceof ImageView) {
                                URL url = null;
                                try {
                                    url = new URL((String) data);
                                    ImageView iv = (ImageView) view;
                                    iv.setImageBitmap(BitmapFactory.decodeStream(url.openStream()));
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            } else
                                return false;
                        }
                    });
                    ListView.setAdapter(adapter);
                    break;
                case 3:
                    Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                    break;
            }
        }
    };
    private SellViewModel mViewModel;
    private SliderLayout sliderLayout;
    private ListView ListView;
    private SearchView searchView_home;
    SimpleAdapter adapter;
    //准备数据
    List<HashMap<String, Object>> list;
    private int id;

    public static SellFragment newInstance() {
        return new SellFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        sliderLayout = view.findViewById(R.id.slider);
        ListView = view.findViewById(R.id.ListView);
        searchView_home = view.findViewById(R.id.searchView_home);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SellViewModel.class);
        // TODO: Use the ViewModel
        if (android.os.Build.VERSION.SDK_INT > 9) {
            //NetworkOnMainThreadException类是从Android API 11开始增加的异常类（Android 3.0），从Android3.0开始网络访问的代码就不能写在主线程中了
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        int netWorkStart = NetworkChangeReceiver.getNetWorkStart(getActivity());
        if (netWorkStart == 1) {

        } else {//如果有网
            List<String> imageUrls = new ArrayList<>();
            final List<String> descriptions = new ArrayList<>();
            imageUrls.add("https://img.alicdn.com/imgextra/i4/6000000002343/O1CN01R93eCl1TB8cPDNEc8_!!6000000002343-0-octopus.jpg");
            imageUrls.add("https://img.alicdn.com/simba/img/TB1_GzjbQ9E3KVjSZFGSuw19XXa.jpg");
            imageUrls.add("https://img.alicdn.com/imgextra/i3/6000000006395/O1CN01DgRinE1x6xTfiZ5gA_!!6000000006395-0-octopus.jpg");
            imageUrls.add("https://aecpm.alicdn.com/simba/img/TB1JpAlgtTfau8jSZFwSut1mVXa.jpg");
            descriptions.add("冬季男装不止5折");
            descriptions.add("蓝牙耳机降价了");
            descriptions.add("苏宁爆款手机家电");
            descriptions.add("2020新款电动车");
            for (int i = 0; i < imageUrls.size(); i++) {
                //新建展示View，并且添加到SliderLayout
                TextSliderView tsv = new TextSliderView(getActivity());
                tsv.image(imageUrls.get(i)).description(descriptions.get(i));
                final int finalI = i;
                tsv.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {//监听事件
                    @Override
                    public void onSliderClick(BaseSliderView slider) {//轮播图监听事件
                        Toast.makeText(getActivity(), descriptions.get(finalI), Toast.LENGTH_SHORT).show();
                    }
                });
                sliderLayout.addSlider(tsv);
            }
            //设置指示器的位置
            sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            //设置图片的切换效果
            sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
            sliderLayout.setCustomAnimation(new DescriptionAnimation()); //添加textView动画特效
            //设置切换时长2000ms
            sliderLayout.setDuration(2000);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this) {
                        String sqlString = "select * from commodity where amount >= '1'";
                        list = DBHelper.getList(sqlString, null);
                        Log.e("数据库", "查询成功");
                        handler.sendEmptyMessage(1);
                    }
                }
            }).start();

            ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Map<String, Object> map = list.get(i);
                    id = Integer.parseInt(map.get("id").toString());
                    handler.sendEmptyMessage(3);
                }
            });
            //搜索框监听事件
            searchView_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                //搜索框按钮点击时
                @Override
                public boolean onQueryTextSubmit(String s) {
//                    String sqlString = "SELECT * FROM commodity WHERE title LIKE '%?%' "+s;
//                    list = DBHelper.getList(sqlString, null);
//                    Log.e("数据库", "查询成功");
//                    handler.sendEmptyMessage(2);
                    list.clear();
                    Log.e("搜索框点击", "" + s);
                    String sqlString = "SELECT * FROM commodity WHERE title LIKE  '%"+s+"%'";
//                    String sqlString = "select * from commodity where amount >= '1'";
                    list = DBHelper.getList(sqlString, null);
                    adapter.notifyDataSetChanged();
                    Log.e("搜索框点击", "" + list);
                    handler.sendEmptyMessage(1);
                    return true;
                }
                //搜索框文字输入时
                @Override
                public boolean onQueryTextChange(String s) {
                    if ("".equals(s)) {//如果没有输入
                        sliderLayout.setVisibility(View.VISIBLE);//显示
                        list.clear();
                        Log.e("搜索框点击", "" + s);
                        String sqlString = "select * from commodity where amount >= '1'";
                        list = DBHelper.getList(sqlString, null);
                        adapter.notifyDataSetChanged();
                        Log.e("搜索框点击", "" + list);
                        handler.sendEmptyMessage(1);
                    } else {
                        sliderLayout.setVisibility(View.GONE);//隐藏
                        list.clear();
                        Log.e("搜索框点击", "" + s);
                        String sqlString = "SELECT * FROM commodity WHERE title LIKE  '%"+s+"%'";
//                    String sqlString = "select * from commodity where amount >= '1'";
                        list = DBHelper.getList(sqlString, null);
                        adapter.notifyDataSetChanged();
                        Log.e("搜索框点击", "" + list);
                        handler.sendEmptyMessage(1);
                    }
                    Log.e("搜索框", "" + s);
                    return true;
                }
            });
        }
    }

}