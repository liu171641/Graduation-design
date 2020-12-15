package com.example.app.Fragment;


import androidx.lifecycle.ViewModelProviders;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.SimpleAdapter;
import android.widget.TextView;


import com.example.app.SellUpdateActivity;
import com.example.app.R;
import com.example.app.Sql.DBHelper;
import com.example.app.Tools.NetworkChangeReceiver;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * 作者 : 刘宇航
 * 邮箱 : 1716413010@qq.com
 * 日期  : 2020/11/5 13:39
 * 内容   :卖出Fragment
 * 版本: 1.0
 */
public class SellFragment extends Fragment {


    private HomeViewModel mViewModel;
    private ListView lv_shopping_list;
    private List<HashMap<String, Object>> list;
    private int id;
    private SimpleAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    adapter = new SimpleAdapter(getActivity(), list, R.layout.listviewstyle_3,
                            new String[]{"shoppingurl", "shoppingtitle", "shoppingprice", "shoppingamount"},
                            new int[]{R.id.iv_item_icon, R.id.tv_item_title, R.id.tv_item_price, R.id.tv_Details_count});
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
                    lv_shopping_list.setAdapter(adapter);
                    break;
                case 2:
                    lv_shopping_list.setAdapter(new lv_shopping_listAdapter());
                    break;
                case 3:
                    //点击商品后跳转，把id转过去
                    Intent intent = new Intent(getActivity(), SellUpdateActivity.class);
                    Log.e("TAG", "" + id);
                    intent.putExtra("id", id);//把id传过去
                    startActivity(intent);
                    break;
            }
        }
    };

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shopping_fragment, container, false);
        lv_shopping_list = view.findViewById(R.id.lv_shopping_list);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        // TODO: Use the ViewModel
        int netWorkStart = NetworkChangeReceiver.getNetWorkStart(getActivity());
        if (netWorkStart == 1) {
        } else {//如果有网
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sp = getActivity().getSharedPreferences("data", MODE_PRIVATE);
                    String UserPhoneNumber = sp.getString("phonenumber", "");
                    Object[] paObjects = new Object[]{UserPhoneNumber};
                    String sqlString = "select * from usershopping where  sellphonenumber=?";
                    list = DBHelper.getList(sqlString, paObjects);
                    Log.e("数据库", "查询成功");
                    handler.sendEmptyMessage(2);
                }
            }).start();
        }
        lv_shopping_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> map = list.get(i);
                id = Integer.parseInt(map.get("id").toString());
                handler.sendEmptyMessage(3);
            }
        });
    }

    class lv_shopping_listAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO 自动生成的方法存根
            return list.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO 自动生成的方法存根
            return list.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO 自动生成的方法存根
            return arg0;
        }

        @Override
        //关键点
        public View getView(final int arg0, View arg1, ViewGroup arg2) {
            // TODO 自动生成的方法存根
            ViewHolder holder;//创建一个类
            if (arg1 == null) {
                arg1 = LayoutInflater.from(getActivity()).
                        inflate(R.layout.listviewstyle_order, arg2, false);//实例化zz布局
                holder = new ViewHolder();//实例化类
                holder.tv_title = arg1.findViewById(R.id.tv_Order_Title);
                holder.tv_price = arg1.findViewById(R.id.tv_Order_Price);
                holder.iv_icon = arg1.findViewById(R.id.iv_Order_Icon);
                holder.tv_state = arg1.findViewById(R.id.tv_Order_State);
                arg1.setTag(holder);//添加标签
            } else {
                holder = (ViewHolder) arg1.getTag();
            }
            Map<String, Object> map = list.get(arg0);
            id = Integer.parseInt(map.get("id").toString());
            holder.tv_title.setText(map.get("shoppingtitle").toString());
            holder.tv_state.setText(map.get("state").toString());
            holder.tv_price.setText(map.get("shoppingprice").toString());
            Picasso.with(getActivity()).load(map.get("shoppingurl").toString().trim()).into(holder.iv_icon);//设置图片Url
            return arg1;
        }

        class ViewHolder {
            TextView tv_title, tv_price, tv_state;
            ImageView iv_icon;
        }
    }
}



